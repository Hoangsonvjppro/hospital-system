package com.hospital.dao;

import com.hospital.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract base class cho tất cả DAO.
 * <p>
 * Cung cấp helper methods cho connection management, resource cleanup, và transactions.
 * <p>
 * Sử dụng:
 * <pre>
 *   public class PatientDAO extends AbstractDAO implements BaseDAO&lt;Patient&gt; {
 *       public Patient findById(int id) {
 *           try (Connection conn = getConnection();
 *                PreparedStatement ps = conn.prepareStatement("SELECT ...")) {
 *               // ...
 *           }
 *       }
 *   }
 * </pre>
 */
public abstract class AbstractDAO {

    private static final Logger LOGGER = Logger.getLogger(AbstractDAO.class.getName());

    /**
     * Lấy connection từ pool.
     */
    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getInstance().getConnection();
    }

    /**
     * Lấy connection với autoCommit=false.
     */
    protected Connection getTransactionalConnection() throws SQLException {
        return DatabaseConfig.getInstance().getTransactionalConnection();
    }

    /**
     * Đóng resource một cách an toàn (không ném exception).
     *
     * @param resources danh sách AutoCloseable cần đóng
     */
    protected void closeQuietly(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Lỗi khi đóng resource", e);
            }
        }
    }

    /**
     * Thực thi logic trong một database transaction.
     * Tự động commit nếu thành công, rollback nếu lỗi.
     *
     * @param callback logic cần thực thi trong transaction
     * @param <T>      kiểu kết quả trả về
     * @return kết quả từ callback
     * @throws SQLException nếu có lỗi DB
     */
    protected <T> T executeInTransaction(TransactionCallback<T> callback) throws SQLException {
        Connection conn = getTransactionalConnection();
        try {
            T result = callback.execute(conn);
            conn.commit();
            return result;
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Lỗi rollback transaction", rollbackEx);
            }
            if (e instanceof SQLException sqlEx) {
                throw sqlEx;
            }
            throw new SQLException("Transaction failed", e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
                // connection sắp close, bỏ qua
            }
            closeQuietly(conn);
        }
    }

    /**
     * Functional interface cho transaction callback.
     *
     * @param <T> kiểu kết quả
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {
        T execute(Connection conn) throws Exception;
    }
}
