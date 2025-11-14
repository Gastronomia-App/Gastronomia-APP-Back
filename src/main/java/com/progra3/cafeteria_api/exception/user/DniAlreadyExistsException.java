package com.progra3.cafeteria_api.exception.user;

public class DniAlreadyExistsException extends RuntimeException {
    public DniAlreadyExistsException(String dni){
        super("An employee with DNI '" + dni + "' already exists and is active.");
    }
}

