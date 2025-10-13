package com.progra3.cafeteria_api.exception.order;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(Long id) {
        super("No items found for ID: " + id);
    }
}



