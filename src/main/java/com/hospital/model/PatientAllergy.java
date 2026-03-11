package com.hospital.model;

import java.time.LocalDateTime;

public class PatientAllergy extends BaseModel {

    public static final String SEVERITY_MILD     = "MILD";
    public static final String SEVERITY_MODERATE = "MODERATE";
    public static final String SEVERITY_SEVERE   = "SEVERE";

    private long patientId;
    private String allergenName;
    private String severity;
    private String reaction;
    private String notes;

    
    public PatientAllergy() {
        this.severity = SEVERITY_MODERATE;
    }

    public PatientAllergy(int id, long patientId, String allergenName, String severity) {
        super(id);
        this.patientId = patientId;
        this.allergenName = allergenName;
        this.severity = severity;
    }

  

    public long getPatientId()                    { return patientId; }
    public void setPatientId(long v)              { this.patientId = v; }

    public String getAllergenName()                { return allergenName; }
    public void setAllergenName(String v)         { this.allergenName = v; }

    public String getSeverity()                   { return severity; }
    public void setSeverity(String v)             { this.severity = v; }

    public String getReaction()                   { return reaction; }
    public void setReaction(String v)             { this.reaction = v; }

    public String getNotes()                      { return notes; }
    public void setNotes(String v)                { this.notes = v; }

    public String getSeverityDisplay() {
        return switch (severity) {
            case SEVERITY_MILD     -> "Nhẹ";
            case SEVERITY_MODERATE -> "Trung bình";
            case SEVERITY_SEVERE   -> "Nặng";
            default                -> severity;
        };
    }

    @Override
    public String toString() {
        return "PatientAllergy{id=" + id
                + ", patientId=" + patientId
                + ", allergenName='" + allergenName + '\''
                + ", severity='" + severity + '\'' + '}';
    }
}
