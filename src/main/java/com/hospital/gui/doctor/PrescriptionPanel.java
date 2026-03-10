package com.hospital.gui.doctor;

import com.hospital.bus.*;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ⑤ Kê đơn thuốc — tìm thuốc, kiểm tra dị ứng/tương tác, lưu đơn.
 */
public class PrescriptionPanel extends JPanel {

    private final PrescriptionBUS prescriptionBUS = new PrescriptionBUS();
    private final MedicineBUS medicineBUS = new MedicineBUS();
    private final PatientBUS patientBUS = new PatientBUS();

    private JTextField txtRecordId, txtMedicineSearch;
    private JLabel lblPatientInfo, lblAllergyWarning;
    private DefaultTableModel prescTableModel;
    private final List<PrescriptionDetail> prescriptionDetails = new ArrayList<>();
    private JLabel lblTotal;

    public PrescriptionPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        add(createActionBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);

        JLabel title = new JLabel("💊 Kê đơn thuốc");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        // Record ID input
        JPanel recordBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        recordBar.setOpaque(false);
        recordBar.add(new JLabel("Mã bệnh án:"));
        txtRecordId = new JTextField(10);
        txtRecordId.setFont(UIConstants.FONT_BODY);
        recordBar.add(txtRecordId);

        RoundedButton btnLoad = new RoundedButton("Tải");
        btnLoad.setPreferredSize(new Dimension(80, 34));
        btnLoad.addActionListener(e -> loadRecord());
        recordBar.add(btnLoad);

        lblPatientInfo = new JLabel("");
        lblPatientInfo.setFont(UIConstants.FONT_BODY);
        lblPatientInfo.setForeground(UIConstants.ACCENT_BLUE);
        recordBar.add(Box.createHorizontalStrut(16));
        recordBar.add(lblPatientInfo);

        lblAllergyWarning = new JLabel("");
        lblAllergyWarning.setFont(UIConstants.FONT_BOLD);
        lblAllergyWarning.setForeground(UIConstants.ERROR_COLOR);

        header.add(title, BorderLayout.NORTH);
        header.add(recordBar, BorderLayout.CENTER);
        header.add(lblAllergyWarning, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        // Medicine search + add
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        searchBar.add(new JLabel("🔍 Thuốc:"));
        txtMedicineSearch = new JTextField(20);
        txtMedicineSearch.setFont(UIConstants.FONT_BODY);
        searchBar.add(txtMedicineSearch);

        RoundedButton btnAdd = new RoundedButton("+ Thêm");
        btnAdd.setPreferredSize(new Dimension(100, 34));
        btnAdd.addActionListener(e -> addMedicine());
        searchBar.add(btnAdd);

        // Prescription table
        prescTableModel = new DefaultTableModel(
                new String[]{"STT", "Tên thuốc", "Liều", "SL", "Cách dùng", "Số ngày", "Đơn giá", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(prescTableModel);
        table.setRowHeight(32);
        table.setFont(UIConstants.FONT_BODY);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);

        // Remove button
        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tableActions.setOpaque(false);
        RoundedButton btnRemove = new RoundedButton("Xóa dòng");
        btnRemove.setColors(UIConstants.DANGER_RED, UIConstants.DANGER_RED_DARK);
        btnRemove.setPreferredSize(new Dimension(120, 34));
        btnRemove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                prescriptionDetails.remove(row);
                refreshTable();
            }
        });
        tableActions.add(btnRemove);

        lblTotal = new JLabel("Tổng: 0 đ");
        lblTotal.setFont(UIConstants.FONT_HEADER);
        lblTotal.setForeground(UIConstants.PRIMARY);
        tableActions.add(Box.createHorizontalStrut(20));
        tableActions.add(lblTotal);

        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.add(searchBar, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        card.add(tableActions, BorderLayout.SOUTH);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        bar.setOpaque(false);

        RoundedButton btnSaveDraft = new RoundedButton("💾 Lưu nháp (DRAFT)");
        btnSaveDraft.setColors(UIConstants.TEXT_SECONDARY, UIConstants.TEXT_PRIMARY);
        btnSaveDraft.setPreferredSize(new Dimension(200, 42));
        btnSaveDraft.addActionListener(e -> savePrescription());

        RoundedButton btnConfirm = new RoundedButton("✅ Xác nhận đơn");
        btnConfirm.setPreferredSize(new Dimension(180, 42));
        btnConfirm.addActionListener(e -> savePrescription());

        bar.add(btnSaveDraft);
        bar.add(btnConfirm);
        return bar;
    }

    private void loadRecord() {
        try {
            long recordId = Long.parseLong(txtRecordId.getText().trim());
            MedicalRecord record = recordBUS.findById(recordId);
            if (record == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy bệnh án.");
                return;
            }
            Patient patient = patientBUS.findById((int) record.getPatientId());
            if (patient != null) {
                lblPatientInfo.setText("BN: " + patient.getFullName() + " | " + record.getDiagnosis());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Mã bệnh án phải là số.");
        }
    }

    private final MedicalRecordBUS recordBUS = new MedicalRecordBUS();

    private void addMedicine() {
        String keyword = txtMedicineSearch.getText().trim();
        if (keyword.isEmpty()) return;
        try {
            List<Medicine> all = medicineBUS.findAll();
            List<Medicine> results = all.stream()
                    .filter(m -> m.getMedicineName() != null &&
                            m.getMedicineName().toLowerCase().contains(keyword.toLowerCase()))
                    .toList();

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy thuốc.");
                return;
            }

            Medicine med;
            if (results.size() == 1) {
                med = results.get(0);
            } else {
                String[] opts = results.stream()
                        .map(m -> m.getMedicineName() + " (" + m.getUnit() + ")")
                        .toArray(String[]::new);
                String sel = (String) JOptionPane.showInputDialog(this, "Chọn thuốc:", "Kết quả",
                        JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
                if (sel == null) return;
                int idx = java.util.Arrays.asList(opts).indexOf(sel);
                med = results.get(idx);
            }

            // Ask quantity and dosage
            String qtyStr = JOptionPane.showInputDialog(this, "Số lượng:", "10");
            if (qtyStr == null || qtyStr.isEmpty()) return;
            int qty = Integer.parseInt(qtyStr);

            String dosage = JOptionPane.showInputDialog(this, "Liều dùng:", med.getDosageForm());
            String instruction = JOptionPane.showInputDialog(this, "Cách dùng:", "Uống sau ăn");
            String durationStr = JOptionPane.showInputDialog(this, "Số ngày:", "7");

            PrescriptionDetail detail = new PrescriptionDetail();
            detail.setMedicineId(med.getId());
            detail.setMedicineName(med.getMedicineName());
            detail.setQuantity(qty);
            detail.setDosage(dosage != null ? dosage : "");
            detail.setInstruction(instruction != null ? instruction : "");
            detail.setDuration(durationStr != null ? Integer.parseInt(durationStr) : 7);
            detail.setUnitPrice(0);
            detail.setUnit(med.getUnit());

            prescriptionDetails.add(detail);
            refreshTable();

            // Check drug interactions
            List<Integer> medIds = prescriptionDetails.stream()
                    .map(d -> (int) d.getMedicineId()).toList();
            List<String> interactions = prescriptionBUS.checkDrugInteractions(medIds);
            if (!interactions.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        String.join("\n", interactions),
                        "⚠ Cảnh báo tương tác thuốc", JOptionPane.WARNING_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }

    private void refreshTable() {
        prescTableModel.setRowCount(0);
        double total = 0;
        int stt = 1;
        for (PrescriptionDetail d : prescriptionDetails) {
            double lineTotal = d.getQuantity() * d.getUnitPrice();
            prescTableModel.addRow(new Object[]{
                    stt++, d.getMedicineName(), d.getDosage(), d.getQuantity(),
                    d.getInstruction(), d.getDuration(),
                    String.format("%,.0f", d.getUnitPrice()),
                    String.format("%,.0f", lineTotal)
            });
            total += lineTotal;
        }
        lblTotal.setText("Tổng: " + String.format("%,.0f", total) + " đ");
    }

    private void savePrescription() {
        try {
            long recordId = Long.parseLong(txtRecordId.getText().trim());
            if (prescriptionDetails.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Đơn thuốc trống.");
                return;
            }

            long prescId = prescriptionBUS.createPrescription(recordId, prescriptionDetails);
            JOptionPane.showMessageDialog(this,
                    "Đơn thuốc đã tạo thành công! Mã: #" + prescId,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            prescriptionDetails.clear();
            refreshTable();

        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã bệnh án.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }
}
