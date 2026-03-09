package com.hospital.model;

/**
 * Entity thuốc — ánh xạ bảng Medicine trong CSDL v4.
 * Giá, tồn kho, hạn dùng quản lý theo lô (MedicineBatch).
 */
public class Medicine extends BaseModel {

    private String medicineCode;
    private String medicineName;
    private String genericName;    // Tên hoạt chất
    private String unit;           // viên/gói/ống/chai
    private String dosageForm;     // viên nén/viên nang/siro/tiêm
    private String manufacturer;
    private String description;
    private boolean isActive;

    // ── Constructors ─────────────────────────────────────────

    public Medicine() {
        this.isActive = true;
    }

    public Medicine(String medicineCode, String medicineName, String unit,
                    String manufacturer, String description, boolean isActive) {
        this.medicineCode = medicineCode;
        this.medicineName = medicineName;
        this.unit = unit;
        this.manufacturer = manufacturer;
        this.description = description;
        this.isActive = isActive;
    }

    // ── Getters & Setters ────────────────────────────────────

    public String getMedicineCode()                     { return medicineCode; }
    public void setMedicineCode(String medicineCode)    { this.medicineCode = medicineCode; }

    public String getMedicineName()                     { return medicineName; }
    public void setMedicineName(String medicineName)    { this.medicineName = medicineName; }

    public String getGenericName()                      { return genericName; }
    public void setGenericName(String genericName)      { this.genericName = genericName; }

    public String getUnit()                             { return unit; }
    public void setUnit(String unit)                    { this.unit = unit; }

    public String getDosageForm()                       { return dosageForm; }
    public void setDosageForm(String dosageForm)        { this.dosageForm = dosageForm; }

    public String getManufacturer()                     { return manufacturer; }
    public void setManufacturer(String manufacturer)    { this.manufacturer = manufacturer; }

    public String getDescription()                      { return description; }
    public void setDescription(String description)      { this.description = description; }

    public boolean isActive()                           { return isActive; }
    public void setActive(boolean active)               { isActive = active; }

    @Override
    public String toString() {
        return "Medicine{" +
                "id=" + id +
                ", medicineCode='" + medicineCode + '\'' +
                ", medicineName='" + medicineName + '\'' +
                ", unit='" + unit + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
