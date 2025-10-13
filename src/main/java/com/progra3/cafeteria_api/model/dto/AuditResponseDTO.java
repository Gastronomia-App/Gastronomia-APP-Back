package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AuditResponseDTO(
        @Schema(description = "Unique identifier of the audit", example = "1")
        Long id,

        @Schema(description = "Start time of the audit", example = "2025-06-14T10:15:30")
        LocalDateTime startTime,

        @Schema(description = "Closing time of the audit", example = "2025-06-14T18:00:00")
        LocalDateTime closeTime,

        @Schema(description = "Initial cash amount at the start of the audit", example = "1000.0")
        Double initialCash,

        @Schema(description = "List of orders included in the audit")
        List<OrderResponseDTO> orders,

        @Schema(description = "List of expenses included in the audit")
        List<ExpenseResponseDTO> expenses,

        @Schema(description = "Status of the audit", example = "OPEN")
        String auditStatus,

        @Schema(description = "Total amount expensed during the audit", example = "500.0")
        Double totalExpensed,

        @Schema(description = "Total amount accounted during the audit", example = "1500.0")
        Double total,

        @Schema(description = "Difference or gap in the balance", example = "0.0")
        Double balanceGap,

        @Schema(description = "Logical deletion flag", example = "false")
        Boolean deleted
) {}
