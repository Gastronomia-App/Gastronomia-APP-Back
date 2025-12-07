package com.progra3.cafeteria_api.service.impl.tickets;

import com.progra3.cafeteria_api.model.dto.ticket.Ticket;
import com.progra3.cafeteria_api.model.dto.ticket.TicketContext;
import com.progra3.cafeteria_api.model.dto.ticket.TicketHeader;
import com.progra3.cafeteria_api.model.dto.ticket.TicketTotals;
import com.progra3.cafeteria_api.model.entity.Business;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.enums.TicketType;
import com.progra3.cafeteria_api.security.EmployeeContext;
import com.progra3.cafeteria_api.service.port.tickets.TicketBuilderStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreTicketBuilder implements TicketBuilderStrategy {

    private final EmployeeContext context;
    private final TicketBuilderHelper helper;

    @Override
    public Ticket build(Order order, TicketContext ticketContext) {
        return Ticket.builder()
                .type(TicketType.PRE_TICKET)
                .header(buildHeader(order))
                .items(helper.buildTicketItems(order))
                .totals(buildTotals(order))
                .build();
    }

    private TicketHeader buildHeader(Order order) {
        Business business = context.getCurrentBusiness();
        Integer seatingNumber = order.getSeating() != null ? order.getSeating().getNumber() : null;
        String title = "PRE TICKET";

        return TicketHeader.builder()
                .businessName(business.getName())
                .orderId(order.getId())
                .seatingNumber(seatingNumber)
                .orderType(order.getType())
                .peopleCount(order.getPeopleCount())
                .employeeName(helper.toPersonName(order.getEmployee()))
                .customerName(helper.toPersonName(order.getCustomer()))
                .dateTime(order.getDateTime())
                .title(title)
                .build();
    }

    private TicketTotals buildTotals(Order order) {
        Double subtotal = helper.defaultZero(order.getSubtotal());
        Integer discountPercent = order.getDiscount() != null ? order.getDiscount() : 0;
        double discountAmount = helper.roundToTwoDecimals(subtotal * discountPercent / 100.0);
        Double total = helper.defaultZero(order.getTotal());

        return TicketTotals.builder()
                .subtotal(subtotal)
                .discountPercent(discountPercent)
                .discountAmount(discountAmount)
                .total(total)
                .build();
    }
}
