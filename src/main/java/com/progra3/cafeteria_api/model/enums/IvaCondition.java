package com.progra3.cafeteria_api.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IvaCondition {
    RESPONSABLE_INSCRIPTO(1),
    RESPONSABLE_MONOTRIBUTO(6),
    MONOTRIBUTISTA_SOCIAL(13),
    MONOTRIBUTO_INDEPENDIENTE_PROMOVIDO(16),
    SUJETO_EXENTO(4),
    CONSUMIDOR_FINAL(5),
    SUJETO_NO_CATEGORIZADO(7),
    PROVEEDOR_DEL_EXTERIOR(8),
    CLIENTE_DEL_EXTERIOR(9),
    IVA_LIBERADO(10),
    IVA_NO_ALCANZADO(15);

    private final int code;
}