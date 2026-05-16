package pl.taxraj.taxraj.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import pl.taxraj.taxraj.model.enums.TypFaktury;
import pl.taxraj.taxraj.model.enums.StatusFaktury;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "faktury")

public class Faktura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Numer faktury jest wymagany")
    @Size(max = 50)
    @Column(nullable = false, unique = true)
    private String numer;

    @NotNull(message = "Typ faktury jest wymagany")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypFaktury typ;

    @NotNull(message = "Data wystawienia jest wymagana")
    @PastOrPresent(message = "Data wystawienia nie może być w przyszłości")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "data_wystawienia", nullable = false)
    private LocalDate dataWystawienia;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "data_platnosci")
    private LocalDate dataPlatnosci;

    @DecimalMin(value = "0.0", inclusive = true, message = "Kwota netto nie może być ujemna")
    @Column(name = "kwota_netto", precision = 12, scale = 2)
    private BigDecimal kwotaNetto;

    @DecimalMin(value = "0.0", inclusive = true, message = "Kwota VAT nie może być ujemna")
    @Column(name = "kwota_vat", precision = 12, scale = 2)
    private BigDecimal kwotaVat;

    @DecimalMin(value = "0.0", inclusive = true, message = "Kwota brutto nie może być ujemna")
    @Column(name = "kwota_brutto", precision = 12, scale = 2)
    private BigDecimal kwotaBrutto;

    @NotNull(message = "Status jest wymagany")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFaktury status;

    @NotNull(message = "Klient jest wymagany")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "klient_id", nullable = false)
    private Klient klient;

    @Column(name = "utworzono")
    private LocalDateTime utworzono;

    private String waluta = "PLN";

    // Relacja: wiele faktur tworzy jeden użytkownik
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uzytkownik_id")
    private Uzytkownik uzytkownik;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kontrahent_id")
    private Kontrahent kontrahent;

    // Relacja: jedna faktura ma wiele pozycji
    @OneToMany(mappedBy = "faktura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PozycjaFaktury> pozycje = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.utworzono = LocalDateTime.now();
    }

    public Faktura() {}

    // Gettery i settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumer() { return numer; }
    public void setNumer(String numer) { this.numer = numer; }

    public TypFaktury getTyp() { return typ; }
    public void setTyp(TypFaktury typ) { this.typ = typ; }

    public LocalDate getDataWystawienia() { return dataWystawienia; }
    public void setDataWystawienia(LocalDate dataWystawienia) { this.dataWystawienia = dataWystawienia; }

    public LocalDate getDataPlatnosci() { return dataPlatnosci; }
    public void setDataPlatnosci(LocalDate dataPlatnosci) { this.dataPlatnosci = dataPlatnosci; }

    public BigDecimal getKwotaNetto() { return kwotaNetto; }
    public void setKwotaNetto(BigDecimal kwotaNetto) { this.kwotaNetto = kwotaNetto; }

    public BigDecimal getKwotaVat() { return kwotaVat; }
    public void setKwotaVat(BigDecimal kwotaVat) { this.kwotaVat = kwotaVat; }

    public BigDecimal getKwotaBrutto() { return kwotaBrutto; }
    public void setKwotaBrutto(BigDecimal kwotaBrutto) { this.kwotaBrutto = kwotaBrutto; }

    public StatusFaktury getStatus() { return status; }
    public void setStatus(StatusFaktury status) { this.status = status; }

    public String getWaluta() { return waluta; }
    public void setWaluta(String waluta) { this.waluta = waluta; }

    public LocalDateTime getUtworzono() { return utworzono; }
    public void setUtworzono(LocalDateTime utworzono) { this.utworzono = utworzono; }

    public Klient getKlient() { return klient; }
    public void setKlient(Klient klient) { this.klient = klient; }

    public Uzytkownik getUzytkownik() { return uzytkownik; }
    public void setUzytkownik(Uzytkownik uzytkownik) { this.uzytkownik = uzytkownik; }

    public List<PozycjaFaktury> getPozycje() { return pozycje; }
    public void setPozycje(List<PozycjaFaktury> pozycje) { this.pozycje = pozycje; }

    public void dodajPozycje(PozycjaFaktury pozycja) {
        pozycje.add(pozycja);
        pozycja.setFaktura(this);
    }

    public void usunPozycje(PozycjaFaktury pozycja) {
        pozycje.remove(pozycja);
        pozycja.setFaktura(null);
    }

    public Kontrahent getKontrahent() { return kontrahent; }
    public void setKontrahent(Kontrahent kontrahent) { this.kontrahent = kontrahent; }

}
