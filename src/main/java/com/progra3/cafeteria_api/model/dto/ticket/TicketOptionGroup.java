package com.progra3.cafeteria_api.model.dto.ticket;

import lombok.Builder;

import java.util.List;

/**
 * TicketOptionGroup groups options under a logical label
 * (e.g. "Cafes", "Cookies").
 */
@Builder
public record TicketOptionGroup(

        /**
         * Group name to display (e.g. "Cafes").
         */
        String groupName,

        /**
         * Options belonging to this group.
         */
        List<TicketOptionLine> options
) {
}
