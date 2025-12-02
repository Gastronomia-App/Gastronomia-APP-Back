package com.progra3.cafeteria_api.exception.paymentmethod;

public class DuplicatePaymentMethodException extends RuntimeException {
    public DuplicatePaymentMethodException(Long paymentMethodId) {
        super("Payment method with ID " + paymentMethodId + " is duplicated in the request");
    }
}

