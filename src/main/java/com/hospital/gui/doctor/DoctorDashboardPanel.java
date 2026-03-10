package com.hospital.gui.doctor;

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
 * Doctor Dashboard — hàng đợi hôm nay + thống kê.
 */
public class DoctorDashboardPanel extends JPanel {

    private final QueueBUS queueBUS = new QueueBUS();
    private DefaultTableModel tableModel;
    private StatCard cardWaiting, cardExamining, cardDone;

    public DoctorDashboardPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        loadData();

        EventBus.getInstance().subscribe(QueueUpdatedEvent.class, e ->
                SwingUtilities.invokeLater(this::loadData));
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("📊 Dashboard Bác sĩ");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        // Stats cards
        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setOpaque(false);
        cards.setBorder(new EmptyBorder(16, 0, 0, 0));

        cardWaiting = new StatCard("Đang chờ", "0", "bệnh nhân", "🕐", UIConstants.STATUS_WAITING);
        cardExamining = new StatCard("Đang khám", "0", "bệnh nhân", "🩺", UIConstants.STATUS_EXAMINING);
        cardDone = new StatCard("Hoàn tất", "0", "hôm nay", "✅", UIConstants.STATUS_DONE);
        cards.add(cardWaiting);
        cards.add(cardExamining);
        cards.add(cardDone);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(cards, BorderLayout.CENTER);
        return top;
    }

    private JPanel createContent() {
        RoundedPanel panel = new RoundedPanel(UIConstants.CARD_RADIUS);
        panel.setBackground(UIConstants.CARD_BG);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Hàng đợi bệnh nhân");
        lblTitle.setFont(UIConstants.FONT_SUBTITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);

        tableModel = new DefaultTableModel(
                new String[]{"STT", "Tên BN", "Ưu tiên", "Trạng thái", "Thời gian"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(34);
        table.setFont(UIConstants.FONT_BODY);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionBar.setOpaque(false);

        RoundedButton btnCall = new RoundedButton("🔔 Gọi BN tiếp theo");
        btnCall.setPreferredSize(new Dimension(200, 40));
        btnCall.addActionListener(e -> callNext());

        RoundedButton btnRefresh = new RoundedButton("🔄 Làm mới");
        btnRefresh.setColors(UIConstants.TEXT_SECONDARY, UIConstants.TEXT_PRIMARY);
        btnRefresh.setPreferredSize(new Dimension(120, 40));
        btnRefresh.addActionListener(e -> loadData());

        actionBar.add(btnCall);
        actionBar.add(btnRefresh);

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actionBar, BorderLayout.SOUTH);
        return panel;
    }

    private void loadData() {
        try {
            List<QueueEntry> queue = queueBUS.getTodayQueue();
            tableModel.setRowCount(0);
            int waiting = 0, examining = 0, done = 0;

            for (QueueEntry entry : queue) {
                String priorityText = switch (entry.getPriority()) {
                    case EMERGENCY -> "🔴 Cấp cứu";
                    case ELDERLY -> "🟡 NCT";
                    case NORMAL -> "🟢 BT";
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
                        priorityText, statusText, time
                });

                switch (entry.getStatus()) {
                    case WAITING -> waiting++;
                    case IN_PROGRESS -> examining++;
                    case COMPLETED -> done++;
                    default -> {}
                }
            }

            cardWaiting.updateValue(String.valueOf(waiting));
            cardExamining.updateValue(String.valueOf(examining));
            cardDone.updateValue(String.valueOf(done));
        } catch (Exception ex) {
            // silently handle
        }
    }

    private void callNext() {
        try {
            QueueEntry next = queueBUS.callNextPatient();
            JOptionPane.showMessageDialog(this,
                    "Gọi BN: " + (next.getPatientName() != null ? next.getPatientName() : "#" + next.getQueueNumber()));
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }
}
