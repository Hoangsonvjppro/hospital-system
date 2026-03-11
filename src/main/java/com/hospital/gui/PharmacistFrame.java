package com.hospital.gui;

import com.hospital.gui.panels.CompletionPanel;
import com.hospital.gui.panels.MedicinePanel;
import com.hospital.gui.panels.PharmacyPanel;
import com.hospital.model.Account;

import javax.swing.*;

/**
 * Frame chính dành cho Dược sĩ — kế thừa BaseFrame.
 * Menu: Quản lý kho, Phát thuốc, Kết thúc khám.
 */
public class PharmacistFrame extends BaseFrame {

    public PharmacistFrame(Account account) {
        super(account, "Dược sĩ", "pill");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("pill", "Quản lý kho",
                () -> showPanel(new MedicinePanel()));

        addMenuItem("syringe", "Phát thuốc",
                () -> showPanel(new PharmacyPanel()));

        addSeparator();

        addMenuItem("check", "Kết thúc khám",
                () -> showPanel(new CompletionPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new MedicinePanel();
    }
}
