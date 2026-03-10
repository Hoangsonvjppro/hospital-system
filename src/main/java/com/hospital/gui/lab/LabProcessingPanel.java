package com.hospital.gui.lab;

import com.hospital.bus.LabResultBUS;
import com.hospital.bus.ServiceOrderBUS;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.*;
import com.hospital.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Xử lý xét nghiệm — NV xét nghiệm nhận yêu cầu, nhập kết quả, hoàn tất.
 */
public class LabProcessingPanel extends JPanel {

    private final ServiceOrderBUS serviceOrderBUS = new ServiceOrderBUS();
    private final LabResultBUS labResultBUS = new LabResultBUS();

    private DefaultTableModel pendingModel;
    private JTable pendingTable;
    private JTextField txtTestName, txtResultValue, txtNormalRange, txtUnit, txtNotes;
    private ServiceOrder selectedOrder;

    public LabProcessingPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        loadPendingOrders();
    }

    private JPanel createHeader() {
        JLabel title = new JLabel("🔬 Xử lý xét nghiệm");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);

        RoundedButton btnRefresh = new RoundedButton("🔄 Làm mới");
        btnRefresh.setBackground(UIConstants.PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> loadPendingOrders());
        header.add(btnRefresh, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createPendingPanel(), createResultInputPanel());
        split.setDividerLocation(480);
        split.setResizeWeight(0.5);
        split.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(split, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Left: Pending orders ──

    private JPanel createPendingPanel() {
        RoundedPanel panel = new RoundedPanel(UIConstants.CARD_RADIUS);
        panel.setBackground(UIConstants.CARD_BG);
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbl = new JLabel("Yêu cầu XN chờ xử lý");
        lbl.setFont(UIConstants.FONT_SUBTITLE);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        panel.add(lbl, BorderLayout.NORTH);

        pendingModel = new DefaultTableModel(
                new String[]{"ID", "Dịch vụ", "Bệnh nhân", "Bác sĩ", "Trạng thái", "Ngày YC"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        pendingTable = new JTable(pendingModel);
        pendingTable.setRowHeight(34);
        pendingTable.setFont(UIConstants.FONT_LABEL);
        pendingTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        pendingTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        pendingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pendingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onOrderSelected();
        });

        JScrollPane scroll = new JScrollPane(pendingTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ── Right: Result input form ──

    private JPanel createResultInputPanel() {
        RoundedPanel panel = new RoundedPanel(UIConstants.CARD_RADIUS);
        panel.setBackground(UIConstants.CARD_BG);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbl = new JLabel("Nhập kết quả xét nghiệm");
        lbl.setFont(UIConstants.FONT_SUBTITLE);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        panel.add(lbl, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        form.add(createLabel("Tên xét nghiệm:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtTestName = new JTextField(20);
        txtTestName.setFont(UIConstants.FONT_BODY);
        form.add(txtTestName, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(createLabel("Kết quả:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtResultValue = new JTextField(20);
        txtResultValue.setFont(UIConstants.FONT_BODY);
        form.add(txtResultValue, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(createLabel("Khoảng bình thường:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtNormalRange = new JTextField(20);
        txtNormalRange.setFont(UIConstants.FONT_BODY);
        form.add(txtNormalRange, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(createLabel("Đơn vị:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtUnit = new JTextField(20);
        txtUnit.setFont(UIConstants.FONT_BODY);
        form.add(txtUnit, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(createLabel("Ghi chú:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtNotes = new JTextField(20);
        txtNotes.setFont(UIConstants.FONT_BODY);
        form.add(txtNotes, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        RoundedButton btnSave = new RoundedButton("💾 Lưu kết quả");
        btnSave.setBackground(UIConstants.SUCCESS_GREEN);
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> saveResult());

        RoundedButton btnComplete = new RoundedButton("✅ Hoàn tất XN");
        btnComplete.setBackground(UIConstants.PRIMARY);
        btnComplete.setForeground(Color.WHITE);
        btnComplete.addActionListener(e -> completeOrder());

        btnPanel.add(btnSave);
        btnPanel.add(btnComplete);
        form.add(btnPanel, gbc);

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    // ── Data ──

    private void loadPendingOrders() {
        pendingModel.setRowCount(0);
        selectedOrder = null;
        clearForm();

        try {
            // Load all orders and filter for pending ones
            List<ServiceOrder> allOrders = serviceOrderBUS.findAll();
            for (ServiceOrder o : allOrders) {
                if ("ORDERED".equals(o.getStatus()) || "IN_PROGRESS".equals(o.getStatus())) {
                    pendingModel.addRow(new Object[]{
                            o.getId(),
                            o.getServiceName() != null ? o.getServiceName() : "DV #" + o.getServiceId(),
                            o.getPatientName() != null ? o.getPatientName() : "",
                            o.getDoctorName() != null ? o.getDoctorName() : "",
                            o.getStatus(),
                            o.getOrderedAt() != null ? o.getOrderedAt().toLocalDate().toString() : ""
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onOrderSelected() {
        int row = pendingTable.getSelectedRow();
        if (row < 0) return;

        try {
            int orderId = (int) pendingModel.getValueAt(row, 0);
            selectedOrder = serviceOrderBUS.findById(orderId);
            if (selectedOrder != null) {
                txtTestName.setText(selectedOrder.getServiceName() != null ? selectedOrder.getServiceName() : "");
            }
        } catch (Exception ignored) {}
    }

    private void saveResult() {
        if (selectedOrder == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn yêu cầu XN từ danh sách.");
            return;
        }

        String testName = txtTestName.getText().trim();
        String resultValue = txtResultValue.getText().trim();
        if (testName.isEmpty() || resultValue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên XN và kết quả.");
            return;
        }

        try {
            LabResult result = new LabResult();
            result.setRecordId(selectedOrder.getRecordId());
            result.setServiceOrderId((long) selectedOrder.getId());
            result.setTestName(testName);
            result.setResultValue(resultValue);
            result.setNormalRange(txtNormalRange.getText().trim());
            result.setUnit(txtUnit.getText().trim());
            result.setNotes(txtNotes.getText().trim());
            result.setTestDate(LocalDateTime.now());

            Account currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                result.setPerformedBy((long) currentUser.getId());
            }

            labResultBUS.insert(result);

            JOptionPane.showMessageDialog(this, "Lưu kết quả XN thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void completeOrder() {
        if (selectedOrder == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn yêu cầu XN.");
            return;
        }

        try {
            selectedOrder.setStatus("COMPLETED");
            selectedOrder.setCompletedAt(LocalDateTime.now());
            serviceOrderBUS.update(selectedOrder);

            JOptionPane.showMessageDialog(this, "Hoàn tất xét nghiệm!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadPendingOrders();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtTestName.setText("");
        txtResultValue.setText("");
        txtNormalRange.setText("");
        txtUnit.setText("");
        txtNotes.setText("");
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        return lbl;
    }
}
