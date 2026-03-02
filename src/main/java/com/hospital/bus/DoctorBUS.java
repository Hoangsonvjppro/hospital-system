package com.hospital.bus;

import com.hospital.dao.DoctorDAO;
import com.hospital.model.Doctor;

import java.util.List;

/**
 * Business logic layer cho bác sĩ.
 */
public class DoctorBUS extends BaseBUS<Doctor> {

    private final DoctorDAO doctorDAO;

    public DoctorBUS() {
        super(new DoctorDAO());
        this.doctorDAO = (DoctorDAO) dao;
    }

    @Override
    protected void validate(Doctor d) {
        if (d == null) throw new com.hospital.exception.BusinessException("D\u1eef li\u1ec7u b\u00e1c s\u0129 kh\u00f4ng h\u1ee3p l\u1ec7");
        if (d.getFullName() == null || d.getFullName().trim().isEmpty())
            throw new com.hospital.exception.BusinessException("T\u00ean b\u00e1c s\u0129 kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng");
        if (d.getSpecialty() == null || d.getSpecialty().trim().isEmpty())
            throw new com.hospital.exception.BusinessException("Chuy\u00ean khoa kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng");
    }

    public List<Doctor> getOnlineDoctors() {
        return doctorDAO.findOnline();
    }

    public int countOnline() {
        return doctorDAO.countOnline();
    }

    public boolean toggleOnlineStatus(int doctorId) {
        Doctor d = doctorDAO.findById(doctorId);
        if (d == null) return false;
        d.setOnline(!d.isOnline());
        return doctorDAO.update(d);
    }
}
