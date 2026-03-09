package com.hospital.gui.dialogs;

import com.hospital.bus.ClinicConfigBUS;
import com.hospital.bus.PatientBUS;
import com.hospital.bus.QueueBUS;
import com.hospital.bus.event.EventBus;
import com.hospital.bus.event.PatientRegisteredEvent;
import com.hospital.dao.PatientDAO;
import com.hospital.exception.BusinessException;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.model.Patient;
import com.hospital.model.Patient.PatientType;
import com.hospital.model.QueueEntry;
import com.hospital.model.QueueEntry.Priority;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Dialog đăng ký / tiếp nhận bệnh nhân.
 * <p>
 * - Không có BHYT
 * - Hỗ trợ thu phí khám trước hoặc ghi nợ
 * - Form: Họ tên*, SĐT*, CCCD, Ngày sinh, Giới tính, Địa chỉ, Tiền sử dị ứng
 * - Nút Tìm kiếm bên cạnh SĐT → auto-fill nếu BN cũ
 * - Radio buttons: Khám lần đầu / Tái khám / Cấp cứu
 */
public class PatientRegistrationDialog extends JDialog {

    private static final DecimalFormat MONEY_FMT = new DecimalFormat("#,### đ");

    private final PatientBUS patientBUS = new PatientBUS();
    private final QueueBUS queueBUS = new QueueBUS();
    private final ClinicConfigBUS configBUS = new ClinicConfigBUS();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Form fields
    private JTextField txtFullName;
    private JTextField txtPhone;
    private JTextField txtCccd;
    private JTextField txtDob;
    private JComboBox<String> cmbGender;
    private JTextField txtAddress;
    private JTextArea txtAllergy;

    // Patient type radio buttons
    private JRadioButton rbFirstVisit;
    private JRadioButton rbRevisit;
    private JRadioButton rbEmergency;

    // Exam fee collection
    private JCheckBox chkCollectFee;
    private JLabel lblExamFee;
    private JComboBox<String> cmbPaymentMethod;
    private double examFeeAmount;
    private boolean examFeePrepaid = false;

    // Existing patient (dùng khi tìm thấy BN cũ)
    private Patient existingPatient;
    private boolean registered = false;

    public PatientRegistrationDialog(Frame owner) {
        super(owner, "Tiếp nhận bệnh nhân", true);
        initComponents();
        pack();
        setMinimumSize(new Dimension(520, 580));
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBorder(new EmptyBorder(20, 24, 20, 24));
        content.setBackground(UIConstants.CARD_BG);

        // ── Title ─────────────────────────────────────────────
        JLabel lblTitle = new JLabel("Đăng ký tiếp nhận bệnh nhân");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(UIConstants.PRIMARY);
        content.add(lblTitle, BorderLayout.NORTH);

        // ── Form ──────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);
        int row = 0;

        // Họ tên *
        addLabel(form, gbc, row, "Họ tên (*):");
        txtFullName = new JTextField(20);
        txtFullName.setFont(UIConstants.FONT_BODY);
        addField(form, gbc, row++, txtFullName);

        // SĐT * + nút Tìm kiếm
        addLabel(form, gbc, row, "SĐT (*):");
        JPanel phonePanel = new JPanel(new BorderLayout(6, 0));
        phonePanel.setOpaque(false);
        txtPhone = new JTextField(15);
        txtPhone.setFont(UIConstants.FONT_BODY);
        RoundedButton btnSearch = new RoundedButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(90, 30));
        btnSearch.addActionListener(e -> searchByPhone());
        phonePanel.add(txtPhone, BorderLayout.CENTER);
        phonePanel.add(btnSearch, BorderLayout.EAST);
        addField(form, gbc, row++, phonePanel);

        // CCCD
        addLabel(form, gbc, row, "CCCD:");
        txtCccd = new JTextField(15);
        txtCccd.setFont(UIConstants.FONT_BODY);
        addField(form, gbc, row++, txtCccd);

        // Ngày sinh
        addLabel(form, gbc, row, "Ngày sinh:");
        txtDob = new JTextField(15);
        txtDob.setFont(UIConstants.FONT_BODY);
        txtDob.setToolTipText("dd/MM/yyyy");
        addField(form, gbc, row++, txtDob);

        // Giới tính
        addLabel(form, gbc, row, "Giới tính:");
        cmbGender = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        cmbGender.setFont(UIConstants.FONT_BODY);
        addField(form, gbc, row++, cmbGender);

        // Địa chỉ
        addLabel(form, gbc, row, "Địa chỉ:");
        txtAddress = new JTextField(25);
        txtAddress.setFont(UIConstants.FONT_BODY);
        addField(form, gbc, row++, txtAddress);

        // Tiền sử dị ứng
        addLabel(form, gbc, row, "Tiền sử dị ứng:");
        txtAllergy = new JTextArea(3, 25);
        txtAllergy.setFont(UIConstants.FONT_BODY);
        txtAllergy.setLineWrap(true);
        txtAllergy.setWrapStyleWord(true);
        JScrollPane allergyScroll = new JScrollPane(txtAllergy);
        allergyScroll.setPreferredSize(new Dimension(0, 60));
        addField(form, gbc, row++, allergyScroll);

        // Phân loại bệnh nhân
        addLabel(form, gbc, row, "Phân loại:");
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        typePanel.setOpaque(false);
        ButtonGroup bgType = new ButtonGroup();
        rbFirstVisit = new JRadioButton("Khám lần đầu", true);
        rbRevisit = new JRadioButton("Tái khám");
        rbEmergency = new JRadioButton("Cấp cứu");
        rbFirstVisit.setFont(UIConstants.FONT_BODY);
        rbRevisit.setFont(UIConstants.FONT_BODY);
        rbEmergency.setFont(UIConstants.FONT_BODY);
        rbEmergency.setForeground(new Color(192, 57, 43));
        bgType.add(rbFirstVisit);
        bgType.add(rbRevisit);
        bgType.add(rbEmergency);
        typePanel.add(rbFirstVisit);
        typePanel.add(rbRevisit);
        typePanel.add(rbEmergency);
        addField(form, gbc, row++, typePanel);

        // ── Phí khám ──
        addLabel(form, gbc, row, "Phí khám:");
        JPanel feePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        feePanel.setOpaque(false);

        // Load exam fee from config
        try {
            examFeeAmount = configBUS.getDefaultExamFee();
        } catch (Exception e) {
            examFeeAmount = 150_000;
        }

        chkCollectFee = new JCheckBox("Thu trước");
        chkCollectFee.setFont(UIConstants.FONT_BODY);
        chkCollectFee.setOpaque(false);

        lblExamFee = new JLabel(MONEY_FMT.format(examFeeAmount));
        lblExamFee.setFont(UIConstants.FONT_BOLD);
        lblExamFee.setForeground(UIConstants.PRIMARY);

        cmbPaymentMethod = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản"});
        cmbPaymentMethod.setFont(UIConstants.FONT_BODY);
        cmbPaymentMethod.setEnabled(false);

        JLabel lblDefer = new JLabel("(Ghi nợ nếu không thu trước)");
        lblDefer.setFont(UIConstants.FONT_CAPTION);
        lblDefer.setForeground(UIConstants.TEXT_MUTED);

        chkCollectFee.addActionListener(e -> {
            cmbPaymentMethod.setEnabled(chkCollectFee.isSelected());
            lblDefer.setVisible(!chkCollectFee.isSelected());
        });

        feePanel.add(lblExamFee);
        feePanel.add(chkCollectFee);
        feePanel.add(cmbPaymentMethod);
        feePanel.add(lblDefer);
        addField(form, gbc, row++, feePanel);

        content.add(form, BorderLayout.CENTER);

        // ── Buttons ───────────────────────────────────────────
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        RoundedButton btnRegister = new RoundedButton("Đăng ký & Chuyển hàng đợi");
        btnRegister.addActionListener(e -> doRegister());

        RoundedButton btnCancel = new RoundedButton("Hủy");
        btnCancel.setColors(UIConstants.TEXT_SECONDARY, UIConstants.STATUS_CANCEL);
        btnCancel.addActionListener(e -> dispose());

        RoundedButton btnClear = new RoundedButton("Làm mới");
        btnClear.setColors(UIConstants.STATUS_WAITING, UIConstants.WARNING_ORANGE);
        btnClear.addActionListener(e -> clearForm());

        buttons.add(btnClear);
        buttons.add(btnCancel);
        buttons.add(btnRegister);
        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
    }

    // ── Tìm kiếm bệnh nhân cũ theo SĐT ──────────────────────
    private void searchByPhone() {
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập SĐT để tìm kiếm.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            PatientDAO dao = new PatientDAO();
            Patient found = dao.findByPhone(phone);
            if (found != null) {
                existingPatient = found;
                fillFormFromPatient(found);
                rbRevisit.setSelected(true);
                JOptionPane.showMessageDialog(this,
                        "Đã tìm thấy bệnh nhân: " + found.getFullName() + "\nThông tin đã được tự động điền.",
                        "Bệnh nhân cũ", JOptionPane.INFORMATION_MESSAGE);
            } else {
                existingPatient = null;
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy bệnh nhân với SĐT: " + phone,
                        "Không tìm thấy", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tìm kiếm: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Đăng ký bệnh nhân & chuyển vào hàng đợi ──────────────
    private void doRegister() {
        try {
            // Validate
            String fullName = txtFullName.getText().trim();
            String phone = txtPhone.getText().trim();
            String cccd = txtCccd.getText().trim();

            if (fullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Họ tên không được để trống.",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                txtFullName.requestFocus();
                return;
            }

            if (phone.isEmpty() || !phone.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(this, "SĐT phải là 10 chữ số.",
                        "SĐT không hợp lệ", JOptionPane.WARNING_MESSAGE);
                txtPhone.requestFocus();
                return;
            }

            if (!cccd.isEmpty() && !cccd.matches("\\d{12}")) {
                JOptionPane.showMessageDialog(this, "CCCD phải là 12 chữ số.",
                        "CCCD không hợp lệ", JOptionPane.WARNING_MESSAGE);
                txtCccd.requestFocus();
                return;
            }

            Patient patient;
            if (existingPatient != null && existingPatient.getPhone().equals(phone)) {
                // Cập nhật bệnh nhân cũ
                patient = existingPatient;
                patient.setFullName(fullName);
                patient.setAddress(txtAddress.getText().trim());
                patient.setAllergyHistory(txtAllergy.getText().trim());
                if (!cccd.isEmpty()) patient.setCccd(cccd);
                patient.setGender(mapGender());
                patient.setPatientType(getSelectedPatientType());
                parseDateOfBirth(patient);
                patientBUS.update(patient);
            } else {
                // Tạo bệnh nhân mới
                patient = new Patient();
                patient.setFullName(fullName);
                patient.setPhone(phone);
                patient.setGender(mapGender());
                patient.setAddress(txtAddress.getText().trim());
                patient.setAllergyHistory(txtAllergy.getText().trim());
                if (!cccd.isEmpty()) patient.setCccd(cccd);
                patient.setPatientType(getSelectedPatientType());
                parseDateOfBirth(patient);
                patientBUS.insert(patient);
            }

            // Xác định priority cho hàng đợi
            Priority priority = Priority.NORMAL;
            if (rbEmergency.isSelected()) {
                priority = Priority.EMERGENCY;
            } else if (patient.getAge() >= 60) {
                priority = Priority.ELDERLY;
            }

            // Thêm vào hàng đợi
            QueueEntry entry = queueBUS.addToQueue(patient.getId(), priority);

            // Ghi nhận thu phí khám trước
            examFeePrepaid = chkCollectFee.isSelected();
            String feeInfo;
            if (examFeePrepaid) {
                String method = cmbPaymentMethod.getSelectedItem().toString();
                feeInfo = "Đã thu phí khám: " + MONEY_FMT.format(examFeeAmount) + " (" + method + ")";
            } else {
                feeInfo = "Phí khám: Ghi nợ (thu khi thanh toán)";
            }

            // Fire event
            EventBus.getInstance().publish(new PatientRegisteredEvent(patient.getId(), patient));

            registered = true;
            JOptionPane.showMessageDialog(this,
                    "Đăng ký thành công!\n" +
                    "Bệnh nhân: " + patient.getFullName() + "\n" +
                    "Số thứ tự: " + entry.getQueueNumber() + "\n" +
                    "Ưu tiên: " + entry.getPriorityDisplay() + "\n" +
                    feeInfo,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(),
                    "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private void fillFormFromPatient(Patient p) {
        txtFullName.setText(p.getFullName() != null ? p.getFullName() : "");
        txtPhone.setText(p.getPhone() != null ? p.getPhone() : "");
        txtCccd.setText(p.getCccd() != null ? p.getCccd() : "");
        txtAddress.setText(p.getAddress() != null ? p.getAddress() : "");
        txtAllergy.setText(p.getAllergyHistory() != null ? p.getAllergyHistory() : "");
        if (p.getDateOfBirth() != null) {
            txtDob.setText(p.getDateOfBirth().format(dateFmt));
        }
        if (p.getGender() != null) {
            switch (p.getGender()) {
                case MALE -> cmbGender.setSelectedIndex(0);
                case FEMALE -> cmbGender.setSelectedIndex(1);
                default -> cmbGender.setSelectedIndex(2);
            }
        }
    }

    private Patient.Gender mapGender() {
        int idx = cmbGender.getSelectedIndex();
        return switch (idx) {
            case 1 -> Patient.Gender.FEMALE;
            case 2 -> Patient.Gender.OTHER;
            default -> Patient.Gender.MALE;
        };
    }

    private PatientType getSelectedPatientType() {
        if (rbEmergency.isSelected()) return PatientType.EMERGENCY;
        if (rbRevisit.isSelected()) return PatientType.REVISIT;
        return PatientType.FIRST_VISIT;
    }

    private void parseDateOfBirth(Patient p) {
        String dobText = txtDob.getText().trim();
        if (!dobText.isEmpty()) {
            try {
                p.setDateOfBirth(LocalDate.parse(dobText, dateFmt));
            } catch (DateTimeParseException ex1) {
                try {
                    p.setDateOfBirth(LocalDate.parse(dobText));
                } catch (DateTimeParseException ex2) {
                    throw new BusinessException("Ngày sinh không hợp lệ. Vui lòng dùng dd/MM/yyyy.");
                }
            }
        }
    }

    private void clearForm() {
        txtFullName.setText("");
        txtPhone.setText("");
        txtCccd.setText("");
        txtDob.setText("");
        txtAddress.setText("");
        txtAllergy.setText("");
        cmbGender.setSelectedIndex(0);
        rbFirstVisit.setSelected(true);
        existingPatient = null;
        txtFullName.requestFocus();
    }

    public boolean isRegistered() {
        return registered;
    }

    public boolean isExamFeePrepaid() {
        return examFeePrepaid;
    }

    public double getExamFeeAmount() {
        return examFeeAmount;
    }

    public String getPaymentMethod() {
        return chkCollectFee.isSelected() ? cmbPaymentMethod.getSelectedItem().toString() : null;
    }

    // ── Layout helpers ────────────────────────────────────────

    private void addLabel(JPanel panel, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        panel.add(lbl, gbc);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, JComponent field) {
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
    }
}
