package com.hospital.gui;

import com.hospital.gui.panels.SamplePanel;

import javax.swing.*;
import java.awt.*;

/**
 * Cửa sổ chính của ứng dụng.
 * Main application window.
 */
public class MainFrame extends JFrame {
    private JPanel contentPanel;
    private JPanel sidebarPanel;

    public MainFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Hospital Management System");
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Layout chính
        setLayout(new BorderLayout());

        // Sidebar
        sidebarPanel = createSidebar();
        add(sidebarPanel, BorderLayout.WEST);

        // Panel nội dung chính
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(contentPanel, BorderLayout.CENTER);

        // Mặc định hiển thị panel mẫu
        showPanel(new SamplePanel());
    }

    /**
     * Tạo sidebar điều hướng.
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBackground(new Color(44, 62, 80));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tiêu đề sidebar
        JLabel titleLabel = new JLabel("MENU");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(titleLabel);
        sidebar.add(Box.createVerticalStrut(20));

        // Thêm các nút menu ở đây
        // addMenuButton(sidebar, "Trang chủ", e -> showPanel(new HomePanel()));
        // addMenuButton(sidebar, "Bệnh nhân", e -> showPanel(new PatientPanel()));
        // addMenuButton(sidebar, "Bác sĩ", e -> showPanel(new DoctorPanel()));

        JButton sampleBtn = createMenuButton("Mẫu (Sample)");
        sampleBtn.addActionListener(e -> showPanel(new SamplePanel()));
        sidebar.add(sampleBtn);

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    /**
     * Tạo nút menu cho sidebar.
     */
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(52, 73, 94));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Thay đổi panel nội dung hiển thị.
     */
    public void showPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
