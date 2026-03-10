package com.hospital.gui;

import com.hospital.gui.common.UIConstants;
import com.hospital.model.Account;
import com.hospital.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Frame nền tảng — sidebar + nội dung.
 * Tất cả các Frame theo vai trò phải kế thừa class này.
 */
public abstract class BaseFrame extends JFrame {

    protected final Account account;
    private final String roleName;
    private final String roleIcon;

    private JPanel contentPanel;
    private JPanel sidebarMenu;
    private JButton activeButton;

    protected BaseFrame(Account account, String roleName, String roleIcon) {
        this.account  = account;
        this.roleName = roleName;
        this.roleIcon = roleIcon;
        initFrame();
        buildUI();
    }

    private void initFrame() {
        setTitle(roleName + " — Phòng Mạch Tư");
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIConstants.CONTENT_BG);
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        add(createSidebar(), BorderLayout.WEST);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIConstants.CONTENT_BG);
        add(contentPanel, BorderLayout.CENTER);

        JPanel defaultPanel = createDefaultPanel();
        if (defaultPanel != null) {
            showPanel(defaultPanel);
        }
    }

    protected abstract void registerMenuItems();
    protected abstract JPanel createDefaultPanel();

    // ══════════════════════════════════════════════════════════
    //  SIDEBAR
    // ══════════════════════════════════════════════════════════

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));
        sidebar.setBackground(UIConstants.SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIConstants.SIDEBAR_SEPARATOR));

        sidebar.add(createSidebarHeader());
        sidebar.add(Box.createVerticalStrut(16));

        sidebarMenu = new JPanel();
        sidebarMenu.setLayout(new BoxLayout(sidebarMenu, BoxLayout.Y_AXIS));
        sidebarMenu.setOpaque(false);

        registerMenuItems();

        sidebar.add(sidebarMenu);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createLogoutButton());

        return sidebar;
    }

    private JPanel createSidebarHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.SIDEBAR_HEADER_BG);
        header.setBorder(new EmptyBorder(24, 20, 24, 20));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JLabel lblIcon = new JLabel(roleIcon);
        lblIcon.setFont(UIConstants.FONT_ICON);
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(lblIcon, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        infoPanel.setOpaque(false);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel lblName = new JLabel(account.getFullName());
        lblName.setFont(UIConstants.FONT_SIDEBAR_HEADER);
        lblName.setForeground(Color.WHITE);
        lblName.setAlignmentX(Component.RIGHT_ALIGNMENT);
        textCol.add(lblName);

        JLabel lblRole = new JLabel(roleName);
        lblRole.setFont(UIConstants.FONT_SIDEBAR_ROLE);
        lblRole.setForeground(new Color(200, 220, 255));
        lblRole.setAlignmentX(Component.RIGHT_ALIGNMENT);
        textCol.add(lblRole);

        infoPanel.add(textCol);
        header.add(infoPanel, BorderLayout.SOUTH);

        return header;
    }

    private JPanel createLogoutButton() {
        JButton btnLogout = createSidebarBtn("\u279C", "Đăng xuất");
        btnLogout.setBackground(UIConstants.LOGOUT_COLOR);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btnLogout.setBackground(UIConstants.LOGOUT_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                btnLogout.setBackground(UIConstants.LOGOUT_COLOR);
            }
        });
        btnLogout.addActionListener(e -> {
            SessionManager.getInstance().logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });

        JPanel logoutWrapper = new JPanel(new BorderLayout());
        logoutWrapper.setOpaque(false);
        logoutWrapper.setBorder(new EmptyBorder(0, 12, 20, 12));
        logoutWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        logoutWrapper.add(btnLogout, BorderLayout.CENTER);
        return logoutWrapper;
    }

    // ══════════════════════════════════════════════════════════
    //  PUBLIC API
    // ══════════════════════════════════════════════════════════

    protected void addMenuItem(String icon, String label, Runnable action) {
        JButton btn = createSidebarBtn(icon, label);
        btn.addActionListener(e -> {
            setActiveButton(btn);
            action.run();
        });
        addSidebarItemToPanel(btn);

        if (activeButton == null) {
            activeButton = btn;
            btn.setBackground(UIConstants.SIDEBAR_BTN_ACTIVE);
            btn.setForeground(UIConstants.SIDEBAR_TEXT_ACTIVE);
        }
    }

    protected void addDisabledMenuItem(String icon, String label) {
        JButton btn = createSidebarBtn(icon, label);
        btn.setEnabled(false);
        btn.setForeground(UIConstants.SIDEBAR_SEPARATOR);
        addSidebarItemToPanel(btn);
    }

    protected void addSeparator() {
        sidebarMenu.add(Box.createVerticalStrut(12));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(UIConstants.SIDEBAR_SEPARATOR);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 20, 0, 20));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        wrapper.add(sep);
        sidebarMenu.add(wrapper);
        sidebarMenu.add(Box.createVerticalStrut(12));
    }

    protected void addSectionLabel(String text) {
        JLabel lbl = new JLabel("    " + text.toUpperCase());
        lbl.setFont(UIConstants.FONT_SIDEBAR_SECTION);
        lbl.setForeground(UIConstants.SIDEBAR_SECTION_TEXT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        sidebarMenu.add(lbl);
        sidebarMenu.add(Box.createVerticalStrut(8));
    }

    public void showPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // ══════════════════════════════════════════════════════════
    //  INTERNAL
    // ══════════════════════════════════════════════════════════

    private JButton createSidebarBtn(String iconText, String labelText) {
        JButton btn = new JButton("") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                        UIConstants.BTN_RADIUS, UIConstants.BTN_RADIUS));
                g2.setColor(getForeground());
                g2.setFont(UIConstants.FONT_SIDEBAR_ICON);
                FontMetrics fmIcon = g2.getFontMetrics();
                int y = (getHeight() + fmIcon.getAscent() - fmIcon.getDescent()) / 2;
                g2.drawString(iconText, 20, y);
                int iconWidth = fmIcon.stringWidth(iconText);
                g2.setFont(UIConstants.FONT_SIDEBAR_BTN);
                FontMetrics fmText = g2.getFontMetrics();
                y = (getHeight() + fmText.getAscent() - fmText.getDescent()) / 2;
                g2.drawString(labelText, 20 + iconWidth + 10, y);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setForeground(UIConstants.SIDEBAR_TEXT);
        btn.setFont(UIConstants.FONT_SIDEBAR_BTN);
        btn.setBackground(UIConstants.SIDEBAR_BTN_DEFAULT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIConstants.SIDEBAR_BTN_H));
        btn.setPreferredSize(new Dimension(200, UIConstants.SIDEBAR_BTN_H));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn != activeButton && btn.isEnabled()) {
                    btn.setBackground(UIConstants.SIDEBAR_BTN_HOVER);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn != activeButton && btn.isEnabled()) {
                    btn.setBackground(UIConstants.SIDEBAR_BTN_DEFAULT);
                }
            }
        });
        return btn;
    }

    private void addSidebarItemToPanel(JButton btn) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(2, 12, 2, 12));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIConstants.SIDEBAR_BTN_H + 4));
        wrapper.add(btn, BorderLayout.CENTER);
        sidebarMenu.add(wrapper);
    }

    private void setActiveButton(JButton btn) {
        if (activeButton != null) {
            activeButton.setBackground(UIConstants.SIDEBAR_BTN_DEFAULT);
            activeButton.setForeground(UIConstants.SIDEBAR_TEXT);
        }
        activeButton = btn;
        btn.setBackground(UIConstants.SIDEBAR_BTN_ACTIVE);
        btn.setForeground(UIConstants.SIDEBAR_TEXT_ACTIVE);
    }
}
