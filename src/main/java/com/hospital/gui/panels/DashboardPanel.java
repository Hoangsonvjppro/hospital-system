package com.hospital.gui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.hospital.dao.DashboardDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Trang Tổng quan (Dashboard) — hiển thị các KPI và thông tin chào mừng.
 * Overview dashboard panel with KPI cards.
 */
public class DashboardPanel extends JPanel {

    // ── Colors ────────────────────────────────────────────────
    private static final Color BG             = new Color(0xF4F7FE);
    private static final Color CARD_BG        = Color.WHITE;
    private static final Color TEXT_DARK      = new Color(0x2B3674);
    private static final Color TEXT_MUTED     = new Color(0xA3AED0);
    private static final Color BLUE           = new Color(0x4318FF);
    private static final Color GREEN          = new Color(0x05CD99);
    private static final Color ORANGE         = new Color(0xFFB547);
    private static final Color RED            = new Color(0xEE5D50);

    private final String userName;
    private final DashboardDAO dashboardDAO = new DashboardDAO();

    public DashboardPanel(String userName) {
        this.userName = userName;
        setBackground(BG);
        setLayout(new BorderLayout(0, 24));
        setBorder(new EmptyBorder(28, 28, 28, 28));
        initComponents();
    }

    private void initComponents() {
        // ═══════ TOP: Chào mừng ═══════
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Lời chào
        JLabel lblWelcome = new JLabel("Xin chào, " + userName + " 👋");
        lblWelcome.putClientProperty(FlatClientProperties.STYLE, "font: bold +10");
        lblWelcome.setForeground(TEXT_DARK);

        // Ngày hiện tại
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy"));
        JLabel lblDate = new JLabel(today);
        lblDate.setForeground(TEXT_MUTED);
        lblDate.putClientProperty(FlatClientProperties.STYLE, "font: +1");

        JPanel welcomeGroup = new JPanel();
        welcomeGroup.setLayout(new BoxLayout(welcomeGroup, BoxLayout.Y_AXIS));
        welcomeGroup.setOpaque(false);
        welcomeGroup.add(lblWelcome);
        welcomeGroup.add(Box.createVerticalStrut(4));
        welcomeGroup.add(lblDate);

        topPanel.add(welcomeGroup, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // ═══════ CENTER: KPI Cards ═══════
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Row 1 — 4 KPI cards
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 20, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        // Load data
        int totalPatients   = dashboardDAO.countActivePatients();
        int totalMedicines  = dashboardDAO.countActiveMedicines();
        int todayVisits     = dashboardDAO.countTodayVisits();
        int lowStockCount   = dashboardDAO.countLowStockMedicines();

        kpiRow.add(createKPICard("👥", "Tổng bệnh nhân",      String.valueOf(totalPatients),  BLUE));
        kpiRow.add(createKPICard("💊", "Danh mục thuốc",       String.valueOf(totalMedicines), GREEN));
        kpiRow.add(createKPICard("🩺", "Khám hôm nay",         String.valueOf(todayVisits),    ORANGE));
        kpiRow.add(createKPICard("⚠️", "Thuốc sắp hết",       String.valueOf(lowStockCount),  RED));

        centerPanel.add(kpiRow);
        centerPanel.add(Box.createVerticalStrut(24));

        // Row 2 — More stats
        JPanel statsRow = new JPanel(new GridLayout(1, 2, 20, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        int totalDoctors = dashboardDAO.countActiveDoctors();
        int totalVisits  = dashboardDAO.countTotalVisits();

        statsRow.add(createInfoCard("🏥", "Thông tin phòng khám",
                new String[]{
                    "Bác sĩ đang hoạt động: " + totalDoctors,
                    "Tổng lượt khám (tất cả): " + totalVisits,
                }));

        statsRow.add(createInfoCard("📋", "Hướng dẫn nhanh",
                new String[]{
                    "Bấm menu bên trái để điều hướng",
                    "Kho thuốc: Quản lý nhập/xuất thuốc",
                }));

        centerPanel.add(statsRow);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════
    //  COMPONENT FACTORIES
    // ══════════════════════════════════════════════════════════

    /**
     * Tạo KPI card với icon, tiêu đề, giá trị, và màu accent.
     */
    private JPanel createKPICard(String icon, String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 16");

        // Left: icon circle
        JLabel lblIcon = new JLabel(icon, SwingConstants.CENTER);
        lblIcon.setFont(new Font("SansSerif", Font.PLAIN, 28));
        lblIcon.setPreferredSize(new Dimension(50, 50));
        card.add(lblIcon, BorderLayout.WEST);

        // Right: title + value
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(TEXT_MUTED);
        lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel lblValue = new JLabel(value);
        lblValue.setForeground(TEXT_DARK);
        lblValue.putClientProperty(FlatClientProperties.STYLE, "font: bold +14");

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(lblValue);

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Tạo info card với danh sách thông tin.
     */
    private JPanel createInfoCard(String icon, String title, String[] lines) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(CARD_BG);
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 16");

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.setOpaque(false);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("SansSerif", Font.PLAIN, 22));
        header.add(lblIcon);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(TEXT_DARK);
        lblTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +2");
        header.add(lblTitle);

        card.add(header, BorderLayout.NORTH);

        // Lines
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        for (String line : lines) {
            JLabel lbl = new JLabel("•  " + line);
            lbl.setForeground(TEXT_MUTED);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lbl.setBorder(new EmptyBorder(3, 0, 3, 0));
            body.add(lbl);
        }

        card.add(body, BorderLayout.CENTER);
        return card;
    }
}
