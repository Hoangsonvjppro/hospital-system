package com.hospital.gui.doctor;

import com.hospital.bus.ScheduleBUS;
import com.hospital.util.SessionManager;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.Schedule;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Quản lý lịch làm việc bác sĩ — xem/thêm/sửa/xóa.
 */
public class DoctorSchedulePanel extends JPanel {

    private final ScheduleBUS scheduleBUS = new ScheduleBUS();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtDate, txtStart, txtEnd, txtNotes;
    private JTextField txtFrom, txtTo; // date range filter
    private int selectedId = -1;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public DoctorSchedulePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createTablePanel(), createFormPanel());
        split.setDividerLocation(300);
        split.setResizeWeight(0.55);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        loadData();
    }

    /* ───── Header + Date range filter ───── */
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("📅 Lịch làm việc");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        LocalDate today = LocalDate.now();
        txtFrom = new JTextField(today.format(DATE_FMT), 10);
        txtTo = new JTextField(today.plusDays(30).format(DATE_FMT), 10);
        txtFrom.setFont(UIConstants.FONT_BODY);
        txtTo.setFont(UIConstants.FONT_BODY);

        RoundedButton btnFilter = new RoundedButton("🔍 Lọc");
        btnFilter.setBackground(UIConstants.PRIMARY);
        btnFilter.setForeground(Color.WHITE);
        btnFilter.addActionListener(e -> loadData());

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterBar.setOpaque(false);
        filterBar.add(new JLabel("Từ:"));
        filterBar.add(txtFrom);
        filterBar.add(new JLabel("Đến:"));
        filterBar.add(txtTo);
        filterBar.add(btnFilter);

        header.add(title, BorderLayout.WEST);
        header.add(filterBar, BorderLayout.EAST);
        return header;
    }

    /* ───── Table ───── */
    private JPanel createTablePanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 4));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Ngày", "Bắt đầu", "Kết thúc", "Ghi chú"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(UIConstants.FONT_BODY);
        table.getTableHeader().setFont(UIConstants.FONT_LABEL);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    /* ───── Form ───── */
    private JPanel createFormPanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(12, 8));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        txtDate = addTextField(fields, gbc, row++, "Ngày (dd/MM/yyyy):", 12);
        txtStart = addTextField(fields, gbc, row++, "Giờ BĐ (HH:mm):", 8);
        txtEnd = addTextField(fields, gbc, row++, "Giờ KT (HH:mm):", 8);
        txtNotes = addTextField(fields, gbc, row++, "Ghi chú:", 30);

        card.add(fields, BorderLayout.CENTER);
        card.add(createButtonBar(), BorderLayout.SOUTH);
        return card;
    }

    private JPanel createButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bar.setOpaque(false);

        RoundedButton btnNew = new RoundedButton("🆕 Mới");
        btnNew.setBackground(UIConstants.ACCENT_BLUE);
        btnNew.setForeground(Color.WHITE);
        btnNew.addActionListener(e -> clearForm());

        RoundedButton btnSave = new RoundedButton("💾 Lưu");
        btnSave.setBackground(UIConstants.SUCCESS_GREEN);
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> save());

        RoundedButton btnDelete = new RoundedButton("🗑 Xóa");
        btnDelete.setBackground(UIConstants.DANGER_RED);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> deleteSelected());

        bar.add(btnNew);
        bar.add(btnSave);
        bar.add(btnDelete);
        return bar;
    }

    /* ───── Data ───── */
    private long getCurrentDoctorId() {
        var account = SessionManager.getInstance().getCurrentUser();
        return account != null ? account.getId() : 0;
    }

    private void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                tableModel.setRowCount(0);
                LocalDate from = LocalDate.parse(txtFrom.getText().trim(), DATE_FMT);
                LocalDate to = LocalDate.parse(txtTo.getText().trim(), DATE_FMT);

                long doctorId = getCurrentDoctorId();
                List<Schedule> list = scheduleBUS.findByDoctorAndDateRange(doctorId, from, to);
                for (Schedule s : list) {
                    tableModel.addRow(new Object[]{
                            s.getId(),
                            s.getWorkDate() != null ? s.getWorkDate().format(DATE_FMT) : "",
                            s.getStartTime() != null ? s.getStartTime().format(TIME_FMT) : "",
                            s.getEndTime() != null ? s.getEndTime().format(TIME_FMT) : "",
                            s.getNotes() != null ? s.getNotes() : ""
                    });
                }
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Ngày không hợp lệ (dd/MM/yyyy)", "Lỗi", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tải lịch: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        txtDate.setText((String) tableModel.getValueAt(row, 1));
        txtStart.setText((String) tableModel.getValueAt(row, 2));
        txtEnd.setText((String) tableModel.getValueAt(row, 3));
        txtNotes.setText((String) tableModel.getValueAt(row, 4));
    }

    private void save() {
        try {
            Schedule s = new Schedule();
            s.setDoctorId(getCurrentDoctorId());

            try {
                s.setWorkDate(LocalDate.parse(txtDate.getText().trim(), DATE_FMT));
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Ngày không hợp lệ (dd/MM/yyyy)", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                s.setStartTime(LocalTime.parse(txtStart.getText().trim(), TIME_FMT));
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Giờ bắt đầu không hợp lệ (HH:mm)", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                s.setEndTime(LocalTime.parse(txtEnd.getText().trim(), TIME_FMT));
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Giờ kết thúc không hợp lệ (HH:mm)", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            s.setNotes(txtNotes.getText().trim());

            if (selectedId > 0) {
                s.setId(selectedId);
                scheduleBUS.update(s);
            } else {
                scheduleBUS.insert(s);
            }
            loadData();
            clearForm();
            JOptionPane.showMessageDialog(this, "Lưu lịch thành công!", "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        if (selectedId <= 0) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa lịch này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                scheduleBUS.delete(selectedId);
                loadData();
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtDate.setText("");
        txtStart.setText("");
        txtEnd.setText("");
        txtNotes.setText("");
        table.clearSelection();
    }

    /* ───── Helpers ───── */
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setPreferredSize(new Dimension(160, 28));
        return lbl;
    }

    private JTextField addTextField(JPanel form, GridBagConstraints gbc, int row, String label, int cols) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(createLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JTextField txt = new JTextField(cols);
        txt.setFont(UIConstants.FONT_BODY);
        form.add(txt, gbc);
        return txt;
    }
}
