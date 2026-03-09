package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.ServiceOrder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceOrderDAO implements BaseDAO<ServiceOrder> {

    private Connection externalConnection;

    public ServiceOrderDAO() {}
    public ServiceOrderDAO(Connection connection) { this.externalConnection = connection; }

    private Connection getConnection() throws SQLException {
        return externalConnection != null ? externalConnection : DatabaseConfig.getInstance().getConnection();
    }

    private void closeIfOwned(Connection conn) {
        if (externalConnection == null && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public ServiceOrder findById(int id) {
        String sql = "SELECT * FROM ServiceOrder WHERE order_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn yêu cầu dịch vụ ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<ServiceOrder> findAll() {
        List<ServiceOrder> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM ServiceOrder ORDER BY ordered_at DESC")) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn danh sách yêu cầu dịch vụ", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<ServiceOrder> findByRecordId(long recordId) {
        List<ServiceOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM ServiceOrder WHERE record_id = ? ORDER BY ordered_at";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, recordId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn yêu cầu dịch vụ theo record", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(ServiceOrder entity) {
        String sql = "INSERT INTO ServiceOrder (record_id, service_id, ordered_by, status, notes) VALUES (?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, entity.getRecordId());
                ps.setLong(2, entity.getServiceId());
                if (entity.getOrderedBy() != null) ps.setLong(3, entity.getOrderedBy()); else ps.setNull(3, Types.BIGINT);
                ps.setString(4, entity.getStatus() != null ? entity.getStatus() : "ORDERED");
                ps.setString(5, entity.getNotes());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) entity.setId(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm yêu cầu dịch vụ", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    @Override
    public boolean update(ServiceOrder entity) {
        String sql = "UPDATE ServiceOrder SET status=?, completed_at=?, notes=? WHERE order_id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, entity.getStatus());
                if (entity.getCompletedAt() != null) ps.setTimestamp(2, Timestamp.valueOf(entity.getCompletedAt()));
                else ps.setNull(2, Types.TIMESTAMP);
                ps.setString(3, entity.getNotes());
                ps.setInt(4, entity.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật yêu cầu dịch vụ ID=" + entity.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM ServiceOrder WHERE order_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể xóa yêu cầu dịch vụ ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    private ServiceOrder mapResultSet(ResultSet rs) throws SQLException {
        ServiceOrder so = new ServiceOrder();
        so.setId(rs.getInt("order_id"));
        so.setRecordId(rs.getLong("record_id"));
        so.setServiceId(rs.getLong("service_id"));
        so.setOrderedBy(rs.getObject("ordered_by") == null ? null : rs.getLong("ordered_by"));
        Timestamp ordAt = rs.getTimestamp("ordered_at");
        if (ordAt != null) so.setOrderedAt(ordAt.toLocalDateTime());
        Timestamp compAt = rs.getTimestamp("completed_at");
        if (compAt != null) so.setCompletedAt(compAt.toLocalDateTime());
        so.setStatus(rs.getString("status"));
        so.setNotes(rs.getString("notes"));
        return so;
    }
}
