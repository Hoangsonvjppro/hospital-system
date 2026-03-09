package com.hospital.model;

/**
 * Entity bác sĩ — ánh xạ bảng Doctor trong CSDL.
 */
public class Doctor extends BaseModel {
    private long userId;
    private Long specialtyId;
    private String licenseNo;
    private boolean isActive;

    // Transient — populated by JOIN
    private String fullName;
    private String email;
    private String phone;
    private String specialtyName;

    public Doctor() {
        this.isActive = true;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public long getUserId()                  { return userId; }
    public void setUserId(long v)            { this.userId = v; }

    public Long getSpecialtyId()             { return specialtyId; }
    public void setSpecialtyId(Long v)       { this.specialtyId = v; }

    public String getLicenseNo()             { return licenseNo; }
    public void setLicenseNo(String v)       { this.licenseNo = v; }

    public boolean isActive()                { return isActive; }
    public void setActive(boolean v)         { this.isActive = v; }

    public String getFullName()              { return fullName; }
    public void setFullName(String v)        { this.fullName = v; }

    public String getEmail()                 { return email; }
    public void setEmail(String v)           { this.email = v; }

    public String getPhone()                 { return phone; }
    public void setPhone(String v)           { this.phone = v; }

    public String getSpecialtyName()         { return specialtyName; }
    public void setSpecialtyName(String v)   { this.specialtyName = v; }

    @Override
    public String toString() {
        return fullName != null ? fullName : "Doctor#" + id;
    }
}
