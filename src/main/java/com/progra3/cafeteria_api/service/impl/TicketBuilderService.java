package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.model.entity.Business;
import com.progra3.cafeteria_api.model.entity.Customer;
import com.progra3.cafeteria_api.model.entity.Employee;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.entity.ProductOption;
import com.progra3.cafeteria_api.model.entity.Seating;
import com.progra3.cafeteria_api.model.entity.SelectedOption;
import com.progra3.cafeteria_api.model.entity.Person;
import com.progra3.cafeteria_api.model.enums.TicketType;
import com.progra3.cafeteria_api.model.ticket.Ticket;
import com.progra3.cafeteria_api.model.ticket.TicketHeader;
import com.progra3.cafeteria_api.model.ticket.TicketItem;
import com.progra3.cafeteria_api.model.ticket.TicketOptionGroup;
import com.progra3.cafeteria_api.model.ticket.TicketOptionLine;
import com.progra3.cafeteria_api.model.ticket.TicketTotals;
import com.progra3.cafeteria_api.service.port.ITicketBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * TicketBuilderService converts Order entities into Ticket models
 * that can later be rendered as PDF tickets.
 */
@Service
@RequiredArgsConstructor
public class TicketBuilderService implements ITicketBuilderService {

    @Override
    public Ticket buildTicket(Order order, TicketType type) {
        TicketHeader header = buildHeader(order, type);

        List<TicketItem> items = switch (type) {
            case KITCHEN -> buildKitchenItems(order);
            case BILL, PAYMENT -> buildBillingItems(order);
        };

        TicketTotals totals = switch (type) {
            case KITCHEN -> null;
            case BILL, PAYMENT -> buildTotals(order);
        };

        return Ticket.builder()
                .type(type)
                .header(header)
                .items(items)
                .totals(totals)
                .build();
    }

    // ===========================
    // Header
    // ===========================

    private TicketHeader buildHeader(Order order, TicketType type) {
        Business business = order.getBusiness();
        Seating seating = order.getSeating();
        Employee employee = order.getEmployee();
        Customer customer = order.getCustomer();

        String businessName = null;
        String businessCuit = null;

        // For now we only map basic business data (name, cuit).
        if (business != null) {
            businessName = business.getName();   // Ajustá si el campo se llama distinto
            businessCuit = business.getCuit();   // Idem
        }

        String employeeName = toPersonName(employee);
        String customerName = toPersonName(customer); // If null, renderer will skip "Cliente:"

        Integer seatingNumber = seating != null ? seating.getNumber() : null;

        String title = switch (type) {
            case KITCHEN -> null;
            case BILL -> "CUENTA / CONSUMO";
            case PAYMENT -> "COMPROBANTE DE PAGO";
        };

        return TicketHeader.builder()
                .businessName(businessName)
                .businessCuit(businessCuit)
                // businessAddress and businessPhone will remain null for now
                .orderId(order.getId())
                .seatingNumber(seatingNumber)
                .orderType(order.getType())
                .peopleCount(order.getPeopleCount())
                .employeeName(employeeName)
                .customerName(customerName)
                .dateTime(order.getDateTime())
                .title(title)
                .build();
    }

    private String toPersonName(Person person) {
        if (person == null) {
            return null;
        }

        String name = person.getName();
        String lastName = person.getLastName();

        boolean nameBlank = (name == null || name.isBlank());
        boolean lastNameBlank = (lastName == null || lastName.isBlank());

        // If both are null/blank, we do not print anything
        if (nameBlank && lastNameBlank) {
            return null;
        }

        // If only one is present, return that one
        if (nameBlank) {
            return lastName;
        }
        if (lastNameBlank) {
            return name;
        }

        // Both present → "Nombre Apellido"
        return name + " " + lastName;
    }

    // ===========================
    // Items for KITCHEN ticket
    // ===========================

    private List<TicketItem> buildKitchenItems(Order order) {
        List<TicketItem> result = new ArrayList<>();

        for (Item item : order.getItems()) {
            if (Boolean.TRUE.equals(item.getDeleted())) {
                continue;
            }

            List<TicketOptionGroup> optionGroups = buildOptionGroups(item.getSelectedOptions());
            String comment = normalizeBlank(item.getComment());

            TicketItem ticketItem = TicketItem.builder()
                    .quantity(item.getQuantity())
                    .productName(item.getProduct().getName())
                    .unitPrice(null)      // Prices are not shown in kitchen tickets
                    .lineTotal(null)
                    .optionGroups(optionGroups)
                    .comment(comment)     // Printed below each item if not null
                    .build();

            result.add(ticketItem);
        }

        return result;
    }

    private List<TicketOptionGroup> buildOptionGroups(List<SelectedOption> selectedOptions) {
        if (selectedOptions == null || selectedOptions.isEmpty()) {
            return List.of();
        }

        // Group options by product group name, preserving insertion order
        Map<String, List<SelectedOption>> grouped = new LinkedHashMap<>();

        for (SelectedOption selected : selectedOptions) {
            if (selected == null) {
                continue;
            }
            ProductOption productOption = selected.getProductOption();
            if (productOption == null) {
                continue;
            }

            String groupName;
            if (productOption.getProductGroup() != null) {
                groupName = productOption.getProductGroup().getName();
            } else {
                groupName = "Opciones"; // Fallback label if no group exists
            }

            grouped.computeIfAbsent(groupName, k -> new ArrayList<>()).add(selected);
        }

        List<TicketOptionGroup> groups = new ArrayList<>();

        for (Map.Entry<String, List<SelectedOption>> entry : grouped.entrySet()) {
            String groupName = entry.getKey();
            List<SelectedOption> optionsInGroup = entry.getValue();

            List<TicketOptionLine> optionLines = new ArrayList<>();

            for (SelectedOption so : optionsInGroup) {
                ProductOption productOption = so.getProductOption();
                String name = productOption.getProduct().getName();
                Integer quantity = so.getQuantity() != null ? so.getQuantity() : 1;

                optionLines.add(TicketOptionLine.builder()
                        .quantity(quantity)
                        .name(name)
                        .build());
            }

            groups.add(TicketOptionGroup.builder()
                    .groupName(groupName)
                    .options(optionLines)
                    .build());
        }

        return groups;
    }

    // ===========================
    // Items for BILL / PAYMENT
    // ===========================

    private List<TicketItem> buildBillingItems(Order order) {
        List<TicketItem> result = new ArrayList<>();

        for (Item item : order.getItems()) {
            if (Boolean.TRUE.equals(item.getDeleted())) {
                continue;
            }

            TicketItem ticketItem = TicketItem.builder()
                    .quantity(item.getQuantity())
                    .productName(item.getProduct().getName())
                    .unitPrice(item.getUnitPrice())
                    .lineTotal(item.getTotalPrice())
                    .optionGroups(List.of()) // Options are not shown in billing tickets for now
                    .comment(null)           // Comments not shown in billing tickets
                    .build();

            result.add(ticketItem);
        }

        return result;
    }

    // ===========================
    // Totals (BILL / PAYMENT)
    // ===========================

    private TicketTotals buildTotals(Order order) {
        Double subtotal = defaultZero(order.getSubtotal());
        Integer discountPercent = order.getDiscount() != null ? order.getDiscount() : 0;

        double discountAmount = subtotal * discountPercent / 100.0;
        discountAmount = roundToTwoDecimals(discountAmount);

        Double total = defaultZero(order.getTotal());

        boolean printInvoiceWarning = true; // For now we always show "Este documento no es factura."

        return TicketTotals.builder()
                .subtotal(subtotal)
                .discountPercent(discountPercent)
                .discountAmount(discountAmount)
                .total(total)
                .printInvoiceWarning(printInvoiceWarning)
                .build();
    }

    // ===========================
    // Helpers
    // ===========================

    /**
     * Normalizes blank strings to null to simplify conditional rendering.
     */
    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
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
