package com.hospital.gui.panels;

import com.hospital.bus.LabOrderBUS;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.model.LabOrder;
import com.hospital.model.LabOrder.LabStatus;
import com.hospital.model.LabOrder.TestType;
import com.hospital.util.AsyncTask;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel yêu cầu xét nghiệm — dành cho Bác sĩ.
 * <p>
 * Cho phép:
 * - Chọn loại xét nghiệm (dropdown)
 * - Nhập tên xét nghiệm cụ thể
 * - Tạo yêu cầu XN (status = PENDING)
 * - Xem danh sách XN đã yêu cầu và trạng thái
 */
public class LabOrderPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(LabOrderPanel.class.getName());
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final LabOrderBUS labOrderBUS = new LabOrderBUS();

    // Context - bác sĩ đang khám bệnh nhân nào
    private long examinationId;
    private long patientId;
    private long doctorId;

    // UI components
    private JComboBox<TestType> cbTestType;
    private JTextField txtTestName;
    private JTextArea txtNotes;
    private DefaultTableModel tableModel;
    private JTable table;

    /**
     * Constructor với context lần khám.
     *
     * @param examinationId ID bệnh án (MedicalRecord)
     * @param patientId     ID bệnh nhân
     * @param doctorId      ID bác sĩ đang khám
     */
    public LabOrderPanel(long examinationId, long patientId, long doctorId) {
        this.examinationId = examinationId;
        this.patientId = patientId;
        this.doctorId = doctorId;

        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
        loadData();
    }

    /**
     * Constructor mặc định (không có context — hiển thị tất cả).
     */
    public LabOrderPanel() {
        this(0, 0, 0);
    }

    /**
     * Cập nhật context khi bác sĩ chọn bệnh nhân khác.
     */
    public void setContext(long examinationId, long patientId, long doctorId) {
        this.examinationId = examinationId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        loadData();
    }
    /**
     * Refresh danh sách các yêu cầu xét nghiệm
     */
    public void refresh() {
        loadData();
    }
    // ════════════════════════════════════════════════════════════
    //  INIT UI
    // ════════════════════════════════════════════════════════════

    private void initComponents() {
        add(createHeader(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("🔬  Yêu cầu xét nghiệm");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(lblTitle, BorderLayout.WEST);

        RoundedButton btnRefresh = new RoundedButton("🔄 Làm mới");
        btnRefresh.setBackground(UIConstants.TEXT_SECONDARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> loadData());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(btnRefresh);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);

        center.add(createOrderForm(), BorderLayout.NORTH);
        center.add(createTablePanel(), BorderLayout.CENTER);

        return center;
    }

    private JPanel createOrderForm() {
        RoundedPanel form = new RoundedPanel(UIConstants.CARD_RADIUS);
        form.setBackground(UIConstants.CARD_BG);
        form.setLayout(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblFormTitle = new JLabel("Tạo yêu cầu xét nghiệm mới");
        lblFormTitle.setFont(UIConstants.FONT_SUBTITLE);
        lblFormTitle.setForeground(UIConstants.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        form.add(lblFormTitle, gbc);
        gbc.gridwidth = 1;

        // Loại xét nghiệm
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(createLabel("Loại XN *"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.4;
        cbTestType = new JComboBox<>(TestType.values());
        cbTestType.setFont(UIConstants.FONT_BODY);
        cbTestType.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TestType tt) {
                    setText(tt.getDisplayName());
                }
                return this;
            }
        });
        form.add(cbTestType, gbc);
        gbc.weightx = 0;

        // Tên xét nghiệm
        gbc.gridx = 2; gbc.gridy = 1;
        form.add(createLabel("Tên XN cụ thể *"), gbc);

        gbc.gridx = 3; gbc.weightx = 0.6;
        txtTestName = new JTextField(20);
        txtTestName.setFont(UIConstants.FONT_BODY);
        txtTestName.putClientProperty("JTextField.placeholderText", "VD: Công thức máu, X-quang ngực...");
        form.add(txtTestName, gbc);
        gbc.weightx = 0;

        // Ghi chú
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(createLabel("Ghi chú"), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        txtNotes = new JTextArea(2, 0);
        txtNotes.setFont(UIConstants.FONT_BODY);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        txtNotes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        JScrollPane notesScroll = new JScrollPane(txtNotes);
        notesScroll.setPreferredSize(new Dimension(0, 50));
        form.add(notesScroll, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Nút yêu cầu
        gbc.gridx = 3; gbc.gridy = 2; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTH;
        RoundedButton btnOrder = new RoundedButton("📋 Yêu cầu XN");
        btnOrder.setBackground(UIConstants.ACCENT_BLUE);
        btnOrder.setForeground(Color.WHITE);
        btnOrder.setPreferredSize(new Dimension(150, 36));
        btnOrder.addActionListener(e -> onCreateOrder());
        form.add(btnOrder, gbc);

        return form;
    }

    private JPanel createTablePanel() {
        String[] cols = {"ID", "Loại XN", "Tên xét nghiệm", "Trạng thái", "Kết quả", "Ngày yêu cầu", "Ngày hoàn tất", "Ghi chú"};
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

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(50);
        cm.getColumn(1).setPreferredWidth(120);
        cm.getColumn(2).setPreferredWidth(180);
        cm.getColumn(3).setPreferredWidth(100);
        cm.getColumn(4).setPreferredWidth(180);
        cm.getColumn(5).setPreferredWidth(130);
        cm.getColumn(6).setPreferredWidth(130);
        cm.getColumn(7).setPreferredWidth(150);

        // Status column renderer
        table.getColumnModel().getColumn(3).setCellRenderer(
                new javax.swing.table.DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable t, Object v,
                                                                    boolean s, boolean f, int r, int c) {
                        super.getTableCellRendererComponent(t, v, s, f, r, c);
                        setHorizontalAlignment(CENTER);
                        String text = v != null ? v.toString() : "";
                        if (text.contains("Chờ")) setForeground(UIConstants.WARNING_ORANGE);
                        else if (text.contains("Đang")) setForeground(UIConstants.STATUS_EXAMINING);
                        else if (text.contains("Hoàn")) setForeground(UIConstants.SUCCESS_GREEN);
                        else setForeground(UIConstants.TEXT_PRIMARY);
                        return this;
                    }
                });

        RoundedPanel wrapper = new RoundedPanel(14);
        wrapper.setLayout(new BorderLayout());
        wrapper.setBackground(UIConstants.CARD_BG);
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        return wrapper;
    }

    // ════════════════════════════════════════════════════════════
    //  DATA
    // ════════════════════════════════════════════════════════════

    public void loadData() {
        AsyncTask.run(
            () -> examinationId > 0 ? labOrderBUS.getByExaminationId(examinationId) : labOrderBUS.findAll(),
            this::refreshTable,
            ex -> {
                LOGGER.log(Level.SEVERE, "Lỗi tải dữ liệu phiếu xét nghiệm", ex);
                JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        );
    }

    private void refreshTable(List<LabOrder> list) {
        tableModel.setRowCount(0);
        for (LabOrder o : list) {
            tableModel.addRow(new Object[]{
                    o.getId(),
                    o.getTestType() != null ? o.getTestType().getDisplayName() : "",
                    o.getTestName(),
                    o.getStatus() != null ? o.getStatus().getDisplayName() : "",
                    o.getResult() != null ? o.getResult() : "—",
                    o.getOrderedAt() != null ? o.getOrderedAt().format(DTF) : "",
                    o.getCompletedAt() != null ? o.getCompletedAt().format(DTF) : "—",
                    o.getNotes() != null ? o.getNotes() : ""
            });
        }
    }

    // ════════════════════════════════════════════════════════════
    //  ACTIONS
    // ════════════════════════════════════════════════════════════

    private void onCreateOrder() {
        if (examinationId <= 0 || patientId <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn bệnh nhân và bắt đầu lần khám trước khi yêu cầu xét nghiệm.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TestType testType = (TestType) cbTestType.getSelectedItem();
        String testName = txtTestName.getText().trim();

        if (testName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên xét nghiệm cụ thể.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            txtTestName.requestFocus();
            return;
        }

        try {
            LabOrder order = new LabOrder(examinationId, patientId, testType, testName, doctorId);
            order.setNotes(txtNotes.getText().trim());

            long id = labOrderBUS.createLabOrder(order);
            if (id > 0) {
                JOptionPane.showMessageDialog(this,
                        "Đã tạo yêu cầu xét nghiệm #" + id + " thành công.\nLoại: " + testType.getDisplayName() + "\nTên: " + testName,
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                txtTestName.setText("");
                txtNotes.setText("");
                loadData();
            }
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Lỗi tạo phiếu xét nghiệm", ex);
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        return lbl;
    }

    /**
     * Kiểm tra xem có XN nào đang pending/in-progress cho lần khám này không.
     */
    public boolean hasPendingOrders() {
        if (examinationId <= 0) return false;
        List<LabOrder> orders = labOrderBUS.getByExaminationId(examinationId);
        return orders.stream().anyMatch(o -> o.getStatus() != LabStatus.COMPLETED);
    }

    /**
     * Kiểm tra xem có kết quả XN mới sẵn sàng không.
     */
    public boolean hasCompletedResults() {
        if (examinationId <= 0) return false;
        List<LabOrder> orders = labOrderBUS.getByExaminationId(examinationId);
        return orders.stream().anyMatch(o -> o.getStatus() == LabStatus.COMPLETED);
    }
}
