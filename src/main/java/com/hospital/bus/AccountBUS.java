package com.hospital.bus;

import com.hospital.dao.AccountDAO;
import com.hospital.model.Account;

import java.util.logging.Logger;

/**
 * BUS cho tài khoản — xử lý nghiệp vụ đăng nhập, đăng ký, validate.
 * Account BUS — handles login, registration, and validation logic.
 *
 * Thiết kế:
 * - Kế thừa BaseBUS để có sẵn CRUD (findById, findAll, update, delete).
 * - Dùng AccountDAO (cast từ parent) để gọi các method đặc biệt
 *   như findByUsername(), existsByUsername().
 * - Mật khẩu được lưu và so sánh dạng plaintext (không hash).
 */
public class AccountBUS extends BaseBUS<Account> {

    private static final Logger LOGGER = Logger.getLogger(AccountBUS.class.getName());

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
    protected void validate(Account entity) {
        if (entity.getUsername() == null || entity.getUsername().trim().isEmpty()) {
            throw new com.hospital.exception.BusinessException("Tên đăng nhập không được để trống");
        }
        if (entity.getFullName() == null || entity.getFullName().trim().isEmpty()) {
            throw new com.hospital.exception.BusinessException("Họ tên không được để trống");
        }
        if (entity.getEmail() == null || entity.getEmail().trim().isEmpty()) {
            throw new com.hospital.exception.BusinessException("Email không được để trống");
        }
        if (entity.getPhone() == null || entity.getPhone().trim().isEmpty()) {
            throw new com.hospital.exception.BusinessException("Số điện thoại không được để trống");
        }
        if (entity.getRoleId() <= 0) {
            throw new com.hospital.exception.BusinessException("Tài khoản chưa được phân quyền");
        }
    }

    // ── Đăng nhập ─────────────────────────────────────────────

    /**
     * Xác thực đăng nhập.
     *
     * Flow: DAO lấy Account theo username → BUS so sánh mật khẩu plaintext.
     *
     * @param username tên đăng nhập
     * @param password mật khẩu (plaintext) người dùng nhập
     * @return Account nếu đăng nhập thành công, null nếu thất bại
     */
    public Account login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            LOGGER.warning("Tên đăng nhập không được để trống!");
            return null;
        }
        if (password == null || password.isEmpty()) {
            LOGGER.warning("Mật khẩu không được để trống!");
            return null;
        }

        // findByUsername() đã lọc is_active = TRUE trong DAO
        Account account = accountDAO.findByUsername(username.trim());

        if (account == null) {
            LOGGER.warning("Tên đăng nhập không tồn tại hoặc tài khoản đã bị vô hiệu hoá!");
            return null;
        }

        // So sánh mật khẩu plaintext
        if (!password.equals(account.getPassword())) {
            LOGGER.warning("Mật khẩu không chính xác!");
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
        validate(account);

        // 2. Kiểm tra username trùng
        if (accountDAO.existsByUsername(account.getUsername())) {
            throw new com.hospital.exception.BusinessException("Tên đăng nhập '" + account.getUsername() + "' đã tồn tại");
        }

        // 3. Kiểm tra mật khẩu đủ mạnh
        if (!isStrongPassword(password)) {
            throw new com.hospital.exception.BusinessException("Mật khẩu không đủ mạnh! Yêu cầu ít nhất 8 ký tự, gồm chữ hoa, chữ thường và số.");
        }

        // 4. Gán mật khẩu plaintext vào entity
        account.setPassword(password);

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

    // ── Quản lý tài khoản ─────────────────────────────────────

    /**
     * Bật/tắt trạng thái hoạt động của tài khoản.
     */
    public boolean toggleActive(int accountId) {
        Account acc = accountDAO.findById(accountId);
        if (acc == null) throw new com.hospital.exception.BusinessException("Không tìm thấy tài khoản");
        acc.setActive(!acc.isActive());
        return accountDAO.update(acc);
    }

    /**
     * Reset mật khẩu về giá trị mặc định (password).
     */
    public boolean resetPassword(int accountId) {
        Account acc = accountDAO.findById(accountId);
        if (acc == null) throw new com.hospital.exception.BusinessException("Không tìm thấy tài khoản");
        acc.setPassword("password");
        return accountDAO.update(acc);
    }

    /**
     * Đổi mật khẩu cho tài khoản xác định.
     */
    public boolean changePassword(int accountId, String newPassword) {
        if (!isStrongPassword(newPassword)) {
            throw new com.hospital.exception.BusinessException("Mật khẩu không đủ mạnh! Yêu cầu ít nhất 8 ký tự, gồm chữ hoa, chữ thường và số.");
        }
        Account acc = accountDAO.findById(accountId);
        if (acc == null) throw new com.hospital.exception.BusinessException("Không tìm thấy tài khoản");
        acc.setPassword(newPassword);
        return accountDAO.update(acc);
    }
}
