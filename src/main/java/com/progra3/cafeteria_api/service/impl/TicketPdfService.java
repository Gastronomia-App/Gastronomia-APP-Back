package com.progra3.cafeteria_api.service.impl;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.DashedLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.progra3.cafeteria_api.model.enums.TicketType;
import com.progra3.cafeteria_api.model.ticket.Ticket;
import com.progra3.cafeteria_api.model.ticket.TicketHeader;
import com.progra3.cafeteria_api.model.ticket.TicketItem;
import com.progra3.cafeteria_api.model.ticket.TicketOptionGroup;
import com.progra3.cafeteria_api.model.ticket.TicketOptionLine;
import com.progra3.cafeteria_api.model.ticket.TicketTotals;
import com.progra3.cafeteria_api.service.port.ITicketPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Renders Ticket as a thermal-style PDF receipt (about 80mm width).
 */
@Service
@RequiredArgsConstructor
public class TicketPdfService implements ITicketPdfService {

    // Thermal width (~80mm)
    private static final float TICKET_WIDTH = 226f;

    // Dynamic height configuration (más ajustado para reducir el blanco)
    private static final float MIN_TICKET_HEIGHT = 260f;
    private static final float MAX_TICKET_HEIGHT = 2000f;  // máximo de seguridad

    // Estimación de alto:
    //  - BASE_HEIGHT: header + separadores + bloque de totales
    //  - ITEM_BLOCK_HEIGHT: cada fila de item (+ pequeño margen)
    //  - OPTION_LINE_HEIGHT: cada sub-opción
    private static final float BASE_HEIGHT = 230f;      // un poco menos
    private static final float ITEM_BLOCK_HEIGHT = 24f; // ANTES: 36f
    private static final float OPTION_LINE_HEIGHT = 12f; // ANTES: 18f

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] generateTicketPdf(Ticket ticket) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);

            // Calculate dynamic height based on ticket content
            float ticketHeight = calculateTicketHeight(ticket);
            Document doc = new Document(pdf, new PageSize(TICKET_WIDTH, ticketHeight));

            // Small margins to use as much paper as possible
            doc.setMargins(10, 10, 10, 10);

            // Fonts
            PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            doc.setFont(fontRegular);
            doc.setFontSize(9f);

            addHeader(doc, ticket, fontBold);
            addBody(doc, ticket, fontBold);
            addTotals(doc, ticket, fontBold);

            doc.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Error generating ticket PDF", e);
        }
    }

    /**
     * Estimate the page height based on the amount of content.
     * We try to be as close as possible to the real content height.
     */
    private float calculateTicketHeight(Ticket ticket) {
        int itemsCount = 0;
        int optionLines = 0;

        if (ticket.items() != null) {
            for (TicketItem item : ticket.items()) {
                itemsCount++;
                if (item.optionGroups() != null) {
                    for (TicketOptionGroup group : item.optionGroups()) {
                        if (group.options() != null) {
                            optionLines += group.options().size();
                        }
                    }
                }
            }
        }

        float estimated = BASE_HEIGHT
                + itemsCount * ITEM_BLOCK_HEIGHT
                + optionLines * OPTION_LINE_HEIGHT;

        if (estimated < MIN_TICKET_HEIGHT) {
            estimated = MIN_TICKET_HEIGHT;
        }
        if (estimated > MAX_TICKET_HEIGHT) {
            estimated = MAX_TICKET_HEIGHT;
        }

        return estimated;
    }

    // ===========================
    // Header
    // ===========================

    private void addHeader(Document doc, Ticket ticket, PdfFont fontBold) {
        TicketHeader header = ticket.header();
        TicketType type = ticket.type();

        // Business name
        if (header.businessName() != null) {
            Paragraph title = new Paragraph(header.businessName())
                    .setFont(fontBold)
                    .setFontSize(14f)
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(title);
        }

        // CUIT
        if (header.businessCuit() != null) {
            doc.add(new Paragraph("CUIT: " + header.businessCuit())
                    .setFontSize(8f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5f));
        }

        // Ticket title (only for BILL / PAYMENT)
        if (type == TicketType.BILL || type == TicketType.PAYMENT) {
            addDashedSeparator(doc);
            if (header.title() != null && !header.title().isBlank()) {
                doc.add(new Paragraph(header.title())
                        .setFont(fontBold)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(2f));
            }
        }

        addDashedSeparator(doc);

        // General info in a 2-column table
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        infoTable.setWidth(UnitValue.createPercentValue(100));

        String mesaVal = header.seatingNumber() != null ? header.seatingNumber().toString() : "-";
        infoTable.addCell(createCell("Mesa: " + mesaVal, TextAlignment.LEFT, fontBold));

        String tipoVal = header.orderType() != null ? header.orderType().name() : "-";
        infoTable.addCell(createCell("Tipo: " + tipoVal, TextAlignment.RIGHT, fontBold));

        String personasVal = header.peopleCount() != null ? header.peopleCount().toString() : "-";
        infoTable.addCell(createCell("Personas: " + personasVal, TextAlignment.LEFT));



        String fechaVal = header.dateTime() != null
                ? header.dateTime().format(DATE_TIME_FORMATTER)
                : "-";
        infoTable.addCell(createCell(fechaVal, TextAlignment.RIGHT).setFontSize(8f));

        doc.add(infoTable);

        if (header.employeeName() != null) {
            doc.add(new Paragraph("Camarero: " + header.employeeName()).setMarginTop(2f));
        }
        if (header.customerName() != null) {
            doc.add(new Paragraph("Cliente: " + header.customerName()));
        }

        doc.add(new Paragraph("")); // spacing
    }

    // ===========================
    // Body
    // ===========================

    private void addBody(Document doc, Ticket ticket, PdfFont fontBold) {
        addDashedSeparator(doc);

        if (ticket.type() == TicketType.KITCHEN) {
            addKitchenBody(doc, ticket, fontBold);
        } else {
            addBillingBody(doc, ticket, fontBold);
        }
    }

    private void addKitchenBody(Document doc, Ticket ticket, PdfFont fontBold) {
        List<TicketItem> items = ticket.items();

        for (TicketItem item : items) {
            doc.add(new Paragraph(item.quantity() + " x " + item.productName())
                    .setFont(fontBold)
                    .setFontSize(10f));

            for (TicketOptionGroup group : item.optionGroups()) {
                doc.add(new Paragraph("  " + group.groupName() + ":").setFontSize(8f));
                for (TicketOptionLine line : group.options()) {
                    doc.add(new Paragraph("    " + line.quantity() + " x " + line.name())
                            .setFontSize(8f));
                }
            }

            if (item.comment() != null && !item.comment().isBlank()) {
                doc.add(new Paragraph("  Comentario: " + item.comment()).setFontSize(8f));
            }

            doc.add(new Paragraph(""));
        }
    }

    private void addBillingBody(Document doc, Ticket ticket, PdfFont fontBold) {
        // 4 columns:
        //  - Quantity
        //  - Separator "|"
        //  - Description
        //  - Amount
        float[] columnWidths = {15f, 5f, 55f, 25f};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Headers
        table.addHeaderCell(createCell("Cant", TextAlignment.LEFT, fontBold));
        table.addHeaderCell(createCell("|", TextAlignment.CENTER, fontBold));
        table.addHeaderCell(createCell("Descripción", TextAlignment.LEFT, fontBold));
        table.addHeaderCell(createCell("Importe", TextAlignment.RIGHT, fontBold));

        for (TicketItem item : ticket.items()) {
            // Main row: "5 | Tostada   $ 7775,00"
            table.addCell(createCell(String.valueOf(item.quantity()), TextAlignment.LEFT));
            table.addCell(createCell("|", TextAlignment.CENTER));
            table.addCell(createCell(item.productName(), TextAlignment.LEFT));
            table.addCell(createCell(formatAmount(item.lineTotal()), TextAlignment.RIGHT));

            // Sub-options shown below the product (no separate amount)
            if (item.optionGroups() != null) {
                item.optionGroups().forEach(group -> {
                    if (group.options() != null) {
                        group.options().forEach(line -> {
                            String optionText =
                                    "- " + line.quantity() + " x " + line.name() +
                                            " (" + group.groupName() + ")";

                            // Empty quantity
                            table.addCell(createCell("", TextAlignment.LEFT));
                            // Keep separator column to align visually
                            table.addCell(createCell("|", TextAlignment.CENTER));
                            // Option description with smaller font
                            Cell optCell = createCell(optionText, TextAlignment.LEFT);
                            optCell.setFontSize(8f);
                            table.addCell(optCell);
                            // Empty amount (included in item line total)
                            table.addCell(createCell("", TextAlignment.RIGHT));
                        });
                    }
                });
            }
        }

        doc.add(table);
    }

    // ===========================
    // Totals
    // ===========================

    private void addTotals(Document doc, Ticket ticket, PdfFont fontBold) {
        TicketTotals totals = ticket.totals();
        if (totals == null) {
            return;
        }

        addDashedSeparator(doc);

        Table totalTable = new Table(UnitValue.createPercentArray(new float[]{3, 2}));
        totalTable.setWidth(UnitValue.createPercentValue(100));

        totalTable.addCell(createCell("Subtotal:", TextAlignment.RIGHT));
        totalTable.addCell(createCell(formatAmount(totals.subtotal()), TextAlignment.RIGHT));

        if (totals.discountPercent() != null && totals.discountPercent() > 0) {
            totalTable.addCell(
                    createCell("Desc (" + totals.discountPercent() + "%):", TextAlignment.RIGHT)
                            .setFontSize(8f)
            );
            totalTable.addCell(
                    createCell("-" + formatAmount(totals.discountAmount()), TextAlignment.RIGHT)
                            .setFontSize(8f)
            );
        }

        totalTable.addCell(
                createCell("TOTAL:", TextAlignment.RIGHT, fontBold)
                        .setFontSize(12f)
        );
        totalTable.addCell(
                createCell(formatAmount(totals.total()), TextAlignment.RIGHT, fontBold)
                        .setFontSize(12f)
        );

        doc.add(totalTable);

        addDashedSeparator(doc);
    }

    // ===========================
    // Visual helpers
    // ===========================

    private Cell createCell(String content, TextAlignment alignment) {
        return createCell(content, alignment, null);
    }

    private Cell createCell(String content, TextAlignment alignment, PdfFont font) {
        Cell cell = new Cell()
                .add(new Paragraph(content != null ? content : ""));
        cell.setTextAlignment(alignment);
        cell.setBorder(Border.NO_BORDER);
        cell.setPadding(0);
        if (font != null) {
            cell.setFont(font);
        }
        return cell;
    }

    private void addDashedSeparator(Document doc) {
        // Simple dashed line separator
        LineSeparator separator = new LineSeparator(new DashedLine());
        separator.setMarginTop(5f);
        separator.setMarginBottom(5f);
        doc.add(separator);
    }

    private String formatAmount(Double value) {
        if (value == null) {
            return "$ 0.00";
        }
        return String.format("$ %.2f", value);
    }
}
