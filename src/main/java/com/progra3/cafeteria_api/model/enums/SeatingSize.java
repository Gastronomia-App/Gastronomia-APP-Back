package com.progra3.cafeteria_api.model.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeatingSize {
    SMALL("Small"),
    MEDIUM("Medium"),
    LARGE("Large");

    private final String label;
}
