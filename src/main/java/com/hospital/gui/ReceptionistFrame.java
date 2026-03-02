package com.hospital.gui;

import com.hospital.gui.panels.PatientPanel;
import com.hospital.gui.panels.ReceptionPanel;
import com.hospital.model.Account;

import javax.swing.*;

/**
 * Frame chính dành cho Lễ tân — kế thừa BaseFrame.
 * Hiển thị ReceptionPanel (tiếp nhận bệnh nhân) trong sidebar layout thống nhất.
 */
public class ReceptionistFrame extends BaseFrame {

    public ReceptionistFrame(Account account) {
        super(account, "Lễ tân", "🏥");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("📋", "Tiếp nhận",
                () -> showPanel(new PatientPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new PatientPanel();
    }
}