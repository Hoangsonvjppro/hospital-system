package com.hospital;

import com.hospital.gui.MainFrame;
import javax.swing.*;

/**
 * Điểm khởi đầu của ứng dụng.
 * Entry point of the application.
 */
public class Main {
    public static void main(String[] args) {
        // Đặt Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Chạy GUI trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
