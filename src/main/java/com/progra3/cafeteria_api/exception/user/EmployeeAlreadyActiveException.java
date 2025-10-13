package com.progra3.cafeteria_api.exception.user;

public class EmployeeAlreadyActiveException extends RuntimeException {
    public EmployeeAlreadyActiveException(String dni){super("Employee DNI: " + dni + " is already active");}
}
