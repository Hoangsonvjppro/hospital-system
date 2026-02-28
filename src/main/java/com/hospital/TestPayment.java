package com.hospital;

import com.formdev.flatlaf.FlatLightLaf;
import com.hospital.gui.UIConstants;
import com.hospital.gui.panels.PaymentPanel;

import javax.swing.*;
import java.awt.*;

/**
 * ★ FILE TẠM — chỉ dùng để test PaymentPanel độc lập.
 *   Chạy:  mvn compile exec:java -Dexec.mainClass="com.hospital.TestPayment"
 *   Hoặc đổi Main class trong run.ps1 tạm thời.
 *   Xóa file này khi tích hợp xong.
 */
public class TestPayment {
    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.focusWidth", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test — Thanh toán & Hóa đơn");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 750);
            frame.setMinimumSize(new Dimension(1000, 600));
            frame.setLocationRelativeTo(null);

            // Thêm PaymentPanel trực tiếp
            PaymentPanel paymentPanel = new PaymentPanel();
            frame.setContentPane(paymentPanel);

            frame.setVisible(true);
        });
    }
}
