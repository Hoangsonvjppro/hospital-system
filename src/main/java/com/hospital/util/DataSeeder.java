package com.hospital.util;

import com.hospital.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tiện ích seed 3 user demo vào database.
 * Chạy: mvn exec:java -Dexec.mainClass="com.hospital.util.DataSeeder"
 */
public class DataSeeder {

    private static final Logger LOGGER = Logger.getLogger(DataSeeder.class.getName());

    public static void main(String[] args) {
        LOGGER.info("=== DataSeeder BẮT ĐẦU ===");

        String plainPassword = "password";
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
        LOGGER.info("[1] Generated hash: " + hash);

        // Verify hash ngay lập tức
        boolean verified = BCrypt.checkpw(plainPassword, hash);
        LOGGER.info("[2] Verify hash: " + (verified ? "OK ✅" : "FAIL ❌"));

        if (!verified) {
            LOGGER.severe("BCrypt thư viện có vấn đề! Dừng lại.");
            return;
        }

        try {
            Connection conn = DatabaseConfig.getInstance().getConnection();
            LOGGER.info("[3] Kết nối DB thành công ✅");

            // Update password hash cho tất cả user
            String updateSql = "UPDATE `User` SET password_hash = ? WHERE username IN ('admin', 'doctor', 'doctor2', 'letan', 'ketoan', 'duocsi', 'nurse1')";
            PreparedStatement ps = conn.prepareStatement(updateSql);
            ps.setString(1, hash);
            int rows = ps.executeUpdate();
            LOGGER.info("[4] Updated " + rows + " users ✅");

            // Kiểm tra lại
            String selectSql = "SELECT user_id, username, password_hash, role_id FROM `User`";
            ResultSet rs = conn.createStatement().executeQuery(selectSql);
            LOGGER.info("[5] Danh sách user trong DB:");
            while (rs.next()) {
                String username = rs.getString("username");
                String dbHash = rs.getString("password_hash");
                long roleId = rs.getLong("role_id");
                boolean check = BCrypt.checkpw(plainPassword, dbHash);
                LOGGER.info("    " + username + " | role=" + roleId
                        + " | checkpw='" + plainPassword + "' → " + (check ? "OK ✅" : "FAIL ❌"));
            }

            LOGGER.info("=== DataSeeder HOÀN TẤT ===");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi DB", e);
        }
    }
}
