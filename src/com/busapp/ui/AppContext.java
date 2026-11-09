package com.busapp.ui;

import com.busapp.exception.StorageException;
import com.busapp.service.*;
import com.busapp.storage.DataStorage;
import com.busapp.storage.DataStore;

/** Wires all services together once and hands them to the menus. */
public class AppContext {
    public final DataStore store;
    public final DataStorage storage;
    public final Console io;
    public final AuthService auth;
    public final FleetService fleet;
    public final BookingService booking;
    public final CommissionService commission;
    public final LostItemService lost;
    public final ReportService reports;

    public AppContext(DataStorage storage, DataStore store, Console io) {
        this.storage = storage;
        this.store = store;
        this.io = io;
        this.auth = new AuthService(store);
        this.fleet = new FleetService(store);
        this.booking = new BookingService(store, fleet);
        this.commission = new CommissionService(store);
        this.lost = new LostItemService(store);
        this.reports = new ReportService(store);
    }

    public void save() {
        try {
            storage.save(store);
        } catch (StorageException e) {
            io.say("WARNING: " + e.getMessage());
        }
    }
}
