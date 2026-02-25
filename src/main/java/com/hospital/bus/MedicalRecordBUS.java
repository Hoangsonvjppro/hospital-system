package com.hospital.bus;

import com.hospital.dao.MedicalRecordDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Patient;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Business logic layer cho benh an.
 * Bao gom: tao benh an + cap nhat chan doan, trieu chung, sinh hieu.
 * Ket hop logic hang doi benh nhan tu PatientDAO.
 */
public class MedicalRecordBUS {

    private MedicalRecordDAO dao;
    private PatientDAO patientDAO;

    public MedicalRecordBUS(Connection connection) {
        this.dao = new MedicalRecordDAO(connection);
        this.patientDAO = new PatientDAO();
    }

    public long createMedicalRecord(long patientId, long doctorId, Long appointmentId) {
        if (patientId <= 0) {
            throw new BusinessException("Patient ID không hợp lệ");
        }
        if (doctorId <= 0) {
            throw new BusinessException("Doctor ID không hợp lệ");
        }
        try {
            return dao.createEmptyRecord(patientId, doctorId, appointmentId);
        } catch (SQLException e) {
            throw new DataAccessException("Không thể tạo bệnh án", e);
        }
    }

    public boolean updateDiagnosis(long recordId, String diagnosis) {
        if (recordId <= 0) {
            throw new BusinessException("Record ID không hợp lệ");
        }
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            throw new BusinessException("Chẩn đoán không được để trống");
        }
        try {
            return dao.updateDiagnosis(recordId, diagnosis.trim());
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật chẩn đoán", e);
        }
    }

    public boolean updateSymptoms(long recordId, String symptoms) {
        if (recordId <= 0) {
            throw new BusinessException("Record ID không hợp lệ");
        }
        if (symptoms == null || symptoms.trim().isEmpty()) {
            throw new BusinessException("Triệu chứng không được để trống");
        }
        try {
            return dao.updateSymptoms(recordId, symptoms.trim());
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật triệu chứng", e);
        }
    }

    public boolean updateDiagnosisAndSymptoms(long recordId, String diagnosis, String symptoms) {
        if (recordId <= 0) {
            throw new BusinessException("Record ID không hợp lệ");
        }
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            throw new BusinessException("Chẩn đoán không được để trống");
        }
        if (symptoms == null || symptoms.trim().isEmpty()) {
            throw new BusinessException("Triệu chứng không được để trống");
        }
        try {
            return dao.updateDiagnosisAndSymptoms(recordId, diagnosis.trim(), symptoms.trim());
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật chẩn đoán và triệu chứng", e);
        }
    }

    public boolean updateVitalSigns(long recordId, double weight, double height,
                                     String bloodPressure, int pulse) {
        if (recordId <= 0) {
            throw new BusinessException("Record ID không hợp lệ");
        }
        if (weight <= 0) {
            throw new BusinessException("Cân nặng phải lớn hơn 0");
        }
        if (height <= 0) {
            throw new BusinessException("Chiều cao phải lớn hơn 0");
        }
        if (pulse <= 0) {
            throw new BusinessException("Mạch phải lớn hơn 0");
        }
        try {
            return dao.updateVitalSigns(recordId, weight, height, bloodPressure, pulse);
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật sinh hiệu", e);
        }
    }

    public boolean updateStatus(long recordId, String status) {
        if (recordId <= 0) {
            throw new BusinessException("Record ID không hợp lệ");
        }
        try {
            return dao.updateStatus(recordId, status);
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật trạng thái bệnh án", e);
        }
    }

    public List<Patient> getWaitingPatients() {
        return patientDAO.findWaiting();
    }

    public List<Patient> getPatientsByStatus(String status) {
        return patientDAO.findByStatus(status);
    }
}
