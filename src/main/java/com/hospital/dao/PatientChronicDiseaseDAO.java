package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.PatientChronicDisease;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientChronicDiseaseDAO implements BaseDAO<PatientChronicDisease> {

    private static final String SELECT_JOIN =
            "SELECT pcd.*, icd.disease_name " +
            "FROM PatientChronicDisease pcd LEFT JOIN ICD10Code icd ON pcd.icd10_code = icd.icd10_code";

    private Connection externalConnection;

    public PatientChronicDiseaseDAO() {}
    public PatientChronicDiseaseDAO(Connection connection) { this.externalConnection = connection; }

    private Connection getConnection() throws SQLException {
        return externalConnection != null ? externalConnection : DatabaseConfig.getInstance().getConnection();
    }

    private void closeIfOwned(Connection conn) {
        if (externalConnection == null && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public PatientChronicDisease findById(int id) {
        String sql = SELECT_JOIN + " WHERE pcd.chronic_id = ?";
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
            throw new DataAccessException("Lỗi truy vấn bệnh mãn tính ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<PatientChronicDisease> findAll() {
        List<PatientChronicDisease> list = new ArrayList<>();
        String sql = SELECT_JOIN + " ORDER BY pcd.diagnosed_at DESC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn danh sách bệnh mãn tính", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<PatientChronicDisease> findByPatientId(long patientId) {
        List<PatientChronicDisease> list = new ArrayList<>();
        String sql = SELECT_JOIN + " WHERE pcd.patient_id = ? ORDER BY pcd.diagnosed_at DESC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn bệnh mãn tính của bệnh nhân ID=" + patientId, e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<PatientChronicDisease> findActiveByPatientId(long patientId) {
        List<PatientChronicDisease> list = new ArrayList<>();
        String sql = SELECT_JOIN + " WHERE pcd.patient_id = ? AND pcd.is_active = TRUE ORDER BY pcd.diagnosed_at DESC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn bệnh mãn tính đang hoạt động của BN ID=" + patientId, e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(PatientChronicDisease entity) {
        String sql = "INSERT INTO PatientChronicDisease (patient_id, icd10_code, diagnosed_at, is_active, note) " +
                     "VALUES (?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, entity.getPatientId());
                ps.setString(2, entity.getIcd10Code());
                ps.setDate(3, entity.getDiagnosedAt() != null ? Date.valueOf(entity.getDiagnosedAt()) : null);
                ps.setBoolean(4, entity.isActive());
                ps.setString(5, entity.getNote());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) entity.setId(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm bệnh mãn tính", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    @Override
    public boolean update(PatientChronicDisease entity) {
        String sql = "UPDATE PatientChronicDisease SET patient_id=?, icd10_code=?, diagnosed_at=?, is_active=?, note=? " +
                     "WHERE chronic_id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, entity.getPatientId());
                ps.setString(2, entity.getIcd10Code());
                ps.setDate(3, entity.getDiagnosedAt() != null ? Date.valueOf(entity.getDiagnosedAt()) : null);
                ps.setBoolean(4, entity.isActive());
                ps.setString(5, entity.getNote());
                ps.setInt(6, entity.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật bệnh mãn tính ID=" + entity.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM PatientChronicDisease WHERE chronic_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể xóa bệnh mãn tính ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    private PatientChronicDisease mapResultSet(ResultSet rs) throws SQLException {
        PatientChronicDisease pcd = new PatientChronicDisease();
        pcd.setId(rs.getInt("chronic_id"));
        pcd.setPatientId(rs.getLong("patient_id"));
        pcd.setIcd10Code(rs.getString("icd10_code"));
        pcd.setDiagnosedAt(rs.getDate("diagnosed_at") != null ? rs.getDate("diagnosed_at").toLocalDate() : null);
        pcd.setActive(rs.getBoolean("is_active"));
        pcd.setNote(rs.getString("note"));
        pcd.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        pcd.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        // transient from JOIN
        try { pcd.setDiseaseName(rs.getString("disease_name")); } catch (SQLException ignored) {}
        return pcd;
    }
}
