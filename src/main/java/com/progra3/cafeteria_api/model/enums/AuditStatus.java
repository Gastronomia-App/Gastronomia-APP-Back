package com.progra3.cafeteria_api.model.enums;

import lombok.*;

@Getter
@RequiredArgsConstructor
public enum AuditStatus {
    IN_PROGRESS("in progress"),
    FINALIZED("finalized"),
    CANCELED("canceled");

    private final String name;
}
