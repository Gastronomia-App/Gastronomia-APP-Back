package com.progra3.cafeteria_api.exception.expense;

public class ExpenseNotFoundException extends RuntimeException {
    public ExpenseNotFoundException(Long id) {
        super("No expense found for ID: " + id);
    }
}