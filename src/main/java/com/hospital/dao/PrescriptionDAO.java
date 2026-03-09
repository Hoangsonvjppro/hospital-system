package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionDetail;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {

    private Connection externalConnection;

    public PrescriptionDAO() {}

    public PrescriptionDAO(Connection connection) {
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

    public long insertPrescription(Prescription p) {
        String sql = """
            INSERT INTO Prescription (record_id, created_at, status, total_amount)
            VALUES (?, NOW(), ?, ?)
        """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, p.getMedicalRecordId());
                ps.setString(2, p.getStatus());
                ps.setDouble(3, p.getTotalAmount());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể tạo đơn thuốc", e);
        } finally {
            closeIfOwned(conn);
        }
        return -1;
    }

    public boolean insertDetail(PrescriptionDetail d) {
        String sql = """
            INSERT INTO PrescriptionDetail
            (prescription_id, medicine_id, quantity, dosage, instruction, unit_price)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, d.getPrescriptionId());
                ps.setLong(2, d.getMedicineId());
                ps.setInt(3, d.getQuantity());
                ps.setString(4, d.getDosage());
                ps.setString(5, d.getInstruction());
                ps.setDouble(6, d.getUnitPrice());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm chi tiết đơn thuốc", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    /**
     * Lấy danh sách chi tiết đơn thuốc theo prescriptionId
     */
    public List<PrescriptionDetail> findDetailsByPrescriptionId(long prescriptionId) {
        List<PrescriptionDetail> result = new ArrayList<>();
        String sql = """
            SELECT pd.*, m.medicine_name, m.unit
            FROM PrescriptionDetail pd
            JOIN Medicine m ON pd.medicine_id = m.medicine_id
            WHERE pd.prescription_id = ?
            """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, prescriptionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        PrescriptionDetail d = new PrescriptionDetail();
                        d.setId(rs.getLong("detail_id"));
                        d.setPrescriptionId(rs.getLong("prescription_id"));
                        d.setMedicineId(rs.getLong("medicine_id"));
                        d.setQuantity(rs.getInt("quantity"));
                        d.setDosage(rs.getString("dosage"));
                        try { d.setInstruction(rs.getString("instruction")); } catch (SQLException ignored) {}
                        try { d.setUnitPrice(rs.getDouble("unit_price")); } catch (SQLException ignored) {}
                        try { d.setMedicineName(rs.getString("medicine_name")); } catch (SQLException ignored) {}
                        result.add(d);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể lấy chi tiết đơn thuốc", e);
        } finally {
            closeIfOwned(conn);
        }
        return result;
    }

    /**
     * Lấy danh sách đơn thuốc theo medical_record_id
     */
    public List<Prescription> findByMedicalRecordId(long medicalRecordId) {
        List<Prescription> result = new ArrayList<>();
        String sql = "SELECT * FROM Prescription WHERE record_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, medicalRecordId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(mapPrescription(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể lấy danh sách đơn thuốc", e);
        } finally {
            closeIfOwned(conn);
        }
        return result;
    }

    /**
     * Lấy danh sách đơn thuốc chờ phát (status = CONFIRMED)
     */
    public List<Prescription> findPendingPrescriptions() {
        List<Prescription> result = new ArrayList<>();
        String sql = "SELECT * FROM Prescription WHERE status = 'CONFIRMED' ORDER BY created_at ASC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(mapPrescription(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể lấy danh sách đơn thuốc chờ phát", e);
        } finally {
            closeIfOwned(conn);
        }
        return result;
    }

    /**
     * Lấy Prescription theo ID.
     */
    public Prescription findById(long prescriptionId) {
        String sql = "SELECT * FROM Prescription WHERE prescription_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, prescriptionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapPrescription(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể lấy đơn thuốc ID=" + prescriptionId, e);
        } finally {
            closeIfOwned(conn);
        }
        return null;
    }

    /**
     * Cập nhật trạng thái đơn thuốc.
     */
    public boolean updateStatus(long prescriptionId, String newStatus) {
        String sql = "UPDATE Prescription SET status = ?, updated_at = NOW() WHERE prescription_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, newStatus);
                ps.setLong(2, prescriptionId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật trạng thái đơn thuốc", e);
        } finally {
            closeIfOwned(conn);
        }
    }

    private Prescription mapPrescription(ResultSet rs) throws SQLException {
        Prescription p = new Prescription();
        p.setId(rs.getInt("prescription_id"));
        p.setMedicalRecordId(rs.getLong("record_id"));
        p.setStatus(rs.getString("status"));
        try { p.setTotalAmount(rs.getDouble("total_amount")); } catch (SQLException ignored) {}
        if (rs.getTimestamp("created_at") != null) {
            p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return p;
    }

    /**
     * Lấy danh sách đơn thuốc theo patient_id (lịch sử đơn thuốc).
     */
    public List<Prescription> findByPatientId(long patientId) {
        List<Prescription> result = new ArrayList<>();
        String sql = """
            SELECT p.* FROM Prescription p
            JOIN MedicalRecord mr ON p.record_id = mr.record_id
            WHERE mr.patient_id = ?
            ORDER BY p.created_at DESC
        """;
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(mapPrescription(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể lấy lịch sử đơn thuốc theo bệnh nhân", e);
        } finally {
            closeIfOwned(conn);
        }
        return result;
    }

    /**
     * Tạo đơn thuốc + danh sách chi tiết trong TRANSACTION.
     * Nếu insert detail thất bại → rollback tất cả.
     */
    public long createPrescriptionWithItems(Prescription p, List<PrescriptionDetail> items) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Insert Prescription
            PrescriptionDAO txDao = new PrescriptionDAO(conn);
            long prescriptionId = txDao.insertPrescription(p);
            if (prescriptionId <= 0) {
                throw new DataAccessException("Không thể tạo đơn thuốc", new Exception("Failed to insert prescription"));
            }

            // 2. Insert từng PrescriptionDetail
            for (PrescriptionDetail d : items) {
                d.setPrescriptionId(prescriptionId);
                if (!txDao.insertDetail(d)) {
                    throw new DataAccessException("Không thể thêm chi tiết đơn thuốc cho thuốc: " + d.getMedicineName(), new Exception("Failed to insert detail"));
                }
            }

            conn.commit();
            return prescriptionId;

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw new DataAccessException("Lỗi tạo đơn thuốc (transaction)", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }
}
