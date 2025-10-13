package com.progra3.cafeteria_api.exception.user;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(String username) {
        super("Employee not found by username: " + username);
    }
    public EmployeeNotFoundException(Long id){super("No employee found for ID: " + id);}
}
