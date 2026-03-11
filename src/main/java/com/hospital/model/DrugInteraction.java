package com.hospital.model;


public class DrugInteraction extends BaseModel {

    public static final String LEVEL_MINOR          = "MINOR";
    public static final String LEVEL_MODERATE       = "MODERATE";
    public static final String LEVEL_SEVERE         = "SEVERE";
    public static final String LEVEL_CONTRAINDICATED = "CONTRAINDICATED";

    private int medicineId1;
    private int medicineId2;
    private String severity;
    private String description;
    private String recommendation;

    private String medicineName1;
    private String medicineName2;

    public DrugInteraction() {}

    public DrugInteraction(int medicineId1, int medicineId2, String severity, String description) {
        this.medicineId1 = medicineId1;
        this.medicineId2 = medicineId2;
        this.severity = severity;
        this.description = description;
    }


    public int getMedicineId1()                     { return medicineId1; }
    public void setMedicineId1(int v)               { this.medicineId1 = v; }

    public int getMedicineId2()                     { return medicineId2; }
    public void setMedicineId2(int v)               { this.medicineId2 = v; }

    public String getSeverity()                     { return severity; }
    public void setSeverity(String v)               { this.severity = v; }

    public String getDescription()                  { return description; }
    public void setDescription(String v)            { this.description = v; }

    public String getRecommendation()               { return recommendation; }
    public void setRecommendation(String v)         { this.recommendation = v; }

    public String getMedicineName1()                { return medicineName1; }
    public void setMedicineName1(String v)          { this.medicineName1 = v; }

    public String getMedicineName2()                { return medicineName2; }
    public void setMedicineName2(String v)          { this.medicineName2 = v; }

    public String getSeverityDisplay() {
        return switch (severity != null ? severity : "") {
            case LEVEL_MINOR          -> "Nhẹ";
            case LEVEL_MODERATE       -> "Trung bình";
            case LEVEL_SEVERE         -> "Nghiêm trọng";
            case LEVEL_CONTRAINDICATED -> "Chống chỉ định";
            default                   -> severity;
        };
    }

    @Override
    public String toString() {
        return "DrugInteraction{" +
                "id=" + id +
                ", med1=" + medicineId1 +
                ", med2=" + medicineId2 +
                ", severity=" + severity +
                '}';
    }
}
