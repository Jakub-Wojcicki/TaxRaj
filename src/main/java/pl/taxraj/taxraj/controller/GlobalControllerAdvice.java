package pl.taxraj.taxraj.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pl.taxraj.taxraj.model.Uzytkownik;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("zalogowany")
    public Uzytkownik zalogowanyUzytkownik() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Uzytkownik u) {
            return u;
        }
        return null;
    }
}