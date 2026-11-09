package com.busapp.model;

import com.busapp.exception.InsufficientFundsException;

public class Passenger extends User {
    private static final long serialVersionUID = 1L;
    private double wallet;

    public Passenger(String id, String name, String phone, String password) { super(id, name, phone, password); }

    @Override public Role getRole() { return Role.PASSENGER; }
    public double getWallet() { return wallet; }

    public void topUp(double amount) {
        if (amount <= 0 || amount > 100000) throw new IllegalArgumentException("Top-up must be between Rs.1 and Rs.100,000.");
        wallet += amount;
    }
    public void debit(double amount) throws InsufficientFundsException {
        if (amount > wallet) throw new InsufficientFundsException(amount, wallet);
        wallet -= amount;
    }
    public void credit(double amount) { wallet += amount; }
}
