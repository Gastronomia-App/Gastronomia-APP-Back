package com.progra3.cafeteria_api.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderType {
    TABLE("Table"),
    TAKEAWAY("Takeaway"),
    DELIVERY("Delivery");

    private final String name;
}
