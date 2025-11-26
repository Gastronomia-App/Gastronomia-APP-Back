package com.progra3.cafeteria_api.event;

import com.progra3.cafeteria_api.model.entity.Audit;

public record AuditFinalizedEvent(Audit audit) {
}

