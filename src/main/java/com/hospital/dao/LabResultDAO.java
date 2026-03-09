package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.LabResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO kết quả xét nghiệm – hỗ trợ cả 2 mode: tự lấy Connection hoặc nhận từ bên ngoài.
 */
public class LabResultDAO {

    private static final Logger LOGGER = Logger.getLogger(LabResultDAO.class.getName());

    private Connection externalConnection;

    public LabResultDAO() {}

    public LabResultDAO(Connection connection) {
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

    // ── Mapping helper ──────────────────────────────────────────────────────

    private LabResult mapRow(ResultSet rs) throws SQLException {
        LabResult r = new LabResult();
        r.setId(rs.getInt("lab_result_id"));
        r.setRecordId(rs.getLong("record_id"));
        long soId = rs.getLong("service_order_id");
        r.setServiceOrderId(rs.wasNull() ? null : soId);
        r.setTestName(rs.getString("test_name"));
        r.setResultValue(rs.getString("result_value"));
        r.setNormalRange(rs.getString("normal_range"));
        r.setUnit(rs.getString("unit"));
        r.setResultText(rs.getString("result_text"));
        long performedBy = rs.getLong("performed_by");
        r.setPerformedBy(rs.wasNull() ? null : performedBy);
        Timestamp ts = rs.getTimestamp("test_date");
        if (ts != null) r.setTestDate(ts.toLocalDateTime());
        r.setNotes(rs.getString("notes"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) r.setCreatedAt(ca.toLocalDateTime());
        return r;
    }

    // ── CRUD ────────────────────────────────────────────────────────────────

    public LabResult findById(int id) {
        String sql = "SELECT * FROM LabResult WHERE lab_result_id = ?";
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
            LOGGER.log(Level.SEVERE, "Lỗi tìm LabResult id=" + id, e);
            throw new DataAccessException("Không thể tìm kết quả xét nghiệm", e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    public List<LabResult> findAll() {
        String sql = "SELECT * FROM LabResult ORDER BY test_date DESC";
        List<LabResult> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách LabResult", e);
            throw new DataAccessException("Không thể lấy danh sách kết quả xét nghiệm", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    /**
     * Lấy kết quả xét nghiệm theo bệnh án.
     */
    public List<LabResult> findByRecordId(long recordId) {
        String sql = "SELECT * FROM LabResult WHERE record_id = ? ORDER BY test_date DESC";
        List<LabResult> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, recordId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm LabResult theo recordId=" + recordId, e);
            throw new DataAccessException("Không thể tìm kết quả xét nghiệm theo bệnh án", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    /**
     * Lấy kết quả xét nghiệm theo phiếu chỉ định.
     */
    public List<LabResult> findByServiceOrderId(long serviceOrderId) {
        String sql = "SELECT * FROM LabResult WHERE service_order_id = ? ORDER BY test_date DESC";
        List<LabResult> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, serviceOrderId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm LabResult theo serviceOrderId=" + serviceOrderId, e);
            throw new DataAccessException("Không thể tìm kết quả xét nghiệm theo phiếu chỉ định", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public boolean insert(LabResult r) {
        String sql = """
                INSERT INTO LabResult (record_id, service_order_id, test_name,
                    result_value, normal_range, unit, result_text, performed_by, test_date, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, r.getRecordId());
                if (r.getServiceOrderId() != null) {
                    ps.setLong(2, r.getServiceOrderId());
                } else {
                    ps.setNull(2, Types.BIGINT);
                }
                ps.setString(3, r.getTestName());
                ps.setString(4, r.getResultValue());
                ps.setString(5, r.getNormalRange());
                ps.setString(6, r.getUnit());
                ps.setString(7, r.getResultText());
                if (r.getPerformedBy() != null) ps.setLong(8, r.getPerformedBy());
                else ps.setNull(8, Types.BIGINT);
                ps.setTimestamp(9, Timestamp.valueOf(r.getTestDate()));
                ps.setString(10, r.getNotes());

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) r.setId(rs.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm LabResult", e);
            throw new DataAccessException("Không thể thêm kết quả xét nghiệm", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    public boolean update(LabResult r) {
        String sql = """
                UPDATE LabResult SET record_id = ?, service_order_id = ?, test_name = ?,
                    result_value = ?, normal_range = ?, unit = ?, result_text = ?,
                    performed_by = ?, test_date = ?, notes = ?
                WHERE lab_result_id = ?
                """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, r.getRecordId());
                if (r.getServiceOrderId() != null) {
                    ps.setLong(2, r.getServiceOrderId());
                } else {
                    ps.setNull(2, Types.BIGINT);
                }
                ps.setString(3, r.getTestName());
                ps.setString(4, r.getResultValue());
                ps.setString(5, r.getNormalRange());
                ps.setString(6, r.getUnit());
                ps.setString(7, r.getResultText());
                if (r.getPerformedBy() != null) ps.setLong(8, r.getPerformedBy());
                else ps.setNull(8, Types.BIGINT);
                ps.setTimestamp(9, Timestamp.valueOf(r.getTestDate()));
                ps.setString(10, r.getNotes());
                ps.setInt(11, r.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật LabResult id=" + r.getId(), e);
            throw new DataAccessException("Không thể cập nhật kết quả xét nghiệm", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM LabResult WHERE lab_result_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa LabResult id=" + id, e);
            throw new DataAccessException("Không thể xóa kết quả xét nghiệm", e);
        } finally {
            closeIfOwned(conn);
        }
    }
}
