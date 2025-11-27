package com.progra3.cafeteria_api.exception.supplier;

public class SupplierPhoneNumberAlreadyExistsException extends RuntimeException {
    public SupplierPhoneNumberAlreadyExistsException(String phoneNumber) {
        super("A supplier with phone number '" + phoneNumber + "' already exists in this business.");
    }
}

