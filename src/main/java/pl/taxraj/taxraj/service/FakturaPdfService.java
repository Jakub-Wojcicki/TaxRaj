package pl.taxraj.taxraj.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.BaseFont;
import org.springframework.stereotype.Service;
import pl.taxraj.taxraj.model.Faktura;
import pl.taxraj.taxraj.model.PozycjaFaktury;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;


@Service
public class FakturaPdfService {

    // Kolory zgodne z paletą strony
    private static final Color INK_COLOR  = new Color(26, 26, 24);
    private static final Color GOLD_COLOR = new Color(184, 151, 58);
    private static final Color MUTED_COLOR = new Color(138, 138, 128);
    private static final Color PAPER_DARK = new Color(236, 232, 220);

    // Czcionki — używamy wbudowanego Helvetica z obsługą Unicode dla polskich znaków
    private BaseFont baseFont;

    public FakturaPdfService() {
        try {
            // Helvetica nie obsługuje polskich znaków — używamy CP1250 (Windows Eastern European)
            baseFont = BaseFont.createFont(BaseFont.HELVETICA, "Cp1250", BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            throw new RuntimeException("Nie udało się załadować czcionki", e);
        }
    }

    public byte[] generujPdf(Faktura faktura) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, baos);
        document.open();

        Font titleFont = new Font(baseFont, 22, Font.BOLD, INK_COLOR);
        Font subtitleFont = new Font(baseFont, 11, Font.NORMAL, MUTED_COLOR);
        Font sectionFont = new Font(baseFont, 11, Font.BOLD, INK_COLOR);
        Font labelFont = new Font(baseFont, 9, Font.NORMAL, MUTED_COLOR);
        Font valueFont = new Font(baseFont, 10, Font.NORMAL, INK_COLOR);
        Font goldFont = new Font(baseFont, 14, Font.BOLD, GOLD_COLOR);

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        // ─── NAGŁÓWEK ───
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{2, 1});

        // Lewa strona — tytuł i numer
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);

        Paragraph title = new Paragraph("FAKTURA", titleFont);
        leftCell.addElement(title);

        Paragraph number = new Paragraph("Nr " + faktura.getNumer(), subtitleFont);
        number.setSpacingBefore(2);
        leftCell.addElement(number);

        header.addCell(leftCell);

        // Prawa strona — Tax-Raj branding
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph brand = new Paragraph("Tax-Raj", new Font(baseFont, 14, Font.BOLD, GOLD_COLOR));
        brand.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(brand);

        Paragraph brandSub = new Paragraph("Kancelaria podatkowa", subtitleFont);
        brandSub.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(brandSub);

        header.addCell(rightCell);
        document.add(header);

        // Pozioma linia
        addSeparator(document);

        // ─── DATY ───
        PdfPTable dates = new PdfPTable(3);
        dates.setWidthPercentage(100);
        dates.setSpacingBefore(15);
        dates.setSpacingAfter(15);
        dates.setWidths(new float[]{1, 1, 1});

        dates.addCell(labelValueCell("Data wystawienia",
                faktura.getDataWystawienia() != null ? faktura.getDataWystawienia().format(dateFmt) : "—",
                labelFont, valueFont));
        dates.addCell(labelValueCell("Termin płatności",
                faktura.getDataPlatnosci() != null ? faktura.getDataPlatnosci().format(dateFmt) : "—",
                labelFont, valueFont));
        dates.addCell(labelValueCell("Status",
                faktura.getStatus() != null ? faktura.getStatus().toString() : "—",
                labelFont, valueFont));

        document.add(dates);

        // ─── SPRZEDAWCA / NABYWCA ───
        PdfPTable parties = new PdfPTable(2);
        parties.setWidthPercentage(100);
        parties.setWidths(new float[]{1, 1});
        parties.setSpacingAfter(20);

        // Sprzedawca = klient (firma, którą obsługuje kancelaria)
        PdfPCell sprzedawcaCell = createPartyCell("SPRZEDAWCA", faktura.getKlient(), labelFont, sectionFont, valueFont);
        parties.addCell(sprzedawcaCell);

        // Nabywca = kontrahent
        PdfPCell nabywcaCell;
        if (faktura.getKontrahent() != null) {
            nabywcaCell = createPartyCell("NABYWCA",
                    faktura.getKontrahent().getNazwa(),
                    faktura.getKontrahent().getNip(),
                    faktura.getKontrahent().getAdres(),
                    labelFont, sectionFont, valueFont);
        } else {
            nabywcaCell = createEmptyPartyCell("NABYWCA", labelFont, sectionFont);
        }
        parties.addCell(nabywcaCell);

        document.add(parties);

        // ─── POZYCJE ───
        Paragraph posTitle = new Paragraph("Pozycje faktury", sectionFont);
        posTitle.setSpacingBefore(5);
        posTitle.setSpacingAfter(8);
        document.add(posTitle);

        PdfPTable positions = new PdfPTable(7);
        positions.setWidthPercentage(100);
        positions.setWidths(new float[]{0.5f, 3, 0.8f, 0.7f, 1.2f, 0.7f, 1.3f});

        // Nagłówek tabeli
        Font tableHeaderFont = new Font(baseFont, 9, Font.BOLD, MUTED_COLOR);
        addTableHeader(positions, "Lp.", tableHeaderFont);
        addTableHeader(positions, "Nazwa", tableHeaderFont);
        addTableHeader(positions, "Ilość", tableHeaderFont);
        addTableHeader(positions, "Jedn.", tableHeaderFont);
        addTableHeader(positions, "Cena netto", tableHeaderFont);
        addTableHeader(positions, "VAT %", tableHeaderFont);
        addTableHeader(positions, "Wartość brutto", tableHeaderFont);

        // Wiersze
        int lp = 1;
        Font cellFont = new Font(baseFont, 9, Font.NORMAL, INK_COLOR);
        for (PozycjaFaktury p : faktura.getPozycje()) {
            addTableCell(positions, String.valueOf(lp++), cellFont, Element.ALIGN_CENTER);
            addTableCell(positions, p.getNazwa() != null ? p.getNazwa() : "—", cellFont, Element.ALIGN_LEFT);
            addTableCell(positions, formatBd(p.getIlosc()), cellFont, Element.ALIGN_RIGHT);
            addTableCell(positions, p.getJednostka() != null ? p.getJednostka() : "—", cellFont, Element.ALIGN_CENTER);
            addTableCell(positions, formatMoney(p.getCenaNetto()), cellFont, Element.ALIGN_RIGHT);
            addTableCell(positions, formatBd(p.getStawkaVat()) + "%", cellFont, Element.ALIGN_RIGHT);
            addTableCell(positions, formatMoney(p.obliczWartoscBrutto()), cellFont, Element.ALIGN_RIGHT);
        }

        document.add(positions);

        // ─── PODSUMOWANIE ───
        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(50);
        summary.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summary.setSpacingBefore(20);
        summary.setWidths(new float[]{1, 1});

        addSummaryRow(summary, "Razem netto:", formatMoney(faktura.getKwotaNetto()), valueFont, valueFont);
        addSummaryRow(summary, "Razem VAT:", formatMoney(faktura.getKwotaVat()), valueFont, valueFont);
        addSummaryRow(summary, "Do zapłaty:", formatMoney(faktura.getKwotaBrutto()) + " " +
                (faktura.getWaluta() != null ? faktura.getWaluta() : "PLN"), goldFont, goldFont);

        document.add(summary);

        // ─── STOPKA ───
        Paragraph footer = new Paragraph(
                "Dokument wygenerowany elektronicznie przez system Tax-Raj. " +
                        "Nie wymaga podpisu ani pieczęci.",
                new Font(baseFont, 8, Font.ITALIC, MUTED_COLOR)
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(40);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }

    // ─── HELPERY ───

    private void addSeparator(Document document) throws DocumentException {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(8);
        document.add(p);

        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell();
        lineCell.setBorder(Rectangle.BOTTOM);
        lineCell.setBorderColor(GOLD_COLOR);
        lineCell.setBorderWidthBottom(1.5f);
        lineCell.setFixedHeight(2);
        line.addCell(lineCell);
        document.add(line);
    }

    private PdfPCell labelValueCell(String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4);

        Paragraph l = new Paragraph(label.toUpperCase(), labelFont);
        cell.addElement(l);

        Paragraph v = new Paragraph(value, valueFont);
        v.setSpacingBefore(2);
        cell.addElement(v);

        return cell;
    }

    private PdfPCell createPartyCell(String label, pl.taxraj.taxraj.model.Klient klient,
                                     Font labelFont, Font sectionFont, Font valueFont) {
        if (klient == null) {
            return createEmptyPartyCell(label, labelFont, sectionFont);
        }
        return createPartyCell(label, klient.getNazwa(), klient.getNip(), klient.getAdres(),
                labelFont, sectionFont, valueFont);
    }

    private PdfPCell createPartyCell(String label, String nazwa, String nip, String adres,
                                     Font labelFont, Font sectionFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(PAPER_DARK);
        cell.setBorderWidth(1);

        Paragraph l = new Paragraph(label, labelFont);
        cell.addElement(l);

        Paragraph n = new Paragraph(nazwa != null ? nazwa : "—", sectionFont);
        n.setSpacingBefore(4);
        cell.addElement(n);

        if (nip != null && !nip.isBlank()) {
            Paragraph nipP = new Paragraph("NIP: " + nip, valueFont);
            nipP.setSpacingBefore(2);
            cell.addElement(nipP);
        }

        if (adres != null && !adres.isBlank()) {
            Paragraph addrP = new Paragraph(adres, valueFont);
            addrP.setSpacingBefore(2);
            cell.addElement(addrP);
        }

        return cell;
    }

    private PdfPCell createEmptyPartyCell(String label, Font labelFont, Font sectionFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(PAPER_DARK);

        Paragraph l = new Paragraph(label, labelFont);
        cell.addElement(l);

        Paragraph empty = new Paragraph("(nie podano)", new Font(baseFont, 10, Font.ITALIC, MUTED_COLOR));
        empty.setSpacingBefore(4);
        cell.addElement(empty);

        return cell;
    }

    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setBackgroundColor(new Color(245, 242, 235));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(GOLD_COLOR);
        cell.setBorderWidthBottom(1.5f);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(PAPER_DARK);
        cell.setBorderWidthBottom(0.5f);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell l = new PdfPCell(new Phrase(label, labelFont));
        l.setBorder(Rectangle.NO_BORDER);
        l.setPadding(4);
        l.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(l);

        PdfPCell v = new PdfPCell(new Phrase(value, valueFont));
        v.setBorder(Rectangle.NO_BORDER);
        v.setPadding(4);
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(v);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "0,00";
        return String.format("%.2f", value).replace(".", ",");
    }

    private String formatBd(BigDecimal value) {
        if (value == null) return "—";
        return String.format("%.2f", value).replace(".", ",");
    }
}