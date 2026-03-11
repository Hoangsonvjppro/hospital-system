package com.hospital.gui.panels;
import com.hospital.bus.DoctorBUS;
import com.hospital.bus.MedicalRecordBUS;
import com.hospital.bus.PatientBUS;
import com.hospital.bus.QueueBUS;
import com.hospital.bus.event.EventBus;
import com.hospital.bus.event.PatientRegisteredEvent;
import com.hospital.exception.BusinessException;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.gui.dialogs.PatientRegistrationDialog;
import com.hospital.model.Doctor;
import com.hospital.model.MedicalRecord;
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
 * Trang Tiếp nhận bệnh nhân (dành cho Lễ tân).
 * <p>
 * Quy trình:
 * 1. Tìm kiếm bệnh nhân cũ (theo tên/SĐT/CCCD) HOẶC Đăng ký mới
 * 2. Xem thông tin chi tiết bệnh nhân khi chọn trong bảng
 * 3. Chuyển bệnh nhân vào hàng đợi khám
 * <p>
 * Layout: Bảng danh sách (trái) + Panel chi tiết bệnh nhân (phải)
 */
public class ReceptionPanel extends JPanel {

    private final PatientBUS patientBUS = new PatientBUS();
    private final QueueBUS queueBUS = new QueueBUS();
    private final DoctorBUS doctorBUS = new DoctorBUS();
    private final MedicalRecordBUS medicalRecordBUS = new MedicalRecordBUS();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private JLabel lblCount;

    // Detail panel fields
    private JLabel lblDetailName, lblDetailGender, lblDetailDob, lblDetailPhone;
    private JLabel lblDetailCccd, lblDetailAddress, lblDetailAllergy, lblDetailNotes;
    private JLabel lblDetailType, lblDetailCode, lblDetailRegTime;
    private JPanel detailPanel;
    private JPanel detailContent;
    private RoundedButton btnToQueue;

    // Store patient IDs for table row mapping
    private final java.util.List<Integer> patientIds = new java.util.ArrayList<>();
    private Patient selectedPatient;

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
        add(createMainContent(), BorderLayout.CENTER);
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

        RoundedButton btnRegister = new RoundedButton("Đăng ký bệnh nhân mới");
        btnRegister.addActionListener(e -> openRegistrationDialog());

        RoundedButton btnHistory = new RoundedButton("Lịch sử khám");
        btnHistory.addActionListener(e -> showMedicalHistory());

        actions.add(btnRegister);
        actions.add(btnHistory);
        titleRow.add(actions, BorderLayout.EAST);

        header.add(titleRow, BorderLayout.NORTH);

        // Search bar + count
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm bệnh nhân theo tên, SĐT hoặc CCCD...");
        txtSearch.setFont(UIConstants.FONT_BODY);
        txtSearch.setPreferredSize(new Dimension(0, 38));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(0, 14, 0, 14)));

        RoundedButton btnSearch = new RoundedButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(110, 38));
        btnSearch.addActionListener(e -> searchPatients());

        RoundedButton btnShowAll = new RoundedButton("Tất cả");
        btnShowAll.setPreferredSize(new Dimension(90, 38));
        btnShowAll.addActionListener(e -> {
            txtSearch.setText("");
            loadAllPatients();
        });

        txtSearch.addActionListener(e -> searchPatients());

        lblCount = new JLabel("0 bệnh nhân");
        lblCount.setFont(UIConstants.FONT_LABEL);
        lblCount.setForeground(UIConstants.TEXT_SECONDARY);
        lblCount.setBorder(new EmptyBorder(0, 8, 0, 0));

        JPanel searchLeft = new JPanel(new BorderLayout(6, 0));
        searchLeft.setOpaque(false);
        searchLeft.add(txtSearch, BorderLayout.CENTER);
        JPanel searchBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        searchBtns.setOpaque(false);
        searchBtns.add(btnSearch);
        searchBtns.add(btnShowAll);
        searchLeft.add(searchBtns, BorderLayout.EAST);

        searchRow.add(searchLeft, BorderLayout.CENTER);
        searchRow.add(lblCount, BorderLayout.EAST);

        header.add(searchRow, BorderLayout.SOUTH);

        return header;
    }

    // ══════════════════════════════════════════════════════════
    //  MAIN CONTENT: Table (left) + Detail (right)
    // ══════════════════════════════════════════════════════════

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(12, 0));
        main.setOpaque(false);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createTablePanel(), createDetailPanel());
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        main.add(splitPane, BorderLayout.CENTER);
        return main;
    }

    // ══════════════════════════════════════════════════════════
    //  TABLE: Danh sách bệnh nhân
    // ══════════════════════════════════════════════════════════

    private JPanel createTablePanel() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"STT", "Mã BN", "Họ tên", "SĐT", "Giới tính", "Phân loại"};
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
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setMinWidth(45);
        table.getColumnModel().getColumn(0).setMaxWidth(55);   // STT
        table.getColumnModel().getColumn(1).setMinWidth(70);
        table.getColumnModel().getColumn(1).setMaxWidth(100);  // Mã BN
        table.getColumnModel().getColumn(2).setMinWidth(120);  // Họ tên – co giãn
        table.getColumnModel().getColumn(3).setMinWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(130); // SĐT
        table.getColumnModel().getColumn(4).setMinWidth(75);
        table.getColumnModel().getColumn(4).setMaxWidth(90);   // Giới tính
        table.getColumnModel().getColumn(5).setMinWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(130); // Phân loại

        // Cell renderer with padding
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Selection listener — show patient details
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onPatientSelected();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.CARD_BG);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ══════════════════════════════════════════════════════════
    //  DETAIL PANEL: Thông tin chi tiết bệnh nhân
    // ══════════════════════════════════════════════════════════

    private JPanel createDetailPanel() {
        detailPanel = new RoundedPanel(UIConstants.CARD_RADIUS);
        detailPanel.setBackground(UIConstants.CARD_BG);
        detailPanel.setLayout(new BorderLayout());
        detailPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        detailPanel.setPreferredSize(new Dimension(360, 0));

        // Title
        JLabel lblDetailTitle = new JLabel("Thông tin bệnh nhân");
        lblDetailTitle.setFont(UIConstants.FONT_SUBTITLE);
        lblDetailTitle.setForeground(UIConstants.PRIMARY);
        lblDetailTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        detailPanel.add(lblDetailTitle, BorderLayout.NORTH);

        // Content — scrollable
        detailContent = new JPanel();
        detailContent.setLayout(new BoxLayout(detailContent, BoxLayout.Y_AXIS));
        detailContent.setBackground(UIConstants.CARD_BG);

        lblDetailCode = new JLabel("-");
        lblDetailName = new JLabel("-");
        lblDetailGender = new JLabel("-");
        lblDetailDob = new JLabel("-");
        lblDetailPhone = new JLabel("-");
        lblDetailCccd = new JLabel("-");
        lblDetailAddress = new JLabel("-");
        lblDetailAllergy = new JLabel("-");
        lblDetailNotes = new JLabel("-");
        lblDetailType = new JLabel("-");
        lblDetailRegTime = new JLabel("-");

        addDetailRow("Mã BN:", lblDetailCode);
        addDetailRow("Họ tên:", lblDetailName);
        addDetailRow("Giới tính:", lblDetailGender);
        addDetailRow("Ngày sinh:", lblDetailDob);
        addDetailRow("SĐT:", lblDetailPhone);
        addDetailRow("CCCD:", lblDetailCccd);
        addDetailRow("Địa chỉ:", lblDetailAddress);
        addDetailRow("Phân loại:", lblDetailType);
        addDetailRow("Tiền sử dị ứng:", lblDetailAllergy);
        addDetailRow("Ghi chú:", lblDetailNotes);
        addDetailRow("Ngày đăng ký:", lblDetailRegTime);

        // Placeholder when no selection
        JLabel lblPlaceholder = new JLabel("<html><center>Chọn một bệnh nhân<br>trong danh sách để xem<br>thông tin chi tiết</center></html>");
        lblPlaceholder.setFont(UIConstants.FONT_BODY);
        lblPlaceholder.setForeground(UIConstants.TEXT_MUTED);
        lblPlaceholder.setHorizontalAlignment(SwingConstants.CENTER);
        lblPlaceholder.setName("placeholder");

        JScrollPane detailScroll = new JScrollPane(detailContent);
        detailScroll.setBorder(null);
        detailScroll.getViewport().setBackground(UIConstants.CARD_BG);
        detailScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Initially show placeholder
        detailContent.setVisible(false);
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(UIConstants.CARD_BG);
        centerWrapper.add(detailScroll, BorderLayout.CENTER);
        centerWrapper.add(lblPlaceholder, BorderLayout.SOUTH);
        detailPanel.add(centerWrapper, BorderLayout.CENTER);

        // Bottom actions
        JPanel bottomActions = new JPanel(new GridLayout(1, 1, 8, 0));
        bottomActions.setOpaque(false);
        bottomActions.setBorder(new EmptyBorder(12, 0, 0, 0));

        btnToQueue = new RoundedButton("Chuyển vào hàng đợi");
        btnToQueue.setColors(UIConstants.SUCCESS_GREEN, UIConstants.SUCCESS_GREEN_DARK);
        btnToQueue.setPreferredSize(new Dimension(0, 42));
        btnToQueue.setEnabled(false);
        btnToQueue.addActionListener(e -> transferToQueue());
        bottomActions.add(btnToQueue);

        detailPanel.add(bottomActions, BorderLayout.SOUTH);

        return detailPanel;
    }

    private void addDetailRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(UIConstants.CARD_BG);
        row.setBorder(new EmptyBorder(6, 0, 6, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(130, 20));
        row.add(lbl, BorderLayout.WEST);

        valueLabel.setFont(UIConstants.FONT_BODY);
        valueLabel.setForeground(UIConstants.TEXT_PRIMARY);
        row.add(valueLabel, BorderLayout.CENTER);

        detailContent.add(row);
    }

    // ══════════════════════════════════════════════════════════
    //  SELECTION HANDLER
    // ══════════════════════════════════════════════════════════

    private void onPatientSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= patientIds.size()) {
            clearDetailPanel();
            return;
        }

        int patientId = patientIds.get(selectedRow);
        selectedPatient = patientBUS.findById(patientId);
        if (selectedPatient == null) {
            clearDetailPanel();
            return;
        }

        showPatientDetails(selectedPatient);
    }

    private void showPatientDetails(Patient p) {
        detailContent.setVisible(true);

        // Kiểm tra xem bệnh nhân đã có trong hàng đợi hôm nay chưa
        boolean isInQueue = queueBUS.isPatientInTodayQueue(p.getId());
        if (isInQueue) {
            btnToQueue.setEnabled(false);
            btnToQueue.setText("Đã trong hàng đợi");
        } else {
            btnToQueue.setEnabled(true);
            btnToQueue.setText("Chuyển vào hàng đợi");
        }

        // Hide placeholder
        Container parent = detailContent.getParent().getParent();
        if (parent instanceof JPanel) {
            for (Component c : ((JPanel) parent).getComponents()) {
                if (c instanceof JLabel && "placeholder".equals(c.getName())) {
                    c.setVisible(false);
                }
            }
        }

        lblDetailCode.setText(p.getPatientCode() != null ? p.getPatientCode() : "-");
        lblDetailName.setText(p.getFullName() != null ? p.getFullName() : "-");
        lblDetailGender.setText(p.getGender() != null ? p.getGender().getDisplayName() : "-");
        lblDetailDob.setText(p.getDateOfBirth() != null ? p.getDateOfBirth().format(dateFmt) : "-");
        lblDetailPhone.setText(p.getPhone() != null ? p.getPhone() : "-");
        lblDetailCccd.setText(p.getCccd() != null && !p.getCccd().isEmpty() ? p.getCccd() : "-");
        lblDetailAddress.setText(p.getAddress() != null && !p.getAddress().isEmpty() ? p.getAddress() : "-");
        lblDetailType.setText(p.getPatientType() != null ? p.getPatientType().getDisplayName() : "Khám lần đầu");
        lblDetailAllergy.setText(p.getAllergyHistory() != null && !p.getAllergyHistory().isEmpty() ? p.getAllergyHistory() : "Không có");
        lblDetailNotes.setText(p.getNotes() != null && !p.getNotes().isEmpty() ? p.getNotes() : "-");
        lblDetailRegTime.setText(p.getCreatedAt() != null ? p.getCreatedAt().format(dateTimeFmt) : "-");

        // Highlight allergy if present
        if (p.getAllergyHistory() != null && !p.getAllergyHistory().isEmpty()) {
            lblDetailAllergy.setForeground(new Color(192, 57, 43));
        } else {
            lblDetailAllergy.setForeground(UIConstants.TEXT_PRIMARY);
        }

        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void clearDetailPanel() {
        selectedPatient = null;
        detailContent.setVisible(false);
        btnToQueue.setEnabled(false);

        // Show placeholder
        Container parent = detailContent.getParent().getParent();
        if (parent instanceof JPanel) {
            for (Component c : ((JPanel) parent).getComponents()) {
                if (c instanceof JLabel && "placeholder".equals(c.getName())) {
                    c.setVisible(true);
                }
            }
        }

        detailPanel.revalidate();
        detailPanel.repaint();
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
        if (selectedPatient == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn bệnh nhân cần chuyển vào hàng đợi.",
                    "Chú ý", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Xác định priority
            Priority priority = Priority.NORMAL;
            if (selectedPatient.getPatientType() == Patient.PatientType.EMERGENCY) {
                priority = Priority.EMERGENCY;
            } else if (selectedPatient.getAge() >= 60) {
                priority = Priority.ELDERLY;
            }

            QueueEntry entry = queueBUS.addToQueue(selectedPatient.getId(), priority);
            JOptionPane.showMessageDialog(this,
                    "Đã chuyển vào hàng đợi!\n" +
                    "Bệnh nhân: " + selectedPatient.getFullName() + "\n" +
                    "Số thứ tự: " + entry.getQueueNumber() + "\n" +
                    "Ưu tiên: " + entry.getPriorityDisplay(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh detail panel to update button state
            showPatientDetails(selectedPatient);
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(),
                    "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xem lịch sử khám của bệnh nhân đã chọn.
     */
    private void showMedicalHistory() {
        if (selectedPatient == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn bệnh nhân để xem lịch sử khám.",
                    "Chú ý", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<MedicalRecord> hist = medicalRecordBUS.getHistoryByPatient(selectedPatient.getId());
            if (hist == null || hist.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy lịch sử khám cho bệnh nhân này.",
                        "Thông tin", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            DefaultTableModel hm = new DefaultTableModel(
                    new String[]{"Ngày giờ", "Bác sĩ", "Chẩn đoán", "Triệu chứng", "STT"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            DoctorBUS doctorBUSLocal = new DoctorBUS();
            for (MedicalRecord r : hist) {
                String date = r.getVisitDate() != null ? r.getVisitDate().toString() : "";
                String docName = "-";
                try {
                    Doctor d = doctorBUSLocal.findById((int) r.getDoctorId());
                    if (d != null) docName = d.getFullName();
                } catch (Exception ignored) {}
                String diag = r.getDiagnosis() != null ? r.getDiagnosis() : "";
                String sym = r.getSymptoms() != null ? r.getSymptoms() : "";
                String q = r.getQueueNumber() != null ? String.valueOf(r.getQueueNumber()) : "";
                hm.addRow(new Object[]{date, docName, diag, sym, q});
            }

            JTable jt = new JTable(hm);
            jt.setRowHeight(28);
            JScrollPane sp = new JScrollPane(jt);
            sp.setPreferredSize(new Dimension(760, 320));
            JOptionPane.showMessageDialog(this, sp,
                    "Lịch sử khám - " + selectedPatient.getFullName(),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể lấy lịch sử: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
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
            List<Patient> results = patientBUS.searchPatients(keyword);
            populateTable(results);
            clearDetailPanel();
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
            List<Patient> patients = patientBUS.findTodayRegistered();
            populateTable(patients);
        } catch (Exception e) {
            // Fallback: show all patients
            loadAllPatients();
        }
    }

    private void loadAllPatients() {
        try {
            List<Patient> all = patientBUS.findAll();
            populateTable(all);
        } catch (Exception ignored) {}
    }

    private void populateTable(List<Patient> patients) {
        tableModel.setRowCount(0);
        patientIds.clear();

        int idx = 0;
        for (Patient p : patients) {
            idx++;
            patientIds.add(p.getId());

            String gender = p.getGender() != null ? p.getGender().getDisplayName() : "";
            String type = p.getPatientType() != null ? p.getPatientType().getDisplayName() : "Khám lần đầu";

            tableModel.addRow(new Object[]{
                    idx,
                    p.getPatientCode(),
                    p.getFullName(),
                    p.getPhone(),
                    gender,
                    type
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
