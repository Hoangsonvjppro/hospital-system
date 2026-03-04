package com.hospital.gui.panels;

import com.hospital.bus.PatientBUS;
import com.hospital.bus.QueueBUS;
import com.hospital.bus.event.EventBus;
import com.hospital.bus.event.PatientRegisteredEvent;
import com.hospital.bus.event.QueueUpdatedEvent;
import com.hospital.dao.PatientDAO;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.dialogs.PatientRegistrationDialog;
import com.hospital.model.Patient;
import com.hospital.model.QueueEntry;
import com.hospital.model.QueueEntry.Priority;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Trang Tiếp nhận bệnh nhân.
 * <p>
 * - Không có BHYT
 * - Không thu phí khám (phí ở bước Thanh toán cuối cùng)
 * - Bảng danh sách bệnh nhân đã tiếp nhận trong ngày
 * - Nút: Đăng ký mới, Tìm kiếm, Chuyển vào hàng đợi
 */
public class ReceptionPanel extends JPanel {

    private final PatientBUS patientBUS = new PatientBUS();
    private final QueueBUS queueBUS = new QueueBUS();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private JLabel lblCount;

    // Store patient IDs for table row mapping
    private final java.util.List<Integer> patientIds = new java.util.ArrayList<>();

    public ReceptionPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
        loadTodayPatients();
        subscribeEvents();
    }

    private void initComponents() {
        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════
    //  HEADER: Tiêu đề + Nút hành động + Tìm kiếm
    // ══════════════════════════════════════════════════════════

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 12));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));

        // Title row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel title = new JLabel("Tiếp nhận bệnh nhân");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        titleRow.add(title, BorderLayout.WEST);

        // Action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        RoundedButton btnRegister = new RoundedButton("Đăng ký mới");
        btnRegister.addActionListener(e -> openRegistrationDialog());

        RoundedButton btnToQueue = new RoundedButton("Chuyển vào hàng đợi");
        btnToQueue.setColors(UIConstants.SUCCESS_GREEN, UIConstants.SUCCESS_GREEN_DARK);
        btnToQueue.addActionListener(e -> transferToQueue());

        actions.add(btnRegister);
        actions.add(btnToQueue);
        titleRow.add(actions, BorderLayout.EAST);

        header.add(titleRow, BorderLayout.NORTH);

        // Search bar + count
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên, SĐT hoặc CCCD...");
        txtSearch.setFont(UIConstants.FONT_BODY);
        txtSearch.setPreferredSize(new Dimension(0, 38));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(0, 14, 0, 14)));

        RoundedButton btnSearch = new RoundedButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100, 38));
        btnSearch.addActionListener(e -> searchPatients());

        txtSearch.addActionListener(e -> searchPatients());

        lblCount = new JLabel("0 bệnh nhân");
        lblCount.setFont(UIConstants.FONT_LABEL);
        lblCount.setForeground(UIConstants.TEXT_SECONDARY);

        JPanel searchLeft = new JPanel(new BorderLayout(8, 0));
        searchLeft.setOpaque(false);
        searchLeft.add(txtSearch, BorderLayout.CENTER);
        searchLeft.add(btnSearch, BorderLayout.EAST);

        searchRow.add(searchLeft, BorderLayout.CENTER);
        searchRow.add(lblCount, BorderLayout.EAST);

        header.add(searchRow, BorderLayout.SOUTH);

        return header;
    }

    // ══════════════════════════════════════════════════════════
    //  TABLE: Danh sách bệnh nhân đã tiếp nhận
    // ══════════════════════════════════════════════════════════

    private JPanel createTablePanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"STT", "Mã BN", "Họ tên", "SĐT", "CCCD", "Ngày sinh", "Giới tính", "Phân loại", "Thời gian đăng ký"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(UIConstants.FONT_LABEL);
        table.setRowHeight(40);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setSelectionBackground(UIConstants.PRIMARY_BG_SOFT);
        table.setSelectionForeground(UIConstants.TEXT_PRIMARY);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setFont(UIConstants.FONT_BOLD);
        header.setBackground(UIConstants.TABLE_HEADER_BG);
        header.setForeground(UIConstants.TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);

        // Column widths
        table.getColumnModel().getColumn(0).setMaxWidth(50);   // STT
        table.getColumnModel().getColumn(1).setMaxWidth(80);   // Mã BN
        table.getColumnModel().getColumn(3).setMaxWidth(110);  // SĐT
        table.getColumnModel().getColumn(4).setMaxWidth(130);  // CCCD
        table.getColumnModel().getColumn(5).setMaxWidth(100);  // Ngày sinh
        table.getColumnModel().getColumn(6).setMaxWidth(80);   // Giới tính
        table.getColumnModel().getColumn(7).setMaxWidth(120);  // Phân loại

        // Cell renderer with padding
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.CARD_BG);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ══════════════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════════════

    /**
     * Mở dialog đăng ký bệnh nhân mới.
     */
    private void openRegistrationDialog() {
        PatientRegistrationDialog dialog = new PatientRegistrationDialog(
                (Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);

        if (dialog.isRegistered()) {
            loadTodayPatients();
        }
    }

    /**
     * Chuyển bệnh nhân đã chọn vào hàng đợi.
     */
    private void transferToQueue() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn bệnh nhân cần chuyển vào hàng đợi.",
                    "Chú ý", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedRow >= patientIds.size()) return;

        int patientId = patientIds.get(selectedRow);
        Patient patient = patientBUS.findById(patientId);
        if (patient == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy bệnh nhân.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Xác định priority
            Priority priority = Priority.NORMAL;
            if (patient.getPatientType() == Patient.PatientType.EMERGENCY) {
                priority = Priority.EMERGENCY;
            } else if (patient.getAge() >= 60) {
                priority = Priority.ELDERLY;
            }

            QueueEntry entry = queueBUS.addToQueue(patientId, priority);
            JOptionPane.showMessageDialog(this,
                    "Đã chuyển vào hàng đợi!\n" +
                    "Bệnh nhân: " + patient.getFullName() + "\n" +
                    "Số thứ tự: " + entry.getQueueNumber() + "\n" +
                    "Ưu tiên: " + entry.getPriorityDisplay(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(),
                    "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Tìm kiếm bệnh nhân.
     */
    private void searchPatients() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadTodayPatients();
            return;
        }

        try {
            List<Patient> results = patientDAO.searchPatients(keyword);
            populateTable(results);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  DATA
    // ══════════════════════════════════════════════════════════

    /**
     * Load danh sách bệnh nhân đã đăng ký hôm nay.
     */
    private void loadTodayPatients() {
        try {
            List<Patient> patients = patientDAO.findTodayRegistered();
            populateTable(patients);
        } catch (Exception e) {
            // Fallback: show all patients
            try {
                List<Patient> all = patientBUS.findAll();
                populateTable(all);
            } catch (Exception ignored) {}
        }
    }

    private void populateTable(List<Patient> patients) {
        tableModel.setRowCount(0);
        patientIds.clear();

        int idx = 0;
        for (Patient p : patients) {
            idx++;
            patientIds.add(p.getId());

            String dob = p.getDateOfBirth() != null ? p.getDateOfBirth().format(dateFmt) : "";
            String gender = p.getGender() != null ? p.getGender().getDisplayName() : "";
            String type = p.getPatientType() != null ? p.getPatientType().getDisplayName() : "Khám lần đầu";
            String regTime = p.getCreatedAt() != null ? p.getCreatedAt().format(dateTimeFmt) : "";

            tableModel.addRow(new Object[]{
                    idx,
                    p.getPatientCode(),
                    p.getFullName(),
                    p.getPhone(),
                    p.getCccd() != null ? p.getCccd() : "",
                    dob,
                    gender,
                    type,
                    regTime
            });
        }

        lblCount.setText(patients.size() + " bệnh nhân");
    }

    // ══════════════════════════════════════════════════════════
    //  EVENT SUBSCRIPTION
    // ══════════════════════════════════════════════════════════

    private void subscribeEvents() {
        EventBus.getInstance().subscribe(PatientRegisteredEvent.class, event -> {
            loadTodayPatients();
        });
    }
}
