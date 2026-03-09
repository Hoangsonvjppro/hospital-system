package com.hospital.model;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Entity bệnh nhân — ánh xạ bảng Patient trong CSDL v4.
 */
public class Patient extends BaseModel {

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

    // -- Trường DB (ánh xạ bảng Patient) --
    private String fullName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String idCard;         // Số CCCD (id_card)
    private String address;
    private String avatarUrl;
    private boolean isActive;

    // -- Trường workflow (transient, phục vụ hàng đợi khám) --
    private String patientCode;
    private String status;
    private long currentRecordId;

    // -- Constructors --

    public Patient() {
        this.isActive = true;
    }

    public Patient(int id, String fullName, Gender gender, LocalDate dateOfBirth,
                   String phone, String idCard, String address, boolean isActive) {
        super(id);
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.idCard = idCard;
        this.address = address;
        this.isActive = isActive;
    }

    // -- DB field getters/setters --

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }

    /** Alias cho getIdCard — tương thích mã cũ dùng getCccd */
    public String getCccd() { return idCard; }
    public void setCccd(String cccd) { this.idCard = cccd; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    // -- Workflow field getters/setters --

    public String getPatientCode() {
        if (patientCode == null || patientCode.isEmpty()) {
            return String.format("BN%03d", id);
        }
        return patientCode;
    }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCurrentRecordId() { return currentRecordId; }
    public void setCurrentRecordId(long currentRecordId) { this.currentRecordId = currentRecordId; }

    // -- Helper methods --

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
                ", idCard='" + idCard + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
