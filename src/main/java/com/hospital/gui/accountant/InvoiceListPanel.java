package com.hospital.gui.accountant;

import com.hospital.bus.InvoiceBUS;
import com.hospital.gui.common.*;
import com.hospital.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Danh sách hóa đơn — xem tất cả, lọc theo trạng thái.
 */
public class InvoiceListPanel extends JPanel {

    private final InvoiceBUS invoiceBUS = new InvoiceBUS();

    private DefaultTableModel invoiceModel;
    private JTable invoiceTable;
    private JComboBox<String> cboFilter;

    public InvoiceListPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        loadInvoices("ALL");
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("📄 Danh sách hóa đơn");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterBar.setOpaque(false);
        filterBar.add(new JLabel("Lọc:"));
        cboFilter = new JComboBox<>(new String[]{"Tất cả", "Chờ thanh toán", "Đã thanh toán", "Đã hủy"});
        cboFilter.setFont(UIConstants.FONT_BODY);
        cboFilter.addActionListener(e -> {
            String selected = (String) cboFilter.getSelectedItem();
            String status = switch (selected) {
                case "Chờ thanh toán" -> "PENDING";
                case "Đã thanh toán" -> "PAID";
                case "Đã hủy" -> "CANCELLED";
                default -> "ALL";
            };
            loadInvoices(status);
        });
        filterBar.add(cboFilter);

        RoundedButton btnRefresh = new RoundedButton("🔄");
        btnRefresh.setBackground(UIConstants.PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> loadInvoices("ALL"));
        filterBar.add(btnRefresh);

        header.add(filterBar, BorderLayout.EAST);
        return header;
    }

    private JPanel createBody() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        invoiceModel = new DefaultTableModel(
                new String[]{"Mã HD", "Bệnh nhân", "SĐT", "Phí khám", "Thuốc", "DV", "Tổng", "Trạng thái", "PT TT", "Ngày"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        invoiceTable = new JTable(invoiceModel);
        invoiceTable.setRowHeight(34);
        invoiceTable.setFont(UIConstants.FONT_LABEL);
        invoiceTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        invoiceTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);

        JScrollPane scroll = new JScrollPane(invoiceTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void loadInvoices(String statusFilter) {
        invoiceModel.setRowCount(0);
        try {
            List<Invoice> list;
            if ("PENDING".equals(statusFilter)) {
                list = invoiceBUS.getPendingInvoices();
            } else if ("PAID".equals(statusFilter)) {
                list = invoiceBUS.getPaidInvoices();
            } else {
                list = invoiceBUS.findAll();
            }

            for (Invoice inv : list) {
                if ("CANCELLED".equals(statusFilter) && !"CANCELLED".equals(inv.getStatus())) continue;

                String statusDisplay = switch (inv.getStatus()) {
                    case "PENDING" -> "Chờ TT";
                    case "PAID" -> "Đã TT";
                    case "CANCELLED" -> "Đã hủy";
                    default -> inv.getStatus();
                };
                String payMethodDisplay = inv.getPaymentMethod() != null ? switch (inv.getPaymentMethod()) {
                    case "CASH" -> "Tiền mặt";
                    case "TRANSFER" -> "CK";
                    case "CARD" -> "Thẻ";
                    default -> inv.getPaymentMethod();
                } : "";

                invoiceModel.addRow(new Object[]{
                        inv.getInvoiceCode(),
                        inv.getPatientName() != null ? inv.getPatientName() : "BN #" + inv.getPatientId(),
                        inv.getPatientPhone() != null ? inv.getPatientPhone() : "",
                        String.format("%,.0f", inv.getExamFee()),
                        String.format("%,.0f", inv.getMedicineFee()),
                        String.format("%,.0f", inv.getServiceFee()),
                        String.format("%,.0f", inv.getTotalAmount()),
                        statusDisplay,
                        payMethodDisplay,
                        inv.getInvoiceDate() != null ? inv.getInvoiceDate().toLocalDate().toString() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải hóa đơn: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
