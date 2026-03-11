package com.hospital.model;

import java.time.LocalDateTime;

public class ServiceOrder extends BaseModel {
    public static final String STATUS_ORDERED    = "ORDERED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED  = "COMPLETED";
    public static final String STATUS_CANCELLED  = "CANCELLED";

    private long   recordId;
    private int    serviceId;
    private String status;
    private LocalDateTime orderedAt;
    private LocalDateTime completedAt;
    private String notes;

    private String serviceName;
    private java.math.BigDecimal price;

    public ServiceOrder() {
        this.status = STATUS_ORDERED;
    }

    public long getRecordId()                         { return recordId; }
    public void setRecordId(long v)                   { this.recordId = v; }

    public int getServiceId()                         { return serviceId; }
    public void setServiceId(int v)                   { this.serviceId = v; }

    public String getStatus()                         { return status; }
    public void setStatus(String v)                   { this.status = v; }

    public LocalDateTime getOrderedAt()               { return orderedAt; }
    public void setOrderedAt(LocalDateTime v)         { this.orderedAt = v; }

    public LocalDateTime getCompletedAt()             { return completedAt; }
    public void setCompletedAt(LocalDateTime v)       { this.completedAt = v; }

    public String getNotes()                          { return notes; }
    public void setNotes(String v)                    { this.notes = v; }

    public String getServiceName()                    { return serviceName; }
    public void setServiceName(String v)              { this.serviceName = v; }

    public java.math.BigDecimal getPrice()            { return price; }
    public void setPrice(java.math.BigDecimal v)      { this.price = v; }

    public String getStatusDisplay() {
        if (status == null) return "";
        return switch (status) {
            case STATUS_ORDERED     -> "Đã yêu cầu";
            case STATUS_IN_PROGRESS -> "Đang thực hiện";
            case STATUS_COMPLETED   -> "Hoàn thành";
            case STATUS_CANCELLED   -> "Đã hủy";
            default                 -> status;
        };
    }

    @Override
    public String toString() {
        return "ServiceOrder{id=" + id
                + ", recordId=" + recordId
                + ", serviceId=" + serviceId
                + ", status='" + status + "'}";
    }
}
