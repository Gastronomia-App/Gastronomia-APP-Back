package com.progra3.cafeteria_api.service.impl.tickets;

import com.progra3.cafeteria_api.model.enums.TicketType;
import com.progra3.cafeteria_api.service.port.tickets.TicketBuilderStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketBuilderFactory {

    private final PreTicketBuilder preTicketBuilder;
    private final FiscalTicketBuilder fiscalTicketBuilder;
    private final KitchenTicketBuilder kitchenTicketBuilder;

    public TicketBuilderStrategy getStrategy(TicketType type) {
        return switch (type) {
            case PRE_TICKET -> preTicketBuilder;
            case FISCAL_TICKET -> fiscalTicketBuilder;
            case KITCHEN_TICKET -> kitchenTicketBuilder;
        };
    }
}