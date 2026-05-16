package pl.taxraj.taxraj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.taxraj.taxraj.model.Klient;
import pl.taxraj.taxraj.repository.KlientRepository;
import pl.taxraj.taxraj.service.DashboardService;
import org.springframework.data.domain.Sort;
import pl.taxraj.taxraj.dto.Sortowanie;

@Controller
public class PageController {

    private final DashboardService dashboardService;
    private final KlientRepository klientRepository;

    public PageController(DashboardService dashboardService, KlientRepository klientRepository) {
        this.dashboardService = dashboardService;
        this.klientRepository = klientRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (error != null) model.addAttribute("blad", "Nieprawidłowy email lub hasło");
        if (logout != null) model.addAttribute("info", "Zostałeś wylogowany");
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Long klientId,
                            @RequestParam(required = false) String sort,
                            @RequestParam(required = false) String kierunek,
                            Model model) {
        model.addAttribute("listaKlientow", klientRepository.findAll());

        Sort sortowanie = Sortowanie.dlaFaktur(sort, kierunek);

        if (klientId != null) {
            Klient klient = klientRepository.findById(klientId)
                    .orElseThrow(() -> new RuntimeException("Nie znaleziono klienta o ID " + klientId));
            model.addAttribute("wybranyKlient", klient);
            model.addAllAttributes(dashboardService.daneKlienta(klient, sortowanie));
        } else {
            model.addAttribute("wybranyKlient", null);
            model.addAllAttributes(dashboardService.daneDashboardu(sortowanie));
        }

        // Atrybuty potrzebne do generowania linków sortujących
        model.addAttribute("sortPole", sort != null ? sort : "dataWystawienia");
        model.addAttribute("sortKierunek", kierunek != null ? kierunek : "DESC");

        return "dashboard";
    }
}