package com.hospital.dao;

import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionDetail;

import java.sql.*;

public class PrescriptionDAO {

    private final Connection conn;

    public PrescriptionDAO(Connection conn) {
        this.conn = conn;
    }

    // =========================================
    // INSERT PRESCRIPTION & RETURN GENERATED ID
    // =========================================
    public long insertAndGetId(Prescription p) throws SQLException {

        String sql = """
            INSERT INTO prescription
            (medical_record_id, doctor_id, prescription_date, status)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, p.getMedicalRecordId());
            ps.setLong(2, p.getDoctorId());
            ps.setTimestamp(3, Timestamp.valueOf(p.getPrescriptionDate()));
            ps.setString(4, p.getStatus());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }

        throw new SQLException("Không lấy được ID prescription");
    }

    // =========================================
    // INSERT PRESCRIPTION DETAIL
    // =========================================
    public void insertDetail(PrescriptionDetail d) throws SQLException {

        String sql = """
            INSERT INTO prescription_detail
            (prescription_id, medicine_id, quantity, dosage, note)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, d.getPrescriptionId());
            ps.setLong(2, d.getMedicineId());
            ps.setInt(3, d.getQuantity());
            ps.setString(4, d.getDosage());
            ps.setString(5, d.getNote());

            ps.executeUpdate();
        }
    }
}
