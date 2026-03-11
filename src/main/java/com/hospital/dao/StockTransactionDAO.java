package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.StockTransaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StockTransactionDAO {

    private static final Logger LOGGER = Logger.getLogger(StockTransactionDAO.class.getName());

    private Connection externalConnection;

    public StockTransactionDAO() {}

    public StockTransactionDAO(Connection connection) {
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

    public void insert(StockTransaction tx) {
        String sql = """
            INSERT INTO StockTransaction
                (medicine_id, transaction_type, quantity, stock_before, stock_after,
                 reference_type, reference_id, notes, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, tx.getMedicineId());
                ps.setString(2, tx.getTransactionType());
                ps.setInt(3, tx.getQuantity());
                ps.setInt(4, tx.getStockBefore());
                ps.setInt(5, tx.getStockAfter());
                ps.setString(6, tx.getReferenceType());
                if (tx.getReferenceId() != null) {
                    ps.setLong(7, tx.getReferenceId());
                } else {
                    ps.setNull(7, Types.BIGINT);
                }
                ps.setString(8, tx.getNotes());
                if (tx.getCreatedBy() != null) {
                    ps.setLong(9, tx.getCreatedBy());
                } else {
                    ps.setNull(9, Types.BIGINT);
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi ghi StockTransaction", e);
            throw new DataAccessException("Lỗi ghi StockTransaction", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    public List<StockTransaction> findByMedicine(int medicineId) {
        String sql = """
            SELECT st.*, m.medicine_name, u.full_name AS created_by_name
            FROM StockTransaction st
            LEFT JOIN Medicine m ON st.medicine_id = m.medicine_id
            LEFT JOIN `User` u ON st.created_by = u.user_id
            WHERE st.medicine_id = ?
            ORDER BY st.created_at DESC
            """;
        List<StockTransaction> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, medicineId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn StockTransaction theo medicineId=" + medicineId, e);
            throw new DataAccessException("Lỗi truy vấn lịch sử tồn kho", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<StockTransaction> findByDateRange(LocalDate from, LocalDate to) {
        String sql = """
            SELECT st.*, m.medicine_name, u.full_name AS created_by_name
            FROM StockTransaction st
            LEFT JOIN Medicine m ON st.medicine_id = m.medicine_id
            LEFT JOIN `User` u ON st.created_by = u.user_id
            WHERE DATE(st.created_at) BETWEEN ? AND ?
            ORDER BY st.created_at DESC
            """;
        List<StockTransaction> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(from));
                ps.setDate(2, Date.valueOf(to));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn StockTransaction theo khoảng ngày", e);
            throw new DataAccessException("Lỗi truy vấn lịch sử tồn kho theo ngày", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<StockTransaction> findByType(String type) {
        String sql = """
            SELECT st.*, m.medicine_name, u.full_name AS created_by_name
            FROM StockTransaction st
            LEFT JOIN Medicine m ON st.medicine_id = m.medicine_id
            LEFT JOIN `User` u ON st.created_by = u.user_id
            WHERE st.transaction_type = ?
            ORDER BY st.created_at DESC
            """;
        List<StockTransaction> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, type);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn StockTransaction theo type=" + type, e);
            throw new DataAccessException("Lỗi truy vấn lịch sử tồn kho theo loại", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<StockTransaction> findAll() {
        String sql = """
            SELECT st.*, m.medicine_name, u.full_name AS created_by_name
            FROM StockTransaction st
            LEFT JOIN Medicine m ON st.medicine_id = m.medicine_id
            LEFT JOIN `User` u ON st.created_by = u.user_id
            ORDER BY st.created_at DESC
            """;
        List<StockTransaction> list = new ArrayList<>();
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
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn tất cả StockTransaction", e);
            throw new DataAccessException("Lỗi truy vấn lịch sử tồn kho", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    private StockTransaction mapResultSet(ResultSet rs) throws SQLException {
        StockTransaction tx = new StockTransaction();
        tx.setId(rs.getInt("transaction_id"));
        tx.setMedicineId(rs.getInt("medicine_id"));
        tx.setTransactionType(rs.getString("transaction_type"));
        tx.setQuantity(rs.getInt("quantity"));
        tx.setStockBefore(rs.getInt("stock_before"));
        tx.setStockAfter(rs.getInt("stock_after"));
        tx.setReferenceType(rs.getString("reference_type"));
        long refId = rs.getLong("reference_id");
        tx.setReferenceId(rs.wasNull() ? null : refId);
        tx.setNotes(rs.getString("notes"));
        long createdBy = rs.getLong("created_by");
        tx.setCreatedBy(rs.wasNull() ? null : createdBy);
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) tx.setCreatedAt(createdAt.toLocalDateTime());
        try { tx.setMedicineName(rs.getString("medicine_name")); } catch (SQLException ignored) {}
        try { tx.setCreatedByName(rs.getString("created_by_name")); } catch (SQLException ignored) {}
        return tx;
    }
}
