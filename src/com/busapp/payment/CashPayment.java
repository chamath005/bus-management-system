package com.busapp.payment;

public class CashPayment implements PaymentMethod {
    @Override public void pay(double amount) { /* conductor collects cash on the bus */ }
    @Override public String getName() { return "Cash"; }
}
