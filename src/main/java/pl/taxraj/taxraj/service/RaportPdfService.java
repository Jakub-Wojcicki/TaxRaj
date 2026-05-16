package pl.taxraj.taxraj.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import pl.taxraj.taxraj.model.Faktura;
import pl.taxraj.taxraj.model.Klient;
import pl.taxraj.taxraj.model.enums.TypFaktury;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class RaportPdfService {

    private static final Color INK_COLOR  = new Color(26, 26, 24);
    private static final Color GOLD_COLOR = new Color(184, 151, 58);
    private static final Color MUTED_COLOR = new Color(138, 138, 128);
    private static final Color PAPER_DARK = new Color(236, 232, 220);
    private static final Color GOLD_PALE = new Color(250, 245, 230);

    private final BaseFont baseFont;

    public RaportPdfService() {
        try {
            baseFont = BaseFont.createFont(BaseFont.HELVETICA, "Cp1250", BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            throw new RuntimeException("Nie udało się załadować czcionki", e);
        }
    }

    @SuppressWarnings("unchecked")
    public byte[] generujZestawienieVAT(Map<String, Object> dane) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        Klient klient = (Klient) dane.get("klient");
        String okres = (String) dane.get("okres");
        List<Faktura> sprzedaz = (List<Faktura>) dane.get("sprzedaz");
        List<Faktura> zakup = (List<Faktura>) dane.get("zakup");
        BigDecimal netSprzedaz = (BigDecimal) dane.get("netSprzedaz");
        BigDecimal vatNalezny = (BigDecimal) dane.get("vatNalezny");
        BigDecimal netZakup = (BigDecimal) dane.get("netZakup");
        BigDecimal vatNaliczony = (BigDecimal) dane.get("vatNaliczony");
        BigDecimal saldo = (BigDecimal) dane.get("saldo");
        boolean doZaplaty = (boolean) dane.get("saldoDoZaplaty");

        Font titleFont = new Font(baseFont, 22, Font.BOLD, INK_COLOR);
        Font subtitleFont = new Font(baseFont, 11, Font.NORMAL, MUTED_COLOR);
        Font sectionFont = new Font(baseFont, 13, Font.BOLD, INK_COLOR);
        Font labelFont = new Font(baseFont, 9, Font.NORMAL, MUTED_COLOR);
        Font valueFont = new Font(baseFont, 11, Font.NORMAL, INK_COLOR);
        Font bigGoldFont = new Font(baseFont, 24, Font.BOLD, GOLD_COLOR);

        // NAGŁÓWEK
        Paragraph title = new Paragraph("ZESTAWIENIE VAT", titleFont);
        doc.add(title);

        Paragraph sub = new Paragraph(klient.getNazwa() + " — okres " + okres, subtitleFont);
        sub.setSpacingAfter(15);
        doc.add(sub);

        // LINIA
        addSeparator(doc);

        // SALDO GŁÓWNE
        PdfPTable saldoBox = new PdfPTable(1);
        saldoBox.setWidthPercentage(100);
        saldoBox.setSpacingBefore(15);
        saldoBox.setSpacingAfter(15);

        PdfPCell saldoCell = new PdfPCell();
        saldoCell.setPadding(20);
        saldoCell.setBackgroundColor(GOLD_PALE);
        saldoCell.setBorder(Rectangle.BOX);
        saldoCell.setBorderColor(GOLD_COLOR);
        saldoCell.setBorderWidth(1);
        saldoCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph saldoLabel = new Paragraph("SALDO VAT", labelFont);
        saldoLabel.setAlignment(Element.ALIGN_CENTER);
        saldoCell.addElement(saldoLabel);

        Paragraph saldoValue = new Paragraph(formatMoney(saldo) + " PLN", bigGoldFont);
        saldoValue.setAlignment(Element.ALIGN_CENTER);
        saldoValue.setSpacingBefore(6);
        saldoCell.addElement(saldoValue);

        Paragraph saldoDesc = new Paragraph(
                saldo.signum() == 0 ? "Saldo zerowe"
                        : (doZaplaty ? "Do zapłaty do Urzędu Skarbowego" : "Do zwrotu z Urzędu Skarbowego"),
                new Font(baseFont, 10, Font.NORMAL, INK_COLOR));
        saldoDesc.setAlignment(Element.ALIGN_CENTER);
        saldoDesc.setSpacingBefore(4);
        saldoCell.addElement(saldoDesc);

        saldoBox.addCell(saldoCell);
        doc.add(saldoBox);

        // SUMY SPRZEDAŻY I ZAKUPÓW
        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(100);
        summary.setWidths(new float[]{1, 1});
        summary.setSpacingAfter(15);

        summary.addCell(buildSummaryBox("Sprzedaż (VAT należny)",
                sprzedaz.size(), netSprzedaz, vatNalezny, sectionFont, labelFont, valueFont));
        summary.addCell(buildSummaryBox("Zakupy (VAT naliczony)",
                zakup.size(), netZakup, vatNaliczony, sectionFont, labelFont, valueFont));

        doc.add(summary);

        // TABELE FAKTUR
        if (!sprzedaz.isEmpty()) {
            doc.add(buildTableTitle("Faktury sprzedażowe", sectionFont));
            doc.add(buildFakturyTable(sprzedaz));
        }

        if (!zakup.isEmpty()) {
            Paragraph spacer = new Paragraph(" ");
            spacer.setSpacingBefore(10);
            doc.add(spacer);
            doc.add(buildTableTitle("Faktury kosztowe", sectionFont));
            doc.add(buildFakturyTable(zakup));
        }

        // STOPKA
        Paragraph footer = new Paragraph(
                "Dokument wygenerowany elektronicznie przez system Tax-Raj.",
                new Font(baseFont, 8, Font.ITALIC, MUTED_COLOR));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        doc.add(footer);

        doc.close();
        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public byte[] generujRejestr(Map<String, Object> dane, String tytul) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);   // poziomo
        PdfWriter.getInstance(doc, baos);
        doc.open();

        Klient klient = (Klient) dane.get("klient");
        String okres = (String) dane.get("okres");
        List<Faktura> faktury = (List<Faktura>) dane.get("faktury");
        BigDecimal sumaNetto = (BigDecimal) dane.get("sumaNetto");
        BigDecimal sumaVat = (BigDecimal) dane.get("sumaVat");
        BigDecimal sumaBrutto = (BigDecimal) dane.get("sumaBrutto");

        Font titleFont = new Font(baseFont, 18, Font.BOLD, INK_COLOR);
        Font subtitleFont = new Font(baseFont, 11, Font.NORMAL, MUTED_COLOR);
        Font headerFont = new Font(baseFont, 9, Font.BOLD, MUTED_COLOR);
        Font cellFont = new Font(baseFont, 9, Font.NORMAL, INK_COLOR);
        Font totalFont = new Font(baseFont, 11, Font.BOLD, GOLD_COLOR);

        Paragraph title = new Paragraph(tytul, titleFont);
        doc.add(title);

        Paragraph sub = new Paragraph(klient.getNazwa() + " — okres " + okres, subtitleFont);
        sub.setSpacingAfter(15);
        doc.add(sub);

        if (faktury.isEmpty()) {
            Paragraph empty = new Paragraph("Brak faktur w wybranym okresie.",
                    new Font(baseFont, 11, Font.ITALIC, MUTED_COLOR));
            empty.setAlignment(Element.ALIGN_CENTER);
            empty.setSpacingBefore(20);
            doc.add(empty);
            doc.close();
            return baos.toByteArray();
        }

        // TABELA
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.5f, 1.5f, 1.2f, 2.5f, 1.2f, 1.2f, 1.2f});

        addTableHeader(table, "Lp.", headerFont);
        addTableHeader(table, "Numer", headerFont);
        addTableHeader(table, "Data", headerFont);
        addTableHeader(table, "Kontrahent", headerFont);
        addTableHeader(table, "Netto", headerFont);
        addTableHeader(table, "VAT", headerFont);
        addTableHeader(table, "Brutto", headerFont);

        int lp = 1;
        for (Faktura f : faktury) {
            addTableCell(table, String.valueOf(lp++), cellFont, Element.ALIGN_CENTER);
            addTableCell(table, f.getNumer(), cellFont, Element.ALIGN_LEFT);
            addTableCell(table, f.getDataWystawienia().toString(), cellFont, Element.ALIGN_LEFT);
            addTableCell(table, f.getKontrahent() != null ? f.getKontrahent().getNazwa() : "—", cellFont, Element.ALIGN_LEFT);
            addTableCell(table, formatMoney(f.getKwotaNetto()), cellFont, Element.ALIGN_RIGHT);
            addTableCell(table, formatMoney(f.getKwotaVat()), cellFont, Element.ALIGN_RIGHT);
            addTableCell(table, formatMoney(f.getKwotaBrutto()), cellFont, Element.ALIGN_RIGHT);
        }

        // PODSUMOWANIE
        PdfPCell totalLabel = new PdfPCell(new Phrase("RAZEM:", totalFont));
        totalLabel.setColspan(4);
        totalLabel.setPadding(8);
        totalLabel.setBorder(Rectangle.TOP);
        totalLabel.setBorderColor(GOLD_COLOR);
        totalLabel.setBorderWidthTop(2);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalLabel);

        addTotalCell(table, formatMoney(sumaNetto), totalFont);
        addTotalCell(table, formatMoney(sumaVat), totalFont);
        addTotalCell(table, formatMoney(sumaBrutto), totalFont);

        doc.add(table);

        // STOPKA
        Paragraph footer = new Paragraph(
                "Dokument wygenerowany elektronicznie przez system Tax-Raj. Liczba faktur: " + faktury.size(),
                new Font(baseFont, 8, Font.ITALIC, MUTED_COLOR));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        doc.add(footer);

        doc.close();
        return baos.toByteArray();
    }

    // ────── HELPERY ──────

    private PdfPCell buildSummaryBox(String tytul, int liczba, BigDecimal netto, BigDecimal vat,
                                     Font titleFont, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(14);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(PAPER_DARK);

        Paragraph t = new Paragraph(tytul, titleFont);
        cell.addElement(t);

        cell.addElement(buildSummaryRow("Liczba faktur:", String.valueOf(liczba), labelFont, valueFont));
        cell.addElement(buildSummaryRow("Razem netto:", formatMoney(netto) + " zł", labelFont, valueFont));
        cell.addElement(buildSummaryRow("VAT:", formatMoney(vat) + " zł", labelFont,
                new Font(valueFont.getBaseFont(), 12, Font.BOLD, GOLD_COLOR)));

        return cell;
    }

    private Paragraph buildSummaryRow(String label, String value, Font labelFont, Font valueFont) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "  ", labelFont));
        p.add(new Chunk(value, valueFont));
        p.setSpacingBefore(4);
        return p;
    }

    private PdfPTable buildFakturyTable(List<Faktura> faktury) {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        try { table.setWidths(new float[]{2f, 1.2f, 1.2f, 1.2f, 1.2f}); } catch (Exception e) {}

        Font headerFont = new Font(baseFont, 9, Font.BOLD, MUTED_COLOR);
        Font cellFont = new Font(baseFont, 9, Font.NORMAL, INK_COLOR);

        addTableHeader(table, "Numer", headerFont);
        addTableHeader(table, "Data", headerFont);
        addTableHeader(table, "Netto", headerFont);
        addTableHeader(table, "VAT", headerFont);
        addTableHeader(table, "Brutto", headerFont);

        for (Faktura f : faktury) {
            addTableCell(table, f.getNumer(), cellFont, Element.ALIGN_LEFT);
            addTableCell(table, f.getDataWystawienia().toString(), cellFont, Element.ALIGN_LEFT);
            addTableCell(table, formatMoney(f.getKwotaNetto()), cellFont, Element.ALIGN_RIGHT);
            addTableCell(table, formatMoney(f.getKwotaVat()), cellFont, Element.ALIGN_RIGHT);
            addTableCell(table, formatMoney(f.getKwotaBrutto()), cellFont, Element.ALIGN_RIGHT);
        }
        return table;
    }

    private Paragraph buildTableTitle(String text, Font font) {
        Paragraph p = new Paragraph(text, font);
        p.setSpacingBefore(8);
        p.setSpacingAfter(6);
        return p;
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

    private void addTotalCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setBorder(Rectangle.TOP);
        cell.setBorderColor(GOLD_COLOR);
        cell.setBorderWidthTop(2);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
    }

    private void addSeparator(Document document) throws DocumentException {
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

    private String formatMoney(BigDecimal value) {
        if (value == null) return "0,00";
        return String.format("%,.2f", value).replace(",", " ").replace(".", ",");
    }
}