package com.progra3.cafeteria_api.exception.user;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("An employee with username '" + username + "' already exists.");
    }
}

