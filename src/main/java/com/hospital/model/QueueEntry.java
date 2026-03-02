package com.hospital.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO hàng đợi khám bệnh — không phải entity DB, chỉ dùng để hiển thị.
 * Chứa thông tin cần thiết cho danh sách hàng đợi trên giao diện.
 */
public class QueueEntry {
    private int queueNumber;
    private int patientId;
    private String patientName;
    private String patientCode;
    private int doctorId;
    private String doctorName;
    private String priority;       // EMERGENCY, ELDERLY, NORMAL
    private String status;         // WAITING, IN_PROGRESS, COMPLETED, CANCELLED
    private String examType;
    private LocalTime arrivalTime;
    private int medicalRecordId;

    public QueueEntry() {}

    // ── Getters & Setters ────────────────────────────────────

    public int getQueueNumber() { return queueNumber; }
    public void setQueueNumber(int v) { this.queueNumber = v; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int v) { this.patientId = v; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String v) { this.patientName = v; }

    public String getPatientCode() {
        if (patientCode == null || patientCode.isEmpty()) {
            return String.format("BN%03d", patientId);
        }
        return patientCode;
    }
    public void setPatientCode(String v) { this.patientCode = v; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int v) { this.doctorId = v; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String v) { this.doctorName = v; }

    public String getPriority() { return priority; }
    public void setPriority(String v) { this.priority = v; }

    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }

    public String getExamType() { return examType; }
    public void setExamType(String v) { this.examType = v; }

    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime v) { this.arrivalTime = v; }

    public int getMedicalRecordId() { return medicalRecordId; }
    public void setMedicalRecordId(int v) { this.medicalRecordId = v; }

    /**
     * Hiển thị ưu tiên tiếng Việt.
     */
    public String getPriorityDisplay() {
        if (priority == null) return "Bình thường";
        return switch (priority) {
            case "EMERGENCY" -> "Cấp cứu";
            case "ELDERLY"   -> "Người cao tuổi";
            default          -> "Bình thường";
        };
    }

    /**
     * Hiển thị trạng thái tiếng Việt.
     */
    public String getStatusDisplay() {
        if (status == null) return "";
        return switch (status) {
            case "WAITING"     -> "Đang chờ";
            case "IN_PROGRESS" -> "Đang khám";
            case "COMPLETED"   -> "Hoàn thành";
            case "CANCELLED"   -> "Đã hủy";
            default            -> status;
        };
    }

    @Override
    public String toString() {
        return "QueueEntry{" +
                "queueNumber=" + queueNumber +
                ", patientName='" + patientName + '\'' +
                ", priority='" + priority + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
