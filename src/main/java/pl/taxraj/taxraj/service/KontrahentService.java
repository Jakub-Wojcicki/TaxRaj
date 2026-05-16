package pl.taxraj.taxraj.service;

import org.springframework.stereotype.Service;
import pl.taxraj.taxraj.model.Kontrahent;
import pl.taxraj.taxraj.repository.KontrahentRepository;
import java.util.List;
import java.util.Optional;

@Service
public class KontrahentService {

    private final KontrahentRepository kontrahentRepository;

    public KontrahentService(KontrahentRepository kontrahentRepository) {
        this.kontrahentRepository = kontrahentRepository;
    }

    public List<Kontrahent> pobierzWszystkich() {
        return kontrahentRepository.findAll();
    }

    public Optional<Kontrahent> pobierzPoId(Long id) {
        return kontrahentRepository.findById(id);
    }

    public Kontrahent zapisz(Kontrahent kontrahent) {
        return kontrahentRepository.save(kontrahent);
    }

    public void usun(Long id) {
        kontrahentRepository.deleteById(id);
    }
}