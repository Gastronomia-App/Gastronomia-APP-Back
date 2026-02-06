package com.progra3.cafeteria_api.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SelectedOptionTransferRequestDTO(
        @NotNull Long selectedOptionId,
        @Min(1) Integer quantity,

        List<SelectedOptionTransferRequestDTO> optionsToMove
) {}