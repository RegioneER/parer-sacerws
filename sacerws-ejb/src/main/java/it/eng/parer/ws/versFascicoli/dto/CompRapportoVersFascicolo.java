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

import javax.xml.datatype.XMLGregorianCalendar;

import it.eng.parer.ws.xml.versfascicoloresp.CodiceEsitoType;
import it.eng.parer.ws.xml.versfascicoloresp.ECConfigurazioneSIPType;
import it.eng.parer.ws.xml.versfascicoloresp.ECConfigurazioneType;
import it.eng.parer.ws.xml.versfascicoloresp.ECErroriUlterioriType;
import it.eng.parer.ws.xml.versfascicoloresp.ECEsitoChiamataWSType;
import it.eng.parer.ws.xml.versfascicoloresp.ECEsitoXSDType;
import it.eng.parer.ws.xml.versfascicoloresp.ECFascicoloType;
import it.eng.parer.ws.xml.versfascicoloresp.ECStatoConsType;
import it.eng.parer.ws.xml.versfascicoloresp.ECWarningUlterioriType;
import it.eng.parer.ws.xml.versfascicoloresp.EsitoGeneraleType;
import it.eng.parer.ws.xml.versfascicoloresp.EsitoVersamentoFascicolo;
import it.eng.parer.ws.xml.versfascicoloresp.EsitoVersamentoNegativoType;
import it.eng.parer.ws.xml.versfascicoloresp.RapportoVersamentoFascicoloType;
import it.eng.parer.ws.xml.versfascicoloresp.SIPType;

/**
 *
 * @author fioravanti_f
 */
public class CompRapportoVersFascicolo {

    EsitoVersamentoFascicolo esitoVersamentoFascicolo;

    RapportoVersamentoFascicoloType rapportoOk;
    EsitoVersamentoNegativoType rapportoKo;

    public CompRapportoVersFascicolo() {
        esitoVersamentoFascicolo = new EsitoVersamentoFascicolo();
        rapportoOk = new RapportoVersamentoFascicoloType();
        rapportoKo = new EsitoVersamentoNegativoType();
    }

    public EsitoVersamentoFascicolo produciEsitoFascicolo() {
        if (rapportoKo.getEsitoGenerale() != null
                && rapportoKo.getEsitoGenerale().getCodiceEsito() != null
                && rapportoKo.getEsitoGenerale().getCodiceEsito() == CodiceEsitoType.NEGATIVO) {
            esitoVersamentoFascicolo.setRapportoVersamentoFascicolo(null);
            esitoVersamentoFascicolo.setEsitoVersamentoNegativo(rapportoKo);
        } else {
            esitoVersamentoFascicolo.setRapportoVersamentoFascicolo(rapportoOk);
            esitoVersamentoFascicolo.setEsitoVersamentoNegativo(null);
        }
        return esitoVersamentoFascicolo;
    }

    public RapportoVersamentoFascicoloType getRapportoOk() {
        return rapportoOk;
    }

    public EsitoVersamentoNegativoType getRapportoKo() {
        return rapportoKo;
    }

    //
    public String getVersioneRapportoVersamento() {
        return esitoVersamentoFascicolo.getVersioneEsitoVersamentoFascicolo();
    }

    public void setVersioneRapportoVersamento(String value) {
        esitoVersamentoFascicolo.setVersioneEsitoVersamentoFascicolo(value);
        rapportoOk.setVersioneRapportoVersamento(value);
    }

    public String getVersioneIndiceSIPFascicolo() {
        return esitoVersamentoFascicolo.getVersioneIndiceSIPFascicolo();
    }

    public void setVersioneIndiceSIPFascicolo(String value) {
        esitoVersamentoFascicolo.setVersioneIndiceSIPFascicolo(value);
    }

    public XMLGregorianCalendar getDataRapportoVersamento() {
        return esitoVersamentoFascicolo.getDataEsitoVersamentoFascicolo();
    }

    public void setDataRapportoVersamento(XMLGregorianCalendar value) {
        esitoVersamentoFascicolo.setDataEsitoVersamentoFascicolo(value);
        rapportoOk.setDataRapportoVersamento(value);
    }

    // MEV#25288
    public String getURNSIP() {
        return esitoVersamentoFascicolo.getURNSIP();
    }

    public void setURNSIP(String value) {
        esitoVersamentoFascicolo.setURNSIP(value);
        rapportoOk.getSIP().setURNSIP(value);
    }
    // end MEV#25288

    //
    public String getIdentificativoRapportoVersamento() {
        return rapportoOk.getIdentificativoRapportoVersamento();
    }

    public void setIdentificativoRapportoVersamento(String value) {
        rapportoOk.setIdentificativoRapportoVersamento(value);
    }

    public SIPType getSIP() {
        return rapportoOk.getSIP();
    }

    public void setSIP(SIPType value) {
        rapportoOk.setSIP(value);
    }

    public EsitoGeneraleType getEsitoGenerale() {
        return rapportoOk.getEsitoGenerale();
    }

    public void setEsitoGenerale(EsitoGeneraleType value) {
        rapportoOk.setEsitoGenerale(value);
        rapportoKo.setEsitoGenerale(value);
    }

    public ECErroriUlterioriType getErroriUlteriori() {
        return rapportoKo.getErroriUlteriori();
    }

    public void setErroriUlteriori(ECErroriUlterioriType value) {
        rapportoKo.setErroriUlteriori(value);
    }

    public ECWarningUlterioriType getWarningUlteriori() {
        return rapportoOk.getWarningUlteriori();
    }

    public void setWarningUlteriori(ECWarningUlterioriType value) {
        rapportoOk.setWarningUlteriori(value);
        rapportoKo.setWarningUlteriori(value);
    }

    public ECEsitoChiamataWSType getEsitoChiamataWS() {
        return rapportoOk.getEsitoChiamataWS();
    }

    public void setEsitoChiamataWS(ECEsitoChiamataWSType value) {
        rapportoOk.setEsitoChiamataWS(value);
        rapportoKo.setEsitoChiamataWS(value);
    }

    public ECEsitoXSDType getEsitoXSD() {
        return rapportoOk.getEsitoXSD();
    }

    public void setEsitoXSD(ECEsitoXSDType value) {
        rapportoOk.setEsitoXSD(value);
        rapportoKo.setEsitoXSD(value);
    }

    public ECConfigurazioneSIPType getParametriVersamento() {
        return rapportoOk.getParametriVersamento();
    }

    public void setParametriVersamento(ECConfigurazioneSIPType value) {
        rapportoOk.setParametriVersamento(value);
        rapportoKo.setParametriVersamento(value);
    }

    public ECConfigurazioneType getConfigurazioneStruttura() {
        return rapportoOk.getConfigurazioneStruttura();
    }

    public void setConfigurazioneStruttura(ECConfigurazioneType value) {
        rapportoOk.setConfigurazioneStruttura(value);
        rapportoKo.setConfigurazioneStruttura(value);
    }

    public ECFascicoloType getFascicolo() {
        return rapportoOk.getFascicolo();
    }

    public void setFascicolo(ECFascicoloType value) {
        rapportoOk.setFascicolo(value);
        rapportoKo.setFascicolo(value);
    }

    public ECStatoConsType getStatoConservazione() {
        return rapportoOk.getStatoConservazione();
    }

    public void setStatoConservazione(ECStatoConsType value) {
        rapportoOk.setStatoConservazione(value);
    }

    public String getIndiceSIP() {
        return rapportoKo.getIndiceSIP();
    }

    public void setIndiceSIP(String value) {
        rapportoKo.setIndiceSIP(value);
    }

    public String getRapportoVersamento() {
        return rapportoKo.getRapportoVersamento();
    }

    public void setRapportoVersamento(String value) {
        rapportoKo.setRapportoVersamento(value);
    }

}
