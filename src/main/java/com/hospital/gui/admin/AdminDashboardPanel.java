package com.hospital.gui.admin;

import com.hospital.dao.DashboardDAO;
import com.hospital.gui.common.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Tổng quan hệ thống — thống kê tổng quát cho admin.
 */
public class AdminDashboardPanel extends JPanel {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    public AdminDashboardPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JLabel title = new JLabel("📊 Tổng quan hệ thống");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);

        RoundedButton btnRefresh = new RoundedButton("🔄 Làm mới");
        btnRefresh.setBackground(UIConstants.PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> refreshAll());
        header.add(btnRefresh, BorderLayout.EAST);

        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 5, 12, 0));
        statsRow.setOpaque(false);

        int patients = 0, doctors = 0, todayVisits = 0, medicines = 0, lowStock = 0;
        double todayRevenue = 0;

        try {
            patients = dashboardDAO.countActivePatients();
            doctors = dashboardDAO.countActiveDoctors();
            todayVisits = dashboardDAO.countTodayVisits();
            medicines = dashboardDAO.countActiveMedicines();
            lowStock = dashboardDAO.countLowStockMedicines();
            todayRevenue = dashboardDAO.getTodayRevenue();
        } catch (Exception ignored) {}

        statsRow.add(new StatCard("👥", "Bệnh nhân", String.valueOf(patients),
                "Đang hoạt động", UIConstants.PRIMARY));
        statsRow.add(new StatCard("🩺", "Bác sĩ", String.valueOf(doctors),
                "Đang hoạt động", UIConstants.SUCCESS_GREEN));
        statsRow.add(new StatCard("🏥", "Lượt khám hôm nay", String.valueOf(todayVisits),
                "Trong ngày", UIConstants.ACCENT_BLUE));
        statsRow.add(new StatCard("💰", "Doanh thu hôm nay", String.format("%,.0f đ", todayRevenue),
                "Tổng thu", UIConstants.REVENUE_PURPLE));
        statsRow.add(new StatCard("💊", "Thuốc", medicines + " / " + lowStock + " cảnh báo",
                "Tổng / Sắp hết", lowStock > 0 ? UIConstants.DANGER_RED : UIConstants.SUCCESS_GREEN));

        body.add(statsRow, BorderLayout.NORTH);

        // Queue summary
        JPanel queueRow = new JPanel(new GridLayout(1, 4, 12, 0));
        queueRow.setOpaque(false);

        int waiting = 0, inProgress = 0, completed = 0, cancelled = 0;
        try {
            waiting = dashboardDAO.countTodayByQueueStatus("WAITING");
            inProgress = dashboardDAO.countTodayByQueueStatus("IN_PROGRESS");
            completed = dashboardDAO.countTodayByQueueStatus("COMPLETED");
            cancelled = dashboardDAO.countTodayByQueueStatus("CANCELLED");
        } catch (Exception ignored) {}

        queueRow.add(new StatCard("⏳", "Đang chờ", String.valueOf(waiting),
                "Hàng đợi", UIConstants.WARNING_ORANGE));
        queueRow.add(new StatCard("🔄", "Đang khám", String.valueOf(inProgress),
                "Hàng đợi", UIConstants.STATUS_EXAMINING));
        queueRow.add(new StatCard("✅", "Hoàn tất", String.valueOf(completed),
                "Hàng đợi", UIConstants.STATUS_DONE));
        queueRow.add(new StatCard("❌", "Đã hủy", String.valueOf(cancelled),
                "Hàng đợi", UIConstants.STATUS_CANCEL));

        body.add(queueRow, BorderLayout.CENTER);

        // Info panel
        RoundedPanel infoCard = new RoundedPanel(UIConstants.CARD_RADIUS);
        infoCard.setBackground(UIConstants.CARD_BG);
        infoCard.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 12));
        infoCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblInfo = new JLabel("ℹ Hệ thống quản lý phòng mạch tư — v1.0");
        lblInfo.setFont(UIConstants.FONT_LABEL);
        lblInfo.setForeground(UIConstants.TEXT_SECONDARY);
        infoCard.add(lblInfo);

        int totalVisits = 0;
        try { totalVisits = dashboardDAO.countTotalVisits(); } catch (Exception ignored) {}
        JLabel lblTotal = new JLabel("Tổng lượt khám: " + totalVisits);
        lblTotal.setFont(UIConstants.FONT_LABEL);
        lblTotal.setForeground(UIConstants.TEXT_PRIMARY);
        infoCard.add(lblTotal);

        body.add(infoCard, BorderLayout.SOUTH);

        return body;
    }

    private void refreshAll() {
        removeAll();
        setLayout(new BorderLayout(0, 16));
        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
