package com.hospital.bus;

import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;
import com.hospital.util.AppUtils;

public class PatientBUS extends BaseBUS<Patient> {

    public PatientBUS() {
        super(new PatientDAO());
    }

    @Override
    protected boolean validate(Patient entity) {

        if (AppUtils.isNullOrEmpty(entity.getFullName())) {
            AppUtils.showError(null, "Tên bệnh nhân không được để trống.");
            return false;
        }

        if (AppUtils.isNullOrEmpty(entity.getPhone())) {
            AppUtils.showError(null, "SĐT không được để trống.");
            return false;
        }

        if (!entity.getPhone().matches("\\d{10}")) {
            AppUtils.showError(null, "SĐT phải đủ 10 chữ số.");
            return false;
        }

        return true;
    }
}
