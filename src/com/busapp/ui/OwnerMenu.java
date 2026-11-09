package com.busapp.ui;

import com.busapp.exception.*;
import com.busapp.model.*;
import com.busapp.util.Fmt;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OwnerMenu extends RoleMenu {
    private final Owner me;

    public OwnerMenu(AppContext ctx, Owner me) { super(ctx, me); this.me = me; }

    @Override protected String title() { return "OWNER MENU - " + me.getName(); }

    @Override protected List<Item> items() {
        return List.of(
            item("Fleet overview (passengers, speed, status, tickets)", this::fleetOverview),
            item("Track one bus in detail", this::trackBus),
            item("Add route", this::addRoute),
            item("Add bus", this::addBus),
            item("Assign route to bus", this::assignRoute),
            item("Assign driver and conductor to bus", this::assignCrew),
            item("Passengers per route / busiest route", this::routeStats),
            item("Revenue report (and save to file)", this::revenue),
            item("Staff and commissions", this::staff),
            item("Breakdown / replacement requests", this::requests),
            item("Mark a bus as repaired", this::repaired),
            item("Speed alerts", this::speedAlerts),
            item("Lost-item reports", this::lostItems),
            item("Notifications", this::showNotifications));
    }

    private List<Bus> myBuses() throws BusAppException {
        List<Bus> list = ctx.fleet.busesOwnedBy(me.getId());
        if (list.isEmpty()) throw new ValidationException("You have no buses yet. Use 'Add bus'.");
        return list;
    }

    private void fleetOverview() throws BusAppException {
        for (Bus b : myBuses()) {
            int inside = ctx.fleet.passengersInside(b).size();
            io.say("\n" + ctx.fleet.describe(b));
            io.say("   " + ctx.fleet.crewNames(b));
            io.say("   Passengers inside: " + inside + " | waiting: " + ctx.fleet.upcomingPassengers(b).size()
                    + " | Trips done: " + b.getTripCount() + " | Top speed: " + (int) b.getMaxSpeedKmh() + " km/h");
            int head = b.getReportedHeadcount();
            if (head < 0) io.say("   Conductor headcount: not reported");
            else if (head > inside) io.say("   !! WARNING: conductor counted " + head + " people but only " + inside + " tickets -> "
                    + (head - inside) + " passenger(s) may be travelling without a ticket");
            else io.say("   Conductor headcount: " + head + " (all ticketed)");
        }
    }

    private void trackBus() throws BusAppException {
        Bus bus = pickBus(myBuses());
        io.say("\n" + ctx.fleet.crewNames(bus));
        if (bus.getRouteId() != null) io.say(ctx.fleet.track(bus, null));
        printPassengers(bus);
    }

    private void addRoute() throws BusAppException {
        String name = io.text("Route name (e.g. Colombo - Galle): ");
        String raw = io.text("Stops in order, separated by commas: ");
        List<String> stops = new ArrayList<>();
        for (String s : Arrays.asList(raw.split(","))) if (!s.isBlank()) stops.add(s.trim());
        double fare = io.decimal("Fare per stop (Rs.): ", 1, 5000);
        int mins = io.integer("Average minutes between stops: ", 1, 120);
        Route r = ctx.fleet.addRoute(name, stops, fare, mins);
        io.say("Route created: " + r.getId() + " with " + r.stopCount() + " stops.");
    }

    private void addBus() throws BusAppException {
        String plate = io.text("Number plate: ");
        int cap = io.integer("Seat capacity (10-80): ", 10, 80);
        Bus b = ctx.fleet.addBus(plate, cap, me.getId());
        io.say("Bus added: " + b.getId() + " (" + b.getPlateNo() + ")");
    }

    private void assignRoute() throws BusAppException {
        Bus bus = pickBus(myBuses());
        Route r = pickRoute();
        ctx.fleet.assignRoute(bus.getId(), r.getId());
        io.say("Bus " + bus.getPlateNo() + " now runs on " + r.getName() + ".");
    }

    private void assignCrew() throws BusAppException {
        Bus bus = pickBus(myBuses());
        List<User> drivers = ctx.store.users.filter(u -> u instanceof Driver d && (d.getAssignedBusId() == null || d.getAssignedBusId().equals(bus.getId())));
        List<User> conductors = ctx.store.users.filter(u -> u instanceof Conductor c && (c.getAssignedBusId() == null || c.getAssignedBusId().equals(bus.getId())));
        if (drivers.isEmpty() || conductors.isEmpty()) throw new ValidationException("No free driver or conductor. They must register first.");
        io.say("Available drivers:");
        drivers.forEach(d -> io.say("  " + d));
        String did = io.text("Driver ID: ");
        io.say("Available conductors:");
        conductors.forEach(c -> io.say("  " + c));
        String cid = io.text("Conductor ID: ");
        ctx.fleet.assignCrew(bus.getId(), did, cid);
        io.say("Crew assigned to " + bus.getPlateNo() + ".");
    }

    private void routeStats() {
        Map<String, Integer> stats = ctx.reports.passengersPerRoute(me.getId());
        if (stats.isEmpty()) { io.say("No tickets sold yet."); return; }
        stats.forEach((route, n) -> io.say(String.format("  %-28s %d passenger(s)", route, n)));
        ctx.reports.busiestRoute(me.getId()).ifPresent(e -> io.say("\nBUSIEST ROUTE: " + e.getKey() + " with " + e.getValue() + " passengers"));
    }

    private void revenue() throws BusAppException {
        String report = ctx.reports.revenueReport(me.getId());
        io.say(report);
        if (!io.confirm("Save this report to a file")) return;
        Path file = Paths.get("data", "revenue_report.txt");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, report);
            io.say("Saved to " + file.toAbsolutePath());
        } catch (IOException e) {
            throw new BusAppException("Could not save report: " + e.getMessage(), e);
        }
    }

    private void staff() {
        List<User> crew = ctx.store.users.filter(u -> u instanceof CrewMember);
        if (crew.isEmpty()) { io.say("No drivers or conductors registered yet."); return; }
        for (User u : crew) {
            CrewMember m = (CrewMember) u;
            io.say("\n" + m + "   Bus: " + (m.getAssignedBusId() == null ? "unassigned" : m.getAssignedBusId()));
            io.say(ctx.commission.summary(m));
        }
    }

    private void requests() throws BusAppException {
        List<ReplacementRequest> pending = ctx.fleet.pendingRequests(me.getId());
        if (pending.isEmpty()) { io.say("No pending replacement requests."); return; }
        for (ReplacementRequest r : pending) {
            Bus broken = ctx.store.buses.get(r.getBrokenBusId());
            io.say(r.getId() + " | bus " + broken.getPlateNo() + " | reason: " + r.getReason()
                    + " | passengers affected: " + ctx.fleet.ticketsOnBus(broken.getId()).size());
        }
        String id = io.optional("Request ID to handle (blank to skip): ");
        if (id.isEmpty()) return;
        if (io.text("Approve (a) or reject (r)? ").toLowerCase().startsWith("a")) {
            List<Bus> spares = ctx.fleet.spareBuses(me.getId());
            if (spares.isEmpty()) throw new ValidationException("You have no spare bus (active, no crew, no passengers). Add one first.");
            Bus spare = pickBus(spares);
            ctx.fleet.approveReplacement(id, spare.getId());
            io.say("Approved. Crew and passengers moved to " + spare.getPlateNo() + ". The broken bus is now in MAINTENANCE.");
        } else {
            ctx.fleet.rejectRequest(id);
            io.say("Request rejected.");
        }
    }

    private void repaired() throws BusAppException {
        List<Bus> list = new ArrayList<>();
        for (Bus b : myBuses()) if (b.getStatus() != BusStatus.ACTIVE) list.add(b);
        Bus bus = pickBus(list);
        ctx.fleet.markRepaired(bus.getId());
        io.say(bus.getPlateNo() + " is ACTIVE again.");
    }

    private void speedAlerts() throws BusAppException {
        boolean any = false;
        for (Bus b : myBuses())
            for (String a : b.getAlerts()) { io.say("  " + a); any = true; }
        if (!any) io.say("No speeding alerts. Limit is " + (int) com.busapp.service.FleetService.SPEED_LIMIT + " km/h.");
    }

    private void lostItems() {
        List<LostItem> list = ctx.lost.forOwner(me.getId());
        if (list.isEmpty()) { io.say("No lost-item reports."); return; }
        for (LostItem l : list) {
            String who = ctx.store.users.find(l.getPassengerId()).map(User::getName).orElse("?");
            String plate = ctx.store.buses.find(l.getBusId()).map(Bus::getPlateNo).orElse("?");
            io.say(l.getId() + " | " + plate + " | " + who + " | " + l.getDescription() + " | " + l.getStatus()
                    + (l.getNote().isEmpty() ? "" : " | " + l.getNote()));
        }
        String id = io.optional("Report ID to close (blank to skip): ");
        if (!id.isEmpty()) {
            try { ctx.lost.close(id); io.say("Closed."); } catch (BusAppException e) { io.say("Error: " + e.getMessage()); }
        }
    }
}
