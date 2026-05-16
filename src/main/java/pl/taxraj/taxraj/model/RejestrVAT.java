package pl.taxraj.taxraj.model;

import jakarta.persistence.*;
import pl.taxraj.taxraj.model.enums.TypRejestru;
import java.math.BigDecimal;

@Entity
@Table(name = "rejestr_vat")
public class RejestrVAT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "typ_rejestru", nullable = false)
    private TypRejestru typRejestru;

    @Column(nullable = false)
    private String okres;

    @Column(precision = 12, scale = 2)
    private BigDecimal podstawa;

    @Column(name = "vat_naliczony", precision = 12, scale = 2)
    private BigDecimal vatNaliczony;

    @Column(name = "vat_nalezny", precision = 12, scale = 2)
    private BigDecimal vatNalezny;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faktura_id")
    private Faktura faktura;

    public RejestrVAT() {}

    public BigDecimal obliczSaldo() {
        BigDecimal nal = vatNalezny != null ? vatNalezny : BigDecimal.ZERO;
        BigDecimal nali = vatNaliczony != null ? vatNaliczony : BigDecimal.ZERO;
        return nal.subtract(nali);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TypRejestru getTypRejestru() { return typRejestru; }
    public void setTypRejestru(TypRejestru typRejestru) { this.typRejestru = typRejestru; }

    public String getOkres() { return okres; }
    public void setOkres(String okres) { this.okres = okres; }

    public BigDecimal getPodstawa() { return podstawa; }
    public void setPodstawa(BigDecimal podstawa) { this.podstawa = podstawa; }

    public BigDecimal getVatNaliczony() { return vatNaliczony; }
    public void setVatNaliczony(BigDecimal vatNaliczony) { this.vatNaliczony = vatNaliczony; }

    public BigDecimal getVatNalezny() { return vatNalezny; }
    public void setVatNalezny(BigDecimal vatNalezny) { this.vatNalezny = vatNalezny; }

    public Faktura getFaktura() { return faktura; }
    public void setFaktura(Faktura faktura) { this.faktura = faktura; }
}
