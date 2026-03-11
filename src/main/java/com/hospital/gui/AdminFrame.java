package com.hospital.gui;

import com.hospital.gui.panels.*;
import com.hospital.model.Account;

import javax.swing.*;

/**
 * Frame chính dành cho Admin — kế thừa BaseFrame.
 * Admin có quyền truy cập TẤT CẢ chức năng + Quản lý tài khoản, Báo cáo, Cấu hình.
 */
public class AdminFrame extends BaseFrame {

    public AdminFrame(Account account) {
        super(account, "Quản trị viên", "shield");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("dashboard", "Tổng quan",
                () -> showPanel(new DashboardPanel(account.getFullName())));

        addSeparator();
        addSectionLabel("Tiếp nhận");

        addMenuItem("clipboard", "Tiếp nhận BN",
                () -> showPanel(new ReceptionPanel()));
        addMenuItem("queue", "Hàng đợi",
                () -> showPanel(new DoctorDashboardPanel()));

        addSeparator();
        addSectionLabel("Khám bệnh");

        addMenuItem("stethoscope", "Khám bệnh",
                () -> showPanel(new DoctorWorkstationPanel()));

        addSeparator();
        addSectionLabel("Dược & Tài chính");

        addMenuItem("pill", "Kho thuốc",
                () -> showPanel(new MedicinePanel()));
        addMenuItem("syringe", "Phát thuốc",
                () -> showPanel(new com.hospital.gui.panels.PharmacyPanel()));
        addMenuItem("money", "Thanh toán",
                () -> showPanel(new PaymentPanel()));
        addMenuItem("check", "Kết thúc khám",
                () -> showPanel(new com.hospital.gui.panels.CompletionPanel()));

        addSeparator();
        addSectionLabel("Báo cáo");

        addMenuItem("trending_up", "Doanh thu",
                () -> showPanel(new AdminReportPanel()));

        addSeparator();
        addSectionLabel("Hệ thống");

        addMenuItem("person", "Quản lý tài khoản",
                () -> showPanel(new AccountManagementPanel()));
        addMenuItem("settings", "Cấu hình phòng khám",
                () -> showPanel(new ClinicConfigPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new DashboardPanel(account.getFullName());
    }
}
