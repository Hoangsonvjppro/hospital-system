package com.hospital.gui.panels;

import com.hospital.bus.PatientBUS;
import com.hospital.bus.QueueBUS;
import com.hospital.exception.BusinessException;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.components.StatCard;
import com.hospital.gui.components.StatusBadge;
import com.hospital.model.Patient;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Trang chủ dành cho Bác sĩ – hiển thị hàng đợi bệnh nhân,
 * thống kê trong ngày và lịch hẹn sắp tới.
 */
public class DoctorDashboardPanel extends JPanel {

    private final PatientBUS     patientBUS     = new PatientBUS();
    private final QueueBUS       queueBUS       = new QueueBUS();

    private DefaultTableModel waitingModel;
    private JTable waitingTable;

    public DoctorDashboardPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
    }

    private void initComponents() {
        // ── Header ──────────────────────────────────────────────────────────
        add(createHeader(), BorderLayout.NORTH);

        // ── Body: Left (stats + queue) + Right (appointments) ───────────────
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JPanel leftCol = new JPanel(new BorderLayout(0, 16));
        leftCol.setOpaque(false);
        leftCol.add(createStatCards(), BorderLayout.NORTH);
        leftCol.add(createWaitingQueue(), BorderLayout.CENTER);

        JPanel rightCol = createRightPanel();
        rightCol.setPreferredSize(new Dimension(270, 0));

        body.add(leftCol,  BorderLayout.CENTER);
        body.add(rightCol, BorderLayout.EAST);
        add(body, BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel titleLbl = new JLabel("Bảng điều khiển – Bác sĩ");
        titleLbl.setFont(UIConstants.FONT_TITLE);
        titleLbl.setForeground(UIConstants.PRIMARY_RED);
        JLabel subLbl = new JLabel("Hôm nay, ngày 20 Tháng 02, 2026");
        subLbl.setFont(UIConstants.FONT_SMALL);
        subLbl.setForeground(UIConstants.TEXT_SECONDARY);
        titleBlock.add(titleLbl);
        titleBlock.add(subLbl);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        RoundedButton callNext = new RoundedButton("▶  Gọi bệnh nhân tiếp theo");
        callNext.setPreferredSize(new Dimension(210, 36));
        callNext.addActionListener(e -> callNextPatient());
        right.add(callNext);

        p.add(titleBlock, BorderLayout.WEST);
        p.add(right,      BorderLayout.EAST);
        return p;
    }

    // ── Stat Cards ────────────────────────────────────────────────────────────
    private JPanel createStatCards() {
        JPanel p = new JPanel(new GridLayout(1, 3, 14, 0));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(0, 100));

        long waiting = queueBUS.countByStatus("WAITING");
        long inProgress = queueBUS.countByStatus("EXAMINING");
        long done = queueBUS.countByStatus("COMPLETED");

        p.add(new StatCard("Chờ khám",    String.valueOf(waiting),
                "Bệnh nhân đang đợi", "hourglass", UIConstants.WARNING_ORANGE));
        p.add(new StatCard("Đang khám",   String.valueOf(inProgress),
                "Đang trong phòng khám", "stethoscope", UIConstants.PRIMARY_RED));
        p.add(new StatCard("Đã khám xong", String.valueOf(done),
                "Hôm nay", "check", UIConstants.SUCCESS_GREEN));
        return p;
    }

    // ── Waiting Queue ─────────────────────────────────────────────────────────
    private JPanel createWaitingQueue() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Title row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel title = new JLabel("Hàng đợi khám bệnh");
        title.setFont(UIConstants.FONT_SUBTITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        JLabel hint = new JLabel("Nhấn vào bệnh nhân để chọn, sau đó gọi vào khám");
        hint.setFont(UIConstants.FONT_SMALL);
        hint.setForeground(UIConstants.TEXT_SECONDARY);
        JPanel titleStack = new JPanel(new GridLayout(2, 1, 0, 2));
        titleStack.setOpaque(false);
        titleStack.add(title);
        titleStack.add(hint);

        RoundedButton refreshBtn = new RoundedButton("↻  Làm mới");
        refreshBtn.setColors(UIConstants.TEXT_SECONDARY, UIConstants.BORDER_COLOR);
        refreshBtn.setPreferredSize(new Dimension(110, 32));
        refreshBtn.addActionListener(e -> loadWaiting());

        titleRow.add(titleStack, BorderLayout.WEST);
        titleRow.add(refreshBtn, BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        // Table
        String[] cols = {"#", "Mã BN", "Họ và tên", "Giờ nhận", "Loại khám", "Trạng thái", "Hành động"};
        waitingModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };

        waitingTable = new JTable(waitingModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row))
                    c.setBackground(row % 2 == 0 ? UIConstants.WHITE : UIConstants.TABLE_ROW_ALT);
                return c;
            }
        };
        waitingTable.setRowHeight(44);
        waitingTable.setGridColor(UIConstants.BORDER_COLOR);
        waitingTable.setShowVerticalLines(false);
        waitingTable.setSelectionBackground(UIConstants.RED_BG_SOFT);
        waitingTable.setSelectionForeground(UIConstants.TEXT_PRIMARY);
        waitingTable.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = waitingTable.getTableHeader();
        header.setBackground(UIConstants.TABLE_HEADER_BG);
        header.setFont(UIConstants.FONT_BOLD);
        header.setReorderingAllowed(false);

        // Column widths
        int[] widths = {35, 75, 180, 90, 150, 110, 110};
        for (int i = 0; i < widths.length; i++)
            waitingTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Status renderer
        waitingTable.getColumnModel().getColumn(5).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean s, boolean f, int row, int col) {
                    StatusBadge badge = new StatusBadge(v == null ? "" : v.toString());
                    badge.setHorizontalAlignment(SwingConstants.CENTER);
                    return badge;
                }
            });

        // Action column - "Gọi vào" button renderer/editor
        waitingTable.getColumnModel().getColumn(6).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean s, boolean f, int row, int col) {
                    RoundedButton btn = new RoundedButton("Gọi vào");
                    btn.setFont(UIConstants.FONT_SMALL);
                    return btn;
                }
            });
        waitingTable.getColumnModel().getColumn(6).setCellEditor(
            new DefaultCellEditor(new JCheckBox()) {
                private RoundedButton btn;
                { btn = new RoundedButton("Gọi vào"); btn.setFont(UIConstants.FONT_SMALL);
                  btn.addActionListener(e -> stopCellEditing()); }
                @Override public Component getTableCellEditorComponent(
                        JTable t, Object v, boolean s, int r, int c) {
                    btn.addActionListener(ev -> callSelectedRow(r));
                    return btn;
                }
                @Override public Object getCellEditorValue() { return ""; }
            });

        // Center align
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[]{0, 1, 3}) waitingTable.getColumnModel().getColumn(i).setCellRenderer(center);

        loadWaiting();
        JScrollPane scroll = new JScrollPane(waitingTable);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR));
        scroll.getViewport().setBackground(UIConstants.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ── Right panel: quick notes ───────────────────────────────
    private JPanel createRightPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);

        // Quick notes card
        RoundedPanel notesCard = new RoundedPanel(UIConstants.CARD_RADIUS);
        notesCard.setBackground(UIConstants.CARD_BG);
        notesCard.setLayout(new BorderLayout(0, 8));
        notesCard.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel notesTitle = new JLabel("Ghi chú nhanh");
        notesTitle.setFont(UIConstants.FONT_SUBTITLE);
        notesTitle.setForeground(UIConstants.TEXT_PRIMARY);
        notesCard.add(notesTitle, BorderLayout.NORTH);

        JTextArea notes = new JTextArea();
        notes.setFont(UIConstants.FONT_LABEL);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.putClientProperty("JTextArea.placeholderText", "Ghi chú của bác sĩ...");
        notes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        notesCard.add(new JScrollPane(notes), BorderLayout.CENTER);

        p.add(notesCard,  BorderLayout.CENTER);
        return p;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void loadWaiting() {
        waitingModel.setRowCount(0);
        List<Patient> list = queueBUS.getWaitingPatients();
        int no = 1;
        for (Patient p : list) {
            waitingModel.addRow(new Object[]{
                no++, p.getPatientCode(), p.getFullName(),
                p.getArrivalTime(), p.getExamType(), p.getStatus(), "Gọi vào"
            });
        }
    }

    private void callSelectedRow(int row) {
        if (row >= 0 && row < waitingModel.getRowCount()) {
            String code = (String) waitingModel.getValueAt(row, 1);
            queueBUS.getWaitingPatients().stream()
                .filter(p -> p.getPatientCode().equals(code))
                .findFirst()
                .ifPresent(p -> {
                    try {
                        queueBUS.updateQueueStatus(p.getCurrentRecordId(), "EXAMINING");
                    } catch (BusinessException ex) {
                        JOptionPane.showMessageDialog(DoctorDashboardPanel.this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(DoctorDashboardPanel.this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                });
            loadWaiting();
            JOptionPane.showMessageDialog(this,
                "Đã gọi bệnh nhân vào phòng khám.", "Gọi khám",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void callNextPatient() {
        queueBUS.getPatientsByStatus("WAITING").stream()
            .findFirst()
            .ifPresentOrElse(p -> {
                try {
                    queueBUS.updateQueueStatus(p.getCurrentRecordId(), "EXAMINING");
                    loadWaiting();
                    JOptionPane.showMessageDialog(this,
                        "Đã gọi bệnh nhân: " + p.getFullName(), "Gọi khám",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (BusinessException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }, () -> JOptionPane.showMessageDialog(this,
                "Không còn bệnh nhân nào đang chờ.", "Thông báo",
                JOptionPane.INFORMATION_MESSAGE));
    }
}
