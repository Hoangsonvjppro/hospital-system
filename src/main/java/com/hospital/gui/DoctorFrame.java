package com.hospital.gui;

import com.hospital.gui.panels.DoctorDashboardPanel;
import com.hospital.gui.panels.DoctorWorkstationPanel;
import com.hospital.model.Account;

import javax.swing.*;

/**
 * Frame chính dành cho Bác sĩ — kế thừa BaseFrame.
 */
public class DoctorFrame extends BaseFrame {

    public DoctorFrame(Account account) {
        super(account, "Bác sĩ", "🩺");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("📋", "Tổng quan",
                () -> showPanel(new DoctorDashboardPanel()));

        addMenuItem("🏥", "Phòng khám",
                () -> showPanel(new DoctorWorkstationPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new DoctorDashboardPanel();
    }
}
