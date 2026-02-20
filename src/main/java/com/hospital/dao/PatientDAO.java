package com.hospital.dao;

import com.hospital.model.Patient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DAO bệnh nhân – dùng mock data (ArrayList).
 */
public class PatientDAO implements BaseDAO<Patient> {

    private static final List<Patient> DATA = new ArrayList<>();
    private static int nextId = 8;

    static {
        DATA.add(new Patient(1, "BN001", "Nguyễn Văn A",
                LocalDate.of(1992, 3, 15), "Nam",
                "0901234561", "123 Lê Lợi, TP.HCM",
                "CHỜ KHÁM", "Khám nội tổng quát", "08:30"));
        DATA.add(new Patient(2, "BN002", "Trần Thị B",
                LocalDate.of(1996, 7, 22), "Nữ",
                "0911234562", "45 Nguyễn Huệ, TP.HCM",
                "ĐANG KHÁM", "Siêu âm", "08:45"));
        DATA.add(new Patient(3, "BN003", "Lê Văn C",
                LocalDate.of(1979, 11, 5), "Nam",
                "0921234563", "67 Hai Bà Trưng, TP.HCM",
                "CHỜ KHÁM", "Tái khám", "09:00"));
        DATA.add(new Patient(4, "BN004", "Phạm Thị D",
                LocalDate.of(2001, 4, 18), "Nữ",
                "0931234564", "89 Lý Tự Trọng, TP.HCM",
                "CHỜ KHÁM", "Khám nhi", "09:15"));
        DATA.add(new Patient(5, "BN005", "Hoàng Văn E",
                LocalDate.of(1985, 8, 30), "Nam",
                "0941234565", "12 Pasteur, TP.HCM",
                "XONG", "Khám tim mạch", "07:30"));
        DATA.add(new Patient(6, "BN006", "Vũ Thị F",
                LocalDate.of(1993, 1, 12), "Nữ",
                "0951234566", "34 Võ Thị Sáu, TP.HCM",
                "XONG", "Xét nghiệm máu", "07:45"));
        DATA.add(new Patient(7, "BN007", "Đặng Văn G",
                LocalDate.of(1970, 6, 25), "Nam",
                "0961234567", "56 Đinh Tiên Hoàng, TP.HCM",
                "CHỜ KHÁM", "Khám mắt", "09:30"));
    }

    @Override
    public Patient findById(int id) {
        return DATA.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    @Override
    public List<Patient> findAll() {
        return new ArrayList<>(DATA);
    }

    public List<Patient> findByStatus(String status) {
        return DATA.stream()
                .filter(p -> p.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    public List<Patient> findWaiting() {
        return DATA.stream()
                .filter(p -> "CHỜ KHÁM".equals(p.getStatus()) || "ĐANG KHÁM".equals(p.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean insert(Patient p) {
        p.setId(nextId++);
        DATA.add(p);
        return true;
    }

    @Override
    public boolean update(Patient p) {
        for (int i = 0; i < DATA.size(); i++) {
            if (DATA.get(i).getId() == p.getId()) {
                DATA.set(i, p);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        return DATA.removeIf(p -> p.getId() == id);
    }

    public int countToday() { return DATA.size(); }
}
