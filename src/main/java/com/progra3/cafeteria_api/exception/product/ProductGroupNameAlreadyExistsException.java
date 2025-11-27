package com.progra3.cafeteria_api.exception.product;

public class ProductGroupNameAlreadyExistsException extends RuntimeException {
    public ProductGroupNameAlreadyExistsException(String name) {
        super("A product group with name '" + name + "' already exists in this business.");
    }
}

