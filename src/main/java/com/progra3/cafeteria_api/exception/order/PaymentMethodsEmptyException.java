package com.progra3.cafeteria_api.exception.order;

public class PaymentMethodsEmptyException extends RuntimeException {
    public PaymentMethodsEmptyException() {
        super("At least one payment method is required to finalize an order");
    }
}

