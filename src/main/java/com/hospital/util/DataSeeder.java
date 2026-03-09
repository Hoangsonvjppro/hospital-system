package com.hospital.util;

import com.hospital.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tiện ích seed user demo vào database.
 * Chạy: mvn exec:java -Dexec.mainClass="com.hospital.util.DataSeeder"
 */
public class DataSeeder {

    private static final Logger LOGGER = Logger.getLogger(DataSeeder.class.getName());

    public static void main(String[] args) {
        LOGGER.info("=== DataSeeder BẮT ĐẦU ===");

        String plainPassword = "password";

        try {
            Connection conn = DatabaseConfig.getInstance().getConnection();
            LOGGER.info("[1] Kết nối DB thành công ✅");

            // Update password cho tất cả user
            String updateSql = "UPDATE `User` SET password = ? WHERE username IN ('admin', 'doctor', 'doctor2', 'letan', 'ketoan', 'duocsi', 'nurse1')";
            PreparedStatement ps = conn.prepareStatement(updateSql);
            ps.setString(1, plainPassword);
            int rows = ps.executeUpdate();
            LOGGER.info("[2] Updated " + rows + " users ✅");

            // Kiểm tra lại
            String selectSql = "SELECT user_id, username, password, role_id FROM `User`";
            ResultSet rs = conn.createStatement().executeQuery(selectSql);
            LOGGER.info("[3] Danh sách user trong DB:");
            while (rs.next()) {
                String username = rs.getString("username");
                String dbPassword = rs.getString("password");
                long roleId = rs.getLong("role_id");
                boolean check = plainPassword.equals(dbPassword);
                LOGGER.info("    " + username + " | role=" + roleId
                        + " | password match → " + (check ? "OK ✅" : "FAIL ❌"));
            }

            LOGGER.info("=== DataSeeder HOÀN TẤT ===");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi DB", e);
        }
    }
}
