package com.progra3.cafeteria_api.service.port.tickets;

import com.progra3.cafeteria_api.model.dto.ticket.Ticket;

/**
 * ITicketPdfService generates a PDF representation for a given Ticket model.
 */
public interface ITicketPdfService {

    /**
     * Generates a PDF document for the given ticket.
     *
     * @param ticket ticket data to be printed
     * @return PDF bytes
     */
    byte[] generateTicketPdf(Ticket ticket);

}