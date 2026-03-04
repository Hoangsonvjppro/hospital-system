package com.hospital.gui.panels;

import com.hospital.bus.QueueBUS;
import com.hospital.bus.event.EventBus;
import com.hospital.bus.event.PatientRegisteredEvent;
import com.hospital.bus.event.QueueUpdatedEvent;
import com.hospital.exception.BusinessException;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.model.QueueEntry;
import com.hospital.model.QueueEntry.Priority;
import com.hospital.model.QueueEntry.QueueStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel quản lý hàng đợi khám bệnh.
 * <p>
 * - Hiển thị bảng: STT | Họ tên | Loại | Ưu tiên | Trạng thái | Thời gian chờ
 * - Color coding: Đỏ = Cấp cứu, Vàng = Người già, Trắng = Bình thường
 * - Nút: Gọi bệnh nhân tiếp theo, Bỏ qua, Hủy
 * - Auto-refresh mỗi 10 giây hoặc khi có event
 */
public class QueuePanel extends JPanel {

    private final QueueBUS queueBUS = new QueueBUS();
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblWaiting;
    private JLabel lblInProgress;
    private JLabel lblTotal;
    private javax.swing.Timer autoRefreshTimer;

    // Map table row → queue entry ID
    private final java.util.List<Integer> queueIds = new java.util.ArrayList<>();

    public QueuePanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
        refreshQueue();
        subscribeEvents();
        startAutoRefresh();
    }

    private void initComponents() {
        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createActionPanel(), BorderLayout.SOUTH);
    }

    // ══════════════════════════════════════════════════════════
    //  HEADER: Tiêu đề + Thống kê
    // ══════════════════════════════════════════════════════════

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 8));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel("Hàng đợi khám bệnh");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        header.add(title, BorderLayout.WEST);

        // Stats panel
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        stats.setOpaque(false);

        lblWaiting = createStatLabel("Đang chờ: 0", UIConstants.STATUS_WAITING);
        lblInProgress = createStatLabel("Đang khám: 0", UIConstants.STATUS_EXAMINING);
        lblTotal = createStatLabel("Tổng: 0", UIConstants.TEXT_SECONDARY);

        stats.add(lblWaiting);
        stats.add(lblInProgress);
        stats.add(lblTotal);

        header.add(stats, BorderLayout.EAST);

        return header;
    }

    private JLabel createStatLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setForeground(color);
        return lbl;
    }

    // ══════════════════════════════════════════════════════════
    //  TABLE: Hàng đợi
    // ══════════════════════════════════════════════════════════

    private JPanel createTablePanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"STT", "Họ tên", "SĐT", "Ưu tiên", "Trạng thái", "Thời gian chờ", "Giờ đăng ký"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(UIConstants.FONT_LABEL);
        table.setRowHeight(44);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setFont(UIConstants.FONT_BOLD);
        header.setBackground(UIConstants.TABLE_HEADER_BG);
        header.setForeground(UIConstants.TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);

        // Column widths
        table.getColumnModel().getColumn(0).setMaxWidth(60);   // STT
        table.getColumnModel().getColumn(2).setMaxWidth(110);  // SĐT
        table.getColumnModel().getColumn(3).setMaxWidth(120);  // Ưu tiên
        table.getColumnModel().getColumn(4).setMaxWidth(110);  // Trạng thái
        table.getColumnModel().getColumn(5).setMaxWidth(120);  // Thời gian chờ
        table.getColumnModel().getColumn(6).setMaxWidth(100);  // Giờ đăng ký

        // Custom cell renderer for color coding
        table.setDefaultRenderer(Object.class, new QueueTableCellRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.CARD_BG);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ══════════════════════════════════════════════════════════
    //  ACTION BUTTONS
    // ══════════════════════════════════════════════════════════

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        panel.setOpaque(false);

        RoundedButton btnCallNext = new RoundedButton("Gọi bệnh nhân tiếp theo");
        btnCallNext.setColors(UIConstants.SUCCESS_GREEN, UIConstants.SUCCESS_GREEN_DARK);
        btnCallNext.setPreferredSize(new Dimension(220, 40));
        btnCallNext.addActionListener(e -> callNextPatient());

        RoundedButton btnSkip = new RoundedButton("Bỏ qua");
        btnSkip.setColors(UIConstants.STATUS_WAITING, UIConstants.WARNING_ORANGE);
        btnSkip.setPreferredSize(new Dimension(100, 40));
        btnSkip.addActionListener(e -> skipSelected());

        RoundedButton btnCancel = new RoundedButton("Hủy");
        btnCancel.setColors(UIConstants.STATUS_CANCEL, UIConstants.STATUS_CANCEL.darker());
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.addActionListener(e -> cancelSelected());

        RoundedButton btnRefresh = new RoundedButton("Làm mới");
        btnRefresh.setColors(UIConstants.ACCENT_BLUE, UIConstants.ACCENT_BLUE_DARK);
        btnRefresh.setPreferredSize(new Dimension(100, 40));
        btnRefresh.addActionListener(e -> refreshQueue());

        panel.add(btnCallNext);
        panel.add(btnSkip);
        panel.add(btnCancel);
        panel.add(btnRefresh);

        return panel;
    }

    // ══════════════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════════════

    /**
     * Gọi bệnh nhân tiếp theo (priority cao nhất, chờ lâu nhất).
     */
    private void callNextPatient() {
        try {
            QueueEntry next = queueBUS.callNextPatient();
            JOptionPane.showMessageDialog(this,
                    "Gọi bệnh nhân: " + next.getPatientName() + "\n" +
                    "Số thứ tự: " + next.getQueueNumber() + "\n" +
                    "Ưu tiên: " + next.getPriorityDisplay(),
                    "Gọi khám", JOptionPane.INFORMATION_MESSAGE);
            refreshQueue();
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Bỏ qua bệnh nhân đã chọn (đưa xuống cuối hàng đợi).
     */
    private void skipSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= queueIds.size()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn bệnh nhân cần bỏ qua.",
                    "Chú ý", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int queueId = queueIds.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bỏ qua bệnh nhân này và đưa xuống cuối hàng đợi?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            queueBUS.skipPatient(queueId);
            refreshQueue();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Hủy bệnh nhân đã chọn khỏi hàng đợi.
     */
    private void cancelSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= queueIds.size()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn bệnh nhân cần hủy.",
                    "Chú ý", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int queueId = queueIds.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Hủy bệnh nhân này khỏi hàng đợi?",
                "Xác nhận hủy", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            queueBUS.cancelQueueEntry(queueId);
            refreshQueue();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  DATA
    // ══════════════════════════════════════════════════════════

    /**
     * Làm mới danh sách hàng đợi từ DB.
     */
    private void refreshQueue() {
        try {
            List<QueueEntry> entries = queueBUS.getTodayQueue();
            populateTable(entries);
        } catch (Exception ex) {
            // Silent fail — log only
            java.util.logging.Logger.getLogger(QueuePanel.class.getName())
                    .log(java.util.logging.Level.WARNING, "Lỗi refresh hàng đợi", ex);
        }
    }

    private void populateTable(List<QueueEntry> entries) {
        tableModel.setRowCount(0);
        queueIds.clear();

        int waiting = 0, inProgress = 0;

        for (QueueEntry e : entries) {
            queueIds.add(e.getId());

            if (e.getStatus() == QueueStatus.WAITING) waiting++;
            if (e.getStatus() == QueueStatus.IN_PROGRESS) inProgress++;

            String regTime = e.getCreatedAt() != null
                    ? e.getCreatedAt().format(timeFmt) : "";

            tableModel.addRow(new Object[]{
                    e.getQueueNumber(),
                    e.getPatientName() != null ? e.getPatientName() : "BN#" + e.getPatientId(),
                    e.getPatientPhone() != null ? e.getPatientPhone() : "",
                    e.getPriorityDisplay(),
                    e.getStatusDisplay(),
                    e.getWaitingTimeDisplay(),
                    regTime
            });
        }

        // Update stats
        lblWaiting.setText("Đang chờ: " + waiting);
        lblInProgress.setText("Đang khám: " + inProgress);
        lblTotal.setText("Tổng: " + entries.size());
    }

    // ══════════════════════════════════════════════════════════
    //  EVENT SUBSCRIPTION
    // ══════════════════════════════════════════════════════════

    private void subscribeEvents() {
        EventBus.getInstance().subscribe(QueueUpdatedEvent.class, event -> {
            refreshQueue();
        });
        EventBus.getInstance().subscribe(PatientRegisteredEvent.class, event -> {
            refreshQueue();
        });
    }

    // ══════════════════════════════════════════════════════════
    //  AUTO REFRESH (mỗi 10 giây)
    // ══════════════════════════════════════════════════════════

    private void startAutoRefresh() {
        autoRefreshTimer = new javax.swing.Timer(10_000, e -> refreshQueue());
        autoRefreshTimer.setInitialDelay(10_000);
        autoRefreshTimer.start();
    }

    // ══════════════════════════════════════════════════════════
    //  CUSTOM CELL RENDERER — Color coding
    // ══════════════════════════════════════════════════════════

    /**
     * Cell renderer với color coding theo ưu tiên:
     * - Đỏ nhạt = Cấp cứu
     * - Vàng nhạt = Người già
     * - Trắng = Bình thường
     */
    private class QueueTableCellRenderer extends DefaultTableCellRenderer {
        private static final Color BG_EMERGENCY = new Color(254, 226, 226); // Đỏ nhạt
        private static final Color BG_ELDERLY = new Color(254, 249, 195);   // Vàng nhạt
        private static final Color BG_NORMAL = Color.WHITE;
        private static final Color FG_EMERGENCY = new Color(153, 27, 27);
        private static final Color FG_ELDERLY = new Color(133, 100, 4);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            setBorder(new EmptyBorder(0, 10, 0, 10));

            if (!isSelected && row < queueIds.size()) {
                // Lấy ưu tiên từ cột "Ưu tiên" (index 3)
                Object priorityObj = tableModel.getValueAt(row, 3);
                String priorityStr = priorityObj != null ? priorityObj.toString() : "";

                if (priorityStr.equals(Priority.EMERGENCY.getDisplayName())) {
                    c.setBackground(BG_EMERGENCY);
                    c.setForeground(FG_EMERGENCY);
                } else if (priorityStr.equals(Priority.ELDERLY.getDisplayName())) {
                    c.setBackground(BG_ELDERLY);
                    c.setForeground(FG_ELDERLY);
                } else {
                    c.setBackground(BG_NORMAL);
                    c.setForeground(UIConstants.TEXT_PRIMARY);
                }
            } else if (isSelected) {
                c.setBackground(UIConstants.PRIMARY_BG_SOFT);
                c.setForeground(UIConstants.TEXT_PRIMARY);
            }

            return c;
        }
    }
}
