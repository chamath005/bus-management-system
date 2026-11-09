package com.busapp.exception;

public class NotFoundException extends BusAppException {
    public NotFoundException(String entity, String id) { super(entity + " not found: " + id); }
}
