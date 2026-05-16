package pl.taxraj.taxraj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.taxraj.taxraj.model.Faktura;
import pl.taxraj.taxraj.model.Klient;
import pl.taxraj.taxraj.model.enums.StatusFaktury;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FakturaRepository extends JpaRepository<Faktura, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Faktura> {
    Optional<Faktura> findByNumer(String numer);
    List<Faktura> findByKlient(Klient klient);
    List<Faktura> findByStatus(StatusFaktury status);
    List<Faktura> findTop10ByOrderByDataWystawieniaDesc();
    List<Faktura> findByKlientAndDataWystawieniaBetween(Klient klient, LocalDate od, LocalDate doDaty);
    List<Faktura> findByTypAndDataWystawieniaBetweenOrderByNumerDesc(
            pl.taxraj.taxraj.model.enums.TypFaktury typ,
            java.time.LocalDate od,
            java.time.LocalDate doDaty
    );

    @Query("SELECT COALESCE(SUM(f.kwotaBrutto), 0) FROM Faktura f " +
            "WHERE f.dataWystawienia BETWEEN :od AND :doDaty")
    BigDecimal sumaPrzychodow(@Param("od") LocalDate od, @Param("doDaty") LocalDate doDaty);

    long countByDataWystawieniaBetween(LocalDate od, LocalDate doDaty);

    long countByStatus(StatusFaktury status);

    @Query("SELECT f.status, COUNT(f) FROM Faktura f GROUP BY f.status")
    List<Object[]> liczbaFakturWgStatusu();

    // Wszystkie faktury od daty — agregację zrobimy w Javie
    List<Faktura> findByDataWystawieniaGreaterThanEqualOrderByDataWystawieniaAsc(LocalDate od);
}