package com.hospital.bus;

import com.hospital.dao.FollowUpDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.FollowUp;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * Business logic layer cho hẹn tái khám (FollowUp).
 */
public class FollowUpBUS {

    private static final Logger LOGGER = Logger.getLogger(FollowUpBUS.class.getName());

    private final FollowUpDAO dao = new FollowUpDAO();

    /**
     * Tạo lịch hẹn tái khám.
     */
    public long scheduleFollowUp(FollowUp followUp) {
        // Validate
        if (followUp.getPatientId() <= 0) {
            throw new BusinessException("Mã bệnh nhân không hợp lệ");
        }
        if (followUp.getRecordId() <= 0) {
            throw new BusinessException("Mã bệnh án không hợp lệ");
        }
        if (followUp.getFollowUpDate() == null) {
            throw new BusinessException("Chưa chọn ngày tái khám");
        }
        if (followUp.getFollowUpDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Ngày tái khám phải từ hôm nay trở đi");
        }
        return dao.scheduleFollowUp(followUp);
    }

    /**
     * Lấy danh sách hẹn hôm nay.
     */
    public List<FollowUp> getTodayFollowUps() {
        return dao.getTodayFollowUps();
    }

    /**
     * Lịch hẹn trong N ngày tới.
     */
    public List<FollowUp> getUpcomingFollowUps(int days) {
        if (days <= 0) days = 7;
        return dao.getUpcomingFollowUps(days);
    }

    /**
     * Lấy lịch hẹn theo bệnh nhân.
     */
    public List<FollowUp> getByPatientId(long patientId) {
        return dao.getByPatientId(patientId);
    }

    /**
     * Lấy lịch hẹn theo record_id.
     */
    public FollowUp getByRecordId(long recordId) {
        return dao.getByRecordId(recordId);
    }

    /**
     * Đánh dấu đã tái khám.
     */
    public boolean markAsCompleted(int id) {
        return dao.markAsCompleted(id);
    }

    /**
     * Hủy lịch hẹn.
     */
    public boolean cancel(int id) {
        return dao.cancel(id);
    }
}
