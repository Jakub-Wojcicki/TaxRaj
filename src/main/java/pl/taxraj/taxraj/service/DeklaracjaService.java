package pl.taxraj.taxraj.service;

import org.springframework.stereotype.Service;
import pl.taxraj.taxraj.model.Deklaracja;
import pl.taxraj.taxraj.model.Faktura;
import pl.taxraj.taxraj.model.Klient;
import pl.taxraj.taxraj.model.enums.StatusDeklaracji;
import pl.taxraj.taxraj.model.enums.TypDeklaracji;
import pl.taxraj.taxraj.model.enums.TypFaktury;
import pl.taxraj.taxraj.repository.DeklaracjaRepository;
import pl.taxraj.taxraj.repository.FakturaRepository;
import pl.taxraj.taxraj.dto.DeklaracjaFiltr;
import pl.taxraj.taxraj.repository.specification.DeklaracjaSpecification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;


@Service
public class DeklaracjaService {

    private final DeklaracjaRepository deklaracjaRepository;
    private final FakturaRepository fakturaRepository;

    public DeklaracjaService(DeklaracjaRepository deklaracjaRepository,
                             FakturaRepository fakturaRepository) {
        this.deklaracjaRepository = deklaracjaRepository;
        this.fakturaRepository = fakturaRepository;
    }

    public List<Deklaracja> pobierzWszystkie() {
        return deklaracjaRepository.findAll();
    }

    public List<Deklaracja> pobierzZFiltrem(DeklaracjaFiltr filtr) {
        if (filtr == null || filtr.isPusty()) {
            return deklaracjaRepository.findAll();
        }
        return deklaracjaRepository.findAll(DeklaracjaSpecification.zFiltrem(filtr));
    }

    public List<Deklaracja> pobierzZFiltremISortowaniem(DeklaracjaFiltr filtr, Sort sort) {
        if (filtr == null || filtr.isPusty()) {
            return deklaracjaRepository.findAll(sort);
        }
        return deklaracjaRepository.findAll(DeklaracjaSpecification.zFiltrem(filtr), sort);
    }

    public Optional<Deklaracja> pobierzPoId(Long id) {
        return deklaracjaRepository.findById(id);
    }

    public Deklaracja zapisz(Deklaracja deklaracja) {
        return deklaracjaRepository.save(deklaracja);
    }

    public void usun(Long id) {
        deklaracjaRepository.deleteById(id);
    }

    /**
     * Generuje deklarację VAT z faktur klienta za podany okres.
     * - VAT należny = suma VAT z faktur sprzedażowych
     * - VAT naliczony = suma VAT z faktur kosztowych
     * - Podstawa = suma netto z faktur sprzedażowych
     */
    public Deklaracja generujZFaktur(Klient klient, String okres, TypDeklaracji typ) {
        // Konwersja "2026-05" → zakres dat
        YearMonth ym = YearMonth.parse(okres);  // wymaga formatu yyyy-MM
        LocalDate od = ym.atDay(1);
        LocalDate doDaty = ym.atEndOfMonth();

        List<Faktura> faktury = fakturaRepository
                .findByKlientAndDataWystawieniaBetween(klient, od, doDaty);

        BigDecimal vatNalezny = BigDecimal.ZERO;
        BigDecimal vatNaliczony = BigDecimal.ZERO;
        BigDecimal podstawa = BigDecimal.ZERO;

        for (Faktura f : faktury) {
            BigDecimal vat = f.getKwotaVat() != null ? f.getKwotaVat() : BigDecimal.ZERO;
            BigDecimal netto = f.getKwotaNetto() != null ? f.getKwotaNetto() : BigDecimal.ZERO;

            if (f.getTyp() == TypFaktury.SPRZEDAZOWA) {
                vatNalezny = vatNalezny.add(vat);
                podstawa = podstawa.add(netto);
            } else if (f.getTyp() == TypFaktury.KOSZTOWA) {
                vatNaliczony = vatNaliczony.add(vat);
            }
        }

        Deklaracja deklaracja = new Deklaracja();
        deklaracja.setTyp(typ);
        deklaracja.setOkres(okres);
        deklaracja.setStatus(StatusDeklaracji.ROBOCZA);
        deklaracja.setKlient(klient);
        deklaracja.setPodstawa(podstawa);
        deklaracja.setVatNaliczony(vatNaliczony);
        deklaracja.setVatNalezny(vatNalezny);

        return deklaracjaRepository.save(deklaracja);
    }
}