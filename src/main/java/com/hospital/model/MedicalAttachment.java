package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Entity đính kèm hồ sơ — ánh xạ bảng MedicalAttachment.
 */
public class MedicalAttachment extends BaseModel {

    private long recordId;
    private Long serviceOrderId;
    private String fileUrl;
    private String fileType;
    private String description;
    private LocalDateTime uploadedAt;

    public MedicalAttachment() {}

    public long getRecordId()                       { return recordId; }
    public void setRecordId(long v)                 { this.recordId = v; }

    public Long getServiceOrderId()                 { return serviceOrderId; }
    public void setServiceOrderId(Long v)           { this.serviceOrderId = v; }

    public String getFileUrl()                      { return fileUrl; }
    public void setFileUrl(String v)                { this.fileUrl = v; }

    public String getFileType()                     { return fileType; }
    public void setFileType(String v)               { this.fileType = v; }

    public String getDescription()                  { return description; }
    public void setDescription(String v)            { this.description = v; }

    public LocalDateTime getUploadedAt()            { return uploadedAt; }
    public void setUploadedAt(LocalDateTime v)      { this.uploadedAt = v; }

    @Override
    public String toString() {
        return "MedicalAttachment{id=" + id + ", file='" + fileUrl + "'}";
    }
}
