package com.busapp.payment;

import com.busapp.exception.ValidationException;

/** Simulated card gateway: approves any well-formed 16-digit number. */
public class CardPayment implements PaymentMethod {
    private final String cardNumber;
    public CardPayment(String cardNumber) { this.cardNumber = cardNumber == null ? "" : cardNumber.replaceAll("\\s", ""); }
    @Override public void pay(double amount) throws ValidationException {
        if (!cardNumber.matches("\\d{16}")) throw new ValidationException("Card number must be exactly 16 digits.");
    }
    @Override public String getName() { return "Card ****" + cardNumber.substring(Math.max(0, cardNumber.length() - 4)); }
}
