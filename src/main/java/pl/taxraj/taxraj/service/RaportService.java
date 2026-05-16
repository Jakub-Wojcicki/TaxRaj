package pl.taxraj.taxraj.service;

import org.springframework.stereotype.Service;
import pl.taxraj.taxraj.model.Faktura;
import pl.taxraj.taxraj.model.Klient;
import pl.taxraj.taxraj.model.enums.TypFaktury;
import pl.taxraj.taxraj.repository.FakturaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RaportService {

    private final FakturaRepository fakturaRepository;

    public RaportService(FakturaRepository fakturaRepository) {
        this.fakturaRepository = fakturaRepository;
    }

    /** Zestawienie VAT — podstawa do deklaracji VAT-7 */
    public Map<String, Object> zestawienieVAT(Klient klient, String okresStr) {
        YearMonth okres = YearMonth.parse(okresStr);
        LocalDate od = okres.atDay(1);
        LocalDate doDaty = okres.atEndOfMonth();

        List<Faktura> faktury = fakturaRepository
                .findByKlientAndDataWystawieniaBetween(klient, od, doDaty);

        // Sprzedaż
        List<Faktura> sprzedaz = faktury.stream()
                .filter(f -> f.getTyp() == TypFaktury.SPRZEDAZOWA)
                .toList();

        BigDecimal netSprzedaz = sumuj(sprzedaz, Faktura::getKwotaNetto);
        BigDecimal vatNalezny = sumuj(sprzedaz, Faktura::getKwotaVat);

        // Zakup
        List<Faktura> zakup = faktury.stream()
                .filter(f -> f.getTyp() == TypFaktury.KOSZTOWA)
                .toList();

        BigDecimal netZakup = sumuj(zakup, Faktura::getKwotaNetto);
        BigDecimal vatNaliczony = sumuj(zakup, Faktura::getKwotaVat);

        // Saldo
        BigDecimal saldo = vatNalezny.subtract(vatNaliczony);

        Map<String, Object> wynik = new HashMap<>();
        wynik.put("klient", klient);
        wynik.put("okres", okresStr);
        wynik.put("sprzedaz", sprzedaz);
        wynik.put("zakup", zakup);
        wynik.put("netSprzedaz", netSprzedaz);
        wynik.put("vatNalezny", vatNalezny);
        wynik.put("netZakup", netZakup);
        wynik.put("vatNaliczony", vatNaliczony);
        wynik.put("saldo", saldo);
        wynik.put("saldoDoZaplaty", saldo.signum() > 0);
        return wynik;
    }

    /** Rejestr (sprzedaży lub zakupów) */
    public Map<String, Object> rejestr(Klient klient, String okresStr, TypFaktury typ) {
        YearMonth okres = YearMonth.parse(okresStr);
        LocalDate od = okres.atDay(1);
        LocalDate doDaty = okres.atEndOfMonth();

        List<Faktura> faktury = fakturaRepository
                .findByKlientAndDataWystawieniaBetween(klient, od, doDaty)
                .stream()
                .filter(f -> f.getTyp() == typ)
                .sorted((a, b) -> a.getDataWystawienia().compareTo(b.getDataWystawienia()))
                .toList();

        BigDecimal sumaNetto = sumuj(faktury, Faktura::getKwotaNetto);
        BigDecimal sumaVat = sumuj(faktury, Faktura::getKwotaVat);
        BigDecimal sumaBrutto = sumuj(faktury, Faktura::getKwotaBrutto);

        Map<String, Object> wynik = new HashMap<>();
        wynik.put("klient", klient);
        wynik.put("okres", okresStr);
        wynik.put("typ", typ);
        wynik.put("faktury", faktury);
        wynik.put("sumaNetto", sumaNetto);
        wynik.put("sumaVat", sumaVat);
        wynik.put("sumaBrutto", sumaBrutto);
        return wynik;
    }

    private BigDecimal sumuj(List<Faktura> faktury, java.util.function.Function<Faktura, BigDecimal> getter) {
        return faktury.stream()
                .map(f -> {
                    BigDecimal v = getter.apply(f);
                    return v != null ? v : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}