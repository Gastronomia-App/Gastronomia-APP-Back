package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
public record CategoryRequestDTO(
        @Schema(description = "Name of the category",
                example = "Beverages",
                required = true,
                minLength = 1,
                maxLength = 50)
        @NotBlank(message = "Name cannot be null")
        @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
        String name
) { }
