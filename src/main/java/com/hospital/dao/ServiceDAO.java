package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO implements BaseDAO<Service> {

    private Connection externalConnection;

    public ServiceDAO() {}
    public ServiceDAO(Connection connection) { this.externalConnection = connection; }

    private Connection getConnection() throws SQLException {
        return externalConnection != null ? externalConnection : DatabaseConfig.getInstance().getConnection();
    }

    private void closeIfOwned(Connection conn) {
        if (externalConnection == null && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public Service findById(int id) {
        String sql = "SELECT * FROM Service WHERE service_id = ?";
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
            throw new DataAccessException("Lỗi truy vấn dịch vụ ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<Service> findAll() {
        List<Service> list = new ArrayList<>();
        String sql = "SELECT * FROM Service WHERE is_active = TRUE ORDER BY service_name";
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn danh sách dịch vụ", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(Service entity) {
        String sql = "INSERT INTO Service (service_name, service_type, price, description, is_active) VALUES (?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, entity.getServiceName());
                ps.setString(2, entity.getServiceType());
                ps.setDouble(3, entity.getPrice());
                ps.setString(4, entity.getDescription());
                ps.setBoolean(5, entity.isActive());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) entity.setId(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm dịch vụ", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    @Override
    public boolean update(Service entity) {
        String sql = "UPDATE Service SET service_name=?, service_type=?, price=?, description=?, is_active=? WHERE service_id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, entity.getServiceName());
                ps.setString(2, entity.getServiceType());
                ps.setDouble(3, entity.getPrice());
                ps.setString(4, entity.getDescription());
                ps.setBoolean(5, entity.isActive());
                ps.setInt(6, entity.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật dịch vụ ID=" + entity.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE Service SET is_active = FALSE WHERE service_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể xóa dịch vụ ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    private Service mapResultSet(ResultSet rs) throws SQLException {
        Service s = new Service();
        s.setId(rs.getInt("service_id"));
        s.setServiceName(rs.getString("service_name"));
        s.setServiceType(rs.getString("service_type"));
        s.setPrice(rs.getDouble("price"));
        s.setDescription(rs.getString("description"));
        s.setActive(rs.getBoolean("is_active"));
        Timestamp c = rs.getTimestamp("created_at");
        if (c != null) s.setCreatedAt(c.toLocalDateTime());
        Timestamp u = rs.getTimestamp("updated_at");
        if (u != null) s.setUpdatedAt(u.toLocalDateTime());
        return s;
    }
}
