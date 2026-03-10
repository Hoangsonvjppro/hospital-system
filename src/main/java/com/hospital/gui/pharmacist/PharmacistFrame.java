package com.hospital.gui.pharmacist;

import com.hospital.gui.BaseFrame;
import com.hospital.model.Account;

import javax.swing.*;

public class PharmacistFrame extends BaseFrame {

    public PharmacistFrame(Account account) {
        super(account, "Dược sĩ", "💊");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("💉", "Phát thuốc", () -> showPanel(new DispensingPanel()));

        addSeparator();
        addSectionLabel("Kho dược");

        addMenuItem("💊", "Kho thuốc", () -> showPanel(new MedicinePanel()));
        addMenuItem("📦", "Nhập hàng theo lô", () -> showPanel(new StockImportPanel()));
        addMenuItem("📋", "Lịch sử kho", () -> showPanel(new StockHistoryPanel()));
        addMenuItem("🏭", "Nhà cung cấp", () -> showPanel(new SupplierPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new DispensingPanel();
    }
}
