package com.hospital.gui;

import com.hospital.bus.QueueBUS;
import com.hospital.model.Account;
import com.hospital.config.DatabaseConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.Vector;

/**
 * Frame chính dành cho Lễ tân.
 * Receptionist main frame — displayed after successful login with RECEPTIONIST role.
 */
public class ReceptionistFrame extends JFrame {

    private static final Color BG_COLOR      = new Color(0x1A1A2E);
    private static final Color CARD_BG       = new Color(0x22223B);
    private static final Color ACCENT_COLOR  = new Color(0x4A4E69);
    private static final Color PRIMARY_COLOR = new Color(0xF2A65A);
    private static final Color TEXT_WHITE    = new Color(0xEEEEEE);
    private static final Color TEXT_MUTED    = new Color(0x8899AA);

    private final Account account;
    private final QueueBUS queueBUS = new QueueBUS();

    public ReceptionistFrame(Account account) {
        this.account = account;
        initFrame();
        initComponents();
    }

    private void initFrame() {
        setTitle("Lễ tân — Phòng Mạch Tư");
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

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
        card.setPreferredSize(new Dimension(500, 400));

        JLabel badge = new JLabel("LỄ TÂN");
        badge.setFont(new Font("SansSerif", Font.BOLD, 14));
        badge.setForeground(new Color(0x1A1A2E));
        badge.setOpaque(true);
        badge.setBackground(PRIMARY_COLOR);
        badge.setBorder(new EmptyBorder(6, 20, 6, 20));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(badge);
        card.add(Box.createVerticalStrut(20));

        JLabel icon = new JLabel("🏥");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(icon);
        card.add(Box.createVerticalStrut(16));

        JLabel title = new JLabel("Lễ tân");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));

        JLabel welcome = new JLabel("Xin chào, " + account.getFullName());
        welcome.setFont(new Font("SansSerif", Font.PLAIN, 16));
        welcome.setForeground(TEXT_MUTED);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(welcome);
        card.add(Box.createVerticalStrut(24));

        // ================== NÚT CHỌN BỆNH NHÂN ==================
        JButton btnSelectPatient = new JButton("Chọn bệnh nhân đưa vào phòng khám");
        btnSelectPatient.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSelectPatient.setMaximumSize(new Dimension(300, 40));
        btnSelectPatient.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnSelectPatient.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnSelectPatient.addActionListener(e -> openPatientSelectionDialog());

        card.add(btnSelectPatient);
        card.add(Box.createVerticalStrut(20));
        // ==========================================================

        JButton btnLogout = createLogoutButton();
        card.add(btnLogout);

        add(card, new GridBagConstraints());
    }

    private void openPatientSelectionDialog() {
        JDialog dialog = new JDialog(this, "Danh sách bệnh nhân", true);
        dialog.setSize(700, 400);
        dialog.setLocationRelativeTo(this);

        Vector<String> columnNames = new Vector<>();
        columnNames.add("ID");
        columnNames.add("Họ tên");
        columnNames.add("Giới tính");
        columnNames.add("Ngày sinh");
        columnNames.add("SĐT");

        Vector<Vector<Object>> data = new Vector<>();

        try {
            Connection conn = DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT patient_id, full_name, gender, date_of_birth, phone FROM Patient WHERE is_active = TRUE";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getLong("patient_id"));
                row.add(rs.getString("full_name"));
                row.add(rs.getString("gender"));
                row.add(rs.getDate("date_of_birth"));
                row.add(rs.getString("phone"));
                data.add(row);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách bệnh nhân!");
            return;
        }
        JTable table = new JTable(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);

        JButton btnAddToRoom = new JButton("Đưa vào phòng khám");

        btnAddToRoom.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn bệnh nhân!");
                return;
            }

            long patientId = (long) table.getValueAt(selectedRow, 0);
            Object patientName = table.getValueAt(selectedRow, 1);

            try {
                // Tìm doctor_id đầu tiên từ DB
                long doctorId = findFirstDoctorId();
                queueBUS.enqueue(patientId, doctorId, "Khám tổng quát");
                JOptionPane.showMessageDialog(dialog,
                        "Đã đưa bệnh nhân " + patientName + " vào phòng khám!");
                dialog.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog,
                        "Lỗi khi đưa bệnh nhân vào hàng đợi: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnAddToRoom);

        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
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

    /**
     * Tìm doctor_id đầu tiên (active) từ DB.
     * Nếu không tìm thấy, trả về 1 (fallback mặc định).
     */
    private long findFirstDoctorId() {
        String sql = "SELECT doctor_id FROM Doctor WHERE is_active = TRUE ORDER BY doctor_id LIMIT 1";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("doctor_id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 1;
    }
}