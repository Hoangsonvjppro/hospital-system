package com.hospital.bus;

import com.hospital.dao.ServiceDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.Service;

import java.math.BigDecimal;
import java.util.List;

public class ServiceBUS extends BaseBUS<Service> {

    private final ServiceDAO serviceDAO;

    public ServiceBUS() {
        super(new ServiceDAO());
        this.serviceDAO = (ServiceDAO) this.dao;
    }

    public List<Service> getActiveServices() {
        return serviceDAO.findActive();
    }

    public boolean deactivate(int id) {
        return serviceDAO.delete(id);
    }

    @Override
    protected void validate(Service entity) {
        if (entity.getServiceName() == null || entity.getServiceName().trim().isEmpty()) {
            throw new BusinessException("Tên dịch vụ không được để trống");
        }
        if (entity.getPrice() == null || entity.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Giá dịch vụ phải >= 0");
        }
    }
}
