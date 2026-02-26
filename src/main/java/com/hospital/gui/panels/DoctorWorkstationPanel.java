package com.hospital.gui.panels;

import com.hospital.bus.MedicalRecordBUS;
import com.hospital.bus.QueueBUS;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Doctor Workstation Panel
 */
public class DoctorWorkstationPanel extends JPanel {

    // Tất cả màu/font dùng UIConstants — KHÔNG khai báo cục bộ.

    private static final String[] TAB_NAMES = {
        "Thông tin & Sinh hiệu", "Khám bệnh", "Chỉ định Dịch vụ", "Kê đơn thuốc"
    };

    private final QueueBUS queueBUS = new QueueBUS();
    private final MedicalRecordBUS medicalRecordBUS = new MedicalRecordBUS();

    private JPanel patientListPanel;
    private JPanel rightContentPanel;
    private JLabel lblPatientCount;
    private Patient selectedPatient;
    private long selectedRecordId = -1;
    private int selectedIndex = -1;

    private JTextField txtWeight;
    private JTextField txtHeight;
    private JTextField txtBloodPressure;
    private JTextField txtPulse;

    private JTextArea txtSymptoms;
    private JTextArea txtDiagnosis;

    private static final String SYMPTOMS_PLACEHOLDER = "Nhập triệu chứng của bệnh nhân...";
    private static final String DIAGNOSIS_PLACEHOLDER = "Nhập chẩn đoán...";

    private int activeTab = 0;
    private JPanel tabBar;

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
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setOpaque(false);
        mainPanel.add(createLeftPanel(), BorderLayout.WEST);
        mainPanel.add(createRightPanel(), BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    // LEFT PANEL
    private JPanel createLeftPanel() {
        JPanel left = new JPanel(new BorderLayout(0, 0));
        left.setPreferredSize(new Dimension(260, 0));
        left.setBackground(UIConstants.CARD_BG);
        left.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIConstants.BORDER_COLOR));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.CARD_BG);
        header.setBorder(new EmptyBorder(20, 18, 16, 18));

        JLabel lblTitle = new JLabel("DANH SÁCH CHỜ");
        lblTitle.setFont(UIConstants.FONT_SMALL);
        lblTitle.setForeground(UIConstants.TEXT_SECONDARY);

        List<Patient> waiting = queueBUS.getWaitingPatients();
        lblPatientCount = new JLabel(waiting.size() + " Bệnh nhân");
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
        left.add(header, BorderLayout.NORTH);

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

    private void loadPatientList() {
        patientListPanel.removeAll();
        List<Patient> waiting = queueBUS.getWaitingPatients();
        lblPatientCount.setText(waiting.size() + " Bệnh nhân");

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

        JLabel timeLabel = new JLabel(patient.getArrivalTime());
        timeLabel.setFont(UIConstants.FONT_SMALL);
        timeLabel.setForeground(UIConstants.TEXT_MUTED);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(timeLabel);

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
                // Cập nhật trạng thái: WAITING → EXAMINING
                if ("WAITING".equals(patient.getStatus())) {
                    queueBUS.updateQueueStatus(selectedRecordId, "EXAMINING");
                }
                loadPatientList();
                updateRightPanel();
            }
        });

        return card;
    }

    // RIGHT PANEL
    private JPanel createRightPanel() {
        JPanel right = new JPanel(new BorderLayout(0, 0));
        right.setBackground(UIConstants.CONTENT_BG);

        tabBar = buildTabBar();
        right.add(tabBar, BorderLayout.NORTH);

        rightContentPanel = new JPanel(new BorderLayout());
        rightContentPanel.setOpaque(false);
        rightContentPanel.setBorder(new EmptyBorder(24, 32, 24, 32));
        right.add(rightContentPanel, BorderLayout.CENTER);

        right.add(createBottomBar(), BorderLayout.SOUTH);

        List<Patient> waiting = queueBUS.getWaitingPatients();
        if (!waiting.isEmpty()) {
            selectedIndex = 0;
            selectedPatient = waiting.get(0);
            selectedRecordId = selectedPatient.getCurrentRecordId();
            loadPatientList();
            updateRightPanel();
        } else {
            showEmptyState();
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
            tab.setBorder(new EmptyBorder(14, 24, 14, 24));
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
            tab.setBorder(new EmptyBorder(14, 24, 14, 24));
            tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tab.setOpaque(false);
            tab.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
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

    private void updateRightPanel() {
        rightContentPanel.removeAll();
        if (selectedPatient == null) {
            showEmptyState();
        } else {
            switch (activeTab) {
                case 0 -> rightContentPanel.add(createInfoAndVitalsContent(), BorderLayout.CENTER);
                case 1 -> rightContentPanel.add(createExaminationContent(), BorderLayout.CENTER);
                case 2 -> rightContentPanel.add(createPlaceholderTab("Chỉ định Dịch vụ"), BorderLayout.CENTER);
                case 3 -> rightContentPanel.add(createPlaceholderTab("Kê đơn thuốc"), BorderLayout.CENTER);
            }
        }
        rightContentPanel.revalidate();
        rightContentPanel.repaint();
    }

    private void showEmptyState() {
        JLabel empty = new JLabel("Chưa có bệnh nhân trong danh sách chờ", SwingConstants.CENTER);
        empty.setFont(UIConstants.FONT_SUBTITLE);
        empty.setForeground(UIConstants.TEXT_MUTED);
        rightContentPanel.add(empty, BorderLayout.CENTER);
    }

    // TAB 0 - Thong tin & Sinh hieu
    private JPanel createInfoAndVitalsContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(createPatientInfoCard());
        content.add(Box.createVerticalStrut(24));
        content.add(createVitalSignsSection());
        return content;
    }

    private JPanel createPatientInfoCard() {
        RoundedPanel card = new RoundedPanel(14);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(24, 28, 24, 28));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

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
            @Override
            public Dimension getPreferredSize() { return new Dimension(60, 60); }
            @Override
            public Dimension getMinimumSize() { return getPreferredSize(); }
        };
        avatar.setOpaque(false);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 3;
        gbc.insets = new Insets(0, 0, 0, 20);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        card.add(avatar, gbc);

        // Name
        JLabel name = new JLabel(selectedPatient.getFullName());
        name.setFont(UIConstants.FONT_TITLE);
        name.setForeground(UIConstants.TEXT_PRIMARY);
        gbc = new GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(name, gbc);

        // Detail row
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dobStr = selectedPatient.getDateOfBirth() != null
                ? selectedPatient.getDateOfBirth().format(fmt) : "N/A";
        int age = selectedPatient.getAge();
        String detailText = dobStr + " (" + age + " tuoi)   |   "
                + selectedPatient.getGender() + "   |   " + selectedPatient.getPhone();
        JLabel detail = new JLabel(detailText);
        detail.setFont(UIConstants.FONT_LABEL);
        detail.setForeground(UIConstants.TEXT_SECONDARY);
        gbc = new GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(detail, gbc);

        // Address
        JLabel address = new JLabel("Địa chỉ: " + selectedPatient.getAddress());
        address.setFont(UIConstants.FONT_LABEL);
        address.setForeground(UIConstants.TEXT_SECONDARY);
        gbc = new GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(address, gbc);

        // Edit button
        JLabel editBtn = new JLabel("Chỉnh sửa");
        editBtn.setFont(UIConstants.FONT_LABEL);
        editBtn.setForeground(UIConstants.ACCENT_BLUE);
        editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JOptionPane.showMessageDialog(DoctorWorkstationPanel.this,
                        "Chức năng chỉnh sửa thông tin bệnh nhân.", "Chỉnh sửa",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridheight = 3;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = new Insets(0, 20, 0, 0);
        card.add(editBtn, gbc);

        return card;
    }

    // Vital Signs
    private JPanel createVitalSignsSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Sinh hiệu");
        title.setFont(UIConstants.FONT_SECTION);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(title);
        section.add(Box.createVerticalStrut(16));

        JPanel grid = new JPanel(new GridLayout(1, 4, 16, 0));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        txtWeight = new JTextField("70");
        txtHeight = new JTextField("175");
        txtBloodPressure = new JTextField("120/80");
        txtPulse = new JTextField("80");

        grid.add(createVitalCard("CÂN NẶNG", txtWeight, "kg"));
        grid.add(createVitalCard("CHIỀU CAO", txtHeight, "cm"));
        grid.add(createVitalCard("HUYẾT ÁP", txtBloodPressure, "mmHg"));
        grid.add(createVitalCard("MẠCH", txtPulse, "bpm"));

        section.add(grid);
        return section;
    }

    private RoundedPanel createVitalCard(String label, JTextField field, String unit) {
        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel mainLabel = new JLabel(label);
        mainLabel.setFont(UIConstants.FONT_OVERLINE);
        mainLabel.setForeground(UIConstants.TEXT_SECONDARY);
        card.add(mainLabel, BorderLayout.NORTH);

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        valuePanel.setOpaque(false);

        field.setFont(UIConstants.FONT_NUMBER_BIG);
        field.setForeground(UIConstants.TEXT_PRIMARY);
        field.setBorder(null);
        field.setBackground(UIConstants.CARD_BG);
        field.setColumns(4);
        field.setCaretColor(UIConstants.ACCENT_BLUE);
        valuePanel.add(field);

        JLabel unitLabel = new JLabel(unit);
        unitLabel.setFont(UIConstants.FONT_BODY);
        unitLabel.setForeground(UIConstants.TEXT_MUTED);
        valuePanel.add(unitLabel);

        card.add(valuePanel, BorderLayout.CENTER);

        return card;
    }

    // TAB 1 - Kham benh
    private JPanel createExaminationContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Symptoms
        txtSymptoms = new JTextArea(5, 0);
        content.add(createTextSection("Triệu chứng (Symptoms)", SYMPTOMS_PLACEHOLDER, txtSymptoms));
        content.add(Box.createVerticalStrut(20));

        // Diagnosis
        txtDiagnosis = new JTextArea(5, 0);
        content.add(createTextSection("Chẩn đoán (Diagnosis)", DIAGNOSIS_PLACEHOLDER, txtDiagnosis));
        return content;
    }

    private JPanel createTextSection(String title, String placeholder, JTextArea area) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(title);
        lbl.setFont(UIConstants.FONT_SECTION);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(lbl);
        section.add(Box.createVerticalStrut(10));

        area.setFont(UIConstants.FONT_LABEL);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretColor(UIConstants.ACCENT_BLUE);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 14, 12, 14)));
        area.setText(placeholder);
        area.setForeground(UIConstants.TEXT_MUTED);
        area.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (area.getText().equals(placeholder)) {
                    area.setText("");
                    area.setForeground(UIConstants.TEXT_PRIMARY);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (area.getText().isEmpty()) {
                    area.setText(placeholder);
                    area.setForeground(UIConstants.TEXT_MUTED);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        section.add(scroll);
        return section;
    }

    // PLACEHOLDER TAB
    private JPanel createPlaceholderTab(String tabName) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        JLabel lbl = new JLabel(tabName + " -- Dang phat trien");
        lbl.setFont(UIConstants.FONT_SUBTITLE);
        lbl.setForeground(UIConstants.TEXT_MUTED);
        panel.add(lbl);
        return panel;
    }

    // BOTTOM BAR
    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIConstants.CARD_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR),
                new EmptyBorder(14, 24, 14, 24)));

        JLabel status = new JLabel("Trạng thái: Đang chỉnh sửa...");
        status.setFont(UIConstants.FONT_ITALIC);
        status.setForeground(UIConstants.STATUS_WAITING);
        bar.add(status, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btns.setOpaque(false);

        RoundedButton btnSave = new RoundedButton("Lưu & Hoàn tất", UIConstants.SUCCESS_GREEN, UIConstants.SUCCESS_GREEN_DARK, 8);
        btnSave.setPreferredSize(new Dimension(160, 40));
        btnSave.addActionListener(e -> onSaveAndComplete());

        RoundedButton btnTransfer = new RoundedButton("Chuyển thanh toán", UIConstants.ACCENT_BLUE, UIConstants.ACCENT_BLUE_DARK, 8);
        btnTransfer.setPreferredSize(new Dimension(180, 40));
        btnTransfer.addActionListener(e -> onTransferPayment());

        btns.add(btnSave);
        btns.add(btnTransfer);
        bar.add(btns, BorderLayout.EAST);

        return bar;
    }

    // ACTIONS
    private void onSaveAndComplete() {
        if (selectedPatient == null || selectedRecordId <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bệnh nhân.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            // Luu sinh hieu (vital signs)
            double weight = Double.parseDouble(txtWeight.getText().trim());
            double height = Double.parseDouble(txtHeight.getText().trim());
            String bp = txtBloodPressure.getText().trim();
            int pulse = Integer.parseInt(txtPulse.getText().trim());

            medicalRecordBUS.updateVitalSigns(selectedRecordId, weight, height, bp, pulse);

            // Luu trieu chung + chan doan (neu da nhap)
            String symptoms = getTextAreaValue(txtSymptoms, SYMPTOMS_PLACEHOLDER);
            String diagnosis = getTextAreaValue(txtDiagnosis, DIAGNOSIS_PLACEHOLDER);

            if (symptoms != null && diagnosis != null) {
                medicalRecordBUS.updateDiagnosisAndSymptoms(selectedRecordId, diagnosis, symptoms);
            } else if (symptoms != null) {
                medicalRecordBUS.updateSymptoms(selectedRecordId, symptoms);
            } else if (diagnosis != null) {
                medicalRecordBUS.updateDiagnosis(selectedRecordId, diagnosis);
            }

            // Chuyen trang thai hang doi
            queueBUS.updateQueueStatus(selectedRecordId, "COMPLETED");

            JOptionPane.showMessageDialog(this,
                    "Đã lưu và hoàn tất khám cho bệnh nhân: " + selectedPatient.getFullName(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            refreshAfterAction();
        } catch (BusinessException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Sinh hiệu phải là số hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onTransferPayment() {
        if (selectedPatient == null || selectedRecordId <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bệnh nhân.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            // Luu sinh hieu truoc khi chuyen
            double weight = Double.parseDouble(txtWeight.getText().trim());
            double height = Double.parseDouble(txtHeight.getText().trim());
            String bp = txtBloodPressure.getText().trim();
            int pulse = Integer.parseInt(txtPulse.getText().trim());

            medicalRecordBUS.updateVitalSigns(selectedRecordId, weight, height, bp, pulse);

            // Luu trieu chung + chan doan (neu da nhap)
            String symptoms = getTextAreaValue(txtSymptoms, SYMPTOMS_PLACEHOLDER);
            String diagnosis = getTextAreaValue(txtDiagnosis, DIAGNOSIS_PLACEHOLDER);

            if (symptoms != null && diagnosis != null) {
                medicalRecordBUS.updateDiagnosisAndSymptoms(selectedRecordId, diagnosis, symptoms);
            } else if (symptoms != null) {
                medicalRecordBUS.updateSymptoms(selectedRecordId, symptoms);
            } else if (diagnosis != null) {
                medicalRecordBUS.updateDiagnosis(selectedRecordId, diagnosis);
            }

            queueBUS.updateQueueStatus(selectedRecordId, "TRANSFERRED");
            JOptionPane.showMessageDialog(this,
                    "Đã chuyển bệnh nhân " + selectedPatient.getFullName() + " sang thanh toán.",
                    "Chuyển thanh toán", JOptionPane.INFORMATION_MESSAGE);
            refreshAfterAction();
        } catch (BusinessException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Sinh hiệu phải là số hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Lay gia tri tu JTextArea, tra ve null neu van la placeholder.
     */
    private String getTextAreaValue(JTextArea area, String placeholder) {
        if (area == null) return null;
        String text = area.getText().trim();
        if (text.isEmpty() || text.equals(placeholder)) return null;
        return text;
    }

    private void refreshAfterAction() {
        selectedIndex = -1;
        selectedPatient = null;
        selectedRecordId = -1;
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
