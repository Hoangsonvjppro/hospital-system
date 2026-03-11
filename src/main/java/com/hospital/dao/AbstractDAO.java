package com.hospital.dao;

import com.hospital.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractDAO {

    private static final Logger LOGGER = Logger.getLogger(AbstractDAO.class.getName());

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getInstance().getConnection();
    }

    protected Connection getTransactionalConnection() throws SQLException {
        return DatabaseConfig.getInstance().getTransactionalConnection();
    }

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
            }
            closeQuietly(conn);
        }
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T execute(Connection conn) throws Exception;
    }
}
