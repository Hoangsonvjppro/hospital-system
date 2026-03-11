package com.hospital.gui.panels;

import com.hospital.bus.ServiceBUS;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.model.Service;
import com.hospital.util.AppUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

public class ServicePanel extends JPanel {

    private final ServiceBUS serviceBUS = new ServiceBUS();
    private JTable table;
    private DefaultTableModel model;
    private List<Service> currentList;
    private final DecimalFormat formatter = new DecimalFormat("###,###,###");

    public ServicePanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.CONTENT_BG);
        initComponents();
        loadData();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(UIConstants.CONTENT_BG);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Quản lý Dịch vụ");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Toolbar buttons
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);

        RoundedButton btnAdd = new RoundedButton("➕ Thêm DV");
        RoundedButton btnEdit = new RoundedButton("✏️ Sửa", UIConstants.ACCENT_BLUE, UIConstants.ACCENT_BLUE_DARK, 8);
        RoundedButton btnDeactivate = new RoundedButton("🔴 Vô hiệu hóa", UIConstants.WARNING_ORANGE, UIConstants.WARNING_ORANGE.darker(), 8);
        RoundedButton btnRefresh = new RoundedButton("🔄 Làm mới", UIConstants.SUCCESS_GREEN, UIConstants.SUCCESS_GREEN_DARK, 8);

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDeactivate);
        toolbar.add(btnRefresh);
        headerPanel.add(toolbar, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Mã DV", "Tên dịch vụ", "Giá (VNĐ)", "Mô tả", "Trạng thái"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setFont(UIConstants.FONT_BODY);
        table.setRowHeight(35);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(mainPanel);

        // Events
        btnAdd.addActionListener(e -> showServiceDialog(null));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                showServiceDialog(currentList.get(row));
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ cần sửa", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        btnDeactivate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                Service s = currentList.get(row);
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Vô hiệu hóa dịch vụ: " + s.getServiceName() + "?",
                        "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        serviceBUS.deactivate(s.getId());
                        loadData();
                    } catch (DataAccessException ex) {
                        AppUtils.showError(this, "Lỗi: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        btnRefresh.addActionListener(e -> loadData());
    }

    private void loadData() {
        SwingWorker<List<Service>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Service> doInBackground() {
                return serviceBUS.findAll();
            }
            @Override
            protected void done() {
                try {
                    currentList = get();
                    renderTable(currentList);
                } catch (Exception ex) {
                    AppUtils.showError(ServicePanel.this, "Lỗi tải dữ liệu: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void renderTable(List<Service> list) {
        model.setRowCount(0);
        for (Service s : list) {
            model.addRow(new Object[]{
                    "DV" + String.format("%05d", s.getId()),
                    s.getServiceName(),
                    formatter.format(s.getPrice()),
                    s.getDescription() != null ? s.getDescription() : "",
                    s.isActive() ? "Hoạt động" : "Ngưng"
            });
        }
    }

    private void showServiceDialog(Service existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Thêm dịch vụ" : "Sửa dịch vụ", true);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 15));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtName = new JTextField();
        JTextField txtPrice = new JTextField();
        JTextField txtDesc = new JTextField();

        form.add(new JLabel("Tên dịch vụ:"));
        form.add(txtName);
        form.add(new JLabel("Giá (VNĐ):"));
        form.add(txtPrice);
        form.add(new JLabel("Mô tả:"));
        form.add(txtDesc);

        if (existing != null) {
            txtName.setText(existing.getServiceName());
            txtPrice.setText(existing.getPrice().toPlainString());
            txtDesc.setText(existing.getDescription() != null ? existing.getDescription() : "");
        }

        JButton btnSave = new JButton("Lưu");
        btnSave.setFont(UIConstants.FONT_BUTTON);
        btnSave.addActionListener(e -> {
            try {
                Service s = existing != null ? existing : new Service();
                s.setServiceName(txtName.getText().trim());
                String priceText = txtPrice.getText().trim();
                s.setPrice(new BigDecimal(priceText));
                s.setDescription(txtDesc.getText().trim());

                boolean ok;
                if (existing == null || existing.getId() == 0) {
                    ok = serviceBUS.insert(s);
                } else {
                    ok = serviceBUS.update(s);
                }
                if (ok) {
                    dialog.dispose();
                    loadData();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá phải là số hợp lệ", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
            } catch (DataAccessException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnSave);

        dialog.setLayout(new BorderLayout());
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
