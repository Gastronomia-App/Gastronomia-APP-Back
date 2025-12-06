package com.progra3.cafeteria_api.afip.wsfe.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record FECAERequest(
    FECAECabRequest feCabReq,
    List<FECAEDetRequest> feDetReq
) {}

