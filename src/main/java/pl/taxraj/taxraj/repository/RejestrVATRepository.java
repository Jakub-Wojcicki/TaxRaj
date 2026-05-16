package pl.taxraj.taxraj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.taxraj.taxraj.model.RejestrVAT;
import java.util.List;

public interface RejestrVATRepository extends JpaRepository<RejestrVAT, Long> {
    List<RejestrVAT> findByOkres(String okres);
}