package com.hospital.model;

import java.time.LocalDate;

public class Patient extends BaseModel {

    private String fullName;       
    private Gender gender;         
    private LocalDate dateOfBirth;  
    private String phone;           
    private String address;        
    private Long userId;           
    private boolean isActive;       

    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }

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
