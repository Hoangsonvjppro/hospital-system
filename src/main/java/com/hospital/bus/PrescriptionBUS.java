package com.hospital.bus;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.MedicineDAO;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionDetail;

public class PrescriptionBUS {

    public boolean createPrescription(long medicalRecordId,
                                      long doctorId,
                                      List<PrescriptionDetail> details) {

        Connection conn = null;

        try {
            conn = DatabaseConfig.getInstance().getConnection();
            conn.setAutoCommit(false);

            PrescriptionDAO pDao = new PrescriptionDAO(conn);
            MedicineDAO mDao = new MedicineDAO(conn);

            Prescription prescription = new Prescription();
            prescription.setMedicalRecordId(medicalRecordId);
            prescription.setDoctorId(doctorId);
            prescription.setPrescriptionDate(LocalDateTime.now());
            prescription.setStatus("CREATED");

            long prescriptionId = pDao.insertAndGetId(prescription);

            for (PrescriptionDetail d : details) {

                d.setPrescriptionId(prescriptionId);
                pDao.insertDetail(d);

                boolean ok = mDao.reduceStock(
                        (int)d.getMedicineId(),
                        d.getQuantity()
                );

                if (!ok)
                    throw new DataAccessException("Không đủ tồn kho!", null);
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
