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

import it.eng.parer.entity.*;
import it.eng.parer.entity.builder.*;
import static it.eng.parer.util.DateUtilsConverter.convert;
import static it.eng.parer.util.FlagUtilsConverter.booleanToFlag;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.AroDoc;
import it.eng.parer.entity.AroUnitaDoc;
import it.eng.parer.entity.DecUsoModelloXsdUniDoc;
import it.eng.parer.entity.IamUser;
import it.eng.parer.entity.OrgStrut;
import it.eng.parer.entity.VrsDatiSessioneVers;
import it.eng.parer.entity.VrsDocNonVer;
import it.eng.parer.entity.VrsSessioneVers;
import it.eng.parer.entity.VrsUnitaDocNonVer;
import it.eng.parer.entity.VrsXmlDatiSessioneVers;
import it.eng.parer.entity.VrsXmlModelloSessioneVers;
import it.eng.parer.entity.constraint.VrsUrnXmlSessioneVers.TiUrnXmlSessioneVers;
import it.eng.parer.util.ejb.AppServerInstance;
import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.ejb.XmlVersCache;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.CostantiDB.TipiHash;
import it.eng.parer.ws.utils.CostantiDB.TipiXmlDati;
import it.eng.parer.ws.utils.HashCalculator;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.versamento.dto.AbsVersamentoExt;
import it.eng.parer.ws.versamento.dto.BackendStorage;
import it.eng.parer.ws.versamento.dto.DocumentoVers;
import it.eng.parer.ws.versamento.dto.FileBinario;
import it.eng.parer.ws.versamento.dto.IRispostaVersWS;
import it.eng.parer.ws.versamento.dto.ObjectStorageResource;
import it.eng.parer.ws.versamento.dto.RispostaWS;
import it.eng.parer.ws.versamento.dto.RispostaWSAggAll;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VersamentoExt;
import it.eng.parer.ws.versamento.dto.VersamentoExtAggAll;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.versamento.ejb.oracleBlb.WriteCompBlbOracle;
import it.eng.parer.ws.xml.versReq.ChiaveType;
import it.eng.parer.ws.xml.versReq.VersatoreType;
import it.eng.parer.ws.xml.versResp.ECEsitoGeneraleType;
import org.springframework.util.Assert;
import java.util.Optional;
import javax.persistence.Query;

/**
 *
 * @author Fioravanti_F
 */
@SuppressWarnings("unchecked")
@Stateless(mappedName = "LogSessioneSync")
@LocalBean
public class LogSessioneSync {

    private static final String ECCEZIONE_SALVATAGGIO_SESSIONE = "Eccezione nella persistenza dati di sessione richiesta";
    @EJB
    private XmlVersCache xmlVersCache;
    //
    @EJB
    private WriteCompBlbOracle writeCompBlbOracle;
    @EJB
    private ObjectStorageService objectStorageService;
    @EJB
    private ControlliSemantici controlliSemantici;
    @EJB
    private AppServerInstance appServerInstance;
    //
    private static final Logger log = LoggerFactory.getLogger(LogSessioneSync.class);
    //
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;
    //
    private static final int DS_ERR_MAX_LEN = 1024;
    private static final String TIPO_ERR_FATALE = "FATALE";
    private static final String TIPO_ERR_WARNING = "WARNING";
    private static final String CD_DS_ERR_DIVERSI = "Diversi";

    public RispostaControlli salvaSessioneVersamento(VersamentoExt versamento,
	    RispostaWS rispostaWS, SyncFakeSessn sessione) {
	boolean tmpReturn = true;
	StringWriter tmpStringWriter = new StringWriter();
	VersatoreType tmpVersatore = null;
	ChiaveType tmpChiave = null;
	RispostaControlli tmpControlli = new RispostaControlli();
	String tmpXmlEsito = null;

	if (versamento.getVersamento() != null) {
	    tmpVersatore = versamento.getVersamento().getIntestazione().getVersatore();
	    tmpChiave = versamento.getVersamento().getIntestazione().getChiave();
	}

	try {
	    Marshaller tmpMarshaller = xmlVersCache.getVersRespCtxforEsitoVersamento()
		    .createMarshaller();
	    tmpMarshaller.marshal(rispostaWS.getIstanzaEsito(), tmpStringWriter);

	    tmpXmlEsito = tmpStringWriter.toString();
	} catch (Exception ex) {
	    tmpControlli.setCodErr("ERR");
	    tmpControlli.setDsErr(
		    "Errore interno nella fase salvataggio sessione (marshalling esito versamento): "
			    + ExceptionUtils.getRootCause(ex));
	    log.error(
		    "Errore interno nella fase salvataggio sessione (marshalling esito versamento): ",
		    ex);
	    tmpReturn = false;
	}

	if (tmpReturn) {
	    try {
		String tmpVersioneXmlVers = rispostaWS.getIstanzaEsito().getVersioneXMLChiamata();
		String tmpVersioneXmlEsito = rispostaWS.getIstanzaEsito().getVersione();
		if (!salvaVrsSessioneVers(versamento, rispostaWS, sessione,
			rispostaWS.getIstanzaEsito().getEsitoGenerale(), tmpVersatore, tmpChiave,
			tmpXmlEsito, tmpVersioneXmlVers, tmpVersioneXmlEsito, null)) {
		    tmpControlli.setCodErr("ERR");
		    tmpControlli.setDsErr("Eccezione generica nella persistenza della sessione");
		}
	    } catch (Exception e) {
		tmpControlli.setCodErr("ERR");
		tmpControlli.setDsErr("Errore generico nella fase salvataggio sessione: "
			+ ExceptionUtils.getRootCause(e));
		log.error("Errore generico nella fase salvataggio sessione: ", e);
	    } finally {
		entityManager.flush();
		entityManager.clear();
	    }
	}

	return tmpControlli;
    }

    public RispostaControlli salvaSessioneVersamento(VersamentoExtAggAll versamento,
	    RispostaWSAggAll rispostaWS, SyncFakeSessn sessione) {
	boolean tmpReturn = true;
	StringWriter tmpStringWriter = new StringWriter();
	VersatoreType tmpVersatore = null;
	ChiaveType tmpChiave = null;
	RispostaControlli tmpControlli = new RispostaControlli();
	String tmpXmlEsito = null;
	DocumentoVers tmpDocumentoVers = null;

	if (versamento.getVersamento() != null) {
	    tmpVersatore = versamento.getVersamento().getIntestazione().getVersatore();
	    tmpChiave = versamento.getVersamento().getIntestazione().getChiave();
	}

	/*
	 * se è definito documentoVers, vuol dire che sto salvando i dati di aggiunta documento e
	 * che il parser ha ricostruito abbastanza informazioni da recuperare i metadati del doc da
	 * versare...
	 */
	if (versamento.getStrutturaComponenti() != null
		&& !versamento.getStrutturaComponenti().getDocumentiAttesi().isEmpty()) {
	    tmpDocumentoVers = versamento.getStrutturaComponenti().getDocumentiAttesi().get(0);
	}

	try {
	    Marshaller tmpMarshaller = xmlVersCache.getVersRespCtxforEsitoVersamentoAggAllegati()
		    .createMarshaller();
	    tmpMarshaller.marshal(rispostaWS.getIstanzaEsito(), tmpStringWriter);
	    tmpXmlEsito = tmpStringWriter.toString();
	} catch (Exception ex) {
	    tmpControlli.setCodErr("ERR");
	    tmpControlli.setDsErr(
		    "Errore interno nella fase salvataggio sessione (marshalling esito versamento aggiunta documento): "
			    + ex.getMessage());
	    log.error(
		    "Errore interno nella fase salvataggio sessione (marshalling esito versamento aggiunta documento): ",
		    ex);
	    tmpReturn = false;
	}

	if (tmpReturn) {
	    try {
		String tmpVersioneXmlVers = rispostaWS.getIstanzaEsito().getVersioneXMLChiamata();
		String tmpVersioneXmlEsito = rispostaWS.getIstanzaEsito().getVersione();
		if (!salvaVrsSessioneVers(versamento, rispostaWS, sessione,
			rispostaWS.getIstanzaEsito().getEsitoGenerale(), tmpVersatore, tmpChiave,
			tmpXmlEsito, tmpVersioneXmlVers, tmpVersioneXmlEsito, tmpDocumentoVers)) {
		    tmpControlli.setCodErr("ERR");
		    tmpControlli.setDsErr("Eccezione generica nella persistenza della sessione ");
		}
	    } catch (Exception e) {
		tmpControlli.setCodErr("ERR");
		tmpControlli.setDsErr("Errore generico interno nella fase salvataggio sessione: "
			+ ExceptionUtils.getRootCauseMessage(e));
		log.error("Errore generico interno nella fase salvataggio sessione: ", e);
	    } finally {
		entityManager.flush();
		entityManager.clear();
	    }
	}

	return tmpControlli;
    }

    private boolean salvaVrsSessioneVers(AbsVersamentoExt versamento, IRispostaVersWS rispostaWS,
	    SyncFakeSessn sessione, ECEsitoGeneraleType esitoGen, VersatoreType versatore,
	    ChiaveType chiave, String xmlEsito, String versioneXmlChiamata, String versioneXmlEsito,
	    DocumentoVers documentoVersIn) throws NoSuchAlgorithmException, IOException {
	boolean tmpReturn = true;
	Map<String, String> sipBlob = new HashMap<>();
	Optional<VrsSessioneVersKo> vrsSessioneVersKo = Optional.empty();
	Optional<VrsSessioneVers> vrsSessioneVers = Optional.empty();

	// calcola l'hash dell'esito del versamento.
	// lo calcolo in questo punto perchè non può più cambiare e in caso di errori da
	// qui in poi
	// non verrebbe salvato nulla.
	String hashXmlEsito = new HashCalculator().calculateHashSHAX(xmlEsito, TipiHash.SHA_256)
		.toHexBinary();

	/*
	 * salvo sessione
	 */
	// questi dati li scrivo sempre
	VrsSessioneVersBuilder vrsSessioneVersBuilder = VrsSessioneVersBuilder.builder()
		.cdVersioneWs(sessione.getVersioneWS())
		.dtApertura(convert(sessione.getTmApertura()))
		.dtChiusura(convert(sessione.getTmChiusura()))
		.tsApertura(convert(sessione.getTmApertura()))
		.tsChiusura(convert(sessione.getTmChiusura()))
		.tiSessioneVers(sessione.getTipoSessioneVers().name()) // VERSAMENTO
								       // o
								       // AGGIUNTA
		.nmUseridWs(sessione.getLoginName())
		// salvo il nome del server/istanza nel cluster che sta salvando i dati e ha
		// gestito il versamento
		.cdIndServer(appServerInstance.getName())
		// salvo l'indirizzo IP del sistema che ha effettuato la richiesta di
		// versamento/aggiunta
		.cdIndIpClient(sessione.getIpChiamante());
	// questi dati li scrivo se l'XML ha superato il controllo formale e quindi sono
	// definiti i tag Versatore e
	// Chiave
	if (versatore != null && chiave != null) {
	    vrsSessioneVersBuilder.aaKeyUnitaDoc(new BigDecimal(chiave.getAnno()))
		    .cdKeyUnitaDoc(chiave.getNumero())
		    .cdRegistroKeyUnitaDoc(chiave.getTipoRegistro())
		    .nmAmbiente(versatore.getAmbiente()).nmEnte(versatore.getEnte())
		    .nmStrut(versatore.getStruttura()).nmUserid(versatore.getUserID()) // questo
										       // è
										       // l'utente
										       // definito
										       // nell'xml
		    .nmUtente(versatore.getUtente()); // questo è l'utente definito nell'xml
	}

	/*
	 * se è definito documentoVers, vuol dire che sto salvando i dati di aggiunta documento e
	 * che il parser ha ricostruito abbastanza informazioni da recuperare i metadati del doc da
	 * versare...
	 */
	if (documentoVersIn != null) {
	    vrsSessioneVersBuilder.cdKeyDocVers(documentoVersIn.getRifDocumento().getIDDocumento());
	    /*
	     * ...inoltre se è definito l'id del record versato e non ci sono errori precedenti,
	     * vuol dire che sono riuscito ad effettuare il versamento e che devo salvare il
	     * riferimento al doc versato nella sessione
	     */
	    if (isOkOrWarningResponse(rispostaWS)) {
		vrsSessioneVersBuilder.aroDoc(
			entityManager.find(AroDoc.class, documentoVersIn.getIdRecDocumentoDB()));
	    }
	}

	// se ho trovato il codice dell'utente definito nell'XML, lo scrivo
	if (versamento.getStrutturaComponenti() != null
		&& versamento.getStrutturaComponenti().getIdUser() != 0) {
	    vrsSessioneVersBuilder.iamUser(entityManager.find(IamUser.class,
		    versamento.getStrutturaComponenti().getIdUser()));
	}

	// se ho trovato il codice della struttura versante, lo scrivo
	if (versamento.getStrutturaComponenti() != null
		&& versamento.getStrutturaComponenti().getIdStruttura() != 0) {
	    vrsSessioneVersBuilder.orgStrut(entityManager.find(OrgStrut.class,
		    versamento.getStrutturaComponenti().getIdStruttura()));
	}

	// se c'è un ID per l'unità documentaria creata o aggiornata, lo scrivo
	if (versamento.getStrutturaComponenti() != null
		&& versamento.getStrutturaComponenti().getIdUnitaDoc() != 0) {
	    vrsSessioneVersBuilder.aroUnitaDoc(entityManager.find(AroUnitaDoc.class,
		    versamento.getStrutturaComponenti().getIdUnitaDoc()));
	}

	try {
	    if (isOkOrWarningResponse(rispostaWS)) {
		// CHIUSA_OK
		vrsSessioneVers = Optional.of(vrsSessioneVersBuilder.buildVrsSessioneVers());
		entityManager.persist(vrsSessioneVers.get());
	    } else {
		// CHIUSA_ERR
		vrsSessioneVersBuilder.flSessioneErrVerif("0"); // se è andata in errore, pongo a
								// FALSE il flag di
								// sessione
		// verificata dall'operatore
		// integro i dati relativi all'errore principale
		String tmpErrMess;
		if (esitoGen.getMessaggioErrore().isEmpty()) {
		    tmpErrMess = "(vuoto)";
		} else {
		    tmpErrMess = esitoGen.getMessaggioErrore();
		    if (tmpErrMess.length() > DS_ERR_MAX_LEN) {
			tmpErrMess = tmpErrMess.substring(0, DS_ERR_MAX_LEN);
		    }
		}
		vrsSessioneVersBuilder.dsErrPrinc(tmpErrMess)
			.cdErrPrinc(esitoGen.getCodiceErrore());
		vrsSessioneVersKo = Optional.of(vrsSessioneVersBuilder.buildVrsSessioneVersKo());
		entityManager.persist(vrsSessioneVersKo.get());
	    }
	} catch (RuntimeException re) {
	    log.error("Eccezione in fase di salvataggio della sessione di versamento", re);
	    tmpReturn = false;
	}

	/*
	 * IN CASO DI ERRORE Salvo i dati relativi all'Unità documentaria o al documento non
	 * versato, nel caso il versamento sia andato male, ma siano comunque identificabili gli
	 * estremi di ciò che si voleva scrivere. In caso di errore per elemento duplicato, non deve
	 * essere scritto nulla.
	 */
	if (tmpReturn && isErrorResponse(rispostaWS) && !rispostaWS.isErroreElementoDoppio()
		&& versamento.getStrutturaComponenti() != null
		&& versamento.getStrutturaComponenti().getIdStruttura() != 0) {

	    CSChiave tmpCSChiave = new CSChiave();
	    tmpCSChiave.setAnno(Long.valueOf(chiave.getAnno()));
	    tmpCSChiave.setNumero(chiave.getNumero());
	    tmpCSChiave.setTipoRegistro(chiave.getTipoRegistro());

	    if (sessione.getTipoSessioneVers() == SyncFakeSessn.TipiSessioneVersamento.VERSAMENTO
		    && this.udNonVersataNonPresente(tmpCSChiave,
			    versamento.getStrutturaComponenti().getIdStruttura())) {
		List<VrsUnitaDocNonVer> udnvs = getVrsUnitaDocNonVers(
			versamento.getStrutturaComponenti().getIdStruttura(),
			tmpCSChiave.getTipoRegistro(), new BigDecimal(tmpCSChiave.getAnno()),
			tmpCSChiave.getNumero());
		try {
		    if (udnvs.isEmpty()) {
			VrsUnitaDocNonVer tmpUnitaDocNonVer = new VrsUnitaDocNonVer();
			tmpUnitaDocNonVer.setAaKeyUnitaDoc(new BigDecimal(tmpCSChiave.getAnno()));
			tmpUnitaDocNonVer.setCdKeyUnitaDoc(tmpCSChiave.getNumero());
			tmpUnitaDocNonVer.setCdRegistroKeyUnitaDoc(tmpCSChiave.getTipoRegistro());
			tmpUnitaDocNonVer.setDtFirstSesErr(convert(sessione.getTmApertura()));
			tmpUnitaDocNonVer.setDtLastSesErr(convert(sessione.getTmApertura()));
			tmpUnitaDocNonVer.setDsErrPrinc(esitoGen.getMessaggioErrore());
			tmpUnitaDocNonVer.setCdErrPrinc(esitoGen.getCodiceErrore());
			tmpUnitaDocNonVer.setOrgStrut(entityManager.find(OrgStrut.class,
				versamento.getStrutturaComponenti().getIdStruttura()));
			entityManager.persist(tmpUnitaDocNonVer);
		    } else {
			VrsUnitaDocNonVer tmpUnitaDocNonVer = udnvs.get(0);
			tmpUnitaDocNonVer.setDtLastSesErr(convert(sessione.getTmApertura()));
			if (!tmpUnitaDocNonVer.getDsErrPrinc()
				.equals(esitoGen.getMessaggioErrore())) {
			    tmpUnitaDocNonVer.setDsErrPrinc(CD_DS_ERR_DIVERSI);
			}
			if (!tmpUnitaDocNonVer.getCdErrPrinc().equals(esitoGen.getCodiceErrore())) {
			    tmpUnitaDocNonVer.setCdErrPrinc(CD_DS_ERR_DIVERSI);
			}
		    }
		} catch (RuntimeException re) {
		    log.error("Eccezione nella persistenza dell'unità documentaria NON versata ",
			    re);
		    tmpReturn = false;
		}
	    } else if (SyncFakeSessn.TipiSessioneVersamento.AGGIUNGI_DOCUMENTO
		    .equals(sessione.getTipoSessioneVers())
		    && documentoVersIn != null
		    && this.docNonVersatoNonPresente(tmpCSChiave,
			    versamento.getStrutturaComponenti().getIdStruttura(),
			    documentoVersIn.getRifDocumento().getIDDocumento())) {
		List<VrsDocNonVer> udnvs = getVrsDocNonVers(
			versamento.getStrutturaComponenti().getIdStruttura(),
			tmpCSChiave.getTipoRegistro(), new BigDecimal(tmpCSChiave.getAnno()),
			tmpCSChiave.getNumero(),
			documentoVersIn.getRifDocumento().getIDDocumento());
		try {
		    if (udnvs.isEmpty()) {
			VrsDocNonVer tmpDocNonVer = new VrsDocNonVer();
			tmpDocNonVer.setAaKeyUnitaDoc(new BigDecimal(tmpCSChiave.getAnno()));
			tmpDocNonVer.setCdKeyUnitaDoc(tmpCSChiave.getNumero());
			tmpDocNonVer.setCdRegistroKeyUnitaDoc(tmpCSChiave.getTipoRegistro());
			tmpDocNonVer.setCdKeyDocVers(
				documentoVersIn.getRifDocumento().getIDDocumento());
			tmpDocNonVer.setDtFirstSesErr(convert(sessione.getTmApertura()));
			tmpDocNonVer.setDtLastSesErr(convert(sessione.getTmApertura()));
			tmpDocNonVer.setDsErrPrinc(esitoGen.getMessaggioErrore());
			tmpDocNonVer.setCdErrPrinc(esitoGen.getCodiceErrore());
			tmpDocNonVer.setOrgStrut(entityManager.find(OrgStrut.class,
				versamento.getStrutturaComponenti().getIdStruttura()));
			entityManager.persist(tmpDocNonVer);
		    } else {
			VrsDocNonVer tmpDocNonVer = udnvs.get(0);
			tmpDocNonVer.setDtLastSesErr(convert(sessione.getTmApertura()));
			if (!tmpDocNonVer.getDsErrPrinc().equals(esitoGen.getMessaggioErrore())) {
			    tmpDocNonVer.setDsErrPrinc(CD_DS_ERR_DIVERSI);
			}
			if (!tmpDocNonVer.getCdErrPrinc().equals(esitoGen.getCodiceErrore())) {
			    tmpDocNonVer.setCdErrPrinc(CD_DS_ERR_DIVERSI);
			}
		    }
		} catch (RuntimeException re) {
		    log.error("Eccezione nella persistenza del documento NON versato ", re);
		    tmpReturn = false;
		}
	    }
	} // fine gestione documenti non versati / non aggiunti

	/*
	 * salva i dati di sessione
	 */
	Optional<VrsDatiSessioneVers> vrsDatiSessioneVers = Optional.empty();
	Optional<VrsDatiSessioneVersKo> vrsDatiSessioneVersKo = Optional.empty();
	if (tmpReturn) {
	    VrsDatiSessioneVersBuilder vrsDatiSessioneVersBuilder = VrsDatiSessioneVersBuilder
		    .builder().vrsSessioneVers(vrsSessioneVers).vrsSessioneVersKo(vrsSessioneVersKo)
		    .tiDatiSessioneVers(sessione.getTipoDatiSessioneVers())
		    .pgDatiSessioneVers(BigDecimal.ONE)
		    /*
		     * scrivo il numero di file arrivati in ogni caso, non è necessaria la
		     * compatibilità con la versione asincrona dal momento che il versamento
		     * asincrono è gestito con un applicativo esterno. Inoltre è stato espressamente
		     * richiesto che i file delle sessioni errate siano sempre salvati
		     */
		    .niFile(new BigDecimal(sessione.getFileBinari().size()));

	    // se ho trovato il codice della struttura versante, lo scrivo
	    if (versamento.getStrutturaComponenti() != null
		    && versamento.getStrutturaComponenti().getIdStruttura() != 0) {
		vrsDatiSessioneVersBuilder.idStrut(
			new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
	    }

	    try {
		if (isOkOrWarningResponse(rispostaWS)) {
		    vrsDatiSessioneVers = Optional
			    .of(vrsDatiSessioneVersBuilder.buildVrsDatiSessioneVers());
		    entityManager.persist(vrsDatiSessioneVers.get());
		} else {
		    vrsDatiSessioneVersKo = Optional
			    .of(vrsDatiSessioneVersBuilder.buildVrsDatiSessioneVersKo());
		    entityManager.persist(vrsDatiSessioneVersKo.get());
		}
	    } catch (RuntimeException re) {
		log.error("Eccezione nella persistenza di VrsDatiSessioneVers*", re);
		tmpReturn = false;
	    }
	}

	/*
	 * BACKEND_SIP: se OS effettua salvataggio su bucket (TipiXmlDati.RICHIESTA +
	 * TipiXmlDati.RISPOSTA + TipiXmlDati.INDICE_FILE + TipiXmlDati.RAPP_VERS)
	 *
	 * Nota: in caso di sessione fallita tutti i dati utilizzeranno il backend di tipo "staging"
	 */
	BackendStorage backendMetadata = null;
	if (tmpReturn) {
	    /* sessione OK */
	    if (isOkOrWarningResponse(rispostaWS)) {
		backendMetadata = objectStorageService.lookupBackendByServiceName(
			versamento.getStrutturaComponenti().getIdTipologiaUnitaDocumentaria(),
			versamento.getDescrizione().getNomeWs());
	    } else /* sessione KO */ {
		backendMetadata = objectStorageService.lookupBackendVrsStaging();
	    }
	}

	/*
	 * salva i dati xml di versamento
	 */
	if (tmpReturn) {
	    final String blobRichiesta = sessione.getDatiDaSalvareIndiceSip().length() == 0 ? "--"
		    : sessione.getDatiDaSalvareIndiceSip();
	    sipBlob.put(TipiXmlDati.RICHIESTA, blobRichiesta);
	    tmpReturn = salvaXmlRichiesta(versamento, sessione, versioneXmlChiamata,
		    documentoVersIn, backendMetadata, vrsDatiSessioneVers, vrsDatiSessioneVersKo,
		    rispostaWS);
	}

	/*
	 * salva i dati xml di esito
	 */
	if (tmpReturn) {
	    final String xmlRisposta = xmlEsito.length() == 0 ? "--" : xmlEsito;
	    sipBlob.put(TipiXmlDati.RISPOSTA, xmlRisposta);
	    tmpReturn = salvaXmlRisposta(versamento, sessione, hashXmlEsito, versioneXmlEsito,
		    xmlEsito, documentoVersIn, backendMetadata, vrsDatiSessioneVers,
		    vrsDatiSessioneVersKo, rispostaWS);
	}

	/*
	 * se sono presenti, salva i dati xml dell'indice MM (solo per VersamentoMM)
	 */
	if (tmpReturn && sessione.getDatiPackInfoSipXml() != null) {
	    final String xmlIndice = sessione.getDatiPackInfoSipXml().length() == 0 ? "--"
		    : sessione.getDatiPackInfoSipXml();
	    sipBlob.put(TipiXmlDati.INDICE_FILE, xmlIndice);
	    tmpReturn = salvaXmlIndiceMMVersamento(versamento, sessione, versioneXmlChiamata,
		    documentoVersIn, backendMetadata, vrsDatiSessioneVers, vrsDatiSessioneVersKo,
		    rispostaWS);
	}

	/*
	 * se sono presenti, salva i dati xml del Rapporto di versamento
	 */
	// questi dati li scrivo se il WS è andato, nel complesso, bene.
	if (isOkOrWarningResponse(rispostaWS) && tmpReturn
		&& sessione.getDatiRapportoVersamento() != null) {
	    final String xmlRappVers = sessione.getDatiRapportoVersamento().length() == 0 ? "--"
		    : sessione.getDatiRapportoVersamento();
	    sipBlob.put(TipiXmlDati.RAPP_VERS, xmlRappVers);
	    tmpReturn = salvaXmlRapportoVersamento(versamento, sessione, documentoVersIn,
		    backendMetadata, vrsDatiSessioneVers, vrsDatiSessioneVersKo, rispostaWS);
	}

	/*
	 * Se backendMetadata di tipo O.S. si effettua il salvataggio (con link su appasita entity)
	 */
	if (tmpReturn && backendMetadata.isObjectStorage()) {
	    ObjectStorageResource res = null;
	    if (isOkOrWarningResponse(rispostaWS)) /* sessione OK */ {
		if (documentoVersIn != null) {
		    res = objectStorageService.createResourcesInSipDocumento(
			    backendMetadata.getBackendName(), sipBlob,
			    versamento.getStrutturaComponenti(),
			    documentoVersIn.getIdRecDocumentoDB());
		} else {
		    res = objectStorageService.createResourcesInSipUnitaDoc(
			    backendMetadata.getBackendName(), sipBlob,
			    versamento.getStrutturaComponenti());
		}
	    } else /* sessione KO */ {
		res = objectStorageService.createSipInStaging(backendMetadata.getBackendName(),
			sipBlob, vrsDatiSessioneVersKo.get().getIdDatiSessioneVersKo(),
			getIdStrut(versamento));
	    }
	    log.debug("Salvati i SIP nel bucket {} con chiave {} ", res.getBucket(), res.getKey());
	}

	/*
	 * se sono presenti, salva i dati xml dei Profili UD/Comp
	 */
	if (isOkOrWarningResponse(rispostaWS) && versamento.getStrutturaComponenti() != null
		&& versamento.getStrutturaComponenti().hasXsdProfile()) {
	    Assert.isTrue(vrsDatiSessioneVers.isPresent(),
		    "VrsDatiSessioneVers deve essere presente in questo caso perché la sessione è andata a buon fine");
	    // se presente il profilo normativo su unità documentaria
	    if (versamento.getStrutturaComponenti().getIdRecUsoXsdProfiloNormativo() != null) {
		tmpReturn = salvaXmlModelloProfiloNormativoUniDoc(
			versamento.getStrutturaComponenti().getIdStruttura(),
			versamento.getStrutturaComponenti().getIdRecUsoXsdProfiloNormativo()
				.longValue(),
			versamento.getStrutturaComponenti().getDatiC14NProfNormXml(),
			vrsDatiSessioneVers.get());
	    }
	    /*
	     * Possibili future estensioni per nuovi profili su unidoc.....
	     */
	}
	/*
	 * salva i dati relativi agli errori
	 */
	if (tmpReturn && !isOkResponse(rispostaWS)) {
	    // CHIUSA_OK con warning o CHIUSA_ERR
	    int progErrore = 1;
	    String tmpErrMess;
	    VrsErrSessioneVersBuilder vrsErrSessioneVersBuilder = VrsErrSessioneVersBuilder
		    .builder();
	    for (VoceDiErrore tmpVoceDiErrore : versamento.getErroriTrovati()) {
		vrsErrSessioneVersBuilder.vrsDatiSessioneVersKo(vrsDatiSessioneVersKo.orElse(null));
		vrsErrSessioneVersBuilder.vrsDatiSessioneVers(vrsDatiSessioneVers.orElse(null));
		// se ho trovato il codice della struttura versante, lo scrivo
		if (versamento.getStrutturaComponenti() != null
			&& versamento.getStrutturaComponenti().getIdStruttura() != 0) {
		    vrsErrSessioneVersBuilder.idStrut(
			    new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
		}

		if (tmpVoceDiErrore.getErrorMessage().isEmpty()) {
		    tmpErrMess = "(vuoto)";
		} else {
		    tmpErrMess = tmpVoceDiErrore.getErrorMessage();
		    if (tmpErrMess.length() > DS_ERR_MAX_LEN) {
			tmpErrMess = tmpErrMess.substring(0, DS_ERR_MAX_LEN);
		    }
		}
		vrsErrSessioneVersBuilder.dsErr(tmpErrMess).cdErr(tmpVoceDiErrore.getErrorCode())
			.flErrPrinc(booleanToFlag(tmpVoceDiErrore.isElementoPrincipale()))
			.pgErrSessioneVers(new BigDecimal(progErrore));
		progErrore++;
		try {
		    if (isErrorResponse(rispostaWS)) {
			vrsErrSessioneVersBuilder.tiErr(TIPO_ERR_FATALE);
			entityManager
				.persist(vrsErrSessioneVersBuilder.buildVrsErrSessioneVersKo());
		    } else {
			vrsErrSessioneVersBuilder.tiErr(TIPO_ERR_WARNING);
			entityManager.persist(vrsErrSessioneVersBuilder.buildVrsErrSessioneVers());
		    }
		} catch (RuntimeException re) {
		    /// logga l'errore e blocca tutto
		    log.error("Eccezione nella persistenza degli errori su sessione ", re);
		    tmpReturn = false;
		    break;
		}
	    }
	}

	/*
	 * se sono ancora in grado di scrivere, salvo i file. --- --- --- --- --- --- --- --- ---
	 * --- --- --- --- --- --- --- --- --- NOTA: il salvataggio dei file andati in errore
	 * avviene solo nel caso di persistenza degli stessi su BLOB oppure il tipo salvataggio non
	 * sia stato determinato. Nel caso i file siano da versare sul filesystem (e successivamente
	 * su nastro, via Tivoli), i file vengono perduti. Vogliamo evitare di salvare su blob file
	 * potenzialmente enormi. MEV29384 - Scrivo i file solo per i versamenti CHIUSA_ERR perché
	 * per quelli andati a buon fine i file sono già salvati su DB
	 */
	long contaFileScritti = 0;
	if (tmpReturn && isErrorResponse(rispostaWS) && (versamento.getStrutturaComponenti() == null
		|| (versamento.getStrutturaComponenti() != null
			&& versamento.getStrutturaComponenti()
				.getTipoSalvataggioFile() != CostantiDB.TipoSalvataggioFile.FILE)))

	{
	    Assert.isTrue(vrsDatiSessioneVersKo.isPresent(),
		    "VrsFileSessioneKo deve essere presente per sessionie errate/fallite");
	    long progressivoFile = 0;
	    for (FileBinario tmpFb : sessione.getFileBinari()) {
		progressivoFile++;
		VrsFileSessioneKo vrsFileSessioneKo = new VrsFileSessioneKo();
		vrsFileSessioneKo.setVrsDatiSessioneVersKo(vrsDatiSessioneVersKo.get());
		if (versamento.getStrutturaComponenti() != null
			&& versamento.getStrutturaComponenti().getIdStruttura() != 0) {
		    vrsFileSessioneKo.setIdStrut(
			    new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
		}
		vrsFileSessioneKo.setNmFileSessione(tmpFb.getId());
		vrsFileSessioneKo.setPgFileSessione(new BigDecimal(progressivoFile));
		vrsFileSessioneKo.setTiStatoFileSessione("CONTR_FATTI"); // nota, questo campo è del
									 // tutto inutile.
		try {
		    if (tmpReturn) {
			entityManager.persist(vrsFileSessioneKo);
		    }
		} catch (RuntimeException re) {
		    /// logga l'errore e blocca tutto
		    log.error("Eccezione nella persistenza dei file di sessione ", re);
		    tmpReturn = false;
		}

		/*
		 * i file li memorizzo in questa tabella, ma solo se si è verificato un errore
		 * diverso da "l'elemento corrisponde ad uno già presente nel sistema". dal momento
		 * che non ci interessa tenere due copie di un file che abbiamo già
		 */
		if (tmpReturn && isErrorResponse(rispostaWS)
			&& (!rispostaWS.isErroreElementoDoppio())) {
		    // procedo alla memorizzazione del file sul blob, via JDBC
		    WriteCompBlbOracle.DatiAccessori datiAccessori = new WriteCompBlbOracle().new DatiAccessori();
		    datiAccessori
			    .setTabellaBlob(WriteCompBlbOracle.TabellaBlob.VRS_CONTENUTO_FILE_KO);
		    datiAccessori.setIdPadre(vrsFileSessioneKo.getIdFileSessioneKo());
		    if (versamento.getStrutturaComponenti() != null
			    && versamento.getStrutturaComponenti().getIdStruttura() != 0) {
			datiAccessori.setIdStruttura(
				versamento.getStrutturaComponenti().getIdStruttura());
		    }
		    datiAccessori.setDtVersamento(sessione.getTmApertura());

		    try {

			BackendStorage backendStaging = objectStorageService
				.lookupBackendVrsStaging();
			RispostaControlli tmpControlli = null;
			if (backendStaging.isDataBase()) {
			    tmpControlli = writeCompBlbOracle.salvaStreamSuBlobComp(datiAccessori,
				    tmpFb);
			} else {
			    // copy componente tmp and create new one in staging
			    ObjectStorageResource osResource = objectStorageService
				    .copyTmpResourceToCompUdInStaging(
					    backendStaging.getBackendName(),
					    tmpFb.getObjectStorageResource(),
					    vrsFileSessioneKo.getIdFileSessioneKo(),
					    getIdStrut(versamento));
			    tmpFb.setObjectStorageResource(osResource);
			    // ho già detto che odio l'oggetto RispostaControlli?
			    tmpControlli = new RispostaControlli();
			    tmpControlli.setrBoolean(true);
			}

			if (tmpControlli.isrBoolean()) {
			    contaFileScritti++;
			} else {
			    tmpReturn = false;
			}

		    } catch (Exception re) {
			/// logga l'errore e blocca tutto
			log.error("Eccezione nella persistenza del blob ", re);
			tmpReturn = false;
		    }
		}
	    }
	}

	// aggiorno nella tabella principale della Sessione di Versamento
	// il numero effettivo di file/blob salvati sul DB
	if (tmpReturn && isErrorResponse(rispostaWS)) {
	    Assert.isTrue(vrsSessioneVersKo.isPresent(),
		    "VrsSessioneVersKo deve essere presente per le sessioni errate/fallite");
	    vrsSessioneVersKo.get().setNiFileErr(new BigDecimal(contaFileScritti));
	}

	return tmpReturn;
    }

    private BigDecimal getIdStrut(AbsVersamentoExt versamento) {
	if (versamento.getStrutturaComponenti() != null
		&& versamento.getStrutturaComponenti().getIdStruttura() != 0) {
	    return BigDecimal.valueOf(versamento.getStrutturaComponenti().getIdStruttura());
	}
	return null;
    }

    private static boolean isErrorResponse(IRispostaVersWS rispostaWS) {
	return IRispostaWS.SeverityEnum.ERROR.equals(rispostaWS.getSeverity());
    }

    private static boolean isOkOrWarningResponse(IRispostaVersWS rispostaWS) {
	return !isErrorResponse(rispostaWS);
    }

    private static boolean isOkResponse(IRispostaVersWS rispostaWS) {
	return IRispostaWS.SeverityEnum.OK.equals(rispostaWS.getSeverity());
    }

    // salva i dati xml di versamento
    private boolean salvaXmlRichiesta(AbsVersamentoExt versamento, SyncFakeSessn sessione,
	    String versioneXmlChiamata, DocumentoVers documentoVersIn,
	    BackendStorage backendMetadata, Optional<VrsDatiSessioneVers> vrsDatiSessioneVers,
	    Optional<VrsDatiSessioneVersKo> vrsDatiSessioneVersKo, IRispostaVersWS rispostaWS) {
	boolean tmpReturn = true;
	VrsXmlDatiSessioneVersBuilder vrsXmlDatiSessioneVersBuilder = VrsXmlDatiSessioneVersBuilder
		.builder().vrsDatiSessioneVers(vrsDatiSessioneVers)
		.vrsDatiSessioneVersKo(vrsDatiSessioneVersKo).tiXmlDati(TipiXmlDati.RICHIESTA);
	// se ho trovato il codice della struttura versante, lo scrivo
	if (versamento.getStrutturaComponenti() != null
		&& versamento.getStrutturaComponenti().getIdStruttura() != 0) {
	    vrsXmlDatiSessioneVersBuilder
		    .idStrut(new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
	}
	vrsXmlDatiSessioneVersBuilder.flCanonicalized(CostantiDB.Flag.TRUE);
	if (backendMetadata.isDataBase()) {
	    vrsXmlDatiSessioneVersBuilder
		    .blXml(sessione.getDatiDaSalvareIndiceSip().length() == 0 ? "--"
			    : sessione.getDatiDaSalvareIndiceSip());
	}

	vrsXmlDatiSessioneVersBuilder.cdVersioneXml(versioneXmlChiamata);
	if (sessione.getUrnIndiceSipXml() != null) {
	    vrsXmlDatiSessioneVersBuilder.dsUrnXmlVers(sessione.getUrnIndiceSipXml())
		    .dsHashXmlVers(sessione.getHashIndiceSipXml())
		    .cdEncodingHashXmlVers(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi())
		    .dsAlgoHashXmlVers(CostantiDB.TipiHash.SHA_256.descrivi());
	}
	Optional<VrsXmlDatiSessioneVers> vrsXmlDatiSessioneVers = Optional.empty();
	Optional<VrsXmlDatiSessioneVersKo> vrsXmlDatiSessioneVersKo = Optional.empty();
	try {
	    if (isOkOrWarningResponse(rispostaWS)) {
		vrsXmlDatiSessioneVers = Optional
			.of(vrsXmlDatiSessioneVersBuilder.buildVrsXmlDatiSessioneVers());
		entityManager.persist(vrsXmlDatiSessioneVers.get());
	    } else {
		vrsXmlDatiSessioneVersKo = Optional
			.of(vrsXmlDatiSessioneVersBuilder.buildVrsXmlDatiSessioneVersKo());
		entityManager.persist(vrsXmlDatiSessioneVersKo.get());
	    }
	} catch (RuntimeException re) {
	    log.error(ECCEZIONE_SALVATAGGIO_SESSIONE, re);
	    tmpReturn = false;
	}

	/*
	 * salva i dati dei nuovi URN su VrsUrnXmlSessioneVers
	 */
	if (tmpReturn) {
	    final URNVersamento urns = calcolaUrn(versamento, documentoVersIn, sessione);
	    if (urns.getUrn() != null) {
		// salvo ORIGINALE
		tmpReturn = this.salvaVrsUrnXmlSessioneVers(
			MessaggiWSFormat.formattaUrnIndiceSip(urns.getUrn(),
				Costanti.UrnFormatter.URN_INDICE_SIP_V2),
			TiUrnXmlSessioneVers.ORIGINALE, vrsXmlDatiSessioneVers,
			vrsXmlDatiSessioneVersKo, rispostaWS, getIdStrut(versamento));
	    }
	    if (urns.getUrnNormalizzato() != null) {
		// salvo NORMALIZZATO
		tmpReturn = this.salvaVrsUrnXmlSessioneVers(
			MessaggiWSFormat.formattaUrnIndiceSip(urns.getUrnNormalizzato(),
				Costanti.UrnFormatter.URN_INDICE_SIP_V2),
			TiUrnXmlSessioneVers.NORMALIZZATO, vrsXmlDatiSessioneVers,
			vrsXmlDatiSessioneVersKo, rispostaWS, getIdStrut(versamento));
	    }
	    if (vrsXmlDatiSessioneVersBuilder.getDsUrnXmlVers() != null) {
		// salvo INIZIALE
		tmpReturn = this.salvaVrsUrnXmlSessioneVers(
			vrsXmlDatiSessioneVersBuilder.getDsUrnXmlVers(),
			TiUrnXmlSessioneVers.INIZIALE, vrsXmlDatiSessioneVers,
			vrsXmlDatiSessioneVersKo, rispostaWS, getIdStrut(versamento));
	    }
	}

	return tmpReturn;
    }

    // salva i dati xml di esito
    private boolean salvaXmlRisposta(AbsVersamentoExt versamento, SyncFakeSessn sessione,
	    String hashXmlEsito, String versioneXmlEsito, String xmlEsito,
	    DocumentoVers documentoVersIn, BackendStorage backendMetadata,
	    Optional<VrsDatiSessioneVers> vrsDatiSessioneVers,
	    Optional<VrsDatiSessioneVersKo> vrsDatiSessioneVersKo, IRispostaVersWS rispostaWS) {
	boolean tmpReturn = true;
	VrsXmlDatiSessioneVersBuilder vrsXmlDatiSessioneVersBuilder = VrsXmlDatiSessioneVersBuilder
		.builder().tiXmlDati(TipiXmlDati.RISPOSTA).vrsDatiSessioneVers(vrsDatiSessioneVers)
		.vrsDatiSessioneVersKo(vrsDatiSessioneVersKo);
	// se ho trovato il codice della struttura versante, lo scrivo
	if (versamento.getStrutturaComponenti() != null
		&& versamento.getStrutturaComponenti().getIdStruttura() != 0) {
	    vrsXmlDatiSessioneVersBuilder
		    .idStrut(new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
	}
	vrsXmlDatiSessioneVersBuilder.flCanonicalized(CostantiDB.Flag.FALSE);
	if (backendMetadata.isDataBase()) {
	    vrsXmlDatiSessioneVersBuilder.blXml(xmlEsito.length() == 0 ? "--" : xmlEsito);
	}
	vrsXmlDatiSessioneVersBuilder.cdVersioneXml(versioneXmlEsito);
	if (sessione.getUrnEsitoVersamento() != null) {
	    vrsXmlDatiSessioneVersBuilder.dsUrnXmlVers(sessione.getUrnEsitoVersamento());
	    vrsXmlDatiSessioneVersBuilder.dsHashXmlVers(hashXmlEsito);
	    vrsXmlDatiSessioneVersBuilder
		    .cdEncodingHashXmlVers(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi());
	    vrsXmlDatiSessioneVersBuilder.dsAlgoHashXmlVers(CostantiDB.TipiHash.SHA_256.descrivi());
	}

	Optional<VrsXmlDatiSessioneVers> vrsXmlDatiSessioneVers = Optional.empty();
	Optional<VrsXmlDatiSessioneVersKo> vrsXmlDatiSessioneVersKo = Optional.empty();
	try {
	    if (isOkOrWarningResponse(rispostaWS)) {
		vrsXmlDatiSessioneVers = Optional
			.of(vrsXmlDatiSessioneVersBuilder.buildVrsXmlDatiSessioneVers());
		entityManager.persist(vrsXmlDatiSessioneVers.get());
	    } else {
		vrsXmlDatiSessioneVersKo = Optional
			.of(vrsXmlDatiSessioneVersBuilder.buildVrsXmlDatiSessioneVersKo());
		entityManager.persist(vrsXmlDatiSessioneVersKo.get());
	    }
	} catch (RuntimeException re) {
	    log.error(ECCEZIONE_SALVATAGGIO_SESSIONE, re);
	    tmpReturn = false;
	}

	if (tmpReturn) {
	    final URNVersamento urns = calcolaUrn(versamento, documentoVersIn, sessione);
	    if (urns.getUrn() != null) {
		// salvo ORIGINALE
		tmpReturn = this.salvaVrsUrnXmlSessioneVers(
			MessaggiWSFormat.formattaUrnEsitoVers(urns.getUrn(),
				Costanti.UrnFormatter.URN_ESITO_VERS_V2),
			TiUrnXmlSessioneVers.ORIGINALE, vrsXmlDatiSessioneVers,
			vrsXmlDatiSessioneVersKo, rispostaWS, getIdStrut(versamento));
	    }
	    if (urns.getUrnNormalizzato() != null) {
		// salvo NORMALIZZATO
		tmpReturn = this.salvaVrsUrnXmlSessioneVers(
			MessaggiWSFormat.formattaUrnEsitoVers(urns.getUrnNormalizzato(),
				Costanti.UrnFormatter.URN_ESITO_VERS_V2),
			TiUrnXmlSessioneVers.NORMALIZZATO, vrsXmlDatiSessioneVers,
			vrsXmlDatiSessioneVersKo, rispostaWS, getIdStrut(versamento));
	    }
	    if (vrsXmlDatiSessioneVersBuilder.getDsUrnXmlVers() != null) {
		// salvo INIZIALE
		tmpReturn = this.salvaVrsUrnXmlSessioneVers(
			vrsXmlDatiSessioneVersBuilder.getDsUrnXmlVers(),
			TiUrnXmlSessioneVers.INIZIALE, vrsXmlDatiSessioneVers,
			vrsXmlDatiSessioneVersKo, rispostaWS, getIdStrut(versamento));
	    }
	}
	return tmpReturn;
    }

    // se sono presenti, salva i dati xml dell'indice MM (solo per VersamentoMM)
    private boolean salvaXmlIndiceMMVersamento(AbsVersamentoExt versamento, SyncFakeSessn sessione,
	    String versioneXmlChiamata, DocumentoVers documentoVersIn,
	    BackendStorage backendMetadata, Optional<VrsDatiSessioneVers> vrsDatiSessioneVers,
	    Optional<VrsDatiSessioneVersKo> vrsDatiSessioneVersKo, IRispostaVersWS rispostaWS) {
	boolean tmpReturn = true;
	VrsXmlDatiSessioneVersBuilder vrsXmlDatiSessioneVersBuilder = VrsXmlDatiSessioneVersBuilder
		.builder().vrsDatiSessioneVers(vrsDatiSessioneVers)
		.vrsDatiSessioneVersKo(vrsDatiSessioneVersKo).tiXmlDati(TipiXmlDati.INDICE_FILE);
	// se ho trovato il codice della struttura versante, lo scrivo
	if (versamento.getStrutturaComponenti() != null
		&& versamento.getStrutturaComponenti().getIdStruttura() != 0) {
	    vrsXmlDatiSessioneVersBuilder
		    .idStrut(new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
	}
	vrsXmlDatiSessioneVersBuilder.flCanonicalized(CostantiDB.Flag.FALSE);
	if (backendMetadata.isDataBase()) {
	    vrsXmlDatiSessioneVersBuilder
		    .blXml(sessione.getDatiPackInfoSipXml().length() == 0 ? "--"
			    : sessione.getDatiPackInfoSipXml());
	}
	vrsXmlDatiSessioneVersBuilder.cdVersioneXml(versioneXmlChiamata);
	if (sessione.getUrnPackInfoSipXml() != null) {
	    vrsXmlDatiSessioneVersBuilder.dsUrnXmlVers(sessione.getUrnPackInfoSipXml())
		    .dsHashXmlVers(sessione.getHashPackInfoSipXml())
		    .cdEncodingHashXmlVers(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi())
		    .dsAlgoHashXmlVers(CostantiDB.TipiHash.SHA_256.descrivi());
	}
	Optional<VrsXmlDatiSessioneVers> vrsXmlDatiSessioneVers = Optional.empty();
	Optional<VrsXmlDatiSessioneVersKo> vrsXmlDatiSessioneVersKo = Optional.empty();
	try {
	    if (isOkOrWarningResponse(rispostaWS)) {
		vrsXmlDatiSessioneVers = Optional
			.of(vrsXmlDatiSessioneVersBuilder.buildVrsXmlDatiSessioneVers());
		entityManager.persist(vrsXmlDatiSessioneVers.get());
	    } else {
		vrsXmlDatiSessioneVersKo = Optional
			.of(vrsXmlDatiSessioneVersBuilder.buildVrsXmlDatiSessioneVersKo());
		entityManager.persist(vrsXmlDatiSessioneVersKo.get());
	    }
	} catch (RuntimeException re) {
	    log.error(ECCEZIONE_SALVATAGGIO_SESSIONE, re);
	    tmpReturn = false;
	}

	if (tmpReturn) {
	    final URNVersamento urns = calcolaUrn(versamento, documentoVersIn, sessione);
	    if (urns.getUrn() != null) {
		// salvo ORIGINALE
		tmpReturn = this.salvaVrsUrnXmlSessioneVers(
			MessaggiWSFormat.formattaUrnEsitoVers(urns.getUrn(),
				Costanti.UrnFormatter.URN_PI_SIP_V2),
			TiUrnXmlSessioneVers.ORIGINALE, vrsXmlDatiSessioneVers,
			vrsXmlDatiSessioneVersKo, rispostaWS, getIdStrut(versamento));
	    }
	    if (urns.getUrnNormalizzato() != null) {
		// salvo NORMALIZZATO
		tmpReturn = this.salvaVrsUrnXmlSessioneVers(
			MessaggiWSFormat.formattaUrnEsitoVers(urns.getUrnNormalizzato(),
				Costanti.UrnFormatter.URN_PI_SIP_V2),
			TiUrnXmlSessioneVers.NORMALIZZATO, vrsXmlDatiSessioneVers,
			vrsXmlDatiSessioneVersKo, rispostaWS, getIdStrut(versamento));
	    }
	    if (vrsXmlDatiSessioneVersBuilder.getDsUrnXmlVers() != null) {
		// salvo INIZIALE
		tmpReturn = this.salvaVrsUrnXmlSessioneVers(
			vrsXmlDatiSessioneVersBuilder.getDsUrnXmlVers(),
			TiUrnXmlSessioneVers.INIZIALE, vrsXmlDatiSessioneVers,
			vrsXmlDatiSessioneVersKo, rispostaWS, getIdStrut(versamento));
	    }
	}
	return tmpReturn;
    }

    // se sono presenti, salva i dati xml del Rapporto di versamento
    private boolean salvaXmlRapportoVersamento(AbsVersamentoExt versamento, SyncFakeSessn sessione,
	    DocumentoVers documentoVersIn, BackendStorage backendMetadata,
	    Optional<VrsDatiSessioneVers> vrsDatiSessioneVers,
	    Optional<VrsDatiSessioneVersKo> vrsDatiSessioneVersKo, IRispostaVersWS rispostaWS) {
	boolean tmpReturn = true;
	VrsXmlDatiSessioneVersBuilder vrsXmlDatiSessioneVersBuilder = VrsXmlDatiSessioneVersBuilder
		.builder();
	if (sessione.getDatiRapportoVersamento() != null) {
	    vrsXmlDatiSessioneVersBuilder.vrsDatiSessioneVers(vrsDatiSessioneVers)
		    .vrsDatiSessioneVersKo(vrsDatiSessioneVersKo).tiXmlDati(TipiXmlDati.RAPP_VERS);
	    // se ho trovato il codice della struttura versante, lo scrivo
	    if (versamento.getStrutturaComponenti() != null
		    && versamento.getStrutturaComponenti().getIdStruttura() != 0) {
		vrsXmlDatiSessioneVersBuilder.idStrut(
			new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
	    }
	    vrsXmlDatiSessioneVersBuilder.flCanonicalized(CostantiDB.Flag.FALSE);
	    if (backendMetadata.isDataBase()) {
		vrsXmlDatiSessioneVersBuilder
			.blXml(sessione.getDatiRapportoVersamento().length() == 0 ? "--"
				: sessione.getDatiRapportoVersamento());
	    }
	    vrsXmlDatiSessioneVersBuilder.cdVersioneXml(Costanti.XML_RAPPORTO_VERS_VRSN);
	    if (sessione.getUrnRapportoVersamento() != null) {
		vrsXmlDatiSessioneVersBuilder.dsUrnXmlVers(sessione.getUrnRapportoVersamento())
			.dsHashXmlVers(sessione.getHashRapportoVersamento())
			.cdEncodingHashXmlVers(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi())
			.dsAlgoHashXmlVers(CostantiDB.TipiHash.SHA_256.descrivi());
	    }
	    Optional<VrsXmlDatiSessioneVers> vrsXmlDatiSessioneVers = Optional.empty();
	    Optional<VrsXmlDatiSessioneVersKo> vrsXmlDatiSessioneVersKo = Optional.empty();
	    try {
		if (isOkOrWarningResponse(rispostaWS)) {
		    vrsXmlDatiSessioneVers = Optional
			    .of(vrsXmlDatiSessioneVersBuilder.buildVrsXmlDatiSessioneVers());
		    entityManager.persist(vrsXmlDatiSessioneVers.get());
		} else {
		    vrsXmlDatiSessioneVersKo = Optional
			    .of(vrsXmlDatiSessioneVersBuilder.buildVrsXmlDatiSessioneVersKo());
		    entityManager.persist(vrsXmlDatiSessioneVersKo.get());
		}
	    } catch (RuntimeException re) {
		log.error(ECCEZIONE_SALVATAGGIO_SESSIONE, re);
		tmpReturn = false;
	    }
	    if (tmpReturn) {
		final URNVersamento urns = calcolaUrn(versamento, documentoVersIn, sessione);
		if (urns.getUrn() != null) {
		    // salvo ORIGINALE
		    tmpReturn = this.salvaVrsUrnXmlSessioneVers(
			    MessaggiWSFormat.formattaUrnEsitoVers(urns.getUrn(),
				    Costanti.UrnFormatter.URN_RAPP_VERS_V2),
			    TiUrnXmlSessioneVers.ORIGINALE, vrsXmlDatiSessioneVers,
			    vrsXmlDatiSessioneVersKo, rispostaWS, getIdStrut(versamento));
		}
		if (urns.getUrnNormalizzato() != null) {
		    // salvo NORMALIZZATO
		    tmpReturn = this.salvaVrsUrnXmlSessioneVers(
			    MessaggiWSFormat.formattaUrnEsitoVers(urns.getUrnNormalizzato(),
				    Costanti.UrnFormatter.URN_RAPP_VERS_V2),
			    TiUrnXmlSessioneVers.NORMALIZZATO, vrsXmlDatiSessioneVers,
			    vrsXmlDatiSessioneVersKo, rispostaWS, getIdStrut(versamento));
		}
		if (vrsXmlDatiSessioneVersBuilder.getDsUrnXmlVers() != null) {
		    // salvo INIZIALE
		    tmpReturn = this.salvaVrsUrnXmlSessioneVers(
			    vrsXmlDatiSessioneVersBuilder.getDsUrnXmlVers(),
			    TiUrnXmlSessioneVers.INIZIALE, vrsXmlDatiSessioneVers,
			    vrsXmlDatiSessioneVersKo, rispostaWS, getIdStrut(versamento));
		}
	    }
	}
	return tmpReturn;
    }

    private static URNVersamento calcolaUrn(AbsVersamentoExt versamento,
	    DocumentoVers documentoVersIn, SyncFakeSessn sessione) {
	/*
	 * calcolo URN
	 */
	// EVO#16486
	String tmpUrn = null;
	String tmpUrnNorm = null;
	if (versamento.getStrutturaComponenti() != null && sessione
		.getTipoSessioneVers() == SyncFakeSessn.TipiSessioneVersamento.VERSAMENTO) {
	    // calcolo parte urn ORIGINALE
	    if (StringUtils.isNotBlank(versamento.getStrutturaComponenti().getUrnPartVersatore())
		    && StringUtils
			    .isNotBlank(versamento.getStrutturaComponenti().getUrnPartChiaveUd())) {
		//
		tmpUrn = MessaggiWSFormat.formattaBaseUrnUnitaDoc(
			versamento.getStrutturaComponenti().getUrnPartVersatore(),
			versamento.getStrutturaComponenti().getUrnPartChiaveUd());
	    }
	    // calcolo parte urn NORMALIZZATO
	    if (StringUtils
		    .isNotBlank(versamento.getStrutturaComponenti().getUrnPartVersatoreNormalized())
		    && StringUtils.isNotBlank(
			    versamento.getStrutturaComponenti().getUrnPartChiaveUdNormalized())) {
		//
		tmpUrnNorm = MessaggiWSFormat.formattaBaseUrnUnitaDoc(
			versamento.getStrutturaComponenti().getUrnPartVersatoreNormalized(),
			versamento.getStrutturaComponenti().getUrnPartChiaveUdNormalized());
	    }
	} else if (documentoVersIn != null && sessione
		.getTipoSessioneVers() == SyncFakeSessn.TipiSessioneVersamento.AGGIUNGI_DOCUMENTO) {
	    // calcolo parte urn ORIGINALE
	    String tmpUrnPartDoc = null;
	    // DOCXXXXXX
	    if (documentoVersIn.getNiOrdDoc() != 0) {
		tmpUrnPartDoc = MessaggiWSFormat.formattaUrnPartDocumento(
			Costanti.CategoriaDocumento.Documento, documentoVersIn.getNiOrdDoc(), true,
			Costanti.UrnFormatter.DOC_FMT_STRING_V2,
			Costanti.UrnFormatter.PAD5DIGITS_FMT);
	    }

	    if (StringUtils.isNotBlank(tmpUrnPartDoc)
		    && StringUtils
			    .isNotBlank(versamento.getStrutturaComponenti().getUrnPartVersatore())
		    && StringUtils
			    .isNotBlank(versamento.getStrutturaComponenti().getUrnPartChiaveUd())) {
		//
		tmpUrn = MessaggiWSFormat.formattaBaseUrnDoc(
			versamento.getStrutturaComponenti().getUrnPartVersatore(),
			versamento.getStrutturaComponenti().getUrnPartChiaveUd(), tmpUrnPartDoc,
			Costanti.UrnFormatter.URN_DOC_FMT_STRING_V2);
	    }

	    // calcolo urn NORMALIZZATO
	    if (StringUtils.isNotBlank(tmpUrnPartDoc)
		    && StringUtils.isNotBlank(
			    versamento.getStrutturaComponenti().getUrnPartVersatoreNormalized())
		    && StringUtils.isNotBlank(
			    versamento.getStrutturaComponenti().getUrnPartChiaveUdNormalized())) {
		//
		tmpUrnNorm = MessaggiWSFormat.formattaBaseUrnDoc(
			versamento.getStrutturaComponenti().getUrnPartVersatoreNormalized(),
			versamento.getStrutturaComponenti().getUrnPartChiaveUdNormalized(),
			tmpUrnPartDoc, Costanti.UrnFormatter.URN_DOC_FMT_STRING_V2);
	    }
	}
	final String urn = tmpUrn;
	final String urnNormalizzato = tmpUrnNorm;

	// end EVO#16486
	return new URNVersamento() {
	    @Override
	    public String getUrn() {
		return urn;
	    }

	    @Override
	    public String getUrnNormalizzato() {
		return urnNormalizzato;
	    }
	};

    }

    private interface URNVersamento {

	String getUrn();

	String getUrnNormalizzato();
    }

    private List<VrsDocNonVer> getVrsDocNonVers(long idStruttura, String tipoRegistro,
	    BigDecimal value, String numero, String idDocumento) {
	String queryStr;
	javax.persistence.Query query;
	queryStr = "select al from VrsDocNonVer al " + "where al.orgStrut.idStrut = :idStrutIn "
		+ "and al.aaKeyUnitaDoc = :aaKeyUnitaDocIn "
		+ "and al.cdKeyUnitaDoc = :cdKeyUnitaDocIn "
		+ "and al.cdRegistroKeyUnitaDoc = :cdRegistroKeyUnitaDocIn "
		+ "and al.cdKeyDocVers = :cdKeyDocVersIn";
	query = entityManager.createQuery(queryStr);
	query.setParameter("idStrutIn", idStruttura);
	query.setParameter("cdRegistroKeyUnitaDocIn", tipoRegistro);
	query.setParameter("aaKeyUnitaDocIn", value);
	query.setParameter("cdKeyUnitaDocIn", numero);
	query.setParameter("cdKeyDocVersIn", idDocumento);
	return query.getResultList();
    }

    private List<VrsUnitaDocNonVer> getVrsUnitaDocNonVers(long idStruttura, String tipoRegistro,
	    BigDecimal aaKeyUnitaDoc, String numero) {
	String queryStr;
	javax.persistence.Query query;
	queryStr = "select al from  VrsUnitaDocNonVer al "
		+ "where al.orgStrut.idStrut = :idStrutIn "
		+ "and al.aaKeyUnitaDoc = :aaKeyUnitaDocIn "
		+ "and al.cdKeyUnitaDoc = :cdKeyUnitaDocIn "
		+ "and al.cdRegistroKeyUnitaDoc = :cdRegistroKeyUnitaDocIn ";
	query = entityManager.createQuery(queryStr);
	query.setParameter("idStrutIn", idStruttura);
	query.setParameter("cdRegistroKeyUnitaDocIn", tipoRegistro);
	query.setParameter("aaKeyUnitaDocIn", aaKeyUnitaDoc);
	query.setParameter("cdKeyUnitaDocIn", numero);
	return query.getResultList();
    }

    /**
     * Controlla che l’unità documentaria identificata dalla struttura versante, registro, anno e
     * numero esista
     * <p>
     * (NOTA: una unità doc può essere annullata più di una volta, per questo il conteggio può
     * essere superiore a 1)
     *
     * @param idStrut               id struttura
     * @param cdRegistroKeyUnitaDoc chiave registro unita doc
     * @param aaKeyUnitaDoc         anno unita doc
     * @param cdKeyUnitaDoc         numero unita doc
     *
     * @return true/false
     */
    public boolean existAroUnitaDoc(long idStrut, String cdRegistroKeyUnitaDoc,
	    BigDecimal aaKeyUnitaDoc, String cdKeyUnitaDoc) {
	String queryStr = "SELECT COUNT(u) FROM AroUnitaDoc u "
		+ "WHERE u.orgStrut.idStrut = :idStrut "
		+ "AND u.cdRegistroKeyUnitaDoc = :cdRegistroKeyUnitaDoc "
		+ "AND u.aaKeyUnitaDoc = :aaKeyUnitaDoc " + "AND u.cdKeyUnitaDoc = :cdKeyUnitaDoc ";
	Query query = entityManager.createQuery(queryStr);
	query.setParameter("idStrut", idStrut);
	query.setParameter("cdRegistroKeyUnitaDoc", cdRegistroKeyUnitaDoc);
	query.setParameter("aaKeyUnitaDoc", aaKeyUnitaDoc);
	query.setParameter("cdKeyUnitaDoc", cdKeyUnitaDoc);
	Long numUd = (Long) query.getSingleResult();
	return numUd > 0;
    }

    /*
     * Salvataggio XML profilo normativo UniDoc
     */
    private boolean salvaXmlModelloProfiloNormativoUniDoc(long idStrut,
	    long idRecUsoXsdProfiloNormativo, String xml, VrsDatiSessioneVers tmpDatiSessioneVers) {
	boolean tmpReturn = true;
	VrsXmlModelloSessioneVers tmpXmlModelloSessioneVers = new VrsXmlModelloSessioneVers();
	tmpXmlModelloSessioneVers.setVrsDatiSessioneVers(tmpDatiSessioneVers);
	// se ho trovato il codice della struttura versante, lo scrivo
	if (idStrut != 0) {
	    tmpXmlModelloSessioneVers.setIdStrut(new BigDecimal(idStrut));
	}
	tmpXmlModelloSessioneVers.setFlCanonicalized(CostantiDB.Flag.TRUE);
	tmpXmlModelloSessioneVers.setBlXml(xml);
	tmpXmlModelloSessioneVers.setDecUsoModelloXsdUniDoc(
		entityManager.find(DecUsoModelloXsdUniDoc.class, idRecUsoXsdProfiloNormativo));
	try {
	    entityManager.persist(tmpXmlModelloSessioneVers);
	    entityManager.flush();
	} catch (RuntimeException re) {
	    /// logga l'errore e blocca tutto
	    log.error("Eccezione nella persistenza della sessione ", re);
	    tmpReturn = false;
	}
	return tmpReturn;
    }

    private boolean udNonVersataNonPresente(CSChiave chiave, long idStruttura) {

	RispostaControlli rc = controlliSemantici.checkChiave(chiave, idStruttura,
		ControlliSemantici.TipiGestioneUDAnnullate.CONSIDERA_ASSENTE);
	return rc.getrLong() == -1;
    }

    private boolean docNonVersatoNonPresente(CSChiave chiave, long idStruttura,
	    String idDocumento) {

	RispostaControlli rc = controlliSemantici.checkChiave(chiave, idStruttura,
		ControlliSemantici.TipiGestioneUDAnnullate.CONSIDERA_ASSENTE);
	if (rc.getrLong() == -1) {
	    return true;
	}
	long idUd = rc.getrLong();
	rc = controlliSemantici.checkDocumentoInUd(idUd, idDocumento, "DUMMY");
	return rc.isrBoolean();
    }

    private boolean salvaVrsUrnXmlSessioneVers(String tmpUrn, TiUrnXmlSessioneVers tiUrn,
	    Optional<VrsXmlDatiSessioneVers> xmlDatiSessioneVers,
	    Optional<VrsXmlDatiSessioneVersKo> xmlDatiSessioneVersKo, IRispostaVersWS rispostaWS,
	    BigDecimal idStrut) {
	boolean tmpReturn = true;
	VrsUrnXmlSessioneVersBuilder builder = VrsUrnXmlSessioneVersBuilder.builder().dsUrn(tmpUrn)
		.tiUrn(tiUrn).idStrut(idStrut).vrsXmlDatiSessioneVers(xmlDatiSessioneVers)
		.vrsXmlDatiSessioneVersKo(xmlDatiSessioneVersKo);
	try {
	    entityManager.persist(
		    isOkOrWarningResponse(rispostaWS) ? builder.buildVrsUrnXmlSessioneVers()
			    : builder.buildVrsUrnXmlSessioneVersKo());
	} catch (RuntimeException re) {
	    log.error("Eccezione nella persistenza della sessione urn xml ", re);
	    tmpReturn = false;
	}
	// TODO#29834 serve? viene usato ?
	// xmlDatiSessioneVers.getVrsUrnXmlSessioneVers().add(tmpVrsUrnXmlSessioneVers)
	return tmpReturn;
    }

}
