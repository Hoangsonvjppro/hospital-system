package com.hospital.bus;

import com.hospital.dao.BaseDAO;
import com.hospital.dao.MedicineDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.Medicine;
import com.hospital.util.AppUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MedicineBUS extends BaseBUS<Medicine> {
    private final MedicineDAO medicineDAO;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public MedicineBUS() {
        super(new MedicineDAO());
        this.medicineDAO=(MedicineDAO) this.dao;
    }

    @Override
    protected boolean validate(Medicine entity) {
        if (AppUtils.isNullOrEmpty(entity.getMedicineName())) {
            throw new BusinessException("Tên thuốc không được để trống.");
        }
        if(AppUtils.isNullOrEmpty(entity.getManufacturer())){
            throw new BusinessException("Nhà cung cấp không được để trống.");
        }
        if (AppUtils.isNullOrEmpty(entity.getUnit())) {
            throw new BusinessException("Đơn vị tính không được để trống.");
        }
        if (entity.getCostPrice() < 0 || entity.getSellPrice() < 0) {
            throw new BusinessException("Giá không được âm.");
        }
        if (entity.getCostPrice() > entity.getSellPrice()) {
            throw new BusinessException("Cảnh báo: Giá bán đang thấp hơn giá vốn.");
        }
        LocalDate expiryDate = entity.getExpiryDate();
        if (expiryDate != null && (expiryDate.isBefore(LocalDate.now()) || expiryDate.isEqual(LocalDate.now()))) {
            throw new BusinessException("Ngày hết hạn không thể là hôm nay hay trước đó!");
        }
        return true;
    }

    @Override
    public boolean delete(int id) {
        return medicineDAO.delete(id);
    }

    @Override
    public boolean update(Medicine entity) {
        if(!validate(entity)){
            return false;
        }
        return medicineDAO.update(entity);
    }

    @Override
    public boolean insert(Medicine entity) {
        if(!validate(entity)){
            return false;
        }
        return medicineDAO.insert(entity);
    }

    @Override
    public List<Medicine> findAll() {
        return medicineDAO.findAll();
    }

    @Override
    public Medicine findById(int id) {
        return medicineDAO.findById(id);
    }
    public List<Medicine> findByName(String keyword){
        return medicineDAO.findByName(keyword);
    }
    public List<Medicine> getExpiredMedicinesList(){
        return medicineDAO.getExpiryDateMedicines();
    }
    public List<Medicine> getLowStockMedicinesList(){
        return medicineDAO.getLowStockMedicines();
    }
}