package com.hospital.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Invoice extends BaseModel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

   

    private long patientId;
    private Long recordId;
    private LocalDateTime invoiceDate;

    private double examFee;        
    private double medicineFee;   
    private double otherFee;      
    private double discount;      
    private double totalAmount; 

    private double paidAmount;    
    private double changeAmount;   

    private String status;      
    private String paymentMethod;  
    private LocalDateTime paymentDate;
    private String notes;
    private Long createdBy;


    private String patientName;
    private String patientPhone;
    private String doctorName;


    private List<InvoiceServiceDetail> serviceDetails = new ArrayList<>();
    private List<InvoiceMedicineDetail> medicineDetails = new ArrayList<>();


    public Invoice() {
        this.status = "PENDING";
    }

    public Invoice(int id) {
        super(id);
        this.status = "PENDING";
    }


    public String getInvoiceCode() {
        return String.format("HD%05d", id);
    }


    public String getPatientCode() {
        return String.format("BN%05d", patientId);
    }

    public String getExamDate() {
        if (invoiceDate == null) return "";
        return invoiceDate.format(DATE_FMT);
    }

    public String getStatusDisplay() {
        if (status == null) return "";
        return switch (status) {
            case "PENDING"   -> "Chờ thanh toán";
            case "PAID"      -> "Đã thanh toán";
            case "CANCELLED" -> "Đã hủy";
            default          -> status;
        };
    }

    public String getPaymentMethodDisplay() {
        if (paymentMethod == null) return "";
        return switch (paymentMethod) {
            case "CASH"     -> "Tiền mặt";
            case "TRANSFER" -> "Chuyển khoản";
            case "CARD"     -> "Thẻ ngân hàng";
            default         -> paymentMethod;
        };
    }

    public void recalculate() {
        this.totalAmount = examFee + medicineFee + otherFee - discount;
    }

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


    public String getPatientName()                 { return patientName; }
    public void setPatientName(String v)           { this.patientName = v; }

    public String getPatientPhone()                { return patientPhone; }
    public void setPatientPhone(String v)          { this.patientPhone = v; }

    public String getDoctorName()                  { return doctorName; }
    public void setDoctorName(String v)            { this.doctorName = v; }



    public List<InvoiceServiceDetail> getServiceDetails()           { return serviceDetails; }
    public void setServiceDetails(List<InvoiceServiceDetail> v)     { this.serviceDetails = v; }

    public List<InvoiceMedicineDetail> getMedicineDetails()         { return medicineDetails; }
    public void setMedicineDetails(List<InvoiceMedicineDetail> v)   { this.medicineDetails = v; }

    @Override
    public String toString() {
        return getInvoiceCode() + " - " + patientName;
    }
}
