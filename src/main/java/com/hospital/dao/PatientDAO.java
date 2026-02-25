package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Patient;

import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DAO benh nhan — truy van bang Patient trong CSDL.
 * Bo sung quan ly hang doi kham (in-memory) phuc vu DoctorWorkstationPanel.
 */
public class PatientDAO implements BaseDAO<Patient> {

    // ── In-memory queue tracking ──────────────────────────────
    // Key: patient_id, Value: [status, examType, arrivalTime]
    private static final Map<Integer, String[]> queueMap = new ConcurrentHashMap<>();

    private Connection externalConnection;

    public PatientDAO() {
        // Mode 1: Tự lấy connection (cho thao tác đơn lẻ)
    }

    public PatientDAO(Connection connection) {
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

    // ── CRUD co ban (tu main) ─────────────────────────────────

    @Override
    public Patient findById(int id) {
        String sql = "SELECT * FROM Patient WHERE patient_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Patient p = mapResultSet(rs);
                        applyQueueInfo(p);
                        return p;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn bệnh nhân ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    @Override
    public List<Patient> findAll() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM Patient WHERE is_active = true";
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stm = conn.createStatement();
                 ResultSet rs = stm.executeQuery(sql)) {
                while (rs.next()) {
                    Patient p = mapResultSet(rs);
                    applyQueueInfo(p);
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn danh sách bệnh nhân", e);
        } finally {
            closeIfOwned(conn);
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
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys.next()) {
                        entity.setId(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm bệnh nhân", e);
        } finally {
            closeIfOwned(conn);
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
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật bệnh nhân ID=" + entity.getId(), e);
        } finally {
            closeIfOwned(conn);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE Patient SET is_active=false WHERE patient_id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể xóa bệnh nhân ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    // ── Hang doi kham benh (doctor workflow) ──────────────────

    /**
     * Dua benh nhan vao hang doi cho kham.
     * @param patientId  ID benh nhan trong DB
     * @param examType   Loai kham (vd: "Kham tong quat")
     */
    public void addToQueue(int patientId, String examType) {
        String arrival = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        queueMap.put(patientId, new String[]{"CHO KHAM", examType, arrival});
    }

    /**
     * Tim benh nhan dang cho hoac dang kham.
     */
    public List<Patient> findWaiting() {
        return findByStatus("CHO KHAM", "DANG KHAM");
    }

    /**
     * Tim benh nhan theo trang thai hang doi.
     */
    public List<Patient> findByStatus(String... statuses) {
        Set<String> statusSet = Set.of(statuses);
        List<Patient> result = new ArrayList<>();

        for (Map.Entry<Integer, String[]> entry : queueMap.entrySet()) {
            if (statusSet.contains(entry.getValue()[0])) {
                Patient p = findById(entry.getKey());
                if (p != null) {
                    result.add(p);
                }
            }
        }

        return result;
    }

    /**
     * Cap nhat trang thai hang doi cua benh nhan.
     */
    public boolean updateStatus(int patientId, String newStatus) {
        String[] info = queueMap.get(patientId);
        if (info != null) {
            info[0] = newStatus;
            return true;
        }
        return false;
    }

    /**
     * Dem so benh nhan trong hang doi hom nay.
     */
    public int countToday() {
        return queueMap.size();
    }

    /**
     * Xoa hang doi (reset moi ngay).
     */
    public void clearQueue() {
        queueMap.clear();
    }

    // -- Helper --

    /**
     * Gan thong tin hang doi (status, examType, arrivalTime) vao Patient object.
     */
    private void applyQueueInfo(Patient p) {
        String[] info = queueMap.get(p.getId());
        if (info != null) {
            p.setStatus(info[0]);
            p.setExamType(info[1]);
            p.setArrivalTime(info[2]);
        }
        // Tao patientCode tu ID
        p.setPatientCode(String.format("BN%03d", p.getId()));
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
