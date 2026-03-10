package com.hospital.bus;

import com.hospital.dao.SupplierDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.Supplier;

import java.util.List;

/**
 * Business logic layer cho nhà cung cấp (Supplier).
 */
public class SupplierBUS extends BaseBUS<Supplier> {

    private final SupplierDAO supplierDAO;

    public SupplierBUS() {
        super(new SupplierDAO());
        this.supplierDAO = (SupplierDAO) dao;
    }

    @Override
    protected void validate(Supplier s) {
        if (s == null) throw new BusinessException("Dữ liệu nhà cung cấp không hợp lệ");
        if (s.getSupplierName() == null || s.getSupplierName().trim().isEmpty())
            throw new BusinessException("Tên nhà cung cấp không được để trống");
        if (s.getPhone() == null || s.getPhone().trim().isEmpty())
            throw new BusinessException("Số điện thoại không được để trống");
    }

    public List<Supplier> findActive() {
        return supplierDAO.findActive();
    }
}
