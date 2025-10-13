package com.progra3.cafeteria_api.exception.product;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Exception thrown when a product is not found")
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id){
        super("Product not found with ID: " + id);
    }
}
