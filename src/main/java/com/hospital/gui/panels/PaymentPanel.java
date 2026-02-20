package com.hospital.gui.panels;

import com.hospital.bus.InvoiceBUS;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.components.StatCard;
import com.hospital.gui.components.StatusBadge;
import com.hospital.model.Invoice;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Trang Thanh toán.
 */
public class PaymentPanel extends JPanel {

    private final InvoiceBUS bus = new InvoiceBUS();
    private DefaultTableModel tableModel;
    private JTable table;
    private StatCard cardTotal, cardPaid, cardPending;

    public PaymentPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Thanh toán");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY_RED);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        body.add(createStatCards(), BorderLayout.NORTH);
        body.add(createInvoiceTable(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    // ── Stat Cards ────────────────────────────────────────────────────────────
    private JPanel createStatCards() {
        JPanel p = new JPanel(new GridLayout(1, 3, 14, 0));
        p.setOpaque(false);

        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi","VN"));
        double total   = bus.getTotalRevenue();
        int    paid    = bus.getPaidInvoices().size();
        int    pending = bus.getPendingInvoices().size();

        cardTotal   = new StatCard("Tổng doanh thu",
                fmt.format(total / 1_000_000.0) + " tr", "Đã thanh toán",
                "\uD83D\uDCB5", UIConstants.SUCCESS_GREEN);
        cardPaid    = new StatCard("Hóa đơn đã thanh toán",
                String.valueOf(paid), "Hôm nay",
                "\u2705", UIConstants.STATUS_DONE);
        cardPending = new StatCard("Chờ thanh toán",
                String.valueOf(pending), "Cần xử lý",
                "\u23F3", UIConstants.WARNING_ORANGE);

        p.add(cardTotal);
        p.add(cardPaid);
        p.add(cardPending);
        return p;
    }

    // ── Table ─────────────────────────────────────────────────────────────────
    private JPanel createInvoiceTable() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setOpaque(false);

        JTextField txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm hóa đơn...");

        RoundedButton btnAll     = new RoundedButton("Tất cả");
        btnAll.setPreferredSize(new Dimension(80, 32));
        btnAll.addActionListener(e -> loadTable(bus.findAll()));

        RoundedButton btnPending = new RoundedButton("Chờ TT");
        btnPending.setColors(UIConstants.WARNING_ORANGE, UIConstants.WARNING_ORANGE.darker());
        btnPending.setPreferredSize(new Dimension(90, 32));
        btnPending.addActionListener(e -> loadTable(bus.getPendingInvoices()));

        RoundedButton btnPay = new RoundedButton("Xác nhận TT");
        btnPay.setColors(UIConstants.SUCCESS_GREEN, UIConstants.SUCCESS_GREEN.darker());
        btnPay.setPreferredSize(new Dimension(130, 32));
        btnPay.addActionListener(e -> paySelected());

        topBar.add(txtSearch, BorderLayout.CENTER);
        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        acts.setOpaque(false);
        acts.add(btnAll); acts.add(btnPending); acts.add(btnPay);
        topBar.add(acts, BorderLayout.EAST);
        card.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Mã HĐ", "Mã BN", "Tên bệnh nhân", "Bác sĩ", "Ngày khám", "Phí khám", "Phí thuốc", "Tổng tiền", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(42);
        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(UIConstants.RED_BG_SOFT);
        table.setSelectionForeground(UIConstants.TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setBackground(UIConstants.TABLE_HEADER_BG);
        header.setFont(UIConstants.FONT_BOLD);

        // Status badge
        table.getColumnModel().getColumn(8).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean s, boolean f, int r, int c) {
                    StatusBadge b = new StatusBadge(v == null ? "" : v.toString());
                    b.setHorizontalAlignment(CENTER);
                    return b;
                }
            });

        // Currency renderer
        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
            final NumberFormat fmt = NumberFormat.getInstance(new Locale("vi","VN"));
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (v instanceof Number) setText(fmt.format(v) + " đ");
                setHorizontalAlignment(RIGHT);
                return this;
            }
        };
        for (int col : new int[]{5, 6, 7}) table.getColumnModel().getColumn(col).setCellRenderer(moneyRenderer);

        int[] widths = {70, 70, 160, 140, 100, 100, 100, 110, 120};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        loadTable(bus.findAll());
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    // ── Logic ─────────────────────────────────────────────────────────────────
    private void loadTable(List<Invoice> list) {
        tableModel.setRowCount(0);
        for (Invoice inv : list) {
            tableModel.addRow(new Object[]{
                inv.getInvoiceCode(), inv.getPatientCode(), inv.getPatientName(),
                inv.getDoctorName(), inv.getExamDate(),
                inv.getExamFee(), inv.getMedicineFee(), inv.getTotalAmount(),
                inv.getStatus()
            });
        }
    }

    private void paySelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn hóa đơn cần thanh toán."); return; }
        String status = (String) tableModel.getValueAt(row, 8);
        if ("Đã thanh toán".equals(status)) {
            JOptionPane.showMessageDialog(this, "Hóa đơn này đã được thanh toán."); return;
        }
        String[] methods = {"Tiền mặt", "Chuyển khoản", "Thẻ ngân hàng"};
        String method = (String) JOptionPane.showInputDialog(this, "Chọn phương thức thanh toán:",
                "Thanh toán", JOptionPane.QUESTION_MESSAGE, null, methods, methods[0]);
        if (method == null) return;

        String code = (String) tableModel.getValueAt(row, 0);
        bus.findAll().stream()
            .filter(inv -> inv.getInvoiceCode().equals(code))
            .findFirst()
            .ifPresent(inv -> { bus.markAsPaid(inv.getId(), method); refreshStats(); loadTable(bus.findAll()); });
        JOptionPane.showMessageDialog(this, "Thanh toán thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshStats() {
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi","VN"));
        cardTotal.updateValue(fmt.format(bus.getTotalRevenue() / 1_000_000.0) + " tr");
        cardPaid.updateValue(String.valueOf(bus.getPaidInvoices().size()));
        cardPending.updateValue(String.valueOf(bus.getPendingInvoices().size()));
    }
}
