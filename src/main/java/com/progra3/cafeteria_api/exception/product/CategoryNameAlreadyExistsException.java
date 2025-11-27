package com.progra3.cafeteria_api.exception.product;

public class CategoryNameAlreadyExistsException extends RuntimeException {
    public CategoryNameAlreadyExistsException(String name) {
        super("A category with name '" + name + "' already exists in this business.");
    }
}

