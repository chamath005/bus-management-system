package com.busapp.exception;

public class SeatUnavailableException extends BusAppException {
    public SeatUnavailableException(int seat) { super("Seat " + seat + " is already taken or does not exist."); }
}
