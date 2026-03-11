package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.ServiceOrder;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceOrderDAO {

    private static final Logger LOGGER = Logger.getLogger(ServiceOrderDAO.class.getName());

    private Connection externalConnection;

    public ServiceOrderDAO() {}

    public ServiceOrderDAO(Connection connection) {
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

    public long insert(ServiceOrder order) {
        String sql = """
            INSERT INTO ServiceOrder (record_id, service_id, status, ordered_at, notes)
            VALUES (?, ?, ?, NOW(), ?)
            """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, order.getRecordId());
                ps.setInt(2, order.getServiceId());
                ps.setString(3, order.getStatus());
                ps.setString(4, order.getNotes());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm ServiceOrder", e);
            throw new DataAccessException("Lỗi thêm yêu cầu dịch vụ", e);
        } finally {
            closeIfOwned(conn);
        }
        return -1;
    }

    public List<ServiceOrder> findByRecord(long recordId) {
        String sql = """
            SELECT so.*, s.service_name, s.price
            FROM ServiceOrder so
            JOIN Service s ON so.service_id = s.service_id
            WHERE so.record_id = ?
            ORDER BY so.ordered_at DESC
            """;
        List<ServiceOrder> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, recordId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn ServiceOrder theo recordId=" + recordId, e);
            throw new DataAccessException("Lỗi truy vấn yêu cầu dịch vụ", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public boolean updateStatus(long orderId, String status) {
        String sql = "UPDATE ServiceOrder SET status = ?" +
                (ServiceOrder.STATUS_COMPLETED.equals(status) ? ", completed_at = NOW()" : "") +
                " WHERE order_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setLong(2, orderId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật trạng thái ServiceOrder ID=" + orderId, e);
            throw new DataAccessException("Lỗi cập nhật trạng thái yêu cầu dịch vụ", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    private ServiceOrder mapResultSet(ResultSet rs) throws SQLException {
        ServiceOrder so = new ServiceOrder();
        so.setId(rs.getInt("order_id"));
        so.setRecordId(rs.getLong("record_id"));
        so.setServiceId(rs.getInt("service_id"));
        so.setStatus(rs.getString("status"));
        Timestamp orderedAt = rs.getTimestamp("ordered_at");
        if (orderedAt != null) so.setOrderedAt(orderedAt.toLocalDateTime());
        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) so.setCompletedAt(completedAt.toLocalDateTime());
        so.setNotes(rs.getString("notes"));
        try { so.setServiceName(rs.getString("service_name")); } catch (SQLException ignored) {}
        try {
            BigDecimal price = rs.getBigDecimal("price");
            so.setPrice(price != null ? price : BigDecimal.ZERO);
        } catch (SQLException ignored) {}
        return so;
    }
}
