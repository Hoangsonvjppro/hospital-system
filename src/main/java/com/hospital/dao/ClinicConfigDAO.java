package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.ClinicConfig;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClinicConfigDAO {

    private static final Logger LOGGER = Logger.getLogger(ClinicConfigDAO.class.getName());


    public ClinicConfig loadAll() {
        ClinicConfig cfg = new ClinicConfig();
        String sql = "SELECT config_key, config_value FROM ClinicConfig";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String key   = rs.getString("config_key");
                String value = rs.getString("config_value");
                mapToField(cfg, key, value);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đọc ClinicConfig", e);
            throw new DataAccessException("Không thể đọc cấu hình phòng khám", e);
        }
        return cfg;
    }

   
    public String getValue(String configKey) {
        String sql = "SELECT config_value FROM ClinicConfig WHERE config_key = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, configKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("config_value");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Lỗi đọc config key=" + configKey, e);
        }
        return null;
    }

    public boolean upsert(String configKey, String configValue) {
        String sql = "INSERT INTO ClinicConfig (config_key, config_value, updated_at) "
                   + "VALUES (?, ?, NOW()) "
                   + "ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), updated_at = NOW()";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, configKey);
            ps.setString(2, configValue);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật config key=" + configKey, e);
            throw new DataAccessException("Không thể cập nhật cấu hình", e);
        }
    }

    public void saveAll(ClinicConfig cfg) {
        upsert(ClinicConfig.KEY_CLINIC_NAME,    cfg.getClinicName());
        upsert(ClinicConfig.KEY_CLINIC_ADDRESS,  cfg.getClinicAddress());
        upsert(ClinicConfig.KEY_CLINIC_PHONE,    cfg.getClinicPhone());
        upsert(ClinicConfig.KEY_CLINIC_EMAIL,    cfg.getClinicEmail());
        upsert(ClinicConfig.KEY_EXAM_FEE,        String.valueOf((long) cfg.getDefaultExamFee()));
        upsert(ClinicConfig.KEY_WORKING_HOURS,   cfg.getWorkingHours());
        upsert(ClinicConfig.KEY_INVOICE_PREFIX,  cfg.getInvoicePrefix());
    }

    private void mapToField(ClinicConfig cfg, String key, String value) {
        if (key == null || value == null) return;
        switch (key) {
            case ClinicConfig.KEY_CLINIC_NAME    -> cfg.setClinicName(value);
            case ClinicConfig.KEY_CLINIC_ADDRESS -> cfg.setClinicAddress(value);
            case ClinicConfig.KEY_CLINIC_PHONE   -> cfg.setClinicPhone(value);
            case ClinicConfig.KEY_CLINIC_EMAIL   -> cfg.setClinicEmail(value);
            case ClinicConfig.KEY_EXAM_FEE       -> {
                try { cfg.setDefaultExamFee(Double.parseDouble(value)); }
                catch (NumberFormatException ignored) {}
            }
            case ClinicConfig.KEY_WORKING_HOURS  -> cfg.setWorkingHours(value);
            case ClinicConfig.KEY_INVOICE_PREFIX  -> cfg.setInvoicePrefix(value);
            default -> LOGGER.fine("Unknown config key: " + key);
        }
    }
}
