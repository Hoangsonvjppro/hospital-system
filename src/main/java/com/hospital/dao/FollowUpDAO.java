package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.FollowUp;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO hẹn tái khám — truy vấn JDBC trên bảng FollowUp.
 *
 * Hỗ trợ 2 mode: tự lấy Connection hoặc nhận từ bên ngoài (transaction).
 */
public class FollowUpDAO {

    private static final Logger LOGGER = Logger.getLogger(FollowUpDAO.class.getName());

    private Connection externalConnection;

    public FollowUpDAO() {}

    public FollowUpDAO(Connection connection) {
        this.externalConnection = connection;
    }

    private Connection getConnection() throws SQLException {
        if (externalConnection != null) return externalConnection;
        return DatabaseConfig.getInstance().getConnection();
    }

    private void closeIfOwned(Connection conn) {
        if (externalConnection == null && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  TẠO LỊCH HẸN TÁI KHÁM
    // ═══════════════════════════════════════════════════════════

    /**
     * Tạo lịch hẹn tái khám.
     * @return follow_up_id vừa tạo
     */
    public long scheduleFollowUp(FollowUp followUp) {
        String sql = """
            INSERT INTO FollowUp (patient_id, record_id, follow_up_date, reason, status, reminder_sent)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, followUp.getPatientId());
                ps.setLong(2, followUp.getRecordId());
                ps.setDate(3, Date.valueOf(followUp.getFollowUpDate()));
                ps.setString(4, followUp.getReason());
                ps.setString(5, followUp.getStatus());
                ps.setBoolean(6, followUp.isReminderSent());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        followUp.setId((int) id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tạo lịch hẹn tái khám", e);
            throw new DataAccessException("Lỗi tạo lịch hẹn tái khám", e);
        } finally {
            closeIfOwned(conn);
        }
        return -1;
    }

    // ═══════════════════════════════════════════════════════════
    //  TRUY VẤN
    // ═══════════════════════════════════════════════════════════

    /**
     * Danh sách lịch hẹn hôm nay.
     */
    public List<FollowUp> getTodayFollowUps() {
        String sql = BASE_SELECT + " WHERE f.follow_up_date = CURRENT_DATE ORDER BY f.follow_up_date, f.created_at";
        return queryList(sql);
    }

    /**
     * Lịch hẹn trong N ngày tới.
     */
    public List<FollowUp> getUpcomingFollowUps(int days) {
        String sql = BASE_SELECT
                + " WHERE f.follow_up_date BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL ? DAY)"
                + " AND f.status = 'SCHEDULED'"
                + " ORDER BY f.follow_up_date, f.created_at";
        Connection conn = null;
        try {
            conn = getConnection();
            List<FollowUp> list = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, days);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSet(rs));
                    }
                }
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy lịch hẹn upcoming", e);
            throw new DataAccessException("Lỗi lấy lịch hẹn tái khám sắp tới", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    /**
     * Lấy lịch hẹn theo bệnh nhân.
     */
    public List<FollowUp> getByPatientId(long patientId) {
        String sql = BASE_SELECT + " WHERE f.patient_id = ? ORDER BY f.follow_up_date DESC";
        Connection conn = null;
        try {
            conn = getConnection();
            List<FollowUp> list = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSet(rs));
                    }
                }
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy lịch hẹn patientId=" + patientId, e);
            throw new DataAccessException("Lỗi lấy lịch hẹn tái khám", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    /**
     * Lấy lịch hẹn theo record_id.
     */
    public FollowUp getByRecordId(long recordId) {
        String sql = BASE_SELECT + " WHERE f.record_id = ? ORDER BY f.created_at DESC LIMIT 1";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, recordId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy follow-up recordId=" + recordId, e);
            throw new DataAccessException("Lỗi lấy lịch hẹn tái khám", e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════
    //  CẬP NHẬT
    // ═══════════════════════════════════════════════════════════

    /**
     * Đánh dấu đã tái khám.
     */
    public boolean markAsCompleted(int id) {
        return updateStatus(id, FollowUp.STATUS_COMPLETED);
    }

    /**
     * Đánh dấu bỏ lỡ.
     */
    public boolean markAsMissed(int id) {
        return updateStatus(id, FollowUp.STATUS_MISSED);
    }

    /**
     * Hủy lịch hẹn.
     */
    public boolean cancel(int id) {
        return updateStatus(id, FollowUp.STATUS_CANCELLED);
    }

    private boolean updateStatus(int id, String status) {
        String sql = "UPDATE FollowUp SET status = ?, updated_at = NOW() WHERE follow_up_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setInt(2, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật trạng thái follow-up ID=" + id, e);
            throw new DataAccessException("Lỗi cập nhật lịch hẹn tái khám", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPER
    // ═══════════════════════════════════════════════════════════

    private static final String BASE_SELECT = """
        SELECT f.follow_up_id, f.patient_id, f.record_id, f.follow_up_date,
               f.reason, f.status, f.reminder_sent, f.created_at, f.updated_at,
               p.full_name AS patient_name, p.phone AS patient_phone,
               u.full_name AS doctor_name,
               mr.diagnosis
        FROM FollowUp f
        JOIN Patient p ON f.patient_id = p.patient_id
        LEFT JOIN MedicalRecord mr ON f.record_id = mr.record_id
        LEFT JOIN Doctor d ON mr.doctor_id = d.doctor_id
        LEFT JOIN `User` u ON d.user_id = u.user_id
        """;

    private List<FollowUp> queryList(String sql) {
        List<FollowUp> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách follow-up", e);
            throw new DataAccessException("Lỗi truy vấn lịch hẹn tái khám", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    private FollowUp mapResultSet(ResultSet rs) throws SQLException {
        FollowUp f = new FollowUp();
        f.setId(rs.getInt("follow_up_id"));
        f.setPatientId(rs.getLong("patient_id"));
        f.setRecordId(rs.getLong("record_id"));
        Date d = rs.getDate("follow_up_date");
        if (d != null) f.setFollowUpDate(d.toLocalDate());
        f.setReason(rs.getString("reason"));
        f.setStatus(rs.getString("status"));
        f.setReminderSent(rs.getBoolean("reminder_sent"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) f.setCreatedAt(createdAt.toLocalDateTime());
        try { f.setPatientName(rs.getString("patient_name")); } catch (SQLException ignored) {}
        try { f.setPatientPhone(rs.getString("patient_phone")); } catch (SQLException ignored) {}
        try { f.setDoctorName(rs.getString("doctor_name")); } catch (SQLException ignored) {}
        try { f.setDiagnosis(rs.getString("diagnosis")); } catch (SQLException ignored) {}
        return f;
    }
}
