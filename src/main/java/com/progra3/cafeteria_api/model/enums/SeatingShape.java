package com.progra3.cafeteria_api.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeatingShape {
    SQUARE("Square"),
    ROUND("Round");

    private final String label;
}