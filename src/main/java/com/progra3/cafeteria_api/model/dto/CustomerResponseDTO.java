package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
public record CustomerResponseDTO (
        @Schema(description = "Unique identifier of the customer", example = "123")
        Long id,

        @Schema(description = "First name of the customer", example = "Juan")
        String name,

        @Schema(description = "Last name of the customer", example = "Pérez")
        String lastName,

        @Schema(description = "National ID number (DNI)", example = "12345678")
        String dni,

        @Schema(description = "Phone number of the customer", example = "541112345678")
        String phoneNumber,

        @Schema(description = "Email address of the customer", example = "juan.perez@example.com")
        String email,

        @Schema(description = "Indicates if the customer record is deleted", example = "false")
        Boolean deleted,

        @Schema(description = "Discount percentage applicable to the customer", example = "10")
        Integer discount
){}
