package com.progra3.cafeteria_api.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

@Builder
public record SeatingPositionRequestDTO(

        @Schema(description = "X position in the board (column)", example = "3", required = true)
        @NotNull(message = "posX cannot be null")
        @PositiveOrZero(message = "posX must be positive or zero")
        Integer posX,

        @Schema(description = "Y position in the board (row)", example = "5", required = true)
        @NotNull(message = "posY cannot be null")
        @PositiveOrZero(message = "posY must be positive or zero")
        Integer posY
) {}
