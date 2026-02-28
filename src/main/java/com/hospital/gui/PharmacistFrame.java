package com.hospital.gui;

import com.hospital.gui.panels.MedicinePanel;
import com.hospital.model.Account;

import javax.swing.*;

/**
 * Frame chính dành cho Dược sĩ — kế thừa BaseFrame.
 * Menu: Quản lý kho, Phát thuốc.
 */
public class PharmacistFrame extends BaseFrame {

    public PharmacistFrame(Account account) {
        super(account, "Dược sĩ", "💊");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("💊", "Quản lý kho",
                () -> showPanel(new MedicinePanel()));

        addDisabledMenuItem("💉", "Phát thuốc");
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new MedicinePanel();
    }
}
