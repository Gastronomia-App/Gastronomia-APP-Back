package com.progra3.cafeteria_api.exception.user;

public class EmployeeCannotBeDeletedException extends RuntimeException {
    public EmployeeCannotBeDeletedException(){super("Only ADMIN can delete employees.");}
}
