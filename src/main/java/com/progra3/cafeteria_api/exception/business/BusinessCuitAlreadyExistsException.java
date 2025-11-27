package com.progra3.cafeteria_api.exception.business;

public class BusinessCuitAlreadyExistsException extends RuntimeException {
    public BusinessCuitAlreadyExistsException(String cuit) {
        super("A business with CUIT '" + cuit + "' already exists.");
    }
}

