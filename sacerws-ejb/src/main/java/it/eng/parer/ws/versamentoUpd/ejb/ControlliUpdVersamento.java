/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.ejb;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.AroCompDoc;
import it.eng.parer.entity.AroDoc;
import it.eng.parer.entity.AroUnitaDoc;
import it.eng.parer.entity.AroXmlUpdUnitaDoc;
import it.eng.parer.entity.DecTipoUnitaDoc;
import it.eng.parer.entity.IamAbilTipoDato;
import it.eng.parer.entity.OrgStrut;
import it.eng.parer.entity.constraint.AroXmlUpdUnitaDoc.TiXmlUpdUnitaDoc;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.viewEntity.OrgVChkPartitionUpdByAa;
import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.ejb.ControlliSemantici.TipiGestioneUDAnnullate;
import it.eng.parer.ws.utils.CostantiDB.StatoConservazioneUnitaDoc;
import it.eng.parer.ws.utils.CostantiDB.TipiHash;
import it.eng.parer.ws.utils.HashCalculator;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.utils.ParametroApplDB.ParametroApplFl;
import it.eng.parer.ws.versamentoUpd.dto.FlControlliUpd;
import it.eng.parer.ws.versamentoUpd.dto.UpdFascPrincipale;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import it.eng.parer.ws.versamentoUpd.utils.UpdCostanti.StrutAaPartion;
import it.eng.parer.ws.xml.versUpdReq.CamiciaFascicoloType;

/**
 *
 * @author sinatti_s
 */
@Stateless(mappedName = "ControlliUpdVersamento")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class ControlliUpdVersamento {

    private static final Logger log = LoggerFactory.getLogger(ControlliUpdVersamento.class);

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @EJB
    private ControlliSemantici controlliSemantici;

    @EJB
    private ConfigurationHelper configurationHelper;

    public RispostaControlli checkUltimoSIPWithHashBinary(String descKey, long idUnitaDoc, String sipXml) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(true);// default il controllo passa a meno che il calcolo su SIP non dica il
        // contrario ...

        List<AroXmlUpdUnitaDoc> aroXmlUpdUnitaDocs = null;

        try {
            String queryStr = "select t from AroXmlUpdUnitaDoc t "
                    + "where t.aroUpdUnitaDoc.aroUnitaDoc.idUnitaDoc = :idUnitaDoc "
                    + "and t.tiXmlUpdUnitaDoc = :tiXmlUpdUnitaDoc " + "order by t.aroUpdUnitaDoc.pgUpdUnitaDoc DESC "; // ordinamento
            // per
            // progressivo
            // al
            // fine
            // di
            // ottenere
            // l'ultimo
            // aggiornamento
            // registrato
            javax.persistence.Query query = entityManager.createQuery(queryStr, AroXmlUpdUnitaDoc.class);
            query.setParameter("idUnitaDoc", idUnitaDoc);
            query.setParameter("tiXmlUpdUnitaDoc", TiXmlUpdUnitaDoc.RICHIESTA);
            aroXmlUpdUnitaDocs = query.getResultList();

            if (aroXmlUpdUnitaDocs.size() >= 1) {
                // se trovato, recupero il risultato che interessa per la verifica

                AroXmlUpdUnitaDoc last = aroXmlUpdUnitaDocs.get(0);
                // SIP hex binary
                String SIPhexBinary = new HashCalculator().calculateHashSHAX(sipXml, TipiHash.SHA_256).toHexBinary();

                // se stesso hash calcolato ....
                if (last.getDsHashXml().equals(SIPhexBinary)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_015_002);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_015_002, descKey));
                    rispostaControlli.setrBoolean(false);
                } else {
                    rispostaControlli.setrString(SIPhexBinary);
                    rispostaControlli.setrBoolean(true);
                }
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliUpdVersamento.verificaUltimoSIPWithHashBinary: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkTipoUDRegIamUserOrganizzazione(CSChiave csChiave,
            String descTipologiaUnitaDocumentaria, long idStruttura, long idUser, long idTipologiaUnitaDocumentaria,
            long idRegistro) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        List<IamAbilTipoDato> iamAbilTipoDatos = null;

        try {

            List<Long> ids = Arrays.asList(idTipologiaUnitaDocumentaria, idRegistro);

            String queryStr = "select t from IamAbilTipoDato t "
                    + "where t.iamAbilOrganiz.iamUser.idUserIam = :idUserIam "
                    + "and t.iamAbilOrganiz.idOrganizApplic = :idOrganizApplic  "
                    + "and t.idTipoDatoApplic in :idTipoDato  "
                    + "and t.nmClasseTipoDato in ('TIPO_UNITA_DOC','REGISTRO')  ";
            javax.persistence.Query query = entityManager.createQuery(queryStr, IamAbilTipoDato.class);
            query.setParameter("idOrganizApplic", idStruttura);
            query.setParameter("idUserIam", idUser);
            query.setParameter("idTipoDato", ids);

            iamAbilTipoDatos = query.getResultList();

            // ottengo i due risultati attesi -> abilitato al tipo fascicolo
            if (iamAbilTipoDatos.size() == 2) {
                rispostaControlli.setrObject(iamAbilTipoDatos);
                rispostaControlli.setrBoolean(true);
            } else {
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_016);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_016,
                        csChiave.getTipoRegistro(), descTipologiaUnitaDocumentaria));
            }

        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliUpdVersamento.checkTipoUDRegIamUserOrganizzazione: " + e.getMessage()));
            log.error("Eccezione nella lettura  della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli getUpdFlagsFromTipoUDOrgStrut(long idTipoUD, Boolean isForzaAggiornamento) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        // verifica tipologia UD
        rispostaControlli = this.getDecTipoUnitaDocById(idTipoUD);
        // ritorno errori sul tipo UD
        if (rispostaControlli.getrLong() != 0) {
            return rispostaControlli;
        }

        // recupero oggetto per costruzione struttura flag (+ verifica su
        // organizzazione)
        DecTipoUnitaDoc decTipoUnitaDoc = (DecTipoUnitaDoc) rispostaControlli.getrObject();
        // Recupero organizzazione
        OrgStrut orgStrut = decTipoUnitaDoc.getOrgStrut();
        if (orgStrut == null) {
            rispostaControlli.setrBoolean(false);
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "ControlliUpdVersamento.checkFlagUD.getOrgStrut: Errore recupero della struttura"));
            return rispostaControlli;// errore non previsto !
        }

        String flAbilitaUpdMeta = null, flAccettaUpdMetaInark = null, flForzaUpdMetaInark = null;

        // fl (possono essere nulli -> recepisco quelli dell'organizzazione)
        // flAbilitaUpdMeta = StringUtils.isNotBlank(decTipoUnitaDoc.getFlAbilitaUpdMeta())
        // ? decTipoUnitaDoc.getFlAbilitaUpdMeta()
        // : orgStrut.getFlAbilitaUpdMeta();
        // flAccettaUpdMetaInark = StringUtils.isNotBlank(decTipoUnitaDoc.getFlAccettaUpdMetaInark())
        // ? decTipoUnitaDoc.getFlAccettaUpdMetaInark()
        // : orgStrut.getFlAccettaUpdMetaInark();
        // flForzaUpdMetaInark = StringUtils.isNotBlank(decTipoUnitaDoc.getFlForzaUpdMetaInark())
        // ? decTipoUnitaDoc.getFlForzaUpdMetaInark()
        // : orgStrut.getFlForzaUpdMetaInark();

        // from PigParamApplic
        flAbilitaUpdMeta = configurationHelper.getParamApplicValueAsFl(ParametroApplFl.FL_ABILITA_UPD_META,
                orgStrut.getIdStrut(), orgStrut.getOrgEnte().getOrgAmbiente().getIdAmbiente(), idTipoUD);
        flAccettaUpdMetaInark = configurationHelper.getParamApplicValueAsFl(ParametroApplFl.FL_ACCETTA_UPD_META_INARK,
                orgStrut.getIdStrut(), orgStrut.getOrgEnte().getOrgAmbiente().getIdAmbiente(), idTipoUD);
        flForzaUpdMetaInark = configurationHelper.getParamApplicValueAsFl(ParametroApplFl.FL_FORZA_UPD_META_INARK,
                orgStrut.getIdStrut(), orgStrut.getOrgEnte().getOrgAmbiente().getIdAmbiente(), idTipoUD);

        // costruzione oggetto
        FlControlliUpd flControlliUpd = new FlControlliUpd();

        flControlliUpd.setFlAbilitaUpdMeta("1".equalsIgnoreCase(flAbilitaUpdMeta));
        flControlliUpd.setFlAccettaUpdMetaInark("1".equalsIgnoreCase(flAccettaUpdMetaInark));
        if (isForzaAggiornamento != null) {
            flControlliUpd.setFlForzaUpdMetaInark(isForzaAggiornamento.booleanValue());
        } else {
            flControlliUpd.setFlForzaUpdMetaInark("1".equalsIgnoreCase(flForzaUpdMetaInark));
        }
        //
        flControlliUpd.setFlProfiloUdObbOggetto(
                "1".equals(configurationHelper.getParamApplicValueAsFl(ParametroApplFl.FL_OBBL_OGGETTO,
                        orgStrut.getIdStrut(), orgStrut.getOrgEnte().getOrgAmbiente().getIdAmbiente(), idTipoUD)));
        flControlliUpd.setFlProfiloUdObbData(
                "1".equals(configurationHelper.getParamApplicValueAsFl(ParametroApplFl.FL_OBBL_DATA,
                        orgStrut.getIdStrut(), orgStrut.getOrgEnte().getOrgAmbiente().getIdAmbiente(), idTipoUD)));

        rispostaControlli.setrObject(flControlliUpd);
        rispostaControlli.setrBoolean(true);

        return rispostaControlli;
    }

    public RispostaControlli checkIdDocumentoInUD(long idUnitaDoc, String idDocumento, String tipoEntita,
            String tipoDocumento, String descChiaveDoc) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        List<AroDoc> lstAd;

        try {
            String queryStr = "select t from AroDoc t " + "where t.aroUnitaDoc.idUnitaDoc = :idUnitaDocIn "
                    + "and t.cdKeyDocVers = :cdKeyDocVersIn " + "and t.dtAnnul > :dataDiOggiIn ";
            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("idUnitaDocIn", idUnitaDoc);
            query.setParameter("cdKeyDocVersIn", idDocumento);
            query.setParameter("dataDiOggiIn", new Date());

            lstAd = query.getResultList();
            if (lstAd.size() == 1) {
                AroDoc aroDoc = lstAd.get(0);
                rispostaControlli.setrLong(aroDoc.getIdDoc());
                rispostaControlli.setrLongExtended(aroDoc.getIdDecTipoDoc());
                rispostaControlli.setrString(aroDoc.getTiDoc());
                rispostaControlli.setrStringExtended(aroDoc.getDecTipoDoc().getNmTipoDoc());
                rispostaControlli.setrBoolean(true);
            } else {
                rispostaControlli.setCodErr(MessaggiWSBundle.DOC_010_001);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.DOC_010_001, descChiaveDoc));
            }
        } catch (Exception e) {
            rispostaControlli.setrBoolean(false);
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "Eccezione ControlliUpdVersamento.checkIdDocumentoAndTipoInUD " + e.getMessage()));
            log.error("Eccezione nella verifica esistenza del documento da recuperare ", e);
        }
        return rispostaControlli;
    }

    public RispostaControlli checkTipoDocRegIamUserOrganizzazione(String tipoDocumento, long idTipoDocumento,
            long idStruttura, long idUser) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        List<IamAbilTipoDato> iamAbilTipoDatos = null;

        try {
            String queryStr = "select t from IamAbilTipoDato t "
                    + "where t.iamAbilOrganiz.iamUser.idUserIam = :idUserIam "
                    + "and t.iamAbilOrganiz.idOrganizApplic = :idOrganizApplic  "
                    + "and t.idTipoDatoApplic = :idTipoDocumento  " + "and t.nmClasseTipoDato = 'TIPO_DOC'  ";
            javax.persistence.Query query = entityManager.createQuery(queryStr, IamAbilTipoDato.class);
            query.setParameter("idOrganizApplic", idStruttura);
            query.setParameter("idUserIam", idUser);
            query.setParameter("idTipoDocumento", idTipoDocumento);

            iamAbilTipoDatos = query.getResultList();

            // ottengo un risultato -> abilitato al tipo fascicolo
            if (iamAbilTipoDatos.size() == 1) {
                rispostaControlli.setrLong(iamAbilTipoDatos.get(0).getIdAbilTipoDato());
                rispostaControlli.setrBoolean(true);
            } else {
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_017);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_017, tipoDocumento));
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliUpdVersamento.checkTipoDocRegIamUserOrganizzazione: " + e.getMessage()));
            log.error("Eccezione nella lettura  della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkIdComponenteInDoc(long idDocIn, int progressivo, String descChiaveComp) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        List<AroCompDoc> lstCompDocs;

        try {
            String queryStr = "select t from AroCompDoc t " + "where t.aroStrutDoc.aroDoc.idDoc = :idDocIn "
                    + "and t.niOrdCompDoc = :niOrdCompDocIn ";
            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("idDocIn", idDocIn);
            query.setParameter("niOrdCompDocIn", new BigDecimal(progressivo));

            lstCompDocs = query.getResultList();
            if (lstCompDocs.size() == 1) {
                rispostaControlli.setrLong(lstCompDocs.get(0).getIdCompDoc());

                rispostaControlli
                        .setrString(lstCompDocs.get(0).getAroStrutDoc().getDecTipoStrutDoc().getNmTipoStrutDoc());
                rispostaControlli.setrStringExtended(lstCompDocs.get(0).getDecTipoCompDoc().getNmTipoCompDoc());
                if (lstCompDocs.get(0).getAroCompDoc() != null) {
                    // questo permette di capire se è un sottocomponente
                    rispostaControlli.setrLongExtended(lstCompDocs.get(0).getAroCompDoc().getIdCompDoc());
                }
                // tipo supporto
                rispostaControlli.getrMap().put(RispostaControlli.ValuesOnrMap.TI_SUPPORTO_COMP.name(),
                        lstCompDocs.get(0).getTiSupportoComp());
                rispostaControlli.setrBoolean(true);
            } else {
                rispostaControlli.setCodErr(MessaggiWSBundle.COMP_010_001);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.COMP_010_001, descChiaveComp));
            }
        } catch (Exception e) {
            rispostaControlli.setrBoolean(false);
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "Eccezione ControlliRecupero.checkIdComponenteinDoc " + e.getMessage()));
            log.error("Eccezione nella verifica esistenza del componenente da recuperare ", e);
        }
        return rispostaControlli;
    }

    public RispostaControlli checkAroStrutDocAndTipo(long idAroCompDoc) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        AroCompDoc aroCompDoc;

        try {

            rispostaControlli = this.getAroCompDocById(idAroCompDoc);
            if (rispostaControlli.getrLong() != -1) {
                aroCompDoc = (AroCompDoc) rispostaControlli.getrObject();
                rispostaControlli.setrLong(aroCompDoc.getAroStrutDoc().getIdStrutDoc());
                rispostaControlli.setrLongExtended(aroCompDoc.getDecTipoCompDoc().getIdTipoCompDoc());
                rispostaControlli.setrBoolean(true);
            }

        } catch (Exception e) {
            rispostaControlli.setrBoolean(false);
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "Eccezione ControlliRecupero.checkAroStrutDocAndTipo " + e.getMessage()));
            log.error("Eccezione nella verifica esistenza della struttura e tipo componente da recuperare ", e);
        }
        return rispostaControlli;
    }

    private RispostaControlli getAroCompDocById(long idAroCompDoc) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        AroCompDoc aroCompDoc = null;

        try {
            aroCompDoc = entityManager.find(AroCompDoc.class, idAroCompDoc);
            if (aroCompDoc == null) {
                rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
                rispostaControlli
                        .setDsErr("ControlliUpdVersamento.getAroCompDocById: non presente per id " + idAroCompDoc);
            } else {
                rispostaControlli.setrLong(0);
                rispostaControlli.setrObject(aroCompDoc);
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliFascicoli.getAroCompDocById: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica " + e);
        }
        return rispostaControlli;
    }

    private RispostaControlli getDecTipoUnitaDocById(long idTipoUD) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        DecTipoUnitaDoc decTipoUnitaDoc = null;

        try {
            decTipoUnitaDoc = entityManager.find(DecTipoUnitaDoc.class, idTipoUD);
            if (decTipoUnitaDoc == null) {
                rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
                rispostaControlli
                        .setDsErr("ControlliUpdVersamento.getDecTipoUnitaDocById: non presente per id " + idTipoUD);
            } else {
                rispostaControlli.setrLong(0);
                rispostaControlli.setrObject(decTipoUnitaDoc);
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliFascicoli.getDecTipoUnitaDocById: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica " + e);
        }
        return rispostaControlli;
    }

    /*
     * Wrapper info fascicolo principale per controllo su aggiornamento fasciscoli secondari
     */
    private RispostaControlli getFascicoloPrincipale(long idUd) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        AroUnitaDoc aroUnitaDoc = null;

        try {
            aroUnitaDoc = entityManager.find(AroUnitaDoc.class, idUd);
            if (aroUnitaDoc == null) {
                rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
                rispostaControlli
                        .setDsErr("ControlliUpdVersamento.getFascicoloPrincipale: non presente per id " + idUd);
            } else {
                rispostaControlli.setrLong(0);
                // wrapping info fascicolo principale (all nullable)
                UpdFascPrincipale updFascPrincipale = new UpdFascPrincipale();
                updFascPrincipale.setClassifica(aroUnitaDoc.getDsClassifPrinc());
                updFascPrincipale.setIdentificativo(aroUnitaDoc.getCdFascicPrinc());
                updFascPrincipale.setOggetto(aroUnitaDoc.getDsOggettoFascicPrinc());
                updFascPrincipale.setSottoIdentificativo(aroUnitaDoc.getCdSottofascicPrinc());
                updFascPrincipale.setSottoOggetto(aroUnitaDoc.getDsOggettoSottofascicPrinc());
                //
                rispostaControlli.setrObject(updFascPrincipale);
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliUpdVersamento.getFascicoloPrincipale: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella " + e);
        }
        return rispostaControlli;
    }

    public RispostaControlli checkFascicoliSecondari(UpdVersamentoExt versamento) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        // recupero fascicolo principale
        rispostaControlli = this.getFascicoloPrincipale(versamento.getStrutturaUpdVers().getIdUd());

        // ritorno errori
        if (rispostaControlli.getrLong() != 0) {
            return rispostaControlli;
        }

        UpdFascPrincipale updFascPrincipale = (UpdFascPrincipale) rispostaControlli.getrObject();

        // preparo un array con i dati del fascicolo principale, per confrontarli
        // con quelli degli eventuali secondari
        String[] tmpArrP = new String[] { "", "", "", "", "" };

        if (updFascPrincipale.getClassifica() != null) {
            tmpArrP[0] = updFascPrincipale.getClassifica();
        }
        if (updFascPrincipale.getIdentificativo() != null) {
            tmpArrP[1] = updFascPrincipale.getIdentificativo();
        }
        if (updFascPrincipale.getOggetto() != null) {
            tmpArrP[2] = updFascPrincipale.getOggetto();
        }
        if (updFascPrincipale.getSottoIdentificativo() != null) {
            tmpArrP[3] = updFascPrincipale.getSottoIdentificativo();
        }
        if (updFascPrincipale.getSottoOggetto() != null) {
            tmpArrP[4] = updFascPrincipale.getSottoOggetto();
        }

        List tmpListP = Arrays.asList(tmpArrP);
        // verifica i fascicoli/sottofascicoli
        // rispetto il principale
        for (CamiciaFascicoloType fascicolo : versamento.getVersamento().getUnitaDocumentaria().getProfiloArchivistico()
                .getFascicoliSecondari().getFascicoloSecondario()) {
            String[] tmpArr = new String[] { "", "", "", "", "" };
            if (fascicolo.getClassifica() != null) {
                tmpArr[0] = fascicolo.getClassifica();
            }
            if (fascicolo.getFascicolo() != null && fascicolo.getFascicolo().getIdentificativo() != null) {
                tmpArr[1] = fascicolo.getFascicolo().getIdentificativo();
            }
            if (fascicolo.getFascicolo() != null && fascicolo.getFascicolo().getOggetto() != null) {
                tmpArr[2] = fascicolo.getFascicolo().getOggetto();
            }
            if (fascicolo.getSottoFascicolo() != null && fascicolo.getSottoFascicolo().getIdentificativo() != null) {
                tmpArr[3] = fascicolo.getSottoFascicolo().getIdentificativo();
            }
            if (fascicolo.getSottoFascicolo() != null && fascicolo.getSottoFascicolo().getOggetto() != null) {
                tmpArr[4] = fascicolo.getSottoFascicolo().getOggetto();
            }
            List tmpList = Arrays.asList(tmpArr);
            if (tmpListP.equals(tmpList)) {
                // se il fascicolo è uguale al principale,
                // restituisco errore
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_017_001);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_017_001));

                return rispostaControlli;
            }
        }
        // tutto ok
        rispostaControlli.setrBoolean(true);
        return rispostaControlli;
    }

    public RispostaControlli checkPartizioniStruttuaByAA(String descKey, long idStruttura, long anno) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);
        StringBuilder sb = new StringBuilder();

        List<OrgVChkPartitionUpdByAa> ovcspupd = null;
        try {
            String queryStr = "select t from OrgVChkPartitionUpdByAa t " + "where t.idStrut = :idStrutIn "
                    + "and t.anno = :annoIn ";

            javax.persistence.Query query = entityManager.createQuery(queryStr, OrgVChkPartitionUpdByAa.class);
            query.setParameter("idStrutIn", new BigDecimal(idStruttura));
            query.setParameter("annoIn", new BigDecimal(anno));

            //
            ovcspupd = query.getResultList();

            if (ovcspupd.size() == 1) {
                // trovata la struttura, vediamo se è tutta ben partizionata.
                // se ci sono problemi il versamento è sempre e comunque errato
                if (ovcspupd.get(0).getFlPartVersinidatispecAaOk().equals("1")
                        && ovcspupd.get(0).getFlPartVersinidatispecOk().equals("1")
                        && ovcspupd.get(0).getFlPartUpddatispecAaOk().equals("1")
                        && ovcspupd.get(0).getFlPartUpddatispecOk().equals("1")
                        && ovcspupd.get(0).getFlPartXmlupdOk().equals("1")
                        && ovcspupd.get(0).getFlPartXmlupdAaOk().equals("1")
                        && ovcspupd.get(0).getFlPartUpdkoAaOk().equals("1")
                        && ovcspupd.get(0).getFlPartUpdkoOk().equals("1")
                        && ovcspupd.get(0).getFlPartSesupdkoAaOk().equals("1")
                        && ovcspupd.get(0).getFlPartSesupdkoOk().equals("1")
                        && ovcspupd.get(0).getFlPartXmlsesupdkoOk().equals("1")
                        && ovcspupd.get(0).getFlPartXmlsesupdkpAaOk().equals("1")) {

                    rispostaControlli.setrBoolean(true);

                } else {
                    // recupero i flag settati a 0 e li concateno per inserirli nel messaggio di
                    // errore
                    if (ovcspupd.get(0).getFlPartVersinidatispecOk().equals("0")
                            || ovcspupd.get(0).getFlPartVersinidatispecAaOk().equals("0")) {
                        sb.append(StrutAaPartion.VERSINIDATISPEC.getPartitionName());
                    }
                    if (ovcspupd.get(0).getFlPartUpddatispecAaOk().equals("0")
                            || ovcspupd.get(0).getFlPartUpddatispecOk().equals("0")) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        sb.append(StrutAaPartion.UPDDATISPECUNITADOC.getPartitionName());
                    }
                    if (ovcspupd.get(0).getFlPartUpdkoOk().equals("0")
                            || ovcspupd.get(0).getFlPartUpdkoAaOk().equals("0")) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        sb.append(StrutAaPartion.UPDUNITADOCKO.getPartitionName());
                    }
                    if (ovcspupd.get(0).getFlPartXmlsesupdkoOk().equals("0")
                            || ovcspupd.get(0).getFlPartXmlsesupdkpAaOk().equals("0")) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        sb.append(StrutAaPartion.XMLSESUPDUNITADOCKO.getPartitionName());
                    }
                    if (ovcspupd.get(0).getFlPartXmlupdOk().equals("0")
                            || ovcspupd.get(0).getFlPartXmlupdAaOk().equals("0")) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        sb.append(StrutAaPartion.XMLUPDUNITADOC.getPartitionName());
                    }
                    if (ovcspupd.get(0).getFlPartSesupdkoAaOk().equals("0")
                            || ovcspupd.get(0).getFlPartSesupdkoOk().equals("0")) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        sb.append(StrutAaPartion.SESUPDUNITADOCKO.getPartitionName());
                    }

                }
            }

            if (!rispostaControlli.isrBoolean()) {
                rispostaControlli.setCodErr(MessaggiWSBundle.PART_006_001);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.PART_006_001, String.valueOf(anno),
                                (sb.length() == 0 ? sb.append(StrutAaPartion.printAll()) : sb.toString())));
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "UpdControlliPartizioni.verificaPartizioniStruttuaByAA: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkChiaveAndTipoDocPrinc(CSChiave key, long idStruttura, TipiGestioneUDAnnullate tguda) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);

        // prendo i paramentri dell'xml
        String numero = key.getNumero();
        Long anno = key.getAnno();
        String tipoReg = key.getTipoRegistro();

        // lista entity JPA ritornate dalle Query
        List results = null;

        // lancio query di controllo
        try {
            // ricavo le ud presenti in base ai parametri impostati
            String queryStr = "select ud, docs " + "from AroUnitaDoc ud INNER JOIN ud.aroDocs docs "
                    + "where ud.orgStrut.idStrut = :idStrutIn " + " and ud.cdKeyUnitaDoc = :cdKeyUnitaDocIn "
                    + " and ud.aaKeyUnitaDoc = :aaKeyUnitaDocIn "
                    + " and ud.cdRegistroKeyUnitaDoc = :cdRegistroKeyUnitaDocIn "
                    + " and docs.decTipoDoc.flTipoDocPrincipale = :documentoPrinc " + " order by ud.dtCreazione desc";

            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("idStrutIn", idStruttura);
            query.setParameter("cdKeyUnitaDocIn", numero);
            query.setParameter("aaKeyUnitaDocIn", anno);
            query.setParameter("cdRegistroKeyUnitaDocIn", tipoReg);
            query.setParameter("documentoPrinc", "1");// fixed : non ha senso recuperare altri tipi dato il controllo
            // adhoc
            results = query.getResultList();

            // chiave già presente (uno o più righe trovate, mi interessa solo l'ultima -
            // più recente)
            if (results.size() > 0) {
                Object[] result = (Object[]) results.get(0); // get first
                // recupero aroUnitaDoc
                AroUnitaDoc aroUnitaDoc = (AroUnitaDoc) (result)[0];
                // recupero aroDoc (se esiste)
                AroDoc aroDoc = (AroDoc) (result)[1];

                StatoConservazioneUnitaDoc scud = StatoConservazioneUnitaDoc
                        .valueOf(aroUnitaDoc.getTiStatoConservazione());
                if (scud == StatoConservazioneUnitaDoc.ANNULLATA
                        && tguda == TipiGestioneUDAnnullate.CONSIDERA_ASSENTE) {
                    // commuto l'errore in UD annullata e rendo true come risposta: in pratica come
                    // se non
                    // avesse trovato l'UD ma con un errore diverso: è lo stesso comportamento della
                    // vecchia versione del metodo
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_012_002);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_012_002,
                            MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));
                    rispostaControlli.setrBoolean(true);
                } else {
                    // gestione normale: ho trovato l'UD e non è annullata.
                    // Oppure è annullata e voglio caricarla lo stesso (il solo caso è nel ws
                    // recupero stato UD)
                    // intanto rendo l'errore di chiave già presente
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_002_001);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_002_001,
                            MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));

                    // id ud
                    rispostaControlli.setrLong(aroUnitaDoc.getIdUnitaDoc());
                    // se la chiave è già presente, oltre all'id dell'UD trovata,
                    // recupero il tipo di salvataggio. Mi serve sia nell'aggiunta documenti
                    // che nel recupero UD
                    rispostaControlli.setrString(aroUnitaDoc.getDecTipoUnitaDoc().getTiSaveFile());
                    //
                    rispostaControlli.setrStringExtended(aroUnitaDoc.getDecTipoUnitaDoc().getNmTipoUnitaDoc());
                    // lo stato conservazione viene usato per l'aggiunta doc:
                    // non posso aggiungere doc se l'ud è nello stato sbagliato
                    rispostaControlli.setrObject(scud);
                    // id tipo unita doc
                    rispostaControlli.setrLongExtended(aroUnitaDoc.getDecTipoUnitaDoc().getIdTipoUnitaDoc());
                    // add values
                    // tipo conservazione (not null)
                    rispostaControlli.getrMap().put(RispostaControlli.ValuesOnrMap.TI_CONSERVAZIONE.name(),
                            aroUnitaDoc.getTiConservazione());
                    // id tipo registro (not null)
                    rispostaControlli.getrMap().put(RispostaControlli.ValuesOnrMap.ID_REGISTROUD.name(),
                            aroUnitaDoc.getDecRegistroUnitaDoc().getIdRegistroUnitaDoc());
                    // sistema migrazione (nullable = default empty string)
                    rispostaControlli.getrMap().put(RispostaControlli.ValuesOnrMap.NM_SISTEMAMIGRAZ.name(),
                            StringUtils.isNotBlank(aroUnitaDoc.getNmSistemaMigraz()) ? aroUnitaDoc.getNmSistemaMigraz()
                                    : StringUtils.EMPTY);
                    // id tipo doc principale (un solo risultato atteso!)
                    rispostaControlli.getrMap().put(RispostaControlli.ValuesOnrMap.ID_TIPO_DOC_PRINC.name(),
                            aroDoc.getDecTipoDoc().getIdTipoDoc());
                    // forza collegamento
                    rispostaControlli.getrMap().put(RispostaControlli.ValuesOnrMap.FL_FORZA_COLL.name(),
                            aroUnitaDoc.getFlForzaCollegamento());
                    // recupero chiave normalizzata (se esiste)
                    rispostaControlli.getrMap().put(RispostaControlli.ValuesOnrMap.CD_KEY_NORMALIZED.name(),
                            aroUnitaDoc.getCdKeyUnitaDocNormaliz());
                }
                return rispostaControlli;
            }

            // Chiave non trovata
            rispostaControlli.setCodErr(MessaggiWSBundle.UD_005_001);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_005_001,
                    MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));
            rispostaControlli.setrBoolean(true);

        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliUpdVersamento.checkChiave: " + e.getMessage()));
            log.error("Eccezione nella lettura  della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

}
