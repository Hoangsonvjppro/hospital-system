package com.hospital;

import com.formdev.flatlaf.FlatLightLaf;
import com.hospital.gui.LoginFrame;
import javax.swing.*;

/**
 * Điểm khởi đầu của ứng dụng.
 * Entry point of the application.
 */
public class Main {
    public static void main(String[] args) {
        // Đặt FlatLaf Look and Feel
        System.out.println("hghg");
        try {
            FlatLightLaf.setup();
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.focusWidth", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Chạy GUI trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
