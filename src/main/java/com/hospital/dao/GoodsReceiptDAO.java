package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.GoodsReceipt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoodsReceiptDAO implements BaseDAO<GoodsReceipt> {

    private static final String SELECT_JOIN =
            "SELECT gr.*, s.supplier_name " +
            "FROM GoodsReceipt gr LEFT JOIN Supplier s ON gr.supplier_id = s.supplier_id";

    private Connection externalConnection;

    public GoodsReceiptDAO() {}
    public GoodsReceiptDAO(Connection connection) { this.externalConnection = connection; }

    private Connection getConnection() throws SQLException {
        return externalConnection != null ? externalConnection : DatabaseConfig.getInstance().getConnection();
    }

    private void closeIfOwned(Connection conn) {
        if (externalConnection == null && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public GoodsReceipt findById(int id) {
        String sql = SELECT_JOIN + " WHERE gr.receipt_id = ?";
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
            throw new DataAccessException("Lỗi truy vấn phiếu nhập ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<GoodsReceipt> findAll() {
        List<GoodsReceipt> list = new ArrayList<>();
        String sql = SELECT_JOIN + " ORDER BY gr.import_date DESC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn danh sách phiếu nhập", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<GoodsReceipt> findBySupplierId(int supplierId) {
        List<GoodsReceipt> list = new ArrayList<>();
        String sql = SELECT_JOIN + " WHERE gr.supplier_id = ? ORDER BY gr.import_date DESC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, supplierId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn phiếu nhập của NCC ID=" + supplierId, e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(GoodsReceipt entity) {
        String sql = "INSERT INTO GoodsReceipt (receipt_code, supplier_id, import_date, total_amount, note, created_by, status) " +
                     "VALUES (?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, entity.getReceiptCode());
                if (entity.getSupplierId() != null) ps.setLong(2, entity.getSupplierId());
                else ps.setNull(2, Types.BIGINT);
                ps.setTimestamp(3, entity.getImportDate() != null ? Timestamp.valueOf(entity.getImportDate()) : null);
                ps.setDouble(4, entity.getTotalAmount());
                ps.setString(5, entity.getNote());
                if (entity.getCreatedBy() != null) ps.setLong(6, entity.getCreatedBy());
                else ps.setNull(6, Types.BIGINT);
                ps.setString(7, entity.getStatus());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) entity.setId(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm phiếu nhập", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    @Override
    public boolean update(GoodsReceipt entity) {
        String sql = "UPDATE GoodsReceipt SET receipt_code=?, supplier_id=?, import_date=?, total_amount=?, " +
                     "note=?, created_by=?, status=? WHERE receipt_id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, entity.getReceiptCode());
                if (entity.getSupplierId() != null) ps.setLong(2, entity.getSupplierId());
                else ps.setNull(2, Types.BIGINT);
                ps.setTimestamp(3, entity.getImportDate() != null ? Timestamp.valueOf(entity.getImportDate()) : null);
                ps.setDouble(4, entity.getTotalAmount());
                ps.setString(5, entity.getNote());
                if (entity.getCreatedBy() != null) ps.setLong(6, entity.getCreatedBy());
                else ps.setNull(6, Types.BIGINT);
                ps.setString(7, entity.getStatus());
                ps.setInt(8, entity.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật phiếu nhập ID=" + entity.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE GoodsReceipt SET status = 'CANCELLED' WHERE receipt_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể hủy phiếu nhập ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    private GoodsReceipt mapResultSet(ResultSet rs) throws SQLException {
        GoodsReceipt gr = new GoodsReceipt();
        gr.setId(rs.getInt("receipt_id"));
        gr.setReceiptCode(rs.getString("receipt_code"));
        long sid = rs.getLong("supplier_id");
        gr.setSupplierId(rs.wasNull() ? null : sid);
        gr.setImportDate(rs.getTimestamp("import_date") != null ? rs.getTimestamp("import_date").toLocalDateTime() : null);
        gr.setTotalAmount(rs.getDouble("total_amount"));
        gr.setNote(rs.getString("note"));
        long createdBy = rs.getLong("created_by");
        gr.setCreatedBy(rs.wasNull() ? null : createdBy);
        gr.setStatus(rs.getString("status"));
        gr.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        gr.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        // transient
        try { gr.setSupplierName(rs.getString("supplier_name")); } catch (SQLException ignored) {}
        return gr;
    }
}
