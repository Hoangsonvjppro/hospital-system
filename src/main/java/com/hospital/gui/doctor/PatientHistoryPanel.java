package com.hospital.gui.doctor;

import com.hospital.bus.MedicalRecordBUS;
import com.hospital.gui.common.*;
import com.hospital.model.MedicalRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tiền sử bệnh nhân — tra cứu lịch sử khám bệnh theo mã BN.
 */
public class PatientHistoryPanel extends JPanel {

    private final MedicalRecordBUS recordBUS = new MedicalRecordBUS();

    private JTextField txtPatientId;
    private JTable table;
    private DefaultTableModel tableModel;

    /* detail area */
    private JTextArea txtDetail;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PatientHistoryPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
    }

    /* ───── Header ───── */
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("📜 Tiền sử bệnh nhân");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        txtPatientId = new JTextField(10);
        txtPatientId.setFont(UIConstants.FONT_BODY);
        txtPatientId.putClientProperty("JTextField.placeholderText", "Mã bệnh nhân...");
        txtPatientId.addActionListener(e -> loadHistory());

        RoundedButton btnLoad = new RoundedButton("🔍 Tra cứu");
        btnLoad.setBackground(UIConstants.PRIMARY);
        btnLoad.setForeground(Color.WHITE);
        btnLoad.addActionListener(e -> loadHistory());

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setOpaque(false);
        searchBar.add(new JLabel("Patient ID:"));
        searchBar.add(txtPatientId);
        searchBar.add(btnLoad);

        header.add(title, BorderLayout.WEST);
        header.add(searchBar, BorderLayout.EAST);
        return header;
    }

    /* ───── Body: Table + Detail ───── */
    private JPanel createBody() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createTablePanel(), createDetailPanel());
        split.setDividerLocation(280);
        split.setResizeWeight(0.55);
        split.setBorder(null);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(split, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel createTablePanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 4));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        tableModel = new DefaultTableModel(
                new String[]{"Record ID", "Ngày khám", "Loại", "Trạng thái", "Chẩn đoán", "Bác sĩ"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(UIConstants.FONT_BODY);
        table.getTableHeader().setFont(UIConstants.FONT_LABEL);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetail();
        });

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel createDetailPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 4));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lbl = new JLabel("📋 Chi tiết bệnh án");
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);

        txtDetail = new JTextArea();
        txtDetail.setEditable(false);
        txtDetail.setFont(UIConstants.FONT_BODY);
        txtDetail.setLineWrap(true);
        txtDetail.setWrapStyleWord(true);
        txtDetail.setBackground(UIConstants.FIELD_BG);

        card.add(lbl, BorderLayout.NORTH);
        card.add(new JScrollPane(txtDetail), BorderLayout.CENTER);
        return card;
    }

    /* ───── Data ───── */
    private void loadHistory() {
        String idStr = txtPatientId.getText().trim();
        if (idStr.isEmpty()) return;
        try {
            long patientId = Long.parseLong(idStr);
            tableModel.setRowCount(0);
            txtDetail.setText("");
            List<MedicalRecord> records = recordBUS.getHistoryByPatient(patientId);
            if (records.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy lịch sử cho bệnh nhân #" + patientId,
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            for (MedicalRecord r : records) {
                tableModel.addRow(new Object[]{
                        r.getId(),
                        r.getVisitDate() != null ? r.getVisitDate().format(FMT) : "",
                        r.getVisitType() != null ? r.getVisitType() : "",
                        r.getQueueStatus() != null ? r.getQueueStatus() : "",
                        r.getDiagnosis() != null ? r.getDiagnosis() : "",
                        r.getDoctorName() != null ? r.getDoctorName() : ""
                });
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Mã bệnh nhân phải là số", "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDetail() {
        int row = table.getSelectedRow();
        if (row < 0) { txtDetail.setText(""); return; }

        int recordId = (int) tableModel.getValueAt(row, 0);
        try {
            MedicalRecord r = recordBUS.findById(recordId);
            if (r == null) { txtDetail.setText("Không tìm thấy."); return; }

            StringBuilder sb = new StringBuilder();
            sb.append("🏷 Record #").append(r.getId()).append("\n");
            sb.append("📅 Ngày khám: ").append(r.getVisitDate() != null ? r.getVisitDate().format(FMT) : "—").append("\n");
            sb.append("🩺 Bác sĩ: ").append(r.getDoctorName() != null ? r.getDoctorName() : "—").append("\n");
            sb.append("📋 Loại: ").append(r.getVisitType() != null ? r.getVisitType() : "—").append("\n");
            sb.append("🔖 Trạng thái: ").append(r.getQueueStatus() != null ? r.getQueueStatus() : "—").append("\n\n");

            sb.append("── Sinh hiệu ──\n");
            sb.append("   Huyết áp: ").append(r.getBloodPressure() != null ? r.getBloodPressure() : "—").append("\n");
            sb.append("   Mạch: ").append(r.getHeartRate() != null ? r.getHeartRate() : "—").append(" bpm\n");
            sb.append("   Nhiệt độ: ").append(r.getTemperature() != null ? r.getTemperature() : "—").append(" °C\n");
            sb.append("   Cân nặng: ").append(r.getWeight() != null ? r.getWeight() : "—").append(" kg\n");
            sb.append("   Chiều cao: ").append(r.getHeight() != null ? r.getHeight() : "—").append(" cm\n");
            sb.append("   SpO2: ").append(r.getSpo2() != null ? r.getSpo2() : "—").append(" %\n\n");

            sb.append("── Triệu chứng ──\n").append(r.getSymptoms() != null ? r.getSymptoms() : "—").append("\n\n");
            sb.append("── Chẩn đoán ──\n").append(r.getDiagnosis() != null ? r.getDiagnosis() : "—");
            if (r.getDiagnosisCode() != null && !r.getDiagnosisCode().isEmpty()) {
                sb.append(" (").append(r.getDiagnosisCode()).append(")");
            }
            sb.append("\n\n");
            if (r.getNotes() != null && !r.getNotes().isEmpty()) {
                sb.append("── Ghi chú ──\n").append(r.getNotes()).append("\n\n");
            }
            if (r.getReferralNote() != null && !r.getReferralNote().isEmpty()) {
                sb.append("── Chuyển viện ──\n").append(r.getReferralNote()).append("\n");
            }

            txtDetail.setText(sb.toString());
            txtDetail.setCaretPosition(0);
        } catch (Exception ex) {
            txtDetail.setText("Lỗi: " + ex.getMessage());
        }
    }
}
