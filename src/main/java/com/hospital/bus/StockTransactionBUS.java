package com.hospital.bus;

import com.hospital.dao.StockTransactionDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.StockTransaction;

import java.util.List;

/**
 * Business logic layer cho lịch sử giao dịch kho (StockTransaction).
 * Đây là audit log — chỉ hỗ trợ insert và truy vấn, không edit/delete.
 */
public class StockTransactionBUS extends BaseBUS<StockTransaction> {

    private final StockTransactionDAO txDAO;

    public StockTransactionBUS() {
        super(new StockTransactionDAO());
        this.txDAO = (StockTransactionDAO) dao;
    }

    @Override
    protected void validate(StockTransaction t) {
        if (t == null) throw new BusinessException("Dữ liệu giao dịch kho không hợp lệ");
        if (t.getMedicineId() <= 0)
            throw new BusinessException("Mã thuốc không hợp lệ");
        if (t.getTransactionType() == null || t.getTransactionType().trim().isEmpty())
            throw new BusinessException("Loại giao dịch không được để trống");
    }

    @Override
    public boolean update(StockTransaction entity) {
        throw new BusinessException("Không thể sửa giao dịch kho (audit log)");
    }

    @Override
    public boolean delete(int id) {
        throw new BusinessException("Không thể xóa giao dịch kho (audit log)");
    }

    public List<StockTransaction> findByMedicineId(long medicineId) {
        return txDAO.findByMedicineId(medicineId);
    }

    public List<StockTransaction> findByBatchId(long batchId) {
        return txDAO.findByBatchId(batchId);
    }
}
