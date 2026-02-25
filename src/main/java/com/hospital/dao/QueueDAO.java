package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO hàng đợi khám bệnh — query trực tiếp từ DB (MedicalRecord JOIN Patient).
 * Thay thế hoàn toàn ConcurrentHashMap trong PatientDAO cũ.
 */
public class QueueDAO {

    private static final Logger LOGGER = Logger.getLogger(QueueDAO.class.getName());

    private Connection externalConnection;

    public QueueDAO() {
        // Mode 1: Tự lấy connection (cho thao tác đơn lẻ)
    }

    public QueueDAO(Connection connection) {
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

    // ── Lấy danh sách bệnh nhân theo trạng thái hàng đợi (ngày hôm nay) ───
    /**
     * Lấy danh sách bệnh nhân đang chờ/đang khám NGÀY HÔM NAY.
     * JOIN MedicalRecord + Patient → 1 query duy nhất, fix N+1.
     *
     * @param statuses Các trạng thái cần lọc (WAITING, EXAMINING, COMPLETED, TRANSFERRED)
     * @return Danh sách Patient với thông tin workflow (status, examType, arrivalTime, currentRecordId)
     */
    public List<Patient> findByQueueStatus(String... statuses) {
        if (statuses == null || statuses.length == 0) {
            return new ArrayList<>();
        }

        String placeholders = String.join(",",
                Collections.nCopies(statuses.length, "?"));
        String sql = """
            SELECT p.patient_id, p.full_name, p.gender, p.date_of_birth,
                   p.phone, p.address, p.user_id, p.is_active,
                   p.created_at, p.updated_at,
                   mr.queue_status, mr.exam_type, mr.arrival_time, mr.record_id
            FROM MedicalRecord mr
            JOIN Patient p ON mr.patient_id = p.patient_id
            WHERE mr.queue_status IN (%s)
              AND DATE(mr.visit_date) = CURDATE()
            ORDER BY mr.arrival_time ASC
        """.formatted(placeholders);

        List<Patient> result = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < statuses.length; i++) {
                    ps.setString(i + 1, statuses[i]);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(mapPatientWithQueue(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn hàng đợi khám bệnh", e);
            throw new DataAccessException("Lỗi truy vấn hàng đợi khám bệnh", e);
        } finally {
            closeIfOwned(conn);
        }
        return result;
    }

    // ── Đưa bệnh nhân vào hàng đợi = Insert MedicalRecord with queue_status ─
    /**
     * Đưa bệnh nhân vào hàng đợi khám.
     * INSERT MedicalRecord với queue_status = 'WAITING'.
     *
     * @param patientId ID bệnh nhân
     * @param doctorId  ID bác sĩ (có thể là bác sĩ mặc định)
     * @param examType  Loại khám (vd: "Khám tổng quát")
     * @return record_id của MedicalRecord vừa tạo
     */
    public long enqueue(long patientId, long doctorId, String examType) {
        String sql = """
            INSERT INTO MedicalRecord
                (patient_id, doctor_id, visit_date, queue_status, arrival_time, exam_type)
            VALUES (?, ?, NOW(), 'WAITING', CURTIME(), ?)
        """;

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, patientId);
                ps.setLong(2, doctorId);
                ps.setString(3, examType);

                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể đưa bệnh nhân vào hàng đợi, patientId=" + patientId, e);
            throw new DataAccessException("Không thể đưa bệnh nhân vào hàng đợi", e);
        } finally {
            closeIfOwned(conn);
        }
        throw new DataAccessException("Không thể tạo record hàng đợi", null);
    }

    // ── Cập nhật trạng thái hàng đợi ────────────────────────────────────────
    /**
     * Cập nhật trạng thái hàng đợi của một bệnh án.
     *
     * @param recordId  ID bệnh án (MedicalRecord)
     * @param newStatus Trạng thái mới (WAITING, EXAMINING, COMPLETED, TRANSFERRED)
     * @return true nếu cập nhật thành công
     */
    public boolean updateQueueStatus(long recordId, String newStatus) {
        String sql = """
            UPDATE MedicalRecord
               SET queue_status = ?,
                   updated_at   = NOW()
             WHERE record_id    = ?
        """;

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, newStatus);
                ps.setLong(2, recordId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật trạng thái hàng đợi, recordId=" + recordId, e);
            throw new DataAccessException("Không thể cập nhật trạng thái hàng đợi", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    // ── Đếm số bệnh nhân trong hàng đợi hôm nay ────────────────────────────
    /**
     * Đếm số bệnh nhân trong hàng đợi ngày hôm nay (tất cả trạng thái).
     */
    public int countToday() {
        String sql = """
            SELECT COUNT(*) FROM MedicalRecord
            WHERE DATE(visit_date) = CURDATE()
              AND queue_status IS NOT NULL
        """;

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm hàng đợi hôm nay", e);
            throw new DataAccessException("Lỗi đếm hàng đợi hôm nay", e);
        } finally {
            closeIfOwned(conn);
        }
        return 0;
    }

    // ── Đếm theo trạng thái cụ thể ─────────────────────────────────────────
    /**
     * Đếm bệnh nhân theo trạng thái hàng đợi (ngày hôm nay).
     *
     * @param status Trạng thái cần đếm (WAITING, EXAMINING, COMPLETED, TRANSFERRED)
     */
    public int countByStatus(String status) {
        String sql = """
            SELECT COUNT(*) FROM MedicalRecord
            WHERE DATE(visit_date) = CURDATE()
              AND queue_status = ?
        """;

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm hàng đợi theo trạng thái: " + status, e);
            throw new DataAccessException("Lỗi đếm hàng đợi theo trạng thái", e);
        } finally {
            closeIfOwned(conn);
        }
        return 0;
    }

    // ── Helper: Map ResultSet → Patient (với thông tin queue) ────────────────
    private Patient mapPatientWithQueue(ResultSet rs) throws SQLException {
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

        // Queue workflow fields
        p.setStatus(rs.getString("queue_status"));
        p.setExamType(rs.getString("exam_type"));

        Time arrivalTime = rs.getTime("arrival_time");
        if (arrivalTime != null) {
            p.setArrivalTime(arrivalTime.toLocalTime()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        }

        p.setCurrentRecordId(rs.getLong("record_id"));

        return p;
    }
}
