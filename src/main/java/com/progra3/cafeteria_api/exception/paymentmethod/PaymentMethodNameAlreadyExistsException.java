package com.progra3.cafeteria_api.exception.paymentmethod;

public class PaymentMethodNameAlreadyExistsException extends RuntimeException {
    public PaymentMethodNameAlreadyExistsException(String name) {
        super("A payment method with name '" + name + "' already exists in this business.");
    }
}

