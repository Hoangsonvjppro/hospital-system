package com.hospital.dao;

import com.hospital.model.Appointment;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO lịch hẹn – dùng mock data.
 */
public class AppointmentDAO implements BaseDAO<Appointment> {

    private static final List<Appointment> DATA = new ArrayList<>();
    private static int nextId = 13;

    static {
        // Tuần 16/02 – 22/02/2026  (Thứ 2 → Chủ nhật)
        // ── Thứ 2 (16/02) ───────────────────────────────────────────────
        DATA.add(new Appointment(1,  "LH001", "Nguyễn Thị Hà",
                "0901230001", "Dr. Lê Văn C", "Khám tổng quát",
                "16/02/2026", "09:00", "10:00", "Đã xác nhận", ""));

        // ── Thứ 3 (17/02) ───────────────────────────────────────────────
        DATA.add(new Appointment(2,  "LH002", "Trần Văn Quân",
                "0901230002", "Dr. Trần Thị D", "Tái khám định kỳ",
                "17/02/2026", "08:15", "09:30", "Đã xác nhận", ""));

        // ── Thứ 4 (18/02) ───────────────────────────────────────────────
        DATA.add(new Appointment(3,  "LH003", "Lê Anh Tú",
                "0901230003", "Dr. Phạm Thị F", "Chờ xác nhận",
                "18/02/2026", "10:15", "11:00", "Mới", ""));

        // ── Thứ 5 (19/02) ───────────────────────────────────────────────
        DATA.add(new Appointment(4,  "LH004", "Phạm Bình",
                "0901230004", "Dr. Hoàng Văn G", "Đã hủy",
                "19/02/2026", "08:00", "09:00", "Hủy", "Bệnh nhân hủy hẹn"));

        // ── Thứ 6 (20/02) ───────────────────────────────────────────────
        DATA.add(new Appointment(5,  "LH005", "Bùi Minh Khang",
                "0901230005", "Dr. Lê Văn C", "Xét nghiệm máu",
                "20/02/2026", "11:15", "12:45", "Đã xác nhận", ""));
        DATA.add(new Appointment(6,  "LH006", "Nguyễn Thị Hoa",
                "0901230006", "Dr. Nguyễn Văn E", "Khám nội",
                "20/02/2026", "08:30", "09:15", "Đã khám", ""));

        // ── Thứ 7 (21/02) ───────────────────────────────────────────────
        DATA.add(new Appointment(7,  "LH007", "Võ Văn Hùng",
                "0901230007", "Dr. Lê Văn C", "Khám tổng quát",
                "21/02/2026", "09:00", "10:00", "Đã xác nhận", ""));
        DATA.add(new Appointment(8,  "LH008", "Lê Thị Lan",
                "0901230008", "Dr. Trần Thị D", "Khám nhi",
                "21/02/2026", "14:00", "15:00", "Mới", ""));

        // ── Chủ nhật (22/02) ────────────────────────────────────────────
        DATA.add(new Appointment(9,  "LH009", "Trương Thị Mai",
                "0901230009", "Dr. Phạm Thị F", "Khám tim mạch",
                "22/02/2026", "08:00", "09:00", "Đã khám", ""));

        // ── Tuần trước / tuần sau thêm vài cái ─────────────────────────
        DATA.add(new Appointment(10, "LH010", "Hoàng Văn Dũng",
                "0901230010", "Dr. Lê Văn C", "Tái khám",
                "23/02/2026", "10:00", "11:00", "Mới", ""));
        DATA.add(new Appointment(11, "LH011", "Đặng Thị Thanh",
                "0901230011", "Dr. Hoàng Văn G", "Khám mắt",
                "24/02/2026", "13:30", "14:30", "Đã xác nhận", ""));
        DATA.add(new Appointment(12, "LH012", "Phan Minh Tuấn",
                "0901230012", "Dr. Trần Thị D", "Khám sức khỏe",
                "25/02/2026", "09:00", "10:00", "Mới", ""));
    }

    @Override
    public Appointment findById(int id) {
        return DATA.stream().filter(a -> a.getId() == id).findFirst().orElse(null);
    }

    @Override
    public List<Appointment> findAll() { return new ArrayList<>(DATA); }

    @Override
    public boolean insert(Appointment a) {
        a.setId(nextId++);
        DATA.add(a);
        return true;
    }

    @Override
    public boolean update(Appointment a) {
        for (int i = 0; i < DATA.size(); i++) {
            if (DATA.get(i).getId() == a.getId()) {
                DATA.set(i, a);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(int id) { return DATA.removeIf(a -> a.getId() == id); }
}
