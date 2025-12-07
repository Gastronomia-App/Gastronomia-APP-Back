package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.model.dto.ticket.*;
import com.progra3.cafeteria_api.model.entity.*;
import com.progra3.cafeteria_api.model.enums.TicketType;
import com.progra3.cafeteria_api.security.EmployeeContext;
import com.progra3.cafeteria_api.service.port.ITicketBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for building Ticket DTOs from Order entities.
 * Supports pre-tickets, fiscal tickets, and kitchen tickets.
 */
@Service
@RequiredArgsConstructor
public class TicketBuilderService implements ITicketBuilderService {

    private final EmployeeContext context;

    // ===========================
    // Public API
    // ===========================

    @Override
    public Ticket buildPreTicket(Order order) {
        return Ticket.builder()
                .type(TicketType.PRE_TICKET)
                .header(buildHeader(order, TicketType.PRE_TICKET, null))
                .items(buildBillItems(order))
                .totals(buildTotals(order, null))
                .build();
    }

    @Override
    public Ticket buildFiscalTicket(Order order, String cae) {
        return Ticket.builder()
                .type(TicketType.FISCAL_TICKET)
                .header(buildHeader(order, TicketType.FISCAL_TICKET, cae))
                .items(buildBillItems(order))
                .totals(buildTotals(order, cae))
                .build();
    }

    @Override
    public Ticket buildKitchenTicket(Order order, List<Item> items) {
        return Ticket.builder()
                .type(TicketType.KITCHEN_TICKET)
                .header(buildHeader(order, TicketType.KITCHEN_TICKET, null))
                .items(buildKitchenItems(items))
                .totals(null)
                .build();
    }

    // ===========================
    // Header Building
    // ===========================

    private TicketHeader buildHeader(Order order, TicketType type, String cae) {
        String businessName = context.getCurrentBusiness().getName();
        Long businessCuit = context.getCurrentBusiness().getCuit();
        Integer seatingNumber = order.getSeating() != null ? order.getSeating().getNumber() : null;

        String title = switch (type) {
            case KITCHEN_TICKET -> null;
            case PRE_TICKET -> "PRE TICKET";
            case FISCAL_TICKET -> "FACTURA ELECTRÓNICA";
        };

        return TicketHeader.builder()
                .businessName(businessName)
                .businessCuit(businessCuit)
                .orderId(order.getId())
                .seatingNumber(seatingNumber)
                .orderType(order.getType())
                .peopleCount(order.getPeopleCount())
                .employeeName(toPersonName(order.getEmployee()))
                .customerName(toPersonName(order.getCustomer()))
                .dateTime(order.getDateTime())
                .title(title)
                .cae(cae)
                .build();
    }

    // ===========================
    // Items Building
    // ===========================

    private List<TicketItem> buildBillItems(Order order) {
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

    private List<TicketItem> buildKitchenItems(List<Item> items) {
        if (items == null) {
            return List.of();
        }

        return items.stream()
                .filter(item -> item != null && !Boolean.TRUE.equals(item.getDeleted()))
                .map(item -> TicketItem.builder()
                        .quantity(item.getQuantity())
                        .productName(item.getProduct().getName())
                        .unitPrice(null)
                        .lineTotal(null)
                        .optionGroups(buildOptionGroups(item.getSelectedOptions()))
                        .comment(normalizeBlank(item.getComment()))
                        .build())
                .toList();
    }

    // ===========================
    // Option Groups Building
    // ===========================

    private List<TicketOptionGroup> buildOptionGroups(List<SelectedOption> rootOptions) {
        if (rootOptions == null || rootOptions.isEmpty()) {
            return List.of();
        }

        List<TicketOptionLine> lines = new ArrayList<>();
        rootOptions.forEach(option -> collectOptionLines(option, 1, lines));

        return List.of(TicketOptionGroup.builder()
                .groupName("Opciones")
                .options(lines)
                .build());
    }

    private void collectOptionLines(SelectedOption option, int level, List<TicketOptionLine> acc) {
        if (option == null || option.getProductOption() == null) {
            return;
        }

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

    // ===========================
    // Totals Building
    // ===========================

    private TicketTotals buildTotals(Order order, String cae) {
        Double subtotal = defaultZero(order.getSubtotal());
        Integer discountPercent = order.getDiscount() != null ? order.getDiscount() : 0;
        double discountAmount = roundToTwoDecimals(subtotal * discountPercent / 100.0);
        Double total = defaultZero(order.getTotal());

        return TicketTotals.builder()
                .subtotal(subtotal)
                .discountPercent(discountPercent)
                .discountAmount(discountAmount)
                .total(total)
                .cae(cae)
                .printInvoiceWarning(cae == null)
                .build();
    }

    // ===========================
    // Helper Methods
    // ===========================

    private String toPersonName(Person person) {
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

    private String normalizeBlank(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Double defaultZero(Double value) {
        return value != null ? value : 0.0;
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
