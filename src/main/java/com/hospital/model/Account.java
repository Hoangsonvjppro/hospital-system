package com.hospital.model;

/**
 * Entity tài khoản đăng nhập — ánh xạ bảng `User` trong CSDL.
 * Account entity — maps to the `User` table in the database.
 *
 * Bảng User:
 *   user_id, username, password_hash, full_name, email, phone,
 *   role_id, is_active, created_at, updated_at
 */
public class Account extends BaseModel {
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String phone;
    private int roleId;
    private boolean isActive;

    // ── Constructors ──────────────────────────────────────────

    public Account() {
    }

    public Account(int id, String username, String passwordHash,
                   String fullName, String email, String phone,
                   int roleId, boolean isActive) {
        super(id);
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.roleId = roleId;
        this.isActive = isActive;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // ── Role helper ───────────────────────────────────────────

    /**
     * Trả về Role enum tương ứng với roleId.
     * Tiện lợi để dùng switch/if thay vì so sánh magic number.
     *
     * @return Role enum
     * @throws IllegalArgumentException nếu roleId không hợp lệ
     */
    public Role getRole() {
        return Role.fromId(roleId);
    }

    // ── toString ──────────────────────────────────────────────

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", roleId=" + roleId +
                ", isActive=" + isActive +
                '}';
    }
}
