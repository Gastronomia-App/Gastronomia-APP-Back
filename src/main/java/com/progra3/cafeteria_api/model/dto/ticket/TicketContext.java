package com.progra3.cafeteria_api.model.dto.ticket;

import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.enums.DocumentType;
import com.progra3.cafeteria_api.model.enums.InvoiceType;
import com.progra3.cafeteria_api.model.enums.IvaCondition;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record TicketContext(
        // Fiscal context for the ticket
        InvoiceType invoiceType,
        Integer puntoVenta,
        Long cbteNumero,
        String concept,
        String cae,
        LocalDate caeExpiration,
        IvaCondition customerIvaCondition,
        DocumentType documentType,
        Long documentNumber,
        String customerName,
        String customerAddress,

        // Kitchen context for the ticket
        List<Item> kitchenItems
) {
}
