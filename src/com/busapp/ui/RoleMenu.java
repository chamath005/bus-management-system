package com.busapp.ui;

import com.busapp.exception.BusAppException;
import com.busapp.exception.ValidationException;
import com.busapp.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract menu. Each role has its own subclass that lists ONLY the features of that role,
 * so a passenger can never reach driver, conductor or owner functions.
 */
public abstract class RoleMenu {
    public interface Action { void run() throws BusAppException; }

    protected static final class Item {
        final String label;
        final Action action;
        Item(String label, Action action) { this.label = label; this.action = action; }
    }

    protected final AppContext ctx;
    protected final Console io;
    protected final User user;

    protected RoleMenu(AppContext ctx, User user) {
        this.ctx = ctx;
        this.io = ctx.io;
        this.user = user;
    }

    protected abstract String title();
    protected abstract List<Item> items();

    protected static Item item(String label, Action action) { return new Item(label, action); }

    public final void run() {
        List<Item> items = new ArrayList<>(items());
        items.add(new Item("Logout", null));
        int unread = user.getNotifications().size();
        if (unread > 0) io.say("\n*** You have " + unread + " notification(s). Choose 'Notifications' to read them. ***");
        while (true) {
            io.say("\n==== " + title() + " ====");
            for (int i = 0; i < items.size(); i++) io.say(String.format("%2d. %s", i + 1, items.get(i).label));
            int choice = io.integer("Choose: ", 1, items.size());
            if (choice == items.size()) { io.say("Logged out."); return; }
            try {
                items.get(choice - 1).action.run();
                ctx.save();
            } catch (BusAppException e) {
                io.say("Error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                io.say("Invalid input: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------ shared helpers
    protected void showNotifications() {
        List<String> list = user.getNotifications();
        if (list.isEmpty()) { io.say("No notifications."); return; }
        list.forEach(n -> io.say(" * " + n));
        if (io.confirm("Clear them")) user.clearNotifications();
    }

    protected Route pickRoute() throws BusAppException {
        List<Route> routes = ctx.store.routes.all();
        if (routes.isEmpty()) throw new ValidationException("No routes have been created yet.");
        for (int i = 0; i < routes.size(); i++)
            io.say((i + 1) + ". " + routes.get(i).getName() + "  (" + routes.get(i).stopCount() + " stops, "
                    + com.busapp.util.Fmt.money(routes.get(i).getFarePerStop()) + " per stop)");
        return routes.get(io.integer("Choose route: ", 1, routes.size()) - 1);
    }

    protected int pickStop(Route r, int fromIndex, String prompt) {
        for (int i = fromIndex; i < r.stopCount(); i++) io.say("  " + (i + 1) + ". " + r.stopName(i));
        return io.integer(prompt, fromIndex + 1, r.stopCount()) - 1;
    }

    protected Bus pickBus(List<Bus> buses) throws BusAppException {
        if (buses.isEmpty()) throw new ValidationException("No buses available for this choice.");
        for (int i = 0; i < buses.size(); i++) io.say((i + 1) + ". " + ctx.fleet.describe(buses.get(i)));
        return buses.get(io.integer("Choose bus: ", 1, buses.size()) - 1);
    }

    protected void printPassengers(Bus bus) {
        List<Ticket> inside = ctx.fleet.passengersInside(bus);
        List<Ticket> upcoming = ctx.fleet.upcomingPassengers(bus);
        io.say("Passengers INSIDE the bus: " + inside.size());
        inside.forEach(t -> io.say("  " + t));
        io.say("Booked, waiting at later stops: " + upcoming.size());
        upcoming.forEach(t -> io.say("  " + t));
    }
}
