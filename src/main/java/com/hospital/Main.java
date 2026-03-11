package com.hospital;

import com.formdev.flatlaf.FlatLightLaf;
import com.hospital.gui.LoginFrame;
import javax.swing.*;


public class Main {
    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.focusWidth", 1);
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.WARNING, "Không thể khởi tạo FlatLaf", e);
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
