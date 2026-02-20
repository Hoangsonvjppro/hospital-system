package com.hospital.gui.panels;

import com.hospital.bus.DoctorBUS;
import com.hospital.bus.InvoiceBUS;
import com.hospital.bus.MedicineBUS;
import com.hospital.bus.PatientBUS;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.components.StatCard;
import com.hospital.gui.components.StatusBadge;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Trang chủ – Bảng điều khiển (Dashboard).
 */
public class DashboardPanel extends JPanel {

    private final PatientBUS  patientBUS  = new PatientBUS();
    private final DoctorBUS   doctorBUS   = new DoctorBUS();
    private final MedicineBUS medicineBUS = new MedicineBUS();
    private final InvoiceBUS  invoiceBUS  = new InvoiceBUS();

    private StatCard cardPatient;
    private StatCard cardRevenue;
    private StatCard cardMedicine;
    private DefaultTableModel tableModel;
    private JTable table;

    public DashboardPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
    }

    private void initComponents() {
        // ── 1. Header ────────────────────────────────────────────────────────
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // ── 2. Nội dung chính ─────────────────────────────────────────────
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // Left: Stats + Table
        JPanel leftCol = new JPanel(new BorderLayout(0, 16));
        leftCol.setOpaque(false);
        leftCol.add(createStatCards(), BorderLayout.NORTH);
        leftCol.add(createWaitingTable(), BorderLayout.CENTER);

        // Right: sidebar info
        JPanel rightCol = createRightPanel();
        rightCol.setPreferredSize(new Dimension(260, 0));

        body.add(leftCol,  BorderLayout.CENTER);
        body.add(rightCol, BorderLayout.EAST);
        add(body, BorderLayout.CENTER);
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel titleLbl = new JLabel("Bảng điều khiển");
        titleLbl.setFont(UIConstants.FONT_TITLE);
        titleLbl.setForeground(UIConstants.PRIMARY_RED);

        JLabel subLbl = new JLabel("Hôm nay, ngày 20 Tháng 02, 2026");
        subLbl.setFont(UIConstants.FONT_SMALL);
        subLbl.setForeground(UIConstants.TEXT_SECONDARY);

        JPanel titleCol = new JPanel(new GridLayout(2, 1, 0, 2));
        titleCol.setOpaque(false);
        titleCol.add(titleLbl);
        titleCol.add(subLbl);
        p.add(titleCol, BorderLayout.WEST);
        return p;
    }

    // ── Stat Cards ────────────────────────────────────────────────────────────
    private JPanel createStatCards() {
        JPanel p = new JPanel(new GridLayout(1, 3, 14, 0));
        p.setOpaque(false);

        int patientCount  = patientBUS.countToday();
        double revenue    = invoiceBUS.getTotalRevenue();
        int lowStock      = medicineBUS.countLowStock();

        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));

        cardPatient = new StatCard(
                "Bệnh nhân hôm nay",
                String.valueOf(patientCount),
                "\u2191 +12%  so với hôm qua",
                "\uD83D\uDC65",
                UIConstants.SUCCESS_GREEN);

        cardRevenue = new StatCard(
                "Doanh thu tạm tính",
                fmt.format(revenue / 1_000_000.0) + " tr",
                "\u2191 +5.2%  so với tuần trước",
                "\uD83D\uDCB0",
                UIConstants.PRIMARY_RED);

        cardMedicine = new StatCard(
                "Thuốc sắp hết",
                String.valueOf(lowStock),
                "Cần nhập kho",
                "\u26A0",
                UIConstants.WARNING_ORANGE);

        p.add(cardPatient);
        p.add(cardRevenue);
        p.add(cardMedicine);
        return p;
    }

    // ── Waiting Table ─────────────────────────────────────────────────────────
    private JPanel createWaitingTable() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Title row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JPanel titleLeft = new JPanel(new GridLayout(2, 1, 0, 2));
        titleLeft.setOpaque(false);
        JLabel title = new JLabel("Danh sách chờ khám");
        title.setFont(UIConstants.FONT_SUBTITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Hôm nay, 20 Tháng 02, 2026");
        subtitle.setFont(UIConstants.FONT_SMALL);
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);
        titleLeft.add(title);
        titleLeft.add(subtitle);

        JPanel titleRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        titleRight.setOpaque(false);
        RoundedButton addBtn = new RoundedButton("+ Thêm bệnh nhân");
        addBtn.setPreferredSize(new Dimension(170, 34));
        addBtn.addActionListener(e -> showAddPatientDialog());
        titleRight.add(addBtn);

        titleRow.add(titleLeft, BorderLayout.WEST);
        titleRow.add(titleRight, BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        // Table
        String[] cols = {"Mã BN", "Họ và Tên", "Giờ tiếp nhận", "Loại khám", "Trạng thái", "Hành động"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5; }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? UIConstants.WHITE : UIConstants.TABLE_ROW_ALT);
                return c;
            }
        };

        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setRowHeight(46);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(UIConstants.RED_BG_SOFT);
        table.setSelectionForeground(UIConstants.TEXT_PRIMARY);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setBackground(UIConstants.TABLE_HEADER_BG);
        header.setForeground(UIConstants.TEXT_SECONDARY);
        header.setFont(UIConstants.FONT_BOLD);
        header.setReorderingAllowed(false);

        // Column widths
        int[] widths = {75, 190, 110, 160, 110, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Status column renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                StatusBadge badge = new StatusBadge(v == null ? "" : v.toString());
                badge.setHorizontalAlignment(SwingConstants.CENTER);
                return badge;
            }
        });

        // Action column renderer+editor
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionEditor(table));

        // Center alignment for columns
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[]{0, 2, 3}) table.getColumnModel().getColumn(i).setCellRenderer(center);

        loadTableData();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR));
        scroll.getViewport().setBackground(UIConstants.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void loadTableData() {
        tableModel.setRowCount(0);
        List<Patient> list = patientBUS.getWaitingPatients();
        for (Patient p : list) {
            tableModel.addRow(new Object[]{
                p.getPatientCode(),
                p.getFullName() + "\n" + p.getAge() + " tuổi • " + p.getGender(),
                p.getArrivalTime(),
                p.getExamType(),
                p.getStatus(),
                p.getStatus()   // action button label based on status
            });
        }
    }

    // ── Right Panel ───────────────────────────────────────────────────────────
    private JPanel createRightPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        p.add(createNotificationPanel());
        p.add(Box.createVerticalStrut(14));
        p.add(createDoctorPanel());
        p.add(Box.createVerticalStrut(14));
        p.add(createQuickActions());
        return p;
    }

    private JPanel createNotificationPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JLabel title = new JLabel("\uD83D\uDD14  THÔNG BÁO KHẨN");
        title.setFont(UIConstants.FONT_BOLD);
        title.setForeground(UIConstants.PRIMARY_RED);
        card.add(title, BorderLayout.NORTH);

        JPanel items = new JPanel();
        items.setOpaque(false);
        items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));

        items.add(createNotifItem("Họp giao ban toàn khoa",
                "14:00 chiều nay tại phòng họp số 1."));
        items.add(Box.createVerticalStrut(8));
        items.add(createNotifItem("Cập nhật hệ thống",
                "Bảo trì lúc 20:00 tối nay."));

        card.add(items, BorderLayout.CENTER);

        JLabel seeAll = new JLabel("XEM TẤT CẢ THÔNG BÁO →");
        seeAll.setFont(UIConstants.FONT_SMALL);
        seeAll.setForeground(UIConstants.PRIMARY_RED);
        seeAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.add(seeAll, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createNotifItem(String title, String body) {
        JPanel p = new JPanel(new BorderLayout(6, 2));
        p.setOpaque(false);

        JLabel bullet = new JLabel("•");
        bullet.setForeground(UIConstants.WARNING_ORANGE);
        bullet.setFont(UIConstants.FONT_BOLD);

        JPanel right = new JPanel(new GridLayout(2, 1));
        right.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(UIConstants.FONT_BOLD);
        t.setForeground(UIConstants.TEXT_PRIMARY);
        JLabel b = new JLabel(body);
        b.setFont(UIConstants.FONT_SMALL);
        b.setForeground(UIConstants.TEXT_SECONDARY);
        right.add(t);
        right.add(b);

        p.add(bullet, BorderLayout.WEST);
        p.add(right,  BorderLayout.CENTER);
        return p;
    }

    private JPanel createDoctorPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        List<Doctor> online = doctorBUS.getOnlineDoctors();
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        JLabel title = new JLabel("BÁC SĨ TRỰC");
        title.setFont(UIConstants.FONT_BOLD);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        JLabel countLbl = new JLabel(online.size() + " TRỰC TUYẾN");
        countLbl.setFont(UIConstants.FONT_SMALL);
        countLbl.setForeground(UIConstants.SUCCESS_GREEN);
        hdr.add(title,   BorderLayout.WEST);
        hdr.add(countLbl, BorderLayout.EAST);
        card.add(hdr, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        for (Doctor d : online.subList(0, Math.min(2, online.size()))) {
            list.add(createDoctorItem(d));
            list.add(Box.createVerticalStrut(6));
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDoctorItem(Doctor d) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // Avatar
        JLabel avatar = new JLabel(d.getAvatar(), SwingConstants.CENTER);
        avatar.setFont(UIConstants.FONT_BOLD);
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(38, 38));

        JPanel avatarWrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.PRIMARY_RED);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        avatarWrapper.setOpaque(false);
        avatarWrapper.setPreferredSize(new Dimension(38, 38));
        avatarWrapper.add(avatar, BorderLayout.CENTER);

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        JLabel name = new JLabel(d.getFullName());
        name.setFont(UIConstants.FONT_BOLD);
        name.setForeground(UIConstants.TEXT_PRIMARY);
        JLabel spec = new JLabel(d.getSpecialty());
        spec.setFont(UIConstants.FONT_SMALL);
        spec.setForeground(UIConstants.TEXT_SECONDARY);
        info.add(name);
        info.add(spec);

        p.add(avatarWrapper, BorderLayout.WEST);
        p.add(info,          BorderLayout.CENTER);
        return p;
    }

    private JPanel createQuickActions() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.PRIMARY_RED);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        JLabel title = new JLabel("Tạo mới nhanh");
        title.setFont(UIConstants.FONT_SUBTITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("THAO TÁC NGHIỆP VỤ");
        sub.setFont(UIConstants.FONT_SMALL);
        sub.setForeground(new Color(255, 255, 255, 180));

        JPanel hdr = new JPanel(new GridLayout(2, 1));
        hdr.setOpaque(false);
        hdr.add(title);
        hdr.add(sub);
        card.add(hdr, BorderLayout.NORTH);

        JPanel btns = new JPanel(new GridLayout(1, 2, 10, 0));
        btns.setOpaque(false);

        RoundedButton btnAppt   = createWhiteBtn("\uD83D\uDCC5", "LỊCH HẸN");
        RoundedButton btnInvoice = createWhiteBtn("\uD83D\uDCC4", "HÓA ĐƠN");
        btns.add(btnAppt);
        btns.add(btnInvoice);
        card.add(btns, BorderLayout.CENTER);
        return card;
    }

    private RoundedButton createWhiteBtn(String icon, String label) {
        RoundedButton btn = new RoundedButton("<html><center>" + icon + "<br>" + label + "</center></html>");
        btn.setColors(new Color(255,255,255,40), new Color(255,255,255,70));
        btn.setFont(new Font(UIConstants.FONT_NAME, Font.BOLD, 11));
        btn.setPreferredSize(new Dimension(100, 52));
        return btn;
    }

    // ── Dialogs ─────────────────────────────────────────────────────────────
    private void showAddPatientDialog() {
        JOptionPane.showMessageDialog(this,
                "Chức năng thêm bệnh nhân sẽ được tích hợp ở trang Tiếp nhận.",
                "Thêm bệnh nhân", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Action Column ─────────────────────────────────────────────────────────
    static class ActionRenderer implements javax.swing.table.TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
            p.setBackground(isSelected ? UIConstants.RED_BG_SOFT : UIConstants.WHITE);

            String status = (String) table.getValueAt(row, 4);
            if ("CHỜ KHÁM".equals(status)) {
                RoundedButton btn = new RoundedButton("Gọi khám");
                btn.setPreferredSize(new Dimension(90, 30));
                btn.setFont(UIConstants.FONT_SMALL);
                p.add(btn);
            } else {
                JButton btn = new JButton("Chi tiết");
                btn.setPreferredSize(new Dimension(80, 30));
                btn.setFont(UIConstants.FONT_SMALL);
                p.add(btn);
            }
            return p;
        }
    }

    static class ActionEditor extends DefaultCellEditor {
        private JPanel panel;
        private JTable table;

        public ActionEditor(JTable table) {
            super(new JCheckBox());
            this.table = table;
            setClickCountToStart(1);
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object value,
                boolean isSelected, int row, int column) {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
            panel.setBackground(UIConstants.RED_BG_SOFT);
            String status = (String) t.getValueAt(row, 4);
            if ("CHỜ KHÁM".equals(status)) {
                RoundedButton btn = new RoundedButton("Gọi khám");
                btn.setPreferredSize(new Dimension(90, 30));
                btn.setFont(UIConstants.FONT_SMALL);
                btn.addActionListener(e -> {
                    fireEditingStopped();
                    JOptionPane.showMessageDialog(t,
                            "Đã gọi bệnh nhân hàng " + (row + 1) + " vào khám.",
                            "Gọi khám", JOptionPane.INFORMATION_MESSAGE);
                });
                panel.add(btn);
            } else {
                JButton btn = new JButton("Chi tiết");
                btn.setPreferredSize(new Dimension(80, 30));
                btn.setFont(UIConstants.FONT_SMALL);
                btn.addActionListener(e -> fireEditingStopped());
                panel.add(btn);
            }
            return panel;
        }

        @Override public Object getCellEditorValue() { return ""; }
    }
}
