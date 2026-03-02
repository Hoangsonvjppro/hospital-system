package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Model kết quả xét nghiệm (Lab Result).
 */
public class LabResult extends BaseModel {

    private long recordId;
    private Long serviceOrderId;
    private String testName;
    private String resultValue;
    private String normalRange;
    private String unit;
    private LocalDateTime testDate;
    private String notes;

    // ── Constructors ─────────────────────────────────────────

    public LabResult() {}

    public LabResult(int id, long recordId, String testName, LocalDateTime testDate) {
        super(id);
        this.recordId = recordId;
        this.testName = testName;
        this.testDate = testDate;
    }

    // ── Getters & Setters ────────────────────────────────────

    public long getRecordId()                     { return recordId; }
    public void setRecordId(long v)               { this.recordId = v; }

    public Long getServiceOrderId()               { return serviceOrderId; }
    public void setServiceOrderId(Long v)         { this.serviceOrderId = v; }

    public String getTestName()                   { return testName; }
    public void setTestName(String v)             { this.testName = v; }

    public String getResultValue()                { return resultValue; }
    public void setResultValue(String v)          { this.resultValue = v; }

    public String getNormalRange()                { return normalRange; }
    public void setNormalRange(String v)          { this.normalRange = v; }

    public String getUnit()                       { return unit; }
    public void setUnit(String v)                 { this.unit = v; }

    public LocalDateTime getTestDate()            { return testDate; }
    public void setTestDate(LocalDateTime v)      { this.testDate = v; }

    public String getNotes()                      { return notes; }
    public void setNotes(String v)                { this.notes = v; }

    @Override
    public String toString() {
        return "LabResult{id=" + id
                + ", recordId=" + recordId
                + ", testName='" + testName + '\''
                + ", resultValue='" + resultValue + '\''
                + ", testDate=" + testDate + '}';
    }
}
