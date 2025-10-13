package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record AddressResponseDTO(
        @Schema(description = "Street name of the address", example = "Av. Corrientes 1234")
        String street,

        @Schema(description = "City of the address", example = "Buenos Aires")
        String city,

        @Schema(description = "Province of the address", example = "Buenos Aires")
        String province,

        @Schema(description = "Zip code of the address", example = "1406")
        String zipCode
) {}