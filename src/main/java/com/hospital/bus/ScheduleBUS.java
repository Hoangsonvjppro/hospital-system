package com.hospital.bus;

import com.hospital.dao.ScheduleDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.Schedule;

import java.time.LocalDate;
import java.util.List;

/**
 * Business logic layer cho lịch làm việc bác sĩ (Schedule).
 */
public class ScheduleBUS extends BaseBUS<Schedule> {

    private final ScheduleDAO scheduleDAO;

    public ScheduleBUS() {
        super(new ScheduleDAO());
        this.scheduleDAO = (ScheduleDAO) dao;
    }

    @Override
    protected void validate(Schedule s) {
        if (s == null) throw new BusinessException("Dữ liệu lịch làm việc không hợp lệ");
        if (s.getDoctorId() <= 0)
            throw new BusinessException("Mã bác sĩ không hợp lệ");
        if (s.getWorkDate() == null)
            throw new BusinessException("Ngày làm việc không được để trống");
        if (s.getStartTime() == null || s.getEndTime() == null)
            throw new BusinessException("Giờ bắt đầu và kết thúc không được để trống");
        if (!s.getEndTime().isAfter(s.getStartTime()))
            throw new BusinessException("Giờ kết thúc phải sau giờ bắt đầu");
    }

    public List<Schedule> findByDoctorAndDateRange(long doctorId, LocalDate from, LocalDate to) {
        return scheduleDAO.findByDoctorAndDateRange(doctorId, from, to);
    }
}
