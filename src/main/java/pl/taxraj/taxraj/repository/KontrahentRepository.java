package pl.taxraj.taxraj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.taxraj.taxraj.model.Kontrahent;
import java.util.Optional;

public interface KontrahentRepository extends JpaRepository<Kontrahent, Long> {
    Optional<Kontrahent> findByNip(String nip);
}