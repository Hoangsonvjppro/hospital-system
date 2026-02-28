package com.hospital.gui;

import com.hospital.gui.panels.DoctorDashboardPanel;
import com.hospital.gui.panels.ReceptionPanel;
import com.hospital.model.Account;

import javax.swing.*;

/**
 * Frame chính dành cho Lễ tân — kế thừa BaseFrame.
 * Menu: Tiếp nhận BN, Hàng đợi.
 */
public class ReceptionistFrame extends BaseFrame {

    public ReceptionistFrame(Account account) {
        super(account, "Lễ tân", "🏥");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("📋", "Tiếp nhận BN",
                () -> showPanel(new ReceptionPanel()));

        addMenuItem("🕐", "Hàng đợi",
                () -> showPanel(new DoctorDashboardPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new ReceptionPanel();
    }
}