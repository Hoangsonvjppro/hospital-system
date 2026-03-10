package com.hospital.bus;

import com.hospital.dao.ServiceDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.Service;

/**
 * Business logic layer cho dịch vụ y tế (xét nghiệm, chẩn đoán hình ảnh, thủ thuật).
 */
public class ServiceBUS extends BaseBUS<Service> {

    private final ServiceDAO serviceDAO;

    public ServiceBUS() {
        super(new ServiceDAO());
        this.serviceDAO = (ServiceDAO) dao;
    }

    @Override
    protected void validate(Service s) {
        if (s == null) throw new BusinessException("Dữ liệu dịch vụ không hợp lệ");
        if (s.getServiceName() == null || s.getServiceName().trim().isEmpty())
            throw new BusinessException("Tên dịch vụ không được để trống");
        if (s.getPrice() < 0)
            throw new BusinessException("Giá dịch vụ không được âm");
    }
}
