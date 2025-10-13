package com.progra3.cafeteria_api.listener;

import com.progra3.cafeteria_api.event.ExpenseCreatedEvent;
import com.progra3.cafeteria_api.model.entity.Expense;
import com.progra3.cafeteria_api.service.impl.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpenseEventListener {
    private final AuditService auditService;

    @EventListener
    public void onExpenseCreated(ExpenseCreatedEvent event) {
        Expense expense = event.expense();

        auditService.getInProgressAudit().ifPresent(audit -> {
            expense.setAudit(audit);
            audit.getExpenses().add(expense);
            auditService.recalculateAudit(audit);
        });
    }
}
