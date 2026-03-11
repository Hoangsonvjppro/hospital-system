package com.hospital.model;

import java.time.LocalDateTime;

public class Prescription extends BaseModel {

    public static final String STATUS_DRAFT     = "DRAFT";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_DISPENSED  = "DISPENSED";
    public static final String STATUS_CANCELLED  = "CANCELLED";
    public static final String STATUS_PENDING    = "CONFIRMED";

    private long medicalRecordId;
    private LocalDateTime createdAt;
    private String status; 
    private double totalAmount;

    public Prescription() {}

    public Prescription(long medicalRecordId) {
        this.medicalRecordId = medicalRecordId;
        this.createdAt = LocalDateTime.now();
        this.status = STATUS_CONFIRMED;
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

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return "Prescription{id=" + id + ", medicalRecordId=" + medicalRecordId + ", status='" + status + "'}";
    }
}
