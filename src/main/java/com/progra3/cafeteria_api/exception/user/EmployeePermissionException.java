package com.progra3.cafeteria_api.exception.user;

public class EmployeePermissionException extends RuntimeException {
    public EmployeePermissionException(){super("Only ADMIN has permission to execute this action.");}
}
