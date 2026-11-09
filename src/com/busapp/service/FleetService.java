package com.busapp.service;

import com.busapp.exception.*;
import com.busapp.model.*;
import com.busapp.storage.DataStore;
import com.busapp.util.Fmt;
import java.util.ArrayList;
import java.util.List;

/** Buses, routes, live tracking, movement, breakdowns and replacements. */
public class FleetService {
    public static final double SPEED_LIMIT = 60.0;
    public static final int STANDING_LIMIT = 15;
    private final DataStore store;

    public FleetService(DataStore store) { this.store = store; }

    // ---------------------------------------------------------------- setup
    public Route addRoute(String name, List<String> stops, double farePerStop, int minutesPerStop) throws ValidationException {
        try {
            Route r = new Route(store.routes.nextId(), name, stops, farePerStop, minutesPerStop);
            store.routes.add(r);
            return r;
        } catch (IllegalArgumentException e) { throw new ValidationException(e.getMessage()); }
    }

    public Bus addBus(String plate, int capacity, String ownerId) throws ValidationException {
        for (Bus b : store.buses.all())
            if (b.getPlateNo().equalsIgnoreCase(plate.trim())) throw new ValidationException("A bus with that plate already exists.");
        try {
            Bus b = new Bus(store.buses.nextId(), plate, capacity, ownerId);
            store.buses.add(b);
            return b;
        } catch (IllegalArgumentException e) { throw new ValidationException(e.getMessage()); }
    }

    public void assignRoute(String busId, String routeId) throws BusAppException {
        Bus bus = store.buses.get(busId);
        Route route = store.routes.get(routeId);
        if (!ticketsOnBus(busId).isEmpty()) throw new ValidationException("Cannot change route while the bus has active tickets.");
        bus.setRouteId(route.getId());
        bus.setStopIndex(0);
    }

    public void assignCrew(String busId, String driverId, String conductorId) throws BusAppException {
        Bus bus = store.buses.get(busId);
        User du = store.users.get(driverId);
        User cu = store.users.get(conductorId);
        if (!(du instanceof Driver driver)) throw new ValidationException(driverId + " is not a driver.");
        if (!(cu instanceof Conductor conductor)) throw new ValidationException(conductorId + " is not a conductor.");
        for (CrewMember m : new CrewMember[]{driver, conductor})
            if (m.getAssignedBusId() != null && !m.getAssignedBusId().equals(busId))
                throw new ValidationException(m.getName() + " is already assigned to bus " + m.getAssignedBusId() + ".");
        releaseCrew(bus);
        bus.setDriverId(driver.getId());
        bus.setConductorId(conductor.getId());
        driver.setAssignedBusId(busId);
        conductor.setAssignedBusId(busId);
    }

    private void releaseCrew(Bus bus) {
        for (String id : new String[]{bus.getDriverId(), bus.getConductorId()})
            store.users.find(id).ifPresent(u -> { if (u instanceof CrewMember m) m.setAssignedBusId(null); });
        bus.setDriverId(null);
        bus.setConductorId(null);
    }

    // ---------------------------------------------------------------- queries
    public Bus busOf(CrewMember m) throws BusAppException {
        if (m.getAssignedBusId() == null) throw new ValidationException("You are not assigned to a bus yet. Please ask the owner.");
        return store.buses.get(m.getAssignedBusId());
    }

    public Route routeOf(Bus bus) throws BusAppException {
        if (bus.getRouteId() == null) throw new ValidationException("Bus " + bus.getPlateNo() + " has no route assigned.");
        return store.routes.get(bus.getRouteId());
    }

    public List<Bus> busesOwnedBy(String ownerId) { return store.buses.filter(b -> b.getOwnerId().equals(ownerId)); }

    public List<Bus> activeBusesOn(String routeId) {
        return store.buses.filter(b -> b.getStatus() == BusStatus.ACTIVE && routeId.equals(b.getRouteId()));
    }

    /** Spare buses: active, no crew and no passengers - candidates for replacement. */
    public List<Bus> spareBuses(String ownerId) {
        return store.buses.filter(b -> b.getOwnerId().equals(ownerId) && b.getStatus() == BusStatus.ACTIVE
                && b.getDriverId() == null && b.getConductorId() == null && ticketsOnBus(b.getId()).isEmpty());
    }

    public List<Ticket> ticketsOnBus(String busId) {
        return store.tickets.filter(t -> t.isActive() && t.getBusId().equals(busId));
    }
    /** Passengers physically on the bus right now (already boarded). */
    public List<Ticket> passengersInside(Bus bus) {
        return store.tickets.filter(t -> t.isActive() && t.getBusId().equals(bus.getId()) && t.getBoardIndex() <= bus.getStopIndex());
    }
    /** Booked passengers still waiting at later stops. */
    public List<Ticket> upcomingPassengers(Bus bus) {
        return store.tickets.filter(t -> t.isActive() && t.getBusId().equals(bus.getId()) && t.getBoardIndex() > bus.getStopIndex());
    }

    public String describe(Bus b) {
        String route = "(no route)";
        String stop = "-";
        if (b.getRouteId() != null) {
            Route r = store.routes.find(b.getRouteId()).orElse(null);
            if (r != null) { route = r.getName(); stop = r.stopName(Math.min(b.getStopIndex(), r.stopCount() - 1)); }
        }
        return String.format("%s [%s] %s | Route: %s | At: %s | %.0f km/h | Free seats: %d/%d",
                b.getId(), b.getPlateNo(), b.getStatus(), route, stop, b.getSpeedKmh(), b.freeSeats(), b.getCapacity());
    }

    public String crewNames(Bus b) {
        String d = store.users.find(b.getDriverId()).map(User::getName).orElse("none");
        String c = store.users.find(b.getConductorId()).map(User::getName).orElse("none");
        return "Driver: " + d + " | Conductor: " + c;
    }

    /** Live tracking text. When a ticket is given, adds ETA information for that passenger. */
    public String track(Bus bus, Ticket ticket) throws BusAppException {
        StringBuilder sb = new StringBuilder();
        sb.append("Bus ").append(bus.getPlateNo()).append(" - status: ").append(bus.getStatus()).append('\n');
        if (bus.getRouteId() == null) return sb.append("Not on a route right now.").toString();
        Route r = routeOf(bus);
        sb.append("Route: ").append(r.getName()).append('\n');
        sb.append("Current stop: ").append(r.stopName(bus.getStopIndex()))
          .append(" (stop ").append(bus.getStopIndex() + 1).append(" of ").append(r.stopCount()).append(")\n");
        sb.append(String.format("Speed: %.0f km/h%n", bus.getSpeedKmh()));
        if (bus.getStatus() != BusStatus.ACTIVE) sb.append("NOTE: this bus is not running. Check your notifications for a replacement bus.\n");
        if (ticket != null && ticket.isActive()) {
            if (ticket.getBoardIndex() > bus.getStopIndex()) {
                int away = ticket.getBoardIndex() - bus.getStopIndex();
                sb.append("Your boarding stop (").append(ticket.getBoardStop()).append(") is ").append(away)
                  .append(" stop(s) away - about ").append(away * r.getMinutesPerStop()).append(" min.\n");
            } else {
                int left = ticket.getDestIndex() - bus.getStopIndex();
                sb.append("You are on board. ").append(ticket.getDestStop()).append(" is ").append(left)
                  .append(" stop(s) away - about ").append(left * r.getMinutesPerStop()).append(" min.\n");
            }
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------- driving
    /** Returns true when the speed is above the limit (an alert is recorded for the owner). */
    public boolean setSpeed(Bus bus, double speed) throws BusAppException {
        if (bus.getStatus() != BusStatus.ACTIVE) throw new ValidationException("Bus is not active (" + bus.getStatus() + ").");
        try { bus.setSpeed(speed); } catch (IllegalArgumentException e) { throw new ValidationException(e.getMessage()); }
        if (speed > SPEED_LIMIT) {
            String msg = String.format("OVERSPEED: %s travelled at %.0f km/h (limit %.0f).", bus.getPlateNo(), speed, SPEED_LIMIT);
            bus.addAlert(Fmt.now() + " - " + msg);
            store.notifyUser(bus.getOwnerId(), msg);
            return true;
        }
        return false;
    }

    /** Moves the bus to the next stop; passengers whose destination is reached get off. */
    public String advance(Bus bus) throws BusAppException {
        if (bus.getStatus() != BusStatus.ACTIVE) throw new ValidationException("Bus is not active (" + bus.getStatus() + ").");
        Route r = routeOf(bus);
        int next = bus.getStopIndex() + 1;
        if (next >= r.stopCount()) {
            int off = completeTickets(bus, Integer.MAX_VALUE);
            bus.setStopIndex(0);
            bus.incrementTrip();
            bus.setSpeed(0);
            bus.setReportedHeadcount(-1);
            return "End of route. " + off + " passenger(s) got off. Bus is back at " + r.stopName(0) + " ready for the next trip.";
        }
        bus.setStopIndex(next);
        int off = completeTickets(bus, next);
        return "Arrived at " + r.stopName(next) + ". " + off + " passenger(s) got off; "
                + passengersInside(bus).size() + " on board, " + upcomingPassengers(bus).size() + " waiting at later stops.";
    }

    private int completeTickets(Bus bus, int uptoStop) {
        int n = 0;
        for (Ticket t : ticketsOnBus(bus.getId())) {
            if (t.getBoardIndex() <= bus.getStopIndex() && t.getDestIndex() <= uptoStop) {
                t.setStatus(TicketStatus.COMPLETED);
                bus.releaseSeat(t.getSeatNo());
                n++;
            }
        }
        return n;
    }

    public void recordHeadcount(Bus bus, int count) throws ValidationException {
        try { bus.setReportedHeadcount(count); } catch (IllegalArgumentException e) { throw new ValidationException(e.getMessage()); }
    }

    // ---------------------------------------------------------------- breakdown & replacement
    public ReplacementRequest reportBreakdown(Bus bus, String reporterId, String reason) throws BusAppException {
        if (bus.getStatus() != BusStatus.ACTIVE) throw new ValidationException("This bus is already marked " + bus.getStatus() + ".");
        ReplacementRequest req;
        try { req = new ReplacementRequest(store.requests.nextId(), bus.getId(), reporterId, reason); }
        catch (IllegalArgumentException e) { throw new ValidationException(e.getMessage()); }
        store.requests.add(req);
        bus.setStatus(BusStatus.BROKEN);
        bus.setSpeed(0);
        store.notifyUser(bus.getOwnerId(), "BREAKDOWN: bus " + bus.getPlateNo() + " - " + reason + ". Replacement request " + req.getId() + " is waiting.");
        for (Ticket t : ticketsOnBus(bus.getId()))
            if (t.getPassengerId() != null)
                store.notifyUser(t.getPassengerId(), "Your bus " + bus.getPlateNo() + " has broken down. A replacement is being arranged.");
        return req;
    }

    public List<ReplacementRequest> pendingRequests(String ownerId) {
        List<ReplacementRequest> out = new ArrayList<>();
        for (ReplacementRequest r : store.requests.all()) {
            if (r.getStatus() != ReplacementRequest.Status.PENDING) continue;
            store.buses.find(r.getBrokenBusId()).ifPresent(b -> { if (b.getOwnerId().equals(ownerId)) out.add(r); });
        }
        return out;
    }

    /** Moves crew, route position and all active tickets from the broken bus to a spare bus. */
    public void approveReplacement(String requestId, String spareBusId) throws BusAppException {
        ReplacementRequest req = store.requests.get(requestId);
        if (req.getStatus() != ReplacementRequest.Status.PENDING) throw new ValidationException("Request is already " + req.getStatus() + ".");
        Bus broken = store.buses.get(req.getBrokenBusId());
        Bus spare = store.buses.get(spareBusId);
        if (spare.getId().equals(broken.getId())) throw new ValidationException("Choose a different bus.");
        if (spare.getStatus() != BusStatus.ACTIVE) throw new ValidationException("Replacement bus must be ACTIVE.");
        if (spare.getDriverId() != null || spare.getConductorId() != null || !ticketsOnBus(spare.getId()).isEmpty())
            throw new ValidationException("Replacement must be a spare bus with no crew and no passengers.");
        List<Ticket> moving = ticketsOnBus(broken.getId());
        if (moving.size() > spare.getCapacity() + STANDING_LIMIT)
            throw new ValidationException("Replacement bus is too small for " + moving.size() + " passengers.");

        spare.setRouteId(broken.getRouteId());
        spare.setStopIndex(broken.getStopIndex());
        spare.setReportedHeadcount(broken.getReportedHeadcount());
        for (Ticket t : moving) {
            int old = t.getSeatNo();
            broken.releaseSeat(old);
            int seat = (old > 0 && old <= spare.getCapacity() && !spare.isSeatTaken(old)) ? old : spare.firstFreeSeat();
            if (seat > 0) spare.assignSeat(seat, t.getId());
            t.setSeatNo(seat);
            t.setBusId(spare.getId());
            if (t.getPassengerId() != null)
                store.notifyUser(t.getPassengerId(), "Replacement bus " + spare.getPlateNo() + " will continue your journey (" + t.seatText() + ").");
        }
        String driverId = broken.getDriverId(), conductorId = broken.getConductorId();
        releaseCrew(broken);
        if (driverId != null && conductorId != null) assignCrew(spare.getId(), driverId, conductorId);
        broken.setStatus(BusStatus.MAINTENANCE);
        broken.setSpeed(0);
        broken.setReportedHeadcount(-1);
        req.approve(spare.getId());
        store.notifyUser(req.getReportedBy(), "Replacement approved: continue the trip with bus " + spare.getPlateNo() + ".");
    }

    public void rejectRequest(String requestId) throws BusAppException {
        ReplacementRequest req = store.requests.get(requestId);
        if (req.getStatus() != ReplacementRequest.Status.PENDING) throw new ValidationException("Request is already " + req.getStatus() + ".");
        req.reject();
        store.notifyUser(req.getReportedBy(), "Replacement request " + req.getId() + " was rejected. Please contact the owner.");
    }

    public void markRepaired(String busId) throws BusAppException {
        Bus bus = store.buses.get(busId);
        if (bus.getStatus() == BusStatus.ACTIVE) throw new ValidationException("Bus is already active.");
        bus.setStatus(BusStatus.ACTIVE);
    }
}
