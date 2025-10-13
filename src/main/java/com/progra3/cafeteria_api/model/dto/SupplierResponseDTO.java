package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
public record SupplierResponseDTO(
        @Schema(description = "Unique identifier of the supplier", example = "1")
        Long id,

        @Schema(description = "Legal name of the supplier", example = "Empresa S.A.")
        String legalName,

        @Schema(description = "Trade name of the supplier", example = "Comercial XYZ")
        String tradeName,

        @Schema(description = "CUIT in format XX-XXXXXXXX-X", example = "30-12345678-9")
        String cuit,

        @Schema(description = "Phone number with 10 to 13 digits", example = "1234567890")
        String phoneNumber,

        @Schema(description = "Email address", example = "supplier@example.com")
        String email,

        @Schema(description = "Address details of the supplier")
        AddressResponseDTO address,

        @Schema(description = "Logical deletion status of the supplier", example = "false")
        Boolean deleted
){}
