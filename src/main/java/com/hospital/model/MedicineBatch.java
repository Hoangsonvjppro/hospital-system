package com.hospital.model;

import java.time.LocalDate;

/**
 * Entity lô thuốc — ánh xạ bảng MedicineBatch.
 */
public class MedicineBatch extends BaseModel {

    private long receiptId;
    private long medicineId;
    private String batchNumber;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private double importPrice;
    private double sellPrice;
    private int initialQty;
    private int currentQty;
    private int minThreshold;

    // Transient
    private String medicineName;
    private String medicineCode;
    private String unit;

    public MedicineBatch() {
        this.minThreshold = 10;
    }

    public long getReceiptId()                  { return receiptId; }
    public void setReceiptId(long v)            { this.receiptId = v; }

    public long getMedicineId()                 { return medicineId; }
    public void setMedicineId(long v)           { this.medicineId = v; }

    public String getBatchNumber()              { return batchNumber; }
    public void setBatchNumber(String v)        { this.batchNumber = v; }

    public LocalDate getManufactureDate()       { return manufactureDate; }
    public void setManufactureDate(LocalDate v) { this.manufactureDate = v; }

    public LocalDate getExpiryDate()            { return expiryDate; }
    public void setExpiryDate(LocalDate v)      { this.expiryDate = v; }

    public double getImportPrice()              { return importPrice; }
    public void setImportPrice(double v)        { this.importPrice = v; }

    public double getSellPrice()                { return sellPrice; }
    public void setSellPrice(double v)          { this.sellPrice = v; }

    public int getInitialQty()                  { return initialQty; }
    public void setInitialQty(int v)            { this.initialQty = v; }

    public int getCurrentQty()                  { return currentQty; }
    public void setCurrentQty(int v)            { this.currentQty = v; }

    public int getMinThreshold()                { return minThreshold; }
    public void setMinThreshold(int v)          { this.minThreshold = v; }

    public String getMedicineName()             { return medicineName; }
    public void setMedicineName(String v)       { this.medicineName = v; }

    public String getMedicineCode()             { return medicineCode; }
    public void setMedicineCode(String v)       { this.medicineCode = v; }

    public String getUnit()                     { return unit; }
    public void setUnit(String v)               { this.unit = v; }

    @Override
    public String toString() {
        return "MedicineBatch{batch='" + batchNumber + "', qty=" + currentQty + ", expiry=" + expiryDate + "}";
    }
}
