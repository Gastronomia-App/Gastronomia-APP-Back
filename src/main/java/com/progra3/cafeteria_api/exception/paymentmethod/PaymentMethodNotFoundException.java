package com.progra3.cafeteria_api.exception.paymentmethod;

public class PaymentMethodNotFoundException extends RuntimeException {
    public PaymentMethodNotFoundException(Long id) {
        super("Payment method not found with ID: " + id);
    }
}

