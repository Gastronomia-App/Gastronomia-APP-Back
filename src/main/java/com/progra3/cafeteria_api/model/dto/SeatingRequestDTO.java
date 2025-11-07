package com.progra3.cafeteria_api.model.dto;

import com.progra3.cafeteria_api.model.enums.SeatingShape;
import com.progra3.cafeteria_api.model.enums.SeatingSize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;


@Builder
public record SeatingRequestDTO(

        @Schema(description = "Number of the seating (table or seat)", example = "15", required = true)
        @NotNull(message = "Number cannot be null")
        @PositiveOrZero(message = "Number must be positive or zero")
        @Max(value = 9999, message = "Number must be less than or equal to 9999")
        Integer number,

        @Schema(description = "X position in the board (column)", example = "3", required = true)
        @NotNull(message = "posX cannot be null")
        @PositiveOrZero(message = "posX must be positive or zero")
        Integer posX,

        @Schema(description = "Y position in the board (row)", example = "5", required = true)
        @NotNull(message = "posY cannot be null")
        @PositiveOrZero(message = "posY must be positive or zero")
        Integer posY,

        @Schema(description = "Shape of the seating (SQUARE/ROUND)", example = "SQUARE", required = true)
        @NotNull(message = "Shape cannot be null")
        SeatingShape shape,

        @Schema(description = "Size of the seating (SMALL/MEDIUM/LARGE)", example = "SMALL", required = true)
        @NotNull(message = "Size cannot be null")
        SeatingSize size
) {}