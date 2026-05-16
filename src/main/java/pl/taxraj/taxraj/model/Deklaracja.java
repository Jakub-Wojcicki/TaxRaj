package pl.taxraj.taxraj.model;

import jakarta.persistence.*;
import pl.taxraj.taxraj.model.enums.TypDeklaracji;
import pl.taxraj.taxraj.model.enums.StatusDeklaracji;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "deklaracje")
public class Deklaracja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Typ deklaracji jest wymagany")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypDeklaracji typ;

    @NotBlank(message = "Okres jest wymagany")
    @Pattern(regexp = "^\\d{4}-\\d{2}(-Q[1-4])?$",
            message = "Okres musi mieć format YYYY-MM lub YYYY-Q1")
    @Column(nullable = false)
    private String okres;

    @NotNull(message = "Status jest wymagany")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDeklaracji status;

    @DecimalMin(value = "0.0", inclusive = true, message = "Podstawa nie może być ujemna")
    @Column(precision = 12, scale = 2)
    private BigDecimal podstawa;

    @DecimalMin(value = "0.0", inclusive = true, message = "VAT naliczony nie może być ujemny")
    @Column(name = "vat_naliczony", precision = 12, scale = 2)
    private BigDecimal vatNaliczony;

    @DecimalMin(value = "0.0", inclusive = true, message = "VAT należny nie może być ujemny")
    @Column(name = "vat_nalezny", precision = 12, scale = 2)
    private BigDecimal vatNalezny;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "data_zlozenia")
    private LocalDate dataZlozenia;

    @Column(name = "utworzono")
    private LocalDateTime utworzono;

    @NotNull(message = "Klient jest wymagany")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "klient_id", nullable = false)
    private Klient klient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uzytkownik_id")
    private Uzytkownik uzytkownik;

    @PrePersist
    protected void onCreate() {
        this.utworzono = LocalDateTime.now();
    }

    public Deklaracja() {}

    public BigDecimal obliczSaldo() {
        BigDecimal nal = vatNalezny != null ? vatNalezny : BigDecimal.ZERO;
        BigDecimal nali = vatNaliczony != null ? vatNaliczony : BigDecimal.ZERO;
        return nal.subtract(nali);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TypDeklaracji getTyp() { return typ; }
    public void setTyp(TypDeklaracji typ) { this.typ = typ; }

    public String getOkres() { return okres; }
    public void setOkres(String okres) { this.okres = okres; }

    public StatusDeklaracji getStatus() { return status; }
    public void setStatus(StatusDeklaracji status) { this.status = status; }

    public BigDecimal getPodstawa() { return podstawa; }
    public void setPodstawa(BigDecimal podstawa) { this.podstawa = podstawa; }

    public BigDecimal getVatNaliczony() { return vatNaliczony; }
    public void setVatNaliczony(BigDecimal vatNaliczony) { this.vatNaliczony = vatNaliczony; }

    public BigDecimal getVatNalezny() { return vatNalezny; }
    public void setVatNalezny(BigDecimal vatNalezny) { this.vatNalezny = vatNalezny; }

    public LocalDate getDataZlozenia() { return dataZlozenia; }
    public void setDataZlozenia(LocalDate dataZlozenia) { this.dataZlozenia = dataZlozenia; }

    public LocalDateTime getUtworzono() { return utworzono; }
    public void setUtworzono(LocalDateTime utworzono) { this.utworzono = utworzono; }

    public Klient getKlient() { return klient; }
    public void setKlient(Klient klient) { this.klient = klient; }

    public Uzytkownik getUzytkownik() { return uzytkownik; }
    public void setUzytkownik(Uzytkownik uzytkownik) { this.uzytkownik = uzytkownik; }
}