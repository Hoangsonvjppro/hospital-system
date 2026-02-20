package com.hospital.gui.panels;

import com.hospital.bus.PatientBUS;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.components.StatusBadge;
import com.hospital.model.Patient;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Trang Tiếp nhận bệnh nhân.
 */
public class ReceptionPanel extends JPanel {

    private final PatientBUS bus = new PatientBUS();
    private DefaultTableModel tableModel;
    private JTable table;

    // Form fields
    private JTextField txtCode, txtName, txtPhone, txtAddress, txtArrival, txtExamType;
    private JComboBox<String> cbGender, cbStatus;
    private JSpinner spnBirthYear;

    public ReceptionPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Tiếp nhận bệnh nhân");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY_RED);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Body: Form left + Table right
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        body.add(createForm(),  BorderLayout.WEST);
        body.add(createTable(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    // ── Form ──────────────────────────────────────────────────────────────────
    private JPanel createForm() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(300, 0));

        JLabel formTitle = new JLabel("Thông tin bệnh nhân");
        formTitle.setFont(UIConstants.FONT_SUBTITLE);
        formTitle.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(formTitle, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 0, 5, 0);
        g.gridx = 0; g.weightx = 1;

        txtCode      = new JTextField();
        txtName      = new JTextField();
        txtPhone     = new JTextField();
        txtAddress   = new JTextField();
        txtArrival   = new JTextField("08:00");
        txtExamType  = new JTextField();
        cbGender     = new JComboBox<>(new String[]{"Nam", "Nữ"});
        cbStatus     = new JComboBox<>(new String[]{"CHỜ KHÁM", "ĐANG KHÁM", "XONG"});
        spnBirthYear = new JSpinner(new SpinnerNumberModel(1990, 1900, 2026, 1));
        spnBirthYear.setEditor(new JSpinner.NumberEditor(spnBirthYear, "#"));

        Object[][] rows = {
            {"Mã BN (*)", txtCode},
            {"Họ và tên (*)", txtName},
            {"Năm sinh", spnBirthYear},
            {"Giới tính", cbGender},
            {"Số điện thoại (*)", txtPhone},
            {"Địa chỉ", txtAddress},
            {"Giờ tiếp nhận", txtArrival},
            {"Loại khám", txtExamType},
            {"Trạng thái", cbStatus},
        };

        int gridy = 0;
        for (Object[] row : rows) {
            g.gridy = gridy++;
            JLabel lbl = new JLabel((String) row[0]);
            lbl.setFont(UIConstants.FONT_LABEL);
            lbl.setForeground(UIConstants.TEXT_SECONDARY);
            fields.add(lbl, g);
            g.gridy = gridy++;
            JComponent comp = (JComponent) row[1];
            comp.setFont(UIConstants.FONT_LABEL);
            fields.add(comp, g);
        }
        card.add(fields, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel(new GridLayout(1, 2, 8, 0));
        btns.setOpaque(false);

        RoundedButton btnAdd = new RoundedButton("Thêm mới");
        btnAdd.addActionListener(e -> addPatient());

        RoundedButton btnClear = new RoundedButton("Làm mới");
        btnClear.setColors(UIConstants.TEXT_SECONDARY, UIConstants.STATUS_CANCEL);
        btnClear.addActionListener(e -> clearForm());

        btns.add(btnAdd);
        btns.add(btnClear);
        card.add(btns, BorderLayout.SOUTH);
        return card;
    }

    // ── Table ─────────────────────────────────────────────────────────────────
    private JPanel createTable() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Search bar
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setOpaque(false);
        JTextField txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm bệnh nhân...");
        txtSearch.addActionListener(e -> searchPatients(txtSearch.getText()));

        RoundedButton btnSearch = new RoundedButton("Tìm");
        btnSearch.setPreferredSize(new Dimension(70, 32));
        btnSearch.addActionListener(e -> searchPatients(txtSearch.getText()));

        RoundedButton btnDelete = new RoundedButton("Xóa");
        btnDelete.setColors(UIConstants.STATUS_CANCEL, UIConstants.STATUS_CANCEL.darker());
        btnDelete.setPreferredSize(new Dimension(70, 32));
        btnDelete.addActionListener(e -> deleteSelected());

        topBar.add(txtSearch, BorderLayout.CENTER);
        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        acts.setOpaque(false);
        acts.add(btnSearch);
        acts.add(btnDelete);
        topBar.add(acts, BorderLayout.EAST);
        card.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Mã BN", "Họ và tên", "Tuổi", "Giới tính", "Điện thoại", "Giờ nhận", "Loại khám", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(42);
        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(UIConstants.RED_BG_SOFT);
        table.setSelectionForeground(UIConstants.TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setBackground(UIConstants.TABLE_HEADER_BG);
        header.setFont(UIConstants.FONT_BOLD);

        // Status renderer
        table.getColumnModel().getColumn(7).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean s, boolean f, int r, int c) {
                    StatusBadge badge = new StatusBadge(v == null ? "" : v.toString());
                    badge.setHorizontalAlignment(SwingConstants.CENTER);
                    return badge;
                }
            });

        // Selection listener to populate form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                populateForm(table.getSelectedRow());
            }
        });

        loadTable(bus.findAll());
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private void loadTable(List<Patient> list) {
        tableModel.setRowCount(0);
        for (Patient p : list) {
            tableModel.addRow(new Object[]{
                p.getPatientCode(), p.getFullName(), p.getAge(),
                p.getGender(), p.getPhone(), p.getArrivalTime(),
                p.getExamType(), p.getStatus()
            });
        }
    }

    private void searchPatients(String kw) {
        List<Patient> all = bus.findAll();
        if (kw == null || kw.isBlank()) { loadTable(all); return; }
        String lc = kw.toLowerCase();
        loadTable(all.stream()
            .filter(p -> p.getFullName().toLowerCase().contains(lc)
                    || p.getPatientCode().toLowerCase().contains(lc)
                    || p.getPhone().contains(lc))
            .collect(java.util.stream.Collectors.toList()));
    }

    private void addPatient() {
        String code = txtCode.getText().trim();
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        if (code.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền các trường có dấu (*)", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int year = (int) spnBirthYear.getValue();
        Patient p = new Patient(0, code, name,
                LocalDate.of(year, 1, 1),
                (String) cbGender.getSelectedItem(), phone,
                txtAddress.getText().trim(),
                (String) cbStatus.getSelectedItem(),
                txtExamType.getText().trim(),
                txtArrival.getText().trim());
        bus.insert(p);
        loadTable(bus.findAll());
        clearForm();
        JOptionPane.showMessageDialog(this, "Đã thêm bệnh nhân thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn một bệnh nhân."); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa bệnh nhân đã chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String code = (String) tableModel.getValueAt(row, 0);
            bus.findAll().stream()
                .filter(p -> p.getPatientCode().equals(code))
                .findFirst()
                .ifPresent(p -> { bus.delete(p.getId()); loadTable(bus.findAll()); });
        }
    }

    private void populateForm(int row) {
        txtCode.setText((String) tableModel.getValueAt(row, 0));
        txtName.setText((String) tableModel.getValueAt(row, 1));
        txtPhone.setText((String) tableModel.getValueAt(row, 4));
        txtArrival.setText((String) tableModel.getValueAt(row, 5));
        txtExamType.setText((String) tableModel.getValueAt(row, 6));
        cbGender.setSelectedItem(tableModel.getValueAt(row, 3));
        cbStatus.setSelectedItem(tableModel.getValueAt(row, 7));
    }

    private void clearForm() {
        txtCode.setText(""); txtName.setText(""); txtPhone.setText("");
        txtAddress.setText(""); txtArrival.setText("08:00"); txtExamType.setText("");
        cbGender.setSelectedIndex(0); cbStatus.setSelectedIndex(0);
        spnBirthYear.setValue(1990);
        table.clearSelection();
    }
}
