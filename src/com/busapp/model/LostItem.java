package com.busapp.model;

import java.time.LocalDateTime;

public class LostItem implements Identifiable {
    public enum Status { OPEN, FOUND, CLOSED }
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String passengerId;
    private final String busId;
    private final String description;
    private Status status = Status.OPEN;
    private String note = "";
    private final LocalDateTime reportedAt = LocalDateTime.now();

    public LostItem(String id, String passengerId, String busId, String description) {
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Please describe the item.");
        this.id = id; this.passengerId = passengerId; this.busId = busId; this.description = description.trim();
    }
    @Override public String getId() { return id; }
    public String getPassengerId() { return passengerId; }
    public String getBusId() { return busId; }
    public String getDescription() { return description; }
    public Status getStatus() { return status; }
    public void setStatus(Status s) { this.status = s; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getReportedAt() { return reportedAt; }
}
