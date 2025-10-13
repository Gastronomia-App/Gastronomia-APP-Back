package com.progra3.cafeteria_api.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    ACTIVE("Active"),
    BILLED("Billed"),
    FINALIZED("Finalized"),
    CANCELED("Canceled");

    private final String name;
}
