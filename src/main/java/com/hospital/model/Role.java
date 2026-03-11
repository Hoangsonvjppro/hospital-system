package com.hospital.model;


public enum Role {

    ADMIN(1, "Quản trị viên"),
    DOCTOR(2, "Bác sĩ"),
    NURSE(3, "Y tá"),
    RECEPTIONIST(4, "Lễ tân"),
    ACCOUNTANT(5, "Kế toán"),
    PATIENT(6, "Bệnh nhân"),
    PHARMACIST(7, "Dược sĩ");

    private final int id;
    private final String displayName;

    Role(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }


    public static Role fromId(int id) {
        for (Role role : values()) {
            if (role.id == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy vai trò với id=" + id);
    }

    public static Role fromName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên vai trò không được để trống");
        }
        return valueOf(name.trim().toUpperCase());
    }
}
