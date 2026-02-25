package com.hospital.bus;

import com.hospital.dao.PatientDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.Patient;
import com.hospital.util.AppUtils;

/**
 * BUS benh nhan — validation + CRUD.
 * Các method liên quan hàng đợi đã chuyển sang QueueBUS.
 */
public class PatientBUS extends BaseBUS<Patient> {

    public PatientBUS() {
        super(new PatientDAO());
    }

    @Override
    protected boolean validate(Patient entity) {

        if (AppUtils.isNullOrEmpty(entity.getFullName())) {
            throw new BusinessException("Tên bệnh nhân không được để trống.");
        }

        if (AppUtils.isNullOrEmpty(entity.getPhone())) {
            throw new BusinessException("SĐT không được để trống.");
        }

        if (!entity.getPhone().matches("\\d{10}")) {
            throw new BusinessException("SĐT phải đủ 10 chữ số.");
        }

        return true;
    }
}
