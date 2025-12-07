package com.progra3.cafeteria_api.service.impl.tickets;

import com.progra3.cafeteria_api.model.dto.ticket.*;
import com.progra3.cafeteria_api.model.entity.*;
import com.progra3.cafeteria_api.model.enums.TicketType;
import com.progra3.cafeteria_api.service.port.tickets.TicketBuilderStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KitchenTicketBuilder implements TicketBuilderStrategy {

    private final TicketBuilderHelper helper;

    @Override
    public Ticket build(Order order, TicketContext ticketContext) {
        return Ticket.builder()
                .type(TicketType.KITCHEN_TICKET)
                .header(buildHeader(order))
                .items(buildKitchenItems(ticketContext.kitchenItems()))
                .totals(null)
                .build();
    }

    private TicketHeader buildHeader(Order order) {
        Integer seatingNumber = order.getSeating() != null ? order.getSeating().getNumber() : null;

        String title = "COCINA";

        return TicketHeader.builder()
                .orderId(order.getId())
                .seatingNumber(seatingNumber)
                .orderType(order.getType())
                .peopleCount(order.getPeopleCount())
                .employeeName(helper.toPersonName(order.getEmployee()))
                .dateTime(order.getDateTime())
                .title(title)
                .build();
    }

    private List<TicketItem> buildKitchenItems(List<Item> items) {
        if (items == null) return List.of();

        return items.stream()
                .filter(item -> item != null && !Boolean.TRUE.equals(item.getDeleted()))
                .map(item -> TicketItem.builder()
                        .quantity(item.getQuantity())
                        .productName(item.getProduct().getName())
                        .unitPrice(null)
                        .lineTotal(null)
                        .optionGroups(buildOptionGroups(item.getSelectedOptions()))
                        .comment(helper.normalizeBlank(item.getComment()))
                        .build())
                .toList();
    }

    private List<TicketOptionGroup> buildOptionGroups(List<SelectedOption> rootOptions) {
        if (rootOptions == null || rootOptions.isEmpty()) return List.of();

        List<TicketOptionLine> lines = new ArrayList<>();
        rootOptions.forEach(option -> collectOptionLines(option, 1, lines));

        return List.of(TicketOptionGroup.builder()
                .groupName("Opciones")
                .options(lines)
                .build());
    }

    private void collectOptionLines(SelectedOption option, int level, List<TicketOptionLine> acc) {
        if (option == null || option.getProductOption() == null) return;

        ProductOption productOption = option.getProductOption();
        String name = productOption.getProduct() != null
                ? productOption.getProduct().getName()
                : "(Sin nombre)";
        int quantity = option.getQuantity() != null ? option.getQuantity() : 1;

        acc.add(TicketOptionLine.builder()
                .quantity(quantity)
                .name(name)
                .level(level)
                .build());

        if (option.getSelectedOptions() != null) {
            option.getSelectedOptions().forEach(child -> collectOptionLines(child, level + 1, acc));
        }
    }

}
