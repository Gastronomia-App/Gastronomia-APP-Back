package com.progra3.cafeteria_api.listener;

import com.progra3.cafeteria_api.event.OrderCreatedEvent;
import com.progra3.cafeteria_api.event.OrderFinalizedEvent;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.service.impl.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final AuditService auditService;

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        // Las órdenes NO se asocian al audit cuando se crean
        // Se asociarán cuando se finalicen dentro del período del audit
    }

    @EventListener
    public void onOrderFinalized(OrderFinalizedEvent event) {
        Order order = event.order();

        auditService.getInProgressAudit().ifPresent(audit -> {
            // Verificar si la orden se finalizó dentro del período del audit
            if (order.getEndDateTime() != null &&
                !order.getEndDateTime().isBefore(audit.getStartTime())) {

                // Asociar la orden al audit
                order.setAudit(audit);
                if (!audit.getOrders().contains(order)) {
                    audit.getOrders().add(order);
                }

                // Recalcular el audit
                auditService.recalculateAudit(audit);
            }
        });
    }
}