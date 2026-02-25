package com.hospital.gui.panels;

import com.hospital.bus.PatientBUS;
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

    private static final Color PRIMARY_BLUE      = new Color(37, 99, 235);
    private static final Color PRIMARY_BLUE_DARK = new Color(29, 78, 216);
    private static final Color PRIMARY_BLUE_LIGHT= new Color(59, 130, 246);
    private static final Color BLUE_BG_SOFT      = new Color(239, 246, 255);
    private static final Color BG_COLOR          = new Color(245, 247, 251);
    private static final Color CARD_BG           = Color.WHITE;
    private static final Color TEXT_DARK          = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY     = new Color(100, 116, 139);
    private static final Color TEXT_MUTED         = new Color(148, 163, 184);
    private static final Color BORDER             = new Color(226, 232, 240);
    private static final Color GREEN_BTN          = new Color(22, 163, 74);
    private static final Color GREEN_BTN_DARK     = new Color(21, 128, 61);

    private static final Font FONT_HEADER        = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_PATIENT_NAME  = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_PATIENT_INFO  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_PATIENT_TIME  = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_BIG_NAME      = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_INFO_DETAIL   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_VITAL_LABEL   = new Font("Segoe UI", Font.BOLD, 10);
    private static final Font FONT_VITAL_VALUE   = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_VITAL_UNIT    = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_TAB           = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_SECTION_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_STATUS        = new Font("Segoe UI", Font.ITALIC, 12);

    private static final String[] TAB_NAMES = {
        "Thong tin & Sinh hieu", "Kham benh", "Chi dinh Dich vu", "Ke don thuoc"
    };

    private final PatientBUS patientBUS = new PatientBUS();

    private JPanel patientListPanel;
    private JPanel rightContentPanel;
    private JLabel lblPatientCount;
    private Patient selectedPatient;
    private int selectedIndex = -1;

    private JTextField txtWeight;
    private JTextField txtHeight;
    private JTextField txtBloodPressure;
    private JTextField txtPulse;

    private int activeTab = 0;
    private JPanel tabBar;

    public DoctorWorkstationPanel() {
        setBackground(BG_COLOR);
        setLayout(new BorderLayout(0, 0));
        initComponents();
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
        left.setBackground(CARD_BG);
        left.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setBorder(new EmptyBorder(20, 18, 16, 18));

        JLabel lblTitle = new JLabel("DANH SACH CHO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(TEXT_SECONDARY);

        List<Patient> waiting = patientBUS.getWaitingPatients();
        lblPatientCount = new JLabel(waiting.size() + " Patients");
        lblPatientCount.setFont(FONT_HEADER);
        lblPatientCount.setForeground(TEXT_DARK);

        JLabel badge = new JLabel("Queue") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
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
        patientListPanel.setBackground(CARD_BG);
        patientListPanel.setBorder(new EmptyBorder(0, 12, 12, 12));
        loadPatientList();

        JScrollPane scroll = new JScrollPane(patientListPanel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(CARD_BG);
        left.add(scroll, BorderLayout.CENTER);

        return left;
    }

    private void loadPatientList() {
        patientListPanel.removeAll();
        List<Patient> waiting = patientBUS.getWaitingPatients();
        lblPatientCount.setText(waiting.size() + " Patients");

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
                g2.setColor(isSelected ? BLUE_BG_SOFT : CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(isSelected ? PRIMARY_BLUE : BORDER);
                g2.setStroke(new BasicStroke(isSelected ? 2f : 1f));
                float off = isSelected ? 1f : 0.5f;
                g2.draw(new RoundRectangle2D.Float(off, off, getWidth() - 2 * off, getHeight() - 2 * off, 12, 12));
                if (isSelected) {
                    g2.setColor(PRIMARY_BLUE);
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
                g2.setColor(isSelected ? PRIMARY_BLUE : new Color(226, 232, 240));
                g2.fill(new Ellipse2D.Float(0, 0, 32, 32));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        numLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        numLabel.setForeground(isSelected ? Color.WHITE : TEXT_SECONDARY);
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
        nameLabel.setFont(FONT_PATIENT_NAME);
        nameLabel.setForeground(isSelected ? PRIMARY_BLUE : TEXT_DARK);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(nameLabel);
        info.add(Box.createVerticalStrut(2));

        JLabel detailLabel = new JLabel(patient.getGender() + " - " + patient.getAge() + " tuoi");
        detailLabel.setFont(FONT_PATIENT_INFO);
        detailLabel.setForeground(TEXT_SECONDARY);
        detailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(detailLabel);
        info.add(Box.createVerticalStrut(4));

        JLabel timeLabel = new JLabel(patient.getArrivalTime());
        timeLabel.setFont(FONT_PATIENT_TIME);
        timeLabel.setForeground(TEXT_MUTED);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(timeLabel);

        card.add(info, BorderLayout.CENTER);

        if (isSelected || "DANG KHAM".equals(patient.getStatus())) {
            JLabel dot = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isSelected ? PRIMARY_BLUE : UIConstants.STATUS_EXAMINING);
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
                loadPatientList();
                updateRightPanel();
            }
        });

        return card;
    }

    // RIGHT PANEL
    private JPanel createRightPanel() {
        JPanel right = new JPanel(new BorderLayout(0, 0));
        right.setBackground(BG_COLOR);

        tabBar = buildTabBar();
        right.add(tabBar, BorderLayout.NORTH);

        rightContentPanel = new JPanel(new BorderLayout());
        rightContentPanel.setOpaque(false);
        rightContentPanel.setBorder(new EmptyBorder(24, 32, 24, 32));
        right.add(rightContentPanel, BorderLayout.CENTER);

        right.add(createBottomBar(), BorderLayout.SOUTH);

        List<Patient> waiting = patientBUS.getWaitingPatients();
        if (!waiting.isEmpty()) {
            selectedIndex = 0;
            selectedPatient = waiting.get(0);
            loadPatientList();
            updateRightPanel();
        } else {
            showEmptyState();
        }

        return right;
    }

    private JPanel buildTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(CARD_BG);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int idx = i;
            JLabel tab = new JLabel(TAB_NAMES[i]) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (idx == activeTab) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(PRIMARY_BLUE);
                        g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                        g2.dispose();
                    }
                }
            };
            tab.setFont(FONT_TAB);
            tab.setForeground(idx == activeTab ? PRIMARY_BLUE : TEXT_SECONDARY);
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
                    if (idx != activeTab) tab.setForeground(PRIMARY_BLUE_LIGHT);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    tab.setForeground(idx == activeTab ? PRIMARY_BLUE : TEXT_SECONDARY);
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
                        g2.setColor(PRIMARY_BLUE);
                        g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                        g2.dispose();
                    }
                }
            };
            tab.setFont(FONT_TAB);
            tab.setForeground(idx == activeTab ? PRIMARY_BLUE : TEXT_SECONDARY);
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
                case 2 -> rightContentPanel.add(createPlaceholderTab("Chi dinh Dich vu"), BorderLayout.CENTER);
                case 3 -> rightContentPanel.add(createPlaceholderTab("Ke don thuoc"), BorderLayout.CENTER);
            }
        }
        rightContentPanel.revalidate();
        rightContentPanel.repaint();
    }

    private void showEmptyState() {
        JLabel empty = new JLabel("Chua co benh nhan trong danh sach cho", SwingConstants.CENTER);
        empty.setFont(UIConstants.FONT_SUBTITLE);
        empty.setForeground(TEXT_MUTED);
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
        card.setBackground(CARD_BG);
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
                g2.setColor(new Color(226, 232, 240));
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
        name.setFont(FONT_BIG_NAME);
        name.setForeground(TEXT_DARK);
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
        detail.setFont(FONT_INFO_DETAIL);
        detail.setForeground(TEXT_SECONDARY);
        gbc = new GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(detail, gbc);

        // Address
        JLabel address = new JLabel("Dia chi: " + selectedPatient.getAddress());
        address.setFont(FONT_INFO_DETAIL);
        address.setForeground(TEXT_SECONDARY);
        gbc = new GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(address, gbc);

        // Edit button
        JLabel editBtn = new JLabel("Edit Info");
        editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        editBtn.setForeground(PRIMARY_BLUE);
        editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JOptionPane.showMessageDialog(DoctorWorkstationPanel.this,
                        "Chuc nang chinh sua thong tin benh nhan.", "Edit Info",
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

        JLabel title = new JLabel("Vital Signs");
        title.setFont(FONT_SECTION_TITLE);
        title.setForeground(TEXT_DARK);
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

        grid.add(createVitalCard("CAN NANG", txtWeight, "kg"));
        grid.add(createVitalCard("CHIEU CAO", txtHeight, "cm"));
        grid.add(createVitalCard("HUYET AP", txtBloodPressure, "mmHg"));
        grid.add(createVitalCard("MACH", txtPulse, "bpm"));

        section.add(grid);
        return section;
    }

    private RoundedPanel createVitalCard(String label, JTextField field, String unit) {
        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(CARD_BG);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel mainLabel = new JLabel(label);
        mainLabel.setFont(FONT_VITAL_LABEL);
        mainLabel.setForeground(TEXT_SECONDARY);
        card.add(mainLabel, BorderLayout.NORTH);

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        valuePanel.setOpaque(false);

        field.setFont(FONT_VITAL_VALUE);
        field.setForeground(TEXT_DARK);
        field.setBorder(null);
        field.setBackground(CARD_BG);
        field.setColumns(4);
        field.setCaretColor(PRIMARY_BLUE);
        valuePanel.add(field);

        JLabel unitLabel = new JLabel(unit);
        unitLabel.setFont(FONT_VITAL_UNIT);
        unitLabel.setForeground(TEXT_MUTED);
        valuePanel.add(unitLabel);

        card.add(valuePanel, BorderLayout.CENTER);

        return card;
    }

    // TAB 1 - Kham benh
    private JPanel createExaminationContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(createTextSection("Trieu chung (Symptoms)", "Nhap trieu chung cua benh nhan..."));
        content.add(Box.createVerticalStrut(20));
        content.add(createTextSection("Chan doan (Diagnosis)", "Nhap chan doan..."));
        return content;
    }

    private JPanel createTextSection(String title, String placeholder) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_SECTION_TITLE);
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(lbl);
        section.add(Box.createVerticalStrut(10));

        JTextArea area = new JTextArea(5, 0);
        area.setFont(UIConstants.FONT_LABEL);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretColor(PRIMARY_BLUE);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)));
        area.setText(placeholder);
        area.setForeground(TEXT_MUTED);
        area.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (area.getText().equals(placeholder)) {
                    area.setText("");
                    area.setForeground(TEXT_DARK);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (area.getText().isEmpty()) {
                    area.setText(placeholder);
                    area.setForeground(TEXT_MUTED);
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
        lbl.setForeground(TEXT_MUTED);
        panel.add(lbl);
        return panel;
    }

    // BOTTOM BAR
    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(CARD_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(14, 24, 14, 24)));

        JLabel status = new JLabel("Status: Editing...");
        status.setFont(FONT_STATUS);
        status.setForeground(UIConstants.STATUS_WAITING);
        bar.add(status, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btns.setOpaque(false);

        RoundedButton btnSave = new RoundedButton("Luu & Hoan tat", GREEN_BTN, GREEN_BTN_DARK, 8);
        btnSave.setPreferredSize(new Dimension(160, 40));
        btnSave.addActionListener(e -> onSaveAndComplete());

        RoundedButton btnTransfer = new RoundedButton("Chuyen thanh toan", PRIMARY_BLUE, PRIMARY_BLUE_DARK, 8);
        btnTransfer.setPreferredSize(new Dimension(180, 40));
        btnTransfer.addActionListener(e -> onTransferPayment());

        btns.add(btnSave);
        btns.add(btnTransfer);
        bar.add(btns, BorderLayout.EAST);

        return bar;
    }

    // ACTIONS
    private void onSaveAndComplete() {
        if (selectedPatient == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon benh nhan.", "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }
        patientBUS.updateStatus(selectedPatient.getId(), "XONG");
        JOptionPane.showMessageDialog(this,
                "Da luu va hoan tat kham cho benh nhan: " + selectedPatient.getFullName(),
                "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
        refreshAfterAction();
    }

    private void onTransferPayment() {
        if (selectedPatient == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon benh nhan.", "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }
        patientBUS.updateStatus(selectedPatient.getId(), "XONG");
        JOptionPane.showMessageDialog(this,
                "Da chuyen benh nhan " + selectedPatient.getFullName() + " sang thanh toan.",
                "Chuyen thanh toan", JOptionPane.INFORMATION_MESSAGE);
        refreshAfterAction();
    }

    private void refreshAfterAction() {
        selectedIndex = -1;
        selectedPatient = null;
        List<Patient> waiting = patientBUS.getWaitingPatients();
        if (!waiting.isEmpty()) {
            selectedIndex = 0;
            selectedPatient = waiting.get(0);
        }
        loadPatientList();
        updateRightPanel();
    }
}
