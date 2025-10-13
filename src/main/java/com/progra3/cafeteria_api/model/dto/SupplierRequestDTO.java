package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
public record SupplierRequestDTO(

        @Schema(description = "Legal name of the supplier", example = "Empresa S.A.", minLength = 1, maxLength = 50)
        @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
        String legalName,

        @Schema(description = "Trade name of the supplier", example = "Comercial XYZ", minLength = 1, maxLength = 50, required = true)
        @NotBlank(message = "Trade name cannot be null")
        @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
        String tradeName,

        @Schema(description = "CUIT in format XX-XXXXXXXX-X", example = "30-12345678-9", pattern = "^\\d{2}-\\d{8}-\\d$")
        @Pattern(
                regexp = "^\\d{2}-\\d{8}-\\d$",
                message = "CUIT must have XX-XXXXXXXX-X format"
        )
        String cuit,

        @Schema(description = "Phone number with 10 to 13 digits", example = "1234567890")
        @Pattern(regexp = "^\\d{10,13}$", message = "Phone number must be between 10 and 13 numeric digits.")
        String phoneNumber,

        @Schema(description = "Email address", example = "supplier@example.com")
        @Email(message = "Invalid email format. Expected format: example@domain.com")
        String email,

        @Schema(description = "Address of the supplier", required = true)
        @Valid
        AddressRequestDTO address
) {}
