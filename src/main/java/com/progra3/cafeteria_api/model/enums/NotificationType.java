package com.progra3.cafeteria_api.model.enums;

public enum NotificationType {
    // Audit notifications
    AUDIT_CREATED,
    AUDIT_FINALIZED,

    // Order notifications
    ORDER_CREATED,
    ORDER_FINALIZED,

    // Seating notifications
    SEATING_ALL_OCCUPIED,
    SEATING_AVAILABLE,

    // Stock notifications
    STOCK_LOW,
    STOCK_OUT
}
