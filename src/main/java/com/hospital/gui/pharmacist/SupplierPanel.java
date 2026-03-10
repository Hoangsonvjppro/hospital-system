package com.hospital.gui.pharmacist;

import com.hospital.bus.SupplierBUS;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Quản lý nhà cung cấp — CRUD supplier.
 */
public class SupplierPanel extends JPanel {

    private final SupplierBUS supplierBUS = new SupplierBUS();

    private DefaultTableModel supModel;
    private JTable supTable;
    private JTextField txtName, txtContact, txtPhone, txtAddress;
    private Supplier selectedSupplier;

    public SupplierPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        loadSuppliers();
    }

    private JPanel createHeader() {
        JLabel title = new JLabel("🏭 Quản lý nhà cung cấp");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JPanel createBody() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createTablePanel(), createFormPanel());
        split.setDividerLocation(550);
        split.setResizeWeight(0.6);
        split.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(split);
        return wrapper;
    }

    private JPanel createTablePanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        supModel = new DefaultTableModel(
                new String[]{"ID", "Tên NCC", "Liên hệ", "SĐT", "Địa chỉ", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        supTable = new JTable(supModel);
        supTable.setRowHeight(32);
        supTable.setFont(UIConstants.FONT_LABEL);
        supTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        supTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        supTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        supTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onSupplierSelected();
        });

        JScrollPane scroll = new JScrollPane(supTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createFormPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblForm = new JLabel("Thông tin NCC");
        lblForm.setFont(UIConstants.FONT_SUBTITLE);
        lblForm.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(lblForm, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        txtName = addField(form, gbc, row++, "Tên NCC:", 20);
        txtContact = addField(form, gbc, row++, "Người liên hệ:", 20);
        txtPhone = addField(form, gbc, row++, "SĐT:", 15);
        txtAddress = addField(form, gbc, row++, "Địa chỉ:", 25);

        // Buttons
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        btnPanel.setOpaque(false);

        RoundedButton btnNew = new RoundedButton("+ Thêm mới");
        btnNew.setBackground(UIConstants.SUCCESS_GREEN);
        btnNew.setForeground(Color.WHITE);
        btnNew.addActionListener(e -> doInsert());

        RoundedButton btnUpdate = new RoundedButton("💾 Cập nhật");
        btnUpdate.setBackground(UIConstants.PRIMARY);
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.addActionListener(e -> doUpdate());

        RoundedButton btnDelete = new RoundedButton("🗑 Xóa");
        btnDelete.setBackground(UIConstants.DANGER_RED);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> doDelete());

        RoundedButton btnClear = new RoundedButton("Xóa form");
        btnClear.setBackground(UIConstants.FIELD_BG);
        btnClear.setForeground(UIConstants.TEXT_PRIMARY);
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnNew);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        form.add(btnPanel, gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JTextField addField(JPanel form, GridBagConstraints gbc, int row, String label, int cols) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_LABEL);
        form.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        JTextField txt = new JTextField(cols);
        txt.setFont(UIConstants.FONT_BODY);
        form.add(txt, gbc);
        return txt;
    }

    // ── Data ──

    private void loadSuppliers() {
        supModel.setRowCount(0);
        try {
            List<Supplier> list = supplierBUS.findAll();
            for (Supplier s : list) {
                supModel.addRow(new Object[]{
                        s.getId(), s.getSupplierName(), s.getContactName(),
                        s.getPhone(), s.getAddress(), s.isActive() ? "Hoạt động" : "Ngưng"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải NCC: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSupplierSelected() {
        int row = supTable.getSelectedRow();
        if (row < 0) return;

        int id = (int) supModel.getValueAt(row, 0);
        try {
            selectedSupplier = supplierBUS.findById(id);
            if (selectedSupplier == null) return;
            txtName.setText(selectedSupplier.getSupplierName());
            txtContact.setText(selectedSupplier.getContactName());
            txtPhone.setText(selectedSupplier.getPhone());
            txtAddress.setText(selectedSupplier.getAddress());
        } catch (Exception ignored) {}
    }

    private void doInsert() {
        try {
            Supplier s = buildFromForm();
            supplierBUS.insert(s);
            JOptionPane.showMessageDialog(this, "Thêm NCC thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadSuppliers();
            clearForm();
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doUpdate() {
        if (selectedSupplier == null) { JOptionPane.showMessageDialog(this, "Chọn NCC."); return; }
        try {
            Supplier s = buildFromForm();
            s.setId(selectedSupplier.getId());
            supplierBUS.update(s);
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadSuppliers();
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doDelete() {
        if (selectedSupplier == null) { JOptionPane.showMessageDialog(this, "Chọn NCC."); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa NCC: " + selectedSupplier.getSupplierName() + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            supplierBUS.delete(selectedSupplier.getId());
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            loadSuppliers();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Supplier buildFromForm() {
        Supplier s = new Supplier();
        s.setSupplierName(txtName.getText().trim());
        s.setContactName(txtContact.getText().trim());
        s.setPhone(txtPhone.getText().trim());
        s.setAddress(txtAddress.getText().trim());
        s.setActive(true);
        return s;
    }

    private void clearForm() {
        selectedSupplier = null;
        txtName.setText(""); txtContact.setText(""); txtPhone.setText(""); txtAddress.setText("");
        supTable.clearSelection();
    }
}
