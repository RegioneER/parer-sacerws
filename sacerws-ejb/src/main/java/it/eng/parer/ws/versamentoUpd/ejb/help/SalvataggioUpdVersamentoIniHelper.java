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

package it.eng.parer.ws.versamentoUpd.ejb.help;

import it.eng.parer.entity.*;
import it.eng.parer.entity.constraint.AroVersIniDatiSpec.TiEntitaSacerAroVersIniDatiSpec;
import it.eng.parer.entity.constraint.AroVersIniDatiSpec.TiUsoXsdAroVersIniDatiSpec;
import it.eng.parer.util.Constants;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamentoUpd.dto.RispostaWSUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.eng.parer.util.DateUtilsConverter.convertLocal;
import it.eng.parer.ws.versamento.dto.BackendStorage;
import it.eng.parer.ws.versamentoUpd.dto.DatiSpecLinkOsKeyMap;

/**
 *
 * @author sinatti_s
 */
@SuppressWarnings("unchecked")
@Stateless(mappedName = "SalvataggioUpdVersamentoIniHelper")
@LocalBean
public class SalvataggioUpdVersamentoIniHelper extends SalvataggioUpdVersamentoBaseHelper {

    private static final String BASE_ERRMSG = "Eccezione nella lettura della tabella dei metadati iniziali unità documentaria";

    public RispostaControlli getAroVersIniUnitaDoc(long idUnitaDoc) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);
        List<AroVersIniUnitaDoc> aroVersIniUnitaDocs;

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
     * @param rispostaWs     risposta ws {@link RispostaWSUpdVers}
     * @param versamento     oggetto versamento {@link UpdVersamentoExt}
     * @param sessione       sessione "fake" di tipo {@link SyncFakeSessn}
     * @param tmpAroUnitaDoc entity {@link AroUnitaDoc} "temporanea" (in lavorazione)
     * @param svf            dto con dati controlli {@link StrutturaUpdVers}
     *
     * @return RispostaControlli con risultato operazione di persistanza su DB
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviAroIniUnitaDoc(RispostaWSUpdVers rispostaWs,
            UpdVersamentoExt versamento, SyncFakeSessn sessione, AroUnitaDoc tmpAroUnitaDoc,
            StrutturaUpdVers svf) {
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
            tmpVersIniUnitaDoc
                    .setDsOggettoSottofascicPrinc(tmpAroUnitaDoc.getDsOggettoFascicPrinc());

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
    public RispostaControlli scriviAroIniArchivSec(RispostaWSUpdVers rispostaWs,
            UpdVersamentoExt versamento, SyncFakeSessn sessione, AroUnitaDoc tmpAroUnitaDoc,
            AroVersIniUnitaDoc tmpAroVersIniUnitaDoc, StrutturaUpdVers svf) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {

            for (AroArchivSec tmpAroArchivSec : tmpAroUnitaDoc.getAroArchivSecs()) {
                AroVersIniArchivSec tmpAroVersIniArchivSec = new AroVersIniArchivSec();

                // FK al versamento unita doc
                tmpAroVersIniArchivSec.setAroVersIniUnitaDoc(tmpAroVersIniUnitaDoc);
                // Info su archiviazione secondaria definite dall’archivazione secondaria
                // (ds_classif, cd_fascic,
                // ds_oggetto_fascic, cd_sottofascic, ds_oggetto_sottofascic)
                tmpAroVersIniArchivSec.setDsClassif(tmpAroArchivSec.getDsClassif());
                tmpAroVersIniArchivSec.setCdFascic(tmpAroArchivSec.getCdFascic());
                tmpAroVersIniArchivSec.setDsOggettoFascic(tmpAroArchivSec.getDsOggettoFascic());
                tmpAroVersIniArchivSec.setCdSottofascic(tmpAroArchivSec.getCdSottofascic());
                tmpAroVersIniArchivSec
                        .setDsOggettoSottofascic(tmpAroArchivSec.getDsOggettoSottofascic());

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
    public RispostaControlli scriviAroIniLinkUnitaDoc(RispostaWSUpdVers rispostaWs,
            UpdVersamentoExt versamento, SyncFakeSessn sessione, AroUnitaDoc tmpAroUnitaDoc,
            AroVersIniUnitaDoc tmpAroVersIniUnitaDoc, StrutturaUpdVers svf) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {

            for (AroLinkUnitaDoc tmpAroLinkUnitaDoc : tmpAroUnitaDoc.getAroLinkUnitaDocs()) {
                AroVersIniLinkUnitaDoc tmpAroVersIniLinkUnitaDoc = new AroVersIniLinkUnitaDoc();

                // FK al versamento unita doc
                tmpAroVersIniLinkUnitaDoc.setAroVersIniUnitaDoc(tmpAroVersIniUnitaDoc);
                // registro, anno e numero del documento collegato
                tmpAroVersIniLinkUnitaDoc
                        .setCdKeyUnitaDocLink(tmpAroLinkUnitaDoc.getCdKeyUnitaDocLink());
                tmpAroVersIniLinkUnitaDoc
                        .setAaKeyUnitaDocLink(tmpAroLinkUnitaDoc.getAaKeyUnitaDocLink());
                tmpAroVersIniLinkUnitaDoc.setCdRegistroKeyUnitaDocLink(
                        tmpAroLinkUnitaDoc.getCdRegistroKeyUnitaDocLink());
                // descrizione del collegamento
                tmpAroVersIniLinkUnitaDoc.setDsLinkUnitaDoc(tmpAroLinkUnitaDoc.getDsLinkUnitaDoc());
                // identificatore dell’unità doc collegata
                tmpAroVersIniLinkUnitaDoc
                        .setAroVersUnitaDocLink(tmpAroLinkUnitaDoc.getAroUnitaDocLink());

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
    public RispostaControlli scriviAroIniDatiSpecUd(SyncFakeSessn sessione,
            AroVersIniUnitaDoc tmpAroVersIniUnitaDoc, AroUsoXsdDatiSpec tmpAroUsoXsdDatiSpec,
            BackendStorage backendMetadata,
            Map<DatiSpecLinkOsKeyMap, Map<String, String>> versIniDatiSpecBlob,
            StrutturaUpdVers svf) {

        // MEV#29276
        DatiSpecLinkOsKeyMap key = new DatiSpecLinkOsKeyMap(
                tmpAroVersIniUnitaDoc.getIdVersIniUnitaDoc(),
                TiEntitaSacerAroVersIniDatiSpec.UNI_DOC.name());
        Map<String, String> versIniDatiSpecUdBlob = (versIniDatiSpecBlob.containsKey(key))
                ? versIniDatiSpecBlob.get(key)
                : new HashMap<>();
        // end MEV#29276

        RispostaControlli tmpRispostaControlli = scriviAroIniDatiSpec(sessione,
                tmpAroVersIniUnitaDoc, null, null, tmpAroUsoXsdDatiSpec, backendMetadata,
                versIniDatiSpecUdBlob, svf);

        // in caso di errore esecuzione query
        if (!tmpRispostaControlli.isrBoolean()) {
            return tmpRispostaControlli;
        }

        // MEV#29276
        if (backendMetadata.isObjectStorage() && !versIniDatiSpecBlob.containsKey(key)) {
            versIniDatiSpecBlob.put(key, versIniDatiSpecUdBlob);
        }
        // end MEV#29276

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviAroIniDatiSpecDoc(SyncFakeSessn sessione,
            AroVersIniUnitaDoc tmpAroVersIniUnitaDoc, AroVersIniDoc tmpAroVersIniDoc,
            AroUsoXsdDatiSpec tmpAroUsoXsdDatiSpec, BackendStorage backendMetadata,
            Map<DatiSpecLinkOsKeyMap, Map<String, String>> versIniDatiSpecBlob,
            StrutturaUpdVers svf) {

        // MEV#29276
        DatiSpecLinkOsKeyMap key = new DatiSpecLinkOsKeyMap(tmpAroVersIniDoc.getIdVersIniDoc(),
                TiEntitaSacerAroVersIniDatiSpec.DOC.name());
        Map<String, String> versIniDatiSpecDocBlob = (versIniDatiSpecBlob.containsKey(key))
                ? versIniDatiSpecBlob.get(key)
                : new HashMap<>();
        // end MEV#29276

        RispostaControlli tmpRispostaControlli = scriviAroIniDatiSpec(sessione,
                tmpAroVersIniUnitaDoc, tmpAroVersIniDoc, null, tmpAroUsoXsdDatiSpec,
                backendMetadata, versIniDatiSpecDocBlob, svf);

        // in caso di errore esecuzione query
        if (!tmpRispostaControlli.isrBoolean()) {
            return tmpRispostaControlli;
        }

        // MEV#29276
        if (backendMetadata.isObjectStorage() && !versIniDatiSpecBlob.containsKey(key)) {
            versIniDatiSpecBlob.put(key, versIniDatiSpecDocBlob);
        }
        // end MEV#29276

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviAroIniDatiSpecComp(SyncFakeSessn sessione,
            AroVersIniUnitaDoc tmpAroVersIniUnitaDoc, AroVersIniDoc tmpAroVersIniDoc,
            AroVersIniComp tmpAroVersIniComp, AroUsoXsdDatiSpec tmpAroUsoXsdDatiSpec,
            BackendStorage backendMetadata,
            Map<DatiSpecLinkOsKeyMap, Map<String, String>> versIniDatiSpecBlob,
            StrutturaUpdVers svf) {

        // MEV#29276
        DatiSpecLinkOsKeyMap key = new DatiSpecLinkOsKeyMap(tmpAroVersIniComp.getIdVersIniComp(),
                TiEntitaSacerAroVersIniDatiSpec.COMP.name());
        Map<String, String> versIniDatiSpecCompBlob = (versIniDatiSpecBlob.containsKey(key))
                ? versIniDatiSpecBlob.get(key)
                : new HashMap<>();
        // end MEV#29276

        RispostaControlli tmpRispostaControlli = scriviAroIniDatiSpec(sessione,
                tmpAroVersIniUnitaDoc, tmpAroVersIniDoc, tmpAroVersIniComp, tmpAroUsoXsdDatiSpec,
                backendMetadata, versIniDatiSpecCompBlob, svf);

        // in caso di errore esecuzione query
        if (!tmpRispostaControlli.isrBoolean()) {
            return tmpRispostaControlli;
        }

        // MEV#29276
        if (backendMetadata.isObjectStorage() && !versIniDatiSpecBlob.containsKey(key)) {
            versIniDatiSpecBlob.put(key, versIniDatiSpecCompBlob);
        }
        // end MEV#29276

        return tmpRispostaControlli;
    }

    private RispostaControlli scriviAroIniDatiSpec(SyncFakeSessn sessione,
            AroVersIniUnitaDoc tmpAroVersIniUnitaDoc, AroVersIniDoc tmpAroVersIniDoc,
            AroVersIniComp tmpAroVersIniComp, AroUsoXsdDatiSpec tmpAroUsoXsdDatiSpec,
            BackendStorage backendMetadata, Map<String, String> tmpVersIniDatiSpecBlob,
            StrutturaUpdVers svf) {
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

            final LocalDate dtReg = convertLocal(sessione.getTmApertura());
            tmpAroVersIniDatiSpec.setDtReg(dtReg);
            // MEV#30089
            tmpAroVersIniDatiSpec.setAaDtReg(dtReg.getYear());

            // tipo di uso del xsd pari a VERS
            tmpAroVersIniDatiSpec.setTiUsoXsd(
                    TiUsoXsdAroVersIniDatiSpec.valueOf(tmpAroUsoXsdDatiSpec.getTiUsoXsd()));
            // tipo entita sacer pari a UNI_DOC
            tmpAroVersIniDatiSpec.setTiEntitaSacer(TiEntitaSacerAroVersIniDatiSpec
                    .valueOf(tmpAroUsoXsdDatiSpec.getTiEntitaSacer()));
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

            // MEV#29276
            // Clob contenente il frammento XML contenuto nel tag “DatiSpecifici” del XML in
            String blXmlDatiSpec = tmpRispostaControlli.getrString();
            if (backendMetadata.isDataBase()) {
                tmpAroVersIniDatiSpec.setBlXmlDatiSpec(blXmlDatiSpec);
            } else {
                tmpVersIniDatiSpecBlob.put(tmpAroUsoXsdDatiSpec.getTiUsoXsd(), blXmlDatiSpec);
            }
            // end MEV#29276

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
                    "Errore interno nella fase di salvataggio metadati iniziali dati specifici: "
                            + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviAroIniDoc(RispostaWSUpdVers rispostaWs,
            UpdVersamentoExt versamento, SyncFakeSessn sessione,
            AroVersIniUnitaDoc tmpAroVersIniUnitaDoc, long idDoc, StrutturaUpdVers svf) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {

            AroVersIniDoc tmpVersIniDoc = new AroVersIniDoc();

            // FK al versamento iniziale dell’unita doc
            tmpVersIniDoc.setAroVersIniUnitaDoc(tmpAroVersIniUnitaDoc);

            // 0. si assume LOCK esclusivo su DOC da aggiornare
            // find AroDoc
            Map<String, Object> properties = new HashMap<>();
            properties.put(Constants.JAVAX_PERSISTENCE_LOCK_TIMEOUT, 25000);
            AroDoc aroDoc = entityManager.find(AroDoc.class, idDoc, LockModeType.PESSIMISTIC_WRITE,
                    properties);

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
                    "Errore interno nella fase di salvataggio metadati iniziali documento: "
                            + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviAroIniComp(RispostaWSUpdVers rispostaWs,
            UpdVersamentoExt versamento, SyncFakeSessn sessione, AroVersIniDoc tmpAroVersIniDoc,
            long idCompDoc, StrutturaUpdVers svf) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {

            AroVersIniComp tmpVersIniComp = new AroVersIniComp();

            // FK al versamento iniziale dell’unita doc
            tmpVersIniComp.setAroVersIniDoc(tmpAroVersIniDoc);

            // si assume LOCK esclusivo su COMP da aggiornare
            // find AroCompDoc
            Map<String, Object> properties = new HashMap<>();
            properties.put(Constants.JAVAX_PERSISTENCE_LOCK_TIMEOUT, 25000);
            AroCompDoc aroCompDoc = entityManager.find(AroCompDoc.class, idCompDoc,
                    LockModeType.PESSIMISTIC_WRITE, properties);

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
                    "Errore interno nella fase di salvataggio metadati iniziali documento: "
                            + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(SalvataggioUpdVersamentoIniHelper.class);
    }
}
