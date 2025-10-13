package com.progra3.cafeteria_api.exception.product;

public class ProductGroupNotFoundException extends RuntimeException {
    public ProductGroupNotFoundException(Long id) {
        super("No product group found for ID: " + id);
    }
}
