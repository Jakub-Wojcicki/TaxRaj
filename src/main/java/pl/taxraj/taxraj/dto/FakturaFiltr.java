package pl.taxraj.taxraj.dto;

import pl.taxraj.taxraj.model.enums.StatusFaktury;
import pl.taxraj.taxraj.model.enums.TypFaktury;
import java.time.LocalDate;

public class FakturaFiltr {
    private Long klientId;
    private TypFaktury typ;
    private StatusFaktury status;
    private LocalDate od;
    private LocalDate doDaty;
    private String numer;       // wyszukiwanie po fragmencie numeru

    public FakturaFiltr() {}

    public Long getKlientId() { return klientId; }
    public void setKlientId(Long klientId) { this.klientId = klientId; }

    public TypFaktury getTyp() { return typ; }
    public void setTyp(TypFaktury typ) { this.typ = typ; }

    public StatusFaktury getStatus() { return status; }
    public void setStatus(StatusFaktury status) { this.status = status; }

    public LocalDate getOd() { return od; }
    public void setOd(LocalDate od) { this.od = od; }

    public LocalDate getDoDaty() { return doDaty; }
    public void setDoDaty(LocalDate doDaty) { this.doDaty = doDaty; }

    public String getNumer() { return numer; }
    public void setNumer(String numer) { this.numer = numer; }

    public boolean isPusty() {
        return klientId == null && typ == null && status == null
                && od == null && doDaty == null
                && (numer == null || numer.isBlank());
    }
}