package com.hospital.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Entity lịch hẹn khám — ánh xạ bảng Appointment.
 */
public class Appointment extends BaseModel {
    private long patientId;
    private long doctorId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status; // SCHEDULED, CHECKED_IN, COMPLETED, CANCELLED
    private String reason;
    private Long createdBy;

    // Transient — populated by JOIN
    private String patientName;
    private String patientPhone;
    private String doctorName;
    private String specialtyName;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public Appointment() {
        this.status = "SCHEDULED";
    }

    // ── Convenience display methods ──────────────────────────────────────────
    public String getFormattedDate() { return appointmentDate != null ? appointmentDate.format(DATE_FMT) : ""; }
    public String getFormattedStartTime() { return startTime != null ? startTime.format(TIME_FMT) : ""; }
    public String getFormattedEndTime() { return endTime != null ? endTime.format(TIME_FMT) : ""; }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public long getPatientId()               { return patientId; }
    public void setPatientId(long v)         { this.patientId = v; }

    public long getDoctorId()                { return doctorId; }
    public void setDoctorId(long v)          { this.doctorId = v; }

    public LocalDate getAppointmentDate()    { return appointmentDate; }
    public void setAppointmentDate(LocalDate v) { this.appointmentDate = v; }

    public LocalTime getStartTime()          { return startTime; }
    public void setStartTime(LocalTime v)    { this.startTime = v; }

    public LocalTime getEndTime()            { return endTime; }
    public void setEndTime(LocalTime v)      { this.endTime = v; }

    public String getStatus()                { return status; }
    public void setStatus(String v)          { this.status = v; }

    public String getReason()                { return reason; }
    public void setReason(String v)          { this.reason = v; }

    public Long getCreatedBy()               { return createdBy; }
    public void setCreatedBy(Long v)         { this.createdBy = v; }

    public String getPatientName()           { return patientName; }
    public void setPatientName(String v)     { this.patientName = v; }

    public String getPatientPhone()          { return patientPhone; }
    public void setPatientPhone(String v)    { this.patientPhone = v; }

    public String getDoctorName()            { return doctorName; }
    public void setDoctorName(String v)      { this.doctorName = v; }

    public String getSpecialtyName()         { return specialtyName; }
    public void setSpecialtyName(String v)   { this.specialtyName = v; }

    @Override
    public String toString() { return "Appointment #" + id + " - " + patientName; }
}
