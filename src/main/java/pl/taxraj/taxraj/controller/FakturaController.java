package pl.taxraj.taxraj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.taxraj.taxraj.model.Faktura;
import pl.taxraj.taxraj.model.PozycjaFaktury;
import pl.taxraj.taxraj.model.enums.StatusFaktury;
import pl.taxraj.taxraj.model.enums.TypFaktury;
import pl.taxraj.taxraj.repository.KlientRepository;
import pl.taxraj.taxraj.repository.KontrahentRepository;
import pl.taxraj.taxraj.service.FakturaService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import pl.taxraj.taxraj.service.FakturaPdfService;
import pl.taxraj.taxraj.dto.FakturaFiltr;
import pl.taxraj.taxraj.model.enums.StatusFaktury;
import org.springframework.data.domain.Sort;
import pl.taxraj.taxraj.dto.Sortowanie;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/faktury")
public class FakturaController {

    private final FakturaService fakturaService;
    private final KlientRepository klientRepository;
    private final KontrahentRepository kontrahentRepository;
    private final FakturaPdfService fakturaPdfService;

    public FakturaController(FakturaService fakturaService,
                             KlientRepository klientRepository,
                             KontrahentRepository kontrahentRepository, FakturaPdfService fakturaPdfService) {
        this.fakturaService = fakturaService;
        this.klientRepository = klientRepository;
        this.kontrahentRepository = kontrahentRepository;
        this.fakturaPdfService = fakturaPdfService;
    }

    @GetMapping
    public String lista(@ModelAttribute FakturaFiltr filtr,
                        @RequestParam(required = false) String sort,
                        @RequestParam(required = false) String kierunek,
                        Model model) {
        Sort sortowanie = Sortowanie.dlaFaktur(sort, kierunek);

        model.addAttribute("faktury", fakturaService.pobierzZFiltremISortowaniem(filtr, sortowanie));
        model.addAttribute("filtr", filtr);
        model.addAttribute("klienci", klientRepository.findAll());
        model.addAttribute("typy", TypFaktury.values());
        model.addAttribute("statusy", StatusFaktury.values());

        // Dane do generowania linków sortujących
        model.addAttribute("sortPole", sort != null ? sort : "dataWystawienia");
        model.addAttribute("sortKierunek", kierunek != null ? kierunek : "DESC");

        return "faktury";
    }

    @GetMapping("/{id}")
    public String szczegoly(@PathVariable Long id, Model model) {
        Faktura faktura = fakturaService.pobierzPoId(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono faktury o ID " + id));
        model.addAttribute("faktura", faktura);
        return "faktura-szczegoly";
    }

    @GetMapping("/nowa")
    public String formularzDodawania(Model model) {
        Faktura faktura = new Faktura();
        faktura.setTyp(TypFaktury.SPRZEDAZOWA);
        faktura.setNumer(fakturaService.generujKolejnyNumer(TypFaktury.SPRZEDAZOWA));  // ← NOWE
        faktura.setDataWystawienia(java.time.LocalDate.now());                          // ← BONUS

        PozycjaFaktury pierwsza = new PozycjaFaktury();
        pierwsza.setIlosc(java.math.BigDecimal.ONE);
        faktura.getPozycje().add(pierwsza);

        model.addAttribute("faktura", faktura);
        wypelnijSlowniki(model);
        return "faktura-form";
    }

    @GetMapping("/{id}/edytuj")
    public String formularzEdycji(@PathVariable Long id, Model model) {
        Faktura faktura = fakturaService.pobierzPoId(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono faktury o ID " + id));
        if (faktura.getPozycje().isEmpty()) {
            faktura.getPozycje().add(new PozycjaFaktury());
        }
        model.addAttribute("faktura", faktura);
        wypelnijSlowniki(model);
        return "faktura-form";
    }

    @GetMapping("/generuj-numer")
    @ResponseBody
    public String generujNumer(@RequestParam TypFaktury typ) {
        return fakturaService.generujKolejnyNumer(typ);
    }

    @PostMapping("/zapisz")
    public String zapisz(@Valid @ModelAttribute("faktura") Faktura faktura,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            wypelnijSlowniki(model);   // bez tego rozwijane listy są puste
            return "faktura-form";
        }

        // Usuń puste wiersze
        List<PozycjaFaktury> niepuste = new ArrayList<>();
        if (faktura.getPozycje() != null) {
            for (PozycjaFaktury p : faktura.getPozycje()) {
                if (p.getNazwa() != null && !p.getNazwa().isBlank()) {
                    p.setFaktura(faktura);
                    niepuste.add(p);
                }
            }
        }
        faktura.setPozycje(niepuste);

        // Auto-oblicz kwoty z pozycji
        if (!niepuste.isEmpty()) {
            BigDecimal sumaNetto = BigDecimal.ZERO;
            BigDecimal sumaBrutto = BigDecimal.ZERO;
            for (PozycjaFaktury p : niepuste) {
                sumaNetto = sumaNetto.add(p.obliczWartoscNetto());
                sumaBrutto = sumaBrutto.add(p.obliczWartoscBrutto());
            }
            faktura.setKwotaNetto(sumaNetto.setScale(2, RoundingMode.HALF_UP));
            faktura.setKwotaBrutto(sumaBrutto.setScale(2, RoundingMode.HALF_UP));
            faktura.setKwotaVat(sumaBrutto.subtract(sumaNetto).setScale(2, RoundingMode.HALF_UP));
        }

        fakturaService.zapisz(faktura);
        return "redirect:/faktury";
    }

    @GetMapping("/{id}/usun")
    public String usun(@PathVariable Long id) {
        fakturaService.usun(id);
        return "redirect:/faktury";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pobierzPdf(@PathVariable Long id) {
        Faktura faktura = fakturaService.pobierzPoId(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono faktury o ID " + id));

        try {
            byte[] pdf = fakturaPdfService.generujPdf(faktura);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline",
                    "Faktura_" + faktura.getNumer().replace("/", "_") + ".pdf");

            return new ResponseEntity<>(pdf, headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("Nie udało się wygenerować PDF: " + e.getMessage(), e);
        }
    }

    private void wypelnijSlowniki(Model model) {
        model.addAttribute("klienci", klientRepository.findAll());
        model.addAttribute("kontrahenci", kontrahentRepository.findAll());
        model.addAttribute("typy", TypFaktury.values());
        model.addAttribute("statusy", StatusFaktury.values());
    }
}