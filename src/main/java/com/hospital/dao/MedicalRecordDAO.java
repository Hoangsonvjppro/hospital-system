package com.hospital.dao;

import com.hospital.config.DatabaseConfig;

import java.sql.*;

/**
 * DAO bệnh án – hỗ trợ cả 2 mode: tự lấy Connection hoặc nhận từ bên ngoài.
 * Bao gồm: tạo bệnh án trống + cập nhật chẩn đoán, triệu chứng, sinh hiệu.
 */
public class MedicalRecordDAO {

    private Connection externalConnection;

    public MedicalRecordDAO() {
        // Mode 1: Tự lấy connection (cho thao tác đơn lẻ)
    }

    public MedicalRecordDAO(Connection connection) {
        // Mode 2: Dùng external connection (cho transaction)
        this.externalConnection = connection;
    }

    private Connection getConnection() throws SQLException {
        if (externalConnection != null) {
            return externalConnection;
        }
        return DatabaseConfig.getInstance().getConnection();
    }

    private void closeIfOwned(Connection conn) {
        if (externalConnection == null && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
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

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        } finally {
            closeIfOwned(conn);
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

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, diagnosis);
                ps.setLong(2, recordId);
                return ps.executeUpdate() > 0;
            }
        } finally {
            closeIfOwned(conn);
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

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, symptoms);
                ps.setLong(2, recordId);
                return ps.executeUpdate() > 0;
            }
        } finally {
            closeIfOwned(conn);
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

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, diagnosis);
                ps.setString(2, symptoms);
                ps.setLong(3, recordId);
                return ps.executeUpdate() > 0;
            }
        } finally {
            closeIfOwned(conn);
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

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, weight);
                ps.setDouble(2, height);
                ps.setString(3, bloodPressure);
                ps.setInt(4, pulse);
                ps.setLong(5, recordId);
                return ps.executeUpdate() > 0;
            }
        } finally {
            closeIfOwned(conn);
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

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setLong(2, recordId);
                return ps.executeUpdate() > 0;
            }
        } finally {
            closeIfOwned(conn);
        }
    }
}
