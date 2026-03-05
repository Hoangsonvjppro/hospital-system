package com.hospital.gui.panels;

import com.hospital.bus.InvoiceBUS;
import com.hospital.dao.MedicalRecordDAO;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.components.StatCard;
import com.hospital.gui.components.StatusBadge;
import com.hospital.model.Invoice;
import com.hospital.model.InvoiceMedicineDetail;
import com.hospital.model.InvoiceServiceDetail;
import com.hospital.util.InvoicePrinter;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Panel Thanh toán — layout 2 cột:
 *   Trái (40%): Stat cards + danh sách hóa đơn (lọc theo trạng thái, tìm kiếm).
 *   Phải (60%): Chi tiết hóa đơn (info bệnh nhân, bảng dịch vụ/thuốc, form thanh toán).
 */
public class PaymentPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(PaymentPanel.class.getName());

    private final InvoiceBUS bus = new InvoiceBUS();
    private final NumberFormat moneyFmt = NumberFormat.getInstance(new Locale("vi", "VN"));

    // ── Left ──
    private DefaultTableModel invoiceTableModel;
    private JTable invoiceTable;
    private StatCard cardTotal, cardPaid, cardPending;
    private JTextField txtSearch;
    private String currentFilter = "ALL";        // ALL | PENDING | PAID
    private List<Invoice> currentList;

    // ── Right ──
    private JPanel rightPanel;
    private JLabel lblPatientName, lblPatientCode, lblDoctorName, lblInvoiceCode, lblExamDate;
    private DefaultTableModel detailTableModel;
    private JTable detailTable;
    private JLabel lblExamFee, lblMedicineFee, lblServiceFee, lblDiscount, lblTotal;
    private JTextField txtPaidAmount;
    private JTextField txtDiscountAmount, txtDiscountReason;
    private JLabel lblChange;
    private RoundedButton btnPay, btnCancel;
    private JPanel paymentFormPanel;

    private Invoice selectedInvoice;

    public PaymentPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
        loadInvoices();
    }

    private void initComponents() {
        // ── Tiêu đề ──
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Thanh toán");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        header.add(title, BorderLayout.WEST);

        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        add(header, BorderLayout.NORTH);

        // ── Split Pane ──
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createLeftPanel(), createRightPanel());
        splitPane.setDividerLocation(520);
        splitPane.setResizeWeight(0.4);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        add(splitPane, BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════════════════
    //  BÊN TRÁI — Stat cards + danh sách hóa đơn
    // ═══════════════════════════════════════════════════════════

    private JPanel createLeftPanel() {
        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setOpaque(false);
        left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        left.add(createStatCards(), BorderLayout.NORTH);
        left.add(createInvoiceListCard(), BorderLayout.CENTER);
        return left;
    }

    private JPanel createStatCards() {
        JPanel p = new JPanel(new GridLayout(1, 3, 10, 0));
        p.setOpaque(false);

        double totalRev = bus.getTotalRevenue();
        int paid    = bus.getPaidInvoices().size();
        int pending = bus.getPendingInvoices().size();

        cardTotal   = new StatCard("Tổng doanh thu",
                moneyFmt.format(totalRev / 1_000_000.0) + " tr", "Đã thanh toán",
                "\uD83D\uDCB5", UIConstants.SUCCESS_GREEN);
        cardPaid    = new StatCard("Đã thanh toán",
                String.valueOf(paid), "Hóa đơn",
                "\u2705", UIConstants.STATUS_DONE);
        cardPending = new StatCard("Chờ thanh toán",
                String.valueOf(pending), "Cần xử lý",
                "\u23F3", UIConstants.WARNING_ORANGE);

        p.add(cardTotal);
        p.add(cardPaid);
        p.add(cardPending);
        return p;
    }

    private JPanel createInvoiceListCard() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // ── Top bar: Search + filter buttons ──
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm (tên BN, mã HĐ...)");
        txtSearch.setFont(UIConstants.FONT_BODY);
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filterTable(); }
        });

        JPanel filterBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        filterBtns.setOpaque(false);

        RoundedButton btnAll = new RoundedButton("Tất cả");
        btnAll.setPreferredSize(new Dimension(72, 30));
        btnAll.addActionListener(e -> { currentFilter = "ALL"; loadInvoices(); });

        RoundedButton btnPendingFilter = new RoundedButton("Chờ TT");
        btnPendingFilter.setColors(UIConstants.WARNING_ORANGE, UIConstants.WARNING_ORANGE.darker());
        btnPendingFilter.setPreferredSize(new Dimension(72, 30));
        btnPendingFilter.addActionListener(e -> { currentFilter = "PENDING"; loadInvoices(); });

        RoundedButton btnPaidFilter = new RoundedButton("Đã TT");
        btnPaidFilter.setColors(UIConstants.SUCCESS_GREEN, UIConstants.SUCCESS_GREEN.darker());
        btnPaidFilter.setPreferredSize(new Dimension(72, 30));
        btnPaidFilter.addActionListener(e -> { currentFilter = "PAID"; loadInvoices(); });

        filterBtns.add(btnAll);
        filterBtns.add(btnPendingFilter);
        filterBtns.add(btnPaidFilter);

        topBar.add(txtSearch, BorderLayout.CENTER);
        topBar.add(filterBtns, BorderLayout.EAST);
        card.add(topBar, BorderLayout.NORTH);

        // ── Table ──
        String[] cols = {"Mã HĐ", "Tên bệnh nhân", "Tổng tiền", "Trạng thái"};
        invoiceTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        invoiceTable = new JTable(invoiceTableModel);
        invoiceTable.setRowHeight(40);
        invoiceTable.setGridColor(UIConstants.BORDER_COLOR);
        invoiceTable.setShowVerticalLines(false);
        invoiceTable.setSelectionBackground(UIConstants.PRIMARY_BG_SOFT);
        invoiceTable.setSelectionForeground(UIConstants.TEXT_PRIMARY);
        invoiceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invoiceTable.setFont(UIConstants.FONT_BODY);

        JTableHeader th = invoiceTable.getTableHeader();
        th.setBackground(UIConstants.TABLE_HEADER_BG);
        th.setFont(UIConstants.FONT_BOLD);

        // Status badge renderer
        invoiceTable.getColumnModel().getColumn(3).setCellRenderer(
            (table, value, isSelected, hasFocus, row, column) -> {
                StatusBadge badge = new StatusBadge(value == null ? "" : value.toString());
                badge.setHorizontalAlignment(SwingConstants.CENTER);
                return badge;
            });

        // Money renderer col 2
        invoiceTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (v instanceof Number) setText(moneyFmt.format(v) + " đ");
                setHorizontalAlignment(RIGHT);
                setFont(UIConstants.FONT_BODY);
                return this;
            }
        });

        // Column widths
        int[] widths = {70, 150, 100, 100};
        for (int i = 0; i < widths.length; i++)
            invoiceTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Selection listener → load chi tiết
        invoiceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onInvoiceSelected();
        });

        JScrollPane scroll = new JScrollPane(invoiceTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ═══════════════════════════════════════════════════════════
    //  BÊN PHẢI — Chi tiết hóa đơn + form thanh toán
    // ═══════════════════════════════════════════════════════════

    private JPanel createRightPanel() {
        rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        // Placeholder khi chưa chọn hóa đơn
        showPlaceholder();
        return rightPanel;
    }

    private void showPlaceholder() {
        rightPanel.removeAll();
        JLabel lbl = new JLabel("Chọn một hóa đơn bên trái để xem chi tiết", SwingConstants.CENTER);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        rightPanel.add(lbl, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void showInvoiceDetail(Invoice inv) {
        rightPanel.removeAll();

        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        card.add(createDetailHeader(inv), BorderLayout.NORTH);
        card.add(createDetailBody(inv), BorderLayout.CENTER);
        card.add(createDetailFooter(inv), BorderLayout.SOUTH);

        rightPanel.add(card, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    // ── Header: Thông tin bệnh nhân ──
    private JPanel createDetailHeader(Invoice inv) {
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(0, 0, 14, 0)
        ));

        // Row 1: Invoice code + date
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);
        lblInvoiceCode = new JLabel(inv.getInvoiceCode());
        lblInvoiceCode.setFont(UIConstants.FONT_HEADER);
        lblInvoiceCode.setForeground(UIConstants.PRIMARY);
        lblExamDate = new JLabel(inv.getExamDate());
        lblExamDate.setFont(UIConstants.FONT_LABEL);
        lblExamDate.setForeground(UIConstants.TEXT_SECONDARY);
        row1.add(lblInvoiceCode, BorderLayout.WEST);
        row1.add(lblExamDate, BorderLayout.EAST);

        // Row 2: Patient info + Doctor
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);
        String patientInfo = (inv.getPatientName() != null ? inv.getPatientName() : "")
                + "  •  " + inv.getPatientCode();
        lblPatientName = new JLabel(patientInfo);
        lblPatientName.setFont(UIConstants.FONT_SUBTITLE);
        lblPatientName.setForeground(UIConstants.TEXT_PRIMARY);

        lblDoctorName = new JLabel("BS: " + (inv.getDoctorName() != null ? inv.getDoctorName() : "—"));
        lblDoctorName.setFont(UIConstants.FONT_LABEL);
        lblDoctorName.setForeground(UIConstants.TEXT_SECONDARY);
        row2.add(lblPatientName, BorderLayout.WEST);
        row2.add(lblDoctorName, BorderLayout.EAST);

        header.add(row1);
        header.add(row2);
        return header;
    }

    // ── Body: Bảng chi tiết dịch vụ + thuốc gộp ──
    private JPanel createDetailBody(Invoice inv) {
        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        JLabel lblTitle = new JLabel("Chi tiết hóa đơn");
        lblTitle.setFont(UIConstants.FONT_SUBTITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        body.add(lblTitle, BorderLayout.NORTH);

        // Table: STT | Tên | Loại | SL | Đơn giá | Thành tiền
        String[] cols = {"#", "Tên mục", "Loại", "SL", "Đơn giá", "Thành tiền"};
        detailTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        detailTable = new JTable(detailTableModel);
        detailTable.setRowHeight(34);
        detailTable.setGridColor(UIConstants.BORDER_COLOR);
        detailTable.setShowVerticalLines(false);
        detailTable.setFont(UIConstants.FONT_BODY);

        JTableHeader dth = detailTable.getTableHeader();
        dth.setBackground(UIConstants.TABLE_HEADER_BG);
        dth.setFont(UIConstants.FONT_BOLD);

        // Money renderer for col 4,5
        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (v instanceof Number) setText(moneyFmt.format(v) + " đ");
                setHorizontalAlignment(RIGHT);
                return this;
            }
        };
        detailTable.getColumnModel().getColumn(4).setCellRenderer(moneyRenderer);
        detailTable.getColumnModel().getColumn(5).setCellRenderer(moneyRenderer);

        // Center for # and SL
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        detailTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        detailTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        // Column widths
        int[] dw = {30, 180, 60, 40, 90, 100};
        for (int i = 0; i < dw.length; i++)
            detailTable.getColumnModel().getColumn(i).setPreferredWidth(dw[i]);

        // Populate detail rows
        loadDetailTable(inv);

        JScrollPane scroll = new JScrollPane(detailTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        body.add(scroll, BorderLayout.CENTER);

        return body;
    }

    // ── Footer: Tổng hợp tiền + form thanh toán ──
    private JPanel createDetailFooter(Invoice inv) {
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 0, 0, 0)
        ));

        // ── Tổng hợp chi phí ──
        JPanel summaryPanel = new JPanel(new GridLayout(0, 2, 8, 4));
        summaryPanel.setOpaque(false);
        summaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        lblExamFee     = addSummaryRow(summaryPanel, "Phí khám:", inv.getExamFee());
        lblServiceFee  = addSummaryRow(summaryPanel, "Phí dịch vụ:", inv.getOtherFee());
        lblMedicineFee = addSummaryRow(summaryPanel, "Tiền thuốc:", inv.getMedicineFee());
        lblDiscount    = addSummaryRow(summaryPanel, "Giảm giá:", inv.getDiscount());

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));

        // Total — highlighted
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel lblTotalLabel = new JLabel("TỔNG CỘNG:");
        lblTotalLabel.setFont(UIConstants.FONT_SUBTITLE);
        lblTotalLabel.setForeground(UIConstants.TEXT_PRIMARY);
        lblTotal = new JLabel(moneyFmt.format(inv.getTotalAmount()) + " đ");
        lblTotal.setFont(UIConstants.FONT_HEADER);
        lblTotal.setForeground(UIConstants.PRIMARY);
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        totalRow.add(lblTotalLabel, BorderLayout.WEST);
        totalRow.add(lblTotal, BorderLayout.EAST);

        footer.add(summaryPanel);
        footer.add(Box.createVerticalStrut(6));
        footer.add(sep);
        footer.add(Box.createVerticalStrut(8));
        footer.add(totalRow);

        // ── Form thanh toán (chỉ hiển thị nếu PENDING) ──
        paymentFormPanel = new JPanel();
        paymentFormPanel.setOpaque(false);
        paymentFormPanel.setLayout(new BoxLayout(paymentFormPanel, BoxLayout.Y_AXIS));
        paymentFormPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        if ("PENDING".equals(inv.getStatus())) {
            // Giảm giá (nhập tay)
            JPanel discountInputRow = new JPanel(new BorderLayout(8, 0));
            discountInputRow.setOpaque(false);
            discountInputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            JLabel lblDiscLabel = new JLabel("Giảm giá (VNĐ):");
            lblDiscLabel.setFont(UIConstants.FONT_BODY);
            txtDiscountAmount = new JTextField();
            txtDiscountAmount.setFont(UIConstants.FONT_BODY);
            txtDiscountAmount.setHorizontalAlignment(JTextField.RIGHT);
            txtDiscountAmount.putClientProperty("JTextField.placeholderText", "0");
            txtDiscountAmount.setText(inv.getDiscount() > 0 ? String.valueOf((long) inv.getDiscount()) : "");
            txtDiscountAmount.addKeyListener(new KeyAdapter() {
                @Override public void keyReleased(KeyEvent e) { updateTotalAfterDiscount(); }
            });
            discountInputRow.add(lblDiscLabel, BorderLayout.WEST);
            discountInputRow.add(txtDiscountAmount, BorderLayout.CENTER);

            JPanel discReasonRow = new JPanel(new BorderLayout(8, 0));
            discReasonRow.setOpaque(false);
            discReasonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            JLabel lblReasonLabel = new JLabel("Lý do giảm:");
            lblReasonLabel.setFont(UIConstants.FONT_BODY);
            txtDiscountReason = new JTextField();
            txtDiscountReason.setFont(UIConstants.FONT_BODY);
            txtDiscountReason.putClientProperty("JTextField.placeholderText", "Nhập lý do (nếu có)...");
            discReasonRow.add(lblReasonLabel, BorderLayout.WEST);
            discReasonRow.add(txtDiscountReason, BorderLayout.CENTER);

            paymentFormPanel.add(discountInputRow);
            paymentFormPanel.add(Box.createVerticalStrut(4));
            paymentFormPanel.add(discReasonRow);
            paymentFormPanel.add(Box.createVerticalStrut(8));

            // Tiền khách đưa
            JPanel paidRow = new JPanel(new BorderLayout(8, 0));
            paidRow.setOpaque(false);
            paidRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            JLabel lblPaidLabel = new JLabel("Tiền khách đưa:");
            lblPaidLabel.setFont(UIConstants.FONT_BODY);
            txtPaidAmount = new JTextField();
            txtPaidAmount.setFont(UIConstants.FONT_BODY);
            txtPaidAmount.setHorizontalAlignment(JTextField.RIGHT);
            txtPaidAmount.putClientProperty("JTextField.placeholderText", "Nhập số tiền...");
            txtPaidAmount.addKeyListener(new KeyAdapter() {
                @Override public void keyReleased(KeyEvent e) { updateChange(); }
            });
            paidRow.add(lblPaidLabel, BorderLayout.WEST);
            paidRow.add(txtPaidAmount, BorderLayout.CENTER);

            // Tiền thừa
            JPanel changeRow = new JPanel(new BorderLayout(8, 0));
            changeRow.setOpaque(false);
            changeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            JLabel lblChangeLabel = new JLabel("Tiền thừa:");
            lblChangeLabel.setFont(UIConstants.FONT_BODY);
            lblChange = new JLabel("0 đ");
            lblChange.setFont(UIConstants.FONT_SUBTITLE);
            lblChange.setForeground(UIConstants.SUCCESS_GREEN);
            lblChange.setHorizontalAlignment(SwingConstants.RIGHT);
            changeRow.add(lblChangeLabel, BorderLayout.WEST);
            changeRow.add(lblChange, BorderLayout.CENTER);

            // Phương thức thanh toán + Nút
            JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actionRow.setOpaque(false);
            actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

            btnCancel = new RoundedButton("Hủy HĐ");
            btnCancel.setColors(UIConstants.STATUS_CANCEL, UIConstants.STATUS_CANCEL.darker());
            btnCancel.setPreferredSize(new Dimension(90, 36));
            btnCancel.addActionListener(e -> cancelInvoice());

            RoundedButton btnPrintPending = new RoundedButton("In hóa đơn");
            btnPrintPending.setPreferredSize(new Dimension(120, 36));
            btnPrintPending.addActionListener(e -> printInvoice());

            btnPay = new RoundedButton("Thanh toán & In");
            btnPay.setColors(UIConstants.SUCCESS_GREEN, UIConstants.SUCCESS_GREEN_DARK);
            btnPay.setPreferredSize(new Dimension(150, 36));
            btnPay.addActionListener(e -> processPayment());

            actionRow.add(btnCancel);
            actionRow.add(btnPrintPending);
            actionRow.add(btnPay);

            paymentFormPanel.add(paidRow);
            paymentFormPanel.add(Box.createVerticalStrut(6));
            paymentFormPanel.add(changeRow);
            paymentFormPanel.add(Box.createVerticalStrut(12));
            paymentFormPanel.add(actionRow);
        } else {
            // Đã thanh toán — hiển thị thông tin
            JPanel paidInfoPanel = new JPanel(new GridLayout(0, 2, 8, 4));
            paidInfoPanel.setOpaque(false);
            addInfoRow(paidInfoPanel, "Trạng thái:", inv.getStatusDisplay());
            addInfoRow(paidInfoPanel, "Phương thức:", inv.getPaymentMethodDisplay());
            addInfoRow(paidInfoPanel, "Đã thanh toán:", moneyFmt.format(inv.getPaidAmount()) + " đ");
            addInfoRow(paidInfoPanel, "Tiền thừa:", moneyFmt.format(inv.getChangeAmount()) + " đ");
            paymentFormPanel.add(paidInfoPanel);

            // Nút In hóa đơn (đã thanh toán)
            paymentFormPanel.add(Box.createVerticalStrut(10));
            JPanel paidActionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            paidActionRow.setOpaque(false);
            paidActionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            RoundedButton btnPrintPaid = new RoundedButton("In hóa đơn");
            btnPrintPaid.setPreferredSize(new Dimension(120, 36));
            btnPrintPaid.addActionListener(e -> printInvoice());
            paidActionRow.add(btnPrintPaid);
            paymentFormPanel.add(paidActionRow);
        }

        footer.add(paymentFormPanel);
        return footer;
    }

    // ═══════════════════════════════════════════════════════════
    //  DATA LOADING
    // ═══════════════════════════════════════════════════════════

    private void loadInvoices() {
        currentList = switch (currentFilter) {
            case "PENDING" -> bus.getPendingInvoices();
            case "PAID"    -> bus.getPaidInvoices();
            default        -> bus.findAll();
        };
        filterTable();
    }

    private void filterTable() {
        String keyword = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        invoiceTableModel.setRowCount(0);
        for (Invoice inv : currentList) {
            if (!keyword.isEmpty()) {
                String searchable = (inv.getInvoiceCode() + " " + inv.getPatientName()
                        + " " + inv.getPatientCode()).toLowerCase();
                if (!searchable.contains(keyword)) continue;
            }
            invoiceTableModel.addRow(new Object[]{
                    inv.getInvoiceCode(),
                    inv.getPatientName(),
                    inv.getTotalAmount(),
                    inv.getStatusDisplay()
            });
        }
        // Clear selection
        invoiceTable.clearSelection();
        showPlaceholder();
    }

    private void onInvoiceSelected() {
        int row = invoiceTable.getSelectedRow();
        if (row < 0) {
            selectedInvoice = null;
            showPlaceholder();
            return;
        }
        String code = (String) invoiceTableModel.getValueAt(row, 0);
        // Tìm Invoice từ currentList
        selectedInvoice = currentList.stream()
                .filter(inv -> inv.getInvoiceCode().equals(code))
                .findFirst().orElse(null);
        if (selectedInvoice == null) {
            showPlaceholder();
            return;
        }
        // Load chi tiết đầy đủ (service + medicine details)
        selectedInvoice = bus.getInvoiceDetails(selectedInvoice.getId());
        if (selectedInvoice != null) {
            showInvoiceDetail(selectedInvoice);
        }
    }

    private void loadDetailTable(Invoice inv) {
        detailTableModel.setRowCount(0);
        int stt = 1;

        // Dịch vụ
        for (InvoiceServiceDetail d : inv.getServiceDetails()) {
            detailTableModel.addRow(new Object[]{
                    stt++, d.getServiceName(), "Dịch vụ",
                    d.getQuantity(), d.getUnitPrice(), d.getLineTotal()
            });
        }

        // Thuốc
        for (InvoiceMedicineDetail d : inv.getMedicineDetails()) {
            String name = d.getMedicineName();
            if (d.getUnit() != null && !d.getUnit().isEmpty()) {
                name += " (" + d.getUnit() + ")";
            }
            detailTableModel.addRow(new Object[]{
                    stt++, name, "Thuốc",
                    d.getQuantity(), d.getUnitPrice(), d.getLineTotal()
            });
        }

        // Phí khám — thêm dòng riêng nếu > 0
        if (inv.getExamFee() > 0) {
            detailTableModel.addRow(new Object[]{
                    stt, "Phí khám", "Khám", 1, inv.getExamFee(), inv.getExamFee()
            });
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  ACTIONS
    // ═══════════════════════════════════════════════════════════

    private void processPayment() {
        if (selectedInvoice == null) return;

        double total = selectedInvoice.getTotalAmount();
        double paid;
        try {
            String text = txtPaidAmount.getText().trim().replace(".", "").replace(",", "");
            paid = Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập số tiền hợp lệ.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (paid < total) {
            JOptionPane.showMessageDialog(this,
                    "Số tiền khách đưa không đủ.\nTổng cộng: " + moneyFmt.format(total) + " đ",
                    "Thiếu tiền", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double change = paid - total;

        // Chọn phương thức thanh toán
        String[] methods = {"Tiền mặt", "Chuyển khoản", "Thẻ ngân hàng"};
        String method = (String) JOptionPane.showInputDialog(this,
                "Chọn phương thức thanh toán:",
                "Phương thức", JOptionPane.QUESTION_MESSAGE, null, methods, methods[0]);
        if (method == null) return;

        try {
            // Lưu giảm giá + lý do trước khi thanh toán
            double discount = selectedInvoice.getDiscount();
            if (discount > 0) {
                String reason = (txtDiscountReason != null ? txtDiscountReason.getText().trim() : "");
                String existingNotes = selectedInvoice.getNotes() != null ? selectedInvoice.getNotes() : "";
                String newNotes = existingNotes;
                if (!reason.isEmpty()) {
                    newNotes = (existingNotes.isEmpty() ? "" : existingNotes + " | ") + "Giảm giá: " + reason;
                }
                selectedInvoice.setNotes(newNotes);
                bus.update(selectedInvoice);
            }

            boolean ok = bus.markAsPaid(selectedInvoice.getId(), method, paid, change);
            if (ok) {
                // Cập nhật trạng thái bệnh án → PAID
                if (selectedInvoice.getRecordId() != null && selectedInvoice.getRecordId() > 0) {
                    try {
                        new MedicalRecordDAO().updateStatus(selectedInvoice.getRecordId(), "PAID");
                    } catch (Exception ex) {
                        LOGGER.warning("Không thể cập nhật trạng thái bệnh án: " + ex.getMessage());
                    }
                }
                JOptionPane.showMessageDialog(this,
                        "Thanh toán thành công!\nTiền thừa: " + moneyFmt.format(change) + " đ",
                        "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                refreshAll();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Thanh toán thất bại. Vui lòng thử lại.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (DataAccessException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelInvoice() {
        if (selectedInvoice == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn hủy hóa đơn " + selectedInvoice.getInvoiceCode() + "?",
                "Xác nhận hủy", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bus.delete(selectedInvoice.getId());
                refreshAll();
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
            } catch (DataAccessException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printInvoice() {
        if (selectedInvoice == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn hóa đơn.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lấy đầy đủ chi tiết hóa đơn
        Invoice fullInvoice = bus.getInvoiceDetails(selectedInvoice.getId());
        if (fullInvoice == null) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy dữ liệu hóa đơn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Mở hộp thoại chọn nơi lưu file
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu hóa đơn PDF");
        chooser.setSelectedFile(new File(fullInvoice.getInvoiceCode() + ".pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        try {
            InvoicePrinter.exportPdf(fullInvoice, file.getAbsolutePath());

            int open = JOptionPane.showConfirmDialog(this,
                    "Đã xuất hóa đơn thành công!\nFile: " + file.getName()
                            + "\n\nBạn có muốn mở file ngay?",
                    "Thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (open == JOptionPane.YES_OPTION) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Lỗi xuất PDF", ex);
            String msg = ex.getMessage();
            if (msg != null && msg.contains("being used by another process")) {
                msg = "File đang được mở bởi chương trình khác.\nVui lòng đóng file rồi thử lại.";
            }
            JOptionPane.showMessageDialog(this,
                    "Lỗi xuất PDF: " + msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTotalAfterDiscount() {
        if (selectedInvoice == null) return;
        try {
            String text = txtDiscountAmount.getText().trim().replace(".", "").replace(",", "");
            double discountVal = text.isEmpty() ? 0 : Double.parseDouble(text);
            if (discountVal < 0) discountVal = 0;

            double subtotal = selectedInvoice.getExamFee() + selectedInvoice.getMedicineFee()
                    + selectedInvoice.getOtherFee();
            if (discountVal > subtotal) discountVal = subtotal;

            double newTotal = subtotal - discountVal;
            selectedInvoice.setDiscount(discountVal);
            selectedInvoice.setTotalAmount(newTotal);

            lblDiscount.setText(moneyFmt.format(discountVal) + " đ");
            lblTotal.setText(moneyFmt.format(newTotal) + " đ");

            updateChange();
        } catch (NumberFormatException ignored) {}
    }

    private void updateChange() {
        if (selectedInvoice == null || txtPaidAmount == null) return;
        try {
            String text = txtPaidAmount.getText().trim().replace(".", "").replace(",", "");
            double paid = Double.parseDouble(text);
            double change = paid - selectedInvoice.getTotalAmount();
            if (change >= 0) {
                lblChange.setText(moneyFmt.format(change) + " đ");
                lblChange.setForeground(UIConstants.SUCCESS_GREEN);
            } else {
                lblChange.setText("Thiếu " + moneyFmt.format(Math.abs(change)) + " đ");
                lblChange.setForeground(UIConstants.ERROR_COLOR);
            }
        } catch (NumberFormatException ex) {
            lblChange.setText("0 đ");
            lblChange.setForeground(UIConstants.TEXT_SECONDARY);
        }
    }

    private void refreshAll() {
        refreshStats();
        loadInvoices();
        showPlaceholder();
        selectedInvoice = null;
    }

    private void refreshStats() {
        cardTotal.updateValue(moneyFmt.format(bus.getTotalRevenue() / 1_000_000.0) + " tr");
        cardPaid.updateValue(String.valueOf(bus.getPaidInvoices().size()));
        cardPending.updateValue(String.valueOf(bus.getPendingInvoices().size()));
    }

    // ═══════════════════════════════════════════════════════════
    //  UI HELPERS
    // ═══════════════════════════════════════════════════════════

    private JLabel addSummaryRow(JPanel panel, String label, double value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        JLabel val = new JLabel(moneyFmt.format(value) + " đ");
        val.setFont(UIConstants.FONT_BODY);
        val.setForeground(UIConstants.TEXT_PRIMARY);
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lbl);
        panel.add(val);
        return val;
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        JLabel val = new JLabel(value != null ? value : "—");
        val.setFont(UIConstants.FONT_BOLD);
        val.setForeground(UIConstants.TEXT_PRIMARY);
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lbl);
        panel.add(val);
    }
}
