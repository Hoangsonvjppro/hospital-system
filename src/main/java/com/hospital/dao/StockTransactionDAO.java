package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.StockTransaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockTransactionDAO implements BaseDAO<StockTransaction> {

    private static final String SELECT_JOIN =
            "SELECT st.*, m.medicine_name, mb.batch_number " +
            "FROM StockTransaction st " +
            "LEFT JOIN Medicine m ON st.medicine_id = m.medicine_id " +
            "LEFT JOIN MedicineBatch mb ON st.batch_id = mb.batch_id";

    private Connection externalConnection;

    public StockTransactionDAO() {}
    public StockTransactionDAO(Connection connection) { this.externalConnection = connection; }

    private Connection getConnection() throws SQLException {
        return externalConnection != null ? externalConnection : DatabaseConfig.getInstance().getConnection();
    }

    private void closeIfOwned(Connection conn) {
        if (externalConnection == null && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public StockTransaction findById(int id) {
        String sql = SELECT_JOIN + " WHERE st.transaction_id = ?";
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
            throw new DataAccessException("Lỗi truy vấn giao dịch kho ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<StockTransaction> findAll() {
        List<StockTransaction> list = new ArrayList<>();
        String sql = SELECT_JOIN + " ORDER BY st.created_at DESC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn danh sách giao dịch kho", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<StockTransaction> findByMedicineId(long medicineId) {
        List<StockTransaction> list = new ArrayList<>();
        String sql = SELECT_JOIN + " WHERE st.medicine_id = ? ORDER BY st.created_at DESC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, medicineId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn giao dịch kho thuốc ID=" + medicineId, e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<StockTransaction> findByBatchId(long batchId) {
        List<StockTransaction> list = new ArrayList<>();
        String sql = SELECT_JOIN + " WHERE st.batch_id = ? ORDER BY st.created_at DESC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, batchId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn giao dịch kho theo lô ID=" + batchId, e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<StockTransaction> findByType(String transactionType) {
        List<StockTransaction> list = new ArrayList<>();
        String sql = SELECT_JOIN + " WHERE st.transaction_type = ? ORDER BY st.created_at DESC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, transactionType);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn giao dịch kho loại " + transactionType, e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(StockTransaction entity) {
        String sql = "INSERT INTO StockTransaction (medicine_id, batch_id, transaction_type, quantity, " +
                     "stock_before, stock_after, reference_id, notes, created_by) VALUES (?,?,?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, entity.getMedicineId());
                if (entity.getBatchId() != 0) ps.setLong(2, entity.getBatchId());
                else ps.setNull(2, Types.BIGINT);
                ps.setString(3, entity.getTransactionType());
                ps.setInt(4, entity.getQuantity());
                ps.setInt(5, entity.getStockBefore());
                ps.setInt(6, entity.getStockAfter());
                if (entity.getReferenceId() != null) ps.setLong(7, entity.getReferenceId());
                else ps.setNull(7, Types.BIGINT);
                ps.setString(8, entity.getNotes());
                if (entity.getCreatedBy() != null) ps.setLong(9, entity.getCreatedBy());
                else ps.setNull(9, Types.BIGINT);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) entity.setId(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm giao dịch kho", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    @Override
    public boolean update(StockTransaction entity) {
        // StockTransaction is an audit log — generally should not be updated
        throw new UnsupportedOperationException("StockTransaction là nhật ký — không hỗ trợ cập nhật");
    }

    @Override
    public boolean delete(int id) {
        // StockTransaction is an audit log — generally should not be deleted
        throw new UnsupportedOperationException("StockTransaction là nhật ký — không hỗ trợ xóa");
    }

    private StockTransaction mapResultSet(ResultSet rs) throws SQLException {
        StockTransaction st = new StockTransaction();
        st.setId(rs.getInt("transaction_id"));
        st.setMedicineId(rs.getLong("medicine_id"));
        long batchId = rs.getLong("batch_id");
        st.setBatchId(rs.wasNull() ? null : batchId);
        st.setTransactionType(rs.getString("transaction_type"));
        st.setQuantity(rs.getInt("quantity"));
        st.setStockBefore(rs.getInt("stock_before"));
        st.setStockAfter(rs.getInt("stock_after"));
        long refId = rs.getLong("reference_id");
        st.setReferenceId(rs.wasNull() ? null : refId);
        st.setNotes(rs.getString("notes"));
        long createdBy = rs.getLong("created_by");
        st.setCreatedBy(rs.wasNull() ? null : createdBy);
        st.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        // transients from JOIN
        try { st.setMedicineName(rs.getString("medicine_name")); } catch (SQLException ignored) {}
        try { st.setBatchNumber(rs.getString("batch_number")); } catch (SQLException ignored) {}
        return st;
    }
}
