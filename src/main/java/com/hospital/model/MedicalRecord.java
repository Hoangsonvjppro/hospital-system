package com.hospital.model;

import java.time.LocalDate;

public class MedicalRecord {

    private long id;
    private long patientId;
    private long doctorId;

    private String symptoms;
    private String diagnosis;
    private String diagnosisCode;

    // Vital signs
    private String bloodPressure;
    private Double temperature;
    private Integer pulse;
    private Double weight;
    private Double height;

    private String doctorNote;
    private LocalDate followUpDate;

    private String status; // WAITING, IN_PROGRESS, PRESCRIBED

    // ================= GETTER / SETTER =================

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getPatientId() { return patientId; }
    public void setPatientId(long patientId) { this.patientId = patientId; }

    public long getDoctorId() { return doctorId; }
    public void setDoctorId(long doctorId) { this.doctorId = doctorId; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getDiagnosisCode() { return diagnosisCode; }
    public void setDiagnosisCode(String diagnosisCode) { this.diagnosisCode = diagnosisCode; }

    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Integer getPulse() { return pulse; }
    public void setPulse(Integer pulse) { this.pulse = pulse; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public String getDoctorNote() { return doctorNote; }
    public void setDoctorNote(String doctorNote) { this.doctorNote = doctorNote; }

    public LocalDate getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDate followUpDate) { this.followUpDate = followUpDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
