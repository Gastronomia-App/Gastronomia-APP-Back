package com.progra3.cafeteria_api.exception.audit;

public class AuditModificationNotAllowedException extends RuntimeException {
    public AuditModificationNotAllowedException(Long id, String status) {
        super("The audit with ID " + id + " is already " + status + " and cannot be modified.");
    }
}
