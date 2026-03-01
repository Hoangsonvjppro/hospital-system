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
    private String doctorNote;
    private LocalDate followUpDate;


    // ── Sinh hiệu (Vital Signs) ─────────────────────────────────────────────
    private double weight;         // Cân nặng (kg)
    private double height;         // Chiều cao (cm)
    private String bloodPressure;  // Huyết áp (vd: "120/80")
    private int pulse;             // Mạch (bpm)
    private Double temperature;
    
    private String status;         // Editing / Completed / Transferred

    public MedicalRecord() {}

    public MedicalRecord(int id, long patientId, long doctorId, Long appointmentId) {
        super(id);
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.visitDate = LocalDateTime.now();
        this.status = "Editing";
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

    public double getWeight()                 { return weight; }
    public void setWeight(double v)           { this.weight = v; }

    public double getHeight()                 { return height; }
    public void setHeight(double v)           { this.height = v; }

    public String getBloodPressure()          { return bloodPressure; }
    public void setBloodPressure(String v)    { this.bloodPressure = v; }

    public int getPulse()                     { return pulse; }
    public void setPulse(int v)               { this.pulse = v; }

    public String getStatus()                 { return status; }
    public void setStatus(String v)           { this.status = v; }

    public Double getTemperature()            { return temperature; }
    public void setTemperature(Double v)      { this.temperature = v; }

    public String getDoctorNote()             { return doctorNote; }
    public void setDoctorNote(String v)       { this.doctorNote = v; }

    public LocalDate getFollowUpDate()        { return followUpDate; }
    public void setFollowUpDate(LocalDate v)  { this.followUpDate = v; }

    @Override
    public String toString() {
        return "MedicalRecord{id=" + id + ", patientId=" + patientId + ", doctorId=" + doctorId + "}";
    }
}
