package com.progra3.cafeteria_api.exception.product;

public class NotEnoughStockException extends RuntimeException {
    public NotEnoughStockException(Long id) {
        super("Not enough stock available for product with ID: " + id);
    }
}