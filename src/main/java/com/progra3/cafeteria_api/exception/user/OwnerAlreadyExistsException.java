package com.progra3.cafeteria_api.exception.user;

public class OwnerAlreadyExistsException extends RuntimeException {
    public OwnerAlreadyExistsException(){super("An admin already exists.");}
}
