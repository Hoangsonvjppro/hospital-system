package com.hospital.model;

public class StockTransaction extends BaseModel {
    public static final String TYPE_IMPORT     = "IMPORT";
    public static final String TYPE_EXPORT     = "EXPORT";
    public static final String TYPE_ADJUSTMENT = "ADJUSTMENT";
    public static final String TYPE_RETURN     = "RETURN";

    private int    medicineId;
    private String transactionType;
    private int    quantity;
    private int    stockBefore;
    private int    stockAfter;
    private String referenceType;
    private Long   referenceId;
    private String notes;
    private Long   createdBy;

    private String medicineName;
    private String createdByName;

    public StockTransaction() {}

    public int getMedicineId()                        { return medicineId; }
    public void setMedicineId(int v)                  { this.medicineId = v; }

    public String getTransactionType()                { return transactionType; }
    public void setTransactionType(String v)          { this.transactionType = v; }

    public int getQuantity()                          { return quantity; }
    public void setQuantity(int v)                    { this.quantity = v; }

    public int getStockBefore()                       { return stockBefore; }
    public void setStockBefore(int v)                 { this.stockBefore = v; }

    public int getStockAfter()                        { return stockAfter; }
    public void setStockAfter(int v)                  { this.stockAfter = v; }

    public String getReferenceType()                  { return referenceType; }
    public void setReferenceType(String v)            { this.referenceType = v; }

    public Long getReferenceId()                      { return referenceId; }
    public void setReferenceId(Long v)                { this.referenceId = v; }

    public String getNotes()                          { return notes; }
    public void setNotes(String v)                    { this.notes = v; }

    public Long getCreatedBy()                        { return createdBy; }
    public void setCreatedBy(Long v)                  { this.createdBy = v; }

    public String getMedicineName()                   { return medicineName; }
    public void setMedicineName(String v)             { this.medicineName = v; }

    public String getCreatedByName()                  { return createdByName; }
    public void setCreatedByName(String v)            { this.createdByName = v; }

    public String getTransactionTypeDisplay() {
        if (transactionType == null) return "";
        return switch (transactionType) {
            case TYPE_IMPORT     -> "Nhập kho";
            case TYPE_EXPORT     -> "Xuất kho";
            case TYPE_ADJUSTMENT -> "Điều chỉnh";
            case TYPE_RETURN     -> "Trả hàng";
            default              -> transactionType;
        };
    }

    @Override
    public String toString() {
        return "StockTransaction{id=" + id
                + ", medicineId=" + medicineId
                + ", type='" + transactionType + "'"
                + ", qty=" + quantity
                + ", before=" + stockBefore
                + ", after=" + stockAfter + "}";
    }
}
