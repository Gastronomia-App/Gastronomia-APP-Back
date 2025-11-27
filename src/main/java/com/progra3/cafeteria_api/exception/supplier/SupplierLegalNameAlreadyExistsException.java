package com.progra3.cafeteria_api.exception.supplier;

public class SupplierLegalNameAlreadyExistsException extends RuntimeException {
    public SupplierLegalNameAlreadyExistsException(String legalName) {
        super("A supplier with legal name '" + legalName + "' already exists in this business.");
    }
}

