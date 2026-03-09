package com.hospital.gui.panels;

import com.hospital.bus.PatientAllergyBUS;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.util.AsyncTask;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.components.StatusBadge;
import com.hospital.model.PatientAllergy;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel quản lý tiền sử dị ứng bệnh nhân — dành cho Bác sĩ / Lễ tân.
 * Hiển thị danh sách dị ứng, lọc theo mã BN, thêm/sửa/xóa.
 */
public class PatientAllergyPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(PatientAllergyPanel.class.getName());
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PatientAllergyBUS allergyBUS = new PatientAllergyBUS();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtPatientId;
    private JTextField txtSearch;
    private List<PatientAllergy> currentList;

    public PatientAllergyPanel() {
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
        add(createBottomBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("⚠  Quản lý dị ứng bệnh nhân");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(lblTitle, BorderLayout.WEST);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filters.setOpaque(false);

        filters.add(createLabel("Mã BN:"));
        txtPatientId = new JTextField(8);
        txtPatientId.setFont(UIConstants.FONT_BODY);
        txtPatientId.putClientProperty("JTextField.placeholderText", "VD: 1");
        filters.add(txtPatientId);

        RoundedButton btnFilter = new RoundedButton("Lọc");
        btnFilter.setBackground(UIConstants.ACCENT_BLUE);
        btnFilter.setForeground(Color.WHITE);
        btnFilter.addActionListener(e -> filterByPatientId());
        filters.add(btnFilter);

        RoundedButton btnAll = new RoundedButton("Tất cả");
        btnAll.setBackground(UIConstants.TEXT_SECONDARY);
        btnAll.setForeground(Color.WHITE);
        btnAll.addActionListener(e -> { txtPatientId.setText(""); loadData(); });
        filters.add(btnAll);

        txtSearch = new JTextField(14);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm tên chất dị ứng...");
        txtSearch.setFont(UIConstants.FONT_BODY);
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { filterTable(); }
        });
        filters.add(txtSearch);

        header.add(filters, BorderLayout.EAST);
        return header;
    }

    private JPanel createTablePanel() {
        String[] cols = {"ID", "Mã BN", "Chất dị ứng", "Mức độ", "Phản ứng", "Ghi chú", "Ngày tạo"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(UIConstants.FONT_BODY);
        table.setRowHeight(36);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setShowGrid(true);

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(50);
        cm.getColumn(1).setPreferredWidth(60);
        cm.getColumn(2).setPreferredWidth(160);
        cm.getColumn(3).setPreferredWidth(100);
        cm.getColumn(4).setPreferredWidth(200);
        cm.getColumn(5).setPreferredWidth(160);
        cm.getColumn(6).setPreferredWidth(130);

        // Severity badge renderer
        cm.getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                if (value instanceof String s && !s.isEmpty()) {
                    return new StatusBadge(s);
                }
                return super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
            }
        });

        // Context menu
        JPopupMenu popup = new JPopupMenu();
        JMenuItem miEdit = new JMenuItem("✏️ Sửa");
        miEdit.addActionListener(e -> editSelected());
        popup.add(miEdit);
        JMenuItem miDelete = new JMenuItem("🗑️ Xóa");
        miDelete.addActionListener(e -> deleteSelected());
        popup.add(miDelete);
        table.setComponentPopupMenu(popup);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) editSelected();
            }
        });

        RoundedPanel wrapper = new RoundedPanel(14);
        wrapper.setLayout(new BorderLayout());
        wrapper.setBackground(UIConstants.CARD_BG);
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);

        RoundedButton btnAdd = new RoundedButton("+ Thêm dị ứng");
        btnAdd.setBackground(UIConstants.PRIMARY);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> showAddDialog());
        bar.add(btnAdd);

        RoundedButton btnEdit = new RoundedButton("✏️ Sửa");
        btnEdit.setBackground(UIConstants.ACCENT_BLUE);
        btnEdit.setForeground(Color.WHITE);
        btnEdit.addActionListener(e -> editSelected());
        bar.add(btnEdit);

        RoundedButton btnDelete = new RoundedButton("🗑️ Xóa");
        btnDelete.setBackground(UIConstants.ERROR_COLOR);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> deleteSelected());
        bar.add(btnDelete);

        RoundedButton btnRefresh = new RoundedButton("🔄 Làm mới");
        btnRefresh.setBackground(UIConstants.TEXT_SECONDARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> loadData());
        bar.add(btnRefresh);

        return bar;
    }

    // ════════════════════════════════════════════════════════════
    //  DATA
    // ════════════════════════════════════════════════════════════

    private void loadData() {
        AsyncTask.run(
            () -> allergyBUS.findAll(),
            list -> { currentList = list; refreshTable(list); },
            ex -> {
                LOGGER.log(Level.SEVERE, "Lỗi tải dữ liệu dị ứng", ex);
                JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        );
    }

    private void refreshTable(List<PatientAllergy> list) {
        tableModel.setRowCount(0);
        for (PatientAllergy a : list) {
            tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getPatientId(),
                    a.getAllergenName(),
                    a.getSeverityDisplay(),
                    a.getReaction() != null ? a.getReaction() : "",
                    a.getNotes() != null ? a.getNotes() : "",
                    a.getCreatedAt() != null ? a.getCreatedAt().format(DTF) : ""
            });
        }
    }

    private void filterByPatientId() {
        String text = txtPatientId.getText().trim();
        if (text.isEmpty()) { loadData(); return; }
        try {
            long pid = Long.parseLong(text);
            currentList = allergyBUS.findByPatientId(pid);
            refreshTable(currentList);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Mã bệnh nhân phải là số!", "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Lỗi lọc dị ứng", ex);
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (currentList == null) return;
        if (keyword.isEmpty()) { refreshTable(currentList); return; }
        List<PatientAllergy> filtered = currentList.stream()
                .filter(a -> a.getAllergenName().toLowerCase().contains(keyword)
                        || (a.getReaction() != null && a.getReaction().toLowerCase().contains(keyword)))
                .toList();
        refreshTable(filtered);
    }

    // ════════════════════════════════════════════════════════════
    //  CRUD
    // ════════════════════════════════════════════════════════════

    private void showAddDialog() {
        AllergyDialog dlg = new AllergyDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadData();
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        PatientAllergy a = allergyBUS.findById(id);
        if (a == null) { JOptionPane.showMessageDialog(this, "Không tìm thấy."); return; }
        AllergyDialog dlg = new AllergyDialog(SwingUtilities.getWindowAncestor(this), a);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadData();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa dị ứng #" + id + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            allergyBUS.delete(id);
            loadData();
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa dị ứng", ex);
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  HELPER
    // ════════════════════════════════════════════════════════════

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        return lbl;
    }

    // ════════════════════════════════════════════════════════════
    //  INNER DIALOG
    // ════════════════════════════════════════════════════════════

    private class AllergyDialog extends JDialog {
        private final PatientAllergy editing;
        private boolean saved = false;

        private JTextField txtPatientIdDlg;
        private JTextField txtAllergenName;
        private JComboBox<String> cboSeverity;
        private JTextField txtReaction;
        private JTextArea txtNotesDlg;

        AllergyDialog(Window owner, PatientAllergy existing) {
            super(owner, existing == null ? "Thêm dị ứng" : "Sửa dị ứng",
                    ModalityType.APPLICATION_MODAL);
            this.editing = existing;
            setSize(440, 420);
            setLocationRelativeTo(owner);
            setResizable(false);
            buildUI();
            if (editing != null) populateFields();
        }

        boolean isSaved() { return saved; }

        private void buildUI() {
            JPanel content = new JPanel(new GridBagLayout());
            content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
            content.setBackground(UIConstants.CARD_BG);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 6, 6, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            int row = 0;

            // Mã bệnh nhân
            gbc.gridx = 0; gbc.gridy = row;
            content.add(createLabel("Mã bệnh nhân *"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            txtPatientIdDlg = new JTextField(15);
            txtPatientIdDlg.setFont(UIConstants.FONT_BODY);
            content.add(txtPatientIdDlg, gbc);
            gbc.weightx = 0;

            // Tên chất dị ứng
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            content.add(createLabel("Chất dị ứng *"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            txtAllergenName = new JTextField(15);
            txtAllergenName.setFont(UIConstants.FONT_BODY);
            content.add(txtAllergenName, gbc);
            gbc.weightx = 0;

            // Mức độ
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            content.add(createLabel("Mức độ *"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            cboSeverity = new JComboBox<>(new String[]{"Nhẹ (MILD)", "Trung bình (MODERATE)", "Nặng (SEVERE)"});
            cboSeverity.setFont(UIConstants.FONT_BODY);
            cboSeverity.setSelectedIndex(1); // default MODERATE
            content.add(cboSeverity, gbc);
            gbc.weightx = 0;

            // Phản ứng
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            content.add(createLabel("Phản ứng"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            txtReaction = new JTextField(15);
            txtReaction.setFont(UIConstants.FONT_BODY);
            content.add(txtReaction, gbc);
            gbc.weightx = 0;

            // Ghi chú
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            content.add(createLabel("Ghi chú"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            txtNotesDlg = new JTextArea(3, 15);
            txtNotesDlg.setFont(UIConstants.FONT_BODY);
            txtNotesDlg.setLineWrap(true);
            txtNotesDlg.setWrapStyleWord(true);
            content.add(new JScrollPane(txtNotesDlg), gbc);
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            // Buttons
            row++;
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            btnPanel.setOpaque(false);

            RoundedButton btnSave = new RoundedButton(editing == null ? "Thêm" : "Lưu");
            btnSave.setBackground(UIConstants.PRIMARY);
            btnSave.setForeground(Color.WHITE);
            btnSave.addActionListener(e -> onSave());
            btnPanel.add(btnSave);

            RoundedButton btnCancel = new RoundedButton("Hủy");
            btnCancel.setBackground(UIConstants.TEXT_SECONDARY);
            btnCancel.setForeground(Color.WHITE);
            btnCancel.addActionListener(e -> dispose());
            btnPanel.add(btnCancel);

            content.add(btnPanel, gbc);
            setContentPane(content);
        }

        private void populateFields() {
            txtPatientIdDlg.setText(String.valueOf(editing.getPatientId()));
            txtPatientIdDlg.setEditable(false);
            txtAllergenName.setText(editing.getAllergenName());
            // Map severity DB value to combo index
            int idx = switch (editing.getSeverity()) {
                case "MILD" -> 0;
                case "SEVERE" -> 2;
                default -> 1; // MODERATE
            };
            cboSeverity.setSelectedIndex(idx);
            txtReaction.setText(editing.getReaction() != null ? editing.getReaction() : "");
            txtNotesDlg.setText(editing.getNotes() != null ? editing.getNotes() : "");
        }

        private void onSave() {
            try {
                PatientAllergy a = editing != null ? editing : new PatientAllergy();
                a.setPatientId(Long.parseLong(txtPatientIdDlg.getText().trim()));
                a.setAllergenName(txtAllergenName.getText().trim());

                // Parse severity from combo
                String sel = (String) cboSeverity.getSelectedItem();
                if (sel != null && sel.contains("MILD")) a.setSeverity("MILD");
                else if (sel != null && sel.contains("SEVERE")) a.setSeverity("SEVERE");
                else a.setSeverity("MODERATE");

                a.setReaction(txtReaction.getText().trim());
                a.setNotes(txtNotesDlg.getText().trim());

                if (editing != null) {
                    allergyBUS.update(a);
                } else {
                    allergyBUS.insert(a);
                }
                saved = true;
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Mã bệnh nhân phải là số!",
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
            } catch (DataAccessException ex) {
                LOGGER.log(Level.SEVERE, "Lỗi lưu dị ứng", ex);
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
