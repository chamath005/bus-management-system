package com.busapp.service;

import com.busapp.exception.*;
import com.busapp.model.*;
import com.busapp.payment.CashPayment;
import com.busapp.payment.PaymentMethod;
import com.busapp.storage.DataStore;
import com.busapp.util.Fmt;
import java.util.List;

public class BookingService {
    private final DataStore store;
    private final FleetService fleet;

    public BookingService(DataStore store, FleetService fleet) { this.store = store; this.fleet = fleet; }

    /** Passenger books a seat online and pays without cash. Payment happens first, so a failure changes nothing. */
    public Ticket bookSeat(Passenger p, Bus bus, int from, int to, int seat, PaymentMethod pay) throws BusAppException {
        Route r = fleet.routeOf(bus);
        validateLeg(bus, r, from, to);
        if (seat < 1 || seat > bus.getCapacity() || bus.isSeatTaken(seat)) throw new SeatUnavailableException(seat);
        double fare = r.fareBetween(from, to);
        pay.pay(fare);
        Ticket t = new Ticket(store.tickets.nextId(), bus.getId(), r.getId(), p.getId(), p.getName(), from, to,
                r.stopName(from), r.stopName(to), seat, fare, "ONLINE", bus.getDriverId(), bus.getConductorId(), pay.getName());
        bus.assignSeat(seat, t.getId());
        store.tickets.add(t);
        return t;
    }

    /** Conductor sells a ticket to a walk-in passenger boarding at the bus's current stop. seat 0 = standing. */
    public Ticket issueTicket(Conductor c, Bus bus, String passengerName, int to, int seat) throws BusAppException {
        if (passengerName == null || passengerName.isBlank()) throw new ValidationException("Passenger name is required.");
        Route r = fleet.routeOf(bus);
        int from = bus.getStopIndex();
        validateLeg(bus, r, from, to);
        if (fleet.ticketsOnBus(bus.getId()).size() >= bus.getCapacity() + FleetService.STANDING_LIMIT)
            throw new ValidationException("The bus is completely full.");
        if (seat != 0 && (seat < 1 || seat > bus.getCapacity() || bus.isSeatTaken(seat))) throw new SeatUnavailableException(seat);
        PaymentMethod cash = new CashPayment();
        double fare = r.fareBetween(from, to);
        cash.pay(fare);
        Ticket t = new Ticket(store.tickets.nextId(), bus.getId(), r.getId(), null, passengerName.trim(), from, to,
                r.stopName(from), r.stopName(to), seat, fare, c.getId(), bus.getDriverId(), bus.getConductorId(), cash.getName());
        if (seat > 0) bus.assignSeat(seat, t.getId());
        store.tickets.add(t);
        return t;
    }

    private void validateLeg(Bus bus, Route r, int from, int to) throws ValidationException {
        if (bus.getStatus() != BusStatus.ACTIVE) throw new ValidationException("This bus is not running (" + bus.getStatus() + ").");
        if (from < bus.getStopIndex()) throw new ValidationException("The bus has already passed that stop.");
        if (to <= from || to >= r.stopCount() || from < 0) throw new ValidationException("Destination must be after the boarding stop.");
    }

    /** Cancel before boarding: seat released and the fare refunded to the wallet. */
    public double cancel(Passenger p, String ticketId) throws BusAppException {
        Ticket t = store.tickets.get(ticketId);
        if (t.getPassengerId() == null || !t.getPassengerId().equals(p.getId())) throw new ValidationException("That is not your ticket.");
        if (!t.isActive()) throw new ValidationException("Ticket is already " + t.getStatus() + ".");
        Bus bus = store.buses.get(t.getBusId());
        if (t.getBoardIndex() <= bus.getStopIndex()) throw new ValidationException("You have already boarded; this ticket cannot be cancelled.");
        bus.releaseSeat(t.getSeatNo());
        t.setStatus(TicketStatus.CANCELLED);
        p.credit(t.getFare());
        return t.getFare();
    }

    public List<Ticket> ticketsOf(Passenger p) { return store.tickets.filter(t -> p.getId().equals(t.getPassengerId())); }

    public String verify(String ticketId, Bus bus) {
        return store.tickets.find(ticketId).map(t -> {
            if (!t.getBusId().equals(bus.getId())) return "INVALID: ticket is for a different bus.";
            if (!t.isActive()) return "INVALID: ticket is " + t.getStatus() + ".";
            return "VALID: " + t.getPassengerName() + ", " + t.getBoardStop() + " -> " + t.getDestStop() + ", " + t.seatText() + ".";
        }).orElse("INVALID: no such ticket.");
    }

    public String receipt(Ticket t) throws BusAppException {
        Bus bus = store.buses.get(t.getBusId());
        Route r = store.routes.get(t.getRouteId());
        String issuer = t.getIssuedBy().equals("ONLINE") ? "Online booking"
                : "Conductor " + store.users.find(t.getIssuedBy()).map(User::getName).orElse("?");
        return "=============== BUS TICKET ===============\n"
                + "Ticket : " + t.getId() + "   Status: " + t.getStatus() + "\n"
                + "Name   : " + t.getPassengerName() + "\n"
                + "Bus    : " + bus.getPlateNo() + "\n"
                + "Route  : " + r.getName() + "\n"
                + "From   : " + t.getBoardStop() + "\n"
                + "To     : " + t.getDestStop() + "\n"
                + "Seat   : " + t.seatText() + "\n"
                + "Fare   : " + Fmt.money(t.getFare()) + " (" + t.getPaymentName() + ")\n"
                + "Issued : " + issuer + " on " + Fmt.time(t.getCreatedAt()) + "\n"
                + "==========================================";
    }
}
