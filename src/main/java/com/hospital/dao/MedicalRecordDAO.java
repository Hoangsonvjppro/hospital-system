package com.hospital.dao;

import java.sql.*;

public class MedicalRecordDAO {

    private Connection connection;

    public MedicalRecordDAO(Connection connection) {
        this.connection = connection;
    }

    // Tạo bệnh án trống và trả về record_id
    public long createEmptyRecord(long patientId, long doctorId, Long appointmentId) throws SQLException {

        String sql = """
            INSERT INTO MedicalRecord (
                patient_id,
                doctor_id,
                appointment_id,
                visit_date
            ) VALUES (?, ?, ?, NOW())
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, patientId);
            ps.setLong(2, doctorId);

            if (appointmentId != null) {
                ps.setLong(3, appointmentId);
            } else {
                ps.setNull(3, Types.BIGINT);
            }

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1); // record_id (số thứ tự)
            }
        }

        throw new SQLException("Không thể tạo Medical Record");
    }
}