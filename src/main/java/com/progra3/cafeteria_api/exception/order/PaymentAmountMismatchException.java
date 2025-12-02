package com.progra3.cafeteria_api.exception.order;

public class PaymentAmountMismatchException extends RuntimeException {
    public PaymentAmountMismatchException(Double expectedAmount, Double providedAmount) {
        super(String.format("Payment amount mismatch. Expected: %.2f, but received: %.2f", expectedAmount, providedAmount));
    }
}

