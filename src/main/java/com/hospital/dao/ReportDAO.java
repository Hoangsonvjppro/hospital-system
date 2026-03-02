package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO thống kê báo cáo — cung cấp dữ liệu cho biểu đồ JFreeChart.
 * <p>
 * Các method chính:
 * <ul>
 *   <li>{@link #getRevenueByDay(LocalDate, LocalDate)} — doanh thu theo ngày</li>
 *   <li>{@link #getTopMedicines(int)} — thuốc bán chạy nhất</li>
 *   <li>{@link #getTopDiseases(int)} — bệnh phổ biến nhất</li>
 *   <li>{@link #getRevenueTotal(LocalDate, LocalDate)} — tổng doanh thu trong khoảng</li>
 *   <li>{@link #getInvoiceCountByStatus(LocalDate, LocalDate)} — đếm hóa đơn theo trạng thái</li>
 * </ul>
 */
public class ReportDAO {

    private static final Logger LOGGER = Logger.getLogger(ReportDAO.class.getName());

    // ═══════════════════════════════════════════════════════════
    //  DOANH THU THEO NGÀY
    // ═══════════════════════════════════════════════════════════

    /**
     * Trả về Map&lt;ngày, doanh thu&gt; cho các hóa đơn đã thanh toán (PAID)
     * trong khoảng [from, to]. Ngày nào không có doanh thu vẫn trả 0.
     */
    public Map<LocalDate, Double> getRevenueByDay(LocalDate from, LocalDate to) {
        // Khởi tạo map đầy đủ các ngày (fill 0)
        Map<LocalDate, Double> result = new LinkedHashMap<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            result.put(d, 0.0);
        }

        String sql = """
                SELECT DATE(payment_date) AS pay_day, SUM(total_amount) AS revenue
                FROM Invoice
                WHERE status = 'PAID'
                  AND DATE(payment_date) BETWEEN ? AND ?
                GROUP BY DATE(payment_date)
                ORDER BY pay_day
                """;

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate day = rs.getDate("pay_day").toLocalDate();
                    double revenue = rs.getDouble("revenue");
                    result.put(day, revenue);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thống kê doanh thu theo ngày", e);
            throw new DataAccessException("Lỗi thống kê doanh thu", e);
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════
    //  TỔNG DOANH THU
    // ═══════════════════════════════════════════════════════════

    /**
     * Tổng doanh thu (PAID) trong khoảng [from, to].
     */
    public double getRevenueTotal(LocalDate from, LocalDate to) {
        String sql = """
                SELECT COALESCE(SUM(total_amount), 0) AS total
                FROM Invoice
                WHERE status = 'PAID'
                  AND DATE(payment_date) BETWEEN ? AND ?
                """;
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tính tổng doanh thu", e);
        }
        return 0;
    }

    // ═══════════════════════════════════════════════════════════
    //  ĐẾM HÓA ĐƠN THEO TRẠNG THÁI
    // ═══════════════════════════════════════════════════════════

    /**
     * Trả Map&lt;status, count&gt; (PENDING, PAID, CANCELLED)
     * cho hóa đơn tạo trong khoảng [from, to].
     */
    public Map<String, Integer> getInvoiceCountByStatus(LocalDate from, LocalDate to) {
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("PAID", 0);
        result.put("PENDING", 0);
        result.put("CANCELLED", 0);

        String sql = """
                SELECT status, COUNT(*) AS cnt
                FROM Invoice
                WHERE DATE(created_at) BETWEEN ? AND ?
                GROUP BY status
                """;
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("status"), rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm hóa đơn theo trạng thái", e);
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════
    //  TOP THUỐC BÁN CHẠY
    // ═══════════════════════════════════════════════════════════

    /**
     * Top N thuốc bán nhiều nhất (theo tổng số lượng đã xuất hóa đơn).
     * Trả List&lt;Object[]&gt; mỗi phần tử = {tên thuốc, tổng SL, tổng tiền}.
     */
    public List<Object[]> getTopMedicines(int limit) {
        String sql = """
                SELECT m.medicine_name, SUM(imd.quantity) AS total_qty,
                       SUM(imd.line_total) AS total_amount
                FROM InvoiceMedicineDetail imd
                JOIN Medicine m ON m.medicine_id = imd.medicine_id
                JOIN Invoice i ON i.invoice_id = imd.invoice_id
                WHERE i.status = 'PAID'
                GROUP BY imd.medicine_id, m.medicine_name
                ORDER BY total_qty DESC
                LIMIT ?
                """;
        return queryTopList(sql, limit);
    }

    // ═══════════════════════════════════════════════════════════
    //  TOP BỆNH PHỔ BIẾN
    // ═══════════════════════════════════════════════════════════

    /**
     * Top N chẩn đoán phổ biến nhất.
     * Trả List&lt;Object[]&gt; mỗi phần tử = {tên bệnh, số ca, null}.
     */
    public List<Object[]> getTopDiseases(int limit) {
        String sql = """
                SELECT diagnosis, COUNT(*) AS cnt, 0 AS dummy
                FROM MedicalRecord
                WHERE diagnosis IS NOT NULL AND diagnosis <> ''
                GROUP BY diagnosis
                ORDER BY cnt DESC
                LIMIT ?
                """;
        return queryTopList(sql, limit);
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPER
    // ═══════════════════════════════════════════════════════════

    private List<Object[]> queryTopList(String sql, int limit) {
        List<Object[]> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                            rs.getString(1),
                            rs.getInt(2),
                            rs.getDouble(3)
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn top list", e);
        }
        return list;
    }
}
