/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.dto;

import java.util.HashMap;
import java.util.List;

import it.eng.parer.ws.versamento.dto.DatoSpecifico;
import it.eng.parer.ws.versamento.dto.DocumentoVers;
import it.eng.parer.ws.versamento.dto.IDatiSpecEntity;

/**
 *
 * @author sinatti_s
 */
public class UpdDocumentoVers extends DocumentoVers implements java.io.Serializable, IDatiSpecEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 6310644609467033551L;

    private it.eng.parer.ws.xml.versUpdReq.DocumentoType rifUpdDocumento;
    private it.eng.parer.ws.xml.versUpdResp.DocumentoType rifUpdDocumentoResp;

    private List<UpdComponenteVers> updComponentiAttesi;
    private List<UpdComponenteVers> updSottoComponentiAttesi;

    private long idAbilTipoDocumentoDB;
    private String tipoDocNonverificato;
    private String catDocNonverificato;
    private String tipoStrutturaNonverificato;

    public it.eng.parer.ws.xml.versUpdReq.DocumentoType getRifUpdDocumento() {
        return rifUpdDocumento;
    }

    public void setRifUpdDocumento(it.eng.parer.ws.xml.versUpdReq.DocumentoType rifAggDocumento) {
        this.rifUpdDocumento = rifAggDocumento;
    }

    public it.eng.parer.ws.xml.versUpdResp.DocumentoType getRifUpdDocumentoResp() {
        return rifUpdDocumentoResp;
    }

    public void setRifUpdDocumentoResp(it.eng.parer.ws.xml.versUpdResp.DocumentoType rifAggDocumentoResp) {
        this.rifUpdDocumentoResp = rifAggDocumentoResp;
    }

    public List<UpdComponenteVers> getUpdComponentiAttesi() {
        return updComponentiAttesi;
    }

    public void setUpdComponentiAttesi(List<UpdComponenteVers> updComponentiAttesi) {
        this.updComponentiAttesi = updComponentiAttesi;
    }

    public List<UpdComponenteVers> getUpdSottoComponentiAttesi() {
        return updSottoComponentiAttesi;
    }

    public void setUpdSottoComponentiAttesi(List<UpdComponenteVers> updSottoComponentiAttesi) {
        this.updSottoComponentiAttesi = updSottoComponentiAttesi;
    }

    public long getIdAbilTipoDocumentoDB() {
        return idAbilTipoDocumentoDB;
    }

    public void setIdAbilTipoDocumentoDB(long idAbilTipoDocumentoDB) {
        this.idAbilTipoDocumentoDB = idAbilTipoDocumentoDB;
    }

    public String getTipoDocNonverificato() {
        return tipoDocNonverificato;
    }

    public void setTipoDocNonverificato(String tipoDocNonverificato) {
        this.tipoDocNonverificato = tipoDocNonverificato;
    }

    public String getTipoStrutturaNonverificato() {
        return tipoStrutturaNonverificato;
    }

    public void setTipoStrutturaNonverificato(String tipoStrutturaNonverificato) {
        this.tipoStrutturaNonverificato = tipoStrutturaNonverificato;
    }

    public String getCatDocNonverificato() {
        return catDocNonverificato;
    }

    public void setCatDocNonverificato(String catDocNonverificato) {
        this.catDocNonverificato = catDocNonverificato;
    }

    @Override
    public HashMap<String, DatoSpecifico> getDatiSpecifici() {
        if (super.getDatiSpecifici() == null) {
            super.datiSpecifici = new HashMap<String, DatoSpecifico>();
        }
        return super.getDatiSpecifici();
    }

    @Override
    public HashMap<String, DatoSpecifico> getDatiSpecificiMigrazione() {
        if (super.getDatiSpecificiMigrazione() == null) {
            super.datiSpecificiMigrazione = new HashMap<String, DatoSpecifico>();
        }
        return super.getDatiSpecificiMigrazione();
    }

}
