package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Model bệnh án (Medical Record).
 */
public class MedicalRecord extends BaseModel {

    private long patientId;
    private long doctorId;
    private Long appointmentId;
    private LocalDateTime visitDate;

    // ── Thông tin khám ───────────────────────────────────────────────────────
    private String symptoms;       // Triệu chứng
    private String diagnosis;      // Chẩn đoán

    // ── Sinh hiệu (Vital Signs) ─────────────────────────────────────────────
    private double weight;         // Cân nặng (kg)
    private double height;         // Chiều cao (cm)
    private String bloodPressure;  // Huyết áp (vd: "120/80")
    private int pulse;             // Mạch (bpm)
    private double temperature;    // Nhiệt độ (°C)
    private int spo2;              // SpO2 (%)

    private String diagnosisCode;  // Mã ICD-10
    private String notes;          // Doctor notes

    private String status;         // Editing / Completed / Transferred

    // Workflow statuses (mở rộng)
    public static final String STATUS_WAITING     = "WAITING";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS"; // tương đương EXAMINING
    public static final String STATUS_PRESCRIBED  = "PRESCRIBED";
    public static final String STATUS_COMPLETED   = "COMPLETED";
    public static final String STATUS_PAID        = "PAID";

    // Queue/workflow fields — synced with DB columns: priority, queue_number, arrival_time, exam_type
    // Used by QueueDAO, DoctorWorkstationPanel, ExaminationPanel, PatientPanel.
    private String priority;       // NORMAL / ELDERLY / EMERGENCY — DB column: priority
    private Integer queueNumber;   // Số thứ tự hôm nay — DB column: queue_number
    private java.time.LocalTime arrivalTime; // Giờ đến — DB column: arrival_time
    private String examTypeField;   // Loại khám (vd: "Kham tong quat") — DB column: exam_type
    private java.time.LocalDate followUpDate; // Ngày hẹn tái khám — set by doctor on completion

    public MedicalRecord() {}

    public MedicalRecord(int id, long patientId, long doctorId, Long appointmentId) {
        super(id);
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.visitDate = LocalDateTime.now();
        this.status = STATUS_WAITING;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public long getPatientId()                { return patientId; }
    public void setPatientId(long v)          { this.patientId = v; }

    public long getDoctorId()                 { return doctorId; }
    public void setDoctorId(long v)           { this.doctorId = v; }

    public Long getAppointmentId()            { return appointmentId; }
    public void setAppointmentId(Long v)      { this.appointmentId = v; }

    public LocalDateTime getVisitDate()       { return visitDate; }
    public void setVisitDate(LocalDateTime v) { this.visitDate = v; }

    public String getSymptoms()               { return symptoms; }
    public void setSymptoms(String v)         { this.symptoms = v; }

    public String getDiagnosis()              { return diagnosis; }
    public void setDiagnosis(String v)        { this.diagnosis = v; }

    public String getPriority()               { return priority; }
    public void setPriority(String v)         { this.priority = v; }

    public Integer getQueueNumber()           { return queueNumber; }
    public void setQueueNumber(Integer v)     { this.queueNumber = v; }

    public java.time.LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(java.time.LocalTime v) { this.arrivalTime = v; }

    public String getExamTypeField()          { return examTypeField; }
    public void setExamTypeField(String v)    { this.examTypeField = v; }

    public java.time.LocalDate getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(java.time.LocalDate v) { this.followUpDate = v; }

    public double getWeight()                 { return weight; }
    public void setWeight(double v)           { this.weight = v; }

    public double getHeight()                 { return height; }
    public void setHeight(double v)           { this.height = v; }

    public String getBloodPressure()          { return bloodPressure; }
    public void setBloodPressure(String v)    { this.bloodPressure = v; }

    public int getPulse()                     { return pulse; }
    public void setPulse(int v)               { this.pulse = v; }

    public double getTemperature()            { return temperature; }
    public void setTemperature(double v)      { this.temperature = v; }

    public int getSpo2()                      { return spo2; }
    public void setSpo2(int v)                { this.spo2 = v; }

    public String getDiagnosisCode()          { return diagnosisCode; }
    public void setDiagnosisCode(String v)    { this.diagnosisCode = v; }

    public String getNotes()                  { return notes; }
    public void setNotes(String v)            { this.notes = v; }

    public String getStatus()                 { return status; }
    public void setStatus(String v)           { this.status = v; }

    @Override
    public String toString() {
        return "MedicalRecord{id=" + id + ", patientId=" + patientId + ", doctorId=" + doctorId + "}";
    }
}
