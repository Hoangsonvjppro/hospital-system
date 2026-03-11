package com.hospital.model;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;


public class Patient extends BaseModel {


    public enum PatientType {
        FIRST_VISIT("Khám lần đầu"),
        REVISIT("Tái khám"),
        EMERGENCY("Cấp cứu");

        private final String displayName;

        PatientType(String displayName) {
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

    private String fullName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String address;
    private Long userId;
    private boolean isActive;
    private String cccd;           
    private String allergyHistory;
    private String notes;          
    private PatientType patientType;

    private String patientCode; 
    private String status;       
    private String examType;     
    private String arrivalTime;   
    private long currentRecordId; 
    public enum Gender {
        MALE("Nam"),
        FEMALE("Nữ"),
        OTHER("Khác");

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
        this.patientType = PatientType.FIRST_VISIT;
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
        this.patientType = PatientType.FIRST_VISIT;
    }


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

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getAllergyHistory() {
        return allergyHistory;
    }

    public void setAllergyHistory(String allergyHistory) {
        this.allergyHistory = allergyHistory;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public PatientType getPatientType() {
        return patientType;
    }

    public void setPatientType(PatientType patientType) {
        this.patientType = patientType;
    }


    public String getPatientCode() {
        if (patientCode == null || patientCode.isEmpty()) {
            return String.format("BN%03d", id);
        }
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

    public long getCurrentRecordId() {
        return currentRecordId;
    }

    public void setCurrentRecordId(long currentRecordId) {
        this.currentRecordId = currentRecordId;
    }

  
    public int getAge() {
        if (dateOfBirth == null) return 0;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return id == patient.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", gender=" + gender +
                ", dateOfBirth=" + dateOfBirth +
                ", phone='" + phone + '\'' +
                ", cccd='" + cccd + '\'' +
                ", patientType=" + patientType +
                ", isActive=" + isActive +
                '}';
    }
}
