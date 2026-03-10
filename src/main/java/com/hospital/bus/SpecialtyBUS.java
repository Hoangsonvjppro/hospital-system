package com.hospital.bus;

import com.hospital.dao.SpecialtyDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.Specialty;

/**
 * Business logic layer cho chuyên khoa (Specialty).
 */
public class SpecialtyBUS extends BaseBUS<Specialty> {

    public SpecialtyBUS() {
        super(new SpecialtyDAO());
    }

    @Override
    protected void validate(Specialty s) {
        if (s == null) throw new BusinessException("Dữ liệu chuyên khoa không hợp lệ");
        if (s.getSpecialtyName() == null || s.getSpecialtyName().trim().isEmpty())
            throw new BusinessException("Tên chuyên khoa không được để trống");
    }
}
