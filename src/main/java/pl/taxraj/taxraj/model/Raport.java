package pl.taxraj.taxraj.model;

import jakarta.persistence.*;
import pl.taxraj.taxraj.model.enums.TypRejestru;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "raporty")
public class Raport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "typ_rejestru")
    private TypRejestru typRejestru;

    @Column(precision = 12, scale = 2)
    private BigDecimal podstawa;

    @Column(name = "vat_naliczony", precision = 12, scale = 2)
    private BigDecimal vatNaliczony;

    @Column(name = "vat_nalezny", precision = 12, scale = 2)
    private BigDecimal vatNalezny;

    @Column(name = "data_wygenerowania")
    private LocalDateTime dataWygenerowania;

    @PrePersist
    protected void onCreate() {
        this.dataWygenerowania = LocalDateTime.now();
    }

    public Raport() {}

    public BigDecimal obliczSaldo() {
        BigDecimal nal = vatNalezny != null ? vatNalezny : BigDecimal.ZERO;
        BigDecimal nali = vatNaliczony != null ? vatNaliczony : BigDecimal.ZERO;
        return nal.subtract(nali);
    }

    public String generujZestawienie() {
        return "Raport: " + typRejestru + ", saldo = " + obliczSaldo();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TypRejestru getTypRejestru() { return typRejestru; }
    public void setTypRejestru(TypRejestru typRejestru) { this.typRejestru = typRejestru; }

    public BigDecimal getPodstawa() { return podstawa; }
    public void setPodstawa(BigDecimal podstawa) { this.podstawa = podstawa; }

    public BigDecimal getVatNaliczony() { return vatNaliczony; }
    public void setVatNaliczony(BigDecimal vatNaliczony) { this.vatNaliczony = vatNaliczony; }

    public BigDecimal getVatNalezny() { return vatNalezny; }
    public void setVatNalezny(BigDecimal vatNalezny) { this.vatNalezny = vatNalezny; }

    public LocalDateTime getDataWygenerowania() { return dataWygenerowania; }
    public void setDataWygenerowania(LocalDateTime dataWygenerowania) { this.dataWygenerowania = dataWygenerowania; }
}