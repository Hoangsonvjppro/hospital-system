package com.hospital.gui.lab;

import com.hospital.gui.BaseFrame;
import com.hospital.model.Account;

import javax.swing.*;

public class LabFrame extends BaseFrame {

    public LabFrame(Account account) {
        super(account, "Xét nghiệm", "🔬");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("🔬", "Xử lý xét nghiệm", () -> showPanel(new LabProcessingPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new LabProcessingPanel();
    }
}
