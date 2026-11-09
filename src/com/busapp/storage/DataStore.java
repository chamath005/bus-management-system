package com.busapp.storage;

import com.busapp.model.*;
import java.io.Serializable;

/** Everything the application knows. Saved to and loaded from one file. */
public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;
    public final Repository<User> users = new Repository<>("User", "U");
    public final Repository<Bus> buses = new Repository<>("Bus", "B");
    public final Repository<Route> routes = new Repository<>("Route", "R");
    public final Repository<Ticket> tickets = new Repository<>("Ticket", "T");
    public final Repository<LostItem> lostItems = new Repository<>("Lost item report", "L");
    public final Repository<ReplacementRequest> requests = new Repository<>("Replacement request", "Q");

    public void notifyUser(String userId, String message) {
        users.find(userId).ifPresent(u -> u.addNotification(com.busapp.util.Fmt.now() + " - " + message));
    }
}
