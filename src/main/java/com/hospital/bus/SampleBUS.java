package com.hospital.bus;

import com.hospital.dao.SampleDAO;
import com.hospital.model.SampleEntity;

/**
 * BUS mẫu — thay thế bằng BUS thực tế của bạn.
 * Sample BUS — replace with your actual BUS classes.
 */
public class SampleBUS extends BaseBUS<SampleEntity> {

    public SampleBUS() {
        super(new SampleDAO());
    }

    @Override
    protected boolean validate(SampleEntity entity) {
        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            System.err.println("Lỗi: Tên không được để trống.");
            return false;
        }
        return true;
    }
}
