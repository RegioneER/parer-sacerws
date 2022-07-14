package it.eng.parer.ws.versamento.ejb;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.AroDoc;
import it.eng.parer.entity.AroUnitaDoc;
import it.eng.parer.entity.DecUsoModelloXsdUniDoc;
import it.eng.parer.entity.IamUser;
import it.eng.parer.entity.OrgStrut;
import it.eng.parer.entity.VrsDatiSessioneVers;
import it.eng.parer.entity.VrsDocNonVer;
import it.eng.parer.entity.VrsErrSessioneVers;
import it.eng.parer.entity.VrsFileSessione;
import it.eng.parer.entity.VrsSessioneVers;
import it.eng.parer.entity.VrsUnitaDocNonVer;
import it.eng.parer.entity.VrsUrnXmlSessioneVers;
import it.eng.parer.entity.VrsXmlDatiSessioneVers;
import it.eng.parer.entity.VrsXmlModelloSessioneVers;
import it.eng.parer.entity.constraint.VrsUrnXmlSessioneVers.TiUrnXmlSessioneVers;
import it.eng.parer.util.ejb.AppServerInstance;
import it.eng.parer.ws.dto.CSChiave;
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
import it.eng.parer.ws.versamento.dto.DocumentoVers;
import it.eng.parer.ws.versamento.dto.FileBinario;
import it.eng.parer.ws.versamento.dto.IRispostaVersWS;
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
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import static it.eng.parer.util.DateUtilsConverter.convert;

/**
 *
 * @author Fioravanti_F
 */
@Stateless(mappedName = "LogSessioneSync")
@LocalBean
public class LogSessioneSync {

    @EJB
    private XmlVersCache xmlVersCache;
    //
    @EJB
    private WriteCompBlbOracle writeCompBlbOracle;
    //
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
    private final static int DS_ERR_MAX_LEN = 1024;
    private final static String SESSIONE_CHIUSA_OK = "CHIUSA_OK";
    private final static String SESSIONE_CHIUSA_ERR = "CHIUSA_ERR";
    private final static String TIPO_ERR_FATALE = "FATALE";
    private final static String TIPO_ERR_WARNING = "WARNING";
    private final static String CD_DS_ERR_DIVERSI = "Diversi";

    public RispostaControlli salvaSessioneVersamento(VersamentoExt versamento, RispostaWS rispostaWS,
            SyncFakeSessn sessione) {
        boolean tmpReturn = true;
        StringWriter tmpStringWriter = new StringWriter();
        VersatoreType tmpVersatore = null;
        ChiaveType tmpChiave = null;
        RispostaControlli tmpControlli = new RispostaControlli();
        String tmpXmlEsito = null;
        String tmpVersioneXmlVers = null;
        String tmpVersioneXmlEsito = null;

        if (versamento.getVersamento() != null) {
            tmpVersatore = versamento.getVersamento().getIntestazione().getVersatore();
            tmpChiave = versamento.getVersamento().getIntestazione().getChiave();
        }

        try {
            Marshaller tmpMarshaller = xmlVersCache.getVersRespCtxforEsitoVersamento().createMarshaller();
            tmpMarshaller.marshal(rispostaWS.getIstanzaEsito(), tmpStringWriter);

            tmpXmlEsito = tmpStringWriter.toString();
        } catch (Exception ex) {
            tmpControlli.setCodErr("ERR");
            tmpControlli.setDsErr("Errore interno nella fase salvataggio sessione: " + ex.getMessage());
            log.error("Errore interno nella fase salvataggio sessione: ", ex);
            tmpReturn = false;
        }

        if (tmpReturn) {
            try {
                tmpVersioneXmlVers = rispostaWS.getIstanzaEsito().getVersioneXMLChiamata();
                tmpVersioneXmlEsito = rispostaWS.getIstanzaEsito().getVersione();
                if (!salvaSessione(versamento, rispostaWS, sessione, rispostaWS.getIstanzaEsito().getEsitoGenerale(),
                        tmpVersatore, tmpChiave, tmpXmlEsito, tmpVersioneXmlVers, tmpVersioneXmlEsito, null)) {
                    tmpControlli.setCodErr("ERR");
                    tmpControlli.setDsErr("Eccezione nella persistenza della sessione ");
                }
            } catch (Exception e) {
                tmpControlli.setCodErr("ERR");
                tmpControlli.setDsErr("Errore interno nella fase salvataggio sessione: " + e.getMessage());
                log.error("Errore interno nella fase salvataggio sessione: ", e);
            } finally {
                entityManager.clear();
            }
        }

        return tmpControlli;
    }

    public RispostaControlli salvaSessioneVersamento(VersamentoExtAggAll versamento, RispostaWSAggAll rispostaWS,
            SyncFakeSessn sessione) {
        boolean tmpReturn = true;
        StringWriter tmpStringWriter = new StringWriter();
        VersatoreType tmpVersatore = null;
        ChiaveType tmpChiave = null;
        RispostaControlli tmpControlli = new RispostaControlli();
        String tmpXmlEsito = null;
        String tmpVersioneXmlVers = null;
        String tmpVersioneXmlEsito = null;
        DocumentoVers tmpDocumentoVers = null;

        if (versamento.getVersamento() != null) {
            tmpVersatore = versamento.getVersamento().getIntestazione().getVersatore();
            tmpChiave = versamento.getVersamento().getIntestazione().getChiave();
        }

        /*
         * se è definito documentoVers, vuol dire che sto salvando i dati di aggiunta documento e che il parser ha
         * ricostruito abbastanza informazioni da recuperare i metadati del doc da versare...
         */
        if (versamento.getStrutturaComponenti() != null
                && versamento.getStrutturaComponenti().getDocumentiAttesi().size() > 0) {
            tmpDocumentoVers = versamento.getStrutturaComponenti().getDocumentiAttesi().get(0);
        }

        try {
            Marshaller tmpMarshaller = xmlVersCache.getVersRespCtxforEsitoVersamentoAggAllegati().createMarshaller();
            tmpMarshaller.marshal(rispostaWS.getIstanzaEsito(), tmpStringWriter);
            tmpXmlEsito = tmpStringWriter.toString();
        } catch (Exception ex) {
            tmpControlli.setCodErr("ERR");
            tmpControlli.setDsErr("Errore interno nella fase salvataggio sessione: " + ex.getMessage());
            log.error("Errore interno nella fase salvataggio sessione: ", ex);
            tmpReturn = false;
        }

        if (tmpReturn) {
            try {
                tmpVersioneXmlVers = rispostaWS.getIstanzaEsito().getVersioneXMLChiamata();
                tmpVersioneXmlEsito = rispostaWS.getIstanzaEsito().getVersione();
                if (!salvaSessione(versamento, rispostaWS, sessione, rispostaWS.getIstanzaEsito().getEsitoGenerale(),
                        tmpVersatore, tmpChiave, tmpXmlEsito, tmpVersioneXmlVers, tmpVersioneXmlEsito,
                        tmpDocumentoVers)) {
                    tmpControlli.setCodErr("ERR");
                    tmpControlli.setDsErr("Eccezione nella persistenza della sessione ");
                }
            } catch (Exception e) {
                tmpControlli.setCodErr("ERR");
                tmpControlli.setDsErr("Errore interno nella fase salvataggio sessione: " + e.getMessage());
                log.error("Errore interno nella fase salvataggio sessione: ", e);
            } finally {
                entityManager.clear();
            }
        }

        return tmpControlli;
    }

    private boolean salvaSessione(AbsVersamentoExt versamento, IRispostaVersWS rispostaWS, SyncFakeSessn sessione,
            ECEsitoGeneraleType esitoGen, VersatoreType versatore, ChiaveType chiave, String xmlEsito,
            String versioneXmlChiamata, String versioneXmlEsito, DocumentoVers documentoVersIn)
            throws NoSuchAlgorithmException, IOException {
        boolean tmpReturn = true;
        VrsSessioneVers tmpSessioneVer = new VrsSessioneVers();
        VrsDatiSessioneVers tmpDatiSessioneVers = new VrsDatiSessioneVers();
        VrsXmlDatiSessioneVers tmpXmlDatiSessioneVers = null;

        VrsErrSessioneVers tmpErrSessioneVers;
        VrsFileSessione tmpFileSessione;

        // calcola l'hash dell'esito del versamento.
        // lo calcolo in questo punto perchè non può più cambiare e in caso di errori da
        // qui in poi
        // non verrebbe salvato nulla.
        String hashXmlEsito = new HashCalculator().calculateHashSHAX(xmlEsito, TipiHash.SHA_256).toHexBinary();

        /*
         * salvo sessione
         */
        // questi dati li scrivo sempre
        tmpSessioneVer.setCdVersioneWs(sessione.getVersioneWS());
        tmpSessioneVer.setDtApertura(convert(sessione.getTmApertura()));
        tmpSessioneVer.setDtChiusura(convert(sessione.getTmChiusura()));
        tmpSessioneVer.setTsApertura(convert(sessione.getTmApertura()));
        tmpSessioneVer.setTsChiusura(convert(sessione.getTmChiusura()));
        tmpSessioneVer.setTiSessioneVers(sessione.getTipoSessioneVers().name()); // VERSAMENTO o AGGIUNTA
        tmpSessioneVer.setNmUseridWs(sessione.getLoginName());
        // salvo il nome del server/istanza nel cluster che sta salvando i dati e ha
        // gestito il versamento
        tmpSessioneVer.setCdIndServer(appServerInstance.getName());
        // salvo l'indirizzo IP del sistema che ha effettuato la richiesta di
        // versamento/aggiunta
        tmpSessioneVer.setCdIndIpClient(sessione.getIpChiamante());

        // questi dati li scrivo se l'XML ha superato il controllo formale e quindi sono
        // definiti i tag Versatore e
        // Chiave
        if (versatore != null && chiave != null) {
            tmpSessioneVer.setAaKeyUnitaDoc(new BigDecimal(chiave.getAnno()));
            tmpSessioneVer.setCdKeyUnitaDoc(chiave.getNumero());
            tmpSessioneVer.setCdRegistroKeyUnitaDoc(chiave.getTipoRegistro());
            tmpSessioneVer.setNmAmbiente(versatore.getAmbiente());
            tmpSessioneVer.setNmEnte(versatore.getEnte());
            tmpSessioneVer.setNmStrut(versatore.getStruttura());
            tmpSessioneVer.setNmUserid(versatore.getUserID()); // questo è l'utente definito nell'xml
            tmpSessioneVer.setNmUtente(versatore.getUtente()); // questo è l'utente definito nell'xml
        }

        /*
         * se è definito documentoVers, vuol dire che sto salvando i dati di aggiunta documento e che il parser ha
         * ricostruito abbastanza informazioni da recuperare i metadati del doc da versare...
         */
        if (documentoVersIn != null) {
            tmpSessioneVer.setCdKeyDocVers(documentoVersIn.getRifDocumento().getIDDocumento());
            /*
             * ...inoltre se è definito l'id del record versato e non ci sono errori precedenti, vuol dire che sono
             * riuscito ad effettuare il versamento e che devo salvare il riferimento al doc versato nella sessione
             */
            if (rispostaWS.getSeverity() != RispostaWS.SeverityEnum.ERROR) {
                tmpSessioneVer.setAroDoc(entityManager.find(AroDoc.class, documentoVersIn.getIdRecDocumentoDB()));
            }
        }

        // se ho trovato il codice dell'utente definito nell'XML, lo scrivo
        if (versamento.getStrutturaComponenti() != null && versamento.getStrutturaComponenti().getIdUser() != 0) {
            tmpSessioneVer
                    .setIamUser(entityManager.find(IamUser.class, versamento.getStrutturaComponenti().getIdUser()));
        }

        // se ho trovato il codice della struttura versante, lo scrivo
        if (versamento.getStrutturaComponenti() != null && versamento.getStrutturaComponenti().getIdStruttura() != 0) {
            tmpSessioneVer.setOrgStrut(
                    entityManager.find(OrgStrut.class, versamento.getStrutturaComponenti().getIdStruttura()));
        }

        // se c'è un ID per l'unità documentaria creata o aggiornata, lo scrivo
        if (versamento.getStrutturaComponenti() != null && versamento.getStrutturaComponenti().getIdUnitaDoc() != 0) {
            tmpSessioneVer.setAroUnitaDoc(
                    entityManager.find(AroUnitaDoc.class, versamento.getStrutturaComponenti().getIdUnitaDoc()));
        }

        // questi dati li scrivo se il WS è andato, nel complesso, bene.
        if (rispostaWS.getSeverity() != RispostaWS.SeverityEnum.ERROR) {
            tmpSessioneVer.setTiStatoSessioneVers(SESSIONE_CHIUSA_OK);
        } else {
            tmpSessioneVer.setTiStatoSessioneVers(SESSIONE_CHIUSA_ERR);
            tmpSessioneVer.setFlSessioneErrVerif("0"); // se è andata in errore, pongo a FALSE il flag di sessione
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
            tmpSessioneVer.setDsErrPrinc(tmpErrMess);
            tmpSessioneVer.setCdErrPrinc(esitoGen.getCodiceErrore());
        }

        try {
            entityManager.persist(tmpSessioneVer);
            entityManager.flush();
        } catch (RuntimeException re) {
            /// logga l'errore e blocca tutto
            log.error("Eccezione nella persistenza della sessione ", re);
            tmpReturn = false;
        }

        /*
         * Salvo i dati relativi all'Unità documentaria o al documento non versato, nel caso il versamento sia andato
         * male, ma siano comunque identificabili gli estremi di ciò che si voleva scrivere. In caso di errore per
         * elemento duplicato, non deve essere scritto nulla.
         */
        if (tmpReturn && rispostaWS.getSeverity() == RispostaWS.SeverityEnum.ERROR
                && (!rispostaWS.isErroreElementoDoppio()) && versamento.getStrutturaComponenti() != null
                && versamento.getStrutturaComponenti().getIdStruttura() != 0) {

            CSChiave tmpCSChiave = new CSChiave();
            tmpCSChiave.setAnno(Long.valueOf(chiave.getAnno()));
            tmpCSChiave.setNumero(chiave.getNumero());
            tmpCSChiave.setTipoRegistro(chiave.getTipoRegistro());

            String queryStr;
            javax.persistence.Query query;

            if (sessione.getTipoSessioneVers() == SyncFakeSessn.TipiSessioneVersamento.VERSAMENTO && this
                    .udNonVersataNonPresente(tmpCSChiave, versamento.getStrutturaComponenti().getIdStruttura())) {
                queryStr = "select al from  VrsUnitaDocNonVer al " + "where al.orgStrut.idStrut = :idStrutIn "
                        + "and al.aaKeyUnitaDoc = :aaKeyUnitaDocIn " + "and al.cdKeyUnitaDoc = :cdKeyUnitaDocIn "
                        + "and al.cdRegistroKeyUnitaDoc = :cdRegistroKeyUnitaDocIn ";
                query = entityManager.createQuery(queryStr);
                query.setParameter("idStrutIn", new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
                query.setParameter("cdRegistroKeyUnitaDocIn", tmpCSChiave.getTipoRegistro());
                query.setParameter("aaKeyUnitaDocIn", new BigDecimal(tmpCSChiave.getAnno()));
                query.setParameter("cdKeyUnitaDocIn", tmpCSChiave.getNumero());
                try {
                    List<VrsUnitaDocNonVer> udnvs = (List<VrsUnitaDocNonVer>) query.getResultList();
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
                        entityManager.flush();
                    } else {
                        VrsUnitaDocNonVer tmpUnitaDocNonVer = udnvs.get(0);
                        tmpUnitaDocNonVer.setDtLastSesErr(convert(sessione.getTmApertura()));
                        if (!tmpUnitaDocNonVer.getDsErrPrinc().equals(esitoGen.getMessaggioErrore())) {
                            tmpUnitaDocNonVer.setDsErrPrinc(CD_DS_ERR_DIVERSI);
                        }
                        if (!tmpUnitaDocNonVer.getCdErrPrinc().equals(esitoGen.getCodiceErrore())) {
                            tmpUnitaDocNonVer.setCdErrPrinc(CD_DS_ERR_DIVERSI);
                        }
                        entityManager.flush();
                    }
                } catch (RuntimeException re) {
                    /// logga l'errore e blocca tutto
                    log.error("Eccezione nella persistenza dell'unità documentaria NON versata ", re);
                    tmpReturn = false;
                }
            } else if (sessione.getTipoSessioneVers() == SyncFakeSessn.TipiSessioneVersamento.AGGIUNGI_DOCUMENTO
                    && documentoVersIn != null
                    && this.docNonVersatoNonPresente(tmpCSChiave, versamento.getStrutturaComponenti().getIdStruttura(),
                            documentoVersIn.getRifDocumento().getIDDocumento())) {
                queryStr = "select al from VrsDocNonVer al " + "where al.orgStrut.idStrut = :idStrutIn "
                        + "and al.aaKeyUnitaDoc = :aaKeyUnitaDocIn " + "and al.cdKeyUnitaDoc = :cdKeyUnitaDocIn "
                        + "and al.cdRegistroKeyUnitaDoc = :cdRegistroKeyUnitaDocIn "
                        + "and al.cdKeyDocVers = :cdKeyDocVersIn";
                query = entityManager.createQuery(queryStr);
                query.setParameter("idStrutIn", new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
                query.setParameter("cdRegistroKeyUnitaDocIn", tmpCSChiave.getTipoRegistro());
                query.setParameter("aaKeyUnitaDocIn", new BigDecimal(tmpCSChiave.getAnno()));
                query.setParameter("cdKeyUnitaDocIn", tmpCSChiave.getNumero());
                query.setParameter("cdKeyDocVersIn", documentoVersIn.getRifDocumento().getIDDocumento());
                try {
                    List<VrsDocNonVer> udnvs = (List<VrsDocNonVer>) query.getResultList();
                    if (udnvs.isEmpty()) {
                        VrsDocNonVer tmpDocNonVer = new VrsDocNonVer();
                        tmpDocNonVer.setAaKeyUnitaDoc(new BigDecimal(tmpCSChiave.getAnno()));
                        tmpDocNonVer.setCdKeyUnitaDoc(tmpCSChiave.getNumero());
                        tmpDocNonVer.setCdRegistroKeyUnitaDoc(tmpCSChiave.getTipoRegistro());
                        tmpDocNonVer.setCdKeyDocVers(documentoVersIn.getRifDocumento().getIDDocumento());
                        tmpDocNonVer.setDtFirstSesErr(convert(sessione.getTmApertura()));
                        tmpDocNonVer.setDtLastSesErr(convert(sessione.getTmApertura()));
                        tmpDocNonVer.setDsErrPrinc(esitoGen.getMessaggioErrore());
                        tmpDocNonVer.setCdErrPrinc(esitoGen.getCodiceErrore());
                        tmpDocNonVer.setOrgStrut(entityManager.find(OrgStrut.class,
                                versamento.getStrutturaComponenti().getIdStruttura()));
                        entityManager.persist(tmpDocNonVer);
                        entityManager.flush();
                    } else {
                        VrsDocNonVer tmpDocNonVer = udnvs.get(0);
                        tmpDocNonVer.setDtLastSesErr(convert(sessione.getTmApertura()));
                        if (!tmpDocNonVer.getDsErrPrinc().equals(esitoGen.getMessaggioErrore())) {
                            tmpDocNonVer.setDsErrPrinc(CD_DS_ERR_DIVERSI);
                        }
                        if (!tmpDocNonVer.getCdErrPrinc().equals(esitoGen.getCodiceErrore())) {
                            tmpDocNonVer.setCdErrPrinc(CD_DS_ERR_DIVERSI);
                        }
                        entityManager.flush();
                    }
                } catch (RuntimeException re) {
                    /// logga l'errore e blocca tutto
                    log.error("Eccezione nella persistenza del documento NON versato ", re);
                    tmpReturn = false;
                }
            }
        }

        /*
         * salva i dati di sessione
         */
        if (tmpReturn) {
            tmpDatiSessioneVers.setVrsSessioneVers(tmpSessioneVer);
            tmpDatiSessioneVers.setTiDatiSessioneVers(sessione.getTipoDatiSessioneVers());
            tmpDatiSessioneVers.setPgDatiSessioneVers(new BigDecimal(1));

            /*
             * scrivo il numero di file arrivati in ogni caso, non è necessaria la compatibilità con la versione
             * asincrona dal momento che il versamento asincrono è gestito con un applicativo esterno. Inoltre è stato
             * espressamente richiesto che i file delle sessioni errate siano sempre salvati
             */
            tmpDatiSessioneVers.setNiFile(new BigDecimal(sessione.getFileBinari().size()));

            // se ho trovato il codice della struttura versante, lo scrivo
            if (versamento.getStrutturaComponenti() != null
                    && versamento.getStrutturaComponenti().getIdStruttura() != 0) {
                tmpDatiSessioneVers.setIdStrut(new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
            }
            try {
                entityManager.persist(tmpDatiSessioneVers);
                entityManager.flush();
            } catch (RuntimeException re) {
                /// logga l'errore e blocca tutto
                log.error("Eccezione nella persistenza della sessione ", re);
                tmpReturn = false;
            }
        }

        /*
         * salva i dati xml di versamento
         */
        if (tmpReturn) {
            tmpXmlDatiSessioneVers = new VrsXmlDatiSessioneVers();
            tmpXmlDatiSessioneVers.setVrsDatiSessioneVers(tmpDatiSessioneVers);
            tmpXmlDatiSessioneVers.setTiXmlDati(TipiXmlDati.RICHIESTA);
            // se ho trovato il codice della struttura versante, lo scrivo
            if (versamento.getStrutturaComponenti() != null
                    && versamento.getStrutturaComponenti().getIdStruttura() != 0) {
                tmpXmlDatiSessioneVers.setIdStrut(new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
            }
            tmpXmlDatiSessioneVers.setFlCanonicalized(CostantiDB.Flag.TRUE);
            tmpXmlDatiSessioneVers.setBlXml(
                    sessione.getDatiDaSalvareIndiceSip().length() == 0 ? "--" : sessione.getDatiDaSalvareIndiceSip());
            tmpXmlDatiSessioneVers.setCdVersioneXml(versioneXmlChiamata);
            if (sessione.getUrnIndiceSipXml() != null) {
                tmpXmlDatiSessioneVers.setDsUrnXmlVers(sessione.getUrnIndiceSipXml());
                tmpXmlDatiSessioneVers.setDsHashXmlVers(sessione.getHashIndiceSipXml());
                tmpXmlDatiSessioneVers.setCdEncodingHashXmlVers(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi());
                // tmpXmlDatiSessioneVers.setDsAlgoHashXmlVers(CostantiDB.TipiHash.SHA_1.descrivi());
                tmpXmlDatiSessioneVers.setDsAlgoHashXmlVers(CostantiDB.TipiHash.SHA_256.descrivi());
            }
            try {
                entityManager.persist(tmpXmlDatiSessioneVers);
                entityManager.flush();
            } catch (RuntimeException re) {
                /// logga l'errore e blocca tutto
                log.error("Eccezione nella persistenza della sessione ", re);
                tmpReturn = false;
            }
        }

        /*
         * calcolo URN
         */
        // EVO#16486
        String tmpUrn = null;
        String tmpUrnNorm = null;
        if (versamento.getStrutturaComponenti() != null
                && sessione.getTipoSessioneVers() == SyncFakeSessn.TipiSessioneVersamento.VERSAMENTO) {
            // calcolo parte urn ORIGINALE
            if (StringUtils.isNotBlank(versamento.getStrutturaComponenti().getUrnPartVersatore())
                    && StringUtils.isNotBlank(versamento.getStrutturaComponenti().getUrnPartChiaveUd())) {
                //
                tmpUrn = MessaggiWSFormat.formattaBaseUrnUnitaDoc(
                        versamento.getStrutturaComponenti().getUrnPartVersatore(),
                        versamento.getStrutturaComponenti().getUrnPartChiaveUd());
            }
            // calcolo parte urn NORMALIZZATO
            if (StringUtils.isNotBlank(versamento.getStrutturaComponenti().getUrnPartVersatoreNormalized())
                    && StringUtils.isNotBlank(versamento.getStrutturaComponenti().getUrnPartChiaveUdNormalized())) {
                //
                tmpUrnNorm = MessaggiWSFormat.formattaBaseUrnUnitaDoc(
                        versamento.getStrutturaComponenti().getUrnPartVersatoreNormalized(),
                        versamento.getStrutturaComponenti().getUrnPartChiaveUdNormalized());
            }
        } else if (documentoVersIn != null
                && sessione.getTipoSessioneVers() == SyncFakeSessn.TipiSessioneVersamento.AGGIUNGI_DOCUMENTO) {
            // calcolo parte urn ORIGINALE
            String tmpUrnPartDoc = null;
            // DOCXXXXXX
            if (documentoVersIn.getNiOrdDoc() != 0) {
                tmpUrnPartDoc = MessaggiWSFormat.formattaUrnPartDocumento(Costanti.CategoriaDocumento.Documento,
                        documentoVersIn.getNiOrdDoc(), true, Costanti.UrnFormatter.DOC_FMT_STRING_V2,
                        Costanti.UrnFormatter.PAD5DIGITS_FMT);
            }

            if (StringUtils.isNotBlank(tmpUrnPartDoc)
                    && StringUtils.isNotBlank(versamento.getStrutturaComponenti().getUrnPartVersatore())
                    && StringUtils.isNotBlank(versamento.getStrutturaComponenti().getUrnPartChiaveUd())) {
                //
                tmpUrn = MessaggiWSFormat.formattaBaseUrnDoc(versamento.getStrutturaComponenti().getUrnPartVersatore(),
                        versamento.getStrutturaComponenti().getUrnPartChiaveUd(), tmpUrnPartDoc,
                        Costanti.UrnFormatter.URN_DOC_FMT_STRING_V2);
            }

            // calcolo urn NORMALIZZATO
            if (StringUtils.isNotBlank(tmpUrnPartDoc)
                    && StringUtils.isNotBlank(versamento.getStrutturaComponenti().getUrnPartVersatoreNormalized())
                    && StringUtils.isNotBlank(versamento.getStrutturaComponenti().getUrnPartChiaveUdNormalized())) {
                //
                tmpUrnNorm = MessaggiWSFormat.formattaBaseUrnDoc(
                        versamento.getStrutturaComponenti().getUrnPartVersatoreNormalized(),
                        versamento.getStrutturaComponenti().getUrnPartChiaveUdNormalized(), tmpUrnPartDoc,
                        Costanti.UrnFormatter.URN_DOC_FMT_STRING_V2);
            }
        }
        // end EVO#16486

        /*
         * salva i dati dei nuovi URN su VrsUrnXmlSessioneVers
         */
        if (tmpReturn) {
            if (tmpUrn != null) {
                // salvo ORIGINALE
                tmpReturn = this.salvaUrnXmlSessioneVers(tmpXmlDatiSessioneVers,
                        MessaggiWSFormat.formattaUrnIndiceSip(tmpUrn, Costanti.UrnFormatter.URN_INDICE_SIP_V2),
                        TiUrnXmlSessioneVers.ORIGINALE);
            }
            if (tmpUrnNorm != null) {
                // salvo NORMALIZZATO
                tmpReturn = this.salvaUrnXmlSessioneVers(tmpXmlDatiSessioneVers,
                        MessaggiWSFormat.formattaUrnIndiceSip(tmpUrnNorm, Costanti.UrnFormatter.URN_INDICE_SIP_V2),
                        TiUrnXmlSessioneVers.NORMALIZZATO);
            }
            if (tmpXmlDatiSessioneVers.getDsUrnXmlVers() != null) {
                // salvo INIZIALE
                tmpReturn = this.salvaUrnXmlSessioneVers(tmpXmlDatiSessioneVers,
                        tmpXmlDatiSessioneVers.getDsUrnXmlVers(), TiUrnXmlSessioneVers.INIZIALE);
            }
        }

        /*
         * salva i dati xml di esito
         */
        if (tmpReturn) {
            tmpXmlDatiSessioneVers = new VrsXmlDatiSessioneVers();
            tmpXmlDatiSessioneVers.setVrsDatiSessioneVers(tmpDatiSessioneVers);
            tmpXmlDatiSessioneVers.setTiXmlDati(TipiXmlDati.RISPOSTA);
            // se ho trovato il codice della struttura versante, lo scrivo
            if (versamento.getStrutturaComponenti() != null
                    && versamento.getStrutturaComponenti().getIdStruttura() != 0) {
                tmpXmlDatiSessioneVers.setIdStrut(new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
            }
            tmpXmlDatiSessioneVers.setFlCanonicalized(CostantiDB.Flag.FALSE);
            tmpXmlDatiSessioneVers.setBlXml(xmlEsito.length() == 0 ? "--" : xmlEsito);
            tmpXmlDatiSessioneVers.setCdVersioneXml(versioneXmlEsito);
            if (sessione.getUrnEsitoVersamento() != null) {
                tmpXmlDatiSessioneVers.setDsUrnXmlVers(sessione.getUrnEsitoVersamento());
                tmpXmlDatiSessioneVers.setDsHashXmlVers(hashXmlEsito);
                tmpXmlDatiSessioneVers.setCdEncodingHashXmlVers(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi());
                // tmpXmlDatiSessioneVers.setDsAlgoHashXmlVers(CostantiDB.TipiHash.SHA_1.descrivi());
                tmpXmlDatiSessioneVers.setDsAlgoHashXmlVers(CostantiDB.TipiHash.SHA_256.descrivi());
            }
            try {
                entityManager.persist(tmpXmlDatiSessioneVers);
                entityManager.flush();
            } catch (RuntimeException re) {
                /// logga l'errore e blocca tutto
                log.error("Eccezione nella persistenza della sessione ", re);
                tmpReturn = false;
            }
        }

        if (tmpReturn) {
            if (tmpUrn != null) {
                // salvo ORIGINALE
                tmpReturn = this.salvaUrnXmlSessioneVers(tmpXmlDatiSessioneVers,
                        MessaggiWSFormat.formattaUrnEsitoVers(tmpUrn, Costanti.UrnFormatter.URN_ESITO_VERS_V2),
                        TiUrnXmlSessioneVers.ORIGINALE);
            }
            if (tmpUrnNorm != null) {
                // salvo NORMALIZZATO
                tmpReturn = this.salvaUrnXmlSessioneVers(tmpXmlDatiSessioneVers,
                        MessaggiWSFormat.formattaUrnEsitoVers(tmpUrnNorm, Costanti.UrnFormatter.URN_ESITO_VERS_V2),
                        TiUrnXmlSessioneVers.NORMALIZZATO);
            }
            if (tmpXmlDatiSessioneVers.getDsUrnXmlVers() != null) {
                // salvo INIZIALE
                tmpReturn = this.salvaUrnXmlSessioneVers(tmpXmlDatiSessioneVers,
                        tmpXmlDatiSessioneVers.getDsUrnXmlVers(), TiUrnXmlSessioneVers.INIZIALE);
            }
        }

        /*
         * se sono presenti, salva i dati xml dell'indice MM (solo per VersamentoMM)
         */
        if (tmpReturn && sessione.getDatiPackInfoSipXml() != null) {
            tmpXmlDatiSessioneVers = new VrsXmlDatiSessioneVers();
            tmpXmlDatiSessioneVers.setVrsDatiSessioneVers(tmpDatiSessioneVers);
            tmpXmlDatiSessioneVers.setTiXmlDati(TipiXmlDati.INDICE_FILE);
            // se ho trovato il codice della struttura versante, lo scrivo
            if (versamento.getStrutturaComponenti() != null
                    && versamento.getStrutturaComponenti().getIdStruttura() != 0) {
                tmpXmlDatiSessioneVers.setIdStrut(new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
            }
            tmpXmlDatiSessioneVers.setFlCanonicalized(CostantiDB.Flag.FALSE);
            tmpXmlDatiSessioneVers
                    .setBlXml(sessione.getDatiPackInfoSipXml().length() == 0 ? "--" : sessione.getDatiPackInfoSipXml());
            tmpXmlDatiSessioneVers.setCdVersioneXml(versioneXmlChiamata);
            if (sessione.getUrnPackInfoSipXml() != null) {
                tmpXmlDatiSessioneVers.setDsUrnXmlVers(sessione.getUrnPackInfoSipXml());
                tmpXmlDatiSessioneVers.setDsHashXmlVers(sessione.getHashPackInfoSipXml());
                tmpXmlDatiSessioneVers.setCdEncodingHashXmlVers(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi());
                // tmpXmlDatiSessioneVers.setDsAlgoHashXmlVers(CostantiDB.TipiHash.SHA_1.descrivi());
                tmpXmlDatiSessioneVers.setDsAlgoHashXmlVers(CostantiDB.TipiHash.SHA_256.descrivi());
            }
            try {
                entityManager.persist(tmpXmlDatiSessioneVers);
                entityManager.flush();
            } catch (RuntimeException re) {
                /// logga l'errore e blocca tutto
                log.error("Eccezione nella persistenza della sessione ", re);
                tmpReturn = false;
            }

            if (tmpReturn) {
                if (tmpUrn != null) {
                    // salvo ORIGINALE
                    tmpReturn = this.salvaUrnXmlSessioneVers(tmpXmlDatiSessioneVers,
                            MessaggiWSFormat.formattaUrnEsitoVers(tmpUrn, Costanti.UrnFormatter.URN_PI_SIP_V2),
                            TiUrnXmlSessioneVers.ORIGINALE);
                }
                if (tmpUrnNorm != null) {
                    // salvo NORMALIZZATO
                    tmpReturn = this.salvaUrnXmlSessioneVers(tmpXmlDatiSessioneVers,
                            MessaggiWSFormat.formattaUrnEsitoVers(tmpUrnNorm, Costanti.UrnFormatter.URN_PI_SIP_V2),
                            TiUrnXmlSessioneVers.NORMALIZZATO);
                }
                if (tmpXmlDatiSessioneVers.getDsUrnXmlVers() != null) {
                    // salvo INIZIALE
                    tmpReturn = this.salvaUrnXmlSessioneVers(tmpXmlDatiSessioneVers,
                            tmpXmlDatiSessioneVers.getDsUrnXmlVers(), TiUrnXmlSessioneVers.INIZIALE);
                }
            }
        }

        /*
         * se sono presenti, salva i dati xml del Rapporto di versamento
         */
        // questi dati li scrivo se il WS è andato, nel complesso, bene.
        if (rispostaWS.getSeverity() != RispostaWS.SeverityEnum.ERROR) {
            if (tmpReturn && sessione.getDatiRapportoVersamento() != null) {
                tmpXmlDatiSessioneVers = new VrsXmlDatiSessioneVers();
                tmpXmlDatiSessioneVers.setVrsDatiSessioneVers(tmpDatiSessioneVers);
                tmpXmlDatiSessioneVers.setTiXmlDati(TipiXmlDati.RAPP_VERS);
                // se ho trovato il codice della struttura versante, lo scrivo
                if (versamento.getStrutturaComponenti() != null
                        && versamento.getStrutturaComponenti().getIdStruttura() != 0) {
                    tmpXmlDatiSessioneVers
                            .setIdStrut(new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
                }
                tmpXmlDatiSessioneVers.setFlCanonicalized(CostantiDB.Flag.FALSE);
                tmpXmlDatiSessioneVers.setBlXml(sessione.getDatiRapportoVersamento().length() == 0 ? "--"
                        : sessione.getDatiRapportoVersamento());
                tmpXmlDatiSessioneVers.setCdVersioneXml(Costanti.XML_RAPPORTO_VERS_VRSN);
                if (sessione.getUrnRapportoVersamento() != null) {
                    tmpXmlDatiSessioneVers.setDsUrnXmlVers(sessione.getUrnRapportoVersamento());
                    tmpXmlDatiSessioneVers.setDsHashXmlVers(sessione.getHashRapportoVersamento());
                    tmpXmlDatiSessioneVers.setCdEncodingHashXmlVers(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi());
                    // tmpXmlDatiSessioneVers.setDsAlgoHashXmlVers(CostantiDB.TipiHash.SHA_1.descrivi());
                    tmpXmlDatiSessioneVers.setDsAlgoHashXmlVers(CostantiDB.TipiHash.SHA_256.descrivi());
                }
                try {
                    entityManager.persist(tmpXmlDatiSessioneVers);
                    entityManager.flush();
                } catch (RuntimeException re) {
                    /// logga l'errore e blocca tutto
                    log.error("Eccezione nella persistenza della sessione ", re);
                    tmpReturn = false;
                }
            }

            if (tmpReturn) {
                if (tmpUrn != null) {
                    // salvo ORIGINALE
                    tmpReturn = this.salvaUrnXmlSessioneVers(tmpXmlDatiSessioneVers,
                            MessaggiWSFormat.formattaUrnEsitoVers(tmpUrn, Costanti.UrnFormatter.URN_RAPP_VERS_V2),
                            TiUrnXmlSessioneVers.ORIGINALE);
                }
                if (tmpUrnNorm != null) {
                    // salvo NORMALIZZATO
                    tmpReturn = this.salvaUrnXmlSessioneVers(tmpXmlDatiSessioneVers,
                            MessaggiWSFormat.formattaUrnEsitoVers(tmpUrnNorm, Costanti.UrnFormatter.URN_RAPP_VERS_V2),
                            TiUrnXmlSessioneVers.NORMALIZZATO);
                }
                if (tmpXmlDatiSessioneVers.getDsUrnXmlVers() != null) {
                    // salvo INIZIALE
                    tmpReturn = this.salvaUrnXmlSessioneVers(tmpXmlDatiSessioneVers,
                            tmpXmlDatiSessioneVers.getDsUrnXmlVers(), TiUrnXmlSessioneVers.INIZIALE);
                }
            }
        }

        /*
         * se sono presenti, salva i dati xml dei Profili UD/Comp
         */
        if (rispostaWS.getSeverity() != RispostaWS.SeverityEnum.ERROR && versamento.getStrutturaComponenti() != null
                && versamento.getStrutturaComponenti().hasXsdProfile()) {
            // se presente il profilo normativo su unità documentaria
            if (versamento.getStrutturaComponenti().getIdRecUsoXsdProfiloNormativo() != null) {
                tmpReturn = salvaXmlModelloProfiloNormativoUniDoc(versamento.getStrutturaComponenti().getIdStruttura(),
                        versamento.getStrutturaComponenti().getIdRecUsoXsdProfiloNormativo().longValue(),
                        versamento.getStrutturaComponenti().getDatiC14NProfNormXml(), tmpDatiSessioneVers);
            }
            /*
             * Possibili future estensioni per nuovi profili su unidoc.....
             */
        }
        /*
         * salva i dati relativi agli errori
         */
        if (tmpReturn) {
            if (rispostaWS.getSeverity() != RispostaWS.SeverityEnum.OK) {
                int progErrore = 1;
                String tmpErrMess;
                for (VoceDiErrore tmpVoceDiErrore : versamento.getErroriTrovati()) {
                    tmpErrSessioneVers = new VrsErrSessioneVers();
                    tmpErrSessioneVers.setVrsDatiSessioneVers(tmpDatiSessioneVers);
                    // se ho trovato il codice della struttura versante, lo scrivo
                    if (versamento.getStrutturaComponenti() != null
                            && versamento.getStrutturaComponenti().getIdStruttura() != 0) {
                        tmpErrSessioneVers
                                .setIdStrut(new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
                    }

                    if (tmpVoceDiErrore.getErrorMessage().isEmpty()) {
                        tmpErrMess = "(vuoto)";
                    } else {
                        tmpErrMess = tmpVoceDiErrore.getErrorMessage();
                        if (tmpErrMess.length() > DS_ERR_MAX_LEN) {
                            tmpErrMess = tmpErrMess.substring(0, DS_ERR_MAX_LEN);
                        }
                    }
                    tmpErrSessioneVers.setDsErr(tmpErrMess);

                    if (tmpVoceDiErrore.getSeverity() == RispostaWS.SeverityEnum.ERROR) {
                        tmpErrSessioneVers.setTiErr(TIPO_ERR_FATALE);
                    } else {
                        tmpErrSessioneVers.setTiErr(TIPO_ERR_WARNING);
                    }

                    tmpErrSessioneVers.setPgErrSessioneVers(new BigDecimal(progErrore));
                    progErrore++;

                    tmpErrSessioneVers.setCdErr(tmpVoceDiErrore.getErrorCode());
                    tmpErrSessioneVers.setFlErrPrinc(tmpVoceDiErrore.isElementoPrincipale() ? "1" : "0");
                    try {
                        entityManager.persist(tmpErrSessioneVers);
                        entityManager.flush();
                    } catch (RuntimeException re) {
                        /// logga l'errore e blocca tutto
                        log.error("Eccezione nella persistenza della sessione ", re);
                        tmpReturn = false;
                        break;
                    }
                }
            }
        }

        /*
         * se sono ancora in grado di scrivere, salvo i file. --- --- --- --- --- --- --- --- --- --- --- --- --- ---
         * --- --- --- --- NOTA: il salvataggio dei file andati in errore avviene solo nel caso di persistenza degli
         * stessi su BLOB oppure il tipo salvataggio non sia stato determinato. Nel caso i file siano da versare sul
         * filesystem (e successivamente su nastro, via Tivoli), i file vengono perduti. Vogliamo evitare di salvare su
         * blob file potenzialmente enormi.
         */
        long contaFileScritti = 0;
        if (tmpReturn && (versamento.getStrutturaComponenti() == null)
                || (versamento.getStrutturaComponenti() != null && versamento.getStrutturaComponenti()
                        .getTipoSalvataggioFile() != CostantiDB.TipoSalvataggioFile.FILE)) {
            long progressivoFile = 0;
            for (FileBinario tmpFb : sessione.getFileBinari()) {
                progressivoFile++;
                tmpFileSessione = new VrsFileSessione();
                tmpFileSessione.setVrsDatiSessioneVers(tmpDatiSessioneVers);
                if (versamento.getStrutturaComponenti() != null
                        && versamento.getStrutturaComponenti().getIdStruttura() != 0) {
                    tmpFileSessione.setIdStrut(new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
                }
                tmpFileSessione.setNmFileSessione(tmpFb.getId());
                tmpFileSessione.setPgFileSessione(new BigDecimal(progressivoFile));
                tmpFileSessione.setTiStatoFileSessione("CONTR_FATTI"); // nota, questo campo è del tutto inutile.
                try {
                    if (tmpReturn) {
                        entityManager.persist(tmpFileSessione);
                        entityManager.flush();
                    }
                } catch (RuntimeException re) {
                    /// logga l'errore e blocca tutto
                    log.error("Eccezione nella persistenza della sessione ", re);
                    tmpReturn = false;
                }

                /*
                 * i file li memorizzo in questa tabella, ma solo se si è verificato un errore diverso da
                 * "l'elemento corrisponde ad uno già presente nel sistema". dal momento che non ci interessa tenere due
                 * copie di un file che abbiamo già
                 */
                if (tmpReturn && rispostaWS.getSeverity() == RispostaWS.SeverityEnum.ERROR
                        && (!rispostaWS.isErroreElementoDoppio())) {
                    // procedo alla memorizzazione del file sul blob, via JDBC
                    WriteCompBlbOracle.DatiAccessori datiAccessori = new WriteCompBlbOracle().new DatiAccessori();
                    datiAccessori.setTabellaBlob(WriteCompBlbOracle.TabellaBlob.VRS_CONTENUTO_FILE);
                    datiAccessori.setIdPadre(tmpFileSessione.getIdFileSessione());
                    if (versamento.getStrutturaComponenti() != null
                            && versamento.getStrutturaComponenti().getIdStruttura() != 0) {
                        datiAccessori.setIdStruttura(versamento.getStrutturaComponenti().getIdStruttura());
                    }
                    datiAccessori.setDtVersamento(sessione.getTmApertura());

                    try {
                        RispostaControlli tmpControlli = writeCompBlbOracle.salvaStreamSuBlobComp(datiAccessori, tmpFb);
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
        if (tmpReturn && rispostaWS.getSeverity() == RispostaWS.SeverityEnum.ERROR) {
            tmpSessioneVer = entityManager.find(VrsSessioneVers.class, tmpSessioneVer.getIdSessioneVers());
            tmpSessioneVer.setNiFileErr(new BigDecimal(contaFileScritti));
            entityManager.flush();
        }

        return tmpReturn;
    }

    /*
     * Salvataggio XML profilo normativo UniDoc
     */
    private boolean salvaXmlModelloProfiloNormativoUniDoc(long idStrut, long idRecUsoXsdProfiloNormativo, String xml,
            VrsDatiSessioneVers tmpDatiSessioneVers) {
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
        if (rc.getrLong() == -1) {
            return true;
        }
        return false;
    }

    private boolean docNonVersatoNonPresente(CSChiave chiave, long idStruttura, String idDocumento) {

        RispostaControlli rc = controlliSemantici.checkChiave(chiave, idStruttura,
                ControlliSemantici.TipiGestioneUDAnnullate.CONSIDERA_ASSENTE);
        if (rc.getrLong() == -1) {
            return true;
        }
        long idUd = rc.getrLong();
        rc = controlliSemantici.checkDocumentoInUd(idUd, idDocumento, "DUMMY");
        if (rc.isrBoolean() == true) {
            return true;
        }
        return false;
    }

    private boolean salvaUrnXmlSessioneVers(VrsXmlDatiSessioneVers xmlDatiSessioneVers, String tmpUrn,
            TiUrnXmlSessioneVers tiUrn) {
        boolean tmpReturn = true;

        VrsUrnXmlSessioneVers tmpVrsUrnXmlSessioneVers = new VrsUrnXmlSessioneVers();
        tmpVrsUrnXmlSessioneVers.setDsUrn(tmpUrn);
        tmpVrsUrnXmlSessioneVers.setTiUrn(tiUrn);
        tmpVrsUrnXmlSessioneVers.setVrsXmlDatiSessioneVers(xmlDatiSessioneVers);

        try {
            // persist
            entityManager.persist(tmpVrsUrnXmlSessioneVers);
            entityManager.flush();
        } catch (RuntimeException re) {
            // logga l'errore e blocca tutto
            log.error("Eccezione nella persistenza della sessione urn xml ", re);
            tmpReturn = false;
        }

        xmlDatiSessioneVers.getVrsUrnXmlSessioneVers().add(tmpVrsUrnXmlSessioneVers);

        return tmpReturn;
    }

}
