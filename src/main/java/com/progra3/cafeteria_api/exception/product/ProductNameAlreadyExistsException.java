package com.progra3.cafeteria_api.exception.product;

public class ProductNameAlreadyExistsException extends RuntimeException {
    public ProductNameAlreadyExistsException(String name) {
        super("A product with name '" + name + "' already exists in this business.");
    }
}

