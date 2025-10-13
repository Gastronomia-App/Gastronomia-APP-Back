package com.progra3.cafeteria_api.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeatingStatus {
    FREE("Free"),
    OCCUPIED("Occupied"),
    BILLING("Billing"),
    DELETED("Deleted");

    private final String name;
}
