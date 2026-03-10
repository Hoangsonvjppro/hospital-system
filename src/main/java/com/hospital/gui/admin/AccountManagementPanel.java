package com.hospital.gui.admin;

import com.hospital.bus.AccountBUS;
import com.hospital.bus.DoctorBUS;
import com.hospital.bus.SpecialtyBUS;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Quản lý tài khoản — CRUD accounts, gán vai trò, quản lý bác sĩ.
 */
public class AccountManagementPanel extends JPanel {

    private final AccountBUS accountBUS = new AccountBUS();
    private final DoctorBUS doctorBUS = new DoctorBUS();
    private final SpecialtyBUS specialtyBUS = new SpecialtyBUS();

    private DefaultTableModel accModel;
    private JTable accTable;
    private JTextField txtUsername, txtFullName, txtEmail, txtPhone, txtPassword;
    private JComboBox<String> cboRole;
    private JCheckBox chkActive;
    private Account selectedAccount;

    public AccountManagementPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        loadAccounts();
    }

    private JPanel createHeader() {
        JLabel title = new JLabel("👤 Quản lý tài khoản");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JPanel createBody() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createTablePanel(), createFormPanel());
        split.setDividerLocation(580);
        split.setResizeWeight(0.6);
        split.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(split);
        return wrapper;
    }

    private JPanel createTablePanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        accModel = new DefaultTableModel(
                new String[]{"ID", "Username", "Họ tên", "Email", "SĐT", "Vai trò", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        accTable = new JTable(accModel);
        accTable.setRowHeight(32);
        accTable.setFont(UIConstants.FONT_LABEL);
        accTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        accTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        accTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onAccountSelected();
        });

        JScrollPane scroll = new JScrollPane(accTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createFormPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblForm = new JLabel("Thông tin tài khoản");
        lblForm.setFont(UIConstants.FONT_SUBTITLE);
        lblForm.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(lblForm, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        txtUsername = addField(form, gbc, row++, "Username:", 15);
        txtPassword = addField(form, gbc, row++, "Mật khẩu:", 15);
        txtFullName = addField(form, gbc, row++, "Họ tên:", 20);
        txtEmail = addField(form, gbc, row++, "Email:", 20);
        txtPhone = addField(form, gbc, row++, "SĐT:", 15);

        // Role combo
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        form.add(new JLabel("Vai trò:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cboRole = new JComboBox<>(new String[]{"ADMIN", "DOCTOR", "RECEPTIONIST", "PHARMACIST", "CASHIER"});
        cboRole.setFont(UIConstants.FONT_BODY);
        form.add(cboRole, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Hoạt động:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        chkActive = new JCheckBox("", true);
        form.add(chkActive, gbc);

        // Buttons
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        btnPanel.setOpaque(false);

        RoundedButton btnNew = new RoundedButton("+ Thêm mới");
        btnNew.setBackground(UIConstants.SUCCESS_GREEN);
        btnNew.setForeground(Color.WHITE);
        btnNew.addActionListener(e -> doInsert());

        RoundedButton btnUpdate = new RoundedButton("💾 Cập nhật");
        btnUpdate.setBackground(UIConstants.PRIMARY);
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.addActionListener(e -> doUpdate());

        RoundedButton btnDelete = new RoundedButton("🗑 Xóa");
        btnDelete.setBackground(UIConstants.DANGER_RED);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> doDelete());

        RoundedButton btnClear = new RoundedButton("Xóa form");
        btnClear.setBackground(UIConstants.FIELD_BG);
        btnClear.setForeground(UIConstants.TEXT_PRIMARY);
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnNew);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        form.add(btnPanel, gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JTextField addField(JPanel form, GridBagConstraints gbc, int row, String label, int cols) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_LABEL);
        form.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        JTextField txt = new JTextField(cols);
        txt.setFont(UIConstants.FONT_BODY);
        form.add(txt, gbc);
        return txt;
    }

    // ── Data ──

    private void loadAccounts() {
        accModel.setRowCount(0);
        try {
            List<Account> list = accountBUS.findAll();
            for (Account a : list) {
                Role role = a.getRole();
                accModel.addRow(new Object[]{
                        a.getId(), a.getUsername(), a.getFullName(), a.getEmail(),
                        a.getPhone(), role != null ? role.getDisplayName() : "ID:" + a.getRoleId(),
                        a.isActive() ? "Hoạt động" : "Ngưng"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải tài khoản: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAccountSelected() {
        int row = accTable.getSelectedRow();
        if (row < 0) return;

        int id = (int) accModel.getValueAt(row, 0);
        try {
            selectedAccount = accountBUS.findById(id);
            if (selectedAccount == null) return;
            txtUsername.setText(selectedAccount.getUsername());
            txtPassword.setText("");
            txtFullName.setText(selectedAccount.getFullName());
            txtEmail.setText(selectedAccount.getEmail());
            txtPhone.setText(selectedAccount.getPhone());
            chkActive.setSelected(selectedAccount.isActive());

            Role role = selectedAccount.getRole();
            if (role != null) {
                cboRole.setSelectedItem(role.name());
            }
        } catch (Exception ignored) {}
    }

    private void doInsert() {
        try {
            Account a = buildFromForm();
            if (a.getPassword() == null || a.getPassword().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mật khẩu không được để trống khi tạo mới.");
                return;
            }
            accountBUS.insert(a);
            JOptionPane.showMessageDialog(this, "Thêm tài khoản thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadAccounts();
            clearForm();
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doUpdate() {
        if (selectedAccount == null) { JOptionPane.showMessageDialog(this, "Chọn tài khoản."); return; }
        try {
            Account a = buildFromForm();
            a.setId(selectedAccount.getId());
            // Only set password if changed
            if (a.getPassword() == null || a.getPassword().isEmpty()) {
                a.setPassword(selectedAccount.getPassword());
            }
            accountBUS.update(a);
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadAccounts();
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doDelete() {
        if (selectedAccount == null) { JOptionPane.showMessageDialog(this, "Chọn tài khoản."); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa tài khoản: " + selectedAccount.getUsername() + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            accountBUS.delete(selectedAccount.getId());
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            loadAccounts();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Account buildFromForm() {
        Account a = new Account();
        a.setUsername(txtUsername.getText().trim());
        a.setPassword(txtPassword.getText().trim());
        a.setFullName(txtFullName.getText().trim());
        a.setEmail(txtEmail.getText().trim());
        a.setPhone(txtPhone.getText().trim());
        a.setActive(chkActive.isSelected());

        String roleName = (String) cboRole.getSelectedItem();
        try {
            Role role = Role.fromName(roleName);
            a.setRoleId(role.getId());
        } catch (Exception e) {
            a.setRoleId(1); // Default admin
        }
        return a;
    }

    private void clearForm() {
        selectedAccount = null;
        txtUsername.setText(""); txtPassword.setText(""); txtFullName.setText("");
        txtEmail.setText(""); txtPhone.setText("");
        cboRole.setSelectedIndex(0);
        chkActive.setSelected(true);
        accTable.clearSelection();
    }
}
