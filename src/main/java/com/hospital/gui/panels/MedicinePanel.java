package com.hospital.gui.panels;

import com.hospital.bus.MedicineBUS;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.components.StatusBadge;
import com.hospital.model.Medicine;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Trang Kho thuốc.
 */
public class MedicinePanel extends JPanel {

    private final MedicineBUS bus = new MedicineBUS();
    private DefaultTableModel tableModel;
    private JTable table;

    // Form
    private JTextField txtCode, txtName, txtUnit, txtPrice,
                       txtQty, txtMinQty, txtCategory, txtMfr, txtExpiry;

    public MedicinePanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Kho thuốc");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY_RED);

        // Low-stock counter
        int low = bus.countLowStock();
        JLabel warnLbl = new JLabel("  ⚠  " + low + " thuốc sắp hết kho");
        warnLbl.setFont(UIConstants.FONT_BOLD);
        warnLbl.setForeground(UIConstants.WARNING_ORANGE);

        header.add(title,   BorderLayout.WEST);
        header.add(warnLbl, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Body
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
        card.setPreferredSize(new Dimension(290, 0));

        JLabel formTitle = new JLabel("Thông tin thuốc");
        formTitle.setFont(UIConstants.FONT_SUBTITLE);
        formTitle.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(formTitle, BorderLayout.NORTH);

        txtCode     = new JTextField();
        txtName     = new JTextField();
        txtUnit     = new JTextField();
        txtPrice    = new JTextField();
        txtQty      = new JTextField();
        txtMinQty   = new JTextField("10");
        txtCategory = new JTextField();
        txtMfr      = new JTextField();
        txtExpiry   = new JTextField("dd/MM/yyyy");

        Object[][] rows = {
            {"Mã thuốc (*)", txtCode},
            {"Tên thuốc (*)", txtName},
            {"Đơn vị", txtUnit},
            {"Đơn giá (VNĐ)", txtPrice},
            {"Số lượng (*)", txtQty},
            {"Ngưỡng cảnh báo", txtMinQty},
            {"Nhóm thuốc", txtCategory},
            {"Nhà sản xuất", txtMfr},
            {"Hạn sử dụng", txtExpiry},
        };

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.weightx = 1;
        g.insets = new Insets(4, 0, 4, 0);
        int row = 0;
        for (Object[] r : rows) {
            g.gridy = row++;
            JLabel lbl = new JLabel((String) r[0]);
            lbl.setFont(UIConstants.FONT_LABEL);
            lbl.setForeground(UIConstants.TEXT_SECONDARY);
            fields.add(lbl, g);
            g.gridy = row++;
            JTextField tf = (JTextField) r[1];
            tf.setFont(UIConstants.FONT_LABEL);
            fields.add(tf, g);
        }
        card.add(fields, BorderLayout.CENTER);

        JPanel btns = new JPanel(new GridLayout(2, 1, 0, 8));
        btns.setOpaque(false);
        RoundedButton btnAdd   = new RoundedButton("Thêm thuốc");
        btnAdd.addActionListener(e -> addMedicine());
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

        // Search
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setOpaque(false);
        JTextField txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm thuốc...");
        RoundedButton btnSearch = new RoundedButton("Tìm");
        btnSearch.setPreferredSize(new Dimension(70, 32));
        txtSearch.addActionListener(e -> searchMedicine(txtSearch.getText()));
        btnSearch.addActionListener(e -> searchMedicine(txtSearch.getText()));

        RoundedButton btnLow = new RoundedButton("Sắp hết kho");
        btnLow.setColors(UIConstants.WARNING_ORANGE, UIConstants.WARNING_ORANGE.darker());
        btnLow.setPreferredSize(new Dimension(120, 32));
        btnLow.addActionListener(e -> loadTable(bus.getLowStockMedicines()));

        topBar.add(txtSearch, BorderLayout.CENTER);
        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        acts.setOpaque(false);
        acts.add(btnSearch);
        acts.add(btnLow);
        topBar.add(acts, BorderLayout.EAST);
        card.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Mã thuốc", "Tên thuốc", "Đơn vị", "Đơn giá", "Tồn kho", "Ngưỡng", "Nhóm", "Hạn SD", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(42);
        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(UIConstants.RED_BG_SOFT);
        table.setSelectionForeground(UIConstants.TEXT_PRIMARY);

        // Status column
        table.getColumnModel().getColumn(8).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean s, boolean f, int r, int c) {
                    String text = Boolean.TRUE.equals(v) ? "Sắp hết" : "Đủ kho";
                    StatusBadge b = new StatusBadge(
                        Boolean.TRUE.equals(v) ? "CHỜ KHÁM" : "XONG");
                    b.setText(text);
                    b.setHorizontalAlignment(CENTER);
                    return b;
                }
            });

        // Price column renderer
        DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer() {
            final NumberFormat fmt = NumberFormat.getInstance(new Locale("vi","VN"));
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (v instanceof Number) setText(fmt.format(v) + " đ");
                setHorizontalAlignment(RIGHT);
                return this;
            }
        };
        table.getColumnModel().getColumn(3).setCellRenderer(priceRenderer);

        // Selection → populate form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0)
                populateForm(table.getSelectedRow());
        });

        loadTable(bus.findAll());
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    // ── Logic ─────────────────────────────────────────────────────────────────
    private void loadTable(List<Medicine> list) {
        tableModel.setRowCount(0);
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi","VN"));
        for (Medicine m : list) {
            tableModel.addRow(new Object[]{
                m.getMedicineCode(), m.getName(), m.getUnit(),
                m.getPrice(), m.getQuantity(), m.getMinQuantity(),
                m.getCategory(), m.getExpiryDate(), m.isLowStock()
            });
        }
    }

    private void searchMedicine(String kw) {
        if (kw == null || kw.isBlank()) { loadTable(bus.findAll()); return; }
        String lc = kw.toLowerCase();
        loadTable(bus.findAll().stream()
            .filter(m -> m.getName().toLowerCase().contains(lc)
                    || m.getMedicineCode().toLowerCase().contains(lc)
                    || m.getCategory().toLowerCase().contains(lc))
            .collect(java.util.stream.Collectors.toList()));
    }

    private void addMedicine() {
        String code = txtCode.getText().trim();
        String name = txtName.getText().trim();
        String qtyStr = txtQty.getText().trim();
        if (code.isEmpty() || name.isEmpty() || qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền các trường có dấu (*).", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            double price = txtPrice.getText().isBlank() ? 0 : Double.parseDouble(txtPrice.getText().trim());
            int qty      = Integer.parseInt(qtyStr);
            int minQty   = txtMinQty.getText().isBlank() ? 10 : Integer.parseInt(txtMinQty.getText().trim());
            Medicine m   = new Medicine(0, code, name,
                    txtUnit.getText().trim(), price, qty, minQty,
                    txtCategory.getText().trim(), txtMfr.getText().trim(), txtExpiry.getText().trim());
            bus.insert(m);
            loadTable(bus.findAll());
            clearForm();
            JOptionPane.showMessageDialog(this, "Đã thêm thuốc thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá và số lượng phải là số.", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateForm(int row) {
        txtCode.setText((String)  tableModel.getValueAt(row, 0));
        txtName.setText((String)  tableModel.getValueAt(row, 1));
        txtUnit.setText((String)  tableModel.getValueAt(row, 2));
        txtPrice.setText(tableModel.getValueAt(row, 3).toString());
        txtQty.setText(  tableModel.getValueAt(row, 4).toString());
        txtMinQty.setText(tableModel.getValueAt(row, 5).toString());
        txtCategory.setText((String) tableModel.getValueAt(row, 6));
        txtExpiry.setText((String)   tableModel.getValueAt(row, 7));
    }

    private void clearForm() {
        for (JTextField tf : new JTextField[]{txtCode,txtName,txtUnit,txtPrice,txtQty,txtCategory,txtMfr,txtExpiry})
            tf.setText("");
        txtMinQty.setText("10");
        table.clearSelection();
    }
}
