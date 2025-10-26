package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BusinessResponseDTO(
        @Schema(description = "Unique identifier of the business", example = "1")
        Long id,

        @Schema(description = "Name of the business", example = "Café Central")
        String name,

        @Schema(description = "CUIT (tax ID) of the business", example = "30-12345678-9")
        String cuit,

        @Schema(description = "Address details of the business")
        AddressResponseDTO address

) {}
