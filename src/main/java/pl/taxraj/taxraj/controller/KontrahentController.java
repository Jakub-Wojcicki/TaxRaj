package pl.taxraj.taxraj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.taxraj.taxraj.model.Kontrahent;
import pl.taxraj.taxraj.service.KontrahentService;

@Controller
@RequestMapping("/kontrahenci")
public class KontrahentController {

    private final KontrahentService kontrahentService;

    public KontrahentController(KontrahentService kontrahentService) {
        this.kontrahentService = kontrahentService;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("kontrahenci", kontrahentService.pobierzWszystkich());
        return "kontrahenci";
    }

    @GetMapping("/nowy")
    public String formularzDodawania(Model model) {
        model.addAttribute("kontrahent", new Kontrahent());
        return "kontrahent-form";
    }

    @GetMapping("/{id}/edytuj")
    public String formularzEdycji(@PathVariable Long id, Model model) {
        Kontrahent kontrahent = kontrahentService.pobierzPoId(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono kontrahenta o ID " + id));
        model.addAttribute("kontrahent", kontrahent);
        return "kontrahent-form";
    }

    @PostMapping("/zapisz")
    public String zapisz(@ModelAttribute Kontrahent kontrahent) {
        kontrahentService.zapisz(kontrahent);
        return "redirect:/kontrahenci";
    }

    @GetMapping("/{id}/usun")
    public String usun(@PathVariable Long id) {
        kontrahentService.usun(id);
        return "redirect:/kontrahenci";
    }
}