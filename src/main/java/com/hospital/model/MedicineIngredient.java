package com.hospital.model;

/**
 * Entity thành phần thuốc — ánh xạ bảng MedicineIngredient.
 */
public class MedicineIngredient extends BaseModel {

    private long medicineId;
    private String ingredientName;

    // Transient
    private String medicineName;

    public MedicineIngredient() {}

    public MedicineIngredient(long medicineId, String ingredientName) {
        this.medicineId = medicineId;
        this.ingredientName = ingredientName;
    }

    public long getMedicineId()                { return medicineId; }
    public void setMedicineId(long v)          { this.medicineId = v; }

    public String getIngredientName()          { return ingredientName; }
    public void setIngredientName(String v)    { this.ingredientName = v; }

    public String getMedicineName()            { return medicineName; }
    public void setMedicineName(String v)      { this.medicineName = v; }

    @Override
    public String toString() {
        return ingredientName;
    }
}
