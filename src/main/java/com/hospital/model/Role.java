package com.hospital.model;

/**
 * Enum phân quyền — ánh xạ bảng Role trong CSDL.
 * <p>
 * Mỗi giá trị tương ứng với role_id trong bảng Role:
 * 1=ADMIN, 2=DOCTOR, 3=NURSE, 4=RECEPTIONIST, 5=ACCOUNTANT, 6=PATIENT, 7=PHARMACIST
 */
public enum Role {

    ADMIN(1, "Quản trị viên"),
    DOCTOR(2, "Bác sĩ"),
    NURSE(3, "Y tá"),
    RECEPTIONIST(4, "Lễ tân"),
    ACCOUNTANT(5, "Kế toán"),
    PATIENT(6, "Bệnh nhân"),
    PHARMACIST(7, "Dược sĩ");

    private final long id;
    private final String displayName;

    Role(long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Tìm Role theo role_id từ CSDL.
     *
     * @param id role_id
     * @return Role tương ứng
     * @throws IllegalArgumentException nếu id không hợp lệ
     */
    public static Role fromId(long id) {
        for (Role role : values()) {
            if (role.id == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy vai trò với id=" + id);
    }

    /**
     * Tìm Role theo tên (role_name trong DB).
     *
     * @param name tên role (VD: "ADMIN", "DOCTOR")
     * @return Role tương ứng
     * @throws IllegalArgumentException nếu tên không hợp lệ
     */
    public static Role fromName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên vai trò không được để trống");
        }
        return valueOf(name.trim().toUpperCase());
    }
}
