package com.progra3.cafeteria_api.exception.supplier;

public class SupplierCuitAlreadyExistsException extends RuntimeException {
    public SupplierCuitAlreadyExistsException(String cuit) {
        super("A supplier with CUIT '" + cuit + "' already exists in this business.");
    }
}

