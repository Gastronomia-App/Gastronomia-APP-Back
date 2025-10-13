package com.progra3.cafeteria_api.exception.product;

public class ProductGroupCannotBeDeletedException extends RuntimeException {
    public ProductGroupCannotBeDeletedException(Long id) {
        super("Cannot delete product group with ID: " + id + " because it has associated products.");
    }
}
