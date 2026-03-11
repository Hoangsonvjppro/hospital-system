package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Account;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO implements BaseDAO<Account> {

    private Connection externalConnection;

    public AccountDAO() {
    }

    public AccountDAO(Connection connection) {
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

    @Override
    public Account findById(int id) {
        String sql = "SELECT * FROM `User` WHERE user_id = ?";
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
            throw new DataAccessException("Lỗi truy vấn tài khoản ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<Account> findAll() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM `User`";
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn danh sách tài khoản", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(Account account) {
        String sql = "INSERT INTO `User` (username, password_hash, full_name, email, phone, role_id, is_active) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, account.getUsername());
                ps.setString(2, account.getPasswordHash());
                ps.setString(3, account.getFullName());
                ps.setString(4, account.getEmail());
                ps.setString(5, account.getPhone());
                ps.setInt(6, account.getRoleId());
                ps.setBoolean(7, account.isActive());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm tài khoản", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean update(Account account) {
        String sql = "UPDATE `User` SET username = ?, password_hash = ?, full_name = ?, "
                   + "email = ?, phone = ?, role_id = ?, is_active = ? WHERE user_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, account.getUsername());
                ps.setString(2, account.getPasswordHash());
                ps.setString(3, account.getFullName());
                ps.setString(4, account.getEmail());
                ps.setString(5, account.getPhone());
                ps.setInt(6, account.getRoleId());
                ps.setBoolean(7, account.isActive());
                ps.setInt(8, account.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật tài khoản ID=" + account.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM `User` WHERE user_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể xóa tài khoản ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    public Account findByUsername(String username) {
        String sql = "SELECT * FROM `User` WHERE username = ? AND is_active = TRUE";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi tìm tài khoản: " + username, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM `User` WHERE username = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi kiểm tra username: " + username, e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    private Account mapResultSet(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getInt("user_id"));
        account.setUsername(rs.getString("username"));
        account.setPasswordHash(rs.getString("password_hash"));
        account.setFullName(rs.getString("full_name"));
        account.setEmail(rs.getString("email"));
        account.setPhone(rs.getString("phone"));
        account.setRoleId(rs.getInt("role_id"));
        account.setActive(rs.getBoolean("is_active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            account.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            account.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return account;
    }
}
