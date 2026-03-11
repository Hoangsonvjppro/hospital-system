package com.hospital.bus;

import com.hospital.dao.DashboardDAO;
import com.hospital.dao.ReportDAO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * BUS thống kê Dashboard & Báo cáo — wrapper cho DashboardDAO và ReportDAO.
 * Không extends BaseBUS vì chỉ có statistical queries, không dùng CRUD thông thường.
 */
public class DashboardBUS {

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final ReportDAO reportDAO = new ReportDAO();

    // ── DashboardDAO delegation ───────────────────────────────

    public int countActivePatients() {
        return dashboardDAO.countActivePatients();
    }

    public int countActiveMedicines() {
        return dashboardDAO.countActiveMedicines();
    }

    public int countTodayVisits() {
        return dashboardDAO.countTodayVisits();
    }

    public int countLowStockMedicines() {
        return dashboardDAO.countLowStockMedicines();
    }

    public int countActiveDoctors() {
        return dashboardDAO.countActiveDoctors();
    }

    public int countTotalVisits() {
        return dashboardDAO.countTotalVisits();
    }

    public int countTodayByQueueStatus(String status) {
        return dashboardDAO.countTodayByQueueStatus(status);
    }

    public double getTodayRevenue() {
        return dashboardDAO.getTodayRevenue();
    }

    public int countTodayDispensedPrescriptions() {
        return dashboardDAO.countTodayDispensedPrescriptions();
    }

    public List<String[]> findLowStockMedicines() {
        return dashboardDAO.findLowStockMedicines();
    }

    public List<String[]> findLongWaitingPatients(int thresholdMinutes) {
        return dashboardDAO.findLongWaitingPatients(thresholdMinutes);
    }

    // ── ReportDAO delegation ──────────────────────────────────

    public Map<LocalDate, Double> getRevenueByDay(LocalDate from, LocalDate to) {
        return reportDAO.getRevenueByDay(from, to);
    }

    public double getRevenueTotal(LocalDate from, LocalDate to) {
        return reportDAO.getRevenueTotal(from, to);
    }

    public Map<String, Integer> getInvoiceCountByStatus(LocalDate from, LocalDate to) {
        return reportDAO.getInvoiceCountByStatus(from, to);
    }

    public List<Object[]> getTopMedicines(int limit) {
        return reportDAO.getTopMedicines(limit);
    }

    public List<Object[]> getTopDiseases(int limit) {
        return reportDAO.getTopDiseases(limit);
    }
}
