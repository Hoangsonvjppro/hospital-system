package com.hospital.model;

import java.time.LocalDateTime;

public class Prescription extends BaseModel {

    private long medicalRecordId;
    private LocalDateTime createdAt;
    private String status; // PENDING / DISPENSED

    public Prescription() {}

    public Prescription(long medicalRecordId) {
        this.medicalRecordId = medicalRecordId;
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    public long getMedicalRecordId() {
        return medicalRecordId;
    }

    public void setMedicalRecordId(long medicalRecordId) {
        this.medicalRecordId = medicalRecordId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Prescription{id=" + id + ", medicalRecordId=" + medicalRecordId + ", status='" + status + "'}";
    }
}
