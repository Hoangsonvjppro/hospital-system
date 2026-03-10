package com.hospital.gui.receptionist;

import com.hospital.bus.*;
import com.hospital.bus.event.*;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.*;
import com.hospital.model.QueueEntry.Priority;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * ① Tiếp nhận bệnh nhân — đăng ký / tái khám + xếp hàng đợi.
 */
public class ReceptionPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(ReceptionPanel.class.getName());

    private final PatientBUS patientBUS = new PatientBUS();
    private final QueueBUS queueBUS = new QueueBUS();
    private final PatientAllergyBUS allergyBUS = new PatientAllergyBUS();

    // Form fields
    private JTextField txtFullName, txtPhone, txtCccd, txtAddress, txtAllergy;
    private JComboBox<String> cboGender, cboPriority;
    private JTextField txtDob;
    private JLabel lblPatientId;
    private Patient currentPatient;

    // Queue table
    private DefaultTableModel queueTableModel;
    private JLabel lblQueueStats;

    public ReceptionPanel() {
        setLayout(new BorderLayout(16, 0));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title
        JLabel title = new JLabel("📋 Tiếp nhận bệnh nhân");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        // Split: Left form, Right queue
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createFormPanel(), createQueuePanel());
        split.setDividerLocation(500);
        split.setResizeWeight(0.5);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        loadTodayQueue();

        // Event listener
        EventBus.getInstance().subscribe(QueueUpdatedEvent.class, e ->
                SwingUtilities.invokeLater(this::loadTodayQueue));
    }

    private JPanel createFormPanel() {
        JPanel panel = new RoundedPanel(UIConstants.CARD_RADIUS);
        panel.setBackground(UIConstants.CARD_BG);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Search bar
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);
        JTextField txtSearch = new JTextField();
        txtSearch.setFont(UIConstants.FONT_BODY);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo SĐT / CCCD / Tên...");
        JButton btnSearch = new RoundedButton("Tìm");
        btnSearch.setPreferredSize(new Dimension(80, 36));
        searchBar.add(txtSearch, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);

        Runnable searchAction = () -> {
            String kw = txtSearch.getText().trim();
            if (kw.isEmpty()) return;
            try {
                List<Patient> results = patientBUS.findAll().stream()
                        .filter(p -> matches(p, kw)).toList();
                if (results.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy. Vui lòng đăng ký mới.");
                    clearForm();
                } else if (results.size() == 1) {
                    fillForm(results.get(0));
                } else {
                    String[] opts = results.stream()
                            .map(p -> p.getFullName() + " - " + p.getPhone()).toArray(String[]::new);
                    String sel = (String) JOptionPane.showInputDialog(this, "Chọn bệnh nhân:",
                            "Kết quả", JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
                    if (sel != null) {
                        int idx = java.util.Arrays.asList(opts).indexOf(sel);
                        if (idx >= 0) fillForm(results.get(idx));
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        };
        btnSearch.addActionListener(e -> searchAction.run());
        txtSearch.addActionListener(e -> searchAction.run());

        // Patient form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        lblPatientId = new JLabel("Mới");
        lblPatientId.setFont(UIConstants.FONT_BOLD);
        lblPatientId.setForeground(UIConstants.PRIMARY);
        addFormRow(form, gbc, 0, "Mã BN:", lblPatientId);

        txtFullName = new JTextField(20);
        addFormRow(form, gbc, 1, "Họ tên (*):", txtFullName);

        txtPhone = new JTextField(20);
        addFormRow(form, gbc, 2, "SĐT (*):", txtPhone);

        txtCccd = new JTextField(20);
        addFormRow(form, gbc, 3, "CCCD:", txtCccd);

        txtDob = new JTextField(20);
        txtDob.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");
        addFormRow(form, gbc, 4, "Ngày sinh:", txtDob);

        cboGender = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        addFormRow(form, gbc, 5, "Giới tính:", cboGender);

        txtAddress = new JTextField(20);
        addFormRow(form, gbc, 6, "Địa chỉ:", txtAddress);

        txtAllergy = new JTextField(20);
        txtAllergy.putClientProperty("JTextField.placeholderText", "VD: Penicillin");
        addFormRow(form, gbc, 7, "Dị ứng:", txtAllergy);

        cboPriority = new JComboBox<>(new String[]{"Bình thường", "Người cao tuổi", "Cấp cứu"});
        addFormRow(form, gbc, 8, "Ưu tiên:", cboPriority);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnPanel.setOpaque(false);

        RoundedButton btnRegister = new RoundedButton("Đăng ký & Xếp hàng");
        btnRegister.setPreferredSize(new Dimension(200, 40));
        btnRegister.addActionListener(e -> registerAndQueue());

        RoundedButton btnClear = new RoundedButton("Làm mới");
        btnClear.setColors(UIConstants.TEXT_SECONDARY, UIConstants.TEXT_PRIMARY);
        btnClear.setPreferredSize(new Dimension(100, 40));
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnRegister);
        btnPanel.add(btnClear);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.add(searchBar, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createQueuePanel() {
        JPanel panel = new RoundedPanel(UIConstants.CARD_RADIUS);
        panel.setBackground(UIConstants.CARD_BG);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("🕐 Hàng đợi hôm nay");
        lblTitle.setFont(UIConstants.FONT_SUBTITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);

        lblQueueStats = new JLabel("Đang tải...");
        lblQueueStats.setFont(UIConstants.FONT_CAPTION);
        lblQueueStats.setForeground(UIConstants.TEXT_SECONDARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblQueueStats, BorderLayout.EAST);

        queueTableModel = new DefaultTableModel(
                new String[]{"STT", "Tên bệnh nhân", "Ưu tiên", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(queueTableModel);
        table.setRowHeight(32);
        table.setFont(UIConstants.FONT_BODY);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);

        JButton btnRefresh = new RoundedButton("Làm mới");
        btnRefresh.setPreferredSize(new Dimension(100, 36));
        btnRefresh.addActionListener(e -> loadTodayQueue());

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnRefresh, BorderLayout.SOUTH);

        return panel;
    }

    private void registerAndQueue() {
        try {
            // Save or update patient
            if (currentPatient == null) {
                currentPatient = new Patient();
            }
            fillPatientFromForm(currentPatient);

            if (currentPatient.getId() == 0) {
                patientBUS.insert(currentPatient);
                // Re-fetch to get the generated ID
                Patient fetched = patientBUS.findAll().stream()
                        .filter(p -> p.getPhone() != null && p.getPhone().equals(currentPatient.getPhone()))
                        .findFirst().orElse(currentPatient);
                currentPatient = fetched;
            } else {
                patientBUS.update(currentPatient);
            }

            // Add allergy if specified
            String allergyText = txtAllergy.getText().trim();
            if (!allergyText.isEmpty()) {
                PatientAllergy allergy = new PatientAllergy();
                allergy.setPatientId(currentPatient.getId());
                allergy.setAllergenName(allergyText);
                allergy.setSeverity("MODERATE");
                try { allergyBUS.insert(allergy); } catch (Exception ignored) {}
            }

            // Determine priority
            Priority priority = switch (cboPriority.getSelectedIndex()) {
                case 1 -> Priority.ELDERLY;
                case 2 -> Priority.EMERGENCY;
                default -> Priority.NORMAL;
            };

            // Add to queue
            queueBUS.addToQueue(currentPatient.getId(), priority);

            JOptionPane.showMessageDialog(this,
                    "Tiếp nhận thành công! BN: " + currentPatient.getFullName(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            EventBus.getInstance().publish(new PatientRegisteredEvent(currentPatient.getId(), currentPatient));

            clearForm();
            loadTodayQueue();

        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            LOGGER.severe("Lỗi tiếp nhận: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTodayQueue() {
        try {
            List<QueueEntry> queue = queueBUS.getTodayQueue();
            queueTableModel.setRowCount(0);
            int waiting = 0, inProgress = 0, done = 0;
            for (QueueEntry entry : queue) {
                String statusText = switch (entry.getStatus()) {
                    case WAITING -> "Chờ khám";
                    case IN_PROGRESS -> "Đang khám";
                    case COMPLETED -> "Hoàn tất";
                    case CANCELLED -> "Đã hủy";
                };
                String priorityText = switch (entry.getPriority()) {
                    case EMERGENCY -> "🔴 CC";
                    case ELDERLY -> "🟡 NCT";
                    case NORMAL -> "🟢 BT";
                };
                queueTableModel.addRow(new Object[]{
                        entry.getQueueNumber(),
                        entry.getPatientName() != null ? entry.getPatientName() : "BN#" + entry.getPatientId(),
                        priorityText,
                        statusText
                });
                switch (entry.getStatus()) {
                    case WAITING -> waiting++;
                    case IN_PROGRESS -> inProgress++;
                    case COMPLETED -> done++;
                    default -> {}
                }
            }
            lblQueueStats.setText("Chờ: " + waiting + " | Khám: " + inProgress + " | Xong: " + done);
        } catch (Exception ex) {
            LOGGER.warning("Lỗi tải hàng đợi: " + ex.getMessage());
        }
    }

    private void fillForm(Patient p) {
        currentPatient = p;
        lblPatientId.setText("BN" + String.format("%05d", p.getId()));
        txtFullName.setText(p.getFullName());
        txtPhone.setText(p.getPhone());
        txtCccd.setText(p.getCccd() != null ? p.getCccd() : "");
        if (p.getDateOfBirth() != null) {
            txtDob.setText(p.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        if (p.getGender() != null) {
            cboGender.setSelectedItem(switch (p.getGender()) {
                case MALE -> "Nam";
                case FEMALE -> "Nữ";
                default -> "Khác";
            });
        }
        txtAddress.setText(p.getAddress() != null ? p.getAddress() : "");

        // Load allergies
        try {
            List<PatientAllergy> allergies = allergyBUS.findByPatientId(p.getId());
            if (!allergies.isEmpty()) {
                txtAllergy.setText(allergies.stream()
                        .map(PatientAllergy::getAllergenName)
                        .reduce((a, b) -> a + ", " + b).orElse(""));
            }
        } catch (Exception ignored) {}
    }

    private void fillPatientFromForm(Patient p) {
        p.setFullName(txtFullName.getText().trim());
        p.setPhone(txtPhone.getText().trim());
        String cccd = txtCccd.getText().trim();
        if (!cccd.isEmpty()) p.setCccd(cccd);
        p.setAddress(txtAddress.getText().trim());

        String genderStr = (String) cboGender.getSelectedItem();
        p.setGender("Nữ".equals(genderStr) ? Patient.Gender.FEMALE :
                     "Khác".equals(genderStr) ? Patient.Gender.OTHER : Patient.Gender.MALE);

        String dobStr = txtDob.getText().trim();
        if (!dobStr.isEmpty()) {
            try {
                p.setDateOfBirth(LocalDate.parse(dobStr, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } catch (Exception ignored) {}
        }
    }

    private void clearForm() {
        currentPatient = null;
        lblPatientId.setText("Mới");
        txtFullName.setText("");
        txtPhone.setText("");
        txtCccd.setText("");
        txtDob.setText("");
        txtAddress.setText("");
        txtAllergy.setText("");
        cboGender.setSelectedIndex(0);
        cboPriority.setSelectedIndex(0);
    }

    private boolean matches(Patient p, String kw) {
        String lower = kw.toLowerCase();
        return (p.getFullName() != null && p.getFullName().toLowerCase().contains(lower))
            || (p.getPhone() != null && p.getPhone().contains(kw))
            || (p.getCccd() != null && p.getCccd().contains(kw));
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_LABEL);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        if (field instanceof JTextField) {
            ((JTextField) field).setFont(UIConstants.FONT_BODY);
        }
        panel.add(field, gbc);
    }
}
