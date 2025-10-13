package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record AddressRequestDTO(
        @Schema(description = "Street name of the address", example = "Av. Corrientes 1234", required = true)
        @NotBlank(message = "Street cannot be null")
        @Size(min = 1, max = 50, message = "Street must be between 1 and 50 characters")
        String street,

        @Schema(description = "City of the address", example = "Buenos Aires", required = true)
        @NotBlank(message = "City cannot be null")
        @Size(min = 1, max = 50, message = "City must be between 1 and 50 characters")
        String city,

        @Schema(description = "Province of the address", example = "Buenos Aires", required = true)
        @NotBlank(message = "Province cannot be null")
        @Size(min = 1, max = 50, message = "Province must be between 1 and 50 characters")
        String province,

        @Schema(description = "Zip code of the address", example = "1406", required = true)
        @NotBlank(message = "Zip code cannot be null")
        @Pattern(regexp = "^\\d{4,8}$", message = "Zip code must be between 4 and 8 digits")
        String zipCode
) {}