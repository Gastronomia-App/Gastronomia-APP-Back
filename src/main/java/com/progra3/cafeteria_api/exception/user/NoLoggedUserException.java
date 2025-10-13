package com.progra3.cafeteria_api.exception.user;

public class NoLoggedUserException extends RuntimeException {
    public NoLoggedUserException(){super("No user is currently logged in");}
}
