package com.hospital.gui;

import com.hospital.gui.panels.DashboardPanel;
import com.hospital.gui.panels.MedicinePanel;
import com.hospital.model.Account;

import javax.swing.*;

/**
 * Frame chính dành cho Admin — kế thừa BaseFrame.
 */
public class AdminFrame extends BaseFrame {

    public AdminFrame(Account account) {
        super(account, "Quản trị viên", "🛡️");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("📊", "Tổng quan",
                () -> showPanel(new DashboardPanel(account.getFullName())));

        addMenuItem("💊", "Kho thuốc",
                () -> showPanel(new MedicinePanel()));

        addSeparator();
        addSectionLabel("Sắp ra mắt");

        addDisabledMenuItem("👥", "Bệnh nhân");
        addDisabledMenuItem("🩺", "Phòng khám BS");
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new DashboardPanel(account.getFullName());
    }
}
