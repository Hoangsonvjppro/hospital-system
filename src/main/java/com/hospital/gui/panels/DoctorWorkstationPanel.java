package com.hospital.gui.panels;

import com.hospital.bus.MedicalRecordBUS;
import com.hospital.bus.MedicineBUS;
import com.hospital.bus.PrescriptionBUS;
import com.hospital.bus.QueueBUS;
import com.hospital.bus.event.EventBus;
import com.hospital.bus.event.LabResultReadyEvent;
import com.hospital.bus.event.QueueUpdatedEvent;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.model.Medicine;
import com.hospital.model.MedicalRecord;
import com.hospital.model.Patient;
import com.hospital.model.PrescriptionDetail;
import com.hospital.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Doctor Workstation Panel - Giao dien kham benh cho bac si.
 *
 * Ben trai: Danh sach benh nhan cho + nut "Goi kham"
 * Ben phai:
 *   Tab 0: Thong tin & Sinh hieu (weight, height, BP, pulse, temperature, SpO2)
 *   Tab 1: Kham benh (trieu chung, chan doan, ICD-10, ghi chu BS, ngay tai kham)
 *   Tab 2: Ke don thuoc (tim thuoc, them vao don, canh bao di ung)
 *   Tab 3: Lich su kham (xem benh an cu)
 */
public class DoctorWorkstationPanel extends JPanel {

    private static final String[] TAB_NAMES = {
        "Th\u00F4ng tin & Sinh hi\u1EC7u", "Kh\u00E1m b\u1EC7nh", "K\u00EA \u0111\u01A1n thu\u1ED1c", "X\u00E9t nghi\u1EC7m", "L\u1ECBch s\u1EED kh\u00E1m"
    };

    private final QueueBUS queueBUS = new QueueBUS();
    private final MedicalRecordBUS medicalRecordBUS = new MedicalRecordBUS();
    private final PrescriptionBUS prescriptionBUS = new PrescriptionBUS();
    private final MedicineBUS medicineBUS = new MedicineBUS();
    private final DecimalFormat moneyFmt = new DecimalFormat("#,###");

    private JPanel patientListPanel;
    private JPanel rightContentPanel;
    private JLabel lblPatientCount;
    private Patient selectedPatient;
    private long selectedRecordId = -1;
    private int selectedIndex = -1;

    // Tab 0 - Vital Signs
    private VitalSignsPanel vitalSignsPanel;

    // Tab 1 - Examination
    private SymptomsPanel symptomsPanel;



    // Tab 2 - Prescription
    private JTextField txtMedicineSearch;
    private JTable tableMedicineSearch;
    private DefaultTableModel modelMedicineSearch;
    private JTable tablePrescription;
    private DefaultTableModel modelPrescription;
    private final List<PrescriptionDetail> prescriptionItems = new ArrayList<>();

    // Tab 3 - Lab Orders
    private LabOrderPanel labOrderPanel;

    private int activeTab = 0;
    private JPanel tabBar;
    private JPanel bottomBar;

    private javax.swing.Timer refreshTimer;

    public DoctorWorkstationPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 0));
        initComponents();
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        refreshTimer = new javax.swing.Timer(10_000, e -> loadPatientList());
        refreshTimer.setRepeats(true);
        refreshTimer.start();
    }

    private void initComponents() {
        vitalSignsPanel = new VitalSignsPanel();
        symptomsPanel = new SymptomsPanel();
        labOrderPanel = new LabOrderPanel();

        // Subscribe to lab result ready events
        EventBus.getInstance().subscribe(LabResultReadyEvent.class, evt -> {
            if (selectedPatient != null && activeTab == 3) {
                // Lab result ready - user can see updates in LabOrderPanel
            }
        });

        // Subscribe to queue updated events - refresh list when receptionist adds patient
        EventBus.getInstance().subscribe(QueueUpdatedEvent.class, evt -> {
            SwingUtilities.invokeLater(() -> loadPatientList());
        });
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setOpaque(false);
        mainPanel.add(createLeftPanel(), BorderLayout.WEST);
        mainPanel.add(createRightPanel(), BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    // ==========================================================================
    //  LEFT PANEL - Patient Queue + "Goi kham" button
    // ==========================================================================

    private JPanel createLeftPanel() {
        JPanel left = new JPanel(new BorderLayout(0, 0));
        left.setPreferredSize(new Dimension(270, 0));
        left.setBackground(UIConstants.CARD_BG);
        left.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIConstants.BORDER_COLOR));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.CARD_BG);
        header.setBorder(new EmptyBorder(20, 18, 10, 18));

        JLabel lblTitle = new JLabel("DANH S\u00C1CH CH\u1ECC");
        lblTitle.setFont(UIConstants.FONT_SMALL);
        lblTitle.setForeground(UIConstants.TEXT_SECONDARY);

        List<Patient> waiting = queueBUS.getWaitingPatients();
        lblPatientCount = new JLabel(waiting.size() + " B\u1EC7nh nh\u00E2n");
        lblPatientCount.setFont(UIConstants.FONT_HEADER);
        lblPatientCount.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel badge = new JLabel("Queue") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.ACCENT_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(UIConstants.FONT_SMALL);
        badge.setForeground(Color.WHITE);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setPreferredSize(new Dimension(58, 24));
        badge.setOpaque(false);

        JPanel countRow = new JPanel(new BorderLayout());
        countRow.setOpaque(false);
        countRow.setBorder(new EmptyBorder(6, 0, 0, 0));
        countRow.add(lblPatientCount, BorderLayout.WEST);
        countRow.add(badge, BorderLayout.EAST);

        JPanel headerContent = new JPanel(new BorderLayout());
        headerContent.setOpaque(false);
        headerContent.add(lblTitle, BorderLayout.NORTH);
        headerContent.add(countRow, BorderLayout.CENTER);
        header.add(headerContent, BorderLayout.CENTER);

        // "Goi kham" button
        RoundedButton btnCallPatient = new RoundedButton("G\u1ECDi kh\u00E1m", UIConstants.ACCENT_BLUE, UIConstants.ACCENT_BLUE_DARK, 8);
        btnCallPatient.setPreferredSize(new Dimension(100, 34));
        btnCallPatient.addActionListener(e -> onCallPatient());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(4, 18, 8, 18));
        btnPanel.add(btnCallPatient);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(btnPanel, BorderLayout.SOUTH);
        left.add(topSection, BorderLayout.NORTH);

        // Patient list
        patientListPanel = new JPanel();
        patientListPanel.setLayout(new BoxLayout(patientListPanel, BoxLayout.Y_AXIS));
        patientListPanel.setBackground(UIConstants.CARD_BG);
        patientListPanel.setBorder(new EmptyBorder(0, 12, 12, 12));
        loadPatientList();

        JScrollPane scroll = new JScrollPane(patientListPanel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(UIConstants.CARD_BG);
        left.add(scroll, BorderLayout.CENTER);

        return left;
    }

    /** Goi benh nhan dang cho vao phong kham (WAITING -> EXAMINING) */
    private void onCallPatient() {
        if (selectedPatient == null || selectedRecordId <= 0) {
            // Auto-select first WAITING patient
            List<Patient> waitingOnly = queueBUS.getPatientsByStatus("WAITING");
            if (waitingOnly.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Kh\u00F4ng c\u00F3 b\u1EC7nh nh\u00E2n n\u00E0o \u0111ang ch\u1EDD.", "Th\u00F4ng b\u00E1o", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            selectedPatient = waitingOnly.get(0);
            selectedRecordId = selectedPatient.getCurrentRecordId();
            selectedIndex = 0;
        }

        if ("WAITING".equals(selectedPatient.getStatus())) {
            try {
                int currentUserId = SessionManager.getInstance().getCurrentUser() != null ? 
                                    SessionManager.getInstance().getCurrentUser().getId() : 0;
                long newRecordId = queueBUS.updateQueueStatus(selectedRecordId, "EXAMINING", currentUserId);
                if (newRecordId > 0) {
                    selectedRecordId = newRecordId;
                    selectedPatient.setCurrentRecordId(newRecordId);
                    selectedPatient.setStatus("EXAMINING");
                    JOptionPane.showMessageDialog(this,
                            "\u0110\u00E3 g\u1ECDi b\u1EC7nh nh\u00E2n: " + selectedPatient.getFullName() + " v\u00E0o ph\u00F2ng kh\u00E1m.",
                            "G\u1ECDi kh\u00E1m", JOptionPane.INFORMATION_MESSAGE);
                    loadPatientList();
                    updateRightPanel();
                }
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "L\u1ED7i nghi\u1EC7p v\u1EE5", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "L\u1ED7i h\u1EC7 th\u1ED1ng: " + ex.getMessage(), "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
            }
        } else if ("EXAMINING".equals(selectedPatient.getStatus())) {
            JOptionPane.showMessageDialog(this,
                    "B\u1EC7nh nh\u00E2n " + selectedPatient.getFullName() + " \u0111ang \u0111\u01B0\u1EE3c kh\u00E1m.",
                    "Th\u00F4ng b\u00E1o", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void loadPatientList() {
        patientListPanel.removeAll();
        List<Patient> waiting = queueBUS.getWaitingPatients();
        lblPatientCount.setText(waiting.size() + " B\u1EC7nh nh\u00E2n");

        for (int i = 0; i < waiting.size(); i++) {
            patientListPanel.add(createPatientCard(waiting.get(i), i));
            patientListPanel.add(Box.createVerticalStrut(8));
        }
        patientListPanel.revalidate();
        patientListPanel.repaint();
    }

    private JPanel createPatientCard(Patient patient, int index) {
        boolean isSelected = (index == selectedIndex);

        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected ? UIConstants.ACCENT_BLUE_SOFT : UIConstants.CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(isSelected ? UIConstants.ACCENT_BLUE : UIConstants.BORDER_COLOR);
                g2.setStroke(new BasicStroke(isSelected ? 2f : 1f));
                float off = isSelected ? 1f : 0.5f;
                g2.draw(new RoundRectangle2D.Float(off, off, getWidth() - 2 * off, getHeight() - 2 * off, 12, 12));
                if (isSelected) {
                    g2.setColor(UIConstants.ACCENT_BLUE);
                    g2.fillRoundRect(0, 4, 4, getHeight() - 8, 4, 4);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 14, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String num = String.format("%02d", index + 1);
        JLabel numLabel = new JLabel(num) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected ? UIConstants.ACCENT_BLUE : UIConstants.BORDER_COLOR);
                g2.fill(new Ellipse2D.Float(0, 0, 32, 32));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        numLabel.setFont(UIConstants.FONT_CAPTION);
        numLabel.setForeground(isSelected ? Color.WHITE : UIConstants.TEXT_SECONDARY);
        numLabel.setHorizontalAlignment(SwingConstants.CENTER);
        numLabel.setPreferredSize(new Dimension(32, 32));

        JPanel numPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        numPanel.setOpaque(false);
        numPanel.setPreferredSize(new Dimension(36, 32));
        numPanel.add(numLabel);
        card.add(numPanel, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel nameLabel = new JLabel(patient.getFullName());
        nameLabel.setFont(UIConstants.FONT_SUBTITLE);
        nameLabel.setForeground(isSelected ? UIConstants.ACCENT_BLUE : UIConstants.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(nameLabel);
        info.add(Box.createVerticalStrut(2));

        JLabel detailLabel = new JLabel(patient.getGender() + " - " + patient.getAge() + " tuoi");
        detailLabel.setFont(UIConstants.FONT_CAPTION);
        detailLabel.setForeground(UIConstants.TEXT_SECONDARY);
        detailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(detailLabel);
        info.add(Box.createVerticalStrut(4));

        // Show status badge
        String statusText = "EXAMINING".equals(patient.getStatus()) ? "\u0110ang kh\u00E1m" : "Ch\u1EDD kh\u00E1m";
        Color statusColor = "EXAMINING".equals(patient.getStatus()) ? UIConstants.STATUS_EXAMINING : UIConstants.STATUS_WAITING;
        JLabel statusLabel = new JLabel(statusText + "  |  " + (patient.getArrivalTime() != null ? patient.getArrivalTime() : ""));
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(statusColor);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(statusLabel);

        card.add(info, BorderLayout.CENTER);

        if (isSelected || "EXAMINING".equals(patient.getStatus())) {
            JLabel dot = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isSelected ? UIConstants.ACCENT_BLUE : UIConstants.STATUS_EXAMINING);
                    g2.fill(new Ellipse2D.Float(0, 0, 10, 10));
                    g2.dispose();
                }
            };
            dot.setPreferredSize(new Dimension(10, 10));
            JPanel dotPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
            dotPanel.setOpaque(false);
            dotPanel.add(dot);
            card.add(dotPanel, BorderLayout.EAST);
        }

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedIndex = index;
                selectedPatient = patient;
                selectedRecordId = patient.getCurrentRecordId();
                prescriptionItems.clear();
                loadPatientList();
                updateRightPanel();
            }
        });

        return card;
    }

    // ==========================================================================
    //  RIGHT PANEL - Tabs + Content + Bottom bar
    // ==========================================================================

    private JPanel createRightPanel() {
        JPanel right = new JPanel(new BorderLayout(0, 0));
        right.setBackground(UIConstants.CONTENT_BG);

        tabBar = buildTabBar();
        right.add(tabBar, BorderLayout.NORTH);

        rightContentPanel = new JPanel(new BorderLayout());
        rightContentPanel.setOpaque(false);
        rightContentPanel.setBorder(new EmptyBorder(20, 28, 20, 28));
        right.add(rightContentPanel, BorderLayout.CENTER);

        bottomBar = createBottomBar();
        right.add(bottomBar, BorderLayout.SOUTH);

        List<Patient> waiting = queueBUS.getWaitingPatients();
        if (!waiting.isEmpty()) {
            selectedIndex = 0;
            selectedPatient = waiting.get(0);
            selectedRecordId = selectedPatient.getCurrentRecordId();
            loadPatientList();
            updateRightPanel();
        } else {
            showEmptyState();
            updateBottomBar();
        }

        return right;
    }

    private JPanel buildTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(UIConstants.CARD_BG);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR));

        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int idx = i;
            JLabel tab = new JLabel(TAB_NAMES[i]) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (idx == activeTab) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(UIConstants.ACCENT_BLUE);
                        g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                        g2.dispose();
                    }
                }
            };
            tab.setFont(UIConstants.FONT_BOLD);
            tab.setForeground(idx == activeTab ? UIConstants.ACCENT_BLUE : UIConstants.TEXT_SECONDARY);
            tab.setHorizontalAlignment(SwingConstants.CENTER);
            tab.setBorder(new EmptyBorder(14, 20, 14, 20));
            tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tab.setOpaque(false);

            tab.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    activeTab = idx;
                    refreshTabBar();
                    updateRightPanel();
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (idx != activeTab) tab.setForeground(UIConstants.ACCENT_BLUE_LIGHT);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    tab.setForeground(idx == activeTab ? UIConstants.ACCENT_BLUE : UIConstants.TEXT_SECONDARY);
                }
            });
            bar.add(tab);
        }
        return bar;
    }

    private void refreshTabBar() {
        // Capture state before switching
        if (selectedPatient != null) {
            if (activeTab == 0) {
               // Assuming we'd capture state here. We'll do it centrally in updateRightPanel
            }
        }
        tabBar.removeAll();
        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int idx = i;
            JLabel tab = new JLabel(TAB_NAMES[i]) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (idx == activeTab) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(UIConstants.ACCENT_BLUE);
                        g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                        g2.dispose();
                    }
                }
            };
            tab.setFont(UIConstants.FONT_BOLD);
            tab.setForeground(idx == activeTab ? UIConstants.ACCENT_BLUE : UIConstants.TEXT_SECONDARY);
            tab.setHorizontalAlignment(SwingConstants.CENTER);
            tab.setBorder(new EmptyBorder(14, 20, 14, 20));
            tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tab.setOpaque(false);
            tab.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // Capture state of current tab before changing activeTab
                    captureCurrentTabState();
                    activeTab = idx;
                    refreshTabBar();
                    updateRightPanel();
                }
            });
            tabBar.add(tab);
        }
        tabBar.revalidate();
        tabBar.repaint();
    }
    
    private Map<Integer, Map<String, Object>> tabStates = new java.util.HashMap<>();
    
    private void captureCurrentTabState() {
        if (selectedPatient == null) return;
        if (activeTab == 0) {
             tabStates.put(0, vitalSignsPanel.captureState());
        } else if (activeTab == 1) {
             tabStates.put(1, symptomsPanel.captureState());
        }
    }

    private void updateRightPanel() {
        rightContentPanel.removeAll();
        if (selectedPatient == null) {
            showEmptyState();
        } else {
            switch (activeTab) {
                case 0 -> {
                    rightContentPanel.add(createInfoAndVitalsContent(), BorderLayout.CENTER);
                    if (tabStates.containsKey(0)) vitalSignsPanel.restoreState(tabStates.get(0));
                }
                case 1 -> {
                    rightContentPanel.add(createExaminationContent(), BorderLayout.CENTER);
                    if (tabStates.containsKey(1)) symptomsPanel.restoreState(tabStates.get(1));
                }
                case 2 -> rightContentPanel.add(createPrescriptionContent(), BorderLayout.CENTER);
                case 3 -> {
                    if (selectedPatient != null && selectedRecordId > 0) {
                        int currentUserId = SessionManager.getInstance().getCurrentUser() != null ? 
                                            SessionManager.getInstance().getCurrentUser().getId() : 0;
                        labOrderPanel.setContext(selectedRecordId, selectedPatient.getId(), currentUserId);
                    }
                    rightContentPanel.add(labOrderPanel, BorderLayout.CENTER);
                }
                case 4 -> rightContentPanel.add(createHistoryContent(), BorderLayout.CENTER);
            }
        }
        rightContentPanel.revalidate();
        rightContentPanel.repaint();
        updateBottomBar();
    }

    private void showEmptyState() {
        JLabel empty = new JLabel("Ch\u01B0a c\u00F3 b\u1EC7nh nh\u00E2n trong danh s\u00E1ch ch\u1EDD", SwingConstants.CENTER);
        empty.setFont(UIConstants.FONT_SUBTITLE);
        empty.setForeground(UIConstants.TEXT_MUTED);
        rightContentPanel.add(empty, BorderLayout.CENTER);
    }

    // ==========================================================================
    //  TAB 0 - Thong tin & Sinh hieu
    // ==========================================================================

    private JPanel createInfoAndVitalsContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        content.add(createPatientInfoCard());
        content.add(Box.createVerticalStrut(24));
        
        MedicalRecord rec = null;
        if (selectedRecordId > 0) {
            try { rec = medicalRecordBUS.findById(selectedRecordId); } catch (Exception ignored) {}
        }
        if (!tabStates.containsKey(0)) {
             vitalSignsPanel.populateFromRecord(rec);
        }
        content.add(vitalSignsPanel);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createPatientInfoCard() {
        RoundedPanel card = new RoundedPanel(14);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(24, 28, 24, 28));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        GridBagConstraints gbc = new GridBagConstraints();

        // Avatar
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.BORDER_COLOR);
                g2.fill(new Ellipse2D.Float(0, 0, 60, 60));
                g2.setColor(new Color(180, 190, 200));
                g2.fill(new Ellipse2D.Float(18, 10, 24, 24));
                g2.fillRoundRect(10, 38, 40, 24, 18, 18);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(60, 60); }
            @Override public Dimension getMinimumSize() { return getPreferredSize(); }
        };
        avatar.setOpaque(false);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 4;
        gbc.insets = new Insets(0, 0, 0, 20);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        card.add(avatar, gbc);

        // Name
        JLabel name = new JLabel(selectedPatient.getFullName());
        name.setFont(UIConstants.FONT_TITLE);
        name.setForeground(UIConstants.TEXT_PRIMARY);
        gbc = new GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(name, gbc);

        // Detail row
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dobStr = selectedPatient.getDateOfBirth() != null ? selectedPatient.getDateOfBirth().format(fmt) : "N/A";
        int age = selectedPatient.getAge();
        String detailText = dobStr + " (" + age + " tu\u1ED5i)   |   " + selectedPatient.getGender() + "   |   " + selectedPatient.getPhone();
        JLabel detail = new JLabel(detailText);
        detail.setFont(UIConstants.FONT_LABEL);
        detail.setForeground(UIConstants.TEXT_SECONDARY);
        gbc = new GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(detail, gbc);

        // Address
        JLabel address = new JLabel("\u0110\u1ECBa ch\u1EC9: " + (selectedPatient.getAddress() != null ? selectedPatient.getAddress() : "N/A"));
        address.setFont(UIConstants.FONT_LABEL);
        address.setForeground(UIConstants.TEXT_SECONDARY);
        gbc = new GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(address, gbc);

        // Allergy warning
        String allergyText = selectedPatient.getAllergyHistory();
        if (allergyText != null && !allergyText.trim().isEmpty()) {
            JLabel allergyLabel = new JLabel("\u26A0 D\u1ECB \u1EE9ng: " + allergyText);
            allergyLabel.setFont(UIConstants.FONT_BOLD);
            allergyLabel.setForeground(UIConstants.ERROR_COLOR);
            gbc = new GridBagConstraints();
            gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1; gbc.anchor = GridBagConstraints.WEST;
            card.add(allergyLabel, gbc);
        }

        return card;
    }



    // ==========================================================================
    //  TAB 1 - Kham benh
    // ==========================================================================

    private JPanel createExaminationContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        MedicalRecord rec = null;
        if (selectedRecordId > 0) {
            try { rec = medicalRecordBUS.findById(selectedRecordId); } catch (Exception ignored) {}
        }
        if (!tabStates.containsKey(1)) {
            symptomsPanel.populateFromRecord(rec);
        }
        
        content.add(symptomsPanel);

        JScrollPane scrollContent = new JScrollPane(content);
        scrollContent.setBorder(null);
        scrollContent.setOpaque(false);
        scrollContent.getViewport().setOpaque(false);
        scrollContent.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scrollContent, BorderLayout.CENTER);
        return wrapper;
    }



    // ==========================================================================
    //  TAB 2 - Ke don thuoc
    // ==========================================================================

    private JPanel createPrescriptionContent() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);

        // Search
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(0, 0, 8, 0));
        JLabel lblSearch = new JLabel("T\u00ECm thu\u1ED1c:");
        lblSearch.setFont(UIConstants.FONT_BOLD);
        lblSearch.setForeground(UIConstants.TEXT_PRIMARY);
        searchPanel.add(lblSearch, BorderLayout.WEST);
        txtMedicineSearch = new JTextField();
        txtMedicineSearch.setFont(UIConstants.FONT_BODY);
        txtMedicineSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        txtMedicineSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { searchMedicines(txtMedicineSearch.getText().trim()); }
        });
        searchPanel.add(txtMedicineSearch, BorderLayout.CENTER);
        content.add(searchPanel, BorderLayout.NORTH);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(200);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // Search result table
        String[] searchCols = {"ID", "T\u00EAn thu\u1ED1c", "\u0110\u01A1n v\u1ECB", "Gi\u00E1 b\u00E1n", "T\u1ED3n kho"};
        modelMedicineSearch = new DefaultTableModel(searchCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableMedicineSearch = new JTable(modelMedicineSearch);
        tableMedicineSearch.setRowHeight(32);
        tableMedicineSearch.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMedicineSearch.getTableHeader().setFont(UIConstants.FONT_BOLD);
        tableMedicineSearch.setFont(UIConstants.FONT_LABEL);
        tableMedicineSearch.getColumnModel().getColumn(0).setMinWidth(0);
        tableMedicineSearch.getColumnModel().getColumn(0).setMaxWidth(0);
        tableMedicineSearch.getColumnModel().getColumn(0).setWidth(0);
        tableMedicineSearch.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tableMedicineSearch.getSelectedRow() >= 0) addMedicineToPrescription();
            }
        });

        JPanel searchResultPanel = new JPanel(new BorderLayout());
        searchResultPanel.setBorder(BorderFactory.createTitledBorder("K\u1EBFt qu\u1EA3 t\u00ECm ki\u1EBFm (nh\u1EA5p \u0111\u00FAp \u0111\u1EC3 th\u00EAm)"));
        searchResultPanel.add(new JScrollPane(tableMedicineSearch), BorderLayout.CENTER);
        RoundedButton btnAdd = new RoundedButton("+ Th\u00EAm v\u00E0o \u0111\u01A1n", UIConstants.ACCENT_BLUE, UIConstants.ACCENT_BLUE_DARK, 6);
        btnAdd.setPreferredSize(new Dimension(140, 30));
        btnAdd.addActionListener(e -> addMedicineToPrescription());
        JPanel addBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addBtnPanel.setOpaque(false);
        addBtnPanel.add(btnAdd);
        searchResultPanel.add(addBtnPanel, BorderLayout.SOUTH);
        splitPane.setTopComponent(searchResultPanel);

        // Prescription table
        String[] prescCols = {"T\u00EAn thu\u1ED1c", "\u0110\u01A1n v\u1ECB", "S\u1ED1 l\u01B0\u1EE3ng", "Li\u1EC1u d\u00F9ng", "H\u01B0\u1EDBng d\u1EABn", "\u0110\u01A1n gi\u00E1", "Th\u00E0nh ti\u1EC1n"};
        modelPrescription = new DefaultTableModel(prescCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 2 || c == 3 || c == 4; }
        };
        tablePrescription = new JTable(modelPrescription);
        tablePrescription.setRowHeight(32);
        tablePrescription.getTableHeader().setFont(UIConstants.FONT_BOLD);
        tablePrescription.setFont(UIConstants.FONT_LABEL);
        modelPrescription.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow(); int col = e.getColumn();
                if (row >= 0 && row < prescriptionItems.size()) {
                    PrescriptionDetail item = prescriptionItems.get(row);
                    try {
                        if (col == 2) { item.setQuantity(Integer.parseInt(modelPrescription.getValueAt(row, 2).toString()));
                            modelPrescription.setValueAt(moneyFmt.format(item.getLineTotal()) + " \u0111", row, 6); }
                        else if (col == 3) item.setDosage(modelPrescription.getValueAt(row, 3).toString());
                        else if (col == 4) item.setInstruction(modelPrescription.getValueAt(row, 4).toString());
                    } catch (NumberFormatException ignored) {}
                }
            }
        });

        JPanel prescPanel = new JPanel(new BorderLayout());
        prescPanel.setBorder(BorderFactory.createTitledBorder("\u0110\u01A1n thu\u1ED1c"));
        prescPanel.add(new JScrollPane(tablePrescription), BorderLayout.CENTER);
        RoundedButton btnRemove = new RoundedButton("X\u00F3a d\u00F2ng", UIConstants.ERROR_COLOR, UIConstants.PRIMARY_DARK, 6);
        btnRemove.setPreferredSize(new Dimension(100, 30));
        btnRemove.addActionListener(e -> removePrescriptionItem());
        JPanel removeBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        removeBtnPanel.setOpaque(false);
        removeBtnPanel.add(btnRemove);
        prescPanel.add(removeBtnPanel, BorderLayout.SOUTH);
        splitPane.setBottomComponent(prescPanel);
        content.add(splitPane, BorderLayout.CENTER);

        loadExistingPrescription();
        searchMedicines("");
        return content;
    }

    private void searchMedicines(String keyword) {
        modelMedicineSearch.setRowCount(0);
        List<Medicine> medicines = (keyword == null || keyword.isEmpty()) ? medicineBUS.findAll() : medicineBUS.findByName(keyword);
        for (Medicine m : medicines) {
            modelMedicineSearch.addRow(new Object[]{ m.getId(), m.getMedicineName(), m.getUnit(), moneyFmt.format(m.getSellPrice()) + " \u0111", m.getStockQty() });
        }
    }

    private void addMedicineToPrescription() {
        int row = tableMedicineSearch.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui l\u00F2ng ch\u1ECDn thu\u1ED1c.", "Th\u00F4ng b\u00E1o", JOptionPane.WARNING_MESSAGE); return; }

        int medicineId = (int) modelMedicineSearch.getValueAt(row, 0);
        String medicineName = modelMedicineSearch.getValueAt(row, 1).toString();
        String unit = modelMedicineSearch.getValueAt(row, 2).toString();

        for (PrescriptionDetail d : prescriptionItems) {
            if (d.getMedicineId() == medicineId) { JOptionPane.showMessageDialog(this, "Thu\u1ED1c n\u00E0y \u0111\u00E3 c\u00F3 trong \u0111\u01A1n.", "Th\u00F4ng b\u00E1o", JOptionPane.WARNING_MESSAGE); return; }
        }

        Medicine med = medicineBUS.findAll().stream().filter(m -> m.getId() == medicineId).findFirst().orElse(null);
        double unitPrice = med != null ? med.getSellPrice() : 0;

        // Check allergy
        if (selectedPatient != null) {
            List<String> allergyWarnings = prescriptionBUS.checkAllergies(selectedPatient.getId(), List.of(medicineId));
            if (!allergyWarnings.isEmpty()) {
                int choice = JOptionPane.showConfirmDialog(this,
                        String.join("\n", allergyWarnings) + "\n\nB\u1EA1n c\u00F3 mu\u1ED1n ti\u1EBFp t\u1EE5c th\u00EAm thu\u1ED1c?",
                        "C\u1EA2NH B\u00C1O D\u1ECAM \u1EAENG", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) return;
            }
        }

        String qtyStr = JOptionPane.showInputDialog(this, "S\u1ED1 l\u01B0\u1EE3ng cho '" + medicineName + "':", "1");
        if (qtyStr == null || qtyStr.trim().isEmpty()) return;
        int qty;
        try { qty = Integer.parseInt(qtyStr.trim()); if (qty <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "S\u1ED1 l\u01B0\u1EE3ng kh\u00F4ng h\u1EE3p l\u1EC7.", "L\u1ED7i", JOptionPane.ERROR_MESSAGE); return; }

        PrescriptionDetail detail = new PrescriptionDetail(medicineId, qty, "2 vi\u00EAn x 3 l\u1EA7n/ng\u00E0y", "U\u1ED1ng sau \u0103n", unitPrice);
        detail.setMedicineName(medicineName);
        prescriptionItems.add(detail);
        modelPrescription.addRow(new Object[]{ medicineName, unit, qty, detail.getDosage(), detail.getInstruction(),
                moneyFmt.format(unitPrice) + " \u0111", moneyFmt.format(detail.getLineTotal()) + " \u0111" });
    }

    private void removePrescriptionItem() {
        int row = tablePrescription.getSelectedRow();
        if (row < 0) return;
        prescriptionItems.remove(row);
        modelPrescription.removeRow(row);
    }

    private void loadExistingPrescription() {
        if (selectedRecordId <= 0) return;
        try {
            var prescriptions = prescriptionBUS.getByMedicalRecordId(selectedRecordId);
            for (var p : prescriptions) {
                var details = prescriptionBUS.getDetails(p.getId());
                for (var d : details) {
                    prescriptionItems.add(d);
                    modelPrescription.addRow(new Object[]{
                            d.getMedicineName() != null ? d.getMedicineName() : "Thu\u1ED1c #" + d.getMedicineId(),
                            "", d.getQuantity(), d.getDosage(), d.getInstruction(),
                            moneyFmt.format(d.getUnitPrice()) + " \u0111", moneyFmt.format(d.getLineTotal()) + " \u0111" });
                }
            }
        } catch (Exception ignored) {}
    }

    // ==========================================================================
    //  TAB 3 - Lich su kham
    // ==========================================================================

    private JPanel createHistoryContent() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);

        JLabel title = new JLabel("L\u1ECBch s\u1EED kh\u00E1m b\u1EC7nh \u2014 " + selectedPatient.getFullName());
        title.setFont(UIConstants.FONT_SECTION);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        content.add(title, BorderLayout.NORTH);

        String[] cols = {"Ng\u00E0y kh\u00E1m", "Tri\u1EC7u ch\u1EE9ng", "Ch\u1EA9n \u0111o\u00E1n", "M\u00E3 ICD-10", "Tr\u1EA1ng th\u00E1i"};
        DefaultTableModel historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable historyTable = new JTable(historyModel);
        historyTable.setRowHeight(36);
        historyTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        historyTable.setFont(UIConstants.FONT_LABEL);

        try {
            List<MedicalRecord> records = medicalRecordBUS.getHistoryByPatient(selectedPatient.getId());
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (MedicalRecord r : records) {
                historyModel.addRow(new Object[]{
                        r.getVisitDate() != null ? r.getVisitDate().format(dtf) : "N/A",
                        r.getSymptoms() != null ? (r.getSymptoms().length() > 60 ? r.getSymptoms().substring(0, 60) + "..." : r.getSymptoms()) : "",
                        r.getDiagnosis() != null ? r.getDiagnosis() : "",
                        r.getDiagnosisCode() != null ? r.getDiagnosisCode() : "",
                        r.getStatus() != null ? r.getStatus() : "" });
            }
        } catch (Exception e) {
            historyModel.addRow(new Object[]{"L\u1ED7i t\u1EA3i d\u1EEF li\u1EC7u", e.getMessage(), "", "", ""});
        }

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        content.add(scroll, BorderLayout.CENTER);
        return content;
    }

    // ==========================================================================
    //  BOTTOM BAR & ACTIONS
    // ==========================================================================

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIConstants.CARD_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR),
                new EmptyBorder(14, 24, 14, 24)));
        return bar;
    }

    private void updateBottomBar() {
        if (bottomBar == null) return;
        bottomBar.removeAll();

        JLabel status = new JLabel("Tr\u1EA1ng th\u00E1i: " + (selectedPatient != null && "EXAMINING".equals(selectedPatient.getStatus()) ? "\u0110ang kh\u00E1m..." : "Ch\u1EDD kh\u00E1m"));
        status.setFont(UIConstants.FONT_ITALIC);
        status.setForeground(selectedPatient != null && "EXAMINING".equals(selectedPatient.getStatus()) ? UIConstants.STATUS_EXAMINING : UIConstants.STATUS_WAITING);
        bottomBar.add(status, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btns.setOpaque(false);

        if (selectedPatient != null) {
            if (activeTab == 0) {
                RoundedButton btnSaveVitals = new RoundedButton("L\u01B0u sinh hi\u1EC7u", UIConstants.ACCENT_BLUE, UIConstants.ACCENT_BLUE_DARK, 8);
                btnSaveVitals.setPreferredSize(new Dimension(140, 40));
                btnSaveVitals.addActionListener(e -> onSaveVitals());
                btns.add(btnSaveVitals);
            } else if (activeTab == 1) {
                RoundedButton btnSaveExam = new RoundedButton("L\u01B0u kh\u00E1m b\u1EC7nh", UIConstants.ACCENT_BLUE, UIConstants.ACCENT_BLUE_DARK, 8);
                btnSaveExam.setPreferredSize(new Dimension(150, 40));
                btnSaveExam.addActionListener(e -> onSaveExamination());
                btns.add(btnSaveExam);
            } else if (activeTab == 2) {
                RoundedButton btnSave = new RoundedButton("Ho\u00E0n t\u1EA5t kh\u00E1m", UIConstants.SUCCESS_GREEN, UIConstants.SUCCESS_GREEN_DARK, 8);
                btnSave.setPreferredSize(new Dimension(160, 40));
                btnSave.addActionListener(e -> onSaveAndComplete());
                btns.add(btnSave);
            } else if (activeTab == 3) {
                // Lab orders tab - no extra buttons needed, LabOrderPanel has its own
            }
        }
        
        bottomBar.add(btns, BorderLayout.EAST);
        bottomBar.revalidate();
        bottomBar.repaint();
    }

    private void onSaveExamination() {
        if (selectedPatient == null || selectedRecordId <= 0) {
            JOptionPane.showMessageDialog(this, "Vui l\u00F2ng ch\u1ECDn b\u1EC7nh nh\u00E2n.", "Th\u00F4ng b\u00E1o", JOptionPane.WARNING_MESSAGE); return;
        }
        try {
            captureCurrentTabState(); // Ensure we have latest data
            Map<String, Object> symptomsMap = tabStates.containsKey(1) ? tabStates.get(1) : symptomsPanel.captureState();

            String symptoms = (String) symptomsMap.getOrDefault("symptoms", "");
            String diagnosis = (String) symptomsMap.getOrDefault("diagnosis", "");
            String diagnosisCode = (String) symptomsMap.getOrDefault("diagnosisCode", "");
            String doctorNotes = (String) symptomsMap.getOrDefault("doctorNotes", "");
            String followUpStr = (String) symptomsMap.getOrDefault("followUpDate", "");

            LocalDate followUpDate = null;
            if (!followUpStr.isEmpty()) {
                try { followUpDate = LocalDate.parse(followUpStr, DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ng\u00E0y t\u00E1i kh\u00E1m kh\u00F4ng h\u1EE3p l\u1EC7. S\u1EED d\u1EE5ng dd/MM/yyyy", "L\u1ED7i", JOptionPane.ERROR_MESSAGE); return;
                }
            }

            medicalRecordBUS.updateFullExamination(selectedRecordId,
                    diagnosis, symptoms,
                    diagnosisCode.isEmpty() ? null : diagnosisCode, doctorNotes.isEmpty() ? null : doctorNotes, followUpDate);
            JOptionPane.showMessageDialog(this, "\u0110\u00E3 l\u01B0u kh\u00E1m b\u1EC7nh cho: " + selectedPatient.getFullName(), "Th\u00E0nh c\u00F4ng", JOptionPane.INFORMATION_MESSAGE);
        } catch (BusinessException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "L\u1ED7i nghi\u1EC7p v\u1EE5", JOptionPane.ERROR_MESSAGE);
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, "L\u1ED7i h\u1EC7 th\u1ED1ng: " + e.getMessage(), "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSaveVitals() {
        if (selectedPatient == null || selectedRecordId <= 0) {
            JOptionPane.showMessageDialog(this, "Vui l\u00F2ng ch\u1ECDn b\u1EC7nh nh\u00E2n.", "Th\u00F4ng b\u00E1o", JOptionPane.WARNING_MESSAGE); return;
        }
        try {
            captureCurrentTabState(); // Ensure we have latest data
            Map<String, Object> vitals = tabStates.containsKey(0) ? tabStates.get(0) : vitalSignsPanel.captureState();

            double weight = parseDoubleFromMap(vitals, "weight", "C\u00E2n n\u1EB7ng");
            double height = parseDoubleFromMap(vitals, "height", "Chi\u1EC1u cao");
            String bp = (String) vitals.getOrDefault("bp", "");
            int pulse = parseIntFromMap(vitals, "pulse", "M\u1EA1ch");
            double temp = parseDoubleFromMapOrZero(vitals, "temp");
            int spo2Val = parseIntFromMapOrZero(vitals, "spo2");
            medicalRecordBUS.updateVitalSigns(selectedRecordId, weight, height, bp, pulse, temp, spo2Val);
            JOptionPane.showMessageDialog(this, "\u0110\u00E3 l\u01B0u sinh hi\u1EC7u cho: " + selectedPatient.getFullName(), "Th\u00E0nh c\u00F4ng", JOptionPane.INFORMATION_MESSAGE);
        } catch (BusinessException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "L\u1ED7i nghi\u1EC7p v\u1EE5", JOptionPane.ERROR_MESSAGE);
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, "L\u1ED7i h\u1EC7 th\u1ED1ng: " + e.getMessage(), "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "L\u1ED7i d\u1EEF li\u1EC7u", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Hoan tat kham:
     * 1. Luu sinh hieu
     * 2. Luu trieu chung + chan doan + ICD-10 + ghi chu + ngay tai kham
     * 3. Tao don thuoc neu co
     * 4. Chuyen trang thai -> PRESCRIBED (neu co don thuoc) hoac COMPLETED (neu khong)
     */
    private void onSaveAndComplete() {
        if (selectedPatient == null || selectedRecordId <= 0) {
            JOptionPane.showMessageDialog(this, "Vui l\u00F2ng ch\u1ECDn b\u1EC7nh nh\u00E2n.", "Th\u00F4ng b\u00E1o", JOptionPane.WARNING_MESSAGE); return;
        }
        try {
            captureCurrentTabState(); // Make sure latest inputs from any open tab are saved to tabStates

            // Ensure DB has at least empty examination if not previously saved
            medicalRecordBUS.findById(selectedRecordId); // check exists
            
            // Generate Prescription
            boolean hasPrescription = !prescriptionItems.isEmpty();
            if (hasPrescription) {
                List<Integer> medIds = prescriptionItems.stream().map(PrescriptionDetail::getMedicineId).toList();
                List<String> allergyWarnings = prescriptionBUS.checkAllergies(selectedPatient.getId(), medIds);
                if (!allergyWarnings.isEmpty()) {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "C\u1EA2NH B\u00C1O D\u1ECAM \u1EAENG:\n" + String.join("\n", allergyWarnings) + "\n\nTi\u1EBFp t\u1EE5c l\u01B0u \u0111\u01A1n thu\u1ED1c?",
                            "C\u1EA2NH B\u00C1O D\u1ECAM \u1EAENG", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choice != JOptionPane.YES_OPTION) return;
                }
                prescriptionBUS.createPrescription(selectedRecordId, prescriptionItems);
            }

            // Status: PRESCRIBED if has prescription, else COMPLETED
            String newStatus = hasPrescription ? MedicalRecord.STATUS_PRESCRIBED : MedicalRecord.STATUS_COMPLETED;
            // Cập nhật trạng thái trên MedicalRecord (queue_status)
            medicalRecordBUS.updateStatus(selectedRecordId, newStatus);

            // Cập nhật QueueEntry → COMPLETED (bệnh nhân đã rời hàng đợi)
            try {
                // Tìm QueueEntry tương ứng qua patient và đánh dấu hoàn tất
                var waitingEntries = queueBUS.getTodayQueue();
                for (var entry : waitingEntries) {
                    if (entry.getPatientId() == selectedPatient.getId()
                            && entry.getStatus() == com.hospital.model.QueueEntry.QueueStatus.IN_PROGRESS) {
                        queueBUS.updateQueueEntryStatus(entry.getId(), com.hospital.model.QueueEntry.QueueStatus.COMPLETED);
                        break;
                    }
                }
            } catch (Exception ignored) {
                // QueueEntry update is best-effort
            }

            String msg = hasPrescription
                    ? "\u0110\u00E3 ho\u00E0n t\u1EA5t kh\u00E1m v\u00E0 k\u00EA \u0111\u01A1n cho: " + selectedPatient.getFullName() + "\nTr\u1EA1ng th\u00E1i: CH\u1ECC PH\u00C1T THU\u1ED0C"
                    : "\u0110\u00E3 ho\u00E0n t\u1EA5t kh\u00E1m cho: " + selectedPatient.getFullName();
            JOptionPane.showMessageDialog(this, msg, "Th\u00E0nh c\u00F4ng", JOptionPane.INFORMATION_MESSAGE);
            refreshAfterAction();

        } catch (BusinessException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "L\u1ED7i nghi\u1EC7p v\u1EE5", JOptionPane.ERROR_MESSAGE);
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, "L\u1ED7i h\u1EC7 th\u1ED1ng: " + e.getMessage(), "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================================================
    //  HELPERS
    // ==========================================================================

    private String getTextAreaValue(JTextArea area, String placeholder) {
        if (area == null) return null;
        String text = area.getText().trim();
        if (text.isEmpty() || text.equals(placeholder)) return null;
        return text;
    }

    private double parseDoubleFromMap(Map<String, Object> map, String key, String name) {
        String text = (String) map.getOrDefault(key, "");
        if (text.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(name + " ph\u1EA3i l\u00E0 s\u1ED1 h\u1EE3p l\u1EC7.");
        }
    }

    private int parseIntFromMap(Map<String, Object> map, String key, String name) {
        String text = (String) map.getOrDefault(key, "");
        if (text.isEmpty()) return 0;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(name + " ph\u1EA3i l\u00E0 s\u1ED1 h\u1EE3p l\u1EC7 nguyen.");
        }
    }

    private double parseDoubleFromMapOrZero(Map<String, Object> map, String key) {
        String text = (String) map.getOrDefault(key, "");
        if (text.isEmpty()) return 0;
        try { return Double.parseDouble(text); } catch (NumberFormatException e) { return 0; }
    }

    private int parseIntFromMapOrZero(Map<String, Object> map, String key) {
        String text = (String) map.getOrDefault(key, "");
        if (text.isEmpty()) return 0;
        try { return Integer.parseInt(text); } catch (NumberFormatException e) { return 0; }
    }

    private void refreshAfterAction() {
        selectedIndex = -1;
        selectedPatient = null;
        selectedRecordId = -1;
        prescriptionItems.clear();
        List<Patient> waiting = queueBUS.getWaitingPatients();
        if (!waiting.isEmpty()) {
            selectedIndex = 0;
            selectedPatient = waiting.get(0);
            selectedRecordId = selectedPatient.getCurrentRecordId();
        }
        loadPatientList();
        updateRightPanel();
    }
}
