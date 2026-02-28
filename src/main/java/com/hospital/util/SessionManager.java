package com.hospital.util;

import com.hospital.model.Account;
import com.hospital.model.Role;

/**
 * Singleton quản lý phiên đăng nhập hiện tại.
 * <p>
 * Lưu thông tin Account + Role của user đang login,
 * để mọi nơi trong ứng dụng có thể truy cập mà không cần truyền tham số.
 * <p>
 * Sử dụng:
 * <pre>
 *   // Sau khi login thành công
 *   SessionManager.getInstance().login(account);
 *
 *   // Lấy thông tin bất kỳ đâu
 *   Account user = SessionManager.getInstance().getCurrentUser();
 *   Role role    = SessionManager.getInstance().getCurrentRole();
 *
 *   // Khi logout
 *   SessionManager.getInstance().logout();
 * </pre>
 */
public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private Account currentUser;
    private Role currentRole;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    // ── Login / Logout ────────────────────────────────────────

    /**
     * Lưu thông tin phiên đăng nhập.
     *
     * @param account tài khoản vừa xác thực thành công
     */
    public void login(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account không được null");
        }
        this.currentUser = account;
        this.currentRole = account.getRole();
    }

    /**
     * Xoá thông tin phiên — gọi khi đăng xuất.
     */
    public void logout() {
        this.currentUser = null;
        this.currentRole = null;
    }

    // ── Getters ───────────────────────────────────────────────

    public Account getCurrentUser() {
        return currentUser;
    }

    public Role getCurrentRole() {
        return currentRole;
    }

    /**
     * Kiểm tra user hiện tại có đúng vai trò không.
     */
    public boolean hasRole(Role role) {
        return currentRole != null && currentRole == role;
    }

    /**
     * Kiểm tra đã đăng nhập chưa.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
