package com.hospital.gui.panels;

import com.hospital.bus.DoctorBUS;
import com.hospital.bus.PatientBUS;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.components.StatusBadge;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Trang Khám bệnh.
 */
public class ExaminationPanel extends JPanel {

    private final PatientBUS patientBUS = new PatientBUS();
    private final DoctorBUS  doctorBUS  = new DoctorBUS();

    private DefaultTableModel waitingModel;
    private DefaultTableModel doctorModel;
    private JTable waitingTable;
    private JTextArea txtDiagnosis;
    private JTextArea txtPrescription;
    private JComboBox<String> cbDoctor;
    private JLabel lblSelectedPatient;

    public ExaminationPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
    }

    private void initComponents() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Khám bệnh");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY_RED);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createWaitingPanel(), createExamPanel());
        split.setOpaque(false);
        split.setDividerLocation(420);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        add(split, BorderLayout.CENTER);
    }

    // ── Waiting List ──────────────────────────────────────────────────────────
    private JPanel createWaitingPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 16, 18, 16));

        JLabel title = new JLabel("Danh sách chờ khám");
        title.setFont(UIConstants.FONT_SUBTITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Mã BN", "Tên bệnh nhân", "Loại khám", "Trạng thái"};
        waitingModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        waitingTable = new JTable(waitingModel);
        waitingTable.setRowHeight(44);
        waitingTable.setGridColor(UIConstants.BORDER_COLOR);
        waitingTable.setShowVerticalLines(false);
        waitingTable.setSelectionBackground(UIConstants.RED_BG_SOFT);
        waitingTable.setSelectionForeground(UIConstants.TEXT_PRIMARY);

        waitingTable.getColumnModel().getColumn(3).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean s, boolean f, int r, int c) {
                    StatusBadge b = new StatusBadge(v == null ? "" : v.toString());
                    b.setHorizontalAlignment(CENTER);
                    return b;
                }
            });

        // Select waiting patient → populate exam panel
        waitingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && waitingTable.getSelectedRow() >= 0) {
                String code = (String) waitingModel.getValueAt(waitingTable.getSelectedRow(), 0);
                String name = (String) waitingModel.getValueAt(waitingTable.getSelectedRow(), 1);
                lblSelectedPatient.setText(code + " – " + name);
                txtDiagnosis.setText("");
                txtPrescription.setText("");
            }
        });

        loadWaiting();
        card.add(new JScrollPane(waitingTable), BorderLayout.CENTER);

        RoundedButton callBtn = new RoundedButton("Gọi vào khám");
        callBtn.addActionListener(e -> callPatient());
        card.add(callBtn, BorderLayout.SOUTH);
        return card;
    }

    // ── Exam Panel ────────────────────────────────────────────────────────────
    private JPanel createExamPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Patient info header
        lblSelectedPatient = new JLabel("— Chưa chọn bệnh nhân —");
        lblSelectedPatient.setFont(UIConstants.FONT_SUBTITLE);
        lblSelectedPatient.setForeground(UIConstants.PRIMARY_RED);
        card.add(lblSelectedPatient, BorderLayout.NORTH);

        // Form fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);
        g.gridx = 0; g.weightx = 1;

        // Doctor selector
        g.gridy = 0;
        form.add(makeLabel("Bác sĩ phụ trách"), g);
        g.gridy = 1;
        List<Doctor> doctors = doctorBUS.findAll();
        String[] docNames = doctors.stream().map(Doctor::getFullName).toArray(String[]::new);
        cbDoctor = new JComboBox<>(docNames);
        form.add(cbDoctor, g);

        // Diagnosis
        g.gridy = 2;
        form.add(makeLabel("Chẩn đoán"), g);
        g.gridy = 3; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        txtDiagnosis = new JTextArea(4, 0);
        txtDiagnosis.setFont(UIConstants.FONT_LABEL);
        txtDiagnosis.setLineWrap(true);
        txtDiagnosis.setWrapStyleWord(true);
        txtDiagnosis.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        JScrollPane diagScroll = new JScrollPane(txtDiagnosis);
        diagScroll.setPreferredSize(new Dimension(0, 100));
        form.add(diagScroll, g);

        // Prescription
        g.gridy = 4; g.weighty = 0; g.fill = GridBagConstraints.HORIZONTAL;
        form.add(makeLabel("Đơn thuốc"), g);
        g.gridy = 5; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        txtPrescription = new JTextArea(4, 0);
        txtPrescription.setFont(UIConstants.FONT_LABEL);
        txtPrescription.setLineWrap(true);
        txtPrescription.setWrapStyleWord(true);
        txtPrescription.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        JScrollPane presScroll = new JScrollPane(txtPrescription);
        presScroll.setPreferredSize(new Dimension(0, 100));
        form.add(presScroll, g);

        card.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        RoundedButton btnSave = new RoundedButton("Lưu kết quả");
        btnSave.setPreferredSize(new Dimension(140, 36));
        btnSave.addActionListener(e -> saveExamResult());

        RoundedButton btnDone = new RoundedButton("Hoàn tất khám");
        btnDone.setColors(UIConstants.SUCCESS_GREEN, UIConstants.SUCCESS_GREEN.darker());
        btnDone.setPreferredSize(new Dimension(150, 36));
        btnDone.addActionListener(e -> markDone());

        btns.add(btnSave);
        btns.add(btnDone);
        card.add(btns, BorderLayout.SOUTH);
        return card;
    }

    // ── Logic ─────────────────────────────────────────────────────────────────
    private void loadWaiting() {
        waitingModel.setRowCount(0);
        for (Patient p : patientBUS.getWaitingPatients()) {
            waitingModel.addRow(new Object[]{
                p.getPatientCode(), p.getFullName(), p.getExamType(), p.getStatus()
            });
        }
    }

    private void callPatient() {
        int row = waitingTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn bệnh nhân để gọi vào khám."); return; }
        String code = (String) waitingModel.getValueAt(row, 0);
        patientBUS.findAll().stream()
            .filter(p -> p.getPatientCode().equals(code))
            .findFirst()
            .ifPresent(p -> { patientBUS.updateStatus(p.getId(), "ĐANG KHÁM"); loadWaiting(); });
        JOptionPane.showMessageDialog(this, "Đã gọi bệnh nhân vào phòng khám.", "Gọi khám", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveExamResult() {
        if (lblSelectedPatient.getText().startsWith("—")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bệnh nhân."); return;
        }
        JOptionPane.showMessageDialog(this, "Đã lưu kết quả khám.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void markDone() {
        if (lblSelectedPatient.getText().startsWith("—")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bệnh nhân."); return;
        }
        JOptionPane.showMessageDialog(this, "Đã hoàn tất khám. Chuyển sang thanh toán.", "Xong", JOptionPane.INFORMATION_MESSAGE);
        loadWaiting();
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        return lbl;
    }
}
