package com.hospital.model;

/**
 * Enum phân quyền — ánh xạ bảng Role trong CSDL.
 * <p>
 * Mỗi giá trị tương ứng với role_id trong bảng Role:
 * 1=ADMIN, 2=DOCTOR, 3=RECEPTIONIST, 4=PHARMACIST, 5=CASHIER
 */
public enum Role {

    ADMIN(1, "Quản trị viên"),
    DOCTOR(2, "Bác sĩ"),
    RECEPTIONIST(3, "Lễ tân"),
    PHARMACIST(4, "Dược sĩ"),
    CASHIER(5, "Thu ngân");

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

    /**
     * Tìm Role theo role_id từ CSDL.
     *
     * @param id role_id
     * @return Role tương ứng
     * @throws IllegalArgumentException nếu id không hợp lệ
     */
    public static Role fromId(int id) {
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
