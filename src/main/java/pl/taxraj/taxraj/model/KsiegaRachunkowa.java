package pl.taxraj.taxraj.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ksiegi_rachunkowe")
public class KsiegaRachunkowa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "konto_winien", nullable = false)
    private String kontoWinien;

    @Column(name = "konto_ma", nullable = false)
    private String kontoMa;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal kwota;

    @Column(name = "data_ksiegowania", nullable = false)
    private LocalDate dataKsiegowania;

    @Column(length = 500)
    private String opis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faktura_id")
    private Faktura faktura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uzytkownik_id")
    private Uzytkownik uzytkownik;

    public KsiegaRachunkowa() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKontoWinien() { return kontoWinien; }
    public void setKontoWinien(String kontoWinien) { this.kontoWinien = kontoWinien; }

    public String getKontoMa() { return kontoMa; }
    public void setKontoMa(String kontoMa) { this.kontoMa = kontoMa; }

    public BigDecimal getKwota() { return kwota; }
    public void setKwota(BigDecimal kwota) { this.kwota = kwota; }

    public LocalDate getDataKsiegowania() { return dataKsiegowania; }
    public void setDataKsiegowania(LocalDate dataKsiegowania) { this.dataKsiegowania = dataKsiegowania; }

    public String getOpis() { return opis; }
    public void setOpis(String opis) { this.opis = opis; }

    public Faktura getFaktura() { return faktura; }
    public void setFaktura(Faktura faktura) { this.faktura = faktura; }

    public Uzytkownik getUzytkownik() { return uzytkownik; }
    public void setUzytkownik(Uzytkownik uzytkownik) { this.uzytkownik = uzytkownik; }
}
