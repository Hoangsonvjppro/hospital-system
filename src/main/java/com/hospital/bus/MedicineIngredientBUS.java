package com.hospital.bus;

import com.hospital.dao.MedicineIngredientDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.MedicineIngredient;

import java.util.List;

/**
 * Business logic layer cho thành phần thuốc (MedicineIngredient).
 */
public class MedicineIngredientBUS extends BaseBUS<MedicineIngredient> {

    private final MedicineIngredientDAO ingredientDAO;

    public MedicineIngredientBUS() {
        super(new MedicineIngredientDAO());
        this.ingredientDAO = (MedicineIngredientDAO) dao;
    }

    @Override
    protected void validate(MedicineIngredient i) {
        if (i == null) throw new BusinessException("Dữ liệu thành phần thuốc không hợp lệ");
        if (i.getMedicineId() <= 0)
            throw new BusinessException("Mã thuốc không hợp lệ");
        if (i.getIngredientName() == null || i.getIngredientName().trim().isEmpty())
            throw new BusinessException("Tên thành phần không được để trống");
    }

    public List<MedicineIngredient> findByMedicineId(long medicineId) {
        return ingredientDAO.findByMedicineId(medicineId);
    }
}
