package com.progra3.cafeteria_api.exception.customer;

public class CustomerEmailAlreadyExistsException extends RuntimeException {
    public CustomerEmailAlreadyExistsException(String email) {
        super("A customer with email '" + email + "' already exists in this business.");
    }
}

