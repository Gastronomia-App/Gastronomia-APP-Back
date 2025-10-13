package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SupplierUpdateDTO(

        @Schema(description = "Legal name of the supplier", example = "Empresa S.A.")
        @Size(min = 1, max = 50, message = "Legal name must be between 1 and 50 characters")
        @Pattern(regexp = "^\\s*\\S.*$", message = "Legal name cannot be blank or only spaces")
        String legalName,

        @Schema(description = "Trade name of the supplier", example = "Comercial XYZ")
        @Size(min = 1, max = 50, message = "Trade name must be between 1 and 50 characters")
        @Pattern(regexp = "^\\s*\\S.*$", message = "Trade name cannot be blank or only spaces")
        String tradeName,

        @Schema(description = "CUIT in format XX-XXXXXXXX-X", example = "30-12345678-9")
        @Pattern(regexp = "^\\d{2}-\\d{8}-\\d$", message = "CUIT must have XX-XXXXXXXX-X format")
        String cuit,

        @Schema(description = "Phone number with 10 to 13 digits", example = "1234567890")
        @Pattern(regexp = "^\\d{10,13}$", message = "Phone number must be between 10 and 13 numeric digits.")
        String phoneNumber,

        @Schema(description = "Email address", example = "supplier@example.com")
        @Email(message = "Invalid email format. Expected format: example@domain.com")
        String email,

        @Schema(description = "Address update details")
        @Valid
        AddressUpdateDTO address
) {}