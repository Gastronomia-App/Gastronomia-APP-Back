package com.progra3.cafeteria_api.event;

import com.progra3.cafeteria_api.model.entity.Product;

public record StockOutEvent(Product product, Long businessId) {
}

