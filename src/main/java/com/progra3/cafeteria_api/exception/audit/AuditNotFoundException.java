package com.progra3.cafeteria_api.exception.audit;

import java.util.function.Supplier;

public class AuditNotFoundException extends RuntimeException {
    public AuditNotFoundException(Long id) {
        super("No audit found for ID " + id + " for the current business.");
    }

    public AuditNotFoundException() {
        super("No audit found for the current business.");
    }
}
