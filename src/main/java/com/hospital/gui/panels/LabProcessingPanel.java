package com.hospital.gui.panels;

import com.hospital.bus.LabOrderBUS;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.model.LabOrder;
import com.hospital.model.LabOrder.LabStatus;
import com.hospital.util.AsyncTask;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel xử lý xét nghiệm — dành cho nhân viên phòng xét nghiệm (Lab).
 * <p>
 * Hiển thị danh sách XN đang chờ (PENDING, IN_PROGRESS).
 * Cho phép:
 * - Chọn XN → Bắt đầu thực hiện (PENDING → IN_PROGRESS)
 * - Nhập kết quả (textarea)
 * - Hoàn tất XN (→ COMPLETED, fire LabResultReadyEvent)
 */
public class LabProcessingPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(LabProcessingPanel.class.getName());
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final LabOrderBUS labOrderBUS = new LabOrderBUS();

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextArea txtResult;
    private JLabel lblSelectedInfo;
    private RoundedButton btnStart;
    private RoundedButton btnComplete;
    private List<LabOrder> currentList;

    public LabProcessingPanel() {
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
        add(createMainContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("  Xử lý xét nghiệm");
        lblTitle.setIcon(com.hospital.gui.IconManager.getIcon("lab", 20, 20));
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(lblTitle, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        RoundedButton btnShowAll = new RoundedButton("Tất cả");
        btnShowAll.setIcon(com.hospital.gui.IconManager.getIcon("clipboard", 14, 14));
        btnShowAll.setBackground(UIConstants.TEXT_SECONDARY);
        btnShowAll.setForeground(Color.WHITE);
        btnShowAll.addActionListener(e -> loadAllData());
        buttons.add(btnShowAll);

        RoundedButton btnRefresh = new RoundedButton("Làm mới");
        btnRefresh.setIcon(com.hospital.gui.IconManager.getIcon("refresh", 14, 14));
        btnRefresh.setBackground(UIConstants.ACCENT_BLUE);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> loadData());
        buttons.add(btnRefresh);

        header.add(buttons, BorderLayout.EAST);
        return header;
    }

    private JPanel createMainContent() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(280);
        split.setOpaque(false);
        split.setBorder(null);

        split.setTopComponent(createTablePanel());
        split.setBottomComponent(createResultPanel());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(split, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createTablePanel() {
        String[] cols = {"ID", "Bệnh nhân", "Loại XN", "Tên xét nghiệm", "Trạng thái", "Bác sĩ chỉ định", "Ngày yêu cầu", "Ghi chú"};
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
        cm.getColumn(1).setPreferredWidth(140);
        cm.getColumn(2).setPreferredWidth(120);
        cm.getColumn(3).setPreferredWidth(180);
        cm.getColumn(4).setPreferredWidth(100);
        cm.getColumn(5).setPreferredWidth(130);
        cm.getColumn(6).setPreferredWidth(130);
        cm.getColumn(7).setPreferredWidth(150);

        // Status column with color
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                                                            boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(CENTER);
                String text = v != null ? v.toString() : "";
                if (text.contains("Chờ")) {
                    setForeground(UIConstants.WARNING_ORANGE);
                    setFont(UIConstants.FONT_BOLD);
                } else if (text.contains("Đang")) {
                    setForeground(UIConstants.STATUS_EXAMINING);
                    setFont(UIConstants.FONT_BOLD);
                } else if (text.contains("Hoàn")) {
                    setForeground(UIConstants.SUCCESS_GREEN);
                } else {
                    setForeground(UIConstants.TEXT_PRIMARY);
                }
                return this;
            }
        });

        // Selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onSelectionChanged();
        });

        RoundedPanel wrapper = new RoundedPanel(14);
        wrapper.setLayout(new BorderLayout());
        wrapper.setBackground(UIConstants.CARD_BG);

        JLabel tableTitle = new JLabel("  Danh sách yêu cầu xét nghiệm đang chờ xử lý");
        tableTitle.setFont(UIConstants.FONT_SUBTITLE);
        tableTitle.setForeground(UIConstants.TEXT_PRIMARY);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(12, 8, 8, 0));
        wrapper.add(tableTitle, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createResultPanel() {
        RoundedPanel panel = new RoundedPanel(14);
        panel.setBackground(UIConstants.CARD_BG);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Info label
        lblSelectedInfo = new JLabel("— Chọn một phiếu xét nghiệm để xử lý —");
        lblSelectedInfo.setFont(UIConstants.FONT_SUBTITLE);
        lblSelectedInfo.setForeground(UIConstants.PRIMARY);
        panel.add(lblSelectedInfo, BorderLayout.NORTH);

        // Result textarea
        JPanel centerPanel = new JPanel(new BorderLayout(0, 6));
        centerPanel.setOpaque(false);

        JLabel lblResult = new JLabel("Kết quả xét nghiệm:");
        lblResult.setFont(UIConstants.FONT_LABEL);
        lblResult.setForeground(UIConstants.TEXT_SECONDARY);
        centerPanel.add(lblResult, BorderLayout.NORTH);

        txtResult = new JTextArea(6, 0);
        txtResult.setFont(UIConstants.FONT_BODY);
        txtResult.setLineWrap(true);
        txtResult.setWrapStyleWord(true);
        txtResult.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        txtResult.setEnabled(false);
        centerPanel.add(new JScrollPane(txtResult), BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Action buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);

        btnStart = new RoundedButton("▶ Bắt đầu XN");
        btnStart.setBackground(UIConstants.ACCENT_BLUE);
        btnStart.setForeground(Color.WHITE);
        btnStart.setPreferredSize(new Dimension(140, 36));
        btnStart.setEnabled(false);
        btnStart.addActionListener(e -> onStartOrder());
        btns.add(btnStart);

        btnComplete = new RoundedButton("Hoàn tất XN");
        btnComplete.setIcon(com.hospital.gui.IconManager.getIcon("check", 14, 14));
        btnComplete.setBackground(UIConstants.SUCCESS_GREEN);
        btnComplete.setForeground(Color.WHITE);
        btnComplete.setPreferredSize(new Dimension(150, 36));
        btnComplete.setEnabled(false);
        btnComplete.addActionListener(e -> onCompleteOrder());
        btns.add(btnComplete);

        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    // ════════════════════════════════════════════════════════════
    //  DATA
    // ════════════════════════════════════════════════════════════

    private void loadData() {
        AsyncTask.run(
            () -> labOrderBUS.getPendingOrders(),
            list -> { currentList = list; refreshTable(list); },
            ex -> {
                LOGGER.log(Level.SEVERE, "Lỗi tải dữ liệu xét nghiệm", ex);
                JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        );
    }

    private void loadAllData() {
        AsyncTask.run(
            () -> labOrderBUS.findAll(),
            list -> { currentList = list; refreshTable(list); },
            ex -> {
                LOGGER.log(Level.SEVERE, "Lỗi tải dữ liệu xét nghiệm", ex);
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
                    o.getPatientName() != null ? o.getPatientName() : "BN #" + o.getPatientId(),
                    o.getTestType() != null ? o.getTestType().getDisplayName() : "",
                    o.getTestName(),
                    o.getStatus() != null ? o.getStatus().getDisplayName() : "",
                    o.getDoctorName() != null ? o.getDoctorName() : "BS #" + o.getOrderedBy(),
                    o.getOrderedAt() != null ? o.getOrderedAt().format(DTF) : "",
                    o.getNotes() != null ? o.getNotes() : ""
            });
        }
        resetResultPanel();
    }

    // ════════════════════════════════════════════════════════════
    //  ACTIONS
    // ════════════════════════════════════════════════════════════

    private void onSelectionChanged() {
        int row = table.getSelectedRow();
        if (row < 0 || currentList == null || row >= currentList.size()) {
            resetResultPanel();
            return;
        }

        LabOrder order = currentList.get(row);
        lblSelectedInfo.setText(String.format("#%d — %s — %s (%s)",
                order.getId(), order.getTestName(),
                order.getPatientName() != null ? order.getPatientName() : "BN #" + order.getPatientId(),
                order.getStatus().getDisplayName()));

        txtResult.setEnabled(true);
        txtResult.setText(order.getResult() != null ? order.getResult() : "");

        // Enable/disable buttons based on status
        boolean isPending = order.getStatus() == LabStatus.PENDING;
        boolean isInProgress = order.getStatus() == LabStatus.IN_PROGRESS;
        boolean isCompleted = order.getStatus() == LabStatus.COMPLETED;

        btnStart.setEnabled(isPending);
        btnComplete.setEnabled(isPending || isInProgress);
        txtResult.setEnabled(!isCompleted);
    }

    private void onStartOrder() {
        int row = table.getSelectedRow();
        if (row < 0 || currentList == null || row >= currentList.size()) return;

        LabOrder order = currentList.get(row);
        try {
            labOrderBUS.startOrder(order.getId());
            JOptionPane.showMessageDialog(this,
                    "Đã bắt đầu xét nghiệm: " + order.getTestName(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Lỗi bắt đầu XN", ex);
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCompleteOrder() {
        int row = table.getSelectedRow();
        if (row < 0 || currentList == null || row >= currentList.size()) return;

        LabOrder order = currentList.get(row);
        String result = txtResult.getText().trim();

        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập kết quả xét nghiệm trước khi hoàn tất.",
                    "Thiếu kết quả", JOptionPane.WARNING_MESSAGE);
            txtResult.requestFocus();
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận hoàn tất xét nghiệm:\n" + order.getTestName() + "\n\nKết quả: " + result,
                "Xác nhận hoàn tất", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            labOrderBUS.completeOrder(order.getId(), result);
            JOptionPane.showMessageDialog(this,
                    "Đã hoàn tất xét nghiệm: " + order.getTestName() + "\nKết quả đã được gửi đến bác sĩ.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Lỗi hoàn tất XN", ex);
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetResultPanel() {
        lblSelectedInfo.setText("— Chọn một phiếu xét nghiệm để xử lý —");
        txtResult.setText("");
        txtResult.setEnabled(false);
        btnStart.setEnabled(false);
        btnComplete.setEnabled(false);
    }
}
