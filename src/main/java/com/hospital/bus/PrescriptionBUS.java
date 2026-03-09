package com.hospital.bus;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.DrugInteractionDAO;
import com.hospital.dao.MedicineDAO;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.model.DrugInteraction;
import com.hospital.model.Medicine;
import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionDetail;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Business logic cho đơn thuốc (Prescription).
 */
public class PrescriptionBUS {

    private static final Logger LOGGER = Logger.getLogger(PrescriptionBUS.class.getName());

    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final DrugInteractionDAO interactionDAO = new DrugInteractionDAO();

    /**
     * Tạo đơn thuốc + chi tiết (transaction).
     * Kiểm tra tồn kho trước khi tạo.
     */
    public long createPrescription(long medicalRecordId, List<PrescriptionDetail> details) {
        if (details == null || details.isEmpty()) {
            throw new BusinessException("Đơn thuốc không được rỗng");
        }

        // Validate + fill unit_price from Medicine + check stock
        double totalAmount = 0;
        List<String> stockWarnings = new ArrayList<>();

        for (PrescriptionDetail d : details) {
            if (d.getQuantity() <= 0) {
                throw new BusinessException("Số lượng thuốc phải > 0");
            }
            Medicine med = medicineDAO.findById(d.getMedicineId());
            if (med == null) {
                throw new BusinessException("Không tìm thấy thuốc ID=" + d.getMedicineId());
            }
            // Auto-fill unit_price from Medicine sell_price if not set
            if (d.getUnitPrice() <= 0) {
                d.setUnitPrice(med.getSellPrice());
            }
            d.setMedicineName(med.getMedicineName());

            // Check stock
            if (med.getStockQty() < d.getQuantity()) {
                stockWarnings.add(String.format("- %s: Cần %d, Tồn kho %d",
                        med.getMedicineName(), d.getQuantity(), med.getStockQty()));
            }

            totalAmount += d.getLineTotal();
        }

        if (!stockWarnings.isEmpty()) {
            throw new BusinessException("CẢNH BÁO TỒN KHO:\n" + String.join("\n", stockWarnings)
                    + "\n\nVui lòng điều chỉnh số lượng hoặc liên hệ kho dược.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getTransactionalConnection();

            PrescriptionDAO pDao = new PrescriptionDAO(conn);

            // 1. Tạo Prescription
            Prescription prescription = new Prescription(medicalRecordId);
            prescription.setTotalAmount(totalAmount);
            long prescriptionId = pDao.insertPrescription(prescription);

            // 2. Insert từng PrescriptionDetail
            for (PrescriptionDetail d : details) {
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
     * Kiểm tra dị ứng cho bệnh nhân với danh sách thuốc.
     * So sánh PatientAllergy.allergen_name với MedicineIngredient.ingredient_name.
     *
     * @return Danh sách cảnh báo dị ứng (empty nếu không có)
     */
    public List<String> checkAllergies(long patientId, List<Integer> medicineIds) {
        List<String> warnings = new ArrayList<>();
        if (medicineIds == null || medicineIds.isEmpty()) return warnings;

        String placeholders = String.join(",", medicineIds.stream().map(id -> "?").toList());
        String sql = """
            SELECT DISTINCT pa.allergen_name, pa.severity, pa.reaction,
                   mi.ingredient_name, m.medicine_name
            FROM PatientAllergy pa
            JOIN MedicineIngredient mi ON LOWER(pa.allergen_name) = LOWER(mi.ingredient_name)
            JOIN Medicine m ON mi.medicine_id = m.medicine_id
            WHERE pa.patient_id = ? AND mi.medicine_id IN (%s)
            """.formatted(placeholders);

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, patientId);
            int idx = 2;
            for (int medId : medicineIds) {
                ps.setInt(idx++, medId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String warning = String.format("⚠ DỊ ỨNG: Thuốc '%s' chứa '%s' — BN dị ứng '%s' (Mức độ: %s, Phản ứng: %s)",
                            rs.getString("medicine_name"),
                            rs.getString("ingredient_name"),
                            rs.getString("allergen_name"),
                            rs.getString("severity"),
                            rs.getString("reaction") != null ? rs.getString("reaction") : "N/A");
                    warnings.add(warning);
                }
            }
        } catch (SQLException e) {
            // Log but don't block prescription
            LOGGER.warning("Lỗi kiểm tra dị ứng: " + e.getMessage());
        }
        return warnings;
    }

    /**
     * Kiểm tra tương tác thuốc-thuốc trong danh sách thuốc.
     *
     * @param medicineIds danh sách ID thuốc cần kiểm tra
     * @return danh sách cảnh báo tương tác (empty nếu không có)
     */
    public List<String> checkDrugInteractions(List<Integer> medicineIds) {
        List<String> warnings = new ArrayList<>();
        if (medicineIds == null || medicineIds.size() < 2) return warnings;

        try {
            List<DrugInteraction> interactions = interactionDAO.findInteractions(medicineIds);
            for (DrugInteraction di : interactions) {
                String icon = switch (di.getSeverity()) {
                    case DrugInteraction.LEVEL_CONTRAINDICATED -> "🚫";
                    case DrugInteraction.LEVEL_SEVERE -> "⛔";
                    case DrugInteraction.LEVEL_MODERATE -> "⚠";
                    default -> "ℹ";
                };
                String warning = String.format("%s TƯƠNG TÁC THUỐC [%s]: '%s' ↔ '%s' — %s",
                        icon, di.getSeverityDisplay(),
                        di.getMedicineName1(), di.getMedicineName2(),
                        di.getDescription());
                if (di.getRecommendation() != null && !di.getRecommendation().isEmpty()) {
                    warning += "\n   → Khuyến nghị: " + di.getRecommendation();
                }
                warnings.add(warning);
            }
        } catch (Exception e) {
            LOGGER.warning("Lỗi kiểm tra tương tác thuốc: " + e.getMessage());
        }
        return warnings;
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

    /**
     * Cập nhật trạng thái đơn thuốc.
     */
    public boolean updateStatus(long prescriptionId, String newStatus) {
        return prescriptionDAO.updateStatus(prescriptionId, newStatus);
    }
}
