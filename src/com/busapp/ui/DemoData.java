package com.busapp.ui;

import com.busapp.exception.BusAppException;
import com.busapp.model.*;
import com.busapp.payment.WalletPayment;
import java.util.List;

/** Creates demo accounts, routes and buses on the very first run so the system can be tried immediately. */
public final class DemoData {
    private DemoData() {}

    public static void seed(AppContext ctx) throws BusAppException {
        Owner owner = (Owner) ctx.auth.register(Role.OWNER, "Saman Perera (Owner)", "0770000001", "owner123", "OWNER2026");
        Driver d1 = (Driver) ctx.auth.register(Role.DRIVER, "Kamal Driver", "0771111111", "driver123", "B1234567");
        Driver d2 = (Driver) ctx.auth.register(Role.DRIVER, "Nuwan Driver", "0771111112", "driver123", "B7654321");
        Conductor c1 = (Conductor) ctx.auth.register(Role.CONDUCTOR, "Sunil Conductor", "0772222221", "conduct123", null);
        Conductor c2 = (Conductor) ctx.auth.register(Role.CONDUCTOR, "Ruwan Conductor", "0772222222", "conduct123", null);
        Passenger p = (Passenger) ctx.auth.register(Role.PASSENGER, "Nimali Passenger", "0773333333", "pass123", null);
        p.topUp(2000);

        Route galle = ctx.fleet.addRoute("Colombo - Galle", List.of("Colombo Fort", "Dehiwala", "Mount Lavinia", "Moratuwa",
                "Panadura", "Kalutara", "Aluthgama", "Galle"), 25, 8);
        Route kandy = ctx.fleet.addRoute("Colombo - Kandy", List.of("Colombo Fort", "Kadawatha", "Kegalle", "Mawanella",
                "Kadugannawa", "Peradeniya", "Kandy"), 60, 15);

        Bus b1 = ctx.fleet.addBus("NB-1001", 40, owner.getId());
        Bus b2 = ctx.fleet.addBus("NB-1002", 40, owner.getId());
        ctx.fleet.addBus("NB-1003", 45, owner.getId());           // spare bus, no crew
        ctx.fleet.assignRoute(b1.getId(), galle.getId());
        ctx.fleet.assignRoute(b2.getId(), kandy.getId());
        ctx.fleet.assignCrew(b1.getId(), d1.getId(), c1.getId());
        ctx.fleet.assignCrew(b2.getId(), d2.getId(), c2.getId());

        ctx.booking.bookSeat(p, b1, 0, 4, 5, new WalletPayment(p));   // one demo booking
    }
}
