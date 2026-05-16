package pl.taxraj.taxraj.service;

import org.springframework.stereotype.Service;
import pl.taxraj.taxraj.model.Deklaracja;
import pl.taxraj.taxraj.model.Faktura;
import pl.taxraj.taxraj.model.Klient;
import pl.taxraj.taxraj.model.enums.StatusDeklaracji;
import pl.taxraj.taxraj.model.enums.StatusFaktury;
import pl.taxraj.taxraj.model.enums.TypFaktury;
import pl.taxraj.taxraj.repository.DeklaracjaRepository;
import pl.taxraj.taxraj.repository.FakturaRepository;
import pl.taxraj.taxraj.repository.KlientRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardService {

    private final FakturaRepository fakturaRepository;
    private final KlientRepository klientRepository;
    private final DeklaracjaRepository deklaracjaRepository;

    public DashboardService(FakturaRepository fakturaRepository,
                            KlientRepository klientRepository,
                            DeklaracjaRepository deklaracjaRepository) {
        this.fakturaRepository = fakturaRepository;
        this.klientRepository = klientRepository;
        this.deklaracjaRepository = deklaracjaRepository;
    }

    /* ───────── WIDOK OGÓLNY (kancelaria) ───────── */
    public Map<String, Object> daneDashboardu(Sort sort) {
        Map<String, Object> dane = new HashMap<>();

        LocalDate dzis = LocalDate.now();
        LocalDate poczatekMiesiaca = dzis.withDayOfMonth(1);
        LocalDate koniecMiesiaca = dzis.withDayOfMonth(dzis.lengthOfMonth());
        LocalDate poczatekPoprzedniego = poczatekMiesiaca.minusMonths(1);
        LocalDate koniecPoprzedniego = poczatekMiesiaca.minusDays(1);

        dane.put("klienci", klientRepository.count());

        long fakturyMiesiac = fakturaRepository.countByDataWystawieniaBetween(poczatekMiesiaca, koniecMiesiaca);
        long fakturyPoprzedniMiesiac = fakturaRepository.countByDataWystawieniaBetween(poczatekPoprzedniego, koniecPoprzedniego);
        dane.put("fakturyMiesiac", fakturyMiesiac);
        dane.put("fakturyDelta", fakturyMiesiac - fakturyPoprzedniMiesiac);

        dane.put("deklaracjeRobocze", deklaracjaRepository.countByStatus(StatusDeklaracji.ROBOCZA));
        dane.put("deklaracjeZlozone",
                deklaracjaRepository.countByDataZlozeniaBetween(poczatekMiesiaca, koniecMiesiaca));

        // Wykres pączkowy
        List<Object[]> statusyRaw = deklaracjaRepository.liczbaDeklaracjiWgStatusu();
        Map<String, Long> wgStatusu = new LinkedHashMap<>();
        for (Object[] row : statusyRaw) {
            if (row[0] != null) {
                wgStatusu.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }
        dane.put("statusyDeklaracji", wgStatusu);

        // Wykres liniowy
        LocalDate od = dzis.minusMonths(5).withDayOfMonth(1);
        List<Faktura> faktury = fakturaRepository
                .findByDataWystawieniaGreaterThanEqualOrderByDataWystawieniaAsc(od);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Long> licznik = new TreeMap<>();
        for (int i = 0; i < 6; i++) {
            licznik.put(dzis.minusMonths(5 - i).format(fmt), 0L);
        }
        for (Faktura f : faktury) {
            licznik.merge(f.getDataWystawienia().format(fmt), 1L, Long::sum);
        }
        dane.put("miesiace", new ArrayList<>(licznik.keySet()));
        dane.put("licznikFakturMiesieczny", new ArrayList<>(licznik.values()));

        Sort sortFinal = (sort != null && sort.isSorted())
                ? sort
                : Sort.by(Sort.Direction.DESC, "dataWystawienia");
        dane.put("ostatnieFaktury", fakturaRepository.findAll(sortFinal));

        return dane;
    }

    /* ───────── WIDOK KLIENTA ───────── */
    public Map<String, Object> daneKlienta(Klient klient, Sort sort) {
        Map<String, Object> dane = new HashMap<>();

        LocalDate dzis = LocalDate.now();
        LocalDate poczatekMiesiaca = dzis.withDayOfMonth(1);
        LocalDate koniecMiesiaca = dzis.withDayOfMonth(dzis.lengthOfMonth());

        List<Faktura> fakturyKlienta = fakturaRepository.findByKlient(klient);
        List<Faktura> fakturyMiesiac = fakturaRepository
                .findByKlientAndDataWystawieniaBetween(klient, poczatekMiesiaca, koniecMiesiaca);

        BigDecimal przychody = fakturyMiesiac.stream()
                .filter(f -> f.getTyp() == TypFaktury.SPRZEDAZOWA)
                .map(f -> f.getKwotaBrutto() != null ? f.getKwotaBrutto() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal koszty = fakturyMiesiac.stream()
                .filter(f -> f.getTyp() == TypFaktury.KOSZTOWA)
                .map(f -> f.getKwotaBrutto() != null ? f.getKwotaBrutto() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal vatNalezny = fakturyMiesiac.stream()
                .filter(f -> f.getTyp() == TypFaktury.SPRZEDAZOWA)
                .map(f -> f.getKwotaVat() != null ? f.getKwotaVat() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal vatNaliczony = fakturyMiesiac.stream()
                .filter(f -> f.getTyp() == TypFaktury.KOSZTOWA)
                .map(f -> f.getKwotaVat() != null ? f.getKwotaVat() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldoVat = vatNalezny.subtract(vatNaliczony);

        BigDecimal niezaplacone = fakturyKlienta.stream()
                .filter(f -> f.getStatus() == StatusFaktury.NIEZAPLACONA
                        || f.getStatus() == StatusFaktury.PRZETERMINOWANA)
                .map(f -> f.getKwotaBrutto() != null ? f.getKwotaBrutto() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long niezaplaconeIle = fakturyKlienta.stream()
                .filter(f -> f.getStatus() == StatusFaktury.NIEZAPLACONA
                        || f.getStatus() == StatusFaktury.PRZETERMINOWANA)
                .count();

        dane.put("przychody", przychody);
        dane.put("koszty", koszty);
        dane.put("saldoVat", saldoVat);
        dane.put("niezaplacone", niezaplacone);
        dane.put("niezaplaconeIle", niezaplaconeIle);

        // Wykres liniowy
        LocalDate od = dzis.minusMonths(5).withDayOfMonth(1);
        List<Faktura> faktury6m = fakturaRepository
                .findByKlientAndDataWystawieniaBetween(klient, od, dzis);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, BigDecimal> przychodyM = new TreeMap<>();
        for (int i = 0; i < 6; i++) {
            przychodyM.put(dzis.minusMonths(5 - i).format(fmt), BigDecimal.ZERO);
        }
        for (Faktura f : faktury6m) {
            if (f.getTyp() == TypFaktury.SPRZEDAZOWA && f.getKwotaBrutto() != null) {
                przychodyM.merge(f.getDataWystawienia().format(fmt), f.getKwotaBrutto(), BigDecimal::add);
            }
        }
        dane.put("miesiace", new ArrayList<>(przychodyM.keySet()));
        dane.put("kwotyMiesieczne", new ArrayList<>(przychodyM.values()));

        Map<String, Long> wgStatusuFaktur = new LinkedHashMap<>();
        for (Faktura f : fakturyKlienta) {
            if (f.getStatus() != null) {
                wgStatusuFaktur.merge(f.getStatus().name(), 1L, Long::sum);
            }
        }
        dane.put("statusyFaktur", wgStatusuFaktur);
        dane.put("ostatnieDeklaracje", deklaracjaRepository.findByKlient(klient));

        // Top 10 faktur klienta z sortowaniem
        Sort sortFinal = (sort != null && sort.isSorted())
                ? sort
                : Sort.by(Sort.Direction.DESC, "dataWystawienia");

        List<Faktura> ostatnie = fakturyKlienta.stream()
                .sorted(stworzKomparator(sortFinal))
                .toList();
        dane.put("ostatnieFaktury", ostatnie);

        return dane;
    }

    // Helper — komparator z Sort dla widoku klienta (sortuje listę w pamięci)
    private Comparator<Faktura> stworzKomparator(Sort sort) {
        Comparator<Faktura> c = null;
        for (Sort.Order order : sort) {
            Comparator<Faktura> partial = switch (order.getProperty()) {
                case "numer"           -> Comparator.comparing(Faktura::getNumer, Comparator.nullsLast(String::compareTo));
                case "dataWystawienia" -> Comparator.comparing(Faktura::getDataWystawienia, Comparator.nullsLast(LocalDate::compareTo));
                case "kwotaBrutto"     -> Comparator.comparing(Faktura::getKwotaBrutto, Comparator.nullsLast(BigDecimal::compareTo));
                case "status"          -> Comparator.comparing(f -> f.getStatus() != null ? f.getStatus().name() : "");
                case "typ"             -> Comparator.comparing(f -> f.getTyp() != null ? f.getTyp().name() : "");
                default                -> Comparator.comparing(Faktura::getDataWystawienia, Comparator.nullsLast(LocalDate::compareTo));
            };
            if (order.isDescending()) partial = partial.reversed();
            c = (c == null) ? partial : c.thenComparing(partial);
        }
        return c != null ? c : Comparator.comparing(Faktura::getDataWystawienia, Comparator.nullsLast(LocalDate::compareTo)).reversed();
    }
}