package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.MedicineBatch;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicineBatchDAO implements BaseDAO<MedicineBatch> {

    private static final String SELECT_JOIN =
            "SELECT mb.*, m.medicine_name, m.medicine_code, m.unit " +
            "FROM MedicineBatch mb LEFT JOIN Medicine m ON mb.medicine_id = m.medicine_id";

    private Connection externalConnection;

    public MedicineBatchDAO() {}
    public MedicineBatchDAO(Connection connection) { this.externalConnection = connection; }

    private Connection getConnection() throws SQLException {
        return externalConnection != null ? externalConnection : DatabaseConfig.getInstance().getConnection();
    }

    private void closeIfOwned(Connection conn) {
        if (externalConnection == null && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public MedicineBatch findById(int id) {
        String sql = SELECT_JOIN + " WHERE mb.batch_id = ?";
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
            throw new DataAccessException("Lỗi truy vấn lô thuốc ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<MedicineBatch> findAll() {
        List<MedicineBatch> list = new ArrayList<>();
        String sql = SELECT_JOIN + " ORDER BY mb.expiry_date ASC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn danh sách lô thuốc", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<MedicineBatch> findByMedicineId(long medicineId) {
        List<MedicineBatch> list = new ArrayList<>();
        String sql = SELECT_JOIN + " WHERE mb.medicine_id = ? ORDER BY mb.expiry_date ASC";
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
            throw new DataAccessException("Lỗi truy vấn lô thuốc của thuốc ID=" + medicineId, e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    /** FEFO: tìm lô có hạn sớm nhất còn tồn kho */
    public List<MedicineBatch> findAvailableFEFO(long medicineId) {
        List<MedicineBatch> list = new ArrayList<>();
        String sql = SELECT_JOIN +
                " WHERE mb.medicine_id = ? AND mb.current_qty > 0 AND mb.expiry_date > CURDATE()" +
                " ORDER BY mb.expiry_date ASC";
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
            throw new DataAccessException("Lỗi truy vấn lô thuốc FEFO cho thuốc ID=" + medicineId, e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    /** Lô sắp hết hạn trong N ngày */
    public List<MedicineBatch> findExpiringSoon(int withinDays) {
        List<MedicineBatch> list = new ArrayList<>();
        String sql = SELECT_JOIN +
                " WHERE mb.current_qty > 0 AND mb.expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY)" +
                " ORDER BY mb.expiry_date ASC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, withinDays);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn lô thuốc sắp hết hạn", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    /** Lô có tồn kho dưới ngưỡng tối thiểu */
    public List<MedicineBatch> findLowStock() {
        List<MedicineBatch> list = new ArrayList<>();
        String sql = SELECT_JOIN +
                " WHERE mb.current_qty > 0 AND mb.current_qty <= mb.min_threshold" +
                " ORDER BY mb.current_qty ASC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn lô thuốc tồn kho thấp", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<MedicineBatch> findByReceiptId(int receiptId) {
        List<MedicineBatch> list = new ArrayList<>();
        String sql = SELECT_JOIN + " WHERE mb.receipt_id = ? ORDER BY mb.batch_id";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, receiptId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn lô thuốc theo phiếu nhập ID=" + receiptId, e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(MedicineBatch entity) {
        String sql = "INSERT INTO MedicineBatch (receipt_id, medicine_id, batch_number, manufacture_date, " +
                     "expiry_date, import_price, sell_price, initial_qty, current_qty, min_threshold) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                if (entity.getReceiptId() != 0) ps.setLong(1, entity.getReceiptId());
                else ps.setNull(1, Types.BIGINT);
                ps.setLong(2, entity.getMedicineId());
                ps.setString(3, entity.getBatchNumber());
                ps.setDate(4, entity.getManufactureDate() != null ? Date.valueOf(entity.getManufactureDate()) : null);
                ps.setDate(5, entity.getExpiryDate() != null ? Date.valueOf(entity.getExpiryDate()) : null);
                ps.setDouble(6, entity.getImportPrice());
                ps.setDouble(7, entity.getSellPrice());
                ps.setInt(8, entity.getInitialQty());
                ps.setInt(9, entity.getCurrentQty());
                ps.setInt(10, entity.getMinThreshold());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) entity.setId(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm lô thuốc", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    @Override
    public boolean update(MedicineBatch entity) {
        String sql = "UPDATE MedicineBatch SET receipt_id=?, medicine_id=?, batch_number=?, manufacture_date=?, " +
                     "expiry_date=?, import_price=?, sell_price=?, initial_qty=?, current_qty=?, min_threshold=? " +
                     "WHERE batch_id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (entity.getReceiptId() != 0) ps.setLong(1, entity.getReceiptId());
                else ps.setNull(1, Types.BIGINT);
                ps.setLong(2, entity.getMedicineId());
                ps.setString(3, entity.getBatchNumber());
                ps.setDate(4, entity.getManufactureDate() != null ? Date.valueOf(entity.getManufactureDate()) : null);
                ps.setDate(5, entity.getExpiryDate() != null ? Date.valueOf(entity.getExpiryDate()) : null);
                ps.setDouble(6, entity.getImportPrice());
                ps.setDouble(7, entity.getSellPrice());
                ps.setInt(8, entity.getInitialQty());
                ps.setInt(9, entity.getCurrentQty());
                ps.setInt(10, entity.getMinThreshold());
                ps.setInt(11, entity.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật lô thuốc ID=" + entity.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    /** Cập nhật tồn kho nhanh */
    public boolean updateCurrentQty(int batchId, int newQty) {
        String sql = "UPDATE MedicineBatch SET current_qty = ? WHERE batch_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, newQty);
                ps.setInt(2, batchId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật tồn kho lô ID=" + batchId, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM MedicineBatch WHERE batch_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể xóa lô thuốc ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    private MedicineBatch mapResultSet(ResultSet rs) throws SQLException {
        MedicineBatch mb = new MedicineBatch();
        mb.setId(rs.getInt("batch_id"));
        long receiptId = rs.getLong("receipt_id");
        mb.setReceiptId(receiptId);
        mb.setMedicineId(rs.getLong("medicine_id"));
        mb.setBatchNumber(rs.getString("batch_number"));
        mb.setManufactureDate(rs.getDate("manufacture_date") != null ? rs.getDate("manufacture_date").toLocalDate() : null);
        mb.setExpiryDate(rs.getDate("expiry_date") != null ? rs.getDate("expiry_date").toLocalDate() : null);
        mb.setImportPrice(rs.getDouble("import_price"));
        mb.setSellPrice(rs.getDouble("sell_price"));
        mb.setInitialQty(rs.getInt("initial_qty"));
        mb.setCurrentQty(rs.getInt("current_qty"));
        mb.setMinThreshold(rs.getInt("min_threshold"));
        // transients from JOIN
        try { mb.setMedicineName(rs.getString("medicine_name")); } catch (SQLException ignored) {}
        try { mb.setMedicineCode(rs.getString("medicine_code")); } catch (SQLException ignored) {}
        try { mb.setUnit(rs.getString("unit")); } catch (SQLException ignored) {}
        return mb;
    }
}
