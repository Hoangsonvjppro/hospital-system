package com.hospital.model;

public class PrescriptionDetail {

    private long id;
    private long prescriptionId;
    private int medicineId;
    private int quantity;
    private String dosage;     
    private String frequency;   
    private int duration;        
    private String instruction;  
    private double unitPrice;    
    private double totalAmount;  
    private String medicineName;
    private String unit;        

    public PrescriptionDetail() {}

    public PrescriptionDetail(int medicineId, int quantity, String dosage) {
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.dosage = dosage;
    }

    public PrescriptionDetail(int medicineId, int quantity, String dosage, String instruction, double unitPrice) {
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.dosage = dosage;
        this.instruction = instruction;
        this.unitPrice = unitPrice;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getLineTotal() {
        return quantity * unitPrice;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
