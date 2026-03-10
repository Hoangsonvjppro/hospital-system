package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO implements BaseDAO<Supplier> {

    private Connection externalConnection;

    public SupplierDAO() {}
    public SupplierDAO(Connection connection) { this.externalConnection = connection; }

    private Connection getConnection() throws SQLException {
        return externalConnection != null ? externalConnection : DatabaseConfig.getInstance().getConnection();
    }

    private void closeIfOwned(Connection conn) {
        if (externalConnection == null && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public Supplier findById(int id) {
        String sql = "SELECT * FROM Supplier WHERE supplier_id = ?";
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
            throw new DataAccessException("Lỗi truy vấn nhà cung cấp ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<Supplier> findAll() {
        List<Supplier> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM Supplier ORDER BY supplier_name")) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn danh sách nhà cung cấp", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<Supplier> findActive() {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM Supplier WHERE is_active = TRUE ORDER BY supplier_name";
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn nhà cung cấp đang hoạt động", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(Supplier entity) {
        String sql = "INSERT INTO Supplier (supplier_name, contact_name, phone, address, is_active) VALUES (?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, entity.getSupplierName());
                ps.setString(2, entity.getContactName());
                ps.setString(3, entity.getPhone());
                ps.setString(4, entity.getAddress());
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
            throw new DataAccessException("Không thể thêm nhà cung cấp", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    @Override
    public boolean update(Supplier entity) {
        String sql = "UPDATE Supplier SET supplier_name=?, contact_name=?, phone=?, address=?, is_active=? WHERE supplier_id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, entity.getSupplierName());
                ps.setString(2, entity.getContactName());
                ps.setString(3, entity.getPhone());
                ps.setString(4, entity.getAddress());
                ps.setBoolean(5, entity.isActive());
                ps.setInt(6, entity.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật nhà cung cấp ID=" + entity.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE Supplier SET is_active = FALSE WHERE supplier_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể xóa nhà cung cấp ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    private Supplier mapResultSet(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getInt("supplier_id"));
        s.setSupplierName(rs.getString("supplier_name"));
        s.setContactName(rs.getString("contact_name"));
        s.setPhone(rs.getString("phone"));
        s.setAddress(rs.getString("address"));
        s.setActive(rs.getBoolean("is_active"));
        return s;
    }
}
