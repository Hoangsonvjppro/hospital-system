package com.hospital.gui.admin;

import com.hospital.gui.BaseFrame;
import com.hospital.gui.receptionist.ReceptionPanel;
import com.hospital.gui.receptionist.PaymentPanel;
import com.hospital.gui.doctor.ExaminationPanel;
import com.hospital.gui.pharmacist.DispensingPanel;
import com.hospital.model.Account;

import javax.swing.*;

public class AdminFrame extends BaseFrame {

    public AdminFrame(Account account) {
        super(account, "Quản trị", "🛡️");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("📊", "Tổng quan", () -> showPanel(new AdminDashboardPanel()));

        addSeparator();
        addSectionLabel("Quy trình khám");

        addMenuItem("📋", "Tiếp nhận", () -> showPanel(new ReceptionPanel()));
        addMenuItem("🩺", "Khám bệnh", () -> showPanel(new ExaminationPanel()));
        addMenuItem("💊", "Phát thuốc", () -> showPanel(new DispensingPanel()));
        addMenuItem("💰", "Thanh toán", () -> showPanel(new PaymentPanel()));

        addSeparator();
        addSectionLabel("Quản lý");

        addMenuItem("👤", "Tài khoản", () -> showPanel(new AccountManagementPanel()));
        addMenuItem("🏥", "Dịch vụ & Giá", () -> showPanel(new ServiceManagementPanel()));
        addMenuItem("⚙", "Cấu hình", () -> showPanel(new ClinicConfigPanel()));
        addMenuItem("📈", "Báo cáo", () -> showPanel(new SystemReportPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new AdminDashboardPanel();
    }
}
