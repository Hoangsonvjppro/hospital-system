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
    protected boolean validate(Appointment a) {
        if (a == null) return false;
        if (a.getPatientName() == null || a.getPatientName().trim().isEmpty()) return false;
        if (a.getDoctorName() == null || a.getDoctorName().trim().isEmpty()) return false;
        if (a.getDate() == null || a.getDate().isEmpty()) return false;
        return true;
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
