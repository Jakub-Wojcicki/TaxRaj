package pl.taxraj.taxraj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.taxraj.taxraj.model.KsiegaRachunkowa;

public interface KsiegaRachunkowaRepository extends JpaRepository<KsiegaRachunkowa, Long> {
}