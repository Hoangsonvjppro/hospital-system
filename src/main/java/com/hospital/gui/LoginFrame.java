package com.hospital.gui;

import com.hospital.bus.AccountBUS;
import com.hospital.model.Account;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Màn hình đăng nhập — Phòng Mạch Tư.
 * Login screen for the Hospital Management System.
 */
public class LoginFrame extends JFrame {

    // ── UI Colors ─────────────────────────────────────────────
    private static final Color BG_COLOR        = new Color(0xEEF2F7);
    private static final Color CARD_BG         = Color.WHITE;
    private static final Color PRIMARY_COLOR   = new Color(0x2196F3);
    private static final Color PRIMARY_HOVER   = new Color(0x1976D2);
    private static final Color TEXT_PRIMARY     = new Color(0x333333);
    private static final Color TEXT_SECONDARY   = new Color(0x888888);
    private static final Color ERROR_COLOR      = new Color(0xE53935);
    private static final Color FIELD_BG         = new Color(0xF5F7FA);
    private static final Color FIELD_BORDER     = new Color(0xDDE2E8);
    private static final Color FIELD_FOCUS      = PRIMARY_COLOR;
    private static final Color ICON_COLOR       = new Color(0xB0BEC5);

    // ── Components ────────────────────────────────────────────
    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JButton        btnLogin;
    private JLabel         lblUsernameError;
    private JLabel         lblPasswordError;
    private JLabel         lblGeneralError;

    // ── BUS ───────────────────────────────────────────────────
    private final AccountBUS accountBUS = new AccountBUS();

    public LoginFrame() {
        initFrame();
        initComponents();
        setupKeyBindings();
    }

    // ══════════════════════════════════════════════════════════
    //  FRAME SETUP
    // ══════════════════════════════════════════════════════════

    private void initFrame() {
        setTitle("Đăng nhập — Phòng Mạch Tư");
        setSize(830, 580);
        setMinimumSize(new Dimension(600, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_COLOR);
    }

    // ══════════════════════════════════════════════════════════
    //  UI BUILDING
    // ══════════════════════════════════════════════════════════

    private void initComponents() {
        setLayout(new GridBagLayout());

        // ── Card Panel (rounded white card) ───────────────────
        JPanel card = new RoundedPanel(24);
        card.setBackground(CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 50, 35, 50));
        card.setPreferredSize(new Dimension(420, 460));

        // ── Icon ──────────────────────────────────────────────
        JLabel iconLabel = createIconLabel();
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(12));

        // ── Title ─────────────────────────────────────────────
        JLabel title = new JLabel("Phòng Mạch Tư");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(PRIMARY_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(4));

        JLabel subtitle = new JLabel("Hệ thống quản lý phòng khám");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(28));

        // ── General error label ───────────────────────────────
        lblGeneralError = new JLabel(" ");
        lblGeneralError.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblGeneralError.setForeground(ERROR_COLOR);
        lblGeneralError.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblGeneralError.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        card.add(lblGeneralError);
        card.add(Box.createVerticalStrut(4));

        // ── Username field ────────────────────────────────────
        JLabel lblUsername = createFieldLabel("Tên đăng nhập");
        card.add(lblUsername);
        card.add(Box.createVerticalStrut(6));

        txtUsername = new JTextField();
        txtUsername.putClientProperty("JTextField.placeholderText", "Nhập tên đăng nhập");
        JPanel usernameField = createInputField(txtUsername, "\uD83D\uDC64");
        card.add(usernameField);

        lblUsernameError = createErrorLabel();
        card.add(lblUsernameError);
        card.add(Box.createVerticalStrut(8));

        // ── Password field ────────────────────────────────────
        JLabel lblPassword = createFieldLabel("Mật khẩu");
        card.add(lblPassword);
        card.add(Box.createVerticalStrut(6));

        txtPassword = new JPasswordField();
        txtPassword.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu");
        JPanel passwordField = createInputField(txtPassword, "\uD83D\uDD12");
        card.add(passwordField);

        lblPasswordError = createErrorLabel();
        card.add(lblPasswordError);
        card.add(Box.createVerticalStrut(18));

        // ── Login button ──────────────────────────────────────
        btnLogin = createLoginButton();
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(14));

        // ── Forgot password link ──────────────────────────────
        JLabel forgotLink = new JLabel("Quên mật khẩu?");
        forgotLink.setFont(new Font("SansSerif", Font.PLAIN, 13));
        forgotLink.setForeground(TEXT_SECONDARY);
        forgotLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        forgotLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                        "Vui lòng liên hệ quản trị viên để đặt lại mật khẩu.",
                        "Quên mật khẩu", JOptionPane.INFORMATION_MESSAGE);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                forgotLink.setForeground(PRIMARY_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                forgotLink.setForeground(TEXT_SECONDARY);
            }
        });
        card.add(forgotLink);

        // ── Add card to frame ─────────────────────────────────
        add(card, new GridBagConstraints());

        // ── Footer ────────────────────────────────────────────
        JLabel footer = new JLabel("© 2024 Phòng Mạch Tư, Version 2.4.1");
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footer.setForeground(new Color(0xB0B0B0));
        footer.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints footerGbc = new GridBagConstraints();
        footerGbc.gridy = 1;
        footerGbc.insets = new Insets(20, 0, 15, 0);
        add(footer, footerGbc);
    }

    // ══════════════════════════════════════════════════════════
    //  COMPONENT FACTORIES
    // ══════════════════════════════════════════════════════════

    /**
     * Tạo icon tròn phía trên tiêu đề.
     */
    private JLabel createIconLabel() {
        JLabel icon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Circle background
                int size = Math.min(getWidth(), getHeight());
                g2.setColor(new Color(0xE3F2FD));
                g2.fillOval((getWidth() - size) / 2, (getHeight() - size) / 2, size, size);
                // Cross icon
                g2.setColor(PRIMARY_COLOR);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int arm = 10;
                g2.drawLine(cx, cy - arm, cx, cy + arm);
                g2.drawLine(cx - arm, cy, cx + arm, cy);
                // Small bag outline
                int bagW = 24, bagH = 18;
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(cx - bagW / 2, cy - bagH / 2 + 2, bagW, bagH, 6, 6);
                // Handle
                g2.drawArc(cx - 7, cy - bagH / 2 - 5, 14, 12, 0, 180);
                g2.dispose();
            }
        };
        icon.setPreferredSize(new Dimension(60, 60));
        icon.setMaximumSize(new Dimension(60, 60));
        return icon;
    }

    /**
     * Nhãn tiêu đề trường nhập liệu.
     */
    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return lbl;
    }

    /**
     * Nhãn hiển thị lỗi dưới trường nhập.
     */
    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(ERROR_COLOR);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        return lbl;
    }

    /**
     * Tạo ô nhập liệu có icon bên trái và bo tròn.
     */
    private JPanel createInputField(JComponent field, String iconText) {
        JPanel wrapper = new RoundedPanel(12) {
            private boolean focused = false;

            {
                setBackground(FIELD_BG);
                setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(12, FIELD_BORDER),
                        new EmptyBorder(0, 12, 0, 12)
                ));
                setLayout(new BorderLayout(8, 0));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
                setPreferredSize(new Dimension(320, 44));
                setAlignmentX(Component.CENTER_ALIGNMENT);

                // Icon
                JLabel icon = new JLabel(iconText);
                icon.setFont(new Font("SansSerif", Font.PLAIN, 16));
                icon.setForeground(ICON_COLOR);
                icon.setPreferredSize(new Dimension(24, 24));
                add(icon, BorderLayout.WEST);

                // Field styling
                field.setOpaque(false);
                field.setBorder(null);
                field.setFont(new Font("SansSerif", Font.PLAIN, 14));
                if (field instanceof JTextField) {
                    ((JTextField) field).setCaretColor(TEXT_PRIMARY);
                }
                add(field, BorderLayout.CENTER);

                // Focus highlighting
                field.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        focused = true;
                        setBorder(BorderFactory.createCompoundBorder(
                                new RoundedBorder(12, FIELD_FOCUS),
                                new EmptyBorder(0, 12, 0, 12)
                        ));
                        repaint();
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        focused = false;
                        setBorder(BorderFactory.createCompoundBorder(
                                new RoundedBorder(12, FIELD_BORDER),
                                new EmptyBorder(0, 12, 0, 12)
                        ));
                        repaint();
                    }
                });
            }
        };
        return wrapper;
    }

    /**
     * Nút đăng nhập bo tròn, màu xanh gradient.
     */
    private JButton createLoginButton() {
        JButton btn = new JButton("Đăng nhập") {
            private boolean hovering = false;

            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setForeground(Color.WHITE);
                setFont(new Font("SansSerif", Font.BOLD, 15));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
                setPreferredSize(new Dimension(320, 44));
                setAlignmentX(Component.CENTER_ALIGNMENT);

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovering = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovering = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = hovering ? PRIMARY_HOVER : PRIMARY_COLOR;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.addActionListener(e -> doLogin());
        return btn;
    }

    // ══════════════════════════════════════════════════════════
    //  KEY BINDINGS
    // ══════════════════════════════════════════════════════════

    private void setupKeyBindings() {
        // Enter key triggers login from any field
        ActionMap am = getRootPane().getActionMap();
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "doLogin");
        am.put("doLogin", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
    }

    // ══════════════════════════════════════════════════════════
    //  VALIDATION & LOGIN
    // ══════════════════════════════════════════════════════════

    /**
     * Validate form → gọi AccountBUS.login() → mở MainFrame nếu thành công.
     */
    private void doLogin() {
        // Reset errors
        lblUsernameError.setText(" ");
        lblPasswordError.setText(" ");
        lblGeneralError.setText(" ");

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        boolean valid = true;

        // ── Client-side validation ────────────────────────────
        if (username.isEmpty()) {
            lblUsernameError.setText("Vui lòng nhập tên đăng nhập");
            valid = false;
        }
        if (password.isEmpty()) {
            lblPasswordError.setText("Vui lòng nhập mật khẩu");
            valid = false;
        }

        if (!valid) {
            return;
        }

        // ── Disable UI during login ───────────────────────────
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // Gọi BUS trên SwingWorker để không block EDT
        new SwingWorker<Account, Void>() {
            @Override
            protected Account doInBackground() {
                return accountBUS.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    Account account = get();
                    if (account != null) {
                        // Đăng nhập thành công → mở MainFrame
                        dispose();
                        SwingUtilities.invokeLater(() -> {
                            MainFrame mainFrame = new MainFrame();
                            mainFrame.setVisible(true);
                        });
                    } else {
                        lblGeneralError.setText("Tên đăng nhập hoặc mật khẩu không đúng!");
                    }
                } catch (Exception ex) {
                    lblGeneralError.setText("Lỗi kết nối! Vui lòng thử lại sau.");
                    ex.printStackTrace();
                } finally {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Đăng nhập");
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    // ══════════════════════════════════════════════════════════
    //  CUSTOM PAINTING HELPERS
    // ══════════════════════════════════════════════════════════

    /**
     * JPanel bo tròn góc.
     */
    private static class RoundedPanel extends JPanel {
        private final int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Border bo tròn góc.
     */
    private static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, width - 1, height - 1, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 0);
        }
    }
}
