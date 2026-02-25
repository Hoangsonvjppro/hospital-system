package com.hospital.dao;

import com.hospital.model.UserAccount;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO xác thực tài khoản – mock data với 2 tài khoản mặc định.
 */
public class AuthDAO {

    private static final List<UserAccount> ACCOUNTS = new ArrayList<>();

    static {
        // ── Tài khoản Admin ───────────────────────────────────────────────
        ACCOUNTS.add(new UserAccount(
                "admin",
                "admin123",
                "Nguyễn Quản Trị",
                "Quản trị hệ thống",
                UserAccount.Role.ADMIN));

        // ── Tài khoản Bác sĩ ─────────────────────────────────────────────
        ACCOUNTS.add(new UserAccount(
                "doctor",
                "doctor123",
                "Dr. Nguyễn Văn A",
                "Khoa Nội tổng quát",
                UserAccount.Role.DOCTOR));
    }

    /** Đăng nhập – trả về tài khoản nếu đúng, null nếu sai. */
    public UserAccount login(String username, String password) {
        return ACCOUNTS.stream()
                .filter(a -> a.getUsername().equalsIgnoreCase(username)
                          && a.getPassword().equals(password)
                          && a.isActive())
                .findFirst()
                .orElse(null);
    }

    public List<UserAccount> findAll() {
        return new ArrayList<>(ACCOUNTS);
    }
}
