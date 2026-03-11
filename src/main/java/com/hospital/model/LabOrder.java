package com.hospital.model;

import java.time.LocalDateTime;


public class LabOrder extends BaseModel {


    public enum TestType {
        BLOOD("Xét nghiệm máu"),
        URINE("Xét nghiệm nước tiểu"),
        XRAY("Chụp X-quang"),
        ULTRASOUND("Siêu âm"),
        OTHER("Khác");

        private final String displayName;

        TestType(String displayName) { this.displayName = displayName; }

        public String getDisplayName() { return displayName; }

        @Override
        public String toString() { return displayName; }
    }


    public enum LabStatus {
        PENDING("Chờ xử lý"),
        IN_PROGRESS("Đang thực hiện"),
        COMPLETED("Hoàn tất");

        private final String displayName;

        LabStatus(String displayName) { this.displayName = displayName; }

        public String getDisplayName() { return displayName; }

        @Override
        public String toString() { return displayName; }
    }


    private long examinationId;  
    private long patientId;       
    private TestType testType;
    private String testName;
    private LabStatus status;
    private String result;     
    private String notes;
    private LocalDateTime orderedAt;
    private LocalDateTime completedAt;
    private long orderedBy;

    private String patientName;
    private String doctorName;


    public LabOrder() {
        this.status = LabStatus.PENDING;
        this.orderedAt = LocalDateTime.now();
    }

    public LabOrder(long examinationId, long patientId, TestType testType, String testName, long orderedBy) {
        this();
        this.examinationId = examinationId;
        this.patientId = patientId;
        this.testType = testType;
        this.testName = testName;
        this.orderedBy = orderedBy;
    }


    public long getExaminationId()                      { return examinationId; }
    public void setExaminationId(long v)                { this.examinationId = v; }

    public long getPatientId()                          { return patientId; }
    public void setPatientId(long v)                    { this.patientId = v; }

    public TestType getTestType()                       { return testType; }
    public void setTestType(TestType v)                 { this.testType = v; }

    public String getTestName()                         { return testName; }
    public void setTestName(String v)                   { this.testName = v; }

    public LabStatus getStatus()                        { return status; }
    public void setStatus(LabStatus v)                  { this.status = v; }

    public String getResult()                           { return result; }
    public void setResult(String v)                     { this.result = v; }

    public String getNotes()                            { return notes; }
    public void setNotes(String v)                      { this.notes = v; }

    public LocalDateTime getOrderedAt()                 { return orderedAt; }
    public void setOrderedAt(LocalDateTime v)           { this.orderedAt = v; }

    public LocalDateTime getCompletedAt()               { return completedAt; }
    public void setCompletedAt(LocalDateTime v)         { this.completedAt = v; }

    public long getOrderedBy()                          { return orderedBy; }
    public void setOrderedBy(long v)                    { this.orderedBy = v; }

    public String getPatientName()                      { return patientName; }
    public void setPatientName(String v)                { this.patientName = v; }

    public String getDoctorName()                       { return doctorName; }
    public void setDoctorName(String v)                 { this.doctorName = v; }

    @Override
    public String toString() {
        return "LabOrder{id=" + id
                + ", examinationId=" + examinationId
                + ", testType=" + testType
                + ", testName='" + testName + '\''
                + ", status=" + status + '}';
    }
}
