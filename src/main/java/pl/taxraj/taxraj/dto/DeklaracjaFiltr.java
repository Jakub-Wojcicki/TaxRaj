package pl.taxraj.taxraj.dto;

import pl.taxraj.taxraj.model.enums.StatusDeklaracji;
import pl.taxraj.taxraj.model.enums.TypDeklaracji;
import java.time.LocalDate;

public class DeklaracjaFiltr {
    private Long klientId;
    private TypDeklaracji typ;
    private StatusDeklaracji status;
    private String okres;
    private LocalDate zlozonaOd;
    private LocalDate zlozonaDo;

    public DeklaracjaFiltr() {}

    public Long getKlientId() { return klientId; }
    public void setKlientId(Long klientId) { this.klientId = klientId; }

    public TypDeklaracji getTyp() { return typ; }
    public void setTyp(TypDeklaracji typ) { this.typ = typ; }

    public StatusDeklaracji getStatus() { return status; }
    public void setStatus(StatusDeklaracji status) { this.status = status; }

    public String getOkres() { return okres; }
    public void setOkres(String okres) { this.okres = okres; }

    public LocalDate getZlozonaOd() { return zlozonaOd; }
    public void setZlozonaOd(LocalDate zlozonaOd) { this.zlozonaOd = zlozonaOd; }

    public LocalDate getZlozonaDo() { return zlozonaDo; }
    public void setZlozonaDo(LocalDate zlozonaDo) { this.zlozonaDo = zlozonaDo; }

    public boolean isPusty() {
        return klientId == null && typ == null && status == null
                && (okres == null || okres.isBlank())
                && zlozonaOd == null && zlozonaDo == null;
    }
}