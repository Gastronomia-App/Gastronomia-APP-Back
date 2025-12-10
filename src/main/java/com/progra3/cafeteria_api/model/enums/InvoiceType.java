package com.progra3.cafeteria_api.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InvoiceType {
    FACTURA_A(1),
    FACTURA_B(6),
    FACTURA_C(11),
    NOTA_CREDITO_A(3),
    NOTA_CREDITO_B(8),
    NOTA_CREDITO_C(13);

    private final int invoiceCode;
}