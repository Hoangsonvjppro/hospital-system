package com.hospital.bus;

import com.hospital.dao.ServiceOrderDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.ServiceOrder;

import java.util.List;

/**
 * Business logic layer cho chỉ định dịch vụ (ServiceOrder).
 */
public class ServiceOrderBUS extends BaseBUS<ServiceOrder> {

    private final ServiceOrderDAO serviceOrderDAO;

    public ServiceOrderBUS() {
        super(new ServiceOrderDAO());
        this.serviceOrderDAO = (ServiceOrderDAO) dao;
    }

    @Override
    protected void validate(ServiceOrder so) {
        if (so == null) throw new BusinessException("Dữ liệu chỉ định dịch vụ không hợp lệ");
        if (so.getRecordId() <= 0)
            throw new BusinessException("Mã bệnh án không hợp lệ");
        if (so.getServiceId() <= 0)
            throw new BusinessException("Mã dịch vụ không hợp lệ");
    }

    public List<ServiceOrder> findByRecordId(long recordId) {
        return serviceOrderDAO.findByRecordId(recordId);
    }
}
