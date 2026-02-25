package com.hospital.bus;

import com.hospital.dao.MedicalRecordDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;

import java.sql.Connection;
import java.util.List;

/**
 * Business logic layer cho bệnh án.
 * Tương thích cấu trúc của bạn (dùng Connection) + logic lấy danh sách chờ.
 */
public class MedicalRecordBUS {

    private MedicalRecordDAO dao;
    private PatientDAO patientDAO;

    public MedicalRecordBUS(Connection connection) {
        this.dao = new MedicalRecordDAO(connection);
        this.patientDAO = new PatientDAO();
    }

    // ── Tạo bệnh án mới ─────────────────────────────────────────────────────

    public long createMedicalRecord(long patientId, long doctorId, Long appointmentId) throws Exception {

        if (patientId <= 0) {
            throw new Exception("Patient ID không hợp lệ");
        }

        if (doctorId <= 0) {
            throw new Exception("Doctor ID không hợp lệ");
        }

        return dao.createEmptyRecord(patientId, doctorId, appointmentId);
    }

    // ── Cập nhật chẩn đoán ───────────────────────────────────────────────────

    public boolean updateDiagnosis(long recordId, String diagnosis) throws Exception {
        if (recordId <= 0) {
            throw new Exception("Record ID không hợp lệ");
        }
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            throw new Exception("Chẩn đoán không được để trống");
        }
        return dao.updateDiagnosis(recordId, diagnosis.trim());
    }

    // ── Cập nhật triệu chứng ────────────────────────────────────────────────

    public boolean updateSymptoms(long recordId, String symptoms) throws Exception {
        if (recordId <= 0) {
            throw new Exception("Record ID không hợp lệ");
        }
        if (symptoms == null || symptoms.trim().isEmpty()) {
            throw new Exception("Triệu chứng không được để trống");
        }
        return dao.updateSymptoms(recordId, symptoms.trim());
    }

    // ── Cập nhật chẩn đoán + triệu chứng cùng lúc ─────────────────────────

    public boolean updateDiagnosisAndSymptoms(long recordId, String diagnosis, String symptoms) throws Exception {
        if (recordId <= 0) {
            throw new Exception("Record ID không hợp lệ");
        }
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            throw new Exception("Chẩn đoán không được để trống");
        }
        if (symptoms == null || symptoms.trim().isEmpty()) {
            throw new Exception("Triệu chứng không được để trống");
        }
        return dao.updateDiagnosisAndSymptoms(recordId, diagnosis.trim(), symptoms.trim());
    }

    // ── Cập nhật sinh hiệu ──────────────────────────────────────────────────

    public boolean updateVitalSigns(long recordId, double weight, double height,
                                     String bloodPressure, int pulse) throws Exception {
        if (recordId <= 0) {
            throw new Exception("Record ID không hợp lệ");
        }
        if (weight <= 0) {
            throw new Exception("Cân nặng phải lớn hơn 0");
        }
        if (height <= 0) {
            throw new Exception("Chiều cao phải lớn hơn 0");
        }
        if (pulse <= 0) {
            throw new Exception("Mạch phải lớn hơn 0");
        }
        return dao.updateVitalSigns(recordId, weight, height, bloodPressure, pulse);
    }

    // ── Cập nhật trạng thái ─────────────────────────────────────────────────

    public boolean updateStatus(long recordId, String status) throws Exception {
        if (recordId <= 0) {
            throw new Exception("Record ID không hợp lệ");
        }
        return dao.updateStatus(recordId, status);
    }

    // ── Lấy danh sách bệnh nhân đang chờ khám ──────────────────────────────

    public List<Patient> getWaitingPatients() {
        return patientDAO.findWaiting();
    }

    // ── Lấy danh sách bệnh nhân theo trạng thái ────────────────────────────

    public List<Patient> getPatientsByStatus(String status) {
        return patientDAO.findByStatus(status);
    }
}
