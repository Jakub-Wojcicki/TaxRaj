package pl.taxraj.taxraj.service;

import org.springframework.stereotype.Service;
import pl.taxraj.taxraj.model.Klient;
import pl.taxraj.taxraj.repository.KlientRepository;
import java.util.List;
import java.util.Optional;

@Service
public class KlientService {

    private final KlientRepository klientRepository;

    public KlientService(KlientRepository klientRepository) {
        this.klientRepository = klientRepository;
    }

    public List<Klient> pobierzWszystkich() {
        return klientRepository.findAll();
    }

    public Optional<Klient> pobierzPoId(Long id) {
        return klientRepository.findById(id);
    }

    public Klient zapisz(Klient klient) {
        return klientRepository.save(klient);
    }

    public void usun(Long id) {
        klientRepository.deleteById(id);
    }
}