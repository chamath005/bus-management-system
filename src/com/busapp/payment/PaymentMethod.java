package com.busapp.payment;

import com.busapp.exception.BusAppException;

/** Every way of paying (wallet, card, cash) implements this. */
public interface PaymentMethod {
    void pay(double amount) throws BusAppException;
    String getName();
}
