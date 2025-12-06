package com.progra3.cafeteria_api.afip.wsfe.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record FECAEResponse(
    FECAECabResponse feCabResp,
    List<FECAEDetResponse> feDetResp,
    List<Evento> events,
    List<Err> errors
) {}

