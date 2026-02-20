package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.model.SampleEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO mẫu — thay thế bằng DAO thực tế của bạn.
 * Sample DAO — replace with your actual DAOs.
 */
public class SampleDAO implements BaseDAO<SampleEntity> {

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getInstance().getConnection();
    }

    @Override
    public SampleEntity findById(int id) {
        String sql = "SELECT * FROM sample_table WHERE id = ?";
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
    public List<SampleEntity> findAll() {
        List<SampleEntity> list = new ArrayList<>();
        String sql = "SELECT * FROM sample_table";
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
    public boolean insert(SampleEntity entity) {
        String sql = "INSERT INTO sample_table (name, description) VALUES (?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(SampleEntity entity) {
        String sql = "UPDATE sample_table SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getDescription());
            ps.setInt(3, entity.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM sample_table WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
