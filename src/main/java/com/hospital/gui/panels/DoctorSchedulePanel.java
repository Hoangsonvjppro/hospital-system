package com.hospital.gui.panels;

import com.hospital.bus.AppointmentBUS;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.components.StatusBadge;
import com.hospital.model.Appointment;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Panel lịch khám bác sĩ — hiển thị lịch hẹn tuần theo bảng.
 * Dùng cho cả DoctorFrame và AdminFrame.
 */
public class DoctorSchedulePanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter FULL_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AppointmentBUS appointmentBUS = new AppointmentBUS();
    private LocalDate weekStart;
    private JLabel lblWeekRange;
    private JPanel scheduleGrid;

    public DoctorSchedulePanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        initComponents();
        loadSchedule();
    }

    private void initComponents() {
        add(createHeader(), BorderLayout.NORTH);

        scheduleGrid = new JPanel(new GridLayout(1, 7, 8, 0));
        scheduleGrid.setOpaque(false);

        JScrollPane scroll = new JScrollPane(scheduleGrid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("📅  Lịch khám trong tuần");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(lblTitle, BorderLayout.WEST);

        // Week navigation
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        nav.setOpaque(false);

        RoundedButton btnPrev = new RoundedButton("◀ Tuần trước");
        btnPrev.addActionListener(e -> { weekStart = weekStart.minusWeeks(1); loadSchedule(); });

        lblWeekRange = new JLabel();
        lblWeekRange.setFont(UIConstants.FONT_SUBTITLE);
        lblWeekRange.setForeground(UIConstants.TEXT_PRIMARY);

        RoundedButton btnNext = new RoundedButton("Tuần sau ▶");
        btnNext.addActionListener(e -> { weekStart = weekStart.plusWeeks(1); loadSchedule(); });

        RoundedButton btnToday = new RoundedButton("Hôm nay");
        btnToday.setBackground(UIConstants.PRIMARY);
        btnToday.setForeground(Color.WHITE);
        btnToday.addActionListener(e -> {
            weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            loadSchedule();
        });

        nav.add(btnPrev);
        nav.add(lblWeekRange);
        nav.add(btnNext);
        nav.add(Box.createHorizontalStrut(12));
        nav.add(btnToday);
        header.add(nav, BorderLayout.CENTER);

        return header;
    }

    private void loadSchedule() {
        LocalDate weekEnd = weekStart.plusDays(6);
        lblWeekRange.setText(weekStart.format(FULL_FMT) + "  —  " + weekEnd.format(FULL_FMT));

        // Get all appointments
        List<Appointment> all = appointmentBUS.findAll();

        // Group by date
        Map<LocalDate, List<Appointment>> byDate = all.stream()
                .filter(a -> a.getDate() != null)
                .filter(a -> !a.getDate().isBefore(weekStart) && !a.getDate().isAfter(weekEnd))
                .collect(Collectors.groupingBy(Appointment::getDate));

        scheduleGrid.removeAll();
        String[] dayNames = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            List<Appointment> dayAppts = byDate.getOrDefault(day, List.of());
            scheduleGrid.add(createDayColumn(dayNames[i], day, dayAppts));
        }

        scheduleGrid.revalidate();
        scheduleGrid.repaint();
    }

    private JPanel createDayColumn(String dayName, LocalDate date, List<Appointment> appointments) {
        RoundedPanel col = new RoundedPanel(UIConstants.CARD_RADIUS);
        col.setLayout(new BorderLayout(0, 4));
        col.setBackground(UIConstants.CARD_BG);
        col.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Day header
        JPanel dayHeader = new JPanel(new BorderLayout());
        dayHeader.setOpaque(false);

        boolean isToday = date.equals(LocalDate.now());
        JLabel lblDay = new JLabel(dayName + "  " + date.format(DATE_FMT), SwingConstants.CENTER);
        lblDay.setFont(isToday ? UIConstants.FONT_SUBTITLE : UIConstants.FONT_BOLD);
        lblDay.setForeground(isToday ? UIConstants.PRIMARY : UIConstants.TEXT_PRIMARY);
        if (isToday) {
            lblDay.setOpaque(true);
            lblDay.setBackground(UIConstants.PRIMARY_BG_SOFT);
            lblDay.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        }
        dayHeader.add(lblDay, BorderLayout.CENTER);

        JLabel lblCount = new JLabel(appointments.size() + " lịch hẹn", SwingConstants.CENTER);
        lblCount.setFont(UIConstants.FONT_SMALL);
        lblCount.setForeground(UIConstants.TEXT_SECONDARY);
        dayHeader.add(lblCount, BorderLayout.SOUTH);

        col.add(dayHeader, BorderLayout.NORTH);

        // Appointment cards
        JPanel cardList = new JPanel();
        cardList.setLayout(new BoxLayout(cardList, BoxLayout.Y_AXIS));
        cardList.setOpaque(false);

        if (appointments.isEmpty()) {
            JLabel lblEmpty = new JLabel("Không có lịch hẹn", SwingConstants.CENTER);
            lblEmpty.setFont(UIConstants.FONT_CAPTION);
            lblEmpty.setForeground(UIConstants.TEXT_MUTED);
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblEmpty.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            cardList.add(lblEmpty);
        } else {
            // Sort by time
            appointments.sort((a, b) -> {
                if (a.getTime() == null || b.getTime() == null) return 0;
                return a.getTime().compareTo(b.getTime());
            });
            for (Appointment a : appointments) {
                cardList.add(createAppointmentCard(a));
                cardList.add(Box.createVerticalStrut(4));
            }
        }

        JScrollPane scroll = new JScrollPane(cardList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        col.add(scroll, BorderLayout.CENTER);

        return col;
    }

    private JPanel createAppointmentCard(Appointment a) {
        JPanel card = new JPanel(new BorderLayout(4, 2));
        card.setBackground(UIConstants.FIELD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.FIELD_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // Time
        String timeStr = a.getFormattedTime() + " – " + a.getFormattedEndTime();
        JLabel lblTime = new JLabel(timeStr);
        lblTime.setFont(UIConstants.FONT_BOLD);
        lblTime.setForeground(UIConstants.ACCENT_BLUE);
        card.add(lblTime, BorderLayout.NORTH);

        // Patient + Doctor
        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        JLabel lblPatient = new JLabel("BN: " + a.getPatientName());
        lblPatient.setFont(UIConstants.FONT_CAPTION);
        lblPatient.setForeground(UIConstants.TEXT_PRIMARY);
        JLabel lblDoctor = new JLabel("BS: " + a.getDoctorName());
        lblDoctor.setFont(UIConstants.FONT_CAPTION);
        lblDoctor.setForeground(UIConstants.TEXT_SECONDARY);
        info.add(lblPatient);
        info.add(lblDoctor);
        card.add(info, BorderLayout.CENTER);

        // Status
        JLabel lblStatus = new StatusBadge(a.getStatus());
        card.add(lblStatus, BorderLayout.EAST);

        return card;
    }
}
