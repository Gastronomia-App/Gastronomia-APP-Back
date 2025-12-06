package com.progra3.cafeteria_api.afip.wsfe.dto;

import lombok.Builder;

@Builder
public record FECAECabRequest(
    Integer cantReg, // Cantidad de registros
    Integer ptoVta,  // Punto de venta
    Integer cbteTipo // Tipo de comprobante
) {}

