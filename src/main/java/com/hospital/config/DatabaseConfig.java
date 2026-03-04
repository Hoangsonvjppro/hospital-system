package com.hospital.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Quản lý kết nối cơ sở dữ liệu — HikariCP Connection Pool.
 * <p>
 * Đọc cấu hình từ {@code application.properties} trên classpath.
 * Nếu không tìm thấy file, sử dụng giá trị mặc định.
 * <p>
 * Sử dụng:
 * <pre>
 *   Connection conn = DatabaseConfig.getInstance().getConnection();
 *   // ... thao tác DB ...
 *   conn.close(); // trả connection về pool
 * </pre>
 */
public class DatabaseConfig {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());

    // ── Giá trị mặc định ──────────────────────────────────────
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/clinic_management"
            + "?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "123456";

    // ── Singleton ─────────────────────────────────────────────
    private static volatile DatabaseConfig instance;
    private final HikariDataSource dataSource;

    // ══════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ══════════════════════════════════════════════════════════

    private DatabaseConfig() {
        Properties props = loadProperties();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url", DEFAULT_URL));
        config.setUsername(props.getProperty("db.user", DEFAULT_USER));
        config.setPassword(props.getProperty("db.password", DEFAULT_PASSWORD));

        // Pool settings
        config.setMaximumPoolSize(
                Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "10")));
        config.setMinimumIdle(
                Integer.parseInt(props.getProperty("db.pool.minimumIdle", "2")));
        config.setConnectionTimeout(
                Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));
        config.setIdleTimeout(
                Long.parseLong(props.getProperty("db.pool.idleTimeout", "600000")));
        config.setMaxLifetime(
                Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));

        // Tuning
        config.setPoolName("ClinicHikariPool");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
        LOGGER.info("HikariCP pool khởi tạo thành công — " + config.getJdbcUrl());
    }

    // ══════════════════════════════════════════════════════════
    //  SINGLETON
    // ══════════════════════════════════════════════════════════

    public static DatabaseConfig getInstance() {
        if (instance == null) {
            synchronized (DatabaseConfig.class) {
                if (instance == null) {
                    instance = new DatabaseConfig();
                }
            }
        }
        return instance;
    }

    // ══════════════════════════════════════════════════════════
    //  PUBLIC API
    // ══════════════════════════════════════════════════════════

    /**
     * Lấy connection từ pool.
     * <b>Luôn close connection sau khi dùng</b> (try-with-resources)
     * để trả về pool.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Lấy connection với autoCommit=false cho transaction.
     * Caller phải tự commit/rollback và close.
     */
    public Connection getTransactionalConnection() throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    /**
     * Đóng pool khi app thoát.
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("HikariCP pool đã đóng.");
        }
    }

    // ══════════════════════════════════════════════════════════
    //  INTERNAL
    // ══════════════════════════════════════════════════════════

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
                LOGGER.info("Đã load application.properties");
            } else {
                LOGGER.warning("Không tìm thấy application.properties — dùng giá trị mặc định");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Lỗi đọc application.properties — dùng giá trị mặc định", e);
        }
        return props;
    }
}
