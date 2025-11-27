package com.progra3.cafeteria_api.exception.customer;

public class CustomerPhoneNumberAlreadyExistsException extends RuntimeException {
    public CustomerPhoneNumberAlreadyExistsException(String phoneNumber) {
        super("A customer with phone number '" + phoneNumber + "' already exists in this business.");
    }
}

