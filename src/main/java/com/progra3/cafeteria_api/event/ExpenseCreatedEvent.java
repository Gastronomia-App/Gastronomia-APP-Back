package com.progra3.cafeteria_api.event;

import com.progra3.cafeteria_api.model.entity.Expense;

public record ExpenseCreatedEvent(Expense expense) {
}
