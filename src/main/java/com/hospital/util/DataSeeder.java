package com.hospital.util;

import com.hospital.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Tiện ích seed 3 user demo vào database.
 * Chạy: mvn exec:java -Dexec.mainClass="com.hospital.util.DataSeeder"
 */
public class DataSeeder {

    public static void main(String[] args) {
        System.err.println("=== DataSeeder BẮT ĐẦU ===");

        String plainPassword = "password";
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
        System.err.println("[1] Generated hash: " + hash);

        // Verify hash ngay lập tức
        boolean verified = BCrypt.checkpw(plainPassword, hash);
        System.err.println("[2] Verify hash: " + (verified ? "OK ✅" : "FAIL ❌"));

        if (!verified) {
            System.err.println("BCrypt thư viện có vấn đề! Dừng lại.");
            return;
        }

        try {
            Connection conn = DatabaseConfig.getInstance().getConnection();
            System.err.println("[3] Kết nối DB thành công ✅");

            // Update password hash cho tất cả user
            String updateSql = "UPDATE `User` SET password_hash = ? WHERE username IN ('admin', 'doctor', 'doctor2', 'letan', 'ketoan', 'nurse1')";
            PreparedStatement ps = conn.prepareStatement(updateSql);
            ps.setString(1, hash);
            int rows = ps.executeUpdate();
            System.err.println("[4] Updated " + rows + " users ✅");

            // Kiểm tra lại
            String selectSql = "SELECT user_id, username, password_hash, role_id FROM `User`";
            ResultSet rs = conn.createStatement().executeQuery(selectSql);
            System.err.println("[5] Danh sách user trong DB:");
            while (rs.next()) {
                String username = rs.getString("username");
                String dbHash = rs.getString("password_hash");
                long roleId = rs.getLong("role_id");
                boolean check = BCrypt.checkpw(plainPassword, dbHash);
                System.err.println("    " + username + " | role=" + roleId
                        + " | checkpw='" + plainPassword + "' → " + (check ? "OK ✅" : "FAIL ❌"));
            }

            DatabaseConfig.getInstance().closeConnection();
            System.err.println("=== DataSeeder HOÀN TẤT ===");

        } catch (SQLException e) {
            System.err.println("❌ Lỗi DB: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
