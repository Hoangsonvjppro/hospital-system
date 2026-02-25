package com.hospital.model;

/**
 * Model hóa đơn thanh toán.
 */
public class Invoice extends BaseModel {
    private String invoiceCode;
    private String patientCode;
    private String patientName;
    private String doctorName;
    private String examDate;       // dd/MM/yyyy
    private double examFee;        // Phí khám
    private double medicineFee;    // Phí thuốc
    private double totalAmount;    // Tổng tiền
    private String paymentMethod;  // Tiền mặt / Chuyển khoản / Thẻ
    private String status;         // Đã thanh toán / Chờ thanh toán

    public Invoice() {}

    public Invoice(int id, String invoiceCode, String patientCode, String patientName,
                   String doctorName, String examDate,
                   double examFee, double medicineFee,
                   String paymentMethod, String status) {
        super(id);
        this.invoiceCode   = invoiceCode;
        this.patientCode   = patientCode;
        this.patientName   = patientName;
        this.doctorName    = doctorName;
        this.examDate      = examDate;
        this.examFee       = examFee;
        this.medicineFee   = medicineFee;
        this.totalAmount   = examFee + medicineFee;
        this.paymentMethod = paymentMethod;
        this.status        = status;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public String getInvoiceCode()   { return invoiceCode; }
    public void setInvoiceCode(String v) { this.invoiceCode = v; }

    public String getPatientCode()   { return patientCode; }
    public void setPatientCode(String v) { this.patientCode = v; }

    public String getPatientName()   { return patientName; }
    public void setPatientName(String v) { this.patientName = v; }

    public String getDoctorName()    { return doctorName; }
    public void setDoctorName(String v) { this.doctorName = v; }

    public String getExamDate()      { return examDate; }
    public void setExamDate(String v){ this.examDate = v; }

    public double getExamFee()       { return examFee; }
    public void setExamFee(double v) { this.examFee = v; recalc(); }

    public double getMedicineFee()   { return medicineFee; }
    public void setMedicineFee(double v) { this.medicineFee = v; recalc(); }

    public double getTotalAmount()   { return totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String v) { this.paymentMethod = v; }

    public String getStatus()        { return status; }
    public void setStatus(String v)  { this.status = v; }

    private void recalc() { this.totalAmount = examFee + medicineFee; }

    @Override
    public String toString() { return invoiceCode + " - " + patientName; }
}
