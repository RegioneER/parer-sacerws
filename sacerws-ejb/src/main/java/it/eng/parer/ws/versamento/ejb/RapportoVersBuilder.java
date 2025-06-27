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

package it.eng.parer.ws.versamento.ejb;

import java.io.IOException;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.exception.SacerWsException;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.XmlVersCache;
import it.eng.parer.ws.utils.BinEncUtility;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.CostantiDB.TipiHash;
import it.eng.parer.ws.utils.HashCalculator;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.utils.XmlDateUtility;
import it.eng.parer.ws.utils.XmlUtils;
import it.eng.parer.ws.versamento.dto.IVersamentoExt;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VersamentoExt;
import it.eng.parer.ws.versamento.dto.VersamentoExtAggAll;
import it.eng.parer.ws.xml.versResp.ComponenteSIPType;
import it.eng.parer.ws.xml.versResp.DocumentoSIPType;
import it.eng.parer.ws.xml.versResp.ECComponenteType;
import it.eng.parer.ws.xml.versResp.ECDocumentoType;
import it.eng.parer.ws.xml.versResp.ECSottoComponenteType;
import it.eng.parer.ws.xml.versResp.EsitoVersAggAllegati;
import it.eng.parer.ws.xml.versResp.EsitoVersamento;
import it.eng.parer.ws.xml.versResp.RapportoVersamento;
import it.eng.parer.ws.xml.versResp.SIPType;
import it.eng.parer.ws.xml.versResp.UnitaDocumentariaSIPType;

/**
 *
 * @author Fioravanti_F, sinatti_s
 */
@Stateless(mappedName = "RapportoVersBuilder")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class RapportoVersBuilder {

    @EJB
    private XmlVersCache xmlVersCache;
    //
    @EJB
    private ControlliRappVers controlliRappVers;
    //
    private static final Logger log = LoggerFactory.getLogger(RapportoVersBuilder.class);
    //

    /**
     * Canonicalizzazione dell'XML SIP recuperato da sessione.getDatiDaSalvareIndiceSip (viene
     * restituito l'xml secondo transformazione e secondo il "formato" comunicato dal client)
     *
     * @param sessione di tipo {@link SyncFakeSessn}
     *
     * @return RispostaControlli con indice sip canonicalizzato
     */
    public RispostaControlli canonicalizzaDaSalvareIndiceSip(SyncFakeSessn sessione) {
	return canonicalizzaDaSalvareIndiceSip(sessione, false);
    }

    /**
     * Canonicalizzazione dell'XML SIP recuperato da sessione.getDatiDaSalvareIndiceSip (viene
     * restituito l'xml in formato prittyPrint se unPrettyPrint a true)
     *
     *
     * @param sessione      di tipo {@link SyncFakeSessn}
     * @param unPrettyPrint deprecato il valore true in quanto, l'xml restituito, deve essere il
     *                      prodotto della trasformazione attuata dalla canonicalizzazione
     *
     * @return RispostaControlli con indice sip canonicalizzato
     */
    private RispostaControlli canonicalizzaDaSalvareIndiceSip(SyncFakeSessn sessione,
	    boolean unPrettyPrint) {
	String xmlSip = sessione.getDatiDaSalvareIndiceSip();
	RispostaControlli tmpControlli = new RispostaControlli();
	try {
	    // refactor
	    String xmlOut = XmlUtils.doCanonicalizzazioneXml(xmlSip, unPrettyPrint);

	    tmpControlli.setrString(xmlOut);
	    tmpControlli.setrBoolean(true);
	    sessione.setDatiC14NIndiceSip(xmlOut);
	} catch (Exception e) {
	    tmpControlli.setrBoolean(false);
	    tmpControlli.setCodErr(MessaggiWSBundle.XSD_001_002);
	    tmpControlli.setDsErr(
		    MessaggiWSBundle.getString(MessaggiWSBundle.XSD_001_002, e.getMessage()));
	}

	return tmpControlli;
    }

    /**
     * Canonicalizzazione dell'XML Indice MM recuperato da sessione.getDatiPackInfoSipXml (viene
     * restituito l'xml secondo transformazione e secondo il "formato" comunicato dal client)
     *
     * @param sessione di tipo {@link SyncFakeSessn}
     *
     * @return RispostaControlli con indice sip canonicalizzato
     */
    public RispostaControlli canonicalizzaPackInfoSipXml(SyncFakeSessn sessione) {
	return canonicalizzaPackInfoSipXml(sessione, false);

    }

    /**
     * Canonicalizzazione dell'XML Indice MM recuperato da sessione.getDatiPackInfoSipXml (viene
     * restituito l'xml in formato prittyPrint se unPrettyPrint a true)
     *
     * @param sessione      di tipo {@link SyncFakeSessn}
     * @param unPrettyPrint true/false per ottenere l'xml in formato "pretty print"
     *
     * @return RispostaControlli con indice sip canonicalizzato
     */
    public RispostaControlli canonicalizzaPackInfoSipXml(SyncFakeSessn sessione,
	    boolean unPrettyPrint) {
	String xmlSip = sessione.getDatiPackInfoSipXml();
	RispostaControlli tmpControlli = new RispostaControlli();
	try {
	    // refactor
	    String xmlOut = XmlUtils.doCanonicalizzazioneXml(xmlSip, unPrettyPrint);

	    tmpControlli.setrString(xmlOut);
	    tmpControlli.setrBoolean(true);
	    sessione.setDatiC14NPackInfoSipXml(xmlOut);
	} catch (SacerWsException e) {
	    tmpControlli.setrBoolean(false);
	    tmpControlli.setCodErr(MessaggiWSBundle.XSD_001_002);
	    tmpControlli.setDsErr(
		    MessaggiWSBundle.getString(MessaggiWSBundle.XSD_001_002, e.getMessage()));
	}

	return tmpControlli;
    }

    //
    public RispostaControlli produciEsitoVecchioRapportoVers(VersamentoExt versamento,
	    long idElemento, EsitoVersamento esito) {
	RispostaControlli tmpControlli = new RispostaControlli();

	try {
	    RispostaControlli tmpRisp = controlliRappVers.trovaVersSessUd(idElemento);
	    if (tmpRisp.getrLong() > 0) {
		tmpRisp = controlliRappVers.leggiXmlRappVersFromUd(tmpRisp.getrLong(), idElemento);
		String xml;
		if (tmpRisp.getrString() != null) {
		    xml = tmpRisp.getrString();
		} else {
		    // calcolo l'URN dell'UD
		    String tmpUrn = MessaggiWSFormat.formattaBaseUrnUnitaDoc(
			    versamento.getStrutturaComponenti().getUrnPartVersatore(),
			    versamento.getStrutturaComponenti().getUrnPartChiaveUd());
		    xml = (MessageFormat.format(
			    "Il rapporto di versamento dell''unità documentaria {0} e URN [ {1} ] non è presente.",
			    versamento.getStrutturaComponenti().getUrnPartChiaveUd(),
			    MessaggiWSFormat.formattaUrnDocUniDoc(tmpUrn)));
		}

		if (versamento.getModificatoriWSCalc()
			.contains(Costanti.ModificatoriWS.TAG_RAPPORTO_VERS_OUT)) {
		    esito.setRapportoVersamento(xml);
		} else {
		    esito.setXMLVersamento(xml);
		}
	    } else {
		esito.setXMLVersamento(
			"La sessione di versamento per l'UD da versare non è presente.");
	    }

	} catch (Exception e) {
	    tmpControlli.setCodErr("ERR");
	    tmpControlli
		    .setDsErr("Errore nella generazione del Rapporto di Versamento UD duplicata: "
			    + e.getMessage());
	    log.error("Errore nella generazione del Rapporto di Versamento UD duplicata ", e);
	}
	return tmpControlli;

    }

    public RispostaControlli produciEsitoVecchioRapportoVers(VersamentoExtAggAll versamento,
	    long idElemento, String urnPartDoc, EsitoVersAggAllegati esito) {
	RispostaControlli tmpControlli = new RispostaControlli();

	try {
	    RispostaControlli tmpRisp = controlliRappVers.trovaVersSessDoc(idElemento,
		    versamento.getStrutturaComponenti().getIdUnitaDoc());
	    if (tmpRisp.getrLong() > 0) {
		tmpRisp = controlliRappVers.leggiXmlRappVersFromDoc(tmpRisp.getrLong(), idElemento);

		String xml;
		if (tmpRisp.getrString() != null) {
		    xml = tmpRisp.getrString();

		} else {
		    String tmpUrn;
		    // calcolo l'URN del documento
		    tmpUrn = MessaggiWSFormat.formattaBaseUrnDoc(
			    versamento.getStrutturaComponenti().getUrnPartVersatore(),
			    versamento.getStrutturaComponenti().getUrnPartChiaveUd(), urnPartDoc);
		    String tmpIdDocumento = versamento.getStrutturaComponenti().getDocumentiAttesi()
			    .get(0).getRifDocumento().getIDDocumento();
		    xml = (MessageFormat.format(
			    "Il rapporto di versamento del documento con <IDDocumento> uguale a {0} per l''UD {1} e URN [ {2} ] non è presente.",
			    tmpIdDocumento,
			    versamento.getStrutturaComponenti().getUrnPartChiaveUd(),
			    MessaggiWSFormat.formattaUrnDocUniDoc(tmpUrn)));
		}
		if (versamento.getModificatoriWSCalc()
			.contains(Costanti.ModificatoriWS.TAG_RAPPORTO_VERS_OUT)) {
		    esito.setRapportoVersamento(xml);
		} else {
		    esito.setXMLVersamento(xml);
		}
	    } else {
		esito.setXMLVersamento(
			"La sessione di versamento per il Documento da versare non è presente.");
	    }
	} catch (Exception e) {
	    tmpControlli.setCodErr("ERR");
	    tmpControlli.setDsErr(
		    "Errore nella generazione del Rapporto di Versamento Documento duplicato: "
			    + e.getMessage());
	    log.error("Errore nella generazione del Rapporto di Versamento Documento duplicato ",
		    e);
	}
	return tmpControlli;
    }

    //
    public RispostaControlli produciEsitoNuovoRapportoVers(VersamentoExt versamento,
	    SyncFakeSessn sessione, EsitoVersamento esito) {
	RispostaControlli tmpControlli = new RispostaControlli();

	try {
	    // calcolo l'URN dell'UD
	    String tmpUrn = MessaggiWSFormat.formattaBaseUrnUnitaDoc(
		    versamento.getStrutturaComponenti().getUrnPartVersatore(),
		    versamento.getStrutturaComponenti().getUrnPartChiaveUd());

	    RapportoVersamento tmpRappVers = this.inizializzaRapportoVersamento(tmpUrn, sessione);
	    tmpRappVers.setEsitoGenerale(esito.getEsitoGenerale());
	    if (esito.getWarningUlteriori() != null) {
		tmpRappVers.setWarningUlteriori(esito.getWarningUlteriori());
	    }
	    tmpRappVers.setVersatore(esito.getUnitaDocumentaria().getVersatore());
	    //
	    tmpRappVers.getSIP().setDataVersamento(esito.getDataVersamento());
	    //
	    // MEV#23176
	    tmpRappVers.getSIP().setURNSIP(esito.getURNSIP());
	    // end MEV#23176
	    //
	    UnitaDocumentariaSIPType tmpUdSip = tmpRappVers.getSIP().getUnitaDocumentaria();
	    tmpUdSip.setChiave(esito.getUnitaDocumentaria().getChiave());
	    tmpUdSip.setTipologiaUnitaDocumentaria(
		    versamento.getStrutturaComponenti().getDescTipologiaUnitaDocumentaria());
	    //
	    DocumentoSIPType tmpDoc = new DocumentoSIPType();
	    tmpUdSip.setDocumentoPrincipale(tmpDoc);
	    aggiungiDocumentoSip(tmpDoc, esito.getUnitaDocumentaria().getDocumentoPrincipale(),
		    versamento);
	    //
	    if (esito.getUnitaDocumentaria().getAllegati() != null) {
		for (ECDocumentoType tmpEsitoDoc : esito.getUnitaDocumentaria().getAllegati()
			.getAllegato()) {
		    DocumentoSIPType tmpDocAll = new DocumentoSIPType();
		    tmpUdSip.getAllegato().add(tmpDocAll);
		    aggiungiDocumentoSip(tmpDocAll, tmpEsitoDoc, versamento);
		}
	    }
	    //
	    if (esito.getUnitaDocumentaria().getAnnessi() != null) {
		for (ECDocumentoType tmpEsitoDoc : esito.getUnitaDocumentaria().getAnnessi()
			.getAnnesso()) {
		    DocumentoSIPType tmpDocAnn = new DocumentoSIPType();
		    tmpUdSip.getAnnesso().add(tmpDocAnn);
		    aggiungiDocumentoSip(tmpDocAnn, tmpEsitoDoc, versamento);
		}
	    }
	    //
	    if (esito.getUnitaDocumentaria().getAnnotazioni() != null) {
		for (ECDocumentoType tmpEsitoDoc : esito.getUnitaDocumentaria().getAnnotazioni()
			.getAnnotazione()) {
		    DocumentoSIPType tmpDocAnt = new DocumentoSIPType();
		    tmpUdSip.getAnnotazione().add(tmpDocAnt);
		    aggiungiDocumentoSip(tmpDocAnt, tmpEsitoDoc, versamento);
		}
	    }

	    StringWriter tmpStringWriter = new StringWriter();
	    Marshaller tmpMarshaller = xmlVersCache.getVersRespCtxforRapportoVersamento()
		    .createMarshaller();
	    tmpMarshaller.marshal(tmpRappVers, tmpStringWriter);

	    sessione.setDatiRapportoVersamento(tmpStringWriter.toString());

	    sessione.setHashRapportoVersamento(new HashCalculator()
		    .calculateHashSHAX(sessione.getDatiRapportoVersamento(), TipiHash.SHA_256)
		    .toHexBinary());
	    //
	    if (versamento.getModificatoriWSCalc()
		    .contains(Costanti.ModificatoriWS.TAG_RAPPORTO_VERS_OUT)) {
		esito.setRapportoVersamento(sessione.getDatiRapportoVersamento());
	    } else {
		esito.setXMLVersamento(sessione.getDatiRapportoVersamento());
	    }

	} catch (Exception e) {
	    tmpControlli.setCodErr("ERR");
	    tmpControlli.setDsErr(
		    "Errore nella generazione del Rapporto di Versamento: " + e.getMessage());
	    log.error("Errore nella generazione del Rapporto di Versamento ", e);
	}
	return tmpControlli;
    }

    public RispostaControlli produciEsitoNuovoRapportoVers(VersamentoExtAggAll versamento,
	    SyncFakeSessn sessione, EsitoVersAggAllegati esito) {
	RispostaControlli tmpControlli = new RispostaControlli();

	try {
	    // calcolo l'URN del documento

	    String tmpUrn = MessaggiWSFormat.formattaBaseUrnDoc(
		    versamento.getStrutturaComponenti().getUrnPartVersatore(),
		    versamento.getStrutturaComponenti().getUrnPartChiaveUd(),
		    versamento.getStrutturaComponenti().getDocumentiAttesi().get(0)
			    .getUrnPartDocumentoNiOrdDoc(),
		    Costanti.UrnFormatter.URN_DOC_FMT_STRING_V2);

	    RapportoVersamento tmpRappVers = this.inizializzaRapportoVersamento(tmpUrn, sessione);
	    tmpRappVers.setEsitoGenerale(esito.getEsitoGenerale());
	    if (esito.getWarningUlteriori() != null) {
		tmpRappVers.setWarningUlteriori(esito.getWarningUlteriori());
	    }
	    tmpRappVers.setVersatore(esito.getUnitaDocumentaria().getVersatore());
	    //
	    tmpRappVers.getSIP().setDataVersamento(esito.getDataVersamento());
	    //
	    // MEV#23176
	    tmpRappVers.getSIP().setURNSIP(esito.getURNSIP());
	    // end MEV#23176
	    //
	    UnitaDocumentariaSIPType tmpUdSip = tmpRappVers.getSIP().getUnitaDocumentaria();
	    tmpUdSip.setChiave(esito.getUnitaDocumentaria().getChiave());
	    tmpUdSip.setTipologiaUnitaDocumentaria(
		    versamento.getStrutturaComponenti().getDescTipologiaUnitaDocumentaria());
	    //
	    if (esito.getUnitaDocumentaria().getAllegato() != null) {
		DocumentoSIPType tmpDocAll = new DocumentoSIPType();
		tmpUdSip.getAllegato().add(tmpDocAll);
		ECDocumentoType tmpEsitoDoc = esito.getUnitaDocumentaria().getAllegato();
		aggiungiDocumentoSip(tmpDocAll, tmpEsitoDoc, versamento);
	    } else if (esito.getUnitaDocumentaria().getAnnesso() != null) {
		DocumentoSIPType tmpDocAll = new DocumentoSIPType();
		tmpUdSip.getAnnesso().add(tmpDocAll);
		ECDocumentoType tmpEsitoDoc = esito.getUnitaDocumentaria().getAnnesso();
		aggiungiDocumentoSip(tmpDocAll, tmpEsitoDoc, versamento);
	    } else if (esito.getUnitaDocumentaria().getAnnotazione() != null) {
		DocumentoSIPType tmpDocAll = new DocumentoSIPType();
		tmpUdSip.getAnnotazione().add(tmpDocAll);
		ECDocumentoType tmpEsitoDoc = esito.getUnitaDocumentaria().getAnnotazione();
		aggiungiDocumentoSip(tmpDocAll, tmpEsitoDoc, versamento);
	    }

	    StringWriter tmpStringWriter = new StringWriter();

	    Marshaller tmpMarshaller = xmlVersCache.getVersRespCtxforRapportoVersamento()
		    .createMarshaller();
	    tmpMarshaller.marshal(tmpRappVers, tmpStringWriter);

	    sessione.setDatiRapportoVersamento(tmpStringWriter.toString());

	    sessione.setHashRapportoVersamento(new HashCalculator()
		    .calculateHashSHAX(sessione.getDatiRapportoVersamento(), TipiHash.SHA_256)
		    .toHexBinary());
	    //

	    if (versamento.getModificatoriWSCalc()
		    .contains(Costanti.ModificatoriWS.TAG_RAPPORTO_VERS_OUT)) {
		esito.setRapportoVersamento(sessione.getDatiRapportoVersamento());
	    } else {
		esito.setXMLVersamento(sessione.getDatiRapportoVersamento());
	    }

	} catch (Exception e) {
	    tmpControlli.setCodErr("ERR");
	    tmpControlli.setDsErr(
		    "Errore nella generazione del Rapporto di Versamento: " + e.getMessage());
	    log.error("Errore nella generazione del Rapporto di Versamento ", e);
	}

	return tmpControlli;
    }

    private RapportoVersamento inizializzaRapportoVersamento(String tmpUrn, SyncFakeSessn sessione)
	    throws NoSuchAlgorithmException, IOException {

	HashCalculator hashCalculator = new HashCalculator();
	//
	sessione.setDatiDaSalvareIndiceSip(sessione.getDatiC14NIndiceSip());
	//
	// calcolo URN di indice, esito e package information
	// calcolo l'hash di indice e PI
	// #versione V2
	sessione.setUrnIndiceSipXml(MessaggiWSFormat.formattaUrnIndiceSip(tmpUrn,
		Costanti.UrnFormatter.URN_INDICE_SIP_V2));
	sessione.setHashIndiceSipXml(hashCalculator
		.calculateHashSHAX(sessione.getDatiDaSalvareIndiceSip(), TipiHash.SHA_256)
		.toHexBinary());
	sessione.setUrnEsitoVersamento(MessaggiWSFormat.formattaUrnEsitoVers(tmpUrn,
		Costanti.UrnFormatter.URN_ESITO_VERS_V2));
	if (sessione.getDatiPackInfoSipXml() != null) {
	    sessione.setUrnPackInfoSipXml(
		    MessaggiWSFormat.formattaUrnPiSip(tmpUrn, Costanti.UrnFormatter.URN_PI_SIP_V2));
	    sessione.setHashPackInfoSipXml(hashCalculator
		    .calculateHashSHAX(sessione.getDatiPackInfoSipXml(), TipiHash.SHA_256)
		    .toHexBinary());
	}
	sessione.setUrnRapportoVersamento(MessaggiWSFormat.formattaUrnRappVers(tmpUrn,
		Costanti.UrnFormatter.URN_RAPP_VERS_V2));
	//
	RapportoVersamento rapportoVersamento = new RapportoVersamento();
	rapportoVersamento.setVersione(Costanti.XML_RAPPORTO_VERS_VRSN);
	rapportoVersamento.setURNRapportoVersamento(sessione.getUrnRapportoVersamento());
	rapportoVersamento.setDataRapportoVersamento(
		XmlDateUtility.dateToXMLGregorianCalendar(sessione.getTmChiusura()));
	//
	SIPType tmpSip = new SIPType();
	rapportoVersamento.setSIP(tmpSip);
	tmpSip.setURNIndiceSIP(sessione.getUrnIndiceSipXml());
	tmpSip.setHashIndiceSIP(sessione.getHashIndiceSipXml());
	tmpSip.setAlgoritmoHashIndiceSIP(CostantiDB.TipiHash.SHA_256.descrivi());
	tmpSip.setEncodingHashIndiceSIP(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi());
	if (sessione.getDatiPackInfoSipXml() != null) {
	    tmpSip.setURNPISIP(sessione.getUrnPackInfoSipXml());
	    tmpSip.setHashPISIP(sessione.getHashPackInfoSipXml());
	    tmpSip.setAlgoritmoHashPISIP(CostantiDB.TipiHash.SHA_256.descrivi());
	    tmpSip.setEncodingHashPISIP(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi());
	}
	//
	UnitaDocumentariaSIPType tmpDocumentariaSIP = new UnitaDocumentariaSIPType();
	tmpSip.setUnitaDocumentaria(tmpDocumentariaSIP);

	return rapportoVersamento;
    }

    private void aggiungiDocumentoSip(DocumentoSIPType docDest, ECDocumentoType esitoDoc,
	    IVersamentoExt versamento) {
	docDest.setChiaveDoc(esitoDoc.getChiaveDoc());
	docDest.setIDDocumento(esitoDoc.getIDDocumento());
	docDest.setTipoDocumento(esitoDoc.getTipoDocumento());
	docDest.setFirmatoDigitalmente(esitoDoc.isFirmatoDigitalmente());
	docDest.setComponenti(new DocumentoSIPType.Componenti());
	for (ECComponenteType tmpEsitoComp : esitoDoc.getComponenti().getComponente()) {
	    ComponenteSIPType tmpComponenteSIP = new ComponenteSIPType();
	    docDest.getComponenti().getComponente().add(tmpComponenteSIP);
	    tmpComponenteSIP.setURN(tmpEsitoComp.getURN());
	    tmpComponenteSIP.setHash(BinEncUtility.encodeUTF8HexString(tmpEsitoComp.getHash()));
	    tmpComponenteSIP.setAlgoritmoHash(tmpEsitoComp.getAlgoritmoHash());
	    tmpComponenteSIP.setEncoding(tmpEsitoComp.getEncoding());
	    // v1.5
	    if (versamento.getModificatoriWSCalc()
		    .contains(Costanti.ModificatoriWS.TAG_RAPPORTO_VERS_1_5)) {
		tmpComponenteSIP.setNomeComponente(tmpEsitoComp.getNomeComponente());
		tmpComponenteSIP.setUrnVersato(tmpEsitoComp.getUrnVersato());
		tmpComponenteSIP.setHashVersato(tmpEsitoComp.getHashVersato());
		tmpComponenteSIP.setIDComponenteVersato(tmpEsitoComp.getIDComponenteVersato());
	    }
	    if (tmpEsitoComp.getSottoComponenti() != null) {
		for (ECSottoComponenteType tmpEsitoSComp : tmpEsitoComp.getSottoComponenti()
			.getSottoComponente()) {
		    ComponenteSIPType tmpSComponenteSIP = new ComponenteSIPType();
		    docDest.getComponenti().getComponente().add(tmpSComponenteSIP);
		    tmpSComponenteSIP.setURN(tmpEsitoSComp.getURN());
		    if (tmpEsitoSComp.getHash() != null && tmpEsitoSComp.getHash().length > 0) {
			tmpSComponenteSIP.setHash(
				BinEncUtility.encodeUTF8HexString(tmpEsitoSComp.getHash()));
			tmpSComponenteSIP.setAlgoritmoHash(tmpEsitoSComp.getAlgoritmoHash());
			tmpSComponenteSIP.setEncoding(tmpEsitoSComp.getEncoding());
		    }
		    // v1.5
		    if (versamento.getModificatoriWSCalc()
			    .contains(Costanti.ModificatoriWS.TAG_RAPPORTO_VERS_1_5)) {
			tmpComponenteSIP.setNomeComponente(tmpEsitoSComp.getNomeComponente());
			tmpComponenteSIP.setUrnVersato(tmpEsitoSComp.getUrnVersato());
			tmpComponenteSIP
				.setIDComponenteVersato(tmpEsitoSComp.getIDComponenteVersato());
		    }
		}
	    }
	}
    }
}
