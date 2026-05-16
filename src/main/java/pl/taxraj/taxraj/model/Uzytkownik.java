package pl.taxraj.taxraj.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "uzytkownicy")
public class Uzytkownik implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imie;

    @Column(nullable = false)
    private String nazwisko;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String haslo;

    @Column(nullable = false)
    private String rola;        // np. "ADMIN", "KSIEGOWY"

    @Column(name = "utworzono")
    private LocalDateTime utworzono;

    @PrePersist
    protected void onCreate() {
        this.utworzono = LocalDateTime.now();
    }

    // Konstruktor pusty (wymagany przez JPA)
    public Uzytkownik() {}

    // Konstruktor z parametrami
    public Uzytkownik(String imie, String nazwisko, String email, String haslo, String rola) {
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.email = email;
        this.haslo = haslo;
        this.rola = rola;
    }

    // Gettery i settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImie() { return imie; }
    public void setImie(String imie) { this.imie = imie; }

    public String getNazwisko() { return nazwisko; }
    public void setNazwisko(String nazwisko) { this.nazwisko = nazwisko; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHaslo() { return haslo; }
    public void setHaslo(String haslo) { this.haslo = haslo; }

    public String getRola() { return rola; }
    public void setRola(String rola) { this.rola = rola; }

    public LocalDateTime getUtworzono() { return utworzono; }
    public void setUtworzono(LocalDateTime utworzono) { this.utworzono = utworzono; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rola));
    }

    @Override
    public String getPassword() {
        return haslo;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
