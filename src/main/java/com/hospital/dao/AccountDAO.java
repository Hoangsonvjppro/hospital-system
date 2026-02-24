package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.model.Account;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho tài khoản đăng nhập — thao tác CRUD trên bảng `User`.
 * Account DAO — CRUD operations on the `User` table.
 *
 * Lưu ý:
 * - findByUsername(): tìm tài khoản theo username (phục vụ đăng nhập).
 * - Việc so sánh password hash (BCrypt) sẽ được thực hiện ở tầng BUS,
 *   KHÔNG so khớp trực tiếp trong câu SQL.
 */
public class AccountDAO implements BaseDAO<Account> {

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getInstance().getConnection();
    }

    // ── CRUD cơ bản (BaseDAO) ─────────────────────────────────

    @Override
    public Account findById(int id) {
        String sql = "SELECT * FROM `User` WHERE user_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Account> findAll() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM `User`";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean insert(Account account) {
        String sql = "INSERT INTO `User` (username, password_hash, full_name, email, phone, role_id, is_active) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getPasswordHash());
            ps.setString(3, account.getFullName());
            ps.setString(4, account.getEmail());
            ps.setString(5, account.getPhone());
            ps.setLong(6, account.getRoleId());
            ps.setBoolean(7, account.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Account account) {
        String sql = "UPDATE `User` SET username = ?, password_hash = ?, full_name = ?, "
                   + "email = ?, phone = ?, role_id = ?, is_active = ? WHERE user_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getPasswordHash());
            ps.setString(3, account.getFullName());
            ps.setString(4, account.getEmail());
            ps.setString(5, account.getPhone());
            ps.setLong(6, account.getRoleId());
            ps.setBoolean(7, account.isActive());
            ps.setInt(8, account.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM `User` WHERE user_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── Truy vấn phục vụ đăng nhập ───────────────────────────

    /**
     * Tìm tài khoản theo username.
     * Dùng cho chức năng đăng nhập: lấy Account ra rồi so sánh
     * password hash ở tầng BUS bằng BCrypt.
     *
     * @param username tên đăng nhập
     * @return Account nếu tìm thấy, null nếu không tồn tại
     */
    public Account findByUsername(String username) {
        String sql = "SELECT * FROM `User` WHERE username = ? AND is_active = TRUE";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Kiểm tra username đã tồn tại trong hệ thống chưa.
     * Dùng khi tạo tài khoản mới để tránh trùng lặp.
     *
     * @param username tên đăng nhập cần kiểm tra
     * @return true nếu đã tồn tại
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM `User` WHERE username = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── Helper ────────────────────────────────────────────────

    /**
     * Map ResultSet thành entity Account.
     */
    private Account mapResultSet(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getInt("user_id"));
        account.setUsername(rs.getString("username"));
        account.setPasswordHash(rs.getString("password_hash"));
        account.setFullName(rs.getString("full_name"));
        account.setEmail(rs.getString("email"));
        account.setPhone(rs.getString("phone"));
        account.setRoleId(rs.getLong("role_id"));
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
