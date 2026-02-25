package com.hospital.dao;

import com.hospital.model.Doctor;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO bác sĩ – dùng mock data.
 */
public class DoctorDAO implements BaseDAO<Doctor> {

    private static final List<Doctor> DATA = new ArrayList<>();
    private static int nextId = 6;

    static {
        DATA.add(new Doctor(1, "BS001", "Dr. Lê Văn C",
                "Khoa Nội", "0901111001", "levanc@hospital.vn", true));
        DATA.add(new Doctor(2, "BS002", "Dr. Trần Thị D",
                "Khoa Nhi", "0901111002", "tranthid@hospital.vn", true));
        DATA.add(new Doctor(3, "BS003", "Dr. Nguyễn Văn E",
                "Khoa Ngoại", "0901111003", "nguyenvane@hospital.vn", false));
        DATA.add(new Doctor(4, "BS004", "Dr. Phạm Thị F",
                "Khoa Tim Mạch", "0901111004", "phamthif@hospital.vn", true));
        DATA.add(new Doctor(5, "BS005", "Dr. Hoàng Văn G",
                "Khoa Mắt", "0901111005", "hoangvang@hospital.vn", false));
    }

    @Override
    public Doctor findById(int id) {
        return DATA.stream().filter(d -> d.getId() == id).findFirst().orElse(null);
    }

    @Override
    public List<Doctor> findAll() { return new ArrayList<>(DATA); }

    public List<Doctor> findOnline() {
        return DATA.stream().filter(Doctor::isOnline).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean insert(Doctor d) {
        d.setId(nextId++);
        DATA.add(d);
        return true;
    }

    @Override
    public boolean update(Doctor d) {
        for (int i = 0; i < DATA.size(); i++) {
            if (DATA.get(i).getId() == d.getId()) {
                DATA.set(i, d);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(int id) { return DATA.removeIf(d -> d.getId() == id); }

    public int countOnline() { return (int) DATA.stream().filter(Doctor::isOnline).count(); }
}
