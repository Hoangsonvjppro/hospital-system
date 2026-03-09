package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Entity lượt khám — ánh xạ bảng MedicalRecord trong CSDL v4.
 */
public class MedicalRecord extends BaseModel {

    // ── Queue status constants ───────────────────────────────
    public static final String STATUS_WAITING         = "WAITING";
    public static final String STATUS_EXAMINING       = "EXAMINING";
    public static final String STATUS_WAITING_LAB     = "WAITING_LAB";
    public static final String STATUS_PRESCRIBING     = "PRESCRIBING";
    public static final String STATUS_WAITING_PAYMENT = "WAITING_PAYMENT";
    public static final String STATUS_COMPLETED       = "COMPLETED";
    public static final String STATUS_TRANSFERRED     = "TRANSFERRED";
    public static final String STATUS_CANCELLED       = "CANCELLED";

    // ── DB fields ────────────────────────────────────────────
    private long patientId;
    private Long doctorId;          // NULL khi lễ tân tiếp nhận, gán bác sĩ sau
    private Long appointmentId;
    private LocalDateTime visitDate;
    private String visitType;       // FIRST_VISIT / REVISIT / EMERGENCY

    // Hàng đợi
    private Integer queueNumber;
    private String priority;        // NORMAL / ELDERLY / EMERGENCY
    private String queueStatus;     // WAITING, EXAMINING, WAITING_LAB, ...

    // Sinh hiệu
    private String bloodPressure;
    private Integer heartRate;      // schema: heart_rate (was pulse)
    private Double temperature;
    private Double weight;
    private Double height;
    private Integer spo2;

    // Chẩn đoán
    private String symptoms;
    private String diagnosis;
    private String diagnosisCode;   // FK → Icd10Code.code
    private String referralNote;
    private String notes;

    // ── Transient (JOIN) ─────────────────────────────────────
    private String patientName;
    private String doctorName;

    // ── Constructors ─────────────────────────────────────────

    public MedicalRecord() {
        this.queueStatus = STATUS_WAITING;
        this.visitType = "FIRST_VISIT";
    }

    public MedicalRecord(int id, long patientId, Long doctorId, Long appointmentId) {
        super(id);
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.visitDate = LocalDateTime.now();
        this.queueStatus = STATUS_WAITING;
        this.visitType = "FIRST_VISIT";
    }

    // ── Getters & Setters ────────────────────────────────────

    public long getPatientId()                    { return patientId; }
    public void setPatientId(long v)              { this.patientId = v; }

    public Long getDoctorId()                     { return doctorId; }
    public void setDoctorId(Long v)               { this.doctorId = v; }

    public Long getAppointmentId()                { return appointmentId; }
    public void setAppointmentId(Long v)          { this.appointmentId = v; }

    public LocalDateTime getVisitDate()           { return visitDate; }
    public void setVisitDate(LocalDateTime v)     { this.visitDate = v; }

    public String getVisitType()                  { return visitType; }
    public void setVisitType(String v)            { this.visitType = v; }

    public Integer getQueueNumber()               { return queueNumber; }
    public void setQueueNumber(Integer v)         { this.queueNumber = v; }

    public String getPriority()                   { return priority; }
    public void setPriority(String v)             { this.priority = v; }

    public String getQueueStatus()                { return queueStatus; }
    public void setQueueStatus(String v)          { this.queueStatus = v; }

    /** Backward-compatible alias: getStatus() → getQueueStatus() */
    public String getStatus()                     { return queueStatus; }
    public void setStatus(String v)               { this.queueStatus = v; }

    public String getBloodPressure()              { return bloodPressure; }
    public void setBloodPressure(String v)        { this.bloodPressure = v; }

    public Integer getHeartRate()                 { return heartRate; }
    public void setHeartRate(Integer v)           { this.heartRate = v; }

    /** Backward-compatible alias: getPulse() → getHeartRate() */
    public int getPulse()                         { return heartRate != null ? heartRate : 0; }
    public void setPulse(int v)                   { this.heartRate = v; }

    public Double getTemperature()                { return temperature; }
    public void setTemperature(Double v)          { this.temperature = v; }

    public Double getWeight()                     { return weight; }
    public void setWeight(Double v)               { this.weight = v; }

    public Double getHeight()                     { return height; }
    public void setHeight(Double v)               { this.height = v; }

    public Integer getSpo2()                      { return spo2; }
    public void setSpo2(Integer v)                { this.spo2 = v; }

    public String getSymptoms()                   { return symptoms; }
    public void setSymptoms(String v)             { this.symptoms = v; }

    public String getDiagnosis()                  { return diagnosis; }
    public void setDiagnosis(String v)            { this.diagnosis = v; }

    public String getDiagnosisCode()              { return diagnosisCode; }
    public void setDiagnosisCode(String v)        { this.diagnosisCode = v; }

    public String getReferralNote()               { return referralNote; }
    public void setReferralNote(String v)         { this.referralNote = v; }

    public String getNotes()                      { return notes; }
    public void setNotes(String v)                { this.notes = v; }

    // Transient
    public String getPatientName()                { return patientName; }
    public void setPatientName(String v)          { this.patientName = v; }

    public String getDoctorName()                 { return doctorName; }
    public void setDoctorName(String v)           { this.doctorName = v; }

    @Override
    public String toString() {
        return "MedicalRecord{id=" + id + ", patientId=" + patientId + ", doctorId=" + doctorId + "}";
    }
}
