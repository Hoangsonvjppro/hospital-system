package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO bệnh án – hỗ trợ cả 2 mode: tự lấy Connection hoặc nhận từ bên ngoài.
 * Bao gồm: tạo bệnh án trống + cập nhật chẩn đoán, triệu chứng, sinh hiệu.
 */
public class MedicalRecordDAO {

    private static final Logger LOGGER = Logger.getLogger(MedicalRecordDAO.class.getName());

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
    public long createEmptyRecord(long patientId, long doctorId, Long appointmentId) {
        // Delegate to the new overload with defaults (no priority, no queue number)
        return createEmptyRecord(patientId, doctorId, appointmentId, null, null, null, null);
    }

    /**
     * Tạo bệnh án với khả năng lưu priority / queue_number / arrival_time / exam_type.
     */
    public long createEmptyRecord(long patientId, long doctorId, Long appointmentId,
                                  String priority, Integer queueNumber,
                                  Time arrivalTime, String examType) {

        String sql = """
            INSERT INTO MedicalRecord (
                patient_id,
                doctor_id,
                appointment_id,
                visit_date,
                priority,
                queue_number,
                arrival_time,
                exam_type
            ) VALUES (?, ?, ?, NOW(), ?, ?, ?, ?)
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

                if (priority != null) {
                    ps.setString(4, priority);
                } else {
                    ps.setNull(4, Types.VARCHAR);
                }

                if (queueNumber != null) {
                    ps.setInt(5, queueNumber);
                } else {
                    ps.setNull(5, Types.INTEGER);
                }

                if (arrivalTime != null) {
                    ps.setTime(6, arrivalTime);
                } else {
                    ps.setNull(6, Types.TIME);
                }

                if (examType != null) {
                    ps.setString(7, examType);
                } else {
                    ps.setNull(7, Types.VARCHAR);
                }

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            return rs.getLong(1);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tạo bệnh án cho patientId=" + patientId, e);
            throw new DataAccessException("Không thể tạo bệnh án", e);
        } finally {
            closeIfOwned(conn);
        }

        throw new DataAccessException("Không thể tạo Medical Record - không lấy được generated key", null);
    }

    /**
     * Kiểm tra xem bệnh nhân đã có record trong ngày hôm nay chưa.
     */
    public boolean patientHasRecordToday(long patientId) {
        String sql = "SELECT COUNT(*) AS cnt FROM MedicalRecord WHERE patient_id = ? AND DATE(visit_date) = CURRENT_DATE";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("cnt") > 0;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra record hôm nay cho patientId=" + patientId, e);
            throw new DataAccessException("Lỗi kiểm tra record hôm nay", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    /**
     * Đếm số bệnh án (hàng đợi) hôm nay cho một bác sĩ (nếu doctorId <=0 thì đếm toàn bộ).
     */
    public int countQueueToday(long doctorId) {
        String sql = (doctorId > 0)
                ? "SELECT COUNT(*) AS cnt FROM MedicalRecord WHERE doctor_id = ? AND DATE(visit_date) = CURRENT_DATE"
                : "SELECT COUNT(*) AS cnt FROM MedicalRecord WHERE DATE(visit_date) = CURRENT_DATE";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (doctorId > 0) ps.setLong(1, doctorId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("cnt");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm hàng đợi hôm nay", e);
            throw new DataAccessException("Lỗi đếm hàng đợi hôm nay", e);
        } finally {
            closeIfOwned(conn);
        }
        return 0;
    }

    /**
     * Lấy danh sách hàng đợi hôm nay (optionally theo bác sĩ). Trả về danh sách MedicalRecord đầy đủ.
     */
    public java.util.List<com.hospital.model.MedicalRecord> listQueueToday(long doctorId) {
        java.util.List<com.hospital.model.MedicalRecord> list = new java.util.ArrayList<>();
        String sql = (doctorId > 0)
                ? "SELECT * FROM MedicalRecord WHERE doctor_id = ? AND DATE(visit_date) = CURRENT_DATE ORDER BY FIELD(priority, 'EMERGENCY','ELDERLY','NORMAL') DESC, queue_number ASC"
                : "SELECT * FROM MedicalRecord WHERE DATE(visit_date) = CURRENT_DATE ORDER BY FIELD(priority, 'EMERGENCY','ELDERLY','NORMAL') DESC, queue_number ASC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (doctorId > 0) ps.setLong(1, doctorId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách hàng đợi hôm nay", e);
            throw new DataAccessException("Lỗi lấy danh sách hàng đợi hôm nay", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    /**
     * Lấy MedicalRecord theo ID
     */
    public com.hospital.model.MedicalRecord findById(long recordId) {
        String sql = "SELECT * FROM MedicalRecord WHERE record_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, recordId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy MedicalRecord ID=" + recordId, e);
            throw new DataAccessException("Lỗi lấy MedicalRecord", e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    private com.hospital.model.MedicalRecord mapResultSet(ResultSet rs) throws SQLException {
        com.hospital.model.MedicalRecord r = new com.hospital.model.MedicalRecord();
        r.setId(rs.getInt("record_id"));
        r.setPatientId(rs.getLong("patient_id"));
        r.setDoctorId(rs.getLong("doctor_id"));
        r.setAppointmentId(rs.getObject("appointment_id") == null ? null : rs.getLong("appointment_id"));
        if (rs.getTimestamp("visit_date") != null) r.setVisitDate(rs.getTimestamp("visit_date").toLocalDateTime());
        r.setSymptoms(rs.getString("symptoms"));
        r.setDiagnosis(rs.getString("diagnosis"));
        r.setWeight(rs.getDouble("weight"));
        r.setHeight(rs.getDouble("height"));
        r.setBloodPressure(rs.getString("blood_pressure"));
        r.setPulse(rs.getInt("heart_rate"));
        r.setStatus(rs.getString("queue_status"));
        try { r.setPriority(rs.getString("priority")); } catch (SQLException ignored) {}
        try { r.setQueueNumber(rs.getObject("queue_number") == null ? null : rs.getInt("queue_number")); } catch (SQLException ignored) {}
        try { r.setExamTypeField(rs.getString("exam_type")); } catch (SQLException ignored) {}
        try { java.sql.Time t = rs.getTime("arrival_time"); if (t != null) r.setArrivalTime(t.toLocalTime()); } catch (SQLException ignored) {}
        return r;
    }

    // ── Cập nhật chẩn đoán ──────────────────────────────────────────────────
    public boolean updateDiagnosis(long recordId, String diagnosis) {

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
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "L\u1ed7i c\u1eadp nh\u1eadt ch\u1ea9n \u0111o\u00e1n recordId=" + recordId, e);
            throw new DataAccessException("Kh\u00f4ng th\u1ec3 c\u1eadp nh\u1eadt ch\u1ea9n \u0111o\u00e1n", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    // ── Cập nhật triệu chứng ────────────────────────────────────────────────
    public boolean updateSymptoms(long recordId, String symptoms) {

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
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "L\u1ed7i c\u1eadp nh\u1eadt tri\u1ec7u ch\u1ee9ng recordId=" + recordId, e);
            throw new DataAccessException("Kh\u00f4ng th\u1ec3 c\u1eadp nh\u1eadt tri\u1ec7u ch\u1ee9ng", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    // ── Cập nhật chẩn đoán + triệu chứng cùng lúc ─────────────────────────
    public boolean updateDiagnosisAndSymptoms(long recordId, String diagnosis, String symptoms) {

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
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "L\u1ed7i c\u1eadp nh\u1eadt ch\u1ea9n \u0111o\u00e1n v\u00e0 tri\u1ec7u ch\u1ee9ng recordId=" + recordId, e);
            throw new DataAccessException("Kh\u00f4ng th\u1ec3 c\u1eadp nh\u1eadt ch\u1ea9n \u0111o\u00e1n v\u00e0 tri\u1ec7u ch\u1ee9ng", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    // ── Cập nhật sinh hiệu (vital signs) ───────────────────────────────────
    public boolean updateVitalSigns(long recordId, double weight, double height,
                                     String bloodPressure, int pulse) {

        String sql = """
            UPDATE MedicalRecord
               SET weight         = ?,
                   height         = ?,
                   blood_pressure = ?,
                   heart_rate     = ?,
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
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "L\u1ed7i c\u1eadp nh\u1eadt sinh hi\u1ec7u recordId=" + recordId, e);
            throw new DataAccessException("Kh\u00f4ng th\u1ec3 c\u1eadp nh\u1eadt sinh hi\u1ec7u", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    // ── Cập nhật trạng thái bệnh án ────────────────────────────────────────
    public boolean updateStatus(long recordId, String status) {

        String sql = """
            UPDATE MedicalRecord
               SET queue_status = ?,
                   updated_at  = NOW()
             WHERE record_id   = ?
        """;

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setLong(2, recordId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật trạng thái bệnh án recordId=" + recordId, e);
            throw new DataAccessException("Không thể cập nhật trạng thái bệnh án", e);
        } finally {
            closeIfOwned(conn);
        }
    }
}
