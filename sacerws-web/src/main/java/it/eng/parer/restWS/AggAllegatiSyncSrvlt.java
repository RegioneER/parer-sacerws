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

package it.eng.parer.restWS;

import static it.eng.spagoCore.ConfigProperties.StandardProperty.AGG_ALLEGATI_MAX_FILE_SIZE;
import static it.eng.spagoCore.ConfigProperties.StandardProperty.AGG_ALLEGATI_MAX_REQUEST_SIZE;
import static it.eng.spagoCore.ConfigProperties.StandardProperty.AGG_ALLEGATI_SAVE_LOG_SESSION;
import static it.eng.spagoCore.ConfigProperties.StandardProperty.WS_INSTANCE_NAME;
import static it.eng.spagoCore.ConfigProperties.StandardProperty.WS_STAGING_UPLOAD_DIR;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.Marshaller;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.restWS.util.RequestPrsr;
import it.eng.parer.restWS.util.Response405;
import it.eng.parer.restWS.util.SrvltHandlingException;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.ejb.XmlVersCache;
import it.eng.parer.ws.utils.AvanzamentoWs;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versamento.dto.FileBinario;
import it.eng.parer.ws.versamento.dto.RispostaWSAggAll;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VersamentoExtAggAll;
import it.eng.parer.ws.versamento.dto.WSDescVersamentoAggAll;
import it.eng.parer.ws.versamento.ejb.AggiuntaAllSync;
import it.eng.parer.ws.xml.versResp.EsitoVersAggAllegati;
import it.eng.spagoCore.ConfigSingleton;

/**
 * Servlet implementation class VersamentoSyncSrvlt
 */
@WebServlet(urlPatterns = {
	"/AggiuntaAllegatiSync" }, asyncSupported = true)
public class AggAllegatiSyncSrvlt extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AggAllegatiSyncSrvlt.class);
    private String uploadDir;
    private boolean salvaLogSessione;
    private long maxRequestSize;
    private long maxFileSize;
    private String instanceName;

    @EJB(mappedName = "java:app/sacerws-ejb/AggiuntaAllSync")
    private AggiuntaAllSync versamentoSync;

    @EJB(mappedName = "java:app/sacerws-ejb/XmlVersCache")
    private XmlVersCache xmlVersCache;

    @Override
    public void init(ServletConfig config) throws ServletException {
	super.init(config);
	// custom
	uploadDir = ConfigSingleton.getInstance().getStringValue(WS_STAGING_UPLOAD_DIR.name());
	salvaLogSessione = ConfigSingleton.getInstance()
		.getBooleanValue(AGG_ALLEGATI_SAVE_LOG_SESSION.name());
	maxRequestSize = ConfigSingleton.getInstance()
		.getLongValue(AGG_ALLEGATI_MAX_REQUEST_SIZE.name());
	maxFileSize = ConfigSingleton.getInstance().getLongValue(AGG_ALLEGATI_MAX_FILE_SIZE.name());
	instanceName = ConfigSingleton.getInstance().getStringValue(WS_INSTANCE_NAME.name());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	try {
	    Response405.fancy405(resp, Response405.NomeWebServiceRest.AGGIUNTA_ALLEGATI_SYNC);
	} catch (IOException e) {
	    log.error("Errore generico", e);
	}
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {

	EsitoVersAggAllegati myEsito = new EsitoVersAggAllegati();
	SyncFakeSessn sessioneFinta = new SyncFakeSessn();
	Iterator<FileItem> tmpIterator = null;
	DiskFileItem tmpFileItem = null;
	List<FileItem> fileItems = null;

	RequestPrsr myRequestPrsr = new RequestPrsr();
	RispostaWSAggAll rispostaWs = new RispostaWSAggAll();
	VersamentoExtAggAll myVersamentoExt = new VersamentoExtAggAll();
	myVersamentoExt.setDescrizione(new WSDescVersamentoAggAll());
	AvanzamentoWs tmpAvanzamento = AvanzamentoWs.nuovoAvanzamentoWS(instanceName,
		AvanzamentoWs.Funzioni.AggiuntaDocumentiSync);
	tmpAvanzamento.logAvanzamento();

	tmpAvanzamento.setFase("EJB recuperato").logAvanzamento();

	versamentoSync.init(rispostaWs, tmpAvanzamento, myVersamentoExt, myEsito);
	myEsito = rispostaWs.getIstanzaEsito();

	sessioneFinta.setSalvaSessione(salvaLogSessione);
	sessioneFinta.setTmApertura(ZonedDateTime.now());

	sessioneFinta.setIpChiamante(myRequestPrsr.leggiIpVersante(request));

	if (rispostaWs.getSeverity() == SeverityEnum.OK) {
	    // Check that we have a file upload request
	    boolean isMultipart = ServletFileUpload.isMultipartContent(request);
	    if (isMultipart) {
		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// maximum size that will be stored in memory
		factory.setSizeThreshold(0);
		//
		factory.setRepository(new File(uploadDir));
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		// maximum size before a FileUploadException will be thrown
		upload.setSizeMax(maxRequestSize);
		upload.setFileSizeMax(maxFileSize);

		tmpAvanzamento.setFase("Servlet pronta a ricevere i file").logAvanzamento();

		try {
		    sessioneFinta.setTipoSessioneVers(
			    SyncFakeSessn.TipiSessioneVersamento.AGGIUNGI_DOCUMENTO);
		    sessioneFinta.setTipoDatiSessioneVers("XML_DOC");

		    tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.TrasferimentoPayloadIn)
			    .setFase("pronto a ricevere").logAvanzamento();

		    RequestPrsr.ReqPrsrConfig tmpPrsrConfig = RequestPrsr.createConfig();
		    tmpPrsrConfig.setLeggiFile(true);
		    tmpPrsrConfig.setLeggindiceMM(false);
		    tmpPrsrConfig.setAvanzamentoWs(tmpAvanzamento);
		    tmpPrsrConfig.setSessioneFinta(sessioneFinta);
		    tmpPrsrConfig.setRequest(request);
		    tmpPrsrConfig.setUploadHandler(upload);
		    //
		    fileItems = myRequestPrsr.parse(rispostaWs, tmpPrsrConfig);
		    //
		    if (rispostaWs.getSeverity() != SeverityEnum.OK) {
			rispostaWs.setEsitoWsError(rispostaWs.getErrorCode(),
				rispostaWs.getErrorMessage());
		    }

		    tmpAvanzamento
			    .setCheckPoint(AvanzamentoWs.CheckPoints.VerificaStrutturaChiamataWs)
			    .setFase("completata").logAvanzamento();

		    // Se il backend di staging configurato è l'object storage qui avviene l'upload
		    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
			tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.CopiaBackendStaging)
				.setFase("inizio verifica/copia backend staging").logAvanzamento();
			versamentoSync.uploadComponentiStaging(sessioneFinta);

			tmpAvanzamento.setFase("fine verifica/copia backend staging")
				.logAvanzamento();
		    }

		    /*
		     * *****************************************************************************
		     * *** fine della verifica della struttura/signature del web service. Verifica
		     * dei dati effettivamente versati
		     * *****************************************************************************
		     * ***
		     */
		    // testa se la versione è corretta
		    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
			tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.VerificaSemantica)
				.setFase("verifica versione").logAvanzamento();

			versamentoSync.verificaVersione(sessioneFinta.getVersioneWS(), rispostaWs,
				myVersamentoExt);
		    }

		    // testa le credenziali utente, tramite ejb
		    myEsito = rispostaWs.getIstanzaEsito();
		    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
			tmpAvanzamento.setFase("verifica credenziali").logAvanzamento();
			versamentoSync.verificaCredenziali(sessioneFinta.getLoginName(),
				sessioneFinta.getPassword(), sessioneFinta.getIpChiamante(),
				rispostaWs, myVersamentoExt);
		    }

		    // verifica formale e semantica dell'XML di versamento
		    myEsito = rispostaWs.getIstanzaEsito();
		    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
			tmpAvanzamento.setFase("verifica xml").logAvanzamento();

			versamentoSync.parseXML(sessioneFinta, rispostaWs, myVersamentoExt);
		    }

		    // caricamento dei file versati nell'EJB di versamento
		    myEsito = rispostaWs.getIstanzaEsito();
		    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
			tmpAvanzamento
				.setCheckPoint(
					AvanzamentoWs.CheckPoints.VerificaCoerenzaComponentiFile)
				.setFase("caricamento file nell'EJB").logAvanzamento();

			sessioneFinta.setXmlOk(true);
			for (FileBinario tmpBinario : sessioneFinta.getFileBinari()) {
			    versamentoSync.addFile(tmpBinario, rispostaWs, myVersamentoExt);
			}
		    }

		    // verifica che tutti i componenti di tipo FILE dichiarati nell'XML abbiano
		    // avuto un corrispondente
		    // payload binario
		    myEsito = rispostaWs.getIstanzaEsito();
		    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
			tmpAvanzamento.setFase("verifica coerenza").logAvanzamento();

			versamentoSync.verificaCoerenzaComponenti(rispostaWs, myVersamentoExt);
		    }

		    // verifica che tutti i componenti di tipo FILE superino i controlli
		    // crittografici
		    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
			tmpAvanzamento
				.setCheckPoint(AvanzamentoWs.CheckPoints.VerificaFirmeComponenti)
				.setFase("inizio").logAvanzamento();

			versamentoSync.controllaFirmeAndHash(rispostaWs, myVersamentoExt);

			tmpAvanzamento.setFase("fine").logAvanzamento();
		    }

		    /*
		     * salvataggio unificato di dati e sessione finta di versamento. avviene se non
		     * ci sono errori nella signature della chiamata al WS. In tutti gli altri casi
		     * viene invocata perché si tenta di salvare per lo meno la sessione di
		     * versamento con gli errori. Da notare che entrambi i salvataggi possono essere
		     * disattivati, l'uno tramite una property e l'altro tramite un tag dell'XML di
		     * versamento.
		     */
		    if (rispostaWs.getErrorType() != RispostaWSAggAll.ErrorTypeEnum.WS_SIGNATURE) {
			versamentoSync.salvaTutto(sessioneFinta, rispostaWs, myVersamentoExt);
		    }

		    // prepara risposta
		    myEsito = rispostaWs.getIstanzaEsito();

		} catch (FileUploadException e1) {
		    rispostaWs.setSeverity(SeverityEnum.ERROR);
		    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
			    "Eccezione generica nella servlet aggiunta allegati sync "
				    + e1.getMessage());
		    log.error("Eccezione nella servlet aggiunta allegati sync", e1);
		} catch (Exception e1) {
		    rispostaWs.setSeverity(SeverityEnum.ERROR);
		    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
			    "Eccezione generica nella servlet aggiunta allegati sync "
				    + e1.getMessage());
		    //
		    SrvltHandlingException.handlingSocketErrors(
			    Response405.NomeWebServiceRest.AGGIUNTA_ALLEGATI_SYNC, e1, rispostaWs);
		    log.error("Eccezione nella servlet aggiunta allegati sync", e1);
		} finally {
		    if (fileItems != null) {
			// elimina i file temporanei
			tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.Pulizia).setFase("")
				.logAvanzamento();
			tmpIterator = fileItems.iterator();
			while (tmpIterator.hasNext()) {
			    tmpFileItem = (DiskFileItem) tmpIterator.next();
			    tmpFileItem.delete();
			}
		    }
		}
	    } else {
		rispostaWs.setSeverity(SeverityEnum.ERROR);
		rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.WS_CHECK,
			"La chiamata non è multipart/formdata ");
		log.error(
			"Errore nella servlet aggiunta allegati sync: la chiamata non è multipart/formdata ");
	    }
	}

	// rispondi
	tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.InvioRisposta).setFase("")
		.logAvanzamento();
	response.reset();
	response.setStatus(HttpServletResponse.SC_OK);
	response.setContentType("application/xml; charset=\"utf-8\"");
	try (OutputStreamWriter tmpStreamWriter = new OutputStreamWriter(response.getOutputStream(),
		StandardCharsets.UTF_8);) {

	    Marshaller tmpMarshaller = xmlVersCache.getVersRespCtxforEsitoVersamentoAggAllegati()
		    .createMarshaller();
	    tmpMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    tmpMarshaller.marshal(myEsito, tmpStreamWriter);

	} catch (Exception e) {
	    log.error("Eccezione nella servlet versamento sync", e);
	}

	tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.Fine).setFase("").logAvanzamento();
    }
}
