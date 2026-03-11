package com.hospital.gui.panels;

import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.model.MedicalRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class VitalSignsPanel extends JPanel {

    private JTextField txtWeight, txtHeight, txtBloodPressure, txtPulse, txtTemperature, txtSpo2, txtBmi;

    public VitalSignsPanel() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        initComponents();
        setupBmiCalculator();
        setupValidation();
    }

    private void initComponents() {
        JLabel title = new JLabel("SINH HIỆU BỆNH NHÂN");
        title.setFont(UIConstants.FONT_SECTION);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);
        headerPanel.add(title);
        add(headerPanel);
        add(Box.createVerticalStrut(20));

        JPanel gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.weightx = 1.0;

        txtPulse = createTextField();
        txtBloodPressure = createTextField();
        txtTemperature = createTextField();
        txtSpo2 = createTextField();
        txtWeight = createTextField();
        txtHeight = createTextField();
        txtBmi = createTextField();
        txtBmi.setEditable(false);
        txtBmi.setBackground(UIConstants.CONTENT_BG);

        // Column 1
        gbc.gridx = 0; gbc.gridy = 0; gridPanel.add(createFieldRow("Mạch (lần/phút):", txtPulse, "bpm"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; gridPanel.add(createFieldRow("Huyết áp (mmHg):", txtBloodPressure, "mmHg"), gbc);
        gbc.gridx = 0; gbc.gridy = 2; gridPanel.add(createFieldRow("Nhiệt độ (°C):", txtTemperature, "°C"), gbc);
        gbc.gridx = 0; gbc.gridy = 3; gridPanel.add(createFieldRow("Nhịp thở (l/ph):", createTextField(), "l/ph"), gbc);

        // Column 2
        gbc.gridx = 1; gbc.gridy = 0; gridPanel.add(createFieldRow("Cân nặng (kg):", txtWeight, "kg"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gridPanel.add(createFieldRow("Chiều cao (cm):", txtHeight, "cm"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gridPanel.add(createFieldRow("BMI:", txtBmi, ""), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gridPanel.add(createFieldRow("SpO2 (%):", txtSpo2, "%"), gbc);

        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10, 10, 20, 10));
        card.add(gridPanel, BorderLayout.CENTER);

        add(card);
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(UIConstants.FONT_NUMBER_BIG);
        field.setForeground(UIConstants.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        field.setCaretColor(UIConstants.ACCENT_BLUE);
        return field;
    }

    private JPanel createFieldRow(String labelText, JTextField field, String unitText) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIConstants.FONT_OVERLINE);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        lbl.setPreferredSize(new Dimension(140, 30));
        panel.add(lbl, BorderLayout.WEST);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setOpaque(false);
        inputPanel.add(field, BorderLayout.CENTER);
        
        if (!unitText.isEmpty()) {
            JLabel unit = new JLabel(unitText);
            unit.setFont(UIConstants.FONT_BODY);
            unit.setForeground(UIConstants.TEXT_MUTED);
            unit.setPreferredSize(new Dimension(40, 30));
            inputPanel.add(unit, BorderLayout.EAST);
        }
        
        panel.add(inputPanel, BorderLayout.CENTER);
        return panel;
    }

    private void setupBmiCalculator() {
        DocumentListener listener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { calculateBmi(); }
            @Override public void removeUpdate(DocumentEvent e) { calculateBmi(); }
            @Override public void changedUpdate(DocumentEvent e) { calculateBmi(); }
        };
        txtWeight.getDocument().addDocumentListener(listener);
        txtHeight.getDocument().addDocumentListener(listener);
    }

    private void calculateBmi() {
        try {
            double weight = Double.parseDouble(txtWeight.getText().trim());
            double heightCm = Double.parseDouble(txtHeight.getText().trim());
            if (heightCm > 0) {
                double heightM = heightCm / 100.0;
                double bmi = weight / (heightM * heightM);
                txtBmi.setText(String.format("%.1f", bmi));
                
                if (bmi < 18.5 || bmi >= 25) {
                    txtBmi.setForeground(UIConstants.ERROR_COLOR);
                } else {
                    txtBmi.setForeground(UIConstants.SUCCESS_GREEN);
                }
            } else {
                txtBmi.setText("");
            }
        } catch (NumberFormatException e) {
            txtBmi.setText("");
        }
    }

    private void setupValidation() {
        addValidation(txtPulse, 40, 200);
        addValidation(txtTemperature, 35.0, 42.0);
        addValidation(txtSpo2, 0, 100);
        
        // Custom validation for BP (e.g. 120/80)
        txtBloodPressure.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validateBP(); }
            @Override public void removeUpdate(DocumentEvent e) { validateBP(); }
            @Override public void changedUpdate(DocumentEvent e) { validateBP(); }
            
            private void validateBP() {
                String bp = txtBloodPressure.getText().trim();
                if (bp.isEmpty()) {
                    txtBloodPressure.setForeground(UIConstants.TEXT_PRIMARY);
                    return;
                }
                
                boolean isValid = false;
                if (bp.contains("/")) {
                    try {
                        String[] parts = bp.split("/");
                        if(parts.length == 2) {
                            int systolic = Integer.parseInt(parts[0].trim());
                            int diastolic = Integer.parseInt(parts[1].trim());
                            if (systolic >= 60 && systolic <= 250 && diastolic >= 40 && diastolic <= 150) {
                                isValid = true;
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                } else { // Try parsing as single number just in case
                     try {
                        int num = Integer.parseInt(bp);
                        if (num >= 60 && num <= 250) isValid = true;
                     } catch (NumberFormatException ignored) {}
                }
                
                txtBloodPressure.setForeground(isValid ? UIConstants.TEXT_PRIMARY : UIConstants.ERROR_COLOR);
            }
        });
    }

    private void addValidation(JTextField field, double min, double max) {
         field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validate(); }
            @Override public void removeUpdate(DocumentEvent e) { validate(); }
            @Override public void changedUpdate(DocumentEvent e) { validate(); }
            
            private void validate() {
                String text = field.getText().trim();
                if (text.isEmpty()) {
                    field.setForeground(UIConstants.TEXT_PRIMARY);
                    return;
                }
                try {
                    double val = Double.parseDouble(text);
                    if (val < min || val > max) field.setForeground(UIConstants.ERROR_COLOR);
                    else field.setForeground(UIConstants.TEXT_PRIMARY);
                } catch (NumberFormatException e) {
                    field.setForeground(UIConstants.ERROR_COLOR);
                }
            }
        });
    }

    public void populateFromRecord(MedicalRecord rec) {
        if (rec != null) {
            txtWeight.setText(rec.getWeight() > 0 ? String.valueOf(rec.getWeight()) : "");
            txtHeight.setText(rec.getHeight() > 0 ? String.valueOf(rec.getHeight()) : "");
            txtBloodPressure.setText(rec.getBloodPressure() != null ? rec.getBloodPressure() : "");
            txtPulse.setText(rec.getPulse() > 0 ? String.valueOf(rec.getPulse()) : "");
            txtTemperature.setText(rec.getTemperature() > 0 ? String.valueOf(rec.getTemperature()) : "");
            txtSpo2.setText(rec.getSpo2() > 0 ? String.valueOf(rec.getSpo2()) : "");
        } else {
            clearFields();
        }
    }

    public void clearFields() {
        txtWeight.setText("");
        txtHeight.setText("");
        txtBloodPressure.setText("");
        txtPulse.setText("");
        txtTemperature.setText("");
        txtSpo2.setText("");
        txtBmi.setText("");
    }

    public Map<String, Object> captureState() {
        Map<String, Object> state = new HashMap<>();
        state.put("weight", txtWeight.getText());
        state.put("height", txtHeight.getText());
        state.put("bp", txtBloodPressure.getText());
        state.put("pulse", txtPulse.getText());
        state.put("temp", txtTemperature.getText());
        state.put("spo2", txtSpo2.getText());
        return state;
    }

    public void restoreState(Map<String, Object> state) {
        if (state != null) {
            if (state.containsKey("weight")) txtWeight.setText((String) state.get("weight"));
            if (state.containsKey("height")) txtHeight.setText((String) state.get("height"));
            if (state.containsKey("bp")) txtBloodPressure.setText((String) state.get("bp"));
            if (state.containsKey("pulse")) txtPulse.setText((String) state.get("pulse"));
            if (state.containsKey("temp")) txtTemperature.setText((String) state.get("temp"));
            if (state.containsKey("spo2")) txtSpo2.setText((String) state.get("spo2"));
        }
    }

}
