package com.hospital.bus;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.MedicalRecordDAO;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;

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

    /**
     * Promote a record to EMERGENCY priority and reindex today's queue for the same doctor.
     */
    public void promoteRecordToEmergency(long recordId) {
        if (recordId <= 0) throw new com.hospital.exception.BusinessException("Record ID không hợp lệ");
        try (java.sql.Connection conn = com.hospital.config.DatabaseConfig.getInstance().getTransactionalConnection()) {
            MedicalRecordDAO txnDao = new MedicalRecordDAO(conn);
            // fetch record to know doctorId
            com.hospital.model.MedicalRecord r = txnDao.findById(recordId);
            if (r == null) throw new com.hospital.exception.BusinessException("Không tìm thấy bản ghi");

            // set priority
            txnDao.updatePriority(recordId, "EMERGENCY");

            // reindex today's queue for this doctor and force the promoted record to be first
            txnDao.reindexTodayQueue(r.getDoctorId(), recordId);

            conn.commit();
        } catch (java.sql.SQLException e) {
            throw new com.hospital.exception.DataAccessException("Không thể đẩy ưu tiên", e);
        }
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

    /**
     * Đưa bệnh nhân vào hàng đợi hôm nay với logic ưu tiên (cấp cứu > người cao tuổi > bình thường).
     * Nếu bệnh nhân đã có record hôm nay sẽ ném BusinessException.
     * Trả về recordId của MedicalRecord vừa tạo.
     */
    public long enqueuePatient(long patientId, long doctorId, Long appointmentId, boolean isEmergency, String examType) {
        if (patientId <= 0) throw new BusinessException("Patient ID không hợp lệ");
        if (doctorId <= 0) throw new BusinessException("Doctor ID không hợp lệ");

        try (Connection conn = DatabaseConfig.getInstance().getTransactionalConnection()) {
            MedicalRecordDAO txnRecordDAO = new MedicalRecordDAO(conn);
            com.hospital.dao.PatientDAO txnPatientDAO = new com.hospital.dao.PatientDAO(conn);

            // Kiểm tra trùng
            if (txnRecordDAO.patientHasRecordToday(patientId)) {
                throw new BusinessException("Bệnh nhân đã có trong hàng đợi hôm nay.");
            }

            // Quyết định priority
            String priority = "NORMAL";
            if (isEmergency) {
                priority = "EMERGENCY";
            } else {
                com.hospital.model.Patient p = txnPatientDAO.findById((int) patientId);
                if (p != null && p.getAge() >= 60) {
                    priority = "ELDERLY";
                }
            }

        // Xác định số thứ tự: đếm hiện tại +1
        int nextQueue = txnRecordDAO.countQueueToday(doctorId) + 1;

        // arrival time = now (store TIME of arrival when receptionist registers)
        Time arrival = Time.valueOf(LocalTime.now());

        // Tạo record trong transaction với priority, queue_number và arrival_time
        long recordId = txnRecordDAO.createEmptyRecord(patientId, doctorId, appointmentId,
            priority, nextQueue, arrival, examType);

            // Đặt trạng thái WAITING explicitly
            txnRecordDAO.updateStatus(recordId, com.hospital.model.MedicalRecord.STATUS_WAITING);

            conn.commit();
            return recordId;
        } catch (SQLException e) {
            throw new com.hospital.exception.DataAccessException("Không thể enqueue bệnh nhân", e);
        }
    }

    /**
     * Lấy danh sách hàng đợi hôm nay cho một bác sĩ (sắp xếp theo priority và queue_number).
     */
    public java.util.List<com.hospital.model.MedicalRecord> getTodayQueue(long doctorId) {
        MedicalRecordDAO dao = new MedicalRecordDAO();
        return dao.listQueueToday(doctorId);
    }

    /**
     * Get follow-up list scheduled for today (optionally for a doctor).
     */
    public java.util.List<com.hospital.model.MedicalRecord> getFollowUpsToday(long doctorId) {
        MedicalRecordDAO dao = new MedicalRecordDAO();
        return dao.listFollowUpsToday(doctorId);
    }

    /**
     * Get history (all medical records) for a patient.
     */
    public java.util.List<com.hospital.model.MedicalRecord> getHistoryByPatient(long patientId) {
        MedicalRecordDAO dao = new MedicalRecordDAO();
        return dao.listByPatient(patientId);
    }

    /**
     * Lấy MedicalRecord theo ID (wrapper cho DAO) để UI có thể đọc queue_number/priority...
     */
    public com.hospital.model.MedicalRecord findById(long recordId) {
        return dao.findById(recordId);
    }

    public boolean updateDiagnosis(long recordId, String diagnosis) {
        if (recordId <= 0) {
            throw new BusinessException("Record ID không hợp lệ");
        }
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            throw new BusinessException("Chẩn đoán không được để trống");
        }
        return dao.updateDiagnosis(recordId, diagnosis.trim());
    }

    public boolean updateSymptoms(long recordId, String symptoms) {
        if (recordId <= 0) {
            throw new BusinessException("Record ID không hợp lệ");
        }
        if (symptoms == null || symptoms.trim().isEmpty()) {
            throw new BusinessException("Triệu chứng không được để trống");
        }
        return dao.updateSymptoms(recordId, symptoms.trim());
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
        return dao.updateDiagnosisAndSymptoms(recordId, diagnosis.trim(), symptoms.trim());
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
        return dao.updateVitalSigns(recordId, weight, height, bloodPressure, pulse);
    }

    public boolean updateStatus(long recordId, String status) {
        if (recordId <= 0) {
            throw new BusinessException("Record ID không hợp lệ");
        }
        return dao.updateStatus(recordId, status);
    }
}
