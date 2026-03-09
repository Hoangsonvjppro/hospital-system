package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Schedule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO implements BaseDAO<Schedule> {

    private Connection externalConnection;

    public ScheduleDAO() {}
    public ScheduleDAO(Connection connection) { this.externalConnection = connection; }

    private Connection getConnection() throws SQLException {
        return externalConnection != null ? externalConnection : DatabaseConfig.getInstance().getConnection();
    }

    private void closeIfOwned(Connection conn) {
        if (externalConnection == null && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public Schedule findById(int id) {
        String sql = "SELECT * FROM Schedule WHERE schedule_id = ?";
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
            throw new DataAccessException("Lỗi truy vấn lịch làm việc ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<Schedule> findAll() {
        List<Schedule> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM Schedule ORDER BY work_date, start_time")) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn danh sách lịch làm việc", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    public List<Schedule> findByDoctorAndDateRange(long doctorId, java.time.LocalDate from, java.time.LocalDate to) {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT * FROM Schedule WHERE doctor_id = ? AND work_date BETWEEN ? AND ? ORDER BY work_date, start_time";
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
            throw new DataAccessException("Lỗi truy vấn lịch làm việc theo bác sĩ", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(Schedule entity) {
        String sql = "INSERT INTO Schedule (doctor_id, work_date, start_time, end_time, notes) VALUES (?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, entity.getDoctorId());
                ps.setDate(2, Date.valueOf(entity.getWorkDate()));
                ps.setTime(3, Time.valueOf(entity.getStartTime()));
                ps.setTime(4, Time.valueOf(entity.getEndTime()));
                ps.setString(5, entity.getNotes());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) entity.setId(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm lịch làm việc", e);
        } finally {
            closeIfOwned(conn);
        }
        return false;
    }

    @Override
    public boolean update(Schedule entity) {
        String sql = "UPDATE Schedule SET doctor_id=?, work_date=?, start_time=?, end_time=?, notes=? WHERE schedule_id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, entity.getDoctorId());
                ps.setDate(2, Date.valueOf(entity.getWorkDate()));
                ps.setTime(3, Time.valueOf(entity.getStartTime()));
                ps.setTime(4, Time.valueOf(entity.getEndTime()));
                ps.setString(5, entity.getNotes());
                ps.setInt(6, entity.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật lịch làm việc ID=" + entity.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Schedule WHERE schedule_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể xóa lịch làm việc ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    private Schedule mapResultSet(ResultSet rs) throws SQLException {
        Schedule s = new Schedule();
        s.setId(rs.getInt("schedule_id"));
        s.setDoctorId(rs.getLong("doctor_id"));
        s.setWorkDate(rs.getDate("work_date").toLocalDate());
        s.setStartTime(rs.getTime("start_time").toLocalTime());
        s.setEndTime(rs.getTime("end_time").toLocalTime());
        s.setNotes(rs.getString("notes"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) s.setCreatedAt(createdAt.toLocalDateTime());
        return s;
    }
}
