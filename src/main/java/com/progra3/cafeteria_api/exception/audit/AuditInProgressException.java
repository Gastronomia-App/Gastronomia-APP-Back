package com.progra3.cafeteria_api.exception.audit;

public class AuditInProgressException extends RuntimeException {
    public AuditInProgressException(){super("An audit is currently in progress");}
}
