package com.hospital.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Model lịch hẹn khám.
 */
public class Appointment extends BaseModel {
    private String appointmentCode;
    private int patientId;
    private int doctorId;
    private String patientName;      // transient — populated by JOIN
    private String patientPhone;
    private String doctorName;       // transient — populated by JOIN
    private String specialty;
    private LocalDate date;
    private LocalTime time;          // Giờ bắt đầu
    private LocalTime endTime;       // Giờ kết thúc
    private String status;           // Mới / Đã xác nhận / Đã khám / Hủy
    private String note;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

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
        this.date            = LocalDate.parse(date, DATE_FMT);
        this.time            = LocalTime.parse(time, TIME_FMT);
        this.endTime         = (endTime != null && !endTime.isEmpty()) ? LocalTime.parse(endTime, TIME_FMT) : null;
        this.status          = status;
        this.note            = note;
    }

    // ── Convenience display methods ──────────────────────────────────────────
    public String getFormattedDate() { return date != null ? date.format(DATE_FMT) : ""; }
    public String getFormattedTime() { return time != null ? time.format(TIME_FMT) : ""; }
    public String getFormattedEndTime() { return endTime != null ? endTime.format(TIME_FMT) : ""; }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public String getAppointmentCode()  { return appointmentCode; }
    public void setAppointmentCode(String v) { this.appointmentCode = v; }

    public int getPatientId()        { return patientId; }
    public void setPatientId(int v)  { this.patientId = v; }

    public int getDoctorId()         { return doctorId; }
    public void setDoctorId(int v)   { this.doctorId = v; }

    public String getPatientName()   { return patientName; }
    public void setPatientName(String v) { this.patientName = v; }

    public String getPatientPhone()  { return patientPhone; }
    public void setPatientPhone(String v) { this.patientPhone = v; }

    public String getDoctorName()    { return doctorName; }
    public void setDoctorName(String v) { this.doctorName = v; }

    public String getSpecialty()     { return specialty; }
    public void setSpecialty(String v){ this.specialty = v; }

    public LocalDate getDate()       { return date; }
    public void setDate(LocalDate v) { this.date = v; }

    public LocalTime getTime()       { return time; }
    public void setTime(LocalTime v) { this.time = v; }

    public LocalTime getEndTime()    { return endTime; }
    public void setEndTime(LocalTime v) { this.endTime = v; }

    public String getStatus()        { return status; }
    public void setStatus(String v)  { this.status = v; }

    public String getNote()          { return note; }
    public void setNote(String v)    { this.note = v; }

    @Override
    public String toString() { return appointmentCode + " - " + patientName; }
}
