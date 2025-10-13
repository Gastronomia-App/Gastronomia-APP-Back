package com.progra3.cafeteria_api.model.dto;

import lombok.Builder;

@Builder
public record LoginResponseDTO(
        String token,
        EmployeeResponseDTO employee
) {
}