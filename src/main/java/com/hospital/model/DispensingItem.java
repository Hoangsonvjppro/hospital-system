package com.hospital.model;

import java.math.BigDecimal;

/**
 * Entity chi tiết phát thuốc — ánh xạ bảng DispensingItem trong CSDL.
 *
 * Bảng DispensingItem:
 *   item_id, dispensing_id, prescription_detail_id, medicine_id,
 *   medicine_name, requested_quantity, dispensed_quantity,
 *   unit_price, subtotal (GENERATED), batch_number, created_at
 */
public class DispensingItem extends BaseModel {

    private long dispensingId;
    private long prescriptionDetailId;
    private long medicineId;
    private String medicineName;
    private int requestedQuantity;
    private int dispensedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;     // GENERATED column — read-only từ DB
    private String batchNumber;
    private String notes;

    // Transient — for display
    private int stockQty;            // Tồn kho hiện tại
    private String unit;             // Đơn vị tính

    // ── Constructors ─────────────────────────────────────────

    public DispensingItem() {
        this.unitPrice = BigDecimal.ZERO;
    }

    public DispensingItem(long dispensingId, long prescriptionDetailId,
                          long medicineId, String medicineName,
                          int requestedQuantity, int dispensedQuantity,
                          BigDecimal unitPrice) {
        this.dispensingId = dispensingId;
        this.prescriptionDetailId = prescriptionDetailId;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.requestedQuantity = requestedQuantity;
        this.dispensedQuantity = dispensedQuantity;
        this.unitPrice = unitPrice;
    }

    // ── Computed ─────────────────────────────────────────────

    /**
     * Thành tiền = dispensedQuantity × unitPrice.
     * Nếu chưa load từ DB, tự tính.
     */
    public BigDecimal getCalculatedSubtotal() {
        if (subtotal != null && subtotal.compareTo(BigDecimal.ZERO) > 0) {
            return subtotal;
        }
        return unitPrice.multiply(BigDecimal.valueOf(dispensedQuantity));
    }

    // ── Getters & Setters ────────────────────────────────────

    public long getDispensingId()                       { return dispensingId; }
    public void setDispensingId(long v)                 { this.dispensingId = v; }

    public long getPrescriptionDetailId()               { return prescriptionDetailId; }
    public void setPrescriptionDetailId(long v)         { this.prescriptionDetailId = v; }

    public long getMedicineId()                         { return medicineId; }
    public void setMedicineId(long v)                   { this.medicineId = v; }

    public String getMedicineName()                     { return medicineName; }
    public void setMedicineName(String v)               { this.medicineName = v; }

    public int getRequestedQuantity()                   { return requestedQuantity; }
    public void setRequestedQuantity(int v)             { this.requestedQuantity = v; }

    public int getDispensedQuantity()                   { return dispensedQuantity; }
    public void setDispensedQuantity(int v)             { this.dispensedQuantity = v; }

    public BigDecimal getUnitPrice()                    { return unitPrice; }
    public void setUnitPrice(BigDecimal v)              { this.unitPrice = v; }

    public BigDecimal getSubtotal()                     { return subtotal; }
    public void setSubtotal(BigDecimal v)               { this.subtotal = v; }

    public String getBatchNumber()                      { return batchNumber; }
    public void setBatchNumber(String v)                { this.batchNumber = v; }

    public String getNotes()                            { return notes; }
    public void setNotes(String v)                      { this.notes = v; }

    public int getStockQty()                            { return stockQty; }
    public void setStockQty(int v)                      { this.stockQty = v; }

    public String getUnit()                             { return unit; }
    public void setUnit(String v)                       { this.unit = v; }

    @Override
    public String toString() {
        return "DispensingItem{id=" + id + ", medicineId=" + medicineId
                + ", medicineName='" + medicineName + "'"
                + ", req=" + requestedQuantity + ", disp=" + dispensedQuantity + "}";
    }
}
