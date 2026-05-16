package pl.taxraj.taxraj.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.taxraj.taxraj.model.Deklaracja;
import pl.taxraj.taxraj.model.Klient;
import pl.taxraj.taxraj.model.Uzytkownik;
import pl.taxraj.taxraj.model.enums.StatusDeklaracji;
import pl.taxraj.taxraj.model.enums.TypDeklaracji;
import pl.taxraj.taxraj.repository.KlientRepository;
import pl.taxraj.taxraj.service.DeklaracjaService;
import pl.taxraj.taxraj.dto.DeklaracjaFiltr;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import pl.taxraj.taxraj.dto.Sortowanie;

@Controller
@RequestMapping("/deklaracje")
public class DeklaracjaController {

    private final DeklaracjaService deklaracjaService;
    private final KlientRepository klientRepository;

    public DeklaracjaController(DeklaracjaService deklaracjaService,
                                KlientRepository klientRepository) {
        this.deklaracjaService = deklaracjaService;
        this.klientRepository = klientRepository;
    }

    @GetMapping
    public String lista(@ModelAttribute DeklaracjaFiltr filtr,
                        @RequestParam(required = false) String sort,
                        @RequestParam(required = false) String kierunek,
                        Model model) {
        Sort sortowanie = Sortowanie.dlaDeklaracji(sort, kierunek);

        model.addAttribute("deklaracje", deklaracjaService.pobierzZFiltremISortowaniem(filtr, sortowanie));
        model.addAttribute("filtr", filtr);
        model.addAttribute("klienci", klientRepository.findAll());
        model.addAttribute("typy", TypDeklaracji.values());
        model.addAttribute("statusy", StatusDeklaracji.values());
        model.addAttribute("sortPole", sort != null ? sort : "okres");
        model.addAttribute("sortKierunek", kierunek != null ? kierunek : "DESC");

        return "deklaracje";
    }

    @GetMapping("/nowa")
    public String formularzDodawania(Model model) {
        Deklaracja deklaracja = new Deklaracja();
        deklaracja.setStatus(StatusDeklaracji.ROBOCZA);
        model.addAttribute("deklaracja", deklaracja);
        wypelnijSlowniki(model);
        return "deklaracja-form";
    }

    @GetMapping("/{id}/edytuj")
    public String formularzEdycji(@PathVariable Long id, Model model) {
        Deklaracja deklaracja = deklaracjaService.pobierzPoId(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono deklaracji o ID " + id));
        model.addAttribute("deklaracja", deklaracja);
        wypelnijSlowniki(model);
        return "deklaracja-form";
    }

    @PostMapping("/zapisz")
    public String zapisz(@Valid @ModelAttribute("deklaracja") Deklaracja deklaracja,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            wypelnijSlowniki(model);
            return "deklaracja-form";
        }

        if (deklaracja.getId() == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Uzytkownik u) {
                deklaracja.setUzytkownik(u);
            }
        } else {
            deklaracjaService.pobierzPoId(deklaracja.getId())
                    .ifPresent(istniejaca -> deklaracja.setUzytkownik(istniejaca.getUzytkownik()));
        }
        deklaracjaService.zapisz(deklaracja);
        return "redirect:/deklaracje";
    }

    @GetMapping("/{id}/usun")
    public String usun(@PathVariable Long id) {
        deklaracjaService.usun(id);
        return "redirect:/deklaracje";
    }

    // ── NOWE: generowanie z faktur ──

    @GetMapping("/generuj")
    public String formularzGenerowania(Model model) {
        wypelnijSlowniki(model);
        return "deklaracja-generuj";
    }

    @PostMapping("/generuj")
    public String generuj(@RequestParam Long klientId,
                          @RequestParam String okres,
                          @RequestParam TypDeklaracji typ) {

        Klient klient = klientRepository.findById(klientId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono klienta o ID " + klientId));

        Deklaracja zapisana = deklaracjaService.generujZFaktur(klient, okres, typ);

        // Ustaw zalogowanego użytkownika
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Uzytkownik u) {
            zapisana.setUzytkownik(u);
            deklaracjaService.zapisz(zapisana);
        }

        // Przekieruj do edycji wygenerowanej deklaracji
        return "redirect:/deklaracje/" + zapisana.getId() + "/edytuj";
    }

    private void wypelnijSlowniki(Model model) {
        model.addAttribute("klienci", klientRepository.findAll());
        model.addAttribute("typy", TypDeklaracji.values());
        model.addAttribute("statusy", StatusDeklaracji.values());
    }
}