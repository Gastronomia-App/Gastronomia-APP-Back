package com.progra3.cafeteria_api.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AuditFinalizeRequestDTO(
        @NotNull(message = "Final cash cannot be null")
        @PositiveOrZero(message = "Final cash cannot be negative")
        Double realCash
) {
}
