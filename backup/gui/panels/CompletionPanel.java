package com.hospital.gui.panels;

import com.hospital.bus.*;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.MedicalRecordDAO;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.components.StatusBadge;
import com.hospital.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import com.hospital.util.MedicalRecordPrinter;

import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel Kết thúc khám (⑧) — Hoàn tất lượt khám.
 *
 * Hiển thị tóm tắt toàn bộ lượt khám (thông tin BN → triệu chứng → chẩn đoán
 * → đơn thuốc → phí thanh toán) và cho phép:
 *   - Đặt lịch hẹn tái khám (FollowUp)
 *   - Hoàn tất bệnh án (queue_status → COMPLETED)
 *   - In bệnh án tóm tắt
 */
public class CompletionPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(CompletionPanel.class.getName());
    private static final DecimalFormat MONEY_FMT = new DecimalFormat("#,###");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MedicalRecordDAO recordDAO = new MedicalRecordDAO();
    private final MedicalRecordBUS recordBUS = new MedicalRecordBUS();
    private final PatientBUS patientBUS = new PatientBUS();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final PrescriptionBUS prescriptionBUS = new PrescriptionBUS();
    private final DispensingBUS dispensingBUS = new DispensingBUS();
    private final InvoiceBUS invoiceBUS = new InvoiceBUS();
    private final FollowUpBUS followUpBUS = new FollowUpBUS();

    // Left: records ready for completion
    private DefaultTableModel recordTableModel;
    private JTable recordTable;
    private List<MedicalRecord> recordList = new ArrayList<>();

    // Right: summary + actions
    private JPanel rightPanel;
    private MedicalRecord selectedRecord;

    public CompletionPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
        loadRecords();
    }

    private void initComponents() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lblTitle = new JLabel("✅  Kết thúc khám");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(lblTitle, BorderLayout.WEST);

        RoundedButton btnRefresh = new RoundedButton("🔄 Làm mới");
        btnRefresh.addActionListener(e -> loadRecords());
        header.add(btnRefresh, BorderLayout.EAST);

        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        add(header, BorderLayout.NORTH);

        // Split
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createLeftPanel(), createRightPanel());
        split.setDividerLocation(360);
        split.setResizeWeight(0.3);
        split.setBorder(null);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);
    }

    // ════════════════════════════════════════════════════════════
    //  LEFT: Record list
    // ════════════════════════════════════════════════════════════

    private JPanel createLeftPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setLayout(new BorderLayout(0, 8));
        card.setBackground(UIConstants.CARD_BG);
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lbl = new JLabel("Bệnh án chờ kết thúc");
        lbl.setFont(UIConstants.FONT_SUBTITLE);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(lbl, BorderLayout.NORTH);

        String[] cols = {"#", "Mã BA", "Bệnh nhân", "Trạng thái"};
        recordTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        recordTable = new JTable(recordTableModel);
        recordTable.setFont(UIConstants.FONT_BODY);
        recordTable.setRowHeight(38);
        recordTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        recordTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        recordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recordTable.setSelectionBackground(UIConstants.PRIMARY_BG_SOFT);
        recordTable.setShowVerticalLines(false);
        recordTable.setGridColor(UIConstants.BORDER_COLOR);

        // Status badge
        recordTable.getColumnModel().getColumn(3).setCellRenderer(
                (t, v, sel, foc, r, c) -> {
                    StatusBadge badge = new StatusBadge(v == null ? "" : v.toString());
                    badge.setHorizontalAlignment(SwingConstants.CENTER);
                    return badge;
                });

        int[] widths = {30, 60, 150, 90};
        for (int i = 0; i < widths.length; i++)
            recordTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        recordTable.getColumnModel().getColumn(0).setCellRenderer(center);
        recordTable.getColumnModel().getColumn(1).setCellRenderer(center);

        recordTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRecordSelected();
        });

        JScrollPane scroll = new JScrollPane(recordTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ════════════════════════════════════════════════════════════
    //  RIGHT: Detail & actions
    // ════════════════════════════════════════════════════════════

    private JPanel createRightPanel() {
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(0, 8, 0, 0));
        showPlaceholder();
        return rightPanel;
    }

    private void showPlaceholder() {
        rightPanel.removeAll();
        JLabel lbl = new JLabel("Chọn bệnh án để xem tóm tắt", SwingConstants.CENTER);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        rightPanel.add(lbl, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    // ════════════════════════════════════════════════════════════
    //  DATA LOADING
    // ════════════════════════════════════════════════════════════

    private void loadRecords() {
        try {
            // Lấy bệnh án DISPENSED (đã phát thuốc, chờ thanh toán/kết thúc)
            // và PAID (đã thanh toán, chờ kết thúc)
            List<MedicalRecord> dispensed = recordDAO.getTodayByStatus("DISPENSED");
            List<MedicalRecord> paid = recordDAO.getTodayByStatus("PAID");
            recordList.clear();
            recordList.addAll(dispensed);
            recordList.addAll(paid);

            recordTableModel.setRowCount(0);
            int stt = 1;
            for (MedicalRecord r : recordList) {
                String patientName = "—";
                try {
                    Patient p = patientBUS.findById((int) r.getPatientId());
                    if (p != null) patientName = p.getFullName();
                } catch (Exception ignored) {}

                String statusDisplay = switch (r.getStatus()) {
                    case "DISPENSED" -> "Đã phát thuốc";
                    case "PAID"      -> "Đã thanh toán";
                    default          -> r.getStatus();
                };

                recordTableModel.addRow(new Object[]{
                        stt++,
                        r.getId(),
                        patientName,
                        statusDisplay
                });
            }

            showPlaceholder();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi tải danh sách bệnh án chờ kết thúc", e);
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRecordSelected() {
        int row = recordTable.getSelectedRow();
        if (row < 0 || row >= recordList.size()) {
            showPlaceholder();
            return;
        }
        selectedRecord = recordList.get(row);
        showSummary(selectedRecord);
    }

    // ════════════════════════════════════════════════════════════
    //  SUMMARY VIEW
    // ════════════════════════════════════════════════════════════

    private void showSummary(MedicalRecord record) {
        rightPanel.removeAll();

        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Scrollable content
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // 1. Patient info
        content.add(createSection("👤 Thông tin bệnh nhân", buildPatientInfo(record)));
        content.add(Box.createVerticalStrut(12));

        // 2. Vital signs
        content.add(createSection("💓 Sinh hiệu", buildVitalSigns(record)));
        content.add(Box.createVerticalStrut(12));

        // 3. Diagnosis
        content.add(createSection("🩺 Chẩn đoán", buildDiagnosisInfo(record)));
        content.add(Box.createVerticalStrut(12));

        // 4. Prescription
        content.add(createSection("💊 Đơn thuốc", buildPrescriptionInfo(record)));
        content.add(Box.createVerticalStrut(12));

        // 5. Payment
        content.add(createSection("💰 Thanh toán", buildPaymentInfo(record)));
        content.add(Box.createVerticalStrut(16));

        // 6. Follow-up scheduling
        content.add(createFollowUpSection(record));
        content.add(Box.createVerticalStrut(16));

        // 7. Action buttons
        content.add(createActionButtons(record));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        card.add(scrollPane, BorderLayout.CENTER);
        rightPanel.add(card, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    // ── Section builder ──────────────────────────────────────

    private JPanel createSection(String title, JPanel body) {
        JPanel section = new JPanel(new BorderLayout(0, 6));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, section.getPreferredSize().height));
        section.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIConstants.FONT_SUBTITLE);
        lblTitle.setForeground(UIConstants.PRIMARY);
        section.add(lblTitle, BorderLayout.NORTH);

        body.setAlignmentX(LEFT_ALIGNMENT);
        section.add(body, BorderLayout.CENTER);

        return section;
    }

    // ── 1. Patient Info ──────────────────────────────────────

    private JPanel buildPatientInfo(MedicalRecord record) {
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 4));
        p.setOpaque(false);

        try {
            Patient patient = patientBUS.findById((int) record.getPatientId());
            if (patient != null) {
                addInfoRow(p, "Họ tên:", patient.getFullName());
                addInfoRow(p, "Mã BN:", patient.getPatientCode() != null ? patient.getPatientCode() : "BN" + patient.getId());
                addInfoRow(p, "Giới tính:", patient.getGender() != null ? patient.getGender().getDisplayName() : "—");
                addInfoRow(p, "SĐT:", patient.getPhone() != null ? patient.getPhone() : "—");
                addInfoRow(p, "Ngày sinh:", patient.getDateOfBirth() != null ? patient.getDateOfBirth().format(DATE_FMT) : "—");
            } else {
                addInfoRow(p, "Patient ID:", String.valueOf(record.getPatientId()));
            }
        } catch (Exception e) {
            addInfoRow(p, "Patient ID:", String.valueOf(record.getPatientId()));
        }

        try {
            Doctor doctor = doctorDAO.findById((int) record.getDoctorId());
            addInfoRow(p, "Bác sĩ:", doctor != null ? doctor.getFullName() : "—");
        } catch (Exception e) {
            addInfoRow(p, "Bác sĩ:", "—");
        }

        addInfoRow(p, "Ngày khám:", record.getVisitDate() != null
                ? record.getVisitDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "—");

        return p;
    }

    // ── 2. Vital Signs ──────────────────────────────────────

    private JPanel buildVitalSigns(MedicalRecord r) {
        JPanel p = new JPanel(new GridLayout(0, 4, 12, 4));
        p.setOpaque(false);

        addVitalItem(p, "Cân nặng", r.getWeight() > 0 ? r.getWeight() + " kg" : "—");
        addVitalItem(p, "Chiều cao", r.getHeight() > 0 ? r.getHeight() + " cm" : "—");
        addVitalItem(p, "Huyết áp", r.getBloodPressure() != null ? r.getBloodPressure() : "—");
        addVitalItem(p, "Mạch", r.getPulse() > 0 ? r.getPulse() + " bpm" : "—");
        addVitalItem(p, "Nhiệt độ", r.getTemperature() > 0 ? r.getTemperature() + " °C" : "—");
        addVitalItem(p, "SpO2", r.getSpo2() > 0 ? r.getSpo2() + "%" : "—");

        return p;
    }

    private void addVitalItem(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_CAPTION);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        panel.add(lbl);

        JLabel val = new JLabel(value);
        val.setFont(UIConstants.FONT_BOLD);
        val.setForeground(UIConstants.TEXT_PRIMARY);
        panel.add(val);
    }

    // ── 3. Diagnosis ─────────────────────────────────────────

    private JPanel buildDiagnosisInfo(MedicalRecord r) {
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 4));
        p.setOpaque(false);

        addInfoRow(p, "Triệu chứng:", r.getSymptoms() != null ? r.getSymptoms() : "—");
        addInfoRow(p, "Chẩn đoán:", r.getDiagnosis() != null ? r.getDiagnosis() : "—");
        if (r.getDiagnosisCode() != null && !r.getDiagnosisCode().isEmpty()) {
            addInfoRow(p, "Mã ICD-10:", r.getDiagnosisCode());
        }
        if (r.getNotes() != null && !r.getNotes().isEmpty()) {
            addInfoRow(p, "Ghi chú BS:", r.getNotes());
        }

        return p;
    }

    // ── 4. Prescription ──────────────────────────────────────

    private JPanel buildPrescriptionInfo(MedicalRecord record) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);

        try {
            List<Prescription> prescriptions = prescriptionBUS.getByMedicalRecordId(record.getId());
            if (prescriptions.isEmpty()) {
                p.add(new JLabel("Không có đơn thuốc"), BorderLayout.CENTER);
                return p;
            }

            String[] cols = {"Thuốc", "Liều dùng", "SL", "Đơn giá", "Thành tiền"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            double totalMed = 0;
            for (Prescription presc : prescriptions) {
                List<PrescriptionDetail> details = prescriptionBUS.getDetails(presc.getId());
                for (PrescriptionDetail d : details) {
                    double lineTotal = d.getQuantity() * d.getUnitPrice();
                    totalMed += lineTotal;
                    model.addRow(new Object[]{
                            d.getMedicineName(),
                            d.getDosage() != null ? d.getDosage() : "—",
                            d.getQuantity(),
                            MONEY_FMT.format(d.getUnitPrice()) + " đ",
                            MONEY_FMT.format(lineTotal) + " đ"
                    });
                }
            }

            JTable table = new JTable(model);
            table.setFont(UIConstants.FONT_CAPTION);
            table.setRowHeight(28);
            table.getTableHeader().setFont(UIConstants.FONT_BOLD);
            table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
            table.setShowVerticalLines(false);
            table.setGridColor(UIConstants.BORDER_COLOR);

            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(0, Math.min(150, 28 + 28 * model.getRowCount())));
            scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
            p.add(scroll, BorderLayout.CENTER);

            JLabel lblTotal = new JLabel("Tổng tiền thuốc: " + MONEY_FMT.format(totalMed) + " đ");
            lblTotal.setFont(UIConstants.FONT_BOLD);
            lblTotal.setForeground(UIConstants.PRIMARY);
            lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
            lblTotal.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            p.add(lblTotal, BorderLayout.SOUTH);

        } catch (Exception e) {
            p.add(new JLabel("Lỗi tải đơn thuốc: " + e.getMessage()), BorderLayout.CENTER);
        }

        return p;
    }

    // ── 5. Payment Info ──────────────────────────────────────

    private JPanel buildPaymentInfo(MedicalRecord record) {
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 4));
        p.setOpaque(false);

        try {
            List<Invoice> invoices = invoiceBUS.findAll();
            Invoice inv = null;
            for (Invoice i : invoices) {
                if (i.getRecordId() != null && i.getRecordId() == (long) record.getId()) {
                    inv = invoiceBUS.getInvoiceDetails(i.getId());
                    break;
                }
            }

            if (inv != null) {
                addInfoRow(p, "Phí khám:", MONEY_FMT.format(inv.getExamFee()) + " đ");
                addInfoRow(p, "Tiền thuốc:", MONEY_FMT.format(inv.getMedicineFee()) + " đ");
                if (inv.getOtherFee() > 0) addInfoRow(p, "Phí dịch vụ:", MONEY_FMT.format(inv.getOtherFee()) + " đ");
                if (inv.getDiscount() > 0) addInfoRow(p, "Giảm giá:", "−" + MONEY_FMT.format(inv.getDiscount()) + " đ");
                addInfoRow(p, "TỔNG CỘNG:", MONEY_FMT.format(inv.getTotalAmount()) + " đ");
                addInfoRow(p, "Trạng thái:", inv.getStatusDisplay());
                if ("PAID".equals(inv.getStatus())) {
                    addInfoRow(p, "Đã thu:", MONEY_FMT.format(inv.getPaidAmount()) + " đ");
                }
            } else {
                addInfoRow(p, "Hóa đơn:", "Chưa tạo");
            }
        } catch (Exception e) {
            addInfoRow(p, "Hóa đơn:", "Lỗi tải: " + e.getMessage());
        }

        return p;
    }

    // ── 6. Follow-up ─────────────────────────────────────────

    private JPanel createFollowUpSection(MedicalRecord record) {
        RoundedPanel section = new RoundedPanel(8);
        section.setBackground(UIConstants.ACCENT_BLUE_SOFT);
        section.setLayout(new BorderLayout(8, 8));
        section.setBorder(new EmptyBorder(12, 16, 12, 16));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        section.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("📅 Hẹn tái khám");
        lblTitle.setFont(UIConstants.FONT_SUBTITLE);
        lblTitle.setForeground(UIConstants.ACCENT_BLUE);

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        fields.setOpaque(false);

        JLabel lblDate = new JLabel("Ngày:");
        lblDate.setFont(UIConstants.FONT_BODY);
        JTextField txtDate = new JTextField(10);
        txtDate.setFont(UIConstants.FONT_BODY);
        txtDate.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");
        // Pre-fill if follow_up_date set by doctor
        if (record.getFollowUpDate() != null) {
            txtDate.setText(record.getFollowUpDate().format(DATE_FMT));
        }

        JLabel lblReason = new JLabel("Lý do:");
        lblReason.setFont(UIConstants.FONT_BODY);
        JTextField txtReason = new JTextField(16);
        txtReason.setFont(UIConstants.FONT_BODY);
        txtReason.putClientProperty("JTextField.placeholderText", "Tái khám theo dõi...");

        RoundedButton btnSchedule = new RoundedButton("Đặt lịch");
        btnSchedule.setColors(UIConstants.ACCENT_BLUE, UIConstants.ACCENT_BLUE_DARK);
        btnSchedule.setForeground(Color.WHITE);
        btnSchedule.setPreferredSize(new Dimension(100, 32));
        btnSchedule.addActionListener(e -> {
            String dateStr = txtDate.getText().trim();
            String reason = txtReason.getText().trim();
            if (dateStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nhập ngày hẹn tái khám.",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                LocalDate date = LocalDate.parse(dateStr, DATE_FMT);
                FollowUp fu = new FollowUp();
                fu.setPatientId(record.getPatientId());
                fu.setRecordId(record.getId());
                fu.setFollowUpDate(date);
                fu.setReason(reason.isEmpty() ? "Tái khám" : reason);
                followUpBUS.scheduleFollowUp(fu);
                JOptionPane.showMessageDialog(this,
                        "Đã đặt lịch tái khám ngày " + dateStr,
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                btnSchedule.setEnabled(false);
                btnSchedule.setText("Đã đặt ✓");
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Ngày không hợp lệ. Định dạng: dd/MM/yyyy",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (BusinessException | DataAccessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        fields.add(lblDate);
        fields.add(txtDate);
        fields.add(lblReason);
        fields.add(txtReason);
        fields.add(btnSchedule);

        section.add(lblTitle, BorderLayout.NORTH);
        section.add(fields, BorderLayout.CENTER);

        return section;
    }

    // ── 7. Action buttons ────────────────────────────────────

    private JPanel createActionButtons(MedicalRecord record) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionPanel.setOpaque(false);
        actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        actionPanel.setAlignmentX(LEFT_ALIGNMENT);

        RoundedButton btnPrint = new RoundedButton("🖨 In bệnh án tóm tắt");
        btnPrint.setPreferredSize(new Dimension(180, 40));
        btnPrint.addActionListener(e -> printSummary(record));

        RoundedButton btnComplete = new RoundedButton("✅ Hoàn tất lượt khám");
        btnComplete.setColors(UIConstants.SUCCESS_GREEN, UIConstants.SUCCESS_GREEN_DARK);
        btnComplete.setForeground(Color.WHITE);
        btnComplete.setFont(UIConstants.FONT_BUTTON);
        btnComplete.setPreferredSize(new Dimension(180, 40));
        btnComplete.addActionListener(e -> completeVisit(record));

        // Disable completion if already COMPLETED
        if ("COMPLETED".equals(record.getStatus())) {
            btnComplete.setEnabled(false);
            btnComplete.setText("Đã hoàn tất ✓");
        }

        actionPanel.add(btnPrint);
        actionPanel.add(btnComplete);
        return actionPanel;
    }

    // ════════════════════════════════════════════════════════════
    //  ACTIONS
    // ════════════════════════════════════════════════════════════

    private void completeVisit(MedicalRecord record) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận hoàn tất lượt khám cho bệnh án #" + record.getId() + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            recordBUS.updateStatus(record.getId(), MedicalRecord.STATUS_COMPLETED);

            JOptionPane.showMessageDialog(this,
                    "Hoàn tất lượt khám thành công!\nBệnh án #" + record.getId(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadRecords();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi hoàn tất lượt khám #" + record.getId(), e);
            JOptionPane.showMessageDialog(this,
                    "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printSummary(MedicalRecord record) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu bệnh án tóm tắt PDF");
        chooser.setSelectedFile(new File("BenhAn_" + record.getId() + ".pdf"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File dest = chooser.getSelectedFile();
        if (!dest.getName().toLowerCase().endsWith(".pdf")) {
            dest = new File(dest.getAbsolutePath() + ".pdf");
        }

        try {
            MedicalRecordPrinter.exportPdf(record, dest.getAbsolutePath());
            int open = JOptionPane.showConfirmDialog(this,
                    "Xuất PDF thành công!\n" + dest.getAbsolutePath() + "\n\nMở file ngay?",
                    "Thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (open == JOptionPane.YES_OPTION) {
                java.awt.Desktop.getDesktop().open(dest);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi xuất PDF bệnh án #" + record.getId(), e);
            JOptionPane.showMessageDialog(this,
                    "Lỗi xuất PDF: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ════════════════════════════════════════════════════════════

    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        panel.add(lbl);

        JLabel val = new JLabel(value != null ? value : "—");
        val.setFont(UIConstants.FONT_BOLD);
        val.setForeground(UIConstants.TEXT_PRIMARY);
        panel.add(val);
    }
}
