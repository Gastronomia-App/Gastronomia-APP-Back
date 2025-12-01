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

        // 1. Nombre del Negocio
        if (header.businessName() != null) {
            Paragraph title = new Paragraph(header.businessName())
                    .setFont(fontBold)
                    .setFontSize(16f) // Un poco más grande
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(title);
        }

        // 2. CUIT
        if (header.businessCuit() != null) {
            doc.add(new Paragraph("CUIT: " + header.businessCuit())
                    .setFontSize(9f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5f));
        }

        // 3. Título del Ticket (Lógica mejorada)
        String ticketTitle = "";

        // Si el objeto header ya trae un título personalizado, úsalo. Si no, usa el default:
        if (header.title() != null && !header.title().isBlank()) {
            ticketTitle = header.title();
        } else {
            switch (type) {
                case BILL:
                    ticketTitle = "DETALLE DE CONSUMO"; // Para el cliente antes de pagar
                    break;
                case PAYMENT:
                    ticketTitle = "FACTURA"; // O "TICKET FINAL"
                    break;
                case KITCHEN:
                    ticketTitle = "COMANDA DE COCINA";
                    break;
                default:
                    ticketTitle = "TICKET";
            }
        }

        // Solo mostramos el título si no es nulo
        if (!ticketTitle.isEmpty()) {
            addDashedSeparator(doc);
            doc.add(new Paragraph(ticketTitle)
                    .setFont(fontBold)
                    .setFontSize(12f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2f));
        }

        addDashedSeparator(doc);

        // ... El resto de la tabla de info (Mesa, Personas, etc) queda igual ...
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
            doc.add(new Paragraph("Mozo/a: " + header.employeeName()).setMarginTop(2f));
        }
        if (header.customerName() != null) {
            doc.add(new Paragraph("Cliente: " + header.customerName()));
        }

        doc.add(new Paragraph(""));
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
        if (items == null || items.isEmpty()) {
            return;
        }

        try {
            // Creamos una fuente Italic sobre la marcha para los extras (opcional, pero queda bien)
            PdfFont fontItalic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            for (TicketItem item : items) {
                // 1. PRODUCTO PRINCIPAL (Más grande y separado)
                // Ejemplo: "2 x HAMBURGUESA"
                Paragraph mainItem = new Paragraph(item.quantity() + " x " + item.productName().toUpperCase())
                        .setFont(fontBold)
                        .setFontSize(11f) // Un poco más grande para lectura rápida
                        .setMarginBottom(0f);
                doc.add(mainItem);

                // 2. EXTRAS / OPCIONES (Indentados visualmente)
                if (item.optionGroups() != null) {
                    item.optionGroups().forEach(group -> {
                        if (group.options() == null || group.options().isEmpty()) {
                            return;
                        }

                        // Opcional: Si quieres mostrar el nombre del grupo (ej: "Salsas:")
                        // doc.add(new Paragraph(group.groupName() + ":").setFontSize(7f).setMarginLeft(10f));

                        group.options().forEach(line -> {
                            // Usamos un bullet point simple "•"
                            String extraText = "• " + line.quantity() + " " + line.name();

                            Paragraph pExtra = new Paragraph(extraText)
                                    .setFont(fontItalic)      // Cursiva para diferenciar
                                    .setFontSize(9f)          // Tamaño normal
                                    .setMarginLeft(15f)       // Sangría real a la derecha
                                    .setMarginTop(0f)
                                    .setMarginBottom(0f);

                            doc.add(pExtra);
                        });
                    });
                }

                // 3. COMENTARIOS (Muy importantes en cocina)
                if (item.comment() != null && !item.comment().isBlank()) {
                    String commentText = "NOTA: " + item.comment();
                    Paragraph pComment = new Paragraph(commentText)
                            .setFont(fontBold)        // Negrita para que no se pierda la nota
                            .setFontSize(8f)
                            .setMarginLeft(15f)
                            .setMarginTop(2f);
                    doc.add(pComment);
                }

                // Línea separadora suave o espacio entre items distintos
                doc.add(new Paragraph("").setMarginBottom(6f));
                // Opcional: Agregar una línea punteada fina entre productos si la orden es muy larga
                addDashedSeparator(doc);
            }
        } catch (IOException e) {
            e.printStackTrace(); // Manejo simple de error de fuente
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
