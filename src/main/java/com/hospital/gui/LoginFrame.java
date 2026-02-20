package com.hospital.gui;

import com.hospital.bus.AuthBUS;
import com.hospital.model.UserAccount;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Cửa sổ đăng nhập – thiết kế đỏ trắng.
 */
public class LoginFrame extends JFrame {

    private final AuthBUS authBUS = new AuthBUS();

    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JButton        btnLogin;
    private JLabel         lblError;

    // Thông tin tài khoản đã đăng nhập (truyền sang MainFrame)
    private UserAccount loggedInUser;

    public LoginFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng nhập – HOSPITAL-SYSTEM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(false);
        setSize(960, 640);
        setLocationRelativeTo(null);
        setResizable(false);

        // Nền xám
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(240, 241, 245));
        setContentPane(root);

        // ── Card trắng bo góc ────────────────────────────────────────────────
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 14));
                g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 8, 24, 24);
                // White card
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(380, 490));
        card.setBorder(BorderFactory.createEmptyBorder(40, 44, 36, 44));

        // ── Logo icon ────────────────────────────────────────────────────────
        JLabel logoIcon = new JLabel("\uD83E\uDE7A", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Pink circle background
                g2.setColor(new Color(253, 237, 236));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        logoIcon.setForeground(UIConstants.PRIMARY_RED);
        logoIcon.setPreferredSize(new Dimension(72, 72));
        logoIcon.setMaximumSize(new Dimension(72, 72));
        logoIcon.setAlignmentX(CENTER_ALIGNMENT);

        // ── App name ─────────────────────────────────────────────────────────
        JLabel lblAppName = new JLabel("HOSPITAL-SYSTEM", SwingConstants.CENTER);
        lblAppName.setFont(new Font(UIConstants.FONT_NAME, Font.BOLD, 20));
        lblAppName.setForeground(UIConstants.PRIMARY_RED);
        lblAppName.setAlignmentX(CENTER_ALIGNMENT);

        // ── Subtitle ─────────────────────────────────────────────────────────
        JLabel lblSub = new JLabel("Hệ thống quản lý phòng khám", SwingConstants.CENTER);
        lblSub.setFont(UIConstants.FONT_LABEL);
        lblSub.setForeground(UIConstants.TEXT_SECONDARY);
        lblSub.setAlignmentX(CENTER_ALIGNMENT);

        // ── Error label (hidden by default) ──────────────────────────────────
        lblError = new JLabel(" ", SwingConstants.CENTER);
        lblError.setFont(UIConstants.FONT_SMALL);
        lblError.setForeground(UIConstants.PRIMARY_RED);
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        // ── Username field ────────────────────────────────────────────────────
        JLabel lblUser = makeFieldLabel("Tên đăng nhập");

        txtUsername = new JTextField();
        txtUsername.setFont(UIConstants.FONT_LABEL);
        styleField(txtUsername, "👤  Nhập tên đăng nhập");
        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());

        // ── Password field ────────────────────────────────────────────────────
        JLabel lblPass = makeFieldLabel("Mật khẩu");

        txtPassword = new JPasswordField();
        txtPassword.setFont(UIConstants.FONT_LABEL);
        styleField(txtPassword, "🔒  Nhập mật khẩu");
        txtPassword.addActionListener(e -> doLogin());

        // ── Show/hide password toggle ─────────────────────────────────────────
        JPanel passRow = new JPanel(new BorderLayout(0, 0));
        passRow.setOpaque(false);
        passRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        passRow.add(txtPassword, BorderLayout.CENTER);
        JLabel eyeToggle = new JLabel("👁");
        eyeToggle.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        eyeToggle.setForeground(UIConstants.TEXT_SECONDARY);
        eyeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeToggle.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 2));
        eyeToggle.addMouseListener(new MouseAdapter() {
            private boolean shown = false;
            @Override public void mouseClicked(MouseEvent e) {
                shown = !shown;
                txtPassword.setEchoChar(shown ? (char) 0 : '•');
                eyeToggle.setText(shown ? "🙈" : "👁");
            }
        });
        passRow.add(eyeToggle, BorderLayout.EAST);

        // ── Login button ──────────────────────────────────────────────────────
        btnLogin = new JButton("ĐĂNG NHẬP") {
            private Color cur = UIConstants.PRIMARY_RED;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { cur = UIConstants.PRIMARY_RED_DARK; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { cur = UIConstants.PRIMARY_RED;      repaint(); }
                    @Override public void mousePressed(MouseEvent e) { cur = UIConstants.PRIMARY_RED_DARK.darker(); repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cur);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font(UIConstants.FONT_NAME, Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setOpaque(false);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnLogin.setAlignmentX(CENTER_ALIGNMENT);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> doLogin());

        // ── Bottom row: forgot + version ──────────────────────────────────────
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel lblForgot = new JLabel("Quên mật khẩu?");
        lblForgot.setFont(UIConstants.FONT_SMALL);
        lblForgot.setForeground(UIConstants.TEXT_SECONDARY);
        lblForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblForgot.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                        "Vui lòng liên hệ quản trị viên để đặt lại mật khẩu.",
                        "Quên mật khẩu", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JLabel lblVersion = new JLabel("v1.0.0");
        lblVersion.setFont(UIConstants.FONT_SMALL);
        lblVersion.setForeground(UIConstants.TEXT_MUTED);
        lblVersion.setHorizontalAlignment(SwingConstants.RIGHT);

        bottomRow.add(lblForgot,  BorderLayout.WEST);
        bottomRow.add(lblVersion, BorderLayout.EAST);

        // ── Hint accounts ─────────────────────────────────────────────────────
        JPanel hintPanel = createHintPanel();

        // ── Assemble card ─────────────────────────────────────────────────────
        card.add(logoIcon);
        card.add(Box.createVerticalStrut(14));
        card.add(lblAppName);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(6));
        card.add(lblError);
        card.add(Box.createVerticalStrut(10));
        card.add(lblUser);
        card.add(Box.createVerticalStrut(6));
        card.add(wrapField(txtUsername));
        card.add(Box.createVerticalStrut(14));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(6));
        card.add(wrapPassRow(passRow));
        card.add(Box.createVerticalStrut(22));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(14));
        card.add(bottomRow);
        card.add(Box.createVerticalStrut(16));
        card.add(hintPanel);

        // ── Footer ────────────────────────────────────────────────────────────
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);

        JLabel footer = new JLabel("© 2026 HOSPITAL-SYSTEM. Bảo mật và An toàn.", SwingConstants.CENTER);
        footer.setFont(UIConstants.FONT_SMALL);
        footer.setForeground(UIConstants.TEXT_MUTED);
        footer.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        root.add(card, gbc);
        gbc.gridy = 1;
        root.add(footer, gbc);
    }

    // ── Hint panel (tài khoản demo) ───────────────────────────────────────────
    private JPanel createHintPanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(253, 237, 236));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel title = new JLabel("Tài khoản demo:");
        title.setFont(new Font(UIConstants.FONT_NAME, Font.BOLD, 11));
        title.setForeground(UIConstants.PRIMARY_RED);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel acc1 = makeHintLabel("Admin:  username = admin   |   password = admin123");
        JLabel acc2 = makeHintLabel("Bác sĩ: username = doctor  |   password = doctor123");

        p.add(title);
        p.add(Box.createVerticalStrut(4));
        p.add(acc1);
        p.add(Box.createVerticalStrut(2));
        p.add(acc2);

        // Click to autofill admin
        acc1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        acc1.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                txtUsername.setText("admin");
                txtPassword.setText("admin123");
                lblError.setText(" ");
            }
        });
        // Click to autofill doctor
        acc2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        acc2.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                txtUsername.setText("doctor");
                txtPassword.setText("doctor123");
                lblError.setText(" ");
            }
        });
        return p;
    }

    private JLabel makeHintLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font(UIConstants.FONT_NAME, Font.PLAIN, 11));
        lbl.setForeground(new Color(120, 50, 40));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    // ── Helper: style field ───────────────────────────────────────────────────
    private void styleField(JTextField field, String placeholder) {
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(UIConstants.BORDER_COLOR, 8),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setFont(UIConstants.FONT_LABEL);
        field.setForeground(UIConstants.TEXT_PRIMARY);
        field.setBackground(Color.WHITE);
        if (field instanceof JPasswordField pf) {
            pf.setEchoChar('•');
        }
        // Focus border highlight
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(UIConstants.PRIMARY_RED, 8),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(UIConstants.BORDER_COLOR, 8),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
        });
    }

    private JPanel wrapField(JTextField field) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel wrapPassRow(JPanel row) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        outer.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(UIConstants.BORDER_COLOR, 8),
            BorderFactory.createEmptyBorder(0, 0, 0, 8)));
        // Remove individual border from password field inside
        txtPassword.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));
        outer.addFocusListener(new FocusAdapter() {});
        txtPassword.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                outer.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(UIConstants.PRIMARY_RED, 8),
                    BorderFactory.createEmptyBorder(0, 0, 0, 8)));
            }
            @Override public void focusLost(FocusEvent e) {
                outer.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(UIConstants.BORDER_COLOR, 8),
                    BorderFactory.createEmptyBorder(0, 0, 0, 8)));
            }
        });
        outer.setBackground(Color.WHITE);
        outer.setOpaque(true);
        outer.add(row, BorderLayout.CENTER);
        return outer;
    }

    private JLabel makeFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return lbl;
    }

    // ── Login logic ───────────────────────────────────────────────────────────
    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        System.out.println("[LOGIN] doLogin called: user='" + username + "'");

        if (username.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập.");
            txtUsername.requestFocusInWindow();
            return;
        }
        if (password.isEmpty()) {
            showError("Vui lòng nhập mật khẩu.");
            txtPassword.requestFocusInWindow();
            return;
        }

        // Disable button + show loading text
        btnLogin.setText("Đang xác thực...");
        btnLogin.setEnabled(false);

        // Chạy trên background thread để tránh block UI
        SwingWorker<UserAccount, Void> worker = new SwingWorker<>() {
            @Override protected UserAccount doInBackground() {
                System.out.println("[LOGIN] SwingWorker doInBackground start");
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                UserAccount result = authBUS.login(username, password);
                System.out.println("[LOGIN] authBUS.login returned: " + result);
                return result;
            }
            @Override protected void done() {
                System.out.println("[LOGIN] SwingWorker done() called");
                try {
                    UserAccount user = get();
                    System.out.println("[LOGIN] get() returned: " + user);
                    if (user != null) {
                        loggedInUser = user;
                        openMainFrame(user);
                    } else {
                        showError("Sai tên đăng nhập hoặc mật khẩu.");
                        shakeFrame();
                        txtPassword.setText("");
                        txtPassword.requestFocusInWindow();
                        btnLogin.setText("ĐĂNG NHẬP");
                        btnLogin.setEnabled(true);
                    }
                } catch (Exception ex) {
                    System.out.println("[LOGIN] Exception in done(): " + ex);
                    ex.printStackTrace();
                    showError("Lỗi hệ thống: " + ex.getMessage());
                    btnLogin.setText("ĐĂNG NHẬP");
                    btnLogin.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void openMainFrame(UserAccount user) {
        System.out.println("[LOGIN] openMainFrame called for: " + user);
        dispose();
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("[LOGIN] Creating MainFrame...");
                MainFrame frame = new MainFrame(user);
                System.out.println("[LOGIN] MainFrame created, setting visible...");
                frame.setVisible(true);
                System.out.println("[LOGIN] MainFrame is now visible.");
            } catch (Exception ex) {
                System.out.println("[LOGIN] ERROR creating MainFrame: " + ex);
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Lỗi mở trang chính: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setForeground(UIConstants.PRIMARY_RED);
    }

    /** Hiệu ứng rung frame khi đăng nhập sai. */
    private void shakeFrame() {
        Point origin = getLocation();
        Timer timer = new Timer(30, null);
        int[] count = {0};
        int[] offsets = {-8, 8, -6, 6, -4, 4, -2, 2, 0};
        timer.addActionListener(e -> {
            if (count[0] < offsets.length) {
                setLocation(origin.x + offsets[count[0]++], origin.y);
            } else {
                setLocation(origin);
                timer.stop();
            }
        });
        timer.start();
    }

    // ── Inner: RoundBorder ────────────────────────────────────────────────────
    static class RoundBorder extends AbstractBorder {
        private final Color color;
        private final int   radius;
        RoundBorder(Color c, int r) { color = c; radius = r; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Double(x + 0.75, y + 0.75, w - 1.5, h - 1.5, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
    }
}
