package com.hospital.model;

/**
 * Entity tài khoản đăng nhập — ánh xạ bảng `User` trong CSDL.
 *
 * Bảng User:
 *   user_id, username, password, full_name, email, phone,
 *   role_id, is_active, created_at, updated_at
 */
public class Account extends BaseModel {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private int roleId;
    private boolean isActive;

    // ── Constructors ──────────────────────────────────────────

    public Account() {
    }

    public Account(int id, String username, String password,
                   String fullName, String email, String phone,
                   int roleId, boolean isActive) {
        super(id);
        this.username = username;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
