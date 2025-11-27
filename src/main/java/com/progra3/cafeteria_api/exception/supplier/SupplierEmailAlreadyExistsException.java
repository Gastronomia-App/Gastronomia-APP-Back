package com.progra3.cafeteria_api.exception.supplier;

public class SupplierEmailAlreadyExistsException extends RuntimeException {
    public SupplierEmailAlreadyExistsException(String email) {
        super("A supplier with email '" + email + "' already exists in this business.");
    }
}

