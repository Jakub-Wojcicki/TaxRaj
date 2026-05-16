package pl.taxraj.taxraj.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pl.taxraj.taxraj.dto.FakturaFiltr;
import pl.taxraj.taxraj.model.Faktura;

import java.util.ArrayList;
import java.util.List;

public class FakturaSpecification {

    public static Specification<Faktura> zFiltrem(FakturaFiltr filtr) {
        return (root, query, cb) -> {
            List<Predicate> warunki = new ArrayList<>();

            if (filtr.getKlientId() != null) {
                warunki.add(cb.equal(root.get("klient").get("id"), filtr.getKlientId()));
            }
            if (filtr.getTyp() != null) {
                warunki.add(cb.equal(root.get("typ"), filtr.getTyp()));
            }
            if (filtr.getStatus() != null) {
                warunki.add(cb.equal(root.get("status"), filtr.getStatus()));
            }
            if (filtr.getOd() != null) {
                warunki.add(cb.greaterThanOrEqualTo(root.get("dataWystawienia"), filtr.getOd()));
            }
            if (filtr.getDoDaty() != null) {
                warunki.add(cb.lessThanOrEqualTo(root.get("dataWystawienia"), filtr.getDoDaty()));
            }
            if (filtr.getNumer() != null && !filtr.getNumer().isBlank()) {
                warunki.add(cb.like(cb.lower(root.get("numer")),
                        "%" + filtr.getNumer().toLowerCase() + "%"));
            }

            return cb.and(warunki.toArray(new Predicate[0]));
        };
    }
}