package com.hospital.model;

/**
 * Entity chuyên khoa — ánh xạ bảng Specialty.
 */
public class Specialty extends BaseModel {

    private String specialtyName;

    public Specialty() {}

    public Specialty(int id, String specialtyName) {
        super(id);
        this.specialtyName = specialtyName;
    }

    public String getSpecialtyName() { return specialtyName; }
    public void setSpecialtyName(String v) { this.specialtyName = v; }

    @Override
    public String toString() { return specialtyName; }
}
