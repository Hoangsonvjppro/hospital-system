package com.hospital.gui.accountant;

import com.hospital.dao.ReportDAO;
import com.hospital.gui.common.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Báo cáo doanh thu — lọc theo khoảng ngày, hiển thị doanh thu + top thuốc + top bệnh.
 */
public class RevenueReportPanel extends JPanel {

    private final ReportDAO reportDAO = new ReportDAO();

    private JTextField txtFrom, txtTo;
    private JLabel lblTotalRevenue;
    private DefaultTableModel revenueModel, topMedModel, topDiseaseModel;

    public RevenueReportPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);

        JLabel title = new JLabel("📈 Báo cáo doanh thu");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(title, BorderLayout.NORTH);

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.setOpaque(false);

        filterBar.add(new JLabel("Từ:"));
        txtFrom = new JTextField(10);
        txtFrom.setFont(UIConstants.FONT_BODY);
        txtFrom.setText(LocalDate.now().withDayOfMonth(1).toString());
        filterBar.add(txtFrom);

        filterBar.add(new JLabel("Đến:"));
        txtTo = new JTextField(10);
        txtTo.setFont(UIConstants.FONT_BODY);
        txtTo.setText(LocalDate.now().toString());
        filterBar.add(txtTo);

        RoundedButton btnFilter = new RoundedButton("📊 Xem báo cáo");
        btnFilter.setBackground(UIConstants.PRIMARY);
        btnFilter.setForeground(Color.WHITE);
        btnFilter.addActionListener(e -> loadReport());
        filterBar.add(btnFilter);

        filterBar.add(Box.createHorizontalStrut(20));
        lblTotalRevenue = new JLabel("Tổng doanh thu: —");
        lblTotalRevenue.setFont(UIConstants.FONT_SUBTITLE);
        lblTotalRevenue.setForeground(UIConstants.REVENUE_PURPLE);
        filterBar.add(lblTotalRevenue);

        header.add(filterBar, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new GridLayout(1, 3, 12, 0));
        body.setOpaque(false);

        // Revenue by day
        RoundedPanel revCard = new RoundedPanel(UIConstants.CARD_RADIUS);
        revCard.setBackground(UIConstants.CARD_BG);
        revCard.setLayout(new BorderLayout(0, 8));
        revCard.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lblRev = new JLabel("Doanh thu theo ngày");
        lblRev.setFont(UIConstants.FONT_SUBTITLE);
        lblRev.setForeground(UIConstants.TEXT_PRIMARY);
        revCard.add(lblRev, BorderLayout.NORTH);

        revenueModel = new DefaultTableModel(new String[]{"Ngày", "Doanh thu"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable revTable = new JTable(revenueModel);
        revTable.setRowHeight(30);
        revTable.setFont(UIConstants.FONT_LABEL);
        revTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        revTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        revCard.add(new JScrollPane(revTable), BorderLayout.CENTER);

        // Top medicines
        RoundedPanel medCard = new RoundedPanel(UIConstants.CARD_RADIUS);
        medCard.setBackground(UIConstants.CARD_BG);
        medCard.setLayout(new BorderLayout(0, 8));
        medCard.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lblMed = new JLabel("Top thuốc bán chạy");
        lblMed.setFont(UIConstants.FONT_SUBTITLE);
        lblMed.setForeground(UIConstants.TEXT_PRIMARY);
        medCard.add(lblMed, BorderLayout.NORTH);

        topMedModel = new DefaultTableModel(new String[]{"Thuốc", "SL"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable medTable = new JTable(topMedModel);
        medTable.setRowHeight(30);
        medTable.setFont(UIConstants.FONT_LABEL);
        medTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        medTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        medCard.add(new JScrollPane(medTable), BorderLayout.CENTER);

        // Top diseases
        RoundedPanel disCard = new RoundedPanel(UIConstants.CARD_RADIUS);
        disCard.setBackground(UIConstants.CARD_BG);
        disCard.setLayout(new BorderLayout(0, 8));
        disCard.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lblDis = new JLabel("Bệnh thường gặp");
        lblDis.setFont(UIConstants.FONT_SUBTITLE);
        lblDis.setForeground(UIConstants.TEXT_PRIMARY);
        disCard.add(lblDis, BorderLayout.NORTH);

        topDiseaseModel = new DefaultTableModel(new String[]{"Mã ICD / Bệnh", "SL"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable disTable = new JTable(topDiseaseModel);
        disTable.setRowHeight(30);
        disTable.setFont(UIConstants.FONT_LABEL);
        disTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        disTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        disCard.add(new JScrollPane(disTable), BorderLayout.CENTER);

        body.add(revCard);
        body.add(medCard);
        body.add(disCard);
        return body;
    }

    private void loadReport() {
        LocalDate from, to;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            from = LocalDate.parse(txtFrom.getText().trim(), fmt);
            to = LocalDate.parse(txtTo.getText().trim(), fmt);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Định dạng ngày không hợp lệ (yyyy-MM-dd).");
            return;
        }

        // Revenue by day
        revenueModel.setRowCount(0);
        try {
            Map<LocalDate, Double> revenueByDay = reportDAO.getRevenueByDay(from, to);
            for (Map.Entry<LocalDate, Double> entry : revenueByDay.entrySet()) {
                revenueModel.addRow(new Object[]{
                        entry.getKey().toString(),
                        String.format("%,.0f đ", entry.getValue())
                });
            }
            double total = reportDAO.getRevenueTotal(from, to);
            lblTotalRevenue.setText(String.format("Tổng doanh thu: %,.0f đ", total));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải doanh thu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        // Top medicines
        topMedModel.setRowCount(0);
        try {
            List<Object[]> topMeds = reportDAO.getTopMedicines(10);
            for (Object[] row : topMeds) {
                topMedModel.addRow(row);
            }
        } catch (Exception ignored) {}

        // Top diseases
        topDiseaseModel.setRowCount(0);
        try {
            List<Object[]> topDiseases = reportDAO.getTopDiseases(10);
            for (Object[] row : topDiseases) {
                topDiseaseModel.addRow(row);
            }
        } catch (Exception ignored) {}
    }
}
