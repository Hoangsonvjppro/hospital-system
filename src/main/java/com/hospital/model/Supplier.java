package com.hospital.model;

/**
 * Entity nhà cung cấp — ánh xạ bảng Supplier.
 */
public class Supplier extends BaseModel {

    private String supplierName;
    private String contactName;
    private String phone;
    private String address;
    private boolean isActive;

    public Supplier() {
        this.isActive = true;
    }

    public String getSupplierName()            { return supplierName; }
    public void setSupplierName(String v)      { this.supplierName = v; }

    public String getContactName()             { return contactName; }
    public void setContactName(String v)       { this.contactName = v; }

    public String getPhone()                   { return phone; }
    public void setPhone(String v)             { this.phone = v; }

    public String getAddress()                 { return address; }
    public void setAddress(String v)           { this.address = v; }

    public boolean isActive()                  { return isActive; }
    public void setActive(boolean v)           { this.isActive = v; }

    @Override
    public String toString() {
        return supplierName;
    }
}
