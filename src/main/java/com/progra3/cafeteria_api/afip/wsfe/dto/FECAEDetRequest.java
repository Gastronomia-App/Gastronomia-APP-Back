package com.progra3.cafeteria_api.afip.wsfe.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FECAEDetRequest(
    Integer concepto,     // 1=Productos, 2=Servicios, 3=Productos y Servicios
    Integer docTipo,      // Tipo de documento del comprador
    Long docNro,          // Número de documento del comprador
    Long cbteDesde,       // Número de comprobante desde
    Long cbteHasta,       // Número de comprobante hasta
    String cbteFch,       // Fecha comprobante formato YYYYMMDD
    BigDecimal impTotal,  // Importe total del comprobante
    BigDecimal impTotConc, // Importe neto no gravado
    BigDecimal impNeto,   // Importe neto gravado
    BigDecimal impOpEx,   // Importe exento
    BigDecimal impTrib,   // Importe de tributos
    BigDecimal impIVA,    // Importe total de IVA
    String monId,         // Moneda "PES"=Pesos
    BigDecimal monCotiz   // Cotización de la moneda (1 para pesos)
) {}

