package com.hospital.gui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.hospital.dao.DashboardDAO;
import com.hospital.gui.UIConstants;

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

    // Tất cả màu/font dùng UIConstants — KHÔNG khai báo cục bộ.
    // KPI accent colors (specific to dashboard cards, defined in UIConstants)

    private final String userName;
    private final DashboardDAO dashboardDAO = new DashboardDAO();

    public DashboardPanel(String userName) {
        this.userName = userName;
        setBackground(UIConstants.CONTENT_BG);
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
        lblWelcome.setForeground(UIConstants.TEXT_PRIMARY);

        // Ngày hiện tại
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

        kpiRow.add(createKPICard("👥", "Tổng bệnh nhân",      String.valueOf(totalPatients),  UIConstants.ACCENT_BLUE));
        kpiRow.add(createKPICard("💊", "Danh mục thuốc",       String.valueOf(totalMedicines), UIConstants.SUCCESS_GREEN));
        kpiRow.add(createKPICard("🩺", "Khám hôm nay",         String.valueOf(todayVisits),    UIConstants.WARNING_ORANGE));
        kpiRow.add(createKPICard("⚠️", "Thuốc sắp hết",       String.valueOf(lowStockCount),  UIConstants.PRIMARY));

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
        card.setBackground(UIConstants.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 16");

        // Left: icon circle
        JLabel lblIcon = new JLabel(icon, SwingConstants.CENTER);
        lblIcon.setFont(UIConstants.FONT_ICON);
        lblIcon.setPreferredSize(new Dimension(50, 50));
        card.add(lblIcon, BorderLayout.WEST);

        // Right: title + value
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(UIConstants.TEXT_SECONDARY);
        lblTitle.setFont(UIConstants.FONT_CAPTION);

        JLabel lblValue = new JLabel(value);
        lblValue.setForeground(UIConstants.TEXT_PRIMARY);
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
        card.setBackground(UIConstants.CARD_BG);
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 16");

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.setOpaque(false);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font(UIConstants.FONT_NAME, Font.PLAIN, 22));
        header.add(lblIcon);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        lblTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +2");
        header.add(lblTitle);

        card.add(header, BorderLayout.NORTH);

        // Lines
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

        card.add(body, BorderLayout.CENTER);
        return card;
    }
}
