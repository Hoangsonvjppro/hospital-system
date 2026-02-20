package com.hospital.gui.panels;

import com.hospital.bus.SampleBUS;
import com.hospital.model.SampleEntity;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel mẫu — thay thế bằng panel thực tế của bạn.
 * Sample panel demonstrating table + form CRUD pattern.
 */
public class SamplePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName;
    private JTextField txtDescription;
    private SampleBUS bus;

    public SamplePanel() {
        bus = new SampleBUS();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // === Phần trên: Form nhập liệu ===
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Tên:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        txtName = new JTextField(20);
        formPanel.add(txtName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        txtDescription = new JTextField(20);
        formPanel.add(txtDescription, gbc);

        add(formPanel, BorderLayout.NORTH);

        // === Phần giữa: Bảng dữ liệu ===
        String[] columns = { "ID", "Tên", "Mô tả" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> onRowSelected());
        add(new JScrollPane(table), BorderLayout.CENTER);

        // === Phần dưới: Các nút thao tác ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnAdd = new JButton("Thêm");
        JButton btnUpdate = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnRefresh = new JButton("Làm mới");

        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> onRefresh());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRefresh);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // === Event Handlers ===

    private void onRowSelected() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            txtName.setText(tableModel.getValueAt(row, 1).toString());
            txtDescription.setText(tableModel.getValueAt(row, 2).toString());
        }
    }

    private void onAdd() {
        SampleEntity entity = new SampleEntity();
        entity.setName(txtName.getText().trim());
        entity.setDescription(txtDescription.getText().trim());
        if (bus.insert(entity)) {
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            onRefresh();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdate() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần sửa.");
            return;
        }
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        SampleEntity entity = new SampleEntity();
        entity.setId(id);
        entity.setName(txtName.getText().trim());
        entity.setDescription(txtDescription.getText().trim());
        if (bus.update(entity)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            onRefresh();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            if (bus.delete(id)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                onRefresh();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onRefresh() {
        tableModel.setRowCount(0);
        List<SampleEntity> list = bus.findAll();
        for (SampleEntity e : list) {
            tableModel.addRow(new Object[] { e.getId(), e.getName(), e.getDescription() });
        }
        txtName.setText("");
        txtDescription.setText("");
    }
}
