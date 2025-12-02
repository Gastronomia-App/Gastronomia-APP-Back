package com.progra3.cafeteria_api.event;

import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;

import java.util.List;

public record OrderItemsAddedEvent(
        Order order,
        List<Item> newItems
) { }