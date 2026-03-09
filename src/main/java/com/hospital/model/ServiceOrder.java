package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Entity yêu cầu dịch vụ — ánh xạ bảng ServiceOrder.
 * Thay thế LabOrder cũ.
 */
public class ServiceOrder extends BaseModel {

    private long recordId;
    private long serviceId;
    private Long orderedBy;
    private LocalDateTime orderedAt;
    private LocalDateTime completedAt;
    private String status; // ORDERED, IN_PROGRESS, COMPLETED, CANCELLED
    private String notes;

    // Transient
    private String serviceName;
    private String serviceType;
    private String patientName;
    private String doctorName;

    public ServiceOrder() {
        this.status = "ORDERED";
    }

    public long getRecordId()                     { return recordId; }
    public void setRecordId(long v)               { this.recordId = v; }

    public long getServiceId()                    { return serviceId; }
    public void setServiceId(long v)              { this.serviceId = v; }

    public Long getOrderedBy()                    { return orderedBy; }
    public void setOrderedBy(Long v)              { this.orderedBy = v; }

    public LocalDateTime getOrderedAt()           { return orderedAt; }
    public void setOrderedAt(LocalDateTime v)     { this.orderedAt = v; }

    public LocalDateTime getCompletedAt()         { return completedAt; }
    public void setCompletedAt(LocalDateTime v)   { this.completedAt = v; }

    public String getStatus()                     { return status; }
    public void setStatus(String v)               { this.status = v; }

    public String getNotes()                      { return notes; }
    public void setNotes(String v)                { this.notes = v; }

    public String getServiceName()                { return serviceName; }
    public void setServiceName(String v)          { this.serviceName = v; }

    public String getServiceType()                { return serviceType; }
    public void setServiceType(String v)          { this.serviceType = v; }

    public String getPatientName()                { return patientName; }
    public void setPatientName(String v)          { this.patientName = v; }

    public String getDoctorName()                 { return doctorName; }
    public void setDoctorName(String v)           { this.doctorName = v; }

    @Override
    public String toString() {
        return "ServiceOrder{id=" + id + ", recordId=" + recordId + ", status='" + status + "'}";
    }
}
