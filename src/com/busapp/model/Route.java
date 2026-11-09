package com.busapp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Route implements Identifiable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String name;
    private final List<String> stops;
    private final double farePerStop;
    private final int minutesPerStop;

    public Route(String id, String name, List<String> stops, double farePerStop, int minutesPerStop) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Route name is required.");
        if (stops == null || stops.size() < 2) throw new IllegalArgumentException("A route needs at least 2 stops.");
        if (farePerStop <= 0) throw new IllegalArgumentException("Fare per stop must be greater than 0.");
        if (minutesPerStop <= 0) throw new IllegalArgumentException("Minutes per stop must be greater than 0.");
        this.id = id;
        this.name = name.trim();
        this.stops = new ArrayList<>(stops);
        this.farePerStop = farePerStop;
        this.minutesPerStop = minutesPerStop;
    }
    @Override public String getId() { return id; }
    public String getName() { return name; }
    public List<String> getStops() { return Collections.unmodifiableList(stops); }
    public int stopCount() { return stops.size(); }
    public String stopName(int i) { return stops.get(i); }
    public double getFarePerStop() { return farePerStop; }
    public int getMinutesPerStop() { return minutesPerStop; }
    public double fareBetween(int from, int to) { return Math.abs(to - from) * farePerStop; }
}
