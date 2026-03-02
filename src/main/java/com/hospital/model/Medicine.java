package com.hospital.model;

import java.time.LocalDate;

public class Medicine extends BaseModel{
    private String medicineName;
    private String unit;
    private double costPrice;
    private double sellPrice;
    private int stockQty;
    private int minThreshold;
    private LocalDate expiryDate;
    private String description;
    private boolean isActive;

    public Medicine(String medicineName, String unit, double costPrice, double sellPrice, int stockQty, int minThreshold, LocalDate expiryDate, String description, boolean isActive) {
        this.medicineName = medicineName;
        this.unit = unit;
        this.costPrice = costPrice;
        this.sellPrice = sellPrice;
        this.stockQty = stockQty;
        this.minThreshold = minThreshold;
        this.expiryDate = expiryDate;
        this.description = description;
        this.isActive = isActive;
    }
    public Medicine() {
        this.isActive=true;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public int getStockQty() {
        return stockQty;
    }

    public void setStockQty(int stockQty) {
        this.stockQty = stockQty;
    }

    public int getMinThreshold() {
        return minThreshold;
    }

    public void setMinThreshold(int minThreshold) {
        this.minThreshold = minThreshold;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Medicine{" +
                "id=" + id +
                ", medicineName='" + medicineName + '\'' +
                ", unit='" + unit + '\'' +
                ", costPrice=" + costPrice +
                ", sellPrice=" + sellPrice +
                ", stockQty=" + stockQty +
                ", minThreshold=" + minThreshold +
                ", expiryDate=" + expiryDate +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
