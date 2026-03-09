package com.hospital.gui;

import com.hospital.bus.AccountBUS;
import com.hospital.model.Account;
import com.hospital.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Màn hình đăng nhập — Phòng Mạch Tư.
 */
public class LoginFrame extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(LoginFrame.class.getName());

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblError;

    private final AccountBUS accountBUS = new AccountBUS();

    public LoginFrame() {
        initFrame();
        initComponents();
    }

    private void initFrame() {
        setTitle("Đăng nhập — Phòng Mạch Tư");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel lblTitle = new JLabel("Phòng Mạch Tư", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        // Username
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Tài khoản:"), gbc);
        txtUsername = new JTextField(18);
        gbc.gridx = 1;
        panel.add(txtUsername, gbc);

        // Password
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Mật khẩu:"), gbc);
        txtPassword = new JPasswordField(18);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        // Error label
        lblError = new JLabel(" ");
        lblError.setForeground(Color.RED);
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(lblError, gbc);

        // Login button
        btnLogin = new JButton("Đăng nhập");
        gbc.gridy = 4;
        panel.add(btnLogin, gbc);

        btnLogin.addActionListener(e -> performLogin());
        txtPassword.addActionListener(e -> performLogin());

        setContentPane(panel);
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Vui lòng nhập tài khoản và mật khẩu");
            return;
        }

        try {
            Account account = accountBUS.login(username, password);
            if (account != null) {
                SessionManager.getInstance().login(account);
                LOGGER.info("Đăng nhập thành công: " + username);
                dispose();
                // TODO: Open role-based frame (AdminFrame, DoctorFrame, etc.)
                JOptionPane.showMessageDialog(null,
                        "Đăng nhập thành công!\nRole: " + account.getRoleId(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                lblError.setText("Sai tài khoản hoặc mật khẩu");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Lỗi đăng nhập", ex);
            lblError.setText("Lỗi hệ thống: " + ex.getMessage());
        }
    }
}
