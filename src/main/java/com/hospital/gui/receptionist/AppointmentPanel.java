package com.hospital.gui.receptionist;

import com.hospital.bus.AppointmentBUS;
import com.hospital.bus.DoctorBUS;
import com.hospital.bus.PatientBUS;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Quản lý lịch hẹn — xem, tạo, xác nhận, hủy.
 */
public class AppointmentPanel extends JPanel {

    private final AppointmentBUS appointmentBUS = new AppointmentBUS();
    private final PatientBUS patientBUS = new PatientBUS();
    private final DoctorBUS doctorBUS = new DoctorBUS();

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboFilter;

    /* form fields */
    private JTextField txtPatientId, txtDate, txtStart, txtEnd, txtReason;
    private JComboBox<Doctor> cboDoctor;
    private int selectedId = -1;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public AppointmentPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createTablePanel(), createFormPanel());
        split.setDividerLocation(320);
        split.setResizeWeight(0.55);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        loadData();
    }

    /* ───── Header ───── */
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("📅 Quản lý lịch hẹn");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        cboFilter = new JComboBox<>(new String[]{"Tất cả", "Chờ xác nhận", "Đã xác nhận", "Hủy"});
        cboFilter.setFont(UIConstants.FONT_BODY);
        cboFilter.addActionListener(e -> loadData());

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterBar.setOpaque(false);
        filterBar.add(new JLabel("Lọc:"));
        filterBar.add(cboFilter);

        header.add(title, BorderLayout.WEST);
        header.add(filterBar, BorderLayout.EAST);
        return header;
    }

    /* ───── Table ───── */
    private JPanel createTablePanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 4));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Bệnh nhân", "SĐT", "Bác sĩ", "Ngày hẹn", "Bắt đầu", "Kết thúc", "Trạng thái", "Lý do"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(UIConstants.FONT_BODY);
        table.getTableHeader().setFont(UIConstants.FONT_LABEL);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    /* ───── Form ───── */
    private JPanel createFormPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(12, 8));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        txtPatientId = addTextField(fields, gbc, row++, "Mã bệnh nhân:", 10);
        txtDate = addTextField(fields, gbc, row++, "Ngày hẹn (dd/MM/yyyy):", 12);
        txtStart = addTextField(fields, gbc, row++, "Giờ BĐ (HH:mm):", 8);
        txtEnd = addTextField(fields, gbc, row++, "Giờ KT (HH:mm):", 8);
        txtReason = addTextField(fields, gbc, row++, "Lý do:", 30);

        /* doctor combo */
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        fields.add(createLabel("Bác sĩ:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cboDoctor = new JComboBox<>();
        cboDoctor.setFont(UIConstants.FONT_BODY);
        loadDoctors();
        fields.add(cboDoctor, gbc);

        card.add(fields, BorderLayout.CENTER);
        card.add(createButtonBar(), BorderLayout.SOUTH);
        return card;
    }

    private JPanel createButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bar.setOpaque(false);

        RoundedButton btnNew = new RoundedButton("🆕 Mới");
        btnNew.setBackground(UIConstants.ACCENT_BLUE);
        btnNew.setForeground(Color.WHITE);
        btnNew.addActionListener(e -> clearForm());

        RoundedButton btnSave = new RoundedButton("💾 Lưu");
        btnSave.setBackground(UIConstants.SUCCESS_GREEN);
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> save());

        RoundedButton btnConfirm = new RoundedButton("✅ Xác nhận");
        btnConfirm.setBackground(new Color(0x28A745));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.addActionListener(e -> confirmAppointment());

        RoundedButton btnCancel = new RoundedButton("❌ Hủy hẹn");
        btnCancel.setBackground(UIConstants.DANGER_RED);
        btnCancel.setForeground(Color.WHITE);
        btnCancel.addActionListener(e -> cancelAppointment());

        bar.add(btnNew);
        bar.add(btnSave);
        bar.add(btnConfirm);
        bar.add(btnCancel);
        return bar;
    }

    /* ───── Data ───── */
    private void loadDoctors() {
        try {
            List<Doctor> doctors = doctorBUS.findAll();
            cboDoctor.removeAllItems();
            for (Doctor d : doctors) {
                cboDoctor.addItem(d);
            }
        } catch (Exception ignored) { }
    }

    private void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                tableModel.setRowCount(0);
                String filter = (String) cboFilter.getSelectedItem();
                List<Appointment> list;
                if ("Tất cả".equals(filter)) {
                    list = appointmentBUS.findAll();
                } else {
                    list = appointmentBUS.getByStatus(filter);
                }
                for (Appointment a : list) {
                    tableModel.addRow(new Object[]{
                            a.getId(),
                            a.getPatientName() != null ? a.getPatientName() : String.valueOf(a.getPatientId()),
                            a.getPatientPhone() != null ? a.getPatientPhone() : "",
                            a.getDoctorName() != null ? a.getDoctorName() : String.valueOf(a.getDoctorId()),
                            a.getAppointmentDate() != null ? a.getAppointmentDate().format(DATE_FMT) : "",
                            a.getStartTime() != null ? a.getStartTime().format(TIME_FMT) : "",
                            a.getEndTime() != null ? a.getEndTime().format(TIME_FMT) : "",
                            a.getStatus(),
                            a.getReason()
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) tableModel.getValueAt(row, 0);

        Appointment a = appointmentBUS.findById(selectedId);
        if (a == null) return;

        txtPatientId.setText(String.valueOf(a.getPatientId()));
        txtDate.setText(a.getAppointmentDate() != null ? a.getAppointmentDate().format(DATE_FMT) : "");
        txtStart.setText(a.getStartTime() != null ? a.getStartTime().format(TIME_FMT) : "");
        txtEnd.setText(a.getEndTime() != null ? a.getEndTime().format(TIME_FMT) : "");
        txtReason.setText(a.getReason() != null ? a.getReason() : "");

        /* select doctor in combo */
        for (int i = 0; i < cboDoctor.getItemCount(); i++) {
            Doctor d = cboDoctor.getItemAt(i);
            if (d.getId() == a.getDoctorId()) {
                cboDoctor.setSelectedIndex(i);
                break;
            }
        }
    }

    private void save() {
        try {
            Appointment a = new Appointment();

            long patientId = Long.parseLong(txtPatientId.getText().trim());
            Patient p = patientBUS.findById((int) patientId);
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy bệnh nhân #" + patientId, "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            a.setPatientId(patientId);
            a.setPatientName(p.getFullName());
            a.setPatientPhone(p.getPhone());

            Doctor doc = (Doctor) cboDoctor.getSelectedItem();
            if (doc == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn bác sĩ", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            a.setDoctorId(doc.getId());
            a.setDoctorName(doc.toString());

            try {
                a.setAppointmentDate(LocalDate.parse(txtDate.getText().trim(), DATE_FMT));
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Ngày hẹn không hợp lệ (dd/MM/yyyy)", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                a.setStartTime(LocalTime.parse(txtStart.getText().trim(), TIME_FMT));
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Giờ bắt đầu không hợp lệ (HH:mm)", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!txtEnd.getText().trim().isEmpty()) {
                try {
                    a.setEndTime(LocalTime.parse(txtEnd.getText().trim(), TIME_FMT));
                } catch (DateTimeParseException e) {
                    JOptionPane.showMessageDialog(this, "Giờ kết thúc không hợp lệ (HH:mm)", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            a.setReason(txtReason.getText().trim());
            a.setStatus("Chờ xác nhận");

            if (selectedId > 0) {
                a.setId(selectedId);
                appointmentBUS.update(a);
            } else {
                appointmentBUS.insert(a);
            }

            loadData();
            clearForm();
            JOptionPane.showMessageDialog(this, "Lưu lịch hẹn thành công!", "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Mã bệnh nhân phải là số", "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmAppointment() {
        if (selectedId <= 0) return;
        try {
            appointmentBUS.confirm(selectedId);
            loadData();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelAppointment() {
        if (selectedId <= 0) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Hủy lịch hẹn này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                appointmentBUS.cancel(selectedId);
                loadData();
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtPatientId.setText("");
        txtDate.setText("");
        txtStart.setText("");
        txtEnd.setText("");
        txtReason.setText("");
        cboDoctor.setSelectedIndex(cboDoctor.getItemCount() > 0 ? 0 : -1);
        table.clearSelection();
    }

    /* ───── Helpers ───── */
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setPreferredSize(new Dimension(170, 28));
        return lbl;
    }

    private JTextField addTextField(JPanel form, GridBagConstraints gbc, int row, String label, int cols) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(createLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JTextField txt = new JTextField(cols);
        txt.setFont(UIConstants.FONT_BODY);
        form.add(txt, gbc);
        return txt;
    }
}
