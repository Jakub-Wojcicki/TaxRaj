package pl.taxraj.taxraj.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pozycje_faktury")
public class PozycjaFaktury {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nazwa;

    @Column(precision = 10, scale = 2)
    private BigDecimal ilosc;

    private String jednostka;

    @Column(name = "cena_netto", precision = 12, scale = 2)
    private BigDecimal cenaNetto;

    @Column(name = "stawka_vat", precision = 5, scale = 2)
    private BigDecimal stawkaVat;       // np. 23.00

    // Relacja zwrotna: każda pozycja należy do jednej faktury
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faktura_id", nullable = false)
    private Faktura faktura;

    public PozycjaFaktury() {}

    // Metoda pomocnicza
    public BigDecimal obliczWartoscNetto() {
        if (ilosc == null || cenaNetto == null) return BigDecimal.ZERO;
        return ilosc.multiply(cenaNetto);
    }

    public BigDecimal obliczWartoscBrutto() {
        BigDecimal netto = obliczWartoscNetto();
        if (stawkaVat == null) return netto;
        BigDecimal vat = netto.multiply(stawkaVat).divide(new BigDecimal("100"));
        return netto.add(vat);
    }

    // Gettery i settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNazwa() { return nazwa; }
    public void setNazwa(String nazwa) { this.nazwa = nazwa; }

    public BigDecimal getIlosc() { return ilosc; }
    public void setIlosc(BigDecimal ilosc) { this.ilosc = ilosc; }

    public String getJednostka() { return jednostka; }
    public void setJednostka(String jednostka) { this.jednostka = jednostka; }

    public BigDecimal getCenaNetto() { return cenaNetto; }
    public void setCenaNetto(BigDecimal cenaNetto) { this.cenaNetto = cenaNetto; }

    public BigDecimal getStawkaVat() { return stawkaVat; }
    public void setStawkaVat(BigDecimal stawkaVat) { this.stawkaVat = stawkaVat; }

    public Faktura getFaktura() { return faktura; }
    public void setFaktura(Faktura faktura) { this.faktura = faktura; }

}