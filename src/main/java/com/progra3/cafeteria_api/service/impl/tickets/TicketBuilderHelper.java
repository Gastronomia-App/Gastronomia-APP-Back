package com.progra3.cafeteria_api.service.impl.tickets;

import com.progra3.cafeteria_api.model.dto.ticket.TicketItem;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.entity.Person;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TicketBuilderHelper {

    public List<TicketItem> buildTicketItems(Order order) {
        if (order.getItems() == null) {
            return List.of();
        }

        return order.getItems().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .map(item -> TicketItem.builder()
                        .quantity(item.getQuantity())
                        .productName(item.getProduct().getName())
                        .unitPrice(item.getUnitPrice())
                        .lineTotal(item.getTotalPrice())
                        .optionGroups(List.of())
                        .comment(null)
                        .build())
                .toList();
    }

    public String toPersonName(Person person) {
        if (person == null) {
            return null;
        }

        String name = person.getName();
        String lastName = person.getLastName();
        boolean nameBlank = (name == null || name.isBlank());
        boolean lastNameBlank = (lastName == null || lastName.isBlank());

        if (nameBlank && lastNameBlank) return null;
        if (nameBlank) return lastName;
        if (lastNameBlank) return name;

        return name + " " + lastName;
    }

    public String normalizeBlank(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public Double defaultZero(Double value) {
        return value != null ? value : 0.0;
    }

    public double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
