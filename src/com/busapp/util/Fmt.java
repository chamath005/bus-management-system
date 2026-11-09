package com.busapp.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Fmt {
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    private Fmt() {}
    public static String money(double v) { return String.format("Rs.%,.2f", v); }
    public static String time(LocalDateTime t) { return t.format(F); }
    public static String now() { return time(LocalDateTime.now()); }
}
