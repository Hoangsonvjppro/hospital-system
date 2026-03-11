package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {

    private Connection externalConnection;

    public DashboardDAO() {}

    public DashboardDAO(Connection connection) {
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

   
    public int countActivePatients() {
        String sql = "SELECT COUNT(*) FROM Patient WHERE is_active = TRUE";
        return queryCount(sql);
    }

  
    public int countActiveMedicines() {
        String sql = "SELECT COUNT(*) FROM Medicine WHERE is_active = TRUE";
        return queryCount(sql);
    }

    public int countTodayVisits() {
        String sql = "SELECT COUNT(*) FROM MedicalRecord WHERE DATE(visit_date) = CURDATE()";
        return queryCount(sql);
    }

    public int countLowStockMedicines() {
        String sql = "SELECT COUNT(*) FROM Medicine WHERE stock_qty <= min_threshold AND is_active = TRUE";
        return queryCount(sql);
    }

    public int countActiveDoctors() {
        String sql = "SELECT COUNT(*) FROM Doctor WHERE is_active = TRUE";
        return queryCount(sql);
    }

    public int countTotalVisits() {
        String sql = "SELECT COUNT(*) FROM MedicalRecord";
        return queryCount(sql);
    }

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

    public int countTodayDispensedPrescriptions() {
        String sql = "SELECT COUNT(*) FROM Prescription WHERE status = 'DISPENSED' AND DATE(created_at) = CURDATE()";
        return queryCount(sql);
    }

    public List<String[]> findLowStockMedicines() {
        String sql = """
            SELECT medicine_name, stock_qty, min_threshold
            FROM Medicine
            WHERE stock_qty <= min_threshold AND is_active = TRUE
            ORDER BY (stock_qty * 1.0 / GREATEST(min_threshold, 1)) ASC
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
                        String.valueOf(rs.getInt("stock_qty")),
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

    public List<String[]> findLongWaitingPatients(int thresholdMinutes) {
        String sql = """
            SELECT p.full_name,
                   TIMESTAMPDIFF(MINUTE, mr.arrival_time,
                       CONVERT(CURTIME(), TIME)) AS wait_mins
            FROM MedicalRecord mr
            JOIN Patient p ON mr.patient_id = p.patient_id
            WHERE mr.queue_status = 'WAITING'
              AND DATE(mr.visit_date) = CURDATE()
              AND mr.arrival_time IS NOT NULL
              AND TIMESTAMPDIFF(MINUTE, mr.arrival_time,
                      CONVERT(CURTIME(), TIME)) >= ?
            ORDER BY mr.arrival_time ASC
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
