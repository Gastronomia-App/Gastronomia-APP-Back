package com.progra3.cafeteria_api.exception.customer;

public class CustomerAlreadyActiveException extends RuntimeException {
    public CustomerAlreadyActiveException(String dni) {
        super("Customer DNI: " + dni + " is already active");
    }
}
