package it.eng.parer.restWS;

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
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.ZonedDateTime;
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
import javax.xml.bind.MarshalException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationException;

/**
 * Servlet implementation class VersamentoSyncSrvlt
 */
public class AggAllegatiSyncSrvlt extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AggAllegatiSyncSrvlt.class);
    private final String uploadDir;
    private final boolean salvaLogSessione;
    private final long maxRequestSize;
    private final long maxFileSize;
    private final String instanceName;

    /**
     * @see HttpServlet#HttpServlet()
     *
     * @throws IOException
     *             errore generico di tipo IO
     */
    public AggAllegatiSyncSrvlt() throws IOException {
        super();
        Properties props = new Properties();
        props.load(this.getClass().getClassLoader().getResourceAsStream("/Sacer.properties"));
        uploadDir = props.getProperty("aggAllegati.upload.directory");
        salvaLogSessione = props.getProperty("aggAllegati.saveLogSession").equals("1") ? true : false;
        maxRequestSize = Long.parseLong(props.getProperty("aggAllegati.maxRequestSize"));
        maxFileSize = Long.parseLong(props.getProperty("aggAllegati.maxFileSize"));

        instanceName = props.getProperty("ws.instanceName");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Response405.fancy405(resp, Response405.NomeWebServiceRest.AGGIUNTA_ALLEGATI_SYNC);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AggiuntaAllSync versamentoSync;
        XmlVersCache xmlVersCache;
        RispostaWSAggAll rispostaWs;
        VersamentoExtAggAll myVersamentoExt;
        EsitoVersAggAllegati myEsito = new EsitoVersAggAllegati();
        SyncFakeSessn sessioneFinta = new SyncFakeSessn();
        Iterator tmpIterator = null;
        DiskFileItem tmpFileItem = null;
        List fileItems = null;
        AvanzamentoWs tmpAvanzamento;
        RequestPrsr myRequestPrsr = new RequestPrsr();
        RequestPrsr.ReqPrsrConfig tmpPrsrConfig = new RequestPrsr().new ReqPrsrConfig();

        rispostaWs = new RispostaWSAggAll();
        myVersamentoExt = new VersamentoExtAggAll();
        myVersamentoExt.setDescrizione(new WSDescVersamentoAggAll());
        tmpAvanzamento = AvanzamentoWs.nuovoAvanzamentoWS(instanceName, AvanzamentoWs.Funzioni.AggiuntaDocumentiSync);
        tmpAvanzamento.logAvanzamento();

        // Recupera l'ejb, se possibile - altrimenti segnala errore
        try {
            versamentoSync = (AggiuntaAllSync) new InitialContext().lookup("java:app/sacerws-ejb/AggiuntaAllSync");
        } catch (NamingException ex) {
            throw new ServletException("Impossibile recuperare l'ejb AggiuntaAllSync ", ex);
        }

        try {
            xmlVersCache = (XmlVersCache) new InitialContext().lookup("java:app/sacerws-ejb/XmlVersCache");
        } catch (NamingException ex) {
            throw new ServletException("Impossibile recuperare l'ejb XmlVersCache ", ex);
        }

        tmpAvanzamento.setFase("EJB recuperato").logAvanzamento();

        versamentoSync.init(rispostaWs, tmpAvanzamento, myVersamentoExt, myEsito);
        myEsito = rispostaWs.getIstanzaEsito();

        sessioneFinta.setSalvaSessione(salvaLogSessione);
        sessioneFinta.setTmApertura(ZonedDateTime.now());
        //
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
                    sessioneFinta.setTipoSessioneVers(SyncFakeSessn.TipiSessioneVersamento.AGGIUNGI_DOCUMENTO);
                    sessioneFinta.setTipoDatiSessioneVers("XML_DOC");

                    tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.TrasferimentoPayloadIn)
                            .setFase("pronto a ricevere").logAvanzamento();

                    //
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
                        rispostaWs.setEsitoWsError(rispostaWs.getErrorCode(), rispostaWs.getErrorMessage());
                    }

                    tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.VerificaStrutturaChiamataWs)
                            .setFase("completata").logAvanzamento();

                    /*
                     * ******************************************************************************** fine della
                     * verifica della struttura/signature del web service. Verifica dei dati effettivamente versati
                     * ********************************************************************************
                     */
                    // testa se la versione è corretta
                    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                        tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.VerificaSemantica)
                                .setFase("verifica versione").logAvanzamento();

                        versamentoSync.verificaVersione(sessioneFinta.getVersioneWS(), rispostaWs, myVersamentoExt);
                    }

                    // testa le credenziali utente, tramite ejb
                    myEsito = rispostaWs.getIstanzaEsito();
                    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                        tmpAvanzamento.setFase("verifica credenziali").logAvanzamento();
                        versamentoSync.verificaCredenziali(sessioneFinta.getLoginName(), sessioneFinta.getPassword(),
                                sessioneFinta.getIpChiamante(), rispostaWs, myVersamentoExt);
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
                        tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.VerificaCoerenzaComponentiFile)
                                .setFase("caricamento file nell'EJB").logAvanzamento();

                        sessioneFinta.setXmlOk(true);
                        for (FileBinario tmpBinario : sessioneFinta.getFileBinari()) {
                            versamentoSync.addFile(tmpBinario, rispostaWs, myVersamentoExt);
                        }
                    }

                    // verifica che tutti i componenti di tipo FILE dichiarati nell'XML abbiano avuto un corrispondente
                    // payload binario
                    myEsito = rispostaWs.getIstanzaEsito();
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        tmpAvanzamento.setFase("verifica coerenza").logAvanzamento();

                        versamentoSync.verificaCoerenzaComponenti(rispostaWs, myVersamentoExt);
                    }

                    // verifica che tutti i componenti di tipo FILE superino i controlli crittografici
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.VerificaFirmeComponenti)
                                .setFase("inizio").logAvanzamento();

                        versamentoSync.controllaFirmeAndHash(rispostaWs, myVersamentoExt);

                        tmpAvanzamento.setFase("fine").logAvanzamento();
                    }

                    /*
                     * salvataggio unificato di dati e sessione finta di versamento. avviene se non ci sono errori nella
                     * signature della chiamata al WS. In tutti gli altri casi viene invocata perché si tenta di salvare
                     * per lo meno la sessione di versamento con gli errori. Da notare che entrambi i salvataggi possono
                     * essere disattivati, l'uno tramite una property e l'altro tramite un tag dell'XML di versamento.
                     */
                    if (rispostaWs.getErrorType() != RispostaWSAggAll.ErrorTypeEnum.WS_SIGNATURE) {
                        versamentoSync.salvaTutto(sessioneFinta, rispostaWs, myVersamentoExt);
                    }

                    // prepara risposta
                    myEsito = rispostaWs.getIstanzaEsito();

                } catch (FileUploadException e1) {
                    rispostaWs.setSeverity(SeverityEnum.ERROR);
                    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                            "Eccezione generica nella servlet aggiunta allegati sync " + e1.getMessage());
                    log.error("Eccezione nella servlet aggiunta allegati sync", e1);
                } catch (Exception e1) {
                    rispostaWs.setSeverity(SeverityEnum.ERROR);
                    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                            "Eccezione generica nella servlet aggiunta allegati sync " + e1.getMessage());
                    //
                    SrvltHandlingException.handlingSocketErrors(Response405.NomeWebServiceRest.AGGIUNTA_ALLEGATI_SYNC,
                            e1, rispostaWs);
                    log.error("Eccezione nella servlet aggiunta allegati sync", e1);
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
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.WS_CHECK, "La chiamata non è multipart/formdata ");
                log.error("Errore nella servlet aggiunta allegati sync: la chiamata non è multipart/formdata ");
            }
        }

        // rispondi
        tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.InvioRisposta).setFase("").logAvanzamento();
        response.reset();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/xml; charset=\"utf-8\"");
        ServletOutputStream out = response.getOutputStream();
        OutputStreamWriter tmpStreamWriter = new OutputStreamWriter(out, "UTF-8");

        try {
            Marshaller tmpMarshaller = xmlVersCache.getVersRespCtxforEsitoVersamentoAggAllegati().createMarshaller();
            tmpMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            tmpMarshaller.marshal(myEsito, tmpStreamWriter);
        } catch (MarshalException e) {
            log.error("Eccezione nella servlet versamento sync", e);
        } catch (ValidationException e) {
            log.error("Eccezione nella servlet versamento sync", e);
        } catch (Exception e) {
            log.error("Eccezione nella servlet versamento sync", e);
        } finally {
            try {
                tmpStreamWriter.flush();
                tmpStreamWriter.close();
            } catch (Exception ei) {
                log.error("Eccezione nella servlet versamento sync", ei);
            }
            try {
                out.flush();
                out.close();
            } catch (Exception ei) {
                log.error("Eccezione nella servlet versamento sync", ei);
            }
        }

        tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.Fine).setFase("").logAvanzamento();
    }
}
