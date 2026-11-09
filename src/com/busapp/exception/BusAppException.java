package com.busapp.exception;

/** Base class of all checked, business-level errors. */
public class BusAppException extends Exception {
    public BusAppException(String message) { super(message); }
    public BusAppException(String message, Throwable cause) { super(message, cause); }
}
