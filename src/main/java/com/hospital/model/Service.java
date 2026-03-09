package com.hospital.model;

/**
 * Entity dịch vụ — ánh xạ bảng Service.
 */
public class Service extends BaseModel {

    private String serviceName;
    private String serviceType; // EXAMINATION, LAB_TEST, IMAGING, PROCEDURE
    private double price;
    private String description;
    private boolean isActive;

    public Service() {
        this.isActive = true;
    }

    public String getServiceName()            { return serviceName; }
    public void setServiceName(String v)      { this.serviceName = v; }

    public String getServiceType()            { return serviceType; }
    public void setServiceType(String v)      { this.serviceType = v; }

    public double getPrice()                  { return price; }
    public void setPrice(double v)            { this.price = v; }

    public String getDescription()            { return description; }
    public void setDescription(String v)      { this.description = v; }

    public boolean isActive()                 { return isActive; }
    public void setActive(boolean v)          { this.isActive = v; }

    @Override
    public String toString() {
        return serviceName + " (" + serviceType + ")";
    }
}
