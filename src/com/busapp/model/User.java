package com.busapp.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Abstract base of every account. Passwords are stored only as salted SHA-256 hashes. */
public abstract class User implements Identifiable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String phone;
    private String name;
    private final String passwordHash;
    private final List<String> notifications = new ArrayList<>();

    protected User(String id, String name, String phone, String rawPassword) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required.");
        if (phone == null || !phone.matches("\\+?\\d{9,12}"))
            throw new IllegalArgumentException("Phone number must be 9-12 digits.");
        if (rawPassword == null || rawPassword.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        this.id = id;
        this.name = name.trim();
        this.phone = phone.trim();
        this.passwordHash = hash(id, rawPassword);
    }

    public abstract Role getRole();

    @Override public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }

    public boolean checkPassword(String raw) { return passwordHash.equals(hash(id, raw)); }

    public void addNotification(String message) { notifications.add(message); }
    public List<String> getNotifications() { return Collections.unmodifiableList(notifications); }
    public void clearNotifications() { notifications.clear(); }

    private static String hash(String salt, String raw) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest((salt + ":" + raw).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    @Override public String toString() { return String.format("%-5s %-20s %-12s %s", id, name, phone, getRole()); }
}
