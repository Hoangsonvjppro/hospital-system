package com.hospital.gui.panels;

import com.hospital.bus.AppointmentBUS;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.model.Appointment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel Lịch hẹn – hiển thị lịch tuần dạng calendar grid.
 */
public class AppointmentPanel extends JPanel {

    private final AppointmentBUS apptBUS = new AppointmentBUS();

    /* ── Tuần hiện tại ──────────────────────────────────────────────── */
    private LocalDate weekStart; // luôn là Thứ 2

    /* ── UI ──────────────────────────────────────────────────────────── */
    private JLabel lblWeekRange;
    private CalendarGrid calendarGrid;
    private JPanel legendPanel;
    private JComboBox<String> cbDoctor;
    private JTextField txtSearch;

    /* ── Hằng số ─────────────────────────────────────────────────────── */
    private static final int START_HOUR = 7;
    private static final int END_HOUR   = 18;
    private static final int SLOT_COUNT = END_HOUR - START_HOUR;

    private static final Color CLR_CONFIRMED = new Color(52, 152, 219);
    private static final Color CLR_WAITING   = new Color(230, 126, 34);
    private static final Color CLR_DONE      = new Color(39, 174, 96);
    private static final Color CLR_CANCEL    = new Color(149, 165, 166);

    private static final String[] DAY_LABELS = {
        "THỨ 2", "THỨ 3", "THỨ 4", "THỨ 5", "THỨ 6", "THỨ 7", "CHỦ NHẬT"
    };
    private static final DateTimeFormatter DD_MM = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public AppointmentPanel() {
        LocalDate today = LocalDate.of(2026, 2, 20);
        weekStart = today.with(WeekFields.ISO.dayOfWeek(), 1);

        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
    }

    /* ════════════════════════════════════════════════════════════════════
       KHỞI TẠO GIAO DIỆN
       ════════════════════════════════════════════════════════════════════ */
    private void initComponents() {
        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(createHeaderBar(),  BorderLayout.NORTH);
        top.add(createFilterBar(),  BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        calendarGrid = new CalendarGrid();
        JScrollPane scroll = new JScrollPane(calendarGrid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(UIConstants.WHITE);
        add(scroll, BorderLayout.CENTER);

        legendPanel = createLegend();
        add(legendPanel, BorderLayout.SOUTH);

        refreshCalendar();
    }

    /* ── Header Bar ──────────────────────────────────────────────────── */
    private JPanel createHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel title = new JLabel("Lịch làm việc Tuần");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY_RED);
        left.add(title);

        for (String view : new String[]{"Ngày", "Tuần", "Tháng"}) {
            RoundedButton btn = new RoundedButton(view);
            btn.setPreferredSize(new Dimension(68, 30));
            btn.setFont(UIConstants.FONT_SMALL);
            if ("Tuần".equals(view)) {
                btn.setColors(UIConstants.PRIMARY_RED, UIConstants.PRIMARY_RED_DARK);
            } else {
                btn.setColors(UIConstants.BORDER_COLOR, UIConstants.BORDER_COLOR.darker());
                btn.setForeground(UIConstants.TEXT_SECONDARY);
            }
            left.add(btn);
        }
        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        txtSearch = new JTextField(14);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm...");
        txtSearch.setPreferredSize(new Dimension(160, 32));
        txtSearch.addActionListener(e -> refreshCalendar());
        right.add(txtSearch);

        RoundedButton btnSearch = new RoundedButton("Tìm");
        btnSearch.setPreferredSize(new Dimension(56, 32));
        btnSearch.addActionListener(e -> refreshCalendar());
        right.add(btnSearch);

        RoundedButton btnAdd = new RoundedButton("+ Thêm lịch hẹn");
        btnAdd.setPreferredSize(new Dimension(140, 32));
        right.add(btnAdd);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    /* ── Filter Bar ──────────────────────────────────────────────────── */
    private JPanel createFilterBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filters.setOpaque(false);

        cbDoctor = new JComboBox<>(new String[]{"Tất cả bác sĩ", "Dr. Lê Văn C", "Dr. Trần Thị D",
            "Dr. Phạm Thị F", "Dr. Hoàng Văn G", "Dr. Nguyễn Văn E"});
        cbDoctor.setFont(UIConstants.FONT_LABEL);
        cbDoctor.setPreferredSize(new Dimension(170, 32));
        cbDoctor.addActionListener(e -> refreshCalendar());
        filters.add(cbDoctor);

        JComboBox<String> cbRoom = new JComboBox<>(new String[]{"Tất cả phòng khám", "Phòng 101", "Phòng 102", "Phòng 103"});
        cbRoom.setFont(UIConstants.FONT_LABEL);
        cbRoom.setPreferredSize(new Dimension(160, 32));
        filters.add(cbRoom);

        bar.add(filters, BorderLayout.WEST);

        JPanel weekNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        weekNav.setOpaque(false);

        RoundedButton btnPrev = new RoundedButton("<");
        btnPrev.setPreferredSize(new Dimension(36, 30));
        btnPrev.setFont(UIConstants.FONT_BOLD);
        btnPrev.setColors(UIConstants.BORDER_COLOR, UIConstants.BORDER_COLOR.darker());
        btnPrev.setForeground(UIConstants.TEXT_PRIMARY);
        btnPrev.addActionListener(e -> { weekStart = weekStart.minusWeeks(1); refreshCalendar(); });
        weekNav.add(btnPrev);

        lblWeekRange = new JLabel();
        lblWeekRange.setFont(UIConstants.FONT_BOLD);
        lblWeekRange.setForeground(UIConstants.TEXT_PRIMARY);
        weekNav.add(lblWeekRange);

        RoundedButton btnNext = new RoundedButton(">");
        btnNext.setPreferredSize(new Dimension(36, 30));
        btnNext.setFont(UIConstants.FONT_BOLD);
        btnNext.setColors(UIConstants.BORDER_COLOR, UIConstants.BORDER_COLOR.darker());
        btnNext.setForeground(UIConstants.TEXT_PRIMARY);
        btnNext.addActionListener(e -> { weekStart = weekStart.plusWeeks(1); refreshCalendar(); });
        weekNav.add(btnNext);

        RoundedButton btnToday = new RoundedButton("Hôm nay");
        btnToday.setPreferredSize(new Dimension(80, 30));
        btnToday.setFont(UIConstants.FONT_SMALL);
        btnToday.addActionListener(e -> {
            LocalDate today = LocalDate.of(2026, 2, 20);
            weekStart = today.with(WeekFields.ISO.dayOfWeek(), 1);
            refreshCalendar();
        });
        weekNav.add(btnToday);

        bar.add(weekNav, BorderLayout.EAST);
        return bar;
    }

    /* ── Legend Bar ───────────────────────────────────────────────────── */
    private JPanel createLegend() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        addLegendItem(panel, CLR_CONFIRMED, "Đã xác nhận", 0);
        addLegendItem(panel, CLR_WAITING,   "Đang chờ",     0);
        addLegendItem(panel, CLR_DONE,      "Đã khám",      0);
        addLegendItem(panel, CLR_CANCEL,    "Đã hủy",       0);
        return panel;
    }

    private void addLegendItem(JPanel parent, Color color, String label, int count) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        item.setOpaque(false);

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(2, 2, 10, 10);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(14, 14));
        dot.setOpaque(false);
        item.add(dot);

        JLabel lbl = new JLabel(label + (count > 0 ? " (" + count + ")" : ""));
        lbl.setFont(UIConstants.FONT_SMALL);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        item.add(lbl);

        parent.add(item);
    }

    /* ════════════════════════════════════════════════════════════════════
       DỮ LIỆU & REFRESH
       ════════════════════════════════════════════════════════════════════ */
    private void refreshCalendar() {
        LocalDate weekEnd = weekStart.plusDays(6);
        lblWeekRange.setText("  " + weekStart.format(DD_MM) + " – " + weekEnd.format(DD_MM_YYYY) + "  ");

        List<Appointment> all = apptBUS.findAll();

        String docFilter = (String) cbDoctor.getSelectedItem();
        if (docFilter != null && !"Tất cả bác sĩ".equals(docFilter)) {
            all = all.stream().filter(a -> a.getDoctorName().equals(docFilter)).collect(Collectors.toList());
        }

        String kw = txtSearch.getText().trim().toLowerCase();
        if (!kw.isEmpty()) {
            all = all.stream().filter(a ->
                a.getPatientName().toLowerCase().contains(kw)
                || a.getAppointmentCode().toLowerCase().contains(kw)
                || a.getPatientPhone().contains(kw)
            ).collect(Collectors.toList());
        }

        Map<Integer, List<Appointment>> byDay = new HashMap<>();
        for (int i = 0; i < 7; i++) byDay.put(i, new ArrayList<>());

        for (Appointment a : all) {
            LocalDate aDate = parseDate(a.getDate());
            if (aDate == null) continue;
            if (!aDate.isBefore(weekStart) && !aDate.isAfter(weekEnd)) {
                int dayIdx = aDate.getDayOfWeek().getValue() - 1;
                byDay.get(dayIdx).add(a);
            }
        }

        calendarGrid.setData(byDay);
        updateLegendCounts(all, weekStart, weekEnd);
    }

    private void updateLegendCounts(List<Appointment> all, LocalDate ws, LocalDate we) {
        int confirmed = 0, waiting = 0, done = 0, cancel = 0;
        for (Appointment a : all) {
            LocalDate d = parseDate(a.getDate());
            if (d == null || d.isBefore(ws) || d.isAfter(we)) continue;
            switch (a.getStatus()) {
                case "Đã xác nhận": confirmed++; break;
                case "Mới":         waiting++;   break;
                case "Đã khám":     done++;      break;
                case "Hủy":         cancel++;    break;
            }
        }
        legendPanel.removeAll();
        addLegendItem(legendPanel, CLR_CONFIRMED, "Đã xác nhận", confirmed);
        addLegendItem(legendPanel, CLR_WAITING,   "Đang chờ",     waiting);
        addLegendItem(legendPanel, CLR_DONE,      "Đã khám",      done);
        addLegendItem(legendPanel, CLR_CANCEL,    "Đã hủy",       cancel);
        legendPanel.revalidate();
        legendPanel.repaint();
    }

    private LocalDate parseDate(String s) {
        try { return LocalDate.parse(s, DD_MM_YYYY); }
        catch (Exception e) { return null; }
    }

    private static Color statusColor(String status) {
        if (status == null) return CLR_WAITING;
        switch (status) {
            case "Đã xác nhận": return CLR_CONFIRMED;
            case "Mới":         return CLR_WAITING;
            case "Đã khám":     return CLR_DONE;
            case "Hủy":         return CLR_CANCEL;
            default:            return CLR_WAITING;
        }
    }

    /* ════════════════════════════════════════════════════════════════════
       CALENDAR GRID – Custom JPanel
       ════════════════════════════════════════════════════════════════════ */
    private class CalendarGrid extends JPanel {

        private static final int TIME_COL_W  = 60;
        private static final int ROW_H       = 70;
        private static final int HEADER_H    = 50;
        private static final int CARD_PAD    = 3;

        private Map<Integer, List<Appointment>> data = new HashMap<>();

        CalendarGrid() {
            setBackground(UIConstants.WHITE);
            setToolTipText("");

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    Appointment a = hitTest(e.getX(), e.getY());
                    if (a != null) showAppointmentPopup(a, e.getX(), e.getY());
                }
            });
        }

        void setData(Map<Integer, List<Appointment>> data) {
            this.data = data;
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            Container parent = getParent();
            int w = (parent != null) ? parent.getWidth() : 900;
            if (w < 700) w = 900;
            int h = HEADER_H + SLOT_COUNT * ROW_H + 20;
            return new Dimension(w, h);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int totalW = getWidth();
            int colW   = (totalW - TIME_COL_W) / 7;

            drawHeader(g2, colW);
            drawGrid(g2, colW);
            drawAppointments(g2, colW);

            g2.dispose();
        }

        private void drawHeader(Graphics2D g2, int colW) {
            g2.setColor(UIConstants.TABLE_HEADER_BG);
            g2.fillRect(0, 0, getWidth(), HEADER_H);

            g2.setColor(UIConstants.BORDER_COLOR);
            g2.drawLine(0, HEADER_H - 1, getWidth(), HEADER_H - 1);

            for (int i = 0; i < 7; i++) {
                int x = TIME_COL_W + i * colW;
                LocalDate day = weekStart.plusDays(i);
                boolean isToday = day.equals(LocalDate.of(2026, 2, 20));

                g2.setFont(UIConstants.FONT_BOLD);
                g2.setColor(isToday ? UIConstants.PRIMARY_RED : UIConstants.TEXT_PRIMARY);
                String dayLabel = DAY_LABELS[i];
                FontMetrics fm = g2.getFontMetrics();
                int labelW = fm.stringWidth(dayLabel);
                g2.drawString(dayLabel, x + (colW - labelW) / 2, 22);

                String dateNum = String.valueOf(day.getDayOfMonth());
                g2.setFont(UIConstants.FONT_LABEL);
                fm = g2.getFontMetrics();
                int numW = fm.stringWidth(dateNum);
                if (isToday) {
                    int cx = x + colW / 2;
                    int cy = 37;
                    g2.setColor(UIConstants.PRIMARY_RED);
                    g2.fillOval(cx - 12, cy - 11, 24, 24);
                    g2.setColor(Color.WHITE);
                    g2.drawString(dateNum, cx - numW / 2, cy + 4);
                } else {
                    g2.setColor(UIConstants.TEXT_SECONDARY);
                    g2.drawString(dateNum, x + (colW - numW) / 2, 40);
                }

                if (i > 0) {
                    g2.setColor(UIConstants.BORDER_COLOR);
                    g2.drawLine(x, 0, x, getHeight());
                }
            }
        }

        private void drawGrid(Graphics2D g2, int colW) {
            g2.setFont(UIConstants.FONT_SMALL);
            FontMetrics fm = g2.getFontMetrics();

            for (int slot = 0; slot <= SLOT_COUNT; slot++) {
                int y = HEADER_H + slot * ROW_H;

                g2.setColor(UIConstants.BORDER_COLOR);
                Stroke old = g2.getStroke();
                if (slot > 0) {
                    g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                            10f, new float[]{4f, 4f}, 0f));
                }
                g2.drawLine(TIME_COL_W, y, getWidth(), y);
                g2.setStroke(old);

                if (slot < SLOT_COUNT) {
                    String timeLabel = String.format("%02d:00", START_HOUR + slot);
                    g2.setColor(UIConstants.TEXT_SECONDARY);
                    int tw = fm.stringWidth(timeLabel);
                    g2.drawString(timeLabel, TIME_COL_W - tw - 6, y + 14);
                }
            }

            g2.setColor(UIConstants.BORDER_COLOR);
            g2.drawLine(TIME_COL_W, 0, TIME_COL_W, getHeight());
        }

        private void drawAppointments(Graphics2D g2, int colW) {
            for (int day = 0; day < 7; day++) {
                List<Appointment> dayList = data.getOrDefault(day, Collections.emptyList());
                int x = TIME_COL_W + day * colW;

                for (Appointment a : dayList) {
                    double startMin = timeToMinutes(a.getTime());
                    double endMin   = timeToMinutes(a.getEndTime());
                    if (endMin <= startMin) endMin = startMin + 60;

                    double startSlot = (startMin - START_HOUR * 60.0) / 60.0;
                    double endSlot   = (endMin   - START_HOUR * 60.0) / 60.0;

                    int y1 = HEADER_H + (int)(startSlot * ROW_H);
                    int y2 = HEADER_H + (int)(endSlot   * ROW_H);
                    int cardH = Math.max(y2 - y1, 28);

                    int cx = x + CARD_PAD + 1;
                    int cw = colW - CARD_PAD * 2 - 2;

                    Color accent = statusColor(a.getStatus());

                    Color bgColor = new Color(
                        Math.min(255, accent.getRed()   + (255 - accent.getRed())   * 85 / 100),
                        Math.min(255, accent.getGreen() + (255 - accent.getGreen()) * 85 / 100),
                        Math.min(255, accent.getBlue()  + (255 - accent.getBlue())  * 85 / 100)
                    );

                    RoundRectangle2D rect = new RoundRectangle2D.Float(cx, y1 + 1, cw, cardH - 2, 6, 6);
                    g2.setColor(bgColor);
                    g2.fill(rect);

                    g2.setColor(accent);
                    g2.fillRoundRect(cx, y1 + 1, 4, cardH - 2, 4, 4);

                    Shape oldClip = g2.getClip();
                    g2.setClip(cx + 6, y1 + 2, cw - 10, cardH - 4);

                    g2.setColor(accent.darker());
                    g2.setFont(new Font(UIConstants.FONT_NAME, Font.BOLD, 10));
                    String timeRange = a.getTime() + " – " + (a.getEndTime().isEmpty() ? "?" : a.getEndTime());
                    g2.drawString(timeRange, cx + 8, y1 + 14);

                    g2.setColor(UIConstants.TEXT_PRIMARY);
                    g2.setFont(new Font(UIConstants.FONT_NAME, Font.BOLD, 11));
                    g2.drawString(a.getPatientName(), cx + 8, y1 + 28);

                    if (cardH > 40) {
                        g2.setColor(UIConstants.TEXT_SECONDARY);
                        g2.setFont(new Font(UIConstants.FONT_NAME, Font.PLAIN, 10));
                        g2.drawString(a.getSpecialty(), cx + 8, y1 + 41);
                    }

                    g2.setClip(oldClip);
                }
            }
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            Appointment a = hitTest(e.getX(), e.getY());
            if (a != null) {
                return "<html><b>" + a.getPatientName() + "</b><br>"
                     + a.getTime() + " – " + a.getEndTime() + "<br>"
                     + "Bác sĩ: " + a.getDoctorName() + "<br>"
                     + "Trạng thái: " + a.getStatus()
                     + (a.getNote().isEmpty() ? "" : "<br>Ghi chú: " + a.getNote())
                     + "</html>";
            }
            return null;
        }

        private Appointment hitTest(int mx, int my) {
            int totalW = getWidth();
            int colW   = (totalW - TIME_COL_W) / 7;

            for (int day = 0; day < 7; day++) {
                int x = TIME_COL_W + day * colW;
                if (mx < x || mx > x + colW) continue;

                List<Appointment> dayList = data.getOrDefault(day, Collections.emptyList());
                for (Appointment a : dayList) {
                    double startMin = timeToMinutes(a.getTime());
                    double endMin   = timeToMinutes(a.getEndTime());
                    if (endMin <= startMin) endMin = startMin + 60;

                    double startSlot = (startMin - START_HOUR * 60.0) / 60.0;
                    double endSlot   = (endMin   - START_HOUR * 60.0) / 60.0;

                    int y1 = HEADER_H + (int)(startSlot * ROW_H);
                    int y2 = HEADER_H + (int)(endSlot   * ROW_H);
                    int cardH = Math.max(y2 - y1, 28);

                    if (my >= y1 && my <= y1 + cardH) return a;
                }
            }
            return null;
        }

        private void showAppointmentPopup(Appointment a, int px, int py) {
            JPopupMenu popup = new JPopupMenu();

            JMenuItem header = new JMenuItem(a.getPatientName() + " – " + a.getAppointmentCode());
            header.setFont(UIConstants.FONT_BOLD);
            header.setEnabled(false);
            popup.add(header);
            popup.addSeparator();

            popup.add(makeMenuItem("Thời gian: " + a.getTime() + " – " + a.getEndTime()));
            popup.add(makeMenuItem("Bác sĩ: " + a.getDoctorName()));
            popup.add(makeMenuItem("Loại khám: " + a.getSpecialty()));
            popup.add(makeMenuItem("Trạng thái: " + a.getStatus()));
            if (!a.getNote().isEmpty()) popup.add(makeMenuItem("Ghi chú: " + a.getNote()));
            popup.addSeparator();

            if ("Mới".equals(a.getStatus())) {
                JMenuItem confirm = new JMenuItem("✓  Xác nhận lịch hẹn");
                confirm.setForeground(CLR_CONFIRMED);
                confirm.addActionListener(e -> {
                    a.setStatus("Đã xác nhận"); apptBUS.update(a); refreshCalendar();
                });
                popup.add(confirm);
            }
            if (!"Hủy".equals(a.getStatus()) && !"Đã khám".equals(a.getStatus())) {
                JMenuItem cancelItem = new JMenuItem("✗  Hủy lịch hẹn");
                cancelItem.setForeground(new Color(192, 57, 43));
                cancelItem.addActionListener(e -> {
                    int ok = JOptionPane.showConfirmDialog(AppointmentPanel.this,
                        "Hủy lịch hẹn của " + a.getPatientName() + "?", "Xác nhận",
                        JOptionPane.YES_NO_OPTION);
                    if (ok == JOptionPane.YES_OPTION) {
                        a.setStatus("Hủy"); apptBUS.update(a); refreshCalendar();
                    }
                });
                popup.add(cancelItem);
            }

            popup.show(this, px, py);
        }

        private JMenuItem makeMenuItem(String text) {
            JMenuItem item = new JMenuItem(text);
            item.setFont(UIConstants.FONT_LABEL);
            item.setEnabled(false);
            return item;
        }
    }

    private static double timeToMinutes(String hhmm) {
        if (hhmm == null || hhmm.isEmpty()) return 0;
        try {
            String[] parts = hhmm.split(":");
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return 0;
        }
    }
}
