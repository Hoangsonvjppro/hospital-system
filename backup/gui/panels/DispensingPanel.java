package com.hospital.gui.panels;

import com.hospital.bus.InvoiceBUS;
import com.hospital.bus.MedicineExportBUS;
import com.hospital.bus.PrescriptionBUS;
import com.hospital.dao.MedicalRecordDAO;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.components.StatusBadge;
import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionDetail;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel phát thuốc — dành cho Dược sĩ.
 * Hiển thị đơn thuốc chờ phát, chi tiết đơn, confirm phát thuốc.
 */
public class DispensingPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(DispensingPanel.class.getName());
    private final NumberFormat moneyFmt = NumberFormat.getInstance(new Locale("vi", "VN"));

    private final PrescriptionBUS prescriptionBUS = new PrescriptionBUS();
    private final MedicineExportBUS exportBUS = new MedicineExportBUS();
    private final InvoiceBUS invoiceBUS = new InvoiceBUS();

    // Left: pending prescriptions list
    private DefaultTableModel prescTableModel;
    private JTable prescTable;
    private List<Prescription> pendingList;

    // Right: details of selected prescription
    private DefaultTableModel detailTableModel;
    private JTable detailTable;
    private JLabel lblPrescInfo;
    private JLabel lblTotalAmount;
    private JCheckBox chkPartialExport;

    public DispensingPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
        loadPendingPrescriptions();
    }

    private void initComponents() {
        // Title
        JLabel lblTitle = new JLabel("💉  Phát thuốc theo đơn");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        add(lblTitle, BorderLayout.NORTH);

        // Split: left 40% prescriptions, right 60% details
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createLeftPanel(), createRightPanel());
        split.setDividerLocation(420);
        split.setResizeWeight(0.4);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    // ════════════════════════════════════════════════════════════
    //  LEFT: Pending prescriptions
    // ════════════════════════════════════════════════════════════

    private JPanel createLeftPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setLayout(new BorderLayout(0, 8));
        card.setBackground(UIConstants.CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblHeader = new JLabel("Đơn thuốc chờ phát");
        lblHeader.setFont(UIConstants.FONT_SUBTITLE);
        lblHeader.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(lblHeader, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Mã bệnh án", "Ngày kê", "Tổng tiền", "Trạng thái"};
        prescTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        prescTable = new JTable(prescTableModel);
        prescTable.setFont(UIConstants.FONT_BODY);
        prescTable.setRowHeight(36);
        prescTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        prescTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        prescTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Hide ID
        prescTable.getColumnModel().getColumn(0).setMinWidth(0);
        prescTable.getColumnModel().getColumn(0).setMaxWidth(0);
        prescTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        prescTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedDetails();
        });

        JScrollPane scroll = new JScrollPane(prescTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        // Refresh button
        RoundedButton btnRefresh = new RoundedButton("🔄 Làm mới");
        btnRefresh.addActionListener(e -> loadPendingPrescriptions());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setOpaque(false);
        bottom.add(btnRefresh);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    // ════════════════════════════════════════════════════════════
    //  RIGHT: Prescription details + action
    // ════════════════════════════════════════════════════════════

    private JPanel createRightPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setLayout(new BorderLayout(0, 8));
        card.setBackground(UIConstants.CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Header
        lblPrescInfo = new JLabel("Chọn đơn thuốc để xem chi tiết");
        lblPrescInfo.setFont(UIConstants.FONT_SUBTITLE);
        lblPrescInfo.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(lblPrescInfo, BorderLayout.NORTH);

        // Detail table
        String[] cols = {"Tên thuốc", "Số lượng", "Liều dùng", "Hướng dẫn", "Đơn giá", "Thành tiền"};
        detailTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        detailTable = new JTable(detailTableModel);
        detailTable.setFont(UIConstants.FONT_BODY);
        detailTable.setRowHeight(34);
        detailTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        detailTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);

        JScrollPane scroll = new JScrollPane(detailTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        // Bottom: total + checkboxes + button
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);

        lblTotalAmount = new JLabel("Tổng: 0 đ");
        lblTotalAmount.setFont(UIConstants.FONT_HEADER);
        lblTotalAmount.setForeground(UIConstants.PRIMARY);
        bottom.add(lblTotalAmount, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actionPanel.setOpaque(false);

        chkPartialExport = new JCheckBox("Cho phép xuất một phần", false);
        chkPartialExport.setFont(UIConstants.FONT_LABEL);
        actionPanel.add(chkPartialExport);

        RoundedButton btnDispense = new RoundedButton("✅ Phát thuốc");
        btnDispense.setBackground(UIConstants.SUCCESS_GREEN);
        btnDispense.setForeground(Color.WHITE);
        btnDispense.setFont(UIConstants.FONT_BUTTON);
        btnDispense.addActionListener(e -> onDispense());
        actionPanel.add(btnDispense);

        bottom.add(actionPanel, BorderLayout.EAST);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    // ════════════════════════════════════════════════════════════
    //  DATA LOADING
    // ════════════════════════════════════════════════════════════

    private void loadPendingPrescriptions() {
        try {
            pendingList = prescriptionBUS.getPendingPrescriptions();
            prescTableModel.setRowCount(0);
            for (Prescription p : pendingList) {
                prescTableModel.addRow(new Object[]{
                        p.getId(),
                        "BA-" + p.getMedicalRecordId(),
                        p.getCreatedAt() != null ? p.getCreatedAt().toLocalDate().toString() : "",
                        moneyFmt.format(p.getTotalAmount()) + " đ",
                        p.getStatus()
                });
            }
            // Clear right panel
            detailTableModel.setRowCount(0);
            lblPrescInfo.setText("Chọn đơn thuốc để xem chi tiết");
            lblTotalAmount.setText("Tổng: 0 đ");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi tải đơn thuốc chờ phát", e);
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedDetails() {
        int row = prescTable.getSelectedRow();
        if (row < 0) return;

        long prescId = ((Number) prescTableModel.getValueAt(row, 0)).longValue();
        try {
            List<PrescriptionDetail> details = prescriptionBUS.getDetails(prescId);
            detailTableModel.setRowCount(0);
            double total = 0;
            for (PrescriptionDetail d : details) {
                double lineTotal = d.getUnitPrice() * d.getQuantity();
                total += lineTotal;
                detailTableModel.addRow(new Object[]{
                        d.getMedicineName(),
                        d.getQuantity(),
                        d.getDosage() != null ? d.getDosage() : "",
                        d.getInstruction() != null ? d.getInstruction() : "",
                        moneyFmt.format(d.getUnitPrice()) + " đ",
                        moneyFmt.format(lineTotal) + " đ"
                });
            }
            lblPrescInfo.setText("Đơn thuốc #" + prescId + " — " + details.size() + " loại thuốc");
            lblTotalAmount.setText("Tổng: " + moneyFmt.format(total) + " đ");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi tải chi tiết đơn thuốc", e);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  DISPENSE ACTION
    // ════════════════════════════════════════════════════════════

    private void onDispense() {
        int row = prescTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn thuốc cần phát.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long prescId = ((Number) prescTableModel.getValueAt(row, 0)).longValue();
        Prescription presc = pendingList.stream().filter(p -> p.getId() == prescId).findFirst().orElse(null);
        if (presc == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận phát thuốc cho đơn #" + prescId + "?",
                "Xác nhận phát thuốc", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            // Get details for export
            List<PrescriptionDetail> details = prescriptionBUS.getDetails(prescId);
            boolean allowPartial = chkPartialExport.isSelected();

            // Export medicine from stock
            String exportResult = exportBUS.processPrescriptionExport(details, 1, allowPartial);
            if (exportResult != null && !exportResult.isEmpty()) {
                LOGGER.info("Kết quả xuất kho: " + exportResult);
            }

            // Update prescription status → DISPENSED
            prescriptionBUS.updateStatus(prescId, Prescription.STATUS_DISPENSED);

            // Update MedicalRecord status → DISPENSED
            try {
                new MedicalRecordDAO().updateStatus(presc.getMedicalRecordId(), "DISPENSED");
            } catch (Exception ex) {
                LOGGER.warning("Không thể cập nhật trạng thái bệnh án: " + ex.getMessage());
            }

            // Create invoice
            try {
                invoiceBUS.createInvoiceFromMedicalRecord(presc.getMedicalRecordId());
            } catch (Exception ex) {
                LOGGER.warning("Không thể tạo hóa đơn tự động: " + ex.getMessage());
            }

            JOptionPane.showMessageDialog(this,
                    "Phát thuốc thành công cho đơn #" + prescId + "!\nHóa đơn đã được tạo tự động.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadPendingPrescriptions();

        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (DataAccessException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
