package com.busapp.model;

/** Shared parent of drivers and conductors: both are assigned to one bus and earn commission. */
public abstract class CrewMember extends User implements Commissionable {
    private static final long serialVersionUID = 1L;
    private String assignedBusId;

    protected CrewMember(String id, String name, String phone, String password) { super(id, name, phone, password); }

    public String getAssignedBusId() { return assignedBusId; }
    public void setAssignedBusId(String busId) { this.assignedBusId = busId; }
}
