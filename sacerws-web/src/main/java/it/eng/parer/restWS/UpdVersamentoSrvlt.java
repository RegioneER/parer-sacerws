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
import it.eng.parer.ws.ejb.XmlUpdVersCache;
import it.eng.parer.ws.utils.AvanzamentoWs;
import it.eng.parer.ws.utils.ControlliWSBundle;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.XmlDateUtility;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore;
import it.eng.parer.ws.versamentoUpd.dto.CompRapportoUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.RispostaWSUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.parer.ws.versamentoUpd.ejb.AggiornamentoVersamentoSync;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import it.eng.parer.ws.versamentoUpd.utils.WSDescUpdVers;
import it.eng.spagoCore.ConfigSingleton;

/**
 *
 * @author sinatti_s
 */
@WebServlet(urlPatterns = {
        "/AggiornamentoUnitaDocumentariaSync" }, asyncSupported = true)
public class UpdVersamentoSrvlt extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(UpdVersamentoSrvlt.class);
    private String uploadDir;
    private String instanceName;

    @EJB(mappedName = "java:app/sacerws-ejb/AggiornamentoVersamentoSync")
    private AggiornamentoVersamentoSync aggVersamentoSync;

    @EJB(mappedName = "java:app/sacerws-ejb/XmlUpdVersCache")
    private XmlUpdVersCache xmlUpdVersCache;

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
            Response405.fancy405(resp, Response405.NomeWebServiceRest.AGGIORNAMENTO_VERSAMENTO);
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
        AvanzamentoWs tmpAvanzamento;

        RequestPrsr myRequestPrsr = new RequestPrsr();
        SyncFakeSessn sessioneFinta = new SyncFakeSessn();
        RispostaWSUpdVers rispostaWs = new RispostaWSUpdVers();
        UpdVersamentoExt myVersamentoExt = new UpdVersamentoExt();

        myVersamentoExt.setDescrizione(new WSDescUpdVers());
        tmpAvanzamento = AvanzamentoWs.nuovoAvanzamentoWS(instanceName,
                AvanzamentoWs.Funzioni.AggiornamentoVersamento);
        tmpAvanzamento.logAvanzamento();

        rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.ASSENTE);

        tmpAvanzamento.setFase("EJB recuperato").logAvanzamento();

        aggVersamentoSync.init(rispostaWs, tmpAvanzamento, myVersamentoExt);
        CompRapportoUpdVers myEsito = rispostaWs.getCompRapportoUpdVers();
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
                    /*
                     * *****************************************************************************
                     * *** Verifica su chiamata al WS
                     * *****************************************************************************
                     * ****
                     */
                    if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.OK) {
                        // Controllo chiamata al WS: aggiornamento del versamento completamente
                        // fallito
                        // e non ne verrà mantenuta traccia nelle sessioni (??! TODO da verificare)
                        rispostaWs.setEsitoWsError(
                                ControlliWSBundle
                                        .getControllo(ControlliWSBundle.CTRL_GENERALI_CHIAMATAWS),
                                rispostaWs.getErrorCode(), rispostaWs.getErrorMessage());
                        //
                        myVersamentoExt.addEsitoControlloOnGenerali(
                                ControlliWSBundle
                                        .getControllo(ControlliWSBundle.CTRL_GENERALI_CHIAMATAWS),
                                rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO,
                                rispostaWs.getErrorCode(), rispostaWs.getErrorMessage());

                    }

                    tmpAvanzamento
                            .setCheckPoint(AvanzamentoWs.CheckPoints.VerificaStrutturaChiamataWs)
                            .setFase("completata").logAvanzamento();

                    /*
                     * *****************************************************************************
                     * *** fine della verifica della struttura/signature del web service. Verifica
                     * dei dati effettivamente versati
                     * *****************************************************************************
                     * ****
                     */
                    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                        // dopo questo punto posso tentare di salvare la sessione di versamento
                        rispostaWs.setStatoSessioneVersamento(
                                IRispostaWS.StatiSessioneVersEnum.ERRATA);
                        //
                        myVersamentoExt.setDatiXml(sessioneFinta.getDatiIndiceSipXml());
                        myVersamentoExt.setStrutturaUpdVers(new StrutturaUpdVers());
                        myVersamentoExt.getStrutturaUpdVers().setDataVersamento(XmlDateUtility
                                .xmlGregorianCalendarToDate(myEsito.getDataRapportoVersamento()));

                        //
                        myEsito.setIndiceSIP(sessioneFinta.getDatiIndiceSipXml());
                        // passate le verifiche di chiamata al WS
                        myVersamentoExt.addControlloOkOnGenerali(ControlliWSBundle
                                .getControllo(ControlliWSBundle.CTRL_GENERALI_CHIAMATAWS));
                    }

                    /*
                     * *****************************************************************************
                     * *** 3.3.2 Controllo versione XSD definita nella chiamata al WS Verifica
                     * versione Nota: in questo caso viene effettuato il controlloEsisteVersioneXSD
                     * + controlloChiamataWS in caso di versione NON presente sulla chiamata (campo
                     * vuoto)
                     * *****************************************************************************
                     * ****
                     */
                    // testa se la versione è corretta
                    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                        tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.VerificaSemantica)
                                .setFase("verifica versione").logAvanzamento();
                        aggVersamentoSync.verificaVersione(sessioneFinta.getVersioneWS(),
                                rispostaWs, myVersamentoExt);
                    }

                    //
                    // testa le credenziali utente, tramite ejb
                    myEsito = rispostaWs.getCompRapportoUpdVers();
                    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                        tmpAvanzamento.setFase("verifica credenziali").logAvanzamento();
                        aggVersamentoSync.verificaCredenziali(sessioneFinta.getLoginName(),
                                sessioneFinta.getPassword(), sessioneFinta.getIpChiamante(),
                                rispostaWs, myVersamentoExt);
                    }

                    // verifica formale e semantica dell'XML di versamento
                    myEsito = rispostaWs.getCompRapportoUpdVers();
                    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                        tmpAvanzamento.setFase("verifica xml").logAvanzamento();
                        aggVersamentoSync.parseXML(sessioneFinta, rispostaWs, myVersamentoExt);
                    }

                    sessioneFinta.setTmChiusura(ZonedDateTime.now());
                    aggVersamentoSync.salvaTutto(sessioneFinta, rispostaWs, myVersamentoExt);

                    // prepara risposta
                    myEsito = rispostaWs.getCompRapportoUpdVers();

                } catch (FileUploadException e1) {
                    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                            "Eccezione generica nella servlet vaggiornamento versamento sync "
                                    + e1.getMessage());
                    log.error("Eccezione nella servlet aggiornamento versamento sync", e1);

                    // salvo la sessione ERRATA
                    // sessione FATALE (serve per gestire diversamente ciò che viene salvato sulla
                    // sessione degli
                    // ERRATI)
                    rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.FATALE);
                    sessioneFinta.setTmChiusura(ZonedDateTime.now());
                    aggVersamentoSync.salvaSessioneErrataSysError(sessioneFinta, rispostaWs,
                            tmpAvanzamento, myVersamentoExt);

                } catch (Exception e1) {
                    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                            "Eccezione generica nella servlet aggiornamento versamento sync "
                                    + e1.getMessage());

                    //
                    SrvltHandlingException.handlingSocketErrors(
                            Response405.NomeWebServiceRest.AGGIORNAMENTO_VERSAMENTO, e1,
                            rispostaWs);
                    log.error("Eccezione generica nella servlet aggiornamento versamento sync", e1);

                    // salvo la sessione ERRATA
                    // sessione FATALE (serve per gestire diversamente ciò che viene salvato sulla
                    // sessione degli
                    // ERRATI)
                    rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.FATALE);
                    sessioneFinta.setTmChiusura(ZonedDateTime.now());
                    aggVersamentoSync.salvaSessioneErrataSysError(sessioneFinta, rispostaWs,
                            tmpAvanzamento, myVersamentoExt);

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
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_GENERALI_CHIAMATAWS),
                        "La chiamata non è multipart/formdata ");
                log.error(
                        "Errore nella servlet aggiornamento versamento sync: la chiamata non è multipart/formdata ");

                // salvo la sessione ERRATA
                // sessione FATALE (serve per gestire diversamente ciò che viene salvato sulla
                // sessione degli ERRATI)
                rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.FATALE);
                sessioneFinta.setTmChiusura(ZonedDateTime.now());
                aggVersamentoSync.salvaSessioneErrataSysError(sessioneFinta, rispostaWs,
                        tmpAvanzamento, myVersamentoExt);
            }
        } /* errore (non dovrebbe mai capitare) in fase di init */ else {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setEsitoWsError(MessaggiWSBundle.ERR_666, rispostaWs.getErrorMessage());
            log.error(
                    "Errore nella servlet aggiornamento versamento sync: Errore generico in fase di inizializzazione del servizio ");

            // salvo la sessione ERRATA
            // sessione FATALE (serve per gestire diversamente ciò che viene salvato sulla
            // sessione degli ERRATI)
            rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.FATALE);
            sessioneFinta.setTmChiusura(ZonedDateTime.now());
            aggVersamentoSync.salvaSessioneErrataSysError(sessioneFinta, rispostaWs, tmpAvanzamento,
                    myVersamentoExt);

        }

        // rispondi
        tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.InvioRisposta).setFase("")
                .logAvanzamento();
        response.reset();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/xml; charset=\"utf-8\"");
        try (OutputStreamWriter tmpStreamWriter = new OutputStreamWriter(response.getOutputStream(),
                StandardCharsets.UTF_8);) {

            Marshaller tmpMarshaller = xmlUpdVersCache
                    .getVersRespCtxforEsitoAggiornamentoVersamento().createMarshaller();
            tmpMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            tmpMarshaller.marshal(myEsito.produciEsitoAggiornamento(), tmpStreamWriter);

        } catch (Exception e) {
            log.error("Eccezione nella servlet aggiornamento versamento sync", e);
        }

        tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.Fine).setFase("").logAvanzamento();
    }

}
