package com.hospital.model;

/**
 * Entity chi tiết đơn thuốc — ánh xạ bảng PrescriptionDetail trong CSDL v4.
 * line_total là cột GENERATED (quantity * unit_price) → chỉ đọc từ DB.
 */
public class PrescriptionDetail {

    private long id;
    private long prescriptionId;
    private long medicineId;
    private Long batchId;           // FK → MedicineBatch (FEFO)
    private int quantity;
    private String dosage;          // "1 viên × 3 lần/ngày"
    private String instruction;     // "Uống sau ăn"
    private String frequency;       // "Ngày 3 lần"
    private int durationDays;       // Số ngày dùng thuốc
    private double unitPrice;
    private double lineTotal;       // GENERATED — read-only

    // Transient — for display
    private String medicineName;
    private String unit;

    // ── Constructors ─────────────────────────────────────────

    public PrescriptionDetail() {}

    public PrescriptionDetail(long medicineId, int quantity, String dosage) {
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.dosage = dosage;
    }

    public PrescriptionDetail(long medicineId, int quantity, String dosage,
                              String instruction, double unitPrice) {
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.dosage = dosage;
        this.instruction = instruction;
        this.unitPrice = unitPrice;
    }

    // ── Getters & Setters ────────────────────────────────────

    public long getId()                              { return id; }
    public void setId(long id)                       { this.id = id; }

    public long getPrescriptionId()                  { return prescriptionId; }
    public void setPrescriptionId(long v)            { this.prescriptionId = v; }

    public long getMedicineId()                      { return medicineId; }
    public void setMedicineId(long v)                { this.medicineId = v; }

    public Long getBatchId()                         { return batchId; }
    public void setBatchId(Long v)                   { this.batchId = v; }

    public int getQuantity()                         { return quantity; }
    public void setQuantity(int v)                   { this.quantity = v; }

    public String getDosage()                        { return dosage; }
    public void setDosage(String v)                  { this.dosage = v; }

    public String getInstruction()                   { return instruction; }
    public void setInstruction(String v)             { this.instruction = v; }

    public String getFrequency()                     { return frequency; }
    public void setFrequency(String v)               { this.frequency = v; }

    public int getDurationDays()                     { return durationDays; }
    public void setDurationDays(int v)               { this.durationDays = v; }

    /** Backward-compatible alias */
    public int getDuration()                         { return durationDays; }
    public void setDuration(int v)                   { this.durationDays = v; }

    public double getUnitPrice()                     { return unitPrice; }
    public void setUnitPrice(double v)               { this.unitPrice = v; }

    /** GENERATED column — falls back to calculation if not loaded from DB */
    public double getLineTotal() {
        return (lineTotal > 0) ? lineTotal : (double) quantity * unitPrice;
    }
    public void setLineTotal(double v)               { this.lineTotal = v; }

    /** Backward-compatible alias */
    public double getTotalAmount()                   { return getLineTotal(); }
    public void setTotalAmount(double v)             { this.lineTotal = v; }

    // Transient
    public String getMedicineName()                  { return medicineName; }
    public void setMedicineName(String v)            { this.medicineName = v; }

    public String getUnit()                          { return unit; }
    public void setUnit(String v)                    { this.unit = v; }
}
