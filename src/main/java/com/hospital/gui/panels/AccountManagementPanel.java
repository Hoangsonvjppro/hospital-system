package com.hospital.gui.panels;

import com.hospital.bus.AccountBUS;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.util.AsyncTask;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.components.StatusBadge;
import com.hospital.model.Account;
import com.hospital.model.Role;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Panel quản lý tài khoản — dành cho Admin.
 * Hiển thị danh sách user, hỗ trợ thêm/sửa/bật-tắt/reset mật khẩu.
 */
public class AccountManagementPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(AccountManagementPanel.class.getName());

    private final AccountBUS accountBUS = new AccountBUS();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private List<Account> currentList;

    public AccountManagementPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
        loadData();
    }

    // ════════════════════════════════════════════════════════════
    //  INIT UI
    // ════════════════════════════════════════════════════════════

    private void initComponents() {
        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        // Title
        JLabel lblTitle = new JLabel("  Quản lý tài khoản");
        lblTitle.setIcon(com.hospital.gui.IconManager.getIcon("person", 20, 20));
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(lblTitle, BorderLayout.WEST);

        // Actions: search + buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        txtSearch = new JTextField(18);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên, username, email...");
        txtSearch.setFont(UIConstants.FONT_BODY);
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { filterTable(); }
        });
        actions.add(txtSearch);

        RoundedButton btnAdd = new RoundedButton("+ Thêm tài khoản");
        btnAdd.setBackground(UIConstants.PRIMARY);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> showAddDialog());
        actions.add(btnAdd);

        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel createTablePanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setLayout(new BorderLayout());
        card.setBackground(UIConstants.CARD_BG);

        String[] cols = {"ID", "Username", "Họ tên", "Email", "SĐT", "Vai trò", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(UIConstants.FONT_BODY);
        table.setRowHeight(38);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Custom renderer for status column
        table.getColumnModel().getColumn(6).setCellRenderer((tbl, value, sel, foc, row, col) -> {
            return new StatusBadge((String) value);
        });

        // Context menu
        JPopupMenu popup = new JPopupMenu();
        JMenuItem itemEdit = new JMenuItem("Sửa thông tin");
        itemEdit.setIcon(com.hospital.gui.IconManager.getIcon("edit", 14, 14));
        JMenuItem itemToggle = new JMenuItem("Đổi trạng thái");
        itemToggle.setIcon(com.hospital.gui.IconManager.getIcon("refresh", 14, 14));
        JMenuItem itemReset = new JMenuItem("Reset mật khẩu");
        itemReset.setIcon(com.hospital.gui.IconManager.getIcon("key", 14, 14));
        JMenuItem itemDelete = new JMenuItem("Xóa tài khoản");
        itemDelete.setIcon(com.hospital.gui.IconManager.getIcon("delete", 14, 14));

        itemEdit.addActionListener(e -> editSelected());
        itemToggle.addActionListener(e -> toggleSelected());
        itemReset.addActionListener(e -> resetPasswordSelected());
        itemDelete.addActionListener(e -> deleteSelected());

        popup.add(itemEdit);
        popup.add(itemToggle);
        popup.add(itemReset);
        popup.addSeparator();
        popup.add(itemDelete);

        table.setComponentPopupMenu(popup);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) table.setRowSelectionInterval(row, row);
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottom.setOpaque(false);

        RoundedButton btnEdit = new RoundedButton("Sửa");
        btnEdit.setIcon(com.hospital.gui.IconManager.getIcon("edit", 14, 14));
        btnEdit.addActionListener(e -> editSelected());
        RoundedButton btnToggle = new RoundedButton("Bật/Tắt");
        btnToggle.setIcon(com.hospital.gui.IconManager.getIcon("refresh", 14, 14));
        btnToggle.addActionListener(e -> toggleSelected());
        RoundedButton btnReset = new RoundedButton("Reset MK");
        btnReset.setIcon(com.hospital.gui.IconManager.getIcon("key", 14, 14));
        btnReset.addActionListener(e -> resetPasswordSelected());

        bottom.add(btnEdit);
        bottom.add(btnToggle);
        bottom.add(btnReset);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    // ════════════════════════════════════════════════════════════
    //  DATA
    // ════════════════════════════════════════════════════════════

    private void loadData() {
        AsyncTask.run(
            () -> accountBUS.findAll(),
            list -> { currentList = list; populateTable(list); },
            ex -> JOptionPane.showMessageDialog(this,
                    "Lỗi tải danh sách tài khoản: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE)
        );
    }

    private void populateTable(List<Account> list) {
        tableModel.setRowCount(0);
        for (Account a : list) {
            String roleName;
            try {
                roleName = Role.fromId(a.getRoleId()).getDisplayName();
            } catch (IllegalArgumentException e) {
                roleName = "ID=" + a.getRoleId();
            }
            tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getUsername(),
                    a.getFullName(),
                    a.getEmail(),
                    a.getPhone(),
                    roleName,
                    a.isActive() ? "Hoạt động" : "Vô hiệu"
            });
        }
    }

    private void filterTable() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            populateTable(currentList);
            return;
        }
        List<Account> filtered = currentList.stream()
                .filter(a -> (a.getUsername() != null && a.getUsername().toLowerCase().contains(keyword))
                        || (a.getFullName() != null && a.getFullName().toLowerCase().contains(keyword))
                        || (a.getEmail() != null && a.getEmail().toLowerCase().contains(keyword))
                        || (a.getPhone() != null && a.getPhone().contains(keyword)))
                .toList();
        populateTable(filtered);
    }

    private Account getSelectedAccount() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản.", "Chú ý", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        return currentList.stream().filter(a -> a.getId() == id).findFirst().orElse(null);
    }

    // ════════════════════════════════════════════════════════════
    //  ACTIONS
    // ════════════════════════════════════════════════════════════

    private void showAddDialog() {
        AccountDialog dialog = new AccountDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null, accountBUS);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void editSelected() {
        Account acc = getSelectedAccount();
        if (acc == null) return;
        AccountDialog dialog = new AccountDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), acc, accountBUS);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void toggleSelected() {
        Account acc = getSelectedAccount();
        if (acc == null) return;
        String action = acc.isActive() ? "vô hiệu hóa" : "kích hoạt";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn " + action + " tài khoản '" + acc.getUsername() + "'?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                accountBUS.toggleActive(acc.getId());
                loadData();
                JOptionPane.showMessageDialog(this, "Đã " + action + " tài khoản.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
            } catch (DataAccessException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetPasswordSelected() {
        Account acc = getSelectedAccount();
        if (acc == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Reset mật khẩu của '" + acc.getUsername() + "' về mặc định (password)?",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                accountBUS.resetPassword(acc.getId());
                JOptionPane.showMessageDialog(this, "Đã reset mật khẩu thành công.\nMật khẩu mới: password",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
            } catch (DataAccessException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelected() {
        Account acc = getSelectedAccount();
        if (acc == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn XÓA VĨNH VIỄN tài khoản '" + acc.getUsername() + "'?\nHành động không thể hoàn tác!",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                accountBUS.delete(acc.getId());
                loadData();
                JOptionPane.showMessageDialog(this, "Đã xóa tài khoản.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
            } catch (DataAccessException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    //  INNER: AccountDialog (Thêm / Sửa tài khoản)
    // ════════════════════════════════════════════════════════════

    private static class AccountDialog extends JDialog {
        private final AccountBUS bus;
        private final Account editing; // null = add mode
        private boolean saved = false;

        private JTextField txtUsername, txtFullName, txtEmail, txtPhone;
        private JPasswordField txtPassword;
        private JComboBox<String> cbRole;
        private JCheckBox chkActive;

        AccountDialog(Frame owner, Account editing, AccountBUS bus) {
            super(owner, editing == null ? "Thêm tài khoản" : "Sửa tài khoản", true);
            this.bus = bus;
            this.editing = editing;
            setSize(420, 460);
            setLocationRelativeTo(owner);
            setResizable(false);
            initUI();
        }

        private void initUI() {
            JPanel content = new JPanel(new GridBagLayout());
            content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 4, 4, 4);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            int row = 0;

            // Username
            gbc.gridx = 0; gbc.gridy = row;
            content.add(new JLabel("Tên đăng nhập:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            txtUsername = new JTextField(20);
            content.add(txtUsername, gbc);

            // Password (only for add)
            row++;
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
            JLabel lblPwd = new JLabel(editing == null ? "Mật khẩu:" : "Mật khẩu mới (bỏ trống = giữ nguyên):");
            content.add(lblPwd, gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            txtPassword = new JPasswordField(20);
            content.add(txtPassword, gbc);

            // Full name
            row++;
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
            content.add(new JLabel("Họ tên:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            txtFullName = new JTextField(20);
            content.add(txtFullName, gbc);

            // Email
            row++;
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
            content.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            txtEmail = new JTextField(20);
            content.add(txtEmail, gbc);

            // Phone
            row++;
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
            content.add(new JLabel("SĐT:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            txtPhone = new JTextField(20);
            content.add(txtPhone, gbc);

            // Role
            row++;
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
            content.add(new JLabel("Vai trò:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            String[] roleNames = new String[Role.values().length];
            for (int i = 0; i < Role.values().length; i++) {
                roleNames[i] = Role.values()[i].getDisplayName();
            }
            cbRole = new JComboBox<>(roleNames);
            content.add(cbRole, gbc);

            // Active
            row++;
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
            content.add(new JLabel("Trạng thái:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            chkActive = new JCheckBox("Hoạt động", true);
            content.add(chkActive, gbc);

            // Buttons
            row++;
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
            RoundedButton btnSave = new RoundedButton("Lưu");
            btnSave.setBackground(UIConstants.PRIMARY);
            btnSave.setForeground(Color.WHITE);
            btnSave.addActionListener(e -> onSave());
            RoundedButton btnCancel = new RoundedButton("Hủy");
            btnCancel.addActionListener(e -> dispose());
            btnPanel.add(btnSave);
            btnPanel.add(btnCancel);
            content.add(btnPanel, gbc);

            add(content);

            // Pre-fill for edit
            if (editing != null) {
                txtUsername.setText(editing.getUsername());
                txtUsername.setEnabled(false); // prevent username change
                txtFullName.setText(editing.getFullName());
                txtEmail.setText(editing.getEmail());
                txtPhone.setText(editing.getPhone());
                chkActive.setSelected(editing.isActive());
                // Select role
                try {
                    Role r = Role.fromId(editing.getRoleId());
                    cbRole.setSelectedItem(r.getDisplayName());
                } catch (IllegalArgumentException ignored) {}
            }
        }

        private void onSave() {
            try {
                String username = txtUsername.getText().trim();
                String fullName = txtFullName.getText().trim();
                String email = txtEmail.getText().trim();
                String phone = txtPhone.getText().trim();
                String password = new String(txtPassword.getPassword());
                boolean active = chkActive.isSelected();

                // Find selected role
                String selectedRoleName = (String) cbRole.getSelectedItem();
                int roleId = 1;
                for (Role r : Role.values()) {
                    if (r.getDisplayName().equals(selectedRoleName)) {
                        roleId = r.getId();
                        break;
                    }
                }

                if (editing == null) {
                    // ADD mode
                    if (password.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Mật khẩu không được để trống khi tạo mới.",
                                "Lỗi", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    Account acc = new Account();
                    acc.setUsername(username);
                    acc.setFullName(fullName);
                    acc.setEmail(email);
                    acc.setPhone(phone);
                    acc.setRoleId(roleId);
                    acc.setActive(active);
                    bus.insert(acc, password);
                    JOptionPane.showMessageDialog(this, "Tạo tài khoản thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // EDIT mode
                    editing.setFullName(fullName);
                    editing.setEmail(email);
                    editing.setPhone(phone);
                    editing.setRoleId(roleId);
                    editing.setActive(active);
                    bus.update(editing);
                    // Change password if provided
                    if (!password.isEmpty()) {
                        bus.changePassword(editing.getId(), password);
                    }
                    JOptionPane.showMessageDialog(this, "Cập nhật tài khoản thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                }
                saved = true;
                dispose();
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
            } catch (DataAccessException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }

        boolean isSaved() { return saved; }
    }
}
