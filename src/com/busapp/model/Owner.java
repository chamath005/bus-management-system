package com.busapp.model;

public class Owner extends User {
    private static final long serialVersionUID = 1L;

    public Owner(String id, String name, String phone, String password) { super(id, name, phone, password); }
    @Override public Role getRole() { return Role.OWNER; }
}
