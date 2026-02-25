package com.hospital.dao;

import com.hospital.model.Invoice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DAO hóa đơn – dùng mock data.
 */
public class InvoiceDAO implements BaseDAO<Invoice> {

    private static final List<Invoice> DATA = new ArrayList<>();
    private static int nextId = 7;

    static {
        DATA.add(new Invoice(1, "HD001", "BN005", "Hoàng Văn E",
                "Dr. Lê Văn C", "20/02/2026",
                150000, 250000, "Tiền mặt", "Đã thanh toán"));
        DATA.add(new Invoice(2, "HD002", "BN006", "Vũ Thị F",
                "Dr. Trần Thị D", "20/02/2026",
                200000, 180000, "Chuyển khoản", "Đã thanh toán"));
        DATA.add(new Invoice(3, "HD003", "BN001", "Nguyễn Văn A",
                "Dr. Nguyễn Văn E", "19/02/2026",
                150000, 320000, "Tiền mặt", "Đã thanh toán"));
        DATA.add(new Invoice(4, "HD004", "BN002", "Trần Thị B",
                "Dr. Phạm Thị F", "19/02/2026",
                350000, 0, "Thẻ ngân hàng", "Chờ thanh toán"));
        DATA.add(new Invoice(5, "HD005", "BN003", "Lê Văn C",
                "Dr. Lê Văn C", "18/02/2026",
                150000, 475000, "Tiền mặt", "Đã thanh toán"));
        DATA.add(new Invoice(6, "HD006", "BN004", "Phạm Thị D",
                "Dr. Trần Thị D", "18/02/2026",
                200000, 120000, "Chuyển khoản", "Chờ thanh toán"));
    }

    @Override
    public Invoice findById(int id) {
        return DATA.stream().filter(i -> i.getId() == id).findFirst().orElse(null);
    }

    @Override
    public List<Invoice> findAll() { return new ArrayList<>(DATA); }

    public List<Invoice> findByStatus(String status) {
        return DATA.stream()
                .filter(i -> i.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    public double getTotalRevenue() {
        return DATA.stream()
                .filter(i -> "Đã thanh toán".equalsIgnoreCase(i.getStatus()))
                .mapToDouble(Invoice::getTotalAmount).sum();
    }

    @Override
    public boolean insert(Invoice inv) {
        inv.setId(nextId++);
        DATA.add(inv);
        return true;
    }

    @Override
    public boolean update(Invoice inv) {
        for (int i = 0; i < DATA.size(); i++) {
            if (DATA.get(i).getId() == inv.getId()) {
                DATA.set(i, inv);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(int id) { return DATA.removeIf(i -> i.getId() == id); }
}
