package pl.taxraj.taxraj.dto;

import org.springframework.data.domain.Sort;

import java.util.Set;

public class Sortowanie {
    // Białą listą dopuszczalnych pól zabezpieczamy przed SQL injection
    private static final Set<String> DOZWOLONE_POLA_FAKTURY = Set.of(
            "numer", "typ", "dataWystawienia", "dataPlatnosci",
            "kwotaBrutto", "kwotaNetto", "status"
    );

    private static final Set<String> DOZWOLONE_POLA_DEKLARACJI = Set.of(
            "typ", "okres", "status", "dataZlozenia", "podstawa"
    );

    public static Sort dlaFaktur(String pole, String kierunek) {
        return zbuduj(pole, kierunek, DOZWOLONE_POLA_FAKTURY, "dataWystawienia");
    }

    public static Sort dlaDeklaracji(String pole, String kierunek) {
        return zbuduj(pole, kierunek, DOZWOLONE_POLA_DEKLARACJI, "okres");
    }

    private static Sort zbuduj(String pole, String kierunek, Set<String> dozwolone, String domyslne) {
        String wybranePole = (pole != null && dozwolone.contains(pole)) ? pole : domyslne;
        Sort.Direction direction = "ASC".equalsIgnoreCase(kierunek)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, wybranePole);
    }
}