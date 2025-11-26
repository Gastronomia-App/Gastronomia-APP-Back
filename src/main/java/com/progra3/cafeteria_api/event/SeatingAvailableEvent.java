package com.progra3.cafeteria_api.event;

import com.progra3.cafeteria_api.model.entity.Seating;

public record SeatingAvailableEvent(Seating seating, Long businessId) {
}

