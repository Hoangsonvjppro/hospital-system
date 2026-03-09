package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Entity phiếu nhập hàng — ánh xạ bảng GoodsReceipt.
 */
public class GoodsReceipt extends BaseModel {

    private String receiptCode;
    private Long supplierId;
    private LocalDateTime importDate;
    private double totalAmount;
    private String note;
    private Long createdBy;
    private String status; // DRAFT, COMPLETED, CANCELLED

    // Transient
    private String supplierName;

    public GoodsReceipt() {
        this.status = "DRAFT";
    }

    public String getReceiptCode()               { return receiptCode; }
    public void setReceiptCode(String v)         { this.receiptCode = v; }

    public Long getSupplierId()                  { return supplierId; }
    public void setSupplierId(Long v)            { this.supplierId = v; }

    public LocalDateTime getImportDate()         { return importDate; }
    public void setImportDate(LocalDateTime v)   { this.importDate = v; }

    public double getTotalAmount()               { return totalAmount; }
    public void setTotalAmount(double v)         { this.totalAmount = v; }

    public String getNote()                      { return note; }
    public void setNote(String v)                { this.note = v; }

    public Long getCreatedBy()                   { return createdBy; }
    public void setCreatedBy(Long v)             { this.createdBy = v; }

    public String getStatus()                    { return status; }
    public void setStatus(String v)              { this.status = v; }

    public String getSupplierName()              { return supplierName; }
    public void setSupplierName(String v)        { this.supplierName = v; }

    @Override
    public String toString() {
        return "GoodsReceipt{code='" + receiptCode + "', status='" + status + "'}";
    }
}
