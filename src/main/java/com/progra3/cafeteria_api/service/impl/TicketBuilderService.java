package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.model.entity.Business;
import com.progra3.cafeteria_api.model.entity.Customer;
import com.progra3.cafeteria_api.model.entity.Employee;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.entity.Person;
import com.progra3.cafeteria_api.model.entity.ProductOption;
import com.progra3.cafeteria_api.model.entity.Seating;
import com.progra3.cafeteria_api.model.entity.SelectedOption;
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

import java.util.ArrayList;
import java.util.List;

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
            case KITCHEN -> buildKitchenItems(order.getItems());
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

    @Override
    public Ticket buildKitchenTicket(Order order, List<Item> items) {
        TicketHeader header = buildHeader(order, TicketType.KITCHEN);
        List<TicketItem> ticketItems = buildKitchenItems(items);

        return Ticket.builder()
                .type(TicketType.KITCHEN)
                .header(header)
                .items(ticketItems)
                .totals(null)
                .build();
    }

    @Override
    public Ticket buildKitchenTicketForItems(Order order, List<Long> itemIds) {
        List<Item> itemsToPrint = filterItemsByIds(order, itemIds);
        return buildKitchenTicket(order, itemsToPrint);
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

        if (business != null) {
            businessName = business.getName();
            businessCuit = business.getCuit();
        }

        String employeeName = toPersonName(employee);
        String customerName = toPersonName(customer);

        Integer seatingNumber = seating != null ? seating.getNumber() : null;

        String title = switch (type) {
            case KITCHEN -> null;
            case BILL -> "CUENTA / CONSUMO";
            case PAYMENT -> "COMPROBANTE DE PAGO";
        };

        return TicketHeader.builder()
                .businessName(businessName)
                .businessCuit(businessCuit)
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

        if (nameBlank && lastNameBlank) {
            return null;
        }

        if (nameBlank) {
            return lastName;
        }
        if (lastNameBlank) {
            return name;
        }

        return name + " " + lastName;
    }

    // ===========================
    // Items for KITCHEN ticket
    // ===========================

    private List<TicketItem> buildKitchenItems(List<Item> items) {
        List<TicketItem> result = new ArrayList<>();
        if (items == null) {
            return result;
        }

        for (Item item : items) {
            if (item == null || Boolean.TRUE.equals(item.getDeleted())) {
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

    /**
     * Filters items of the order by the given IDs, excluding logically deleted ones.
     */
    private List<Item> filterItemsByIds(Order order, List<Long> itemIds) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()
                || itemIds == null || itemIds.isEmpty()) {
            return List.of();
        }

        return order.getItems().stream()
                .filter(item -> itemIds.contains(item.getId()))
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .toList();
    }

    /**
     * Builds a single flattened group of options for an item, traversing
     * all levels of SelectedOption (nested extras).
     */
    private List<TicketOptionGroup> buildOptionGroups(List<SelectedOption> rootOptions) {
        if (rootOptions == null || rootOptions.isEmpty()) {
            return List.of();
        }

        List<TicketOptionLine> lines = new ArrayList<>();

        // Level 1 for options directly attached to the item
        for (SelectedOption root : rootOptions) {
            collectOptionLines(root, 1, lines);
        }

        TicketOptionGroup group = TicketOptionGroup.builder()
                .groupName("Opciones")
                .options(lines)
                .build();

        return List.of(group);
    }

    /**
     * Recursively flattens the SelectedOption tree into a list of TicketOptionLine.
     * We do not expose "level" in the model for now, only use it internally if needed.
     */
    /**
     * Recursively flattens the SelectedOption tree into a list of TicketOptionLine,
     * preserving the nesting level for proper indentation in the ticket.
     */
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
                .level(level)   // <-- key: store the nesting level
                .build());

        if (option.getSelectedOptions() != null) {
            for (SelectedOption child : option.getSelectedOptions()) {
                // Children use level + 1 to support infinite nesting
                collectOptionLines(child, level + 1, acc);
            }
        }
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
