package com.hospital.gui.doctor;

import com.hospital.bus.*;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * ③ Thăm khám — sinh hiệu + triệu chứng + chẩn đoán ICD-10.
 */
public class ExaminationPanel extends JPanel {

    private final QueueBUS queueBUS = new QueueBUS();
    private final MedicalRecordBUS recordBUS = new MedicalRecordBUS();
    private final PatientBUS patientBUS = new PatientBUS();
    private final PatientAllergyBUS allergyBUS = new PatientAllergyBUS();
    private final PatientChronicDiseaseBUS chronicBUS = new PatientChronicDiseaseBUS();
    private final Icd10CodeBUS icd10BUS = new Icd10CodeBUS();

    // Current state
    private QueueEntry currentEntry;
    private Patient currentPatient;
    private MedicalRecord currentRecord;

    // UI Fields
    private JLabel lblPatientInfo;
    private DefaultTableModel historyModel;

    // Vital signs
    private JTextField txtPulse, txtBP, txtTemp, txtWeight, txtHeight, txtSpo2;

    // Symptoms & Diagnosis
    private JTextArea txtSymptoms, txtDiagnosis, txtNotes;
    private JTextField txtIcd10;
    private JLabel lblAllergies, lblChronic;

    public ExaminationPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createTopBar(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createActionBar(), BorderLayout.SOUTH);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        JLabel title = new JLabel("🩺 Thăm khám bệnh nhân");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        lblPatientInfo = new JLabel("Chưa chọn bệnh nhân — Nhấn \"Gọi BN\" để bắt đầu");
        lblPatientInfo.setFont(UIConstants.FONT_BODY);
        lblPatientInfo.setForeground(UIConstants.TEXT_SECONDARY);

        RoundedButton btnCallNext = new RoundedButton("🔔 Gọi BN tiếp theo");
        btnCallNext.setPreferredSize(new Dimension(200, 38));
        btnCallNext.addActionListener(e -> callNextPatient());

        JPanel left = new JPanel(new BorderLayout(0, 4));
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(lblPatientInfo, BorderLayout.SOUTH);

        bar.add(left, BorderLayout.CENTER);
        bar.add(btnCallNext, BorderLayout.EAST);
        return bar;
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel(new GridLayout(1, 3, 12, 0));
        main.setOpaque(false);

        main.add(createHistoryPanel());
        main.add(createVitalsPanel());
        main.add(createDiagnosisPanel());

        return main;
    }

    private JPanel createHistoryPanel() {
        RoundedPanel panel = new RoundedPanel(UIConstants.CARD_RADIUS);
        panel.setBackground(UIConstants.CARD_BG);
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbl = new JLabel("📋 Tiền sử");
        lbl.setFont(UIConstants.FONT_SUBTITLE);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);

        // Allergies
        lblAllergies = new JLabel("Dị ứng: —");
        lblAllergies.setFont(UIConstants.FONT_CAPTION);
        lblAllergies.setForeground(UIConstants.ERROR_COLOR);

        // Chronic diseases
        lblChronic = new JLabel("Bệnh mãn tính: —");
        lblChronic.setFont(UIConstants.FONT_CAPTION);
        lblChronic.setForeground(UIConstants.TEXT_SECONDARY);

        // History table
        historyModel = new DefaultTableModel(new String[]{"Ngày", "Chẩn đoán", "Mã ICD"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable historyTable = new JTable(historyModel);
        historyTable.setRowHeight(28);
        historyTable.setFont(UIConstants.FONT_CAPTION);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        infoPanel.add(lblAllergies);
        infoPanel.add(lblChronic);

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setOpaque(false);
        topPanel.add(lbl, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createVitalsPanel() {
        RoundedPanel panel = new RoundedPanel(UIConstants.CARD_RADIUS);
        panel.setBackground(UIConstants.CARD_BG);
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbl = new JLabel("💓 Sinh hiệu & Triệu chứng");
        lbl.setFont(UIConstants.FONT_SUBTITLE);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtPulse = addVitalField(form, gbc, 0, "Mạch (bpm):", "72");
        txtBP = addVitalField(form, gbc, 1, "HA (mmHg):", "120/80");
        txtTemp = addVitalField(form, gbc, 2, "Nhiệt độ (°C):", "37.0");
        txtWeight = addVitalField(form, gbc, 3, "Cân nặng (kg):", "");
        txtHeight = addVitalField(form, gbc, 4, "Chiều cao (cm):", "");
        txtSpo2 = addVitalField(form, gbc, 5, "SpO2 (%):", "98");

        // Symptoms text area
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JLabel lblSym = new JLabel("Triệu chứng:");
        lblSym.setFont(UIConstants.FONT_LABEL);
        form.add(lblSym, gbc);

        txtSymptoms = new JTextArea(4, 20);
        txtSymptoms.setFont(UIConstants.FONT_BODY);
        txtSymptoms.setLineWrap(true);
        txtSymptoms.setWrapStyleWord(true);
        gbc.gridy = 7;
        form.add(new JScrollPane(txtSymptoms), gbc);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDiagnosisPanel() {
        RoundedPanel panel = new RoundedPanel(UIConstants.CARD_RADIUS);
        panel.setBackground(UIConstants.CARD_BG);
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbl = new JLabel("📝 Chẩn đoán & Quyết định");
        lbl.setFont(UIConstants.FONT_SUBTITLE);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        // ICD-10 search
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("ICD-10:"), gbc);
        txtIcd10 = new JTextField(15);
        txtIcd10.setFont(UIConstants.FONT_BODY);
        txtIcd10.putClientProperty("JTextField.placeholderText", "Tìm mã ICD-10...");
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(txtIcd10, gbc);

        // Diagnosis
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        form.add(new JLabel("Chẩn đoán:"), gbc);

        txtDiagnosis = new JTextArea(3, 20);
        txtDiagnosis.setFont(UIConstants.FONT_BODY);
        txtDiagnosis.setLineWrap(true);
        txtDiagnosis.setWrapStyleWord(true);
        gbc.gridy = 2; gbc.weighty = 0.5; gbc.fill = GridBagConstraints.BOTH;
        form.add(new JScrollPane(txtDiagnosis), gbc);

        // Notes
        gbc.gridy = 3; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(new JLabel("Ghi chú BS:"), gbc);

        txtNotes = new JTextArea(2, 20);
        txtNotes.setFont(UIConstants.FONT_BODY);
        txtNotes.setLineWrap(true);
        gbc.gridy = 4; gbc.weighty = 0.3; gbc.fill = GridBagConstraints.BOTH;
        form.add(new JScrollPane(txtNotes), gbc);

        // Action buttons
        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 0, 8));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

        RoundedButton btnSave = new RoundedButton("💾 Lưu khám");
        btnSave.addActionListener(e -> saveExamination());

        RoundedButton btnPrescribe = new RoundedButton("💊 Kê đơn thuốc");
        btnPrescribe.setColors(UIConstants.SUCCESS_GREEN, UIConstants.SUCCESS_GREEN_DARK);
        btnPrescribe.addActionListener(e -> {
            if (currentRecord != null) {
                saveExamination();
            }
        });

        RoundedButton btnLab = new RoundedButton("🧪 Yêu cầu XN");
        btnLab.setColors(UIConstants.WARNING_ORANGE, UIConstants.WARNING_ORANGE.darker());

        RoundedButton btnComplete = new RoundedButton("✅ Kết thúc khám");
        btnComplete.setColors(UIConstants.ACCENT_BLUE, UIConstants.ACCENT_BLUE_DARK);

        btnPanel.add(btnSave);
        btnPanel.add(btnPrescribe);
        btnPanel.add(btnLab);
        btnPanel.add(btnComplete);

        gbc.gridy = 5; gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(btnPanel, gbc);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bar.setOpaque(false);
        return bar;
    }

    private void callNextPatient() {
        try {
            currentEntry = queueBUS.callNextPatient();
            currentPatient = patientBUS.findById(currentEntry.getPatientId());

            if (currentPatient != null) {
                String info = String.format("BN: %s | %s | %d tuổi | %s",
                        currentPatient.getFullName(),
                        currentPatient.getGender() != null ? currentPatient.getGender().name() : "",
                        currentPatient.getAge(),
                        currentEntry.getPriority().name());
                lblPatientInfo.setText(info);
                lblPatientInfo.setForeground(UIConstants.ACCENT_BLUE);

                loadPatientHistory();
                loadAllergiesAndChronic();
            }
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void loadPatientHistory() {
        historyModel.setRowCount(0);
        if (currentPatient == null) return;
        try {
            List<MedicalRecord> records = recordBUS.getHistoryByPatient(currentPatient.getId());
            for (MedicalRecord r : records) {
                historyModel.addRow(new Object[]{
                        r.getCreatedAt() != null ? r.getCreatedAt().toLocalDate().toString() : "",
                        r.getDiagnosis() != null ? r.getDiagnosis() : "",
                        r.getDiagnosisCode() != null ? r.getDiagnosisCode() : ""
                });
            }
        } catch (Exception ignored) {}
    }

    private void loadAllergiesAndChronic() {
        if (currentPatient == null) return;
        try {
            List<PatientAllergy> allergies = allergyBUS.findByPatientId(currentPatient.getId());
            if (!allergies.isEmpty()) {
                String text = "⚠ Dị ứng: " + allergies.stream()
                        .map(a -> a.getAllergenName() + " (" + a.getSeverity() + ")")
                        .reduce((a, b) -> a + ", " + b).orElse("");
                lblAllergies.setText(text);
            } else {
                lblAllergies.setText("Dị ứng: Không có");
            }
        } catch (Exception ex) {
            lblAllergies.setText("Dị ứng: —");
        }
        try {
            List<PatientChronicDisease> chronic = chronicBUS.findActiveByPatientId(currentPatient.getId());
            if (!chronic.isEmpty()) {
                String text = "Bệnh mãn tính: " + chronic.stream()
                        .map(PatientChronicDisease::getDiseaseName)
                        .reduce((a, b) -> a + ", " + b).orElse("");
                lblChronic.setText(text);
            } else {
                lblChronic.setText("Bệnh mãn tính: Không có");
            }
        } catch (Exception ex) {
            lblChronic.setText("Bệnh mãn tính: —");
        }
    }

    private void saveExamination() {
        if (currentPatient == null || currentEntry == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn bệnh nhân. Nhấn 'Gọi BN' trước.");
            return;
        }
        try {
            // Create or get medical record
            if (currentRecord == null) {
                long recordId = recordBUS.createMedicalRecord(currentPatient.getId(), 
                        com.hospital.util.SessionManager.getInstance().getCurrentUser().getId(), null);
                currentRecord = recordBUS.findById(recordId);
            }

            // Save vital signs
            double weight = parseDouble(txtWeight.getText(), 0);
            double height = parseDouble(txtHeight.getText(), 0);
            int pulse = parseInt(txtPulse.getText(), 0);
            double temp = parseDouble(txtTemp.getText(), 0);
            int spo2 = parseInt(txtSpo2.getText(), 0);
            String bp = txtBP.getText().trim();

            if (weight > 0 && height > 0 && pulse > 0) {
                recordBUS.updateVitalSigns(currentRecord.getId(), weight, height, bp, pulse, temp, spo2);
            }

            // Save diagnosis & symptoms
            String symptoms = txtSymptoms.getText().trim();
            String diagnosis = txtDiagnosis.getText().trim();
            String icd10 = txtIcd10.getText().trim();
            String notes = txtNotes.getText().trim();

            if (!symptoms.isEmpty() && !diagnosis.isEmpty()) {
                recordBUS.updateFullExamination(currentRecord.getId(),
                        diagnosis, symptoms, icd10.isEmpty() ? null : icd10,
                        notes.isEmpty() ? null : notes, null);
            }

            JOptionPane.showMessageDialog(this, "Lưu khám thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField addVitalField(JPanel form, GridBagConstraints gbc, int row, String label, String placeholder) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_LABEL);
        form.add(lbl, gbc);

        JTextField field = new JTextField(10);
        field.setFont(UIConstants.FONT_BODY);
        if (!placeholder.isEmpty()) field.putClientProperty("JTextField.placeholderText", placeholder);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(field, gbc);
        return field;
    }

    private double parseDouble(String s, double def) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; }
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }
}
