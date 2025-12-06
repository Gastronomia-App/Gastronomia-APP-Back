package com.progra3.cafeteria_api.afip.wsfe.dto;

import lombok.Builder;

@Builder
public record FECAECabResponse(
    Long cuit,
    Integer ptoVta,
    Integer cbteTipo,
    Integer cantReg,
    String resultado, // A=Aprobado, P=Parcial, R=Rechazado
    String reproceso
) {}

