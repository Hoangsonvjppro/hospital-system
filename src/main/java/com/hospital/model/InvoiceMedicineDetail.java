package com.hospital.model;

/**
 * Entity chi tiết hóa đơn — phần THUỐC.
 * Ánh xạ bảng InvoiceMedicineDetail trong CSDL.
 *
 * Bảng InvoiceMedicineDetail:
 *   detail_id, invoice_id, medicine_id, prescription_detail_id,
 *   medicine_name (snapshot), quantity, unit_price (giá bán), cost_price (giá vốn),
 *   line_total  (GENERATED = quantity * unit_price),
 *   profit_total (GENERATED = quantity * (unit_price - cost_price))
 *
 * Lưu ý:
 * - line_total và profit_total là cột GENERATED → chỉ đọc, không INSERT/UPDATE.
 * - medicine_name là snapshot tại thời điểm lập hóa đơn.
 * - cost_price lưu giá vốn để tính lợi nhuận thuốc.
 * - unit (đơn vị) là trường hiển thị, lấy từ JOIN với bảng Medicine nếu cần.
 */
public class InvoiceMedicineDetail extends BaseModel {

    private long invoiceId;
    private long medicineId;
    private Long prescriptionDetailId;   // Nullable: FK → PrescriptionDetail
    private Long batchId;                // FK → MedicineBatch
    private String medicineName;         // Snapshot tên thuốc
    private int quantity;
    private double unitPrice;            // Giá bán snapshot
    private double costPrice;            // Giá vốn snapshot
    private double lineTotal;            // GENERATED: quantity × unitPrice
    private double profitTotal;          // GENERATED: quantity × (unitPrice - costPrice)

    // ── Trường hiển thị (transient — từ JOIN Medicine) ───────

    private String unit;                 // Đơn vị tính: Viên, Chai, Gói…

    // ── Constructors ─────────────────────────────────────────

    public InvoiceMedicineDetail() {
        this.quantity = 1;
    }

    public InvoiceMedicineDetail(long invoiceId, long medicineId,
                                 String medicineName, int quantity,
                                 double unitPrice, double costPrice) {
        this.invoiceId = invoiceId;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.costPrice = costPrice;
        this.lineTotal = quantity * unitPrice;
        this.profitTotal = quantity * (unitPrice - costPrice);
    }

    // ── Getters & Setters ────────────────────────────────────

    public long getInvoiceId()                        { return invoiceId; }
    public void setInvoiceId(long v)                  { this.invoiceId = v; }

    public long getMedicineId()                       { return medicineId; }
    public void setMedicineId(long v)                 { this.medicineId = v; }

    public Long getPrescriptionDetailId()             { return prescriptionDetailId; }
    public void setPrescriptionDetailId(Long v)       { this.prescriptionDetailId = v; }

    public Long getBatchId()                          { return batchId; }
    public void setBatchId(Long v)                    { this.batchId = v; }

    public String getMedicineName()                   { return medicineName; }
    public void setMedicineName(String v)             { this.medicineName = v; }

    public int getQuantity()                          { return quantity; }
    public void setQuantity(int v)                    { this.quantity = v; }

    public double getUnitPrice()                      { return unitPrice; }
    public void setUnitPrice(double v)                { this.unitPrice = v; }

    public double getCostPrice()                      { return costPrice; }
    public void setCostPrice(double v)                { this.costPrice = v; }

    /**
     * Thành tiền = quantity × unitPrice.
     * Nếu chưa load từ DB (lineTotal == 0 và quantity > 0), tự tính.
     */
    public double getLineTotal() {
        return (lineTotal > 0) ? lineTotal : (double) quantity * unitPrice;
    }
    public void setLineTotal(double v) { this.lineTotal = v; }

    /**
     * Lợi nhuận = quantity × (unitPrice − costPrice).
     */
    public double getProfitTotal() {
        return (profitTotal > 0) ? profitTotal : (double) quantity * (unitPrice - costPrice);
    }
    public void setProfitTotal(double v) { this.profitTotal = v; }

    // Display field
    public String getUnit()                           { return unit; }
    public void setUnit(String v)                     { this.unit = v; }

    // ── toString ─────────────────────────────────────────────

    @Override
    public String toString() {
        return "InvoiceMedicineDetail{" +
                "id=" + id +
                ", invoiceId=" + invoiceId +
                ", medicineName='" + medicineName + '\'' +
                ", qty=" + quantity +
                ", unitPrice=" + unitPrice +
                ", costPrice=" + costPrice +
                ", lineTotal=" + getLineTotal() +
                '}';
    }
}
