package com.busapp.exception;

public class InsufficientFundsException extends BusAppException {
    public InsufficientFundsException(double needed, double available) {
        super(String.format("Insufficient wallet balance. Needed Rs.%.2f but you only have Rs.%.2f. Please top up.", needed, available));
    }
}
