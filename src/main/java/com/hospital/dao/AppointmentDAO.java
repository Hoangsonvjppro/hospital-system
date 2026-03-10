package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Appointment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO lịch hẹn — truy vấn bảng Appointment JOIN Patient, Doctor, User, Specialty.
 */
public class AppointmentDAO implements BaseDAO<Appointment> {

    private static final Logger LOGGER = Logger.getLogger(AppointmentDAO.class.getName());

    private static final String SELECT_JOIN = """
            SELECT a.*,
                   p.full_name AS patient_name, p.phone AS patient_phone,
                   u.full_name AS doctor_name,
                   s.specialty_name
              FROM Appointment a
              JOIN Patient p  ON a.patient_id = p.patient_id
              JOIN Doctor  d  ON a.doctor_id  = d.doctor_id
              JOIN `User`  u  ON d.user_id    = u.user_id
              LEFT JOIN Specialty s ON d.specialty_id = s.specialty_id
            """;

    private Connection externalConnection;

    public AppointmentDAO() {}

    public AppointmentDAO(Connection connection) {
        this.externalConnection = connection;
    }

    private Connection getConnection() throws SQLException {
        if (externalConnection != null) return externalConnection;
        return DatabaseConfig.getInstance().getConnection();
    }

    private void closeIfOwned(Connection conn) {
        if (externalConnection == null && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public Appointment findById(int id) {
        String sql = SELECT_JOIN + " WHERE a.appointment_id = ?";
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
            throw new DataAccessException("Lỗi truy vấn lịch hẹn ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<Appointment> findAll() {
        List<Appointment> list = new ArrayList<>();
        String sql = SELECT_JOIN + " ORDER BY a.appointment_date DESC, a.start_time";
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn danh sách lịch hẹn", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    /**
     * Lịch hẹn theo bác sĩ trong khoảng ngày.
     */
    public List<Appointment> findByDoctorAndDateRange(long doctorId, java.time.LocalDate from, java.time.LocalDate to) {
        List<Appointment> list = new ArrayList<>();
        String sql = SELECT_JOIN + " WHERE a.doctor_id = ? AND a.appointment_date BETWEEN ? AND ? ORDER BY a.appointment_date, a.start_time";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, doctorId);
                ps.setDate(2, Date.valueOf(from));
                ps.setDate(3, Date.valueOf(to));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn lịch hẹn theo bác sĩ", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(Appointment a) {
        String sql = """
                INSERT INTO Appointment (patient_id, doctor_id, appointment_date, start_time, end_time, status, reason, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, a.getPatientId());
                ps.setLong(2, a.getDoctorId());
                ps.setDate(3, Date.valueOf(a.getAppointmentDate()));
                ps.setTime(4, Time.valueOf(a.getStartTime()));
                ps.setTime(5, Time.valueOf(a.getEndTime()));
                ps.setString(6, a.getStatus() != null ? a.getStatus() : "SCHEDULED");
                ps.setString(7, a.getReason());
                if (a.getCreatedBy() != null) ps.setLong(8, a.getCreatedBy()); else ps.setNull(8, Types.BIGINT);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) a.setId(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm lịch hẹn", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    @Override
    public boolean update(Appointment a) {
        String sql = """
                UPDATE Appointment SET patient_id=?, doctor_id=?, appointment_date=?,
                       start_time=?, end_time=?, status=?, reason=?
                WHERE appointment_id=?
                """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, a.getPatientId());
                ps.setLong(2, a.getDoctorId());
                ps.setDate(3, Date.valueOf(a.getAppointmentDate()));
                ps.setTime(4, Time.valueOf(a.getStartTime()));
                ps.setTime(5, Time.valueOf(a.getEndTime()));
                ps.setString(6, a.getStatus());
                ps.setString(7, a.getReason());
                ps.setInt(8, a.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật lịch hẹn ID=" + a.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Appointment WHERE appointment_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể xóa lịch hẹn ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    private Appointment mapResultSet(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("appointment_id"));
        a.setPatientId(rs.getLong("patient_id"));
        a.setDoctorId(rs.getLong("doctor_id"));
        a.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
        a.setStartTime(rs.getTime("start_time").toLocalTime());
        a.setEndTime(rs.getTime("end_time").toLocalTime());
        a.setStatus(rs.getString("status"));
        a.setReason(rs.getString("reason"));
        a.setCreatedBy(rs.getObject("created_by") == null ? null : rs.getLong("created_by"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) a.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) a.setUpdatedAt(updatedAt.toLocalDateTime());

        // Transient JOIN fields
        try { a.setPatientName(rs.getString("patient_name")); } catch (SQLException ignored) {}
        try { a.setPatientPhone(rs.getString("patient_phone")); } catch (SQLException ignored) {}
        try { a.setDoctorName(rs.getString("doctor_name")); } catch (SQLException ignored) {}
        try { a.setSpecialtyName(rs.getString("specialty_name")); } catch (SQLException ignored) {}

        return a;
    }

    /**
     * Lấy danh sách lịch hẹn theo trạng thái (SQL-level filter).
     */
    public List<Appointment> findByStatus(String status) {
        String sql = SELECT_JOIN + " WHERE a.status = ? ORDER BY a.appointment_date, a.start_time";
        List<Appointment> result = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(mapResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy lịch hẹn theo trạng thái=" + status, e);
            throw new DataAccessException("Lỗi lấy lịch hẹn theo trạng thái", e);
        } finally {
            closeIfOwned(conn);
        }
        return result;
    }
}
