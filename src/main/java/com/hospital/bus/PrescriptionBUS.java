package com.hospital.bus;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.MedicineDAO;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionDetail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Business logic cho đơn thuốc (Prescription).
 */
public class PrescriptionBUS {

    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();

    /**
     * Tạo đơn thuốc + chi tiết (transaction).
     */
    public long createPrescription(long medicalRecordId, List<PrescriptionDetail> details) {
        if (details == null || details.isEmpty()) {
            throw new BusinessException("Đơn thuốc không được rỗng");
        }

        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getTransactionalConnection();

            PrescriptionDAO pDao = new PrescriptionDAO(conn);

            // 1. Tạo Prescription
            Prescription prescription = new Prescription(medicalRecordId);
            long prescriptionId = pDao.insertPrescription(prescription);

            // 2. Insert từng PrescriptionDetail
            for (PrescriptionDetail d : details) {
                if (d.getQuantity() <= 0) {
                    throw new BusinessException("Số lượng thuốc phải > 0");
                }
                d.setPrescriptionId(prescriptionId);
                pDao.insertDetail(d);
            }

            conn.commit();
            return prescriptionId;

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            throw new DataAccessException("Lỗi tạo đơn thuốc", e);
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * Lấy danh sách đơn thuốc chờ phát.
     */
    public List<Prescription> getPendingPrescriptions() {
        return prescriptionDAO.findPendingPrescriptions();
    }

    /**
     * Lấy chi tiết đơn thuốc theo prescriptionId.
     */
    public List<PrescriptionDetail> getDetails(long prescriptionId) {
        return prescriptionDAO.findDetailsByPrescriptionId(prescriptionId);
    }

    /**
     * Lấy danh sách đơn thuốc theo medical_record_id.
     */
    public List<Prescription> getByMedicalRecordId(long medicalRecordId) {
        return prescriptionDAO.findByMedicalRecordId(medicalRecordId);
    }
}
