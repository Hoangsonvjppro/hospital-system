package com.hospital.bus;

import com.hospital.dao.MedicineDAO;
import com.hospital.model.Medicine;

import java.util.List;

/**
 * Business logic layer cho kho thuốc.
 */
public class MedicineBUS extends BaseBUS<Medicine> {

    private final MedicineDAO medicineDAO;

    public MedicineBUS() {
        super(new MedicineDAO());
        this.medicineDAO = (MedicineDAO) dao;
    }

    @Override
    protected boolean validate(Medicine m) {
        if (m == null) return false;
        if (m.getName() == null || m.getName().trim().isEmpty()) return false;
        if (m.getPrice() < 0) return false;
        if (m.getQuantity() < 0) return false;
        return true;
    }

    public List<Medicine> getLowStockMedicines() {
        return medicineDAO.findLowStock();
    }

    public int countLowStock() {
        return medicineDAO.countLowStock();
    }

    public boolean adjustStock(int medicineId, int delta) {
        Medicine m = medicineDAO.findById(medicineId);
        if (m == null) return false;
        int newQty = m.getQuantity() + delta;
        if (newQty < 0) return false;
        m.setQuantity(newQty);
        return medicineDAO.update(m);
    }
}
