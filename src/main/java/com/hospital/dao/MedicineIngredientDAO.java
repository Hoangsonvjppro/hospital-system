package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.MedicineIngredient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MedicineIngredientDAO {

    private static final Logger LOGGER = Logger.getLogger(MedicineIngredientDAO.class.getName());

    public List<MedicineIngredient> findByMedicine(int medicineId) {
        String sql = """
            SELECT mi.*, m.medicine_name
            FROM MedicineIngredient mi
            LEFT JOIN Medicine m ON mi.medicine_id = m.medicine_id
            WHERE mi.medicine_id = ?
            ORDER BY mi.ingredient_name
            """;
        List<MedicineIngredient> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn thành phần thuốc theo medicineId=" + medicineId, e);
            throw new DataAccessException("Lỗi truy vấn thành phần thuốc", e);
        }
        return list;
    }

    public void insert(MedicineIngredient ingredient) {
        String sql = "INSERT INTO MedicineIngredient (medicine_id, ingredient_name) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ingredient.getMedicineId());
            ps.setString(2, ingredient.getIngredientName());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm thành phần thuốc", e);
            throw new DataAccessException("Lỗi thêm thành phần thuốc", e);
        }
    }

    public void delete(int medicineId, String ingredientName) {
        String sql = "DELETE FROM MedicineIngredient WHERE medicine_id = ? AND ingredient_name = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ps.setString(2, ingredientName);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa thành phần thuốc", e);
            throw new DataAccessException("Lỗi xóa thành phần thuốc", e);
        }
    }

    public List<MedicineIngredient> findAll() {
        String sql = """
            SELECT mi.*, m.medicine_name
            FROM MedicineIngredient mi
            LEFT JOIN Medicine m ON mi.medicine_id = m.medicine_id
            ORDER BY m.medicine_name, mi.ingredient_name
            """;
        List<MedicineIngredient> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn tất cả thành phần thuốc", e);
            throw new DataAccessException("Lỗi truy vấn thành phần thuốc", e);
        }
        return list;
    }

    private MedicineIngredient mapResultSet(ResultSet rs) throws SQLException {
        MedicineIngredient mi = new MedicineIngredient();
        mi.setId(rs.getInt("ingredient_id"));
        mi.setMedicineId(rs.getInt("medicine_id"));
        mi.setIngredientName(rs.getString("ingredient_name"));
        try { mi.setMedicineName(rs.getString("medicine_name")); } catch (SQLException ignored) {}
        return mi;
    }
}
