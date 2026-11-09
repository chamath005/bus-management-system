package com.busapp.model;

import com.busapp.exception.SeatUnavailableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Bus implements Identifiable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String plateNo;
    private final int capacity;
    private final String ownerId;
    private String routeId;
    private String driverId;
    private String conductorId;
    private BusStatus status = BusStatus.ACTIVE;
    private double speedKmh;
    private double maxSpeedKmh;
    private int stopIndex;
    private int tripCount;
    private int reportedHeadcount = -1;          // conductor's physical count; -1 = not reported
    private final Map<Integer, String> seats = new TreeMap<>();   // seat number -> ticket id
    private final List<String> alerts = new ArrayList<>();

    public Bus(String id, String plateNo, int capacity, String ownerId) {
        if (plateNo == null || plateNo.isBlank()) throw new IllegalArgumentException("Number plate is required.");
        if (capacity < 10 || capacity > 80) throw new IllegalArgumentException("Seat capacity must be between 10 and 80.");
        this.id = id;
        this.plateNo = plateNo.trim().toUpperCase();
        this.capacity = capacity;
        this.ownerId = ownerId;
    }

    @Override public String getId() { return id; }
    public String getPlateNo() { return plateNo; }
    public int getCapacity() { return capacity; }
    public String getOwnerId() { return ownerId; }
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    public String getConductorId() { return conductorId; }
    public void setConductorId(String conductorId) { this.conductorId = conductorId; }
    public BusStatus getStatus() { return status; }
    public void setStatus(BusStatus status) { this.status = status; }
    public double getSpeedKmh() { return speedKmh; }
    public double getMaxSpeedKmh() { return maxSpeedKmh; }
    public int getStopIndex() { return stopIndex; }
    public void setStopIndex(int stopIndex) { this.stopIndex = stopIndex; }
    public int getTripCount() { return tripCount; }
    public void incrementTrip() { tripCount++; }
    public int getReportedHeadcount() { return reportedHeadcount; }
    public void setReportedHeadcount(int n) {
        if (n < -1 || n > capacity + 15) throw new IllegalArgumentException("Headcount must be between 0 and " + (capacity + 15) + ".");
        this.reportedHeadcount = n;
    }
    public List<String> getAlerts() { return Collections.unmodifiableList(alerts); }
    public void addAlert(String a) { alerts.add(a); }

    public void setSpeed(double kmh) {
        if (kmh < 0 || kmh > 140) throw new IllegalArgumentException("Speed must be between 0 and 140 km/h.");
        speedKmh = kmh;
        maxSpeedKmh = Math.max(maxSpeedKmh, kmh);
    }

    // ----- seats -----
    public boolean isSeatTaken(int seat) { return seats.containsKey(seat); }
    public int freeSeats() { return capacity - seats.size(); }

    public void assignSeat(int seat, String ticketId) throws SeatUnavailableException {
        if (seat < 1 || seat > capacity || seats.containsKey(seat)) throw new SeatUnavailableException(seat);
        seats.put(seat, ticketId);
    }
    public void releaseSeat(int seat) { seats.remove(seat); }

    public int firstFreeSeat() {
        for (int i = 1; i <= capacity; i++) if (!seats.containsKey(i)) return i;
        return 0;
    }

    public String seatMap() {
        StringBuilder sb = new StringBuilder("Seat map (XX = taken):\n");
        for (int i = 1; i <= capacity; i++) {
            sb.append(seats.containsKey(i) ? "[XX]" : String.format("[%02d]", i));
            sb.append(i % 4 == 0 ? "\n" : (i % 2 == 0 ? "   " : " "));
        }
        if (capacity % 4 != 0) sb.append('\n');
        return sb.toString();
    }
}
