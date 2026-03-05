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
        super(account, "Dược sĩ", "💊");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("💊", "Quản lý kho",
                () -> showPanel(new MedicinePanel()));

        addMenuItem("💉", "Phát thuốc",
                () -> showPanel(new PharmacyPanel()));

        addSeparator();

        addMenuItem("✅", "Kết thúc khám",
                () -> showPanel(new CompletionPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new MedicinePanel();
    }
}
