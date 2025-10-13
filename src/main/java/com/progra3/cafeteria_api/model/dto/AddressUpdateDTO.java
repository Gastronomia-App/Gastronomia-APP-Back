package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record AddressUpdateDTO(

        @Schema(
                description = "Street name of the address. Maximum 50 characters. Cannot be blank or only spaces.",
                example = "Av. Santa Fe 2345",
                maxLength = 50,
                nullable = true
        )
        @Size(max = 50, message = "Street must be at most 50 characters")
        @Pattern(regexp = "^\\s*\\S.*$", message = "Street cannot be blank or only spaces")
        String street,

        @Schema(
                description = "City of the address. Maximum 50 characters. Cannot be blank or only spaces.",
                example = "Rosario",
                maxLength = 50,
                nullable = true
        )
        @Size(max = 50, message = "City must be at most 50 characters")
        @Pattern(regexp = "^\\s*\\S.*$", message = "City cannot be blank or only spaces")
        String city,

        @Schema(
                description = "Province of the address. Maximum 50 characters. Cannot be blank or only spaces.",
                example = "Santa Fe",
                maxLength = 50,
                nullable = true
        )
        @Size(max = 50, message = "Province must be at most 50 characters")
        @Pattern(regexp = "^\\s*\\S.*$", message = "Province cannot be blank or only spaces")
        String province,

        @Schema(
                description = "Zip code of the address. Must be between 4 and 8 digits.",
                example = "2000",
                pattern = "^\\d{4,8}$",
                nullable = true
        )
        @Pattern(regexp = "^\\d{4,8}$", message = "Zip code must be between 4 and 8 digits")
        String zipCode
) {}