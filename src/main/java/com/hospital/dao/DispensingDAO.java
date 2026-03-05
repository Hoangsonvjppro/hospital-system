package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Dispensing;
import com.hospital.model.DispensingItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO phát thuốc — truy vấn JDBC trên bảng Dispensing, DispensingItem.
 *
 * Hỗ trợ 2 mode:
 * - Mode 1 (default): tự lấy Connection → dùng cho thao tác đơn lẻ.
 * - Mode 2 (external connection): nhận Connection từ bên ngoài → dùng cho transaction.
 */
public class DispensingDAO {

    private static final Logger LOGGER = Logger.getLogger(DispensingDAO.class.getName());

    private Connection externalConnection;

    public DispensingDAO() {}

    public DispensingDAO(Connection connection) {
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

    // ═══════════════════════════════════════════════════════════
    //  TẠO PHIẾU PHÁT THUỐC (TRANSACTION)
    // ═══════════════════════════════════════════════════════════

    /**
     * Tạo phiếu phát thuốc trong transaction:
     * 1. INSERT Dispensing record
     * 2. INSERT từng DispensingItem
     * 3. UPDATE Medicine.stock_qty (trừ tồn kho)
     * 4. INSERT StockTransaction (audit trail)
     *
     * @return dispensing_id vừa tạo
     */
    public long createDispensing(Dispensing d, List<DispensingItem> items) {
        String insertDispensing = """
            INSERT INTO Dispensing (prescription_id, patient_id, dispensed_by, status, total_amount, notes, dispensed_at)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            """;
        String insertItem = """
            INSERT INTO DispensingItem (dispensing_id, prescription_detail_id, medicine_id,
                                        medicine_name, requested_quantity, dispensed_quantity,
                                        unit_price, batch_number)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        String updateStock = """
            UPDATE Medicine SET stock_qty = stock_qty - ?, updated_at = NOW()
            WHERE medicine_id = ? AND stock_qty >= ?
            """;
        String insertStockTx = """
            INSERT INTO StockTransaction (medicine_id, transaction_type, quantity,
                                          stock_before, stock_after,
                                          reference_type, reference_id, notes, created_by)
            VALUES (?, 'EXPORT', ?, ?, ?, 'DISPENSING', ?, ?, ?)
            """;
        String getStock = "SELECT stock_qty FROM Medicine WHERE medicine_id = ?";

        Connection conn = null;
        try {
            conn = getConnection();
            // 1. Insert Dispensing
            long dispensingId;
            try (PreparedStatement ps = conn.prepareStatement(insertDispensing, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, d.getPrescriptionId());
                ps.setLong(2, d.getPatientId());
                if (d.getDispensedBy() != null) {
                    ps.setLong(3, d.getDispensedBy());
                } else {
                    ps.setNull(3, Types.BIGINT);
                }
                ps.setString(4, d.getStatus());
                ps.setBigDecimal(5, d.getTotalAmount());
                ps.setString(6, d.getNotes());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        dispensingId = rs.getLong(1);
                    } else {
                        throw new DataAccessException("Không lấy được ID phiếu phát thuốc", null);
                    }
                }
            }

            // 2. Insert items + 3. Update stock + 4. Audit trail
            for (DispensingItem item : items) {
                item.setDispensingId(dispensingId);

                // Insert item
                try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
                    ps.setLong(1, dispensingId);
                    ps.setLong(2, item.getPrescriptionDetailId());
                    ps.setLong(3, item.getMedicineId());
                    ps.setString(4, item.getMedicineName());
                    ps.setInt(5, item.getRequestedQuantity());
                    ps.setInt(6, item.getDispensedQuantity());
                    ps.setBigDecimal(7, item.getUnitPrice());
                    ps.setString(8, item.getBatchNumber());
                    ps.executeUpdate();
                }

                if (item.getDispensedQuantity() > 0) {
                    // Get current stock before update
                    int stockBefore;
                    try (PreparedStatement ps = conn.prepareStatement(getStock)) {
                        ps.setLong(1, item.getMedicineId());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new DataAccessException("Không tìm thấy thuốc ID=" + item.getMedicineId(), null);
                            stockBefore = rs.getInt("stock_qty");
                        }
                    }

                    // Update stock
                    try (PreparedStatement ps = conn.prepareStatement(updateStock)) {
                        ps.setInt(1, item.getDispensedQuantity());
                        ps.setLong(2, item.getMedicineId());
                        ps.setInt(3, item.getDispensedQuantity());
                        int affected = ps.executeUpdate();
                        if (affected == 0) {
                            throw new DataAccessException(
                                    "Không đủ tồn kho cho thuốc: " + item.getMedicineName(), null);
                        }
                    }

                    // Audit trail
                    int stockAfter = stockBefore - item.getDispensedQuantity();
                    try (PreparedStatement ps = conn.prepareStatement(insertStockTx)) {
                        ps.setLong(1, item.getMedicineId());
                        ps.setInt(2, -item.getDispensedQuantity()); // EXPORT → negative
                        ps.setInt(3, stockBefore);
                        ps.setInt(4, stockAfter);
                        ps.setLong(5, dispensingId);
                        ps.setString(6, "Phát thuốc đơn #" + d.getPrescriptionId());
                        if (d.getDispensedBy() != null) {
                            ps.setLong(7, d.getDispensedBy());
                        } else {
                            ps.setNull(7, Types.BIGINT);
                        }
                        ps.executeUpdate();
                    }
                }
            }

            return dispensingId;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tạo phiếu phát thuốc", e);
            throw new DataAccessException("Lỗi tạo phiếu phát thuốc", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  TRUY VẤN
    // ═══════════════════════════════════════════════════════════

    /**
     * Kiểm tra đơn thuốc đã phát chưa.
     */
    public Dispensing getByPrescriptionId(long prescriptionId) {
        String sql = """
            SELECT d.*, p.full_name AS patient_name
            FROM Dispensing d
            JOIN Patient pt ON d.patient_id = pt.patient_id
            LEFT JOIN `User` p ON d.dispensed_by = p.user_id
            WHERE d.prescription_id = ?
            ORDER BY d.created_at DESC LIMIT 1
            """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, prescriptionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapDispensing(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn dispensing theo prescriptionId=" + prescriptionId, e);
            throw new DataAccessException("Lỗi truy vấn phiếu phát thuốc", e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    /**
     * Danh sách đơn chờ phát (Prescription CONFIRMED nhưng chưa có Dispensing DISPENSED).
     * Trả về danh sách Dispensing pending hoặc Prescription chưa phát.
     */
    public List<Dispensing> getPendingDispensings() {
        // Lấy Prescription status = CONFIRMED mà chưa có dispensing DISPENSED
        String sql = """
            SELECT p.prescription_id, p.record_id, p.total_amount, p.created_at, p.status,
                   mr.patient_id, pt.full_name AS patient_name
            FROM Prescription p
            JOIN MedicalRecord mr ON p.record_id = mr.record_id
            JOIN Patient pt ON mr.patient_id = pt.patient_id
            WHERE p.status = 'CONFIRMED'
              AND NOT EXISTS (
                  SELECT 1 FROM Dispensing d
                  WHERE d.prescription_id = p.prescription_id
                    AND d.status IN ('DISPENSED', 'PARTIAL')
              )
            ORDER BY p.created_at ASC
            """;
        List<Dispensing> result = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Dispensing d = new Dispensing();
                        d.setPrescriptionId(rs.getLong("prescription_id"));
                        d.setPatientId(rs.getLong("patient_id"));
                        d.setPatientName(rs.getString("patient_name"));
                        d.setStatus(Dispensing.STATUS_PENDING);
                        BigDecimal amt = rs.getBigDecimal("total_amount");
                        d.setTotalAmount(amt != null ? amt : BigDecimal.ZERO);
                        Timestamp ts = rs.getTimestamp("created_at");
                        if (ts != null) d.setCreatedAt(ts.toLocalDateTime());
                        result.add(d);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách đơn chờ phát", e);
            throw new DataAccessException("Lỗi lấy danh sách đơn chờ phát", e);
        } finally {
            closeIfOwned(conn);
        }
        return result;
    }

    /**
     * Lấy chi tiết items của dispensing record.
     */
    public List<DispensingItem> getDispensingItems(long dispensingId) {
        String sql = """
            SELECT di.*, m.stock_qty, m.unit
            FROM DispensingItem di
            JOIN Medicine m ON di.medicine_id = m.medicine_id
            WHERE di.dispensing_id = ?
            ORDER BY di.item_id
            """;
        List<DispensingItem> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, dispensingId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapDispensingItem(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy chi tiết phát thuốc dispensingId=" + dispensingId, e);
            throw new DataAccessException("Lỗi lấy chi tiết phát thuốc", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    /**
     * Lấy danh sách thuốc cần phát cho một đơn (từ PrescriptionDetail + Medicine stock info).
     */
    public List<DispensingItem> getItemsForPrescription(long prescriptionId) {
        String sql = """
            SELECT pd.detail_id, pd.medicine_id, pd.quantity, pd.unit_price,
                   m.medicine_name, m.stock_qty, m.unit, m.expiry_date, m.min_threshold
            FROM PrescriptionDetail pd
            JOIN Medicine m ON pd.medicine_id = m.medicine_id
            WHERE pd.prescription_id = ?
            ORDER BY pd.detail_id
            """;
        List<DispensingItem> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, prescriptionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DispensingItem item = new DispensingItem();
                        item.setPrescriptionDetailId(rs.getLong("detail_id"));
                        item.setMedicineId(rs.getLong("medicine_id"));
                        item.setMedicineName(rs.getString("medicine_name"));
                        item.setRequestedQuantity(rs.getInt("quantity"));
                        item.setDispensedQuantity(rs.getInt("quantity")); // default: phát đủ
                        BigDecimal price = rs.getBigDecimal("unit_price");
                        item.setUnitPrice(price != null ? price : BigDecimal.ZERO);
                        item.setStockQty(rs.getInt("stock_qty"));
                        item.setUnit(rs.getString("unit"));

                        // Tự điều chỉnh nếu tồn kho không đủ
                        if (item.getStockQty() < item.getRequestedQuantity()) {
                            item.setDispensedQuantity(item.getStockQty());
                        }

                        list.add(item);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy items cho prescriptionId=" + prescriptionId, e);
            throw new DataAccessException("Lỗi lấy danh sách thuốc cần phát", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    /**
     * Lấy tổng tiền thuốc đã phát cho một record (dùng cho tạo Invoice).
     */
    public BigDecimal getDispensingTotalByRecordId(long recordId) {
        String sql = """
            SELECT COALESCE(SUM(di.dispensed_quantity * di.unit_price), 0) AS total
            FROM Dispensing d
            JOIN DispensingItem di ON d.dispensing_id = di.dispensing_id
            JOIN Prescription p ON d.prescription_id = p.prescription_id
            WHERE p.record_id = ? AND d.status IN ('DISPENSED', 'PARTIAL')
            """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, recordId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        BigDecimal total = rs.getBigDecimal("total");
                        return total != null ? total : BigDecimal.ZERO;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy tổng tiền thuốc cho recordId=" + recordId, e);
        } finally {
            closeIfOwned(conn);
        }
        return BigDecimal.ZERO;
    }

    // ═══════════════════════════════════════════════════════════
    //  MAPPING
    // ═══════════════════════════════════════════════════════════

    private Dispensing mapDispensing(ResultSet rs) throws SQLException {
        Dispensing d = new Dispensing();
        d.setId(rs.getInt("dispensing_id"));
        d.setPrescriptionId(rs.getLong("prescription_id"));
        d.setPatientId(rs.getLong("patient_id"));
        long dispensedBy = rs.getLong("dispensed_by");
        d.setDispensedBy(rs.wasNull() ? null : dispensedBy);
        d.setStatus(rs.getString("status"));
        d.setTotalAmount(rs.getBigDecimal("total_amount"));
        d.setNotes(rs.getString("notes"));
        Timestamp dispensedAt = rs.getTimestamp("dispensed_at");
        if (dispensedAt != null) d.setDispensedAt(dispensedAt.toLocalDateTime());
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) d.setCreatedAt(createdAt.toLocalDateTime());
        try { d.setPatientName(rs.getString("patient_name")); } catch (SQLException ignored) {}
        return d;
    }

    private DispensingItem mapDispensingItem(ResultSet rs) throws SQLException {
        DispensingItem item = new DispensingItem();
        item.setId(rs.getInt("item_id"));
        item.setDispensingId(rs.getLong("dispensing_id"));
        item.setPrescriptionDetailId(rs.getLong("prescription_detail_id"));
        item.setMedicineId(rs.getLong("medicine_id"));
        item.setMedicineName(rs.getString("medicine_name"));
        item.setRequestedQuantity(rs.getInt("requested_quantity"));
        item.setDispensedQuantity(rs.getInt("dispensed_quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setSubtotal(rs.getBigDecimal("subtotal"));
        try { item.setBatchNumber(rs.getString("batch_number")); } catch (SQLException ignored) {}
        try { item.setStockQty(rs.getInt("stock_qty")); } catch (SQLException ignored) {}
        try { item.setUnit(rs.getString("unit")); } catch (SQLException ignored) {}
        return item;
    }
}
