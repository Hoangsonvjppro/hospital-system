package com.hospital.bus;

import com.hospital.dao.MedicineBatchDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.MedicineBatch;

import java.time.LocalDate;
import java.util.List;

/**
 * Business logic layer cho lô thuốc (MedicineBatch).
 * Quản lý batch-level inventory: nhập lô, kiểm tra hạn dùng, FEFO.
 */
public class MedicineBatchBUS extends BaseBUS<MedicineBatch> {

    private final MedicineBatchDAO batchDAO;

    public MedicineBatchBUS() {
        super(new MedicineBatchDAO());
        this.batchDAO = (MedicineBatchDAO) dao;
    }

    @Override
    protected void validate(MedicineBatch batch) {
        if (batch == null) throw new BusinessException("Dữ liệu lô thuốc không hợp lệ");
        if (batch.getMedicineId() <= 0)
            throw new BusinessException("Mã thuốc không hợp lệ");
        if (batch.getBatchNumber() == null || batch.getBatchNumber().trim().isEmpty())
            throw new BusinessException("Số lô không được để trống");
        if (batch.getExpiryDate() == null)
            throw new BusinessException("Ngày hết hạn không được để trống");
        if (batch.getExpiryDate().isBefore(LocalDate.now()))
            throw new BusinessException("Ngày hết hạn không được trong quá khứ");
        if (batch.getInitialQty() <= 0)
            throw new BusinessException("Số lượng nhập phải > 0");
        if (batch.getSellPrice() < 0 || batch.getImportPrice() < 0)
            throw new BusinessException("Giá không được âm");
    }

    public List<MedicineBatch> findByMedicineId(long medicineId) {
        return batchDAO.findByMedicineId(medicineId);
    }

    public List<MedicineBatch> findAvailableFEFO(long medicineId) {
        return batchDAO.findAvailableFEFO(medicineId);
    }
}
