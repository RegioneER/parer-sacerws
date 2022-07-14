package it.eng.parer.restWS;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

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
import java.time.ZonedDateTime;

/**
 *
 * @author sinatti_s
 */
public class UpdVersamentoSrvlt extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(UpdVersamentoSrvlt.class);
    private final String uploadDir;
    private final String instanceName;
    private final boolean salvaLogSessione;

    public UpdVersamentoSrvlt() throws IOException {
        super();
        Properties props = new Properties();
        props.load(this.getClass().getClassLoader().getResourceAsStream("/Sacer.properties"));
        /*
         * TODO: per il momento, dato che questo servizio fa riferimento ai versamenti (ne aggiorna i metadati), a rigor
         * di logica dovrebbe utilizzare gli stessi parametri, a meno che non si decide di utilizzarne altri o di farne
         * ad-hoc
         */

        uploadDir = props.getProperty("recuperoSync.upload.directory");
        salvaLogSessione = props.getProperty("recuperoSync.saveLogSession").equals("1") ? true : false;

        instanceName = props.getProperty("ws.instanceName");

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Response405.fancy405(resp, Response405.NomeWebServiceRest.AGGIORNAMENTO_VERSAMENTO);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request
     *            servlet request
     * @param response
     *            servlet response
     *
     * @throws ServletException
     *             if a servlet-specific error occurs
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AggiornamentoVersamentoSync aggVersamentoSync;
        XmlUpdVersCache xmlUpdVersCache;
        RispostaWSUpdVers rispostaWs;
        UpdVersamentoExt myVersamentoExt;
        SyncFakeSessn sessioneFinta = new SyncFakeSessn();
        Iterator tmpIterator = null;
        DiskFileItem tmpFileItem = null;
        List fileItems = null;
        AvanzamentoWs tmpAvanzamento;
        RequestPrsr myRequestPrsr = new RequestPrsr();
        RequestPrsr.ReqPrsrConfig tmpPrsrConfig = new RequestPrsr().new ReqPrsrConfig();

        rispostaWs = new RispostaWSUpdVers();
        myVersamentoExt = new UpdVersamentoExt();
        myVersamentoExt.setDescrizione(new WSDescUpdVers());
        tmpAvanzamento = AvanzamentoWs.nuovoAvanzamentoWS(instanceName, AvanzamentoWs.Funzioni.AggiornamentoVersamento);
        tmpAvanzamento.logAvanzamento();

        rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.ASSENTE);

        // Recupera l'ejb, se possibile - altrimenti segnala errore
        try {
            aggVersamentoSync = (AggiornamentoVersamentoSync) new InitialContext()
                    .lookup("java:app/sacerws-ejb/AggiornamentoVersamentoSync");
        } catch (NamingException ex) {
            throw new ServletException("Impossibile recuperare l'ejb AggiornamentoVersamentoSync ", ex);
        }

        try {
            xmlUpdVersCache = (XmlUpdVersCache) new InitialContext().lookup("java:app/sacerws-ejb/XmlUpdVersCache");
        } catch (NamingException ex) {
            throw new ServletException("Impossibile recuperare l'ejb XmlUpdVersCache ", ex);
        }

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
                    //
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
                     * ******************************************************************************** Verifica su
                     * chiamata al WS *********************************************************************************
                     */
                    if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.OK) {
                        // Controllo chiamata al WS: aggiornamento del versamento completamente fallito
                        // e non ne verrà mantenuta traccia nelle sessioni (??! TODO da verificare)
                        rispostaWs.setEsitoWsError(
                                ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_GENERALI_CHIAMATAWS),
                                rispostaWs.getErrorCode(), rispostaWs.getErrorMessage());
                        //
                        myVersamentoExt.addEsitoControlloOnGenerali(
                                ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_GENERALI_CHIAMATAWS),
                                rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, rispostaWs.getErrorCode(),
                                rispostaWs.getErrorMessage());

                    }

                    tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.VerificaStrutturaChiamataWs)
                            .setFase("completata").logAvanzamento();

                    /*
                     * ******************************************************************************** fine della
                     * verifica della struttura/signature del web service. Verifica dei dati effettivamente versati
                     * *********************************************************************************
                     */
                    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                        // dopo questo punto posso tentare di salvare la sessione di versamento
                        rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.ERRATA);
                        //
                        myVersamentoExt.setDatiXml(sessioneFinta.getDatiIndiceSipXml());
                        myVersamentoExt.setStrutturaUpdVers(new StrutturaUpdVers());
                        myVersamentoExt.getStrutturaUpdVers().setDataVersamento(
                                XmlDateUtility.xmlGregorianCalendarToDate(myEsito.getDataRapportoVersamento()));

                        //
                        myEsito.setIndiceSIP(sessioneFinta.getDatiIndiceSipXml());
                        // passate le verifiche di chiamata al WS
                        myVersamentoExt.addControlloOkOnGenerali(
                                ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_GENERALI_CHIAMATAWS));
                    }

                    /*
                     * ******************************************************************************** 3.3.2 Controllo
                     * versione XSD definita nella chiamata al WS Verifica versione Nota: in questo caso viene
                     * effettuato il controlloEsisteVersioneXSD + controlloChiamataWS in caso di versione NON presente
                     * sulla chiamata (campo vuoto)
                     * *********************************************************************************
                     */
                    // testa se la versione è corretta
                    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                        tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.VerificaSemantica)
                                .setFase("verifica versione").logAvanzamento();
                        aggVersamentoSync.verificaVersione(sessioneFinta.getVersioneWS(), rispostaWs, myVersamentoExt);
                    }

                    //
                    // testa le credenziali utente, tramite ejb
                    myEsito = rispostaWs.getCompRapportoUpdVers();
                    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                        tmpAvanzamento.setFase("verifica credenziali").logAvanzamento();
                        aggVersamentoSync.verificaCredenziali(sessioneFinta.getLoginName(), sessioneFinta.getPassword(),
                                sessioneFinta.getIpChiamante(), rispostaWs, myVersamentoExt);
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
                            "Eccezione generica nella servlet vaggiornamento versamento sync " + e1.getMessage());
                    log.error("Eccezione nella servlet aggiornamento versamento sync", e1);

                    // salvo la sessione ERRATA
                    // sessione FATALE (serve per gestire diversamente ciò che viene salvato sulla sessione degli
                    // ERRATI)
                    rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.FATALE);
                    sessioneFinta.setTmChiusura(ZonedDateTime.now());
                    aggVersamentoSync.salvaSessioneErrataSysError(sessioneFinta, rispostaWs, tmpAvanzamento,
                            myVersamentoExt);

                } catch (Exception e1) {
                    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                            "Eccezione generica nella servlet aggiornamento versamento sync " + e1.getMessage());

                    //
                    SrvltHandlingException.handlingSocketErrors(Response405.NomeWebServiceRest.AGGIORNAMENTO_VERSAMENTO,
                            e1, rispostaWs);
                    log.error("Eccezione generica nella servlet aggiornamento versamento sync", e1);

                    // salvo la sessione ERRATA
                    // sessione FATALE (serve per gestire diversamente ciò che viene salvato sulla sessione degli
                    // ERRATI)
                    rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.FATALE);
                    sessioneFinta.setTmChiusura(ZonedDateTime.now());
                    aggVersamentoSync.salvaSessioneErrataSysError(sessioneFinta, rispostaWs, tmpAvanzamento,
                            myVersamentoExt);

                } finally {
                    if (fileItems != null) {
                        // elimina i file temporanei
                        tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.Pulizia).setFase("").logAvanzamento();
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
                log.error("Errore nella servlet aggiornamento versamento sync: la chiamata non è multipart/formdata ");

                // salvo la sessione ERRATA
                // sessione FATALE (serve per gestire diversamente ciò che viene salvato sulla
                // sessione degli ERRATI)
                rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.FATALE);
                sessioneFinta.setTmChiusura(ZonedDateTime.now());
                aggVersamentoSync.salvaSessioneErrataSysError(sessioneFinta, rispostaWs, tmpAvanzamento,
                        myVersamentoExt);
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
            aggVersamentoSync.salvaSessioneErrataSysError(sessioneFinta, rispostaWs, tmpAvanzamento, myVersamentoExt);

        }

        // rispondi
        tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.InvioRisposta).setFase("").logAvanzamento();
        response.reset();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/xml; charset=\"utf-8\"");
        ServletOutputStream out = response.getOutputStream();
        OutputStreamWriter tmpStreamWriter = new OutputStreamWriter(out, "UTF-8");
        try {
            JAXBContext tmpcontesto = xmlUpdVersCache.getVersRespCtxforEsitoAggiornamentoVersamento();
            Marshaller tmpMarshaller = tmpcontesto.createMarshaller();
            tmpMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            tmpMarshaller.marshal(myEsito.produciEsitoAggiornamento(), tmpStreamWriter);
        } catch (JAXBException e) {
            log.error("Eccezione nella servlet aggiornamento versamento sync", e);
        } finally {
            try {
                tmpStreamWriter.flush();
                tmpStreamWriter.close();
            } catch (Exception ei) {
                log.error("Eccezione nella servlet aggiornamento versamento sync", ei);
            }
            try {
                out.flush();
                out.close();
            } catch (Exception ei) {
                log.error("Eccezione nella servlet aggiornamento versamento sync", ei);
            }
        }

        tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.Fine).setFase("").logAvanzamento();
    }

}
