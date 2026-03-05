package com.hospital.bus;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.DispensingDAO;
import com.hospital.dao.MedicalRecordDAO;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Dispensing;
import com.hospital.model.DispensingItem;
import com.hospital.model.Prescription;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Business logic layer cho phát thuốc (Dispensing).
 *
 * Flow:
 * 1. Dược sĩ chọn đơn thuốc chờ phát (Prescription CONFIRMED)
 * 2. Hệ thống hiển thị chi tiết: thuốc, SL yêu cầu, tồn kho, SL phát, giá
 * 3. Dược sĩ điều chỉnh SL phát (nếu tồn kho không đủ)
 * 4. Xác nhận → tạo Dispensing record + trừ kho trong TRANSACTION
 */
public class DispensingBUS {

    private static final Logger LOGGER = Logger.getLogger(DispensingBUS.class.getName());

    private final DispensingDAO dispensingDAO = new DispensingDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    /**
     * Lấy danh sách đơn thuốc chờ phát.
     */
    public List<Dispensing> getPendingDispensings() {
        return dispensingDAO.getPendingDispensings();
    }

    /**
     * Lấy chi tiết thuốc cần phát cho 1 đơn thuốc.
     * Tự động điều chỉnh dispensedQuantity nếu tồn kho không đủ.
     */
    public List<DispensingItem> getItemsForPrescription(long prescriptionId) {
        return dispensingDAO.getItemsForPrescription(prescriptionId);
    }

    /**
     * Kiểm tra đơn thuốc đã phát chưa.
     */
    public Dispensing getByPrescriptionId(long prescriptionId) {
        return dispensingDAO.getByPrescriptionId(prescriptionId);
    }

    /**
     * Xử lý phát thuốc — TRANSACTION:
     * 1. Validate dữ liệu
     * 2. Tạo Dispensing + DispensingItem + trừ kho
     * 3. Cập nhật Prescription status → DISPENSED
     * 4. Cập nhật MedicalRecord status → DISPENSED
     *
     * @param prescriptionId ID đơn thuốc
     * @param patientId      ID bệnh nhân
     * @param items          Danh sách thuốc phát (đã điều chỉnh SL)
     * @param pharmacistId   ID dược sĩ (user_id)
     * @param notes          Ghi chú
     * @return dispensing_id vừa tạo
     */
    public long processDispensing(long prescriptionId, long patientId,
                                  List<DispensingItem> items,
                                  Long pharmacistId, String notes) {

        // Validate
        if (items == null || items.isEmpty()) {
            throw new BusinessException("Danh sách thuốc phát không được rỗng");
        }

        // Check đã phát chưa
        Dispensing existing = dispensingDAO.getByPrescriptionId(prescriptionId);
        if (existing != null && (Dispensing.STATUS_DISPENSED.equals(existing.getStatus())
                || Dispensing.STATUS_PARTIAL.equals(existing.getStatus()))) {
            throw new BusinessException("Đơn thuốc #" + prescriptionId + " đã được phát trước đó");
        }

        // Tính tổng tiền và xác định trạng thái
        BigDecimal totalAmount = BigDecimal.ZERO;
        boolean isPartial = false;
        List<String> warnings = new ArrayList<>();

        for (DispensingItem item : items) {
            if (item.getDispensedQuantity() < 0) {
                throw new BusinessException("Số lượng phát không được âm: " + item.getMedicineName());
            }
            if (item.getDispensedQuantity() > item.getRequestedQuantity()) {
                throw new BusinessException("Số lượng phát vượt quá yêu cầu: " + item.getMedicineName());
            }
            if (item.getDispensedQuantity() < item.getRequestedQuantity()) {
                isPartial = true;
                warnings.add(String.format("- %s: Yêu cầu %d, Phát %d",
                        item.getMedicineName(), item.getRequestedQuantity(), item.getDispensedQuantity()));
            }
            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getDispensedQuantity()));
            totalAmount = totalAmount.add(lineTotal);
        }

        // Transaction
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getTransactionalConnection();
            DispensingDAO txDao = new DispensingDAO(conn);
            PrescriptionDAO txPrescDAO = new PrescriptionDAO(conn);
            MedicalRecordDAO txRecordDAO = new MedicalRecordDAO(conn);

            // 1. Tạo Dispensing record
            Dispensing dispensing = new Dispensing(prescriptionId, patientId);
            dispensing.setDispensedBy(pharmacistId);
            dispensing.setStatus(isPartial ? Dispensing.STATUS_PARTIAL : Dispensing.STATUS_DISPENSED);
            dispensing.setTotalAmount(totalAmount);
            dispensing.setNotes(notes);

            long dispensingId = txDao.createDispensing(dispensing, items);

            // 2. Cập nhật Prescription status → DISPENSED
            txPrescDAO.updateStatus(prescriptionId, Prescription.STATUS_DISPENSED);

            // 3. Cập nhật MedicalRecord status → DISPENSED
            try {
                // Lấy record_id từ Prescription
                Prescription presc = txPrescDAO.findById(prescriptionId);
                if (presc != null) {
                    txRecordDAO.updateStatus(presc.getMedicalRecordId(), "DISPENSED");
                }
            } catch (Exception ex) {
                LOGGER.warning("Không thể cập nhật trạng thái bệnh án: " + ex.getMessage());
            }

            conn.commit();

            if (!warnings.isEmpty()) {
                LOGGER.info("Phát thuốc một phần cho đơn #" + prescriptionId + ":\n"
                        + String.join("\n", warnings));
            }

            return dispensingId;

        } catch (BusinessException e) {
            rollback(conn);
            throw e;
        } catch (DataAccessException e) {
            rollback(conn);
            throw e;
        } catch (Exception e) {
            rollback(conn);
            LOGGER.log(Level.SEVERE, "Lỗi phát thuốc đơn #" + prescriptionId, e);
            throw new BusinessException("Lỗi phát thuốc: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Lấy tổng tiền thuốc đã phát cho một bệnh án.
     */
    public BigDecimal getDispensingTotalByRecordId(long recordId) {
        return dispensingDAO.getDispensingTotalByRecordId(recordId);
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ignored) {}
        }
    }
}
