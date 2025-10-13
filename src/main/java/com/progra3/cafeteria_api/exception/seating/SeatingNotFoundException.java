package com.progra3.cafeteria_api.exception.seating;

public class SeatingNotFoundException extends RuntimeException {
    public SeatingNotFoundException(Long id) {
        super("No seating found for ID: " + id);
    }
    public SeatingNotFoundException(Integer number) {
        super("No seating found for number: " + number);
    }
}
