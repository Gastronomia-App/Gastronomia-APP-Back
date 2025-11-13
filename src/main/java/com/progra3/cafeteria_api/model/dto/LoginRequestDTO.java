package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record LoginRequestDTO(
        @NotBlank(message = "Username is required")
        String username,

        @Schema(description = "User password for authentication", example = "MySecret123")
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
        String password

) {}
