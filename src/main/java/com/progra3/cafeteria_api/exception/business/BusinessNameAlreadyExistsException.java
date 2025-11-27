package com.progra3.cafeteria_api.exception.business;

public class BusinessNameAlreadyExistsException extends RuntimeException {
    public BusinessNameAlreadyExistsException(String name) {
        super("A business with name '" + name + "' already exists.");
    }
}

