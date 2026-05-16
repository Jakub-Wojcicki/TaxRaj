package pl.taxraj.taxraj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.taxraj.taxraj.model.PozycjaFaktury;

public interface PozycjaFakturyRepository extends JpaRepository<PozycjaFaktury, Long> {
}