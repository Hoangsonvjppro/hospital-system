package com.hospital.model;

import java.time.LocalDate;

/**
 * Model bệnh nhân.
 */
public class Patient extends BaseModel {
    private String patientCode;   // BN001
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;        // Nam / Nữ
    private String phone;
    private String address;
    private String status;        // CHỜ KHÁM / ĐANG KHÁM / XONG
    private String examType;      // Loại khám
    private String arrivalTime;   // Giờ tiếp nhận

    public Patient() {}

    public Patient(int id, String patientCode, String fullName, LocalDate dob,
                   String gender, String phone, String address,
                   String status, String examType, String arrivalTime) {
        super(id);
        this.patientCode = patientCode;
        this.fullName    = fullName;
        this.dateOfBirth = dob;
        this.gender      = gender;
        this.phone       = phone;
        this.address     = address;
        this.status      = status;
        this.examType    = examType;
        this.arrivalTime = arrivalTime;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public String getPatientCode()  { return patientCode; }
    public void setPatientCode(String v) { this.patientCode = v; }

    public String getFullName()     { return fullName; }
    public void setFullName(String v) { this.fullName = v; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate v) { this.dateOfBirth = v; }

    public String getGender()       { return gender; }
    public void setGender(String v) { this.gender = v; }

    public String getPhone()        { return phone; }
    public void setPhone(String v)  { this.phone = v; }

    public String getAddress()      { return address; }
    public void setAddress(String v){ this.address = v; }

    public String getStatus()       { return status; }
    public void setStatus(String v) { this.status = v; }

    public String getExamType()     { return examType; }
    public void setExamType(String v){ this.examType = v; }

    public String getArrivalTime()  { return arrivalTime; }
    public void setArrivalTime(String v) { this.arrivalTime = v; }

    public int getAge() {
        if (dateOfBirth == null) return 0;
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    @Override
    public String toString() {
        return patientCode + " - " + fullName;
    }
}
