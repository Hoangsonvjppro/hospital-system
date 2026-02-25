package com.hospital.bus;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.MedicalRecordDAO;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Business logic layer cho benh an.
 * Bao gom: tao benh an + cap nhat chan doan, trieu chung, sinh hieu.
 * Không còn giữ Connection trong field — mỗi operation tự tạo connection/transaction.
 * Các method liên quan hàng đợi đã chuyển sang QueueBUS.
 */
public class MedicalRecordBUS {

    private final MedicalRecordDAO dao;

    public MedicalRecordBUS() {
        this.dao = new MedicalRecordDAO();
    }

    public long createMedicalRecord(long patientId, long doctorId, Long appointmentId) {
        if (patientId <= 0) {
            throw new BusinessException("Patient ID không hợp lệ");
        }
        if (doctorId <= 0) {
            throw new BusinessException("Doctor ID không hợp lệ");
        }
        // Sử dụng transaction: tạo record + có thể cập nhật trạng thái bệnh nhân
        try (Connection conn = DatabaseConfig.getInstance().getTransactionalConnection()) {
            MedicalRecordDAO txnRecordDAO = new MedicalRecordDAO(conn);
            // PatientDAO txnPatientDAO = new PatientDAO(conn); // Sẵn sàng nếu cần thao tác thêm

            long recordId = txnRecordDAO.createEmptyRecord(patientId, doctorId, appointmentId);

            conn.commit();
            return recordId;
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
}
