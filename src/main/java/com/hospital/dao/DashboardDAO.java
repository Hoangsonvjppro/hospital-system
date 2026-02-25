package com.hospital.dao;

import com.hospital.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO chuyên chạy các câu SQL thống kê cho Dashboard (Trang Tổng quan).
 * Dashboard statistics DAO — provides aggregate counts for the overview panel.
 */
public class DashboardDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getInstance().getConnection();
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
     * Đếm số thuốc sắp hết hàng (tồn kho <= ngưỡng cảnh báo).
     */
    public int countLowStockMedicines() {
        String sql = "SELECT COUNT(*) FROM Medicine WHERE stock_qty <= min_threshold AND is_active = TRUE";
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

    // ── Helper ────────────────────────────────────────────────

    private int queryCount(String sql) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
