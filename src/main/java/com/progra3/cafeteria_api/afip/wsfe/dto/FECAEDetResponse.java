package com.progra3.cafeteria_api.afip.wsfe.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record FECAEDetResponse(
    Integer concepto,
    Integer docTipo,
    Long docNro,
    Long cbteDesde,
    Long cbteHasta,
    String cbteFch,
    String resultado, // A=Aprobado, R=Rechazado
    String cae,       // Código de Autorización Electrónico
    String caeFchVto, // Fecha vencimiento CAE formato YYYYMMDD
    List<Observacion> observaciones
) {}

