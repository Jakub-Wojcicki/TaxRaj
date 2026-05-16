package pl.taxraj.taxraj.model;

import jakarta.persistence.*;

@Entity
@Table(name = "kontrahenci")
public class Kontrahent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nazwa;

    @Column(unique = true)
    private String nip;

    private String adres;
    private String email;
    private String telefon;

    public Kontrahent() {}

    public Kontrahent(String nazwa, String nip, String adres, String email, String telefon) {
        this.nazwa = nazwa;
        this.nip = nip;
        this.adres = adres;
        this.email = email;
        this.telefon = telefon;
    }

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
}