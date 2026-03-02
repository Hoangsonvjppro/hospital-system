package com.hospital.gui.panels;

import com.hospital.bus.PatientBUS;
import com.hospital.bus.MedicalRecordBUS;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel quản lý bệnh nhân — hiển thị danh sách + thêm mới.
 */
public class PatientPanel extends JPanel {

    private JTextField txtName, txtPhone, txtAddress, txtDob;
    private JComboBox<Patient.Gender> cmbGender;
    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTable queueTable;
    private DefaultTableModel queueTableModel;
    private JTabbedPane tabbedPane;
    private final PatientBUS patientBUS = new PatientBUS();
    private final MedicalRecordBUS medicalRecordBUS = new MedicalRecordBUS();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PatientPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(24, 28, 24, 28));
        initComponents();
        loadData();
        refreshQueue();
    }

    private void initComponents() {
        add(createHeaderPanel(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Đăng ký / Tìm kiếm", createRegistrationTab());
        tabbedPane.addTab("Hàng đợi hôm nay", createQueueTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // Create Registration/Search tab
    private JPanel createRegistrationTab() {
        JPanel root = new JPanel(new BorderLayout(16, 12));
        root.setOpaque(false);

        // Left: form
        RoundedPanel formCard = new RoundedPanel(UIConstants.CARD_RADIUS);
        formCard.setBackground(UIConstants.CARD_BG);
        formCard.setBorder(new EmptyBorder(12,12,12,12));
        formCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6,4,6,4);
        gbc.weightx = 1;

    JTextField txtCccd = new JTextField();
    JTextField txtAllergy = new JTextField();
    JTextField txtNotes = new JTextField();
    JCheckBox chkEmergency = new JCheckBox("Cấp cứu");

    txtName    = new JTextField();
    cmbGender  = new JComboBox<>(Patient.Gender.values());
    cmbGender.setSelectedItem(Patient.Gender.MALE);
    txtPhone   = new JTextField();
    txtAddress = new JTextField();
    txtDob     = new JTextField();

    JLabel[] labels = new JLabel[]{
        new JLabel("Họ tên:"), new JLabel("Giới tính:"), new JLabel("SĐT:"), new JLabel("CCCD:"),
        new JLabel("Ngày sinh:"), new JLabel("Địa chỉ:"), new JLabel("Tiền sử dị ứng:"), new JLabel("Ghi chú:")
    };
    JComponent[] inputs = new JComponent[]{txtName, cmbGender, txtPhone, txtCccd, txtDob, txtAddress, txtAllergy, txtNotes};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            labels[i].setFont(UIConstants.FONT_BOLD);
            formCard.add(labels[i], gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            inputs[i].setFont(UIConstants.FONT_BODY);
            formCard.add(inputs[i], gbc);
        }

        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2; gbc.weightx = 1;
        formCard.add(chkEmergency, gbc);

        RoundedButton btnRegister = new RoundedButton("Đăng ký khám");
        btnRegister.addActionListener(e -> {
            try {
                // If a patient row is selected, use it; else create a new patient
                int selected = table.getSelectedRow();
                Patient p;
                if (selected >= 0) {
                    int pid = (int) tableModel.getValueAt(selected, 0);
                    p = patientBUS.findById(pid);
                } else {
                    p = new Patient();
                    p.setFullName(txtName.getText().trim());
                    p.setPhone(txtPhone.getText().trim());
                    p.setAddress(txtAddress.getText().trim());
                    String dobText = txtDob.getText().trim();
                    if (!dobText.isEmpty()) {
                        // Try parsing dd/MM/yyyy first, then ISO yyyy-MM-dd as fallback
                        try {
                            p.setDateOfBirth(LocalDate.parse(dobText, dateFmt));
                        } catch (Exception exParse) {
                            try {
                                p.setDateOfBirth(LocalDate.parse(dobText));
                            } catch (Exception ex2) {
                                throw new IllegalArgumentException("Định dạng ngày sinh không hợp lệ. Vui lòng dùng dd/MM/yyyy hoặc yyyy-MM-dd.");
                            }
                        }
                    }
                    // map cccd/allergy/notes if columns exist in model
                    // gender
                    try { p.setGender((Patient.Gender)((JComboBox<Patient.Gender>)inputs[1]).getSelectedItem()); } catch (Exception ignored) {}
                    try { p.setCccd(((JTextField)inputs[3]).getText().trim()); } catch (Exception ignored) {}
                    try { p.setAllergyHistory(((JTextField)inputs[6]).getText().trim()); } catch (Exception ignored) {}
                    try { p.setNotes(((JTextField)inputs[7]).getText().trim()); } catch (Exception ignored) {}

                    patientBUS.insert(p);
                    loadData();
                }

                // ASSUMPTION: receptionist assigns to default doctorId = 1. Can be changed to a selectable dropdown later.
                long doctorId = 1L;
                boolean isEmergency = chkEmergency.isSelected();
                String examType = "Kham tong quat";

        long recordId = medicalRecordBUS.enqueuePatient(p.getId(), doctorId, null, isEmergency, examType);
        // Fetch the created record to show the assigned queue number (STT)
        try {
            com.hospital.model.MedicalRecord created = medicalRecordBUS.findById(recordId);
            String display = (created != null && created.getQueueNumber() != null)
                ? String.valueOf(created.getQueueNumber())
                : String.valueOf(recordId); // fallback to recordId
            JOptionPane.showMessageDialog(this, "Đăng ký thành công. Số thứ tự: " + display,
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex2) {
            // If anything goes wrong fetching record, fallback to recordId
            JOptionPane.showMessageDialog(this, "Đăng ký thành công. ID bản ghi: " + recordId,
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
                refreshQueue();
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridy = labels.length + 1; gbc.anchor = GridBagConstraints.EAST; gbc.insets = new Insets(12,4,0,4);
        formCard.add(btnRegister, gbc);

        // Right: search + results table (reuse existing table)
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        // Use the header search field instead of duplicating it here.
        right.add(createTablePanel(), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formCard, right);
        split.setResizeWeight(0.35);
        root.add(split, BorderLayout.CENTER);

        return root;
    }

    // Create Queue tab
    private JPanel createQueueTab() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout());

        queueTableModel = new DefaultTableModel(new String[]{"STT","Tên BN","Trạng thái","Thời gian đến","Ưu tiên","Hành động"},0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        queueTable = new JTable(queueTableModel);
        queueTable.setRowHeight(40);
        JScrollPane scroll = new JScrollPane(queueTable);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);

        // Action buttons panel
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        RoundedButton btnPromote = new RoundedButton("Đẩy ưu tiên");
        btnPromote.addActionListener(e -> {
            int sel = queueTable.getSelectedRow();
            if (sel < 0) return;
            // For now just show placeholder — implementing promotion requires update DAO/DB (could set priority to EMERGENCY)
            JOptionPane.showMessageDialog(this, "Tính năng đẩy ưu tiên tạm (chưa cập nhật DB)", "Info", JOptionPane.INFORMATION_MESSAGE);
        });
        actions.add(btnPromote);
        card.add(actions, BorderLayout.SOUTH);

        return card;
    }

    // ════════════════════════════════════════════════════════════════
    //  HEADER: Title + Search + Add button
    // ════════════════════════════════════════════════════════════════

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(16, 12));
        header.setOpaque(false);

        // Title row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý Bệnh nhân");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        titleRow.add(lblTitle, BorderLayout.WEST);

    // NOTE: 'Thêm bệnh nhân' button removed — registration is done via the "Đăng ký khám" button in the tab

        header.add(titleRow, BorderLayout.NORTH);

        // Search bar
        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên hoặc SĐT...");
        txtSearch.setFont(UIConstants.FONT_BODY);
        txtSearch.setPreferredSize(new Dimension(0, 38));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(0, 14, 0, 14)));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterTable(txtSearch.getText().trim().toLowerCase());
            }
        });

        header.add(txtSearch, BorderLayout.SOUTH);
        return header;
    }

    // ════════════════════════════════════════════════════════════════
    //  TABLE
    // ════════════════════════════════════════════════════════════════

    private JPanel createTablePanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(2, 0, 2, 0));

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Họ tên", "Giới tính", "Ngày sinh", "SĐT", "Địa chỉ"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(UIConstants.FONT_LABEL);
        table.setRowHeight(40);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setSelectionBackground(UIConstants.PRIMARY_BG_SOFT);
        table.setSelectionForeground(UIConstants.TEXT_PRIMARY);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(UIConstants.FONT_BOLD);
        header.setBackground(UIConstants.TABLE_HEADER_BG);
        header.setForeground(UIConstants.TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);

        // Cell renderer
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 14, 0, 14));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Column widths
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(2).setMaxWidth(100);
        table.getColumnModel().getColumn(3).setMaxWidth(120);
        table.getColumnModel().getColumn(4).setMaxWidth(130);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.CARD_BG);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ════════════════════════════════════════════════════════════════
    //  ADD DIALOG
    // ════════════════════════════════════════════════════════════════

    private void showAddDialog() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setPreferredSize(new Dimension(380, 220));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.weightx = 1;

        txtName    = new JTextField();
        cmbGender  = new JComboBox<>(Patient.Gender.values());
        cmbGender.setSelectedItem(Patient.Gender.MALE);
        txtPhone   = new JTextField();
        txtAddress = new JTextField();
        txtDob     = new JTextField();

        String[][] fields = {
            {"Họ tên:", "Nguyễn Văn A"},
            {"Giới tính:", "Nam"},
            {"SĐT:", "0901234567"},
            {"Địa chỉ:", "123 Đường ABC, Q1, TP.HCM"},
            {"Ngày sinh:", "yyyy-MM-dd"}
        };
        JComponent[] inputs = {txtName, cmbGender, txtPhone, txtAddress, txtDob};

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            JLabel lbl = new JLabel(fields[i][0]);
            lbl.setFont(UIConstants.FONT_BOLD);
            form.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 1;
            inputs[i].setFont(UIConstants.FONT_BODY);
            inputs[i].putClientProperty("JTextField.placeholderText", fields[i][1]);
            form.add(inputs[i], gbc);
        }

        int result = JOptionPane.showConfirmDialog(this, form,
                "Thêm bệnh nhân mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            addPatient();
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  ACTIONS
    // ════════════════════════════════════════════════════════════════

    private void addPatient() {
        try {
            Patient p = new Patient();
            p.setFullName(txtName.getText().trim());
            try { p.setGender((Patient.Gender)cmbGender.getSelectedItem()); } catch (Exception ignored) {}
            p.setPhone(txtPhone.getText().trim());
            p.setAddress(txtAddress.getText().trim());

            String dobText = txtDob.getText().trim();
            if (!dobText.isEmpty()) {
                try {
                    p.setDateOfBirth(LocalDate.parse(dobText, dateFmt));
                } catch (Exception exParse) {
                    try {
                        p.setDateOfBirth(LocalDate.parse(dobText));
                    } catch (Exception ex2) {
                        throw new IllegalArgumentException("Định dạng ngày sinh không hợp lệ. Vui lòng dùng dd/MM/yyyy hoặc yyyy-MM-dd.");
                    }
                }
            }

            if (patientBUS.insert(p)) {
                JOptionPane.showMessageDialog(this, "Thêm bệnh nhân thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            }
        } catch (BusinessException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Patient> list = patientBUS.findAll();
        for (Patient p : list) {
            String dob = p.getDateOfBirth() != null ? p.getDateOfBirth().format(dateFmt) : "";
            String gender = p.getGender() != null ? p.getGender().getDisplayName() : "";
            tableModel.addRow(new Object[]{
                    p.getId(), p.getFullName(), gender, dob, p.getPhone(), p.getAddress()
            });
        }
    }

    private void filterTable(String keyword) {
        tableModel.setRowCount(0);
        List<Patient> list = patientBUS.findAll();
        for (Patient p : list) {
            boolean match = keyword.isEmpty()
                    || (p.getFullName() != null && p.getFullName().toLowerCase().contains(keyword))
                    || (p.getPhone() != null && p.getPhone().contains(keyword));
            if (match) {
                String dob = p.getDateOfBirth() != null ? p.getDateOfBirth().format(dateFmt) : "";
                String gender = p.getGender() != null ? p.getGender().getDisplayName() : "";
                tableModel.addRow(new Object[]{
                        p.getId(), p.getFullName(), gender, dob, p.getPhone(), p.getAddress()
                });
            }
        }
    }

    /**
     * Refresh today's queue table from MedicalRecordBUS.
     * ASSUMPTION: use default doctorId = 1 for now.
     */
    private void refreshQueue() {
        if (queueTableModel == null) return;
        queueTableModel.setRowCount(0);
        long doctorId = 1L; // TODO: make selectable
        java.util.List<com.hospital.model.MedicalRecord> list = medicalRecordBUS.getTodayQueue(doctorId);
        int idx = 0;
        for (com.hospital.model.MedicalRecord r : list) {
            idx++; // display index as sequential STT (1,2,3...)
            String name = "-";
            try {
                com.hospital.model.Patient p = patientBUS.findById((int) r.getPatientId());
                if (p != null) name = p.getFullName();
            } catch (Exception ignored) {}

            String arrival = r.getArrivalTime() != null ? r.getArrivalTime().toString() : "";
            String priority = r.getPriority() != null ? r.getPriority() : "NORMAL";
            String status = r.getStatus() != null ? r.getStatus() : "";

        queueTableModel.addRow(new Object[]{
            idx,
            name,
            status,
            arrival,
            priority,
            ""
        });
        }
    }
}
