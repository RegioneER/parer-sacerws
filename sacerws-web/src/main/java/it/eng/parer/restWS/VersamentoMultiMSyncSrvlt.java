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
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationException;

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
import it.eng.parer.ws.versamento.dto.RispostaWS;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamentoMM.dto.VersamentoMMExt;
import it.eng.parer.ws.versamentoMM.dto.WSDescVersamentoMM;
import it.eng.parer.ws.versamentoMM.ejb.VersamentoSyncMM;
import it.eng.parer.ws.xml.versResp.EsitoVersamento;
import java.time.ZonedDateTime;

/**
 * Servlet implementation class VersamentoMultiMSyncSrvlt
 */
public class VersamentoMultiMSyncSrvlt extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(VersamentoMultiMSyncSrvlt.class);
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
    public VersamentoMultiMSyncSrvlt() throws IOException {
        super();
        Properties props = new Properties();
        props.load(this.getClass().getClassLoader().getResourceAsStream("/Sacer.properties"));
        uploadDir = props.getProperty("versamentoSync.upload.directory");
        salvaLogSessione = props.getProperty("versamentoSync.saveLogSession").equals("1") ? true : false;
        maxRequestSize = Long.parseLong(props.getProperty("versamentoSync.maxRequestSize"));
        maxFileSize = Long.parseLong(props.getProperty("versamentoSync.maxFileSize"));

        instanceName = props.getProperty("ws.instanceName");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Response405.fancy405(resp, Response405.NomeWebServiceRest.VERSAMENTO_MM);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     *
     * @throws ServletException
     *             errore generico
     * @throws IOException
     *             errore generico di tipo IO
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        VersamentoSyncMM versamentoSync;
        XmlVersCache xmlVersCache;
        RispostaWS rispostaWs;
        VersamentoMMExt myVersamentoMMExt;
        EsitoVersamento myEsito = new EsitoVersamento();
        SyncFakeSessn sessioneFinta = new SyncFakeSessn();
        Iterator tmpIterator = null;
        DiskFileItem tmpFileItem = null;
        List fileItems = null;
        AvanzamentoWs tmpAvanzamento;
        RequestPrsr myRequestPrsr = new RequestPrsr();
        RequestPrsr.ReqPrsrConfig tmpPrsrConfig = new RequestPrsr().new ReqPrsrConfig();

        rispostaWs = new RispostaWS();
        myVersamentoMMExt = new VersamentoMMExt();
        myVersamentoMMExt.setDescrizione(new WSDescVersamentoMM());
        tmpAvanzamento = AvanzamentoWs.nuovoAvanzamentoWS(instanceName, AvanzamentoWs.Funzioni.VersamentoSync);
        tmpAvanzamento.logAvanzamento();

        // Recupera l'ejb, se possibile - altrimenti segnala errore
        try {
            versamentoSync = (VersamentoSyncMM) new InitialContext().lookup("java:app/sacerws-ejb/VersamentoSyncMM");
        } catch (NamingException ex) {
            throw new ServletException("Impossibile recuperare l'ejb VersamentoSyncMM ", ex);
        }

        try {
            xmlVersCache = (XmlVersCache) new InitialContext().lookup("java:app/sacerws-ejb/XmlVersCache");
        } catch (NamingException ex) {
            throw new ServletException("Impossibile recuperare l'ejb XmlVersCache ", ex);
        }

        tmpAvanzamento.setFase("EJB recuperato").logAvanzamento();

        versamentoSync.init(rispostaWs, tmpAvanzamento, myVersamentoMMExt, myEsito);
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
                    sessioneFinta.setTipoSessioneVers(SyncFakeSessn.TipiSessioneVersamento.VERSAMENTO);
                    sessioneFinta.setTipoDatiSessioneVers("XML_DOC");

                    tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.TrasferimentoPayloadIn)
                            .setFase("pronto a ricevere").logAvanzamento();

                    //
                    tmpPrsrConfig.setLeggiFile(false);
                    tmpPrsrConfig.setLeggindiceMM(true);
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

                        versamentoSync.verificaVersione(sessioneFinta.getVersioneWS(), rispostaWs, myVersamentoMMExt);
                    }

                    // testa le credenziali utente, tramite ejb
                    myEsito = rispostaWs.getIstanzaEsito();
                    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                        tmpAvanzamento.setFase("verifica credenziali").logAvanzamento();
                        versamentoSync.verificaCredenziali(sessioneFinta.getLoginName(), sessioneFinta.getPassword(),
                                sessioneFinta.getIpChiamante(), rispostaWs, myVersamentoMMExt);
                    }

                    // verifica formale e semantica dell'XML indice MM
                    myEsito = rispostaWs.getIstanzaEsito();
                    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                        tmpAvanzamento.setFase("verifica xml indice").logAvanzamento();
                        versamentoSync.parseXMLIndiceMM(sessioneFinta, rispostaWs, myVersamentoMMExt);
                    }

                    // verifica formale e semantica dell'XML di versamento
                    if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                        tmpAvanzamento.setFase("verifica xml").logAvanzamento();
                        versamentoSync.parseXML(sessioneFinta, rispostaWs, myVersamentoMMExt);
                    }

                    // verifica che tutti i componenti di tipo FILE dichiarati nell'XML abbiano avuto un corrispondente
                    // payload binario
                    myEsito = rispostaWs.getIstanzaEsito();
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        sessioneFinta.setXmlOk(true);
                        tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.VerificaCoerenzaComponentiFile)
                                .setFase("verifica coerenza").logAvanzamento();
                        versamentoSync.verificaCoerenzaComponenti(rispostaWs, myVersamentoMMExt, sessioneFinta,
                                uploadDir);
                    }

                    // verifica che tutti i componenti di tipo FILE superino i controlli crittografici
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.VerificaFirmeComponenti)
                                .setFase("inizio").logAvanzamento();

                        versamentoSync.controllaFirmeAndHash(rispostaWs, myVersamentoMMExt);

                        tmpAvanzamento.setFase("fine").logAvanzamento();
                    }

                    /*
                     * salvataggio unificato di dati e sessione finta di versamento. avviene se non ci sono errori nella
                     * signature della chiamata al WS. In tutti gli altri casi viene invocata perché si tenta di salvare
                     * per lo meno la sessione di versamento con gli errori. Da notare che entrambi i salvataggi possono
                     * essere disattivati, l'uno tramite una property e l'altro tramite un tag dell'XML di versamento.
                     */
                    if (rispostaWs.getErrorType() != RispostaWS.ErrorTypeEnum.WS_SIGNATURE) {
                        versamentoSync.salvaTutto(sessioneFinta, rispostaWs, myVersamentoMMExt);
                    }

                    // prepara risposta
                    myEsito = rispostaWs.getIstanzaEsito();

                } catch (FileUploadException e1) {
                    rispostaWs.setSeverity(SeverityEnum.ERROR);
                    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                            "Eccezione generica nella servlet versamento sync " + e1.getMessage());
                    log.error("Eccezione nella servlet versamentoMM sync", e1);
                } catch (Exception e1) {
                    rispostaWs.setSeverity(SeverityEnum.ERROR);
                    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                            "Eccezione generica nella servlet versamento sync " + e1.getMessage());
                    //
                    SrvltHandlingException.handlingSocketErrors(Response405.NomeWebServiceRest.VERSAMENTO_MM, e1,
                            rispostaWs);
                    log.error("Eccezione generica nella servlet versamentoMM sync", e1);
                } finally {
                    if (fileItems != null) {
                        // elimina i file temporanei che compongono la request
                        tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.Pulizia).setFase("").logAvanzamento();
                        tmpIterator = fileItems.iterator();
                        while (tmpIterator.hasNext()) {
                            tmpFileItem = (DiskFileItem) tmpIterator.next();
                            tmpFileItem.delete();
                        }
                    }

                    if (myVersamentoMMExt.isContainerZip() && myVersamentoMMExt.getPathLocaleContenutoZip() != null) {
                        versamentoSync.pulisciZipTemp(rispostaWs, myVersamentoMMExt, sessioneFinta);
                    }
                    /*
                     * nota, non c'è un else perché se i file non vengono estratti da uno zip ma caricati direttamente
                     * dalle referenze indicate nel file indice, la rimozione degli stessi deve essere a carico del
                     * chiamante.
                     */
                }
            } else {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.WS_CHECK, "La chiamata non è multipart/formdata ");
                log.error("Errore nella servlet versamentoMM sync: la chiamata non è multipart/formdata ");
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
            Marshaller tmpMarshaller = xmlVersCache.getVersRespCtxforEsitoVersamento().createMarshaller();
            tmpMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            tmpMarshaller.marshal(myEsito, tmpStreamWriter);
        } catch (MarshalException e) {
            log.error("Eccezione nella servlet versamentoMM sync", e);
        } catch (ValidationException e) {
            log.error("Eccezione nella servlet versamentoMM sync", e);
        } catch (Exception e) {
            log.error("Eccezione nella servlet versamentoMM sync", e);
        } finally {
            try {
                tmpStreamWriter.flush();
                tmpStreamWriter.close();
            } catch (Exception ei) {
                log.error("Eccezione nella servlet versamentoMM sync", ei);
            }
            try {
                out.flush();
                out.close();
            } catch (Exception ei) {
                log.error("Eccezione nella servlet versamentoMM sync", ei);
            }
        }

        tmpAvanzamento.setCheckPoint(AvanzamentoWs.CheckPoints.Fine).setFase("").logAvanzamento();
    }
}
