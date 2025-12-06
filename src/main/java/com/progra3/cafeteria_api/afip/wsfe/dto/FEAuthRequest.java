package com.progra3.cafeteria_api.afip.wsfe.dto;

import lombok.Builder;

@Builder
public record FEAuthRequest(
    Long cuit,
    String sign,
    String token
) {}


