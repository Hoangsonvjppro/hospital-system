package com.hospital.bus;

import com.hospital.config.DatabaseConfig;
import com.hospital.dao.InvoiceDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.Invoice;
import com.hospital.model.InvoiceMedicineDetail;
import com.hospital.model.InvoiceServiceDetail;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Business logic layer cho hóa đơn.
 *
 * Quy ước:
 * - Mọi truy vấn Invoice đều ủy quyền cho {@link InvoiceDAO}.
 * - Logic tạo hóa đơn từ Medical Record ({@link #createInvoiceFromMedicalRecord})
 *   cần truy cập cross-domain (MedicalRecord, Prescription, ServiceOrder, ClinicConfig)
 *   → dùng transaction trong BUS (không có DAO riêng cho các bảng đó).
 */
public class InvoiceBUS extends BaseBUS<Invoice> {

    private static final Logger LOGGER = Logger.getLogger(InvoiceBUS.class.getName());

    private final InvoiceDAO invoiceDAO;

    public InvoiceBUS() {
        super(new InvoiceDAO());
        this.invoiceDAO = (InvoiceDAO) dao;
    }

    @Override
    protected void validate(Invoice inv) {
        if (inv == null) throw new BusinessException("Dữ liệu hóa đơn không hợp lệ");
        if (inv.getPatientId() <= 0) throw new BusinessException("Mã bệnh nhân không hợp lệ");
        if (inv.getExamFee() < 0 || inv.getMedicineFee() < 0)
            throw new BusinessException("Phí khám hoặc tiền thuốc không được âm");
    }

    // ═══════════════════════════════════════════════════════════
    //  TRUY VẤN — ủy quyền InvoiceDAO
    // ═══════════════════════════════════════════════════════════

    public List<Invoice> getPendingInvoices() {
        return invoiceDAO.findByStatus("PENDING");
    }

    public List<Invoice> getPaidInvoices() {
        return invoiceDAO.findByStatus("PAID");
    }

    public double getTotalRevenue() {
        return invoiceDAO.getTotalRevenue();
    }

    /**
     * Lấy hóa đơn kèm đầy đủ chi tiết dịch vụ + thuốc.
     * Dùng cho panel chi tiết / in hóa đơn.
     */
    public Invoice getInvoiceDetails(long invoiceId) {
        return invoiceDAO.getInvoiceWithDetails(invoiceId);
    }

    // ═══════════════════════════════════════════════════════════
    //  THANH TOÁN
    // ═══════════════════════════════════════════════════════════

    /**
     * Xác nhận thanh toán hóa đơn.
     *
     * @param invoiceId   ID hóa đơn
     * @param method      Phương thức: "Tiền mặt" / "Chuyển khoản" / "Thẻ ngân hàng" hoặc mã DB
     * @param paidAmount  Số tiền khách đưa
     * @param changeAmount Tiền thừa trả lại
     */
    public boolean markAsPaid(int invoiceId, String method,
                              double paidAmount, double changeAmount) {
        Invoice inv = invoiceDAO.findById(invoiceId);
        if (inv == null) return false;
        if ("PAID".equals(inv.getStatus())) return false;   // đã thanh toán rồi

        String dbMethod = mapPaymentMethod(method);
        return invoiceDAO.updatePayment(invoiceId, dbMethod, paidAmount, changeAmount);
    }

    /**
     * Overload đơn giản — khách đưa đúng tổng, không có tiền thừa.
     */
    public boolean markAsPaid(int invoiceId, String method) {
        Invoice inv = invoiceDAO.findById(invoiceId);
        if (inv == null) return false;
        return markAsPaid(invoiceId, method, inv.getTotalAmount(), 0);
    }

    // ═══════════════════════════════════════════════════════════
    //  TẠO HÓA ĐƠN TỪ MEDICAL RECORD (Transaction)
    // ═══════════════════════════════════════════════════════════

    /**
     * Tạo hóa đơn tự động từ hồ sơ bệnh án.
     *
     * Logic:
     *  1. Lấy thông tin MedicalRecord (patient_id).
     *  2. Lấy phí khám mặc định từ ClinicConfig ('default_exam_fee').
     *  3. Lấy danh sách dịch vụ chỉ định (ServiceOrder JOIN Service).
     *  4. Lấy danh sách thuốc kê đơn (Prescription → PrescriptionDetail JOIN Medicine).
     *  5. Tính tổng → tạo Invoice + detail rows trong 1 transaction.
     *
     * @param medicalRecordId record_id trong bảng MedicalRecord
     * @return Invoice đã tạo (có id), hoặc throw BusinessException nếu lỗi
     */
    public Invoice createInvoiceFromMedicalRecord(long medicalRecordId) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getInstance().getTransactionalConnection();
            InvoiceDAO txDao = new InvoiceDAO(conn);

            // 1. Lấy thông tin bệnh án
            long patientId = txDao.getPatientIdByRecordId(medicalRecordId);
            if (patientId < 0) {
                throw new BusinessException("Không tìm thấy bệnh án ID=" + medicalRecordId);
            }

            // 2. Phí khám mặc định từ ClinicConfig
            double examFee = new ClinicConfigBUS().getDefaultExamFee();

            // 3. Dịch vụ chỉ định
            List<InvoiceServiceDetail> serviceDetails = txDao.getServiceDetailsForRecord(medicalRecordId);
            double serviceFee = 0;
            for (InvoiceServiceDetail d : serviceDetails) {
                serviceFee += d.getLineTotal();
            }

            // 4. Thuốc kê đơn
            List<InvoiceMedicineDetail> medicineDetails = txDao.getMedicineDetailsForRecord(medicalRecordId);
            double medicineFee = 0;
            for (InvoiceMedicineDetail d : medicineDetails) {
                medicineFee += d.getLineTotal();
            }

            // 5. Tạo Invoice
            double totalAmount = examFee + medicineFee + serviceFee;

            Invoice invoice = new Invoice();
            invoice.setPatientId(patientId);
            invoice.setRecordId(medicalRecordId);
            invoice.setInvoiceDate(LocalDateTime.now());
            invoice.setExamFee(examFee);
            invoice.setServiceFee(serviceFee);
            invoice.setMedicineFee(medicineFee);
            invoice.setOtherFee(0);
            invoice.setDiscount(0);
            invoice.setTotalAmount(totalAmount);
            invoice.setStatus("PENDING");

            txDao.insert(invoice);
            long invoiceId = invoice.getId();

            // Insert chi tiết dịch vụ
            for (InvoiceServiceDetail d : serviceDetails) {
                d.setInvoiceId(invoiceId);
                txDao.insertServiceDetail(d);
            }

            // Insert chi tiết thuốc
            for (InvoiceMedicineDetail d : medicineDetails) {
                d.setInvoiceId(invoiceId);
                txDao.insertMedicineDetail(d);
            }

            conn.commit();

            invoice.setServiceDetails(serviceDetails);
            invoice.setMedicineDetails(medicineDetails);
            return invoice;

        } catch (BusinessException e) {
            rollback(conn);
            throw e;
        } catch (Exception e) {
            rollback(conn);
            LOGGER.log(Level.SEVERE, "Lỗi tạo hóa đơn từ bệnh án ID=" + medicalRecordId, e);
            throw new BusinessException("Không thể tạo hóa đơn từ bệnh án: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPER
    // ═══════════════════════════════════════════════════════════

    /**
     * Map phương thức thanh toán tiếng Việt → mã DB.
     */
    private String mapPaymentMethod(String displayMethod) {
        if (displayMethod == null) return null;
        return switch (displayMethod) {
            case "Tiền mặt"      -> "CASH";
            case "Chuyển khoản"  -> "TRANSFER";
            case "Thẻ ngân hàng" -> "CARD";
            default              -> displayMethod;
        };
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ignored) {}
        }
    }
}
