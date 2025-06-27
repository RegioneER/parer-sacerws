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
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.ejb.XmlFascCache;
import it.eng.parer.ws.utils.AvanzamentoWs;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.XmlDateUtility;
import it.eng.parer.ws.versFascicoli.dto.CompRapportoVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.RispostaWSFascicolo;
import it.eng.parer.ws.versFascicoli.dto.StrutturaVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.VersFascicoloExt;
import it.eng.parer.ws.versFascicoli.dto.WSDescVersFascicolo;
import it.eng.parer.ws.versFascicoli.ejb.VersFascicoloSync;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.xml.versfascicoloresp.ECEsitoPosNegType;
import it.eng.parer.ws.xml.versfascicoloresp.ECEsitoXSDType;
import it.eng.spagoCore.ConfigSingleton;

/**
 *
 * @author fioravanti_f
 */
@WebServlet(urlPatterns = {
	"/VersamentoFascicoloSync" }, asyncSupported = true)
public class VersamentoFascicoloSrvlt extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(VersamentoFascicoloSrvlt.class);
    private String uploadDir;
    private String instanceName;

    @EJB(mappedName = "java:app/sacerws-ejb/VersFascicoloSync")
    private VersFascicoloSync versFascicoloSync;

    @EJB(mappedName = "java:app/sacerws-ejb/XmlFascCache")
    private XmlFascCache xmlFascCache;

    @Override
    public void init(ServletConfig config) throws ServletException {
	super.init(config);
	// custom
	uploadDir = ConfigSingleton.getInstance().getStringValue(WS_STAGING_UPLOAD_DIR.name());
	instanceName = ConfigSingleton.getInstance().getStringValue(WS_INSTANCE_NAME.name());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	try {
	    Response405.fancy405(resp, Response405.NomeWebServiceRest.VERSAMENTO_FASCICOLO);
	} catch (IOException e) {
	    log.error("Errore generico", e);
	}
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     *
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	Iterator<FileItem> tmpIterator = null;
	DiskFileItem tmpFileItem = null;
	List<FileItem> fileItems = null;

	SyncFakeSessn sessioneFinta = new SyncFakeSessn();
	RequestPrsr myRequestPrsr = new RequestPrsr();
	RispostaWSFascicolo rispostaWs = new RispostaWSFascicolo();
	VersFascicoloExt myVersamentoExt = new VersFascicoloExt();
	myVersamentoExt.setDescrizione(new WSDescVersFascicolo());
	AvanzamentoWs tmpAvanzamento = AvanzamentoWs.nuovoAvanzamentoWS(instanceName,
		AvanzamentoWs.Funzioni.VersamentoFascicolo);
	tmpAvanzamento.logAvanzamento();

	// in questo punto non ho elementi per salvare la sessione di versamento, per
	// quanto errata
	// (mi serve almeno l'user id definito nella chiamata del ws)
	rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.ASSENTE);

	tmpAvanzamento.setFase("EJB recuperato").logAvanzamento();

	versFascicoloSync.init(rispostaWs, tmpAvanzamento, myVersamentoExt);
	CompRapportoVersFascicolo myEsito = rispostaWs.getCompRapportoVersFascicolo();
	sessioneFinta.setTmApertura(ZonedDateTime.now());
	sessioneFinta.setIpChiamante(myRequestPrsr.leggiIpVersante(request));

	if (rispostaWs.getSeverity() == IRispostaWS.SeverityEnum.OK) {
	    // Check that we have a file upload request
	    boolean isMultipart = ServletFileUpload.isMultipartContent(request);
	    if (isMultipart) {
		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();

		// maximum size that will be stored in memory
		factory.setSizeThreshold(1);
		factory.setRepository(new File(uploadDir));

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		tmpAvanzamento.setFase("Servlet pronta a ricevere i file").logAvanzamento();
		try {
		    tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.TrasferimentoPayloadIn)
			    .setFase("pronto a ricevere").logAvanzamento();
		    RequestPrsr.ReqPrsrConfig tmpPrsrConfig = RequestPrsr.createConfig();
		    tmpPrsrConfig.setLeggiFile(false);
		    tmpPrsrConfig.setLeggindiceMM(false);
		    tmpPrsrConfig.setAvanzamentoWs(tmpAvanzamento);
		    tmpPrsrConfig.setSessioneFinta(sessioneFinta);
		    tmpPrsrConfig.setRequest(request);
		    tmpPrsrConfig.setUploadHandler(upload);
		    //
		    fileItems = myRequestPrsr.parse(rispostaWs, tmpPrsrConfig);
		    //
		    if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.OK) {
			// è un errore WS-CHECK: il versamento è completamente fallito
			// e non ne verrà mantenuta traccia nelle sessioni
			rispostaWs.setEsitoWsError(rispostaWs.getErrorCode(),
				rispostaWs.getErrorMessage());
			myEsito.getEsitoChiamataWS().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
			myEsito.getEsitoChiamataWS()
				.setCredenzialiOperatore(ECEsitoPosNegType.NEGATIVO);
			myEsito.getEsitoChiamataWS()
				.setVersioneWSCorretta(ECEsitoPosNegType.NEGATIVO);
		    }

		    tmpAvanzamento
			    .setCheckPoint(AvanzamentoWs.CheckPoints.VerificaStrutturaChiamataWs)
			    .setFase("completata").logAvanzamento();

		    /*
		     * *****************************************************************************
		     * *** fine della verifica della struttura/signature del web service. Verifica
		     * dei dati effettivamente versati
		     * *****************************************************************************
		     * ***
		     */
		    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
			// dopo questo punto posso tentare di salvare la sessione di versamento
			rispostaWs.setStatoSessioneVersamento(
				IRispostaWS.StatiSessioneVersEnum.ERRATA);
			//
			myVersamentoExt.setDatiXml(sessioneFinta.getDatiIndiceSipXml());
			myVersamentoExt.setStrutturaComponenti(new StrutturaVersFascicolo());
			myVersamentoExt.getStrutturaComponenti().setDataVersamento(XmlDateUtility
				.xmlGregorianCalendarToDate(myEsito.getDataRapportoVersamento()));
		    }
		    // testa se la versione è corretta
		    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
			tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.VerificaSemantica)
				.setFase("verifica versione").logAvanzamento();

			versFascicoloSync.verificaVersione(sessioneFinta.getVersioneWS(),
				rispostaWs, myVersamentoExt);
		    }

		    //
		    // testa le credenziali utente, tramite ejb
		    myEsito = rispostaWs.getCompRapportoVersFascicolo();
		    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
			tmpAvanzamento.setFase("verifica credenziali").logAvanzamento();
			versFascicoloSync.verificaCredenziali(sessioneFinta.getLoginName(),
				sessioneFinta.getPassword(), sessioneFinta.getIpChiamante(),
				rispostaWs, myVersamentoExt);
		    }

		    // verifica formale e semantica dell'XML di versamento
		    myEsito = rispostaWs.getCompRapportoVersFascicolo();
		    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
			myEsito.setEsitoXSD(new ECEsitoXSDType());
			myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.POSITIVO);
			//
			tmpAvanzamento.setFase("verifica xml").logAvanzamento();
			versFascicoloSync.parseXML(sessioneFinta, rispostaWs, myVersamentoExt);
		    }

		    sessioneFinta.setTmChiusura(ZonedDateTime.now());
		    versFascicoloSync.salvaTutto(sessioneFinta, rispostaWs, myVersamentoExt);

		    // prepara risposta
		    myEsito = rispostaWs.getCompRapportoVersFascicolo();

		} catch (FileUploadException e1) {
		    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
		    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
			    "Eccezione generica nella servlet versamento fascicolo sync "
				    + e1.getMessage());
		    log.error("Eccezione nella servlet versamento fascicolo sync", e1);
		} catch (Exception e1) {
		    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
		    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
			    "Eccezione generica nella servlet versamento fascicolo sync "
				    + e1.getMessage());

		    //
		    SrvltHandlingException.handlingSocketErrors(
			    Response405.NomeWebServiceRest.VERSAMENTO_FASCICOLO, e1, rispostaWs);
		    log.error("Eccezione generica nella servlet vetsamento fascicolo sync", e1);
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
		rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
		rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.WS_CHECK,
			"La chiamata non è multipart/formdata ");
		log.error(
			"Errore nella servlet versamento fascicolo sync: la chiamata non è multipart/formdata ");
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

	    Marshaller tmpMarshaller = xmlFascCache.getVersRespFascicoloCtx().createMarshaller();
	    tmpMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    tmpMarshaller.marshal(myEsito.produciEsitoFascicolo(), tmpStreamWriter);

	} catch (Exception e) {
	    log.error("Eccezione nella servlet versamento fascicolo sync", e);
	}

	tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.Fine).setFase("").logAvanzamento();
    }

}
