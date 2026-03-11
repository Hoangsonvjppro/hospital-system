package com.hospital.gui.panels;

import com.hospital.bus.LabResultBUS;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.util.AsyncTask;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.model.LabResult;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel quản lý kết quả xét nghiệm — dành cho Bác sĩ.
 * Hiển thị danh sách kết quả, hỗ trợ thêm/sửa/xóa, lọc theo mã bệnh án.
 */
public class LabResultPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(LabResultPanel.class.getName());
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final LabResultBUS labResultBUS = new LabResultBUS();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private JTextField txtRecordId;
    private List<LabResult> currentList;

    public LabResultPanel() {
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

        JLabel lblTitle = new JLabel("  Kết quả xét nghiệm");
        lblTitle.setIcon(com.hospital.gui.IconManager.getIcon("microscope", 20, 20));
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(lblTitle, BorderLayout.WEST);

        // Filter bar
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filters.setOpaque(false);

        filters.add(createLabel("Mã bệnh án:"));
        txtRecordId = new JTextField(8);
        txtRecordId.setFont(UIConstants.FONT_BODY);
        txtRecordId.putClientProperty("JTextField.placeholderText", "VD: 101");
        filters.add(txtRecordId);

        RoundedButton btnFilter = new RoundedButton("Lọc");
        btnFilter.setBackground(UIConstants.ACCENT_BLUE);
        btnFilter.setForeground(Color.WHITE);
        btnFilter.addActionListener(e -> filterByRecordId());
        filters.add(btnFilter);

        RoundedButton btnShowAll = new RoundedButton("Tất cả");
        btnShowAll.setBackground(UIConstants.TEXT_SECONDARY);
        btnShowAll.setForeground(Color.WHITE);
        btnShowAll.addActionListener(e -> {
            txtRecordId.setText("");
            loadData();
        });
        filters.add(btnShowAll);

        txtSearch = new JTextField(16);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm tên xét nghiệm...");
        txtSearch.setFont(UIConstants.FONT_BODY);
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) { filterTable(); }
        });
        filters.add(txtSearch);

        header.add(filters, BorderLayout.EAST);
        return header;
    }

    private JPanel createTablePanel() {
        String[] cols = {"ID", "Mã BA", "Tên xét nghiệm", "Kết quả", "Khoảng BT", "Đơn vị", "Ngày XN", "Ghi chú"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(UIConstants.FONT_BODY);
        table.setRowHeight(36);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setShowGrid(true);

        // Column widths
        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(50);   // ID
        cm.getColumn(1).setPreferredWidth(60);   // Mã BA
        cm.getColumn(2).setPreferredWidth(180);  // Tên XN
        cm.getColumn(3).setPreferredWidth(150);  // Kết quả
        cm.getColumn(4).setPreferredWidth(120);  // Khoảng BT
        cm.getColumn(5).setPreferredWidth(70);   // Đơn vị
        cm.getColumn(6).setPreferredWidth(130);  // Ngày XN
        cm.getColumn(7).setPreferredWidth(180);  // Ghi chú

        // Right-click context menu
        JPopupMenu popup = new JPopupMenu();
        JMenuItem miEdit = new JMenuItem("Sửa");
        miEdit.setIcon(com.hospital.gui.IconManager.getIcon("edit", 14, 14));
        miEdit.addActionListener(e -> editSelected());
        popup.add(miEdit);
        JMenuItem miDelete = new JMenuItem("Xóa");
        miDelete.setIcon(com.hospital.gui.IconManager.getIcon("delete", 14, 14));
        miDelete.addActionListener(e -> deleteSelected());
        popup.add(miDelete);
        table.setComponentPopupMenu(popup);

        // Double-click to edit
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
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

        RoundedButton btnAdd = new RoundedButton("+ Thêm kết quả");
        btnAdd.setBackground(UIConstants.PRIMARY);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> showAddDialog());
        bar.add(btnAdd);

        RoundedButton btnEdit = new RoundedButton("Sửa");
        btnEdit.setIcon(com.hospital.gui.IconManager.getIcon("edit", 14, 14));
        btnEdit.setBackground(UIConstants.ACCENT_BLUE);
        btnEdit.setForeground(Color.WHITE);
        btnEdit.addActionListener(e -> editSelected());
        bar.add(btnEdit);

        RoundedButton btnDelete = new RoundedButton("Xóa");
        btnDelete.setIcon(com.hospital.gui.IconManager.getIcon("delete", 14, 14));
        btnDelete.setBackground(UIConstants.ERROR_COLOR);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> deleteSelected());
        bar.add(btnDelete);

        RoundedButton btnRefresh = new RoundedButton("Làm mới");
        btnRefresh.setIcon(com.hospital.gui.IconManager.getIcon("refresh", 14, 14));
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
            () -> labResultBUS.findAll(),
            list -> { currentList = list; refreshTable(list); },
            ex -> {
                LOGGER.log(Level.SEVERE, "Lỗi tải dữ liệu xét nghiệm", ex);
                JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        );
    }

    private void refreshTable(List<LabResult> list) {
        tableModel.setRowCount(0);
        for (LabResult r : list) {
            tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getRecordId(),
                    r.getTestName(),
                    r.getResultValue() != null ? r.getResultValue() : "",
                    r.getNormalRange() != null ? r.getNormalRange() : "",
                    r.getUnit() != null ? r.getUnit() : "",
                    r.getTestDate() != null ? r.getTestDate().format(DTF) : "",
                    r.getNotes() != null ? r.getNotes() : ""
            });
        }
    }

    private void filterByRecordId() {
        String text = txtRecordId.getText().trim();
        if (text.isEmpty()) {
            loadData();
            return;
        }
        try {
            long recordId = Long.parseLong(text);
            currentList = labResultBUS.findByRecordId(recordId);
            refreshTable(currentList);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Mã bệnh án phải là số!", "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Lỗi lọc xét nghiệm theo bệnh án", ex);
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (currentList == null) return;
        if (keyword.isEmpty()) {
            refreshTable(currentList);
            return;
        }
        List<LabResult> filtered = currentList.stream()
                .filter(r -> r.getTestName().toLowerCase().contains(keyword)
                        || (r.getResultValue() != null && r.getResultValue().toLowerCase().contains(keyword))
                        || (r.getNotes() != null && r.getNotes().toLowerCase().contains(keyword)))
                .toList();
        refreshTable(filtered);
    }

    // ════════════════════════════════════════════════════════════
    //  CRUD ACTIONS
    // ════════════════════════════════════════════════════════════

    private void showAddDialog() {
        LabResultDialog dlg = new LabResultDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadData();
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để sửa.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        LabResult r = labResultBUS.findById(id);
        if (r == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả xét nghiệm.");
            return;
        }
        LabResultDialog dlg = new LabResultDialog(SwingUtilities.getWindowAncestor(this), r);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadData();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để xóa.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận xóa kết quả xét nghiệm #" + id + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            labResultBUS.delete(id);
            loadData();
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa xét nghiệm", ex);
            JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
    //  INNER DIALOG — Add / Edit Lab Result
    // ════════════════════════════════════════════════════════════

    private class LabResultDialog extends JDialog {
        private final LabResult editing;
        private boolean saved = false;

        private JTextField txtRecordIdDlg;
        private JTextField txtServiceOrderId;
        private JTextField txtTestName;
        private JTextField txtResultValue;
        private JTextField txtNormalRange;
        private JTextField txtUnit;
        private JTextArea txtNotesDlg;

        LabResultDialog(Window owner, LabResult existing) {
            super(owner, existing == null ? "Thêm kết quả xét nghiệm" : "Sửa kết quả xét nghiệm",
                    ModalityType.APPLICATION_MODAL);
            this.editing = existing;
            setSize(480, 520);
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

            // Mã bệnh án
            gbc.gridx = 0; gbc.gridy = row;
            content.add(createLabel("Mã bệnh án *"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            txtRecordIdDlg = new JTextField(15);
            txtRecordIdDlg.setFont(UIConstants.FONT_BODY);
            content.add(txtRecordIdDlg, gbc);
            gbc.weightx = 0;

            // Mã phiếu chỉ định (optional)
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            content.add(createLabel("Mã phiếu CĐ"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            txtServiceOrderId = new JTextField(15);
            txtServiceOrderId.setFont(UIConstants.FONT_BODY);
            content.add(txtServiceOrderId, gbc);
            gbc.weightx = 0;

            // Tên xét nghiệm
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            content.add(createLabel("Tên xét nghiệm *"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            txtTestName = new JTextField(15);
            txtTestName.setFont(UIConstants.FONT_BODY);
            content.add(txtTestName, gbc);
            gbc.weightx = 0;

            // Kết quả
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            content.add(createLabel("Kết quả"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            txtResultValue = new JTextField(15);
            txtResultValue.setFont(UIConstants.FONT_BODY);
            content.add(txtResultValue, gbc);
            gbc.weightx = 0;

            // Khoảng bình thường
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            content.add(createLabel("Khoảng BT"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            txtNormalRange = new JTextField(15);
            txtNormalRange.setFont(UIConstants.FONT_BODY);
            content.add(txtNormalRange, gbc);
            gbc.weightx = 0;

            // Đơn vị
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            content.add(createLabel("Đơn vị"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            txtUnit = new JTextField(15);
            txtUnit.setFont(UIConstants.FONT_BODY);
            content.add(txtUnit, gbc);
            gbc.weightx = 0;

            // Ghi chú
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            content.add(createLabel("Ghi chú"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            txtNotesDlg = new JTextArea(4, 15);
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
            txtRecordIdDlg.setText(String.valueOf(editing.getRecordId()));
            txtRecordIdDlg.setEditable(false);
            if (editing.getServiceOrderId() != null) {
                txtServiceOrderId.setText(String.valueOf(editing.getServiceOrderId()));
            }
            txtTestName.setText(editing.getTestName());
            txtResultValue.setText(editing.getResultValue() != null ? editing.getResultValue() : "");
            txtNormalRange.setText(editing.getNormalRange() != null ? editing.getNormalRange() : "");
            txtUnit.setText(editing.getUnit() != null ? editing.getUnit() : "");
            txtNotesDlg.setText(editing.getNotes() != null ? editing.getNotes() : "");
        }

        private void onSave() {
            try {
                LabResult r = editing != null ? editing : new LabResult();
                r.setRecordId(Long.parseLong(txtRecordIdDlg.getText().trim()));

                String soText = txtServiceOrderId.getText().trim();
                r.setServiceOrderId(soText.isEmpty() ? null : Long.parseLong(soText));

                r.setTestName(txtTestName.getText().trim());
                r.setResultValue(txtResultValue.getText().trim());
                r.setNormalRange(txtNormalRange.getText().trim());
                r.setUnit(txtUnit.getText().trim());
                r.setTestDate(LocalDateTime.now()); // auto timestamp
                r.setNotes(txtNotesDlg.getText().trim());

                if (editing != null) {
                    labResultBUS.update(r);
                } else {
                    labResultBUS.insert(r);
                }
                saved = true;
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Mã bệnh án / phiếu CĐ phải là số!",
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
            } catch (DataAccessException ex) {
                LOGGER.log(Level.SEVERE, "Lỗi lưu kết quả xét nghiệm", ex);
                JOptionPane.showMessageDialog(this, "Lỗi lưu: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
