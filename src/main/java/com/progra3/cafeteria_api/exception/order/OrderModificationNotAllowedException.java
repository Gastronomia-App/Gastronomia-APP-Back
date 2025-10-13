package com.progra3.cafeteria_api.exception.order;

public class OrderModificationNotAllowedException extends RuntimeException {
    public OrderModificationNotAllowedException(String status) {
        super("The order is already " + status + " and cannot be modified.");
    }
}
