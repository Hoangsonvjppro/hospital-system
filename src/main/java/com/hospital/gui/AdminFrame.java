package com.hospital.gui;

import com.hospital.gui.panels.DashboardPanel;
import com.hospital.gui.panels.MedicinePanel;
import com.hospital.model.Account;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Frame chính dành cho Admin — giao diện chính của hệ thống.
 * Admin main frame with sidebar navigation and content switching.
 */
public class AdminFrame extends JFrame {

    // ── UI Colors ─────────────────────────────────────────────
    private static final Color SIDEBAR_BG      = new Color(0x1B2A4A);
    private static final Color SIDEBAR_HEADER   = new Color(0x152238);
    private static final Color CONTENT_BG       = new Color(0xF4F7FE);
    private static final Color BTN_DEFAULT      = new Color(0x223558);
    private static final Color BTN_HOVER        = new Color(0x2A4270);
    private static final Color BTN_ACTIVE       = new Color(0x0D6EFD);
    private static final Color TEXT_WHITE        = new Color(0xEEEEEE);
    private static final Color TEXT_MUTED        = new Color(0x8899BB);
    private static final Color LOGOUT_COLOR      = new Color(0xE74C3C);
    private static final Color LOGOUT_HOVER      = new Color(0xC0392B);

    private final Account account;
    private JPanel contentPanel;
    private JButton activeButton;

    public AdminFrame(Account account) {
        this.account = account;
        initFrame();
        initComponents();
    }

    private void initFrame() {
        setTitle("Quản trị viên — Phòng Mạch Tư");
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(CONTENT_BG);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ── Sidebar ──────────────────────────────────────────
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // ── Content area ─────────────────────────────────────
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(CONTENT_BG);
        add(contentPanel, BorderLayout.CENTER);

        // Mặc định hiển thị DashboardPanel
        showPanel(new DashboardPanel(account.getFullName()));
    }

    // ══════════════════════════════════════════════════════════
    //  SIDEBAR
    // ══════════════════════════════════════════════════════════

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(SIDEBAR_BG);

        // ── Header: avatar + tên ─────────────────────────────
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(SIDEBAR_HEADER);
        header.setBorder(new EmptyBorder(24, 20, 24, 20));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JLabel lblIcon = new JLabel("🛡️");
        lblIcon.setFont(new Font("SansSerif", Font.PLAIN, 32));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(lblIcon);
        header.add(Box.createVerticalStrut(8));

        JLabel lblName = new JLabel(account.getFullName());
        lblName.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblName.setForeground(TEXT_WHITE);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(lblName);

        JLabel lblRole = new JLabel("Quản trị viên");
        lblRole.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblRole.setForeground(TEXT_MUTED);
        lblRole.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(lblRole);

        sidebar.add(header);
        sidebar.add(Box.createVerticalStrut(16));

        // ── Menu buttons ─────────────────────────────────────

        // 1. Tổng quan (Dashboard)
        JButton btnDashboard = createSidebarButton("📊  Tổng quan");
        btnDashboard.addActionListener(e -> {
            setActiveButton(btnDashboard);
            showPanel(new DashboardPanel(account.getFullName()));
        });
        addSidebarItem(sidebar, btnDashboard);

        // Đặt Dashboard là active mặc định
        activeButton = btnDashboard;
        btnDashboard.setBackground(BTN_ACTIVE);

        // 2. Kho thuốc (MedicinePanel — TV4 đã hoàn thành)
        JButton btnMedicine = createSidebarButton("💊  Kho thuốc");
        btnMedicine.addActionListener(e -> {
            setActiveButton(btnMedicine);
            showPanel(new MedicinePanel());
        });
        addSidebarItem(sidebar, btnMedicine);

        // ── Separator ────────────────────────────────────────
        sidebar.add(Box.createVerticalStrut(12));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(0x2A4270));
        JPanel sepWrapper = new JPanel(new BorderLayout());
        sepWrapper.setOpaque(false);
        sepWrapper.setBorder(new EmptyBorder(0, 20, 0, 20));
        sepWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sepWrapper.add(sep);
        sidebar.add(sepWrapper);
        sidebar.add(Box.createVerticalStrut(12));

        // ── Label "Sắp ra mắt" ──────────────────────────────
        JLabel lblComingSoon = new JLabel("    SẮP RA MẮT");
        lblComingSoon.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblComingSoon.setForeground(new Color(0x556688));
        lblComingSoon.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblComingSoon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        sidebar.add(lblComingSoon);
        sidebar.add(Box.createVerticalStrut(8));

        // 3. Placeholder: Bệnh nhân (TV1 — chưa merge)
        JButton btnPatient = createSidebarButton("👥  Bệnh nhân");
        btnPatient.setEnabled(false);
        btnPatient.setForeground(new Color(0x556688));
        addSidebarItem(sidebar, btnPatient);

        // 4. Placeholder: Phòng khám Bác sĩ (TV3 — chưa merge)
        JButton btnDoctor = createSidebarButton("🩺  Phòng khám BS");
        btnDoctor.setEnabled(false);
        btnDoctor.setForeground(new Color(0x556688));
        addSidebarItem(sidebar, btnDoctor);

        sidebar.add(Box.createVerticalGlue());

        // ── Logout button ────────────────────────────────────
        JButton btnLogout = createSidebarButton("🚪  Đăng xuất");
        btnLogout.setBackground(LOGOUT_COLOR);
        btnLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogout.setBackground(LOGOUT_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnLogout.setBackground(LOGOUT_COLOR);
            }
        });
        btnLogout.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        });

        JPanel logoutWrapper = new JPanel(new BorderLayout());
        logoutWrapper.setOpaque(false);
        logoutWrapper.setBorder(new EmptyBorder(0, 12, 20, 12));
        logoutWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        logoutWrapper.add(btnLogout, BorderLayout.CENTER);
        sidebar.add(logoutWrapper);

        return sidebar;
    }

    // ══════════════════════════════════════════════════════════
    //  COMPONENT FACTORIES
    // ══════════════════════════════════════════════════════════

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setForeground(TEXT_WHITE);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setBackground(BTN_DEFAULT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(200, 44));
        btn.setBorder(new EmptyBorder(0, 20, 0, 10));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != activeButton && btn.isEnabled()) {
                    btn.setBackground(BTN_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeButton && btn.isEnabled()) {
                    btn.setBackground(BTN_DEFAULT);
                }
            }
        });

        return btn;
    }

    private void addSidebarItem(JPanel sidebar, JButton btn) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(2, 12, 2, 12));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        wrapper.add(btn, BorderLayout.CENTER);
        sidebar.add(wrapper);
    }

    private void setActiveButton(JButton btn) {
        if (activeButton != null) {
            activeButton.setBackground(BTN_DEFAULT);
        }
        activeButton = btn;
        btn.setBackground(BTN_ACTIVE);
    }

    // ══════════════════════════════════════════════════════════
    //  CONTENT MANAGEMENT
    // ══════════════════════════════════════════════════════════

    private void showPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
