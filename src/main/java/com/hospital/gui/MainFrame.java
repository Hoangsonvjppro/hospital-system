package com.hospital.gui;

import com.hospital.gui.admin.AdminFrame;
import com.hospital.gui.accountant.AccountantFrame;
import com.hospital.gui.common.UIConstants;
import com.hospital.gui.doctor.DoctorFrame;
import com.hospital.gui.pharmacist.PharmacistFrame;
import com.hospital.gui.receptionist.ReceptionistFrame;
import com.hospital.model.Account;
import com.hospital.model.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Router — phân vai trò → mở frame tương ứng.
 * Fallback cho role chưa cấu hình.
 */
public class MainFrame extends JFrame {

    public MainFrame(Account account) {
        if (account == null) {
            showFallback();
            return;
        }

        Role role = account.getRole();
        if (role == null) {
            showFallback();
            return;
        }

        JFrame frame = switch (role) {
            case ADMIN        -> new AdminFrame(account);
            case DOCTOR       -> new DoctorFrame(account);
            case RECEPTIONIST -> new ReceptionistFrame(account);
            case PHARMACIST   -> new PharmacistFrame(account);
            case CASHIER      -> new AccountantFrame(account);
        };

        frame.setVisible(true);
        dispose();
    }

    private void showFallback() {
        setTitle("Phòng Mạch Tư");
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

        JLabel icon = new JLabel("🏥", SwingConstants.CENTER);
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
        setVisible(true);
    }
}
