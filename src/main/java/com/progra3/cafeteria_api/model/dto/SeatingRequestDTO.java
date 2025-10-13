package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record SeatingRequestDTO(

        @Schema(description = "Number of the seating (table or seat)", example = "15", required = true)
        @NotNull(message = "Number cannot be null")
        @PositiveOrZero(message = "Number must be positive or zero")
        Integer number

) {}
