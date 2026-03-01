package com.hospital.dao;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionDetail;

import java.sql.*;

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
            INSERT INTO Prescription (medical_record_id, created_at, status)
            VALUES (?, NOW(), ?)
        """;

        Connection conn = null;

        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setLong(1, p.getMedicalRecordId());
                ps.setString(2, p.getStatus());

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
            (prescription_id, medicine_id, quantity, dosage)
            VALUES (?, ?, ?, ?)
        """;

        Connection conn = null;

        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setLong(1, d.getPrescriptionId());
                ps.setInt(2, d.getMedicineId());
                ps.setInt(3, d.getQuantity());
                ps.setString(4, d.getDosage());

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm chi tiết đơn thuốc", e);
        } finally {
            closeIfOwned(conn);
        }
    }
}
