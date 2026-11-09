package com.busapp.ui;

import com.busapp.exception.*;
import com.busapp.model.*;
import com.busapp.payment.*;
import com.busapp.util.Fmt;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PassengerMenu extends RoleMenu {
    private final Passenger me;

    public PassengerMenu(AppContext ctx, Passenger me) { super(ctx, me); this.me = me; }

    @Override protected String title() { return "PASSENGER MENU - " + me.getName(); }

    @Override protected List<Item> items() {
        return List.of(
            item("Browse routes and buses", this::browse),
            item("Book a seat (pay without cash)", this::book),
            item("My tickets", this::myTickets),
            item("Cancel a ticket (refund to wallet)", this::cancel),
            item("Track my bus", this::track),
            item("Wallet: balance / top-up", this::wallet),
            item("Save ticket receipt to a file", this::receipt),
            item("Report a lost item", this::reportLost),
            item("My lost-item reports", this::myLost),
            item("Notifications", this::showNotifications));
    }

    private void browse() {
        for (Route r : ctx.store.routes.all()) {
            io.say("\n" + r.getName() + "  (" + Fmt.money(r.getFarePerStop()) + " per stop)");
            io.say("  Stops: " + String.join(" -> ", r.getStops()));
            List<Bus> buses = ctx.fleet.activeBusesOn(r.getId());
            if (buses.isEmpty()) io.say("  No buses running right now.");
            buses.forEach(b -> io.say("  " + ctx.fleet.describe(b)));
        }
    }

    private void book() throws BusAppException {
        Route r = pickRoute();
        Bus bus = pickBus(ctx.fleet.activeBusesOn(r.getId()));
        io.say("The bus is now at: " + r.stopName(bus.getStopIndex()) + "\nBoarding stop:");
        int from = pickStop(r, bus.getStopIndex(), "Boarding stop no: ");
        if (from >= r.stopCount() - 1) throw new ValidationException("That is the last stop. Choose an earlier boarding stop.");
        io.say("Destination:");
        int to = pickStop(r, from + 1, "Destination stop no: ");
        io.say(bus.seatMap());
        int seat = io.integer("Seat number: ", 1, bus.getCapacity());
        double fare = r.fareBetween(from, to);
        io.say("Fare: " + Fmt.money(fare));
        io.say("Pay with:  1. Wallet (balance " + Fmt.money(me.getWallet()) + ")   2. Card");
        PaymentMethod pm = io.integer("Choice: ", 1, 2) == 1 ? new WalletPayment(me) : new CardPayment(io.text("Card number (16 digits): "));
        if (!io.confirm("Confirm booking")) { io.say("Cancelled."); return; }
        Ticket t = ctx.booking.bookSeat(me, bus, from, to, seat, pm);
        io.say("\nBooking successful!\n" + ctx.booking.receipt(t));
    }

    private void myTickets() {
        List<Ticket> list = ctx.booking.ticketsOf(me);
        if (list.isEmpty()) io.say("You have no tickets yet.");
        list.forEach(t -> io.say(t.toString()));
    }

    private void cancel() throws BusAppException {
        double refund = ctx.booking.cancel(me, io.text("Ticket ID to cancel: "));
        io.say("Ticket cancelled. " + Fmt.money(refund) + " refunded to your wallet.");
    }

    private Ticket pickOwnTicket(boolean includeCompleted) throws BusAppException {
        List<Ticket> list = new ArrayList<>();
        for (Ticket t : ctx.booking.ticketsOf(me))
            if (t.isActive() || (includeCompleted && t.getStatus() == TicketStatus.COMPLETED)) list.add(t);
        if (list.isEmpty()) throw new ValidationException("You have no suitable tickets.");
        list.forEach(t -> io.say("  " + t));
        Ticket t = ctx.store.tickets.get(io.text("Ticket ID: "));
        if (!list.contains(t)) throw new ValidationException("Choose one of the tickets listed above.");
        return t;
    }

    private void track() throws BusAppException {
        Ticket t = pickOwnTicket(true);
        Bus bus = ctx.store.buses.get(t.getBusId());
        io.say("\n" + ctx.fleet.track(bus, t));
    }

    private void wallet() throws BusAppException {
        io.say("Wallet balance: " + Fmt.money(me.getWallet()));
        if (!io.confirm("Top up now")) return;
        double amount = io.decimal("Amount (Rs.): ", 1, 100000);
        new CardPayment(io.text("Card number (16 digits): ")).pay(amount);
        me.topUp(amount);
        io.say("Top-up successful. New balance: " + Fmt.money(me.getWallet()));
    }

    private void receipt() throws BusAppException {
        Ticket t = pickOwnTicket(true);
        Path file = Paths.get("data", "receipts", t.getId() + ".txt");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, ctx.booking.receipt(t));
            io.say("Saved to " + file.toAbsolutePath());
        } catch (IOException e) {
            throw new BusAppException("Could not save receipt: " + e.getMessage(), e);
        }
    }

    private void reportLost() throws BusAppException {
        Ticket t = pickOwnTicket(true);
        LostItem item = ctx.lost.report(me, t.getBusId(), io.text("Describe the lost item: "));
        io.say("Report " + item.getId() + " sent to the conductor and owner. You can track the bus from 'Track my bus'.");
    }

    private void myLost() {
        List<LostItem> list = ctx.lost.forPassenger(me);
        if (list.isEmpty()) io.say("No lost-item reports.");
        for (LostItem l : list)
            io.say(l.getId() + " | " + l.getDescription() + " | " + l.getStatus() + (l.getNote().isEmpty() ? "" : " | " + l.getNote()));
    }
}
