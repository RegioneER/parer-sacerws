package it.eng.parer.ws.versamentoUpd.dto;

import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.versamento.dto.IDatiSpecEntity;

/**
 *
 * @author sinatti_s
 */
public class UpdComponenteVers extends ComponenteVers implements java.io.Serializable, IDatiSpecEntity {

    private it.eng.parer.ws.xml.versUpdReq.ComponenteType myUpdComponente;
    private it.eng.parer.ws.xml.versUpdReq.SottoComponenteType myUpdSottoComponente;
    private UpdDocumentoVers rifAggDocumentoVers;

    private String keyCtrl;

    private String tipoComponenteNonVerificato;

    public it.eng.parer.ws.xml.versUpdReq.ComponenteType getMyUpdComponente() {
        return myUpdComponente;
    }

    public void setMyUpdComponente(it.eng.parer.ws.xml.versUpdReq.ComponenteType myAggComponente) {
        this.myUpdComponente = myAggComponente;
    }

    public it.eng.parer.ws.xml.versUpdReq.SottoComponenteType getMyUpdSottoComponente() {
        return myUpdSottoComponente;
    }

    public void setMyUpdSottoComponente(it.eng.parer.ws.xml.versUpdReq.SottoComponenteType myAggSottoComponente) {
        this.myUpdSottoComponente = myAggSottoComponente;
    }

    public UpdDocumentoVers getRifUpdDocumentoVers() {
        return rifAggDocumentoVers;
    }

    public void setRifUpdDocumentoVers(UpdDocumentoVers rifAggDocumentoVers) {
        this.rifAggDocumentoVers = rifAggDocumentoVers;
    }

    public String getTipoComponenteNonVerificato() {
        return tipoComponenteNonVerificato;
    }

    public void setTipoComponenteNonVerificato(String tipoComponenteNonVerificato) {
        this.tipoComponenteNonVerificato = tipoComponenteNonVerificato;
    }

    public String getKeyCtrl() {
        return keyCtrl;
    }

    public void setKeyCtrl(String keyCtrl) {
        this.keyCtrl = keyCtrl;
    }

}
