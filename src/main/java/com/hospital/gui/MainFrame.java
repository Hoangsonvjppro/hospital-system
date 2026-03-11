package com.hospital.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Cửa sổ chính mặc định (fallback cho role chưa cấu hình).
 * Main application window — fallback for unconfigured roles.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Hospital Management System");
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIConstants.CONTENT_BG);
        setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 60, 40, 60));

        JLabel icon = new JLabel(com.hospital.gui.IconManager.getIcon("hospital", 48, 48), SwingConstants.CENTER);
        icon.setFont(UIConstants.FONT_ICON);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(icon);
        card.add(Box.createVerticalStrut(16));

        JLabel title = new JLabel("Phòng Mạch Tư");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel("Vai trò của bạn chưa được cấu hình. Vui lòng liên hệ quản trị viên.");
        subtitle.setFont(UIConstants.FONT_LABEL);
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);

        add(card, new GridBagConstraints());
    }
}
