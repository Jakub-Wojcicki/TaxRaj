package pl.taxraj.taxraj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.taxraj.taxraj.model.Klient;
import java.util.Optional;

public interface KlientRepository extends JpaRepository<Klient, Long> {
    Optional<Klient> findByNip(String nip);
}