package com.hospital.gui.pharmacist;

import com.hospital.bus.GoodsReceiptBUS;
import com.hospital.bus.MedicineBUS;
import com.hospital.bus.MedicineBatchBUS;
import com.hospital.bus.SupplierBUS;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.*;
import com.hospital.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Nhập hàng theo lô — tạo phiếu nhập, thêm thuốc theo lô, xác nhận.
 */
public class StockImportPanel extends JPanel {

    private final SupplierBUS supplierBUS = new SupplierBUS();
    private final MedicineBUS medicineBUS = new MedicineBUS();
    private final GoodsReceiptBUS receiptBUS = new GoodsReceiptBUS();
    private final MedicineBatchBUS batchBUS = new MedicineBatchBUS();

    private JComboBox<Supplier> cboSupplier;
    private JTextField txtMedicineSearch, txtBatchNumber, txtExpiryDate, txtQty, txtImportPrice;
    private JComboBox<Medicine> cboMedicine;
    private DefaultTableModel lineModel;
    private JLabel lblTotal;
    private final List<BatchLine> batchLines = new ArrayList<>();

    public StockImportPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        add(createActionBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JLabel title = new JLabel("📦 Nhập hàng theo lô");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);

        // Top: Receipt info
        body.add(createReceiptInfoPanel(), BorderLayout.NORTH);
        // Center: Line items
        body.add(createLineItemsPanel(), BorderLayout.CENTER);

        return body;
    }

    private JPanel createReceiptInfoPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        card.add(new JLabel("Nhà cung cấp:"));
        cboSupplier = new JComboBox<>();
        cboSupplier.setFont(UIConstants.FONT_BODY);
        cboSupplier.setPreferredSize(new Dimension(250, 32));
        loadSuppliers();
        card.add(cboSupplier);

        card.add(Box.createHorizontalStrut(16));
        card.add(new JLabel("Ngày nhập: " + LocalDate.now()));

        return card;
    }

    private JPanel createLineItemsPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Add line form
        JPanel addForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        addForm.setOpaque(false);

        addForm.add(new JLabel("Thuốc:"));
        cboMedicine = new JComboBox<>();
        cboMedicine.setFont(UIConstants.FONT_BODY);
        cboMedicine.setPreferredSize(new Dimension(200, 30));
        loadMedicines();
        addForm.add(cboMedicine);

        addForm.add(new JLabel("Số lô:"));
        txtBatchNumber = new JTextField(8);
        txtBatchNumber.setFont(UIConstants.FONT_BODY);
        addForm.add(txtBatchNumber);

        addForm.add(new JLabel("HSD:"));
        txtExpiryDate = new JTextField(8);
        txtExpiryDate.setFont(UIConstants.FONT_BODY);
        txtExpiryDate.setToolTipText("yyyy-MM-dd");
        addForm.add(txtExpiryDate);

        addForm.add(new JLabel("SL:"));
        txtQty = new JTextField(5);
        txtQty.setFont(UIConstants.FONT_BODY);
        addForm.add(txtQty);

        addForm.add(new JLabel("Giá nhập:"));
        txtImportPrice = new JTextField(8);
        txtImportPrice.setFont(UIConstants.FONT_BODY);
        addForm.add(txtImportPrice);

        RoundedButton btnAdd = new RoundedButton("+ Thêm");
        btnAdd.setBackground(UIConstants.SUCCESS_GREEN);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> addLineItem());
        addForm.add(btnAdd);

        card.add(addForm, BorderLayout.NORTH);

        // Table
        lineModel = new DefaultTableModel(
                new String[]{"Mã thuốc", "Tên thuốc", "Số lô", "HSD", "SL", "Giá nhập", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable lineTable = new JTable(lineModel);
        lineTable.setRowHeight(32);
        lineTable.setFont(UIConstants.FONT_LABEL);
        lineTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        lineTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);

        JScrollPane scroll = new JScrollPane(lineTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        card.add(scroll, BorderLayout.CENTER);

        // Total
        lblTotal = new JLabel("Tổng tiền: 0 đ");
        lblTotal.setFont(UIConstants.FONT_SUBTITLE);
        lblTotal.setForeground(UIConstants.PRIMARY);
        lblTotal.setBorder(new EmptyBorder(8, 0, 0, 0));
        card.add(lblTotal, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bar.setOpaque(false);

        RoundedButton btnClear = new RoundedButton("❌ Hủy");
        btnClear.setBackground(UIConstants.DANGER_RED);
        btnClear.setForeground(Color.WHITE);
        btnClear.addActionListener(e -> clearAll());

        RoundedButton btnConfirm = new RoundedButton("✅ Xác nhận nhập kho");
        btnConfirm.setBackground(UIConstants.SUCCESS_GREEN);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.addActionListener(e -> confirmImport());

        bar.add(btnClear);
        bar.add(btnConfirm);
        return bar;
    }

    // ── Data ──

    private void loadSuppliers() {
        try {
            List<Supplier> suppliers = supplierBUS.findActive();
            cboSupplier.removeAllItems();
            for (Supplier s : suppliers) {
                cboSupplier.addItem(s);
            }
        } catch (Exception ignored) {}
    }

    private void loadMedicines() {
        try {
            List<Medicine> meds = medicineBUS.findAll();
            cboMedicine.removeAllItems();
            for (Medicine m : meds) {
                if (m.isActive()) cboMedicine.addItem(m);
            }
        } catch (Exception ignored) {}
    }

    private void addLineItem() {
        Medicine med = (Medicine) cboMedicine.getSelectedItem();
        if (med == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thuốc.");
            return;
        }

        String batchNum = txtBatchNumber.getText().trim();
        String expiryStr = txtExpiryDate.getText().trim();
        int qty;
        double price;

        try {
            qty = Integer.parseInt(txtQty.getText().trim());
            price = Double.parseDouble(txtImportPrice.getText().trim().replace(",", ""));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "SL và giá nhập phải là số hợp lệ.");
            return;
        }

        if (batchNum.isEmpty() || expiryStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số lô và HSD.");
            return;
        }

        LocalDate expiryDate;
        try {
            expiryDate = LocalDate.parse(expiryStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "HSD không hợp lệ (yyyy-MM-dd).");
            return;
        }

        BatchLine line = new BatchLine(med, batchNum, expiryDate, qty, price);
        batchLines.add(line);

        lineModel.addRow(new Object[]{
                med.getMedicineCode(), med.getMedicineName(), batchNum,
                expiryStr, qty, String.format("%,.0f", price),
                String.format("%,.0f", qty * price)
        });

        updateTotal();
        txtBatchNumber.setText("");
        txtExpiryDate.setText("");
        txtQty.setText("");
        txtImportPrice.setText("");
    }

    private void updateTotal() {
        double total = batchLines.stream().mapToDouble(l -> l.qty * l.price).sum();
        lblTotal.setText(String.format("Tổng tiền: %,.0f đ", total));
    }

    private void confirmImport() {
        if (batchLines.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có dòng thuốc nào.");
            return;
        }

        Supplier supplier = (Supplier) cboSupplier.getSelectedItem();
        if (supplier == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận nhập kho " + batchLines.size() + " dòng thuốc?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            // Create GoodsReceipt
            GoodsReceipt receipt = new GoodsReceipt();
            receipt.setSupplierId((long) supplier.getId());
            receipt.setImportDate(LocalDateTime.now());
            receipt.setTotalAmount(batchLines.stream().mapToDouble(l -> l.qty * l.price).sum());
            receipt.setStatus("COMPLETED");
            receipt.setNote("Nhập từ " + supplier.getSupplierName());

            Account currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                receipt.setCreatedBy((long) currentUser.getId());
            }

            receiptBUS.insert(receipt);

            // Create MedicineBatch for each line
            for (BatchLine line : batchLines) {
                MedicineBatch batch = new MedicineBatch();
                batch.setReceiptId(receipt.getId());
                batch.setMedicineId(line.medicine.getId());
                batch.setBatchNumber(line.batchNumber);
                batch.setExpiryDate(line.expiryDate);
                batch.setImportPrice(line.price);
                batch.setSellPrice(0); // Will use existing sell price
                batch.setInitialQty(line.qty);
                batch.setCurrentQty(line.qty);
                batchBUS.insert(batch);
            }

            JOptionPane.showMessageDialog(this, "Nhập kho thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            clearAll();
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi nhập kho: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearAll() {
        batchLines.clear();
        lineModel.setRowCount(0);
        lblTotal.setText("Tổng tiền: 0 đ");
        txtBatchNumber.setText("");
        txtExpiryDate.setText("");
        txtQty.setText("");
        txtImportPrice.setText("");
    }

    // ── Inner class for line items ──
    private static class BatchLine {
        final Medicine medicine;
        final String batchNumber;
        final LocalDate expiryDate;
        final int qty;
        final double price;

        BatchLine(Medicine medicine, String batchNumber, LocalDate expiryDate, int qty, double price) {
            this.medicine = medicine;
            this.batchNumber = batchNumber;
            this.expiryDate = expiryDate;
            this.qty = qty;
            this.price = price;
        }
    }
}
