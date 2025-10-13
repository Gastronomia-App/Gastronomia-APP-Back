package com.progra3.cafeteria_api.exception.user;

public class OwnerCannotBeDeletedException extends RuntimeException {
    public OwnerCannotBeDeletedException(){super("Cannot delete ADMIN.");}
}
