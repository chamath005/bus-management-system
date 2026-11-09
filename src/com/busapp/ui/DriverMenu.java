package com.busapp.ui;

import com.busapp.exception.BusAppException;
import com.busapp.model.*;
import java.util.List;

public class DriverMenu extends RoleMenu {
    private final Driver me;

    public DriverMenu(AppContext ctx, Driver me) { super(ctx, me); this.me = me; }

    @Override protected String title() { return "DRIVER MENU - " + me.getName(); }

    @Override protected List<Item> items() {
        return List.of(
            item("My bus and trip status", this::status),
            item("Passengers on my bus", this::passengers),
            item("Update current speed", this::speed),
            item("Arrive at next stop", this::next),
            item("Report breakdown / request replacement bus", this::breakdown),
            item("My commission", () -> io.say(ctx.commission.summary(me))),
            item("Notifications", this::showNotifications));
    }

    private void status() throws BusAppException {
        Bus bus = ctx.fleet.busOf(me);
        io.say(ctx.fleet.describe(bus));
        io.say(ctx.fleet.track(bus, null));
        io.say("Passengers inside: " + ctx.fleet.passengersInside(bus).size()
                + " | waiting at later stops: " + ctx.fleet.upcomingPassengers(bus).size());
    }

    private void passengers() throws BusAppException { printPassengers(ctx.fleet.busOf(me)); }

    private void speed() throws BusAppException {
        Bus bus = ctx.fleet.busOf(me);
        boolean over = ctx.fleet.setSpeed(bus, io.decimal("Current speed (km/h): ", 0, 140));
        io.say(over ? "WARNING: you are over the speed limit of " + (int) com.busapp.service.FleetService.SPEED_LIMIT
                + " km/h. The owner has been alerted." : "Speed updated.");
    }

    private void next() throws BusAppException { io.say(ctx.fleet.advance(ctx.fleet.busOf(me))); }

    private void breakdown() throws BusAppException {
        Bus bus = ctx.fleet.busOf(me);
        if (!io.confirm("Report bus " + bus.getPlateNo() + " as broken down")) return;
        ReplacementRequest req = ctx.fleet.reportBreakdown(bus, me.getId(), io.text("What is wrong? "));
        io.say("Request " + req.getId() + " sent to the owner. Passengers have been notified. Wait for a replacement bus.");
    }
}
