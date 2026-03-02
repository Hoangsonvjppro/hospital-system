package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO benh nhan — truy van bang Patient trong CSDL.
 */
public class PatientDAO implements BaseDAO<Patient> {

    private static final Logger LOGGER = Logger.getLogger(PatientDAO.class.getName());

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
                        return mapResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn bệnh nhân ID=" + id, e);
            throw new DataAccessException("Lỗi truy vấn bệnh nhân ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    /**
     * Tìm bệnh nhân theo số điện thoại (chính xác).
     */
    public Patient findByPhone(String phone) {
        String sql = "SELECT * FROM Patient WHERE phone = ? AND is_active = true";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, phone);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn bệnh nhân theo SĐT=" + phone, e);
            throw new DataAccessException("Lỗi truy vấn bệnh nhân theo SĐT=" + phone, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    /**
     * Tìm bệnh nhân theo CCCD / id_card (nếu cột tồn tại). Nếu cột không tồn tại, trả về null.
     */
    public Patient findByCccd(String cccd) {
        // Try multiple possible column names for CCCD/id card to be robust across schemas
        String[] candidates = new String[]{"id_card", "idcard", "cccd", "identity_card", "identity_number", "id_number", "citizen_id"};
        Connection conn = null;
        try {
            conn = getConnection();
            LOGGER.log(Level.FINE, "findByCccd: trying candidates for CCCD lookup");
            for (String col : candidates) {
                boolean has = false;
                try { has = hasColumn(conn, "Patient", col); } catch (Exception exMeta) { has = false; }
                LOGGER.log(Level.FINE, "findByCccd: candidate='" + col + "' hasColumn=" + has);
                // quick metadata check first
                if (!has) continue;
                String sql = "SELECT * FROM Patient WHERE " + col + " = ? AND is_active = true";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, cccd);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return mapResultSet(rs);
                        }
                    }
                } catch (SQLException inner) {
                    // column might not actually exist / driver different — try next candidate
                    LOGGER.log(Level.FINER, "Tried Patient." + col + " but query failed, try next candidate.", inner);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn bệnh nhân theo CCCD=" + cccd, e);
            throw new DataAccessException("Lỗi truy vấn bệnh nhân theo CCCD=" + cccd, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    /**
     * Tìm bệnh nhân theo tên (like) để hỗ trợ tìm tái khám.
     */
    public List<Patient> searchByName(String name) {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM Patient WHERE full_name LIKE ? AND is_active = true";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "%" + name + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm bệnh nhân theo tên=" + name, e);
            throw new DataAccessException("Lỗi tìm bệnh nhân theo tên=" + name, e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public List<Patient> findAll() {
        List<Patient> list = new ArrayList<>();
        // Return all patients from DB. Previously we filtered by is_active=true which hid soft-deleted
        // records; change to show all patients as requested.
        String sql = "SELECT * FROM Patient";
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stm = conn.createStatement();
                 ResultSet rs = stm.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách bệnh nhân", e);
            throw new DataAccessException("Lỗi truy vấn danh sách bệnh nhân", e);
        } finally {
            closeIfOwned(conn);
        }
        return list;
    }

    @Override
    public boolean insert(Patient entity) {
        // Server-side defensive duplicate checks before attempting INSERT.
        try {
            // Check CCCD if present and column exists
            if (entity.getCccd() != null && !entity.getCccd().isEmpty()) {
                Patient existing = findByCccd(entity.getCccd());
                if (existing != null) {
                    throw new DataAccessException("CCCD đã tồn tại cho bệnh nhân khác", null);
                }
            }
            // Check phone
            if (entity.getPhone() != null && !entity.getPhone().isEmpty()) {
                Patient byPhone = findByPhone(entity.getPhone());
                if (byPhone != null) {
                    throw new DataAccessException("SĐT đã tồn tại cho bệnh nhân khác", null);
                }
            }
        } catch (DataAccessException dae) {
            // Rethrow our duplicate detection as-is
            throw dae;
        } catch (Exception ex) {
            // If duplicate check can't complete (metadata/driver issue), continue to attempt insert and rely on DB constraints
        }

        String sql = """
                INSERT INTO Patient
                (full_name, gender, date_of_birth, phone, address, id_card, allergy_note, is_active)
                VALUES (?,?,?,?,?,?,?,?)
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
                // CCCD (id_card)
                if (entity.getCccd() != null) ps.setString(6, entity.getCccd()); else ps.setNull(6, Types.VARCHAR);
                // allergy note
                if (entity.getAllergyHistory() != null) ps.setString(7, entity.getAllergyHistory()); else ps.setNull(7, Types.VARCHAR);
                ps.setBoolean(8, entity.isActive());

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
            LOGGER.log(Level.SEVERE, "Không thể thêm bệnh nhân", e);
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
                    id_card=?,
                    allergy_note=?,
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
                // id_card
                if (entity.getCccd() != null) ps.setString(6, entity.getCccd()); else ps.setNull(6, Types.VARCHAR);
                // allergy_note
                if (entity.getAllergyHistory() != null) ps.setString(7, entity.getAllergyHistory()); else ps.setNull(7, Types.VARCHAR);
                ps.setBoolean(8, entity.isActive());
                ps.setInt(9, entity.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật bệnh nhân ID=" + entity.getId(), e);
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
            LOGGER.log(Level.SEVERE, "Không thể xóa bệnh nhân ID=" + id, e);
            throw new DataAccessException("Không thể xóa bệnh nhân ID=" + id, e);
        } finally {
            closeIfOwned(conn);
        }
    }

    // -- Helper --

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

        // Optional columns: cccd, allergy_history, notes — không bắt buộc tồn tại trong mọi schema
        try {
            rs.findColumn("id_card");
            p.setCccd(rs.getString("id_card"));
        } catch (SQLException ignored) {
        }

        try {
            // prefer allergy_note (init SQL uses this), fallback to allergy_history
            try {
                rs.findColumn("allergy_note");
                p.setAllergyHistory(rs.getString("allergy_note"));
            } catch (SQLException ex) {
                rs.findColumn("allergy_history");
                p.setAllergyHistory(rs.getString("allergy_history"));
            }
        } catch (SQLException ignored) {
        }

        try {
            rs.findColumn("notes");
            p.setNotes(rs.getString("notes"));
        } catch (SQLException ignored) {
        }

        return p;
    }

    /**
     * Kiểm tra xem bảng có cột cụ thể hay không.
     */
    private boolean hasColumn(Connection conn, String tableName, String columnName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            // Some drivers/databases are case-sensitive or expect catalog/schema parameters.
            // We'll iterate all columns for the table and compare names case-insensitively.
            String catalog = conn.getCatalog();
            try (ResultSet rs = meta.getColumns(catalog, null, tableName, "%")) {
                while (rs.next()) {
                    String col = rs.getString("COLUMN_NAME");
                    if (col != null && col.equalsIgnoreCase(columnName)) {
                        return true;
                    }
                }
            }
            // Try with upper-cased table name (some DBs store upper-case metadata)
            try (ResultSet rs2 = meta.getColumns(catalog, null, tableName.toUpperCase(), "%")) {
                while (rs2.next()) {
                    String col = rs2.getString("COLUMN_NAME");
                    if (col != null && col.equalsIgnoreCase(columnName)) {
                        return true;
                    }
                }
            }
            try (ResultSet rs3 = meta.getColumns(catalog, null, tableName.toLowerCase(), "%")) {
                while (rs3.next()) {
                    String col = rs3.getString("COLUMN_NAME");
                    if (col != null && col.equalsIgnoreCase(columnName)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Không thể kiểm tra cột " + columnName + " trong bảng " + tableName, e);
            return false;
        }
    }
}
