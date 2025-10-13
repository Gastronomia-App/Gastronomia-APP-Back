package com.progra3.cafeteria_api.exception.order;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("No orders found for ID: " + id);
    }
}
