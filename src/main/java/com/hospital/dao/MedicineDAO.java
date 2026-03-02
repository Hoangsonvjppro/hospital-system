package com.hospital.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Medicine;

public class MedicineDAO implements BaseDAO<Medicine> {

    private static final Logger LOGGER = Logger.getLogger(MedicineDAO.class.getName());

    private Connection externalConnection;

    public MedicineDAO() {}

    public MedicineDAO(Connection connection) {
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

    // =====================================================
    // 🔹 THÊM: LẤY TỒN KHO THEO ID (CHO KÊ ĐƠN)
    // =====================================================
    public int getStockById(int id) {
        String sql = "SELECT stock_qty FROM Medicine WHERE medicine_id=? AND is_active=true";
        Connection conn = null;

        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("stock_qty");
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy tồn kho thuốc ID=" + id, e);
            throw new DataAccessException("Lỗi lấy tồn kho thuốc ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }

        return 0;
    }

    // =====================================================
    // 🔹 THÊM: TRỪ TỒN KHO (KHI PHÁT THUỐC)
    // =====================================================
    public boolean decreaseStock(int id, int quantity) {

        String sql = """
            UPDATE Medicine
            SET stock_qty = stock_qty - ?
            WHERE medicine_id = ?
            AND stock_qty >= ?
        """;

        Connection conn = null;

        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, quantity);
                ps.setInt(2, id);
                ps.setInt(3, quantity);

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể trừ tồn kho thuốc ID=" + id, e);
            throw new DataAccessException("Không thể trừ tồn kho thuốc ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    // =====================================================
    // 🔹 THÊM: KIỂM TRA THUỐC CÒN HOẠT ĐỘNG
    // =====================================================
    public boolean existsAndActive(int id) {

        String sql = "SELECT 1 FROM Medicine WHERE medicine_id=? AND is_active=true";

        Connection conn = null;

        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra thuốc ID=" + id, e);
            throw new DataAccessException("Lỗi kiểm tra thuốc ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    // =====================================================
    // 🔹 THÊM: TÌM THUỐC CÒN HÀNG
    // =====================================================
    public List<Medicine> findAvailableByName(String keyword) {

        List<Medicine> arr = new ArrayList<>();

        String sql = """
            SELECT * FROM Medicine
            WHERE is_active=true
            AND stock_qty > 0
            AND medicine_name LIKE ?
        """;

        Connection conn = null;

        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, "%" + keyword + "%");

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        arr.add(mapResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể tìm thuốc còn hàng", e);
            throw new DataAccessException("Không thể tìm thuốc còn hàng", e);
        } finally {
            closeIfOwned(conn);
        }

        return arr;
    }


    @Override
    public Medicine findById(int id) {
        String sql = "Select * from Medicine where medicine_id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn thuốc ID=" + id, e);
            throw new DataAccessException("Lỗi truy vấn thuốc ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<Medicine> findAll() {
        List<Medicine> arr = new ArrayList<>();
        String sql = "SELECT * FROM Medicine WHERE is_active = true";
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stm = conn.createStatement();
                 ResultSet rs = stm.executeQuery(sql)) {
                while (rs.next()) {
                    arr.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách thuốc", e);
            throw new DataAccessException("Lỗi truy vấn danh sách thuốc", e);
        } finally {
            closeIfOwned(conn);
        }
        return arr;
    }

    @Override
    public boolean insert(Medicine entity) {
        String sql = "INSERT INTO Medicine (medicine_name, unit, cost_price, sell_price, stock_qty, min_threshold, expiry_date, description, is_active) VALUES (?,?,?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, entity.getMedicineName());
                ps.setString(2, entity.getUnit());
                ps.setDouble(3, entity.getCostPrice());
                ps.setDouble(4, entity.getSellPrice());
                ps.setInt(5, entity.getStockQty());
                ps.setInt(6, entity.getMinThreshold());
                if (entity.getExpiryDate() != null)
                    ps.setDate(7, java.sql.Date.valueOf(entity.getExpiryDate()));
                else
                    ps.setNull(7, Types.DATE);
                ps.setString(8, entity.getDescription());
                ps.setBoolean(9, entity.isActive());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể thêm thuốc", e);
            throw new DataAccessException("Không thể thêm thuốc", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean update(Medicine entity) {
        String sql = "UPDATE Medicine SET medicine_name=?, unit=?, cost_price=?, sell_price=?, stock_qty=?, min_threshold=?, expiry_date=?, description=?, is_active=? WHERE medicine_id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, entity.getMedicineName());
                ps.setString(2, entity.getUnit());
                ps.setDouble(3, entity.getCostPrice());
                ps.setDouble(4, entity.getSellPrice());
                ps.setInt(5, entity.getStockQty());
                ps.setInt(6, entity.getMinThreshold());
                if (entity.getExpiryDate() != null)
                    ps.setDate(7, java.sql.Date.valueOf(entity.getExpiryDate()));
                else
                    ps.setNull(7, Types.DATE);
                ps.setString(8, entity.getDescription());
                ps.setBoolean(9, entity.isActive());
                ps.setInt(10, entity.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật thuốc ID=" + entity.getId(), e);
            throw new DataAccessException("Không thể cập nhật thuốc ID=" + entity.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE Medicine SET is_active = false WHERE medicine_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể xoá thuốc ID=" + id, e);
            throw new DataAccessException("Không thể xoá thuốc ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    public boolean reduceStock(int medicineId, int quantity) {
        String sql = "UPDATE Medicine SET stock_qty = stock_qty - ? WHERE medicine_id = ? AND stock_qty >= ?";
        Connection conn = null;
        try {
           conn = getConnection();
           try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, quantity);
                ps.setInt(2, medicineId);
                ps.setInt(3, quantity);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi giảm tồn kho", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    private Medicine mapResultSet(ResultSet rs) throws SQLException {
        Medicine thuoc = new Medicine();
        thuoc.setId(rs.getInt("medicine_id"));
        thuoc.setMedicineName(rs.getString("medicine_name"));
        thuoc.setUnit(rs.getString("unit"));
        thuoc.setCostPrice(rs.getDouble("cost_price"));
        thuoc.setSellPrice(rs.getDouble("sell_price"));
        thuoc.setStockQty(rs.getInt("stock_qty"));
        thuoc.setMinThreshold(rs.getInt("min_threshold"));
        if (rs.getDate("expiry_date") != null)
            thuoc.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
        thuoc.setDescription(rs.getString("description"));
        thuoc.setActive(rs.getBoolean("is_active"));
        return thuoc;
    }

    public List<Medicine> findByName(String keyword) {

    String sql = "SELECT * FROM Medicine WHERE medicine_name LIKE ? AND is_active = true";

    List<Medicine> list = new ArrayList<>();
    Connection conn = null;

    try {
        conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));   
                }
            }
        }
    } catch (SQLException e) {
        throw new DataAccessException("Lỗi tìm thuốc theo tên", e);
    } finally {
        closeIfOwned(conn);
    }

    return list;
}
}
