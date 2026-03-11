package com.hospital.dao;

import com.hospital.exception.DataAccessException;
import com.hospital.model.Icd10Code;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Icd10CodeDAO extends AbstractDAO {

    private static final Logger LOGGER = Logger.getLogger(Icd10CodeDAO.class.getName());

    public List<Icd10Code> search(String keyword) {
        List<Icd10Code> result = new ArrayList<>();
        String sql = """
            SELECT icd_id, code, name_vi, name_en, category
            FROM Icd10Code
            WHERE code LIKE ? OR name_vi LIKE ? OR name_en LIKE ?
            ORDER BY code
            LIMIT 20
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm kiếm ICD-10", e);
            throw new DataAccessException("Lỗi tìm kiếm ICD-10", e);
        }
        return result;
    }

    public Icd10Code findByCode(String code) {
        String sql = "SELECT icd_id, code, name_vi, name_en, category FROM Icd10Code WHERE code = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm ICD-10 theo code", e);
            throw new DataAccessException("Lỗi tìm ICD-10 theo code", e);
        }
        return null;
    }

    public List<Icd10Code> findAll() {
        List<Icd10Code> result = new ArrayList<>();
        String sql = "SELECT icd_id, code, name_vi, name_en, category FROM Icd10Code ORDER BY code";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách ICD-10", e);
            throw new DataAccessException("Lỗi lấy danh sách ICD-10", e);
        }
        return result;
    }

    private Icd10Code mapRow(ResultSet rs) throws SQLException {
        Icd10Code c = new Icd10Code();
        c.setId(rs.getInt("icd_id"));
        c.setCode(rs.getString("code"));
        c.setNameVi(rs.getString("name_vi"));
        c.setNameEn(rs.getString("name_en"));
        c.setCategory(rs.getString("category"));
        return c;
    }
}
