package com.progra3.cafeteria_api.service.impl.tickets;

import com.progra3.cafeteria_api.model.dto.ticket.*;
import com.progra3.cafeteria_api.model.entity.*;
import com.progra3.cafeteria_api.model.enums.TicketType;
import com.progra3.cafeteria_api.service.port.tickets.ITicketBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for building Ticket
 */
@Service
@RequiredArgsConstructor
public class TicketBuilderService implements ITicketBuilderService {

    private final TicketBuilderFactory factory;

    @Override
    public Ticket build(TicketType type, Order order, TicketContext context) {
        return factory.getStrategy(type).build(order, context);
    }
}
