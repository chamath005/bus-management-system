package com.busapp.payment;

import com.busapp.exception.BusAppException;
import com.busapp.model.Passenger;

public class WalletPayment implements PaymentMethod {
    private final Passenger passenger;
    public WalletPayment(Passenger passenger) { this.passenger = passenger; }
    @Override public void pay(double amount) throws BusAppException { passenger.debit(amount); }
    @Override public String getName() { return "Wallet"; }
}
