package com.progra3.cafeteria_api.event;

import com.progra3.cafeteria_api.model.entity.Order;

public record OrderFinalizedEvent(Order order) {
}
