package com.busapp.model;

import java.time.LocalDateTime;

public class ReplacementRequest implements Identifiable {
    public enum Status { PENDING, APPROVED, REJECTED }
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String brokenBusId;
    private final String reportedBy;
    private final String reason;
    private Status status = Status.PENDING;
    private String replacementBusId;
    private final LocalDateTime createdAt = LocalDateTime.now();

    public ReplacementRequest(String id, String brokenBusId, String reportedBy, String reason) {
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("Please give the reason for the breakdown.");
        this.id = id; this.brokenBusId = brokenBusId; this.reportedBy = reportedBy; this.reason = reason.trim();
    }
    @Override public String getId() { return id; }
    public String getBrokenBusId() { return brokenBusId; }
    public String getReportedBy() { return reportedBy; }
    public String getReason() { return reason; }
    public Status getStatus() { return status; }
    public String getReplacementBusId() { return replacementBusId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void approve(String replacementBusId) { this.status = Status.APPROVED; this.replacementBusId = replacementBusId; }
    public void reject() { this.status = Status.REJECTED; }
}
