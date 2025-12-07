package com.progra3.cafeteria_api.service.impl.tickets;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.DashedLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.progra3.cafeteria_api.model.enums.TicketType;
import com.progra3.cafeteria_api.model.dto.ticket.Ticket;
import com.progra3.cafeteria_api.model.dto.ticket.TicketHeader;
import com.progra3.cafeteria_api.model.dto.ticket.TicketItem;
import com.progra3.cafeteria_api.model.dto.ticket.TicketOptionGroup;
import com.progra3.cafeteria_api.model.dto.ticket.TicketOptionLine;
import com.progra3.cafeteria_api.model.dto.ticket.TicketTotals;
import com.progra3.cafeteria_api.service.port.tickets.ITicketPdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders Ticket as a thermal-style PDF receipt (about 80mm width).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketPdfService implements ITicketPdfService {

    // Thermal width (~80mm)
    private static final float TICKET_WIDTH = 226f;

    // Dynamic height configuration
    private static final float MIN_TICKET_HEIGHT = 260f;
    private static final float MAX_TICKET_HEIGHT = 2000f;

    private static final float BASE_HEIGHT = 230f;
    private static final float ITEM_BLOCK_HEIGHT = 24f;
    private static final float OPTION_LINE_HEIGHT = 12f;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Monospace font resource (src/main/resources/fonts/DejaVuSansMono.ttf)
    private static final String MONO_FONT_RESOURCE = "/fonts/DejaVuSansMono.ttf";

    // QR code size for fiscal ticket
    private static final int QR_SIZE = 150;

    // Box-drawing characters
    private static final String VERTICAL = "│";
    private static final String BRANCH = "├";
    private static final String LAST_BRANCH = "└";
    // Bloque “vacío” para columnas de ancestros sin más hermanos
    private static final String EMPTY_BLOCK = "\u00A0\u00A0\u00A0\u00A0"; // 4 NBSP

    @Override
    public byte[] generateTicketPdf(Ticket ticket) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);

            float ticketHeight = calculateTicketHeight(ticket);
            Document doc = new Document(pdf, new PageSize(TICKET_WIDTH, ticketHeight));

            doc.setMargins(10, 10, 10, 10);

            PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontMono = loadMonoFont(); // para el árbol de opciones

            doc.setFont(fontRegular);
            doc.setFontSize(9f);

            addHeader(doc, ticket, fontBold);
            addBody(doc, ticket, fontBold, fontMono);
            addTotals(doc, ticket, fontBold);

            doc.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Error generating ticket PDF", e);
        }
    }

    /**
     * Estimate the page height based on the amount of content.
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

        // Add extra height for fiscal tickets (QR code, CAE, fiscal header info)
        if (ticket.type() == TicketType.FISCAL_TICKET) {
            estimated += 180f; // QR (~150) + CAE info + extra header lines
        }

        if (estimated < MIN_TICKET_HEIGHT) {
            estimated = MIN_TICKET_HEIGHT;
        }
        if (estimated > MAX_TICKET_HEIGHT) {
            estimated = MAX_TICKET_HEIGHT;
        }

        return estimated;
    }

    // ===========================
    // Fonts
    // ===========================

    private PdfFont loadMonoFont() {
        try (InputStream is = getClass().getResourceAsStream(MONO_FONT_RESOURCE)) {
            if (is == null) {
                return fallbackMonoFont();
            }
            byte[] fontBytes = is.readAllBytes();

            FontProgram fontProgram = FontProgramFactory.createFont(fontBytes);

            return PdfFontFactory.createFont(
                    fontProgram,
                    PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );
        } catch (IOException e) {
            return fallbackMonoFont();
        }
    }

    private PdfFont fallbackMonoFont() {
        try {
            return PdfFontFactory.createFont(StandardFonts.COURIER);
        } catch (IOException ex) {
            throw new IllegalStateException("Error loading fallback mono font", ex);
        }
    }

    // ===========================
    // Header
    // ===========================

    private void addHeader(Document doc, Ticket ticket, PdfFont fontBold) {
        TicketHeader header = ticket.header();
        TicketType type = ticket.type();

        // Minimal header for kitchen tickets: mesa, fecha/hora y mozo
        if (type == TicketType.KITCHEN_TICKET) {
            addKitchenHeader(doc, header, fontBold);
            return;
        }

        // Fiscal ticket with AFIP-compliant header
        if (type == TicketType.FISCAL_TICKET) {
            addFiscalHeader(doc, header, fontBold);
            return;
        }

        // Default header for PRE_TICKET
        addDefaultHeader(doc, header, type, fontBold);
    }

    private void addKitchenHeader(Document doc, TicketHeader header, PdfFont fontBold) {
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        infoTable.setWidth(UnitValue.createPercentValue(100));

        String mesaVal = header.seatingNumber() != null
                ? header.seatingNumber().toString()
                : "-";
        infoTable.addCell(createCell("Mesa: " + mesaVal, TextAlignment.LEFT, fontBold));

        String fechaVal = header.dateTime() != null
                ? header.dateTime().format(DATE_TIME_FORMATTER)
                : "-";
        infoTable.addCell(
                createCell(fechaVal, TextAlignment.RIGHT, fontBold)
                        .setFontSize(8f)
        );

        doc.add(infoTable);

        if (header.employeeName() != null) {
            doc.add(new Paragraph("Mozo/a: " + header.employeeName()).setMarginTop(2f));
        }

        doc.add(new Paragraph(""));
    }

    private void addFiscalHeader(Document doc, TicketHeader header, PdfFont fontBold) {
        // Business name
        if (header.businessName() != null) {
            doc.add(new Paragraph("Razón social: " + header.businessName())
                    .setFontSize(9f)
                    .setMarginBottom(0f));
        }

        // Business address
        if (header.businessAddress() != null) {
            doc.add(new Paragraph("Dirección: " + header.businessAddress())
                    .setFontSize(9f)
                    .setMarginBottom(0f));
        }

        // Business CUIT
        if (header.businessCuit() != null) {
            doc.add(new Paragraph("C.U.I.T.: " + formatCuit(header.businessCuit()))
                    .setFontSize(9f)
                    .setMarginBottom(0f));
        }

        // IVA condition
        if (header.businessIvaCondition() != null) {
            doc.add(new Paragraph(header.businessIvaCondition())
                    .setFontSize(9f)
                    .setMarginBottom(0f));
        }

        // IIBB (Ingresos Brutos)
        if (header.businessIibb() != null) {
            doc.add(new Paragraph("IIBB: " + header.businessIibb())
                    .setFontSize(9f)
                    .setMarginBottom(0f));
        }

        // Activity start date
        if (header.businessActivityStart() != null) {
            doc.add(new Paragraph("Inicio de actividad: " + header.businessActivityStart().format(DATE_FORMATTER))
                    .setFontSize(9f)
                    .setMarginBottom(0f));
        }

        addDashedSeparator(doc);

        // Invoice type title (FACTURA A, FACTURA B, FACTURA C, etc.)
        String invoiceTitle = header.title() != null ? header.title() : "FACTURA";
        doc.add(new Paragraph(invoiceTitle)
                .setFont(fontBold)
                .setFontSize(16f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(0f));

        // Invoice code (Código)
        if (header.invoiceCode() != null) {
            doc.add(new Paragraph("Código " + String.format("%02d", header.invoiceCode()))
                    .setFontSize(9f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2f));
        }

        addDashedSeparator(doc);

        // Punto de Venta and Comprobante number
        if (header.puntoVenta() != null) {
            doc.add(new Paragraph("P.V: " + String.format("%05d", header.puntoVenta()))
                    .setFontSize(9f)
                    .setMarginBottom(0f));
        }

        if (header.cbteNumero() != null) {
            doc.add(new Paragraph("Nro: " + String.format("%08d", header.cbteNumero()))
                    .setFontSize(9f)
                    .setMarginBottom(0f));
        }

        // Invoice date
        if (header.dateTime() != null) {
            doc.add(new Paragraph("Fecha: " + header.dateTime().format(DATE_FORMATTER))
                    .setFontSize(9f)
                    .setMarginBottom(0f));
        }

        // Concept
        if (header.concept() != null) {
            doc.add(new Paragraph("Concepto: " + header.concept())
                    .setFontSize(9f)
                    .setMarginBottom(0f));
        }

        addDashedSeparator(doc);

        // Customer IVA condition
        if (header.customerIvaCondition() != null) {
            doc.add(new Paragraph("A " + header.customerIvaCondition())
                    .setFontSize(9f)
                    .setMarginBottom(0f));
        }

        doc.add(new Paragraph(""));
    }

    private void addDefaultHeader(Document doc, TicketHeader header, TicketType type, PdfFont fontBold) {
        // Full header for PRE_TICKET
        if (header.businessName() != null) {
            Paragraph title = new Paragraph(header.businessName())
                    .setFont(fontBold)
                    .setFontSize(16f)
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(title);
        }

        if (header.businessCuit() != null) {
            doc.add(new Paragraph("CUIT: " + header.businessCuit())
                    .setFontSize(9f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5f));
        }

        String ticketTitle;
        if (header.title() != null && !header.title().isBlank()) {
            ticketTitle = header.title();
        } else {
            ticketTitle = switch (type) {
                case PRE_TICKET -> "DETALLE DE CONSUMO";
                case FISCAL_TICKET -> "FACTURA";
                default -> "TICKET";
            };
        }

        if (!ticketTitle.isEmpty()) {
            addDashedSeparator(doc);
            doc.add(new Paragraph(ticketTitle)
                    .setFont(fontBold)
                    .setFontSize(12f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2f));
        }

        addDashedSeparator(doc);

        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        infoTable.setWidth(UnitValue.createPercentValue(100));

        String mesaVal = header.seatingNumber() != null
                ? header.seatingNumber().toString()
                : "-";
        infoTable.addCell(createCell("Mesa: " + mesaVal, TextAlignment.LEFT, fontBold));

        String tipoVal = header.orderType() != null
                ? header.orderType().name()
                : "-";
        infoTable.addCell(createCell("Tipo: " + tipoVal, TextAlignment.RIGHT, fontBold));

        String personasVal = header.peopleCount() != null
                ? header.peopleCount().toString()
                : "-";
        infoTable.addCell(createCell("Personas: " + personasVal, TextAlignment.LEFT));

        String fechaVal = header.dateTime() != null
                ? header.dateTime().format(DATE_TIME_FORMATTER)
                : "-";
        infoTable.addCell(
                createCell(fechaVal, TextAlignment.RIGHT)
                        .setFontSize(8f)
        );

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

    private void addBody(Document doc, Ticket ticket, PdfFont fontBold, PdfFont fontMono) {
        addDashedSeparator(doc);

        if (ticket.type() == TicketType.KITCHEN_TICKET) {
            addKitchenBody(doc, ticket, fontBold, fontMono);
        } else {
            addBillingBody(doc, ticket, fontBold);
        }
    }
    private void debugOptionLines(List<TicketOptionLine> lines) {
        System.out.println("---- OPTION LINES ----");
        for (int i = 0; i < lines.size(); i++) {
            TicketOptionLine l = lines.get(i);
            System.out.printf(
                    "%d -> level=%d, qty=%d, name=%s%n",
                    i,
                    l.level(),
                    l.quantity(),
                    l.name()
            );
        }
    }
    private void addKitchenBody(Document doc, Ticket ticket, PdfFont fontBold, PdfFont fontMono) {
        List<TicketItem> items = ticket.items();
        if (items == null || items.isEmpty()) {
            return;
        }

        for (TicketItem item : items) {
            // Main product line (e.g. "2 x HAMBURGUESA")
            Paragraph mainItem = new Paragraph(item.quantity() + " x " + item.productName().toUpperCase())
                    .setFont(fontBold)
                    .setFontSize(11f)
                    .setMarginBottom(0f);
            doc.add(mainItem);

            // Collect all option lines for this item
            List<TicketOptionLine> optionLines = new ArrayList<>();
            if (item.optionGroups() != null) {
                item.optionGroups().forEach(group -> {
                    if (group.options() != null && !group.options().isEmpty()) {
                        optionLines.addAll(group.options());
                    }
                });
            }

            // Render options as ASCII tree
            debugOptionLines(optionLines);
            addOptionTree(doc, optionLines, fontMono);

            // Optional comment for kitchen
            if (item.comment() != null && !item.comment().isBlank()) {
                String commentText = "NOTA: " + item.comment();
                Paragraph pComment = new Paragraph(commentText)
                        .setFont(fontBold)
                        .setFontSize(8f)
                        .setMarginLeft(15f)
                        .setMarginTop(2f);
                doc.add(pComment);
            }

            // Space between items
            doc.add(new Paragraph("").setMarginBottom(6f));
        }
    }

    private void addBillingBody(Document doc, Ticket ticket, PdfFont fontBold) {
        float[] columnWidths = {15f, 5f, 55f, 25f};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(createCell("Cant", TextAlignment.LEFT, fontBold));
        table.addHeaderCell(createCell("|", TextAlignment.CENTER, fontBold));
        table.addHeaderCell(createCell("Descripción", TextAlignment.LEFT, fontBold));
        table.addHeaderCell(createCell("Importe", TextAlignment.RIGHT, fontBold));

        for (TicketItem item : ticket.items()) {
            table.addCell(createCell(String.valueOf(item.quantity()), TextAlignment.LEFT));
            table.addCell(createCell("|", TextAlignment.CENTER));
            table.addCell(createCell(item.productName(), TextAlignment.LEFT));
            table.addCell(createCell(formatAmount(item.lineTotal()), TextAlignment.RIGHT));

            if (item.optionGroups() != null) {
                item.optionGroups().forEach(group -> {
                    if (group.options() != null) {
                        group.options().forEach(line -> {
                            String optionText =
                                    "- " + line.quantity() + " x " + line.name() +
                                            " (" + group.groupName() + ")";

                            table.addCell(createCell("", TextAlignment.LEFT));
                            table.addCell(createCell("|", TextAlignment.CENTER));
                            Cell optCell = createCell(optionText, TextAlignment.LEFT);
                            optCell.setFontSize(8f);
                            table.addCell(optCell);
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

        // Fiscal ticket footer: CAE, expiration and QR code
        if (ticket.type() == TicketType.FISCAL_TICKET) {
            addFiscalFooter(doc, totals, fontBold);
        }

        // Pre-ticket warning
        if (totals.printInvoiceWarning()) {
            doc.add(new Paragraph("Este documento no es valido como factura.")
                    .setFontSize(8f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(5f));
        }
    }

    private void addFiscalFooter(Document doc, TicketTotals totals, PdfFont fontBold) {
        // CAE
        if (totals.cae() != null) {
            doc.add(new Paragraph("CAE: " + totals.cae())
                    .setFontSize(9f)
                    .setMarginBottom(0f));
        }

        // CAE Expiration
        if (totals.caeExpiration() != null) {
            doc.add(new Paragraph("Vto: " + totals.caeExpiration().format(DATE_FORMATTER))
                    .setFontSize(9f)
                    .setMarginBottom(2f));
        }

        // QR Code
        if (totals.qrData() != null) {
            try {
                byte[] qrBytes = generateQrCode(totals.qrData());
                ImageData imageData = ImageDataFactory.create(qrBytes);
                Image qrImage = new Image(imageData);
                qrImage.setWidth(QR_SIZE);
                qrImage.setHeight(QR_SIZE);
                qrImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
                qrImage.setMarginTop(3f);
                qrImage.setMarginBottom(5f);
                doc.add(qrImage);
            } catch (Exception e) {
                log.error("Failed to generate QR code for fiscal ticket", e);
                doc.add(new Paragraph("Error generando código QR")
                        .setFontSize(8f)
                        .setTextAlignment(TextAlignment.CENTER));
            }
        }
    }

    /**
     * Generates a QR code image as PNG bytes.
     */
    private byte[] generateQrCode(String content) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
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

    // ===========================
    // Tree helpers for kitchen options
    // ===========================

    private int getLevelOrDefault(TicketOptionLine line) {
        return line.level() != null ? line.level() : 1;
    }

    /**
     * Builds a tree-like prefix for the given option line using box-drawing
     * characters (├, └, │). In this version we always draw a vertical line
     * for every ancestor level so children are visually attached to their parent
     * even if the parent is the last sibling.
     */
    private String buildTreePrefix(List<TicketOptionLine> lines, int index) {
        TicketOptionLine current = lines.get(index);
        int currentLevel = getLevelOrDefault(current);
        StringBuilder prefix = new StringBuilder();

        // 1) Columnas de los ancestros (niveles 1 .. currentLevel-1)
        for (int level = 1; level < currentLevel; level++) {
            boolean hasMoreAtThisLevel = false;

            // ¿Hay alguna línea más adelante en este mismo nivel 'level'?
            for (int j = index + 1; j < lines.size(); j++) {
                int levelJ = getLevelOrDefault(lines.get(j));

                if (levelJ < level) {
                    // Ya salimos del bloque de este nivel
                    break;
                }
                if (levelJ == level) {
                    hasMoreAtThisLevel = true;
                    break;
                }
            }

            if (hasMoreAtThisLevel) {
                prefix.append(VERTICAL).append("   ");     // "│   "
            } else {
                prefix.append(EMPTY_BLOCK);               // NBSP NBSP NBSP NBSP
            }
        }

        // 2) Rama de la línea actual (├ o └)
        boolean hasNextSameLevel = false;
        for (int j = index + 1; j < lines.size(); j++) {
            int levelJ = getLevelOrDefault(lines.get(j));
            if (levelJ < currentLevel) {
                break;
            }
            if (levelJ == currentLevel) {
                hasNextSameLevel = true;
                break;
            }
        }

        String branch = hasNextSameLevel ? BRANCH : LAST_BRANCH; // "├" o "└"
        prefix.append(branch).append("── ");

        return prefix.toString();
    }

    private void addOptionTree(Document doc, List<TicketOptionLine> optionLines, PdfFont fontMono) {
        if (optionLines == null || optionLines.isEmpty()) {
            return;
        }

        for (int i = 0; i < optionLines.size(); i++) {
            TicketOptionLine line = optionLines.get(i);

            String treePrefix = buildTreePrefix(optionLines, i);
            int qty = line.quantity() != null ? line.quantity() : 1;
            String label = qty + " x " + line.name();

            Paragraph p = new Paragraph(treePrefix + label)
                    .setFont(fontMono)
                    .setFontSize(9f)
                    .setMarginLeft(5f)
                    .setMarginTop(0f)
                    .setMarginBottom(0f);

            doc.add(p);
        }
    }

    /**
     * Formats a CUIT number as XX-XXXXXXXX-X.
     */
    private String formatCuit(Long cuit) {
        if (cuit == null) {
            return "";
        }
        String cuitStr = String.valueOf(cuit);
        if (cuitStr.length() == 11) {
            return cuitStr.substring(0, 2) + "-" + cuitStr.substring(2, 10) + "-" + cuitStr.substring(10);
        }
        return cuitStr;
    }
}
