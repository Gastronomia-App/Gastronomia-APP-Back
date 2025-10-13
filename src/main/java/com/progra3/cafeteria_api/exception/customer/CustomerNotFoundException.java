package com.progra3.cafeteria_api.exception.customer;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long id) {
        super("No customer found for ID: " + id);
    }
}
