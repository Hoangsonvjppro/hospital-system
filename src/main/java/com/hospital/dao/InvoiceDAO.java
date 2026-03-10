package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Invoice;
import com.hospital.model.InvoiceMedicineDetail;
import com.hospital.model.InvoiceServiceDetail;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO hóa đơn thanh toán — truy vấn JDBC trên bảng Invoice, InvoiceServiceDetail,
 * InvoiceMedicineDetail.
 *
 * Hỗ trợ 2 mode:
 * - Mode 1 (default): tự lấy Connection → dùng cho thao tác đơn lẻ.
 * - Mode 2 (external connection): nhận Connection từ bên ngoài → dùng cho transaction.
 *
 * Quy ước:
 * - findAll/findByStatus trả về Invoice kèm thông tin hiển thị (patientName, doctorName)
 *   thông qua JOIN Invoice → Patient, MedicalRecord → Doctor → User.
 * - getServiceDetails / getMedicineDetails load chi tiết riêng khi cần.
 * - delete() = soft-delete bằng cách SET status = 'CANCELLED'.
 */
public class InvoiceDAO implements BaseDAO<Invoice> {

    private static final Logger LOGGER = Logger.getLogger(InvoiceDAO.class.getName());

    private Connection externalConnection;

    public InvoiceDAO() {
        // Mode 1: Tự lấy connection
    }

    public InvoiceDAO(Connection connection) {
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

    // ═══════════════════════════════════════════════════════════
    //  SQL chung — JOIN Invoice → Patient, MedicalRecord → Doctor → User
    // ═══════════════════════════════════════════════════════════

    /**
     * Query cơ sở cho các method findAll/findById/findByStatus.
     * JOIN để lấy patient_name, patient_phone, doctor_name.
     */
    private static final String BASE_SELECT = """
        SELECT i.invoice_id, i.patient_id, i.record_id, i.invoice_date,
               i.exam_fee, i.service_fee, i.medicine_fee, i.other_fee, i.discount,
               i.total_amount, i.paid_amount, i.change_amount,
               i.status, i.payment_method, i.payment_date,
               i.notes, i.cashier_id, i.created_at, i.updated_at,
               p.full_name  AS patient_name,
               p.phone      AS patient_phone,
               u.full_name  AS doctor_name
        FROM Invoice i
        JOIN Patient p ON i.patient_id = p.patient_id
        LEFT JOIN MedicalRecord mr ON i.record_id = mr.record_id
        LEFT JOIN Doctor d         ON mr.doctor_id = d.doctor_id
        LEFT JOIN `User` u         ON d.user_id   = u.user_id
        """;

    // ═══════════════════════════════════════════════════════════
    //  BaseDAO<Invoice> — CRUD cơ bản
    // ═══════════════════════════════════════════════════════════

    @Override
    public Invoice findById(int id) {
        String sql = BASE_SELECT + " WHERE i.invoice_id = ?";
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
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn hóa đơn ID=" + id, e);
            throw new DataAccessException("Lỗi truy vấn hóa đơn ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<Invoice> findAll() {
        String sql = BASE_SELECT + " ORDER BY i.invoice_date DESC";
        return queryList(sql);
    }

    @Override
    public boolean insert(Invoice entity) {
        String sql = """
            INSERT INTO Invoice
                (patient_id, record_id, invoice_date,
                 exam_fee, service_fee, medicine_fee, other_fee, discount, total_amount,
                 paid_amount, change_amount,
                 status, payment_method, payment_date, notes, cashier_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setInsertParams(ps, entity);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            entity.setId(keys.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể tạo hóa đơn", e);
            throw new DataAccessException("Không thể tạo hóa đơn", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    @Override
    public boolean update(Invoice entity) {
        String sql = """
            UPDATE Invoice
            SET patient_id     = ?,
                record_id      = ?,
                invoice_date   = ?,
                exam_fee       = ?,
                service_fee    = ?,
                medicine_fee   = ?,
                other_fee      = ?,
                discount       = ?,
                total_amount   = ?,
                paid_amount    = ?,
                change_amount  = ?,
                status         = ?,
                payment_method = ?,
                payment_date   = ?,
                notes          = ?
            WHERE invoice_id   = ?
            """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, entity.getPatientId());
                setNullableLong(ps, 2, entity.getRecordId());
                setNullableTimestamp(ps, 3, entity.getInvoiceDate());
                ps.setDouble(4, entity.getExamFee());
                ps.setDouble(5, entity.getServiceFee());
                ps.setDouble(6, entity.getMedicineFee());
                ps.setDouble(7, entity.getOtherFee());
                ps.setDouble(8, entity.getDiscount());
                ps.setDouble(9, entity.getTotalAmount());
                ps.setDouble(10, entity.getPaidAmount());
                ps.setDouble(11, entity.getChangeAmount());
                ps.setString(12, entity.getStatus());
                ps.setString(13, entity.getPaymentMethod());
                setNullableTimestamp(ps, 14, entity.getPaymentDate());
                ps.setString(15, entity.getNotes());
                ps.setInt(16, entity.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật hóa đơn ID=" + entity.getId(), e);
            throw new DataAccessException("Không thể cập nhật hóa đơn ID=" + entity.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    /**
     * Soft-delete: đổi status → CANCELLED (không xóa vật lý dữ liệu tài chính).
     */
    @Override
    public boolean delete(int id) {
        return updateStatus(id, "CANCELLED");
    }

    // ═══════════════════════════════════════════════════════════
    //  TRUY VẤN CHUYÊN BIỆT
    // ═══════════════════════════════════════════════════════════

    /**
     * Lấy danh sách hóa đơn theo trạng thái (PENDING / PAID / CANCELLED).
     */
    public List<Invoice> findByStatus(String status) {
        String sql = BASE_SELECT + " WHERE i.status = ? ORDER BY i.invoice_date DESC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                return executeQuery(ps);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn hóa đơn status=" + status, e);
            throw new DataAccessException("Lỗi truy vấn hóa đơn theo trạng thái", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    /**
     * Lấy danh sách hóa đơn chờ thanh toán (status = PENDING).
     * Alias rõ ràng cho findByStatus("PENDING").
     */
    public List<Invoice> getUnpaidInvoices() {
        return findByStatus("PENDING");
    }

    /**
     * Cập nhật trạng thái hóa đơn (PENDING → PAID / CANCELLED).
     */
    public boolean updateStatus(long invoiceId, String newStatus) {
        String sql = "UPDATE Invoice SET status = ?, updated_at = NOW() WHERE invoice_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, newStatus);
                ps.setLong(2, invoiceId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật trạng thái hóa đơn ID=" + invoiceId, e);
            throw new DataAccessException("Không thể cập nhật trạng thái hóa đơn", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    /**
     * Xử lý thanh toán: cập nhật trạng thái + thông tin thanh toán.
     */
    public boolean updatePayment(long invoiceId, String paymentMethod,
                                 double paidAmount, double changeAmount) {
        String sql = """
            UPDATE Invoice
            SET status         = 'PAID',
                payment_method = ?,
                paid_amount    = ?,
                change_amount  = ?,
                payment_date   = NOW(),
                updated_at     = NOW()
            WHERE invoice_id   = ?
            """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, paymentMethod);
                ps.setDouble(2, paidAmount);
                ps.setDouble(3, changeAmount);
                ps.setLong(4, invoiceId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể thanh toán hóa đơn ID=" + invoiceId, e);
            throw new DataAccessException("Không thể thanh toán hóa đơn", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    /**
     * Tính tổng doanh thu từ hóa đơn đã thanh toán.
     */
    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM Invoice WHERE status = 'PAID'";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tính tổng doanh thu", e);
            throw new DataAccessException("Lỗi tính tổng doanh thu", e);
        } finally {
            closeIfOwned(conn);
        }
        return 0;
    }

    /**
     * Lấy Invoice kèm toàn bộ chi tiết (service + medicine).
     * Dùng khi hiển thị chi tiết hóa đơn hoặc in.
     */
    public Invoice getInvoiceWithDetails(long invoiceId) {
        Invoice inv = findById((int) invoiceId);
        if (inv != null) {
            inv.setServiceDetails(getServiceDetails(invoiceId));
            inv.setMedicineDetails(getMedicineDetails(invoiceId));
        }
        return inv;
    }

    // ═══════════════════════════════════════════════════════════
    //  CHI TIẾT HÓA ĐƠN — DỊCH VỤ
    // ═══════════════════════════════════════════════════════════

    /**
     * Lấy danh sách chi tiết dịch vụ của hóa đơn.
     */
    public List<InvoiceServiceDetail> getServiceDetails(long invoiceId) {
        String sql = """
            SELECT detail_id, invoice_id, service_order_id,
                   service_name, quantity, unit_price, line_total
            FROM InvoiceServiceDetail
            WHERE invoice_id = ?
            ORDER BY detail_id
            """;
        List<InvoiceServiceDetail> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, invoiceId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapServiceDetail(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn chi tiết dịch vụ, invoiceId=" + invoiceId, e);
            throw new DataAccessException("Lỗi truy vấn chi tiết dịch vụ hóa đơn", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    /**
     * Thêm chi tiết dịch vụ vào hóa đơn.
     * Lưu ý: KHÔNG insert line_total (cột GENERATED).
     */
    public boolean insertServiceDetail(InvoiceServiceDetail detail) {
        String sql = """
            INSERT INTO InvoiceServiceDetail
                (invoice_id, service_order_id, service_name, quantity, unit_price)
            VALUES (?, ?, ?, ?, ?)
            """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, detail.getInvoiceId());
                ps.setLong(2, detail.getServiceOrderId());
                ps.setString(3, detail.getServiceName());
                ps.setInt(4, detail.getQuantity());
                ps.setDouble(5, detail.getUnitPrice());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            detail.setId(keys.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể thêm chi tiết dịch vụ", e);
            throw new DataAccessException("Không thể thêm chi tiết dịch vụ hóa đơn", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════
    //  CHI TIẾT HÓA ĐƠN — THUỐC
    // ═══════════════════════════════════════════════════════════

    /**
     * Lấy danh sách chi tiết thuốc của hóa đơn.
     * JOIN Medicine để lấy đơn vị tính (unit).
     */
    public List<InvoiceMedicineDetail> getMedicineDetails(long invoiceId) {
        String sql = """
            SELECT imd.detail_id, imd.invoice_id, imd.medicine_id,
                   imd.prescription_detail_id, imd.batch_id, imd.medicine_name,
                   imd.quantity, imd.unit_price, imd.cost_price,
                   imd.line_total, imd.profit_total,
                   m.unit AS medicine_unit
            FROM InvoiceMedicineDetail imd
            LEFT JOIN Medicine m ON imd.medicine_id = m.medicine_id
            WHERE imd.invoice_id = ?
            ORDER BY imd.detail_id
            """;
        List<InvoiceMedicineDetail> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, invoiceId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapMedicineDetail(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn chi tiết thuốc, invoiceId=" + invoiceId, e);
            throw new DataAccessException("Lỗi truy vấn chi tiết thuốc hóa đơn", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    /**
     * Thêm chi tiết thuốc vào hóa đơn.
     * Lưu ý: KHÔNG insert line_total, profit_total (cột GENERATED).
     */
    public boolean insertMedicineDetail(InvoiceMedicineDetail detail) {
        String sql = """
            INSERT INTO InvoiceMedicineDetail
                (invoice_id, medicine_id, prescription_detail_id, batch_id,
                 medicine_name, quantity, unit_price, cost_price)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, detail.getInvoiceId());
                ps.setLong(2, detail.getMedicineId());
                setNullableLong(ps, 3, detail.getPrescriptionDetailId());
                setNullableLong(ps, 4, detail.getBatchId());
                ps.setString(5, detail.getMedicineName());
                ps.setInt(6, detail.getQuantity());
                ps.setDouble(7, detail.getUnitPrice());
                ps.setDouble(8, detail.getCostPrice());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            detail.setId(keys.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể thêm chi tiết thuốc", e);
            throw new DataAccessException("Không thể thêm chi tiết thuốc hóa đơn", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPER — Mapping ResultSet → Model
    // ═══════════════════════════════════════════════════════════

    /**
     * Map ResultSet (từ BASE_SELECT) → Invoice entity.
     */
    private Invoice mapResultSet(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getInt("invoice_id"));
        inv.setPatientId(rs.getLong("patient_id"));

        long recordId = rs.getLong("record_id");
        inv.setRecordId(rs.wasNull() ? null : recordId);

        Timestamp invoiceDate = rs.getTimestamp("invoice_date");
        if (invoiceDate != null) {
            inv.setInvoiceDate(invoiceDate.toLocalDateTime());
        }

        inv.setExamFee(rs.getDouble("exam_fee"));
        inv.setServiceFee(rs.getDouble("service_fee"));
        inv.setMedicineFee(rs.getDouble("medicine_fee"));
        inv.setOtherFee(rs.getDouble("other_fee"));
        inv.setDiscount(rs.getDouble("discount"));
        inv.setTotalAmount(rs.getDouble("total_amount"));
        inv.setPaidAmount(rs.getDouble("paid_amount"));
        inv.setChangeAmount(rs.getDouble("change_amount"));

        inv.setStatus(rs.getString("status"));
        inv.setPaymentMethod(rs.getString("payment_method"));

        Timestamp paymentDate = rs.getTimestamp("payment_date");
        if (paymentDate != null) {
            inv.setPaymentDate(paymentDate.toLocalDateTime());
        }

        inv.setNotes(rs.getString("notes"));

        long createdBy = rs.getLong("cashier_id");
        inv.setCashierId(rs.wasNull() ? null : createdBy);

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) inv.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) inv.setUpdatedAt(updatedAt.toLocalDateTime());

        // Trường hiển thị (từ JOIN)
        inv.setPatientName(rs.getString("patient_name"));
        inv.setPatientPhone(rs.getString("patient_phone"));
        inv.setDoctorName(rs.getString("doctor_name"));

        return inv;
    }

    /**
     * Map ResultSet → InvoiceServiceDetail.
     */
    private InvoiceServiceDetail mapServiceDetail(ResultSet rs) throws SQLException {
        InvoiceServiceDetail d = new InvoiceServiceDetail();
        d.setId(rs.getInt("detail_id"));
        d.setInvoiceId(rs.getLong("invoice_id"));
        d.setServiceOrderId(rs.getLong("service_order_id"));
        d.setServiceName(rs.getString("service_name"));
        d.setQuantity(rs.getInt("quantity"));
        d.setUnitPrice(rs.getDouble("unit_price"));
        d.setLineTotal(rs.getDouble("line_total"));
        return d;
    }

    /**
     * Map ResultSet → InvoiceMedicineDetail.
     * Bao gồm unit từ JOIN Medicine (nếu có).
     */
    private InvoiceMedicineDetail mapMedicineDetail(ResultSet rs) throws SQLException {
        InvoiceMedicineDetail d = new InvoiceMedicineDetail();
        d.setId(rs.getInt("detail_id"));
        d.setInvoiceId(rs.getLong("invoice_id"));
        d.setMedicineId(rs.getLong("medicine_id"));

        long prescDetailId = rs.getLong("prescription_detail_id");
        d.setPrescriptionDetailId(rs.wasNull() ? null : prescDetailId);

        long batchId = rs.getLong("batch_id");
        d.setBatchId(rs.wasNull() ? null : batchId);

        d.setMedicineName(rs.getString("medicine_name"));
        d.setQuantity(rs.getInt("quantity"));
        d.setUnitPrice(rs.getDouble("unit_price"));
        d.setCostPrice(rs.getDouble("cost_price"));
        d.setLineTotal(rs.getDouble("line_total"));
        d.setProfitTotal(rs.getDouble("profit_total"));

        // Unit từ JOIN Medicine (có thể null nếu thuốc bị xóa)
        try {
            d.setUnit(rs.getString("medicine_unit"));
        } catch (SQLException ignored) {
            // Column không tồn tại khi query đơn giản
        }

        return d;
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPER — Utility
    // ═══════════════════════════════════════════════════════════

    /**
     * Thực hiện query và trả về danh sách Invoice (không cần parameter).
     */
    private List<Invoice> queryList(String sql) {
        List<Invoice> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách hóa đơn", e);
            throw new DataAccessException("Lỗi truy vấn danh sách hóa đơn", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    /**
     * Thực hiện PreparedStatement đã set sẵn parameter → trả danh sách Invoice.
     */
    private List<Invoice> executeQuery(PreparedStatement ps) throws SQLException {
        List<Invoice> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Set tham số INSERT cho Invoice.
     */
    private void setInsertParams(PreparedStatement ps, Invoice e) throws SQLException {
        ps.setLong(1, e.getPatientId());
        setNullableLong(ps, 2, e.getRecordId());

        if (e.getInvoiceDate() != null) {
            ps.setTimestamp(3, Timestamp.valueOf(e.getInvoiceDate()));
        } else {
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        }

        ps.setDouble(4, e.getExamFee());
        ps.setDouble(5, e.getServiceFee());
        ps.setDouble(6, e.getMedicineFee());
        ps.setDouble(7, e.getOtherFee());
        ps.setDouble(8, e.getDiscount());
        ps.setDouble(9, e.getTotalAmount());
        ps.setDouble(10, e.getPaidAmount());
        ps.setDouble(11, e.getChangeAmount());
        ps.setString(12, e.getStatus());
        ps.setString(13, e.getPaymentMethod());
        setNullableTimestamp(ps, 14, e.getPaymentDate());
        ps.setString(15, e.getNotes());
        setNullableLong(ps, 16, e.getCashierId());
    }

    private void setNullableLong(PreparedStatement ps, int idx, Long value) throws SQLException {
        if (value != null) {
            ps.setLong(idx, value);
        } else {
            ps.setNull(idx, Types.BIGINT);
        }
    }

    private void setNullableTimestamp(PreparedStatement ps, int idx, LocalDateTime value) throws SQLException {
        if (value != null) {
            ps.setTimestamp(idx, Timestamp.valueOf(value));
        } else {
            ps.setNull(idx, Types.TIMESTAMP);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  CROSS-DOMAIN QUERIES (cho createInvoiceFromMedicalRecord)
    // ═══════════════════════════════════════════════════════════

    /**
     * Lấy patient_id từ MedicalRecord.
     */
    public long getPatientIdByRecordId(long recordId) throws SQLException {
        String sql = "SELECT patient_id FROM MedicalRecord WHERE record_id = ?";
        Connection conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return -1;
                return rs.getLong("patient_id");
            }
        } finally {
            closeIfOwned(conn);
        }
    }

    /**
     * Lấy danh sách dịch vụ chỉ định cho một bệnh án (ServiceOrder JOIN Service).
     */
    public List<InvoiceServiceDetail> getServiceDetailsForRecord(long recordId) throws SQLException {
        String sql = """
                SELECT so.order_id, s.service_name, s.price
                FROM ServiceOrder so
                JOIN Service s ON so.service_id = s.service_id
                WHERE so.record_id = ? AND so.status != 'CANCELLED'
                """;
        List<InvoiceServiceDetail> list = new ArrayList<>();
        Connection conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvoiceServiceDetail d = new InvoiceServiceDetail();
                    d.setServiceOrderId(rs.getLong("order_id"));
                    d.setServiceName(rs.getString("service_name"));
                    d.setQuantity(1);
                    d.setUnitPrice(rs.getDouble("price"));
                    list.add(d);
                }
            }
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    /**
     * Lấy danh sách thuốc kê đơn cho một bệnh án (Prescription → PrescriptionDetail JOIN Medicine).
     */
    public List<InvoiceMedicineDetail> getMedicineDetailsForRecord(long recordId) throws SQLException {
        String sql = """
                SELECT pd.detail_id AS presc_detail_id,
                       pd.medicine_id, pd.quantity, pd.unit_price,
                       pd.batch_id,
                       m.medicine_name,
                       COALESCE(mb.import_price, 0) AS cost_price
                FROM Prescription p
                JOIN PrescriptionDetail pd ON p.prescription_id = pd.prescription_id
                JOIN Medicine m ON pd.medicine_id = m.medicine_id
                LEFT JOIN MedicineBatch mb ON pd.batch_id = mb.batch_id
                WHERE p.record_id = ? AND p.status != 'CANCELLED'
                """;
        List<InvoiceMedicineDetail> list = new ArrayList<>();
        Connection conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvoiceMedicineDetail d = new InvoiceMedicineDetail();
                    d.setPrescriptionDetailId(rs.getLong("presc_detail_id"));
                    d.setMedicineId(rs.getLong("medicine_id"));
                    d.setMedicineName(rs.getString("medicine_name"));
                    d.setQuantity(rs.getInt("quantity"));
                    d.setUnitPrice(rs.getDouble("unit_price"));
                    d.setCostPrice(rs.getDouble("cost_price"));
                    long batchId = rs.getLong("batch_id");
                    if (!rs.wasNull()) {
                        d.setBatchId(batchId);
                    }
                    list.add(d);
                }
            }
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }
}
