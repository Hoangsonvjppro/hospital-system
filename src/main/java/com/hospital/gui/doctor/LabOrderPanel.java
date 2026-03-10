package com.hospital.gui.doctor;

import com.hospital.bus.LabResultBUS;
import com.hospital.bus.ServiceBUS;
import com.hospital.bus.ServiceOrderBUS;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * ④ Yêu cầu xét nghiệm — bác sĩ chỉ định dịch vụ XN cho bệnh án.
 */
public class LabOrderPanel extends JPanel {

    private final ServiceOrderBUS serviceOrderBUS = new ServiceOrderBUS();
    private final ServiceBUS serviceBUS = new ServiceBUS();

    private JTextField txtRecordId;
    private JComboBox<Service> cboService;
    private JTextField txtNotes;
    private DefaultTableModel orderModel;
    private long currentRecordId = -1;

    public LabOrderPanel() {
        this(null);
    }

    public LabOrderPanel(Long initialRecordId) {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        if (initialRecordId != null) {
            txtRecordId.setText(String.valueOf(initialRecordId));
            loadExistingOrders();
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);

        JLabel title = new JLabel("🧪 Yêu cầu xét nghiệm");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JPanel idBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        idBar.setOpaque(false);
        idBar.add(new JLabel("Mã bệnh án:"));
        txtRecordId = new JTextField(12);
        txtRecordId.setFont(UIConstants.FONT_BODY);
        idBar.add(txtRecordId);

        RoundedButton btnLoad = new RoundedButton("Tải");
        btnLoad.setBackground(UIConstants.PRIMARY);
        btnLoad.setForeground(Color.WHITE);
        btnLoad.addActionListener(e -> loadExistingOrders());
        idBar.add(btnLoad);

        header.add(title, BorderLayout.NORTH);
        header.add(idBar, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        // Order form
        body.add(createOrderForm(), BorderLayout.NORTH);

        // Existing orders table
        RoundedPanel tableCard = new RoundedPanel(UIConstants.CARD_RADIUS);
        tableCard.setBackground(UIConstants.CARD_BG);
        tableCard.setLayout(new BorderLayout(0, 8));
        tableCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblOrders = new JLabel("Danh sách yêu cầu XN");
        lblOrders.setFont(UIConstants.FONT_SUBTITLE);
        lblOrders.setForeground(UIConstants.TEXT_PRIMARY);
        tableCard.add(lblOrders, BorderLayout.NORTH);

        orderModel = new DefaultTableModel(
                new String[]{"ID", "Dịch vụ", "Loại", "Trạng thái", "Ngày yêu cầu", "Ghi chú"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable orderTable = new JTable(orderModel);
        orderTable.setRowHeight(34);
        orderTable.setFont(UIConstants.FONT_LABEL);
        orderTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        orderTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);

        JScrollPane scroll = new JScrollPane(orderTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        tableCard.add(scroll, BorderLayout.CENTER);

        body.add(tableCard, BorderLayout.CENTER);
        return body;
    }

    private JPanel createOrderForm() {
        RoundedPanel formCard = new RoundedPanel(UIConstants.CARD_RADIUS);
        formCard.setBackground(UIConstants.CARD_BG);
        formCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        formCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        formCard.add(new JLabel("Dịch vụ:"));
        cboService = new JComboBox<>();
        cboService.setFont(UIConstants.FONT_BODY);
        cboService.setPreferredSize(new Dimension(300, 32));
        loadServices();
        formCard.add(cboService);

        formCard.add(new JLabel("Ghi chú:"));
        txtNotes = new JTextField(20);
        txtNotes.setFont(UIConstants.FONT_BODY);
        formCard.add(txtNotes);

        RoundedButton btnOrder = new RoundedButton("+ Yêu cầu XN");
        btnOrder.setBackground(UIConstants.SUCCESS_GREEN);
        btnOrder.setForeground(Color.WHITE);
        btnOrder.addActionListener(e -> createOrder());
        formCard.add(btnOrder);

        return formCard;
    }

    private void loadServices() {
        try {
            List<Service> services = serviceBUS.findAll();
            cboService.removeAllItems();
            for (Service s : services) {
                if (s.isActive()) {
                    cboService.addItem(s);
                }
            }
        } catch (Exception ignored) {}
    }

    private void loadExistingOrders() {
        String idText = txtRecordId.getText().trim();
        if (idText.isEmpty()) return;

        try {
            currentRecordId = Long.parseLong(idText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Mã bệnh án không hợp lệ.");
            return;
        }

        orderModel.setRowCount(0);
        try {
            List<ServiceOrder> orders = serviceOrderBUS.findByRecordId(currentRecordId);
            for (ServiceOrder o : orders) {
                orderModel.addRow(new Object[]{
                        o.getId(),
                        o.getServiceName() != null ? o.getServiceName() : "DV #" + o.getServiceId(),
                        o.getServiceType() != null ? o.getServiceType() : "",
                        o.getStatus(),
                        o.getOrderedAt() != null ? o.getOrderedAt().toLocalDate().toString() : "",
                        o.getNotes() != null ? o.getNotes() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách XN: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createOrder() {
        if (currentRecordId < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã bệnh án và nhấn Tải trước.");
            return;
        }

        Service selected = (Service) cboService.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ xét nghiệm.");
            return;
        }

        try {
            ServiceOrder order = new ServiceOrder();
            order.setRecordId(currentRecordId);
            order.setServiceId(selected.getId());
            order.setStatus("ORDERED");
            order.setNotes(txtNotes.getText().trim());

            serviceOrderBUS.insert(order);

            JOptionPane.showMessageDialog(this, "Yêu cầu xét nghiệm thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            txtNotes.setText("");
            loadExistingOrders();
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
