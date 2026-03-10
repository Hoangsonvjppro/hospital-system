package com.hospital.bus;

import com.hospital.bus.event.EventBus;
import com.hospital.bus.event.QueueUpdatedEvent;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.QueueDAO;
import com.hospital.dao.QueueEntryDAO;
import com.hospital.dao.MedicalRecordDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.Patient;
import com.hospital.model.QueueEntry;
import com.hospital.model.QueueEntry.Priority;
import com.hospital.model.QueueEntry.QueueStatus;

import java.util.List;

/**
 * Business logic layer cho hàng đợi khám bệnh.
 * Sử dụng QueueEntryDAO (bảng queue_entries) cho hàng đợi mới,
 * giữ QueueDAO (MedicalRecord) cho tương thích ngược.
 */
public class QueueBUS {

    private final QueueDAO queueDAO;
    private final QueueEntryDAO queueEntryDAO;
    private final PatientDAO patientDAO;
    private final MedicalRecordDAO medicalRecordDAO;
    private final DoctorDAO doctorDAO;

    public QueueBUS() {
        this.queueDAO = new QueueDAO();
        this.queueEntryDAO = new QueueEntryDAO();
        this.patientDAO = new PatientDAO();
        this.medicalRecordDAO = new MedicalRecordDAO();
        this.doctorDAO = new DoctorDAO();
    }

    // ══════════════════════════════════════════════════════════
    //  QUEUE ENTRY (bảng queue_entries mới)
    // ══════════════════════════════════════════════════════════

    /**
     * Thêm bệnh nhân vào hàng đợi.
     *
     * @param patientId ID bệnh nhân
     * @param priority  Ưu tiên (EMERGENCY, ELDERLY, NORMAL)
     * @return QueueEntry đã tạo
     */
    public QueueEntry addToQueue(int patientId, Priority priority) {
        if (patientId <= 0) {
            throw new BusinessException("Patient ID không hợp lệ");
        }

        // Kiểm tra xem bệnh nhân đã có trong hàng đợi hôm nay chưa
        if (queueEntryDAO.isPatientInTodayQueue(patientId)) {
            throw new BusinessException("Bệnh nhân đã có trong hàng đợi khám hôm nay. Không thể thêm lại.");
        }

        QueueEntry entry = new QueueEntry();
        entry.setPatientId(patientId);
        entry.setPriority(priority != null ? priority : Priority.NORMAL);

        queueEntryDAO.addToQueue(entry);

        // Fire event
        EventBus.getInstance().publish(new QueueUpdatedEvent(entry.getId(), QueueStatus.WAITING.name()));
        return entry;
    }

    /**
     * Lấy toàn bộ hàng đợi hôm nay.
     */
    public List<QueueEntry> getTodayQueue() {
        return queueEntryDAO.getTodayQueue();
    }

    /**
     * Lấy danh sách đang chờ hôm nay.
     */
    public List<QueueEntry> getWaitingQueue() {
        return queueEntryDAO.getWaitingQueue();
    }

    /**
     * Cập nhật trạng thái hàng đợi.
     */
    public boolean updateQueueEntryStatus(int queueId, QueueStatus status) {
        if (queueId <= 0) {
            throw new BusinessException("Queue ID không hợp lệ");
        }
        boolean result = queueEntryDAO.updateStatus(queueId, status);
        if (result) {
            EventBus.getInstance().publish(new QueueUpdatedEvent(queueId, status.name()));
        }
        return result;
    }

    /**
     * Lấy bệnh nhân tiếp theo cần khám.
     */
    public QueueEntry getNextPatient() {
        return queueEntryDAO.getNextPatient();
    }

    /**
     * Gọi bệnh nhân tiếp theo — cập nhật trạng thái sang IN_PROGRESS.
     */
    public QueueEntry callNextPatient() {
        QueueEntry next = queueEntryDAO.getNextPatient();
        if (next == null) {
            throw new BusinessException("Không còn bệnh nhân trong hàng đợi.");
        }
        queueEntryDAO.updateStatus(next.getId(), QueueStatus.IN_PROGRESS);
        next.setStatus(QueueStatus.IN_PROGRESS);
        EventBus.getInstance().publish(new QueueUpdatedEvent(next.getId(), QueueStatus.IN_PROGRESS.name()));
        return next;
    }

    /**
     * Bỏ qua bệnh nhân — đưa xuống cuối hàng đợi (tạo entry mới với WAITING).
     */
    public void skipPatient(int queueId) {
        QueueEntry entry = queueEntryDAO.findById(queueId);
        if (entry == null) {
            throw new BusinessException("Không tìm thấy bệnh nhân trong hàng đợi.");
        }
        // Cancel entry cũ
        queueEntryDAO.updateStatus(queueId, QueueStatus.CANCELLED);
        // Tạo entry mới ở cuối
        QueueEntry newEntry = new QueueEntry();
        newEntry.setPatientId(entry.getPatientId());
        newEntry.setPriority(entry.getPriority());
        queueEntryDAO.addToQueue(newEntry);
        EventBus.getInstance().publish(new QueueUpdatedEvent(newEntry.getId(), QueueStatus.WAITING.name()));
    }

    /**
     * Hủy bệnh nhân khỏi hàng đợi.
     */
    public void cancelQueueEntry(int queueId) {
        queueEntryDAO.updateStatus(queueId, QueueStatus.CANCELLED);
        EventBus.getInstance().publish(new QueueUpdatedEvent(queueId, QueueStatus.CANCELLED.name()));
    }

    /**
     * Đếm số đang chờ hôm nay.
     */
    public int countTodayWaiting() {
        return queueEntryDAO.countTodayWaiting();
    }

    // ══════════════════════════════════════════════════════════
    //  LEGACY (QueueDAO — MedicalRecord-based) — giữ tương thích
    // ══════════════════════════════════════════════════════════

    /**
     * Đưa bệnh nhân vào hàng đợi khám (legacy — MedicalRecord).
     */
    public long enqueue(long patientId, long doctorId, String examType) {
        if (patientId <= 0) {
            throw new BusinessException("Patient ID không hợp lệ");
        }
        if (doctorId <= 0) {
            throw new BusinessException("Doctor ID không hợp lệ");
        }
        return queueDAO.enqueue(patientId, doctorId, examType);
    }

    /**
     * Lấy danh sách bệnh nhân đang chờ khám + đang khám (ngày hôm nay).
     */
    public List<Patient> getWaitingPatients() {
        // Đọc từ QueueEntry (hệ thống mới) - lấy cả WAITING và IN_PROGRESS (đang khám)
        List<QueueEntry> queueEntries = queueEntryDAO.getTodayQueue();
        List<Patient> patients = new java.util.ArrayList<>();
        
        for (QueueEntry entry : queueEntries) {
            // Only include WAITING and IN_PROGRESS, skip COMPLETED and CANCELLED
            if (entry.getStatus() == QueueEntry.QueueStatus.COMPLETED || 
                entry.getStatus() == QueueEntry.QueueStatus.CANCELLED) {
                continue;
            }
            
            try {
                Patient p = patientDAO.findById(entry.getPatientId());
                if (p != null) {
                    // Set transient fields for display
                    p.setPatientCode(p.getPatientCode()); // BN00X format
                    String statusStr = entry.getStatus() != null ? entry.getStatus().name() : "WAITING";
                    if ("IN_PROGRESS".equals(statusStr)) {
                        statusStr = "EXAMINING";
                    }
                    p.setStatus(statusStr);
                    p.setArrivalTime(entry.getCreatedAt() != null ? 
                        entry.getCreatedAt().toLocalTime().toString() : "");
                    p.setExamType("Khám tổng quát");
                    
                    if ("EXAMINING".equals(statusStr)) {
                        // Nếu đang khám, tìm record_id trong bảng MedicalRecord hôm nay
                        // Điều này cực kỳ quan trọng để LabOrder/Prescription có examinationId đúng.
                        List<com.hospital.model.MedicalRecord> todayRecords = medicalRecordDAO.listQueueToday(0);
                        long realRecordId = todayRecords.stream()
                            .filter(mr -> mr.getPatientId() == p.getId() && "EXAMINING".equals(mr.getStatus()))
                            .map(mr -> (long) mr.getId())
                            .findFirst()
                            .orElse((long) entry.getId());
                        p.setCurrentRecordId(realRecordId);
                    } else {
                        p.setCurrentRecordId(entry.getId());
                    }
                    patients.add(p);
                }
            } catch (Exception e) {
                // Skip invalid entries
            }
        }
        
        return patients;
    }

    /**
     * Lấy danh sách bệnh nhân theo trạng thái hàng đợi.
     */
    public List<Patient> getPatientsByStatus(String... statuses) {
        List<Patient> allWaiting = getWaitingPatients();
        List<Patient> result = new java.util.ArrayList<>();
        List<String> statusList = java.util.Arrays.asList(statuses);
        for (Patient p : allWaiting) {
            if (statusList.contains(p.getStatus())) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Cập nhật trạng thái hàng đợi (legacy).
     */
    /**
     * Cập nhật trạng thái hàng đợi (legacy wrapper).
     * Trả về recordId (MedicalRecord) nếu status là EXAMINING, ngược lại trả về queueId.
     */
    public long updateQueueStatus(long queueId, String newStatus, int doctorUserId) {
        if (queueId <= 0) {
            throw new BusinessException("Queue ID không hợp lệ");
        }
        
        try {
            String mappedStatus = "EXAMINING".equals(newStatus) ? "IN_PROGRESS" : newStatus;
            QueueStatus status = QueueStatus.valueOf(mappedStatus);
            
            boolean updated = updateQueueEntryStatus((int) queueId, status);
            
            if (updated && status == QueueStatus.IN_PROGRESS) {
                // Tạo MedicalRecord khi bắt đầu khám
                QueueEntry entry = queueEntryDAO.findById((int) queueId);
                if (entry != null) {
                    // Tìm doctorId từ doctorUserId
                    com.hospital.model.Doctor doctor = doctorDAO.findByUserId(doctorUserId);
                    if (doctor == null) {
                        throw new BusinessException("Không tìm thấy thông tin bác sĩ cho người dùng này.");
                    }
                    
                    // Kiểm tra xem đã có record hôm nay chưa (để tránh tạo trùng)
                    // Ở đây ta cứ tạo mới mỗi lần call (phiên khám mới)
                    return medicalRecordDAO.createEmptyRecord(
                        entry.getPatientId(), 
                        doctor.getId(), 
                        null, 
                        entry.getPriority() != null ? entry.getPriority().name() : "NORMAL",
                        entry.getQueueNumber(),
                        null,
                        "Khám tổng quát"
                    );
                }
            }
            
            return updated ? queueId : -1;
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }

    /**
     * @deprecated Use the version with doctorUserId
     */
    @Deprecated
    public boolean updateQueueStatus(long recordId, String newStatus) {
        return updateQueueStatus(recordId, newStatus, 0) > 0;
    }

    /**
     * Đếm tổng số bệnh nhân trong hàng đợi hôm nay (legacy).
     */
    public int countToday() {
        return queueEntryDAO.getTodayQueue().size();
    }

    /**
     * Đếm bệnh nhân theo trạng thái (hôm nay) (legacy).
     */
    public int countByStatus(String status) {
        List<QueueEntry> today = queueEntryDAO.getTodayQueue();
        String targetStatus = "EXAMINING".equals(status) ? "IN_PROGRESS" : status;
        int count = 0;
        for (QueueEntry e : today) {
            String s = e.getStatus() != null ? e.getStatus().name() : "WAITING";
            if (s.equals(targetStatus)) {
                count++;
            }
        }
        return count;
    }
}
