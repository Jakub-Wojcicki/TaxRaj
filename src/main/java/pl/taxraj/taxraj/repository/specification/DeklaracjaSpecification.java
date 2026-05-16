package pl.taxraj.taxraj.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pl.taxraj.taxraj.dto.DeklaracjaFiltr;
import pl.taxraj.taxraj.model.Deklaracja;

import java.util.ArrayList;
import java.util.List;

public class DeklaracjaSpecification {

    public static Specification<Deklaracja> zFiltrem(DeklaracjaFiltr filtr) {
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
            if (filtr.getOkres() != null && !filtr.getOkres().isBlank()) {
                warunki.add(cb.like(cb.lower(root.get("okres")),
                        "%" + filtr.getOkres().toLowerCase() + "%"));
            }
            if (filtr.getZlozonaOd() != null) {
                warunki.add(cb.greaterThanOrEqualTo(root.get("dataZlozenia"), filtr.getZlozonaOd()));
            }
            if (filtr.getZlozonaDo() != null) {
                warunki.add(cb.lessThanOrEqualTo(root.get("dataZlozenia"), filtr.getZlozonaDo()));
            }

            return cb.and(warunki.toArray(new Predicate[0]));
        };
    }
}