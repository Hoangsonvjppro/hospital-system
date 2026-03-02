package com.hospital.gui;

import com.hospital.gui.panels.AdminReportPanel;
import com.hospital.gui.panels.ClinicConfigPanel;
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
        addSectionLabel("Báo cáo");

        addMenuItem("📈", "Doanh thu",
                () -> showPanel(new AdminReportPanel()));

        addSeparator();
        addSectionLabel("Hệ thống");

        addMenuItem("⚙️", "Cấu hình phòng khám",
                () -> showPanel(new ClinicConfigPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new DashboardPanel(account.getFullName());
    }
}
