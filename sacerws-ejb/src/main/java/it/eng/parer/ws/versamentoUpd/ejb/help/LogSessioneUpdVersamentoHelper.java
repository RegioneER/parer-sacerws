package it.eng.parer.ws.versamentoUpd.ejb.help;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.AroUpdUnitaDoc;
import it.eng.parer.entity.AroXmlUpdUnitaDoc;
import it.eng.parer.entity.DecControlloWs;
import it.eng.parer.entity.DecErrSacer;
import it.eng.parer.entity.DecRegistroUnitaDoc;
import it.eng.parer.entity.DecTipoDoc;
import it.eng.parer.entity.DecTipoUnitaDoc;
import it.eng.parer.entity.IamUser;
import it.eng.parer.entity.MonContaSesUpdUdKo;
import it.eng.parer.entity.MonKeyTotalUd;
import it.eng.parer.entity.MonKeyTotalUdKo;
import it.eng.parer.entity.OrgStrut;
import it.eng.parer.entity.VrsErrSesUpdUnitaDocErr;
import it.eng.parer.entity.VrsErrSesUpdUnitaDocKo;
import it.eng.parer.entity.VrsSesUpdUnitaDocErr;
import it.eng.parer.entity.VrsSesUpdUnitaDocKo;
import it.eng.parer.entity.VrsUpdUnitaDocKo;
import it.eng.parer.entity.VrsXmlSesUpdUnitaDocErr;
import it.eng.parer.entity.VrsXmlSesUpdUnitaDocKo;
import it.eng.parer.entity.constraint.MonContaSesUpdUdKo.TiStatoUdpUdKoMonContaSesUpdUdKo;
import it.eng.parer.entity.constraint.VrsErrSesUpdUnitaDocErr.TiErrVrsErrSesUpdUnitaDocErr;
import it.eng.parer.entity.constraint.VrsErrUpdUnitaDocKo.TiErrVrsErrUpdUnitaDocKo;
import it.eng.parer.entity.constraint.VrsSesUpdUnitaDocErr.TiStatoSesVrsSesUpdUnitaDocErr;
import it.eng.parer.entity.constraint.VrsSesUpdUnitaDocKo.TiStatoSesUpdKo;
import it.eng.parer.entity.constraint.VrsUpdUnitaDocKo.TiStatoUdpUdKo;
import it.eng.parer.entity.constraint.VrsXmlSesUpdUnitaDocErr.TiXmlVrsXmlSesUpdUnitaDocErr;
import it.eng.parer.entity.constraint.VrsXmlSesUpdUnitaDocKo.TiXmlVrsXmlSesUpdUnitaDocKo;
import it.eng.parer.util.ejb.AppServerInstance;
import it.eng.parer.viewEntity.LogVVisLastSched;
import it.eng.parer.viewEntity.VrsVModifUpdUdKo;
import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.IRispostaWS.StatiSessioneVersEnum;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.XmlUpdVersCache;
import it.eng.parer.ws.utils.ControlliWSHelper;
import it.eng.parer.ws.utils.LogSessioneUtils;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSHelper;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.versamentoUpd.dto.CompRapportoUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.ControlloEseguito;
import it.eng.parer.ws.versamentoUpd.dto.RispostaWSUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.UpdComponenteVers;
import it.eng.parer.ws.versamentoUpd.dto.UpdDocumentoVers;
import it.eng.parer.ws.versamentoUpd.dto.UpdUnitaDocColl;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import static it.eng.parer.util.DateUtilsConverter.convert;

/**
 *
 * @author sinatti_s
 */
@Stateless(mappedName = "LogSessioneUpdVersamentoHelper")
@LocalBean
public class LogSessioneUpdVersamentoHelper {

    private static final Logger log = LoggerFactory.getLogger(LogSessioneUpdVersamentoHelper.class);
    //

    @EJB
    private MessaggiWSHelper messaggiWSHelper;

    @EJB
    private ControlliWSHelper controlliWSHelper;

    @EJB
    private XmlUpdVersCache xmlUpdVersCache;

    @EJB
    private AppServerInstance appServerInstance;

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    private final static String CD_DS_ERR_DIVERSI = "Diversi";

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli verificaPartizioneUpdErr() {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);
        long conta = 0;
        try {
            String queryStr = "select count(t) from OrgVChkPartitionUpdErr t "
                    + "where t.flPartSesupderrOk = :flPartSesupderrOk ";
            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("flPartSesupderrOk", "1");
            conta = (Long) query.getSingleResult();

            if (conta == 0) {
                rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                        "La tabella delle sessioni errate non è correttamente partizionata. "
                                + "Impossibile salvare aggiornamento di versamento in errore"));
                return rispostaControlli;
            }
            rispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "UpdLogSessioneHelper.verificaPartizioneUpdErr: " + e.getMessage()));
            log.error("Eccezione nella lettura  della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli verificaPartizioneUpdByAaStrutKo(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        long conta = 0;
        try {
            String queryStr = "select count(t) from OrgVChkPartitionUpdByAa t "
                    + "where t.idStrut = :idStrut and t.anno = :anno "
                    + "and t.flPartSesupdkoAaOk = :flPartSesupdkoAaOk " + "and t.flPartSesupdkoOk = :flPartSesupdkoOk "
                    + "and t.flPartUpddatispecAaOk = :flPartUpddatispecAaOk "
                    + "and t.flPartUpddatispecOk = :flPartUpddatispecOk " + "and t.flPartUpdkoAaOk = :flPartUpdkoAaOk "
                    + "and t.flPartUpdkoOk = :flPartUpdkoOk "
                    + "and t.flPartVersinidatispecAaOk = :flPartVersinidatispecAaOk "
                    + "and t.flPartVersinidatispecOk = :flPartVersinidatispecOk "
                    + "and t.flPartXmlsesupdkoOk = :flPartXmlsesupdkoOk "
                    + "and t.flPartXmlsesupdkpAaOk = :flPartXmlsesupdkpAaOk "
                    + "and t.flPartXmlupdAaOk = :flPartXmlupdAaOk " + "and t.flPartXmlupdOk = :flPartXmlupdOk ";

            javax.persistence.Query query = entityManager.createQuery(queryStr);

            query.setParameter("idStrut", strutturaUpdVers.getIdStruttura());
            query.setParameter("anno", strutturaUpdVers.getChiaveNonVerificata().getAnno());

            query.setParameter("flPartSesupdkoAaOk", "1");
            query.setParameter("flPartSesupdkoOk", "1");
            query.setParameter("flPartUpddatispecAaOk", "1");
            query.setParameter("flPartUpddatispecOk", "1");
            query.setParameter("flPartUpdkoAaOk", "1");
            query.setParameter("flPartUpdkoOk", "1");
            query.setParameter("flPartVersinidatispecAaOk", "1");
            query.setParameter("flPartVersinidatispecOk", "1");
            query.setParameter("flPartXmlsesupdkoOk", "1");
            query.setParameter("flPartXmlsesupdkpAaOk", "1");
            query.setParameter("flPartXmlupdAaOk", "1");
            query.setParameter("flPartXmlupdOk", "1");

            conta = (Long) query.getSingleResult();

            if (conta == 0) {
                rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                        "La tabella delle sessioni fallite non è correttamente partizionata. "
                                + "Impossibile salvare l'aggiornamento fallito"));
                return rispostaControlli;
            }
            rispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "UpdLogSessioneHelper.verificaPartizioneUpdByAaStrutKo: " + e.getMessage()));
            log.error("Eccezione nella lettura  della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli recuperoUpdUnitaDocOk(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);
        rispostaControlli.setrLong(-1);

        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        List<AroUpdUnitaDoc> aroUpdUnitaDocs = null;
        try {
            String queryStr = "select t from AroUpdUnitaDoc t " + "where t.aroUnitaDoc.idUnitaDoc = :idUnitaDoc "
                    + " and t.tsIniSes > :dataDiOggiIn " // aggiornamento successivo alla data in cui si sta registrando
                    // la sessione FALLITA
                    + " order by t.pgUpdUnitaDoc DESC "; // ordinamento per progressivo al fine di
            // "pescare" l'ultimo aggiornamento registrato (se esiste)

            javax.persistence.Query query = entityManager.createQuery(queryStr, AroUpdUnitaDoc.class);
            query.setParameter("idUnitaDoc", strutturaUpdVers.getIdUd());
            query.setParameter("dataDiOggiIn", convert(sessione.getTmApertura()));

            aroUpdUnitaDocs = query.getResultList();

            if (aroUpdUnitaDocs.size() >= 1) {
                // se trovato, recupero il risultato che interessa per la verifica
                rispostaControlli.setrLong(0);

                AroUpdUnitaDoc last = aroUpdUnitaDocs.get(0);
                rispostaControlli.setrObject(last);
            }
            rispostaControlli.setrBoolean(true);// query is good
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "UpdLogSessioneHelper.recuperoUpdUnitaDocOk: " + e.getMessage()));
            log.error("Eccezione nella lettura  della tabella di aggiornamento unità documentaria ", e);
        }

        return rispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviUpdUnitaDocErr(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        // salvo sessione errata
        tmpRispostaControlli.setrBoolean(false);
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();

        try {
            VrsSesUpdUnitaDocErr tmpUpdUnitaDocErr = new VrsSesUpdUnitaDocErr();
            tmpUpdUnitaDocErr.setCdVersioneWs(sessione.getVersioneWS());
            tmpUpdUnitaDocErr.setTsIniSes(convert(sessione.getTmApertura()));
            tmpUpdUnitaDocErr.setTsFineSes(convert(sessione.getTmChiusura()));
            tmpUpdUnitaDocErr.setNmUseridWs(sessione.getLoginName());
            // salvo il nome del server/istanza nel cluster che sta salvando i dati e ha
            // gestito il versamento
            // tmpUpdUnitaDocErr.setCdIndServer(appServerInstance.getName());
            // salvo l'indirizzo IP del sistema che ha effettuato la richiesta di
            // versamento/aggiunta
            // tmpUpdUnitaDocErr.setCdIndIpClient(sessione.getIpChiamante());
            tmpUpdUnitaDocErr.setTiStatoSes(TiStatoSesVrsSesUpdUnitaDocErr.NON_VERIFICATA);
            if (strutturaUpdVers.getVersatoreNonverificato() != null) {
                if (StringUtils.isNotBlank(strutturaUpdVers.getVersatoreNonverificato().getAmbiente())) {
                    tmpUpdUnitaDocErr.setNmAmbiente(strutturaUpdVers.getVersatoreNonverificato().getAmbiente());
                }
                if (StringUtils.isNotBlank(strutturaUpdVers.getVersatoreNonverificato().getEnte())) {
                    tmpUpdUnitaDocErr.setNmEnte(strutturaUpdVers.getVersatoreNonverificato().getEnte());
                }
                if (StringUtils.isNotBlank(strutturaUpdVers.getVersatoreNonverificato().getStruttura())) {
                    tmpUpdUnitaDocErr.setNmStrut(strutturaUpdVers.getVersatoreNonverificato().getStruttura());
                }
            }
            if (strutturaUpdVers.getIdStruttura() > 0) {
                tmpUpdUnitaDocErr.setOrgStrut(entityManager.find(OrgStrut.class, strutturaUpdVers.getIdStruttura()));
            }
            if (strutturaUpdVers.getChiaveNonVerificata() != null) {
                if (strutturaUpdVers.getChiaveNonVerificata().getAnno() > 0) {
                    tmpUpdUnitaDocErr
                            .setAaKeyUnitaDoc(new BigDecimal(strutturaUpdVers.getChiaveNonVerificata().getAnno()));
                }
                if (StringUtils.isNotBlank(strutturaUpdVers.getChiaveNonVerificata().getNumero())) {
                    tmpUpdUnitaDocErr.setCdKeyUnitaDoc(strutturaUpdVers.getChiaveNonVerificata().getNumero());
                }
                if (StringUtils.isNotBlank(strutturaUpdVers.getChiaveNonVerificata().getTipoRegistro())) {
                    tmpUpdUnitaDocErr
                            .setCdRegistroKeyUnitaDoc(strutturaUpdVers.getChiaveNonVerificata().getTipoRegistro());
                }
            }

            // tipo unità doc
            DecTipoUnitaDoc decTipoUnitaDoc = null;
            long idDecTipoUnitaDoc = 0;
            if (strutturaUpdVers.getIdTipologiaUnitaDocumentaria() > 0) {
                idDecTipoUnitaDoc = strutturaUpdVers.getIdTipologiaUnitaDocumentaria();
            } else if (strutturaUpdVers.getIdTipoUDNonVerificata() > 0) {
                idDecTipoUnitaDoc = strutturaUpdVers.getIdTipoUDNonVerificata();
            } else if (strutturaUpdVers.getIdTipoUDUnknown() > 0) {
                idDecTipoUnitaDoc = strutturaUpdVers.getIdTipoUDUnknown();
            }
            if (idDecTipoUnitaDoc > 0) {
                decTipoUnitaDoc = entityManager.find(DecTipoUnitaDoc.class, idDecTipoUnitaDoc);
                if (decTipoUnitaDoc != null) {
                    tmpUpdUnitaDocErr.setDecTipoUnitaDoc(decTipoUnitaDoc);
                    tmpUpdUnitaDocErr.setNmTipoUnitaDoc(decTipoUnitaDoc.getDsTipoUnitaDoc());
                }
            } else {
                tmpUpdUnitaDocErr.setNmTipoUnitaDoc(strutturaUpdVers.getDescTipologiaUnitaDocumentariaNonVerificata());
            }

            // tipo doc principale
            DecTipoDoc decTipoDocPrinc = null;
            long idDecTipoDoc = 0;
            if (strutturaUpdVers.getIdTipoDocPrincipale() > 0) {
                idDecTipoDoc = strutturaUpdVers.getIdTipoDocPrincipale();
            } else if (strutturaUpdVers.getIdTipoDOCPRINCUnknown() > 0) {
                idDecTipoDoc = strutturaUpdVers.getIdTipoDOCPRINCUnknown();
            }
            if (idDecTipoDoc > 0) {
                decTipoDocPrinc = entityManager.find(DecTipoDoc.class, idDecTipoDoc);
                if (decTipoDocPrinc != null) {
                    tmpUpdUnitaDocErr.setDecTipoDocPrinc(decTipoDocPrinc);
                    tmpUpdUnitaDocErr.setNmTipoDocPrinc(decTipoDocPrinc.getNmTipoDoc());
                }
            } else {
                tmpUpdUnitaDocErr.setNmTipoDocPrinc(strutturaUpdVers.getDescTipoDocPrincipaleNonVerificato());
            }

            // registro
            if (strutturaUpdVers.getIdRegistro() > 0) {
                tmpUpdUnitaDocErr.setDecRegistroUnitaDoc(
                        entityManager.find(DecRegistroUnitaDoc.class, strutturaUpdVers.getIdRegistro()));
            } else if (strutturaUpdVers.getIdTipoREGUnknown() > 0) {
                tmpUpdUnitaDocErr.setDecRegistroUnitaDoc(
                        entityManager.find(DecRegistroUnitaDoc.class, strutturaUpdVers.getIdTipoREGUnknown()));
            }

            //
            // CompRapportoAggiornamentoVers esito =
            // rispostaWs.getCompRapportoAggiornamentoVers();
            if (rispostaWs.getSeverity() == IRispostaWS.SeverityEnum.ERROR) {
                // al momento si raccoglie, del controllo, il primo errore collezionato !
                tmpUpdUnitaDocErr.setDecErrSacerPrinc(messaggiWSHelper.caricaDecErrore(rispostaWs.getErrorCode()));
                tmpUpdUnitaDocErr.setDsErrPrinc(LogSessioneUtils.getDsErrAtMaxLen(rispostaWs.getErrorMessage()));
                tmpUpdUnitaDocErr.setDecControlloWsPrinc(
                        controlliWSHelper.caricaCdControlloWs(rispostaWs.getControlloWs().getCdControllo()));
            }

            entityManager.persist(tmpUpdUnitaDocErr);
            tmpRispostaControlli.setrObject(tmpUpdUnitaDocErr);
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio sessione errata: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            log.error("Errore interno nella fase di salvataggio sessione errata", e);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviXmlSesUpdUnitaDocErr(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione, VrsSesUpdUnitaDocErr vrsSesUpdUnitaDocErr) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        // salvo xml di request e response sessione errata
        tmpRispostaControlli.setrBoolean(false);
        CompRapportoUpdVers esito = rispostaWs.getCompRapportoUpdVers();
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        try {
            VrsXmlSesUpdUnitaDocErr tmpXmlSesUpdUnitaDocErr = new VrsXmlSesUpdUnitaDocErr();

            // se FATALE registro esclusivamente la RISPOSTA
            if (!rispostaWs.getStatoSessioneVersamento().equals(StatiSessioneVersEnum.FATALE)) {
                tmpXmlSesUpdUnitaDocErr.setVrsSesUpdUnitaDocErr(vrsSesUpdUnitaDocErr);
                tmpXmlSesUpdUnitaDocErr.setTiXml(TiXmlVrsXmlSesUpdUnitaDocErr.RICHIESTA);
                tmpXmlSesUpdUnitaDocErr.setCdVersioneXml(strutturaUpdVers.getVersioneIndiceSipNonVerificata());
                tmpXmlSesUpdUnitaDocErr
                        .setBlXml(versamento.getDatiXml().length() == 0 ? "--" : versamento.getDatiXml());
                entityManager.persist(tmpXmlSesUpdUnitaDocErr);
            }
            String xmlesito = this.generaRapportoVersamento(rispostaWs);

            tmpXmlSesUpdUnitaDocErr = new VrsXmlSesUpdUnitaDocErr();
            tmpXmlSesUpdUnitaDocErr.setVrsSesUpdUnitaDocErr(vrsSesUpdUnitaDocErr);
            tmpXmlSesUpdUnitaDocErr.setTiXml(TiXmlVrsXmlSesUpdUnitaDocErr.RISPOSTA);
            tmpXmlSesUpdUnitaDocErr.setCdVersioneXml(strutturaUpdVers.getVersioneIndiceSipNonVerificata());
            tmpXmlSesUpdUnitaDocErr.setBlXml(xmlesito);
            entityManager.persist(tmpXmlSesUpdUnitaDocErr);
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio  dati xml sessione errata: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            log.error("Errore interno nella fase di salvataggio  dati xml sessione errata", e);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli cercaAggiornamentoKo(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        // cerco e recupero il fascicolo fallito
        tmpRispostaControlli.setrBoolean(false);
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        List<VrsUpdUnitaDocKo> updUnitaDocKo;

        try {
            String queryStr = "select ud " + "from VrsUpdUnitaDocKo ud " + "where ud.orgStrut.idStrut = :idStrutIn "
                    + " and ud.cdKeyUnitaDoc = :cdKeyUnitaDoc " + " and ud.aaKeyUnitaDoc = :aaKeyUnitaDoc "
                    + " and ud.cdRegistroKeyUnitaDoc = :cdRegistroKeyUnitaDoc ";

            javax.persistence.Query query = entityManager.createQuery(queryStr, VrsUpdUnitaDocKo.class);
            query.setParameter("idStrutIn", strutturaUpdVers.getIdStruttura());
            query.setParameter("cdKeyUnitaDoc", strutturaUpdVers.getChiaveNonVerificata().getNumero());
            query.setParameter("aaKeyUnitaDoc", new BigDecimal(strutturaUpdVers.getChiaveNonVerificata().getAnno()));
            query.setParameter("cdRegistroKeyUnitaDoc", strutturaUpdVers.getChiaveNonVerificata().getTipoRegistro());

            updUnitaDocKo = query.getResultList();
            // rendo comunque true, per indicare che la query è andata bene:
            // è perfettamente normale non trovare nulla se questo è il primo
            // errore per l'aggiornamento. in questo caso dovrò creare la riga in tabella
            // nel caso di scrittura di sessione fallita.
            tmpRispostaControlli.setrBoolean(true);
            if (updUnitaDocKo.size() > 0) {
                // se poi ho anche trovato qualcosa di utile, lo restituisco al
                // chiamante dopo averlo bloccato in modo esclusivo
                Map<String, Object> properties = new HashMap();
                properties.put("javax.persistence.lock.timeout", 25);
                VrsUpdUnitaDocKo tmpVrsUpdUnitaDocKo = entityManager.find(VrsUpdUnitaDocKo.class,
                        updUnitaDocKo.get(0).getIdUpdUnitaDocKo(), LockModeType.PESSIMISTIC_WRITE, properties);

                // questo stesso lock viene usato sia in fase di scrittura sessione KO
                // che di scrittura sessione buona. In questo secondo caso la riga verrà
                // cancellata. Dal momento che per condizioni sfortunate di concorrenza è
                // possibile che la query iniziale renda dei dati ma la find non
                // trovi nulla, verifico che nel frattempo la riga non sia stata cancellata.
                if (tmpVrsUpdUnitaDocKo != null) {
                    tmpRispostaControlli.setrLong(tmpVrsUpdUnitaDocKo.getIdUpdUnitaDocKo());
                    tmpRispostaControlli.setrObject(tmpVrsUpdUnitaDocKo);
                } else {
                    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
                    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                            "Errore di concorrenza nel salvataggio della sessione:"
                                    + "aggionamento versamento KO è stato rimosso. "
                                    + "Si consiglia di ritentare il versamento."));
                    tmpRispostaControlli.setrBoolean(false);
                }
            }
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase ricerca Aggiornamento Versamento KO (SF): " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            log.error("Errore interno nella fase ricerca Aggiornamento Versamento KO (SF)", e);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviVrsUpdUnitaDocKo(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        // scrive una nuova istanza di Fascicolo KO per il versamento
        tmpRispostaControlli.setrBoolean(false);
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        CompRapportoUpdVers esito = rispostaWs.getCompRapportoUpdVers();

        try {
            VrsUpdUnitaDocKo tmpUpdUnitaDocKo = new VrsUpdUnitaDocKo();
            tmpUpdUnitaDocKo.setOrgStrut(entityManager.find(OrgStrut.class, strutturaUpdVers.getIdStruttura()));
            tmpUpdUnitaDocKo.setAaKeyUnitaDoc(new BigDecimal(strutturaUpdVers.getChiaveNonVerificata().getAnno()));
            tmpUpdUnitaDocKo.setCdKeyUnitaDoc(strutturaUpdVers.getChiaveNonVerificata().getNumero());
            tmpUpdUnitaDocKo.setCdRegistroKeyUnitaDoc(strutturaUpdVers.getChiaveNonVerificata().getTipoRegistro());
            tmpUpdUnitaDocKo.setTsIniFirstSes(convert(sessione.getTmApertura()));
            tmpUpdUnitaDocKo.setTsIniLastSes(convert(sessione.getTmApertura())); // coincidono: c'è una sola sessione
            tmpUpdUnitaDocKo.setDecErrSacerPrinc(messaggiWSHelper.caricaDecErrore(rispostaWs.getErrorCode()));
            tmpUpdUnitaDocKo.setDsErrPrinc(LogSessioneUtils.getDsErrAtMaxLen(rispostaWs.getErrorMessage()));
            tmpUpdUnitaDocKo.setDecControlloWsPrinc(
                    controlliWSHelper.caricaCdControlloWs(rispostaWs.getControlloWs().getCdControllo()));
            tmpUpdUnitaDocKo.setTiStatoUdpUdKo(TiStatoUdpUdKo.NON_VERIFICATO);
            // TODO da verificare
            if (strutturaUpdVers.getIdRegistro() > 0) {
                tmpUpdUnitaDocKo.setDecRegistroUnitaDocLast(
                        entityManager.find(DecRegistroUnitaDoc.class, strutturaUpdVers.getIdRegistro()));
            } else if (strutturaUpdVers.getIdTipoREGUnknown() > 0) {
                tmpUpdUnitaDocKo.setDecRegistroUnitaDocLast(
                        entityManager.find(DecRegistroUnitaDoc.class, strutturaUpdVers.getIdTipoREGUnknown()));
            }

            DecTipoUnitaDoc decTipoUnitaDoc = null;
            long idDecTipoUnitaDoc = 0;
            if (strutturaUpdVers.getIdTipologiaUnitaDocumentaria() > 0) {
                idDecTipoUnitaDoc = strutturaUpdVers.getIdTipologiaUnitaDocumentaria();
            } else if (strutturaUpdVers.getIdTipoUDNonVerificata() > 0) {
                idDecTipoUnitaDoc = strutturaUpdVers.getIdTipoUDNonVerificata();
            } else if (strutturaUpdVers.getIdTipoUDUnknown() > 0) {
                idDecTipoUnitaDoc = strutturaUpdVers.getIdTipoUDUnknown();
            }
            if (idDecTipoUnitaDoc > 0) {
                decTipoUnitaDoc = entityManager.find(DecTipoUnitaDoc.class, idDecTipoUnitaDoc);
                if (decTipoUnitaDoc != null) {
                    tmpUpdUnitaDocKo.setDecTipoUnitaDocLast(decTipoUnitaDoc);
                }
            }
            // TODO da verificare
            DecTipoDoc decTipoDocPrinc = null;
            long idDecTipoDoc = 0;
            if (strutturaUpdVers.getIdTipoDocPrincipale() > 0) {
                idDecTipoDoc = strutturaUpdVers.getIdTipoDocPrincipale();
            } else if (strutturaUpdVers.getIdTipoDOCPRINCUnknown() > 0) {
                idDecTipoDoc = strutturaUpdVers.getIdTipoDOCPRINCUnknown();
            }
            if (idDecTipoDoc > 0) {
                decTipoDocPrinc = entityManager.find(DecTipoDoc.class, idDecTipoDoc);
                if (decTipoDocPrinc != null) {
                    tmpUpdUnitaDocKo.setDecTipoDocPrincLast(decTipoDocPrinc);
                }
            }

            // TODO: necessaria una logica di gestione recupero della prima e dell'ultima
            // sessione in KO
            tmpUpdUnitaDocKo.setVrsSesUpdUnitaDocKoFirst(null);
            tmpUpdUnitaDocKo.setVrsSesUpdUnitaDocKoLast(null);

            // gli altri due campi li potrò popolare solo una volta persistita la sessione
            // KO
            entityManager.persist(tmpUpdUnitaDocKo);
            tmpRispostaControlli.setrObject(tmpUpdUnitaDocKo);

            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);

        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio aggiornamentoKO: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            log.error("Errore interno nella fase di salvataggio aggiornamentoKO", e);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviSessioneUpdUnitaDocKo(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione, VrsUpdUnitaDocKo vrsUpdUnitaDocKo, AroUpdUnitaDoc aroUpdUnitaDocOk) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        CompRapportoUpdVers esito = rispostaWs.getCompRapportoUpdVers();

        try {
            TiStatoSesUpdKo tmpStatoSessione = null;
            VrsSesUpdUnitaDocKo tmpSesUpdUnitaDocKo = new VrsSesUpdUnitaDocKo();
            if (aroUpdUnitaDocOk != null) {
                // se sto aggiungendo una sessione fallita ad un aggiornamento
                // già presente, questa è _risolta_ per definizione
                tmpSesUpdUnitaDocKo.setAroUpdUnitaDoc(aroUpdUnitaDocOk);
                tmpStatoSessione = TiStatoSesUpdKo.RISOLTO;
            } else {
                // altrimenti è _non verificata_
                tmpSesUpdUnitaDocKo.setVrsUpdUnitaDocKo(vrsUpdUnitaDocKo);
                tmpStatoSessione = TiStatoSesUpdKo.NON_VERIFICATO;
            }

            DecTipoUnitaDoc decTipoUnitaDoc = null;
            long idDecTipoUnitaDoc = 0;
            if (strutturaUpdVers.getIdTipologiaUnitaDocumentaria() > 0) {
                idDecTipoUnitaDoc = strutturaUpdVers.getIdTipologiaUnitaDocumentaria();
            } else if (strutturaUpdVers.getIdTipoUDNonVerificata() > 0) {
                idDecTipoUnitaDoc = strutturaUpdVers.getIdTipoUDNonVerificata();
            } else if (strutturaUpdVers.getIdTipoUDUnknown() > 0) {
                idDecTipoUnitaDoc = strutturaUpdVers.getIdTipoUDUnknown();
            }
            if (idDecTipoUnitaDoc > 0) {
                decTipoUnitaDoc = entityManager.find(DecTipoUnitaDoc.class, idDecTipoUnitaDoc);
                if (decTipoUnitaDoc != null) {
                    tmpSesUpdUnitaDocKo.setDecTipoUnitaDoc(decTipoUnitaDoc);
                }
            }
            tmpSesUpdUnitaDocKo.setTsIniSes(convert(sessione.getTmApertura()));
            tmpSesUpdUnitaDocKo.setTsFineSes(convert(sessione.getTmChiusura()));

            if (strutturaUpdVers.getIdRegistro() > 0) {
                tmpSesUpdUnitaDocKo.setDecRegistroUnitaDoc(
                        entityManager.find(DecRegistroUnitaDoc.class, strutturaUpdVers.getIdRegistro()));
            } else if (strutturaUpdVers.getIdTipoREGUnknown() > 0) {
                tmpSesUpdUnitaDocKo.setDecRegistroUnitaDoc(
                        entityManager.find(DecRegistroUnitaDoc.class, strutturaUpdVers.getIdTipoREGUnknown()));
            }

            DecTipoDoc decTipoDocPrinc = null;
            long idDecTipoDoc = 0;
            if (strutturaUpdVers.getIdTipoDocPrincipale() > 0) {
                idDecTipoDoc = strutturaUpdVers.getIdTipoDocPrincipale();
            } else if (strutturaUpdVers.getIdTipoDOCPRINCUnknown() > 0) {
                idDecTipoDoc = strutturaUpdVers.getIdTipoDOCPRINCUnknown();
            }
            if (idDecTipoDoc > 0) {
                decTipoDocPrinc = entityManager.find(DecTipoDoc.class, idDecTipoDoc);
                if (decTipoDocPrinc != null) {
                    tmpSesUpdUnitaDocKo.setDecTipoDocPrinc(decTipoDocPrinc);
                }
            }
            // è possibile avere una sessione fallita anche senza che sia indicata
            // la versione del versamento
            if (StringUtils.isEmpty(sessione.getVersioneWS())) {
                tmpSesUpdUnitaDocKo.setCdVersioneWs("N/A");
            } else {
                tmpSesUpdUnitaDocKo.setCdVersioneWs(sessione.getVersioneWS());
            }
            // salvo l'utente solo se identificato, altrimenti il campo può essere NULL
            // anche in questo caso è possibile avere una versione fallita senza aver
            // indentificato
            // l'utente versante
            if (versamento.getUtente() != null) {
                tmpSesUpdUnitaDocKo.setIamUser(entityManager.find(IamUser.class, versamento.getUtente().getIdUtente()));
            }
            tmpSesUpdUnitaDocKo.setDecErrSacerPrinc(messaggiWSHelper.caricaDecErrore(rispostaWs.getErrorCode()));
            tmpSesUpdUnitaDocKo.setDsErrPrinc(LogSessioneUtils.getDsErrAtMaxLen(rispostaWs.getErrorMessage()));
            tmpSesUpdUnitaDocKo.setDecControlloWsPrinc(
                    controlliWSHelper.caricaCdControlloWs(rispostaWs.getControlloWs().getCdControllo()));
            tmpSesUpdUnitaDocKo.setTiStatoSesUpdKo(tmpStatoSessione);
            tmpSesUpdUnitaDocKo.setOrgStrut(vrsUpdUnitaDocKo.getOrgStrut());// da VRS_UPD_UNITA_DOC_KO
            tmpSesUpdUnitaDocKo.setAaKeyUnitaDoc(vrsUpdUnitaDocKo.getAaKeyUnitaDoc());// da VRS_UPD_UNITA_DOC_KO

            entityManager.persist(tmpSesUpdUnitaDocKo);
            tmpRispostaControlli.setrObject(tmpSesUpdUnitaDocKo);
            entityManager.flush();

            // se sto aggiungendo una sessione ad un aggiornamento KO, lo devo aggiornare
            // inoltre devo aggiornare - forse - la tabella MON_CONTA_SES_UPD_UD_KO.
            // la stessa cosa la dovrò fare quando creo un nuovo aggiornamento per il
            // quale esistono sessioni fallite
            if (vrsUpdUnitaDocKo != null) {
                // non serve aggiornare MON_CONTA_SES_UPD_UD_KO perché la sessione fallita e’
                // registrata in data corrente
                //
                VrsVModifUpdUdKo updModifUpdKo = entityManager.find(VrsVModifUpdUdKo.class,
                        vrsUpdUnitaDocKo.getIdUpdUnitaDocKo());
                if (updModifUpdKo != null) {
                    if (updModifUpdKo.getIdErrSacerPrinc() != null) {
                        vrsUpdUnitaDocKo.setDecErrSacerPrinc(
                                entityManager.find(DecErrSacer.class, updModifUpdKo.getIdErrSacerPrinc().longValue()));
                        vrsUpdUnitaDocKo
                                .setDsErrPrinc(LogSessioneUtils.getDsErrAtMaxLen(updModifUpdKo.getDsErrPrinc()));
                    } else {
                        vrsUpdUnitaDocKo.setDecErrSacerPrinc(null);
                        vrsUpdUnitaDocKo.setDsErrPrinc(LogSessioneUtils.getDsErrAtMaxLen(CD_DS_ERR_DIVERSI));
                    }

                    if (updModifUpdKo.getIdControlloWsPrinc() != null) {
                        vrsUpdUnitaDocKo.setDecControlloWsPrinc(entityManager.find(DecControlloWs.class,
                                updModifUpdKo.getIdControlloWsPrinc().longValue()));
                    } else {
                        vrsUpdUnitaDocKo.setDecControlloWsPrinc(null);
                    }
                    if (StringUtils.isNotBlank(updModifUpdKo.getTiStatoUpdUdKo())) {
                        vrsUpdUnitaDocKo.setTiStatoUdpUdKo(TiStatoUdpUdKo.valueOf(updModifUpdKo.getTiStatoUpdUdKo()));
                    } else {
                        vrsUpdUnitaDocKo.setTiStatoUdpUdKo(null);
                    }

                    // ha senso solo se questa è la prima sessione fallita per la modifica
                    vrsUpdUnitaDocKo.setVrsSesUpdUnitaDocKoFirst(entityManager.find(VrsSesUpdUnitaDocKo.class,
                            updModifUpdKo.getIdSesUpdUdKoFirst().longValue()));

                    if (updModifUpdKo.getIdTipoUnitaDocLast() != null) {
                        vrsUpdUnitaDocKo.setDecTipoUnitaDocLast(entityManager.find(DecTipoUnitaDoc.class,
                                updModifUpdKo.getIdTipoUnitaDocLast().longValue()));
                    }

                    if (updModifUpdKo.getIdRegistroUnitaDocLast() != null) {
                        vrsUpdUnitaDocKo.setDecRegistroUnitaDocLast(entityManager.find(DecRegistroUnitaDoc.class,
                                updModifUpdKo.getIdRegistroUnitaDocLast().longValue()));
                    }

                    if (updModifUpdKo.getIdTipoDocPrincLast() != null) {
                        vrsUpdUnitaDocKo.setDecTipoDocPrincLast(entityManager.find(DecTipoDoc.class,
                                updModifUpdKo.getIdTipoDocPrincLast().longValue()));
                    }
                } else {
                    // TODO : da verificare
                    vrsUpdUnitaDocKo.setDecTipoUnitaDocLast(tmpSesUpdUnitaDocKo.getDecTipoUnitaDoc());

                    vrsUpdUnitaDocKo.setDecRegistroUnitaDocLast(tmpSesUpdUnitaDocKo.getDecRegistroUnitaDoc());

                    vrsUpdUnitaDocKo.setDecTipoDocPrincLast(tmpSesUpdUnitaDocKo.getDecTipoDocPrinc());
                }

                vrsUpdUnitaDocKo.setTsIniLastSes(convert(sessione.getTmApertura()));
                vrsUpdUnitaDocKo.setVrsSesUpdUnitaDocKoLast(
                        entityManager.find(VrsSesUpdUnitaDocKo.class, tmpSesUpdUnitaDocKo.getIdSesUpdUnitaDocKo()));
                // aggiungo la sessione appena creata all'aggiornamento KO
                vrsUpdUnitaDocKo.getVrsSesUpdUnitaDocKos().add(tmpSesUpdUnitaDocKo);
            }
            /*
             * else { // TODO nel caso dei fascicoli KO veniva settato un flag per segnalare la presenza di una sessione
             * fallita ! // altrimenti aggiorno il flag di errore del fascicolo OK, // che forse è già alzato, ma male
             * non fa... fascicoloOk.setFlSesFascicoloKo("1"); }
             */
            tmpRispostaControlli.setrObject(tmpSesUpdUnitaDocKo);
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio dati xml sessione KO: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            log.error("Errore interno nella fase di salvataggio dati xml sessione KO", e);
        }

        return tmpRispostaControlli;
    }

    private RispostaControlli creaConteggioMonContaSesUpdUdKo(VrsSesUpdUnitaDocKo vrsSesUpdUnitaDocKo,
            MonKeyTotalUdKo tmpMonKeyTotalUdKo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {
            MonContaSesUpdUdKo monContaSesUpdUdKo = new MonContaSesUpdUdKo();
            monContaSesUpdUdKo.setDtRifConta(vrsSesUpdUnitaDocKo.getTsIniSes());
            monContaSesUpdUdKo.setNiSesUpdUdKo(BigDecimal.ONE);
            monContaSesUpdUdKo.setTiStatoUdpUdKo(
                    TiStatoUdpUdKoMonContaSesUpdUdKo.valueOf(vrsSesUpdUnitaDocKo.getTiStatoSesUpdKo().name()));
            monContaSesUpdUdKo.setMonKeyTotalUdKo(tmpMonKeyTotalUdKo);

            entityManager.persist(monContaSesUpdUdKo);
            tmpRispostaControlli.setrObject(monContaSesUpdUdKo);
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio dati conteggio sessione KO: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            log.error("Errore interno nella fase di salvataggio dati conteggio sessione KO", e);
        }

        return tmpRispostaControlli;

    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli lockAndGetMonContaSesUpdUdKo(VrsUpdUnitaDocKo updUnitaDocKo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);
        List<MonKeyTotalUdKo> mcfks;

        String queryStr = "select ud " + "from MonKeyTotalUdKo ud " + "where ud.orgStrut = :orgStrut "
                + " and ud.aaKeyUnitaDoc = :aaKeyUnitaDoc " + " and ud.decRegistroUnitaDoc = :decRegistroUnitaDoc "
                + " and ud.decTipoDocPrinc = :decTipoDocPrinc " + " and ud.decTipoUnitaDoc = :decTipoUnitaDoc ";

        javax.persistence.Query query = entityManager.createQuery(queryStr, MonKeyTotalUdKo.class);
        query.setParameter("orgStrut", updUnitaDocKo.getOrgStrut());
        query.setParameter("aaKeyUnitaDoc", updUnitaDocKo.getAaKeyUnitaDoc());
        query.setParameter("decRegistroUnitaDoc", updUnitaDocKo.getDecRegistroUnitaDocLast());
        query.setParameter("decTipoDocPrinc", updUnitaDocKo.getDecTipoDocPrincLast());
        query.setParameter("decTipoUnitaDoc", updUnitaDocKo.getDecTipoUnitaDocLast());

        mcfks = query.getResultList();
        if (mcfks.size() > 0) {
            MonKeyTotalUdKo tmpMonKeyTotalUdKo = mcfks.get(0);
            tmpRispostaControlli.setrObject(tmpMonKeyTotalUdKo);
            // lock each one
            for (MonContaSesUpdUdKo tmpMonContaSesUpdUdKo : tmpMonKeyTotalUdKo.getMonContaSesUpdUdKos()) {
                Map<String, Object> properties = new HashMap();
                properties.put("javax.persistence.lock.timeout", 25);
                entityManager.find(MonContaSesUpdUdKo.class, tmpMonContaSesUpdUdKo.getIdContaSesUpdUdKo(),
                        LockModeType.PESSIMISTIC_WRITE, properties);
            }

        }
        tmpRispostaControlli.setrBoolean(true);
        return tmpRispostaControlli;

    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli aggiornaConteggioMonContaSesUpdUdKo(VrsSesUpdUnitaDocKo vrsSesUpdUnitaDocKo,
            MonKeyTotalUdKo tmpMonKeyTotalUdKo, boolean addOrCreate) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);
        List<MonContaSesUpdUdKo> mcfks;

        String queryStr = "select ud " + "from MonContaSesUpdUdKo ud " + "where ud.monKeyTotalUdKo = :monKeyTotalUdKo "
                + " and ud.dtRifConta = :dtRifConta " + " and ud.tiStatoUdpUdKo = :tiStatoUdpUdKo ";

        javax.persistence.Query query = entityManager.createQuery(queryStr, MonContaSesUpdUdKo.class);
        query.setParameter("monKeyTotalUdKo", tmpMonKeyTotalUdKo);
        query.setParameter("dtRifConta", LogSessioneUtils.getDatePart(vrsSesUpdUnitaDocKo.getTsIniSes()));
        query.setParameter("tiStatoUdpUdKo",
                TiStatoUdpUdKoMonContaSesUpdUdKo.valueOf(vrsSesUpdUnitaDocKo.getTiStatoSesUpdKo().name()));

        mcfks = query.getResultList();
        if (mcfks.size() > 0) {
            if (addOrCreate) {
                mcfks.get(0).setNiSesUpdUdKo(mcfks.get(0).getNiSesUpdUdKo().add(BigDecimal.ONE));
            } else {
                // testo la sottrazione se zero elimino il record
                if (mcfks.get(0).getNiSesUpdUdKo().subtract(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                    // rimuovo aggiornamento KO
                    entityManager.remove(mcfks.get(0));
                } else {
                    mcfks.get(0).setNiSesUpdUdKo(mcfks.get(0).getNiSesUpdUdKo().subtract(BigDecimal.ONE));
                }
            }
            //
            entityManager.flush();
        }

        // se non esistono record lo si crea (quando si tenta di contare sommando una
        // unità)
        if (mcfks.isEmpty() && addOrCreate) {
            // crea il record su MonContaSesUpdUdKo
            return this.creaConteggioMonContaSesUpdUdKo(vrsSesUpdUnitaDocKo, tmpMonKeyTotalUdKo);
        }
        tmpRispostaControlli.setrBoolean(true); // update is good
        return tmpRispostaControlli;

    }

    public RispostaControlli verificaDataAttivazioneJob() {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);
        rispostaControlli.setrLong(-1);

        List<LogVVisLastSched> logVVisLastScheds = null;
        try {
            String queryStr = "select u " + " from LogVVisLastSched u "
                    + " where u.nmJob = 'CALCOLO_CONTENUTO_AGGIORNAMENTI' " + " order by u.dtRegLogJobIni desc ";

            javax.persistence.Query query = entityManager.createQuery(queryStr, AroXmlUpdUnitaDoc.class);

            logVVisLastScheds = query.getResultList();

            if (logVVisLastScheds.size() >= 1) {
                // se trovato, recupero il risultato che interessa per la verifica
                rispostaControlli.setrLong(0);

                LogVVisLastSched job = logVVisLastScheds.get(0);
                rispostaControlli.setrDate(job.getDtRegLogJobIni());
            }
            rispostaControlli.setrBoolean(true);// query is good
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "UpdLogSessioneHelper.verificaDataAttivazioneJob: " + e.getMessage()));
            log.error("Eccezione nella lettura  della tabella dei job ", e);
        }

        return rispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviXmlUpdUnitaDocKo(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione, VrsSesUpdUnitaDocKo sesUpdUnitaDocKo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        // salvo xml di request e response sessione fallita
        tmpRispostaControlli.setrBoolean(false);
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        CompRapportoUpdVers esito = rispostaWs.getCompRapportoUpdVers();

        try {
            VrsXmlSesUpdUnitaDocKo tmpXmlSesUpdUnitaDocKo = new VrsXmlSesUpdUnitaDocKo();

            tmpXmlSesUpdUnitaDocKo.setVrsSesUpdUnitaDocKo(sesUpdUnitaDocKo);
            tmpXmlSesUpdUnitaDocKo.setTiXml(TiXmlVrsXmlSesUpdUnitaDocKo.RICHIESTA);
            tmpXmlSesUpdUnitaDocKo.setCdVersioneXml(strutturaUpdVers.getVersioneIndiceSipNonVerificata());
            tmpXmlSesUpdUnitaDocKo.setBlXml(versamento.getDatiXml().length() == 0 ? "--" : versamento.getDatiXml());
            tmpXmlSesUpdUnitaDocKo.setOrgStrut(sesUpdUnitaDocKo.getOrgStrut()); // da VRS_UPD_UNITA_DOC_KO
            tmpXmlSesUpdUnitaDocKo.setDtRegXml(convert(sessione.getTmApertura()));

            entityManager.persist(tmpXmlSesUpdUnitaDocKo);
            sesUpdUnitaDocKo.getVrsXmlSesUpdUnitaDocKos().add(tmpXmlSesUpdUnitaDocKo);

            String xmlesito = this.generaRapportoVersamento(rispostaWs);

            tmpXmlSesUpdUnitaDocKo = new VrsXmlSesUpdUnitaDocKo();
            tmpXmlSesUpdUnitaDocKo.setVrsSesUpdUnitaDocKo(sesUpdUnitaDocKo);
            tmpXmlSesUpdUnitaDocKo.setTiXml(TiXmlVrsXmlSesUpdUnitaDocKo.RISPOSTA);
            tmpXmlSesUpdUnitaDocKo.setCdVersioneXml(esito.getVersioneRapportoVersamento());
            tmpXmlSesUpdUnitaDocKo.setBlXml(xmlesito);
            tmpXmlSesUpdUnitaDocKo.setOrgStrut(sesUpdUnitaDocKo.getOrgStrut());
            tmpXmlSesUpdUnitaDocKo.setDtRegXml(convert(sessione.getTmApertura()));

            entityManager.persist(tmpXmlSesUpdUnitaDocKo);
            sesUpdUnitaDocKo.getVrsXmlSesUpdUnitaDocKos().add(tmpXmlSesUpdUnitaDocKo);

            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio  dati xml sessione fallita: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            log.error("Errore interno nella fase di salvataggio  dati xml sessione fallita", e);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviCtrlErrWarnUpdUnitaDocKo(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione, VrsSesUpdUnitaDocKo sesUpdUnitaDocKo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        // salvo gli errori ed i warning sessione fallita
        tmpRispostaControlli.setrBoolean(false);
        int progErrore = 1;
        try {
            /*
             * per ogni controllo generale e per ogni controlllo svolto sull’unità doc da aggiornare con esito =
             * NEGATIVO o WARNING (in ordine di numero d’ordine del controllo)
             */
            progErrore = buildKo(versamento.getControlliGenerali(), rispostaWs, versamento, sesUpdUnitaDocKo,
                    progErrore);
            progErrore = buildKo(versamento.getControlliUnitaDocumentaria(), rispostaWs, versamento, sesUpdUnitaDocKo,
                    progErrore);

            StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
            // per ogni controllo su collegamento
            for (UpdUnitaDocColl link : strutturaUpdVers.getUnitaDocCollegate()) {
                CSChiave key = new CSChiave();
                key.setAnno(new Long(link.getAggChiave().getAnno()));
                key.setNumero(link.getAggChiave().getNumero());
                key.setTipoRegistro(link.getAggChiave().getTipoRegistro());

                progErrore = buildKo(versamento.getControlliCollegamento(key), rispostaWs, versamento, sesUpdUnitaDocKo,
                        progErrore);
            }

            // per ogni constrollo su documenti
            for (UpdDocumentoVers documento : strutturaUpdVers.getDocumentiAttesi()) {

                switch (documento.getCategoriaDoc()) {
                case Principale:
                    progErrore = buildKo(
                            versamento.getControlliDocPrincipale(documento.getRifUpdDocumento().getIDDocumento()),
                            rispostaWs, versamento, sesUpdUnitaDocKo, progErrore);
                    break;
                case Allegato:
                    progErrore = buildKo(
                            versamento.getControlliAllegato(documento.getRifUpdDocumento().getIDDocumento()),
                            rispostaWs, versamento, sesUpdUnitaDocKo, progErrore);
                    break;
                case Annesso:
                    progErrore = buildKo(
                            versamento.getControlliAnnesso(documento.getRifUpdDocumento().getIDDocumento()), rispostaWs,
                            versamento, sesUpdUnitaDocKo, progErrore);
                    break;
                case Annotazione:
                    progErrore = buildKo(
                            versamento.getControlliAnnotazione(documento.getRifUpdDocumento().getIDDocumento()),
                            rispostaWs, versamento, sesUpdUnitaDocKo, progErrore);
                    break;
                }

                // per ogni componente
                for (UpdComponenteVers componente : documento.getUpdComponentiAttesi()) {
                    switch (documento.getCategoriaDoc()) {
                    case Principale:
                        progErrore = buildKo(versamento.getControlliComponenteDocPrincipale(componente.getKeyCtrl()),
                                rispostaWs, versamento, sesUpdUnitaDocKo, progErrore);
                        break;
                    case Allegato:
                        progErrore = buildKo(versamento.getControlliComponenteAllegati(componente.getKeyCtrl()),
                                rispostaWs, versamento, sesUpdUnitaDocKo, progErrore);
                        break;
                    case Annesso:
                        progErrore = buildKo(versamento.getControlliComponenteAnnessi(componente.getKeyCtrl()),
                                rispostaWs, versamento, sesUpdUnitaDocKo, progErrore);
                        break;
                    case Annotazione:
                        progErrore = buildKo(versamento.getControlliComponenteAnnotazioni(componente.getKeyCtrl()),
                                rispostaWs, versamento, sesUpdUnitaDocKo, progErrore);
                        break;
                    }
                }

            }

            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio errori sessione fallita: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    private int buildKo(Set<ControlloEseguito> ctrlList, RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            VrsSesUpdUnitaDocKo sesUpdUnitaDocKo, int progErrore) {
        VrsErrSesUpdUnitaDocKo tmpErrSesUpdUnitaDocKo;
        // sort list
        List<ControlloEseguito> sortedCtrlList = versamento.sortControlli(ctrlList);
        for (ControlloEseguito controlloEseguito : sortedCtrlList) {
            for (VoceDiErrore tmpVoceDiErrore : controlloEseguito.getErrori()) {
                tmpErrSesUpdUnitaDocKo = new VrsErrSesUpdUnitaDocKo();
                tmpErrSesUpdUnitaDocKo.setVrsSesUpdUnitaDocKo(sesUpdUnitaDocKo);
                //
                tmpErrSesUpdUnitaDocKo.setPgErr(new BigDecimal(progErrore));
                if (tmpVoceDiErrore.getSeverity() == IRispostaWS.SeverityEnum.ERROR) {
                    tmpErrSesUpdUnitaDocKo.setTiErr(TiErrVrsErrUpdUnitaDocKo.FATALE);
                } else {
                    tmpErrSesUpdUnitaDocKo.setTiErr(TiErrVrsErrUpdUnitaDocKo.WARNING);
                }
                tmpErrSesUpdUnitaDocKo
                        .setDecControlloWs(controlliWSHelper.caricaCdControlloWs(controlloEseguito.getCdControllo()));
                tmpErrSesUpdUnitaDocKo.setDecErrSacer(messaggiWSHelper.caricaDecErrore(tmpVoceDiErrore.getErrorCode()));
                tmpErrSesUpdUnitaDocKo.setDsErr(LogSessioneUtils.getDsErrAtMaxLen(tmpVoceDiErrore.getErrorMessage()));

                // gestione errore principale
                if (versamento.verificaErrorePrinc(rispostaWs, controlloEseguito, tmpVoceDiErrore)) {
                    tmpErrSesUpdUnitaDocKo.setFlErrPrinc("1");
                } else {
                    tmpErrSesUpdUnitaDocKo.setFlErrPrinc("0");
                }

                //
                entityManager.persist(tmpErrSesUpdUnitaDocKo);
                sesUpdUnitaDocKo.getVrsErrSesUpdUnitaDocKos().add(tmpErrSesUpdUnitaDocKo);
                // TODO: inutile settarlo è già stata impostato il legame con la sessione !
                // sesUpdUnitaDocErr.getVrsXmlSesUpdUnitaDocErrs().add(null);
                progErrore++;
            }
        }
        return progErrore;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviCtrlErrWarnUpdUnitaDocErr(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione, VrsSesUpdUnitaDocErr sesUpdUnitaDocErr) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        // salvo gli errori ed i warning sessione fallita
        tmpRispostaControlli.setrBoolean(false);
        int progErrore = 1;
        try {
            /*
             * per ogni controllo generale e per ogni controlllo svolto sull’unità doc da aggiornare con esito =
             * NEGATIVO o WARNING (in ordine di numero d’ordine del controllo)
             */

            /*
             * Caso particolare : errore di sistema
             */
            progErrore = buildError(versamento.getControlliDiSistema(), rispostaWs, versamento, sesUpdUnitaDocErr,
                    progErrore);

            progErrore = buildError(versamento.getControlliGenerali(), rispostaWs, versamento, sesUpdUnitaDocErr,
                    progErrore);
            progErrore = buildError(versamento.getControlliUnitaDocumentaria(), rispostaWs, versamento,
                    sesUpdUnitaDocErr, progErrore);

            StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
            // per ogni controllo su collegamento
            for (UpdUnitaDocColl link : strutturaUpdVers.getUnitaDocCollegate()) {
                CSChiave key = new CSChiave();
                key.setAnno(new Long(link.getAggChiave().getAnno()));
                key.setNumero(link.getAggChiave().getNumero());
                key.setTipoRegistro(link.getAggChiave().getTipoRegistro());

                progErrore = buildError(versamento.getControlliCollegamento(key), rispostaWs, versamento,
                        sesUpdUnitaDocErr, progErrore);
            }

            // per ogni constrollo su documenti
            for (UpdDocumentoVers documento : strutturaUpdVers.getDocumentiAttesi()) {

                switch (documento.getCategoriaDoc()) {
                case Principale:
                    progErrore = buildError(
                            versamento.getControlliDocPrincipale(documento.getRifUpdDocumento().getIDDocumento()),
                            rispostaWs, versamento, sesUpdUnitaDocErr, progErrore);
                    break;
                case Allegato:
                    progErrore = buildError(
                            versamento.getControlliAllegato(documento.getRifUpdDocumento().getIDDocumento()),
                            rispostaWs, versamento, sesUpdUnitaDocErr, progErrore);
                    break;
                case Annesso:
                    progErrore = buildError(
                            versamento.getControlliAnnesso(documento.getRifUpdDocumento().getIDDocumento()), rispostaWs,
                            versamento, sesUpdUnitaDocErr, progErrore);
                    break;
                case Annotazione:
                    progErrore = buildError(
                            versamento.getControlliAnnotazione(documento.getRifUpdDocumento().getIDDocumento()),
                            rispostaWs, versamento, sesUpdUnitaDocErr, progErrore);
                    break;
                }

                // per ogni componente
                for (UpdComponenteVers componente : documento.getUpdComponentiAttesi()) {
                    switch (documento.getCategoriaDoc()) {
                    case Principale:
                        progErrore = buildError(versamento.getControlliComponenteDocPrincipale(componente.getKeyCtrl()),
                                rispostaWs, versamento, sesUpdUnitaDocErr, progErrore);
                        break;
                    case Allegato:
                        progErrore = buildError(versamento.getControlliComponenteAllegati(componente.getKeyCtrl()),
                                rispostaWs, versamento, sesUpdUnitaDocErr, progErrore);
                        break;
                    case Annesso:
                        progErrore = buildError(versamento.getControlliComponenteAnnessi(componente.getKeyCtrl()),
                                rispostaWs, versamento, sesUpdUnitaDocErr, progErrore);
                        break;
                    case Annotazione:
                        progErrore = buildError(versamento.getControlliComponenteAnnotazioni(componente.getKeyCtrl()),
                                rispostaWs, versamento, sesUpdUnitaDocErr, progErrore);
                        break;
                    }
                }

            }

            // altro ???
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio errori sessione fallita: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    private int buildError(Set<ControlloEseguito> ctrlList, RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            VrsSesUpdUnitaDocErr sesUpdUnitaDocErr, int progErrore) {
        VrsErrSesUpdUnitaDocErr tmpErrSesUpdUnitaDocErr;
        // sort list
        List<ControlloEseguito> sortedCtrlList = versamento.sortControlli(ctrlList);
        for (ControlloEseguito controlloEseguito : sortedCtrlList) {
            for (VoceDiErrore tmpVoceDiErrore : controlloEseguito.getErrori()) {
                tmpErrSesUpdUnitaDocErr = new VrsErrSesUpdUnitaDocErr();
                tmpErrSesUpdUnitaDocErr.setVrsSesUpdUnitaDocErr(sesUpdUnitaDocErr);
                //
                tmpErrSesUpdUnitaDocErr.setPgErr(new BigDecimal(progErrore));
                if (tmpVoceDiErrore.getSeverity() == IRispostaWS.SeverityEnum.ERROR) {
                    tmpErrSesUpdUnitaDocErr.setTiErr(TiErrVrsErrSesUpdUnitaDocErr.FATALE);
                } else {
                    tmpErrSesUpdUnitaDocErr.setTiErr(TiErrVrsErrSesUpdUnitaDocErr.WARNING);
                }
                tmpErrSesUpdUnitaDocErr
                        .setDecControlloWs(controlliWSHelper.caricaCdControlloWs(controlloEseguito.getCdControllo()));
                tmpErrSesUpdUnitaDocErr
                        .setDecErrSacer(messaggiWSHelper.caricaDecErrore(tmpVoceDiErrore.getErrorCode()));
                tmpErrSesUpdUnitaDocErr.setDsErr(LogSessioneUtils.getDsErrAtMaxLen(tmpVoceDiErrore.getErrorMessage()));

                // gestione errore principale
                if (versamento.verificaErrorePrinc(rispostaWs, controlloEseguito, tmpVoceDiErrore)) {
                    tmpErrSesUpdUnitaDocErr.setFlErrPrinc("1");
                } else {
                    tmpErrSesUpdUnitaDocErr.setFlErrPrinc("0");
                }

                //
                entityManager.persist(tmpErrSesUpdUnitaDocErr);
                sesUpdUnitaDocErr.getVrsErrSesUpdUnitaDocErrs().add(tmpErrSesUpdUnitaDocErr);
                // TODO: inutile settarlo è già stata impostato il legame con la sessione !
                // sesUpdUnitaDocErr.getVrsXmlSesUpdUnitaDocErrs().add(null);
                progErrore++;
            }
        }
        return progErrore;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String generaRapportoVersamento(RispostaWSUpdVers rispostaWs) throws JAXBException {
        // questo metodo viene invocato sia internamente alla classe (_non_ attraverso
        // il
        // container) che dall'EJB di salvataggio fascicolo. Dal momento che la
        // transazione
        // è sempre REQUIRED, non c'è molta differenza agli effetti pratici.
        StringWriter tmpStreamWriter = new StringWriter();
        CompRapportoUpdVers esito = rispostaWs.getCompRapportoUpdVers();
        JAXBContext tmpcontesto = xmlUpdVersCache.getVersRespCtxforEsitoAggiornamentoVersamento();
        Marshaller tmpMarshaller = tmpcontesto.createMarshaller();
        tmpMarshaller.marshal(esito.produciEsitoAggiornamento(), tmpStreamWriter);

        return tmpStreamWriter.toString();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli aggiornaMonKeyTotalUdKo(VrsUpdUnitaDocKo updUnitaDocKo, Date dtLastUpdKo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);
        List<MonKeyTotalUdKo> mktudkos;

        // lock (ricerca secondo le chiavi legate al vincolo di unicità UN_KEY_TOT_UD)
        String queryStr = "select ud " + "from MonKeyTotalUdKo ud " + "where ud.aaKeyUnitaDoc = :aaKeyUnitaDoc "
                + " and ud.decRegistroUnitaDoc = :decRegistroUnitaDoc " + " and ud.orgStrut = :orgStrut "
                + " and ud.decTipoUnitaDoc = :decTipoUnitaDoc " + " and ud.decTipoDocPrinc = :decTipoDocPrinc ";
        javax.persistence.Query query = entityManager.createQuery(queryStr, MonKeyTotalUdKo.class);
        query.setParameter("aaKeyUnitaDoc", updUnitaDocKo.getAaKeyUnitaDoc());
        query.setParameter("decRegistroUnitaDoc", updUnitaDocKo.getDecRegistroUnitaDocLast());
        query.setParameter("decTipoUnitaDoc", updUnitaDocKo.getDecTipoUnitaDocLast());
        query.setParameter("orgStrut", updUnitaDocKo.getOrgStrut());
        query.setParameter("decTipoDocPrinc", updUnitaDocKo.getDecTipoDocPrincLast());

        mktudkos = query.getResultList();
        if (mktudkos.size() > 0) {
            // TODO: probabilmente lock inutile dato che le condizioni non porteranno mai a
            // casi di concorrenza tra client nella stessa sottostruttura ....
            Map<String, Object> properties = new HashMap();
            properties.put("javax.persistence.lock.timeout", 25);
            entityManager.find(MonKeyTotalUd.class, mktudkos.get(0).getIdKeyTotalUdKo(), LockModeType.PESSIMISTIC_WRITE,
                    properties);

            // update
            mktudkos.get(0).setDtLastUpdUdKo(LogSessioneUtils.getDatePart(dtLastUpdKo));
        } else {
            MonKeyTotalUdKo monKeyTotalUdKo = new MonKeyTotalUdKo();
            monKeyTotalUdKo.setAaKeyUnitaDoc(updUnitaDocKo.getAaKeyUnitaDoc());
            monKeyTotalUdKo.setDecRegistroUnitaDoc(updUnitaDocKo.getDecRegistroUnitaDocLast());
            monKeyTotalUdKo.setDecTipoDocPrinc(updUnitaDocKo.getDecTipoDocPrincLast());
            monKeyTotalUdKo.setDecTipoUnitaDoc(updUnitaDocKo.getDecTipoUnitaDocLast());
            monKeyTotalUdKo.setOrgStrut(updUnitaDocKo.getOrgStrut());
            monKeyTotalUdKo.setDtLastUpdUdKo(dtLastUpdKo);

            entityManager.persist(monKeyTotalUdKo);
            tmpRispostaControlli.setrObject(monKeyTotalUdKo);
        }

        entityManager.flush();
        tmpRispostaControlli.setrBoolean(true);
        return tmpRispostaControlli;

    }

}
