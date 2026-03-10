package com.hospital.gui.pharmacist;

import com.hospital.bus.DispensingBUS;
import com.hospital.bus.MedicineBatchBUS;
import com.hospital.bus.MedicineBUS;
import com.hospital.bus.PrescriptionBUS;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.*;
import com.hospital.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ⑥ Phát thuốc theo đơn — danh sách đơn chờ + chi tiết + phát thuốc.
 */
public class DispensingPanel extends JPanel {

    private final PrescriptionBUS prescriptionBUS = new PrescriptionBUS();
    private final DispensingBUS dispensingBUS = new DispensingBUS();
    private final MedicineBUS medicineBUS = new MedicineBUS();
    private final MedicineBatchBUS batchBUS = new MedicineBatchBUS();

    private DefaultTableModel pendingModel;
    private JTable pendingTable;
    private DefaultTableModel detailModel;
    private JTable detailTable;
    private JLabel lblSelectedInfo, lblTotalAmount;
    private Prescription selectedPrescription;
    private List<PrescriptionDetail> currentDetails = new ArrayList<>();

    public DispensingPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        loadPendingPrescriptions();
    }

    private JPanel createHeader() {
        JLabel title = new JLabel("💉 Phát thuốc theo đơn");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);

        RoundedButton btnRefresh = new RoundedButton("🔄 Làm mới");
        btnRefresh.setBackground(UIConstants.PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> loadPendingPrescriptions());
        header.add(btnRefresh, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createPendingListPanel(), createDetailPanel());
        split.setDividerLocation(360);
        split.setResizeWeight(0.35);
        split.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(split, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Left: Pending prescriptions list ──

    private JPanel createPendingListPanel() {
        RoundedPanel panel = new RoundedPanel(UIConstants.CARD_RADIUS);
        panel.setBackground(UIConstants.CARD_BG);
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbl = new JLabel("Đơn thuốc chờ phát");
        lbl.setFont(UIConstants.FONT_SUBTITLE);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        panel.add(lbl, BorderLayout.NORTH);

        pendingModel = new DefaultTableModel(
                new String[]{"Mã đơn", "Bệnh nhân", "Thời gian", "SL thuốc"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        pendingTable = new JTable(pendingModel);
        pendingTable.setRowHeight(36);
        pendingTable.setFont(UIConstants.FONT_LABEL);
        pendingTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        pendingTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        pendingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pendingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onPrescriptionSelected();
        });

        JScrollPane scroll = new JScrollPane(pendingTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ── Right: Prescription detail + dispense ──

    private JPanel createDetailPanel() {
        RoundedPanel panel = new RoundedPanel(UIConstants.CARD_RADIUS);
        panel.setBackground(UIConstants.CARD_BG);
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        lblSelectedInfo = new JLabel("Chọn một đơn thuốc từ danh sách bên trái");
        lblSelectedInfo.setFont(UIConstants.FONT_SUBTITLE);
        lblSelectedInfo.setForeground(UIConstants.TEXT_PRIMARY);
        panel.add(lblSelectedInfo, BorderLayout.NORTH);

        detailModel = new DefaultTableModel(
                new String[]{"Thuốc", "ĐVT", "Yêu cầu", "Phát", "Lô", "Tồn kho"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; }
        };
        detailTable = new JTable(detailModel);
        detailTable.setRowHeight(34);
        detailTable.setFont(UIConstants.FONT_LABEL);
        detailTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        detailTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);

        JScrollPane scroll = new JScrollPane(detailTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        panel.add(scroll, BorderLayout.CENTER);

        // Bottom bar
        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(12, 0, 0, 0));

        lblTotalAmount = new JLabel("Tổng: 0 đ");
        lblTotalAmount.setFont(UIConstants.FONT_SUBTITLE);
        lblTotalAmount.setForeground(UIConstants.PRIMARY);
        bottom.add(lblTotalAmount, BorderLayout.WEST);

        RoundedButton btnDispense = new RoundedButton("✅ Xác nhận phát thuốc");
        btnDispense.setBackground(UIConstants.SUCCESS_GREEN);
        btnDispense.setForeground(Color.WHITE);
        btnDispense.addActionListener(e -> doDispense());
        bottom.add(btnDispense, BorderLayout.EAST);

        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    // ── Data loading ──

    private void loadPendingPrescriptions() {
        pendingModel.setRowCount(0);
        selectedPrescription = null;
        detailModel.setRowCount(0);
        lblSelectedInfo.setText("Chọn một đơn thuốc từ danh sách bên trái");
        lblTotalAmount.setText("Tổng: 0 đ");

        try {
            List<Prescription> pending = prescriptionBUS.getPendingPrescriptions();
            for (Prescription rx : pending) {
                List<PrescriptionDetail> details = prescriptionBUS.getDetails(rx.getId());
                pendingModel.addRow(new Object[]{
                        "RX" + String.format("%05d", rx.getId()),
                        "Đơn #" + rx.getMedicalRecordId(),
                        rx.getCreatedAt() != null ? rx.getCreatedAt().toLocalTime().toString().substring(0, 5) : "",
                        details.size() + " thuốc"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách đơn: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onPrescriptionSelected() {
        int row = pendingTable.getSelectedRow();
        if (row < 0) return;

        try {
            List<Prescription> pending = prescriptionBUS.getPendingPrescriptions();
            if (row >= pending.size()) return;
            selectedPrescription = pending.get(row);

            lblSelectedInfo.setText("Chi tiết đơn: RX" + String.format("%05d", selectedPrescription.getId()));
            currentDetails = prescriptionBUS.getDetails(selectedPrescription.getId());
            detailModel.setRowCount(0);

            double total = 0;
            for (PrescriptionDetail d : currentDetails) {
                Medicine med = medicineBUS.findById((int) d.getMedicineId());
                String unit = med != null ? med.getUnit() : "";
                String batchInfo = "";
                int stock = 0;
                try {
                    List<MedicineBatch> batches = batchBUS.findAvailableFEFO(d.getMedicineId());
                    if (!batches.isEmpty()) {
                        batchInfo = batches.get(0).getBatchNumber();
                        stock = batches.stream().mapToInt(MedicineBatch::getCurrentQty).sum();
                    }
                } catch (Exception ignored) {}

                detailModel.addRow(new Object[]{
                        d.getMedicineName() != null ? d.getMedicineName() : (med != null ? med.getMedicineName() : ""),
                        unit,
                        d.getQuantity(),
                        d.getQuantity(),
                        batchInfo,
                        stock
                });
                total += d.getLineTotal();
            }
            lblTotalAmount.setText(String.format("Tổng: %,.0f đ", total));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết đơn: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Dispense action ──

    private void doDispense() {
        if (selectedPrescription == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn thuốc cần phát.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận phát thuốc cho đơn RX" + String.format("%05d", selectedPrescription.getId()) + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            List<DispensingItem> items = new ArrayList<>();
            for (int i = 0; i < detailModel.getRowCount(); i++) {
                PrescriptionDetail pd = currentDetails.get(i);
                int dispensedQty;
                try {
                    dispensedQty = Integer.parseInt(detailModel.getValueAt(i, 3).toString());
                } catch (NumberFormatException e) {
                    dispensedQty = pd.getQuantity();
                }

                DispensingItem item = new DispensingItem();
                item.setPrescriptionDetailId(pd.getId());
                item.setMedicineId(pd.getMedicineId());
                item.setMedicineName(pd.getMedicineName());
                item.setRequestedQuantity(pd.getQuantity());
                item.setDispensedQuantity(dispensedQty);
                item.setUnitPrice(BigDecimal.valueOf(pd.getUnitPrice()));
                items.add(item);
            }

            Long pharmacistId = null;
            Account currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                pharmacistId = (long) currentUser.getId();
            }

            dispensingBUS.processDispensing(
                    selectedPrescription.getId(),
                    0, // patientId will be resolved from prescription
                    items,
                    pharmacistId,
                    null
            );

            JOptionPane.showMessageDialog(this, "Phát thuốc thành công!", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            loadPendingPrescriptions();
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi phát thuốc: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
