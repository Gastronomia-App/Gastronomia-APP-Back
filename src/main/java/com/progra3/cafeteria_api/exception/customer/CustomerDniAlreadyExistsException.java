package com.progra3.cafeteria_api.exception.customer;

public class CustomerDniAlreadyExistsException extends RuntimeException {
    public CustomerDniAlreadyExistsException(String dni) {
        super("A customer with DNI '" + dni + "' already exists in this business.");
    }
}

