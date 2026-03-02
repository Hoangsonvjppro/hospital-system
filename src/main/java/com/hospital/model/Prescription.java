package com.hospital.model;

import java.time.LocalDateTime;

public class Prescription extends BaseModel {

    private long medicalRecordId;
    private long doctorId;
    private LocalDateTime prescriptionDate;
    private String status; // PENDING / DISPENSED

    public Prescription() {
        this.prescriptionDate = LocalDateTime.now();
        this.status = "PENDING";
    }

    // ================= GETTER / SETTER =================

    public long getMedicalRecordId() {
        return medicalRecordId;
    }

    public void setMedicalRecordId(long medicalRecordId) {
        this.medicalRecordId = medicalRecordId;
    }

    public long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(LocalDateTime prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Prescription{id=" + getId() +
                ", medicalRecordId=" + medicalRecordId +
                ", doctorId=" + doctorId +
                ", status='" + status + '\'' +
                '}';
    }
}
