package com.busapp.model;

public class Driver extends CrewMember {
    private static final long serialVersionUID = 1L;
    private final String licenseNo;

    public Driver(String id, String name, String phone, String password, String licenseNo) {
        super(id, name, phone, password);
        if (licenseNo == null || licenseNo.isBlank()) throw new IllegalArgumentException("Driving licence number is required.");
        this.licenseNo = licenseNo.trim().toUpperCase();
    }
    public String getLicenseNo() { return licenseNo; }
    @Override public Role getRole() { return Role.DRIVER; }
    @Override public double getCommissionRate() { return 0.02; }   // 2% of fares on own bus
}
