package com.progra3.cafeteria_api.model.DTO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Public information of the business/cafeteria")
public record PublicBusinessDTO(

        @Schema(description = "Unique identifier of the business", example = "1")
        Long id,

        @Schema(description = "Commercial name of the business", example = "Cafetería Central")
        String name,

        @Schema(description = "URL friendly identifier", example = "cafeteria-central")
        String slug,

        @Schema(description = "Contact phone number", example = "+54 223 1234567")
        String phoneNumber,

        @Schema(description = "Street address", example = "Av. Independencia 1234")
        String addressStreet,

        @Schema(description = "City name", example = "Mar del Plata")
        String addressCity,

        @Schema(description = "Postal/Zip code", example = "7600")
        String addressZipCode,

        @Schema(description = "Province or State name", example = "Buenos Aires")
        String addressProvince
) {}