package com.busapp.model;

public class Conductor extends CrewMember {
    private static final long serialVersionUID = 1L;

    public Conductor(String id, String name, String phone, String password) { super(id, name, phone, password); }
    @Override public Role getRole() { return Role.CONDUCTOR; }
    @Override public double getCommissionRate() { return 0.03; }   // 3% of fares on own bus
}
