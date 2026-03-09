package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.MedicalAttachment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicalAttachmentDAO implements BaseDAO<MedicalAttachment> {

    private Connection externalConnection;

    public MedicalAttachmentDAO() {}
    public MedicalAttachmentDAO(Connection connection) { this.externalConnection = connection; }

    private Connection getConnection() throws SQLException {
        return externalConnection != null ? externalConnection : DatabaseConfig.getInstance().getConnection();
    }

    private void closeIfOwned(Connection conn) {
        if (externalConnection == null && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public MedicalAttachment findById(int id) {
        String sql = "SELECT * FROM MedicalAttachment WHERE attachment_id = ?";
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
            throw new DataAccessException("Lỗi truy vấn tệp đính kèm ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<MedicalAttachment> findAll() {
        List<MedicalAttachment> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM MedicalAttachment ORDER BY uploaded_at DESC")) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn danh sách tệp đính kèm", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<MedicalAttachment> findByRecordId(long recordId) {
        List<MedicalAttachment> list = new ArrayList<>();
        String sql = "SELECT * FROM MedicalAttachment WHERE record_id = ? ORDER BY uploaded_at DESC";
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
            throw new DataAccessException("Lỗi truy vấn tệp đính kèm của hồ sơ ID=" + recordId, e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<MedicalAttachment> findByServiceOrderId(long serviceOrderId) {
        List<MedicalAttachment> list = new ArrayList<>();
        String sql = "SELECT * FROM MedicalAttachment WHERE service_order_id = ? ORDER BY uploaded_at DESC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, serviceOrderId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn tệp đính kèm của yêu cầu dịch vụ ID=" + serviceOrderId, e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(MedicalAttachment entity) {
        String sql = "INSERT INTO MedicalAttachment (record_id, service_order_id, file_url, file_type, description) " +
                     "VALUES (?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, entity.getRecordId());
                if (entity.getServiceOrderId() != null) ps.setLong(2, entity.getServiceOrderId());
                else ps.setNull(2, Types.BIGINT);
                ps.setString(3, entity.getFileUrl());
                ps.setString(4, entity.getFileType());
                ps.setString(5, entity.getDescription());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) entity.setId(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm tệp đính kèm", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    @Override
    public boolean update(MedicalAttachment entity) {
        String sql = "UPDATE MedicalAttachment SET record_id=?, service_order_id=?, file_url=?, file_type=?, " +
                     "description=? WHERE attachment_id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, entity.getRecordId());
                if (entity.getServiceOrderId() != null) ps.setLong(2, entity.getServiceOrderId());
                else ps.setNull(2, Types.BIGINT);
                ps.setString(3, entity.getFileUrl());
                ps.setString(4, entity.getFileType());
                ps.setString(5, entity.getDescription());
                ps.setInt(6, entity.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật tệp đính kèm ID=" + entity.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM MedicalAttachment WHERE attachment_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể xóa tệp đính kèm ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    private MedicalAttachment mapResultSet(ResultSet rs) throws SQLException {
        MedicalAttachment ma = new MedicalAttachment();
        ma.setId(rs.getInt("attachment_id"));
        ma.setRecordId(rs.getLong("record_id"));
        long soId = rs.getLong("service_order_id");
        ma.setServiceOrderId(rs.wasNull() ? null : soId);
        ma.setFileUrl(rs.getString("file_url"));
        ma.setFileType(rs.getString("file_type"));
        ma.setDescription(rs.getString("description"));
        ma.setUploadedAt(rs.getTimestamp("uploaded_at") != null ? rs.getTimestamp("uploaded_at").toLocalDateTime() : null);
        return ma;
    }
}
