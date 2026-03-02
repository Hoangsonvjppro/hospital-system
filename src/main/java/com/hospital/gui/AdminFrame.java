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
        super(account, "Quản trị viên", "🛡️");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("📊", "Tổng quan",
                () -> showPanel(new DashboardPanel(account.getFullName())));

        addSeparator();
        addSectionLabel("Tiếp nhận");

        addMenuItem("📋", "Tiếp nhận BN",
                () -> showPanel(new ReceptionPanel()));
        addMenuItem("🕐", "Hàng đợi",
                () -> showPanel(new DoctorDashboardPanel()));

        addSeparator();
        addSectionLabel("Khám bệnh");

        addMenuItem("🩺", "Khám bệnh",
                () -> showPanel(new DoctorWorkstationPanel()));

        addSeparator();
        addSectionLabel("Dược & Tài chính");

        addMenuItem("💊", "Kho thuốc",
                () -> showPanel(new MedicinePanel()));
        addMenuItem("💰", "Thanh toán",
                () -> showPanel(new PaymentPanel()));

        addSeparator();
        addSectionLabel("Báo cáo");

        addMenuItem("📈", "Doanh thu",
                () -> showPanel(new AdminReportPanel()));

        addSeparator();
        addSectionLabel("Hệ thống");

        addDisabledMenuItem("👤", "Quản lý tài khoản");
        addMenuItem("⚙️", "Cấu hình phòng khám",
                () -> showPanel(new ClinicConfigPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new DashboardPanel(account.getFullName());
    }
}
