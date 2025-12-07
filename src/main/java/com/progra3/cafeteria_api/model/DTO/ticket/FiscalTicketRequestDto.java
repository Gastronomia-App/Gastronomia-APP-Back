package com.progra3.cafeteria_api.model.dto.ticket;

import com.progra3.cafeteria_api.model.enums.DocumentType;
import com.progra3.cafeteria_api.model.enums.InvoiceType;
import com.progra3.cafeteria_api.model.enums.IvaCondition;

public record FiscalTicketRequestDTO(
        InvoiceType invoiceType,
        IvaCondition ivaCondition,
        DocumentType documentType,
        Long documentNumber,
        String customerName,
        String customerAddress
) {
}