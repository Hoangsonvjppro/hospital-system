package com.hospital.gui.panels;

import com.hospital.dao.DashboardDAO;
import com.hospital.dao.ReportDAO;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.components.StatCard;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Panel Báo cáo Doanh thu — dành cho Admin.
 * <p>
 * Bao gồm:
 * <ul>
 *   <li>KPI cards (Doanh thu trong kỳ, Đã thanh toán, Chờ thanh toán, Tổng lượt khám)</li>
 *   <li>Bar chart doanh thu theo ngày (JFreeChart)</li>
 *   <li>Bảng Top thuốc bán chạy</li>
 *   <li>Bảng Top bệnh phổ biến</li>
 *   <li>Bộ lọc ngày nhanh (7 ngày / 30 ngày / tùy chỉnh)</li>
 * </ul>
 */
public class AdminReportPanel extends JPanel {

    private final DashboardDAO dashDAO  = new DashboardDAO();
    private final ReportDAO reportDAO   = new ReportDAO();
    private final NumberFormat moneyFmt = NumberFormat.getInstance(new Locale("vi", "VN"));
    private final DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd/MM");

    // ── Date range ────────────────────────────────────────────
    private LocalDate dateFrom;
    private LocalDate dateTo;

    // ── Refreshable containers ────────────────────────────────
    private JPanel kpiPanel;
    private JPanel chartContainer;
    private JPanel topMedicinesContainer;
    private JPanel topDiseasesContainer;
    private JTextField txtFrom;
    private JTextField txtTo;

    public AdminReportPanel() {
        dateFrom = LocalDate.now().minusDays(6);
        dateTo   = LocalDate.now();

        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 20, 24));
        initComponents();
    }

    // ══════════════════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════════════════

    private void initComponents() {
        // ── Top: header + filter ──
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setOpaque(false);

        JLabel title = new JLabel("  Báo cáo Doanh thu");
        title.setIcon(com.hospital.gui.IconManager.getIcon("trending_up", 20, 20));
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        topBar.add(title, BorderLayout.WEST);

        topBar.add(createFilterPanel(), BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── Center: scrollable content ──
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // KPI cards row
        kpiPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        kpiPanel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(Box.createVerticalStrut(12));
        content.add(kpiPanel);

        // Bar chart
        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setOpaque(false);
        chartContainer.setAlignmentX(LEFT_ALIGNMENT);
        chartContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        content.add(Box.createVerticalStrut(16));
        content.add(chartContainer);

        // Bottom: top medicines + top diseases side by side
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(LEFT_ALIGNMENT);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        topMedicinesContainer = new JPanel(new BorderLayout());
        topMedicinesContainer.setOpaque(false);
        topDiseasesContainer = new JPanel(new BorderLayout());
        topDiseasesContainer.setOpaque(false);

        bottomRow.add(topMedicinesContainer);
        bottomRow.add(topDiseasesContainer);

        content.add(Box.createVerticalStrut(16));
        content.add(bottomRow);
        content.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        // Initial data load
        refreshAll();
    }

    // ══════════════════════════════════════════════════════════
    //  FILTER PANEL
    // ══════════════════════════════════════════════════════════

    private JPanel createFilterPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        p.setOpaque(false);

        JLabel lblFrom = new JLabel("Từ:");
        lblFrom.setFont(UIConstants.FONT_BODY);
        txtFrom = new JTextField(8);
        txtFrom.setFont(UIConstants.FONT_BODY);
        txtFrom.setText(dateFrom.format(DateTimeFormatter.ISO_LOCAL_DATE));

        JLabel lblTo = new JLabel("Đến:");
        lblTo.setFont(UIConstants.FONT_BODY);
        txtTo = new JTextField(8);
        txtTo.setFont(UIConstants.FONT_BODY);
        txtTo.setText(dateTo.format(DateTimeFormatter.ISO_LOCAL_DATE));

        JButton btnFilter = new JButton("Lọc");
        btnFilter.setIcon(com.hospital.gui.IconManager.getIcon("search", 14, 14));
        btnFilter.setFont(UIConstants.FONT_BODY);
        btnFilter.setBackground(UIConstants.PRIMARY);
        btnFilter.setForeground(Color.WHITE);
        btnFilter.setFocusPainted(false);
        btnFilter.addActionListener(e -> applyFilter());

        JButton btn7d = new JButton("7 ngày");
        btn7d.setFont(UIConstants.FONT_SMALL);
        btn7d.addActionListener(e -> quickRange(6));

        JButton btn30d = new JButton("30 ngày");
        btn30d.setFont(UIConstants.FONT_SMALL);
        btn30d.addActionListener(e -> quickRange(29));

        p.add(btn7d);
        p.add(btn30d);
        p.add(Box.createHorizontalStrut(8));
        p.add(lblFrom);
        p.add(txtFrom);
        p.add(lblTo);
        p.add(txtTo);
        p.add(btnFilter);

        return p;
    }

    private void quickRange(int daysBack) {
        dateFrom = LocalDate.now().minusDays(daysBack);
        dateTo   = LocalDate.now();
        txtFrom.setText(dateFrom.format(DateTimeFormatter.ISO_LOCAL_DATE));
        txtTo.setText(dateTo.format(DateTimeFormatter.ISO_LOCAL_DATE));
        refreshAll();
    }

    private void applyFilter() {
        try {
            dateFrom = LocalDate.parse(txtFrom.getText().trim());
            dateTo   = LocalDate.parse(txtTo.getText().trim());
            if (dateFrom.isAfter(dateTo)) {
                JOptionPane.showMessageDialog(this,
                        "Ngày bắt đầu phải trước ngày kết thúc!",
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Định dạng ngày không hợp lệ (yyyy-MM-dd)!",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  REFRESH ALL SECTIONS
    // ══════════════════════════════════════════════════════════

    private void refreshAll() {
        refreshKPI();
        refreshChart();
        refreshTopMedicines();
        refreshTopDiseases();
        revalidate();
        repaint();
    }

    // ══════════════════════════════════════════════════════════
    //  KPI CARDS
    // ══════════════════════════════════════════════════════════

    private void refreshKPI() {
        kpiPanel.removeAll();

        double totalRevenue = reportDAO.getRevenueTotal(dateFrom, dateTo);
        Map<String, Integer> statusCnt = reportDAO.getInvoiceCountByStatus(dateFrom, dateTo);
        int paidCount    = statusCnt.getOrDefault("PAID", 0);
        int pendingCount = statusCnt.getOrDefault("PENDING", 0);
        int totalVisits  = dashDAO.countTotalVisits();

        kpiPanel.add(new StatCard("Doanh Thu",
                moneyFmt.format(totalRevenue) + " đ",
                dateFrom.format(dayFmt) + " → " + dateTo.format(dayFmt),
                "money", UIConstants.SUCCESS_GREEN));
        kpiPanel.add(new StatCard("Đã Thanh Toán",
                String.valueOf(paidCount), "Hóa đơn trong kỳ",
                "check", UIConstants.STATUS_DONE));
        kpiPanel.add(new StatCard("Chờ Thanh Toán",
                String.valueOf(pendingCount), "Cần xử lý",
                "hourglass", UIConstants.WARNING_ORANGE));
        kpiPanel.add(new StatCard("Tổng Lượt Khám",
                String.valueOf(totalVisits), "Tất cả thời gian",
                "stethoscope", UIConstants.PRIMARY));
    }

    // ══════════════════════════════════════════════════════════
    //  BAR CHART — DOANH THU THEO NGÀY (JFreeChart)
    // ══════════════════════════════════════════════════════════

    private void refreshChart() {
        chartContainer.removeAll();

        Map<LocalDate, Double> revenueMap = reportDAO.getRevenueByDay(dateFrom, dateTo);

        // Build dataset — đơn vị nghìn VNĐ
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<LocalDate, Double> entry : revenueMap.entrySet()) {
            String label = entry.getKey().format(dayFmt);
            dataset.addValue(entry.getValue() / 1000.0, "Doanh thu (nghìn VNĐ)", label);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                null,                          // title (đã có header)
                "Ngày",                        // x-axis label
                "Doanh thu (nghìn VNĐ)",       // y-axis label
                dataset,
                PlotOrientation.VERTICAL,
                false,   // legend
                true,    // tooltips
                false    // urls
        );

        styleChart(chart);

        // Wrap chart in card
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel chartTitle = new JLabel("  Doanh thu theo ngày");
        chartTitle.setIcon(com.hospital.gui.IconManager.getIcon("dashboard", 18, 18));
        chartTitle.setFont(UIConstants.FONT_SUBTITLE);
        chartTitle.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(chartTitle, BorderLayout.NORTH);

        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(600, 260));
        cp.setMouseWheelEnabled(false);
        cp.setPopupMenu(null);
        card.add(cp, BorderLayout.CENTER);

        chartContainer.add(card, BorderLayout.CENTER);
    }

    /**
     * Style biểu đồ để phù hợp với FlatLaf dark-light + UIConstants palette.
     */
    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(UIConstants.CARD_BG);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(UIConstants.FIELD_BORDER);
        plot.setRangeGridlinePaint(UIConstants.FIELD_BORDER);
        plot.setOutlinePaint(null);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, UIConstants.PRIMARY);
        renderer.setBarPainter(new StandardBarPainter());   // flat, no gradient
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(0.06);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setTickLabelFont(UIConstants.FONT_SMALL);
        domainAxis.setTickLabelPaint(UIConstants.TEXT_SECONDARY);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(UIConstants.FONT_SMALL);
        rangeAxis.setTickLabelPaint(UIConstants.TEXT_SECONDARY);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    }

    // ══════════════════════════════════════════════════════════
    //  TOP THUỐC BÁN CHẠY
    // ══════════════════════════════════════════════════════════

    private void refreshTopMedicines() {
        topMedicinesContainer.removeAll();

        List<Object[]> data = reportDAO.getTopMedicines(10);

        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel lbl = new JLabel("  Top thuốc bán chạy");
        lbl.setIcon(com.hospital.gui.IconManager.getIcon("pill", 18, 18));
        lbl.setFont(UIConstants.FONT_SUBTITLE);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(lbl, BorderLayout.NORTH);

        String[] cols = {"#", "Tên thuốc", "SL bán", "Doanh thu"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        int stt = 1;
        for (Object[] row : data) {
            model.addRow(new Object[]{
                    stt++,
                    row[0],
                    row[1],
                    moneyFmt.format(((Number) row[2]).doubleValue()) + " đ"
            });
        }

        JTable table = createStyledTable(model);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        topMedicinesContainer.add(card, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════
    //  TOP BỆNH PHỔ BIẾN
    // ══════════════════════════════════════════════════════════

    private void refreshTopDiseases() {
        topDiseasesContainer.removeAll();

        List<Object[]> data = reportDAO.getTopDiseases(10);

        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel lbl = new JLabel("  Top bệnh phổ biến");
        lbl.setIcon(com.hospital.gui.IconManager.getIcon("hospital", 18, 18));
        lbl.setFont(UIConstants.FONT_SUBTITLE);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(lbl, BorderLayout.NORTH);

        String[] cols = {"#", "Chẩn đoán", "Số ca"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        int stt = 1;
        for (Object[] row : data) {
            model.addRow(new Object[]{
                    stt++,
                    row[0],
                    row[1]
            });
        }

        JTable table = createStyledTable(model);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        topDiseasesContainer.add(card, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════
    //  TABLE HELPER
    // ══════════════════════════════════════════════════════════

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(UIConstants.FONT_BODY);
        table.setSelectionBackground(UIConstants.PRIMARY_BG_SOFT);
        table.setGridColor(UIConstants.FIELD_BORDER);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.getTableHeader().setFont(UIConstants.FONT_BODY.deriveFont(Font.BOLD));
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        table.getTableHeader().setForeground(UIConstants.TEXT_PRIMARY);
        table.getTableHeader().setReorderingAllowed(false);

        // Center align # column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        return table;
    }
}
