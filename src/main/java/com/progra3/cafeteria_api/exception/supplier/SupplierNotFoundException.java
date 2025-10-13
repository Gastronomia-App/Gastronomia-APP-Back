package com.progra3.cafeteria_api.exception.supplier;


public class SupplierNotFoundException extends RuntimeException {
  public SupplierNotFoundException(Long id) {
    super("No supplier found for ID: " + id);
  }
}
