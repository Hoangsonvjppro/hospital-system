package com.hospital.dao;

import java.sql.*;

/**
 * DAO bệnh án – tương thích cấu trúc của bạn (dùng Connection).
 * Bao gồm: tạo bệnh án trống + cập nhật chẩn đoán, triệu chứng, sinh hiệu.
 */
public class MedicalRecordDAO {

    private Connection connection;

    public MedicalRecordDAO(Connection connection) {
        this.connection = connection;
    }

    // ── Tạo bệnh án trống và trả về record_id ──────────────────────────────
    public long createEmptyRecord(long patientId, long doctorId, Long appointmentId) throws SQLException {

        String sql = """
            INSERT INTO MedicalRecord (
                patient_id,
                doctor_id,
                appointment_id,
                visit_date
            ) VALUES (?, ?, ?, NOW())
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, patientId);
            ps.setLong(2, doctorId);

            if (appointmentId != null) {
                ps.setLong(3, appointmentId);
            } else {
                ps.setNull(3, Types.BIGINT);
            }

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
        }

        throw new SQLException("Không thể tạo Medical Record");
    }

    // ── Cập nhật chẩn đoán ──────────────────────────────────────────────────
    public boolean updateDiagnosis(long recordId, String diagnosis) throws SQLException {

        String sql = """
            UPDATE MedicalRecord
               SET diagnosis  = ?,
                   updated_at = NOW()
             WHERE record_id  = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, diagnosis);
            ps.setLong(2, recordId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Cập nhật triệu chứng ────────────────────────────────────────────────
    public boolean updateSymptoms(long recordId, String symptoms) throws SQLException {

        String sql = """
            UPDATE MedicalRecord
               SET symptoms   = ?,
                   updated_at = NOW()
             WHERE record_id  = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, symptoms);
            ps.setLong(2, recordId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Cập nhật chẩn đoán + triệu chứng cùng lúc ─────────────────────────
    public boolean updateDiagnosisAndSymptoms(long recordId, String diagnosis, String symptoms) throws SQLException {

        String sql = """
            UPDATE MedicalRecord
               SET diagnosis  = ?,
                   symptoms   = ?,
                   updated_at = NOW()
             WHERE record_id  = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, diagnosis);
            ps.setString(2, symptoms);
            ps.setLong(3, recordId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Cập nhật sinh hiệu (vital signs) ───────────────────────────────────
    public boolean updateVitalSigns(long recordId, double weight, double height,
                                     String bloodPressure, int pulse) throws SQLException {

        String sql = """
            UPDATE MedicalRecord
               SET weight         = ?,
                   height         = ?,
                   blood_pressure = ?,
                   pulse          = ?,
                   updated_at     = NOW()
             WHERE record_id      = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, weight);
            ps.setDouble(2, height);
            ps.setString(3, bloodPressure);
            ps.setInt(4, pulse);
            ps.setLong(5, recordId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Cập nhật trạng thái bệnh án ────────────────────────────────────────
    public boolean updateStatus(long recordId, String status) throws SQLException {

        String sql = """
            UPDATE MedicalRecord
               SET status     = ?,
                   updated_at = NOW()
             WHERE record_id  = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, recordId);
            return ps.executeUpdate() > 0;
        }
    }
}
