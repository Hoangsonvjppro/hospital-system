package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.MedicineIngredient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineIngredientDAO implements BaseDAO<MedicineIngredient> {

    private Connection externalConnection;

    public MedicineIngredientDAO() {}
    public MedicineIngredientDAO(Connection connection) { this.externalConnection = connection; }

    private Connection getConnection() throws SQLException {
        return externalConnection != null ? externalConnection : DatabaseConfig.getInstance().getConnection();
    }

    private void closeIfOwned(Connection conn) {
        if (externalConnection == null && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public MedicineIngredient findById(int id) {
        String sql = "SELECT * FROM MedicineIngredient WHERE ingredient_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn thành phần thuốc ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<MedicineIngredient> findAll() {
        List<MedicineIngredient> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM MedicineIngredient")) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn danh sách thành phần thuốc", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<MedicineIngredient> findByMedicineId(long medicineId) {
        List<MedicineIngredient> list = new ArrayList<>();
        String sql = "SELECT * FROM MedicineIngredient WHERE medicine_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, medicineId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn thành phần của thuốc ID=" + medicineId, e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(MedicineIngredient entity) {
        String sql = "INSERT INTO MedicineIngredient (medicine_id, ingredient_name) VALUES (?,?)";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, entity.getMedicineId());
                ps.setString(2, entity.getIngredientName());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) entity.setId(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm thành phần thuốc", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    @Override
    public boolean update(MedicineIngredient entity) {
        String sql = "UPDATE MedicineIngredient SET medicine_id=?, ingredient_name=? WHERE ingredient_id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, entity.getMedicineId());
                ps.setString(2, entity.getIngredientName());
                ps.setInt(3, entity.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật thành phần thuốc ID=" + entity.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM MedicineIngredient WHERE ingredient_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể xóa thành phần thuốc ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    private MedicineIngredient mapResultSet(ResultSet rs) throws SQLException {
        MedicineIngredient mi = new MedicineIngredient();
        mi.setId(rs.getInt("ingredient_id"));
        mi.setMedicineId(rs.getLong("medicine_id"));
        mi.setIngredientName(rs.getString("ingredient_name"));
        return mi;
    }
}
