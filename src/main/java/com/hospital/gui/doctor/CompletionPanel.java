package com.hospital.gui.doctor;

import com.hospital.bus.FollowUpBUS;
import com.hospital.bus.InvoiceBUS;
import com.hospital.bus.MedicalRecordBUS;
import com.hospital.bus.PrescriptionBUS;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ⑧ Kết thúc khám — tóm tắt bệnh án, hẹn tái khám, chuyển thanh toán.
 */
public class CompletionPanel extends JPanel {

    private final MedicalRecordBUS recordBUS = new MedicalRecordBUS();
    private final FollowUpBUS followUpBUS = new FollowUpBUS();
    private final InvoiceBUS invoiceBUS = new InvoiceBUS();
    private final PrescriptionBUS prescriptionBUS = new PrescriptionBUS();

    private JTextField txtRecordId;
    private JPanel summaryPanel;
    private JTextField txtFollowUpDate, txtFollowUpReason;
    private MedicalRecord currentRecord;

    public CompletionPanel() {
        this(null);
    }

    public CompletionPanel(Long initialRecordId) {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        add(createActionBar(), BorderLayout.SOUTH);

        if (initialRecordId != null) {
            txtRecordId.setText(String.valueOf(initialRecordId));
            loadRecord();
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);

        JLabel title = new JLabel("✅ Kết thúc khám bệnh");
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
        btnLoad.addActionListener(e -> loadRecord());
        idBar.add(btnLoad);

        header.add(title, BorderLayout.NORTH);
        header.add(idBar, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new GridLayout(1, 2, 16, 0));
        body.setOpaque(false);

        // Left: Summary
        summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setOpaque(false);
        summaryPanel.add(createPlaceholderLabel("Nhập mã bệnh án để xem tóm tắt"));

        RoundedPanel leftCard = new RoundedPanel(UIConstants.CARD_RADIUS);
        leftCard.setBackground(UIConstants.CARD_BG);
        leftCard.setLayout(new BorderLayout());
        leftCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblSummary = new JLabel("📋 Tóm tắt bệnh án");
        lblSummary.setFont(UIConstants.FONT_SUBTITLE);
        lblSummary.setForeground(UIConstants.TEXT_PRIMARY);
        leftCard.add(lblSummary, BorderLayout.NORTH);

        JScrollPane scrollSummary = new JScrollPane(summaryPanel);
        scrollSummary.setBorder(null);
        scrollSummary.getViewport().setOpaque(false);
        leftCard.add(scrollSummary, BorderLayout.CENTER);

        // Right: Follow-up scheduling
        RoundedPanel rightCard = new RoundedPanel(UIConstants.CARD_RADIUS);
        rightCard.setBackground(UIConstants.CARD_BG);
        rightCard.setLayout(new BorderLayout(0, 12));
        rightCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblFollowUp = new JLabel("📅 Hẹn tái khám");
        lblFollowUp.setFont(UIConstants.FONT_SUBTITLE);
        lblFollowUp.setForeground(UIConstants.TEXT_PRIMARY);
        rightCard.add(lblFollowUp, BorderLayout.NORTH);

        JPanel followUpForm = new JPanel(new GridBagLayout());
        followUpForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        followUpForm.add(createLabel("Ngày tái khám:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtFollowUpDate = new JTextField(12);
        txtFollowUpDate.setFont(UIConstants.FONT_BODY);
        txtFollowUpDate.setToolTipText("yyyy-MM-dd");
        followUpForm.add(txtFollowUpDate, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        followUpForm.add(createLabel("Lý do:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtFollowUpReason = new JTextField(20);
        txtFollowUpReason.setFont(UIConstants.FONT_BODY);
        followUpForm.add(txtFollowUpReason, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        RoundedButton btnSchedule = new RoundedButton("📅 Đặt lịch tái khám");
        btnSchedule.setBackground(UIConstants.WARNING_ORANGE);
        btnSchedule.setForeground(Color.WHITE);
        btnSchedule.addActionListener(e -> scheduleFollowUp());
        followUpForm.add(btnSchedule, gbc);

        rightCard.add(followUpForm, BorderLayout.CENTER);

        // Today's follow-ups
        JPanel todaySection = new JPanel(new BorderLayout(0, 4));
        todaySection.setOpaque(false);
        JLabel lblToday = new JLabel("Tái khám hôm nay:");
        lblToday.setFont(UIConstants.FONT_BOLD);
        todaySection.add(lblToday, BorderLayout.NORTH);

        JTextArea txtTodayFollowUps = new JTextArea(4, 20);
        txtTodayFollowUps.setFont(UIConstants.FONT_LABEL);
        txtTodayFollowUps.setEditable(false);
        txtTodayFollowUps.setLineWrap(true);
        try {
            List<FollowUp> todayList = followUpBUS.getTodayFollowUps();
            StringBuilder sb = new StringBuilder();
            for (FollowUp fu : todayList) {
                sb.append("• ").append(fu.getPatientName() != null ? fu.getPatientName() : "BN #" + fu.getPatientId())
                        .append(" — ").append(fu.getReason() != null ? fu.getReason() : "").append("\n");
            }
            txtTodayFollowUps.setText(sb.length() > 0 ? sb.toString() : "Không có lịch tái khám hôm nay.");
        } catch (Exception ex) {
            txtTodayFollowUps.setText("Lỗi tải dữ liệu.");
        }
        todaySection.add(new JScrollPane(txtTodayFollowUps), BorderLayout.CENTER);
        rightCard.add(todaySection, BorderLayout.SOUTH);

        body.add(leftCard);
        body.add(rightCard);
        return body;
    }

    private JPanel createActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(8, 0, 0, 0));

        RoundedButton btnComplete = new RoundedButton("✅ Hoàn tất & Chuyển thanh toán");
        btnComplete.setBackground(UIConstants.SUCCESS_GREEN);
        btnComplete.setForeground(Color.WHITE);
        btnComplete.addActionListener(e -> completeAndCreateInvoice());
        bar.add(btnComplete);

        return bar;
    }

    // ── Data ──

    private void loadRecord() {
        String idText = txtRecordId.getText().trim();
        if (idText.isEmpty()) return;

        try {
            long recordId = Long.parseLong(idText);
            currentRecord = recordBUS.findById(recordId);
            if (currentRecord == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy bệnh án #" + idText);
                return;
            }
            displaySummary();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Mã bệnh án không hợp lệ.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displaySummary() {
        summaryPanel.removeAll();

        addSummaryRow("Bệnh nhân", currentRecord.getPatientName() != null
                ? currentRecord.getPatientName() : "BN #" + currentRecord.getPatientId());
        addSummaryRow("Bác sĩ", currentRecord.getDoctorName() != null
                ? currentRecord.getDoctorName() : "BS #" + currentRecord.getDoctorId());
        addSummaryRow("Trạng thái", currentRecord.getStatus() != null ? currentRecord.getStatus() : "N/A");

        addSummaryRow("Sinh hiệu",
                String.format("HA: %s | Mạch: %s | Nhiệt: %s°C | SpO2: %s%%",
                        currentRecord.getBloodPressure() != null ? currentRecord.getBloodPressure() : "—",
                        currentRecord.getHeartRate() != null ? currentRecord.getHeartRate() : "—",
                        currentRecord.getTemperature() != null ? currentRecord.getTemperature() : "—",
                        currentRecord.getSpo2() != null ? currentRecord.getSpo2() : "—"));

        addSummaryRow("Triệu chứng", currentRecord.getSymptoms() != null ? currentRecord.getSymptoms() : "—");
        addSummaryRow("Chẩn đoán", (currentRecord.getDiagnosisCode() != null ? currentRecord.getDiagnosisCode() + " — " : "")
                + (currentRecord.getDiagnosis() != null ? currentRecord.getDiagnosis() : "—"));
        addSummaryRow("Ghi chú", currentRecord.getNotes() != null ? currentRecord.getNotes() : "—");

        // Prescriptions
        try {
            List<Prescription> rxList = prescriptionBUS.getByMedicalRecordId(currentRecord.getId());
            if (!rxList.isEmpty()) {
                addSummaryRow("Đơn thuốc", rxList.size() + " đơn");
                for (Prescription rx : rxList) {
                    List<PrescriptionDetail> details = prescriptionBUS.getDetails(rx.getId());
                    for (PrescriptionDetail d : details) {
                        addSummaryRow("  • " + (d.getMedicineName() != null ? d.getMedicineName() : ""),
                                "SL: " + d.getQuantity() + " | " + (d.getDosage() != null ? d.getDosage() : ""));
                    }
                }
            }
        } catch (Exception ignored) {}

        summaryPanel.revalidate();
        summaryPanel.repaint();
    }

    private void addSummaryRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setBorder(new EmptyBorder(2, 0, 2, 0));

        JLabel lblKey = new JLabel(label + ":");
        lblKey.setFont(UIConstants.FONT_BOLD);
        lblKey.setForeground(UIConstants.TEXT_SECONDARY);
        lblKey.setPreferredSize(new Dimension(120, 24));
        row.add(lblKey, BorderLayout.WEST);

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(UIConstants.FONT_LABEL);
        lblVal.setForeground(UIConstants.TEXT_PRIMARY);
        row.add(lblVal, BorderLayout.CENTER);

        summaryPanel.add(row);
    }

    // ── Actions ──

    private void scheduleFollowUp() {
        if (currentRecord == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng tải bệnh án trước.");
            return;
        }

        String dateStr = txtFollowUpDate.getText().trim();
        String reason = txtFollowUpReason.getText().trim();
        if (dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập ngày tái khám (yyyy-MM-dd).");
            return;
        }

        try {
            LocalDate followUpDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (!followUpDate.isAfter(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Ngày tái khám phải sau hôm nay.");
                return;
            }

            FollowUp fu = new FollowUp();
            fu.setPatientId(currentRecord.getPatientId());
            fu.setRecordId(currentRecord.getId());
            fu.setDoctorId(currentRecord.getDoctorId());
            fu.setFollowUpDate(followUpDate);
            fu.setReason(reason.isEmpty() ? "Tái khám" : reason);
            fu.setStatus(FollowUp.STATUS_SCHEDULED);

            followUpBUS.scheduleFollowUp(fu);
            JOptionPane.showMessageDialog(this, "Đặt lịch tái khám thành công: " + dateStr,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            txtFollowUpDate.setText("");
            txtFollowUpReason.setText("");
        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Định dạng ngày không hợp lệ. Dùng yyyy-MM-dd.");
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void completeAndCreateInvoice() {
        if (currentRecord == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng tải bệnh án trước.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Hoàn tất khám bệnh và tạo hóa đơn cho bệnh án #" + currentRecord.getId() + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            recordBUS.updateStatus(currentRecord.getId(), MedicalRecord.STATUS_COMPLETED);
            
            // Mark queue entry as COMPLETED too if it exists
            try {
                com.hospital.bus.QueueBUS qBus = new com.hospital.bus.QueueBUS();
                for (com.hospital.model.QueueEntry qe : qBus.getTodayQueue()) {
                    if (qe.getPatientId() == currentRecord.getPatientId() && 
                        qe.getStatus() == com.hospital.model.QueueEntry.QueueStatus.IN_PROGRESS) {
                        qBus.updateQueueEntryStatus(qe.getId(), com.hospital.model.QueueEntry.QueueStatus.COMPLETED);
                        break;
                    }
                }
            } catch (Exception ignored) {}
            try {
                invoiceBUS.createInvoiceFromMedicalRecord(currentRecord.getId());
            } catch (Exception ex) {
                // Invoice may already exist
            }
            JOptionPane.showMessageDialog(this, "Hoàn tất khám bệnh & tạo hóa đơn thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadRecord(); // Refresh summary
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        return lbl;
    }

    private JLabel createPlaceholderLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(UIConstants.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
}
