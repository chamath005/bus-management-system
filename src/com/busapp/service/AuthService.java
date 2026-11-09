package com.busapp.service;

import com.busapp.exception.*;
import com.busapp.model.*;
import com.busapp.storage.DataStore;

public class AuthService {
    /** Secret key a business owner must enter to create an owner account. */
    public static final String OWNER_KEY = "OWNER2026";
    private final DataStore store;

    public AuthService(DataStore store) { this.store = store; }

    public User register(Role role, String name, String phone, String password, String extra) throws BusAppException {
        for (User u : store.users.all())
            if (u.getPhone().equals(phone == null ? "" : phone.trim()))
                throw new ValidationException("This phone number is already registered.");
        String id = store.users.nextId();
        User user;
        try {
            switch (role) {
                case PASSENGER: user = new Passenger(id, name, phone, password); break;
                case DRIVER: user = new Driver(id, name, phone, password, extra); break;
                case CONDUCTOR: user = new Conductor(id, name, phone, password); break;
                case OWNER:
                    if (!OWNER_KEY.equals(extra)) throw new AuthException("Invalid owner registration key.");
                    user = new Owner(id, name, phone, password);
                    break;
                default: throw new ValidationException("Unknown role.");
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        }
        store.users.add(user);
        return user;
    }

    public User login(String phone, String password) throws AuthException {
        for (User u : store.users.all())
            if (u.getPhone().equals(phone.trim()) && u.checkPassword(password)) return u;
        throw new AuthException("Wrong phone number or password.");
    }
}
