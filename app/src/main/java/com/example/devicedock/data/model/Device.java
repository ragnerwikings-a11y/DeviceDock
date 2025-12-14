package com.example.devicedock.data.model;

public class Device {
    private long id;
    private String name;
    private String ipAddress;
    private String serviceType;
    private boolean isOnline;

    public Device(long id, String name, String ipAddress, String serviceType, boolean isOnline) {
        this.id = id;
        this.name = name;
        this.ipAddress = ipAddress;
        this.serviceType = serviceType;
        this.isOnline = isOnline;
    }

    public Device(String name, String ipAddress, String serviceType) {
        this(-1, name, ipAddress, serviceType, true);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getIpAddress() { return ipAddress; }
    public String getServiceType() { return serviceType; }
    public boolean isOnline() { return isOnline; }

    public void setId(long id) { this.id = id; }
    public void setOnline(boolean online) { isOnline = online; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return name.equals(device.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
