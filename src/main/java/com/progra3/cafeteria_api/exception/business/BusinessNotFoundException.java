package com.progra3.cafeteria_api.exception.business;

public class BusinessNotFoundException extends RuntimeException {
    public BusinessNotFoundException(Long id) {
        super("No business found for ID: " + id);
    }

    public BusinessNotFoundException(String slug) {
        super("Business not found with slug: " + slug);
    }
}