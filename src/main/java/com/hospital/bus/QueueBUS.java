package com.hospital.bus;

import com.hospital.dao.QueueDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.Patient;

import java.util.List;

/**
 * Business logic layer cho hàng đợi khám bệnh.
 * Thay thế các method queue cũ trong PatientBUS.
 * Dữ liệu được persist xuống DB (bảng MedicalRecord) thay vì in-memory.
 */
public class QueueBUS {

    private final QueueDAO queueDAO;

    public QueueBUS() {
        this.queueDAO = new QueueDAO();
    }

    /**
     * Đưa bệnh nhân vào hàng đợi chờ khám.
     *
     * @param patientId ID bệnh nhân
     * @param doctorId  ID bác sĩ
     * @param examType  Loại khám (vd: "Khám tổng quát")
     * @return record_id của MedicalRecord vừa tạo
     */
    public long enqueue(long patientId, long doctorId, String examType) {
        if (patientId <= 0) {
            throw new BusinessException("Patient ID không hợp lệ");
        }
        if (doctorId <= 0) {
            throw new BusinessException("Doctor ID không hợp lệ");
        }
        return queueDAO.enqueue(patientId, doctorId, examType);
    }

    /**
     * Lấy danh sách bệnh nhân đang chờ khám + đang khám (ngày hôm nay).
     */
    public List<Patient> getWaitingPatients() {
        return queueDAO.findByQueueStatus("WAITING", "EXAMINING");
    }

    /**
     * Lấy danh sách bệnh nhân theo trạng thái hàng đợi.
     *
     * @param statuses Các trạng thái cần lọc
     */
    public List<Patient> getPatientsByStatus(String... statuses) {
        return queueDAO.findByQueueStatus(statuses);
    }

    /**
     * Cập nhật trạng thái hàng đợi.
     *
     * @param recordId  ID bệnh án (MedicalRecord)
     * @param newStatus Trạng thái mới (WAITING, EXAMINING, COMPLETED, TRANSFERRED)
     * @return true nếu cập nhật thành công
     */
    public boolean updateQueueStatus(long recordId, String newStatus) {
        if (recordId <= 0) {
            throw new BusinessException("Record ID không hợp lệ");
        }
        return queueDAO.updateQueueStatus(recordId, newStatus);
    }

    /**
     * Đếm tổng số bệnh nhân trong hàng đợi hôm nay.
     */
    public int countToday() {
        return queueDAO.countToday();
    }

    /**
     * Đếm bệnh nhân theo trạng thái (hôm nay).
     */
    public int countByStatus(String status) {
        return queueDAO.countByStatus(status);
    }
}
