package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Service;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceDAO implements BaseDAO<Service> {

    private static final Logger LOGGER = Logger.getLogger(ServiceDAO.class.getName());

    @Override
    public Service findById(int id) {
        String sql = "SELECT * FROM Service WHERE service_id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn dịch vụ ID=" + id, e);
            throw new DataAccessException("Lỗi truy vấn dịch vụ", e);
        }
        return null;
    }

    @Override
    public List<Service> findAll() {
        String sql = "SELECT * FROM Service ORDER BY service_name";
        List<Service> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách dịch vụ", e);
            throw new DataAccessException("Lỗi truy vấn danh sách dịch vụ", e);
        }
        return list;
    }

    public List<Service> findActive() {
        String sql = "SELECT * FROM Service WHERE is_active = TRUE ORDER BY service_name";
        List<Service> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn dịch vụ đang hoạt động", e);
            throw new DataAccessException("Lỗi truy vấn dịch vụ", e);
        }
        return list;
    }

    @Override
    public boolean insert(Service entity) {
        String sql = "INSERT INTO Service (service_name, price, description, is_active) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getServiceName());
            ps.setBigDecimal(2, entity.getPrice());
            ps.setString(3, entity.getDescription());
            ps.setBoolean(4, entity.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm dịch vụ", e);
            throw new DataAccessException("Lỗi thêm dịch vụ", e);
        }
    }

    @Override
    public boolean update(Service entity) {
        String sql = "UPDATE Service SET service_name = ?, price = ?, description = ?, is_active = ?, updated_at = NOW() WHERE service_id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getServiceName());
            ps.setBigDecimal(2, entity.getPrice());
            ps.setString(3, entity.getDescription());
            ps.setBoolean(4, entity.isActive());
            ps.setInt(5, entity.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật dịch vụ ID=" + entity.getId(), e);
            throw new DataAccessException("Lỗi cập nhật dịch vụ", e);
        }
    }

    @Override
    public boolean delete(int id) {
        // Soft delete
        String sql = "UPDATE Service SET is_active = FALSE, updated_at = NOW() WHERE service_id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi vô hiệu hóa dịch vụ ID=" + id, e);
            throw new DataAccessException("Lỗi vô hiệu hóa dịch vụ", e);
        }
    }

    private Service mapResultSet(ResultSet rs) throws SQLException {
        Service s = new Service();
        s.setId(rs.getInt("service_id"));
        s.setServiceName(rs.getString("service_name"));
        BigDecimal price = rs.getBigDecimal("price");
        s.setPrice(price != null ? price : BigDecimal.ZERO);
        s.setDescription(rs.getString("description"));
        s.setActive(rs.getBoolean("is_active"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) s.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) s.setUpdatedAt(updatedAt.toLocalDateTime());
        return s;
    }
}
