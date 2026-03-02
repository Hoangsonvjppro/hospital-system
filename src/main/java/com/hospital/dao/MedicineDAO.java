package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Medicine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MedicineDAO implements BaseDAO<Medicine> {

    private static final Logger LOGGER = Logger.getLogger(MedicineDAO.class.getName());

    private Connection externalConnection;

    public MedicineDAO() {
        // Mode 1: Tự lấy connection (cho thao tác đơn lẻ)
    }

    public MedicineDAO(Connection connection) {
        // Mode 2: Dùng external connection (cho transaction)
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

    public List<Medicine> findByName(String keyword) {
        List<Medicine> arr = new ArrayList<>();
        String sql = "Select * from Medicine where is_active=true and medicine_name like ?";
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
            LOGGER.log(Level.SEVERE, "Không thể tìm thuốc theo tên: " + keyword, e);
            throw new DataAccessException("Không thể tìm thuốc theo tên: " + keyword, e);
        } finally {
            closeIfOwned(conn);
        }
        return arr;
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
                if (entity.getExpiryDate() != null) {
                    ps.setDate(7, java.sql.Date.valueOf(entity.getExpiryDate()));
                } else {
                    ps.setNull(7, Types.DATE);
                }
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
                if (entity.getExpiryDate() != null) {
                    ps.setDate(7, java.sql.Date.valueOf(entity.getExpiryDate()));
                } else {
                    ps.setNull(7, Types.DATE);
                }
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
    public List<Medicine> getLowStockMedicines(){
        List<Medicine> arr=new ArrayList<>();
        String sql="Select * from Medicine where is_active=true and stock_qty<=min_threshold";
        Connection conn=null;
        try {
            conn=getConnection();
            try(Statement stm=conn.createStatement()) {
                ResultSet rs=stm.executeQuery(sql);
                while(rs.next()){
                    arr.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn thuốc", e);
            throw new DataAccessException("Lỗi truy vấn thuốc",e);
        }finally {
            closeIfOwned(conn);
        }
        return arr;
    }
    public List<Medicine> getExpiryDateMedicines(){
        List<Medicine> arr=new ArrayList<>();
        String sql="Select * from Medicine where is_active=true and expiry_date is not null " +
                "and expiry_date between curdate() and date_add(curdate(),interval 30 day)";
        Connection con=null;
        try {
            con=getConnection();
            try(Statement stm=con.createStatement()) {
                ResultSet rs= stm.executeQuery(sql);
                while (rs.next()){
                    arr.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn thuốc", e);
            throw new DataAccessException("Lỗi truy vấn thuốc",e);
        }finally {
            closeIfOwned(con);
        }
        return arr;
    }
    //nhập kho bằng Transaction
    public boolean importMedicineStock(int medicineId, int importQty, int userId, String notes) throws Exception {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction
            String checkStockSql = "select stock_qty FROM Medicine where medicine_id = ? for update";
            int stockBefore = 0;
            try (PreparedStatement psCheck = conn.prepareStatement(checkStockSql)) {
                psCheck.setInt(1, medicineId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) stockBefore = rs.getInt("stock_qty");
                    else throw new Exception("Không tìm thấy thuốc!");
                }
            }
            int stockAfter = stockBefore + importQty;
            String updateSQL = "update Medicine set stock_qty = ? where medicine_id = ?";
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSQL)) {
                psUpdate.setInt(1, stockAfter);
                psUpdate.setInt(2, medicineId);
                psUpdate.executeUpdate();
            }
            String sql = "insert into StockTransaction " +
                    "(medicine_id, transaction_type, quantity, stock_before, stock_after, reference_type, notes, created_by) " +
                    "values (?, 'IMPORT', ?, ?, ?, 'MANUAL_IMPORT', ?, ?)";
            try (PreparedStatement psTrans = conn.prepareStatement(sql)) {
                psTrans.setInt(1, medicineId);
                psTrans.setInt(2, importQty); // Nhập kho -> quantity dương
                psTrans.setInt(3, stockBefore);
                psTrans.setInt(4, stockAfter);
                psTrans.setString(5, notes);
                psTrans.setInt(6, userId);
                psTrans.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw new DataAccessException("Lỗi Database khi nhập kho: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }
    //Top thuốc xuất
    public List<Object[]> getTopExportedMedicines() {
        List<Object[]> list = new ArrayList<>();
        String sql = "select m.medicine_name, sum(abs(s.quantity)) as total_exported " +
                "from StockTransaction s JOIN Medicine m ON s.medicine_id = m.medicine_id " +
                "where s.transaction_type = 'EXPORT' " +
                "group by s.medicine_id, m.medicine_name " +
                "order by total_exported desc limit 10";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{rs.getString("medicine_name"), rs.getInt("total_exported")});
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn top thuốc xuất", e);
        }
        return list;
    }

    private Medicine mapResultSet(ResultSet rs) throws SQLException {
        Medicine thuoc = new Medicine();
        thuoc.setId(rs.getInt("medicine_id"));
        thuoc.setMedicineCode(rs.getString("medicine_code"));
        thuoc.setMedicineName(rs.getString("medicine_name"));
        thuoc.setUnit(rs.getString("unit"));
        thuoc.setCostPrice(rs.getDouble("cost_price"));
        thuoc.setSellPrice(rs.getDouble("sell_price"));
        thuoc.setStockQty(rs.getInt("stock_qty"));
        thuoc.setMinThreshold(rs.getInt("min_threshold"));
        thuoc.setManufacturer(rs.getString("manufacturer"));
        if (rs.getDate("expiry_date") != null) {
            thuoc.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
        }
        thuoc.setDescription(rs.getString("description"));
        thuoc.setActive(rs.getBoolean("is_active"));
        return thuoc;
    }
}