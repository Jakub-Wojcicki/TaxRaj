package pl.taxraj.taxraj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.taxraj.taxraj.model.Deklaracja;
import pl.taxraj.taxraj.model.Klient;
import pl.taxraj.taxraj.model.enums.StatusDeklaracji;
import java.time.LocalDate;
import java.util.List;

public interface DeklaracjaRepository extends JpaRepository<Deklaracja, Long>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<Deklaracja> {
    List<Deklaracja> findByKlient(Klient klient);
    List<Deklaracja> findByStatus(StatusDeklaracji status);

    long countByStatus(StatusDeklaracji status);
    long countByDataZlozeniaBetween(LocalDate od, LocalDate doDaty);

    @Query("SELECT d.status, COUNT(d) FROM Deklaracja d GROUP BY d.status")
    List<Object[]> liczbaDeklaracjiWgStatusu();
}