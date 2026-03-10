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
 * DAO bác sĩ – thao tác trên bảng Doctor trong CSDL.
 */
public class DoctorDAO implements BaseDAO<Doctor> {

    private static final Logger LOGGER = Logger.getLogger(DoctorDAO.class.getName());
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
        String sql = "SELECT d.*, u.full_name, u.email, u.phone FROM Doctor d JOIN `User` u ON d.user_id = u.user_id WHERE d.doctor_id = ?";
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
            LOGGER.log(Level.SEVERE, "Lỗi tìm doctor id=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    public Doctor findByUserId(int userId) {
        String sql = "SELECT d.*, u.full_name, u.email, u.phone FROM Doctor d JOIN `User` u ON d.user_id = u.user_id WHERE d.user_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm doctor by userId=" + userId, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<Doctor> findAll() {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT d.*, u.full_name, u.email, u.phone FROM Doctor d JOIN `User` u ON d.user_id = u.user_id";
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
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách bác sĩ", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(Doctor d) {
        // Implementation for inserting into Doctor table would go here
        return false;
    }

    @Override
    public boolean update(Doctor d) {
        // Implementation for updating Doctor table would go here
        return false;
    }

    @Override
    public boolean delete(int id) {
        // Implementation for deleting from Doctor table would go here
        return false;
    }

    public List<Doctor> findOnline() {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT d.*, u.full_name, u.email, u.phone FROM Doctor d JOIN `User` u ON d.user_id = u.user_id WHERE d.is_active = TRUE";
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
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách bác sĩ online", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public int countOnline() {
        String sql = "SELECT COUNT(*) FROM Doctor WHERE is_active = TRUE";
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm bác sĩ online", e);
        } finally {
            closeIfOwned(conn);
        }
        return 0;
    }

    private Doctor mapResultSet(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setId(rs.getInt("doctor_id"));
        d.setDoctorCode(rs.getString("license_no")); // Using license_no as doctorCode for now
        d.setFullName(rs.getString("full_name"));
        d.setSpecialty(rs.getString("specialty"));
        d.setPhone(rs.getString("phone"));
        d.setEmail(rs.getString("email"));
        d.setOnline(true);
        return d;
    }
}
