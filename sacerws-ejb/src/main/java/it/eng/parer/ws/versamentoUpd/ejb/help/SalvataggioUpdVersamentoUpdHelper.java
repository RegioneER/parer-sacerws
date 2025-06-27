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

package it.eng.parer.ws.versamentoUpd.ejb.help;

import it.eng.parer.entity.*;
import it.eng.parer.entity.constraint.AroUpdDatiSpecUnitaDoc.TiEntitaAroUpdDatiSpecUnitaDoc;
import it.eng.parer.entity.constraint.AroUpdDatiSpecUnitaDoc.TiUsoXsdAroUpdDatiSpecUnitaDoc;
import it.eng.parer.entity.constraint.AroUpdUnitaDoc.AroUpdUDTiStatoUpdElencoVers;
import it.eng.parer.entity.constraint.AroXmlUpdUnitaDoc.TiXmlUpdUnitaDoc;
import it.eng.parer.entity.constraint.ElvUpdUdDaElabElenco.ElvUpdUdDaElabTiStatoUpdElencoVers;
import it.eng.parer.entity.constraint.VrsSesUpdUnitaDocKo.TiStatoSesUpdKo;
import it.eng.parer.util.Constants;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.*;
import it.eng.parer.ws.utils.Costanti.CategoriaDocumento;
import it.eng.parer.ws.utils.CostantiDB.TipiHash;
import it.eng.parer.ws.utils.CostantiDB.TipoAnnullamentoUnitaDoc;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.versamentoUpd.dto.*;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import it.eng.parer.ws.versamentoUpd.utils.UpdDocumentiUtils;
import it.eng.parer.ws.xml.versUpdReq.CamiciaFascicoloType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.LockModeType;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static it.eng.parer.util.DateUtilsConverter.convert;
import static it.eng.parer.util.DateUtilsConverter.convertLocal;
import static it.eng.parer.util.FlagUtilsConverter.booleanToFlag;
import it.eng.parer.ws.versamento.dto.BackendStorage;

/**
 *
 * @author sinatti_s, dilorenzo_f
 */
@Stateless(mappedName = "SalvataggioUpdVersamentoUpdHelper")
@LocalBean
public class SalvataggioUpdVersamentoUpdHelper extends SalvataggioUpdVersamentoBaseHelper {

    public static final String ERR_SALVA_XML_UD = "Errore interno nella fase di salvataggio dell'aggiornamento dell'xml dell'unità documentaria";

    public RispostaControlli getNextPgAroUpdUnitaDoc(long idUnitaDoc) {
	RispostaControlli rispostaControlli;
	rispostaControlli = new RispostaControlli();
	rispostaControlli.setrLong(-1);
	rispostaControlli.setrBoolean(false);
	BigDecimal max = null;

	// lancio query di controllo
	try {
	    String queryStr = "select max(ud.pgUpdUnitaDoc) " + "from AroUpdUnitaDoc ud "
		    + "where ud.aroUnitaDoc.idUnitaDoc = :idUnitaDoc ";

	    javax.persistence.Query query = entityManager.createQuery(queryStr);
	    query.setParameter("idUnitaDoc", idUnitaDoc);

	    max = (BigDecimal) query.getSingleResult();
	    if (max != null) {
		rispostaControlli.setrLong(max.longValue() + 1);
	    } else {
		rispostaControlli.setrLong(1);
	    }
	    rispostaControlli.setrBoolean(true);// query is good
	} catch (Exception e) {
	    rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
	    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
		    "SalvataggioUpdVersamentoHelper.getNextPgAroUpdUnitaDoc: " + e.getMessage()));
	    getLogger().error("Eccezione nella lettura della tabella degli aggiornamenti ", e);
	}

	return rispostaControlli;
    }

    /*
     * TODO: i campi non comunicati da aggiornamento (e che quindi non avranno un impatto sulla
     * reale update eseguita su DB) dovrebbero essere comunque riportati sulla ARO_UPD_UNITA_DOC ?
     * riportando, di fatto, l'ATTUALE valore (quindi l'ultimo aggiornato) della relativa
     * ARO_UNITA_DOC
     */
    /**
     *
     * @param versamento     oggetto versamento {@link UpdVersamentoExt}
     * @param sessione       "fake" session di tipo {@link SyncFakeSessn}
     * @param tmpAroUnitaDoc entity {@link AroUnitaDoc} "temporanea" (in lavorazione)
     * @param pgUpdUnitaDoc  progressivo aggiornamento
     * @param svf            dto con dati controlli {@link StrutturaUpdVers}
     *
     * @return RispostaControlli con risultato operazione di persistanza su DB
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviAroUpdUnitaDoc(UpdVersamentoExt versamento,
	    SyncFakeSessn sessione, AroUnitaDoc tmpAroUnitaDoc, long pgUpdUnitaDoc,
	    StrutturaUpdVers svf) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	tmpRispostaControlli.setrBoolean(false);

	try {
	    // dt_annull default
	    Calendar tmpCal = Calendar.getInstance();
	    tmpCal.set(2444, 11, 31, 0, 0, 0);

	    StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();

	    AroUpdUnitaDoc tmpAroUpdUnitaDoc = new AroUpdUnitaDoc();

	    // FK all’unità doc definita da unita doc da aggiornare
	    tmpAroUpdUnitaDoc.setAroUnitaDoc(tmpAroUnitaDoc);
	    // progessivo di aggiornamento definito contando gli aggiornamenti presenti per
	    // l’unità doc e sommando 1
	    tmpAroUpdUnitaDoc.setPgUpdUnitaDoc(new BigDecimal(pgUpdUnitaDoc));
	    // timestamp di inizio sessione e di fine pari al valore di inizio sessione
	    // definito su unita doc da aggiornare
	    tmpAroUpdUnitaDoc.setTsIniSes(convert(sessione.getTmApertura()));
	    tmpAroUpdUnitaDoc.setTsFineSes(convert(sessione.getTmChiusura()));
	    /*
	     * indirizzo IP del client chiamante indirizzo IP del server che elabora l’aggiornamento
	     */

	    // salvo il nome del server/istanza nel cluster che sta salvando i dati e ha
	    // gestito il versamento
	    tmpAroUpdUnitaDoc.setCdIndServer(appServerInstance.getName());
	    // salvo l'indirizzo IP del sistema che ha effettuato la richiesta di
	    // versamento/aggiunta
	    tmpAroUpdUnitaDoc.setCdIndIpClient(sessione.getIpChiamante());
	    // identificatore dell’utente definito da unita doc da aggiornare
	    tmpAroUpdUnitaDoc.setIamUser(
		    entityManager.find(IamUser.class, versamento.getUtente().getIdUtente()));
	    // indicatore di forzatura dell’aggiornamento definito da unita doc da
	    // aggiornare
	    tmpAroUpdUnitaDoc
		    .setFlForzaUpd(booleanToFlag(svf.getFlControlliUpd().isFlAbilitaUpdMeta()));
	    // note dell’aggiornamento definito da unita doc da aggiornare
	    tmpAroUpdUnitaDoc.setNtUpd(svf.getNoteAggiornamento());

	    // TODO: info non comunicata da aggiornamento !
	    if (StringUtils.isBlank(tmpAroUnitaDoc.getTiAnnul()) || !tmpAroUnitaDoc.getTiAnnul()
		    .equals(TipoAnnullamentoUnitaDoc.ANNULLAMENTO.name())) {
		tmpAroUpdUnitaDoc.setDtAnnul(tmpCal.getTime());
	    }

	    /*
	     * info su classificazione principale definite da tag “FascicoloPrincipale” (e relativi
	     * tag figli) del XML in input (ds_classif_princ, cd_fascic_princ,
	     * ds_oggetto_fascic_princ, cd_sottofascic_princ e ds_oggetto_sottofascic_princ); se tag
	     * figlio non presente o con dimensione nulla si assegna valore nullo
	     */
	    if (versamento.hasPAFascicoloPrincipaleToUpd()) {
		tmpAroUpdUnitaDoc
			.setDsClassifPrinc(versamento.getVersamento().getUnitaDocumentaria()
				.getProfiloArchivistico().getFascicoloPrincipale().getClassifica()); // può
												     // essere
												     // null
		if (versamento.getVersamento().getUnitaDocumentaria().getProfiloArchivistico()
			.getFascicoloPrincipale().getFascicolo() != null) /* figlio 1 */ {
		    tmpAroUpdUnitaDoc.setCdFascicPrinc(StringUtils.isNotBlank(versamento
			    .getVersamento().getUnitaDocumentaria().getProfiloArchivistico()
			    .getFascicoloPrincipale().getFascicolo().getIdentificativo())
				    ? versamento.getVersamento().getUnitaDocumentaria()
					    .getProfiloArchivistico().getFascicoloPrincipale()
					    .getFascicolo().getIdentificativo()
				    : null);
		    //
		    tmpAroUpdUnitaDoc.setDsOggettoFascicPrinc(StringUtils.isNotBlank(versamento
			    .getVersamento().getUnitaDocumentaria().getProfiloArchivistico()
			    .getFascicoloPrincipale().getFascicolo().getOggetto())
				    ? versamento.getVersamento().getUnitaDocumentaria()
					    .getProfiloArchivistico().getFascicoloPrincipale()
					    .getFascicolo().getOggetto()
				    : null);

		}
		if (versamento.getVersamento().getUnitaDocumentaria().getProfiloArchivistico()
			.getFascicoloPrincipale().getSottoFascicolo() != null) /* figlio 2 */ {

		    tmpAroUpdUnitaDoc.setCdSottofascicPrinc(StringUtils.isNotBlank(versamento
			    .getVersamento().getUnitaDocumentaria().getProfiloArchivistico()
			    .getFascicoloPrincipale().getSottoFascicolo().getIdentificativo())
				    ? versamento.getVersamento().getUnitaDocumentaria()
					    .getProfiloArchivistico().getFascicoloPrincipale()
					    .getSottoFascicolo().getIdentificativo()
				    : null);// può essere null

		    tmpAroUpdUnitaDoc.setDsOggettoSottofascicPrinc(StringUtils.isNotBlank(versamento
			    .getVersamento().getUnitaDocumentaria().getProfiloArchivistico()
			    .getFascicoloPrincipale().getSottoFascicolo().getOggetto())
				    ? versamento.getVersamento().getUnitaDocumentaria()
					    .getProfiloArchivistico().getFascicoloPrincipale()
					    .getSottoFascicolo().getOggetto()
				    : null);// può essere null
		}
	    } else if (!versamento.hasProfiloArchivisticoToUpdNull()) {
		// non esiste il ramo ProfiloArchivistico/FascicoloPrincipale
		// setto / recupero i valori presente nell'attuale ARO_UNITA_DOC

		tmpAroUpdUnitaDoc.setDsClassifPrinc(tmpAroUnitaDoc.getDsClassifPrinc());
		tmpAroUpdUnitaDoc.setCdFascicPrinc(tmpAroUnitaDoc.getCdFascicPrinc());
		tmpAroUpdUnitaDoc.setDsOggettoFascicPrinc(tmpAroUnitaDoc.getDsOggettoFascicPrinc());
		tmpAroUpdUnitaDoc.setCdSottofascicPrinc(tmpAroUnitaDoc.getCdSottofascicPrinc());
		tmpAroUpdUnitaDoc.setDsOggettoSottofascicPrinc(
			tmpAroUnitaDoc.getDsOggettoSottofascicPrinc());
	    }

	    /*
	     * info su profilo unita doc definite da tag “ProfiloUnitaDocumentaria” (e relativi tag
	     * figli) del XML in input (dl_oggetto_unita_doc e dt_reg_unita_doc); se tag figlio non
	     * presente o con dimensione nulla si assegna valore nullo
	     */
	    if (versamento.hasProfiloUnitaDocumentariaToUpd()) {
		tmpAroUpdUnitaDoc
			.setDlOggettoUnitaDoc(StringUtils.isNotBlank(versamento.getVersamento()
				.getUnitaDocumentaria().getProfiloUnitaDocumentaria().getOggetto())
					? versamento.getVersamento().getUnitaDocumentaria()
						.getProfiloUnitaDocumentaria().getOggetto()
					: null);
		tmpAroUpdUnitaDoc.setDtRegUnitaDoc(versamento.getVersamento().getUnitaDocumentaria()
			.getProfiloUnitaDocumentaria().getData() != null
				? new SimpleDateFormat("yyyy-MM-dd")
					.parse(versamento.getVersamento().getUnitaDocumentaria()
						.getProfiloUnitaDocumentaria().getData())
				: null);
	    } else {
		// non esiste il ramo rofiloUnitaDocumentaria
		// setto / recupero i valori presente nell'attuale ARO_UNITA_DOC

		tmpAroUpdUnitaDoc.setDlOggettoUnitaDoc(tmpAroUnitaDoc.getDlOggettoUnitaDoc());
		tmpAroUpdUnitaDoc.setDtRegUnitaDoc(tmpAroUnitaDoc.getDtRegUnitaDoc());

	    }

	    // stato dell’aggiornamento rispetto al processo degli elenchi di versamento =
	    // IN_ATTESA_SCHED
	    tmpAroUpdUnitaDoc.setTiStatoUpdElencoVers(AroUpdUDTiStatoUpdElencoVers.IN_ATTESA_SCHED);
	    //
	    tmpAroUpdUnitaDoc.setOrgStrut(
		    entityManager.find(OrgStrut.class, strutturaUpdVers.getIdStruttura()));
	    //
	    tmpAroUpdUnitaDoc.setAaKeyUnitaDoc(tmpAroUnitaDoc.getAaKeyUnitaDoc());
	    //
	    tmpAroUpdUnitaDoc.setDecRegistroUnitaDoc(entityManager.find(DecRegistroUnitaDoc.class,
		    strutturaUpdVers.getIdRegistro()));
	    //
	    tmpAroUpdUnitaDoc.setDecTipoUnitaDoc(entityManager.find(DecTipoUnitaDoc.class,
		    strutturaUpdVers.getIdTipologiaUnitaDocumentaria()));
	    //
	    tmpAroUpdUnitaDoc.setDecTipoDocPrinc(entityManager.find(DecTipoDoc.class,
		    strutturaUpdVers.getIdTipoDocPrincipale()));
	    // indicatore di presenza di aggiornamenti falliti risolti = false
	    tmpAroUpdUnitaDoc.setFlSesUpdKoRisolti("0");
	    //
	    tmpAroUpdUnitaDoc
		    .setFlUpdProfiloArchiv(booleanToFlag(versamento.hasProfiloArchivisticoToUpd()));
	    //
	    tmpAroUpdUnitaDoc.setFlUpdFascicoloPrinc(
		    booleanToFlag(versamento.hasPAFascicoloPrincipaleToUpd()));
	    //
	    tmpAroUpdUnitaDoc
		    .setFlUpdFascicoliSec(booleanToFlag(versamento.hasPAFascicoliSecondariToUp()));
	    //
	    tmpAroUpdUnitaDoc.setFlUpdProfiloUnitaDoc(
		    booleanToFlag(versamento.hasProfiloUnitaDocumentariaToUpd()));
	    //
	    tmpAroUpdUnitaDoc
		    .setFlUpdLinkUnitaDoc(booleanToFlag(versamento.hasDocumentiCollegatiToUpd()));
	    //
	    tmpAroUpdUnitaDoc
		    .setFlUpdDatiSpec(booleanToFlag(versamento.hasDatiSpecificiToBeUpdated()));
	    //
	    tmpAroUpdUnitaDoc.setFlUpdDatiSpecMigraz(
		    booleanToFlag(versamento.hasDatiSpecificiMigrazioneToUpd()));
	    //
	    tmpAroUpdUnitaDoc
		    .setFlUpdProfiloNormativo(booleanToFlag(versamento.hasProfiloNormativoToUpd()));
	    //
	    tmpAroUpdUnitaDoc.setTipoUpdUnitaDoc(strutturaUpdVers.getTipoAggiornamento());

	    entityManager.persist(tmpAroUpdUnitaDoc);
	    // ad on list
	    tmpAroUnitaDoc.getAroUpdUnitaDocs().add(tmpAroUpdUnitaDoc);
	    tmpRispostaControlli.setrObject(tmpAroUpdUnitaDoc);
	    entityManager.flush();
	    strutturaUpdVers.setIdRecAggiornamentoDB(tmpAroUpdUnitaDoc.getIdUpdUnitaDoc());
	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio dell'aggiornamento dell'unità documentaria: "
			    + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	    getLogger().error(
		    "Errore interno nella fase di salvataggio dell'aggiornamento dell'unità documentaria",
		    e);
	}

	return tmpRispostaControlli;
    }

    /**
     *
     * @param rispostaWs        risposta ws {@link RispostaWSUpdVers}
     * @param versamento        oggetto versamento {@link UpdVersamentoExt}
     * @param sessione          "fake" session di tipo {@link SyncFakeSessn}
     * @param tmpAroUnitaDoc    entity {@link AroUnitaDoc} "temporanea" (in lavorazione)
     * @param tmpAroUpdUnitaDoc entity {@link AroUpdUnitaDoc} "temporanea" (in lavorazione)
     * @param backendMetadata   backend usato
     * @param sipBlob           mappa contenente i clob del sip
     * @param strutturaUpdVers  dto con dati controlli {@link StrutturaUpdVers}
     *
     * @return RispostaControlli con risultato operazione di persistanza su DB
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviAroXmlUpdUnitaDoc(RispostaWSUpdVers rispostaWs,
	    UpdVersamentoExt versamento, SyncFakeSessn sessione, AroUnitaDoc tmpAroUnitaDoc,
	    AroUpdUnitaDoc tmpAroUpdUnitaDoc, BackendStorage backendMetadata,
	    Map<String, String> sipBlob, StrutturaUpdVers strutturaUpdVers) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	tmpRispostaControlli.setrBoolean(false);
	CompRapportoUpdVers esito = rispostaWs.getCompRapportoUpdVers();
	final LocalDate dtIniSes = convertLocal(sessione.getTmApertura());

	try {
	    AroXmlUpdUnitaDoc tmpAroXmlUpdUnitaDoc = new AroXmlUpdUnitaDoc();
	    // FK all’aggiornamento unità doc
	    tmpAroXmlUpdUnitaDoc.setAroUpdUnitaDoc(tmpAroUpdUnitaDoc);
	    // tipo XML pari a RICHIESTA
	    tmpAroXmlUpdUnitaDoc.setTiXmlUpdUnitaDoc(TiXmlUpdUnitaDoc.RICHIESTA);
	    // MEV#29276
	    String blXml = sessione.getDatiDaSalvareIndiceSip().length() == 0 ? "--"
		    : sessione.getDatiDaSalvareIndiceSip();
	    if (backendMetadata.isDataBase()) {
		// clob contenente lo XML in input (canonicalizzato)
		tmpAroXmlUpdUnitaDoc.setBlXml(blXml);
	    } else {
		sipBlob.put(TiXmlUpdUnitaDoc.RICHIESTA.toString(), blXml);
	    }
	    // end MEV#29276
	    // versione xml in input definita da unita doc da aggiornare
	    tmpAroXmlUpdUnitaDoc
		    .setCdVersioneXml(strutturaUpdVers.getVersioneIndiceSipNonVerificata());

	    // calcolo l'URN UPD
	    String baseUrn = MessaggiWSFormat.formattaBaseUrnUpdUnitaDoc(
		    versamento.getStrutturaUpdVers().getUrnPartVersatore(),
		    versamento.getStrutturaUpdVers().getUrnPartChiaveUd(),
		    tmpAroUpdUnitaDoc.getPgUpdUnitaDoc().longValue(), true,
		    Costanti.UrnFormatter.UPD_FMT_STRING_V3, Costanti.UrnFormatter.PAD5DIGITS_FMT);
	    //
	    esito.setURNIndiceSIP(MessaggiWSFormat.formattaUrnIndiceSipUpd(baseUrn,
		    Costanti.UrnFormatter.URN_INDICE_SIP_V2));
	    //
	    // MEV#23176
	    // calcolo l'URN Sip
	    esito.setURNSIP(
		    MessaggiWSFormat.formattaUrnSip(baseUrn, Costanti.UrnFormatter.URN_SIP_UPD_V2));
	    // end MEV#23176
	    //
	    // calcolo l'URN RapportoVers
	    esito.setIdentificativoRapportoVersamento(MessaggiWSFormat
		    .formattaUrnPartRappVersUpd(baseUrn, Costanti.UrnFormatter.URN_RAPP_VERS_V2));
	    //
	    // calcolo l'URN UPD normalizzato
	    String baseUrnNorm = MessaggiWSFormat.formattaBaseUrnUpdUnitaDoc(
		    MessaggiWSFormat.formattaUrnPartVersatore(
			    versamento.getStrutturaUpdVers().getVersatoreNonverificato(), true,
			    Costanti.UrnFormatter.VERS_FMT_STRING),
		    MessaggiWSFormat.formattaUrnPartUnitaDoc(
			    versamento.getStrutturaUpdVers().getChiaveNonVerificata(), true,
			    Costanti.UrnFormatter.UD_FMT_STRING),
		    tmpAroUpdUnitaDoc.getPgUpdUnitaDoc().longValue(), true,
		    Costanti.UrnFormatter.UPD_FMT_STRING_V3, Costanti.UrnFormatter.PAD5DIGITS_FMT);

	    // hash del XML in input calcolato con SHA-256 (definito da unita doc da
	    // aggiornare)
	    tmpAroXmlUpdUnitaDoc.setDsUrnXml(esito.getURNIndiceSIP());
	    tmpAroXmlUpdUnitaDoc.setDsUrnNormalizXml(MessaggiWSFormat
		    .formattaUrnIndiceSipUpd(baseUrnNorm, Costanti.UrnFormatter.URN_INDICE_SIP_V2));
	    tmpAroXmlUpdUnitaDoc.setDsHashXml(new HashCalculator()
		    .calculateHashSHAX(sessione.getDatiDaSalvareIndiceSip(), TipiHash.SHA_256)
		    .toHexBinary());
	    tmpAroXmlUpdUnitaDoc
		    .setCdEncodingHashXml(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi());
	    tmpAroXmlUpdUnitaDoc.setDsAlgoHashXml(CostantiDB.TipiHash.SHA_256.descrivi());

	    // identificatore della struttura definito dall’unita doc da aggiornare
	    tmpAroXmlUpdUnitaDoc.setOrgStrut(
		    entityManager.find(OrgStrut.class, strutturaUpdVers.getIdStruttura()));
	    tmpAroXmlUpdUnitaDoc.setDtIniSes(dtIniSes);
	    // MEV#30089
	    tmpAroXmlUpdUnitaDoc.setAaIniSes(dtIniSes.getYear());

	    entityManager.persist(tmpAroXmlUpdUnitaDoc);
	    // add on list
	    tmpAroUpdUnitaDoc.getAroXmlUpdUnitaDocs().add(tmpAroXmlUpdUnitaDoc);

	    String xmlesito = updLogSessioneHelper.generaRapportoVersamento(rispostaWs);

	    tmpAroXmlUpdUnitaDoc = new AroXmlUpdUnitaDoc();
	    // FK all’aggiornamento unità doc
	    tmpAroXmlUpdUnitaDoc.setAroUpdUnitaDoc(tmpAroUpdUnitaDoc);
	    // tipo XML pari a RISPOSTA
	    tmpAroXmlUpdUnitaDoc.setTiXmlUpdUnitaDoc(TiXmlUpdUnitaDoc.RISPOSTA);
	    // MEV#29276
	    if (backendMetadata.isDataBase()) {
		// clob contenente lo XML in input (canonicalizzato)
		tmpAroXmlUpdUnitaDoc.setBlXml(xmlesito);
	    } else {
		sipBlob.put(TiXmlUpdUnitaDoc.RISPOSTA.toString(), xmlesito);
	    }
	    // end MEV#29276
	    // versione xml in input definita da unita doc da aggiornare
	    tmpAroXmlUpdUnitaDoc.setCdVersioneXml(esito.getVersioneRapportoVersamento());

	    // hash del XML in input calcolato con SHA-256 (definito da unita doc da
	    // aggiornare)
	    tmpAroXmlUpdUnitaDoc.setDsUrnXml(esito.getIdentificativoRapportoVersamento());
	    tmpAroXmlUpdUnitaDoc.setDsUrnNormalizXml(MessaggiWSFormat.formattaUrnPartRappVersUpd(
		    baseUrnNorm, Costanti.UrnFormatter.URN_RAPP_VERS_V2));
	    tmpAroXmlUpdUnitaDoc.setDsHashXml(new HashCalculator()
		    .calculateHashSHAX(xmlesito, TipiHash.SHA_256).toHexBinary());
	    tmpAroXmlUpdUnitaDoc
		    .setCdEncodingHashXml(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi());
	    tmpAroXmlUpdUnitaDoc.setDsAlgoHashXml(CostantiDB.TipiHash.SHA_256.descrivi());

	    // identificatore della struttura definito dall’unita doc da aggiornare
	    tmpAroXmlUpdUnitaDoc.setOrgStrut(
		    entityManager.find(OrgStrut.class, strutturaUpdVers.getIdStruttura()));
	    tmpAroXmlUpdUnitaDoc.setDtIniSes(dtIniSes);
	    // MEV#30089
	    tmpAroXmlUpdUnitaDoc.setAaIniSes(dtIniSes.getYear());

	    entityManager.persist(tmpAroXmlUpdUnitaDoc);
	    entityManager.flush();
	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    ERR_SALVA_XML_UD + ": " + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	    getLogger().error(ERR_SALVA_XML_UD, e);
	}

	return tmpRispostaControlli;
    }

    public RispostaControlli scriviAroWarnUpdUnitaDocForUD(UpdVersamentoExt versamento,
	    AroUpdUnitaDoc aroUpdUnitaDoc) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	tmpRispostaControlli.setrBoolean(false);
	int progWarn = 1;
	try {
	    // per ogni controllo svolto sull’unità doc da aggiornare con esito = WARNING
	    // (in ordine di numero d’ordine del controllo)
	    // NOTA: non serve utilizzare la lista warnings dato che lo stesso errore è
	    // presente nelle altre due
	    progWarn = buildWarn(versamento.getControlliGenerali(), versamento, aroUpdUnitaDoc,
		    progWarn);
	    buildWarn(versamento.getControlliUnitaDocumentaria(), versamento, aroUpdUnitaDoc,
		    progWarn);

	    entityManager.flush();
	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio dell'aggiornamento warning dell'unità documentaria: "
			    + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	    getLogger().error(
		    "Errore interno nella fase di salvataggio dell'aggiornamento warning dell'unità documentaria",
		    e);
	}

	return tmpRispostaControlli;

    }

    public RispostaControlli scriviAroWarnUpdUnitaDocForDoc(UpdVersamentoExt versamento,
	    AroUpdUnitaDoc aroUpdUnitaDoc, StrutturaUpdVers strutturaUpdVers) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	tmpRispostaControlli.setrBoolean(false);
	int progWarn = 1;
	try {
	    /*
	     * 5. per ogni documento da aggiornare 5.1. per ogni controllo svolto sul doc da
	     * aggiornare
	     */
	    for (UpdDocumentoVers documento : strutturaUpdVers.getDocumentiAttesi()) {

		switch (documento.getCategoriaDoc()) {
		case Principale:
		    progWarn = buildWarn(
			    versamento.getControlliDocPrincipale(
				    documento.getRifUpdDocumento().getIDDocumento()),
			    versamento, aroUpdUnitaDoc, progWarn);
		    break;
		case Allegato:
		    progWarn = buildWarn(
			    versamento.getControlliAllegato(
				    documento.getRifUpdDocumento().getIDDocumento()),
			    versamento, aroUpdUnitaDoc, progWarn);
		    break;
		case Annesso:
		    progWarn = buildWarn(
			    versamento.getControlliAnnesso(
				    documento.getRifUpdDocumento().getIDDocumento()),
			    versamento, aroUpdUnitaDoc, progWarn);
		    break;
		case Annotazione:
		    progWarn = buildWarn(
			    versamento.getControlliAnnotazione(
				    documento.getRifUpdDocumento().getIDDocumento()),
			    versamento, aroUpdUnitaDoc, progWarn);
		    break;
		default:
		    // niente da fare negli altri casi
		    break;
		}
	    }
	    entityManager.flush();
	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio dell'aggiornamento warning del documento: "
			    + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	    getLogger().error(
		    "Errore interno nella fase di salvataggio dell'aggiornamento warning del documento",
		    e);
	}

	return tmpRispostaControlli;
    }

    public RispostaControlli scriviAroWarnUpdUnitaDocForComp(UpdVersamentoExt versamento,
	    AroUpdUnitaDoc aroUpdUnitaDoc, StrutturaUpdVers strutturaUpdVers) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	tmpRispostaControlli.setrBoolean(false);
	int progWarn = 1;
	try {
	    /*
	     * 5.3. per ogni componente da aggiornare 5.3.1. per ogni controllo svolto sul doc da
	     * aggiornare con esito = WARNING (in ordine di numero d’ordine del controllo)
	     */
	    for (UpdDocumentoVers documento : strutturaUpdVers.getDocumentiAttesi()) {
		for (UpdComponenteVers componente : documento.getUpdComponentiAttesi()) {
		    switch (documento.getCategoriaDoc()) {
		    case Principale:
			progWarn = buildWarn(
				versamento.getControlliComponenteDocPrincipale(
					componente.getKeyCtrl()),
				versamento, aroUpdUnitaDoc, progWarn);
			break;
		    case Allegato:
			progWarn = buildWarn(
				versamento.getControlliComponenteAllegati(componente.getKeyCtrl()),
				versamento, aroUpdUnitaDoc, progWarn);
			break;
		    case Annesso:
			progWarn = buildWarn(
				versamento.getControlliComponenteAnnessi(componente.getKeyCtrl()),
				versamento, aroUpdUnitaDoc, progWarn);

			break;
		    case Annotazione:
			progWarn = buildWarn(
				versamento
					.getControlliComponenteAnnotazioni(componente.getKeyCtrl()),
				versamento, aroUpdUnitaDoc, progWarn);
			break;
		    default:
			// niente da fare negli altri casi
			break;
		    }

		}
	    }
	    entityManager.flush();
	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio dell'aggiornamento warning del documento: "
			    + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	    getLogger().error(
		    "Errore interno nella fase di salvataggio dell'aggiornamento warning del documento",
		    e);
	}

	return tmpRispostaControlli;
    }

    private int buildWarn(Set<ControlloEseguito> ctrlList, UpdVersamentoExt versamento,
	    AroUpdUnitaDoc tmpAroUpdUnitaDoc, int progWarn) {
	AroWarnUpdUnitaDoc tmpAroWarnUpdUnitaDoc;
	// filter
	Set<ControlloEseguito> filterCtrlList = ctrlList.stream()
		.filter(c -> c.getEsito().equals(VoceDiErrore.TipiEsitoErrore.WARNING))
		.collect(Collectors.toSet());
	// sort list
	List<ControlloEseguito> sortedCtrlList = versamento.sortControlli(filterCtrlList);
	for (ControlloEseguito controlloEseguito : sortedCtrlList) {
	    for (VoceDiErrore tmpVoceDiErrore : controlloEseguito.getErrori()) {
		tmpAroWarnUpdUnitaDoc = new AroWarnUpdUnitaDoc();
		// FK all’aggiornamento unità doc
		tmpAroWarnUpdUnitaDoc.setAroUpdUnitaDoc(tmpAroUpdUnitaDoc);
		// progressivo warning assegnato a partire da 1 ed incrementato per ogni warning
		// registrato
		tmpAroWarnUpdUnitaDoc.setPgWarn(new BigDecimal(progWarn));
		// FK al codice di errore
		tmpAroWarnUpdUnitaDoc.setDecControlloWs(
			controlliWSHelper.caricaCdControlloWs(controlloEseguito.getCdControllo()));
		// descrizione dell’errore
		tmpAroWarnUpdUnitaDoc.setDecErrSacer(
			messaggiWSHelper.caricaDecErrore(tmpVoceDiErrore.getErrorCode()));
		tmpAroWarnUpdUnitaDoc.setDsErr(
			LogSessioneUtils.getDsErrAtMaxLen(tmpVoceDiErrore.getErrorMessage()));

		//
		entityManager.persist(tmpAroWarnUpdUnitaDoc);
		// ad on list
		tmpAroUpdUnitaDoc.getAroWarnUpdUnitaDocs().add(tmpAroWarnUpdUnitaDoc);
		progWarn++;
	    }
	}
	return progWarn;
    }

    /**
     * Nota : al fine di evitare errore in fase di persistenza la lista dei fascicoli viene
     * normalizzata
     *
     * @param rispostaWs        risposta ws {@link RispostaWSUpdVers}
     * @param versamento        oggetto versamento {@link UpdVersamentoExt}
     * @param sessione          "fake" session di tipo {@link SyncFakeSessn}
     * @param tmpAroUnitaDoc    entity {@link AroUnitaDoc} "temporanea" (in lavorazione)
     * @param tmpAroUpdUnitaDoc entity {@link AroUpdUnitaDoc} "temporanea" (in lavorazione)
     * @param strutturaUpdVers  dto con dati controlli {@link StrutturaUpdVers}
     *
     * @return RispostaControlli con risultato operazione di persistanza su DB
     */
    public RispostaControlli scriviAroUpdArchivSec(RispostaWSUpdVers rispostaWs,
	    UpdVersamentoExt versamento, SyncFakeSessn sessione, AroUnitaDoc tmpAroUnitaDoc,
	    AroUpdUnitaDoc tmpAroUpdUnitaDoc, StrutturaUpdVers strutturaUpdVers) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	tmpRispostaControlli.setrBoolean(false);
	try {
	    if (versamento.hasPAFascicoliSecondariToUp()) {
		// normalizzza
		Map<List<String>, CamiciaFascicoloType> tmpFascicoliUnivoci = this
			.removeDuplicateFascicoli(versamento);

		for (CamiciaFascicoloType fascicolo : tmpFascicoliUnivoci.values()) {
		    //
		    AroUpdArchivSec tmpAroUpdArchivSec = new AroUpdArchivSec();

		    /*
		     * FK all’aggiornamento unità doc Info su archiviazione secondaria definite dai
		     * tag figli di “FascicoloSecondario” (ds_classif, cd_fascic, ds_oggetto_fascic,
		     * cd_sottofascic, ds_oggetto_sottofascic) ; se tag figlio non presente o con
		     * dimensione nulla si assegna valore nullo
		     */
		    tmpAroUpdArchivSec.setAroUpdUnitaDoc(tmpAroUpdUnitaDoc);
		    tmpAroUpdArchivSec
			    .setDsClassif(StringUtils.isNotBlank(fascicolo.getClassifica())
				    ? fascicolo.getClassifica()
				    : null);
		    // figli
		    if (fascicolo.getFascicolo() != null) {
			tmpAroUpdArchivSec.setCdFascic(
				StringUtils.isNotBlank(fascicolo.getFascicolo().getIdentificativo())
					? fascicolo.getFascicolo().getIdentificativo()
					: null);
			tmpAroUpdArchivSec.setDsOggettoFascic(
				StringUtils.isNotBlank(fascicolo.getFascicolo().getOggetto())
					? fascicolo.getFascicolo().getOggetto()
					: null);
		    }
		    if (fascicolo.getSottoFascicolo() != null) {
			tmpAroUpdArchivSec.setCdSottofascic(StringUtils
				.isNotBlank(fascicolo.getSottoFascicolo().getIdentificativo())
					? fascicolo.getSottoFascicolo().getIdentificativo()
					: null);
			tmpAroUpdArchivSec.setDsOggettoSottofascic(
				StringUtils.isNotBlank(fascicolo.getSottoFascicolo().getOggetto())
					? fascicolo.getSottoFascicolo().getOggetto()
					: null);
		    }
		    // persist
		    entityManager.persist(tmpAroUpdArchivSec);
		    // add on list
		    tmpAroUpdUnitaDoc.getAroUpdArchivSecs().add(tmpAroUpdArchivSec);

		}

	    } else if (!versamento.hasProfiloArchivisticoToUpdNull()) {
		// altrimenti si riportano i campi (se esistono) dell'unita doc
		for (AroArchivSec tmpAroArchivSec : tmpAroUnitaDoc.getAroArchivSecs()) {
		    AroUpdArchivSec tmpAroUpdArchivSec = new AroUpdArchivSec();

		    /*
		     * FK all’aggiornamento unità doc Info su archiviazione secondaria definite dai
		     * tag figli di “FascicoloSecondario” (ds_classif, cd_fascic, ds_oggetto_fascic,
		     * cd_sottofascic, ds_oggetto_sottofascic) ; se tag figlio non presente o con
		     * dimensione nulla si assegna valore nullo
		     */
		    tmpAroUpdArchivSec.setAroUpdUnitaDoc(tmpAroUpdUnitaDoc);
		    tmpAroUpdArchivSec.setDsClassif(tmpAroArchivSec.getDsClassif());
		    // figli
		    tmpAroUpdArchivSec.setCdFascic(tmpAroArchivSec.getCdFascic());
		    tmpAroUpdArchivSec.setDsOggettoFascic(tmpAroArchivSec.getDsOggettoFascic());
		    tmpAroUpdArchivSec.setCdSottofascic(tmpAroArchivSec.getCdSottofascic());
		    tmpAroUpdArchivSec
			    .setDsOggettoSottofascic(tmpAroArchivSec.getDsOggettoSottofascic());

		    // persist
		    entityManager.persist(tmpAroUpdArchivSec);
		    // add on list
		    tmpAroUpdUnitaDoc.getAroUpdArchivSecs().add(tmpAroUpdArchivSec);
		}
	    }

	    entityManager.flush();
	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    ERR_SALVA_XML_UD + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	    getLogger().error(ERR_SALVA_XML_UD, e);
	}

	return tmpRispostaControlli;
    }

    private Map<List<String>, CamiciaFascicoloType> removeDuplicateFascicoli(
	    UpdVersamentoExt versamento) {
	// preparo un array con i dati del fascicolo principale, per confrontarli
	// con quelli degli eventuali secondari ed eliminare i doppioni
	Map<List<String>, CamiciaFascicoloType> tmpFascicoliUnivoci = new HashMap<>();
	String[] tmpArrP = new String[] {
		"", "", "", "", "" };
	if (versamento.hasPAFascicoloPrincipaleToUpd()) {
	    CamiciaFascicoloType tmpFp = versamento.getVersamento().getUnitaDocumentaria()
		    .getProfiloArchivistico().getFascicoloPrincipale();

	    if (tmpFp.getClassifica() != null) {
		tmpArrP[0] = tmpFp.getClassifica();
	    }
	    if (tmpFp.getFascicolo() != null && tmpFp.getFascicolo().getIdentificativo() != null) {
		tmpArrP[1] = tmpFp.getFascicolo().getIdentificativo();
	    }
	    if (tmpFp.getFascicolo() != null && tmpFp.getFascicolo().getOggetto() != null) {
		tmpArrP[2] = tmpFp.getFascicolo().getOggetto();
	    }
	    if (tmpFp.getSottoFascicolo() != null
		    && tmpFp.getSottoFascicolo().getIdentificativo() != null) {
		tmpArrP[3] = tmpFp.getSottoFascicolo().getIdentificativo();
	    }
	    if (tmpFp.getSottoFascicolo() != null
		    && tmpFp.getSottoFascicolo().getOggetto() != null) {
		tmpArrP[4] = tmpFp.getSottoFascicolo().getOggetto();
	    }
	}
	List<String> tmpListP = Arrays.asList(tmpArrP);
	// elimina i fascicoli/sottofascicoli inseriti due volte
	// il problema si presenta con alcuni versatori poco precisi.
	// non si vuole rendere un messaggio di errore, perciò i doppioni vengono
	// eliminati senza segnalazioni
	for (CamiciaFascicoloType fascicolo : versamento.getVersamento().getUnitaDocumentaria()
		.getProfiloArchivistico().getFascicoliSecondari().getFascicoloSecondario()) {
	    String[] tmpArr = new String[] {
		    "", "", "", "", "" };
	    if (fascicolo.getClassifica() != null) {
		tmpArr[0] = fascicolo.getClassifica();
	    }
	    if (fascicolo.getFascicolo() != null
		    && fascicolo.getFascicolo().getIdentificativo() != null) {
		tmpArr[1] = fascicolo.getFascicolo().getIdentificativo();
	    }
	    if (fascicolo.getFascicolo() != null && fascicolo.getFascicolo().getOggetto() != null) {
		tmpArr[2] = fascicolo.getFascicolo().getOggetto();
	    }
	    if (fascicolo.getSottoFascicolo() != null
		    && fascicolo.getSottoFascicolo().getIdentificativo() != null) {
		tmpArr[3] = fascicolo.getSottoFascicolo().getIdentificativo();
	    }
	    if (fascicolo.getSottoFascicolo() != null
		    && fascicolo.getSottoFascicolo().getOggetto() != null) {
		tmpArr[4] = fascicolo.getSottoFascicolo().getOggetto();
	    }
	    List<String> tmpList = Arrays.asList(tmpArr);
	    if (!tmpListP.equals(tmpList)) {
		// se il fascicolo è uguale al principale, non lo considero
		// in ogni caso inserisco il dato in un'hashmap, così i fascicoli duplicati
		// vengono caricati una volta sola
		tmpFascicoliUnivoci.put(tmpList, fascicolo);
	    }
	}

	return tmpFascicoliUnivoci;
    }

    public RispostaControlli scriviAroUpdLinkUnitaDoc(UpdVersamentoExt versamento,
	    AroUnitaDoc tmpAroUnitaDoc, AroUpdUnitaDoc tmpAroUpdUnitaDoc,
	    StrutturaUpdVers strutturaUpdVers) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	tmpRispostaControlli.setrBoolean(false);

	try {

	    if (versamento.hasDocumentiCollegatiToUpd()) {
		for (UpdUnitaDocColl updUnitaDocColl : strutturaUpdVers.getUnitaDocCollegate()) {
		    AroUpdLinkUnitaDoc tmpAroUpdLinkUnitaDoc = new AroUpdLinkUnitaDoc();

		    // FK all’aggiornamento unità doc
		    tmpAroUpdLinkUnitaDoc.setAroUpdUnitaDoc(tmpAroUpdUnitaDoc);
		    // registro, anno e numero del documento collegato
		    tmpAroUpdLinkUnitaDoc.setAaKeyUnitaDocLink(
			    new BigDecimal(updUnitaDocColl.getAggChiave().getAnno()));
		    tmpAroUpdLinkUnitaDoc.setCdRegistroKeyUnitaDocLink(
			    updUnitaDocColl.getAggChiave().getTipoRegistro());
		    tmpAroUpdLinkUnitaDoc
			    .setCdKeyUnitaDocLink(updUnitaDocColl.getAggChiave().getNumero());
		    // descrizione del collegamento
		    tmpAroUpdLinkUnitaDoc.setDsLinkUnitaDoc(updUnitaDocColl.generaDescrizione());
		    // identificatore dell’unità doc collegata
		    if (updUnitaDocColl.getIdUnitaDocLink() != -1) {
			tmpAroUpdLinkUnitaDoc.setAroUnitaDocLink(entityManager
				.find(AroUnitaDoc.class, updUnitaDocColl.getIdUnitaDocLink()));
		    }

		    // persist
		    entityManager.persist(tmpAroUpdLinkUnitaDoc);
		    // add on list
		    tmpAroUpdUnitaDoc.getAroUpdLinkUnitaDocs().add(tmpAroUpdLinkUnitaDoc);
		}
	    } else {
		for (AroLinkUnitaDoc unitaDocColl : tmpAroUnitaDoc.getAroLinkUnitaDocs()) {
		    AroUpdLinkUnitaDoc tmpAroUpdLinkUnitaDoc = new AroUpdLinkUnitaDoc();

		    // FK all’aggiornamento unità doc
		    tmpAroUpdLinkUnitaDoc.setAroUpdUnitaDoc(tmpAroUpdUnitaDoc);
		    // registro, anno e numero del documento collegato
		    tmpAroUpdLinkUnitaDoc.setAaKeyUnitaDocLink(unitaDocColl.getAaKeyUnitaDocLink());
		    tmpAroUpdLinkUnitaDoc.setCdRegistroKeyUnitaDocLink(
			    unitaDocColl.getCdRegistroKeyUnitaDocLink());
		    tmpAroUpdLinkUnitaDoc.setCdKeyUnitaDocLink(unitaDocColl.getCdKeyUnitaDocLink());
		    // descrizione del collegamento
		    tmpAroUpdLinkUnitaDoc.setDsLinkUnitaDoc(unitaDocColl.getDsLinkUnitaDoc());
		    // identificatore dell’unità doc collegata
		    tmpAroUpdLinkUnitaDoc.setAroUnitaDocLink(unitaDocColl.getAroUnitaDocLink());

		    // persist
		    entityManager.persist(tmpAroUpdLinkUnitaDoc);
		    // add on list
		    tmpAroUpdUnitaDoc.getAroUpdLinkUnitaDocs().add(tmpAroUpdLinkUnitaDoc);
		}
	    }

	    entityManager.flush();
	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    ERR_SALVA_XML_UD + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	    getLogger().error(ERR_SALVA_XML_UD, e);
	}

	return tmpRispostaControlli;
    }

    public RispostaControlli scriviAroUpdDatiSpecUnitaDoc(UpdVersamentoExt versamento,
	    SyncFakeSessn sessione, AroUnitaDoc tmpAroUnitaDoc, AroUpdUnitaDoc tmpAroUpdUnitaDoc,
	    TiUsoXsdAroUpdDatiSpecUnitaDoc tiUsoXsd, TiEntitaAroUpdDatiSpecUnitaDoc tiEntitaSacer,
	    BackendStorage backendMetadata,
	    Map<DatiSpecLinkOsKeyMap, Map<String, String>> updDatiSpecBlob,
	    StrutturaUpdVers strutturaUpdVers) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	tmpRispostaControlli.setrBoolean(false);

	try {

	    // MEV#29276
	    DatiSpecLinkOsKeyMap key = new DatiSpecLinkOsKeyMap(
		    tmpAroUpdUnitaDoc.getIdUpdUnitaDoc(),
		    TiEntitaAroUpdDatiSpecUnitaDoc.UPD_UNI_DOC.name());
	    Map<String, String> updDatiSpecUdBlob = (updDatiSpecBlob.containsKey(key))
		    ? updDatiSpecBlob.get(key)
		    : new HashMap<>();
	    // end MEV#29276

	    if (tiUsoXsd.equals(TiUsoXsdAroUpdDatiSpecUnitaDoc.VERS)) {
		if (versamento.hasDatiSpecificiToBeUpdated()
			&& strutturaUpdVers.getIdRecXsdDatiSpec() != 0) {
		    // build entity
		    buildAroUpdDatiSpecUnitaDocFromUpd(sessione,
			    versamento.getVersamento().getUnitaDocumentaria().getDatiSpecifici(),
			    tmpAroUpdUnitaDoc, null, null, tiUsoXsd, tiEntitaSacer, backendMetadata,
			    updDatiSpecUdBlob, strutturaUpdVers.getIdRecXsdDatiSpec());
		} else {
		    tmpRispostaControlli = super.checkUsoXsdDatiSpecifici(
			    tmpAroUnitaDoc.getIdUnitaDoc(), CostantiDB.TipiUsoDatiSpec.VERS,
			    CostantiDB.TipiEntitaSacer.UNI_DOC);

		    // errore query
		    if (!tmpRispostaControlli.isrBoolean()) {
			return tmpRispostaControlli;
		    }

		    // è presente ...
		    if (tmpRispostaControlli.getrLong() != -1) {
			AroUsoXsdDatiSpec aroUsoXsdDatiSpec = (AroUsoXsdDatiSpec) tmpRispostaControlli
				.getrObject();
			//
			tmpRispostaControlli = buildAroUpdDatiSpecUnitaDocFromAro(sessione,
				tmpAroUpdUnitaDoc, null, null, aroUsoXsdDatiSpec, backendMetadata,
				updDatiSpecUdBlob);

			// in caso di errore esecuzione query per ricerva valori su attributo
			if (!tmpRispostaControlli.isrBoolean()) {
			    return tmpRispostaControlli;
			}
		    }
		}
	    } // if VERS

	    if (tiUsoXsd.equals(TiUsoXsdAroUpdDatiSpecUnitaDoc.MIGRAZ)) {
		if (versamento.hasDatiSpecificiMigrazioneToUpd()
			&& strutturaUpdVers.getIdRecXsdDatiSpecMigrazione() != 0) {
		    // build entity
		    buildAroUpdDatiSpecUnitaDocFromUpd(sessione,
			    versamento.getVersamento().getUnitaDocumentaria()
				    .getDatiSpecificiMigrazione(),
			    tmpAroUpdUnitaDoc, null, null, tiUsoXsd, tiEntitaSacer, backendMetadata,
			    updDatiSpecUdBlob, strutturaUpdVers.getIdRecXsdDatiSpecMigrazione());
		} else {
		    tmpRispostaControlli = super.checkUsoXsdDatiSpecifici(
			    tmpAroUnitaDoc.getIdUnitaDoc(), CostantiDB.TipiUsoDatiSpec.MIGRAZ,
			    CostantiDB.TipiEntitaSacer.UNI_DOC);

		    // errore query
		    if (!tmpRispostaControlli.isrBoolean()) {
			return tmpRispostaControlli;
		    }

		    // è presente ...
		    if (tmpRispostaControlli.getrLong() != -1) {
			AroUsoXsdDatiSpec aroUsoXsdDatiSpec = (AroUsoXsdDatiSpec) tmpRispostaControlli
				.getrObject();
			//
			tmpRispostaControlli = buildAroUpdDatiSpecUnitaDocFromAro(sessione,
				tmpAroUpdUnitaDoc, null, null, aroUsoXsdDatiSpec, backendMetadata,
				updDatiSpecUdBlob);

			// in caso di errore esecuzione query per ricerva valori su attributo
			if (!tmpRispostaControlli.isrBoolean()) {
			    return tmpRispostaControlli;
			}
		    }
		}
	    } // if MIGRAZ

	    // MEV#29276
	    if (backendMetadata.isObjectStorage() && !updDatiSpecBlob.containsKey(key)) {
		updDatiSpecBlob.put(key, updDatiSpecUdBlob);
	    }
	    // end MEV#29276

	    entityManager.flush();
	    tmpRispostaControlli.setrBoolean(true);

	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    ERR_SALVA_XML_UD + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	    getLogger().error(ERR_SALVA_XML_UD, e);
	}

	return tmpRispostaControlli;
    }

    private void buildAroUpdDatiSpecUnitaDocFromUpd(SyncFakeSessn sessione,
	    JAXBElement<it.eng.parer.ws.xml.versUpdReq.DatiSpecificiType> datiSpecifici,
	    AroUpdUnitaDoc tmpAroUpdUnitaDoc, AroUpdDocUnitaDoc tmpAroUpdDocUnitaDoc,
	    AroUpdCompUnitaDoc tmpAroUpdCompUnitaDoc, TiUsoXsdAroUpdDatiSpecUnitaDoc tiUsoXsd,
	    TiEntitaAroUpdDatiSpecUnitaDoc tiEntitaSacer, BackendStorage backendMetadata,
	    Map<String, String> tmpUpdDatiSpecBlob, long idRecXsdDatiSpec) throws JAXBException {

	AroUpdDatiSpecUnitaDoc tmpAroUpdDatiSpecUnitaDoc = new AroUpdDatiSpecUnitaDoc();
	// FK all’aggiornamento unità doc
	tmpAroUpdDatiSpecUnitaDoc.setAroUpdUnitaDoc(tmpAroUpdUnitaDoc);
	// FK all'aggiornamento doc unità doc
	tmpAroUpdDatiSpecUnitaDoc.setAroUpdDocUnitaDoc(tmpAroUpdDocUnitaDoc);
	// FK comp
	tmpAroUpdDatiSpecUnitaDoc.setAroUpdCompUnitaDoc(tmpAroUpdCompUnitaDoc);
	// FK struttura
	tmpAroUpdDatiSpecUnitaDoc.setOrgStrut(tmpAroUpdUnitaDoc.getAroUnitaDoc().getOrgStrut());
	// dt inizio sessione
	final LocalDate dtIniSes = convertLocal(sessione.getTmApertura());
	tmpAroUpdDatiSpecUnitaDoc.setDtIniSes(dtIniSes);
	// MEV#30089
	tmpAroUpdDatiSpecUnitaDoc.setAaIniSes(dtIniSes.getYear());
	// * tipo di uso del xsd pari a
	tmpAroUpdDatiSpecUnitaDoc.setTiUsoXsd(tiUsoXsd);
	// tipo entita sacer pari a
	tmpAroUpdDatiSpecUnitaDoc.setTiEntitaSacer(tiEntitaSacer);
	// FK alla versione XSD definito dall’unita doc da aggiornare
	tmpAroUpdDatiSpecUnitaDoc
		.setDecXsdDatiSpec(entityManager.find(DecXsdDatiSpec.class, idRecXsdDatiSpec));
	// MEV#29276
	// Clob contenente il frammento XML contenuto nel tag “DatiSpecifici” del XML in
	String blXmlDatiSpec = generaXmlDatiSpecFromUpd(datiSpecifici);
	if (backendMetadata.isDataBase()) {
	    tmpAroUpdDatiSpecUnitaDoc.setBlXmlDatiSpec(blXmlDatiSpec);
	} else {
	    tmpUpdDatiSpecBlob.put(tiUsoXsd.name(), blXmlDatiSpec);
	}
	// end MEV#29276

	// persist
	entityManager.persist(tmpAroUpdDatiSpecUnitaDoc);
	// add on list
	tmpAroUpdUnitaDoc.getAroUpdDatiSpecUnitaDocs().add(tmpAroUpdDatiSpecUnitaDoc);

	if (tmpAroUpdDocUnitaDoc != null) {
	    tmpAroUpdDocUnitaDoc.getAroUpdDatiSpecUnitaDocs().add(tmpAroUpdDatiSpecUnitaDoc);
	}

	if (tmpAroUpdCompUnitaDoc != null) {
	    tmpAroUpdCompUnitaDoc.getAroUpdDatiSpecUnitaDocs().add(tmpAroUpdDatiSpecUnitaDoc);
	}

    }

    private String generaXmlDatiSpecFromUpd(
	    JAXBElement<it.eng.parer.ws.xml.versUpdReq.DatiSpecificiType> datiSpecifici)
	    throws JAXBException {
	//
	// TODO: non dovrebbe occorrere la validazione dato che è stata già fatta in
	// fase di controllo !
	// vedi UpdGestioneDatiSpec
	//
	Marshaller tmpMarshaller = xmlUpdVersCache.getVersReqCtxforDatiSpecifici()
		.createMarshaller();
	StringWriter sw = new StringWriter();
	tmpMarshaller.marshal(datiSpecifici, sw);
	return sw.toString();
    }

    public RispostaControlli scriviElvUpdUdDaElabElenco(SyncFakeSessn sessione,
	    AroUnitaDoc tmpAroUnitaDoc, AroUpdUnitaDoc tmpAroUpdUnitaDoc,
	    StrutturaUpdVers strutturaUpdVers) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	tmpRispostaControlli.setrBoolean(false);

	try {

	    ElvUpdUdDaElabElenco tmpElvUpdUdDaElabElenco = new ElvUpdUdDaElabElenco();

	    // FK all’aggiornamento unità doc
	    tmpElvUpdUdDaElabElenco.setAroUpdUnitaDoc(tmpAroUpdUnitaDoc);
	    // Identificatore struttura ed anno della chiave pari ai valori definiti
	    // sull’unità doc in aggiornamento
	    tmpElvUpdUdDaElabElenco.setOrgStrut(
		    entityManager.find(OrgStrut.class, strutturaUpdVers.getIdStruttura()));
	    // Identificatore della sub struttura pari al valore definito sull’unità doc in
	    // aggiornamento
	    tmpElvUpdUdDaElabElenco.setOrgSubStrut(tmpAroUnitaDoc.getOrgSubStrut());

	    tmpElvUpdUdDaElabElenco.setAaKeyUnitaDoc(
		    new BigDecimal(strutturaUpdVers.getChiaveNonVerificata().getAnno()));
	    // Data creazione pari al timestamp di inizio sessione di aggiornamento
	    tmpElvUpdUdDaElabElenco.setDtCreazione(convert(sessione.getTmApertura()));
	    // stato dell’aggiornamento rispetto al processo degli elenchi di versamento =
	    // IN_ATTESA_SCHED
	    tmpElvUpdUdDaElabElenco
		    .setTiStatoUpdElencoVers(ElvUpdUdDaElabTiStatoUpdElencoVers.IN_ATTESA_SCHED);
	    // registro
	    tmpElvUpdUdDaElabElenco.setDecRegistroUnitaDoc(entityManager
		    .find(DecRegistroUnitaDoc.class, strutturaUpdVers.getIdRegistro()));
	    // tipo ud
	    tmpElvUpdUdDaElabElenco.setDecTipoUnitaDoc(entityManager.find(DecTipoUnitaDoc.class,
		    strutturaUpdVers.getIdTipologiaUnitaDocumentaria()));
	    // topo doc princ
	    tmpElvUpdUdDaElabElenco.setDecTipoDocPrinc(entityManager.find(DecTipoDoc.class,
		    strutturaUpdVers.getIdTipoDocPrincipale()));
	    // persist
	    entityManager.persist(tmpElvUpdUdDaElabElenco);
	    // add on list
	    tmpAroUpdUnitaDoc.getElvUpdUdDaElabElencos().add(tmpElvUpdUdDaElabElenco);

	    tmpRispostaControlli.setrObject(tmpElvUpdUdDaElabElenco);
	    entityManager.flush();
	    strutturaUpdVers.setTiStatoUpdElencoVers(tmpAroUpdUnitaDoc.getTiStatoUpdElencoVers());
	    tmpRispostaControlli.setrBoolean(true);

	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio dell'aggiornamento della coda elenco dell'unità documentaria: "
			    + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	    getLogger().error(
		    "Errore interno nella fase di salvataggio dell'aggiornamento della coda elenco dell'unità documentaria",
		    e);
	}

	return tmpRispostaControlli;
    }

    public RispostaControlli scriviAroUpdDocUnitaDoc(UpdVersamentoExt versamento,
	    SyncFakeSessn sessione, AroUpdUnitaDoc tmpAroUpdUnitaDoc,
	    BackendStorage backendMetadata,
	    Map<DatiSpecLinkOsKeyMap, Map<String, String>> updDatiSpecBlob,
	    StrutturaUpdVers strutturaUpdVers) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	tmpRispostaControlli.setrBoolean(false);

	try {
	    List<UpdDocumentoVers> updDocs = null;
	    if (versamento.hasDocumentoPrincipaleToUpd()) {
		// recupero documenti di tipo principale da documenti attesi
		updDocs = strutturaUpdVers.getDocumentiAttesi().stream()
			.filter(d -> d.getCategoriaDoc().equals(CategoriaDocumento.Principale))
			.collect(Collectors.toList());
		// build entity AroUpdDocUnitaDoc
		tmpRispostaControlli = buildAroUpdDocFromUpd(sessione, tmpAroUpdUnitaDoc,
			backendMetadata, updDatiSpecBlob, updDocs);

		// in caso di errore esecuzione esco
		if (!tmpRispostaControlli.isrBoolean()) {
		    return tmpRispostaControlli;
		}

	    }
	    if (versamento.hasAllegatiToUpd()) {
		// recupero documenti di tipo principale da documenti attesi
		updDocs = strutturaUpdVers.getDocumentiAttesi().stream()
			.filter(d -> d.getCategoriaDoc().equals(CategoriaDocumento.Allegato))
			.collect(Collectors.toList());
		// build entity
		tmpRispostaControlli = buildAroUpdDocFromUpd(sessione, tmpAroUpdUnitaDoc,
			backendMetadata, updDatiSpecBlob, updDocs);

		// in caso di errore esecuzione esco
		if (!tmpRispostaControlli.isrBoolean()) {
		    return tmpRispostaControlli;
		}

	    } /* allegati */
	    if (versamento.hasAnnessiToUpd()) {
		// recupero documenti di tipo principale da documenti attesi
		updDocs = strutturaUpdVers.getDocumentiAttesi().stream()
			.filter(d -> d.getCategoriaDoc().equals(CategoriaDocumento.Annesso))
			.collect(Collectors.toList());
		// build entity
		tmpRispostaControlli = buildAroUpdDocFromUpd(sessione, tmpAroUpdUnitaDoc,
			backendMetadata, updDatiSpecBlob, updDocs);

		// in caso di errore esecuzione esco
		if (!tmpRispostaControlli.isrBoolean()) {
		    return tmpRispostaControlli;
		}

	    } /* annessi */
	    if (versamento.hasAnnotazioniToUpd()) {
		// recupero documenti di tipo principale da documenti attesi
		updDocs = strutturaUpdVers.getDocumentiAttesi().stream()
			.filter(d -> d.getCategoriaDoc().equals(CategoriaDocumento.Annotazione))
			.collect(Collectors.toList());
		// build entity
		tmpRispostaControlli = buildAroUpdDocFromUpd(sessione, tmpAroUpdUnitaDoc,
			backendMetadata, updDatiSpecBlob, updDocs);

		// in caso di errore esecuzione esco
		if (!tmpRispostaControlli.isrBoolean()) {
		    return tmpRispostaControlli;
		}

	    } /* annotazione */

	    entityManager.flush();
	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio dell'aggiornamento del documento dell'unità documentaria: "
			    + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	    getLogger().error(
		    "Errore interno nella fase di salvataggio dell'aggiornamento del documento dell'unità documentaria",
		    e);
	}

	return tmpRispostaControlli;
    }

    private RispostaControlli buildAroUpdDocFromUpd(SyncFakeSessn sessione,
	    AroUpdUnitaDoc tmpAroUpdUnitaDoc, BackendStorage backendMetadata,
	    Map<DatiSpecLinkOsKeyMap, Map<String, String>> updDatiSpecBlob,
	    List<UpdDocumentoVers> documenti) throws JAXBException, ParserConfigurationException {

	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	tmpRispostaControlli.setrBoolean(false);

	// per ogni documento da aggiornare ...
	for (UpdDocumentoVers documento : documenti) {
	    Map<String, Object> properties = new HashMap<>();
	    properties.put(Constants.JAVAX_PERSISTENCE_LOCK_TIMEOUT, 25000);
	    AroDoc aroDoc = entityManager.find(AroDoc.class, documento.getIdRecDocumentoDB(),
		    LockModeType.PESSIMISTIC_WRITE, properties);

	    AroUpdDocUnitaDoc tmpAroUpdDocUnitaDoc = new AroUpdDocUnitaDoc();
	    // FK all’aggiornamento unità doc
	    tmpAroUpdDocUnitaDoc.setAroUpdUnitaDoc(tmpAroUpdUnitaDoc);
	    // FK al documento definita da doc da aggiornare
	    tmpAroUpdDocUnitaDoc.setAroDoc(aroDoc);
	    /*
	     * info su profilo documento definite da tag “ProfiloDocumento” (e relativi tag figli)
	     * del XML in input
	     *
	     * se tag figlio non presente o con dimensione nulla si assegna valore nullo
	     */
	    if (documento.getRifUpdDocumento().getProfiloDocumento() != null) {
		tmpAroUpdDocUnitaDoc.setDsAutoreDoc(StringUtils.isNotBlank(
			documento.getRifUpdDocumento().getProfiloDocumento().getAutore())
				? documento.getRifUpdDocumento().getProfiloDocumento().getAutore()
				: null);
		tmpAroUpdDocUnitaDoc.setDlDoc(StringUtils.isNotBlank(
			documento.getRifUpdDocumento().getProfiloDocumento().getDescrizione())
				? documento.getRifUpdDocumento().getProfiloDocumento()
					.getDescrizione()
				: null);
		//
		tmpAroUpdDocUnitaDoc.setFlUpdProfiloDoc("1");

	    } else {
		tmpAroUpdDocUnitaDoc.setDsAutoreDoc(aroDoc.getDsAutoreDoc()); // from aro
		tmpAroUpdDocUnitaDoc.setDlDoc(aroDoc.getDlDoc());// from aro
		//
		tmpAroUpdDocUnitaDoc.setFlUpdProfiloDoc("0");
	    }
	    //
	    tmpAroUpdDocUnitaDoc.setFlUpdDatiSpec(
		    booleanToFlag(documento.getRifUpdDocumento().getDatiSpecifici() != null));
	    //
	    tmpAroUpdDocUnitaDoc.setFlUpdDatiSpecMigraz(booleanToFlag(
		    documento.getRifUpdDocumento().getDatiSpecificiMigrazione() != null));

	    // persist
	    entityManager.persist(tmpAroUpdDocUnitaDoc);
	    // add on list
	    tmpAroUpdUnitaDoc.getAroUpdDocUnitaDocs().add(tmpAroUpdDocUnitaDoc);

	    // MEV#29276
	    DatiSpecLinkOsKeyMap key = new DatiSpecLinkOsKeyMap(
		    tmpAroUpdDocUnitaDoc.getIdUpdDocUnitaDoc(),
		    TiEntitaAroUpdDatiSpecUnitaDoc.UPD_DOC.name());
	    Map<String, String> updDatiSpecDocBlob = (updDatiSpecBlob.containsKey(key))
		    ? updDatiSpecBlob.get(key)
		    : new HashMap<>();
	    // end MEV#29276

	    // 17.2. se nel XML in input il tag “DatiSpecifici” per il documento corrente e’
	    // definito
	    if (documento.getRifUpdDocumento().getDatiSpecifici() != null
		    && documento.getIdRecXsdDatiSpec() != 0) {

		buildAroUpdDatiSpecUnitaDocFromUpd(sessione, tmpAroUpdUnitaDoc,
			tmpAroUpdDocUnitaDoc, null, TiUsoXsdAroUpdDatiSpecUnitaDoc.VERS,
			TiEntitaAroUpdDatiSpecUnitaDoc.UPD_DOC,
			documento.getRifUpdDocumento().getDatiSpecifici(), backendMetadata,
			updDatiSpecDocBlob, documento.getIdRecXsdDatiSpec());

	    } else {
		// build from aro
		tmpRispostaControlli = super.checkUsoXsdDatiSpecifici(aroDoc.getIdDoc(),
			CostantiDB.TipiUsoDatiSpec.VERS, CostantiDB.TipiEntitaSacer.DOC);

		// errore query
		if (!tmpRispostaControlli.isrBoolean()) {
		    return tmpRispostaControlli;
		}

		// è presente ...
		if (tmpRispostaControlli.getrLong() != -1) {
		    AroUsoXsdDatiSpec aroUsoXsdDatiSpec = (AroUsoXsdDatiSpec) tmpRispostaControlli
			    .getrObject();
		    //
		    tmpRispostaControlli = buildAroUpdDatiSpecUnitaDocFromAro(sessione,
			    tmpAroUpdUnitaDoc, tmpAroUpdDocUnitaDoc, null, aroUsoXsdDatiSpec,
			    backendMetadata, updDatiSpecDocBlob);

		    // in caso di errore esecuzione query per ricerva valori su attributo
		    if (!tmpRispostaControlli.isrBoolean()) {
			return tmpRispostaControlli;
		    }
		}
	    }

	    // 17.4. se nel XML in input il tag “DatiSpecificiMigrazione” per il documento
	    // corrente e’ definito
	    // definito
	    if (documento.getRifUpdDocumento().getDatiSpecificiMigrazione() != null
		    && documento.getIdRecXsdDatiSpecMigrazione() != 0) {

		buildAroUpdDatiSpecUnitaDocFromUpd(sessione, tmpAroUpdUnitaDoc,
			tmpAroUpdDocUnitaDoc, null, TiUsoXsdAroUpdDatiSpecUnitaDoc.MIGRAZ,
			TiEntitaAroUpdDatiSpecUnitaDoc.UPD_DOC,
			documento.getRifUpdDocumento().getDatiSpecificiMigrazione(),
			backendMetadata, updDatiSpecDocBlob,
			documento.getIdRecXsdDatiSpecMigrazione());
	    } else {
		// build from aro
		tmpRispostaControlli = super.checkUsoXsdDatiSpecifici(aroDoc.getIdDoc(),
			CostantiDB.TipiUsoDatiSpec.MIGRAZ, CostantiDB.TipiEntitaSacer.DOC);

		// errore query
		if (!tmpRispostaControlli.isrBoolean()) {
		    return tmpRispostaControlli;
		}

		// è presente ...
		if (tmpRispostaControlli.getrLong() != -1) {
		    AroUsoXsdDatiSpec aroUsoXsdDatiSpec = (AroUsoXsdDatiSpec) tmpRispostaControlli
			    .getrObject();
		    //
		    tmpRispostaControlli = buildAroUpdDatiSpecUnitaDocFromAro(sessione,
			    tmpAroUpdUnitaDoc, tmpAroUpdDocUnitaDoc, null, aroUsoXsdDatiSpec,
			    backendMetadata, updDatiSpecDocBlob);

		    // in caso di errore esecuzione query per ricerva valori su attributo
		    if (!tmpRispostaControlli.isrBoolean()) {
			return tmpRispostaControlli;
		    }
		}

	    }

	    // MEV#29276
	    if (backendMetadata.isObjectStorage() && !updDatiSpecBlob.containsKey(key)) {
		updDatiSpecBlob.put(key, updDatiSpecDocBlob);
	    }
	    // end MEV#29276

	    // 18. inserisci record in ARO_UPD_COMP_UNITA_DOC:
	    for (UpdComponenteVers componente : documento.getUpdComponentiAttesi()) {

		tmpRispostaControlli = buildAroUpdCompUnitaDocFromUpd(sessione, tmpAroUpdUnitaDoc,
			tmpAroUpdDocUnitaDoc, backendMetadata, updDatiSpecBlob, componente);

		// in caso di errore esecuzione
		if (!tmpRispostaControlli.isrBoolean()) {
		    return tmpRispostaControlli;
		}

	    } // for
	} // for

	entityManager.flush();
	tmpRispostaControlli.setrBoolean(true);

	return tmpRispostaControlli;
    }

    private RispostaControlli buildAroUpdCompUnitaDocFromUpd(SyncFakeSessn sessione,
	    AroUpdUnitaDoc tmpAroUpdUnitaDoc, AroUpdDocUnitaDoc tmpAroUpdDocUnitaDoc,
	    BackendStorage backendMetadata,
	    Map<DatiSpecLinkOsKeyMap, Map<String, String>> updDatiSpecBlob,
	    UpdComponenteVers componente) throws JAXBException, ParserConfigurationException {

	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	tmpRispostaControlli.setrBoolean(false);

	Map<String, Object> properties = new HashMap<>();
	properties.put(Constants.JAVAX_PERSISTENCE_LOCK_TIMEOUT, 25000);
	AroCompDoc tmpAroCompDoc = entityManager.find(AroCompDoc.class, componente.getIdRecDB(),
		LockModeType.PESSIMISTIC_WRITE, properties);

	AroUpdCompUnitaDoc tmpAroUpdCompUnitaDoc = new AroUpdCompUnitaDoc();
	// FK all’aggiornamento documento
	tmpAroUpdCompUnitaDoc.setAroUpdDocUnitaDoc(tmpAroUpdDocUnitaDoc);
	// FK al componente definita da comp da aggiornare
	tmpAroUpdCompUnitaDoc.setAroCompDoc(tmpAroCompDoc);
	/*
	 * info su componente definite da tag “Componente” (e relativi tag figli) del XML in input
	 * (ds_nome_comp_vers, dl_urn_comp_vers e ds_id_comp_vers)
	 */
	tmpAroUpdCompUnitaDoc.setDlUrnCompVers(
		StringUtils.isNotBlank(componente.getMyUpdComponente().getUrnVersato())
			? componente.getMyUpdComponente().getUrnVersato()
			: null);
	tmpAroUpdCompUnitaDoc.setDsIdCompVers(
		StringUtils.isNotBlank(componente.getMyUpdComponente().getIDComponenteVersato())
			? componente.getMyUpdComponente().getIDComponenteVersato()
			: null);
	tmpAroUpdCompUnitaDoc.setDsNomeCompVers(
		StringUtils.isNotBlank(componente.getMyUpdComponente().getNomeComponente())
			? componente.getMyUpdComponente().getNomeComponente()
			: null);

	//
	tmpAroUpdCompUnitaDoc.setFlUpdDatiSpec(
		booleanToFlag(componente.getMyUpdComponente().getDatiSpecifici() != null));
	//
	tmpAroUpdCompUnitaDoc.setFlUpdDatiSpecMigraz(booleanToFlag(
		componente.getMyUpdComponente().getDatiSpecificiMigrazione() != null));

	// persist
	entityManager.persist(tmpAroUpdCompUnitaDoc);
	// add on list
	tmpAroUpdDocUnitaDoc.getAroUpdCompUnitaDocs().add(tmpAroUpdCompUnitaDoc);

	// MEV#29276
	DatiSpecLinkOsKeyMap key = new DatiSpecLinkOsKeyMap(
		tmpAroUpdCompUnitaDoc.getIdUpdCompUnitaDoc(),
		TiEntitaAroUpdDatiSpecUnitaDoc.UPD_COMP.name());
	Map<String, String> updDatiSpecCompBlob = (updDatiSpecBlob.containsKey(key))
		? updDatiSpecBlob.get(key)
		: new HashMap<>();
	// end MEV#29276

	// definito
	if (componente.getMyUpdComponente().getDatiSpecifici() != null
		&& componente.getIdRecXsdDatiSpec() != 0) {

	    buildAroUpdDatiSpecUnitaDocFromUpd(sessione, tmpAroUpdUnitaDoc, tmpAroUpdDocUnitaDoc,
		    tmpAroUpdCompUnitaDoc, TiUsoXsdAroUpdDatiSpecUnitaDoc.VERS,
		    TiEntitaAroUpdDatiSpecUnitaDoc.UPD_COMP,
		    componente.getMyUpdComponente().getDatiSpecifici(), backendMetadata,
		    updDatiSpecCompBlob, componente.getIdRecXsdDatiSpec());
	} else {
	    // build from aro
	    tmpRispostaControlli = super.checkUsoXsdDatiSpecifici(tmpAroCompDoc.getIdCompDoc(),
		    CostantiDB.TipiUsoDatiSpec.VERS, CostantiDB.TipiEntitaSacer.COMP);

	    // errore query
	    if (!tmpRispostaControlli.isrBoolean()) {
		return tmpRispostaControlli;
	    }

	    // è presente ...
	    if (tmpRispostaControlli.getrLong() != -1) {
		AroUsoXsdDatiSpec aroUsoXsdDatiSpec = (AroUsoXsdDatiSpec) tmpRispostaControlli
			.getrObject();
		//
		tmpRispostaControlli = buildAroUpdDatiSpecUnitaDocFromAro(sessione,
			tmpAroUpdUnitaDoc, tmpAroUpdDocUnitaDoc, tmpAroUpdCompUnitaDoc,
			aroUsoXsdDatiSpec, backendMetadata, updDatiSpecCompBlob);

		// in caso di errore esecuzione query per ricerva valori su attributo
		if (!tmpRispostaControlli.isrBoolean()) {
		    return tmpRispostaControlli;
		}

	    }
	}
	// 17.4. se nel XML in input il tag “DatiSpecificiMigrazione” per il documento
	// corrente e’ definito
	// definito
	if (componente.getMyUpdComponente().getDatiSpecificiMigrazione() != null
		&& componente.getIdRecXsdDatiSpecMigrazione() != 0) {

	    buildAroUpdDatiSpecUnitaDocFromUpd(sessione, tmpAroUpdUnitaDoc, tmpAroUpdDocUnitaDoc,
		    tmpAroUpdCompUnitaDoc, TiUsoXsdAroUpdDatiSpecUnitaDoc.MIGRAZ,
		    TiEntitaAroUpdDatiSpecUnitaDoc.UPD_COMP,
		    componente.getMyUpdComponente().getDatiSpecificiMigrazione(), backendMetadata,
		    updDatiSpecCompBlob, componente.getIdRecXsdDatiSpecMigrazione());
	} else {
	    // build from aro
	    tmpRispostaControlli = super.checkUsoXsdDatiSpecifici(tmpAroCompDoc.getIdCompDoc(),
		    CostantiDB.TipiUsoDatiSpec.MIGRAZ, CostantiDB.TipiEntitaSacer.COMP);

	    // errore query
	    if (!tmpRispostaControlli.isrBoolean()) {
		return tmpRispostaControlli;
	    }

	    // è presente ...
	    if (tmpRispostaControlli.getrLong() != -1) {
		AroUsoXsdDatiSpec aroUsoXsdDatiSpec = (AroUsoXsdDatiSpec) tmpRispostaControlli
			.getrObject();
		//
		tmpRispostaControlli = buildAroUpdDatiSpecUnitaDocFromAro(sessione,
			tmpAroUpdUnitaDoc, tmpAroUpdDocUnitaDoc, tmpAroUpdCompUnitaDoc,
			aroUsoXsdDatiSpec, backendMetadata, updDatiSpecCompBlob);

		// in caso di errore esecuzione query per ricerva valori su attributo
		if (!tmpRispostaControlli.isrBoolean()) {
		    return tmpRispostaControlli;
		}

	    }
	}

	// MEV#29276
	if (backendMetadata.isObjectStorage() && !updDatiSpecBlob.containsKey(key)) {
	    updDatiSpecBlob.put(key, updDatiSpecCompBlob);
	}
	// end MEV#29276

	tmpRispostaControlli.setrBoolean(true);
	return tmpRispostaControlli;
    }

    //
    private void buildAroUpdDatiSpecUnitaDocFromUpd(SyncFakeSessn sessione,
	    AroUpdUnitaDoc tmpAroUpdUnitaDoc, AroUpdDocUnitaDoc tmpAroUpdDocUnitaDoc,
	    AroUpdCompUnitaDoc tmpAroUpdCompUnitaDoc, TiUsoXsdAroUpdDatiSpecUnitaDoc tiUsoXsd,
	    TiEntitaAroUpdDatiSpecUnitaDoc tiEntitaSacer,
	    JAXBElement<it.eng.parer.ws.xml.versUpdReq.DatiSpecificiType> datiSpec,
	    BackendStorage backendMetadata, Map<String, String> tmpUpdDatiSpecBlob,
	    long idRecXsdDatiSpec) throws JAXBException {

	buildAroUpdDatiSpecUnitaDocFromUpd(sessione, datiSpec, tmpAroUpdUnitaDoc,
		tmpAroUpdDocUnitaDoc, tmpAroUpdCompUnitaDoc, tiUsoXsd, tiEntitaSacer,
		backendMetadata, tmpUpdDatiSpecBlob, idRecXsdDatiSpec);
    }

    private RispostaControlli buildAroUpdDatiSpecUnitaDocFromAro(SyncFakeSessn sessione,
	    AroUpdUnitaDoc tmpAroUpdUnitaDoc, AroUpdDocUnitaDoc tmpAroUpdDocUnitaDoc,
	    AroUpdCompUnitaDoc tmpAroUpdCompUnitaDoc, AroUsoXsdDatiSpec aroUsoXsdDatiSpec,
	    BackendStorage backendMetadata, Map<String, String> tmpUpdDatiSpecBlob)
	    throws JAXBException, ParserConfigurationException {

	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	tmpRispostaControlli.setrBoolean(true); // default passa il controllo (eventuale errore su
						// generazione XML)

	AroUpdDatiSpecUnitaDoc tmpAroUpdDatiSpecUnitaDoc = new AroUpdDatiSpecUnitaDoc();
	// FK all'aggiornamento unità doc
	tmpAroUpdDatiSpecUnitaDoc.setAroUpdUnitaDoc(tmpAroUpdUnitaDoc);
	// FK all'aggiornamento doc unità doc
	tmpAroUpdDatiSpecUnitaDoc.setAroUpdDocUnitaDoc(tmpAroUpdDocUnitaDoc);
	// FK all'aggiornamento comp doc unità doc
	tmpAroUpdDatiSpecUnitaDoc.setAroUpdCompUnitaDoc(tmpAroUpdCompUnitaDoc);

	final LocalDate dtIniSes = convertLocal(sessione.getTmApertura());
	tmpAroUpdDatiSpecUnitaDoc.setDtIniSes(dtIniSes);
	// MEV#30089
	tmpAroUpdDatiSpecUnitaDoc.setAaIniSes(dtIniSes.getYear());
	tmpAroUpdDatiSpecUnitaDoc.setOrgStrut(tmpAroUpdUnitaDoc.getAroUnitaDoc().getOrgStrut());

	// * tipo di uso del xsd pari a VERS
	// need mapping between ENUM
	tmpAroUpdDatiSpecUnitaDoc.setTiUsoXsd((TiUsoXsdAroUpdDatiSpecUnitaDoc) UpdDocumentiUtils
		.convertEnumTiUsoXsd(true, aroUsoXsdDatiSpec.getTiUsoXsd()));
	// tipo entita sacer pari a UPD_UNI_DOC
	tmpAroUpdDatiSpecUnitaDoc
		.setTiEntitaSacer((TiEntitaAroUpdDatiSpecUnitaDoc) UpdDocumentiUtils
			.convertEnumTiEntita(true, aroUsoXsdDatiSpec.getTiEntitaSacer()));
	// FK alla versione XSD definito dall’unita doc da aggiornare
	tmpAroUpdDatiSpecUnitaDoc.setDecXsdDatiSpec(aroUsoXsdDatiSpec.getDecXsdDatiSpec());

	//
	tmpRispostaControlli = generaXmlDatiSpecFromAroUnitaDoc(
		aroUsoXsdDatiSpec.getDecXsdDatiSpec().getCdVersioneXsd(),
		aroUsoXsdDatiSpec.getTiUsoXsd(),
		aroUsoXsdDatiSpec.getDecXsdDatiSpec().getDecXsdAttribDatiSpecs(),
		aroUsoXsdDatiSpec.getIdUsoXsdDatiSpec(),
		tmpAroUpdUnitaDoc.getAroUnitaDoc().getIdOrgStrut());

	// in caso di errore esecuzione query per ricerva valori su attributo
	if (!tmpRispostaControlli.isrBoolean()) {
	    return tmpRispostaControlli;
	}

	// MEV#29276
	if (backendMetadata.isDataBase()) {
	    // Clob contenente il frammento XML contenuto nel tag “DatiSpecifici” del XML in
	    tmpAroUpdDatiSpecUnitaDoc.setBlXmlDatiSpec(tmpRispostaControlli.getrString());
	} else {
	    tmpUpdDatiSpecBlob.put(aroUsoXsdDatiSpec.getTiUsoXsd(),
		    tmpRispostaControlli.getrString());
	}
	// end MEV#29276

	// persist
	entityManager.persist(tmpAroUpdDatiSpecUnitaDoc);

	// add on list
	tmpAroUpdUnitaDoc.getAroUpdDatiSpecUnitaDocs().add(tmpAroUpdDatiSpecUnitaDoc);

	if (tmpAroUpdDocUnitaDoc != null) {
	    tmpAroUpdDocUnitaDoc.getAroUpdDatiSpecUnitaDocs().add(tmpAroUpdDatiSpecUnitaDoc);
	}

	return tmpRispostaControlli;
    }

    public RispostaControlli ereditaVrsSesUpdUnitaDocKoRisolte(AroUpdUnitaDoc tmpAroUpdUnitaDoc,
	    VrsUpdUnitaDocKo tmpUpdUnitaDocKo, MonKeyTotalUdKo tmpMonKeyTotalUdKo) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	tmpRispostaControlli.setrBoolean(false);

	try {
	    // logica su conteggio sessioni fallite/risolte da verificare
	    // lock conta
	    // aggiorno tutte le sessione fallite relative al KO
	    // ancora lockato: pongo a null la FK al KO, imposto la
	    // FK all'aggiornamento appena creato e pongo lo stato a RISOLTO
	    for (VrsSesUpdUnitaDocKo vsuko : tmpUpdUnitaDocKo.getVrsSesUpdUnitaDocKos()) {
		// count (1)
		if (vsuko.getTsIniSes().before(tmpAroUpdUnitaDoc.getTsIniSes())
			|| vsuko.getTsIniSes().equals(tmpAroUpdUnitaDoc.getTsIniSes())) {
		    tmpRispostaControlli = updLogSessioneHelper
			    .aggiornaConteggioMonContaSesUpdUdKo(vsuko, tmpMonKeyTotalUdKo, false);
		    if (!tmpRispostaControlli.isrBoolean()) {
			// errore su db
			return tmpRispostaControlli;
		    }
		}

		/*
		 * 2.3.3. modifica sessione fallita assegnando: FK ad aggiornamento unita doc ko
		 * nulla Fk ad aggiornamento unita doc inserito (record ARO_UPD_UNITA_DOC) Stato =
		 * RISOLTO
		 */
		vsuko.setAroUpdUnitaDoc(tmpAroUpdUnitaDoc);
		vsuko.setVrsUpdUnitaDocKo(null);
		vsuko.setTiStatoSesUpdKo(TiStatoSesUpdKo.RISOLTO);

		// aggiorna flag
		tmpAroUpdUnitaDoc.setFlSesUpdKoRisolti("1");

		// count (2)
		tmpRispostaControlli = updLogSessioneHelper.verificaDataAttivazioneJob();
		if (!tmpRispostaControlli.isrBoolean()) {
		    // errore su db
		    return tmpRispostaControlli;
		}
		/*
		 * 2.3.4. se data di inizio sessione fallita e’ minore dell’ultima data di
		 * attivazione del job “CALCOLO_CONTENUTO_SACER” (vedi vista LOG_V_VIS_LAST_SCHED a
		 * cui si accede per il nome del job)
		 */
		if (tmpRispostaControlli.getrLong() != -1
			&& LogSessioneUtils.getDatePart(vsuko.getTsIniSes()).before(
				LogSessioneUtils.getDatePart(tmpRispostaControlli.getrDate()))) {
		    tmpRispostaControlli = updLogSessioneHelper
			    .aggiornaConteggioMonContaSesUpdUdKo(vsuko, tmpMonKeyTotalUdKo, true);
		    if (!tmpRispostaControlli.isrBoolean()) {
			// errore su db
			return tmpRispostaControlli;
		    }
		}
	    }
	    entityManager.flush();
	    //
	    // rimuovo aggiornamento KO
	    entityManager.remove(tmpUpdUnitaDocKo);
	    entityManager.flush();
	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio dell'aggiornamento per ereditare le sessioni fallite: "
			    + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	    getLogger().error(
		    "Errore interno nella fase di salvataggio dell'aggiornamento per ereditare le sessioni fallite",
		    e);
	}

	return tmpRispostaControlli;
    }

    @SuppressWarnings("unchecked")
    public RispostaControlli scriviMonKeyTotalUd(AroUnitaDoc tmpAroUnitaDoc,
	    AroUpdUnitaDoc tmpAroUpdUnitaDoc, StrutturaUpdVers strutturaUpdVers) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	tmpRispostaControlli.setrBoolean(false);
	List<MonKeyTotalUd> mktuds;

	try {
	    String queryStr = "select ud " + "from MonKeyTotalUd ud "
		    + "where ud.aaKeyUnitaDoc = :aaKeyUnitaDoc "
		    + " and ud.orgSubStrut.idSubStrut = :idSubStrutIn "
		    + " and ud.decRegistroUnitaDoc.idRegistroUnitaDoc = :idRegistroUnitaDoc "
		    + " and ud.decTipoUnitaDoc.idTipoUnitaDoc = :idTipoUnitaDoc "
		    + " and ud.decTipoDocPrinc.idTipoDoc = :idTipoDoc ";
	    javax.persistence.Query query = entityManager.createQuery(queryStr,
		    MonKeyTotalUd.class);
	    query.setParameter("aaKeyUnitaDoc",
		    new BigDecimal(strutturaUpdVers.getChiaveNonVerificata().getAnno()));
	    query.setParameter("idSubStrutIn", tmpAroUnitaDoc.getOrgSubStrut().getIdSubStrut());
	    query.setParameter("idRegistroUnitaDoc", strutturaUpdVers.getIdRegistro());
	    query.setParameter("idTipoUnitaDoc",
		    strutturaUpdVers.getIdTipologiaUnitaDocumentaria());
	    query.setParameter("idTipoDoc", strutturaUpdVers.getIdTipoDocPrincipale());

	    mktuds = query.getResultList();
	    if (!mktuds.isEmpty()) {
		// TODO: probabilmente lock inutile dato che le condizioni non porteranno mai a
		// casi di concorrenza tra client nella stessa sottostruttura ....
		Map<String, Object> properties = new HashMap<>();
		properties.put(Constants.JAVAX_PERSISTENCE_LOCK_TIMEOUT, 25000);
		entityManager.find(MonKeyTotalUd.class, mktuds.get(0).getIdKeyTotalUd(),
			LockModeType.PESSIMISTIC_WRITE, properties);

		// update
		mktuds.get(0).setDtLastUpdUd(
			LogSessioneUtils.getDatePart(tmpAroUpdUnitaDoc.getTsIniSes()));
	    } else {
		MonKeyTotalUd monKeyTotalUd = new MonKeyTotalUd();
		monKeyTotalUd.setAaKeyUnitaDoc(
			new BigDecimal(strutturaUpdVers.getChiaveNonVerificata().getAnno()));
		monKeyTotalUd.setDecRegistroUnitaDoc(entityManager.find(DecRegistroUnitaDoc.class,
			strutturaUpdVers.getIdRegistro()));
		monKeyTotalUd.setDecTipoDocPrinc(entityManager.find(DecTipoDoc.class,
			strutturaUpdVers.getIdTipoDocPrincipale()));
		monKeyTotalUd.setDecTipoUnitaDoc(entityManager.find(DecTipoUnitaDoc.class,
			strutturaUpdVers.getIdTipologiaUnitaDocumentaria()));
		monKeyTotalUd.setOrgStrut(
			entityManager.find(OrgStrut.class, strutturaUpdVers.getIdStruttura()));
		monKeyTotalUd.setOrgSubStrut(tmpAroUnitaDoc.getOrgSubStrut());
		monKeyTotalUd.setDtLastUpdUd(tmpAroUpdUnitaDoc.getTsIniSes());

		entityManager.persist(monKeyTotalUd);
		tmpRispostaControlli.setrObject(monKeyTotalUd);
	    }

	    entityManager.flush();
	    //
	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio / aggiornamento conteggio chiave UD aggiornate totale: "
			    + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	    getLogger().error(
		    "Errore interno nella fase di salvataggio / aggiornamento conteggio chiave UD aggiornate totale",
		    e);
	}

	return tmpRispostaControlli;
    }

    @Override
    public Logger getLogger() {
	return LoggerFactory.getLogger(SalvataggioUpdVersamentoUpdHelper.class);
    }

}
