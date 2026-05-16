package pl.taxraj.taxraj.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.taxraj.taxraj.repository.UzytkownikRepository;

@Service
public class UzytkownikService implements UserDetailsService {

    private final UzytkownikRepository uzytkownikRepository;

    public UzytkownikService(UzytkownikRepository uzytkownikRepository) {
        this.uzytkownikRepository = uzytkownikRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return uzytkownikRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Nie znaleziono użytkownika: " + email
                ));
    }
}