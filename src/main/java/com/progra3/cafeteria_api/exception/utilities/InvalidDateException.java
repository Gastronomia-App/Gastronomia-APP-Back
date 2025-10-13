package com.progra3.cafeteria_api.exception.utilities;

public class InvalidDateException extends RuntimeException {
    public InvalidDateException(String message) {
        super(message);
    }
}