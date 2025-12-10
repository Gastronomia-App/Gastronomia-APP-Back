package com.progra3.cafeteria_api.exception.order;

public class FailedToPrintException extends RuntimeException {
    public FailedToPrintException(String message, Throwable cause) {
        super(message, cause);
    }
}
