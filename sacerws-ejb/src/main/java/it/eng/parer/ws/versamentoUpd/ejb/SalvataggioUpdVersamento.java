/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.ejb;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.AroUnitaDoc;
import it.eng.parer.entity.AroUpdUnitaDoc;
import it.eng.parer.entity.AroUsoXsdDatiSpec;
import it.eng.parer.entity.AroVersIniComp;
import it.eng.parer.entity.AroVersIniDoc;
import it.eng.parer.entity.AroVersIniUnitaDoc;
import it.eng.parer.entity.MonKeyTotalUdKo;
import it.eng.parer.entity.VrsUpdUnitaDocKo;
import it.eng.parer.entity.constraint.AroUpdDatiSpecUnitaDoc.TiEntitaAroUpdDatiSpecUnitaDoc;
import it.eng.parer.entity.constraint.AroUpdDatiSpecUnitaDoc.TiUsoXsdAroUpdDatiSpecUnitaDoc;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.ControlliWSBundle;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.PayLoad;
import it.eng.parer.ws.utils.ejb.JmsProducerUtilEjb;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamentoUpd.dto.CompRapportoUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.RispostaWSUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.UpdComponenteVers;
import it.eng.parer.ws.versamentoUpd.dto.UpdDocumentoVers;
import it.eng.parer.ws.versamentoUpd.ejb.help.LogSessioneUpdVersamentoHelper;
import it.eng.parer.ws.versamentoUpd.ejb.help.SalvataggioPregVersamentoAroHelper;
import it.eng.parer.ws.versamentoUpd.ejb.help.SalvataggioUpdVersamentoAroHelper;
import it.eng.parer.ws.versamentoUpd.ejb.help.SalvataggioUpdVersamentoIniHelper;
import it.eng.parer.ws.versamentoUpd.ejb.help.SalvataggioUpdVersamentoUpdHelper;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;

/**
 *
 * @author sinatti_s
 */
@Stateless(mappedName = "SalvataggioUpdVersamento")
@LocalBean
public class SalvataggioUpdVersamento {

    static final Logger log = LoggerFactory.getLogger(SalvataggioUpdVersamento.class);
    //
    // MEV#27048
    @Resource(mappedName = "jms/ProducerConnectionFactory")
    private ConnectionFactory connectionFactory;
    @Resource(mappedName = "jms/ElenchiDaElabInAttesaSchedQueue")
    private Queue queue;
    // end MEV#27048
    //
    //
    @EJB
    private SalvataggioUpdVersamentoIniHelper salvataggioUpdIniVersamentoHelper;

    @EJB
    private SalvataggioUpdVersamentoUpdHelper salvataggioUpdVersamentoHelper;

    @EJB
    private SalvataggioUpdVersamentoAroHelper salvataggioUpdAroVersamentoHelper;

    @EJB
    private SalvataggioPregVersamentoAroHelper salvataggioPregVersamentoAroHelper;

    @EJB
    private LogSessioneUpdVersamentoHelper updLogSessioneHelper;

    // MEV#27048
    @EJB
    private JmsProducerUtilEjb jmsProducerUtilEjb;
    // end MEV#27048

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @Resource
    private EJBContext context;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean salvaAggiornamento(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione) {
        // salva aggiornamento
        // eventualmente rimozione su aggiornamento KO relativo a tentativi
        // precedenti di versamento
        RispostaControlli tmpRispostaControlli = null;
        tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        CompRapportoUpdVers myEsito = rispostaWs.getCompRapportoUpdVers();

        try {
            // 0. si assume LOCK esclusivo su UD da aggiornare (in modo tale che altri
            // dovranno aspettare il rilascio del LOCK che avverrà al termine di tutte le
            // operazioni di aggiornamento)
            Map<String, Object> properties = new HashMap();
            properties.put("javax.persistence.lock.timeout", 25);
            AroUnitaDoc tmpAroUnitaDoc = entityManager.find(AroUnitaDoc.class, strutturaUpdVers.getIdUd(),
                    LockModeType.PESSIMISTIC_WRITE, properties);

            // TODO: valutare se necessario effettuare un check sull'esistenza di
            // AroUnitaDoc e nel caso gestirla con erorre
            if (tmpAroUnitaDoc == null) {
                context.setRollbackOnly();
                tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
                tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                        "Errore interno nella lettura dell'unità documentaria da aggiornare"));
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            /*
             * §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
             * §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
             * 
             * Gestione KEY NORMALIZED / URN PREGRESSI
             * 
             * 
             * §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
             * §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
             */
            // 1. se il numero normalizzato sull’unità doc nel DB è nullo ->
            // il sistema aggiorna ARO_UNITA_DOC
            tmpRispostaControlli = salvataggioPregVersamentoAroHelper.salvaCdKeyNormUnitaDocumentaria(rispostaWs,
                    versamento, sessione, tmpAroUnitaDoc, strutturaUpdVers);
            // errore query
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }
            // 2. verifica pregresso
            tmpRispostaControlli = salvataggioPregVersamentoAroHelper.aggiornaPregCompDocUrnSip(rispostaWs, versamento,
                    sessione, tmpAroUnitaDoc, strutturaUpdVers);
            // errore query
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            /*
             * §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
             * §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
             * 
             * Gestione Etity *INI*
             * 
             * Metadati Iniziali
             * 
             * §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
             * §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
             */

            // 1. creazione dei metadati iniziali (INI) per l'UD (se ancora non esistono)
            // ATTENZIONE: questa operazione dovrà essere fatta ogni volta in quanto all'UD
            // iniziale possono essere, successivamente al primo aggiornamento,
            // inserite nuove "parti" (= allegati -> vedi servizio AggAllegatiSync)
            AroVersIniUnitaDoc tmpAroVersIniUnitaDoc = null;
            // ricerca aggiornamento
            tmpRispostaControlli = salvataggioUpdIniVersamentoHelper
                    .getAroVersIniUnitaDoc(tmpAroUnitaDoc.getIdUnitaDoc());
            // in caso di errore esecuzione query
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // se non esistono metadati iniziali li vado a creare ...
            if (tmpRispostaControlli.getrLong() == -1) {
                // salvo dati iniziali
                tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.scriviAroIniUnitaDoc(rispostaWs, versamento,
                        sessione, tmpAroUnitaDoc, strutturaUpdVers);
                if (!tmpRispostaControlli.isrBoolean()) {
                    context.setRollbackOnly();
                    this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                    return false;
                }

                tmpAroVersIniUnitaDoc = (AroVersIniUnitaDoc) tmpRispostaControlli.getrObject();

                // per ogni record in ARO_ARCHIV_SEC relativo all’unita doc da aggiornare
                tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.scriviAroIniArchivSec(rispostaWs, versamento,
                        sessione, tmpAroUnitaDoc, tmpAroVersIniUnitaDoc, strutturaUpdVers);
                if (!tmpRispostaControlli.isrBoolean()) {
                    context.setRollbackOnly();
                    this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                    return false;
                }

                // per ogni record in ARO_LINK_UNITA_DOC relativo all’unita doc da aggiornare
                tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.scriviAroIniLinkUnitaDoc(rispostaWs,
                        versamento, sessione, tmpAroUnitaDoc, tmpAroVersIniUnitaDoc, strutturaUpdVers);
                if (!tmpRispostaControlli.isrBoolean()) {
                    context.setRollbackOnly();
                    this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                    return false;
                }

                // se record ARO_USO_XSD_DATI_SPEC relativo all’unita doc da aggiornare con tipo
                // uso = VERS e tipo entita sacer = UNI_DOC e’ presente
                tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.checkUsoXsdDatiSpecifici(
                        tmpAroUnitaDoc.getIdUnitaDoc(), CostantiDB.TipiUsoDatiSpec.VERS,
                        CostantiDB.TipiEntitaSacer.UNI_DOC);

                // errore query
                if (!tmpRispostaControlli.isrBoolean()) {
                    context.setRollbackOnly();
                    this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                    return false;
                }

                // è presente ...
                if (tmpRispostaControlli.getrLong() != -1) {
                    AroUsoXsdDatiSpec tmpUsoXsdDatiSpec = (AroUsoXsdDatiSpec) tmpRispostaControlli.getrObject();

                    // per ogni record in ARO_VALORE_ATTRIB_DATI_SPEC relativo al xsd usato
                    tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.scriviAroIniDatiSpecUd(rispostaWs,
                            versamento, sessione, tmpAroVersIniUnitaDoc, tmpUsoXsdDatiSpec, strutturaUpdVers);
                    if (!tmpRispostaControlli.isrBoolean()) {
                        context.setRollbackOnly();
                        this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                        return false;
                    }

                } // AroUsoXsdDatiSpec

                // se record ARO_USO_XSD_DATI_SPEC relativo all’unita doc da aggiornare con tipo
                // uso = VERS e tipo entita sacer = UNI_DOC e’ presente
                tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.checkUsoXsdDatiSpecifici(
                        tmpAroUnitaDoc.getIdUnitaDoc(), CostantiDB.TipiUsoDatiSpec.MIGRAZ,
                        CostantiDB.TipiEntitaSacer.UNI_DOC);

                // errore query
                if (!tmpRispostaControlli.isrBoolean()) {
                    context.setRollbackOnly();
                    this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                    return false;
                }

                // è presente ...
                if (tmpRispostaControlli.getrLong() != -1) {
                    AroUsoXsdDatiSpec tmpUsoXsdDatiSpec = (AroUsoXsdDatiSpec) tmpRispostaControlli.getrObject();

                    // per ogni record in ARO_VALORE_ATTRIB_DATI_SPEC relativo al xsd usato
                    tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.scriviAroIniDatiSpecUd(rispostaWs,
                            versamento, sessione, tmpAroVersIniUnitaDoc, tmpUsoXsdDatiSpec, strutturaUpdVers);
                    if (!tmpRispostaControlli.isrBoolean()) {
                        context.setRollbackOnly();
                        this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                        return false;
                    }
                } // AroUsoXsdDatiSpec

            } else {
                /* se non esistono aggiornamenti per l’unità doc da aggiornare */
                tmpAroVersIniUnitaDoc = (AroVersIniUnitaDoc) tmpRispostaControlli.getrObject();

            }

            // per ogni documento da aggiornare contenuto in unita doc da aggiornare
            for (UpdDocumentoVers updDocumentoVers : strutturaUpdVers.getDocumentiAttesi()) {
                AroVersIniDoc tmpAroVersIniDoc = null;
                // se per il versamento iniziale dell’unita doc non e’ presente il versamento
                // iniziale del documento
                tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.getAroVersIniDoc(
                        tmpAroVersIniUnitaDoc.getIdVersIniUnitaDoc(), updDocumentoVers.getIdRecDocumentoDB());

                // errore esecuzione query
                if (!tmpRispostaControlli.isrBoolean()) {
                    context.setRollbackOnly();
                    this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                    return false;
                }

                // se NON ha trovato metadati iniziali per il documento da aggiornare persisto
                // altrimenti skip next
                if (tmpRispostaControlli.getrLong() == -1) {
                    //
                    tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.scriviAroIniDoc(rispostaWs, versamento,
                            sessione, tmpAroVersIniUnitaDoc, updDocumentoVers.getIdRecDocumentoDB(), strutturaUpdVers);
                    if (!tmpRispostaControlli.isrBoolean()) {
                        context.setRollbackOnly();
                        this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                        return false;
                    }

                    // recupero entity dalla creazione
                    tmpAroVersIniDoc = (AroVersIniDoc) tmpRispostaControlli.getrObject();

                    tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.checkUsoXsdDatiSpecifici(
                            updDocumentoVers.getIdRecDocumentoDB(), CostantiDB.TipiUsoDatiSpec.VERS,
                            CostantiDB.TipiEntitaSacer.DOC);
                    // errore query
                    if (!tmpRispostaControlli.isrBoolean()) {
                        context.setRollbackOnly();
                        this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                        return false;
                    }

                    // è presente ...
                    if (tmpRispostaControlli.getrLong() != -1) {
                        AroUsoXsdDatiSpec tmpUsoXsdDatiSpec = (AroUsoXsdDatiSpec) tmpRispostaControlli.getrObject();

                        // per ogni record in ARO_VALORE_ATTRIB_DATI_SPEC relativo al xsd usato
                        tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.scriviAroIniDatiSpecDoc(rispostaWs,
                                versamento, sessione, tmpAroVersIniUnitaDoc, tmpAroVersIniDoc, null, tmpUsoXsdDatiSpec,
                                strutturaUpdVers);
                        if (!tmpRispostaControlli.isrBoolean()) {
                            context.setRollbackOnly();
                            this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                            return false;
                        }
                    } // AroUsoXsdDatiSpec

                    tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.checkUsoXsdDatiSpecifici(
                            updDocumentoVers.getIdRecDocumentoDB(), CostantiDB.TipiUsoDatiSpec.MIGRAZ,
                            CostantiDB.TipiEntitaSacer.DOC);

                    if (!tmpRispostaControlli.isrBoolean()) {
                        context.setRollbackOnly();
                        this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                        return false;
                    }

                    // è presente ...
                    if (tmpRispostaControlli.getrLong() != -1) {
                        AroUsoXsdDatiSpec tmpUsoXsdDatiSpec = (AroUsoXsdDatiSpec) tmpRispostaControlli.getrObject();

                        // per ogni record in ARO_VALORE_ATTRIB_DATI_SPEC relativo al xsd usato
                        tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.scriviAroIniDatiSpecDoc(rispostaWs,
                                versamento, sessione, tmpAroVersIniUnitaDoc, tmpAroVersIniDoc, null, tmpUsoXsdDatiSpec,
                                strutturaUpdVers);
                        if (!tmpRispostaControlli.isrBoolean()) {
                            context.setRollbackOnly();
                            this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                            return false;
                        }
                    } // AroUsoXsdDatiSpec
                } /* ini doc */ else {
                    // recupero entity esistente
                    tmpAroVersIniDoc = (AroVersIniDoc) tmpRispostaControlli.getrObject();
                }

                // per ogni componente da aggiornare nel documento da aggiornare
                for (UpdComponenteVers updComponenteVers : updDocumentoVers.getUpdComponentiAttesi()) {

                    AroVersIniComp tmpAroVersIniComp = null;
                    // se per il versamento iniziale dell’unita doc non e’ presente il versamento
                    // iniziale del documento
                    tmpRispostaControlli = salvataggioUpdIniVersamentoHelper
                            .getAroVersIniComp(tmpAroVersIniDoc.getIdVersIniDoc(), updComponenteVers.getIdRecDB());

                    // errore esecuzione query
                    if (!tmpRispostaControlli.isrBoolean()) {
                        context.setRollbackOnly();
                        this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                        return false;
                    }

                    // se NON ha trovato metadati iniziali per il documento da aggiornare
                    if (tmpRispostaControlli.getrLong() == -1) {
                        //
                        tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.scriviAroIniComp(rispostaWs,
                                versamento, sessione, tmpAroVersIniDoc, updComponenteVers.getIdRecDB(),
                                strutturaUpdVers);
                        if (!tmpRispostaControlli.isrBoolean()) {
                            context.setRollbackOnly();
                            this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                            return false;
                        }

                        // recupero entity o perché esistente o dalla creazione
                        tmpAroVersIniComp = (AroVersIniComp) tmpRispostaControlli.getrObject();

                        tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.checkUsoXsdDatiSpecifici(
                                updComponenteVers.getIdRecDB(), CostantiDB.TipiUsoDatiSpec.VERS,
                                CostantiDB.TipiEntitaSacer.COMP);

                        // errore query
                        if (!tmpRispostaControlli.isrBoolean()) {
                            context.setRollbackOnly();
                            this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                            return false;
                        }

                        // è presente ...
                        if (tmpRispostaControlli.getrLong() != -1) {

                            AroUsoXsdDatiSpec tmpUsoXsdDatiSpec = (AroUsoXsdDatiSpec) tmpRispostaControlli.getrObject();

                            // per ogni record in ARO_VALORE_ATTRIB_DATI_SPEC relativo al xsd usato
                            tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.scriviAroIniDatiSpecDoc(rispostaWs,
                                    versamento, sessione, tmpAroVersIniUnitaDoc, tmpAroVersIniDoc, tmpAroVersIniComp,
                                    tmpUsoXsdDatiSpec, strutturaUpdVers);
                            if (!tmpRispostaControlli.isrBoolean()) {
                                context.setRollbackOnly();
                                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                                return false;
                            }
                        } // AroUsoXsdDatiSpec

                        tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.checkUsoXsdDatiSpecifici(
                                updComponenteVers.getIdRecDB(), CostantiDB.TipiUsoDatiSpec.MIGRAZ,
                                CostantiDB.TipiEntitaSacer.COMP);

                        // errore query
                        if (!tmpRispostaControlli.isrBoolean()) {
                            context.setRollbackOnly();
                            this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                            return false;
                        }

                        // è presente ...
                        if (tmpRispostaControlli.getrLong() != -1) {

                            AroUsoXsdDatiSpec tmpUsoXsdDatiSpec = (AroUsoXsdDatiSpec) tmpRispostaControlli.getrObject();

                            // per ogni record in ARO_VALORE_ATTRIB_DATI_SPEC relativo al xsd usato
                            tmpRispostaControlli = salvataggioUpdIniVersamentoHelper.scriviAroIniDatiSpecDoc(rispostaWs,
                                    versamento, sessione, tmpAroVersIniUnitaDoc, tmpAroVersIniDoc, tmpAroVersIniComp,
                                    tmpUsoXsdDatiSpec, strutturaUpdVers);
                            if (!tmpRispostaControlli.isrBoolean()) {
                                context.setRollbackOnly();
                                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                                return false;
                            }
                        } // AroUsoXsdDatiSpec

                    } // ini componente

                } // for componente

            } // for documento

            // cerco se esiste una precedente registrazione fallita per
            // l'aggiornamento dell'unità documentaria, lo blocco in modo esclusivo: lo devo
            // rimuovere
            // e devo riallocare tutte le sue sessioni all'aggiornamento che sto creando
            tmpRispostaControlli = updLogSessioneHelper.cercaAggiornamentoKo(rispostaWs, versamento, sessione);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            VrsUpdUnitaDocKo tmpUpdUnitaDocKo = null;
            if (tmpRispostaControlli.getrObject() != null) {
                tmpUpdUnitaDocKo = (VrsUpdUnitaDocKo) tmpRispostaControlli.getrObject();
            }

            /*
             * §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
             * §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
             * 
             * Gestione Etity *UPD*
             * 
             * Aggiornamento §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
             * §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
             */

            long pgAroUpdUnitaDoc = 1;
            tmpRispostaControlli = salvataggioUpdVersamentoHelper
                    .getNextPgAroUpdUnitaDoc(tmpAroUnitaDoc.getIdUnitaDoc());
            if (!tmpRispostaControlli.isrBoolean() && tmpRispostaControlli.getrLong() != -1) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            } else {
                pgAroUpdUnitaDoc = tmpRispostaControlli.getrLong();
                // aggiorno la risposta con il progressivo
                myEsito.setProgressivoAggiornamento(BigInteger.valueOf(pgAroUpdUnitaDoc));
            }

            // 1. inserisci record in ARO_UPD_UNITA_DOC, ...:
            tmpRispostaControlli = salvataggioUpdVersamentoHelper.scriviAroUpdUnitaDoc(rispostaWs, versamento, sessione,
                    tmpAroUnitaDoc, pgAroUpdUnitaDoc, strutturaUpdVers);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // 2. inserisci record in ARO_XML_UPD_UNITA_DOC, ...:
            AroUpdUnitaDoc tmpAroUpdUnitaDoc = (AroUpdUnitaDoc) tmpRispostaControlli.getrObject();
            tmpRispostaControlli = salvataggioUpdVersamentoHelper.scriviAroXmlUpdUnitaDoc(rispostaWs, versamento,
                    sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, strutturaUpdVers);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // 3. per ogni controllo svolto sull’unità doc da aggiornare con esito = WARNING
            // (in ordine di numero d’ordine del controllo)
            tmpRispostaControlli = salvataggioUpdVersamentoHelper.scriviAroWarnUpdUnitaDocForUD(rispostaWs, versamento,
                    sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, strutturaUpdVers);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }
            // 4. fine per ogni controllo svolto sull’unità doc
            // 5. per ogni documento da aggiornare
            tmpRispostaControlli = salvataggioUpdVersamentoHelper.scriviAroWarnUpdUnitaDocForDoc(rispostaWs, versamento,
                    sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, strutturaUpdVers);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }
            // 5.3. per ogni componente da aggiornare
            tmpRispostaControlli = salvataggioUpdVersamentoHelper.scriviAroWarnUpdUnitaDocForComp(rispostaWs,
                    versamento, sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, strutturaUpdVers);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }
            // 5.4. fine per ogni componente da aggiornare
            // 6. fine per ogni documento da aggiornare
            // 7. per ogni tag “FascicoloSecondario“ del XML in input
            tmpRispostaControlli = salvataggioUpdVersamentoHelper.scriviAroUpdArchivSec(rispostaWs, versamento,
                    sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, strutturaUpdVers);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }
            // 8. fine per ogni ogni tag “FascicoloSecondario “
            // 9. per ogni documento collegato definito su unita doc da aggiornare
            tmpRispostaControlli = salvataggioUpdVersamentoHelper.scriviAroUpdLinkUnitaDoc(rispostaWs, versamento,
                    sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, strutturaUpdVers);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }
            // 11. se nel XML in input il tag “DatiSpecifici” e’ definito
            tmpRispostaControlli = salvataggioUpdVersamentoHelper.scriviAroUpdDatiSpecUnitaDoc(rispostaWs, versamento,
                    sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, TiUsoXsdAroUpdDatiSpecUnitaDoc.VERS,
                    TiEntitaAroUpdDatiSpecUnitaDoc.UPD_UNI_DOC, strutturaUpdVers);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }
            // 12. fine se
            // 13. se nel XML in input il tag “DatiSpecificiMigrazione” e’ definito
            tmpRispostaControlli = salvataggioUpdVersamentoHelper.scriviAroUpdDatiSpecUnitaDoc(rispostaWs, versamento,
                    sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, TiUsoXsdAroUpdDatiSpecUnitaDoc.MIGRAZ,
                    TiEntitaAroUpdDatiSpecUnitaDoc.UPD_UNI_DOC, strutturaUpdVers);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }
            // 14. fine se
            // 15. inserisci record in ELV_UPD_UD_DA_ELAB_ELENCO
            tmpRispostaControlli = salvataggioUpdVersamentoHelper.scriviElvUpdUdDaElabElenco(rispostaWs, versamento,
                    sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, strutturaUpdVers);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }
            // 16. fine se
            // 17. inserisci record in MON_KEY_TOTAL_UD
            tmpRispostaControlli = salvataggioUpdVersamentoHelper.scriviMonKeyTotalUd(rispostaWs, versamento, sessione,
                    tmpAroUnitaDoc, tmpAroUpdUnitaDoc, strutturaUpdVers);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }
            // 17. per ogni documento da aggiornare contenuto in unita doc da aggiornare
            // 17.1. inserisci record in ARO_UPD_DOC_UNITA_DOC
            tmpRispostaControlli = salvataggioUpdVersamentoHelper.scriviAroUpdDocUnitaDoc(rispostaWs, versamento,
                    sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, strutturaUpdVers);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            //

            /*
             * §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
             * §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
             * 
             * Gestione Etity *ARO*
             * 
             * Aggiornamento UD §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
             * §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
             */

            // 1. se stato di conservazione per unita doc = AIP_GENERATO o AIP_FIRMATO o AIP_IN_ARCHIVIO o
            // AIP_IN_CUSTODIA
            tmpRispostaControlli = salvataggioUpdAroVersamentoHelper.aggiornaStatoConservazione(rispostaWs, versamento,
                    sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, strutturaUpdVers);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // 2. se su unità doc da aggiornare il tag “ProfiloArchivistico” e’ da
            // aggiornare
            if (versamento.hasProfiloArchivisticoToUpd()) {
                tmpRispostaControlli = salvataggioUpdAroVersamentoHelper.aggiornaProfiloArchivistico(rispostaWs,
                        versamento, sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, strutturaUpdVers);
                if (!tmpRispostaControlli.isrBoolean()) {
                    context.setRollbackOnly();
                    this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                    return false;
                }
            }
            // 3. se su unità doc da aggiornare il tag “ProfiloUnitaDocumentaria” e’ da aggiornare
            if (versamento.hasProfiloUnitaDocumentariaToUpd()) {
                tmpRispostaControlli = salvataggioUpdAroVersamentoHelper.aggiornaProfiloUnitaDocumentaria(rispostaWs,
                        versamento, sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, strutturaUpdVers);
                if (!tmpRispostaControlli.isrBoolean()) {
                    context.setRollbackOnly();
                    this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                    return false;
                }
            }
            // 4. se su unità doc da aggiornare il tag “DocumentiCollegati” e’ da aggiornare
            if (versamento.hasDocumentiCollegatiToUpd()) {
                tmpRispostaControlli = salvataggioUpdAroVersamentoHelper.aggiornaDocumentiCollegati(rispostaWs,
                        versamento, sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, strutturaUpdVers);
                if (!tmpRispostaControlli.isrBoolean()) {
                    context.setRollbackOnly();
                    this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                    return false;
                }
            }
            // 5. se su unità doc da aggiornare il tag “DatiSpecifici” e’ da aggiornare
            if (versamento.hasDatiSpecificiToBeUpdated()) {
                tmpRispostaControlli = salvataggioUpdAroVersamentoHelper.aggiornaDatiSpecificiUd(rispostaWs, versamento,
                        sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, TiUsoXsdAroUpdDatiSpecUnitaDoc.VERS,
                        strutturaUpdVers);
                if (!tmpRispostaControlli.isrBoolean()) {
                    context.setRollbackOnly();
                    this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                    return false;
                }
            }
            // 6. se su unità doc da aggiornare il tag “DatiSpecificiMigrazione” e’ da aggiornare
            if (versamento.hasDatiSpecificiMigrazioneToUpd()) {
                tmpRispostaControlli = salvataggioUpdAroVersamentoHelper.aggiornaDatiSpecificiUd(rispostaWs, versamento,
                        sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, TiUsoXsdAroUpdDatiSpecUnitaDoc.MIGRAZ,
                        strutturaUpdVers);
                if (!tmpRispostaControlli.isrBoolean()) {
                    context.setRollbackOnly();
                    this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                    return false;
                }
            }

            // 7. per ogni aggiornamento documento contenuto in un aggiornamento unita doc
            if (versamento.hasDocumentiToUpd()) {
                tmpRispostaControlli = salvataggioUpdAroVersamentoHelper.aggiornaDocumento(rispostaWs, versamento,
                        sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, strutturaUpdVers);
                if (!tmpRispostaControlli.isrBoolean()) {
                    context.setRollbackOnly();
                    this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                    return false;
                }
            }

            // 8. aggiorna aggregazioni
            tmpRispostaControlli = salvataggioUpdAroVersamentoHelper.aggiornaAggregazioni(rispostaWs, versamento,
                    sessione, tmpAroUnitaDoc, tmpAroUpdUnitaDoc, strutturaUpdVers);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // al termine della registrazione dell'aggiornamento
            // si provvede ad ereditare tutte le sessioni FALLITE
            // e si cancella il padre
            if (tmpUpdUnitaDocKo != null) {

                MonKeyTotalUdKo tmpMonKeyTotalUdKo = null;
                tmpRispostaControlli = updLogSessioneHelper.lockAndGetMonContaSesUpdUdKo(tmpUpdUnitaDocKo);
                if (!tmpRispostaControlli.isrBoolean()) {
                    // errore su db
                    context.setRollbackOnly();
                    this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                    return false;
                }
                if (tmpRispostaControlli.getrObject() != null) {
                    tmpMonKeyTotalUdKo = (MonKeyTotalUdKo) tmpRispostaControlli.getrObject();
                }

                if (tmpMonKeyTotalUdKo != null) {
                    tmpRispostaControlli = salvataggioUpdVersamentoHelper.ereditaVrsSesUpdUnitaDocKoRisolte(rispostaWs,
                            versamento, sessione, tmpAroUpdUnitaDoc, tmpUpdUnitaDocKo, tmpMonKeyTotalUdKo);
                    if (!tmpRispostaControlli.isrBoolean()) {
                        context.setRollbackOnly();
                        this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                        return false;
                    }
                }
            }

            // MEV#27048
            if (CostantiDB.TipiStatoElementoVersato.IN_ATTESA_SCHED.name()
                    .equals(versamento.getStrutturaUpdVers().getTiStatoUpdElencoVers().name())) {
                PayLoad pl = new PayLoad();
                pl.setTipoEntitaSacer("UPD");
                pl.setStato(CostantiDB.TipiStatoElementoVersato.IN_ATTESA_SCHED.name());
                pl.setId(versamento.getStrutturaUpdVers().getIdRecAggiornamentoDB());
                pl.setIdStrut(versamento.getStrutturaUpdVers().getIdStruttura());
                pl.setAaKeyUnitaDoc(
                        versamento.getVersamento().getUnitaDocumentaria().getIntestazione().getChiave().getAnno());
                pl.setDtCreazione(versamento.getStrutturaUpdVers().getDataVersamento().getTime());

                tmpRispostaControlli = jmsProducerUtilEjb.inviaMessaggioInFormatoJson(connectionFactory, queue, pl,
                        "CodaElenchiDaElabInAttesaSched");
                if (!tmpRispostaControlli.isrBoolean()) {
                    context.setRollbackOnly();
                    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P, tmpRispostaControlli.getDsErr());
                    log.error(
                            "Eccezione nella fase di Invio messaggio alla coda JMS " + tmpRispostaControlli.getDsErr());
                    return false;
                }
            }
            // end MEV#27048

            tmpRispostaControlli.setrBoolean(true);
            // RILASCIO di tutti i LOCK acquisiti al termine della transazione corrente
            entityManager.flush();
        } catch (Exception e) {
            // l'errore di persistenza viene aggiunto alla pila
            // di errori esistenti e in seguito serializzato nell'xml
            // di risposta. Inoltre tenterò di salvare una sessione errata con questi stessi
            // dati.
            context.setRollbackOnly();
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio aggiornamento: " + e.getMessage()));
            this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
            log.error("Errore interno nella fase di salvataggio aggiornamento.", e);
            return false;
        }
        return true;
    }

    private void impostaErrore(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            RispostaControlli tmpRispostaControlli) {
        rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
        rispostaWs.setEsitoWsError(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_ERRORIDB),
                tmpRispostaControlli.getCodErr(), tmpRispostaControlli.getDsErr());
    }

}
