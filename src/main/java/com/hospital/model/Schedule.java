package com.hospital.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Entity ca làm việc bác sĩ — ánh xạ bảng Schedule.
 */
public class Schedule extends BaseModel {

    private long doctorId;
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String notes;

    // Transient
    private String doctorName;

    public Schedule() {}

    public long getDoctorId()                { return doctorId; }
    public void setDoctorId(long v)          { this.doctorId = v; }

    public LocalDate getWorkDate()           { return workDate; }
    public void setWorkDate(LocalDate v)     { this.workDate = v; }

    public LocalTime getStartTime()          { return startTime; }
    public void setStartTime(LocalTime v)    { this.startTime = v; }

    public LocalTime getEndTime()            { return endTime; }
    public void setEndTime(LocalTime v)      { this.endTime = v; }

    public String getNotes()                 { return notes; }
    public void setNotes(String v)           { this.notes = v; }

    public String getDoctorName()            { return doctorName; }
    public void setDoctorName(String v)      { this.doctorName = v; }

    @Override
    public String toString() {
        return "Schedule{doctorId=" + doctorId + ", date=" + workDate + "}";
    }
}
