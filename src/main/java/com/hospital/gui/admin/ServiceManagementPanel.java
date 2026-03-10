package com.hospital.gui.admin;

import com.hospital.bus.ServiceBUS;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.Service;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Quản lý dịch vụ y tế — CRUD (xét nghiệm, chẩn đoán hình ảnh, thủ thuật…).
 */
public class ServiceManagementPanel extends JPanel {

    private final ServiceBUS serviceBUS = new ServiceBUS();

    private JTable table;
    private DefaultTableModel tableModel;

    /* form fields */
    private JTextField txtName, txtPrice, txtDescription;
    private JComboBox<String> cboType;
    private JCheckBox chkActive;
    private int selectedId = -1;

    private static final String[] SERVICE_TYPES = {"EXAMINATION", "LAB_TEST", "IMAGING", "PROCEDURE"};

    public ServiceManagementPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createTablePanel(), createFormPanel());
        split.setDividerLocation(320);
        split.setResizeWeight(0.6);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        loadData();
    }

    /* ───── Header ───── */
    private JPanel createHeader() {
        JLabel title = new JLabel("🏥 Quản lý dịch vụ y tế");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        return header;
    }

    /* ───── Table ───── */
    private JPanel createTablePanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Tên dịch vụ", "Loại", "Giá (VNĐ)", "Mô tả", "Hoạt động"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.setFont(UIConstants.FONT_BODY);
        table.getTableHeader().setFont(UIConstants.FONT_LABEL);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    /* ───── Form ───── */
    private JPanel createFormPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(12, 8));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        txtName = addTextField(fields, gbc, row++, "Tên dịch vụ:", 25);

        /* service type combo */
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        fields.add(createLabel("Loại dịch vụ:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cboType = new JComboBox<>(SERVICE_TYPES);
        cboType.setFont(UIConstants.FONT_BODY);
        fields.add(cboType, gbc);
        row++;

        txtPrice = addTextField(fields, gbc, row++, "Giá (VNĐ):", 15);
        txtDescription = addTextField(fields, gbc, row++, "Mô tả:", 35);

        /* active checkbox */
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        fields.add(createLabel("Hoạt động:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        chkActive = new JCheckBox("", true);
        chkActive.setOpaque(false);
        fields.add(chkActive, gbc);

        card.add(fields, BorderLayout.CENTER);
        card.add(createButtonBar(), BorderLayout.SOUTH);
        return card;
    }

    private JPanel createButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bar.setOpaque(false);

        RoundedButton btnNew = new RoundedButton("🆕 Mới");
        btnNew.setBackground(UIConstants.ACCENT_BLUE);
        btnNew.setForeground(Color.WHITE);
        btnNew.addActionListener(e -> clearForm());

        RoundedButton btnSave = new RoundedButton("💾 Lưu");
        btnSave.setBackground(UIConstants.SUCCESS_GREEN);
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> save());

        RoundedButton btnDelete = new RoundedButton("🗑 Xóa");
        btnDelete.setBackground(UIConstants.DANGER_RED);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> deleteSelected());

        bar.add(btnNew);
        bar.add(btnSave);
        bar.add(btnDelete);
        return bar;
    }

    /* ───── Data ───── */
    private void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                tableModel.setRowCount(0);
                List<Service> list = serviceBUS.findAll();
                for (Service s : list) {
                    tableModel.addRow(new Object[]{
                            s.getId(),
                            s.getServiceName(),
                            s.getServiceType(),
                            String.format("%,.0f", s.getPrice()),
                            s.getDescription(),
                            s.isActive() ? "✓" : "✗"
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        txtName.setText((String) tableModel.getValueAt(row, 1));
        cboType.setSelectedItem(tableModel.getValueAt(row, 2));
        txtPrice.setText(((String) tableModel.getValueAt(row, 3)).replace(",", "").replace(".", ""));
        txtDescription.setText((String) tableModel.getValueAt(row, 4));
        chkActive.setSelected("✓".equals(tableModel.getValueAt(row, 5)));
    }

    private void save() {
        try {
            Service s = new Service();
            s.setServiceName(txtName.getText().trim());
            s.setServiceType((String) cboType.getSelectedItem());
            s.setDescription(txtDescription.getText().trim());
            s.setActive(chkActive.isSelected());

            try {
                s.setPrice(Double.parseDouble(txtPrice.getText().trim().replace(",", "")));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Giá không hợp lệ", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (selectedId > 0) {
                s.setId(selectedId);
                serviceBUS.update(s);
            } else {
                serviceBUS.insert(s);
            }
            loadData();
            clearForm();
            JOptionPane.showMessageDialog(this, "Lưu thành công!", "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        if (selectedId <= 0) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa dịch vụ này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                serviceBUS.delete(selectedId);
                loadData();
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtName.setText("");
        cboType.setSelectedIndex(0);
        txtPrice.setText("");
        txtDescription.setText("");
        chkActive.setSelected(true);
        table.clearSelection();
    }

    /* ───── Helpers ───── */
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setPreferredSize(new Dimension(130, 28));
        return lbl;
    }

    private JTextField addTextField(JPanel form, GridBagConstraints gbc, int row, String label, int cols) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(createLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JTextField txt = new JTextField(cols);
        txt.setFont(UIConstants.FONT_BODY);
        form.add(txt, gbc);
        return txt;
    }
}
