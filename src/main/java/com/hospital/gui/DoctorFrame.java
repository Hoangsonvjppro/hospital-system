package com.hospital.gui;

import com.hospital.model.Account;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Frame chính dành cho Bác sĩ.
 * Doctor main frame — displayed after successful login with DOCTOR role.
 */
public class DoctorFrame extends JFrame {

    private static final Color BG_COLOR      = new Color(0x0D1B2A);
    private static final Color CARD_BG       = new Color(0x1B2838);
    private static final Color ACCENT_COLOR  = new Color(0x1B4332);
    private static final Color PRIMARY_COLOR = new Color(0x2D6A4F);
    private static final Color TEXT_WHITE     = new Color(0xEEEEEE);
    private static final Color TEXT_MUTED     = new Color(0x8899AA);

    private final Account account;

    public DoctorFrame(Account account) {
        this.account = account;
        initFrame();
        initComponents();
    }

    private void initFrame() {
        setTitle("Bác sĩ — Phòng Mạch Tư");
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        // ── Main card ─────────────────────────────────────────
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(50, 60, 50, 60));
        card.setPreferredSize(new Dimension(500, 350));

        // ── Role badge ────────────────────────────────────────
        JLabel badge = new JLabel("BÁC SĨ");
        badge.setFont(new Font("SansSerif", Font.BOLD, 14));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(PRIMARY_COLOR);
        badge.setBorder(new EmptyBorder(6, 20, 6, 20));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(badge);
        card.add(Box.createVerticalStrut(20));

        // ── Icon ──────────────────────────────────────────────
        JLabel icon = new JLabel("🩺");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(icon);
        card.add(Box.createVerticalStrut(16));

        // ── Title ─────────────────────────────────────────────
        JLabel title = new JLabel("Bác sĩ");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));

        // ── Welcome message ───────────────────────────────────
        JLabel welcome = new JLabel("Xin chào, " + account.getFullName());
        welcome.setFont(new Font("SansSerif", Font.PLAIN, 16));
        welcome.setForeground(TEXT_MUTED);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(welcome);
        card.add(Box.createVerticalStrut(24));

        // ── Logout button ─────────────────────────────────────
        JButton btnLogout = createLogoutButton();
        card.add(btnLogout);

        // ── Add card to frame ─────────────────────────────────
        add(card, new GridBagConstraints());
    }

    private JButton createLogoutButton() {
        JButton btn = new JButton("Đăng xuất") {
            private boolean hovering = false;

            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setForeground(Color.WHITE);
                setFont(new Font("SansSerif", Font.BOLD, 14));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setMaximumSize(new Dimension(200, 40));
                setPreferredSize(new Dimension(200, 40));
                setAlignmentX(Component.CENTER_ALIGNMENT);

                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hovering = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hovering = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = hovering ? PRIMARY_COLOR.darker() : ACCENT_COLOR;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        });
        return btn;
    }
}
