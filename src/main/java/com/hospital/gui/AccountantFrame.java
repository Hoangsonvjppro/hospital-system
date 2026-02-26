package com.hospital.gui;

import com.hospital.gui.panels.MedicinePanel;
import com.hospital.model.Account;

import javax.swing.*;

/**
 * Frame chính dành cho Kế toán — kế thừa BaseFrame.
 */
public class AccountantFrame extends BaseFrame {

    public AccountantFrame(Account account) {
        super(account, "Kế toán", "💰");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("💊", "Kho thuốc",
                () -> showPanel(new MedicinePanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new MedicinePanel();
    }
}
