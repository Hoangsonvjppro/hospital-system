package com.hospital.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * Entity benh nhan — anh xa bang Patient trong CSDL.
 * Bo sung cac truong workflow (status, examType, arrivalTime, patientCode)
 * phuc vu hang doi kham benh tai DoctorWorkstationPanel.
 * Cac truong workflow la transient (khong luu trong bang Patient).
 */
public class Patient extends BaseModel {

    // -- Truong DB (anh xa bang Patient) --
    private String fullName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String address;
    private Long userId;
    private boolean isActive;

    // -- Truong workflow (in-memory, phuc vu hang doi kham) --
    private String patientCode;   // Ma BN hien thi (vd: "BN001")
    private String status;        // CHO KHAM / DANG KHAM / XONG
    private String examType;      // Loai kham (vd: "Kham tong quat")
    private String arrivalTime;   // Gio den (vd: "08:30")

    public enum Gender {
        MALE("Nam"),
        FEMALE("Nu"),
        OTHER("Khac");

        private final String displayName;

        Gender(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    // -- Constructors --

    public Patient() {
        this.isActive = true;
    }

    public Patient(int id, String fullName, Gender gender, LocalDate dateOfBirth,
                   String phone, String address, Long userId, boolean isActive) {
        super(id);
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.address = address;
        this.userId = userId;
        this.isActive = isActive;
    }

    // -- DB field getters/setters --

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // -- Workflow field getters/setters --

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    // -- Helper methods --

    /**
     * Tinh tuoi dua tren ngay sinh.
     */
    public int getAge() {
        if (dateOfBirth == null) return 0;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    // -- toString --

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", gender=" + gender +
                ", dateOfBirth=" + dateOfBirth +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", userId=" + userId +
                ", isActive=" + isActive +
                '}';
    }
}
