package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderSplitRequestDTO(

        @Schema(description = "New order where items will be transferred to")
        @Valid
        @NotNull
        OrderRequestDTO destinationOrder,

        @Schema(description = "List of items and quantities to move to the new order")
        @Valid
        @NotNull
        List<ItemTransferRequestDTO> itemsToMove

) { }
