package com.hospital.gui.panels;

import com.hospital.bus.MedicineBUS;
import com.hospital.bus.PrescriptionBUS;
import com.hospital.bus.event.EventBus;
import com.hospital.bus.event.PrescriptionCreatedEvent;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.model.Medicine;
import com.hospital.model.PrescriptionDetail;
import com.hospital.util.AsyncTask;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel kê đơn thuốc — dành cho Bác sĩ.
 * <p>
 * Tính năng:
 * - Autocomplete search thuốc (gõ tên → gợi ý)
 * - Hiển thị thông tin thuốc: tồn kho, giá, đơn vị
 * - Form: Thuốc | Liều dùng | Số lượng | Cách dùng | Số ngày
 * - Kiểm tra dị ứng bệnh nhân → cảnh báo đỏ
 * - Kiểm tra tồn kho → cảnh báo vàng
 * - Bảng hiển thị thuốc đã kê trong đơn
 * - Nút: Thêm, Xóa, Hoàn tất đơn, In đơn thuốc
 */
public class PrescriptionPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(PrescriptionPanel.class.getName());
    private static final DecimalFormat MONEY_FMT = new DecimalFormat("#,### đ");

    private final MedicineBUS medicineBUS = new MedicineBUS();
    private final PrescriptionBUS prescriptionBUS = new PrescriptionBUS();

    // Context
    private long examinationId;
    private long patientId;
    private long doctorId;

    // Search
    private JTextField txtSearch;
    private JTable searchTable;
    private DefaultTableModel searchModel;

    // Form fields for adding medicine
    private JLabel lblMedicineInfo;
    private JTextField txtDosage;
    private JTextField txtQuantity;
    private JTextField txtFrequency;
    private JTextField txtDuration;
    private JComboBox<String> cbInstruction;

    // Prescription table
    private JTable prescTable;
    private DefaultTableModel prescModel;
    private final List<PrescriptionDetail> prescriptionItems = new ArrayList<>();

    // Warning panel
    private JPanel warningPanel;

    // Selected medicine from search
    private Medicine selectedMedicine;

    /**
     * Constructor với context lần khám.
     */
    public PrescriptionPanel(long examinationId, long patientId, long doctorId) {
        this.examinationId = examinationId;
        this.patientId = patientId;
        this.doctorId = doctorId;

        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        initComponents();
        loadExistingPrescription();
    }

    public PrescriptionPanel() {
        this(0, 0, 0);
    }

    public void setContext(long examinationId, long patientId, long doctorId) {
        this.examinationId = examinationId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        prescriptionItems.clear();
        prescModel.setRowCount(0);
        loadExistingPrescription();
    }

    /**
     * Trả về danh sách thuốc đã kê (để tích hợp với DoctorWorkstation).
     */
    public List<PrescriptionDetail> getPrescriptionItems() {
        return new ArrayList<>(prescriptionItems);
    }

    // ════════════════════════════════════════════════════════════
    //  INIT UI
    // ════════════════════════════════════════════════════════════

    private void initComponents() {
        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createBottomBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("💊  Kê đơn thuốc");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(lblTitle, BorderLayout.WEST);

        // Warning panel (for allergy/stock alerts)
        warningPanel = new JPanel();
        warningPanel.setLayout(new BoxLayout(warningPanel, BoxLayout.Y_AXIS));
        warningPanel.setOpaque(false);
        header.add(warningPanel, BorderLayout.SOUTH);

        return header;
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setOpaque(false);

        // Top: search + medicine form
        JPanel topPanel = new JPanel(new BorderLayout(12, 0));
        topPanel.setOpaque(false);
        topPanel.add(createSearchPanel(), BorderLayout.CENTER);
        topPanel.add(createMedicineForm(), BorderLayout.EAST);
        main.add(topPanel, BorderLayout.NORTH);

        // Center: prescription table
        main.add(createPrescriptionTable(), BorderLayout.CENTER);

        return main;
    }

    private JPanel createSearchPanel() {
        RoundedPanel panel = new RoundedPanel(12);
        panel.setBackground(UIConstants.CARD_BG);
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Search field
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍 Tìm thuốc:");
        lblSearch.setFont(UIConstants.FONT_BOLD);
        lblSearch.setForeground(UIConstants.TEXT_PRIMARY);
        searchRow.add(lblSearch, BorderLayout.WEST);

        txtSearch = new JTextField();
        txtSearch.setFont(UIConstants.FONT_BODY);
        txtSearch.putClientProperty("JTextField.placeholderText", "Gõ tên thuốc để tìm kiếm...");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onSearchChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { onSearchChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onSearchChanged(); }
        });
        searchRow.add(txtSearch, BorderLayout.CENTER);
        panel.add(searchRow, BorderLayout.NORTH);

        // Search results table
        String[] cols = {"ID", "Tên thuốc", "Hoạt chất", "Đơn vị", "Dạng bào chế", "Giá bán", "Tồn kho"};
        searchModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        searchTable = new JTable(searchModel);
        searchTable.setFont(UIConstants.FONT_LABEL);
        searchTable.setRowHeight(32);
        searchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        searchTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        searchTable.setGridColor(UIConstants.BORDER_COLOR);

        // Hide ID column
        searchTable.getColumnModel().getColumn(0).setMinWidth(0);
        searchTable.getColumnModel().getColumn(0).setMaxWidth(0);
        searchTable.getColumnModel().getColumn(0).setWidth(0);

        // Stock column coloring
        searchTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                                                            boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(RIGHT);
                try {
                    int stock = Integer.parseInt(v.toString());
                    if (stock <= 0) { setForeground(UIConstants.ERROR_COLOR); setFont(UIConstants.FONT_BOLD); }
                    else if (stock <= 10) { setForeground(UIConstants.WARNING_ORANGE); setFont(UIConstants.FONT_BOLD); }
                    else { setForeground(UIConstants.SUCCESS_GREEN); setFont(UIConstants.FONT_LABEL); }
                } catch (NumberFormatException ignored) {}
                return this;
            }
        });

        // Double click or select to fill form
        searchTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) onSelectMedicine();
            }
        });
        searchTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onMedicineHighlighted();
        });

        JScrollPane scroll = new JScrollPane(searchTable);
        scroll.setPreferredSize(new Dimension(0, 160));
        panel.add(scroll, BorderLayout.CENTER);

        // Load all medicines initially
        searchMedicines("");

        return panel;
    }

    private JPanel createMedicineForm() {
        RoundedPanel form = new RoundedPanel(12);
        form.setBackground(UIConstants.CARD_BG);
        form.setLayout(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        form.setPreferredSize(new Dimension(340, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        JLabel formTitle = new JLabel("Thêm thuốc vào đơn");
        formTitle.setFont(UIConstants.FONT_SUBTITLE);
        formTitle.setForeground(UIConstants.PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(formTitle, gbc);
        gbc.gridwidth = 1;

        // Medicine info
        lblMedicineInfo = new JLabel("Chọn thuốc từ danh sách bên trái");
        lblMedicineInfo.setFont(UIConstants.FONT_CAPTION);
        lblMedicineInfo.setForeground(UIConstants.TEXT_SECONDARY);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        form.add(lblMedicineInfo, gbc);
        gbc.gridwidth = 1;

        // Dosage
        gbc.gridx = 0; gbc.gridy = 2;
        form.add(makeLabel("Liều dùng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtDosage = new JTextField("1 viên");
        txtDosage.setFont(UIConstants.FONT_BODY);
        form.add(txtDosage, gbc);
        gbc.weightx = 0;

        // Quantity
        gbc.gridx = 0; gbc.gridy = 3;
        form.add(makeLabel("Số lượng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtQuantity = new JTextField("10");
        txtQuantity.setFont(UIConstants.FONT_BODY);
        form.add(txtQuantity, gbc);
        gbc.weightx = 0;

        // Frequency
        gbc.gridx = 0; gbc.gridy = 4;
        form.add(makeLabel("Cách dùng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtFrequency = new JTextField("Ngày 3 lần, mỗi lần 1 viên");
        txtFrequency.setFont(UIConstants.FONT_BODY);
        form.add(txtFrequency, gbc);
        gbc.weightx = 0;

        // Duration
        gbc.gridx = 0; gbc.gridy = 5;
        form.add(makeLabel("Số ngày:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtDuration = new JTextField("7");
        txtDuration.setFont(UIConstants.FONT_BODY);
        form.add(txtDuration, gbc);
        gbc.weightx = 0;

        // Instruction
        gbc.gridx = 0; gbc.gridy = 6;
        form.add(makeLabel("Hướng dẫn:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cbInstruction = new JComboBox<>(new String[]{
                "Uống sau ăn", "Uống trước ăn", "Uống lúc đói",
                "Uống với nhiều nước", "Ngậm dưới lưỡi", "Bôi ngoài da",
                "Tiêm bắp", "Tiêm tĩnh mạch", "Nhỏ mắt", "Khác"
        });
        cbInstruction.setFont(UIConstants.FONT_BODY);
        cbInstruction.setEditable(true);
        form.add(cbInstruction, gbc);
        gbc.weightx = 0;

        // Add button
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(12, 4, 4, 4);
        RoundedButton btnAdd = new RoundedButton("+ Thêm vào đơn");
        btnAdd.setBackground(UIConstants.ACCENT_BLUE);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(180, 36));
        btnAdd.addActionListener(e -> onAddToPrescription());
        form.add(btnAdd, gbc);

        return form;
    }

    private JPanel createPrescriptionTable() {
        RoundedPanel panel = new RoundedPanel(12);
        panel.setBackground(UIConstants.CARD_BG);
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel title = new JLabel("📋 Đơn thuốc hiện tại");
        title.setFont(UIConstants.FONT_SUBTITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Tên thuốc", "Đơn vị", "Liều dùng", "Số lượng", "Cách dùng", "Số ngày", "Hướng dẫn", "Đơn giá", "Thành tiền"};
        prescModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 2 || c == 3 || c == 4 || c == 5 || c == 6; // dosage, qty, frequency, duration, instruction
            }
        };
        prescTable = new JTable(prescModel);
        prescTable.setFont(UIConstants.FONT_LABEL);
        prescTable.setRowHeight(34);
        prescTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        prescTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        prescTable.setGridColor(UIConstants.BORDER_COLOR);

        TableColumnModel cm = prescTable.getColumnModel();
        cm.getColumn(0).setPreferredWidth(160);
        cm.getColumn(1).setPreferredWidth(60);
        cm.getColumn(2).setPreferredWidth(80);
        cm.getColumn(3).setPreferredWidth(60);
        cm.getColumn(4).setPreferredWidth(160);
        cm.getColumn(5).setPreferredWidth(60);
        cm.getColumn(6).setPreferredWidth(110);
        cm.getColumn(7).setPreferredWidth(90);
        cm.getColumn(8).setPreferredWidth(100);

        // Update model when cell edited
        prescModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (row >= 0 && row < prescriptionItems.size()) {
                    PrescriptionDetail item = prescriptionItems.get(row);
                    try {
                        if (col == 2) item.setDosage(prescModel.getValueAt(row, 2).toString());
                        if (col == 3) {
                            item.setQuantity(Integer.parseInt(prescModel.getValueAt(row, 3).toString()));
                            prescModel.setValueAt(MONEY_FMT.format(item.getLineTotal()), row, 8);
                        }
                        if (col == 4) item.setFrequency(prescModel.getValueAt(row, 4).toString());
                        if (col == 5) item.setDuration(Integer.parseInt(prescModel.getValueAt(row, 5).toString()));
                        if (col == 6) item.setInstruction(prescModel.getValueAt(row, 6).toString());
                    } catch (NumberFormatException ignored) {}
                }
            }
        });

        panel.add(new JScrollPane(prescTable), BorderLayout.CENTER);

        // Remove button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        RoundedButton btnRemove = new RoundedButton("🗑 Xóa dòng");
        btnRemove.setBackground(UIConstants.ERROR_COLOR);
        btnRemove.setForeground(Color.WHITE);
        btnRemove.setPreferredSize(new Dimension(120, 32));
        btnRemove.addActionListener(e -> onRemoveItem());
        btnPanel.add(btnRemove);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIConstants.CARD_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR),
                new EmptyBorder(12, 20, 12, 20)));

        // Total amount
        JLabel lblTotal = new JLabel("Tổng cộng: 0 đ");
        lblTotal.setFont(UIConstants.FONT_HEADER);
        lblTotal.setForeground(UIConstants.PRIMARY);
        bar.add(lblTotal, BorderLayout.WEST);

        // Update total when table changes
        prescModel.addTableModelListener(e -> {
            double total = prescriptionItems.stream().mapToDouble(PrescriptionDetail::getLineTotal).sum();
            lblTotal.setText("Tổng cộng: " + MONEY_FMT.format(total));
        });

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);

        RoundedButton btnPrint = new RoundedButton("🖨 In đơn thuốc");
        btnPrint.setBackground(UIConstants.TEXT_SECONDARY);
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setPreferredSize(new Dimension(160, 38));
        btnPrint.addActionListener(e -> onPrintPrescription());
        btns.add(btnPrint);

        RoundedButton btnComplete = new RoundedButton("✅ Hoàn tất đơn thuốc");
        btnComplete.setBackground(UIConstants.SUCCESS_GREEN);
        btnComplete.setForeground(Color.WHITE);
        btnComplete.setPreferredSize(new Dimension(200, 38));
        btnComplete.addActionListener(e -> onCompletePrescription());
        btns.add(btnComplete);

        bar.add(btns, BorderLayout.EAST);
        return bar;
    }

    // ════════════════════════════════════════════════════════════
    //  SEARCH LOGIC
    // ════════════════════════════════════════════════════════════

    private javax.swing.Timer searchTimer;

    private void onSearchChanged() {
        // Debounce search
        if (searchTimer != null && searchTimer.isRunning()) searchTimer.stop();
        searchTimer = new javax.swing.Timer(300, e -> {
            String keyword = txtSearch.getText().trim();
            searchMedicines(keyword);
        });
        searchTimer.setRepeats(false);
        searchTimer.start();
    }

    private void searchMedicines(String keyword) {
        List<Medicine> medicines = (keyword == null || keyword.isEmpty())
                ? medicineBUS.findAll()
                : medicineBUS.findByName(keyword);
        searchModel.setRowCount(0);
        for (Medicine m : medicines) {
            searchModel.addRow(new Object[]{
                    m.getId(),
                    m.getMedicineName(),
                    m.getGenericName() != null ? m.getGenericName() : "",
                    m.getUnit(),
                    m.getDosageForm() != null ? m.getDosageForm() : "",
                    MONEY_FMT.format(m.getSellPrice()),
                    m.getStockQty()
            });
        }
    }

    private void onMedicineHighlighted() {
        int row = searchTable.getSelectedRow();
        if (row < 0) {
            selectedMedicine = null;
            lblMedicineInfo.setText("Chọn thuốc từ danh sách bên trái");
            return;
        }
        int medId = (int) searchModel.getValueAt(row, 0);
        selectedMedicine = medicineBUS.findAll().stream()
                .filter(m -> m.getId() == medId).findFirst().orElse(null);
        if (selectedMedicine != null) {
            lblMedicineInfo.setText(String.format("<html><b>%s</b> — %s | Giá: %s | Kho: %d %s</html>",
                    selectedMedicine.getMedicineName(),
                    selectedMedicine.getGenericName() != null ? selectedMedicine.getGenericName() : "",
                    MONEY_FMT.format(selectedMedicine.getSellPrice()),
                    selectedMedicine.getStockQty(),
                    selectedMedicine.getUnit()));
            lblMedicineInfo.setForeground(selectedMedicine.getStockQty() <= 0
                    ? UIConstants.ERROR_COLOR : UIConstants.TEXT_PRIMARY);
        }
    }

    private void onSelectMedicine() {
        onMedicineHighlighted();
        if (selectedMedicine != null) {
            txtDosage.requestFocus();
            txtDosage.selectAll();
        }
    }

    // ════════════════════════════════════════════════════════════
    //  ADD / REMOVE MEDICINE
    // ════════════════════════════════════════════════════════════

    private void onAddToPrescription() {
        if (selectedMedicine == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn thuốc từ danh sách tìm kiếm.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check duplicate
        for (PrescriptionDetail d : prescriptionItems) {
            if (d.getMedicineId() == selectedMedicine.getId()) {
                JOptionPane.showMessageDialog(this,
                        "Thuốc '" + selectedMedicine.getMedicineName() + "' đã có trong đơn.",
                        "Trùng thuốc", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Parse quantity
        int qty;
        try {
            qty = Integer.parseInt(txtQuantity.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng phải là số nguyên dương.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtQuantity.requestFocus();
            return;
        }

        // Parse duration
        int duration = 0;
        try {
            String durText = txtDuration.getText().trim();
            if (!durText.isEmpty()) duration = Integer.parseInt(durText);
        } catch (NumberFormatException ignored) {}

        // Check allergy
        if (patientId > 0) {
            try {
                List<String> allergyWarnings = prescriptionBUS.checkAllergies(patientId, List.of(selectedMedicine.getId()));
                if (!allergyWarnings.isEmpty()) {
                    showAllergyWarning(allergyWarnings);
                    int choice = JOptionPane.showConfirmDialog(this,
                            "⚠ CẢNH BÁO DỊ ỨNG:\n" + String.join("\n", allergyWarnings)
                                    + "\n\nBạn có muốn tiếp tục thêm thuốc?",
                            "CẢNH BÁO DỊ ỨNG", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choice != JOptionPane.YES_OPTION) return;
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Lỗi kiểm tra dị ứng", ex);
            }
        }

        // Check drug interactions with existing prescriptions
        if (!prescriptionItems.isEmpty()) {
            try {
                List<Integer> allMedIds = new ArrayList<>(prescriptionItems.stream()
                        .map(PrescriptionDetail::getMedicineId).toList());
                allMedIds.add(selectedMedicine.getId());
                List<String> interactionWarnings = prescriptionBUS.checkDrugInteractions(allMedIds);
                if (!interactionWarnings.isEmpty()) {
                    showInteractionWarning(interactionWarnings);
                    int choice = JOptionPane.showConfirmDialog(this,
                            "CẢNH BÁO TƯƠNG TÁC THUỐC:\n" + String.join("\n", interactionWarnings)
                                    + "\n\nBạn có muốn tiếp tục thêm thuốc?",
                            "CẢNH BÁO TƯƠNG TÁC", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choice != JOptionPane.YES_OPTION) return;
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Lỗi kiểm tra tương tác thuốc", ex);
            }
        }

        // Check stock
        if (selectedMedicine.getStockQty() < qty) {
            showStockWarning(selectedMedicine.getMedicineName(), qty, selectedMedicine.getStockQty());
            int choice = JOptionPane.showConfirmDialog(this,
                    "⚠ CẢNH BÁO TỒN KHO:\nThuốc '" + selectedMedicine.getMedicineName()
                            + "' — Cần: " + qty + ", Tồn kho: " + selectedMedicine.getStockQty()
                            + "\n\nBạn có muốn tiếp tục?",
                    "CẢNH BÁO TỒN KHO", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;
        }

        // Create PrescriptionDetail
        String dosage = txtDosage.getText().trim();
        String frequency = txtFrequency.getText().trim();
        String instruction = cbInstruction.getSelectedItem() != null
                ? cbInstruction.getSelectedItem().toString() : "";

        PrescriptionDetail detail = new PrescriptionDetail(
                selectedMedicine.getId(), qty, dosage, instruction, selectedMedicine.getSellPrice());
        detail.setMedicineName(selectedMedicine.getMedicineName());
        detail.setUnit(selectedMedicine.getUnit());
        detail.setFrequency(frequency);
        detail.setDuration(duration);
        detail.setTotalAmount(detail.getLineTotal());

        prescriptionItems.add(detail);
        prescModel.addRow(new Object[]{
                selectedMedicine.getMedicineName(),
                selectedMedicine.getUnit(),
                dosage,
                qty,
                frequency,
                duration > 0 ? String.valueOf(duration) : "",
                instruction,
                MONEY_FMT.format(selectedMedicine.getSellPrice()),
                MONEY_FMT.format(detail.getLineTotal())
        });

        // Clear form
        txtSearch.setText("");
        selectedMedicine = null;
        lblMedicineInfo.setText("Chọn thuốc từ danh sách bên trái");
        searchMedicines("");
    }

    private void onRemoveItem() {
        int row = prescTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng thuốc cần xóa.");
            return;
        }
        prescriptionItems.remove(row);
        prescModel.removeRow(row);
    }

    // ════════════════════════════════════════════════════════════
    //  COMPLETE & PRINT
    // ════════════════════════════════════════════════════════════

    private void onCompletePrescription() {
        if (examinationId <= 0 || patientId <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn bệnh nhân trước khi hoàn tất đơn thuốc.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (prescriptionItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Đơn thuốc trống. Vui lòng thêm thuốc.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Final allergy check
        List<Integer> medIds = prescriptionItems.stream().map(PrescriptionDetail::getMedicineId).toList();
        try {
            List<String> allergyWarnings = prescriptionBUS.checkAllergies(patientId, medIds);
            if (!allergyWarnings.isEmpty()) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "⚠ CẢNH BÁO DỊ ỨNG:\n" + String.join("\n", allergyWarnings)
                                + "\n\nTiếp tục lưu đơn thuốc?",
                        "CẢNH BÁO DỊ ỨNG", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) return;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Lỗi kiểm tra dị ứng", ex);
        }

        // Final drug interaction check
        try {
            List<String> interactionWarnings = prescriptionBUS.checkDrugInteractions(new ArrayList<>(medIds));
            if (!interactionWarnings.isEmpty()) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "CẢNH BÁO TƯƠNG TÁC THUỐC:\n" + String.join("\n", interactionWarnings)
                                + "\n\nTiếp tục lưu đơn thuốc?",
                        "CẢNH BÁO TƯƠNG TÁC", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) return;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Lỗi kiểm tra tương tác thuốc", ex);
        }

        try {
            long prescriptionId = prescriptionBUS.createPrescription(examinationId, prescriptionItems);
            JOptionPane.showMessageDialog(this,
                    "Đã hoàn tất đơn thuốc #" + prescriptionId + " thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            // Fire event
            EventBus.getInstance().publish(new PrescriptionCreatedEvent(prescriptionId));

        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Lỗi tạo đơn thuốc", ex);
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onPrintPrescription() {
        if (prescriptionItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Đơn thuốc trống, không có gì để in.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Build a formatted prescription text
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("              PHÒNG MẠCH TƯ\n");
        sb.append("              ĐƠN THUỐC\n");
        sb.append("═══════════════════════════════════════════════════\n\n");
        sb.append("Mã lần khám: ").append(examinationId).append("\n");
        sb.append("Mã bệnh nhân: ").append(patientId).append("\n\n");
        sb.append("─────────────────────────────────────────────────\n");

        double total = 0;
        for (int i = 0; i < prescriptionItems.size(); i++) {
            PrescriptionDetail d = prescriptionItems.get(i);
            sb.append(String.format("%d. %s\n", i + 1, d.getMedicineName()));
            sb.append(String.format("   Liều dùng: %s | SL: %d | %s\n",
                    d.getDosage() != null ? d.getDosage() : "N/A",
                    d.getQuantity(),
                    d.getFrequency() != null ? d.getFrequency() : ""));
            if (d.getDuration() > 0) sb.append(String.format("   Số ngày: %d ngày\n", d.getDuration()));
            if (d.getInstruction() != null && !d.getInstruction().isEmpty())
                sb.append(String.format("   HD: %s\n", d.getInstruction()));
            sb.append(String.format("   Đơn giá: %s → Thành tiền: %s\n",
                    MONEY_FMT.format(d.getUnitPrice()), MONEY_FMT.format(d.getLineTotal())));
            sb.append("\n");
            total += d.getLineTotal();
        }
        sb.append("─────────────────────────────────────────────────\n");
        sb.append(String.format("TỔNG CỘNG: %s\n", MONEY_FMT.format(total)));
        sb.append("═══════════════════════════════════════════════════\n");

        // Show in a dialog with monospaced font
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea.setEditable(false);
        textArea.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(550, 500));

        JOptionPane.showMessageDialog(this, scroll, "Xem trước đơn thuốc", JOptionPane.PLAIN_MESSAGE);
    }

    // ════════════════════════════════════════════════════════════
    //  WARNINGS
    // ════════════════════════════════════════════════════════════

    private void showAllergyWarning(List<String> warnings) {
        warningPanel.removeAll();
        for (String w : warnings) {
            JLabel lbl = new JLabel(w);
            lbl.setFont(UIConstants.FONT_BOLD);
            lbl.setForeground(Color.WHITE);
            lbl.setOpaque(true);
            lbl.setBackground(UIConstants.ERROR_COLOR);
            lbl.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            warningPanel.add(lbl);
            warningPanel.add(Box.createVerticalStrut(4));
        }
        warningPanel.revalidate();
        warningPanel.repaint();
    }

    private void showStockWarning(String medicineName, int needed, int available) {
        JLabel lbl = new JLabel(String.format("⚠ Tồn kho thấp: %s — Cần: %d, Có: %d", medicineName, needed, available));
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setForeground(UIConstants.WARNING_ORANGE);
        lbl.setOpaque(true);
        lbl.setBackground(UIConstants.ALERT_AMBER_BG);
        lbl.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        warningPanel.add(lbl);
        warningPanel.add(Box.createVerticalStrut(4));
        warningPanel.revalidate();
        warningPanel.repaint();
    }

    private void showInteractionWarning(List<String> warnings) {
        for (String w : warnings) {
            JLabel lbl = new JLabel("<html>" + w.replace("\n", "<br>") + "</html>");
            lbl.setFont(UIConstants.FONT_BOLD);
            lbl.setForeground(Color.WHITE);
            lbl.setOpaque(true);
            lbl.setBackground(new Color(142, 68, 173)); // Purple for interactions
            lbl.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            warningPanel.add(lbl);
            warningPanel.add(Box.createVerticalStrut(4));
        }
        warningPanel.revalidate();
        warningPanel.repaint();
    }

    // ════════════════════════════════════════════════════════════
    //  LOAD EXISTING
    // ════════════════════════════════════════════════════════════

    private void loadExistingPrescription() {
        if (examinationId <= 0) return;
        try {
            var prescriptions = prescriptionBUS.getByMedicalRecordId(examinationId);
            for (var p : prescriptions) {
                var details = prescriptionBUS.getDetails(p.getId());
                for (var d : details) {
                    prescriptionItems.add(d);
                    prescModel.addRow(new Object[]{
                            d.getMedicineName() != null ? d.getMedicineName() : "Thuốc #" + d.getMedicineId(),
                            d.getUnit() != null ? d.getUnit() : "",
                            d.getDosage(),
                            d.getQuantity(),
                            d.getFrequency() != null ? d.getFrequency() : "",
                            d.getDuration() > 0 ? String.valueOf(d.getDuration()) : "",
                            d.getInstruction(),
                            MONEY_FMT.format(d.getUnitPrice()),
                            MONEY_FMT.format(d.getLineTotal())
                    });
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Lỗi tải đơn thuốc hiện tại", ex);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        return lbl;
    }
}
