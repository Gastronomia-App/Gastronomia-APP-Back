package com.progra3.cafeteria_api.model.dto;

import com.progra3.cafeteria_api.model.enums.SeatingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record SeatingResponseDTO(

        @Schema(description = "Unique identifier of the seating", example = "1")
        Long id,

        @Schema(description = "Number assigned to the seating (table or seat)", example = "15")
        Integer number,

        @Schema(description = "Current status of the seating", example = "AVAILABLE")
        SeatingStatus status,

        @Schema(description = "Indicates if the seating is logically deleted", example = "false")
        Boolean deleted
) {}
