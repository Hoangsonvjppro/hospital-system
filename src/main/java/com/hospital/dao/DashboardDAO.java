package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO chuyên chạy các câu SQL thống kê cho Dashboard (Trang Tổng quan).
 * Dashboard statistics DAO — provides aggregate counts for the overview panel.
 */
public class DashboardDAO {

    private Connection externalConnection;

    public DashboardDAO() {
        // Mode 1: Tự lấy connection
    }

    public DashboardDAO(Connection connection) {
        // Mode 2: Dùng external connection
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

    /**
     * Đếm tổng số bệnh nhân đang hoạt động.
     */
    public int countActivePatients() {
        String sql = "SELECT COUNT(*) FROM Patient WHERE is_active = TRUE";
        return queryCount(sql);
    }

    /**
     * Đếm tổng danh mục thuốc đang hoạt động.
     */
    public int countActiveMedicines() {
        String sql = "SELECT COUNT(*) FROM Medicine WHERE is_active = TRUE";
        return queryCount(sql);
    }

    /**
     * Đếm số lượt khám trong ngày hôm nay.
     */
    public int countTodayVisits() {
        String sql = "SELECT COUNT(*) FROM MedicalRecord WHERE DATE(visit_date) = CURDATE()";
        return queryCount(sql);
    }

    /**
     * Đếm số lô thuốc sắp hết hàng (tồn kho <= ngưỡng cảnh báo).
     */
    public int countLowStockMedicines() {
        String sql = "SELECT COUNT(*) FROM MedicineBatch WHERE current_qty > 0 AND current_qty <= min_threshold";
        return queryCount(sql);
    }

    /**
     * Đếm tổng số bác sĩ đang hoạt động.
     */
    public int countActiveDoctors() {
        String sql = "SELECT COUNT(*) FROM Doctor WHERE is_active = TRUE";
        return queryCount(sql);
    }

    /**
     * Đếm tổng số lượt khám (tất cả).
     */
    public int countTotalVisits() {
        String sql = "SELECT COUNT(*) FROM MedicalRecord";
        return queryCount(sql);
    }

    // ── Phase 2 — Real-time dashboard statistics ──────────────

    /**
     * Đếm số bệnh nhân hôm nay theo trạng thái hàng đợi.
     *
     * @param status queue_status cần đếm (WAITING, EXAMINING, PRESCRIBED, DISPENSED, COMPLETED…)
     * @return số lượng bệnh nhân có trạng thái đó trong ngày hôm nay
     */
    public int countTodayByQueueStatus(String status) {
        String sql = "SELECT COUNT(*) FROM MedicalRecord WHERE DATE(visit_date) = CURDATE() AND queue_status = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi đếm BN theo trạng thái: " + status, e);
        } finally {
            closeIfOwned(conn);
        }
        return 0;
    }

    /**
     * Tính tổng doanh thu hôm nay (chỉ hóa đơn đã thanh toán).
     */
    public double getTodayRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM Invoice WHERE status = 'PAID' AND DATE(payment_date) = CURDATE()";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi tính doanh thu hôm nay", e);
        } finally {
            closeIfOwned(conn);
        }
        return 0;
    }

    /**
     * Đếm số đơn thuốc đã phát (DISPENSED) trong ngày hôm nay.
     */
    public int countTodayDispensedPrescriptions() {
        String sql = "SELECT COUNT(*) FROM Prescription WHERE status = 'DISPENSED' AND DATE(created_at) = CURDATE()";
        return queryCount(sql);
    }

    /**
     * Lấy danh sách lô thuốc sắp hết hàng (tồn ≤ ngưỡng).
     *
     * @return List of String[] { medicineName, currentQty, minThreshold }
     */
    public List<String[]> findLowStockMedicines() {
        String sql = """
            SELECT m.medicine_name, mb.current_qty, mb.min_threshold
            FROM MedicineBatch mb
            JOIN Medicine m ON mb.medicine_id = m.medicine_id
            WHERE mb.current_qty > 0 AND mb.current_qty <= mb.min_threshold
            ORDER BY (mb.current_qty * 1.0 / GREATEST(mb.min_threshold, 1)) ASC
            LIMIT 10
        """;
        List<String[]> result = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new String[]{
                        rs.getString("medicine_name"),
                        String.valueOf(rs.getInt("current_qty")),
                        String.valueOf(rs.getInt("min_threshold"))
                    });
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn thuốc sắp hết", e);
        } finally {
            closeIfOwned(conn);
        }
        return result;
    }

    /**
     * Lấy danh sách bệnh nhân chờ lâu hơn ngưỡng cho phép (phút).
     *
     * @param thresholdMinutes số phút tối đa chờ trước khi cảnh báo
     * @return List of String[] { patientName, waitingMinutes }
     */
    public List<String[]> findLongWaitingPatients(int thresholdMinutes) {
        String sql = """
            SELECT p.full_name,
                   TIMESTAMPDIFF(MINUTE, mr.created_at, NOW()) AS wait_mins
            FROM MedicalRecord mr
            JOIN Patient p ON mr.patient_id = p.patient_id
            WHERE mr.queue_status = 'WAITING'
              AND DATE(mr.visit_date) = CURDATE()
              AND TIMESTAMPDIFF(MINUTE, mr.created_at, NOW()) >= ?
            ORDER BY mr.created_at ASC
            LIMIT 10
        """;
        List<String[]> result = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, thresholdMinutes);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(new String[]{
                            rs.getString("full_name"),
                            String.valueOf(rs.getInt("wait_mins"))
                        });
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn BN chờ lâu", e);
        } finally {
            closeIfOwned(conn);
        }
        return result;
    }

    // ── Helper ────────────────────────────────────────────────

    private int queryCount(String sql) {
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi thống kê Dashboard", e);
        } finally {
            closeIfOwned(conn);
        }
        return 0;
    }
}
