package com.progra3.cafeteria_api.exception.order;

public class PaymentMethodRequiredException extends RuntimeException {
    public PaymentMethodRequiredException() {
        super("Payment method is required to finalize an order");
    }
}

