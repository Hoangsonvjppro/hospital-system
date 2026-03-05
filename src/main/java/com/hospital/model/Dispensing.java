package com.hospital.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity phiếu phát thuốc — ánh xạ bảng Dispensing trong CSDL.
 *
 * Bảng Dispensing:
 *   dispensing_id, prescription_id, patient_id, dispensed_by,
 *   status (PENDING/DISPENSED/PARTIAL), total_amount, notes,
 *   dispensed_at, created_at, updated_at
 */
public class Dispensing extends BaseModel {

    public static final String STATUS_PENDING   = "PENDING";
    public static final String STATUS_DISPENSED  = "DISPENSED";
    public static final String STATUS_PARTIAL    = "PARTIAL";

    private long prescriptionId;
    private long patientId;
    private Long dispensedBy;      // FK → User.user_id (dược sĩ)
    private LocalDateTime dispensedAt;
    private String status;
    private String notes;
    private BigDecimal totalAmount;

    // Transient — for display
    private String patientName;
    private String pharmacistName;

    // ── Constructors ─────────────────────────────────────────

    public Dispensing() {
        this.status = STATUS_PENDING;
        this.totalAmount = BigDecimal.ZERO;
    }

    public Dispensing(long prescriptionId, long patientId) {
        this();
        this.prescriptionId = prescriptionId;
        this.patientId = patientId;
    }

    // ── Display helpers ──────────────────────────────────────

    public String getStatusDisplay() {
        if (status == null) return "";
        return switch (status) {
            case STATUS_PENDING   -> "Chờ phát";
            case STATUS_DISPENSED -> "Đã phát";
            case STATUS_PARTIAL   -> "Phát một phần";
            default               -> status;
        };
    }

    // ── Getters & Setters ────────────────────────────────────

    public long getPrescriptionId()                  { return prescriptionId; }
    public void setPrescriptionId(long v)            { this.prescriptionId = v; }

    public long getPatientId()                       { return patientId; }
    public void setPatientId(long v)                 { this.patientId = v; }

    public Long getDispensedBy()                     { return dispensedBy; }
    public void setDispensedBy(Long v)               { this.dispensedBy = v; }

    public LocalDateTime getDispensedAt()            { return dispensedAt; }
    public void setDispensedAt(LocalDateTime v)      { this.dispensedAt = v; }

    public String getStatus()                        { return status; }
    public void setStatus(String v)                  { this.status = v; }

    public String getNotes()                         { return notes; }
    public void setNotes(String v)                   { this.notes = v; }

    public BigDecimal getTotalAmount()               { return totalAmount; }
    public void setTotalAmount(BigDecimal v)         { this.totalAmount = v; }

    public String getPatientName()                   { return patientName; }
    public void setPatientName(String v)             { this.patientName = v; }

    public String getPharmacistName()                { return pharmacistName; }
    public void setPharmacistName(String v)          { this.pharmacistName = v; }

    @Override
    public String toString() {
        return "Dispensing{id=" + id + ", prescriptionId=" + prescriptionId
                + ", status='" + status + "', total=" + totalAmount + "}";
    }
}
