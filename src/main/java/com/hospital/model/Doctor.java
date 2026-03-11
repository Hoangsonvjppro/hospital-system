package com.hospital.model;

public class Doctor extends BaseModel {
    private String doctorCode;
    private String fullName;
    private String specialty;    
    private String phone;
    private String email;
    private boolean online;      
    private String avatar;        

    public Doctor() {}

    public Doctor(int id, String doctorCode, String fullName, String specialty,
                  String phone, String email, boolean online) {
        super(id);
        this.doctorCode = doctorCode;
        this.fullName   = fullName;
        this.specialty  = specialty;
        this.phone      = phone;
        this.email      = email;
        this.online     = online;
        String[] parts = fullName.split(" ");
        this.avatar = parts.length >= 2
            ? String.valueOf(parts[parts.length - 2].charAt(0)) + parts[parts.length - 1].charAt(0)
            : fullName.substring(0, Math.min(2, fullName.length())).toUpperCase();
    }

    public String getDoctorCode()   { return doctorCode; }
    public void setDoctorCode(String v) { this.doctorCode = v; }

    public String getFullName()     { return fullName; }
    public void setFullName(String v) { this.fullName = v; }

    public String getSpecialty()    { return specialty; }
    public void setSpecialty(String v) { this.specialty = v; }

    public String getPhone()        { return phone; }
    public void setPhone(String v)  { this.phone = v; }

    public String getEmail()        { return email; }
    public void setEmail(String v)  { this.email = v; }

    public boolean isOnline()       { return online; }
    public void setOnline(boolean v){ this.online = v; }

    public String getAvatar()       { return avatar; }
    public void setAvatar(String v) { this.avatar = v; }

    @Override
    public String toString() { return doctorCode + " - " + fullName; }
}
