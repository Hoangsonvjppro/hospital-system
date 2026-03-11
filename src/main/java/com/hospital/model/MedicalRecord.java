package com.hospital.model;

import java.time.LocalDateTime;


public class MedicalRecord extends BaseModel {

    private long patientId;
    private long doctorId;
    private Long appointmentId;
    private LocalDateTime visitDate;

    private String symptoms;       
    private String diagnosis;     

    private double weight;         
    private double height;        
    private String bloodPressure; 
    private int pulse;            
    private double temperature;    
    private int spo2;            

    private String diagnosisCode;  
    private String notes;         

    private String status;         

    public static final String STATUS_WAITING     = "WAITING";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS"; 
    public static final String STATUS_PRESCRIBED  = "PRESCRIBED";
    public static final String STATUS_COMPLETED   = "COMPLETED";
    public static final String STATUS_PAID        = "PAID";

    private String priority;       
    private Integer queueNumber;
    private java.time.LocalTime arrivalTime; 
    private String examTypeField;   
    private java.time.LocalDate followUpDate; 

    public MedicalRecord() {}

    public MedicalRecord(int id, long patientId, long doctorId, Long appointmentId) {
        super(id);
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.visitDate = LocalDateTime.now();
        this.status = STATUS_WAITING;
    }


    public long getPatientId()                { return patientId; }
    public void setPatientId(long v)          { this.patientId = v; }

    public long getDoctorId()                 { return doctorId; }
    public void setDoctorId(long v)           { this.doctorId = v; }

    public Long getAppointmentId()            { return appointmentId; }
    public void setAppointmentId(Long v)      { this.appointmentId = v; }

    public LocalDateTime getVisitDate()       { return visitDate; }
    public void setVisitDate(LocalDateTime v) { this.visitDate = v; }

    public String getSymptoms()               { return symptoms; }
    public void setSymptoms(String v)         { this.symptoms = v; }

    public String getDiagnosis()              { return diagnosis; }
    public void setDiagnosis(String v)        { this.diagnosis = v; }

    public String getPriority()               { return priority; }
    public void setPriority(String v)         { this.priority = v; }

    public Integer getQueueNumber()           { return queueNumber; }
    public void setQueueNumber(Integer v)     { this.queueNumber = v; }

    public java.time.LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(java.time.LocalTime v) { this.arrivalTime = v; }

    public String getExamTypeField()          { return examTypeField; }
    public void setExamTypeField(String v)    { this.examTypeField = v; }

    public java.time.LocalDate getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(java.time.LocalDate v) { this.followUpDate = v; }

    public double getWeight()                 { return weight; }
    public void setWeight(double v)           { this.weight = v; }

    public double getHeight()                 { return height; }
    public void setHeight(double v)           { this.height = v; }

    public String getBloodPressure()          { return bloodPressure; }
    public void setBloodPressure(String v)    { this.bloodPressure = v; }

    public int getPulse()                     { return pulse; }
    public void setPulse(int v)               { this.pulse = v; }

    public double getTemperature()            { return temperature; }
    public void setTemperature(double v)      { this.temperature = v; }

    public int getSpo2()                      { return spo2; }
    public void setSpo2(int v)                { this.spo2 = v; }

    public String getDiagnosisCode()          { return diagnosisCode; }
    public void setDiagnosisCode(String v)    { this.diagnosisCode = v; }

    public String getNotes()                  { return notes; }
    public void setNotes(String v)            { this.notes = v; }

    public String getStatus()                 { return status; }
    public void setStatus(String v)           { this.status = v; }

    @Override
    public String toString() {
        return "MedicalRecord{id=" + id + ", patientId=" + patientId + ", doctorId=" + doctorId + "}";
    }
}
