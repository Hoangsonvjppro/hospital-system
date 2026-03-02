package com.hospital.gui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.hospital.dao.DashboardDAO;
import com.hospital.gui.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Trang Tổng quan (Dashboard) — hiển thị dữ liệu real-time.
 * <p>
 * Phase 2 nâng cấp:
 * - Số BN hôm nay (đang chờ / đã khám / đã thanh toán)
 * - Doanh thu hôm nay
 * - Số đơn thuốc đã phát
 * - Cảnh báo: thuốc sắp hết, BN chờ lâu
 * - Auto-refresh mỗi 30 giây (SwingWorker off-EDT)
 */
public class DashboardPanel extends JPanel {

    // Tất cả màu/font dùng UIConstants — KHÔNG khai báo cục bộ.

    private static final int REFRESH_INTERVAL_MS = 30_000; // 30 giây
    private static final int LONG_WAIT_THRESHOLD = 30;     // 30 phút

    private final String userName;
    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final NumberFormat currencyFmt;

    // ── KPI labels (update text mà không rebuild UI) ─────────
    private JLabel lblTodayPatients;
    private JLabel lblTodayPatientsDetail;
    private JLabel lblRevenue;
    private JLabel lblDispensedRx;
    private JLabel lblLowStock;

    // ── Alert panels ─────────────────────────────────────────
    private JPanel alertLowStockBody;
    private JPanel alertLongWaitBody;
    private JPanel alertLowStockCard;
    private JPanel alertLongWaitCard;

    // ── Info labels ──────────────────────────────────────────
    private JLabel lblActiveDoctors;
    private JLabel lblTotalVisits;
    private JLabel lblLastUpdate;

    // ── Timer ────────────────────────────────────────────────
    private Timer refreshTimer;

    public DashboardPanel(String userName) {
        this.userName = userName;
        this.currencyFmt = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(28, 28, 28, 28));
        initComponents();
        loadDataAsync();
        startAutoRefresh();
    }

    // ══════════════════════════════════════════════════════════
    //  LAYOUT — Build once, update labels repeatedly
    // ══════════════════════════════════════════════════════════

    private void initComponents() {
        // ═══════ TOP: Chào mừng ═══════
        add(createTopPanel(), BorderLayout.NORTH);

        // ═══════ CENTER: scrollable content ═══════
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        centerPanel.add(createKPIRow());
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(createAlertRow());
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(createInfoRow());
        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(createFooter());
        centerPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ── TOP: Welcome ─────────────────────────────────────────

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblWelcome = new JLabel("Xin chào, " + userName + " 👋");
        lblWelcome.putClientProperty(FlatClientProperties.STYLE, "font: bold +10");
        lblWelcome.setForeground(UIConstants.TEXT_PRIMARY);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy"));
        JLabel lblDate = new JLabel(today);
        lblDate.setForeground(UIConstants.TEXT_SECONDARY);
        lblDate.putClientProperty(FlatClientProperties.STYLE, "font: +1");

        JPanel welcomeGroup = new JPanel();
        welcomeGroup.setLayout(new BoxLayout(welcomeGroup, BoxLayout.Y_AXIS));
        welcomeGroup.setOpaque(false);
        welcomeGroup.add(lblWelcome);
        welcomeGroup.add(Box.createVerticalStrut(4));
        welcomeGroup.add(lblDate);

        topPanel.add(welcomeGroup, BorderLayout.WEST);
        return topPanel;
    }

    // ── ROW 1: KPI Cards ─────────────────────────────────────

    private JPanel createKPIRow() {
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // Card 1 — BN hôm nay (có sub-detail)
        lblTodayPatients = new JLabel("...");
        lblTodayPatientsDetail = new JLabel("Đang tải...");
        kpiRow.add(createKPICardWithDetail("🏥", "BN hôm nay",
                lblTodayPatients, lblTodayPatientsDetail, UIConstants.ACCENT_BLUE));

        // Card 2 — Doanh thu
        lblRevenue = new JLabel("...");
        kpiRow.add(createKPICard("💰", "Doanh thu hôm nay", lblRevenue, UIConstants.REVENUE_PURPLE));

        // Card 3 — Đơn thuốc đã phát
        lblDispensedRx = new JLabel("...");
        kpiRow.add(createKPICard("💊", "Đơn thuốc đã phát", lblDispensedRx, UIConstants.SUCCESS_GREEN));

        // Card 4 — Thuốc sắp hết
        lblLowStock = new JLabel("...");
        kpiRow.add(createKPICard("⚠️", "Thuốc sắp hết", lblLowStock, UIConstants.WARNING_ORANGE));

        return kpiRow;
    }

    // ── ROW 2: Alert Cards ───────────────────────────────────

    private JPanel createAlertRow() {
        JPanel alertRow = new JPanel(new GridLayout(1, 2, 16, 0));
        alertRow.setOpaque(false);
        alertRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // Alert — Thuốc sắp hết
        alertLowStockBody = new JPanel();
        alertLowStockBody.setLayout(new BoxLayout(alertLowStockBody, BoxLayout.Y_AXIS));
        alertLowStockBody.setOpaque(false);
        alertLowStockCard = createAlertCard("🔴", "Cảnh báo: Thuốc sắp hết",
                alertLowStockBody, UIConstants.ALERT_RED_BG, UIConstants.ALERT_RED_BORDER);
        alertRow.add(alertLowStockCard);

        // Alert — BN chờ lâu
        alertLongWaitBody = new JPanel();
        alertLongWaitBody.setLayout(new BoxLayout(alertLongWaitBody, BoxLayout.Y_AXIS));
        alertLongWaitBody.setOpaque(false);
        alertLongWaitCard = createAlertCard("🟡", "Cảnh báo: BN chờ lâu (>" + LONG_WAIT_THRESHOLD + " phút)",
                alertLongWaitBody, UIConstants.ALERT_AMBER_BG, UIConstants.ALERT_AMBER_BORDER);
        alertRow.add(alertLongWaitCard);

        return alertRow;
    }

    // ── ROW 3: Info Cards ────────────────────────────────────

    private JPanel createInfoRow() {
        JPanel statsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // Info — Phòng khám
        lblActiveDoctors = new JLabel("•  Bác sĩ đang hoạt động: ...");
        lblActiveDoctors.setForeground(UIConstants.TEXT_SECONDARY);
        lblActiveDoctors.setFont(UIConstants.FONT_LABEL);
        lblActiveDoctors.setBorder(new EmptyBorder(3, 0, 3, 0));

        lblTotalVisits = new JLabel("•  Tổng lượt khám (tất cả): ...");
        lblTotalVisits.setForeground(UIConstants.TEXT_SECONDARY);
        lblTotalVisits.setFont(UIConstants.FONT_LABEL);
        lblTotalVisits.setBorder(new EmptyBorder(3, 0, 3, 0));

        JPanel infoBody = new JPanel();
        infoBody.setLayout(new BoxLayout(infoBody, BoxLayout.Y_AXIS));
        infoBody.setOpaque(false);
        infoBody.add(lblActiveDoctors);
        infoBody.add(lblTotalVisits);

        statsRow.add(createInfoCard("🏥", "Thông tin phòng khám", infoBody));

        // Info — Hướng dẫn
        statsRow.add(createInfoCard("📋", "Hướng dẫn nhanh",
                new String[]{
                    "Bấm menu bên trái để điều hướng",
                    "Kho thuốc: Quản lý nhập/xuất thuốc",
                    "Dữ liệu tự cập nhật mỗi 30 giây",
                }));

        return statsRow;
    }

    // ── FOOTER: Last-update + refresh button ─────────────────

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        lblLastUpdate = new JLabel("Đang tải...");
        lblLastUpdate.setFont(UIConstants.FONT_SMALL);
        lblLastUpdate.setForeground(UIConstants.TEXT_MUTED);
        footer.add(lblLastUpdate);

        JButton btnRefresh = new JButton("🔄 Cập nhật");
        btnRefresh.setFont(UIConstants.FONT_SMALL);
        btnRefresh.setForeground(UIConstants.ACCENT_BLUE);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadDataAsync());
        footer.add(btnRefresh);

        return footer;
    }

    // ══════════════════════════════════════════════════════════
    //  DATA LOADING — SwingWorker (off-EDT)
    // ══════════════════════════════════════════════════════════

    /**
     * DTO chứa toàn bộ dữ liệu dashboard — load 1 lần, update UI 1 lần.
     */
    private static class DashboardData {
        int todayTotal;
        int waiting;
        int examining;
        int prescribed;
        int dispensed;
        int completed;
        double todayRevenue;
        int dispensedRx;
        int lowStockCount;
        int activeDoctors;
        int totalVisits;
        List<String[]> lowStockList;
        List<String[]> longWaitList;
    }

    private void loadDataAsync() {
        new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() {
                DashboardData d = new DashboardData();
                d.todayTotal    = dashboardDAO.countTodayVisits();
                d.waiting       = dashboardDAO.countTodayByQueueStatus("WAITING");
                d.examining     = dashboardDAO.countTodayByQueueStatus("EXAMINING");
                d.prescribed    = dashboardDAO.countTodayByQueueStatus("PRESCRIBED");
                d.dispensed     = dashboardDAO.countTodayByQueueStatus("DISPENSED");
                d.completed     = dashboardDAO.countTodayByQueueStatus("COMPLETED");
                d.todayRevenue  = dashboardDAO.getTodayRevenue();
                d.dispensedRx   = dashboardDAO.countTodayDispensedPrescriptions();
                d.lowStockCount = dashboardDAO.countLowStockMedicines();
                d.activeDoctors = dashboardDAO.countActiveDoctors();
                d.totalVisits   = dashboardDAO.countTotalVisits();
                d.lowStockList  = dashboardDAO.findLowStockMedicines();
                d.longWaitList  = dashboardDAO.findLongWaitingPatients(LONG_WAIT_THRESHOLD);
                return d;
            }

            @Override
            protected void done() {
                try {
                    DashboardData d = get();
                    updateUI(d);
                } catch (Exception ex) {
                    lblLastUpdate.setText("⚠ Lỗi tải dữ liệu!");
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Cập nhật toàn bộ UI labels/panels từ DashboardData — chạy trên EDT.
     */
    private void updateUI(DashboardData d) {
        // ── KPI Row ──────────────────────────────────────────
        lblTodayPatients.setText(String.valueOf(d.todayTotal));

        // Chi tiết BN: Chờ X · Khám Y · Xong Z
        int doneCount = d.prescribed + d.dispensed + d.completed;
        lblTodayPatientsDetail.setText(
                String.format("Chờ: %d · Khám: %d · Xong: %d", d.waiting, d.examining, doneCount));

        lblRevenue.setText(currencyFmt.format(d.todayRevenue));
        lblDispensedRx.setText(String.valueOf(d.dispensedRx));
        lblLowStock.setText(String.valueOf(d.lowStockCount));

        // ── Alert: Thuốc sắp hết ────────────────────────────
        alertLowStockBody.removeAll();
        if (d.lowStockList.isEmpty()) {
            addAlertLine(alertLowStockBody, "✅ Không có thuốc nào sắp hết hàng", UIConstants.SUCCESS_GREEN);
        } else {
            for (String[] med : d.lowStockList) {
                String line = med[0] + ":  tồn " + med[1] + " / ngưỡng " + med[2];
                Color color = Integer.parseInt(med[1]) == 0 ? UIConstants.PRIMARY : UIConstants.TEXT_PRIMARY;
                addAlertLine(alertLowStockBody, "•  " + line, color);
            }
        }
        alertLowStockBody.revalidate();
        alertLowStockBody.repaint();

        // ── Alert: BN chờ lâu ────────────────────────────────
        alertLongWaitBody.removeAll();
        if (d.longWaitList.isEmpty()) {
            addAlertLine(alertLongWaitBody, "✅ Không có BN nào chờ quá " + LONG_WAIT_THRESHOLD + " phút",
                    UIConstants.SUCCESS_GREEN);
        } else {
            for (String[] patient : d.longWaitList) {
                String line = patient[0] + ":  " + patient[1] + " phút";
                Color color = Integer.parseInt(patient[1]) >= 60 ? UIConstants.PRIMARY : UIConstants.TEXT_PRIMARY;
                addAlertLine(alertLongWaitBody, "•  " + line, color);
            }
        }
        alertLongWaitBody.revalidate();
        alertLongWaitBody.repaint();

        // ── Info ─────────────────────────────────────────────
        lblActiveDoctors.setText("•  Bác sĩ đang hoạt động: " + d.activeDoctors);
        lblTotalVisits.setText("•  Tổng lượt khám (tất cả): " + d.totalVisits);

        // ── Footer ───────────────────────────────────────────
        String now = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        lblLastUpdate.setText("Cập nhật lúc " + now + "  •");
    }

    private void addAlertLine(JPanel body, String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(color);
        lbl.setBorder(new EmptyBorder(2, 0, 2, 0));
        body.add(lbl);
    }

    // ══════════════════════════════════════════════════════════
    //  AUTO-REFRESH
    // ══════════════════════════════════════════════════════════

    private void startAutoRefresh() {
        refreshTimer = new Timer(REFRESH_INTERVAL_MS, e -> loadDataAsync());
        refreshTimer.setRepeats(true);
        refreshTimer.start();
    }

    /**
     * Dừng timer khi panel bị remove khỏi hierarchy — tránh memory leak.
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        if (refreshTimer != null) {
            refreshTimer.stop();
            refreshTimer = null;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  COMPONENT FACTORIES
    // ══════════════════════════════════════════════════════════

    /**
     * KPI card cơ bản — icon, title, big-value label.
     */
    private JPanel createKPICard(String icon, String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(UIConstants.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 16");

        JLabel lblIcon = new JLabel(icon, SwingConstants.CENTER);
        lblIcon.setFont(UIConstants.FONT_ICON);
        lblIcon.setPreferredSize(new Dimension(50, 50));
        card.add(lblIcon, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(UIConstants.TEXT_SECONDARY);
        lblTitle.setFont(UIConstants.FONT_CAPTION);

        valueLabel.setForeground(UIConstants.TEXT_PRIMARY);
        valueLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +14");

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(valueLabel);

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * KPI card với thêm dòng chi tiết nhỏ bên dưới value.
     */
    private JPanel createKPICardWithDetail(String icon, String title,
                                           JLabel valueLabel, JLabel detailLabel,
                                           Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(UIConstants.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                new EmptyBorder(18, 20, 18, 20)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 16");

        JLabel lblIcon = new JLabel(icon, SwingConstants.CENTER);
        lblIcon.setFont(UIConstants.FONT_ICON);
        lblIcon.setPreferredSize(new Dimension(50, 50));
        card.add(lblIcon, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(UIConstants.TEXT_SECONDARY);
        lblTitle.setFont(UIConstants.FONT_CAPTION);

        valueLabel.setForeground(UIConstants.TEXT_PRIMARY);
        valueLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +14");

        detailLabel.setForeground(UIConstants.TEXT_SECONDARY);
        detailLabel.setFont(UIConstants.FONT_SMALL);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(detailLabel);

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Alert card — nền màu nhạt + viền màu, chứa body panel động.
     */
    private JPanel createAlertCard(String icon, String title, JPanel body,
                                   Color bgColor, Color borderColor) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        header.setOpaque(false);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font(UIConstants.FONT_NAME, Font.PLAIN, 16));
        header.add(lblIcon);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        lblTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +1");
        header.add(lblTitle);

        card.add(header, BorderLayout.NORTH);

        // Scrollable body
        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        bodyScroll.setOpaque(false);
        bodyScroll.getViewport().setOpaque(false);
        bodyScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(10);
        card.add(bodyScroll, BorderLayout.CENTER);

        return card;
    }

    /**
     * Info card với body panel tùy chỉnh.
     */
    private JPanel createInfoCard(String icon, String title, JPanel body) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(UIConstants.CARD_BG);
        card.setBorder(new EmptyBorder(18, 22, 18, 22));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 16");

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.setOpaque(false);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font(UIConstants.FONT_NAME, Font.PLAIN, 20));
        header.add(lblIcon);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        lblTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +2");
        header.add(lblTitle);

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    /**
     * Info card với danh sách string cố định.
     */
    private JPanel createInfoCard(String icon, String title, String[] lines) {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        for (String line : lines) {
            JLabel lbl = new JLabel("•  " + line);
            lbl.setForeground(UIConstants.TEXT_SECONDARY);
            lbl.setFont(UIConstants.FONT_LABEL);
            lbl.setBorder(new EmptyBorder(3, 0, 3, 0));
            body.add(lbl);
        }

        return createInfoCard(icon, title, body);
    }
}
