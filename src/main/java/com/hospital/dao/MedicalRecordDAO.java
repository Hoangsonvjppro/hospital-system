package com.hospital.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.hospital.config.DatabaseConfig;
import com.hospital.model.MedicalRecord;

public class MedicalRecordDAO {

    // ================= FIND WAITING =================
    public List<MedicalRecord> findWaitingRecords() {

        List<MedicalRecord> list = new ArrayList<>();

        String sql = "SELECT * FROM medical_record WHERE status = 'WAITING' ORDER BY id ASC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi lấy danh sách hồ sơ chờ khám", e);
        }

        return list;
    }

    // ================= UPDATE FULL EXAMINATION =================
    public boolean updateExamination(MedicalRecord record) {

        String sql = """
            UPDATE medical_record
            SET symptoms = ?,
                diagnosis = ?,
                diagnosis_code = ?,
                blood_pressure = ?,
                temperature = ?,
                pulse = ?,
                weight = ?,
                height = ?,
                doctor_note = ?,
                follow_up_date = ?,
                status = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, record.getSymptoms());
            ps.setString(2, record.getDiagnosis());
            ps.setString(3, record.getDiagnosisCode());
            ps.setString(4, record.getBloodPressure());
            ps.setDouble(5, record.getTemperature());
            ps.setInt(6, record.getPulse());
            ps.setDouble(7, record.getWeight());
            ps.setDouble(8, record.getHeight());
            ps.setString(9, record.getDoctorNote());

            if (record.getFollowUpDate() != null)
                ps.setDate(10, Date.valueOf(record.getFollowUpDate()));
            else
                ps.setNull(10, Types.DATE);

            ps.setString(11, record.getStatus());
            ps.setLong(12, record.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật khám bệnh", e);
        }
    }

    // ================= UPDATE VITAL SIGNS ONLY =================
    public boolean updateVitalSigns(long recordId,
                                    double temperature,
                                    String bloodPressure,
                                    int pulse,
                                    double weight,
                                    double height) {

        String sql = """
            UPDATE medical_record
            SET temperature = ?,
                blood_pressure = ?,
                pulse = ?,
                weight = ?,
                height = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, temperature);
            ps.setString(2, bloodPressure);
            ps.setInt(3, pulse);
            ps.setDouble(4, weight);
            ps.setDouble(5, height);
            ps.setLong(6, recordId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật sinh hiệu", e);
        }
    }

    // ================= UPDATE DIAGNOSIS + SYMPTOMS =================
    public boolean updateDiagnosisAndSymptoms(long recordId,
                                              String diagnosis,
                                              String symptoms) {

        String sql = """
            UPDATE medical_record
            SET diagnosis = ?,
                symptoms = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, diagnosis);
            ps.setString(2, symptoms);
            ps.setLong(3, recordId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật chẩn đoán", e);
        }
    }

    // ================= MAP RESULTSET =================
    private MedicalRecord map(ResultSet rs) {

        try {

            MedicalRecord r = new MedicalRecord();

            r.setId(rs.getLong("id"));
            r.setPatientId(rs.getLong("patient_id"));
            r.setDoctorId(rs.getLong("doctor_id"));
            r.setSymptoms(rs.getString("symptoms"));
            r.setDiagnosis(rs.getString("diagnosis"));
            r.setDiagnosisCode(rs.getString("diagnosis_code"));
            r.setBloodPressure(rs.getString("blood_pressure"));
            r.setTemperature(rs.getDouble("temperature"));
            r.setPulse(rs.getInt("pulse"));
            r.setWeight(rs.getDouble("weight"));
            r.setHeight(rs.getDouble("height"));
            r.setDoctorNote(rs.getString("doctor_note"));
            r.setStatus(rs.getString("status"));

            Date follow = rs.getDate("follow_up_date");
            if (follow != null)
                r.setFollowUpDate(follow.toLocalDate());

            return r;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi map MedicalRecord", e);
        }
    }
}
