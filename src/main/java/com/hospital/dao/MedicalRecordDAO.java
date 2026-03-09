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
        return createEmptyRecord(patientId, (Long) doctorId, appointmentId, null, null, null);
    }

    /**
     * Tạo bệnh án với khả năng lưu priority / queue_number / visit_type.
     */
    public long createEmptyRecord(long patientId, Long doctorId, Long appointmentId,
                                  String priority, Integer queueNumber,
                                  String visitType) {

        String sql = """
            INSERT INTO MedicalRecord (
                patient_id,
                doctor_id,
                appointment_id,
                visit_date,
                visit_type,
                priority,
                queue_number
            ) VALUES (?, ?, ?, NOW(), ?, ?, ?)
        """;

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, patientId);

                if (doctorId != null) {
                    ps.setLong(2, doctorId);
                } else {
                    ps.setNull(2, Types.BIGINT);
                }

                if (appointmentId != null) {
                    ps.setLong(3, appointmentId);
                } else {
                    ps.setNull(3, Types.BIGINT);
                }

                ps.setString(4, visitType != null ? visitType : "FIRST_VISIT");

                if (priority != null) {
                    ps.setString(5, priority);
                } else {
                    ps.setNull(5, Types.VARCHAR);
                }

                if (queueNumber != null) {
                    ps.setInt(6, queueNumber);
                } else {
                    ps.setNull(6, Types.INTEGER);
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
    // Order priorities so that EMERGENCY comes first, then ELDERLY, then NORMAL
    String sql = (doctorId > 0)
        ? "SELECT * FROM MedicalRecord WHERE doctor_id = ? AND DATE(visit_date) = CURRENT_DATE ORDER BY FIELD(priority, 'EMERGENCY','ELDERLY','NORMAL') ASC, queue_number ASC"
        : "SELECT * FROM MedicalRecord WHERE DATE(visit_date) = CURRENT_DATE ORDER BY FIELD(priority, 'EMERGENCY','ELDERLY','NORMAL') ASC, queue_number ASC";
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

    /**
     * List all medical records for a patient (history), ordered by visit_date desc.
     */
    public java.util.List<com.hospital.model.MedicalRecord> listByPatient(long patientId) {
        java.util.List<com.hospital.model.MedicalRecord> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM MedicalRecord WHERE patient_id = ? ORDER BY visit_date DESC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy lịch sử khám cho patientId=" + patientId, e);
            throw new DataAccessException("Không thể lấy lịch sử khám", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    private com.hospital.model.MedicalRecord mapResultSet(ResultSet rs) throws SQLException {
        com.hospital.model.MedicalRecord r = new com.hospital.model.MedicalRecord();
        r.setId(rs.getInt("record_id"));
        r.setPatientId(rs.getLong("patient_id"));
        r.setDoctorId(rs.getObject("doctor_id") == null ? null : rs.getLong("doctor_id"));
        r.setAppointmentId(rs.getObject("appointment_id") == null ? null : rs.getLong("appointment_id"));
        if (rs.getTimestamp("visit_date") != null) r.setVisitDate(rs.getTimestamp("visit_date").toLocalDateTime());
        try { r.setVisitType(rs.getString("visit_type")); } catch (SQLException ignored) {}
        r.setSymptoms(rs.getString("symptoms"));
        r.setDiagnosis(rs.getString("diagnosis"));
        try { r.setWeight(rs.getObject("weight") == null ? null : rs.getDouble("weight")); } catch (SQLException ignored) {}
        try { r.setHeight(rs.getObject("height") == null ? null : rs.getDouble("height")); } catch (SQLException ignored) {}
        try { r.setBloodPressure(rs.getString("blood_pressure")); } catch (SQLException ignored) {}
        try { r.setHeartRate(rs.getObject("heart_rate") == null ? null : rs.getInt("heart_rate")); } catch (SQLException ignored) {}
        try { r.setTemperature(rs.getObject("temperature") == null ? null : rs.getDouble("temperature")); } catch (SQLException ignored) {}
        try { r.setSpo2(rs.getObject("spo2") == null ? null : rs.getInt("spo2")); } catch (SQLException ignored) {}
        try { r.setDiagnosisCode(rs.getString("diagnosis_code")); } catch (SQLException ignored) {}
        try { r.setReferralNote(rs.getString("referral_note")); } catch (SQLException ignored) {}
        try { r.setNotes(rs.getString("notes")); } catch (SQLException ignored) {}
        r.setQueueStatus(rs.getString("queue_status"));
        try { r.setPriority(rs.getString("priority")); } catch (SQLException ignored) {}
        try { r.setQueueNumber(rs.getObject("queue_number") == null ? null : rs.getInt("queue_number")); } catch (SQLException ignored) {}
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
                                     String bloodPressure, int pulse,
                                     double temperature, int spo2) {

        String sql = """
            UPDATE MedicalRecord
               SET weight         = ?,
                   height         = ?,
                   blood_pressure = ?,
                   heart_rate     = ?,
                   temperature    = ?,
                   spo2           = ?,
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
                ps.setDouble(5, temperature);
                ps.setInt(6, spo2);
                ps.setLong(7, recordId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật sinh hiệu recordId=" + recordId, e);
            throw new DataAccessException("Không thể cập nhật sinh hiệu", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    /**
     * Backward-compatible overload (without temperature/spo2).
     */
    public boolean updateVitalSigns(long recordId, double weight, double height,
                                     String bloodPressure, int pulse) {
        return updateVitalSigns(recordId, weight, height, bloodPressure, pulse, 0, 0);
    }

    /**
     * Cập nhật chẩn đoán, triệu chứng, mã ICD-10, ghi chú bác sĩ, và ngày tái khám.
     */
    public boolean updateFullExamination(long recordId, String diagnosis, String symptoms,
                                          String diagnosisCode, String notes,
                                          String referralNote) {
        String sql = """
            UPDATE MedicalRecord
               SET diagnosis       = ?,
                   symptoms        = ?,
                   diagnosis_code  = ?,
                   notes           = ?,
                   referral_note   = ?,
                   updated_at      = NOW()
             WHERE record_id       = ?
        """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, diagnosis);
                ps.setString(2, symptoms);
                ps.setString(3, diagnosisCode);
                ps.setString(4, notes);
                ps.setString(5, referralNote);
                ps.setLong(6, recordId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật khám bệnh recordId=" + recordId, e);
            throw new DataAccessException("Không thể cập nhật khám bệnh", e);
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

    /**
     * Lấy danh sách bệnh án hôm nay theo trạng thái (vd: DISPENSED, PAID).
     */
    public java.util.List<com.hospital.model.MedicalRecord> getTodayByStatus(String status) {
        java.util.List<com.hospital.model.MedicalRecord> list = new java.util.ArrayList<>();
        String sql = """
            SELECT *
              FROM MedicalRecord
             WHERE queue_status = ?
               AND DATE(visit_date) = CURRENT_DATE
             ORDER BY updated_at DESC
        """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy bệnh án theo trạng thái " + status, e);
            throw new DataAccessException("Lỗi lấy bệnh án theo trạng thái", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    /**
     * Update priority of a record (e.g., to EMERGENCY) and set updated_at.
     */
    public boolean updatePriority(long recordId, String priority) {
        String sql = "UPDATE MedicalRecord SET priority = ?, updated_at = NOW() WHERE record_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, priority);
                ps.setLong(2, recordId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật priority recordId=" + recordId, e);
            throw new DataAccessException("Không thể cập nhật priority", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    /**
     * Reindex today's queue_number for a doctor (or all doctors if doctorId<=0).
     * Ordering: priority (EMERGENCY, ELDERLY, NORMAL) desc, arrival_time ASC, visit_date ASC
     */
    public void reindexTodayQueue(long doctorId) {
        // Delegate to new overload without a forced-top record
        reindexTodayQueue(doctorId, null);
    }

    /**
     * Reindex today's queue_number for a doctor (or all doctors if doctorId<=0).
     * If topRecordId is provided, that record will be ordered first (queue_number = 1),
     * then the rest will follow ordered by priority and arrival_time.
     */
    public void reindexTodayQueue(long doctorId, Long topRecordId) {
        String selectSql;
        if (doctorId > 0) {
            if (topRecordId != null) {
                // Place existing EMERGENCY records first, then the promoted record, then ELDERLY, then NORMAL
                selectSql = "SELECT record_id FROM MedicalRecord WHERE doctor_id = ? AND DATE(visit_date) = CURRENT_DATE "
                        + "ORDER BY (CASE WHEN priority = 'EMERGENCY' AND record_id <> ? THEN 0 WHEN record_id = ? THEN 1 WHEN priority = 'ELDERLY' THEN 2 ELSE 3 END), arrival_time ASC, visit_date ASC";
            } else {
                selectSql = "SELECT record_id FROM MedicalRecord WHERE doctor_id = ? AND DATE(visit_date) = CURRENT_DATE "
                        + "ORDER BY FIELD(priority, 'EMERGENCY','ELDERLY','NORMAL') DESC, arrival_time ASC, visit_date ASC";
            }
        } else {
            if (topRecordId != null) {
                selectSql = "SELECT record_id FROM MedicalRecord WHERE DATE(visit_date) = CURRENT_DATE "
                        + "ORDER BY (CASE WHEN record_id = ? THEN 0 ELSE 1 END), FIELD(priority, 'EMERGENCY','ELDERLY','NORMAL') DESC, arrival_time ASC, visit_date ASC";
            } else {
                selectSql = "SELECT record_id FROM MedicalRecord WHERE DATE(visit_date) = CURRENT_DATE "
                        + "ORDER BY FIELD(priority, 'EMERGENCY','ELDERLY','NORMAL') DESC, arrival_time ASC, visit_date ASC";
            }
        }

        Connection conn = null;
        boolean localConn = false;
        try {
            conn = getConnection();
            if (externalConnection == null) {
                localConn = true;
                conn.setAutoCommit(false);
            }

            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                int paramIndex = 1;
                if (doctorId > 0) {
                    ps.setLong(paramIndex++, doctorId);
                }
                    if (topRecordId != null) {
                        // For the CASE expression we need to bind topRecordId twice (for the <> ? and = ? checks)
                        ps.setLong(paramIndex++, topRecordId);
                        ps.setLong(paramIndex++, topRecordId);
                    }

                try (ResultSet rs = ps.executeQuery()) {
                    // Collect ordered record ids first so we can log before/after
                    java.util.List<Long> ordered = new java.util.ArrayList<>();
                    while (rs.next()) {
                        ordered.add(rs.getLong("record_id"));
                    }

                    LOGGER.info(() -> "Reindex selected order (doctorId=" + doctorId + ", topRecordId=" + topRecordId + "): " + ordered.toString());

                    int idx = 0;
                    StringBuilder sb = new StringBuilder();
                    for (Long rid : ordered) {
                        idx++;
                        try (PreparedStatement up = conn.prepareStatement("UPDATE MedicalRecord SET queue_number = ?, updated_at = NOW() WHERE record_id = ?")) {
                            up.setInt(1, idx);
                            up.setLong(2, rid);
                            up.executeUpdate();
                        }
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(rid).append("->").append(idx);
                    }

                    LOGGER.info(() -> "Reindex applied (doctorId=" + doctorId + ", topRecordId=" + topRecordId + "): " + sb.toString());
                }
            }

            if (localConn) conn.commit();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi reindex hàng đợi hôm nay", e);
            try { if (conn != null && localConn) conn.rollback(); } catch (SQLException ignored) {}
            throw new DataAccessException("Không thể reindex hàng đợi hôm nay", e);
        } finally {
            if (localConn && conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            closeIfOwned(conn);
        }
    }
}
