package com.hospital.bus;

import com.hospital.dao.AuthDAO;
import com.hospital.model.UserAccount;

/**
 * Business logic xác thực đăng nhập.
 */
public class AuthBUS {

    private final AuthDAO dao = new AuthDAO();

    /**
     * Xác thực đăng nhập.
     *
     * @return UserAccount nếu đúng, null nếu sai username/password.
     */
    public UserAccount login(String username, String password) {
        if (username == null || username.isBlank()) return null;
        if (password == null || password.isEmpty())  return null;
        return dao.login(username.trim(), password);
    }
}
