package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.PatientAllergy;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO tiền sử dị ứng bệnh nhân.
 */
public class PatientAllergyDAO {

    private static final Logger LOGGER = Logger.getLogger(PatientAllergyDAO.class.getName());

    private Connection externalConnection;

    public PatientAllergyDAO() {}

    public PatientAllergyDAO(Connection connection) {
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

    // ── Mapping ─────────────────────────────────────────────────────────────

    private PatientAllergy mapRow(ResultSet rs) throws SQLException {
        PatientAllergy a = new PatientAllergy();
        a.setId(rs.getInt("allergy_id"));
        a.setPatientId(rs.getLong("patient_id"));
        a.setAllergenName(rs.getString("allergen_name"));
        a.setSeverity(rs.getString("severity"));
        a.setReaction(rs.getString("reaction"));
        a.setNotes(rs.getString("notes"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) a.setCreatedAt(ca.toLocalDateTime());
        return a;
    }

    // ── Queries ─────────────────────────────────────────────────────────────

    public PatientAllergy findById(int id) {
        String sql = "SELECT * FROM PatientAllergy WHERE allergy_id = ?";
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
            LOGGER.log(Level.SEVERE, "Lỗi tìm allergy id=" + id, e);
            throw new DataAccessException("Không thể tìm dị ứng", e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    public List<PatientAllergy> findAll() {
        String sql = "SELECT * FROM PatientAllergy ORDER BY created_at DESC";
        List<PatientAllergy> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách dị ứng", e);
            throw new DataAccessException("Không thể lấy danh sách dị ứng", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    /**
     * Lấy dị ứng theo bệnh nhân.
     */
    public List<PatientAllergy> findByPatientId(long patientId) {
        String sql = "SELECT * FROM PatientAllergy WHERE patient_id = ? ORDER BY severity DESC, allergen_name";
        List<PatientAllergy> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm dị ứng theo patientId=" + patientId, e);
            throw new DataAccessException("Không thể tìm dị ứng theo bệnh nhân", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    // ── CUD ─────────────────────────────────────────────────────────────────

    public boolean insert(PatientAllergy a) {
        String sql = """
                INSERT INTO PatientAllergy (patient_id, allergen_name, severity, reaction, notes)
                VALUES (?, ?, ?, ?, ?)
                """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, a.getPatientId());
                ps.setString(2, a.getAllergenName());
                ps.setString(3, a.getSeverity());
                ps.setString(4, a.getReaction());
                ps.setString(5, a.getNotes());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) a.setId(rs.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm dị ứng", e);
            throw new DataAccessException("Không thể thêm dị ứng", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    public boolean update(PatientAllergy a) {
        String sql = """
                UPDATE PatientAllergy SET patient_id = ?, allergen_name = ?,
                    severity = ?, reaction = ?, notes = ?
                WHERE allergy_id = ?
                """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, a.getPatientId());
                ps.setString(2, a.getAllergenName());
                ps.setString(3, a.getSeverity());
                ps.setString(4, a.getReaction());
                ps.setString(5, a.getNotes());
                ps.setInt(6, a.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật dị ứng id=" + a.getId(), e);
            throw new DataAccessException("Không thể cập nhật dị ứng", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM PatientAllergy WHERE allergy_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa dị ứng id=" + id, e);
            throw new DataAccessException("Không thể xóa dị ứng", e);
        } finally {
            closeIfOwned(conn);
        }
    }
}
