package com.hospital.bus;

import com.hospital.dao.BaseDAO;
import com.hospital.dao.MedicineDAO;
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
        if(AppUtils.isNullOrEmpty(entity.getMedicineName())){
            AppUtils.showError(null,"Tên thuốc không được để trống.");
            return false;
        }
        if(AppUtils.isNullOrEmpty(entity.getUnit())){
            AppUtils.showError(null,"Đơn vị tính không được để trống.");
            return false;
        }
        if(entity.getCostPrice()>entity.getSellPrice()){
            AppUtils.showError(null,"Cảnh báo: Giá bán đang thấp hơn giá vốn.");
            return false;
        }
        if(entity.getCostPrice()<0||entity.getSellPrice()<0){
            AppUtils.showError(null,"Giá không được âm");
            return false;
        }
        LocalDate expiryDate = entity.getExpiryDate();
        if (expiryDate.isBefore(LocalDate.now()) || expiryDate.isEqual(LocalDate.now())) {
            AppUtils.showError(null, "Ngày hết hạn không thể là hôm nay hay trước đó!");
            return false;
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
}
