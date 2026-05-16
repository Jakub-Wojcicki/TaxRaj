package pl.taxraj.taxraj.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import pl.taxraj.taxraj.model.Faktura;
import pl.taxraj.taxraj.model.Klient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class RaportXlsxService {

    @SuppressWarnings("unchecked")
    public byte[] generujZestawienieVAT(Map<String, Object> dane) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Klient klient = (Klient) dane.get("klient");
            String okres = (String) dane.get("okres");
            List<Faktura> sprzedaz = (List<Faktura>) dane.get("sprzedaz");
            List<Faktura> zakup = (List<Faktura>) dane.get("zakup");
            BigDecimal vatNalezny = (BigDecimal) dane.get("vatNalezny");
            BigDecimal vatNaliczony = (BigDecimal) dane.get("vatNaliczony");
            BigDecimal saldo = (BigDecimal) dane.get("saldo");

            CellStyle titleStyle = createTitleStyle(wb);
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle moneyStyle = createMoneyStyle(wb);
            CellStyle totalStyle = createTotalStyle(wb);
            CellStyle labelStyle = createLabelStyle(wb);

            // Arkusz 1: Podsumowanie
            Sheet podsumowanie = wb.createSheet("Podsumowanie");

            Row r0 = podsumowanie.createRow(0);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("ZESTAWIENIE VAT");
            c0.setCellStyle(titleStyle);

            Row r1 = podsumowanie.createRow(1);
            r1.createCell(0).setCellValue("Klient: " + klient.getNazwa() + " | Okres: " + okres);

            int rowIdx = 3;
            addRow(podsumowanie, rowIdx++, "VAT należny (sprzedaż)", vatNalezny, labelStyle, moneyStyle);
            addRow(podsumowanie, rowIdx++, "VAT naliczony (zakup)", vatNaliczony, labelStyle, moneyStyle);
            addRow(podsumowanie, rowIdx++, "SALDO VAT", saldo, labelStyle, totalStyle);

            podsumowanie.setColumnWidth(0, 6000);
            podsumowanie.setColumnWidth(1, 4000);

            // Arkusz 2: Sprzedaż
            if (!sprzedaz.isEmpty()) {
                addFakturySheet(wb, "Sprzedaż", sprzedaz, headerStyle, moneyStyle);
            }

            // Arkusz 3: Zakupy
            if (!zakup.isEmpty()) {
                addFakturySheet(wb, "Zakupy", zakup, headerStyle, moneyStyle);
            }

            wb.write(baos);
            return baos.toByteArray();
        }
    }

    @SuppressWarnings("unchecked")
    public byte[] generujRejestr(Map<String, Object> dane, String tytul) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Klient klient = (Klient) dane.get("klient");
            String okres = (String) dane.get("okres");
            List<Faktura> faktury = (List<Faktura>) dane.get("faktury");

            CellStyle titleStyle = createTitleStyle(wb);
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle moneyStyle = createMoneyStyle(wb);
            CellStyle totalStyle = createTotalStyle(wb);

            Sheet sheet = wb.createSheet(tytul.length() > 31 ? tytul.substring(0, 31) : tytul);

            Row r0 = sheet.createRow(0);
            Cell c0 = r0.createCell(0);
            c0.setCellValue(tytul);
            c0.setCellStyle(titleStyle);

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Klient: " + klient.getNazwa() + " | Okres: " + okres);

            // Nagłówki tabeli
            Row hr = sheet.createRow(3);
            String[] headers = {"Lp.", "Numer", "Data", "Kontrahent", "Netto", "VAT", "Brutto"};
            for (int i = 0; i < headers.length; i++) {
                Cell c = hr.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // Dane
            int rowIdx = 4;
            int lp = 1;
            BigDecimal sumaNetto = BigDecimal.ZERO;
            BigDecimal sumaVat = BigDecimal.ZERO;
            BigDecimal sumaBrutto = BigDecimal.ZERO;

            for (Faktura f : faktury) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(lp++);
                r.createCell(1).setCellValue(f.getNumer());
                r.createCell(2).setCellValue(f.getDataWystawienia().toString());
                r.createCell(3).setCellValue(f.getKontrahent() != null ? f.getKontrahent().getNazwa() : "—");

                Cell netto = r.createCell(4);
                netto.setCellValue(f.getKwotaNetto() != null ? f.getKwotaNetto().doubleValue() : 0);
                netto.setCellStyle(moneyStyle);

                Cell vat = r.createCell(5);
                vat.setCellValue(f.getKwotaVat() != null ? f.getKwotaVat().doubleValue() : 0);
                vat.setCellStyle(moneyStyle);

                Cell brutto = r.createCell(6);
                brutto.setCellValue(f.getKwotaBrutto() != null ? f.getKwotaBrutto().doubleValue() : 0);
                brutto.setCellStyle(moneyStyle);

                if (f.getKwotaNetto() != null)  sumaNetto = sumaNetto.add(f.getKwotaNetto());
                if (f.getKwotaVat() != null)    sumaVat = sumaVat.add(f.getKwotaVat());
                if (f.getKwotaBrutto() != null) sumaBrutto = sumaBrutto.add(f.getKwotaBrutto());
            }

            // Podsumowanie
            Row total = sheet.createRow(rowIdx);
            Cell totalLabel = total.createCell(3);
            totalLabel.setCellValue("RAZEM:");
            totalLabel.setCellStyle(totalStyle);

            Cell tN = total.createCell(4); tN.setCellValue(sumaNetto.doubleValue());   tN.setCellStyle(totalStyle);
            Cell tV = total.createCell(5); tV.setCellValue(sumaVat.doubleValue());     tV.setCellStyle(totalStyle);
            Cell tB = total.createCell(6); tB.setCellValue(sumaBrutto.doubleValue());  tB.setCellStyle(totalStyle);

            // Auto-szerokość kolumn
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(baos);
            return baos.toByteArray();
        }
    }

    // ────── HELPERY ──────

    private void addFakturySheet(Workbook wb, String name, List<Faktura> faktury,
                                 CellStyle headerStyle, CellStyle moneyStyle) {
        Sheet sheet = wb.createSheet(name);
        Row hr = sheet.createRow(0);
        String[] headers = {"Numer", "Data", "Kontrahent", "Netto", "VAT", "Brutto"};
        for (int i = 0; i < headers.length; i++) {
            Cell c = hr.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }
        int rowIdx = 1;
        for (Faktura f : faktury) {
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue(f.getNumer());
            r.createCell(1).setCellValue(f.getDataWystawienia().toString());
            r.createCell(2).setCellValue(f.getKontrahent() != null ? f.getKontrahent().getNazwa() : "—");

            Cell c4 = r.createCell(3);
            c4.setCellValue(f.getKwotaNetto() != null ? f.getKwotaNetto().doubleValue() : 0);
            c4.setCellStyle(moneyStyle);

            Cell c5 = r.createCell(4);
            c5.setCellValue(f.getKwotaVat() != null ? f.getKwotaVat().doubleValue() : 0);
            c5.setCellStyle(moneyStyle);

            Cell c6 = r.createCell(5);
            c6.setCellValue(f.getKwotaBrutto() != null ? f.getKwotaBrutto().doubleValue() : 0);
            c6.setCellStyle(moneyStyle);
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private void addRow(Sheet sheet, int rowIdx, String label, BigDecimal value,
                        CellStyle labelStyle, CellStyle moneyStyle) {
        Row r = sheet.createRow(rowIdx);
        Cell l = r.createCell(0);
        l.setCellValue(label);
        l.setCellStyle(labelStyle);

        Cell v = r.createCell(1);
        v.setCellValue(value != null ? value.doubleValue() : 0);
        v.setCellStyle(moneyStyle);
    }

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 16);
        s.setFont(f);
        return s;
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle createMoneyStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0.00 zł"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    private CellStyle createTotalStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_YELLOW.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0.00 zł"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    private CellStyle createLabelStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        return s;
    }
}