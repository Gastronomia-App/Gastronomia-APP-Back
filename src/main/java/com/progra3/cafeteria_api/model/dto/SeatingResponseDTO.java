package com.progra3.cafeteria_api.model.dto;

import com.progra3.cafeteria_api.model.enums.SeatingShape;
import com.progra3.cafeteria_api.model.enums.SeatingSize;
import com.progra3.cafeteria_api.model.enums.SeatingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record SeatingResponseDTO(

        @Schema(description = "Unique identifier of the seating", example = "1")
        Long id,

        @Schema(description = "Number assigned to the seating (table or seat)", example = "15")
        Integer number,

        @Schema(description = "X position in the board (column)", example = "3")
        Integer posX,

        @Schema(description = "Y position in the board (row)", example = "5")
        Integer posY,

        @Schema(description = "Shape of the seating", example = "SQUARE")
        SeatingShape shape,

        @Schema(description = "Size of the seating", example = "SMALL")
        SeatingSize size,

        @Schema(description = "Current status of the seating", example = "FREE")
        SeatingStatus status,

        @Schema(description = "Indicates if the seating is logically deleted", example = "false")
        Boolean deleted

) {}
