package com.progra3.cafeteria_api.exception.paymentmethod;

public class PaymentMethodBusinessMismatchException extends RuntimeException {
    public PaymentMethodBusinessMismatchException(Long paymentMethodId, Long businessId) {
        super("Payment method with ID: " + paymentMethodId + " does not belong to business with ID: " + businessId);
    }
}

