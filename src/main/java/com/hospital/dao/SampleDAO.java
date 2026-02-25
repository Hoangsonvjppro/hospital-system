package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.SampleEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO mẫu — thay thế bằng DAO thực tế của bạn.
 * Sample DAO — replace with your actual DAOs.
 */
public class SampleDAO implements BaseDAO<SampleEntity> {

    private Connection externalConnection;

    public SampleDAO() {
        // Mode 1: Tự lấy connection
    }

    public SampleDAO(Connection connection) {
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

    @Override
    public SampleEntity findById(int id) {
        String sql = "SELECT * FROM sample_table WHERE id = ?";
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
            throw new DataAccessException("Lỗi truy vấn sample ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<SampleEntity> findAll() {
        List<SampleEntity> list = new ArrayList<>();
        String sql = "SELECT * FROM sample_table";
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
            throw new DataAccessException("Lỗi truy vấn danh sách sample", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(SampleEntity entity) {
        String sql = "INSERT INTO sample_table (name, description) VALUES (?, ?)";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, entity.getName());
                ps.setString(2, entity.getDescription());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm sample", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean update(SampleEntity entity) {
        String sql = "UPDATE sample_table SET name = ?, description = ? WHERE id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, entity.getName());
                ps.setString(2, entity.getDescription());
                ps.setInt(3, entity.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật sample ID=" + entity.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM sample_table WHERE id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể xóa sample ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    /**
     * Map ResultSet thành entity.
     */
    private SampleEntity mapResultSet(ResultSet rs) throws SQLException {
        SampleEntity entity = new SampleEntity();
        entity.setId(rs.getInt("id"));
        entity.setName(rs.getString("name"));
        entity.setDescription(rs.getString("description"));
        return entity;
    }
}
