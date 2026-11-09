package com.busapp.service;

import com.busapp.model.*;
import com.busapp.storage.DataStore;
import com.busapp.util.Fmt;

public class CommissionService {
    private final DataStore store;
    public CommissionService(DataStore store) { this.store = store; }

    /** Fare revenue from tickets that this crew member was responsible for (cancelled tickets excluded). */
    public double revenueHandledBy(CrewMember m) {
        double sum = 0;
        for (Ticket t : store.tickets.all()) {
            if (t.getStatus() == TicketStatus.CANCELLED) continue;
            String crewId = (m instanceof Driver) ? t.getDriverId() : t.getConductorId();
            if (m.getId().equals(crewId)) sum += t.getFare();
        }
        return sum;
    }

    /** Polymorphism: the rate comes from the Driver/Conductor subclass. */
    public double commissionFor(CrewMember m) { return revenueHandledBy(m) * m.getCommissionRate(); }

    public String summary(CrewMember m) {
        double revenue = revenueHandledBy(m);
        return String.format("%s (%s)%n  Fare revenue handled: %s%n  Commission rate: %.0f%%%n  Commission earned: %s",
                m.getName(), m.getRole(), Fmt.money(revenue), m.getCommissionRate() * 100, Fmt.money(revenue * m.getCommissionRate()));
    }
}
