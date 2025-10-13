package com.progra3.cafeteria_api.exception.product;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Exception thrown when a category cannot be deleted")
public class CategoryCannotBeDeletedException extends RuntimeException {
    public CategoryCannotBeDeletedException(String message) {
        super(message);
    }
}
