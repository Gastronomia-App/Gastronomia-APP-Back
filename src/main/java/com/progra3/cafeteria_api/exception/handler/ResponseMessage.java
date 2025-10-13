package com.progra3.cafeteria_api.exception.handler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Response message containing error details")
public class ResponseMessage {

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Error message", example = "Category not found")
    private String message;

    @Schema(description = "Timestamp of the error", example = "2025-05-09T12:00:00")
    private LocalDateTime timestamp;
}