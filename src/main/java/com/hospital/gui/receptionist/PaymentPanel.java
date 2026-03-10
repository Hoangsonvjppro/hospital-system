package com.hospital.gui.receptionist;

import com.hospital.bus.InvoiceBUS;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * ⑦ Thanh toán — danh sách hóa đơn chờ + chi tiết + thu tiền.
 */
public class PaymentPanel extends JPanel {

    private final InvoiceBUS invoiceBUS = new InvoiceBUS();

    private DefaultTableModel invoiceModel;
    private JTable invoiceTable;
    private JLabel lblDetail, lblTotal;
    private DefaultTableModel lineModel;
    private JComboBox<String> cboPaymentMethod;
    private JTextField txtPaidAmount, txtChange;
    private Invoice selectedInvoice;

    public PaymentPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        loadPendingInvoices();
    }

    private JPanel createHeader() {
        JLabel title = new JLabel("💰 Thanh toán");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);

        RoundedButton btnRefresh = new RoundedButton("🔄 Làm mới");
        btnRefresh.setBackground(UIConstants.PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> loadPendingInvoices());
        header.add(btnRefresh, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createInvoiceListPanel(), createInvoiceDetailPanel());
        split.setDividerLocation(350);
        split.setResizeWeight(0.3);
        split.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(split, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Left: Pending invoices ──

    private JPanel createInvoiceListPanel() {
        RoundedPanel panel = new RoundedPanel(UIConstants.CARD_RADIUS);
        panel.setBackground(UIConstants.CARD_BG);
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbl = new JLabel("Hóa đơn chờ thanh toán");
        lbl.setFont(UIConstants.FONT_SUBTITLE);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        panel.add(lbl, BorderLayout.NORTH);

        invoiceModel = new DefaultTableModel(
                new String[]{"Mã HD", "Bệnh nhân", "Tổng tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        invoiceTable = new JTable(invoiceModel);
        invoiceTable.setRowHeight(36);
        invoiceTable.setFont(UIConstants.FONT_LABEL);
        invoiceTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        invoiceTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        invoiceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invoiceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onInvoiceSelected();
        });

        JScrollPane scroll = new JScrollPane(invoiceTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ── Right: Invoice detail + payment ──

    private JPanel createInvoiceDetailPanel() {
        RoundedPanel panel = new RoundedPanel(UIConstants.CARD_RADIUS);
        panel.setBackground(UIConstants.CARD_BG);
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        lblDetail = new JLabel("Chọn hóa đơn từ danh sách bên trái");
        lblDetail.setFont(UIConstants.FONT_SUBTITLE);
        lblDetail.setForeground(UIConstants.TEXT_PRIMARY);
        panel.add(lblDetail, BorderLayout.NORTH);

        // Line items table
        lineModel = new DefaultTableModel(
                new String[]{"Mô tả", "SL", "Đơn giá", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable lineTable = new JTable(lineModel);
        lineTable.setRowHeight(32);
        lineTable.setFont(UIConstants.FONT_LABEL);
        lineTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        lineTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);

        JScrollPane scroll = new JScrollPane(lineTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        panel.add(scroll, BorderLayout.CENTER);

        // Payment section
        panel.add(createPaymentSection(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPaymentSection() {
        JPanel paySection = new JPanel(new GridBagLayout());
        paySection.setOpaque(false);
        paySection.setBorder(new EmptyBorder(12, 0, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Total
        gbc.gridx = 0; gbc.gridy = 0;
        paySection.add(new JLabel("TỔNG CỘNG:"), gbc);
        gbc.gridx = 1;
        lblTotal = new JLabel("0 đ");
        lblTotal.setFont(UIConstants.FONT_SUBTITLE);
        lblTotal.setForeground(UIConstants.DANGER_RED);
        paySection.add(lblTotal, gbc);

        // Payment method
        gbc.gridx = 0; gbc.gridy = 1;
        paySection.add(new JLabel("PT thanh toán:"), gbc);
        gbc.gridx = 1;
        cboPaymentMethod = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản", "Thẻ"});
        paySection.add(cboPaymentMethod, gbc);

        // Paid amount
        gbc.gridx = 0; gbc.gridy = 2;
        paySection.add(new JLabel("Tiền nhận:"), gbc);
        gbc.gridx = 1;
        txtPaidAmount = new JTextField(15);
        txtPaidAmount.setFont(UIConstants.FONT_BODY);
        txtPaidAmount.addActionListener(e -> calculateChange());
        paySection.add(txtPaidAmount, gbc);

        // Change
        gbc.gridx = 0; gbc.gridy = 3;
        paySection.add(new JLabel("Tiền thừa:"), gbc);
        gbc.gridx = 1;
        txtChange = new JTextField(15);
        txtChange.setFont(UIConstants.FONT_BODY);
        txtChange.setEditable(false);
        txtChange.setText("0 đ");
        paySection.add(txtChange, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        RoundedButton btnPay = new RoundedButton("💰 Thanh toán");
        btnPay.setBackground(UIConstants.SUCCESS_GREEN);
        btnPay.setForeground(Color.WHITE);
        btnPay.addActionListener(e -> doPayment());

        RoundedButton btnPrint = new RoundedButton("🖨 In hóa đơn");
        btnPrint.setBackground(UIConstants.PRIMARY);
        btnPrint.setForeground(Color.WHITE);
        btnPrint.addActionListener(e -> {
            if (selectedInvoice != null)
                JOptionPane.showMessageDialog(this, "Chức năng in đang phát triển.");
        });

        btnPanel.add(btnPay);
        btnPanel.add(btnPrint);
        paySection.add(btnPanel, gbc);

        return paySection;
    }

    // ── Data loading ──

    private void loadPendingInvoices() {
        invoiceModel.setRowCount(0);
        selectedInvoice = null;
        lineModel.setRowCount(0);
        lblDetail.setText("Chọn hóa đơn từ danh sách bên trái");
        lblTotal.setText("0 đ");

        try {
            List<Invoice> pending = invoiceBUS.getPendingInvoices();
            for (Invoice inv : pending) {
                invoiceModel.addRow(new Object[]{
                        inv.getInvoiceCode(),
                        inv.getPatientName() != null ? inv.getPatientName() : "BN #" + inv.getPatientId(),
                        String.format("%,.0f đ", inv.getTotalAmount())
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải hóa đơn: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onInvoiceSelected() {
        int row = invoiceTable.getSelectedRow();
        if (row < 0) return;

        try {
            List<Invoice> pending = invoiceBUS.getPendingInvoices();
            if (row >= pending.size()) return;
            Invoice inv = pending.get(row);
            selectedInvoice = invoiceBUS.getInvoiceDetails(inv.getId());
            if (selectedInvoice == null) selectedInvoice = inv;

            lblDetail.setText("Chi tiết: " + selectedInvoice.getInvoiceCode() +
                    (selectedInvoice.getPatientName() != null ? " — " + selectedInvoice.getPatientName() : ""));

            lineModel.setRowCount(0);

            // Exam fee
            if (selectedInvoice.getExamFee() > 0) {
                lineModel.addRow(new Object[]{
                        "Phí khám bệnh", 1,
                        String.format("%,.0f", selectedInvoice.getExamFee()),
                        String.format("%,.0f", selectedInvoice.getExamFee())
                });
            }

            // Service details
            if (selectedInvoice.getServiceDetails() != null) {
                for (InvoiceServiceDetail sd : selectedInvoice.getServiceDetails()) {
                    lineModel.addRow(new Object[]{
                            sd.getServiceName(),
                            sd.getQuantity(),
                            String.format("%,.0f", sd.getUnitPrice()),
                            String.format("%,.0f", sd.getLineTotal())
                    });
                }
            }

            // Medicine details
            if (selectedInvoice.getMedicineDetails() != null) {
                for (InvoiceMedicineDetail md : selectedInvoice.getMedicineDetails()) {
                    lineModel.addRow(new Object[]{
                            md.getMedicineName(),
                            md.getQuantity(),
                            String.format("%,.0f", md.getUnitPrice()),
                            String.format("%,.0f", md.getLineTotal())
                    });
                }
            }

            lblTotal.setText(String.format("%,.0f đ", selectedInvoice.getTotalAmount()));
            txtPaidAmount.setText("");
            txtChange.setText("0 đ");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calculateChange() {
        if (selectedInvoice == null) return;
        try {
            double paid = Double.parseDouble(txtPaidAmount.getText().replace(",", "").trim());
            double change = paid - selectedInvoice.getTotalAmount();
            txtChange.setText(String.format("%,.0f đ", Math.max(0, change)));
        } catch (NumberFormatException e) {
            txtChange.setText("0 đ");
        }
    }

    private void doPayment() {
        if (selectedInvoice == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần thanh toán.");
            return;
        }

        String methodVi = (String) cboPaymentMethod.getSelectedItem();
        String method;
        switch (methodVi) {
            case "Chuyển khoản" -> method = "TRANSFER";
            case "Thẻ" -> method = "CARD";
            default -> method = "CASH";
        }

        double paidAmount;
        try {
            paidAmount = Double.parseDouble(txtPaidAmount.getText().replace(",", "").trim());
        } catch (NumberFormatException e) {
            paidAmount = selectedInvoice.getTotalAmount();
        }

        if (paidAmount < selectedInvoice.getTotalAmount()) {
            JOptionPane.showMessageDialog(this, "Số tiền nhận không đủ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double change = paidAmount - selectedInvoice.getTotalAmount();

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Thanh toán %s: %,.0f đ\nTiền nhận: %,.0f đ\nTiền thừa: %,.0f đ",
                        methodVi, selectedInvoice.getTotalAmount(), paidAmount, change),
                "Xác nhận thanh toán", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            invoiceBUS.markAsPaid(selectedInvoice.getId(), method, paidAmount, change);
            JOptionPane.showMessageDialog(this, "Thanh toán thành công!", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            loadPendingInvoices();
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi thanh toán: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
