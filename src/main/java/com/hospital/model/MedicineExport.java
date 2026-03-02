package com.hospital.model;

import java.time.LocalDateTime;

public class MedicineExport extends BaseModel {
    private int prescriptionDetailId;
    private int medicineId;
    private int quantityExported;
    private LocalDateTime exportDate;
    private int pharmacistId;

    public MedicineExport() {
    }

    public MedicineExport(int prescriptionDetailId, int medicineId, int quantityExported, LocalDateTime exportDate, int pharmacistId) {
        this.prescriptionDetailId = prescriptionDetailId;
        this.medicineId = medicineId;
        this.quantityExported = quantityExported;
        this.exportDate = exportDate;
        this.pharmacistId = pharmacistId;
    }

    public int getPrescriptionDetailId() { return prescriptionDetailId; }
    public void setPrescriptionDetailId(int prescriptionDetailId) { this.prescriptionDetailId = prescriptionDetailId; }

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public int getQuantityExported() { return quantityExported; }
    public void setQuantityExported(int quantityExported) { this.quantityExported = quantityExported; }

    public LocalDateTime getExportDate() { return exportDate; }
    public void setExportDate(LocalDateTime exportDate) { this.exportDate = exportDate; }

    public int getPharmacistId() { return pharmacistId; }
    public void setPharmacistId(int pharmacistId) { this.pharmacistId = pharmacistId; }

    @Override
    public String toString() {
        return "";
    }
}