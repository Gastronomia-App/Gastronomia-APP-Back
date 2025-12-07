package com.progra3.cafeteria_api.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentType {
    DNI(96),
    CUIT(80),
    CONSUMIDOR_FINAL(99);

    private final int code;
}
