package com.hospital.dao;

import com.hospital.model.Medicine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DAO thuốc – dùng mock data.
 */
public class MedicineDAO implements BaseDAO<Medicine> {

    private static final List<Medicine> DATA = new ArrayList<>();
    private static int nextId = 11;

    static {
        DATA.add(new Medicine(1, "TH001", "Paracetamol 500mg", "Viên",
                500, 200, 20, "Hạ sốt – Giảm đau",
                "Pymepharco", "31/12/2026"));
        DATA.add(new Medicine(2, "TH002", "Amoxicillin 500mg", "Viên",
                1200, 150, 30, "Kháng sinh",
                "Stada", "30/06/2026"));
        DATA.add(new Medicine(3, "TH003", "Vitamin C 1000mg", "Viên",
                800, 8, 10, "Vitamin",
                "DHG Pharma", "31/03/2027"));
        DATA.add(new Medicine(4, "TH004", "Omeprazole 20mg", "Viên",
                900, 5, 10, "Dạ dày",
                "Traphaco", "28/02/2026"));
        DATA.add(new Medicine(5, "TH005", "Cetirizine 10mg", "Viên",
                700, 3, 10, "Dị ứng",
                "Pymepharco", "31/01/2027"));
        DATA.add(new Medicine(6, "TH006", "Metformin 500mg", "Viên",
                600, 100, 20, "Tiểu đường",
                "Stada", "30/09/2026"));
        DATA.add(new Medicine(7, "TH007", "Atorvastatin 20mg", "Viên",
                1500, 80, 15, "Tim mạch",
                "Pfizer", "31/07/2026"));
        DATA.add(new Medicine(8, "TH008", "Dung dịch muối 0.9%", "Chai",
                8000, 50, 10, "Dịch truyền",
                "B.Braun", "31/12/2025"));
        DATA.add(new Medicine(9, "TH009", "Ibuprofen 400mg", "Viên",
                600, 120, 20, "Giảm đau – Kháng viêm",
                "DHG Pharma", "30/11/2026"));
        DATA.add(new Medicine(10, "TH010", "Azithromycin 250mg", "Viên",
                2500, 60, 15, "Kháng sinh",
                "Traphaco", "28/02/2027"));
    }

    @Override
    public Medicine findById(int id) {
        return DATA.stream().filter(m -> m.getId() == id).findFirst().orElse(null);
    }

    @Override
    public List<Medicine> findAll() { return new ArrayList<>(DATA); }

    public List<Medicine> findLowStock() {
        return DATA.stream().filter(Medicine::isLowStock).collect(Collectors.toList());
    }

    @Override
    public boolean insert(Medicine m) {
        m.setId(nextId++);
        DATA.add(m);
        return true;
    }

    @Override
    public boolean update(Medicine m) {
        for (int i = 0; i < DATA.size(); i++) {
            if (DATA.get(i).getId() == m.getId()) {
                DATA.set(i, m);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(int id) { return DATA.removeIf(m -> m.getId() == id); }

    public int countLowStock() { return (int) DATA.stream().filter(Medicine::isLowStock).count(); }
}
