package pl.taxraj.taxraj.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.taxraj.taxraj.model.Klient;
import pl.taxraj.taxraj.model.enums.TypFaktury;
import pl.taxraj.taxraj.repository.KlientRepository;
import pl.taxraj.taxraj.service.RaportPdfService;
import pl.taxraj.taxraj.service.RaportService;
import pl.taxraj.taxraj.service.RaportXlsxService;

import java.util.Map;

@Controller
@RequestMapping("/raporty")
public class RaportController {

    private final RaportService raportService;
    private final RaportPdfService raportPdfService;
    private final RaportXlsxService raportXlsxService;
    private final KlientRepository klientRepository;

    public RaportController(RaportService raportService,
                            RaportPdfService raportPdfService,
                            RaportXlsxService raportXlsxService,
                            KlientRepository klientRepository) {
        this.raportService = raportService;
        this.raportPdfService = raportPdfService;
        this.raportXlsxService = raportXlsxService;
        this.klientRepository = klientRepository;
    }

    @GetMapping
    public String menu() {
        return "raporty";
    }

    // ────── WIDOK HTML ──────

    @GetMapping("/vat")
    public String zestawienieVAT(@RequestParam(required = false) Long klientId,
                                 @RequestParam(required = false) String okres,
                                 Model model) {
        model.addAttribute("klienci", klientRepository.findAll());
        model.addAttribute("wybranyKlientId", klientId);

        String okresDoWyswietlenia = (okres != null && !okres.isBlank())
                ? okres
                : java.time.YearMonth.now().toString();
        model.addAttribute("wybranyOkres", okresDoWyswietlenia);

        if (klientId != null && okres != null && !okres.isBlank()) {
            Klient klient = klientRepository.findById(klientId).orElseThrow();
            model.addAllAttributes(raportService.zestawienieVAT(klient, okres));
            model.addAttribute("wygenerowany", true);
        }
        return "raport-vat";
    }

    @GetMapping("/rejestr-sprzedazy")
    public String rejestrSprzedazy(@RequestParam(required = false) Long klientId,
                                   @RequestParam(required = false) String okres,
                                   Model model) {
        return rejestr(klientId, okres, TypFaktury.SPRZEDAZOWA, "Rejestr sprzedaży", model);
    }

    @GetMapping("/rejestr-zakupow")
    public String rejestrZakupow(@RequestParam(required = false) Long klientId,
                                 @RequestParam(required = false) String okres,
                                 Model model) {
        return rejestr(klientId, okres, TypFaktury.KOSZTOWA, "Rejestr zakupów", model);
    }

    private String rejestr(Long klientId, String okres, TypFaktury typ, String tytul, Model model) {
        model.addAttribute("klienci", klientRepository.findAll());
        model.addAttribute("wybranyKlientId", klientId);
        model.addAttribute("wybranyOkres", okres);
        model.addAttribute("tytul", tytul);
        model.addAttribute("typFaktury", typ);
        model.addAttribute("czySprzedaz", typ == TypFaktury.SPRZEDAZOWA);

        String okresDoWyswietlenia = (okres != null && !okres.isBlank())
                ? okres
                : java.time.YearMonth.now().toString();
        model.addAttribute("wybranyOkres", okresDoWyswietlenia);

        if (klientId != null && okres != null && !okres.isBlank()) {
            Klient klient = klientRepository.findById(klientId).orElseThrow();
            model.addAllAttributes(raportService.rejestr(klient, okres, typ));
            model.addAttribute("wygenerowany", true);
        }
        return "raport-rejestr";
    }

    // ────── EKSPORTY ──────

    @GetMapping("/vat/pdf")
    public ResponseEntity<byte[]> pdfVAT(@RequestParam Long klientId, @RequestParam String okres) throws Exception {
        Klient klient = klientRepository.findById(klientId).orElseThrow();
        Map<String, Object> dane = raportService.zestawienieVAT(klient, okres);
        byte[] pdf = raportPdfService.generujZestawienieVAT(dane);
        return pdfResponse(pdf, "Zestawienie_VAT_" + klient.getNazwa().replaceAll("\\s+", "_") + "_" + okres + ".pdf");
    }

    @GetMapping("/vat/xlsx")
    public ResponseEntity<byte[]> xlsxVAT(@RequestParam Long klientId, @RequestParam String okres) throws Exception {
        Klient klient = klientRepository.findById(klientId).orElseThrow();
        Map<String, Object> dane = raportService.zestawienieVAT(klient, okres);
        byte[] xlsx = raportXlsxService.generujZestawienieVAT(dane);
        return xlsxResponse(xlsx, "Zestawienie_VAT_" + klient.getNazwa().replaceAll("\\s+", "_") + "_" + okres + ".xlsx");
    }

    @GetMapping("/rejestr-sprzedazy/pdf")
    public ResponseEntity<byte[]> pdfRejestrSprzedazy(@RequestParam Long klientId, @RequestParam String okres) throws Exception {
        return rejestrPdf(klientId, okres, TypFaktury.SPRZEDAZOWA, "Rejestr sprzedaży");
    }

    @GetMapping("/rejestr-sprzedazy/xlsx")
    public ResponseEntity<byte[]> xlsxRejestrSprzedazy(@RequestParam Long klientId, @RequestParam String okres) throws Exception {
        return rejestrXlsx(klientId, okres, TypFaktury.SPRZEDAZOWA, "Rejestr sprzedaży");
    }

    @GetMapping("/rejestr-zakupow/pdf")
    public ResponseEntity<byte[]> pdfRejestrZakupow(@RequestParam Long klientId, @RequestParam String okres) throws Exception {
        return rejestrPdf(klientId, okres, TypFaktury.KOSZTOWA, "Rejestr zakupów");
    }

    @GetMapping("/rejestr-zakupow/xlsx")
    public ResponseEntity<byte[]> xlsxRejestrZakupow(@RequestParam Long klientId, @RequestParam String okres) throws Exception {
        return rejestrXlsx(klientId, okres, TypFaktury.KOSZTOWA, "Rejestr zakupów");
    }

    private ResponseEntity<byte[]> rejestrPdf(Long klientId, String okres, TypFaktury typ, String tytul) throws Exception {
        Klient klient = klientRepository.findById(klientId).orElseThrow();
        Map<String, Object> dane = raportService.rejestr(klient, okres, typ);
        byte[] pdf = raportPdfService.generujRejestr(dane, tytul);
        return pdfResponse(pdf, tytul.replaceAll("\\s+", "_") + "_" + klient.getNazwa().replaceAll("\\s+", "_") + "_" + okres + ".pdf");
    }

    private ResponseEntity<byte[]> rejestrXlsx(Long klientId, String okres, TypFaktury typ, String tytul) throws Exception {
        Klient klient = klientRepository.findById(klientId).orElseThrow();
        Map<String, Object> dane = raportService.rejestr(klient, okres, typ);
        byte[] xlsx = raportXlsxService.generujRejestr(dane, tytul);
        return xlsxResponse(xlsx, tytul.replaceAll("\\s+", "_") + "_" + klient.getNazwa().replaceAll("\\s+", "_") + "_" + okres + ".xlsx");
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] data, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", filename);
        return new ResponseEntity<>(data, headers, org.springframework.http.HttpStatus.OK);
    }

    private ResponseEntity<byte[]> xlsxResponse(byte[] data, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);
        return new ResponseEntity<>(data, headers, org.springframework.http.HttpStatus.OK);
    }
}