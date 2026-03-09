package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Doctor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO bác sĩ — truy vấn bảng Doctor JOIN User, Specialty.
 */
public class DoctorDAO implements BaseDAO<Doctor> {

    private static final Logger LOGGER = Logger.getLogger(DoctorDAO.class.getName());

    private static final String SELECT_JOIN = """
            SELECT d.doctor_id, d.user_id, d.specialty_id, d.license_no, d.is_active,
                   d.created_at, d.updated_at,
                   u.full_name, u.email, u.phone,
                   s.specialty_name
              FROM Doctor d
              JOIN `User` u ON d.user_id = u.user_id
              LEFT JOIN Specialty s ON d.specialty_id = s.specialty_id
            """;

    private Connection externalConnection;

    public DoctorDAO() {}

    public DoctorDAO(Connection connection) {
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

    @Override
    public Doctor findById(int id) {
        String sql = SELECT_JOIN + " WHERE d.doctor_id = ?";
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
            throw new DataAccessException("Lỗi truy vấn bác sĩ ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    /**
     * Tìm bác sĩ theo user_id (để liên kết tài khoản đăng nhập).
     */
    public Doctor findByUserId(long userId) {
        String sql = SELECT_JOIN + " WHERE d.user_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn bác sĩ theo userId=" + userId, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<Doctor> findAll() {
        List<Doctor> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(SELECT_JOIN + " ORDER BY d.doctor_id")) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn danh sách bác sĩ", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    /**
     * Danh sách bác sĩ đang hoạt động.
     */
    public List<Doctor> findActive() {
        List<Doctor> list = new ArrayList<>();
        String sql = SELECT_JOIN + " WHERE d.is_active = TRUE ORDER BY d.doctor_id";
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn bác sĩ đang hoạt động", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(Doctor d) {
        String sql = "INSERT INTO Doctor (user_id, specialty_id, license_no, is_active) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, d.getUserId());
                if (d.getSpecialtyId() != null) ps.setLong(2, d.getSpecialtyId()); else ps.setNull(2, Types.BIGINT);
                ps.setString(3, d.getLicenseNo());
                ps.setBoolean(4, d.isActive());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) d.setId(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm bác sĩ", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    @Override
    public boolean update(Doctor d) {
        String sql = "UPDATE Doctor SET specialty_id = ?, license_no = ?, is_active = ? WHERE doctor_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (d.getSpecialtyId() != null) ps.setLong(1, d.getSpecialtyId()); else ps.setNull(1, Types.BIGINT);
                ps.setString(2, d.getLicenseNo());
                ps.setBoolean(3, d.isActive());
                ps.setInt(4, d.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật bác sĩ ID=" + d.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE Doctor SET is_active = FALSE WHERE doctor_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể xóa bác sĩ ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    private Doctor mapResultSet(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setId(rs.getInt("doctor_id"));
        d.setUserId(rs.getLong("user_id"));
        d.setSpecialtyId(rs.getObject("specialty_id") == null ? null : rs.getLong("specialty_id"));
        d.setLicenseNo(rs.getString("license_no"));
        d.setActive(rs.getBoolean("is_active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) d.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) d.setUpdatedAt(updatedAt.toLocalDateTime());

        // Transient JOIN fields
        try { d.setFullName(rs.getString("full_name")); } catch (SQLException ignored) {}
        try { d.setEmail(rs.getString("email")); } catch (SQLException ignored) {}
        try { d.setPhone(rs.getString("phone")); } catch (SQLException ignored) {}
        try { d.setSpecialtyName(rs.getString("specialty_name")); } catch (SQLException ignored) {}

        return d;
    }
}
