package com.hospital.gui;

import com.hospital.model.Account;
import com.hospital.gui.panels.DoctorWorkstationPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Frame chính dành cho Bác sĩ.
 * Doctor main frame — displayed after successful login with DOCTOR role.
 */
public class DoctorFrame extends JFrame {

    private final Account account;

    public DoctorFrame(Account account) {
        this.account = account;
        initFrame();
        initComponents();
    }

    private void initFrame() {
        setTitle("Doctor Workstation — Phòng Mạch Tư");
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 247, 251));
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));

        // ── Top bar ───────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(10, 20, 10, 20)));

        // Left: title
        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftTop.setOpaque(false);
        JLabel titleLabel = new JLabel("Doctor Workstation");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(new Color(30, 41, 59));
        leftTop.add(titleLabel);
        topBar.add(leftTop, BorderLayout.WEST);

        // Right: doctor info + logout
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightTop.setOpaque(false);

        JLabel doctorName = new JLabel(account.getFullName());
        doctorName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        doctorName.setForeground(new Color(30, 41, 59));
        rightTop.add(doctorName);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnLogout.setForeground(new Color(100, 116, 139));
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setToolTipText("Đăng xuất");
        btnLogout.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        });
        rightTop.add(btnLogout);
        topBar.add(rightTop, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // ── Doctor Workstation Panel ──────────────────────
        add(new DoctorWorkstationPanel(), BorderLayout.CENTER);
    }
}
