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
        Order order = event.order();

        auditService.getInProgressAudit().ifPresent(audit -> {
            order.setAudit(audit);
            audit.getOrders().add(order);
        });
    }

    @EventListener
    public void onOrderFinalized(OrderFinalizedEvent event) {
        auditService.getInProgressAudit().ifPresent(auditService::recalculateAudit);
    }
}