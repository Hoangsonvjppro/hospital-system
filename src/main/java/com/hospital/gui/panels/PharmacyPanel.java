package com.hospital.gui.panels;

import com.hospital.bus.DispensingBUS;
import com.hospital.bus.InvoiceBUS;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.components.StatusBadge;
import com.hospital.model.Dispensing;
import com.hospital.model.DispensingItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel phát thuốc nâng cao — dành cho Dược sĩ.
 *
 * Tính năng:
 * - Danh sách đơn thuốc chờ phát (từ bác sĩ gửi sang)
 * - Chọn đơn → hiển thị chi tiết: Thuốc | SL yêu cầu | Tồn kho | SL phát | Đơn giá | Thành tiền
 * - Nếu tồn kho không đủ → cho phép phát một phần (dispensedQty < requestedQty)
 * - Tổng tiền thuốc tự động tính
 * - Nút "Phát thuốc" → trừ kho, tạo dispensing record
 * - Cảnh báo thuốc hết hạn, tồn kho thấp
 */
public class PharmacyPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(PharmacyPanel.class.getName());
    private static final DecimalFormat MONEY_FMT = new DecimalFormat("#,###");

    private final DispensingBUS dispensingBUS = new DispensingBUS();
    private final InvoiceBUS invoiceBUS = new InvoiceBUS();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    // Left: pending prescriptions
    private DefaultTableModel prescTableModel;
    private JTable prescTable;
    private List<Dispensing> pendingList = new ArrayList<>();

    // Right: dispensing items
    private DefaultTableModel itemTableModel;
    private JTable itemTable;
    private List<DispensingItem> currentItems = new ArrayList<>();
    private JLabel lblPrescInfo;
    private JLabel lblTotalAmount;
    private JTextArea txtNotes;
    private JPanel warningPanel;

    // Current selection
    private Dispensing selectedDispensing;

    public PharmacyPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
        loadPendingPrescriptions();
    }

    private void initComponents() {
        // Title
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lblTitle = new JLabel("  Phát thuốc theo đơn");
        lblTitle.setIcon(com.hospital.gui.IconManager.getIcon("syringe", 20, 20));
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(lblTitle, BorderLayout.WEST);

        RoundedButton btnRefreshTop = new RoundedButton("Làm mới");
        btnRefreshTop.setIcon(com.hospital.gui.IconManager.getIcon("refresh", 14, 14));
        btnRefreshTop.addActionListener(e -> loadPendingPrescriptions());
        header.add(btnRefreshTop, BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        add(header, BorderLayout.NORTH);

        // Split: left 35% prescriptions, right 65% details
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createLeftPanel(), createRightPanel());
        split.setDividerLocation(460);
        split.setResizeWeight(0.35);
        split.setBorder(null);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);
    }

    // ════════════════════════════════════════════════════════════
    //  LEFT: Pending prescriptions
    // ════════════════════════════════════════════════════════════

    private JPanel createLeftPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setLayout(new BorderLayout(0, 8));
        card.setBackground(UIConstants.CARD_BG);
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lblHeader = new JLabel("Đơn thuốc chờ phát");
        lblHeader.setFont(UIConstants.FONT_SUBTITLE);
        lblHeader.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(lblHeader, BorderLayout.NORTH);

        // Table
        String[] cols = {"#", "ID Đơn", "Bệnh nhân", "Tổng tiền", "Thời gian"};
        prescTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        prescTable = new JTable(prescTableModel);
        prescTable.setFont(UIConstants.FONT_BODY);
        prescTable.setRowHeight(38);
        prescTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        prescTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        prescTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        prescTable.setSelectionBackground(UIConstants.PRIMARY_BG_SOFT);
        prescTable.setShowVerticalLines(false);
        prescTable.setGridColor(UIConstants.BORDER_COLOR);

        // Column widths
        prescTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        int[] widths = {30, 70, 140, 100, 100};
        for (int i = 0; i < widths.length; i++) {
            prescTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Center alignment for # and ID
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        prescTable.getColumnModel().getColumn(0).setCellRenderer(center);
        prescTable.getColumnModel().getColumn(1).setCellRenderer(center);

        prescTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onPrescriptionSelected();
        });

        JScrollPane scroll = new JScrollPane(prescTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ════════════════════════════════════════════════════════════
    //  RIGHT: Dispensing items + action
    // ════════════════════════════════════════════════════════════

    private JPanel createRightPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setLayout(new BorderLayout(0, 8));
        card.setBackground(UIConstants.CARD_BG);
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Header info
        lblPrescInfo = new JLabel("Chọn đơn thuốc để xem chi tiết");
        lblPrescInfo.setFont(UIConstants.FONT_SUBTITLE);
        lblPrescInfo.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(lblPrescInfo, BorderLayout.NORTH);

        // Center: warning + table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);

        // Warning panel
        warningPanel = new JPanel();
        warningPanel.setLayout(new BoxLayout(warningPanel, BoxLayout.Y_AXIS));
        warningPanel.setOpaque(false);
        warningPanel.setVisible(false);
        centerPanel.add(warningPanel, BorderLayout.NORTH);

        // Item table: Thuốc | ĐV | SL yêu cầu | Tồn kho | SL phát | Đơn giá | Thành tiền
        String[] cols = {"Tên thuốc", "ĐV", "SL yêu cầu", "Tồn kho", "SL phát", "Đơn giá", "Thành tiền"};
        itemTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 4; // Chỉ cho phép sửa "SL phát"
            }
            @Override
            public Class<?> getColumnClass(int col) {
                return switch (col) {
                    case 2, 3, 4 -> Integer.class;
                    default -> Object.class;
                };
            }
        };

        itemTable = new JTable(itemTableModel);
        itemTable.setFont(UIConstants.FONT_BODY);
        itemTable.setRowHeight(36);
        itemTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        itemTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        itemTable.setShowVerticalLines(false);
        itemTable.setGridColor(UIConstants.BORDER_COLOR);

        // Column widths
        int[] iw = {160, 50, 70, 70, 70, 90, 100};
        for (int i = 0; i < iw.length; i++) {
            itemTable.getColumnModel().getColumn(i).setPreferredWidth(iw[i]);
        }

        // Center alignment
        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(SwingConstants.CENTER);
        for (int c : new int[]{1, 2, 3, 4}) {
            itemTable.getColumnModel().getColumn(c).setCellRenderer(centerR);
        }

        // Money renderer
        DefaultTableCellRenderer moneyR = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (v != null) setText(MONEY_FMT.format(v) + " đ");
                setHorizontalAlignment(RIGHT);
                return this;
            }
        };
        itemTable.getColumnModel().getColumn(5).setCellRenderer(moneyR);
        itemTable.getColumnModel().getColumn(6).setCellRenderer(moneyR);

        // Stock warning renderer — highlight red if stock < requested
        itemTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(CENTER);
                if (v instanceof Integer stock) {
                    Object reqObj = t.getModel().getValueAt(r, 2);
                    int req = (reqObj instanceof Integer) ? (int) reqObj : 0;
                    if (stock < req) {
                        setForeground(UIConstants.ERROR_COLOR);
                        setFont(UIConstants.FONT_BOLD);
                    } else {
                        setForeground(UIConstants.TEXT_PRIMARY);
                        setFont(UIConstants.FONT_BODY);
                    }
                }
                return this;
            }
        });

        // Listen for SL phát changes → recalculate
        itemTableModel.addTableModelListener(e -> {
            if (e.getColumn() == 4) {
                recalculateTotal();
            }
        });

        JScrollPane scroll = new JScrollPane(itemTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        centerPanel.add(scroll, BorderLayout.CENTER);

        card.add(centerPanel, BorderLayout.CENTER);

        // Bottom: notes + total + action
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        // Notes
        JPanel notesPanel = new JPanel(new BorderLayout(4, 4));
        notesPanel.setOpaque(false);
        JLabel lblNotes = new JLabel("Ghi chú:");
        lblNotes.setFont(UIConstants.FONT_LABEL);
        txtNotes = new JTextArea(2, 20);
        txtNotes.setFont(UIConstants.FONT_BODY);
        txtNotes.setLineWrap(true);
        txtNotes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.FIELD_BORDER),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        notesPanel.add(lblNotes, BorderLayout.NORTH);
        notesPanel.add(new JScrollPane(txtNotes), BorderLayout.CENTER);
        bottomPanel.add(notesPanel, BorderLayout.CENTER);

        // Total + Button
        JPanel rightBottom = new JPanel();
        rightBottom.setOpaque(false);
        rightBottom.setLayout(new BoxLayout(rightBottom, BoxLayout.Y_AXIS));

        lblTotalAmount = new JLabel("Tổng: 0 đ");
        lblTotalAmount.setFont(UIConstants.FONT_HEADER);
        lblTotalAmount.setForeground(UIConstants.PRIMARY);
        lblTotalAmount.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightBottom.add(lblTotalAmount);
        rightBottom.add(Box.createVerticalStrut(8));

        RoundedButton btnDispense = new RoundedButton("Phát thuốc");
        btnDispense.setIcon(com.hospital.gui.IconManager.getIcon("check", 14, 14));
        btnDispense.setColors(UIConstants.SUCCESS_GREEN, UIConstants.SUCCESS_GREEN_DARK);
        btnDispense.setForeground(Color.WHITE);
        btnDispense.setFont(UIConstants.FONT_BUTTON);
        btnDispense.setPreferredSize(new Dimension(160, 40));
        btnDispense.setMaximumSize(new Dimension(160, 40));
        btnDispense.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btnDispense.addActionListener(e -> onDispense());
        rightBottom.add(btnDispense);

        bottomPanel.add(rightBottom, BorderLayout.EAST);

        card.add(bottomPanel, BorderLayout.SOUTH);

        return card;
    }

    // ════════════════════════════════════════════════════════════
    //  DATA LOADING
    // ════════════════════════════════════════════════════════════

    private void loadPendingPrescriptions() {
        try {
            pendingList = dispensingBUS.getPendingDispensings();
            prescTableModel.setRowCount(0);
            int stt = 1;
            for (Dispensing d : pendingList) {
                prescTableModel.addRow(new Object[]{
                        stt++,
                        d.getPrescriptionId(),
                        d.getPatientName() != null ? d.getPatientName() : "—",
                        MONEY_FMT.format(d.getTotalAmount()) + " đ",
                        d.getCreatedAt() != null ? d.getCreatedAt().toLocalDate().toString() : ""
                });
            }
            // Clear right
            clearRight();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi tải đơn thuốc chờ phát", e);
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onPrescriptionSelected() {
        int row = prescTable.getSelectedRow();
        if (row < 0 || row >= pendingList.size()) {
            clearRight();
            return;
        }

        selectedDispensing = pendingList.get(row);
        long prescId = selectedDispensing.getPrescriptionId();

        try {
            currentItems = dispensingBUS.getItemsForPrescription(prescId);
            loadItemTable();
            lblPrescInfo.setText("Đơn thuốc #" + prescId + " — " + selectedDispensing.getPatientName()
                    + " — " + currentItems.size() + " loại thuốc");
            checkWarnings();
            recalculateTotal();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi tải chi tiết đơn thuốc #" + prescId, e);
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadItemTable() {
        itemTableModel.setRowCount(0);
        for (DispensingItem item : currentItems) {
            itemTableModel.addRow(new Object[]{
                    item.getMedicineName(),
                    item.getUnit() != null ? item.getUnit() : "",
                    item.getRequestedQuantity(),
                    item.getStockQty(),
                    item.getDispensedQuantity(),
                    item.getUnitPrice(),
                    item.getUnitPrice().multiply(BigDecimal.valueOf(item.getDispensedQuantity()))
            });
        }
    }

    private void recalculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < itemTableModel.getRowCount(); i++) {
            Object dispQtyObj = itemTableModel.getValueAt(i, 4);
            int dispQty = 0;
            if (dispQtyObj instanceof Integer) {
                dispQty = (int) dispQtyObj;
            } else if (dispQtyObj != null) {
                try { dispQty = Integer.parseInt(dispQtyObj.toString()); } catch (NumberFormatException ignored) {}
            }

            if (i < currentItems.size()) {
                currentItems.get(i).setDispensedQuantity(dispQty);
                BigDecimal lineTotal = currentItems.get(i).getUnitPrice().multiply(BigDecimal.valueOf(dispQty));
                total = total.add(lineTotal);
                // Update Thành tiền column
                itemTableModel.setValueAt(lineTotal, i, 6);
            }
        }
        lblTotalAmount.setText("Tổng: " + MONEY_FMT.format(total) + " đ");
    }

    private void checkWarnings() {
        warningPanel.removeAll();
        warningPanel.setVisible(false);

        List<String> warnings = new ArrayList<>();
        for (DispensingItem item : currentItems) {
            if (item.getStockQty() < item.getRequestedQuantity()) {
                warnings.add("⚠ " + item.getMedicineName() + ": Tồn kho (" + item.getStockQty()
                        + ") < Yêu cầu (" + item.getRequestedQuantity() + ")");
            }
            if (item.getStockQty() == 0) {
                warnings.add("🚫 " + item.getMedicineName() + ": HẾT HÀNG");
            }
        }

        if (!warnings.isEmpty()) {
            warningPanel.setVisible(true);
            JPanel warnCard = new JPanel(new BorderLayout(8, 4));
            warnCard.setBackground(UIConstants.ALERT_AMBER_BG);
            warnCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIConstants.ALERT_AMBER_BORDER),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            JLabel icon = new JLabel();
            icon.setIcon(com.hospital.gui.IconManager.getIcon("warning", 16, 16));
            icon.setFont(UIConstants.FONT_SUBTITLE);
            warnCard.add(icon, BorderLayout.WEST);
            JTextArea warnText = new JTextArea(String.join("\n", warnings));
            warnText.setFont(UIConstants.FONT_CAPTION);
            warnText.setForeground(UIConstants.TEXT_PRIMARY);
            warnText.setBackground(UIConstants.ALERT_AMBER_BG);
            warnText.setEditable(false);
            warnText.setLineWrap(true);
            warnCard.add(warnText, BorderLayout.CENTER);
            warningPanel.add(warnCard);
            warningPanel.revalidate();
        }
    }

    private void clearRight() {
        selectedDispensing = null;
        currentItems.clear();
        itemTableModel.setRowCount(0);
        lblPrescInfo.setText("Chọn đơn thuốc để xem chi tiết");
        lblTotalAmount.setText("Tổng: 0 đ");
        warningPanel.setVisible(false);
        txtNotes.setText("");
    }

    // ════════════════════════════════════════════════════════════
    //  DISPENSE ACTION
    // ════════════════════════════════════════════════════════════

    private void onDispense() {
        if (selectedDispensing == null || currentItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn thuốc cần phát.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Sync lại SL phát từ table
        for (int i = 0; i < itemTableModel.getRowCount() && i < currentItems.size(); i++) {
            Object qtyObj = itemTableModel.getValueAt(i, 4);
            int qty = 0;
            if (qtyObj instanceof Integer) qty = (int) qtyObj;
            else if (qtyObj != null) {
                try { qty = Integer.parseInt(qtyObj.toString()); } catch (NumberFormatException ignored) {}
            }
            currentItems.get(i).setDispensedQuantity(qty);
        }

        // Validate
        boolean anyDispensed = currentItems.stream().anyMatch(it -> it.getDispensedQuantity() > 0);
        if (!anyDispensed) {
            JOptionPane.showMessageDialog(this, "Không có thuốc nào được phát (SL phát = 0).",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check tồn kho
        List<String> stockIssues = new ArrayList<>();
        for (DispensingItem item : currentItems) {
            if (item.getDispensedQuantity() > item.getStockQty()) {
                stockIssues.add("- " + item.getMedicineName() + ": Phát " + item.getDispensedQuantity()
                        + " > Tồn kho " + item.getStockQty());
            }
        }
        if (!stockIssues.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Số lượng phát vượt tồn kho:\n" + String.join("\n", stockIssues),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check partial
        boolean isPartial = currentItems.stream()
                .anyMatch(it -> it.getDispensedQuantity() < it.getRequestedQuantity());

        String confirmMsg = isPartial
                ? "Đơn thuốc sẽ được phát MỘT PHẦN.\nBạn có chắc chắn?"
                : "Xác nhận phát thuốc cho đơn #" + selectedDispensing.getPrescriptionId() + "?";

        int confirm = JOptionPane.showConfirmDialog(this, confirmMsg,
                "Xác nhận phát thuốc", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            long dispensingId = dispensingBUS.processDispensing(
                    selectedDispensing.getPrescriptionId(),
                    selectedDispensing.getPatientId(),
                    currentItems,
                    null, // TODO: pass current pharmacist user_id
                    txtNotes.getText().trim()
            );

            // Tạo hóa đơn tự động
            boolean invoiceCreated = false;
            try {
                var presc = prescriptionDAO.findById(selectedDispensing.getPrescriptionId());
                if (presc != null) {
                    invoiceBUS.createInvoiceFromMedicalRecord(presc.getMedicalRecordId());
                    invoiceCreated = true;
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Không thể tạo hóa đơn tự động", ex);
                JOptionPane.showMessageDialog(this,
                        "Phát thuốc thành công nhưng KHÔNG thể tạo hóa đơn tự động.\nLỗi: " + ex.getMessage()
                                + "\n\nVui lòng liên hệ Kế toán để tạo hóa đơn thủ công.",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }

            JOptionPane.showMessageDialog(this,
                    "Phát thuốc thành công!\nMã phiếu: #" + dispensingId
                            + (isPartial ? "\n(Phát một phần)" : "")
                            + (invoiceCreated ? "\nHóa đơn đã được tạo tự động." : ""),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            loadPendingPrescriptions();

        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (DataAccessException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
