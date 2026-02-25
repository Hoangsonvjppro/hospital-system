package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.model.Patient;
import com.hospital.util.AppUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO implements BaseDAO<Patient> {

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getInstance().getConnection();
    }

    @Override
    public Patient findById(int id) {
        String sql = "SELECT * FROM Patient WHERE patient_id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }

        } catch (SQLException e) {
            AppUtils.showError(null, "Lỗi database");
        }
        return null;
    }

    @Override
    public List<Patient> findAll() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM Patient WHERE is_active = true";

        try (Statement stm = getConnection().createStatement()) {
            ResultSet rs = stm.executeQuery(sql);

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            AppUtils.showError(null, "Lỗi database");
        }

        return list;
    }

    @Override
    public boolean insert(Patient entity) {

        String sql = """
                INSERT INTO Patient
                (full_name, gender, date_of_birth, phone, address,
                 is_active)
                VALUES (?,?,?,?,?,?)
                """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {

            ps.setString(1, entity.getFullName());

            if (entity.getGender() != null) {
                ps.setString(2, entity.getGender().name());
            } else {
                ps.setString(2, "OTHER");
            }

            if (entity.getDateOfBirth() != null) {
                ps.setDate(3, Date.valueOf(entity.getDateOfBirth()));
            } else {
                ps.setNull(3, Types.DATE);
            }

            ps.setString(4, entity.getPhone());
            ps.setString(5, entity.getAddress());
            ps.setBoolean(6, entity.isActive());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            AppUtils.showError(null, "Không thể thêm bệnh nhân");
        }

        return false;
    }

    @Override
    public boolean update(Patient entity) {

        String sql = """
                UPDATE Patient
                SET full_name=?,
                    gender=?,
                    date_of_birth=?,
                    phone=?,
                    address=?,
                    is_active=?
                WHERE patient_id=?
                """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {

            ps.setString(1, entity.getFullName());

            if (entity.getGender() != null) {
                ps.setString(2, entity.getGender().name());
            } else {
                ps.setString(2, "OTHER");
            }

            if (entity.getDateOfBirth() != null) {
                ps.setDate(3, Date.valueOf(entity.getDateOfBirth()));
            } else {
                ps.setNull(3, Types.DATE);
            }

            ps.setString(4, entity.getPhone());
            ps.setString(5, entity.getAddress());
            ps.setBoolean(6, entity.isActive());
            ps.setInt(7, entity.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            AppUtils.showError(null, "Không thể cập nhật");
        }

        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE Patient SET is_active=false WHERE patient_id=?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            AppUtils.showError(null, "Không thể xoá");
        }

        return false;
    }

    private Patient mapResultSet(ResultSet rs) throws SQLException {

        Patient p = new Patient();

        p.setId(rs.getInt("patient_id"));
        p.setFullName(rs.getString("full_name"));
        p.setPhone(rs.getString("phone"));
        p.setAddress(rs.getString("address"));

        String genderStr = rs.getString("gender");
        if (genderStr != null) {
            p.setGender(Patient.Gender.valueOf(genderStr));
        }

        if (rs.getDate("date_of_birth") != null) {
            p.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        }

        p.setActive(rs.getBoolean("is_active"));

        if (rs.getTimestamp("created_at") != null) {
            p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }

        if (rs.getTimestamp("updated_at") != null) {
            p.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }

        return p;
    }
}
