package com.hospital.gui;

import com.hospital.gui.panels.*;
import com.hospital.model.UserAccount;
import com.hospital.model.UserAccount.Role;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Cửa sổ chính của ứng dụng – layout theo thiết kế đỏ-trắng.
 * Sidebar thay đổi theo vai trò người dùng (Admin / Bác sĩ).
 */
public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private JButton activeButton;
    private UserAccount currentUser;

    // ── Menu items theo vai trò: {icon, label} ─────────────────────────────
    private static final String[][] ADMIN_MENU = {
        {"🏠", "Trang chủ"},
        {"👤", "Tiếp nhận"},
        {"💊", "Kho thuốc"},
        {"💳", "Thanh toán"},
    };
    private static final String[][] ADMIN_BOTTOM = {
        {"⚙", "Hệ thống"},
    };

    private static final String[][] DOCTOR_MENU = {
        {"🏥", "Trang chủ"},
        {"🩺", "Khám bệnh"},
        {"📅", "Lịch hẹn"},
    };
    // Bác sĩ không có mục cấu hình
    private static final String[][] DOCTOR_BOTTOM = {};

    /** Constructor mặc định (không đăng nhập). */
    public MainFrame() {
        this(null);
    }

    /** Constructor với tài khoản đã đăng nhập. */
    public MainFrame(UserAccount user) {
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("HOSPITAL-SYSTEM – Quản lý phòng khám");
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        // ── Sidebar ──────────────────────────────────────────────────────────
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // ── Right side: TopBar + Content ─────────────────────────────────────
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setBackground(UIConstants.CONTENT_BG);
        rightPanel.add(createTopBar(),  BorderLayout.NORTH);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIConstants.CONTENT_BG);
        rightPanel.add(contentPanel, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);

        // Default: show trang chủ phù hợp theo vai trò
        boolean isAdmin = (currentUser == null || currentUser.getRole() == Role.ADMIN);
        showPanel(isAdmin ? new DashboardPanel() : new DoctorDashboardPanel());
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UIConstants.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));

        // ─ Logo area ─
        JPanel logoArea = new JPanel(new BorderLayout(0, 2));
        logoArea.setBackground(UIConstants.SIDEBAR_BG);
        logoArea.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));

        JLabel logoIcon = new JLabel("🏥", SwingConstants.CENTER);
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        logoIcon.setForeground(UIConstants.PRIMARY_RED);
        logoIcon.setPreferredSize(new Dimension(44, 44));

        JPanel appNamePanel = new JPanel(new GridLayout(2, 1, 0, 0));
        appNamePanel.setOpaque(false);
        JLabel appName = new JLabel("HOSPITAL");
        appName.setFont(new Font(UIConstants.FONT_NAME, Font.BOLD, 14));
        appName.setForeground(Color.WHITE);
        JLabel appSub = new JLabel("SYSTEM");
        appSub.setFont(new Font(UIConstants.FONT_NAME, Font.PLAIN, 10));
        appSub.setForeground(UIConstants.SIDEBAR_TEXT);
        appNamePanel.add(appName);
        appNamePanel.add(appSub);

        logoArea.add(logoIcon,    BorderLayout.WEST);
        logoArea.add(appNamePanel, BorderLayout.CENTER);

        // ─ Separator ─
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 20));
        sep.setBackground(UIConstants.SIDEBAR_BG);

        sidebar.add(logoArea, BorderLayout.NORTH);

        // ─ Nav items ─
        JPanel navPanel = new JPanel();
        navPanel.setBackground(UIConstants.SIDEBAR_BG);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // Chọn menu theo vai trò
        boolean isAdmin = (currentUser == null || currentUser.getRole() == Role.ADMIN);
        String[][] menuItems  = isAdmin ? ADMIN_MENU   : DOCTOR_MENU;
        String[][] menuBottom = isAdmin ? ADMIN_BOTTOM : DOCTOR_BOTTOM;

        // Category label
        JLabel categoryLbl = new JLabel(isAdmin ? "QUẢN LÝ" : "CHỨC NĂNG");
        categoryLbl.setFont(new Font(UIConstants.FONT_NAME, Font.BOLD, 10));
        categoryLbl.setForeground(new Color(255, 255, 255, 80));
        categoryLbl.setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 0));
        categoryLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        navPanel.add(categoryLbl);

        // Main menu
        for (int i = 0; i < menuItems.length; i++) {
            JButton btn = createNavButton(menuItems[i][0], menuItems[i][1]);
            final int idx = i;
            btn.addActionListener(e -> {
                setActive(btn);
                navigateTo(idx);
            });
            navPanel.add(btn);
            navPanel.add(Box.createVerticalStrut(6));
            if (i == 0) { activeButton = btn; setActive(btn); }
        }

        // Đẩy phần CẤU HÌNH xuống đáy
        navPanel.add(Box.createVerticalGlue());

        // ─ Bottom section (chỉ Admin) ─
        if (menuBottom.length > 0) {
            JLabel catConfig = new JLabel("CẤU HÌNH");
            catConfig.setFont(new Font(UIConstants.FONT_NAME, Font.BOLD, 10));
            catConfig.setForeground(new Color(255, 255, 255, 80));
            catConfig.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 0));
            catConfig.setAlignmentX(Component.LEFT_ALIGNMENT);
            navPanel.add(catConfig);
            for (String[] item : menuBottom) {
                JButton sysBtn = createNavButton(item[0], item[1]);
                sysBtn.addActionListener(e -> { setActive(sysBtn); showPanel(new SystemPanel()); });
                navPanel.add(sysBtn);
                navPanel.add(Box.createVerticalStrut(6));
            }
        }

        sidebar.add(navPanel, BorderLayout.CENTER);

        // ─ User profile ─
        JPanel profileArea = createProfileArea();
        sidebar.add(profileArea, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String icon, String label) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (this == activeButton) {
                    g2.setColor(UIConstants.PRIMARY_RED);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                } else if (getModel().isRollover()) {
                    g2.setColor(UIConstants.SIDEBAR_ITEM_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setLayout(new GridBagLayout());
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH - 20, 42));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        iconLbl.setForeground(UIConstants.SIDEBAR_TEXT);

        JLabel textLbl = new JLabel(label);
        textLbl.setFont(UIConstants.FONT_SIDEBAR);
        textLbl.setForeground(UIConstants.SIDEBAR_TEXT);

        // Icon + text trong panel con, dùng GridBagLayout để căn giữa dọc
        JPanel inner = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        inner.setOpaque(false);
        inner.add(iconLbl);
        inner.add(textLbl);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        btn.add(inner, gbc);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                iconLbl.setForeground(Color.WHITE);
                textLbl.setForeground(Color.WHITE);
                btn.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn != activeButton) {
                    iconLbl.setForeground(UIConstants.SIDEBAR_TEXT);
                    textLbl.setForeground(UIConstants.SIDEBAR_TEXT);
                }
                btn.repaint();
            }
        });

        return btn;
    }

    private void setActive(JButton btn) {
        activeButton = btn;
        // Update icon/text colors for all buttons by repainting
        getContentPane().repaint();
        btn.repaint();
    }

    private JPanel createProfileArea() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(new Color(20, 26, 38));
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // Avatar – dùng chữ tắt từ tài khoản đăng nhập
        String initials = (currentUser != null) ? currentUser.getInitials() : "HS";
        String displayName = (currentUser != null) ? currentUser.getFullName() : "HOSPITAL-SYSTEM";
        String displayRole = (currentUser != null) ? currentUser.getRole().getDisplayName()
                           + (currentUser.getSpecialty().isEmpty() ? "" : " – " + currentUser.getSpecialty())
                           : "";

        JLabel avatar = new JLabel(initials, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.PRIMARY_RED);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font(UIConstants.FONT_NAME, Font.BOLD, 12));
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(36, 36));

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        JLabel name = new JLabel(displayName);
        name.setFont(new Font(UIConstants.FONT_NAME, Font.BOLD, 12));
        name.setForeground(Color.WHITE);
        JLabel role = new JLabel(displayRole);
        role.setFont(UIConstants.FONT_SMALL);
        role.setForeground(UIConstants.SIDEBAR_TEXT);
        info.add(name);
        info.add(role);

        p.add(avatar, BorderLayout.WEST);
        p.add(info,   BorderLayout.CENTER);

        JLabel logout = new JLabel("⏏");
        logout.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        logout.setForeground(UIConstants.SIDEBAR_TEXT);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                String[] options = {"Đăng xuất", "Thoát hẳn", "Hủy"};
                int r = JOptionPane.showOptionDialog(MainFrame.this,
                        "Bạn muốn làm gì?", "Tùy chọn",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);
                if (r == 0) {           // Đăng xuất → về LoginFrame
                    dispose();
                    SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
                } else if (r == 1) {    // Thoát hẳn
                    System.exit(0);
                }
            }
        });
        p.add(logout, BorderLayout.EAST);
        return p;
    }

    // ── TopBar ────────────────────────────────────────────────────────────────
    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(UIConstants.WHITE);
        bar.setPreferredSize(new Dimension(0, UIConstants.TOPBAR_HEIGHT));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR),
            BorderFactory.createEmptyBorder(0, 24, 0, 24)));

        // Left: page title (red bar)
        JPanel leftBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftBar.setOpaque(false);
        JPanel redBar = new JPanel();
        redBar.setBackground(UIConstants.PRIMARY_RED);
        redBar.setPreferredSize(new Dimension(4, 28));
        JLabel pageTitle = new JLabel("  Bảng điều khiển");
        pageTitle.setFont(UIConstants.FONT_SUBTITLE);
        pageTitle.setForeground(UIConstants.TEXT_PRIMARY);
        leftBar.add(redBar);
        leftBar.add(pageTitle);

        // Center: Search
        JTextField searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm bệnh nhân, hồ sơ...");
        searchField.setPreferredSize(new Dimension(300, 34));
        searchField.setMaximumSize(new Dimension(340, 34));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchPanel.setOpaque(false);
        searchPanel.add(searchField);

        // Right: icons
        JPanel rightIcons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightIcons.setOpaque(false);

        JLabel bellIcon = makeTopIcon("🔔");
        JLabel helpIcon = makeTopIcon("❓");
        rightIcons.add(bellIcon);
        rightIcons.add(helpIcon);

        bar.add(leftBar,    BorderLayout.WEST);
        bar.add(searchPanel, BorderLayout.CENTER);
        bar.add(rightIcons,  BorderLayout.EAST);
        return bar;
    }

    private JLabel makeTopIcon(String icon) {
        JLabel lbl = new JLabel(icon, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.setPreferredSize(new Dimension(32, 32));
        return lbl;
    }

    // ── Navigation (role-aware) ────────────────────────────────────────────
    private void navigateTo(int index) {
        boolean isAdmin = (currentUser == null || currentUser.getRole() == Role.ADMIN);
        JPanel panel;
        if (isAdmin) {
            panel = switch (index) {
                case 0 -> new DashboardPanel();
                case 1 -> new ReceptionPanel();
                case 2 -> new MedicinePanel();
                case 3 -> new PaymentPanel();
                default -> new DashboardPanel();
            };
        } else {
            panel = switch (index) {
                case 0 -> new DoctorDashboardPanel();
                case 1 -> new ExaminationPanel();
                case 2 -> new AppointmentPanel();
                default -> new DoctorDashboardPanel();
            };
        }
        showPanel(panel);
    }

    public void showPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
