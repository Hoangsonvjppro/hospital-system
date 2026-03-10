package com.hospital.gui.accountant;

import com.hospital.dao.DashboardDAO;
import com.hospital.gui.common.*;
import com.hospital.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dashboard tài chính — tổng quan thu chi, cảnh báo.
 */
public class FinanceDashboardPanel extends JPanel {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    public FinanceDashboardPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JLabel title = new JLabel("📊 Dashboard tài chính");
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

        body.add(createStatCardsRow(), BorderLayout.NORTH);
        body.add(createDetailSection(), BorderLayout.CENTER);

        return body;
    }

    private JPanel createStatCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);

        double todayRevenue = 0;
        int todayVisits = 0;
        int pendingPayments = 0;
        int dispensedCount = 0;

        try {
            todayRevenue = dashboardDAO.getTodayRevenue();
            todayVisits = dashboardDAO.countTodayVisits();
            pendingPayments = dashboardDAO.countTodayByQueueStatus("WAITING_PAYMENT");
            dispensedCount = dashboardDAO.countTodayDispensedPrescriptions();
        } catch (Exception ignored) {}

        row.add(new StatCard("💰", "Doanh thu hôm nay", String.format("%,.0f đ", todayRevenue),
                "Tổng thu trong ngày", UIConstants.REVENUE_PURPLE));
        row.add(new StatCard("🏥", "Lượt khám", String.valueOf(todayVisits),
                "Bệnh nhân hôm nay", UIConstants.PRIMARY));
        row.add(new StatCard("⏳", "Chờ thanh toán", String.valueOf(pendingPayments),
                "Hóa đơn pending", UIConstants.WARNING_ORANGE));
        row.add(new StatCard("💊", "Đơn đã phát", String.valueOf(dispensedCount),
                "Đơn thuốc hôm nay", UIConstants.SUCCESS_GREEN));

        return row;
    }

    private JPanel createDetailSection() {
        JPanel detail = new JPanel(new GridLayout(1, 2, 16, 0));
        detail.setOpaque(false);

        // Low stock alerts
        RoundedPanel alertCard = new RoundedPanel(UIConstants.CARD_RADIUS);
        alertCard.setBackground(UIConstants.CARD_BG);
        alertCard.setLayout(new BorderLayout(0, 8));
        alertCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblAlert = new JLabel("⚠ Thuốc sắp hết");
        lblAlert.setFont(UIConstants.FONT_SUBTITLE);
        lblAlert.setForeground(UIConstants.DANGER_RED);
        alertCard.add(lblAlert, BorderLayout.NORTH);

        DefaultTableModel lowStockModel = new DefaultTableModel(
                new String[]{"Thuốc", "Tồn kho", "Tối thiểu"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable lowStockTable = new JTable(lowStockModel);
        lowStockTable.setRowHeight(30);
        lowStockTable.setFont(UIConstants.FONT_LABEL);
        lowStockTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        lowStockTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);

        try {
            List<String[]> lowStock = dashboardDAO.findLowStockMedicines();
            for (String[] item : lowStock) {
                lowStockModel.addRow(item);
            }
        } catch (Exception ignored) {}

        alertCard.add(new JScrollPane(lowStockTable), BorderLayout.CENTER);

        // Long waiting patients
        RoundedPanel waitCard = new RoundedPanel(UIConstants.CARD_RADIUS);
        waitCard.setBackground(UIConstants.CARD_BG);
        waitCard.setLayout(new BorderLayout(0, 8));
        waitCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblWait = new JLabel("⏰ BN chờ lâu (>30 phút)");
        lblWait.setFont(UIConstants.FONT_SUBTITLE);
        lblWait.setForeground(UIConstants.WARNING_ORANGE);
        waitCard.add(lblWait, BorderLayout.NORTH);

        DefaultTableModel waitModel = new DefaultTableModel(
                new String[]{"BN", "Thời gian chờ", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable waitTable = new JTable(waitModel);
        waitTable.setRowHeight(30);
        waitTable.setFont(UIConstants.FONT_LABEL);
        waitTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        waitTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);

        try {
            List<String[]> longWaiting = dashboardDAO.findLongWaitingPatients(30);
            for (String[] item : longWaiting) {
                waitModel.addRow(item);
            }
        } catch (Exception ignored) {}

        waitCard.add(new JScrollPane(waitTable), BorderLayout.CENTER);

        detail.add(alertCard);
        detail.add(waitCard);
        return detail;
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
