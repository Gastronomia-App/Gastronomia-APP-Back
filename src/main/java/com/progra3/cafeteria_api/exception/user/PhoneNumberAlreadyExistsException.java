package com.progra3.cafeteria_api.exception.user;

public class PhoneNumberAlreadyExistsException extends RuntimeException {
    public PhoneNumberAlreadyExistsException(String phoneNumber) {
        super("An employee with phone number '" + phoneNumber + "' already exists in this business.");
    }
}

