package com.hospital.gui.panels;

import com.hospital.bus.InvoiceBUS;
import com.hospital.bus.event.EventBus;
import com.hospital.bus.event.PaymentCompletedEvent;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.util.AsyncTask;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.components.StatusBadge;
import com.hospital.model.Invoice;
import com.hospital.model.InvoiceMedicineDetail;
import com.hospital.model.InvoiceServiceDetail;
import com.hospital.util.InvoicePrinter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel danh sách hóa đơn — dành cho Kế toán.
 * Hiển thị tất cả hóa đơn, lọc theo trạng thái, xem chi tiết.
 */
public class InvoiceListPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(InvoiceListPanel.class.getName());
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat VND = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    private final InvoiceBUS invoiceBUS = new InvoiceBUS();
    private DefaultTableModel tableModel;
    private JTable table;
    private JComboBox<String> cboStatus;
    private JTextField txtSearch;
    private List<Invoice> currentList;

    public InvoiceListPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
        loadData("ALL");

        // Subscribe to payment completed events - auto refresh
        EventBus.getInstance().subscribe(PaymentCompletedEvent.class, evt -> {
            SwingUtilities.invokeLater(() -> onStatusFilter());
        });
    }

    // ════════════════════════════════════════════════════════════
    //  INIT UI
    // ════════════════════════════════════════════════════════════

    private void initComponents() {
        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("  Danh sách hóa đơn");
        lblTitle.setIcon(com.hospital.gui.IconManager.getIcon("document", 20, 20));
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(lblTitle, BorderLayout.WEST);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filters.setOpaque(false);

        filters.add(createLabel("Trạng thái:"));
        cboStatus = new JComboBox<>(new String[]{"Tất cả", "Chờ thanh toán", "Đã thanh toán", "Đã hủy"});
        cboStatus.setFont(UIConstants.FONT_BODY);
        cboStatus.addActionListener(e -> onStatusFilter());
        filters.add(cboStatus);

        txtSearch = new JTextField(16);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm mã HĐ, tên BN...");
        txtSearch.setFont(UIConstants.FONT_BODY);
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { filterTable(); }
        });
        filters.add(txtSearch);

        RoundedButton btnRefresh = new RoundedButton("Làm mới");
        btnRefresh.setIcon(com.hospital.gui.IconManager.getIcon("refresh", 14, 14));
        btnRefresh.setBackground(UIConstants.TEXT_SECONDARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> onStatusFilter());
        filters.add(btnRefresh);

        header.add(filters, BorderLayout.EAST);
        return header;
    }

    private JPanel createTablePanel() {
        String[] cols = {"ID", "Mã HĐ", "Bệnh nhân", "Ngày tạo",
                "Phí khám", "Tiền thuốc", "Tổng cộng", "Trạng thái", "Thanh toán"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(UIConstants.FONT_BODY);
        table.setRowHeight(36);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setShowGrid(true);

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(40);     // ID
        cm.getColumn(1).setPreferredWidth(80);     // Mã HĐ
        cm.getColumn(2).setPreferredWidth(160);    // Tên BN
        cm.getColumn(3).setPreferredWidth(130);    // Ngày tạo
        cm.getColumn(4).setPreferredWidth(100);    // Phí khám
        cm.getColumn(5).setPreferredWidth(100);    // Tiền thuốc
        cm.getColumn(6).setPreferredWidth(110);    // Tổng cộng
        cm.getColumn(7).setPreferredWidth(110);    // Trạng thái
        cm.getColumn(8).setPreferredWidth(100);    // Thanh toán

        // Status badge renderer
        cm.getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                if (val instanceof String s && !s.isEmpty()) {
                    return new StatusBadge(s);
                }
                return super.getTableCellRendererComponent(t, val, sel, foc, r, c);
            }
        });

        // Right-click: view details
        JPopupMenu popup = new JPopupMenu();
        JMenuItem miDetail = new JMenuItem("Xem chi tiết");
        miDetail.setIcon(com.hospital.gui.IconManager.getIcon("clipboard", 14, 14));
        miDetail.addActionListener(e -> viewDetail());
        popup.add(miDetail);
        table.setComponentPopupMenu(popup);

        // Double-click to view detail
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) viewDetail();
            }
        });

        RoundedPanel wrapper = new RoundedPanel(14);
        wrapper.setLayout(new BorderLayout());
        wrapper.setBackground(UIConstants.CARD_BG);
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        return wrapper;
    }

    // ════════════════════════════════════════════════════════════
    //  DATA
    // ════════════════════════════════════════════════════════════

    private void loadData(String statusFilter) {
        AsyncTask.run(
            () -> switch (statusFilter) {
                case "PENDING" -> invoiceBUS.getPendingInvoices();
                case "PAID"    -> invoiceBUS.getPaidInvoices();
                default        -> invoiceBUS.findAll();
            },
            list -> { currentList = list; refreshTable(list); },
            ex -> {
                LOGGER.log(Level.SEVERE, "Lỗi tải hóa đơn", ex);
                JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        );
    }

    private void refreshTable(List<Invoice> list) {
        tableModel.setRowCount(0);
        for (Invoice inv : list) {
            tableModel.addRow(new Object[]{
                    inv.getId(),
                    inv.getInvoiceCode(),
                    inv.getPatientName() != null ? inv.getPatientName() : "BN#" + inv.getPatientId(),
                    inv.getInvoiceDate() != null ? inv.getInvoiceDate().format(DTF) : "",
                    VND.format(inv.getExamFee()),
                    VND.format(inv.getMedicineFee()),
                    VND.format(inv.getTotalAmount()),
                    inv.getStatusDisplay(),
                    inv.getPaymentMethodDisplay()
            });
        }
    }

    private void onStatusFilter() {
        int idx = cboStatus.getSelectedIndex();
        String filter = switch (idx) {
            case 1  -> "PENDING";
            case 2  -> "PAID";
            case 3  -> "CANCELLED";
            default -> "ALL";
        };
        loadData(filter);
    }

    private void filterTable() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (currentList == null) return;
        if (keyword.isEmpty()) { refreshTable(currentList); return; }
        List<Invoice> filtered = currentList.stream()
                .filter(inv -> inv.getInvoiceCode().toLowerCase().contains(keyword)
                        || (inv.getPatientName() != null && inv.getPatientName().toLowerCase().contains(keyword)))
                .toList();
        refreshTable(filtered);
    }

    // ════════════════════════════════════════════════════════════
    //  VIEW DETAIL
    // ════════════════════════════════════════════════════════════

    private void viewDetail() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            Invoice inv = invoiceBUS.getInvoiceDetails(id);
            if (inv == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn.");
                return;
            }
            showDetailDialog(inv);
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy chi tiết hóa đơn", ex);
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDetailDialog(Invoice inv) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Chi tiết hóa đơn " + inv.getInvoiceCode(), Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(600, 520);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        content.setBackground(UIConstants.CARD_BG);

        // Header info
        JPanel info = new JPanel(new GridLayout(0, 2, 8, 4));
        info.setOpaque(false);
        info.add(createLabel("Mã HĐ:")); info.add(new JLabel(inv.getInvoiceCode()));
        info.add(createLabel("Bệnh nhân:"));
        info.add(new JLabel(inv.getPatientName() != null ? inv.getPatientName() : "BN#" + inv.getPatientId()));
        info.add(createLabel("Ngày tạo:"));
        info.add(new JLabel(inv.getInvoiceDate() != null ? inv.getInvoiceDate().format(DTF) : ""));
        info.add(createLabel("Trạng thái:")); info.add(new JLabel(inv.getStatusDisplay()));
        info.add(createLabel("Phí khám:")); info.add(new JLabel(VND.format(inv.getExamFee())));
        info.add(createLabel("Tiền thuốc:")); info.add(new JLabel(VND.format(inv.getMedicineFee())));
        info.add(createLabel("Phí khác:")); info.add(new JLabel(VND.format(inv.getOtherFee())));
        info.add(createLabel("Giảm giá:")); info.add(new JLabel(VND.format(inv.getDiscount())));
        JLabel lblTotal = new JLabel(VND.format(inv.getTotalAmount()));
        lblTotal.setFont(UIConstants.FONT_SUBTITLE);
        lblTotal.setForeground(UIConstants.PRIMARY);
        info.add(createLabel("TỔNG CỘNG:")); info.add(lblTotal);
        content.add(info, BorderLayout.NORTH);

        // Tabs for service details and medicine details
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIConstants.FONT_BODY);

        // Service details tab
        DefaultTableModel svcModel = new DefaultTableModel(
                new String[]{"Dịch vụ", "SL", "Đơn giá", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable svcTable = new JTable(svcModel);
        svcTable.setFont(UIConstants.FONT_BODY);
        svcTable.setRowHeight(30);
        for (InvoiceServiceDetail d : inv.getServiceDetails()) {
            svcModel.addRow(new Object[]{
                    d.getServiceName(), d.getQuantity(),
                    VND.format(d.getUnitPrice()), VND.format(d.getLineTotal())
            });
        }
        tabs.addTab("Dịch vụ (" + inv.getServiceDetails().size() + ")", new JScrollPane(svcTable));

        // Medicine details tab
        DefaultTableModel medModel = new DefaultTableModel(
                new String[]{"Thuốc", "SL", "Đơn giá", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable medTable = new JTable(medModel);
        medTable.setFont(UIConstants.FONT_BODY);
        medTable.setRowHeight(30);
        for (InvoiceMedicineDetail d : inv.getMedicineDetails()) {
            medModel.addRow(new Object[]{
                    d.getMedicineName(), d.getQuantity(),
                    VND.format(d.getUnitPrice()), VND.format(d.getLineTotal())
            });
        }
        tabs.addTab("Thuốc (" + inv.getMedicineDetails().size() + ")", new JScrollPane(medTable));

        content.add(tabs, BorderLayout.CENTER);

        // Close button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        RoundedButton btnPrint = new RoundedButton("🖨️ In hóa đơn");
        btnPrint.setBackground(UIConstants.PRIMARY);
        btnPrint.setForeground(Color.WHITE);
        btnPrint.addActionListener(e -> printInvoice(inv, dlg));
        btnPanel.add(btnPrint);

        RoundedButton btnClose = new RoundedButton("Đóng");
        btnClose.setBackground(UIConstants.TEXT_SECONDARY);
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> dlg.dispose());
        btnPanel.add(btnClose);
        content.add(btnPanel, BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════
    //  PRINT INVOICE
    // ════════════════════════════════════════════════════════════

    private void printInvoice(Invoice inv, JDialog parentDlg) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu hóa đơn PDF");
        chooser.setSelectedFile(new File(inv.getInvoiceCode() + ".pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));

        if (chooser.showSaveDialog(parentDlg) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        try {
            InvoicePrinter.exportPdf(inv, file.getAbsolutePath());

            int open = JOptionPane.showConfirmDialog(parentDlg,
                    "Đã xuất hóa đơn thành công!\nFile: " + file.getName()
                            + "\n\nBạn có muốn mở file ngay?",
                    "Thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (open == JOptionPane.YES_OPTION) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Lỗi xuất PDF hóa đơn", ex);
            JOptionPane.showMessageDialog(parentDlg,
                    "Lỗi xuất PDF: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  HELPER
    // ════════════════════════════════════════════════════════════

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        return lbl;
    }
}
