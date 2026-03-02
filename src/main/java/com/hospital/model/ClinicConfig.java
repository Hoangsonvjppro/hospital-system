package com.hospital.model;

/**
 * Model bao bọc bảng ClinicConfig (key-value).
 * <p>
 * Gom các config_key thông dụng thành các field Java có kiểu rõ ràng,
 * giúp BUS / GUI không phải nhớ tên key.
 * <p>
 * Các key hợp lệ: clinic_name, clinic_address, clinic_phone, clinic_email,
 * default_exam_fee, working_hours, invoice_prefix.
 */
public class ClinicConfig {

    // ── Enum chứa tên key ─────────────────────────────────────
    public static final String KEY_CLINIC_NAME     = "clinic_name";
    public static final String KEY_CLINIC_ADDRESS  = "clinic_address";
    public static final String KEY_CLINIC_PHONE    = "clinic_phone";
    public static final String KEY_CLINIC_EMAIL    = "clinic_email";
    public static final String KEY_EXAM_FEE        = "default_exam_fee";
    public static final String KEY_WORKING_HOURS   = "working_hours";
    public static final String KEY_INVOICE_PREFIX  = "invoice_prefix";

    // ── Fields ────────────────────────────────────────────────
    private String clinicName;
    private String clinicAddress;
    private String clinicPhone;
    private String clinicEmail;
    private double defaultExamFee;
    private String workingHours;
    private String invoicePrefix;

    // ── Constructors ──────────────────────────────────────────

    public ClinicConfig() {
        // Giá trị mặc định
        this.clinicName    = "Phòng Khám";
        this.clinicAddress = "";
        this.clinicPhone   = "";
        this.clinicEmail   = "";
        this.defaultExamFee = 150_000;
        this.workingHours  = "07:30 - 17:00";
        this.invoicePrefix = "HD";
    }

    // ── Getters / Setters ─────────────────────────────────────

    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }

    public String getClinicAddress() { return clinicAddress; }
    public void setClinicAddress(String clinicAddress) { this.clinicAddress = clinicAddress; }

    public String getClinicPhone() { return clinicPhone; }
    public void setClinicPhone(String clinicPhone) { this.clinicPhone = clinicPhone; }

    public String getClinicEmail() { return clinicEmail; }
    public void setClinicEmail(String clinicEmail) { this.clinicEmail = clinicEmail; }

    public double getDefaultExamFee() { return defaultExamFee; }
    public void setDefaultExamFee(double defaultExamFee) { this.defaultExamFee = defaultExamFee; }

    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }

    public String getInvoicePrefix() { return invoicePrefix; }
    public void setInvoicePrefix(String invoicePrefix) { this.invoicePrefix = invoicePrefix; }
}
