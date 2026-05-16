package pl.taxraj.taxraj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.taxraj.taxraj.model.Klient;
import pl.taxraj.taxraj.service.KlientService;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/klienci")
public class KlientController {

    private final KlientService klientService;

    public KlientController(KlientService klientService) {
        this.klientService = klientService;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("klienci", klientService.pobierzWszystkich());
        return "klienci";
    }

    @GetMapping("/nowy")
    public String formularzDodawania(Model model) {
        model.addAttribute("klient", new Klient());
        return "klient-form";
    }

    @GetMapping("/{id}/edytuj")
    public String formularzEdycji(@PathVariable Long id, Model model) {
        Klient klient = klientService.pobierzPoId(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono klienta o ID " + id));
        model.addAttribute("klient", klient);
        return "klient-form";
    }

    @PostMapping("/zapisz")
    public String zapisz(@Valid @ModelAttribute("klient") Klient klient, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "klient-form";   // zostań na formularzu, błędy się wyświetlą
        }
        klientService.zapisz(klient);
        return "redirect:/klienci";
    }

    @GetMapping("/{id}/usun")
    public String usun(@PathVariable Long id) {
        klientService.usun(id);
        return "redirect:/klienci";
    }
}