package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.QueueEntry;
import com.hospital.model.QueueEntry.Priority;
import com.hospital.model.QueueEntry.QueueStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO hàng đợi khám bệnh — truy vấn bảng queue_entries.
 * Tất cả method dùng PreparedStatement, try-with-resources.
 */
public class QueueEntryDAO {

    private static final Logger LOGGER = Logger.getLogger(QueueEntryDAO.class.getName());

    private Connection externalConnection;

    public QueueEntryDAO() {}

    public QueueEntryDAO(Connection connection) {
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

    // ── addToQueue ────────────────────────────────────────────
    /**
     * Thêm bệnh nhân vào hàng đợi. Auto-generate queueNumber (reset mỗi ngày).
     * Trả về generated ID.
     */
    public int addToQueue(QueueEntry entry) {
        // Lấy số thứ tự tiếp theo trong ngày
        int nextNumber = getNextQueueNumber();
        entry.setQueueNumber(nextNumber);

        String sql = """
            INSERT INTO queue_entries
                (patient_id, queue_number, priority, status, created_at)
            VALUES (?, ?, ?, ?, ?)
        """;

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, entry.getPatientId());
                ps.setInt(2, entry.getQueueNumber());
                ps.setString(3, entry.getPriority() != null ? entry.getPriority().name() : "NORMAL");
                ps.setString(4, QueueStatus.WAITING.name());
                ps.setTimestamp(5, Timestamp.valueOf(
                        entry.getCreatedAt() != null ? entry.getCreatedAt() : LocalDateTime.now()));

                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        entry.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể thêm vào hàng đợi, patientId=" + entry.getPatientId(), e);
            throw new DataAccessException("Không thể thêm vào hàng đợi", e);
        } finally {
            closeIfOwned(conn);
        }
        throw new DataAccessException("Không thể tạo queue entry", null);
    }

    // ── getTodayQueue ─────────────────────────────────────────
    /**
     * Lấy toàn bộ hàng đợi hôm nay, sắp xếp theo priority rồi created_at.
     */
    public List<QueueEntry> getTodayQueue() {
        String sql = """
            SELECT qe.*, p.full_name, p.phone
            FROM queue_entries qe
            JOIN Patient p ON qe.patient_id = p.patient_id
            WHERE DATE(qe.created_at) = CURDATE()
            ORDER BY
                FIELD(qe.priority, 'EMERGENCY', 'ELDERLY', 'NORMAL'),
                qe.created_at ASC
        """;
        return queryList(sql);
    }

    // ── getWaitingQueue ───────────────────────────────────────
    /**
     * Chỉ lấy status = WAITING hôm nay.
     */
    public List<QueueEntry> getWaitingQueue() {
        String sql = """
            SELECT qe.*, p.full_name, p.phone
            FROM queue_entries qe
            JOIN Patient p ON qe.patient_id = p.patient_id
            WHERE DATE(qe.created_at) = CURDATE()
              AND qe.status = 'WAITING'
            ORDER BY
                FIELD(qe.priority, 'EMERGENCY', 'ELDERLY', 'NORMAL'),
                qe.created_at ASC
        """;
        return queryList(sql);
    }

    // ── updateStatus ──────────────────────────────────────────
    /**
     * Cập nhật trạng thái hàng đợi.
     */
    public boolean updateStatus(int id, QueueStatus status) {
        String sql;
        if (status == QueueStatus.IN_PROGRESS) {
            sql = "UPDATE queue_entries SET status = ?, called_at = NOW() WHERE id = ?";
        } else if (status == QueueStatus.COMPLETED || status == QueueStatus.CANCELLED) {
            sql = "UPDATE queue_entries SET status = ?, completed_at = NOW() WHERE id = ?";
        } else {
            sql = "UPDATE queue_entries SET status = ? WHERE id = ?";
        }

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status.name());
                ps.setInt(2, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật trạng thái hàng đợi, id=" + id, e);
            throw new DataAccessException("Không thể cập nhật trạng thái hàng đợi", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    // ── getNextPatient ────────────────────────────────────────
    /**
     * Lấy bệnh nhân tiếp theo cần khám (priority cao nhất, chờ lâu nhất).
     */
    public QueueEntry getNextPatient() {
        String sql = """
            SELECT qe.*, p.full_name, p.phone
            FROM queue_entries qe
            JOIN Patient p ON qe.patient_id = p.patient_id
            WHERE DATE(qe.created_at) = CURDATE()
              AND qe.status = 'WAITING'
            ORDER BY
                FIELD(qe.priority, 'EMERGENCY', 'ELDERLY', 'NORMAL'),
                qe.created_at ASC
            LIMIT 1
        """;

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy bệnh nhân tiếp theo", e);
            throw new DataAccessException("Lỗi lấy bệnh nhân tiếp theo", e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    // ── getNextQueueNumber ────────────────────────────────────
    /**
     * Tạo số thứ tự tiếp theo trong ngày (reset mỗi ngày).
     */
    public int getNextQueueNumber() {
        String sql = "SELECT COALESCE(MAX(queue_number), 0) + 1 FROM queue_entries WHERE DATE(created_at) = CURDATE()";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy số thứ tự hàng đợi", e);
            throw new DataAccessException("Lỗi lấy số thứ tự hàng đợi", e);
        } finally {
            closeIfOwned(conn);
        }
        return 1;
    }

    // ── findById ──────────────────────────────────────────────
    public QueueEntry findById(int id) {
        String sql = """
            SELECT qe.*, p.full_name, p.phone
            FROM queue_entries qe
            JOIN Patient p ON qe.patient_id = p.patient_id
            WHERE qe.id = ?
        """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn queue entry id=" + id, e);
            throw new DataAccessException("Lỗi truy vấn queue entry", e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    // ── countTodayWaiting ─────────────────────────────────────
    public int countTodayWaiting() {
        String sql = "SELECT COUNT(*) FROM queue_entries WHERE DATE(created_at) = CURDATE() AND status = 'WAITING'";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm hàng đợi", e);
            throw new DataAccessException("Lỗi đếm hàng đợi", e);
        } finally {
            closeIfOwned(conn);
        }
        return 0;
    }

    // ── Helpers ───────────────────────────────────────────────

    private List<QueueEntry> queryList(String sql) {
        List<QueueEntry> list = new ArrayList<>();
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
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn hàng đợi", e);
            throw new DataAccessException("Lỗi truy vấn hàng đợi", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    private QueueEntry mapResultSet(ResultSet rs) throws SQLException {
        QueueEntry e = new QueueEntry();
        e.setId(rs.getInt("id"));
        e.setPatientId(rs.getInt("patient_id"));
        e.setQueueNumber(rs.getInt("queue_number"));

        // Priority
        String priStr = rs.getString("priority");
        if (priStr != null) {
            try { e.setPriority(Priority.valueOf(priStr)); }
            catch (IllegalArgumentException ignored) { e.setPriority(Priority.NORMAL); }
        }

        // Status
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try { e.setStatus(QueueStatus.valueOf(statusStr)); }
            catch (IllegalArgumentException ignored) { e.setStatus(QueueStatus.WAITING); }
        }

        // Timestamps
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) e.setCreatedAt(createdTs.toLocalDateTime());

        try {
            Timestamp calledTs = rs.getTimestamp("called_at");
            if (calledTs != null) e.setCalledAt(calledTs.toLocalDateTime());
        } catch (SQLException ignored) {}

        try {
            Timestamp completedTs = rs.getTimestamp("completed_at");
            if (completedTs != null) e.setCompletedAt(completedTs.toLocalDateTime());
        } catch (SQLException ignored) {}

        // JOIN fields
        try {
            e.setPatientName(rs.getString("full_name"));
        } catch (SQLException ignored) {}

        try {
            e.setPatientPhone(rs.getString("phone"));
        } catch (SQLException ignored) {}

        return e;
    }
}
