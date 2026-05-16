package pl.taxraj.taxraj.service;

import org.springframework.stereotype.Service;
import pl.taxraj.taxraj.model.Faktura;
import pl.taxraj.taxraj.repository.FakturaRepository;
import java.util.List;
import java.util.Optional;
import pl.taxraj.taxraj.model.enums.TypFaktury;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import pl.taxraj.taxraj.dto.FakturaFiltr;
import pl.taxraj.taxraj.repository.specification.FakturaSpecification;
import org.springframework.data.domain.Sort;

@Service
public class FakturaService {

    private final FakturaRepository fakturaRepository;

    public FakturaService(FakturaRepository fakturaRepository) {
        this.fakturaRepository = fakturaRepository;
    }

    public List<Faktura> pobierzWszystkie() {
        return fakturaRepository.findAll();
    }

    public Optional<Faktura> pobierzPoId(Long id) {
        return fakturaRepository.findById(id);
    }

    public List<Faktura> pobierzZFiltremISortowaniem(FakturaFiltr filtr, Sort sort) {
        if (filtr == null || filtr.isPusty()) {
            return fakturaRepository.findAll(sort);
        }
        return fakturaRepository.findAll(FakturaSpecification.zFiltrem(filtr), sort);
    }

    public Faktura zapisz(Faktura faktura) {
        return fakturaRepository.save(faktura);
    }

    public void usun(Long id) {
        fakturaRepository.deleteById(id);
    }

    public long policz() {
        return fakturaRepository.count();
    }

    public String generujKolejnyNumer(TypFaktury typ) {
        String prefix = (typ == TypFaktury.KOSZTOWA) ? "FK" : "FV";

        YearMonth biezacy = YearMonth.now();
        LocalDate od = biezacy.atDay(1);
        LocalDate doDaty = biezacy.atEndOfMonth();

        List<Faktura> fakturyMiesiaca = fakturaRepository
                .findByTypAndDataWystawieniaBetweenOrderByNumerDesc(typ, od, doDaty);

        int kolejnyNumer = 1;
        if (!fakturyMiesiaca.isEmpty()) {
            String ostatniNumer = fakturyMiesiaca.get(0).getNumer();
            // Wyciągnij ostatni segment po "/"
            String[] czesci = ostatniNumer.split("/");
            if (czesci.length > 0) {
                try {
                    kolejnyNumer = Integer.parseInt(czesci[czesci.length - 1]) + 1;
                } catch (NumberFormatException e) {
                    kolejnyNumer = fakturyMiesiaca.size() + 1; // fallback
                }
            }
        }

        return String.format("%s/%d/%02d/%03d",
                prefix,
                biezacy.getYear(),
                biezacy.getMonthValue(),
                kolejnyNumer);
    }
    public List<Faktura> pobierzZFiltrem(FakturaFiltr filtr) {
        if (filtr == null || filtr.isPusty()) {
            return fakturaRepository.findAll();
        }
        return fakturaRepository.findAll(FakturaSpecification.zFiltrem(filtr));
    }
}