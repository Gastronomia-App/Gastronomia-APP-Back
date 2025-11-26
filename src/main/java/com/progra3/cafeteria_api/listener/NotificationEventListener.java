package com.progra3.cafeteria_api.listener;

import com.progra3.cafeteria_api.event.*;
import com.progra3.cafeteria_api.model.entity.Audit;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.entity.Product;
import com.progra3.cafeteria_api.model.entity.Seating;
import com.progra3.cafeteria_api.model.enums.NotificationType;
import com.progra3.cafeteria_api.service.impl.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    //AUDIT EVENTS

    @EventListener
    public void onAuditCreated(AuditCreatedEvent event) {
        Audit audit = event.audit();
        Long businessId = audit.getBusiness().getId();

        String message = String.format("Nueva auditoría iniciada a las %s",
                audit.getStartTime().format(TIME_FORMATTER));

        Map<String, Object> data = new HashMap<>();
        data.put("auditId", audit.getId());
        data.put("startTime", audit.getStartTime());
        data.put("initialCash", audit.getInitialCash());

        notificationService.sendNotification(
                NotificationType.AUDIT_CREATED,
                message,
                data,
                businessId
        );

        log.info("Audit created notification sent for audit {} in business {}", audit.getId(), businessId);
    }

    @EventListener
    public void onAuditFinalized(AuditFinalizedEvent event) {
        Audit audit = event.audit();
        Long businessId = audit.getBusiness().getId();

        String message = String.format("Auditoría finalizada. Total: $%.2f", audit.getTotal());

        Map<String, Object> data = new HashMap<>();
        data.put("auditId", audit.getId());
        data.put("closeTime", audit.getCloseTime());
        data.put("total", audit.getTotal());
        data.put("balanceGap", audit.getBalanceGap());

        notificationService.sendNotification(
                NotificationType.AUDIT_FINALIZED,
                message,
                data,
                businessId
        );

        log.info("Audit finalized notification sent for audit {} in business {}", audit.getId(), businessId);
    }

    //ORDER EVENTS

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        Order order = event.order();
        Long businessId = order.getBusiness().getId();

        String seatingInfo = order.getSeating() != null ?
                "Mesa " + order.getSeating().getNumber() :
                "Sin mesa asignada";

        String message = String.format("Nueva orden creada a las %s - %s",
                order.getDateTime().format(TIME_FORMATTER), seatingInfo);

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("orderType", order.getType());
        data.put("seatingNumber", order.getSeating() != null ? order.getSeating().getNumber() : null);
        data.put("createdAt", order.getDateTime());

        notificationService.sendNotification(
                NotificationType.ORDER_CREATED,
                message,
                data,
                businessId
        );

        log.info("Order created notification sent for order {} in business {}", order.getId(), businessId);
    }

    @EventListener
    public void onOrderFinalized(OrderFinalizedEvent event) {
        Order order = event.order();
        Long businessId = order.getBusiness().getId();

        String message = String.format("Orden finalizada a las %s - Total: $%.2f",
                order.getDateTime().format(TIME_FORMATTER), order.getTotal());

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("total", order.getTotal());
        data.put("finalizedAt", order.getDateTime());
        data.put("seatingNumber", order.getSeating() != null ? order.getSeating().getNumber() : null);

        notificationService.sendNotification(
                NotificationType.ORDER_FINALIZED,
                message,
                data,
                businessId
        );

        log.info("Order finalized notification sent for order {} in business {}", order.getId(), businessId);
    }

    //SEATING EVENTS

    @EventListener
    public void onSeatingAllOccupied(SeatingAllOccupiedEvent event) {
        Long businessId = event.businessId();

        String message = "Todas las mesas están ocupadas";

        notificationService.sendNotification(
                NotificationType.SEATING_ALL_OCCUPIED,
                message,
                businessId
        );

        log.info("All seatings occupied notification sent for business {}", businessId);
    }

    @EventListener
    public void onSeatingAvailable(SeatingAvailableEvent event) {
        Seating seating = event.seating();
        Long businessId = event.businessId();

        String message = String.format("Mesa #%d disponible", seating.getNumber());

        Map<String, Object> data = new HashMap<>();
        data.put("seatingId", seating.getId());
        data.put("seatingNumber", seating.getNumber());
        data.put("status", seating.getStatus());

        notificationService.sendNotification(
                NotificationType.SEATING_AVAILABLE,
                message,
                data,
                businessId
        );

        log.info("Seating available notification sent for seating {} in business {}", seating.getId(), businessId);
    }

    //STOCK EVENTS

    @EventListener
    public void onStockLow(StockLowEvent event) {
        Product product = event.product();
        Long businessId = event.businessId();

        String message = String.format("Stock bajo: %s (quedan %d unidades)",
                product.getName(), product.getStock());

        Map<String, Object> data = new HashMap<>();
        data.put("productId", product.getId());
        data.put("productName", product.getName());
        data.put("stock", product.getStock());

        notificationService.sendNotification(
                NotificationType.STOCK_LOW,
                message,
                data,
                businessId
        );

        log.info("Stock low notification sent for product {} in business {}", product.getId(), businessId);
    }

    @EventListener
    public void onStockOut(StockOutEvent event) {
        Product product = event.product();
        Long businessId = event.businessId();

        String message = String.format("Sin stock: %s", product.getName());

        Map<String, Object> data = new HashMap<>();
        data.put("productId", product.getId());
        data.put("productName", product.getName());
        data.put("stock", 0);

        notificationService.sendNotification(
                NotificationType.STOCK_OUT,
                message,
                data,
                businessId
        );

        log.info("Stock out notification sent for product {} in business {}", product.getId(), businessId);
    }
}

