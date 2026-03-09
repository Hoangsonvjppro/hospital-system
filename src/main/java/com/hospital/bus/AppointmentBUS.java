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
        if (a.getPatientName() == null || a.getPatientName().trim().isEmpty())
            throw new com.hospital.exception.BusinessException("T\u00ean b\u1ec7nh nh\u00e2n kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng");
        if (a.getDoctorName() == null || a.getDoctorName().trim().isEmpty())
            throw new com.hospital.exception.BusinessException("T\u00ean b\u00e1c s\u0129 kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng");
        if (a.getAppointmentDate() == null)
            throw new com.hospital.exception.BusinessException("Ng\u00e0y h\u1eb9n kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng");
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
}
