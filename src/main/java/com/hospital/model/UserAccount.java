package com.hospital.model;

/**
 * Model tài khoản người dùng.
 */
public class UserAccount {

    public enum Role {
        ADMIN("Quản trị viên"),
        DOCTOR("Bác sĩ");

        private final String displayName;
        Role(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    private String username;
    private String password;
    private String fullName;
    private String specialty;   // Chuyên khoa (dành cho bác sĩ)
    private Role   role;
    private boolean active;

    public UserAccount(String username, String password, String fullName,
                       String specialty, Role role) {
        this.username  = username;
        this.password  = password;
        this.fullName  = fullName;
        this.specialty = specialty;
        this.role      = role;
        this.active    = true;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String  getUsername()  { return username; }
    public String  getPassword()  { return password; }
    public String  getFullName()  { return fullName; }
    public String  getSpecialty() { return specialty; }
    public Role    getRole()      { return role; }
    public boolean isActive()     { return active; }
    public void    setActive(boolean v) { this.active = v; }

    /** Tạo chữ viết tắt tên (avatar). */
    public String getInitials() {
        String[] parts = fullName.split(" ");
        if (parts.length >= 2) {
            return String.valueOf(parts[parts.length - 2].charAt(0))
                 + parts[parts.length - 1].charAt(0);
        }
        return fullName.substring(0, Math.min(2, fullName.length())).toUpperCase();
    }

    @Override
    public String toString() { return fullName + " (" + role.getDisplayName() + ")"; }
}
