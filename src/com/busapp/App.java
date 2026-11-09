package com.busapp;

import com.busapp.exception.BusAppException;
import com.busapp.exception.StorageException;
import com.busapp.model.*;
import com.busapp.service.AuthService;
import com.busapp.storage.DataStorage;
import com.busapp.storage.DataStore;
import com.busapp.ui.*;
import java.nio.file.Paths;

/** Entry point: register / login, then open the menu that belongs to the user's role. */
public class App {
    public static void main(String[] args) {
        Console io = new Console();
        DataStorage storage = new DataStorage(Paths.get("data", "busapp.dat"));
        DataStore store;
        try {
            store = storage.load();
        } catch (StorageException e) {
            io.say(e.getMessage());
            return;
        }
        AppContext ctx = new AppContext(storage, store, io);
        try {
            if (store.users.isEmpty()) {
                DemoData.seed(ctx);
                ctx.save();
                io.say("(First run: demo data created. See README for demo logins.)");
            }
            mainMenu(ctx);
        } catch (BusAppException e) {
            io.say("Fatal error: " + e.getMessage());
        } catch (IllegalStateException e) {
            io.say("\nInput closed. Goodbye.");
        }
        ctx.save();
    }

    private static void mainMenu(AppContext ctx) {
        Console io = ctx.io;
        io.say("\n==========================================");
        io.say("   BUS FLEET MANAGEMENT & PASSENGER SYSTEM");
        io.say("==========================================");
        while (true) {
            io.say("\n1. Login\n2. Register\n0. Exit");
            int c = io.integer("Choose: ", 0, 2);
            if (c == 0) { io.say("Goodbye!"); return; }
            try {
                if (c == 1) login(ctx); else register(ctx);
            } catch (BusAppException e) {
                io.say("Error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                io.say("Invalid input: " + e.getMessage());
            }
        }
    }

    private static void register(AppContext ctx) throws BusAppException {
        Console io = ctx.io;
        io.say("Register as:  1. Passenger  2. Driver  3. Conductor  4. Bus owner");
        int r = io.integer("Choose: ", 1, 4);
        Role role = Role.values()[r - 1];
        String name = io.text("Full name: ");
        String phone = io.text("Phone number: ");
        String password = io.text("Password (min 6 characters): ");
        if (!password.equals(io.text("Confirm password: "))) { io.say("Passwords do not match."); return; }
        String extra = null;
        if (role == Role.DRIVER) extra = io.text("Driving licence number: ");
        if (role == Role.OWNER) extra = io.text("Owner registration key: ");
        ctx.auth.register(role, name, phone, password, extra);
        ctx.save();
        io.say("Registration successful. You can now log in as " + role + ".");
    }

    private static void login(AppContext ctx) throws BusAppException {
        Console io = ctx.io;
        User user = ctx.auth.login(io.text("Phone number: "), io.text("Password: "));
        io.say("\nWelcome, " + user.getName() + " (" + user.getRole() + ")");
        RoleMenu menu;
        switch (user.getRole()) {
            case PASSENGER: menu = new PassengerMenu(ctx, (Passenger) user); break;
            case DRIVER: menu = new DriverMenu(ctx, (Driver) user); break;
            case CONDUCTOR: menu = new ConductorMenu(ctx, (Conductor) user); break;
            default: menu = new OwnerMenu(ctx, (Owner) user); break;
        }
        menu.run();
    }
}
