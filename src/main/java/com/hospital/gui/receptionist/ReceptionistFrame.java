package com.hospital.gui.receptionist;

import com.hospital.gui.BaseFrame;
import com.hospital.model.Account;

import javax.swing.*;

public class ReceptionistFrame extends BaseFrame {

    public ReceptionistFrame(Account account) {
        super(account, "Lễ tân", "🏥");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("📋", "Tiếp nhận BN", () -> showPanel(new ReceptionPanel()));
        addMenuItem("🕐", "Hàng đợi", () -> showPanel(new QueueDisplayPanel()));

        addSeparator();
        addSectionLabel("Hồ sơ");

        addMenuItem("👤", "Hồ sơ bệnh nhân", () -> showPanel(new PatientPanel()));
        addMenuItem("📅", "Lịch hẹn", () -> showPanel(new AppointmentPanel()));

        addSeparator();
        addSectionLabel("Thu ngân");

        addMenuItem("💰", "Thanh toán", () -> showPanel(new PaymentPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new ReceptionPanel();
    }
}
