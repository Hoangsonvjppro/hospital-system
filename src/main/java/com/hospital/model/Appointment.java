package com.hospital.model;

/**
 * Model lịch hẹn khám.
 */
public class Appointment extends BaseModel {
    private String appointmentCode;
    private String patientName;
    private String patientPhone;
    private String doctorName;
    private String specialty;
    private String date;          // dd/MM/yyyy
    private String time;          // HH:mm (giờ bắt đầu)
    private String endTime;       // HH:mm (giờ kết thúc)
    private String status;        // Mới / Đã xác nhận / Đã khám / Hủy
    private String note;

    public Appointment() {}

    public Appointment(int id, String appointmentCode, String patientName,
                       String patientPhone, String doctorName, String specialty,
                       String date, String time, String status, String note) {
        this(id, appointmentCode, patientName, patientPhone, doctorName, specialty,
             date, time, "", status, note);
    }

    public Appointment(int id, String appointmentCode, String patientName,
                       String patientPhone, String doctorName, String specialty,
                       String date, String time, String endTime, String status, String note) {
        super(id);
        this.appointmentCode = appointmentCode;
        this.patientName     = patientName;
        this.patientPhone    = patientPhone;
        this.doctorName      = doctorName;
        this.specialty       = specialty;
        this.date            = date;
        this.time            = time;
        this.endTime         = endTime;
        this.status          = status;
        this.note            = note;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public String getAppointmentCode()  { return appointmentCode; }
    public void setAppointmentCode(String v) { this.appointmentCode = v; }

    public String getPatientName()   { return patientName; }
    public void setPatientName(String v) { this.patientName = v; }

    public String getPatientPhone()  { return patientPhone; }
    public void setPatientPhone(String v) { this.patientPhone = v; }

    public String getDoctorName()    { return doctorName; }
    public void setDoctorName(String v) { this.doctorName = v; }

    public String getSpecialty()     { return specialty; }
    public void setSpecialty(String v){ this.specialty = v; }

    public String getDate()          { return date; }
    public void setDate(String v)    { this.date = v; }

    public String getTime()          { return time; }
    public void setTime(String v)    { this.time = v; }

    public String getEndTime()       { return endTime; }
    public void setEndTime(String v) { this.endTime = v; }

    public String getStatus()        { return status; }
    public void setStatus(String v)  { this.status = v; }

    public String getNote()          { return note; }
    public void setNote(String v)    { this.note = v; }

    @Override
    public String toString() { return appointmentCode + " - " + patientName; }
}
