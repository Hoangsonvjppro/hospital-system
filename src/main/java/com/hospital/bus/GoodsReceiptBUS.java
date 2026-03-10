package com.hospital.bus;

import com.hospital.dao.GoodsReceiptDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.GoodsReceipt;

import java.util.List;

/**
 * Business logic layer cho phiếu nhập kho (GoodsReceipt).
 */
public class GoodsReceiptBUS extends BaseBUS<GoodsReceipt> {

    private final GoodsReceiptDAO receiptDAO;

    public GoodsReceiptBUS() {
        super(new GoodsReceiptDAO());
        this.receiptDAO = (GoodsReceiptDAO) dao;
    }

    @Override
    protected void validate(GoodsReceipt r) {
        if (r == null) throw new BusinessException("Dữ liệu phiếu nhập không hợp lệ");
        if (r.getSupplierId() == null || r.getSupplierId() <= 0)
            throw new BusinessException("Nhà cung cấp không hợp lệ");
        if (r.getTotalAmount() < 0)
            throw new BusinessException("Tổng tiền không được âm");
    }

    public List<GoodsReceipt> findBySupplierId(int supplierId) {
        return receiptDAO.findBySupplierId(supplierId);
    }
}
