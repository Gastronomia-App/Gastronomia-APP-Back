package com.progra3.cafeteria_api.exception.user;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("An employee with email '" + email + "' already exists in this business.");
    }
}
