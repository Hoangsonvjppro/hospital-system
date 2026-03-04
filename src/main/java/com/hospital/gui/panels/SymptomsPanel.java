package com.hospital.gui.panels;

import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.model.MedicalRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class SymptomsPanel extends JPanel {

    private JTextArea txtSymptoms, txtDiagnosis;
    private JTextField txtDiagnosisCode, txtDoctorNotes;
    private JTextField txtFollowUpDate;

    private static final String SYMPTOMS_PLACEHOLDER = "Nhập triệu chứng của bệnh nhân...";
    private static final String DIAGNOSIS_PLACEHOLDER = "Nhập chẩn đoán...";

    public SymptomsPanel() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        initComponents();
    }

    private void initComponents() {
        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 450));

        txtSymptoms = new JTextArea(4, 0);
        card.add(createTextSection("Triệu chứng (Symptoms)", SYMPTOMS_PLACEHOLDER, txtSymptoms));
        card.add(Box.createVerticalStrut(16));

        txtDiagnosis = new JTextArea(4, 0);
        card.add(createTextSection("Chẩn đoán (Diagnosis)", DIAGNOSIS_PLACEHOLDER, txtDiagnosis));
        card.add(Box.createVerticalStrut(16));

        JPanel fieldsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        fieldsRow.setOpaque(false);
        fieldsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        txtDiagnosisCode = createTextField();
        txtDoctorNotes = createTextField();
        txtFollowUpDate = createTextField();

        fieldsRow.add(createLabeledField("Mã ICD-10", txtDiagnosisCode));
        fieldsRow.add(createLabeledField("Ghi chú bác sĩ", txtDoctorNotes));
        fieldsRow.add(createLabeledField("Ngày tái khám (dd/MM/yyyy)", txtFollowUpDate));

        card.add(fieldsRow);
        
        add(card);
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(UIConstants.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        return field;
    }

    private JPanel createLabeledField(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_OVERLINE);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTextSection(String title, String placeholder, JTextArea area) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(title);
        lbl.setFont(UIConstants.FONT_SECTION);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(lbl);
        section.add(Box.createVerticalStrut(8));

        area.setFont(UIConstants.FONT_LABEL);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretColor(UIConstants.ACCENT_BLUE);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        
        setupPlaceholder(area, placeholder);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        section.add(scroll);
        return section;
    }

    private void setupPlaceholder(JTextArea area, String placeholder) {
        if (area.getText() == null || area.getText().isEmpty()) {
            area.setText(placeholder);
            area.setForeground(UIConstants.TEXT_MUTED);
        }
        area.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (area.getText().equals(placeholder)) { area.setText(""); area.setForeground(UIConstants.TEXT_PRIMARY); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (area.getText().isEmpty()) { area.setText(placeholder); area.setForeground(UIConstants.TEXT_MUTED); }
            }
        });
    }

    public void populateFromRecord(MedicalRecord rec) {
        if (rec != null) {
            String symptoms = rec.getSymptoms() != null ? rec.getSymptoms() : "";
            if (!symptoms.isEmpty()) {
                txtSymptoms.setText(symptoms);
                txtSymptoms.setForeground(UIConstants.TEXT_PRIMARY);
            } else {
                txtSymptoms.setText(SYMPTOMS_PLACEHOLDER);
                txtSymptoms.setForeground(UIConstants.TEXT_MUTED);
            }

            String diagnosis = rec.getDiagnosis() != null ? rec.getDiagnosis() : "";
            if (!diagnosis.isEmpty()) {
                txtDiagnosis.setText(diagnosis);
                txtDiagnosis.setForeground(UIConstants.TEXT_PRIMARY);
            } else {
                txtDiagnosis.setText(DIAGNOSIS_PLACEHOLDER);
                txtDiagnosis.setForeground(UIConstants.TEXT_MUTED);
            }

            txtDiagnosisCode.setText(rec.getDiagnosisCode() != null ? rec.getDiagnosisCode() : "");
            txtDoctorNotes.setText(rec.getNotes() != null ? rec.getNotes() : "");
            
            if (rec.getFollowUpDate() != null) {
                 txtFollowUpDate.setText(rec.getFollowUpDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                 txtFollowUpDate.setText("");
            }
        } else {
            clearFields();
        }
    }

    public void clearFields() {
        txtSymptoms.setText(SYMPTOMS_PLACEHOLDER);
        txtSymptoms.setForeground(UIConstants.TEXT_MUTED);
        txtDiagnosis.setText(DIAGNOSIS_PLACEHOLDER);
        txtDiagnosis.setForeground(UIConstants.TEXT_MUTED);
        txtDiagnosisCode.setText("");
        txtDoctorNotes.setText("");
        txtFollowUpDate.setText("");
    }

    public Map<String, Object> captureState() {
        Map<String, Object> state = new HashMap<>();
        state.put("symptoms", txtSymptoms.getText().equals(SYMPTOMS_PLACEHOLDER) ? "" : txtSymptoms.getText());
        state.put("diagnosis", txtDiagnosis.getText().equals(DIAGNOSIS_PLACEHOLDER) ? "" : txtDiagnosis.getText());
        state.put("diagnosisCode", txtDiagnosisCode.getText());
        state.put("doctorNotes", txtDoctorNotes.getText());
        state.put("followUpDate", txtFollowUpDate.getText());
        return state;
    }

    public void restoreState(Map<String, Object> state) {
        if (state != null) {
            if (state.containsKey("symptoms")) {
                String val = (String) state.get("symptoms");
                if (val != null && !val.isEmpty()) {
                    txtSymptoms.setText(val);
                    txtSymptoms.setForeground(UIConstants.TEXT_PRIMARY);
                } else {
                    txtSymptoms.setText(SYMPTOMS_PLACEHOLDER);
                    txtSymptoms.setForeground(UIConstants.TEXT_MUTED);
                }
            }
            if (state.containsKey("diagnosis")) {
                String val = (String) state.get("diagnosis");
                if (val != null && !val.isEmpty()) {
                    txtDiagnosis.setText(val);
                    txtDiagnosis.setForeground(UIConstants.TEXT_PRIMARY);
                } else {
                    txtDiagnosis.setText(DIAGNOSIS_PLACEHOLDER);
                    txtDiagnosis.setForeground(UIConstants.TEXT_MUTED);
                }
            }
            if (state.containsKey("diagnosisCode")) txtDiagnosisCode.setText((String) state.get("diagnosisCode"));
            if (state.containsKey("doctorNotes")) txtDoctorNotes.setText((String) state.get("doctorNotes"));
            if (state.containsKey("followUpDate")) txtFollowUpDate.setText((String) state.get("followUpDate"));
        }
    }

    public String getSymptoms() { return txtSymptoms.getText().equals(SYMPTOMS_PLACEHOLDER) ? "" : txtSymptoms.getText(); }
    public String getDiagnosis() { return txtDiagnosis.getText().equals(DIAGNOSIS_PLACEHOLDER) ? "" : txtDiagnosis.getText(); }
    public String getDiagnosisCode() { return txtDiagnosisCode.getText(); }
    public String getDoctorNotes() { return txtDoctorNotes.getText(); }
    public String getFollowUpDate() { return txtFollowUpDate.getText(); }
}
