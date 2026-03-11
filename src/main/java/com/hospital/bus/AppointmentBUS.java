package com.hospital.bus;

import com.hospital.dao.AppointmentDAO;
import com.hospital.model.Appointment;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic layer cho lịch hẹn.
 */
public class AppointmentBUS extends BaseBUS<Appointment> {

    private final AppointmentDAO appointmentDAO;

    public AppointmentBUS() {
        super(new AppointmentDAO());
        this.appointmentDAO = (AppointmentDAO) dao;
    }

    @Override
    protected void validate(Appointment a) {
        if (a == null) throw new com.hospital.exception.BusinessException("D\u1eef li\u1ec7u l\u1ecbch h\u1eb9n kh\u00f4ng h\u1ee3p l\u1ec7");
        if (a.getPatientId() <= 0)
            throw new com.hospital.exception.BusinessException("Ch\u01b0a ch\u1ecdn b\u1ec7nh nh\u00e2n");
        if (a.getDoctorId() <= 0)
            throw new com.hospital.exception.BusinessException("Ch\u01b0a ch\u1ecdn b\u00e1c s\u0129");
        if (a.getDate() == null)
            throw new com.hospital.exception.BusinessException("Ng\u00e0y h\u1eb9n kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng");
        if (a.getTime() == null)
            throw new com.hospital.exception.BusinessException("Gi\u1edd b\u1eaft \u0111\u1ea7u kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng");
    }

    public List<Appointment> getByStatus(String status) {
        return appointmentDAO.findAll().stream()
                .filter(a -> a.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    public boolean confirm(int id) {
        Appointment a = appointmentDAO.findById(id);
        if (a == null) return false;
        a.setStatus("Đã xác nhận");
        return appointmentDAO.update(a);
    }

    public boolean cancel(int id) {
        Appointment a = appointmentDAO.findById(id);
        if (a == null) return false;
        a.setStatus("Hủy");
        return appointmentDAO.update(a);
    }

    /** Danh sách tên bác sĩ có lịch hẹn (dùng cho filter combo). */
    public List<String> getDistinctDoctorNames() {
        return appointmentDAO.findDistinctDoctorNames();
    }
}
