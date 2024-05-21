/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.eng.parer.ws.versamento.dto.DatoSpecifico;
import it.eng.parer.ws.versamento.dto.DocumentoVers;

/**
 *
 * @author sinatti_s
 */
public class UpdDocumentoVers extends DocumentoVers {

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
    public Map<String, DatoSpecifico> getDatiSpecifici() {
        if (super.getDatiSpecifici() == null) {
            super.datiSpecifici = new HashMap<String, DatoSpecifico>();
        }
        return super.getDatiSpecifici();
    }

    @Override
    public Map<String, DatoSpecifico> getDatiSpecificiMigrazione() {
        if (super.getDatiSpecificiMigrazione() == null) {
            super.datiSpecificiMigrazione = new HashMap<String, DatoSpecifico>();
        }
        return super.getDatiSpecificiMigrazione();
    }

}
