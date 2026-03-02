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

    @Override
    protected boolean validate(Patient entity) {

        if (AppUtils.isNullOrEmpty(entity.getFullName())) {
            throw new BusinessException("Tên bệnh nhân không được để trống.");
        }

        if (AppUtils.isNullOrEmpty(entity.getPhone())) {
            throw new BusinessException("SĐT không được để trống.");
        }

        if (!entity.getPhone().matches("\\d{10}")) {
            throw new BusinessException("SĐT phải đủ 10 chữ số.");
        }

        // Normalize phone (keep digits only) for comparison
        String normalizedPhone = entity.getPhone() != null ? entity.getPhone().replaceAll("\\D", "") : null;
        // Kiểm tra SĐT không trùng với bệnh nhân khác bằng cách quét toàn bộ DB (đảm bảo so sánh toàn repository)
        try {
            for (com.hospital.model.Patient p : dao.findAll()) {
                if (p == null) continue;
                try {
                    String pPhone = p.getPhone() != null ? p.getPhone().replaceAll("\\D", "") : null;
                    if (normalizedPhone != null && normalizedPhone.equals(pPhone) && p.getId() != entity.getId()) {
                        throw new BusinessException("SĐT đã tồn tại cho bệnh nhân khác.");
                    }
                } catch (BusinessException be) { throw be; } catch (Exception ignored) {}
            }
        } catch (BusinessException be) { throw be; } catch (Exception ex) {
            // ignore errors from fallback scan (e.g., DB issues) and continue to other checks
        }

        // CCCD: bắt buộc và phải là 12 chữ số
        if (AppUtils.isNullOrEmpty(entity.getCccd())) {
            throw new BusinessException("CCCD không được để trống.");
        }
        if (!entity.getCccd().matches("\\d{12}")) {
            throw new BusinessException("CCCD phải là 12 chữ số.");
        }

        // Kiểm tra CCCD không trùng với bệnh nhân khác bằng cách quét toàn bộ DB
        try {
            for (com.hospital.model.Patient p : dao.findAll()) {
                if (p == null) continue;
                try {
                    String pCccd = p.getCccd();
                    if (entity.getCccd() != null && entity.getCccd().equals(pCccd) && p.getId() != entity.getId()) {
                        throw new BusinessException("CCCD đã tồn tại cho bệnh nhân khác.");
                    }
                } catch (BusinessException be) { throw be; } catch (Exception ignored) {}
            }
        } catch (BusinessException be) { throw be; } catch (Exception ex) {
            // ignore errors from fallback scan
        }

        // Tuổi hợp lệ nếu có ngày sinh
        // Ngày sinh bắt buộc theo schema DB
        if (entity.getDateOfBirth() == null) {
            throw new BusinessException("Ngày sinh không được để trống.");
        } else {
            int age = entity.getAge();
            if (age <= 0 || age > 150) {
                throw new BusinessException("Tuổi không hợp lệ.");
            }
        }

        return true;
    }
}
