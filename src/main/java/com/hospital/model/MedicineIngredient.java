package com.hospital.model;

public class MedicineIngredient extends BaseModel {
    private int    medicineId;
    private String ingredientName;

    private String medicineName;

    public MedicineIngredient() {}

    public int getMedicineId()                       { return medicineId; }
    public void setMedicineId(int v)                 { this.medicineId = v; }

    public String getIngredientName()                { return ingredientName; }
    public void setIngredientName(String v)          { this.ingredientName = v; }

    public String getMedicineName()                  { return medicineName; }
    public void setMedicineName(String v)            { this.medicineName = v; }

    @Override
    public String toString() {
        return "MedicineIngredient{id=" + id
                + ", medicineId=" + medicineId
                + ", ingredientName='" + ingredientName + "'}";
    }
}
