package com.progra3.cafeteria_api.exception.paymentmethod;

public class PaymentMethodAlreadyActiveException extends RuntimeException {
    public PaymentMethodAlreadyActiveException(String name) {
        super("A payment method with name '" + name + "' is already active in this business.");
    }
}

