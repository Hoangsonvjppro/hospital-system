package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.DrugInteraction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO tương tác thuốc — kiểm tra cặp thuốc xung đột.
 */
public class DrugInteractionDAO extends AbstractDAO {

    private static final Logger LOGGER = Logger.getLogger(DrugInteractionDAO.class.getName());

    /**
     * Tìm tương tác thuốc-thuốc cho danh sách medicine IDs.
     * Trả về các cặp tương tác giữa các thuốc trong danh sách.
     *
     * @param medicineIds danh sách ID thuốc cần kiểm tra
     * @return danh sách tương tác phát hiện được
     */
    public List<DrugInteraction> findInteractions(List<Integer> medicineIds) {
        List<DrugInteraction> result = new ArrayList<>();
        if (medicineIds == null || medicineIds.size() < 2) return result;

        String placeholders = String.join(",", medicineIds.stream().map(id -> "?").toList());
        String sql = """
            SELECT di.interaction_id, di.medicine_id_1, di.medicine_id_2,
                   di.severity, di.description, di.recommendation,
                   m1.medicine_name AS name1, m2.medicine_name AS name2
            FROM DrugInteraction di
            JOIN Medicine m1 ON di.medicine_id_1 = m1.medicine_id
            JOIN Medicine m2 ON di.medicine_id_2 = m2.medicine_id
            WHERE di.medicine_id_1 IN (%s) AND di.medicine_id_2 IN (%s)
            """.formatted(placeholders, placeholders);

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            for (int medId : medicineIds) ps.setInt(idx++, medId);
            for (int medId : medicineIds) ps.setInt(idx++, medId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra tương tác thuốc", e);
            throw new DataAccessException("Lỗi kiểm tra tương tác thuốc", e);
        }
        return result;
    }

    public List<DrugInteraction> findAll() {
        List<DrugInteraction> result = new ArrayList<>();
        String sql = """
            SELECT di.interaction_id, di.medicine_id_1, di.medicine_id_2,
                   di.severity, di.description, di.recommendation,
                   m1.medicine_name AS name1, m2.medicine_name AS name2
            FROM DrugInteraction di
            JOIN Medicine m1 ON di.medicine_id_1 = m1.medicine_id
            JOIN Medicine m2 ON di.medicine_id_2 = m2.medicine_id
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách tương tác thuốc", e);
            throw new DataAccessException("Lỗi lấy danh sách tương tác thuốc", e);
        }
        return result;
    }

    public boolean insert(DrugInteraction di) {
        String sql = """
            INSERT INTO DrugInteraction (medicine_id_1, medicine_id_2, severity, description, recommendation)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, di.getMedicineId1());
            ps.setInt(2, di.getMedicineId2());
            ps.setString(3, di.getSeverity());
            ps.setString(4, di.getDescription());
            ps.setString(5, di.getRecommendation());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) di.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm tương tác thuốc", e);
            throw new DataAccessException("Lỗi thêm tương tác thuốc", e);
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM DrugInteraction WHERE interaction_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa tương tác thuốc", e);
            throw new DataAccessException("Lỗi xóa tương tác thuốc", e);
        }
    }

    private DrugInteraction mapRow(ResultSet rs) throws SQLException {
        DrugInteraction di = new DrugInteraction();
        di.setId(rs.getInt("interaction_id"));
        di.setMedicineId1(rs.getInt("medicine_id_1"));
        di.setMedicineId2(rs.getInt("medicine_id_2"));
        di.setSeverity(rs.getString("severity"));
        di.setDescription(rs.getString("description"));
        di.setRecommendation(rs.getString("recommendation"));
        di.setMedicineName1(rs.getString("name1"));
        di.setMedicineName2(rs.getString("name2"));
        return di;
    }
}
