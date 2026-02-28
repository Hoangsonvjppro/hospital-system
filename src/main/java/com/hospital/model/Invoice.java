package com.hospital.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity hóa đơn thanh toán — ánh xạ bảng Invoice trong CSDL.
 *
 * Bảng Invoice:
 *   invoice_id, patient_id, record_id, invoice_date,
 *   exam_fee, medicine_fee, other_fee, discount, total_amount,
 *   paid_amount, change_amount, status (PENDING/PAID/CANCELLED),
 *   payment_method (CASH/TRANSFER/CARD), payment_date, notes, created_by,
 *   created_at, updated_at
 *
 * Các trường hiển thị (JOIN / VIEW InvoiceSummary):
 *   patient_name, patient_phone, doctor_name
 */
public class Invoice extends BaseModel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Trường DB (bảng Invoice) ─────────────────────────────

    private long patientId;
    private Long recordId;
    private LocalDateTime invoiceDate;

    private double examFee;        // Phí khám
    private double medicineFee;    // Tổng tiền thuốc
    private double otherFee;       // Phí khác (xét nghiệm...)
    private double discount;       // Giảm giá
    private double totalAmount;    // Tổng cộng

    private double paidAmount;     // Số tiền BN đưa
    private double changeAmount;   // Tiền thừa trả lại

    private String status;         // PENDING, PAID, CANCELLED
    private String paymentMethod;  // CASH, TRANSFER, CARD
    private LocalDateTime paymentDate;
    private String notes;
    private Long createdBy;

    // ── Trường hiển thị (transient — từ JOIN / VIEW) ─────────

    private String patientName;
    private String patientPhone;
    private String doctorName;

    // ── Chi tiết hóa đơn (load riêng khi cần) ───────────────

    private List<InvoiceServiceDetail> serviceDetails = new ArrayList<>();
    private List<InvoiceMedicineDetail> medicineDetails = new ArrayList<>();

    // ══════════════════════════════════════════════════════════
    //  CONSTRUCTORS
    // ══════════════════════════════════════════════════════════

    public Invoice() {
        this.status = "PENDING";
    }

    public Invoice(int id) {
        super(id);
        this.status = "PENDING";
    }

    // ══════════════════════════════════════════════════════════
    //  DISPLAY HELPERS (backward compatible với PaymentPanel cũ)
    // ══════════════════════════════════════════════════════════

    /**
     * Mã hóa đơn hiển thị: HD00001.
     */
    public String getInvoiceCode() {
        return String.format("HD%05d", id);
    }

    /**
     * Mã bệnh nhân hiển thị: BN00001.
     */
    public String getPatientCode() {
        return String.format("BN%05d", patientId);
    }

    /**
     * Ngày khám hiển thị: dd/MM/yyyy.
     */
    public String getExamDate() {
        if (invoiceDate == null) return "";
        return invoiceDate.format(DATE_FMT);
    }

    /**
     * Trạng thái hiển thị tiếng Việt.
     */
    public String getStatusDisplay() {
        if (status == null) return "";
        return switch (status) {
            case "PENDING"   -> "Chờ thanh toán";
            case "PAID"      -> "Đã thanh toán";
            case "CANCELLED" -> "Đã hủy";
            default          -> status;
        };
    }

    /**
     * Phương thức thanh toán hiển thị tiếng Việt.
     */
    public String getPaymentMethodDisplay() {
        if (paymentMethod == null) return "";
        return switch (paymentMethod) {
            case "CASH"     -> "Tiền mặt";
            case "TRANSFER" -> "Chuyển khoản";
            case "CARD"     -> "Thẻ ngân hàng";
            default         -> paymentMethod;
        };
    }

    /**
     * Tính lại tổng tiền từ các thành phần.
     */
    public void recalculate() {
        this.totalAmount = examFee + medicineFee + otherFee - discount;
    }

    // ══════════════════════════════════════════════════════════
    //  GETTERS & SETTERS — Trường DB
    // ══════════════════════════════════════════════════════════

    public long getPatientId()                     { return patientId; }
    public void setPatientId(long v)               { this.patientId = v; }

    public Long getRecordId()                      { return recordId; }
    public void setRecordId(Long v)                { this.recordId = v; }

    public LocalDateTime getInvoiceDate()          { return invoiceDate; }
    public void setInvoiceDate(LocalDateTime v)    { this.invoiceDate = v; }

    public double getExamFee()                     { return examFee; }
    public void setExamFee(double v)               { this.examFee = v; }

    public double getMedicineFee()                 { return medicineFee; }
    public void setMedicineFee(double v)           { this.medicineFee = v; }

    public double getOtherFee()                    { return otherFee; }
    public void setOtherFee(double v)              { this.otherFee = v; }

    public double getDiscount()                    { return discount; }
    public void setDiscount(double v)              { this.discount = v; }

    public double getTotalAmount()                 { return totalAmount; }
    public void setTotalAmount(double v)           { this.totalAmount = v; }

    public double getPaidAmount()                  { return paidAmount; }
    public void setPaidAmount(double v)            { this.paidAmount = v; }

    public double getChangeAmount()                { return changeAmount; }
    public void setChangeAmount(double v)          { this.changeAmount = v; }

    public String getStatus()                      { return status; }
    public void setStatus(String v)                { this.status = v; }

    public String getPaymentMethod()               { return paymentMethod; }
    public void setPaymentMethod(String v)         { this.paymentMethod = v; }

    public LocalDateTime getPaymentDate()          { return paymentDate; }
    public void setPaymentDate(LocalDateTime v)    { this.paymentDate = v; }

    public String getNotes()                       { return notes; }
    public void setNotes(String v)                 { this.notes = v; }

    public Long getCreatedBy()                     { return createdBy; }
    public void setCreatedBy(Long v)               { this.createdBy = v; }

    // ══════════════════════════════════════════════════════════
    //  GETTERS & SETTERS — Trường hiển thị (JOIN)
    // ══════════════════════════════════════════════════════════

    public String getPatientName()                 { return patientName; }
    public void setPatientName(String v)           { this.patientName = v; }

    public String getPatientPhone()                { return patientPhone; }
    public void setPatientPhone(String v)          { this.patientPhone = v; }

    public String getDoctorName()                  { return doctorName; }
    public void setDoctorName(String v)            { this.doctorName = v; }

    // ══════════════════════════════════════════════════════════
    //  GETTERS & SETTERS — Chi tiết hóa đơn
    // ══════════════════════════════════════════════════════════

    public List<InvoiceServiceDetail> getServiceDetails()           { return serviceDetails; }
    public void setServiceDetails(List<InvoiceServiceDetail> v)     { this.serviceDetails = v; }

    public List<InvoiceMedicineDetail> getMedicineDetails()         { return medicineDetails; }
    public void setMedicineDetails(List<InvoiceMedicineDetail> v)   { this.medicineDetails = v; }

    // ══════════════════════════════════════════════════════════
    //  TOSTRING
    // ══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return getInvoiceCode() + " - " + patientName;
    }
}
