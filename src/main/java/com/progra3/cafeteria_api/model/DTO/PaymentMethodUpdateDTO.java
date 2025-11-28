package com.progra3.cafeteria_api.model.dto;

import jakarta.validation.constraints.Size;

public record PaymentMethodUpdateDTO(
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}

