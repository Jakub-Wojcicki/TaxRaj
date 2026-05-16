package pl.taxraj.taxraj.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "klienci")
public class Klient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nazwa jest wymagana")
    @Size(max = 200, message = "Nazwa nie może przekraczać 200 znaków")
    @Column(nullable = false)
    private String nazwa;

    @Pattern(regexp = "^$|^\\d{10}$", message = "NIP musi mieć dokładnie 10 cyfr")
    @Column(unique = true)
    private String nip;

    @Size(max = 200)
    private String adres;

    @Email(message = "Nieprawidłowy adres email")
    private String email;

    @Pattern(regexp = "^$|^[+]?[\\d\\s-]{6,20}$", message = "Nieprawidłowy numer telefonu")
    private String telefon;

    @Column(name = "utworzono")
    private LocalDateTime utworzono;

    @PrePersist
    protected void onCreate() {
        this.utworzono = LocalDateTime.now();
    }

    public Klient() {}

    public Klient(String nazwa, String nip, String adres, String email, String telefon) {
        this.nazwa = nazwa;
        this.nip = nip;
        this.adres = adres;
        this.email = email;
        this.telefon = telefon;
    }

    // Gettery i settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNazwa() { return nazwa; }
    public void setNazwa(String nazwa) { this.nazwa = nazwa; }

    public String getNip() { return nip; }
    public void setNip(String nip) { this.nip = nip; }

    public String getAdres() { return adres; }
    public void setAdres(String adres) { this.adres = adres; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public LocalDateTime getUtworzono() { return utworzono; }
    public void setUtworzono(LocalDateTime utworzono) { this.utworzono = utworzono; }

}
