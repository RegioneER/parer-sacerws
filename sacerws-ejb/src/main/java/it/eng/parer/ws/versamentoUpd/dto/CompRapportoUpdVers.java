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
package it.eng.parer.ws.versamentoUpd.dto;

import java.math.BigInteger;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import it.eng.parer.ws.xml.versUpdResp.ChiaveType;
import it.eng.parer.ws.xml.versUpdResp.CodiceEsitoType;
import it.eng.parer.ws.xml.versUpdResp.ControlliFallitiUlterioriType;
import it.eng.parer.ws.xml.versUpdResp.ControlliType;
import it.eng.parer.ws.xml.versUpdResp.DocumentoCollegatoType;
import it.eng.parer.ws.xml.versUpdResp.DocumentoType;
import it.eng.parer.ws.xml.versUpdResp.EsitoAggiornamento;
import it.eng.parer.ws.xml.versUpdResp.EsitoGeneraleNegativoType;
import it.eng.parer.ws.xml.versUpdResp.EsitoGeneralePositivoType;
import it.eng.parer.ws.xml.versUpdResp.EsitoNegativoAggiornamentoType;
import it.eng.parer.ws.xml.versUpdResp.ParametriAggiornamentoType;
import it.eng.parer.ws.xml.versUpdResp.RapportoVersamentoType;
import it.eng.parer.ws.xml.versUpdResp.SIPType;
import it.eng.parer.ws.xml.versUpdResp.UnitaDocumentariaType;
import it.eng.parer.ws.xml.versUpdResp.VersatoreType;
import it.eng.parer.ws.xml.versUpdResp.WarningsType;

/**
 *
 * @author sinatti_s
 */
public class CompRapportoUpdVers {

    EsitoAggiornamento esitoAggiornamento;

    RapportoVersamentoType rapportoOk;
    EsitoNegativoAggiornamentoType rapportoKo;

    public CompRapportoUpdVers() {
	esitoAggiornamento = new EsitoAggiornamento();
	rapportoOk = new RapportoVersamentoType();
	rapportoKo = new EsitoNegativoAggiornamentoType();
    }

    public EsitoAggiornamento produciEsitoAggiornamento() {
	if (rapportoKo.getEsitoGenerale() != null
		&& rapportoKo.getEsitoGenerale().getCodiceEsito() != null
		&& rapportoKo.getEsitoGenerale().getCodiceEsito() == CodiceEsitoType.NEGATIVO) {
	    esitoAggiornamento.setRapportoVersamento(null);
	    esitoAggiornamento.setEsitoNegativoAggiornamento(rapportoKo);
	} else {
	    esitoAggiornamento.setRapportoVersamento(rapportoOk);
	    esitoAggiornamento.setEsitoNegativoAggiornamento(null);
	}
	return esitoAggiornamento;
    }

    public RapportoVersamentoType getRapportoOk() {
	return rapportoOk;
    }

    public EsitoNegativoAggiornamentoType getRapportoKo() {
	return rapportoKo;
    }

    //
    public String getVersioneRapportoVersamento() {
	return esitoAggiornamento.getVersioneEsitoAggiornamento();
    }

    public void setVersioneRapportoVersamento(String value) {
	esitoAggiornamento.setVersioneEsitoAggiornamento(value);
	rapportoOk.setVersioneRapportoVersamento(value);
    }

    public String getVersioneIndiceSIPAggiornamento() {
	return esitoAggiornamento.getVersioneIndiceSIPAggiornamento();
    }

    public void setVersioneIndiceSIPAggiornamento(String value) {
	esitoAggiornamento.setVersioneIndiceSIPAggiornamento(value);
    }

    public XMLGregorianCalendar getDataRapportoVersamento() {
	return esitoAggiornamento.getDataEsitoAggiornamento();
    }

    public void setDataRapportoVersamento(XMLGregorianCalendar value) {
	esitoAggiornamento.setDataEsitoAggiornamento(value);
	rapportoOk.setDataRapportoVersamento(value);
    }

    // MEV#23176
    public String getURNSIP() {
	return esitoAggiornamento.getURNSIP();
    }

    public void setURNSIP(String value) {
	esitoAggiornamento.setURNSIP(value);
	rapportoOk.getSIP().setURNSIP(value);
    }
    // end MEV#23176

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

    public String getURNIndiceSIP() {
	return getSIP().getURNIndiceSIP();
    }

    public void setURNIndiceSIP(String value) {
	getSIP().setURNIndiceSIP(value);
    }

    public EsitoGeneralePositivoType getEsitoGeneralePositivo() {
	return rapportoOk.getEsitoGenerale();
    }

    public void setEsitoGeneralePositivo(EsitoGeneralePositivoType value) {
	rapportoOk.setEsitoGenerale(value);
    }

    public EsitoGeneraleNegativoType getEsitoGeneraleNegativo() {
	return rapportoKo.getEsitoGenerale();
    }

    public void setEsitoGeneraleNegativo(EsitoGeneraleNegativoType value) {
	rapportoKo.setEsitoGenerale(value);
    }

    public WarningsType getWarnings() {
	return rapportoOk.getEsitoGenerale().getWarnings();
    }

    public void setWarnings(WarningsType value) {
	rapportoOk.getEsitoGenerale().setWarnings(value);
    }

    public ControlliType getControlliGenerali() {
	return rapportoOk.getControlliGenerali();
    }

    public ControlliType getControlliGeneraliKo() {
	return rapportoKo.getControlliGenerali();
    }

    public void setControlliGenerali(ControlliType value) {
	rapportoKo.setControlliGenerali(value);
	rapportoOk.setControlliGenerali(value);
    }

    public ControlliFallitiUlterioriType getControlliFallitiUlteriori() {
	return rapportoKo.getControlliFallitiUlteriori();
    }

    public void setControlliFallitiUlteriori(ControlliFallitiUlterioriType value) {
	rapportoKo.setControlliFallitiUlteriori(value);
    }

    public UnitaDocumentariaType getUnitaDocumentaria() {
	return rapportoOk.getUnitaDocumentaria();
    }

    public UnitaDocumentariaType getUnitaDocumentariaKo() {
	return rapportoKo.getUnitaDocumentaria();
    }

    public void setUnitaDocumentaria(UnitaDocumentariaType value) {
	rapportoKo.setUnitaDocumentaria(value);
	rapportoOk.setUnitaDocumentaria(value);
    }

    public void setControlliUnitaDocumentaria(ControlliType value) {
	getUnitaDocumentaria().setControlliUnitaDocumentaria(value);
	getUnitaDocumentariaKo().setControlliUnitaDocumentaria(value);
    }

    public ControlliType getControlliUnitaDocumentaria() {
	return getUnitaDocumentaria().getControlliUnitaDocumentaria();
    }

    public ControlliType getControlliUnitaDocumentariaKo() {
	return getUnitaDocumentariaKo().getControlliUnitaDocumentaria();
    }

    public ParametriAggiornamentoType getParametriAggiornamento() {
	if (rapportoOk.getParametriAggiornamento() == null
		|| rapportoKo.getParametriAggiornamento() == null) {
	    this.setParametriAggiornamentoType(new ParametriAggiornamentoType());
	}
	return rapportoOk.getParametriAggiornamento();
    }

    private void setParametriAggiornamentoType(ParametriAggiornamentoType value) {
	rapportoOk.setParametriAggiornamento(value);
	rapportoKo.setParametriAggiornamento(value);
    }

    public String getIndiceSIP() {
	return rapportoKo.getIndiceSIP();
    }

    public void setIndiceSIP(String value) {
	rapportoKo.setIndiceSIP(value);
    }

    public BigInteger getProgressivoAggiornamento() {
	return getUnitaDocumentaria().getProgressivoAggiornamento();
    }

    public void setProgressivoAggiornamento(BigInteger value) {
	getUnitaDocumentaria().setProgressivoAggiornamento(value);
    }

    public String getRegistro() {
	return rapportoOk.getUnitaDocumentaria().getChiave().getRegistro();
    }

    public void setRegistro(String value) {
	rapportoOk.getUnitaDocumentaria().getChiave().setRegistro(value);
	rapportoKo.getUnitaDocumentaria().getChiave().setRegistro(value);
    }

    public String getTipologiaUnitaDocumentaria() {
	return rapportoOk.getUnitaDocumentaria().getChiave().getRegistro();
    }

    public void setTipologiaUnitaDocumentaria(String value) {
	rapportoOk.getUnitaDocumentaria().setTipologiaUnitaDocumentaria(value);
	rapportoKo.getUnitaDocumentaria().setTipologiaUnitaDocumentaria(value);
    }

    public void setDocumentiCollegati(DocumentoCollegatoType value) {
	rapportoOk.getUnitaDocumentaria().setDocumentiCollegati(value);
	rapportoKo.getUnitaDocumentaria().setDocumentiCollegati(value);
    }

    /*
     * Nota: i documenti collegati restituiti fanno riferimento ad una UNICA istanza di
     * DocumentoCollegatoType quindi alla prima chiamata questa viene istanziata e poi si chiama
     * SEMPRE la stessa istanza (indipendentemente dall'UD che la chiama Questo vuol dire che è
     * indifferente se da rapportoOk o da rapportoKo
     */
    public DocumentoCollegatoType getDocumentiCollegati() {
	return getUnitaDocumentaria().getDocumentiCollegati();
    }

    public void setDocumentiCollegati(List<UpdUnitaDocColl> unitaDocCollegate) {
	DocumentoCollegatoType documentoCollegatoType = new DocumentoCollegatoType();
	for (UpdUnitaDocColl aggUnitaDocColl : unitaDocCollegate) {
	    DocumentoCollegatoType.DocumentoCollegato documentoCollegato = new DocumentoCollegatoType.DocumentoCollegato();
	    ChiaveType chiave = new ChiaveType();
	    chiave.setAnno(String.valueOf(aggUnitaDocColl.getAggChiave().getAnno()));
	    chiave.setNumero(aggUnitaDocColl.getAggChiave().getNumero());
	    chiave.setRegistro(aggUnitaDocColl.getAggChiave().getTipoRegistro());
	    documentoCollegato.setChiaveCollegamento(chiave);

	    documentoCollegatoType.getDocumentoCollegato().add(documentoCollegato);
	}

	setDocumentiCollegati(documentoCollegatoType);

    }

    public void setUnitaDocumentariaVersatore(VersatoreType value) {
	rapportoOk.getUnitaDocumentaria().setVersatore(value);
	rapportoKo.getUnitaDocumentaria().setVersatore(value);
    }

    public VersatoreType getUnitaDocumentariaVersatore() {
	return getUnitaDocumentaria().getVersatore();
    }

    public VersatoreType getUnitaDocumentariaVersatoreKo() {
	return getUnitaDocumentariaKo().getVersatore();
    }

    public void setUnitaDocumentariaChiave(ChiaveType value) {
	rapportoOk.getUnitaDocumentaria().setChiave(value);
	rapportoKo.getUnitaDocumentaria().setChiave(value);

    }

    public ChiaveType getUnitaDocumentariaChiave() {
	return getUnitaDocumentaria().getChiave();
    }

    public ChiaveType getUnitaDocumentariaChiaveKo() {
	return getUnitaDocumentariaKo().getChiave();
    }

    public void setDocumentoPrincipale(DocumentoType documentoType) {
	rapportoOk.getUnitaDocumentaria().setDocumentoPrincipale(documentoType);
	rapportoKo.getUnitaDocumentaria().setDocumentoPrincipale(documentoType);
    }

    public DocumentoType getDocumentoPrincipale() {
	return getUnitaDocumentaria().getDocumentoPrincipale();
    }

    public void resetControlliWithUD() {

	// remove tag from response
	rapportoKo.setControlliGenerali(null);
	rapportoKo.setControlliFallitiUlteriori(null);
	// remove UD
	rapportoKo.setUnitaDocumentaria(null);
	// remove Params
	rapportoKo.setParametriAggiornamento(null);

    }

}
