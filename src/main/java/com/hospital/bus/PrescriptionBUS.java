package com.hospital.bus;

import com.hospital.dao.MedicineDAO;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionDetail;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PrescriptionBUS {

    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();

    /**
     * Tạo đơn thuốc + trừ kho (transaction)
     */
    public boolean createPrescription(
            int medicalRecordId,
            int doctorId,
            List<PrescriptionDetail> details
    ) {

        Connection conn = null;

        try {
            conn = com.hospital.config.DatabaseConfig
                    .getInstance()
                    .getConnection();

            conn.setAutoCommit(false);

            PrescriptionDAO pDao = new PrescriptionDAO(conn);
            MedicineDAO mDao = new MedicineDAO(conn);

            // 1️⃣ Tạo Prescription
            Prescription prescription = new Prescription();
            prescription.setMedicalRecordId(medicalRecordId);
            prescription.setDoctorId(doctorId);
            prescription.setPrescriptionDate(LocalDate.now());

            int prescriptionId = pDao.insertAndGetId(prescription);

            // 2️⃣ Insert từng PrescriptionDetail + trừ kho
            for (PrescriptionDetail d : details) {

                d.setPrescriptionId(prescriptionId);
                pDao.insertDetail(d);

                boolean ok = mDao.reduceStock(
                        d.getMedicineId(),
                        d.getQuantity()
                );

                if (!ok) {
                    throw new DataAccessException("Không đủ tồn kho!");
                }
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ignored) {}
            throw new DataAccessException("Lỗi tạo đơn thuốc", e);

        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ignored) {}
        }
    }
}
