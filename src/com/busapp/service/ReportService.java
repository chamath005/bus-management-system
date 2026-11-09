package com.busapp.service;

import com.busapp.model.*;
import com.busapp.storage.DataStore;
import com.busapp.util.Fmt;
import java.util.*;

public class ReportService {
    private final DataStore store;
    public ReportService(DataStore store) { this.store = store; }

    /** Non-cancelled tickets on buses owned by this owner. */
    public List<Ticket> ownedTickets(String ownerId) {
        return store.tickets.filter(t -> t.getStatus() != TicketStatus.CANCELLED
                && store.buses.find(t.getBusId()).map(b -> b.getOwnerId().equals(ownerId)).orElse(false));
    }

    public Map<String, Integer> passengersPerRoute(String ownerId) {
        Map<String, Integer> map = new TreeMap<>();
        for (Ticket t : ownedTickets(ownerId))
            map.merge(store.routes.find(t.getRouteId()).map(Route::getName).orElse("?"), 1, Integer::sum);
        return map;
    }

    public Optional<Map.Entry<String, Integer>> busiestRoute(String ownerId) {
        return passengersPerRoute(ownerId).entrySet().stream().max(Map.Entry.comparingByValue());
    }

    public Map<String, Double> revenuePerBus(String ownerId) {
        Map<String, Double> map = new TreeMap<>();
        for (Ticket t : ownedTickets(ownerId))
            map.merge(store.buses.find(t.getBusId()).map(Bus::getPlateNo).orElse("?"), t.getFare(), Double::sum);
        return map;
    }

    public String revenueReport(String ownerId) {
        StringBuilder sb = new StringBuilder("========== REVENUE REPORT (" + Fmt.now() + ") ==========\n");
        double total = 0;
        for (Map.Entry<String, Double> e : revenuePerBus(ownerId).entrySet()) {
            sb.append(String.format("  %-14s %s%n", e.getKey(), Fmt.money(e.getValue())));
            total += e.getValue();
        }
        sb.append("Total ticket revenue: ").append(Fmt.money(total)).append('\n');
        sb.append("Tickets sold: ").append(ownedTickets(ownerId).size()).append('\n');
        sb.append("Passengers per route:\n");
        for (Map.Entry<String, Integer> e : passengersPerRoute(ownerId).entrySet())
            sb.append(String.format("  %-26s %d%n", e.getKey(), e.getValue()));
        busiestRoute(ownerId).ifPresent(e -> sb.append("Busiest route: ").append(e.getKey()).append(" (").append(e.getValue()).append(" passengers)\n"));
        return sb.toString();
    }
}
