/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna <p/> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version. <p/> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. <p/> You should
 * have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.ws.versFascicoli.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.eng.parer.ws.dto.CSChiave;

/**
 *
 * @author fioravanti_f
 */
public class DatiXmlProfiloGenerale {

    private String oggettoFascicolo;
    private Date dataApertura;
    private Date dataChiusura;
    private CSChiave primoDocumento;
    private CSChiave ultimoDocumento;
    private BigDecimal tempoConservazione;
    private Long idPrimoDocumento;
    private Long idUltimoDocumento;
    private String noteFascicolo;
    private String lvlRiservatezza;
    private List<DXPGAmminPartecipante> ammPartecipanti;
    private List<DXPGSoggettoCoinvolto> soggettiCoinvolti;
    private List<DXPGRespFascicolo> responsabili;
    private DXPGAmminTitolare ammtitolare;
    private List<String> uoOrgResponsabili;
    private DXPGProcAmmininistrativo procAmm;

    public String getOggettoFascicolo() {
        return oggettoFascicolo;
    }

    public void setOggettoFascicolo(String oggettoFascicolo) {
        this.oggettoFascicolo = oggettoFascicolo;
    }

    public Date getDataApertura() {
        return dataApertura;
    }

    public void setDataApertura(Date dataApertura) {
        this.dataApertura = dataApertura;
    }

    public Date getDataChiusura() {
        return dataChiusura;
    }

    public void setDataChiusura(Date dataChiusura) {
        this.dataChiusura = dataChiusura;
    }

    public CSChiave getPrimoDocumento() {
        return primoDocumento;
    }

    public void setPrimoDocumento(CSChiave primoDocumento) {
        this.primoDocumento = primoDocumento;
    }

    public CSChiave getUltimoDocumento() {
        return ultimoDocumento;
    }

    public void setUltimoDocumento(CSChiave ultimoDocumento) {
        this.ultimoDocumento = ultimoDocumento;
    }

    public BigDecimal getTempoConservazione() {
        return tempoConservazione;
    }

    public void setTempoConservazione(BigDecimal tempoConservazione) {
        this.tempoConservazione = tempoConservazione;
    }

    public Long getIdPrimoDocumento() {
        return idPrimoDocumento;
    }

    public void setIdPrimoDocumento(Long idPrimoDocumento) {
        this.idPrimoDocumento = idPrimoDocumento;
    }

    public Long getIdUltimoDocumento() {
        return idUltimoDocumento;
    }

    public void setIdUltimoDocumento(Long idUltimoDocumento) {
        this.idUltimoDocumento = idUltimoDocumento;
    }

    public String getNoteFascicolo() {
        return noteFascicolo;
    }

    public void setNoteFascicolo(String noteFascicolo) {
        this.noteFascicolo = noteFascicolo;
    }

    public List<DXPGAmminPartecipante> getAmmPartecipanti() {
        if (ammPartecipanti == null) {
            ammPartecipanti = new ArrayList<>(0);
        }
        return ammPartecipanti;
    }

    public void setAmmPartecipanti(List<DXPGAmminPartecipante> ammPartecipanti) {
        this.ammPartecipanti = ammPartecipanti;
    }

    public DXPGAmminPartecipante addAmminPartecipante(DXPGAmminPartecipante amminPartecipante) {
        getAmmPartecipanti().add(amminPartecipante);
        return amminPartecipante;
    }

    public DXPGAmminPartecipante removeAmminPartecipanteFasc(
            DXPGAmminPartecipante amminPartecipante) {
        getAmmPartecipanti().remove(amminPartecipante);
        return amminPartecipante;
    }

    //
    public List<DXPGSoggettoCoinvolto> getSoggettiCoinvolti() {
        if (soggettiCoinvolti == null) {
            soggettiCoinvolti = new ArrayList<>(0);
        }
        return soggettiCoinvolti;
    }

    public void setSoggettiCoinvolti(List<DXPGSoggettoCoinvolto> soggettiCoinvolti) {
        this.soggettiCoinvolti = soggettiCoinvolti;
    }

    public DXPGSoggettoCoinvolto addSoggettoCoinvolto(DXPGSoggettoCoinvolto soggetto) {
        getSoggettiCoinvolti().add(soggetto);
        return soggetto;
    }

    public DXPGSoggettoCoinvolto removeSoggettoCoinvolto(DXPGSoggettoCoinvolto soggetto) {
        getSoggettiCoinvolti().remove(soggetto);
        return soggetto;
    }

    public List<DXPGRespFascicolo> getResponsabili() {
        if (responsabili == null) {
            responsabili = new ArrayList<DXPGRespFascicolo>(0);
        }
        return responsabili;
    }

    public void setResponsabili(List<DXPGRespFascicolo> responsabili) {
        this.responsabili = responsabili;
    }

    public DXPGRespFascicolo addResponsabile(DXPGRespFascicolo responsabile) {
        getResponsabili().add(responsabile);
        return responsabile;
    }

    public DXPGRespFascicolo removeResponsabile(DXPGRespFascicolo responsabile) {
        getResponsabili().remove(responsabile);
        return responsabile;
    }

    public DXPGAmminTitolare getAmmtitolare() {
        return ammtitolare;
    }

    public void setAmmtitolare(DXPGAmminTitolare ammtitolare) {
        this.ammtitolare = ammtitolare;
    }

    public String getLvlRiservatezza() {
        return lvlRiservatezza;
    }

    public void setLvlRiservatezza(String lvlRiservatezza) {
        this.lvlRiservatezza = lvlRiservatezza;
    }

    public List<String> getUoOrgResponsabili() {
        if (uoOrgResponsabili == null) {
            uoOrgResponsabili = new ArrayList<String>(0);
        }
        return uoOrgResponsabili;
    }

    public void setUoOrgResponsabili(List<String> uoOrgResponsabili) {
        this.uoOrgResponsabili = uoOrgResponsabili;
    }

    public String addUoOrgResponsabile(String uo) {
        getUoOrgResponsabili().add(uo);
        return uo;
    }

    public DXPGRespFascicolo removeUoOrgResponsabile(DXPGRespFascicolo uo) {
        getResponsabili().remove(uo);
        return uo;
    }

    public DXPGProcAmmininistrativo getProcAmm() {
        return procAmm;
    }

    public void setProcAmm(DXPGProcAmmininistrativo procAmm) {
        this.procAmm = procAmm;
    }

}
