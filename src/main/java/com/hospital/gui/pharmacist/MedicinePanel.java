package com.hospital.gui.pharmacist;

import com.hospital.bus.MedicineBUS;
import com.hospital.bus.MedicineIngredientBUS;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Quản lý danh mục thuốc — CRUD thuốc + xem thành phần.
 */
public class MedicinePanel extends JPanel {

    private final MedicineBUS medicineBUS = new MedicineBUS();
    private final MedicineIngredientBUS ingredientBUS = new MedicineIngredientBUS();

    private DefaultTableModel medModel;
    private JTable medTable;
    private JTextField txtSearch;
    private JTextField txtCode, txtName, txtGeneric, txtUnit, txtForm, txtManufacturer, txtDesc;
    private DefaultTableModel ingredientModel;
    private Medicine selectedMedicine;

    public MedicinePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        loadMedicines();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("💊 Kho thuốc");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setOpaque(false);
        txtSearch = new JTextField(20);
        txtSearch.setFont(UIConstants.FONT_BODY);
        txtSearch.addActionListener(e -> searchMedicines());
        searchBar.add(txtSearch);

        RoundedButton btnSearch = new RoundedButton("🔍 Tìm");
        btnSearch.setBackground(UIConstants.PRIMARY);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.addActionListener(e -> searchMedicines());
        searchBar.add(btnSearch);

        RoundedButton btnRefresh = new RoundedButton("🔄");
        btnRefresh.setBackground(UIConstants.ACCENT_BLUE);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadMedicines(); });
        searchBar.add(btnRefresh);

        header.add(searchBar, BorderLayout.EAST);
        return header;
    }

    private JPanel createBody() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                createTablePanel(), createDetailPanel());
        split.setDividerLocation(300);
        split.setResizeWeight(0.55);
        split.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(split, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createTablePanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        medModel = new DefaultTableModel(
                new String[]{"ID", "Mã thuốc", "Tên thuốc", "Hoạt chất", "ĐVT", "Dạng", "NSX", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        medTable = new JTable(medModel);
        medTable.setRowHeight(32);
        medTable.setFont(UIConstants.FONT_LABEL);
        medTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        medTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        medTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        medTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onMedicineSelected();
        });

        JScrollPane scroll = new JScrollPane(medTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createDetailPanel() {
        JPanel detail = new JPanel(new GridLayout(1, 2, 12, 0));
        detail.setOpaque(false);

        // Left: Edit form
        RoundedPanel formCard = new RoundedPanel(UIConstants.CARD_RADIUS);
        formCard.setBackground(UIConstants.CARD_BG);
        formCard.setLayout(new BorderLayout(0, 8));
        formCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblForm = new JLabel("Thông tin thuốc");
        lblForm.setFont(UIConstants.FONT_SUBTITLE);
        lblForm.setForeground(UIConstants.TEXT_PRIMARY);
        formCard.add(lblForm, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        txtCode = addFormField(form, gbc, row++, "Mã thuốc:", 15);
        txtName = addFormField(form, gbc, row++, "Tên thuốc:", 20);
        txtGeneric = addFormField(form, gbc, row++, "Hoạt chất:", 20);
        txtUnit = addFormField(form, gbc, row++, "Đơn vị:", 10);
        txtForm = addFormField(form, gbc, row++, "Dạng bào chế:", 15);
        txtManufacturer = addFormField(form, gbc, row++, "Nhà sản xuất:", 20);
        txtDesc = addFormField(form, gbc, row++, "Mô tả:", 20);

        // Buttons
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
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

        formCard.add(form, BorderLayout.CENTER);

        // Right: Ingredients
        RoundedPanel ingredientCard = new RoundedPanel(UIConstants.CARD_RADIUS);
        ingredientCard.setBackground(UIConstants.CARD_BG);
        ingredientCard.setLayout(new BorderLayout(0, 8));
        ingredientCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblIng = new JLabel("Thành phần thuốc");
        lblIng.setFont(UIConstants.FONT_SUBTITLE);
        lblIng.setForeground(UIConstants.TEXT_PRIMARY);
        ingredientCard.add(lblIng, BorderLayout.NORTH);

        ingredientModel = new DefaultTableModel(
                new String[]{"ID", "Thành phần"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable ingredientTable = new JTable(ingredientModel);
        ingredientTable.setRowHeight(30);
        ingredientTable.setFont(UIConstants.FONT_LABEL);
        ingredientTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        ingredientTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);

        JScrollPane scrollIng = new JScrollPane(ingredientTable);
        scrollIng.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        ingredientCard.add(scrollIng, BorderLayout.CENTER);

        detail.add(formCard);
        detail.add(ingredientCard);
        return detail;
    }

    private JTextField addFormField(JPanel form, GridBagConstraints gbc, int row, String label, int cols) {
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

    private void loadMedicines() {
        medModel.setRowCount(0);
        try {
            List<Medicine> list = medicineBUS.findAll();
            for (Medicine m : list) {
                medModel.addRow(new Object[]{
                        m.getId(), m.getMedicineCode(), m.getMedicineName(),
                        m.getGenericName(), m.getUnit(), m.getDosageForm(),
                        m.getManufacturer(), m.isActive() ? "Hoạt động" : "Ngưng"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải thuốc: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchMedicines() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) { loadMedicines(); return; }

        medModel.setRowCount(0);
        try {
            List<Medicine> list = medicineBUS.findByName(keyword);
            for (Medicine m : list) {
                medModel.addRow(new Object[]{
                        m.getId(), m.getMedicineCode(), m.getMedicineName(),
                        m.getGenericName(), m.getUnit(), m.getDosageForm(),
                        m.getManufacturer(), m.isActive() ? "Hoạt động" : "Ngưng"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onMedicineSelected() {
        int row = medTable.getSelectedRow();
        if (row < 0) return;

        int id = (int) medModel.getValueAt(row, 0);
        try {
            selectedMedicine = medicineBUS.findById(id);
            if (selectedMedicine == null) return;

            txtCode.setText(selectedMedicine.getMedicineCode());
            txtName.setText(selectedMedicine.getMedicineName());
            txtGeneric.setText(selectedMedicine.getGenericName());
            txtUnit.setText(selectedMedicine.getUnit());
            txtForm.setText(selectedMedicine.getDosageForm());
            txtManufacturer.setText(selectedMedicine.getManufacturer());
            txtDesc.setText(selectedMedicine.getDescription());

            // Load ingredients
            ingredientModel.setRowCount(0);
            List<MedicineIngredient> ingredients = ingredientBUS.findByMedicineId(selectedMedicine.getId());
            for (MedicineIngredient ing : ingredients) {
                ingredientModel.addRow(new Object[]{
                        ing.getId(),
                        ing.getIngredientName()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doInsert() {
        try {
            Medicine m = buildMedicineFromForm();
            medicineBUS.insert(m);
            JOptionPane.showMessageDialog(this, "Thêm thuốc thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadMedicines();
            clearForm();
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doUpdate() {
        if (selectedMedicine == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thuốc cần cập nhật.");
            return;
        }
        try {
            Medicine m = buildMedicineFromForm();
            m.setId(selectedMedicine.getId());
            medicineBUS.update(m);
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadMedicines();
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doDelete() {
        if (selectedMedicine == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thuốc cần xóa.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa thuốc: " + selectedMedicine.getMedicineName() + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            medicineBUS.delete(selectedMedicine.getId());
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            loadMedicines();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Medicine buildMedicineFromForm() {
        Medicine m = new Medicine();
        m.setMedicineCode(txtCode.getText().trim());
        m.setMedicineName(txtName.getText().trim());
        m.setGenericName(txtGeneric.getText().trim());
        m.setUnit(txtUnit.getText().trim());
        m.setDosageForm(txtForm.getText().trim());
        m.setManufacturer(txtManufacturer.getText().trim());
        m.setDescription(txtDesc.getText().trim());
        m.setActive(true);
        return m;
    }

    private void clearForm() {
        selectedMedicine = null;
        txtCode.setText(""); txtName.setText(""); txtGeneric.setText("");
        txtUnit.setText(""); txtForm.setText(""); txtManufacturer.setText(""); txtDesc.setText("");
        ingredientModel.setRowCount(0);
        medTable.clearSelection();
    }
}
