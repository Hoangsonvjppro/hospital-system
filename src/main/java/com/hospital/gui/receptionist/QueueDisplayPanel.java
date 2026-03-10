package com.hospital.gui.receptionist;

import com.hospital.bus.QueueBUS;
import com.hospital.bus.event.EventBus;
import com.hospital.bus.event.QueueUpdatedEvent;
import com.hospital.gui.common.*;
import com.hospital.model.QueueEntry;
import com.hospital.model.QueueEntry.QueueStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * ② Bảng hiển thị hàng đợi — gọi BN tiếp theo, bỏ qua, hủy.
 */
public class QueueDisplayPanel extends JPanel {

    private final QueueBUS queueBUS = new QueueBUS();
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblCurrentPatient;
    private JLabel lblStats;

    public QueueDisplayPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createActionBar(), BorderLayout.SOUTH);

        loadQueue();

        EventBus.getInstance().subscribe(QueueUpdatedEvent.class, e ->
                SwingUtilities.invokeLater(this::loadQueue));
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("📺 Bảng hàng đợi");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        lblStats = new JLabel();
        lblStats.setFont(UIConstants.FONT_BODY);
        lblStats.setForeground(UIConstants.TEXT_SECONDARY);

        header.add(title, BorderLayout.WEST);
        header.add(lblStats, BorderLayout.EAST);
        return header;
    }

    private JPanel createMainContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        // Current patient card
        RoundedPanel currentCard = new RoundedPanel(UIConstants.CARD_RADIUS);
        currentCard.setBackground(UIConstants.ACCENT_BLUE_SOFT);
        currentCard.setLayout(new BorderLayout());
        currentCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        currentCard.setPreferredSize(new Dimension(0, 80));

        lblCurrentPatient = new JLabel("Chưa có bệnh nhân đang khám");
        lblCurrentPatient.setFont(UIConstants.FONT_HEADER);
        lblCurrentPatient.setForeground(UIConstants.ACCENT_BLUE);
        currentCard.add(lblCurrentPatient, BorderLayout.CENTER);

        // Queue table
        tableModel = new DefaultTableModel(
                new String[]{"STT", "Tên BN", "SĐT", "Ưu tiên", "Trạng thái", "Thời gian"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(36);
        table.setFont(UIConstants.FONT_BODY);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(currentCard, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        bar.setOpaque(false);

        RoundedButton btnCallNext = new RoundedButton("🔔 Gọi BN tiếp theo");
        btnCallNext.setPreferredSize(new Dimension(200, 42));
        btnCallNext.addActionListener(e -> callNextPatient());

        RoundedButton btnSkip = new RoundedButton("⏭ Bỏ qua");
        btnSkip.setColors(UIConstants.WARNING_ORANGE, UIConstants.WARNING_ORANGE.darker());
        btnSkip.setPreferredSize(new Dimension(120, 42));
        btnSkip.addActionListener(e -> skipSelected());

        RoundedButton btnCancel = new RoundedButton("❌ Hủy");
        btnCancel.setColors(UIConstants.DANGER_RED, UIConstants.DANGER_RED_DARK);
        btnCancel.setPreferredSize(new Dimension(100, 42));
        btnCancel.addActionListener(e -> cancelSelected());

        RoundedButton btnRefresh = new RoundedButton("🔄 Làm mới");
        btnRefresh.setColors(UIConstants.TEXT_SECONDARY, UIConstants.TEXT_PRIMARY);
        btnRefresh.setPreferredSize(new Dimension(120, 42));
        btnRefresh.addActionListener(e -> loadQueue());

        bar.add(btnCallNext);
        bar.add(btnSkip);
        bar.add(btnCancel);
        bar.add(btnRefresh);
        return bar;
    }

    private void loadQueue() {
        try {
            List<QueueEntry> queue = queueBUS.getTodayQueue();
            tableModel.setRowCount(0);
            int waiting = 0, inProgress = 0, done = 0;
            QueueEntry current = null;

            for (QueueEntry entry : queue) {
                if (entry.getStatus() == QueueStatus.IN_PROGRESS && current == null) {
                    current = entry;
                }

                String priorityText = switch (entry.getPriority()) {
                    case EMERGENCY -> "🔴 Cấp cứu";
                    case ELDERLY -> "🟡 NCT";
                    case NORMAL -> "🟢 Bình thường";
                };
                String statusText = switch (entry.getStatus()) {
                    case WAITING -> "Chờ";
                    case IN_PROGRESS -> "Đang khám";
                    case COMPLETED -> "Hoàn tất";
                    case CANCELLED -> "Đã hủy";
                };
                String time = entry.getCreatedAt() != null ?
                        entry.getCreatedAt().toLocalTime().toString().substring(0, 5) : "";

                tableModel.addRow(new Object[]{
                        entry.getQueueNumber(),
                        entry.getPatientName() != null ? entry.getPatientName() : "BN#" + entry.getPatientId(),
                        entry.getPatientPhone() != null ? entry.getPatientPhone() : "",
                        priorityText,
                        statusText,
                        time
                });

                switch (entry.getStatus()) {
                    case WAITING -> waiting++;
                    case IN_PROGRESS -> inProgress++;
                    case COMPLETED -> done++;
                    default -> {}
                }
            }

            lblStats.setText("Chờ: " + waiting + "  |  Đang khám: " + inProgress + "  |  Hoàn tất: " + done);

            if (current != null) {
                lblCurrentPatient.setText("ĐANG KHÁM:  #" + current.getQueueNumber() + " — "
                        + (current.getPatientName() != null ? current.getPatientName() : "BN#" + current.getPatientId()));
            } else {
                lblCurrentPatient.setText("Chưa có bệnh nhân đang khám");
            }
        } catch (Exception ex) {
            lblStats.setText("Lỗi tải dữ liệu");
        }
    }

    private void callNextPatient() {
        try {
            QueueEntry next = queueBUS.callNextPatient();
            JOptionPane.showMessageDialog(this,
                    "Gọi BN: " + (next.getPatientName() != null ? next.getPatientName() : "#" + next.getQueueNumber()),
                    "Gọi bệnh nhân", JOptionPane.INFORMATION_MESSAGE);
            loadQueue();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void skipSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bệnh nhân cần bỏ qua.");
            return;
        }
        try {
            List<QueueEntry> queue = queueBUS.getTodayQueue();
            if (row < queue.size()) {
                queueBUS.skipPatient(queue.get(row).getId());
                loadQueue();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void cancelSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bệnh nhân cần hủy.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận hủy lượt khám?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            List<QueueEntry> queue = queueBUS.getTodayQueue();
            if (row < queue.size()) {
                queueBUS.cancelQueueEntry(queue.get(row).getId());
                loadQueue();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}
