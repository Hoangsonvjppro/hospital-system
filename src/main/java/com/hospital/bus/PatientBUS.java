package com.hospital.bus;

import com.hospital.dao.PatientDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.Patient;
import com.hospital.util.AppUtils;

/**
 * BUS benh nhan — validation + CRUD.
 * Các method liên quan hàng đợi đã chuyển sang QueueBUS.
 */
public class PatientBUS extends BaseBUS<Patient> {

    public PatientBUS() {
        super(new PatientDAO());
    }

    /**
     * Permanently delete a patient from DB (delegates to DAO.deletePermanent).
     */
    public boolean deletePermanent(int id) {
        try {
            return ((PatientDAO) dao).deletePermanent(id);
        } catch (Exception e) {
            throw e;
        }
    }

    public Patient findByPhone(String phone) {
        return ((PatientDAO) dao).findByPhone(phone);
    }

    public java.util.List<Patient> searchPatients(String keyword) {
        return ((PatientDAO) dao).searchPatients(keyword);
    }

    public java.util.List<Patient> findTodayRegistered() {
        return ((PatientDAO) dao).findTodayRegistered();
    }

    @Override
    protected void validate(Patient entity) {

        if (AppUtils.isNullOrEmpty(entity.getFullName())) {
            throw new BusinessException("Tên bệnh nhân không được để trống.");
        }

        if (AppUtils.isNullOrEmpty(entity.getPhone())) {
            throw new BusinessException("SĐT không được để trống.");
        }

        if (!entity.getPhone().matches("\\d{10}")) {
            throw new BusinessException("SĐT phải đủ 10 chữ số.");
        }

        // Kiểm tra SĐT không trùng với bệnh nhân khác
        try {
            com.hospital.model.Patient byPhone = ((PatientDAO) dao).findByPhone(entity.getPhone());
            if (byPhone != null && byPhone.getId() != entity.getId()) {
                throw new BusinessException("SĐT đã tồn tại cho bệnh nhân khác.");
            }
        } catch (BusinessException be) { throw be; } catch (Exception ignored) {}

        // CCCD: không bắt buộc, nhưng nếu có phải là 12 chữ số
        if (!AppUtils.isNullOrEmpty(entity.getCccd())) {
            if (!entity.getCccd().matches("\\d{12}")) {
                throw new BusinessException("CCCD phải là 12 chữ số.");
            }
            // Kiểm tra CCCD không trùng với bệnh nhân khác
            try {
                com.hospital.model.Patient byCccd = ((PatientDAO) dao).findByCccd(entity.getCccd());
                if (byCccd != null && byCccd.getId() != entity.getId()) {
                    throw new BusinessException("CCCD đã tồn tại cho bệnh nhân khác.");
                }
            } catch (BusinessException be) { throw be; } catch (Exception ignored) {}
        }

        // Ngày sinh: nếu có thì tuổi phải hợp lệ
        if (entity.getDateOfBirth() != null) {
            int age = entity.getAge();
            if (age < 0 || age > 150) {
                throw new BusinessException("Tuổi không hợp lệ.");
            }
        }
    }
}
