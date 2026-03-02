package com.hospital.bus;

import com.hospital.dao.ClinicConfigDAO;
import com.hospital.model.ClinicConfig;

/**
 * Business logic layer cho Cấu hình phòng khám.
 * <p>
 * Cache nội bộ — load 1 lần, ghi → invalidate cache.
 */
public class ClinicConfigBUS {

    private final ClinicConfigDAO dao = new ClinicConfigDAO();
    private ClinicConfig cached;

    /**
     * Lấy toàn bộ cấu hình (có cache).
     */
    public ClinicConfig getConfig() {
        if (cached == null) {
            cached = dao.loadAll();
        }
        return cached;
    }

    /**
     * Lấy phí khám mặc định.
     */
    public double getDefaultExamFee() {
        return getConfig().getDefaultExamFee();
    }

    /**
     * Lấy tên phòng khám.
     */
    public String getClinicName() {
        return getConfig().getClinicName();
    }

    /**
     * Lấy giá trị 1 key bất kỳ (không qua cache).
     */
    public String getValue(String key) {
        return dao.getValue(key);
    }

    /**
     * Lưu toàn bộ cấu hình.
     */
    public void saveConfig(ClinicConfig cfg) {
        dao.saveAll(cfg);
        cached = cfg;   // refresh cache
    }

    /**
     * Xóa cache, lần lấy tiếp sẽ query DB.
     */
    public void invalidateCache() {
        cached = null;
    }
}
