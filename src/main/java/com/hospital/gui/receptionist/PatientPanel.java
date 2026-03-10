package com.hospital.gui.receptionist;

import com.hospital.bus.PatientBUS;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.Patient;
import com.hospital.model.Patient.Gender;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Quản lý hồ sơ bệnh nhân — tìm kiếm, thêm/sửa/xóa.
 */
public class PatientPanel extends JPanel {

    private final PatientBUS patientBUS = new PatientBUS();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtSearch, txtName, txtPhone, txtCccd, txtAddress;
    private JTextField txtDob; // dd/MM/yyyy
    private JComboBox<Gender> cboGender;
    private JCheckBox chkActive;
    private int selectedId = -1;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PatientPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createTablePanel(), createFormPanel());
        split.setDividerLocation(340);
        split.setResizeWeight(0.6);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        loadData("");
    }

    /* ───── Header + Search ───── */
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("👤 Hồ sơ bệnh nhân");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        txtSearch = new JTextField(20);
        txtSearch.setFont(UIConstants.FONT_BODY);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên / SĐT / CCCD...");
        txtSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));

        RoundedButton btnSearch = new RoundedButton("🔍 Tìm");
        btnSearch.setBackground(UIConstants.PRIMARY);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setOpaque(false);
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);

        header.add(title, BorderLayout.WEST);
        header.add(searchBar, BorderLayout.EAST);
        return header;
    }

    /* ───── Table ───── */
    private JPanel createTablePanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 4));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Họ tên", "Giới tính", "Ngày sinh", "SĐT", "CCCD", "Địa chỉ", "Active"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(UIConstants.FONT_BODY);
        table.getTableHeader().setFont(UIConstants.FONT_LABEL);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);

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
        txtName = addTextField(fields, gbc, row++, "Họ tên:", 25);
        txtPhone = addTextField(fields, gbc, row++, "SĐT:", 15);
        txtCccd = addTextField(fields, gbc, row++, "CCCD:", 15);
        txtDob = addTextField(fields, gbc, row++, "Ngày sinh (dd/MM/yyyy):", 12);
        txtAddress = addTextField(fields, gbc, row++, "Địa chỉ:", 30);

        /* gender combo */
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        fields.add(createLabel("Giới tính:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cboGender = new JComboBox<>(Gender.values());
        cboGender.setFont(UIConstants.FONT_BODY);
        fields.add(cboGender, gbc);
        row++;

        /* active checkbox */
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        fields.add(createLabel("Hoạt động:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        chkActive = new JCheckBox("", true);
        chkActive.setOpaque(false);
        fields.add(chkActive, gbc);

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

        RoundedButton btnDeactivate = new RoundedButton("🚫 Vô hiệu hóa");
        btnDeactivate.setBackground(UIConstants.DANGER_RED);
        btnDeactivate.setForeground(Color.WHITE);
        btnDeactivate.addActionListener(e -> deactivate());

        bar.add(btnNew);
        bar.add(btnSave);
        bar.add(btnDeactivate);
        return bar;
    }

    /* ───── Data ───── */
    private void loadData(String keyword) {
        SwingUtilities.invokeLater(() -> {
            try {
                tableModel.setRowCount(0);
                List<Patient> list = patientBUS.findAll();
                String kw = keyword.toLowerCase();
                for (Patient p : list) {
                    if (!kw.isEmpty()) {
                        boolean match = (p.getFullName() != null && p.getFullName().toLowerCase().contains(kw))
                                || (p.getPhone() != null && p.getPhone().contains(kw))
                                || (p.getIdCard() != null && p.getIdCard().contains(kw));
                        if (!match) continue;
                    }
                    tableModel.addRow(new Object[]{
                            p.getId(),
                            p.getFullName(),
                            p.getGender() != null ? p.getGender().getDisplayName() : "",
                            p.getDateOfBirth() != null ? p.getDateOfBirth().format(FMT) : "",
                            p.getPhone(),
                            p.getIdCard(),
                            p.getAddress(),
                            p.isActive() ? "✓" : "✗"
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

        Patient p = patientBUS.findById(selectedId);
        if (p == null) return;

        txtName.setText(p.getFullName() != null ? p.getFullName() : "");
        txtPhone.setText(p.getPhone() != null ? p.getPhone() : "");
        txtCccd.setText(p.getIdCard() != null ? p.getIdCard() : "");
        txtDob.setText(p.getDateOfBirth() != null ? p.getDateOfBirth().format(FMT) : "");
        txtAddress.setText(p.getAddress() != null ? p.getAddress() : "");
        cboGender.setSelectedItem(p.getGender() != null ? p.getGender() : Gender.OTHER);
        chkActive.setSelected(p.isActive());
    }

    private void save() {
        try {
            Patient p = new Patient();
            p.setFullName(txtName.getText().trim());
            p.setPhone(txtPhone.getText().trim());
            p.setCccd(txtCccd.getText().trim());
            p.setAddress(txtAddress.getText().trim());
            p.setGender((Gender) cboGender.getSelectedItem());
            p.setActive(chkActive.isSelected());

            if (!txtDob.getText().trim().isEmpty()) {
                try {
                    p.setDateOfBirth(LocalDate.parse(txtDob.getText().trim(), FMT));
                } catch (DateTimeParseException e) {
                    JOptionPane.showMessageDialog(this, "Ngày sinh không hợp lệ (dd/MM/yyyy)", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            if (selectedId > 0) {
                p.setId(selectedId);
                patientBUS.update(p);
            } else {
                patientBUS.insert(p);
            }
            loadData(txtSearch.getText().trim());
            clearForm();
            JOptionPane.showMessageDialog(this, "Lưu thành công!", "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deactivate() {
        if (selectedId <= 0) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Vô hiệu hóa bệnh nhân này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Patient p = patientBUS.findById(selectedId);
                if (p != null) {
                    p.setActive(false);
                    patientBUS.update(p);
                    loadData(txtSearch.getText().trim());
                    clearForm();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtName.setText("");
        txtPhone.setText("");
        txtCccd.setText("");
        txtDob.setText("");
        txtAddress.setText("");
        cboGender.setSelectedIndex(0);
        chkActive.setSelected(true);
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
