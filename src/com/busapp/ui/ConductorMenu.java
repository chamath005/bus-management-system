package com.busapp.ui;

import com.busapp.exception.BusAppException;
import com.busapp.model.*;
import com.busapp.util.Fmt;
import java.util.List;

public class ConductorMenu extends RoleMenu {
    private final Conductor me;

    public ConductorMenu(AppContext ctx, Conductor me) { super(ctx, me); this.me = me; }

    @Override protected String title() { return "CONDUCTOR MENU - " + me.getName(); }

    @Override protected List<Item> items() {
        return List.of(
            item("My bus status", this::status),
            item("Passengers on my bus", this::passengers),
            item("Seat map", () -> io.say(ctx.fleet.busOf(me).seatMap())),
            item("Issue ticket to a walk-in passenger (cash)", this::issue),
            item("Verify a ticket", this::verify),
            item("Record physical headcount", this::headcount),
            item("Lost-item reports for my bus", this::lostItems),
            item("My commission", () -> io.say(ctx.commission.summary(me))),
            item("Notifications", this::showNotifications));
    }

    private void status() throws BusAppException {
        Bus bus = ctx.fleet.busOf(me);
        io.say(ctx.fleet.describe(bus));
        io.say(ctx.fleet.track(bus, null));
    }

    private void passengers() throws BusAppException { printPassengers(ctx.fleet.busOf(me)); }

    private void issue() throws BusAppException {
        Bus bus = ctx.fleet.busOf(me);
        Route r = ctx.fleet.routeOf(bus);
        int from = bus.getStopIndex();
        io.say("Passenger boards at: " + r.stopName(from));
        if (from >= r.stopCount() - 1) { io.say("This is the last stop - nobody can board."); return; }
        int to = pickStop(r, from + 1, "Destination stop no: ");
        String name = io.text("Passenger name: ");
        io.say(bus.seatMap());
        int seat = io.integer("Seat number (0 = standing): ", 0, bus.getCapacity());
        io.say("Fare to collect: " + Fmt.money(r.fareBetween(from, to)));
        if (!io.confirm("Cash collected")) { io.say("Ticket not issued."); return; }
        Ticket t = ctx.booking.issueTicket(me, bus, name, to, seat);
        io.say(ctx.booking.receipt(t));
    }

    private void verify() throws BusAppException {
        io.say(ctx.booking.verify(io.text("Ticket ID: "), ctx.fleet.busOf(me)));
    }

    private void headcount() throws BusAppException {
        Bus bus = ctx.fleet.busOf(me);
        int n = io.integer("Number of people physically inside the bus: ", 0, bus.getCapacity() + 15);
        ctx.fleet.recordHeadcount(bus, n);
        int tickets = ctx.fleet.passengersInside(bus).size();
        io.say("Recorded. Tickets for people inside: " + tickets
                + (n > tickets ? "  -> " + (n - tickets) + " passenger(s) have NO ticket! Please issue tickets." : "  -> all passengers are ticketed."));
    }

    private void lostItems() throws BusAppException {
        Bus bus = ctx.fleet.busOf(me);
        List<LostItem> open = ctx.lost.openForBus(bus.getId());
        if (open.isEmpty()) { io.say("No open lost-item reports for your bus."); return; }
        for (LostItem l : open) io.say(l.getId() + " | " + l.getDescription() + " | reported " + Fmt.time(l.getReportedAt()));
        String id = io.optional("Enter report ID to mark as FOUND (blank to skip): ");
        if (id.isEmpty()) return;
        ctx.lost.markFound(id, me, io.text("Note for the passenger (where to collect): "));
        io.say("Marked as found. The passenger has been notified.");
    }
}
