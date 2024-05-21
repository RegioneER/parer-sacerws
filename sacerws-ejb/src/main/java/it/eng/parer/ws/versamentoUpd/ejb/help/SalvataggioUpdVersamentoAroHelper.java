/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.ejb.help;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityGraph;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.AroArchivSec;
import it.eng.parer.entity.AroCompDoc;
import it.eng.parer.entity.AroDoc;
import it.eng.parer.entity.AroLinkUnitaDoc;
import it.eng.parer.entity.AroUnitaDoc;
import it.eng.parer.entity.AroUpdArchivSec;
import it.eng.parer.entity.AroUpdCompUnitaDoc;
import it.eng.parer.entity.AroUpdDatiSpecUnitaDoc;
import it.eng.parer.entity.AroUpdDocUnitaDoc;
import it.eng.parer.entity.AroUpdLinkUnitaDoc;
import it.eng.parer.entity.AroUpdUnitaDoc;
import it.eng.parer.entity.AroUsoXsdDatiSpec;
import it.eng.parer.entity.AroValoreAttribDatiSpec;
import it.eng.parer.entity.DecAttribDatiSpec;
import it.eng.parer.entity.DecUsoModelloXsdUniDoc;
import it.eng.parer.entity.FasFascicolo;
import it.eng.parer.entity.SerVerSerie;
import it.eng.parer.entity.VrsDatiSessioneVers;
import it.eng.parer.entity.VrsSessioneVers;
import it.eng.parer.entity.VrsXmlModelloSessioneVers;
import it.eng.parer.entity.constraint.AroUpdDatiSpecUnitaDoc.TiEntitaAroUpdDatiSpecUnitaDoc;
import it.eng.parer.entity.constraint.AroUpdDatiSpecUnitaDoc.TiUsoXsdAroUpdDatiSpecUnitaDoc;
import it.eng.parer.view_entity.FasVLisFascByUpdUdId;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.CostantiDB.TipiEntitaSacer;
import it.eng.parer.ws.utils.CostantiDB.TipiUsoDatiSpec;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versamento.dto.DatoSpecifico;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.UpdComponenteVers;
import it.eng.parer.ws.versamentoUpd.dto.UpdDocumentoVers;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import it.eng.parer.ws.versamentoUpd.utils.UpdDocumentiUtils;
import javax.persistence.TypedQuery;

import static it.eng.parer.ws.utils.CostantiDB.TiStatoSesioneVers.CHIUSA_OK;

/**
 *
 * @author sinatti_s
 */
@SuppressWarnings("unchecked")
@Stateless(mappedName = "SalvataggioUpdVersamentoAroHelper")
@LocalBean
public class SalvataggioUpdVersamentoAroHelper extends SalvataggioUpdVersamentoBaseHelper {
    public static final String JAVAX_PERSISTENCE_FETCHGRAPH = "javax.persistence.fetchgraph";

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli aggiornaStatoConservazione(AroUnitaDoc tmpAroUnitaDoc, StrutturaUpdVers strutturaUpdVers) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        CostantiDB.StatoConservazioneUnitaDoc scud = strutturaUpdVers.getStatoConservazioneUnitaDoc();

        try {
            // 1. se stato di conservazione per unita doc = AIP_GENERATO o AIP_FIRMATO o
            // AIP_IN_ARCHIVIO o AIP_IN_CUSTODIA
            if (scud == CostantiDB.StatoConservazioneUnitaDoc.AIP_GENERATO
                    || scud == CostantiDB.StatoConservazioneUnitaDoc.AIP_FIRMATO
                    // MAC#26281
                    // // MAC#22948
                    // || scud == CostantiDB.StatoConservazioneUnitaDoc.IN_VOLUME_DI_CONSERVAZIONE
                    // // end MAC#22948
                    // end MAC#26281
                    || scud == CostantiDB.StatoConservazioneUnitaDoc.IN_ARCHIVIO
                    || scud == CostantiDB.StatoConservazioneUnitaDoc.IN_CUSTODIA) {

                // 1.1. aggiorna ARO_UNITA_DOC assegnando stato di conservazione =
                // AIP_IN_AGGIORNAMENTO
                tmpAroUnitaDoc
                        .setTiStatoConservazione(CostantiDB.StatoConservazioneUnitaDoc.AIP_IN_AGGIORNAMENTO.name());
            }

            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di aggiornamento stato conservazione " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            getLogger().error("Errore interno nella fase di aggiornamento stato conservazione", e);
        }
        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli aggiornaProfiloArchivistico(UpdVersamentoExt versamento, AroUnitaDoc tmpAroUnitaDoc,
            AroUpdUnitaDoc tmpAroUpdUnitaDoc) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {
            // 1.1. se su unit� doc da aggiornare il tag �FascicoloPrincipale� e� da
            // aggiornare
            // anche in caso di annullamento dei campi
            if (versamento.hasPAFascicoloPrincipaleToUpd() || versamento.hasProfiloArchivisticoToUpdNull()) {
                tmpAroUnitaDoc.setDsClassifPrinc(tmpAroUpdUnitaDoc.getDsClassifPrinc());
                tmpAroUnitaDoc.setCdFascicPrinc(tmpAroUpdUnitaDoc.getCdFascicPrinc());
                tmpAroUnitaDoc.setDsOggettoFascicPrinc(tmpAroUpdUnitaDoc.getDsOggettoFascicPrinc());
                //
                tmpAroUnitaDoc.setCdSottofascicPrinc(tmpAroUpdUnitaDoc.getCdSottofascicPrinc());
                tmpAroUnitaDoc.setDsOggettoSottofascicPrinc(tmpAroUpdUnitaDoc.getDsOggettoSottofascicPrinc());
            }

            // 1.2. se su unit� doc da aggiornare il tag �FascicoliSecondari� e� da
            // aggiornare
            // anche in caso di annullamento dei campi
            if (versamento.hasPAFascicoliSecondariToUp() || versamento.hasProfiloArchivisticoToUpdNull()) {
                // 1.2.1. elimina i record relativi alle archivazioni secondarie dell�unit� doc
                // in modifica (ARO_ARCHIV_SEC)
                for (AroArchivSec remAroArchivSec : tmpAroUnitaDoc.getAroArchivSecs()) {
                    // remove
                    entityManager.remove(remAroArchivSec);
                }
                // clear list
                tmpAroUnitaDoc.getAroArchivSecs().clear();
                // delete
                entityManager.flush();
                // 1.2.2. per ogni record presente in ARO_UPD_ARCHIV_SEC
                // 1.2.2.1. inserisci record in ARO_ARCHIV_SEC
                for (AroUpdArchivSec updAroUpdArchivSec : tmpAroUpdUnitaDoc.getAroUpdArchivSecs()) {
                    AroArchivSec newAroArchivSec = new AroArchivSec();
                    // FK
                    newAroArchivSec.setAroUnitaDoc(tmpAroUnitaDoc);
                    // cd fasc
                    newAroArchivSec.setCdFascic(updAroUpdArchivSec.getCdFascic());
                    // cd sottofasc
                    newAroArchivSec.setCdSottofascic(updAroUpdArchivSec.getCdSottofascic());
                    // dsClassif
                    newAroArchivSec.setDsClassif(updAroUpdArchivSec.getDsClassif());
                    // dsOggettoFascic
                    newAroArchivSec.setDsOggettoFascic(updAroUpdArchivSec.getDsOggettoFascic());
                    // dsOggettoSottofascic
                    newAroArchivSec.setDsOggettoSottofascic(updAroUpdArchivSec.getDsOggettoSottofascic());
                    // idStrut
                    newAroArchivSec.setIdStrut(new BigDecimal(tmpAroUnitaDoc.getIdOrgStrut()));

                    // persist
                    entityManager.persist(newAroArchivSec);

                }
                // 1.2.3. fine per ogni record presente in ARO_UPD_ARCHIV_SEC
            }
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di aggiornamento aroUnitaDoc profilo archivistico " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            getLogger().error("Errore interno nella fase di aggiornamento aroUnitaDoc profilo archivistico", e);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli aggiornaProfiloUnitaDocumentaria(AroUnitaDoc tmpAroUnitaDoc,
            AroUpdUnitaDoc tmpAroUpdUnitaDoc) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {
            // 2. se su unit� doc da aggiornare il tag �ProfiloUnitaDocumentaria� e� da
            // aggiornare
            tmpAroUnitaDoc.setDlOggettoUnitaDoc(tmpAroUpdUnitaDoc.getDlOggettoUnitaDoc());
            tmpAroUnitaDoc.setDtRegUnitaDoc(tmpAroUpdUnitaDoc.getDtRegUnitaDoc());

            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di aggiornamento profilo ud " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            getLogger().error("Errore interno nella fase di aggiornamento profilo ud", e);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli aggiornaProfiloNormativo(AroUnitaDoc tmpAroUnitaDoc, StrutturaUpdVers strutturaUpdVers) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {
            List<VrsXmlModelloSessioneVers> lstXmlModelloSessioneVers;
            lstXmlModelloSessioneVers = retreiveXmlVersamentiModelloXsdUnitaDoc(CostantiDB.TiUsoModelloXsd.VERS.name(),
                    it.eng.parer.entity.constraint.DecModelloXsdUd.TiModelloXsdUd.PROFILO_NORMATIVO_UNITA_DOC,
                    tmpAroUnitaDoc.getIdUnitaDoc());
            if (!lstXmlModelloSessioneVers.isEmpty()) {
                VrsXmlModelloSessioneVers vrsXmlModelloSessioneVers = lstXmlModelloSessioneVers.get(0);
                if (strutturaUpdVers.getIdRecUsoXsdProfiloNormativo() == 0) {
                    entityManager.remove(vrsXmlModelloSessioneVers);
                    entityManager.flush();
                } else {
                    vrsXmlModelloSessioneVers.setBlXml(strutturaUpdVers.getDatiC14NProfNormXml());
                    // persist
                    entityManager.persist(vrsXmlModelloSessioneVers);
                    entityManager.flush();
                }
            } else {
                List<VrsDatiSessioneVers> listDatiSessioneVers;
                // recupero la sessione relativa al versamento originale dell'UD
                List<VrsSessioneVers> tmpSessioneVer = tmpAroUnitaDoc.getVrsSessioneVers();
                if (!tmpSessioneVer.isEmpty()) {
                    listDatiSessioneVers = retreiveVrsDatiSessioneVersByIsSessioneVers(
                            tmpSessioneVer.get(0).getIdSessioneVers());

                    VrsXmlModelloSessioneVers tmpXmlModelloSessioneVers = new VrsXmlModelloSessioneVers();
                    tmpXmlModelloSessioneVers.setVrsDatiSessioneVers(listDatiSessioneVers.get(0));

                    if (strutturaUpdVers.getIdStruttura() != 0) {
                        tmpXmlModelloSessioneVers.setIdStrut(new BigDecimal(strutturaUpdVers.getIdStruttura()));
                    }
                    tmpXmlModelloSessioneVers.setFlCanonicalized(CostantiDB.Flag.TRUE);
                    tmpXmlModelloSessioneVers.setBlXml(strutturaUpdVers.getDatiC14NProfNormXml());
                    tmpXmlModelloSessioneVers.setDecUsoModelloXsdUniDoc(entityManager.find(DecUsoModelloXsdUniDoc.class,
                            strutturaUpdVers.getIdRecUsoXsdProfiloNormativo()));

                    entityManager.persist(tmpXmlModelloSessioneVers);
                }
            }
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di aggiornamento profilo ud " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            getLogger().error("Errore interno nella fase di aggiornamento profilo ud", e);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli aggiornaDocumentiCollegati(AroUnitaDoc tmpAroUnitaDoc, AroUpdUnitaDoc tmpAroUpdUnitaDoc) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {
            // 3. se su unit� doc da aggiornare il tag �DocumentiCollegati� e� da aggiornare
            // 3.1. elimina i record relativi ai documenti collegati dell�unit� doc in
            // modifica (ARO_LINK_UNITA_DOC)
            for (AroLinkUnitaDoc remAroLinkUnitaDoc : tmpAroUnitaDoc.getAroLinkUnitaDocs()) {
                // remove
                entityManager.remove(remAroLinkUnitaDoc);
            }
            // clear list
            tmpAroUnitaDoc.getAroLinkUnitaDocs().clear();
            // delete
            entityManager.flush();
            // 3.2. per ogni record presente in ARO_UPD_LINK_UNITA_DOC
            // 3.2.1. inserisci record in ARO_LINK_UNITA_DOC
            for (AroUpdLinkUnitaDoc updAroLinkUnitaDoc : tmpAroUpdUnitaDoc.getAroUpdLinkUnitaDocs()) {
                AroLinkUnitaDoc newAroLinkUnitaDoc = new AroLinkUnitaDoc();

                // FK
                newAroLinkUnitaDoc.setAroUnitaDoc(tmpAroUnitaDoc);

                // chiave
                newAroLinkUnitaDoc.setAaKeyUnitaDocLink(updAroLinkUnitaDoc.getAaKeyUnitaDocLink());
                newAroLinkUnitaDoc.setCdKeyUnitaDocLink(updAroLinkUnitaDoc.getCdKeyUnitaDocLink());
                newAroLinkUnitaDoc.setCdRegistroKeyUnitaDocLink(updAroLinkUnitaDoc.getCdRegistroKeyUnitaDocLink());
                // FK
                newAroLinkUnitaDoc.setAroUnitaDocLink(updAroLinkUnitaDoc.getAroUnitaDocLink());

                // dsLinkUnitaDoc
                newAroLinkUnitaDoc.setDsLinkUnitaDoc(updAroLinkUnitaDoc.getDsLinkUnitaDoc());
                // idStrut
                newAroLinkUnitaDoc.setIdStrut(new BigDecimal(tmpAroUnitaDoc.getIdOrgStrut()));

                // persist
                entityManager.persist(newAroLinkUnitaDoc);

            }
            // 3.3. fine per ogni record presente in ARO_UPD_LINK_UNITA_DOC

            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di aggiornamento aroUnitaDoc documenti collegati " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            getLogger().error("Errore interno nella fase di aggiornamento aroUnitaDoc documenti collegati", e);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli aggiornaDatiSpecificiUd(AroUnitaDoc tmpAroUnitaDoc, AroUpdUnitaDoc tmpAroUpdUnitaDoc,
            TiUsoXsdAroUpdDatiSpecUnitaDoc tiUsoXsdAroUpdDatiSpecUnitaDoc, StrutturaUpdVers strutturaUpdVers) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {
            // 4. se su unit� doc da aggiornare il tag �DatiSpecifici� e� da aggiornare
            // per ogni dato specifico
            for (AroUpdDatiSpecUnitaDoc tmpAroUpdDocUnitaDoc : tmpAroUpdUnitaDoc.getAroUpdDatiSpecUnitaDocs()) {

                // verifica
                // 4.1. se in ARO_UPD_DATI_SPEC_UNITA_DOC e� presente il record con tipo entita
                // = UPD_UNI_DOC e tipo uso = VERS
                if (tmpAroUpdDocUnitaDoc.getTiEntitaSacer().equals(TiEntitaAroUpdDatiSpecUnitaDoc.UPD_UNI_DOC)
                        && tmpAroUpdDocUnitaDoc.getTiUsoXsd().equals(tiUsoXsdAroUpdDatiSpecUnitaDoc)) {

                    TipiUsoDatiSpec tipiUsoDatiSpec = tiUsoXsdAroUpdDatiSpecUnitaDoc.equals(
                            TiUsoXsdAroUpdDatiSpecUnitaDoc.VERS) ? TipiUsoDatiSpec.VERS : TipiUsoDatiSpec.MIGRAZ;
                    tmpRispostaControlli = this.checkUsoXsdDatiSpecifici(tmpAroUnitaDoc.getIdUnitaDoc(),
                            tipiUsoDatiSpec, TipiEntitaSacer.UNI_DOC/* fixed */);

                    // errore query
                    // non dovrebbe mai capitare ....
                    if (!tmpRispostaControlli.isrBoolean()) {
                        return tmpRispostaControlli;
                    }

                    AroUsoXsdDatiSpec tmpUsoXsdDatiSpec = new AroUsoXsdDatiSpec();
                    // � presente ...
                    if (tmpRispostaControlli.getrLong() != -1) {
                        tmpUsoXsdDatiSpec = (AroUsoXsdDatiSpec) tmpRispostaControlli.getrObject();
                        // 4.1.1. elimina i record relativi ai valori dei dati specifici
                        // (ARO_VALORE_ATTRIB_DATI_SPEC) determinati mediante il record relativo all�uso
                        // del XSD per dati specifici relativo all�unita doc in modifica con tipo entita
                        // = UNI_DOC e tipo uso = VERS (ARO_USO_XSD_DATI_SPEC)
                        for (AroValoreAttribDatiSpec tmpAroValoreAttribDatiSpec : tmpUsoXsdDatiSpec
                                .getAroValoreAttribDatiSpecs()) {
                            // remove
                            entityManager.remove(tmpAroValoreAttribDatiSpec);
                        }
                        // clear list
                        tmpUsoXsdDatiSpec.getAroValoreAttribDatiSpecs().clear();
                        // delete
                        entityManager.flush();

                        // 4.1.2. se esiste record in ARO_USO_XSD_DATI_SPEC relativo all�unita doc in
                        // modifica con tipo entita = UNI_DOC e tipo uso = VERS
                        // FK alla versione XSD per dati specifici con il valore definito
                        // ARO_UPD_DATI_SPEC_UNITA_DOC
                        tmpUsoXsdDatiSpec.setDecXsdDatiSpec(tmpAroUpdDocUnitaDoc.getDecXsdDatiSpec());
                    } else {
                        if (strutturaUpdVers.getIdRecXsdDatiSpec() != 0) {
                            // 4.1.3.1. inserisci record in ARO_USO_XSD_DATI_SPEC relativo all�unita doc in
                            // modifica con tipo entita = UNI_DOC e tipo uso = VERS, specificando:
                            // FK
                            tmpUsoXsdDatiSpec.setAroUnitaDoc(tmpAroUnitaDoc);
                            tmpUsoXsdDatiSpec.setDecXsdDatiSpec(tmpAroUpdDocUnitaDoc.getDecXsdDatiSpec());
                            tmpUsoXsdDatiSpec.setIdStrut(new BigDecimal(tmpAroUnitaDoc.getIdOrgStrut()));
                            //
                            tmpUsoXsdDatiSpec.setTiEntitaSacer(TipiEntitaSacer.UNI_DOC.name());// fixed
                            tmpUsoXsdDatiSpec.setTiUsoXsd(tipiUsoDatiSpec.name());

                            // persist
                            entityManager.persist(tmpUsoXsdDatiSpec);
                            // add on list
                            tmpAroUnitaDoc.getAroUsoXsdDatiSpecs().add(tmpUsoXsdDatiSpec);
                        }
                    }

                    if (strutturaUpdVers.getIdRecXsdDatiSpec() != 0) {
                        Map<String, DatoSpecifico> datiSpecifici = TiUsoXsdAroUpdDatiSpecUnitaDoc.VERS
                                .equals(tiUsoXsdAroUpdDatiSpecUnitaDoc) ? strutturaUpdVers.getDatiSpecifici()
                                        : strutturaUpdVers.getDatiSpecificiMigrazione();
                        // 4.1.4.1. registra record in ARO_VALORE_ATTRIB_DATI_SPEC, specificando:
                        for (DatoSpecifico datoSpec : datiSpecifici.values()) {
                            AroValoreAttribDatiSpec newAroValoreAttribDatiSpec = new AroValoreAttribDatiSpec();
                            // FK
                            newAroValoreAttribDatiSpec.setDecAttribDatiSpec(
                                    entityManager.find(DecAttribDatiSpec.class, datoSpec.getIdDatoSpec()));
                            newAroValoreAttribDatiSpec.setIdStrut(new BigDecimal(tmpAroUnitaDoc.getIdOrgStrut()));
                            newAroValoreAttribDatiSpec.setDlValore(datoSpec.getValore());
                            newAroValoreAttribDatiSpec.setAroUsoXsdDatiSpec(tmpUsoXsdDatiSpec);

                            // persist
                            entityManager.persist(newAroValoreAttribDatiSpec);
                            // add on list
                            // Nota: se l'entity è appena creata non ho relazione quindi la lista non è
                            // inizializzata
                            tmpUsoXsdDatiSpec.getAroValoreAttribDatiSpecs().add(newAroValoreAttribDatiSpec);
                        }
                    }
                } // if
            } // for AroUpdDatiSpecUnitaDoc
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di aggiornamento dati specifici " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            getLogger().error("Errore interno nella fase di aggiornamento dati specifici", e);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli aggiornaDocumento(AroUpdUnitaDoc tmpAroUpdUnitaDoc, StrutturaUpdVers strutturaUpdVers) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {
            // 6. per ogni aggiornamento documento contenuto in un aggiornamento unita doc
            for (AroUpdDocUnitaDoc aroUpdDocUnitaDoc : tmpAroUpdUnitaDoc.getAroUpdDocUnitaDocs()) {
                AroDoc aroDoc = aroUpdDocUnitaDoc.getAroDoc();

                // info su profilo documento pari a quelle definite da ARO_UPD_DOC_UNITA_DOC
                // (dl_doc e ds_autor_doc)
                aroDoc.setDlDoc(aroUpdDocUnitaDoc.getDlDoc());
                aroDoc.setDsAutoreDoc(aroUpdDocUnitaDoc.getDsAutoreDoc());

                // TODO : non dovrebbe mai capitare ! (nella lista dei documenti attesi, che
                // sono stati controllati, esiste sempre il documento identificato su DB)
                long countDoc = strutturaUpdVers.getDocumentiAttesi().stream()
                        .filter(d -> d.getIdRecDocumentoDB() == aroDoc.getIdDoc()).count();
                if (countDoc != 1) {
                    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
                    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                            "SalvataggioUpdVersamentoAroHelper.aggiornaDatiSpecDoc: errore in fase di ricerca documento da aggiornare"));
                    return tmpRispostaControlli;
                }
                //
                UpdDocumentoVers updDocumentoVers = strutturaUpdVers.getDocumentiAttesi().stream()
                        .filter(d -> d.getIdRecDocumentoDB() == aroDoc.getIdDoc()).collect(Collectors.toList()).get(0); // single
                // result

                // 2. se su documento da aggiornare il tag �DatiSpecifici� e� da aggiornare
                if (updDocumentoVers.getRifUpdDocumento().getDatiSpecifici() != null) {
                    for (AroUpdDatiSpecUnitaDoc aroUpdDatiSpecUnitaDoc : aroUpdDocUnitaDoc
                            .getAroUpdDatiSpecUnitaDocs()) {
                        tmpRispostaControlli = aggiornaDatiSpecDoc(aroDoc, aroUpdDatiSpecUnitaDoc, strutturaUpdVers,
                                TiUsoXsdAroUpdDatiSpecUnitaDoc.VERS, TiEntitaAroUpdDatiSpecUnitaDoc.UPD_DOC);
                    }
                }

                // 3. se su documento da aggiornare il tag �DatiSpecificiMigrazione� e� da
                // aggiornare
                if (updDocumentoVers.getRifUpdDocumento().getDatiSpecificiMigrazione() != null) {
                    for (AroUpdDatiSpecUnitaDoc aroUpdDatiSpecUnitaDoc : aroUpdDocUnitaDoc
                            .getAroUpdDatiSpecUnitaDocs()) {
                        tmpRispostaControlli = aggiornaDatiSpecDoc(aroDoc, aroUpdDatiSpecUnitaDoc, strutturaUpdVers,
                                TiUsoXsdAroUpdDatiSpecUnitaDoc.MIGRAZ, TiEntitaAroUpdDatiSpecUnitaDoc.UPD_DOC);
                    }
                }

                // ciclo componenti
                for (AroUpdCompUnitaDoc aroUpdCompUnitaDoc : aroUpdDocUnitaDoc.getAroUpdCompUnitaDocs()) {
                    AroCompDoc aroCompDoc = aroUpdCompUnitaDoc.getAroCompDoc();
                    // info su componente pari a quelle definite da ARO_UPD_COMP_UNITA_DOC
                    // (ds_nome_comp_vers, dl_urn_comp_vers, ds_id_comp_vers)
                    aroCompDoc.setDsNomeCompVers(aroUpdCompUnitaDoc.getDsNomeCompVers());
                    aroCompDoc.setDlUrnCompVers(aroUpdCompUnitaDoc.getDlUrnCompVers());
                    aroCompDoc.setDsIdCompVers(aroUpdCompUnitaDoc.getDsIdCompVers());

                    for (UpdComponenteVers updComponenteVers : updDocumentoVers.getUpdComponentiAttesi().stream()
                            .filter(d -> d.getIdRecDB() == aroCompDoc.getIdCompDoc()).collect(Collectors.toList())) {

                        // 2. se su documento da aggiornare il tag �DatiSpecifici� e� da aggiornare
                        if (updComponenteVers.getMyUpdComponente().getDatiSpecifici() != null) {

                            for (AroUpdDatiSpecUnitaDoc aroUpdDatiSpecUnitaDoc : aroUpdDocUnitaDoc
                                    .getAroUpdDatiSpecUnitaDocs()) {
                                tmpRispostaControlli = aggiornaDatiSpecDoc(aroDoc, aroCompDoc, aroUpdDatiSpecUnitaDoc,
                                        strutturaUpdVers, TiUsoXsdAroUpdDatiSpecUnitaDoc.VERS,
                                        TiEntitaAroUpdDatiSpecUnitaDoc.UPD_COMP);
                            }
                        }
                        // 2. se su documento da aggiornare il tag �DatiSpecificiMigrazione� e� da
                        // aggiornare
                        if (updComponenteVers.getMyUpdComponente().getDatiSpecificiMigrazione() != null) {
                            for (AroUpdDatiSpecUnitaDoc aroUpdDatiSpecUnitaDoc : aroUpdDocUnitaDoc
                                    .getAroUpdDatiSpecUnitaDocs()) {
                                tmpRispostaControlli = aggiornaDatiSpecDoc(aroDoc, aroCompDoc, aroUpdDatiSpecUnitaDoc,
                                        strutturaUpdVers, TiUsoXsdAroUpdDatiSpecUnitaDoc.MIGRAZ,
                                        TiEntitaAroUpdDatiSpecUnitaDoc.UPD_COMP);
                            }
                        }
                    } // for UpdComponenteVers
                }
            } // for

            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di aggiornamento documento " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            getLogger().error("Errore interno nella fase di aggiornamento documento", e);
        }

        return tmpRispostaControlli;
    }

    private RispostaControlli aggiornaDatiSpecDoc(AroDoc aroDoc, AroUpdDatiSpecUnitaDoc tmpAroUpdDatiSpecUnitaDoc,
            StrutturaUpdVers strutturaUpdVers, TiUsoXsdAroUpdDatiSpecUnitaDoc tiUsoXsdAroUpdDatiSpecUnitaDoc,
            TiEntitaAroUpdDatiSpecUnitaDoc tiEntitaAroUpdDatiSpecUnitaDoc) {
        return aggiornaDatiSpecDoc(aroDoc, null, tmpAroUpdDatiSpecUnitaDoc, strutturaUpdVers,
                tiUsoXsdAroUpdDatiSpecUnitaDoc, tiEntitaAroUpdDatiSpecUnitaDoc);
    }

    private RispostaControlli aggiornaDatiSpecDoc(AroDoc aroDoc, AroCompDoc aroCompDoc,
            AroUpdDatiSpecUnitaDoc tmpAroUpdDatiSpecUnitaDoc, StrutturaUpdVers strutturaUpdVers,
            TiUsoXsdAroUpdDatiSpecUnitaDoc tiUsoXsdAroUpdDatiSpecUnitaDoc,
            TiEntitaAroUpdDatiSpecUnitaDoc tiEntitaAroUpdDatiSpecUnitaDoc) {

        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {
            // 2.1. se in ARO_UPD_DATI_SPEC_UNITA_DOC e� presente il record con tipo entita
            // = UPD_DOC e tipo uso = VERS relativo
            // all�aggiornamento del documento corrente (NOTA: la condizione e� definita per
            // precauzione: se il tag �DatiSpecifici� e� da aggiornare significa che nel XML
            // di input il tag c�e� e che contiene i valori dei dati spec)
            if (tmpAroUpdDatiSpecUnitaDoc.getTiEntitaSacer().equals(tiEntitaAroUpdDatiSpecUnitaDoc)
                    && tmpAroUpdDatiSpecUnitaDoc.getTiUsoXsd().equals(tiUsoXsdAroUpdDatiSpecUnitaDoc)) {

                tmpRispostaControlli = this.checkUsoXsdDatiSpecifici(
                        TiEntitaAroUpdDatiSpecUnitaDoc.UPD_DOC.equals(tiEntitaAroUpdDatiSpecUnitaDoc)
                                ? aroDoc.getIdDoc() : aroCompDoc.getIdCompDoc(),
                        (TipiUsoDatiSpec) UpdDocumentiUtils.convertEnumTipiUsoDatiSpec(false,
                                tiUsoXsdAroUpdDatiSpecUnitaDoc.name()),
                        (TipiEntitaSacer) UpdDocumentiUtils.convertEnumTiEntita(false,
                                tiEntitaAroUpdDatiSpecUnitaDoc.name()));

                // errore query
                // non dovrebbe mai capitare ....
                if (!tmpRispostaControlli.isrBoolean()) {
                    return tmpRispostaControlli;
                }

                AroUsoXsdDatiSpec tmpUsoXsdDatiSpec;
                // � presente ...
                if (tmpRispostaControlli.getrLong() != -1) {
                    tmpUsoXsdDatiSpec = (AroUsoXsdDatiSpec) tmpRispostaControlli.getrObject();
                    // 4.1.1. elimina i record relativi ai valori dei dati specifici
                    // (ARO_VALORE_ATTRIB_DATI_SPEC) determinati mediante il record relativo all�uso
                    // del XSD per dati specifici relativo all�unita doc in modifica con tipo entita
                    // = UNI_DOC e tipo uso = VERS (ARO_USO_XSD_DATI_SPEC)
                    for (AroValoreAttribDatiSpec tmpAroValoreAttribDatiSpec : tmpUsoXsdDatiSpec
                            .getAroValoreAttribDatiSpecs()) {
                        // remove
                        entityManager.remove(tmpAroValoreAttribDatiSpec);
                    }

                    // clear list
                    tmpUsoXsdDatiSpec.getAroValoreAttribDatiSpecs().clear();
                    // delete
                    entityManager.flush();

                    // 4.1.2. se esiste record in ARO_USO_XSD_DATI_SPEC relativo all�unita doc in
                    // modifica con tipo entita = UNI_DOC e tipo uso = VERS
                    // FK alla versione XSD per dati specifici con il valore definito
                    // ARO_UPD_DATI_SPEC_UNITA_DOC
                    tmpUsoXsdDatiSpec.setDecXsdDatiSpec(tmpAroUpdDatiSpecUnitaDoc.getDecXsdDatiSpec());

                } else {
                    // 4.1.3.1. inserisci record in ARO_USO_XSD_DATI_SPEC relativo all�unita doc in
                    // modifica con tipo entita = UNI_DOC e tipo uso = VERS, specificando:
                    tmpUsoXsdDatiSpec = new AroUsoXsdDatiSpec();
                    // FK
                    tmpUsoXsdDatiSpec.setAroUnitaDoc(aroDoc.getAroUnitaDoc());
                    tmpUsoXsdDatiSpec.setAroDoc(aroDoc);
                    tmpUsoXsdDatiSpec.setAroCompDoc(aroCompDoc);
                    tmpUsoXsdDatiSpec.setDecXsdDatiSpec(tmpAroUpdDatiSpecUnitaDoc.getDecXsdDatiSpec());
                    tmpUsoXsdDatiSpec.setIdStrut(new BigDecimal(aroDoc.getAroUnitaDoc().getIdOrgStrut()));
                    //
                    tmpUsoXsdDatiSpec.setTiEntitaSacer(((TipiEntitaSacer) UpdDocumentiUtils.convertEnumTiEntita(false,
                            tiEntitaAroUpdDatiSpecUnitaDoc.name())).name());
                    tmpUsoXsdDatiSpec.setTiUsoXsd(((TipiUsoDatiSpec) UpdDocumentiUtils.convertEnumTipiUsoDatiSpec(false,
                            tiUsoXsdAroUpdDatiSpecUnitaDoc.name())).name());

                    // persist
                    entityManager.persist(tmpUsoXsdDatiSpec);

                }

                if (TiEntitaAroUpdDatiSpecUnitaDoc.UPD_DOC.equals(tiEntitaAroUpdDatiSpecUnitaDoc)) {

                    // add on list
                    aroDoc.getAroUsoXsdDatiSpecs().add(tmpUsoXsdDatiSpec);

                    // TODO : non dovrebbe mai capitare ! (nella lista dei documenti attesi, che
                    // sono stati controllati, esiste sempre il documento identificato su DB)
                    long countDoc = strutturaUpdVers.getDocumentiAttesi().stream()
                            .filter(d -> d.getIdRecDocumentoDB() == aroDoc.getIdDoc()).count();
                    if (countDoc != 1) {
                        tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
                        tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                                "SalvataggioUpdVersamentoAroHelper.aggiornaDatiSpecDoc: errore in fase di ricerca documento da aggiornare"));
                        return tmpRispostaControlli;
                    }
                    //
                    UpdDocumentoVers updDocumentoVers = strutturaUpdVers.getDocumentiAttesi().stream()
                            .filter(d -> d.getIdRecDocumentoDB() == aroDoc.getIdDoc()).collect(Collectors.toList())
                            .get(0); // single
                    // result

                    // 4.1.4.1. registra record in ARO_VALORE_ATTRIB_DATI_SPEC, specificando:
                    Map<String, DatoSpecifico> datiSpecifici = TiUsoXsdAroUpdDatiSpecUnitaDoc.VERS
                            .equals(tiUsoXsdAroUpdDatiSpecUnitaDoc) ? updDocumentoVers.getDatiSpecifici()
                                    : updDocumentoVers.getDatiSpecificiMigrazione();
                    //
                    buildDatoSpecifico(aroDoc, tmpUsoXsdDatiSpec, datiSpecifici);

                    // add on list
                    aroDoc.getAroUsoXsdDatiSpecs().add(tmpUsoXsdDatiSpec);
                }

                if (TiEntitaAroUpdDatiSpecUnitaDoc.UPD_COMP.equals(tiEntitaAroUpdDatiSpecUnitaDoc)) {

                    // add on list
                    aroCompDoc.getAroUsoXsdDatiSpecs().add(tmpUsoXsdDatiSpec);

                    for (UpdDocumentoVers updDocumentoVers : strutturaUpdVers.getDocumentiAttesi()) {
                        for (UpdComponenteVers updComponenteVers : updDocumentoVers.getUpdComponentiAttesi().stream()
                                .filter(d -> d.getIdRecDB() == aroCompDoc.getIdCompDoc())
                                .collect(Collectors.toList())) {
                            // 4.1.4.1. registra record in ARO_VALORE_ATTRIB_DATI_SPEC, specificando:
                            Map<String, DatoSpecifico> datiSpecifici = TiUsoXsdAroUpdDatiSpecUnitaDoc.VERS
                                    .equals(tiUsoXsdAroUpdDatiSpecUnitaDoc) ? updComponenteVers.getDatiSpecifici()
                                            : updComponenteVers.getDatiSpecificiMigrazione();
                            //
                            buildDatoSpecifico(aroDoc, tmpUsoXsdDatiSpec, datiSpecifici);
                        } // for
                    }

                    // add on list
                    aroCompDoc.getAroUsoXsdDatiSpecs().add(tmpUsoXsdDatiSpec);

                }
            }

            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di aggiornamento dati specifici documento " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            getLogger().error("Errore interno nella fase di aggiornamento dati specifici documento", e);
        }

        return tmpRispostaControlli;
    }

    private void buildDatoSpecifico(AroDoc aroDoc, AroUsoXsdDatiSpec tmpUsoXsdDatiSpec,
            Map<String, DatoSpecifico> datiSpecifici) {
        for (DatoSpecifico datoSpec : datiSpecifici.values()) {
            AroValoreAttribDatiSpec newAroValoreAttribDatiSpec = new AroValoreAttribDatiSpec();
            // FK
            newAroValoreAttribDatiSpec
                    .setDecAttribDatiSpec(entityManager.find(DecAttribDatiSpec.class, datoSpec.getIdDatoSpec()));
            newAroValoreAttribDatiSpec.setIdStrut(aroDoc.getIdStrut());
            newAroValoreAttribDatiSpec.setDlValore(datoSpec.getValore());
            newAroValoreAttribDatiSpec.setAroUsoXsdDatiSpec(tmpUsoXsdDatiSpec);

            // persist
            entityManager.persist(newAroValoreAttribDatiSpec);
            // add on list
            // Nota: se l'entity � appena creata non ho relazione quindi la lista non �
            // inizializzata
            tmpUsoXsdDatiSpec.getAroValoreAttribDatiSpecs().add(newAroValoreAttribDatiSpec);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli aggiornaAggregazioni(AroUnitaDoc tmpAroUnitaDoc) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try {
            // 1. il sistema aggiorna la versione serie settando l�indicatore che segnala
            // che la serie deve essere ricalcolata a causa di aggiornamento
            // (vedi vista SER_V_LIS_VERSER_BY_UPD_UD)
            List<BigDecimal> idVerSeries = this.retrieveSerVLisVerserByUpdUd(tmpAroUnitaDoc.getIdUnitaDoc());
            for (BigDecimal idVerSerie : idVerSeries) {
                SerVerSerie verSerie = entityManager.find(SerVerSerie.class, idVerSerie.longValue());
                verSerie.setFlUpdModifUnitaDoc("1");
            }

            // 2. il sistema aggiorna il fascicolo settando l�indicatore che segnala che la
            // serie deve essere ricalcolata a causa di aggiornamento
            // metadati o aggiunta documento di almeno una unit� documentaria
            // (vedi vista FAS_V_LIS_FASC_BY_UPD_UD)

            List<FasVLisFascByUpdUdId> idFascs = this.retrieveFasVLisFascByUpdUdId(tmpAroUnitaDoc.getIdUnitaDoc());
            for (FasVLisFascByUpdUdId idFasc : idFascs) {
                FasFascicolo fasFascicolo = entityManager.find(FasFascicolo.class, idFasc.getIdFascicolo().longValue());
                fasFascicolo.setFlUpdModifUnitaDoc("1");
            }

            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di aggiornamento aggregati " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            getLogger().error("Errore interno nella fase di aggiornamento aggregati", e);
        }
        return tmpRispostaControlli;
    }

    public List<VrsDatiSessioneVers> retreiveVrsDatiSessioneVersByIsSessioneVers(Long idSessioneVers) {
        /*
         * ricavo i documenti XML relativi alla sessione di versamento individuata
         */
        String queryStr = "select vrsDatiSessioneVers from VrsDatiSessioneVers vrsDatiSessioneVers "
                + "where vrsDatiSessioneVers.vrsSessioneVers.idSessioneVers = :idSessioneVers "
                + "and vrsDatiSessioneVers.tiDatiSessioneVers = 'XML_DOC' ";

        TypedQuery<VrsDatiSessioneVers> query = entityManager.createQuery(queryStr, VrsDatiSessioneVers.class);
        query.setParameter("idSessioneVers", idSessioneVers);

        return query.getResultList();
    }

    public List<VrsXmlModelloSessioneVers> retreiveXmlVersamentiModelloXsdUnitaDoc(String tiUsoModelloXsd,
            it.eng.parer.entity.constraint.DecModelloXsdUd.TiModelloXsdUd tiModelloXsdUd, long idUnitaDoc) {
        List<VrsXmlModelloSessioneVers> lstXmlModelloSessioneVers = null;

        try {

            String queryStr = "select xms from VrsXmlModelloSessioneVers xms "
                    + "join xms.decUsoModelloXsdUniDoc uso_modello " + "join uso_modello.decModelloXsdUd modello_xsd "
                    + "join xms.vrsDatiSessioneVers dati_ses " + "join dati_ses.vrsSessioneVers ses "
                    + "where modello_xsd.tiUsoModelloXsd = :tiUsoModelloXsd "
                    + "and modello_xsd.tiModelloXsd = :tiModelloXsdUd " + "and dati_ses.tiDatiSessioneVers = 'XML_DOC' "
                    + "and ses.aroUnitaDoc.idUnitaDoc = :idUnitaDoc " + "and ses.aroDoc is null "
                    + "and ses.tiStatoSessioneVers = '" + CHIUSA_OK + "' " + "and ses.tiSessioneVers = 'VERSAMENTO' ";
            EntityGraph<VrsXmlModelloSessioneVers> entityGraph = entityManager
                    .createEntityGraph(VrsXmlModelloSessioneVers.class);
            entityGraph.addSubgraph("decUsoModelloXsdUniDoc").addAttributeNodes("decModelloXsdUd");
            TypedQuery<VrsXmlModelloSessioneVers> query = entityManager.createQuery(queryStr,
                    VrsXmlModelloSessioneVers.class);
            query.setHint(JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph);
            query.setParameter("tiUsoModelloXsd", tiUsoModelloXsd);
            query.setParameter("tiModelloXsdUd", tiModelloXsdUd);
            query.setParameter("idUnitaDoc", idUnitaDoc);

            lstXmlModelloSessioneVers = query.getResultList();

        } catch (Exception e) {

            getLogger().error("Eccezione nella lettura modello xsd unit� documentaria", e);
        }
        return lstXmlModelloSessioneVers;
    }

    /*
     * il sistema determina le versioni serie correnti con stato = CONTROLLATA o VALIDAZIONE_IN_CORSO o VALIDATA o
     * DA_FIRMARE o FIRMATA o IN_CUSTODIA, oppure con stato = DA_CONTROLLARE e contenuto effettivo con stato =
     * CONTROLLO_CONSIST_IN_CORSO, nel cui contenuto di tipo EFFETTIVO sia presente l�unit� documentaria in
     * aggiornamento
     * 
     * return list of idVerSerie
     */
    public List<BigDecimal> retrieveSerVLisVerserByUpdUd(long idUnitaDoc) {
        TypedQuery<BigDecimal> query = entityManager.createQuery(
                "SELECT ser.serVLisVerserByUpdUdId.idVerSerie FROM SerVLisVerserByUpdUd ser WHERE ser.serVLisVerserByUpdUdId.idUnitaDoc = :idUnitaDoc ",
                BigDecimal.class);
        query.setParameter("idUnitaDoc", new BigDecimal(idUnitaDoc));
        return query.getResultList();
    }

    /*
     * il sistema determina i fascicoli con stato nell�elenco = IN_ELENCO_IN_CODA_CREAZIONE_AIP o
     * IN_ELENCO_CON_AIP_CREATO o IN_ELENCO_CON_ELENCO_INDICI_AIP_CREATO o IN_ELENCO_COMPLETATO nel cui contenuto e�
     * presente l�unit� documentaria in aggiornamento
     * 
     * return list of FasVLisFascByUpdUdId
     */
    public List<FasVLisFascByUpdUdId> retrieveFasVLisFascByUpdUdId(long idUnitaDoc) {
        TypedQuery<FasVLisFascByUpdUdId> query = entityManager.createQuery(
                "SELECT fas.fasVLisFascByUpdUdId FROM FasVLisFascByUpdUd fas WHERE fas.fasVLisFascByUpdUdId.idUnitaDoc = :idUnitaDoc ",
                FasVLisFascByUpdUdId.class);
        query.setParameter("idUnitaDoc", new BigDecimal(idUnitaDoc));
        return query.getResultList();
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(SalvataggioUpdVersamentoAroHelper.class);
    }

}
