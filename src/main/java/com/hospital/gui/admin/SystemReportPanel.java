package com.hospital.gui.admin;

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
 * Báo cáo hệ thống — doanh thu, top thuốc, top bệnh, trạng thái hóa đơn.
 */
public class SystemReportPanel extends JPanel {

    private final ReportDAO reportDAO = new ReportDAO();

    private JTextField txtFrom, txtTo;
    private DefaultTableModel tblRevenueModel, tblMedicineModel, tblDiseaseModel, tblInvoiceModel;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public SystemReportPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        loadReport();
    }

    /* ───── Header ───── */
    private JPanel createHeader() {
        JLabel title = new JLabel("📊 Báo cáo hệ thống");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        return header;
    }

    /* ───── Body ───── */
    private JPanel createBody() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);

        wrapper.add(createFilterBar(), BorderLayout.NORTH);
        wrapper.add(createTablesGrid(), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createFilterBar() {
        RoundedPanel bar = new RoundedPanel(UIConstants.CARD_RADIUS);
        bar.setBackground(UIConstants.CARD_BG);
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        bar.setBorder(new EmptyBorder(8, 16, 8, 16));

        LocalDate today = LocalDate.now();
        txtFrom = new JTextField(today.minusDays(30).format(FMT), 10);
        txtTo = new JTextField(today.format(FMT), 10);
        txtFrom.setFont(UIConstants.FONT_BODY);
        txtTo.setFont(UIConstants.FONT_BODY);

        RoundedButton btnFilter = new RoundedButton("📈 Lọc báo cáo");
        btnFilter.setBackground(UIConstants.PRIMARY);
        btnFilter.setForeground(Color.WHITE);
        btnFilter.addActionListener(e -> loadReport());

        bar.add(new JLabel("Từ:"));
        bar.add(txtFrom);
        bar.add(new JLabel("Đến:"));
        bar.add(txtTo);
        bar.add(btnFilter);
        return bar;
    }

    private JPanel createTablesGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setOpaque(false);

        /* 1. Revenue by day */
        tblRevenueModel = new DefaultTableModel(new String[]{"Ngày", "Doanh thu (VNĐ)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        grid.add(createTableCard("💰 Doanh thu theo ngày", tblRevenueModel));

        /* 2. Top medicines */
        tblMedicineModel = new DefaultTableModel(new String[]{"Tên thuốc", "Số lượng bán"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        grid.add(createTableCard("💊 Top thuốc bán chạy", tblMedicineModel));

        /* 3. Top diseases */
        tblDiseaseModel = new DefaultTableModel(new String[]{"Bệnh / Mã ICD", "Số ca"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        grid.add(createTableCard("🦠 Top bệnh phổ biến", tblDiseaseModel));

        /* 4. Invoice status */
        tblInvoiceModel = new DefaultTableModel(new String[]{"Trạng thái", "Số lượng"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        grid.add(createTableCard("🧾 Trạng thái hóa đơn", tblInvoiceModel));

        return grid;
    }

    private RoundedPanel createTableCard(String title, DefaultTableModel model) {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 4));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lbl = new JLabel(title);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(lbl, BorderLayout.NORTH);

        JTable tbl = new JTable(model);
        tbl.setRowHeight(28);
        tbl.setFont(UIConstants.FONT_BODY);
        tbl.getTableHeader().setFont(UIConstants.FONT_LABEL);
        tbl.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        card.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return card;
    }

    /* ───── Load ───── */
    private void loadReport() {
        try {
            LocalDate from = LocalDate.parse(txtFrom.getText().trim(), FMT);
            LocalDate to = LocalDate.parse(txtTo.getText().trim(), FMT);

            /* revenue by day */
            tblRevenueModel.setRowCount(0);
            Map<LocalDate, Double> rev = reportDAO.getRevenueByDay(from, to);
            for (Map.Entry<LocalDate, Double> e : rev.entrySet()) {
                tblRevenueModel.addRow(new Object[]{e.getKey().format(FMT), String.format("%,.0f", e.getValue())});
            }

            /* top medicines */
            tblMedicineModel.setRowCount(0);
            List<Object[]> meds = reportDAO.getTopMedicines(10);
            for (Object[] row : meds) {
                tblMedicineModel.addRow(new Object[]{row[0], row[1]});
            }

            /* top diseases */
            tblDiseaseModel.setRowCount(0);
            List<Object[]> dis = reportDAO.getTopDiseases(10);
            for (Object[] row : dis) {
                tblDiseaseModel.addRow(new Object[]{row[0], row[1]});
            }

            /* invoice status */
            tblInvoiceModel.setRowCount(0);
            Map<String, Integer> inv = reportDAO.getInvoiceCountByStatus(from, to);
            for (Map.Entry<String, Integer> e : inv.entrySet()) {
                tblInvoiceModel.addRow(new Object[]{e.getKey(), e.getValue()});
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải báo cáo: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
