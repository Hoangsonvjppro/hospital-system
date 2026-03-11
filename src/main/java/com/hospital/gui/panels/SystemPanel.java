package com.hospital.gui.panels;

import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Trang Hệ thống – cấu hình chung.
 */
public class SystemPanel extends JPanel {

    public SystemPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Hệ thống");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Grid of setting cards
        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        grid.add(createSettingCard("hospital", "Thông tin phòng khám",
                "Tên, địa chỉ, hotline, logo phòng khám.",
                "Chỉnh sửa", this::showClinicInfo));

        grid.add(createSettingCard("doctor", "Quản lý bác sĩ",
                "Thêm, sửa, xóa thông tin bác sĩ và chuyên khoa.",
                "Quản lý", this::showDoctorManage));

        grid.add(createSettingCard("lock", "Tài khoản & Mật khẩu",
                "Đổi mật khẩu, phân quyền người dùng.",
                "Cập nhật", this::showAccount));

        grid.add(createSettingCard("cabinet", "Cơ sở dữ liệu",
                "Kết nối CSDL, sao lưu và khôi phục dữ liệu.",
                "Cấu hình", this::showDatabase));

        grid.add(createSettingCard("print", "In ấn & Báo cáo",
                "Mẫu in hóa đơn, đơn thuốc, báo cáo thống kê.",
                "Cài đặt", this::showPrint));

        grid.add(createSettingCard("info", "Về ứng dụng",
                "Phiên bản v1.0.0 – HOSPITAL-SYSTEM © 2026.",
                "Xem", this::showAbout));

        add(grid, BorderLayout.CENTER);
    }

    private JPanel createSettingCard(String icon, String title, String desc, String btnText, Runnable action) {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(24, 22, 22, 22));

        // Icon + Title
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        hdr.setOpaque(false);
        JLabel iconLbl = new JLabel(com.hospital.gui.IconManager.getIcon(icon, 26, 26));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIConstants.FONT_SUBTITLE);
        titleLbl.setForeground(UIConstants.TEXT_PRIMARY);
        hdr.add(iconLbl);
        hdr.add(titleLbl);
        card.add(hdr, BorderLayout.NORTH);

        // Description
        JTextArea descArea = new JTextArea(desc);
        descArea.setFont(UIConstants.FONT_LABEL);
        descArea.setForeground(UIConstants.TEXT_SECONDARY);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        card.add(descArea, BorderLayout.CENTER);

        // Button
        RoundedButton btn = new RoundedButton(btnText);
        btn.setPreferredSize(new Dimension(110, 34));
        btn.addActionListener(e -> action.run());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btn);
        card.add(btnPanel, BorderLayout.SOUTH);
        return card;
    }

    // ── Action handlers ───────────────────────────────────────────────────────
    private void showClinicInfo() {
        JOptionPane.showMessageDialog(this,
            "Tên phòng khám: HOSPITAL-SYSTEM\nĐịa chỉ: 123 Lê Lợi, TP.HCM\nHotline: 1900-xxxx",
            "Thông tin phòng khám", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showDoctorManage() {
        JOptionPane.showMessageDialog(this,
            "Quản lý bác sĩ sẽ được mở trong phiên bản tiếp theo.",
            "Quản lý bác sĩ", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAccount() {
        JPasswordField pwField = new JPasswordField();
        int result = JOptionPane.showConfirmDialog(this, pwField, "Nhập mật khẩu mới:", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION && pwField.getPassword().length > 0) {
            JOptionPane.showMessageDialog(this, "Đã cập nhật mật khẩu.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showDatabase() {
        JOptionPane.showMessageDialog(this,
            "Cấu hình kết nối:\nHost: localhost\nPort: 3306\nDatabase: hospital_db\n(Mock data đang được sử dụng)",
            "Cơ sở dữ liệu", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showPrint() {
        JOptionPane.showMessageDialog(this,
            "Chức năng in ấn sẽ được tích hợp khi kết nối CSDL thực tế.",
            "In ấn & Báo cáo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            "HOSPITAL-SYSTEM v1.0.0\nKiến trúc 3 lớp: GUI – BUS – DAO\nGiao diện: Java Swing + FlatLaf\n© 2026 – Nhóm phát triển",
            "Về ứng dụng", JOptionPane.INFORMATION_MESSAGE);
    }
}
