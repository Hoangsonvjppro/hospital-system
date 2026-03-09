package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Entity giao dịch kho — ánh xạ bảng StockTransaction.
 */
public class StockTransaction extends BaseModel {

    private long medicineId;
    private long batchId;
    private String transactionType; // IMPORT, EXPORT_PRESCRIPTION, ADJUSTMENT, RETURN_TO_SUPPLIER, EXPIRED_DISPOSAL
    private int quantity;
    private int stockBefore;
    private int stockAfter;
    private Long referenceId;
    private String notes;
    private Long createdBy;

    // Transient
    private String medicineName;
    private String batchNumber;

    public StockTransaction() {}

    public long getMedicineId()                 { return medicineId; }
    public void setMedicineId(long v)           { this.medicineId = v; }

    public long getBatchId()                    { return batchId; }
    public void setBatchId(long v)              { this.batchId = v; }

    public String getTransactionType()          { return transactionType; }
    public void setTransactionType(String v)    { this.transactionType = v; }

    public int getQuantity()                    { return quantity; }
    public void setQuantity(int v)              { this.quantity = v; }

    public int getStockBefore()                 { return stockBefore; }
    public void setStockBefore(int v)           { this.stockBefore = v; }

    public int getStockAfter()                  { return stockAfter; }
    public void setStockAfter(int v)            { this.stockAfter = v; }

    public Long getReferenceId()                { return referenceId; }
    public void setReferenceId(Long v)          { this.referenceId = v; }

    public String getNotes()                    { return notes; }
    public void setNotes(String v)              { this.notes = v; }

    public Long getCreatedBy()                  { return createdBy; }
    public void setCreatedBy(Long v)            { this.createdBy = v; }

    public String getMedicineName()             { return medicineName; }
    public void setMedicineName(String v)       { this.medicineName = v; }

    public String getBatchNumber()              { return batchNumber; }
    public void setBatchNumber(String v)        { this.batchNumber = v; }

    @Override
    public String toString() {
        return "StockTransaction{type='" + transactionType + "', qty=" + quantity + "}";
    }
}
