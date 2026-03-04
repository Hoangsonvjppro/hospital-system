package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.LabOrder;
import com.hospital.model.LabOrder.LabStatus;
import com.hospital.model.LabOrder.TestType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO phiếu yêu cầu xét nghiệm (LabOrder).
 * Hỗ trợ 2 mode: tự lấy Connection hoặc nhận external Connection (cho transaction).
 */
public class LabOrderDAO {

    private static final Logger LOGGER = Logger.getLogger(LabOrderDAO.class.getName());

    private Connection externalConnection;

    public LabOrderDAO() {}

    public LabOrderDAO(Connection connection) {
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

    // ── Mapping ──────────────────────────────────────────────────────────────

    private LabOrder mapRow(ResultSet rs) throws SQLException {
        LabOrder o = new LabOrder();
        o.setId(rs.getInt("lab_order_id"));
        o.setExaminationId(rs.getLong("examination_id"));
        o.setPatientId(rs.getLong("patient_id"));

        String testTypeStr = rs.getString("test_type");
        if (testTypeStr != null) {
            try { o.setTestType(TestType.valueOf(testTypeStr)); }
            catch (IllegalArgumentException e) { o.setTestType(TestType.OTHER); }
        }

        o.setTestName(rs.getString("test_name"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try { o.setStatus(LabStatus.valueOf(statusStr)); }
            catch (IllegalArgumentException e) { o.setStatus(LabStatus.PENDING); }
        }

        o.setResult(rs.getString("result"));
        o.setNotes(rs.getString("notes"));

        Timestamp orderedTs = rs.getTimestamp("ordered_at");
        if (orderedTs != null) o.setOrderedAt(orderedTs.toLocalDateTime());

        Timestamp completedTs = rs.getTimestamp("completed_at");
        if (completedTs != null) o.setCompletedAt(completedTs.toLocalDateTime());

        o.setOrderedBy(rs.getLong("ordered_by"));

        // Transient fields (may not always be present)
        try { o.setPatientName(rs.getString("patient_name")); } catch (SQLException ignored) {}
        try { o.setDoctorName(rs.getString("doctor_name")); } catch (SQLException ignored) {}

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) o.setCreatedAt(createdAt.toLocalDateTime());

        return o;
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    /**
     * Tạo phiếu yêu cầu xét nghiệm, trả về generated ID.
     */
    public long createLabOrder(LabOrder order) {
        String sql = """
            INSERT INTO LabOrder (examination_id, patient_id, test_type, test_name,
                status, result, notes, ordered_at, ordered_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), ?)
        """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, order.getExaminationId());
                ps.setLong(2, order.getPatientId());
                ps.setString(3, order.getTestType() != null ? order.getTestType().name() : TestType.OTHER.name());
                ps.setString(4, order.getTestName());
                ps.setString(5, order.getStatus() != null ? order.getStatus().name() : LabStatus.PENDING.name());
                ps.setString(6, order.getResult());
                ps.setString(7, order.getNotes());
                ps.setLong(8, order.getOrderedBy());

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể tạo phiếu xét nghiệm", e);
            throw new DataAccessException("Không thể tạo phiếu xét nghiệm", e);
        } finally {
            closeIfOwned(conn);
        }
        return -1;
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    /**
     * Lấy phiếu XN theo ID.
     */
    public LabOrder findById(int id) {
        String sql = """
            SELECT lo.*, p.full_name AS patient_name, u.full_name AS doctor_name
            FROM LabOrder lo
            LEFT JOIN Patient p ON lo.patient_id = p.patient_id
            LEFT JOIN Doctor d ON lo.ordered_by = d.doctor_id
            LEFT JOIN `User` u ON d.user_id = u.user_id
            WHERE lo.lab_order_id = ?
        """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm LabOrder id=" + id, e);
            throw new DataAccessException("Không thể tìm phiếu xét nghiệm", e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    /**
     * Lấy tất cả XN của 1 lần khám (examination / medical record).
     */
    public List<LabOrder> getByExaminationId(long examId) {
        String sql = """
            SELECT lo.*, p.full_name AS patient_name, u.full_name AS doctor_name
            FROM LabOrder lo
            LEFT JOIN Patient p ON lo.patient_id = p.patient_id
            LEFT JOIN Doctor d ON lo.ordered_by = d.doctor_id
            LEFT JOIN `User` u ON d.user_id = u.user_id
            WHERE lo.examination_id = ?
            ORDER BY lo.ordered_at DESC
        """;
        return queryList(sql, ps -> ps.setLong(1, examId));
    }

    /**
     * Lịch sử XN của bệnh nhân.
     */
    public List<LabOrder> getByPatientId(long patientId) {
        String sql = """
            SELECT lo.*, p.full_name AS patient_name, u.full_name AS doctor_name
            FROM LabOrder lo
            LEFT JOIN Patient p ON lo.patient_id = p.patient_id
            LEFT JOIN Doctor d ON lo.ordered_by = d.doctor_id
            LEFT JOIN `User` u ON d.user_id = u.user_id
            WHERE lo.patient_id = ?
            ORDER BY lo.ordered_at DESC
        """;
        return queryList(sql, ps -> ps.setLong(1, patientId));
    }

    /**
     * Tất cả XN đang chờ (PENDING + IN_PROGRESS) — cho bên lab.
     */
    public List<LabOrder> getPendingOrders() {
        String sql = """
            SELECT lo.*, p.full_name AS patient_name, u.full_name AS doctor_name
            FROM LabOrder lo
            LEFT JOIN Patient p ON lo.patient_id = p.patient_id
            LEFT JOIN Doctor d ON lo.ordered_by = d.doctor_id
            LEFT JOIN `User` u ON d.user_id = u.user_id
            WHERE lo.status IN ('PENDING', 'IN_PROGRESS')
            ORDER BY lo.ordered_at ASC
        """;
        return queryList(sql, ps -> {});
    }

    /**
     * Tất cả LabOrder.
     */
    public List<LabOrder> findAll() {
        String sql = """
            SELECT lo.*, p.full_name AS patient_name, u.full_name AS doctor_name
            FROM LabOrder lo
            LEFT JOIN Patient p ON lo.patient_id = p.patient_id
            LEFT JOIN Doctor d ON lo.ordered_by = d.doctor_id
            LEFT JOIN `User` u ON d.user_id = u.user_id
            ORDER BY lo.ordered_at DESC
        """;
        return queryList(sql, ps -> {});
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    /**
     * Cập nhật kết quả xét nghiệm.
     */
    public boolean updateResult(int id, String result) {
        String sql = "UPDATE LabOrder SET result = ?, updated_at = NOW() WHERE lab_order_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, result);
                ps.setInt(2, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật kết quả LabOrder id=" + id, e);
            throw new DataAccessException("Không thể cập nhật kết quả xét nghiệm", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    /**
     * Cập nhật trạng thái xét nghiệm.
     */
    public boolean updateStatus(int id, LabStatus status) {
        String sql;
        if (status == LabStatus.COMPLETED) {
            sql = "UPDATE LabOrder SET status = ?, completed_at = NOW(), updated_at = NOW() WHERE lab_order_id = ?";
        } else {
            sql = "UPDATE LabOrder SET status = ?, updated_at = NOW() WHERE lab_order_id = ?";
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
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật trạng thái LabOrder id=" + id, e);
            throw new DataAccessException("Không thể cập nhật trạng thái xét nghiệm", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    /**
     * Cập nhật kết quả + trạng thái COMPLETED cùng lúc.
     */
    public boolean completeOrder(int id, String result) {
        String sql = """
            UPDATE LabOrder
            SET result = ?, status = 'COMPLETED', completed_at = NOW(), updated_at = NOW()
            WHERE lab_order_id = ?
        """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, result);
                ps.setInt(2, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi hoàn tất LabOrder id=" + id, e);
            throw new DataAccessException("Không thể hoàn tất xét nghiệm", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface ParamSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    private List<LabOrder> queryList(String sql, ParamSetter setter) {
        List<LabOrder> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                setter.set(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn LabOrder", e);
            throw new DataAccessException("Không thể truy vấn phiếu xét nghiệm", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }
}
