package com.progra3.cafeteria_api.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeatingOrientation {
    HORIZONTAL("Horizontal"),
    VERTICAL("Vertical");

    private final String label;
}