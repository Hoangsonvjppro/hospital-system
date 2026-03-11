package com.hospital.model;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class FollowUp extends BaseModel {

    public static final String STATUS_SCHEDULED  = "SCHEDULED";
    public static final String STATUS_COMPLETED  = "COMPLETED";
    public static final String STATUS_MISSED     = "MISSED";
    public static final String STATUS_CANCELLED  = "CANCELLED";

    private long patientId;
    private long recordId;     
    private LocalDate followUpDate;
    private String reason;
    private String status;
    private boolean reminderSent;

    private String patientName;
    private String patientPhone;
    private String doctorName;
    private String diagnosis;


    public FollowUp() {
        this.status = STATUS_SCHEDULED;
        this.reminderSent = false;
    }

    public FollowUp(long patientId, long recordId, LocalDate followUpDate, String reason) {
        this();
        this.patientId = patientId;
        this.recordId = recordId;
        this.followUpDate = followUpDate;
        this.reason = reason;
    }


    public String getStatusDisplay() {
        if (status == null) return "";
        return switch (status) {
            case STATUS_SCHEDULED -> "Đã hẹn";
            case STATUS_COMPLETED -> "Đã tái khám";
            case STATUS_MISSED    -> "Bỏ lỡ";
            case STATUS_CANCELLED -> "Đã hủy";
            default               -> status;
        };
    }


    public long getPatientId()                       { return patientId; }
    public void setPatientId(long v)                 { this.patientId = v; }

    public long getRecordId()                        { return recordId; }
    public void setRecordId(long v)                  { this.recordId = v; }

    public LocalDate getFollowUpDate()               { return followUpDate; }
    public void setFollowUpDate(LocalDate v)         { this.followUpDate = v; }

    public String getReason()                        { return reason; }
    public void setReason(String v)                  { this.reason = v; }

    public String getStatus()                        { return status; }
    public void setStatus(String v)                  { this.status = v; }

    public boolean isReminderSent()                  { return reminderSent; }
    public void setReminderSent(boolean v)           { this.reminderSent = v; }

    public String getPatientName()                   { return patientName; }
    public void setPatientName(String v)             { this.patientName = v; }

    public String getPatientPhone()                  { return patientPhone; }
    public void setPatientPhone(String v)            { this.patientPhone = v; }

    public String getDoctorName()                    { return doctorName; }
    public void setDoctorName(String v)              { this.doctorName = v; }

    public String getDiagnosis()                     { return diagnosis; }
    public void setDiagnosis(String v)               { this.diagnosis = v; }

    @Override
    public String toString() {
        return "FollowUp{id=" + id + ", patientId=" + patientId
                + ", date=" + followUpDate + ", status='" + status + "'}";
    }
}
