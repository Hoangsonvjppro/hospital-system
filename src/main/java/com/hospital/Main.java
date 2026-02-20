package com.hospital;

import com.formdev.flatlaf.FlatLightLaf;
import com.hospital.gui.LoginFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.*;

/**
 * Điểm khởi đầu của ứng dụng.
 */
public class Main {
    public static void main(String[] args) {
        // Cấu hình FlatLaf
        FlatLightLaf.setup();

        // Tùy chỉnh màu FlatLaf
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("Table.rowHeight", 45);
        UIManager.put("TableHeader.height", 38);

        // Chạy GUI trên Event Dispatch Thread – bắt đầu từ màn hình đăng nhập
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
