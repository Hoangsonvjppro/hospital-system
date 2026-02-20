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
    protected boolean validate(Doctor d) {
        if (d == null) return false;
        if (d.getFullName() == null || d.getFullName().trim().isEmpty()) return false;
        if (d.getSpecialty() == null || d.getSpecialty().trim().isEmpty()) return false;
        return true;
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
