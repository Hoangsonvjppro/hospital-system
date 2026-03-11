package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Appointment;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppointmentDAO extends AbstractDAO implements BaseDAO<Appointment> {

    private static final Logger LOGGER = Logger.getLogger(AppointmentDAO.class.getName());

    private Connection externalConnection;

    public AppointmentDAO() {}

    public AppointmentDAO(Connection connection) {
        this.externalConnection = connection;
    }

    private Connection conn() throws SQLException {
        if (externalConnection != null) return externalConnection;
        return getConnection();
    }

    private void closeIfOwned(Connection c) {
        if (externalConnection == null && c != null) {
            closeQuietly(c);
        }
    }

    static String dbToDisplay(String dbStatus) {
        if (dbStatus == null) return "Mới";
        return switch (dbStatus) {
            case "SCHEDULED"  -> "Mới";
            case "CHECKED_IN" -> "Đã xác nhận";
            case "COMPLETED"  -> "Đã khám";
            case "CANCELLED"  -> "Hủy";
            default           -> "Mới";
        };
    }

    static String displayToDb(String display) {
        if (display == null) return "SCHEDULED";
        return switch (display) {
            case "Mới"         -> "SCHEDULED";
            case "Đã xác nhận" -> "CHECKED_IN";
            case "Đã khám"     -> "COMPLETED";
            case "Hủy"         -> "CANCELLED";
            default            -> "SCHEDULED";
        };
    }

    private static final String SELECT_BASE =
            "SELECT a.appointment_id, a.patient_id, a.doctor_id, "
          + "       a.appointment_date, a.start_time, a.end_time, "
          + "       a.status, a.reason, "
          + "       p.full_name AS patient_name, p.phone AS patient_phone, "
          + "       u.full_name AS doctor_name "
          + "FROM Appointment a "
          + "JOIN Patient p ON a.patient_id = p.patient_id "
          + "JOIN Doctor  d ON a.doctor_id  = d.doctor_id "
          + "JOIN `User`  u ON d.user_id    = u.user_id ";

    @Override
    public Appointment findById(int id) {
        String sql = SELECT_BASE + "WHERE a.appointment_id = ?";
        Connection c = null;
        try {
            c = conn();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn lịch hẹn ID=" + id, e);
            throw new DataAccessException("Lỗi truy vấn lịch hẹn ID=" + id, e);
        } finally {
            closeIfOwned(c);
        }
        return null;
    }

    @Override
    public List<Appointment> findAll() {
        List<Appointment> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE a.is_active = true ORDER BY a.appointment_date, a.start_time";
        Connection c = null;
        try {
            c = conn();
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách lịch hẹn", e);
            throw new DataAccessException("Lỗi truy vấn danh sách lịch hẹn", e);
        } finally {
            closeIfOwned(c);
        }
        return list;
    }

    @Override
    public boolean insert(Appointment a) {
        String sql = "INSERT INTO Appointment (patient_id, doctor_id, appointment_date, "
                   + "start_time, end_time, status, reason) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection c = null;
        try {
            c = conn();
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, a.getPatientId());
                ps.setInt(2, a.getDoctorId());
                ps.setDate(3, Date.valueOf(a.getDate()));
                ps.setTime(4, Time.valueOf(a.getTime()));
                ps.setTime(5, a.getEndTime() != null
                        ? Time.valueOf(a.getEndTime())
                        : Time.valueOf(a.getTime().plusHours(1)));
                ps.setString(6, displayToDb(a.getStatus()));
                ps.setString(7, a.getSpecialty());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) a.setId(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm lịch hẹn", e);
            throw new DataAccessException("Lỗi thêm lịch hẹn", e);
        } finally {
            closeIfOwned(c);
        }
        return false;
    }

    @Override
    public boolean update(Appointment a) {
        String sql = "UPDATE Appointment SET patient_id=?, doctor_id=?, appointment_date=?, "
                   + "start_time=?, end_time=?, status=?, reason=? WHERE appointment_id=?";
        Connection c = null;
        try {
            c = conn();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, a.getPatientId());
                ps.setInt(2, a.getDoctorId());
                ps.setDate(3, Date.valueOf(a.getDate()));
                ps.setTime(4, Time.valueOf(a.getTime()));
                ps.setTime(5, a.getEndTime() != null
                        ? Time.valueOf(a.getEndTime())
                        : Time.valueOf(a.getTime().plusHours(1)));
                ps.setString(6, displayToDb(a.getStatus()));
                ps.setString(7, a.getSpecialty());
                ps.setInt(8, a.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật lịch hẹn ID=" + a.getId(), e);
            throw new DataAccessException("Lỗi cập nhật lịch hẹn ID=" + a.getId(), e);
        } finally {
            closeIfOwned(c);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE Appointment SET is_active = false WHERE appointment_id = ?";
        Connection c = null;
        try {
            c = conn();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa lịch hẹn ID=" + id, e);
            throw new DataAccessException("Lỗi xóa lịch hẹn ID=" + id, e);
        } finally {
            closeIfOwned(c);
        }
    }

    public List<String> findDistinctDoctorNames() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT DISTINCT u.full_name "
                   + "FROM Appointment a "
                   + "JOIN Doctor d ON a.doctor_id = d.doctor_id "
                   + "JOIN `User` u ON d.user_id = u.user_id "
                   + "WHERE a.is_active = true ORDER BY u.full_name";
        Connection c = null;
        try {
            c = conn();
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) names.add(rs.getString(1));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách bác sĩ", e);
        } finally {
            closeIfOwned(c);
        }
        return names;
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        int id = rs.getInt("appointment_id");
        a.setId(id);
        a.setAppointmentCode(String.format("LH%03d", id));
        a.setPatientId(rs.getInt("patient_id"));
        a.setDoctorId(rs.getInt("doctor_id"));
        a.setPatientName(rs.getString("patient_name"));
        String phone = rs.getString("patient_phone");
        a.setPatientPhone(phone != null ? phone : "");
        a.setDoctorName(rs.getString("doctor_name"));
        String reason = rs.getString("reason");
        a.setSpecialty(reason != null ? reason : "");
        a.setDate(rs.getDate("appointment_date").toLocalDate());
        a.setTime(rs.getTime("start_time").toLocalTime());
        a.setEndTime(rs.getTime("end_time").toLocalTime());
        a.setStatus(dbToDisplay(rs.getString("status")));
        a.setNote("");
        return a;
    }
}
