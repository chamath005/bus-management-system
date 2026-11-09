package com.busapp.ui;

import java.util.Scanner;

/** Safe keyboard input: re-asks until the value is valid, so typing mistakes never crash the program. */
public class Console {
    private final Scanner in = new Scanner(System.in);

    public void say(String s) { System.out.println(s); }

    public String optional(String prompt) {
        System.out.print(prompt);
        if (!in.hasNextLine()) throw new IllegalStateException("Input closed");
        return in.nextLine().trim();
    }

    public String text(String prompt) {
        while (true) {
            String s = optional(prompt);
            if (!s.isEmpty()) return s;
            say("This field cannot be empty.");
        }
    }

    public int integer(String prompt, int min, int max) {
        while (true) {
            try {
                int v = Integer.parseInt(text(prompt));
                if (v >= min && v <= max) return v;
                say("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                say("Please enter a whole number.");
            }
        }
    }

    public double decimal(String prompt, double min, double max) {
        while (true) {
            try {
                double v = Double.parseDouble(text(prompt));
                if (v >= min && v <= max) return v;
                say("Please enter a value between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                say("Please enter a valid number.");
            }
        }
    }

    public boolean confirm(String prompt) {
        return text(prompt + " (y/n): ").toLowerCase().startsWith("y");
    }
}
