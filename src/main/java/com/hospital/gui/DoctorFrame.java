package com.hospital.gui;

import com.hospital.gui.panels.DoctorDashboardPanel;
import com.hospital.gui.panels.DoctorWorkstationPanel;
import com.hospital.gui.panels.ExaminationPanel;
import com.hospital.model.Account;

import javax.swing.*;

/**
 * Frame chính dành cho Bác sĩ — kế thừa BaseFrame.
 * Menu: Danh sách chờ khám, Khám bệnh, Kê đơn.
 */
public class DoctorFrame extends BaseFrame {

    public DoctorFrame(Account account) {
        super(account, "Bác sĩ", "🩺");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("📋", "Danh sách chờ khám",
                () -> showPanel(new DoctorDashboardPanel()));

        addMenuItem("🩺", "Khám bệnh",
                () -> showPanel(new DoctorWorkstationPanel()));

        addDisabledMenuItem("📝", "Kê đơn");
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new DoctorDashboardPanel();
    }
}
