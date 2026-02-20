package com.hospital.model;

/**
 * Entity mẫu — thay thế bằng entity thực tế của bạn.
 * Sample entity — replace with your actual entities.
 *
 * Ví dụ: Patient, Doctor, Department, Medicine, v.v.
 */
public class SampleEntity extends BaseModel {
    private String name;
    private String description;

    public SampleEntity() {
    }

    public SampleEntity(int id, String name, String description) {
        super(id);
        this.name = name;
        this.description = description;
    }

    // Getters & Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "SampleEntity{id=" + id + ", name='" + name + "', description='" + description + "'}";
    }
}
