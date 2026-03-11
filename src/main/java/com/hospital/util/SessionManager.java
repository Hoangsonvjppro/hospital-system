package com.hospital.util;

import com.hospital.model.Account;
import com.hospital.model.Role;

public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private Account currentUser;
    private Role currentRole;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void login(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account không được null");
        }
        this.currentUser = account;
        this.currentRole = account.getRole();
    }
    public void logout() {
        this.currentUser = null;
        this.currentRole = null;
    }

    public Account getCurrentUser() {
        return currentUser;
    }

    public Role getCurrentRole() {
        return currentRole;
    }

    public boolean hasRole(Role role) {
        return currentRole != null && currentRole == role;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
