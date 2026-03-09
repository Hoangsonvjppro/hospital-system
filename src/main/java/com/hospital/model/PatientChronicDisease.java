package com.hospital.model;

import java.time.LocalDate;

/**
 * Entity bệnh mãn tính — ánh xạ bảng PatientChronicDisease.
 */
public class PatientChronicDisease extends BaseModel {

    private long patientId;
    private String icd10Code;
    private LocalDate diagnosedAt;
    private boolean isActive;
    private String note;

    // Transient
    private String diseaseName;

    public PatientChronicDisease() {
        this.isActive = true;
    }

    public long getPatientId()                 { return patientId; }
    public void setPatientId(long v)           { this.patientId = v; }

    public String getIcd10Code()               { return icd10Code; }
    public void setIcd10Code(String v)         { this.icd10Code = v; }

    public LocalDate getDiagnosedAt()          { return diagnosedAt; }
    public void setDiagnosedAt(LocalDate v)    { this.diagnosedAt = v; }

    public boolean isActive()                  { return isActive; }
    public void setActive(boolean v)           { this.isActive = v; }

    public String getNote()                    { return note; }
    public void setNote(String v)              { this.note = v; }

    public String getDiseaseName()             { return diseaseName; }
    public void setDiseaseName(String v)       { this.diseaseName = v; }

    @Override
    public String toString() {
        return "PatientChronicDisease{icd10='" + icd10Code + "'}";
    }
}
