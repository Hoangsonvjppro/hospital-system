package com.hospital.bus;

import com.hospital.dao.AccountDAO;
import com.hospital.model.Account;
import org.mindrot.jbcrypt.BCrypt;

/**
 * BUS cho tài khoản — xử lý nghiệp vụ đăng nhập, đăng ký, validate.
 * Account BUS — handles login, registration, and validation logic.
 *
 * Thiết kế:
 * - Kế thừa BaseBUS để có sẵn CRUD (findById, findAll, update, delete).
 * - Dùng AccountDAO (cast từ parent) để gọi các method đặc biệt
 *   như findByUsername(), existsByUsername().
 * - BCrypt hash/verify password được thực hiện ở đây, KHÔNG ở DAO.
 */
public class AccountBUS extends BaseBUS<Account> {

    // Cast sang AccountDAO để truy cập findByUsername(), existsByUsername()
    private AccountDAO accountDAO;

    public AccountBUS() {
        super(new AccountDAO());
        // parent.dao đã được gán ở BaseBUS constructor
        // cast lại để dùng các method riêng của AccountDAO
        this.accountDAO = (AccountDAO) this.dao;
    }

    // ── Validate ──────────────────────────────────────────────

    @Override
    protected boolean validate(Account entity) {
        if (entity.getUsername() == null || entity.getUsername().trim().isEmpty()) {
            System.err.println("Lỗi: Tên đăng nhập không được để trống!");
            return false;
        }
        if (entity.getFullName() == null || entity.getFullName().trim().isEmpty()) {
            System.err.println("Lỗi: Họ tên không được để trống!");
            return false;
        }
        if (entity.getEmail() == null || entity.getEmail().trim().isEmpty()) {
            System.err.println("Lỗi: Email không được để trống!");
            return false;
        }
        if (entity.getPhone() == null || entity.getPhone().trim().isEmpty()) {
            System.err.println("Lỗi: Số điện thoại không được để trống!");
            return false;
        }
        if (entity.getRoleId() <= 0) {
            System.err.println("Lỗi: Tài khoản chưa được phân quyền!");
            return false;
        }
        return true;
    }

    // ── Đăng nhập ─────────────────────────────────────────────

    /**
     * Xác thực đăng nhập.
     *
     * Flow: DAO lấy Account theo username → BUS so sánh BCrypt hash.
     *
     * @param username tên đăng nhập
     * @param password mật khẩu (plaintext) người dùng nhập
     * @return Account nếu đăng nhập thành công, null nếu thất bại
     */
    public Account login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("Lỗi: Tên đăng nhập không được để trống!");
            return null;
        }
        if (password == null || password.isEmpty()) {
            System.err.println("Lỗi: Mật khẩu không được để trống!");
            return null;
        }

        // findByUsername() đã lọc is_active = TRUE trong DAO
        Account account = accountDAO.findByUsername(username.trim());

        if (account == null) {
            System.err.println("Lỗi: Tên đăng nhập không tồn tại hoặc tài khoản đã bị vô hiệu hoá!");
            return null;
        }

        // So sánh BCrypt hash ở tầng BUS
        if (!BCrypt.checkpw(password, account.getPasswordHash())) {
            System.err.println("Lỗi: Mật khẩu không chính xác!");
            return null;
        }

        return account;
    }

    // ── Đăng ký tài khoản ─────────────────────────────────────

    /**
     * Tạo tài khoản mới với kiểm tra đầy đủ:
     * validate entity → check trùng username → check mật khẩu mạnh → hash → insert.
     *
     * @param account  entity Account (chưa có passwordHash)
     * @param password mật khẩu plaintext người dùng nhập
     * @return true nếu tạo thành công
     */
    public boolean insert(Account account, String password) {
        // 1. Validate entity
        if (!validate(account)) {
            return false;
        }

        // 2. Kiểm tra username trùng
        if (accountDAO.existsByUsername(account.getUsername())) {
            System.err.println("Lỗi: Tên đăng nhập '" + account.getUsername() + "' đã tồn tại!");
            return false;
        }

        // 3. Kiểm tra mật khẩu đủ mạnh
        if (!isStrongPassword(password)) {
            System.err.println("Lỗi: Mật khẩu không đủ mạnh! "
                    + "Yêu cầu ít nhất 8 ký tự, gồm chữ hoa, chữ thường và số.");
            return false;
        }

        // 4. Hash mật khẩu bằng BCrypt rồi gán vào entity
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));
        account.setPasswordHash(hashedPassword);

        // 5. Gọi DAO insert
        return accountDAO.insert(account);
    }

    // ── Tiện ích ──────────────────────────────────────────────

    /**
     * Kiểm tra mật khẩu đủ mạnh.
     * Yêu cầu: ≥ 8 ký tự, có chữ hoa, chữ thường và số.
     *
     * @param password mật khẩu plaintext
     * @return true nếu đủ mạnh
     */
    public boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        // Ít nhất 1 chữ thường, 1 chữ hoa, 1 số
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";
        return password.matches(regex);
    }
}
