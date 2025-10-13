package com.progra3.cafeteria_api.exception.supplier;

public class SupplierAlreadyActiveException extends RuntimeException {
    public SupplierAlreadyActiveException(String message) {
        super(message);
    }
}
