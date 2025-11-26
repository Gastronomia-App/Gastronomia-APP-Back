package com.progra3.cafeteria_api.model.dto;

import com.progra3.cafeteria_api.model.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponseDTO(
        NotificationType type,
        String message,
        LocalDateTime timestamp,
        Object data,
        Long businessId
) {
}
