package pl.taxraj.taxraj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.taxraj.taxraj.model.Raport;

public interface RaportRepository extends JpaRepository<Raport, Long> {
}