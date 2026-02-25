package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO benh nhan — truy van bang Patient trong CSDL.
 */
public class PatientDAO implements BaseDAO<Patient> {

    private static final Logger LOGGER = Logger.getLogger(PatientDAO.class.getName());

    private Connection externalConnection;

    public PatientDAO() {
        // Mode 1: Tự lấy connection (cho thao tác đơn lẻ)
    }

    public PatientDAO(Connection connection) {
        // Mode 2: Dùng external connection (cho transaction)
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

    // ── CRUD co ban (tu main) ─────────────────────────────────

    @Override
    public Patient findById(int id) {
        String sql = "SELECT * FROM Patient WHERE patient_id = ?";
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
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn bệnh nhân ID=" + id, e);
            throw new DataAccessException("Lỗi truy vấn bệnh nhân ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<Patient> findAll() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM Patient WHERE is_active = true";
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stm = conn.createStatement();
                 ResultSet rs = stm.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách bệnh nhân", e);
            throw new DataAccessException("Lỗi truy vấn danh sách bệnh nhân", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(Patient entity) {
        String sql = """
                INSERT INTO Patient
                (full_name, gender, date_of_birth, phone, address,
                 is_active)
                VALUES (?,?,?,?,?,?)
                """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, entity.getFullName());
                if (entity.getGender() != null) {
                    ps.setString(2, entity.getGender().name());
                } else {
                    ps.setString(2, "OTHER");
                }
                if (entity.getDateOfBirth() != null) {
                    ps.setDate(3, Date.valueOf(entity.getDateOfBirth()));
                } else {
                    ps.setNull(3, Types.DATE);
                }
                ps.setString(4, entity.getPhone());
                ps.setString(5, entity.getAddress());
                ps.setBoolean(6, entity.isActive());

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys.next()) {
                        entity.setId(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể thêm bệnh nhân", e);
            throw new DataAccessException("Không thể thêm bệnh nhân", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    @Override
    public boolean update(Patient entity) {
        String sql = """
                UPDATE Patient
                SET full_name=?,
                    gender=?,
                    date_of_birth=?,
                    phone=?,
                    address=?,
                    is_active=?
                WHERE patient_id=?
                """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, entity.getFullName());
                if (entity.getGender() != null) {
                    ps.setString(2, entity.getGender().name());
                } else {
                    ps.setString(2, "OTHER");
                }
                if (entity.getDateOfBirth() != null) {
                    ps.setDate(3, Date.valueOf(entity.getDateOfBirth()));
                } else {
                    ps.setNull(3, Types.DATE);
                }
                ps.setString(4, entity.getPhone());
                ps.setString(5, entity.getAddress());
                ps.setBoolean(6, entity.isActive());
                ps.setInt(7, entity.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật bệnh nhân ID=" + entity.getId(), e);
            throw new DataAccessException("Không thể cập nhật bệnh nhân ID=" + entity.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE Patient SET is_active=false WHERE patient_id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể xóa bệnh nhân ID=" + id, e);
            throw new DataAccessException("Không thể xóa bệnh nhân ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    // -- Helper --

    private Patient mapResultSet(ResultSet rs) throws SQLException {

        Patient p = new Patient();

        p.setId(rs.getInt("patient_id"));
        p.setFullName(rs.getString("full_name"));
        p.setPhone(rs.getString("phone"));
        p.setAddress(rs.getString("address"));

        String genderStr = rs.getString("gender");
        if (genderStr != null) {
            p.setGender(Patient.Gender.valueOf(genderStr));
        }

        if (rs.getDate("date_of_birth") != null) {
            p.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        }

        p.setActive(rs.getBoolean("is_active"));

        if (rs.getTimestamp("created_at") != null) {
            p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }

        if (rs.getTimestamp("updated_at") != null) {
            p.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }

        return p;
    }
}
