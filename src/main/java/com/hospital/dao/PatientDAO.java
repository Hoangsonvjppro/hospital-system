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
                (patient_name, phone, address, date_of_birth,
                 is_active, created_at, updated_at)
                VALUES (?,?,?,?,?,?,?)
                """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {

            ps.setString(1, entity.getPatientName());
            ps.setString(2, entity.getPhone());
            ps.setString(3, entity.getAddress());

            if (entity.getDateOfBirth() != null) {
                ps.setDate(4, Date.valueOf(entity.getDateOfBirth()));
            } else {
                ps.setNull(4, Types.DATE);
            }

            ps.setBoolean(5, entity.isActive());

            Timestamp now = Timestamp.valueOf(java.time.LocalDateTime.now());
            ps.setTimestamp(6, now);
            ps.setTimestamp(7, now);

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
                SET patient_name=?,
                    phone=?,
                    address=?,
                    date_of_birth=?,
                    is_active=?,
                    updated_at=?
                WHERE patient_id=?
                """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {

            ps.setString(1, entity.getPatientName());
            ps.setString(2, entity.getPhone());
            ps.setString(3, entity.getAddress());

            if (entity.getDateOfBirth() != null) {
                ps.setDate(4, Date.valueOf(entity.getDateOfBirth()));
            } else {
                ps.setNull(4, Types.DATE);
            }

            ps.setBoolean(5, entity.isActive());
            ps.setTimestamp(6, Timestamp.valueOf(java.time.LocalDateTime.now()));
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
        p.setPatientName(rs.getString("patient_name"));
        p.setPhone(rs.getString("phone"));
        p.setAddress(rs.getString("address"));

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
