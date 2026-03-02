package com.hospital.gui.panels;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.hospital.bus.MedicalRecordBUS;
import com.hospital.bus.QueueBUS;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.model.Patient;

public class DoctorWorkstationPanel extends JPanel {

    private final QueueBUS queueBUS = new QueueBUS();
    private final MedicalRecordBUS medicalRecordBUS = new MedicalRecordBUS();

    private JPanel patientListPanel;
    private JPanel rightContentPanel;
    private JLabel lblPatientCount;

    private Patient selectedPatient;
    private long selectedRecordId = -1;
    private int selectedIndex = -1;

    private JTextField txtTemperature;
    private JTextField txtWeight;
    private JTextField txtHeight;
    private JTextField txtBloodPressure;
    private JTextField txtPulse;

    private JTextArea txtSymptoms;
    private JTextArea txtDiagnosis;

    private javax.swing.Timer refreshTimer;

    public DoctorWorkstationPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout());
        initUI();
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        refreshTimer = new javax.swing.Timer(10000, e -> loadPatientList());
        refreshTimer.start();
    }

    private void initUI() {
        add(createLeftPanel(), BorderLayout.WEST);
        add(createRightPanel(), BorderLayout.CENTER);
    }

    // ================= LEFT PANEL =================

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(260, 0));
        panel.setBackground(UIConstants.CARD_BG);

        lblPatientCount = new JLabel();
        lblPatientCount.setBorder(new EmptyBorder(20, 20, 10, 20));
        panel.add(lblPatientCount, BorderLayout.NORTH);

        patientListPanel = new JPanel();
        patientListPanel.setLayout(new BoxLayout(patientListPanel, BoxLayout.Y_AXIS));
        patientListPanel.setBackground(UIConstants.CARD_BG);

        JScrollPane scroll = new JScrollPane(patientListPanel);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        loadPatientList();
        return panel;
    }

    private void loadPatientList() {
        patientListPanel.removeAll();

        List<Patient> waiting = queueBUS.getWaitingPatients();
        lblPatientCount.setText("Danh sách chờ (" + waiting.size() + ")");

        for (int i = 0; i < waiting.size(); i++) {
            patientListPanel.add(createPatientCard(waiting.get(i), i));
            patientListPanel.add(Box.createVerticalStrut(5));
        }

        patientListPanel.revalidate();
        patientListPanel.repaint();
    }

    private JPanel createPatientCard(Patient patient, int index) {
        boolean isSelected = index == selectedIndex;

        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new EmptyBorder(10, 15, 10, 15));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setBackground(isSelected ? UIConstants.ACCENT_BLUE_SOFT : UIConstants.CARD_BG);

        JLabel name = new JLabel(patient.getFullName());
        JLabel info = new JLabel(patient.getGender() + " - " + patient.getAge() + " tuổi");

        card.add(name, BorderLayout.NORTH);
        card.add(info, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedIndex = index;
                selectedPatient = patient;
                selectedRecordId = patient.getCurrentRecordId();

                if ("WAITING".equals(patient.getStatus())) {
                    queueBUS.updateQueueStatus(selectedRecordId, "IN_PROGRESS");
                }

                loadPatientList();
                updateRightPanel();
            }
        });

        return card;
    }

    // ================= RIGHT PANEL =================

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIConstants.CONTENT_BG);

        rightContentPanel = new JPanel(new BorderLayout());
        rightContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(rightContentPanel, BorderLayout.CENTER);

        panel.add(createBottomBar(), BorderLayout.SOUTH);

        updateRightPanel();
        return panel;
    }

    private void updateRightPanel() {
        rightContentPanel.removeAll();

        if (selectedPatient == null) {
            rightContentPanel.add(new JLabel("Chưa chọn bệnh nhân"), BorderLayout.CENTER);
        } else {
            rightContentPanel.add(createMedicalForm(), BorderLayout.CENTER);
        }

        rightContentPanel.revalidate();
        rightContentPanel.repaint();
    }

    private JPanel createMedicalForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        txtTemperature = new JTextField("37");
        txtWeight = new JTextField("70");
        txtHeight = new JTextField("170");
        txtBloodPressure = new JTextField("120/80");
        txtPulse = new JTextField("80");

        txtSymptoms = new JTextArea(3, 20);
        txtDiagnosis = new JTextArea(3, 20);

        panel.add(new JLabel("Nhiệt độ (°C)"));
        panel.add(txtTemperature);
        panel.add(new JLabel("Cân nặng (kg)"));
        panel.add(txtWeight);
        panel.add(new JLabel("Chiều cao (cm)"));
        panel.add(txtHeight);
        panel.add(new JLabel("Huyết áp"));
        panel.add(txtBloodPressure);
        panel.add(new JLabel("Mạch"));
        panel.add(txtPulse);
        panel.add(new JLabel("Triệu chứng"));
        panel.add(new JScrollPane(txtSymptoms));
        panel.add(new JLabel("Chẩn đoán"));
        panel.add(new JScrollPane(txtDiagnosis));

        return panel;
    }

    // ================= BOTTOM BAR =================

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        RoundedButton btnSave = new RoundedButton(
                "Hoàn tất khám",
                UIConstants.SUCCESS_GREEN,
                UIConstants.SUCCESS_GREEN_DARK,
                8
        );

        btnSave.addActionListener(e -> onComplete());

        bar.add(btnSave);
        return bar;
    }

    private void onComplete() {

        if (selectedPatient == null) {
            JOptionPane.showMessageDialog(this, "Chọn bệnh nhân trước.");
            return;
        }

        try {
            double temperature = Double.parseDouble(txtTemperature.getText().trim());
            double weight = Double.parseDouble(txtWeight.getText().trim());
            double height = Double.parseDouble(txtHeight.getText().trim());
            String bp = txtBloodPressure.getText().trim();
            int pulse = Integer.parseInt(txtPulse.getText().trim());

            medicalRecordBUS.updateVitalSigns(
                    selectedRecordId,
                    temperature,
                    bp,
                    pulse,
                    weight,
                    height
            );

            String symptoms = txtSymptoms.getText().trim();
            String diagnosis = txtDiagnosis.getText().trim();

            medicalRecordBUS.updateDiagnosisAndSymptoms(
                    selectedRecordId,
                    diagnosis,
                    symptoms
            );

            queueBUS.updateQueueStatus(selectedRecordId, "PRESCRIBED");

            JOptionPane.showMessageDialog(this, "Hoàn tất khám thành công.");

            selectedIndex = -1;
            selectedPatient = null;
            selectedRecordId = -1;

            loadPatientList();
            updateRightPanel();

        } catch (BusinessException | DataAccessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Sinh hiệu không hợp lệ.");
        }
    }
}
