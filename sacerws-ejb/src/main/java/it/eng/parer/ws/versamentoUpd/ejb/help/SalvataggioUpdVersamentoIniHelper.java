package it.eng.parer.ws.versamentoUpd.ejb.help;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.LockModeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.AroArchivSec;
import it.eng.parer.entity.AroCompDoc;
import it.eng.parer.entity.AroDoc;
import it.eng.parer.entity.AroLinkUnitaDoc;
import it.eng.parer.entity.AroUnitaDoc;
import it.eng.parer.entity.AroUsoXsdDatiSpec;
import it.eng.parer.entity.AroVersIniArchivSec;
import it.eng.parer.entity.AroVersIniComp;
import it.eng.parer.entity.AroVersIniDatiSpec;
import it.eng.parer.entity.AroVersIniDoc;
import it.eng.parer.entity.AroVersIniLinkUnitaDoc;
import it.eng.parer.entity.AroVersIniUnitaDoc;
import it.eng.parer.entity.constraint.AroVersIniDatiSpec.TiEntitaSacerAroVersIniDatiSpec;
import it.eng.parer.entity.constraint.AroVersIniDatiSpec.TiUsoXsdAroVersIniDatiSpec;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamentoUpd.dto.RispostaWSUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import static it.eng.parer.util.DateUtilsConverter.convert;

/**
 *
 * @author sinatti_s
 */
@Stateless(mappedName = "SalvataggioUpdVersamentoIniHelper")
@LocalBean
public class SalvataggioUpdVersamentoIniHelper extends SalvataggioUpdVersamentoBaseHelper {

    private static final String BASE_ERRMSG = "Eccezione nella lettura della tabella dei metadati iniziali unità documentaria";

    public RispostaControlli getAroVersIniUnitaDoc(long idUnitaDoc) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);
        List<AroVersIniUnitaDoc> aroVersIniUnitaDocs = null;

        try {

            String queryStr = "select ud " + "from AroVersIniUnitaDoc ud "
                    + "where ud.aroUnitaDoc.idUnitaDoc = :idUnitaDoc ";

            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("idUnitaDoc", idUnitaDoc);

            aroVersIniUnitaDocs = query.getResultList();

            if (aroVersIniUnitaDocs.size() == 1) {
                rispostaControlli.setrObject(aroVersIniUnitaDocs.get(0));
                rispostaControlli.setrLong(0);
            }

            rispostaControlli.setrBoolean(true);// query is good
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "SalvataggioUpdIniVersamentoHelper.getAroVersIniUnitaDoc: " + e.getMessage()));
            getLogger().error(BASE_ERRMSG, e);
        }
        return rispostaControlli;
    }

    public RispostaControlli getAroVersIniDoc(long idVersIniUnitaDoc, long idDoc) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);
        List<AroVersIniDoc> aroVersIniDocs = null;

        try {

            String queryStr = "select ud " + "from AroVersIniDoc ud "
                    + "where ud.aroVersIniUnitaDoc.idVersIniUnitaDoc = :idVersIniUnitaDoc "
                    + "AND ud.aroDoc.idDoc = :idDoc ";

            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("idVersIniUnitaDoc", idVersIniUnitaDoc);
            query.setParameter("idDoc", idDoc);

            aroVersIniDocs = query.getResultList();

            if (aroVersIniDocs.size() == 1) {
                rispostaControlli.setrObject(aroVersIniDocs.get(0));
                rispostaControlli.setrLong(0);
            }

            rispostaControlli.setrBoolean(true);// query is good
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "SalvataggioUpdIniVersamentoHelper.getAroVersIniDoc: " + e.getMessage()));
            getLogger().error(BASE_ERRMSG, e);
        }
        return rispostaControlli;
    }

    public RispostaControlli getAroVersIniComp(long idVersIniDoc, long idCompDoc) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);
        List<AroVersIniComp> aroVersIniComps = null;

        try {

            String queryStr = "select ud " + "from AroVersIniComp ud "
                    + "where ud.aroVersIniDoc.idVersIniDoc = :idVersIniDoc "
                    + "AND ud.aroCompDoc.idCompDoc = :idCompDoc ";

            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("idVersIniDoc", idVersIniDoc);
            query.setParameter("idCompDoc", idCompDoc);

            aroVersIniComps = query.getResultList();

            if (aroVersIniComps.size() == 1) {
                rispostaControlli.setrObject(aroVersIniComps.get(0));
                rispostaControlli.setrLong(0);
            }

            rispostaControlli.setrBoolean(true);// query is good
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "SalvataggioUpdIniVersamentoHelper.getAroVersIniComp: " + e.getMessage()));
            getLogger().error(BASE_ERRMSG, e);
        }
        return rispostaControlli;
    }

    /**
     *
     * @param rispostaWs
     *            risposta ws {@link RispostaWSUpdVers}
     * @param versamento
     *            oggetto versamento {@link UpdVersamentoExt}
     * @param sessione
     *            sessione "fake" di tipo {@link SyncFakeSessn}
     * @param tmpAroUnitaDoc
     *            entity {@link AroUnitaDoc} "temporanea" (in lavorazione)
     * @param svf
     *            dto con dati controlli {@link StrutturaUpdVers}
     *
     * @return RispostaControlli con risultato operazione di persistanza su DB
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviAroIniUnitaDoc(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione, AroUnitaDoc tmpAroUnitaDoc, StrutturaUpdVers svf) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);
        // salvo i metadati iniziali UD
        try {
            AroVersIniUnitaDoc tmpVersIniUnitaDoc = new AroVersIniUnitaDoc();

            // FK UD
            tmpVersIniUnitaDoc.setAroUnitaDoc(tmpAroUnitaDoc);

            // INFO Classificazione
            tmpVersIniUnitaDoc.setDsClassifPrinc(tmpAroUnitaDoc.getDsClassifPrinc());
            tmpVersIniUnitaDoc.setCdFascicPrinc(tmpAroUnitaDoc.getCdFascicPrinc());
            tmpVersIniUnitaDoc.setDsOggettoFascicPrinc(tmpAroUnitaDoc.getDsOggettoFascicPrinc());
            tmpVersIniUnitaDoc.setCdSottofascicPrinc(tmpAroUnitaDoc.getCdSottofascicPrinc());
            tmpVersIniUnitaDoc.setDsOggettoSottofascicPrinc(tmpAroUnitaDoc.getDsOggettoFascicPrinc());

            // INFO Profilo UD
            tmpVersIniUnitaDoc.setDlOggettoUnitaDoc(tmpAroUnitaDoc.getDlOggettoUnitaDoc());
            tmpVersIniUnitaDoc.setDtRegUnitaDoc(tmpAroUnitaDoc.getDtRegUnitaDoc());

            entityManager.persist(tmpVersIniUnitaDoc);
            tmpRispostaControlli.setrObject(tmpVersIniUnitaDoc);
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio metadati iniziali unità documentaria: "
                            + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviAroIniArchivSec(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione, AroUnitaDoc tmpAroUnitaDoc, AroVersIniUnitaDoc tmpAroVersIniUnitaDoc,
            StrutturaUpdVers svf) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {

            for (AroArchivSec tmpAroArchivSec : tmpAroUnitaDoc.getAroArchivSecs()) {
                AroVersIniArchivSec tmpAroVersIniArchivSec = new AroVersIniArchivSec();

                // FK al versamento unita doc
                tmpAroVersIniArchivSec.setAroVersIniUnitaDoc(tmpAroVersIniUnitaDoc);
                // Info su archiviazione secondaria definite dall’archivazione secondaria (ds_classif, cd_fascic,
                // ds_oggetto_fascic, cd_sottofascic, ds_oggetto_sottofascic)
                tmpAroVersIniArchivSec.setDsClassif(tmpAroArchivSec.getDsClassif());
                tmpAroVersIniArchivSec.setCdFascic(tmpAroArchivSec.getCdFascic());
                tmpAroVersIniArchivSec.setDsOggettoFascic(tmpAroArchivSec.getDsOggettoFascic());
                tmpAroVersIniArchivSec.setCdSottofascic(tmpAroArchivSec.getCdSottofascic());
                tmpAroVersIniArchivSec.setDsOggettoSottofascic(tmpAroArchivSec.getDsOggettoSottofascic());

                // persist
                entityManager.persist(tmpAroVersIniArchivSec);
            }

            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio metadati iniziali archivio secondario: "
                            + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviAroIniLinkUnitaDoc(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione, AroUnitaDoc tmpAroUnitaDoc, AroVersIniUnitaDoc tmpAroVersIniUnitaDoc,
            StrutturaUpdVers svf) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {

            for (AroLinkUnitaDoc tmpAroLinkUnitaDoc : tmpAroUnitaDoc.getAroLinkUnitaDocs()) {
                AroVersIniLinkUnitaDoc tmpAroVersIniLinkUnitaDoc = new AroVersIniLinkUnitaDoc();

                // FK al versamento unita doc
                tmpAroVersIniLinkUnitaDoc.setAroVersIniUnitaDoc(tmpAroVersIniUnitaDoc);
                // registro, anno e numero del documento collegato
                tmpAroVersIniLinkUnitaDoc.setCdKeyUnitaDocLink(tmpAroLinkUnitaDoc.getCdKeyUnitaDocLink());
                tmpAroVersIniLinkUnitaDoc.setAaKeyUnitaDocLink(tmpAroLinkUnitaDoc.getAaKeyUnitaDocLink());
                tmpAroVersIniLinkUnitaDoc
                        .setCdRegistroKeyUnitaDocLink(tmpAroLinkUnitaDoc.getCdRegistroKeyUnitaDocLink());
                // descrizione del collegamento
                tmpAroVersIniLinkUnitaDoc.setDsLinkUnitaDoc(tmpAroLinkUnitaDoc.getDsLinkUnitaDoc());
                // identificatore dell’unità doc collegata
                tmpAroVersIniLinkUnitaDoc.setAroVersUnitaDocLink(tmpAroLinkUnitaDoc.getAroUnitaDocLink());

                // persist
                entityManager.persist(tmpAroVersIniLinkUnitaDoc);
            }

            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio metadati iniziali documenti collegati: "
                            + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviAroIniDatiSpecUd(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione, AroVersIniUnitaDoc tmpAroVersIniUnitaDoc, AroUsoXsdDatiSpec tmpAroUsoXsdDatiSpec,
            StrutturaUpdVers svf) {
        return scriviAroIniDatiSpec(rispostaWs, versamento, sessione, tmpAroVersIniUnitaDoc, null, null,
                tmpAroUsoXsdDatiSpec, svf);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviAroIniDatiSpecDoc(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione, AroVersIniUnitaDoc tmpAroVersIniUnitaDoc, AroVersIniDoc tmpAroVersIniDoc,
            AroVersIniComp tmpAroVersIniComp, AroUsoXsdDatiSpec tmpAroUsoXsdDatiSpec, StrutturaUpdVers svf) {
        return scriviAroIniDatiSpec(rispostaWs, versamento, sessione, tmpAroVersIniUnitaDoc, tmpAroVersIniDoc,
                tmpAroVersIniComp, tmpAroUsoXsdDatiSpec, svf);
    }

    private RispostaControlli scriviAroIniDatiSpec(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione, AroVersIniUnitaDoc tmpAroVersIniUnitaDoc, AroVersIniDoc tmpAroVersIniDoc,
            AroVersIniComp tmpAroVersIniComp, AroUsoXsdDatiSpec tmpAroUsoXsdDatiSpec, StrutturaUpdVers svf) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {

            AroVersIniDatiSpec tmpAroVersIniDatiSpec = new AroVersIniDatiSpec();

            // FK al versamento unita doc
            tmpAroVersIniDatiSpec.setAroVersIniUnitaDoc(tmpAroVersIniUnitaDoc);

            tmpAroVersIniDatiSpec.setAroVersIniDoc(tmpAroVersIniDoc);

            tmpAroVersIniDatiSpec.setAroVersIniComp(tmpAroVersIniComp);

            // FK
            tmpAroVersIniDatiSpec.setIdStrut(new BigDecimal(svf.getIdStruttura()));

            tmpAroVersIniDatiSpec.setDtReg(convert(sessione.getTmApertura()));

            // tipo di uso del xsd pari a VERS
            tmpAroVersIniDatiSpec.setTiUsoXsd(TiUsoXsdAroVersIniDatiSpec.valueOf(tmpAroUsoXsdDatiSpec.getTiUsoXsd()));
            // tipo entita sacer pari a UNI_DOC
            tmpAroVersIniDatiSpec
                    .setTiEntitaSacer(TiEntitaSacerAroVersIniDatiSpec.valueOf(tmpAroUsoXsdDatiSpec.getTiEntitaSacer()));
            // FK alla versione XSD definito dal record ARO_USO_XSD_DATI_SPEC
            tmpAroVersIniDatiSpec.setDecXsdDatiSpec(tmpAroUsoXsdDatiSpec.getDecXsdDatiSpec());
            //

            tmpRispostaControlli = generaXmlDatiSpecFromAroUnitaDoc(
                    tmpAroUsoXsdDatiSpec.getDecXsdDatiSpec().getCdVersioneXsd(),
                    tmpAroUsoXsdDatiSpec.getDecXsdDatiSpec().getTiUsoXsd(),
                    tmpAroUsoXsdDatiSpec.getDecXsdDatiSpec().getDecXsdAttribDatiSpecs(),
                    tmpAroUsoXsdDatiSpec.getIdUsoXsdDatiSpec(), svf.getIdStruttura());

            // in caso di errore esecuzione query per ricerva valori su attributo
            if (!tmpRispostaControlli.isrBoolean()) {
                return tmpRispostaControlli;
            }

            tmpAroVersIniDatiSpec.setBlXmlDatiSpec(tmpRispostaControlli.getrString());

            // persist
            entityManager.persist(tmpAroVersIniDatiSpec);
            // add on list
            tmpAroVersIniUnitaDoc.getAroVersIniDatiSpecs().add(tmpAroVersIniDatiSpec);

            if (tmpAroVersIniDoc != null) {
                tmpAroVersIniDoc.getAroVersIniDatiSpecs().add(tmpAroVersIniDatiSpec);
            }

            if (tmpAroVersIniComp != null) {
                tmpAroVersIniComp.getAroVersIniDatiSpecs().add(tmpAroVersIniDatiSpec);
            }

            //
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio metadati iniziali dati specifici: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviAroIniDoc(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione, AroVersIniUnitaDoc tmpAroVersIniUnitaDoc, long idDoc, StrutturaUpdVers svf) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {

            AroVersIniDoc tmpVersIniDoc = new AroVersIniDoc();

            // FK al versamento iniziale dell’unita doc
            tmpVersIniDoc.setAroVersIniUnitaDoc(tmpAroVersIniUnitaDoc);

            // 0. si assume LOCK esclusivo su DOC da aggiornare
            // find AroDoc
            Map<String, Object> properties = new HashMap<>();
            properties.put("javax.persistence.lock.timeout", 25);
            AroDoc aroDoc = entityManager.find(AroDoc.class, idDoc, LockModeType.PESSIMISTIC_WRITE, properties);

            // info su profilo documento definite dal versamento documento (dl_doc e
            // ds_autore_doc)
            tmpVersIniDoc.setDlDoc(aroDoc.getDlDoc());
            tmpVersIniDoc.setDsAutoreDoc(aroDoc.getDsAutoreDoc());

            tmpVersIniDoc.setAroDoc(aroDoc);

            entityManager.persist(tmpVersIniDoc);
            tmpRispostaControlli.setrObject(tmpVersIniDoc);
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio metadati iniziali documento: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviAroIniComp(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
            SyncFakeSessn sessione, AroVersIniDoc tmpAroVersIniDoc, long idCompDoc, StrutturaUpdVers svf) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {

            AroVersIniComp tmpVersIniComp = new AroVersIniComp();

            // FK al versamento iniziale dell’unita doc
            tmpVersIniComp.setAroVersIniDoc(tmpAroVersIniDoc);

            // si assume LOCK esclusivo su COMP da aggiornare
            // find AroCompDoc
            Map<String, Object> properties = new HashMap<>();
            properties.put("javax.persistence.lock.timeout", 25);
            AroCompDoc aroCompDoc = entityManager.find(AroCompDoc.class, idCompDoc, LockModeType.PESSIMISTIC_WRITE,
                    properties);

            // info su componente definite da tag “Componente” (e relativi tag figli) del
            // XML in input (ds_nome_comp_vers, dl_urn_comp_vers e ds_id_comp_vers)
            tmpVersIniComp.setDlUrnCompVers(aroCompDoc.getDlUrnCompVers());
            tmpVersIniComp.setDsIdCompVers(aroCompDoc.getDsIdCompVers());
            tmpVersIniComp.setDsNomeCompVers(aroCompDoc.getDsNomeCompVers());

            tmpVersIniComp.setAroCompDoc(aroCompDoc);

            entityManager.persist(tmpVersIniComp);
            tmpRispostaControlli.setrObject(tmpVersIniComp);
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio metadati iniziali documento: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(SalvataggioUpdVersamentoIniHelper.class);
    }
}
