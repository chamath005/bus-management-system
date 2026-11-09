package com.busapp.model;

import java.time.LocalDateTime;

public class Ticket implements Identifiable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String busId;
    private final String routeId;
    private final String passengerId;        // null for walk-in passengers
    private final String passengerName;
    private final int boardIndex;
    private final int destIndex;
    private final String boardStop;
    private final String destStop;
    private int seatNo;                      // 0 = standing
    private final double fare;
    private final String issuedBy;           // "ONLINE" or a conductor id
    private final String driverId;
    private final String conductorId;
    private final String paymentName;
    private TicketStatus status = TicketStatus.ACTIVE;
    private final LocalDateTime createdAt = LocalDateTime.now();

    public Ticket(String id, String busId, String routeId, String passengerId, String passengerName,
                  int boardIndex, int destIndex, String boardStop, String destStop, int seatNo,
                  double fare, String issuedBy, String driverId, String conductorId, String paymentName) {
        this.id = id; this.busId = busId; this.routeId = routeId; this.passengerId = passengerId;
        this.passengerName = passengerName; this.boardIndex = boardIndex; this.destIndex = destIndex;
        this.boardStop = boardStop; this.destStop = destStop; this.seatNo = seatNo; this.fare = fare;
        this.issuedBy = issuedBy; this.driverId = driverId; this.conductorId = conductorId;
        this.paymentName = paymentName;
    }

    @Override public String getId() { return id; }
    public String getBusId() { return busId; }
    public void setBusId(String busId) { this.busId = busId; }
    public String getRouteId() { return routeId; }
    public String getPassengerId() { return passengerId; }
    public String getPassengerName() { return passengerName; }
    public int getBoardIndex() { return boardIndex; }
    public int getDestIndex() { return destIndex; }
    public String getBoardStop() { return boardStop; }
    public String getDestStop() { return destStop; }
    public int getSeatNo() { return seatNo; }
    public void setSeatNo(int seatNo) { this.seatNo = seatNo; }
    public double getFare() { return fare; }
    public String getIssuedBy() { return issuedBy; }
    public String getDriverId() { return driverId; }
    public String getConductorId() { return conductorId; }
    public String getPaymentName() { return paymentName; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return status == TicketStatus.ACTIVE; }

    public String seatText() { return seatNo == 0 ? "Standing" : "Seat " + seatNo; }

    @Override public String toString() {
        return String.format("%-7s %-16s %s -> %s  %-9s Rs.%8.2f  %s", id, passengerName, boardStop, destStop,
                seatText(), fare, status);
    }
}
