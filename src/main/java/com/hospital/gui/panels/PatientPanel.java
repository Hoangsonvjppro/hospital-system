package com.hospital.gui.panels;

import com.hospital.bus.PatientBUS;
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
    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private final PatientBUS patientBUS = new PatientBUS();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PatientPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(24, 28, 24, 28));
        initComponents();
        loadData();
    }

    private void initComponents() {
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
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

        RoundedButton btnAdd = new RoundedButton("+ Thêm bệnh nhân");
        btnAdd.setPreferredSize(new Dimension(170, 38));
        btnAdd.addActionListener(e -> showAddDialog());
        titleRow.add(btnAdd, BorderLayout.EAST);

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
        txtPhone   = new JTextField();
        txtAddress = new JTextField();
        txtDob     = new JTextField();

        String[][] fields = {
            {"Họ tên:", "Nguyễn Văn A"},
            {"SĐT:", "0901234567"},
            {"Địa chỉ:", "123 Đường ABC, Q1, TP.HCM"},
            {"Ngày sinh:", "yyyy-MM-dd"}
        };
        JTextField[] inputs = {txtName, txtPhone, txtAddress, txtDob};

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
            p.setPhone(txtPhone.getText().trim());
            p.setAddress(txtAddress.getText().trim());

            String dobText = txtDob.getText().trim();
            if (!dobText.isEmpty()) {
                p.setDateOfBirth(LocalDate.parse(dobText));
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
}
