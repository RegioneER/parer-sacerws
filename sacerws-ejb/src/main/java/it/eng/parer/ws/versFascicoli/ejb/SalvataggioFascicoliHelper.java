/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versFascicoli.ejb;

import static it.eng.parer.util.DateUtilsConverter.convert;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import it.eng.parer.entity.AroUnitaDoc;
import it.eng.parer.entity.DecAaTipoFascicolo;
import it.eng.parer.entity.DecModelloXsdFascicolo;
import it.eng.parer.entity.DecTipoFascicolo;
import it.eng.parer.entity.DecVoceTitol;
import it.eng.parer.entity.DecWarnAaTipoFascicolo;
import it.eng.parer.entity.FasAmminPartec;
import it.eng.parer.entity.FasFascicolo;
import it.eng.parer.entity.FasLinkFascicolo;
import it.eng.parer.entity.FasRespFascicolo;
import it.eng.parer.entity.FasSogFascicolo;
import it.eng.parer.entity.FasUniOrgRespFascicolo;
import it.eng.parer.entity.FasUnitaDocFascicolo;
import it.eng.parer.entity.FasWarnFascicolo;
import it.eng.parer.entity.FasXmlFascicolo;
import it.eng.parer.entity.FasXmlVersFascicolo;
import it.eng.parer.entity.IamUser;
import it.eng.parer.entity.OrgStrut;
import it.eng.parer.entity.VrsFascicoloKo;
import it.eng.parer.entity.VrsSesFascicoloKo;
import it.eng.parer.util.ejb.AppServerInstance;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.XmlFascCache;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.CostantiDB.TipiHash;
import it.eng.parer.ws.utils.HashCalculator;
import it.eng.parer.ws.utils.LogSessioneUtils;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.utils.MessaggiWSHelper;
import it.eng.parer.ws.versFascicoli.dto.CompRapportoVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.DXPGAmminPartecipante;
import it.eng.parer.ws.versFascicoli.dto.DXPGAmminTitolare;
import it.eng.parer.ws.versFascicoli.dto.DXPGProcAmmininistrativo;
import it.eng.parer.ws.versFascicoli.dto.DXPGRespFascicolo;
import it.eng.parer.ws.versFascicoli.dto.DXPGSoggettoCoinvolto;
import it.eng.parer.ws.versFascicoli.dto.DatiXmlProfiloGenerale;
import it.eng.parer.ws.versFascicoli.dto.FascicoloLink;
import it.eng.parer.ws.versFascicoli.dto.RispostaWSFascicolo;
import it.eng.parer.ws.versFascicoli.dto.StrutturaVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.VersFascicoloExt;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;

/**
 *
 * @author fioravanti_f
 */
@Stateless(mappedName = "SalvataggioFascicoliHelper")
@LocalBean
public class SalvataggioFascicoliHelper {

    //

    @EJB
    MessaggiWSHelper messaggiWSHelper;

    @EJB
    XmlFascCache xmlFascCache;

    @EJB
    private AppServerInstance appServerInstance;

    @EJB
    LogSessioneFascicoliHelper logSessioneFascicoliHelper;

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    private static final int DS_INDICE_CLASS_MAX_LEN = 254;
    private static final int DS_NOTA_MAX_LEN = 4000;
    private static final int DS_OGGETTO_FASC_MAX_LEN = 4000;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviFascicolo(RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento,
            SyncFakeSessn sessione, VrsFascicoloKo fascicoloKo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
        // salvo il fascicolo quando il versamento è andato bene
        tmpRispostaControlli.setrBoolean(false);
        try {
            Calendar tmpCal = GregorianCalendar.getInstance();
            tmpCal.set(2444, 11, 31, 0, 0, 0); // 31 dicembre 2444, data di annullo fittizia

            FasFascicolo tmpFascicolo = new FasFascicolo();

            tmpFascicolo.setOrgStrut(entityManager.find(OrgStrut.class, svf.getIdStruttura()));
            tmpFascicolo.setAaFascicolo(new BigDecimal(svf.getChiaveNonVerificata().getAnno()));
            tmpFascicolo.setCdKeyFascicolo(svf.getChiaveNonVerificata().getNumero());
            tmpFascicolo.setDtAnnull(tmpCal.getTime());
            tmpFascicolo.setTsIniSes(convert(sessione.getTmApertura()));
            tmpFascicolo.setTsFineSes(convert(sessione.getTmChiusura()));
            // salvo il nome del server/istanza nel cluster che sta salvando i dati e ha gestito il versamento
            tmpFascicolo.setCdIndServer(appServerInstance.getName());
            // salvo l'indirizzo IP del sistema che ha effettuato la richiesta di versamento/aggiunta
            tmpFascicolo.setCdIndIpClient(sessione.getIpChiamante());
            // salvo l'utente identificato (se non è identificato, la sessione è errata)
            tmpFascicolo.setIamUser(entityManager.find(IamUser.class, versamento.getUtente().getIdUtente()));
            tmpFascicolo.setDecTipoFascicolo(entityManager.find(DecTipoFascicolo.class, svf.getIdTipoFascicolo()));
            //
            tmpFascicolo.setTiConservazione(it.eng.parer.entity.constraint.FasFascicolo.TiConservazione
                    .valueOf(versamento.getVersamento().getParametri().getTipoConservazione().name()));
            // TODO IL sistema di migraziobe va gestito
            //
            tmpFascicolo.setFlForzaContrClassif(svf.getFlControlliFasc().isFlForzaContrFlassif() ? "1" : "0");
            tmpFascicolo.setFlForzaContrColleg(svf.getFlControlliFasc().isFlForzaContrColleg() ? "1" : "0");
            tmpFascicolo.setFlForzaContrNumero(svf.getFlControlliFasc().isFlForzaContrNumero() ? "1" : "0");
            tmpFascicolo.setFlUpdAnnulUnitaDoc("0"); // fixed
            //
            if (svf.getDatiXmlProfiloArchivistico() != null) {
                tmpFascicolo.setCdIndiceClassif(svf.getDatiXmlProfiloArchivistico().getIndiceClassificazione());
                if (StringUtils.isNotBlank(svf.getDatiXmlProfiloArchivistico().getDescIndiceClassificazione())) {
                    tmpFascicolo.setDsIndiceClassif(LogSessioneUtils.getStringAtMaxLen(
                            svf.getDatiXmlProfiloArchivistico().getDescIndiceClassificazione(),
                            DS_INDICE_CLASS_MAX_LEN));
                }
            }

            if (svf.getIdVoceTitol() != null) {
                tmpFascicolo.setDecVoceTitol(entityManager.find(DecVoceTitol.class, svf.getIdVoceTitol().longValue()));
            }

            if (svf.getDatiXmlProfiloGenerale() != null) {
                DatiXmlProfiloGenerale dxpg = svf.getDatiXmlProfiloGenerale();
                // oggetto fascicolo
                if (StringUtils.isNotBlank(dxpg.getOggettoFascicolo())) {
                    tmpFascicolo.setDsOggettoFascicolo(
                            LogSessioneUtils.getStringAtMaxLen(dxpg.getOggettoFascicolo(), DS_OGGETTO_FASC_MAX_LEN));
                }
                // popolamento data apertura, chiusura, ud first e last, aaconservazione, note
                tmpFascicolo.setDtApeFascicolo(dxpg.getDataApertura());
                tmpFascicolo.setDtChiuFascicolo(dxpg.getDataChiusura());
                if (dxpg.getIdPrimoDocumento() != null) {
                    tmpFascicolo.setAroUnitaDocFirst(entityManager.find(AroUnitaDoc.class, dxpg.getIdPrimoDocumento()));
                }
                if (dxpg.getIdUltimoDocumento() != null) {
                    tmpFascicolo.setAroUnitaDocLast(entityManager.find(AroUnitaDoc.class, dxpg.getIdUltimoDocumento()));
                }
                if (dxpg.getTempoConservazione() != null) {
                    tmpFascicolo.setNiAaConservazione(dxpg.getTempoConservazione());
                } else {
                    tmpFascicolo.setNiAaConservazione(new BigDecimal(9999));
                }
                /*
                 * if(!dxpg.getAmmPartecipanti().isEmpty()) {
                 * tmpFascicolo.setFasAmminPartecs(dxpg.getAmmPartecipanti()); }
                 */
                if (StringUtils.isNotBlank(dxpg.getNoteFascicolo())) {
                    tmpFascicolo
                            .setDsNota(LogSessioneUtils.getStringAtMaxLen(dxpg.getNoteFascicolo(), DS_NOTA_MAX_LEN));
                }

                tmpFascicolo.setCdLivelloRiserv(dxpg.getLvlRiservatezza());
                if (dxpg.getAmmtitolare() != null) {
                    DXPGAmminTitolare titolare = dxpg.getAmmtitolare();
                    tmpFascicolo.setCdAmminTitol(titolare.getCodice());
                    tmpFascicolo.setDsAmminTitol(titolare.getDenominazione());
                    tmpFascicolo.setTiCodiceAmminTitol(titolare.getTiCodice());
                }

                if (dxpg.getProcAmm() != null) {
                    DXPGProcAmmininistrativo procAmm = dxpg.getProcAmm();
                    tmpFascicolo.setCdProcAmmin(procAmm.getCodice());
                    tmpFascicolo.setDsProcAmmin(procAmm.getDenominazione());
                }

            }
            // TODO: gestire questi campi:
            // aa_fascicolo_padre

            // cd_key_fascicolo_padre ds_oggetto_fascicolo_padre id_fascicolo_padre
            //
            tmpFascicolo.setCdKeyOrd(svf.getKeyOrdCalcolata());
            tmpFascicolo.setNiUnitaDoc(new BigDecimal(svf.getUnitaDocElencate().size()));
            tmpFascicolo.setNiSottoFascicoli(BigDecimal.ZERO);
            tmpFascicolo.setTiStatoFascElencoVers(
                    it.eng.parer.entity.constraint.FasFascicolo.TiStatoFascElencoVers.IN_ATTESA_SCHED);
            // da fare: id_elenco_vers_fasc
            tmpFascicolo.setTiStatoConservazione(
                    it.eng.parer.entity.constraint.FasFascicolo.TiStatoConservazione.PRESA_IN_CARICO);
            // gestione fascicolo KO, in funzione della presenza o meno di un fascicolo KO
            // corrispondente
            if (fascicoloKo != null) {
                tmpFascicolo.setFlSesFascicoloKo("1");
            } else {
                tmpFascicolo.setFlSesFascicoloKo("0"); // il campo in tabella è NOT NULL
            }
            entityManager.persist(tmpFascicolo);
            tmpRispostaControlli.setrObject(tmpFascicolo);
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio fascicolo: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviAmmPartecipanti(RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento,
            SyncFakeSessn sessione, FasFascicolo fascicolo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
        // salvo amm. partecipanti del fascicolo
        tmpRispostaControlli.setrBoolean(false);
        try {
            if (svf.getDatiXmlProfiloGenerale() != null
                    && svf.getDatiXmlProfiloGenerale().getAmmPartecipanti().size() > 0) {
                DatiXmlProfiloGenerale dxpg = svf.getDatiXmlProfiloGenerale();
                for (DXPGAmminPartecipante ammPart : dxpg.getAmmPartecipanti()) {
                    FasAmminPartec fasAmminPartec = new FasAmminPartec();
                    fasAmminPartec.setDsAmminPartec(ammPart.getDenominazione());
                    fasAmminPartec.setCdAmminPartec(ammPart.getCodice());
                    fasAmminPartec.setTiCodiceAmminPartec(ammPart.getTipoCodice());// TODO: è codificato?
                    fasAmminPartec.setFasFascicolo(fascicolo);
                    entityManager.persist(fasAmminPartec);
                    fascicolo.getFasAmminPartecs().add(fasAmminPartec);
                }
            }
            // OK
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio fascicolo: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviSoggCoinvolti(RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento,
            SyncFakeSessn sessione, FasFascicolo fascicolo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
        // salvo amm. partecipanti del fascicolo
        tmpRispostaControlli.setrBoolean(false);
        try {
            if (svf.getDatiXmlProfiloGenerale() != null
                    && svf.getDatiXmlProfiloGenerale().getSoggettiCoinvolti().size() > 0) {
                DatiXmlProfiloGenerale dxpg = svf.getDatiXmlProfiloGenerale();
                for (DXPGSoggettoCoinvolto sogg : dxpg.getSoggettiCoinvolti()) {
                    FasSogFascicolo fasSogFascicolo = new FasSogFascicolo();
                    // can be null
                    fasSogFascicolo.setDsDenomSog(sogg.getDenominazione());
                    // can be null
                    fasSogFascicolo.setNmCognSog(sogg.getCognome());
                    // can be null
                    fasSogFascicolo.setNmNomeSog(sogg.getNome());

                    fasSogFascicolo.setTiCdSog(sogg.getTipoIdentificativo());
                    fasSogFascicolo.setTiRapp(sogg.getTipoRapporto());
                    fasSogFascicolo.setCdSog(sogg.getIdentificativo());
                    fasSogFascicolo.setFasFascicolo(fascicolo);

                    entityManager.persist(fasSogFascicolo);
                    fascicolo.getFasSogFascicolos().add(fasSogFascicolo);
                }
            }
            // OK
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio fascicolo: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviResponsabili(RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento,
            SyncFakeSessn sessione, FasFascicolo fascicolo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
        // salvo amm. partecipanti del fascicolo
        tmpRispostaControlli.setrBoolean(false);
        try {
            if (svf.getDatiXmlProfiloGenerale() != null
                    && svf.getDatiXmlProfiloGenerale().getResponsabili().size() > 0) {
                DatiXmlProfiloGenerale dxpg = svf.getDatiXmlProfiloGenerale();
                for (DXPGRespFascicolo resp : dxpg.getResponsabili()) {
                    FasRespFascicolo fasRespFascicolo = new FasRespFascicolo();
                    /*
                     * TODO: TiOggResp.FASCICOLO is fixed (da gestire il caso PROC_AMMIN)
                     */
                    fasRespFascicolo.setTiOggResp(it.eng.parer.entity.constraint.FasRespFascicolo.TiOggResp.FASCICOLO);
                    fasRespFascicolo.setTiResp(resp.getResponsabilita());

                    fasRespFascicolo.setNmNomeResp(resp.getNome());
                    fasRespFascicolo.setNmCognResp(resp.getCognome());
                    fasRespFascicolo.setCdResp(resp.getCdIdentificativo());
                    fasRespFascicolo.setTiCdResp(resp.getTiCdIdentificativo());
                    fasRespFascicolo.setFasFascicolo(fascicolo);
                    entityManager.persist(fasRespFascicolo);
                    fascicolo.getFasRespFascicolos().add(fasRespFascicolo);
                }
            }
            // OK
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio fascicolo: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviUoOrgResponsabili(RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento,
            SyncFakeSessn sessione, FasFascicolo fascicolo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
        // salvo amm. partecipanti del fascicolo
        tmpRispostaControlli.setrBoolean(false);
        try {
            if (svf.getDatiXmlProfiloGenerale() != null
                    && svf.getDatiXmlProfiloGenerale().getUoOrgResponsabili().size() > 0) {
                DatiXmlProfiloGenerale dxpg = svf.getDatiXmlProfiloGenerale();
                for (String uo : dxpg.getUoOrgResponsabili()) {
                    FasUniOrgRespFascicolo fasUniOrgRespFascicolo = new FasUniOrgRespFascicolo();
                    fasUniOrgRespFascicolo.setCdUniOrgResp(uo);
                    fasUniOrgRespFascicolo.setFasFascicolo(fascicolo);

                    entityManager.persist(fasUniOrgRespFascicolo);
                    fascicolo.getFasUniOrgRespFascicolos().add(fasUniOrgRespFascicolo);
                }
            }
            // OK
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio fascicolo: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviRequestResponseFascicolo(RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento,
            SyncFakeSessn sessione, FasFascicolo fascicolo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        // salvo xml di request e response
        tmpRispostaControlli.setrBoolean(false);
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
        CompRapportoVersFascicolo esito = rispostaWs.getCompRapportoVersFascicolo();
        try {
            FasXmlVersFascicolo tmpXmlVersFascicolo = new FasXmlVersFascicolo();
            tmpXmlVersFascicolo.setFasFascicolo(fascicolo);
            tmpXmlVersFascicolo.setTiXmlVers(CostantiDB.TipiXmlDati.RICHIESTA);
            tmpXmlVersFascicolo.setCdVersioneXml(svf.getVersioneIndiceSipNonVerificata());
            tmpXmlVersFascicolo.setBlXmlVers(
                    sessione.getDatiDaSalvareIndiceSip().length() == 0 ? "--" : sessione.getDatiDaSalvareIndiceSip());
            tmpXmlVersFascicolo.setCdEncodingHashXmlVers(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi());
            // tmpXmlVersFascicolo.setDsAlgoHashXmlVers(CostantiDB.TipiHash.SHA_1.descrivi());
            tmpXmlVersFascicolo.setDsAlgoHashXmlVers(CostantiDB.TipiHash.SHA_256.descrivi());
            tmpXmlVersFascicolo.setDsUrnXmlVers(MessaggiWSFormat.formattaUrnIndiceSipFasc(
                    versamento.getStrutturaComponenti().getUrnPartChiaveFascicolo(),
                    Costanti.UrnFormatter.URN_INDICE_SIP_V2));
            // normalized URN
            tmpXmlVersFascicolo.setDsUrnNormalizXmlVers(MessaggiWSFormat.formattaUrnIndiceSipFasc(
                    versamento.getStrutturaComponenti().getUrnPartChiaveFascicoloNormalized(),
                    Costanti.UrnFormatter.URN_INDICE_SIP_V2));
            tmpXmlVersFascicolo.setDsHashXmlVers(new HashCalculator()
                    .calculateHashSHAX(sessione.getDatiDaSalvareIndiceSip(), TipiHash.SHA_256).toHexBinary());
            tmpXmlVersFascicolo.setIdStrut(new BigDecimal(svf.getIdStruttura()));
            tmpXmlVersFascicolo.setDtVersFascicolo(convert(sessione.getTmApertura()));
            entityManager.persist(tmpXmlVersFascicolo);
            fascicolo.getFasXmlVersFascicolos().add(tmpXmlVersFascicolo);

            String xmlesito = logSessioneFascicoliHelper.generaRapportoVersamento(rispostaWs);

            tmpXmlVersFascicolo = new FasXmlVersFascicolo();
            tmpXmlVersFascicolo.setFasFascicolo(fascicolo);
            tmpXmlVersFascicolo.setTiXmlVers(CostantiDB.TipiXmlDati.RISPOSTA);
            tmpXmlVersFascicolo.setCdVersioneXml(esito.getVersioneRapportoVersamento());
            tmpXmlVersFascicolo.setBlXmlVers(xmlesito);
            tmpXmlVersFascicolo.setCdEncodingHashXmlVers(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi());
            // tmpXmlVersFascicolo.setDsAlgoHashXmlVers(CostantiDB.TipiHash.SHA_1.descrivi());
            tmpXmlVersFascicolo.setDsAlgoHashXmlVers(CostantiDB.TipiHash.SHA_256.descrivi());
            tmpXmlVersFascicolo.setDsUrnXmlVers(MessaggiWSFormat.formattaUrnRappVersFasc(
                    versamento.getStrutturaComponenti().getUrnPartChiaveFascicolo(),
                    Costanti.UrnFormatter.URN_RAPP_VERS_V2));
            // normalized URN
            tmpXmlVersFascicolo.setDsUrnNormalizXmlVers(MessaggiWSFormat.formattaUrnRappVersFasc(
                    versamento.getStrutturaComponenti().getUrnPartChiaveFascicoloNormalized(),
                    Costanti.UrnFormatter.URN_RAPP_VERS_V2));
            // tmpXmlVersFascicolo.setDsHashXmlVers(new HashCalculator().calculateHash(xmlesito).toHexBinary());
            tmpXmlVersFascicolo
                    .setDsHashXmlVers(new HashCalculator().calculateHashSHAX(xmlesito, TipiHash.SHA_256).toHexBinary());
            tmpXmlVersFascicolo.setIdStrut(new BigDecimal(svf.getIdStruttura()));
            tmpXmlVersFascicolo.setDtVersFascicolo(convert(sessione.getTmApertura()));
            entityManager.persist(tmpXmlVersFascicolo);
            fascicolo.getFasXmlVersFascicolos().add(tmpXmlVersFascicolo);

            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio XML versamento fascicolo: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviProfiliXMLFascicolo(RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento,
            SyncFakeSessn sessione, FasFascicolo fascicolo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
        // salvo gli XML relativi al profilo Archivistico ed al profilo Generale
        // estratti dall'XML generale dei metadati
        tmpRispostaControlli.setrBoolean(false);
        try {
            FasXmlFascicolo fasXmlFascicolo = new FasXmlFascicolo();
            fasXmlFascicolo.setFasFascicolo(fascicolo);
            fasXmlFascicolo.setTiModelloXsd(ControlliProfiliFascicolo.NOME_METADATI_PROFILO);
            fasXmlFascicolo.setDecModelloXsdFascicolo(
                    entityManager.find(DecModelloXsdFascicolo.class, svf.getIdRecxsdProfiloGenerale()));
            String xmlProfilo = this.generaXmlProfilo(versamento.getVersamento().getProfiloGenerale().getAny());
            fasXmlFascicolo.setBlXml(xmlProfilo);
            fasXmlFascicolo.setIdStrut(new BigDecimal(svf.getIdStruttura()));
            fasXmlFascicolo.setDtVersFascicolo(convert(sessione.getTmApertura()));
            entityManager.persist(fasXmlFascicolo);
            fascicolo.getFasXmlFascicolos().add(fasXmlFascicolo);

            // il profilo archivistico potrebbe non esistere
            if (svf.getIdRecXsdProfiloArchivistico() > 0) {
                fasXmlFascicolo = new FasXmlFascicolo();
                fasXmlFascicolo.setFasFascicolo(fascicolo);
                fasXmlFascicolo.setTiModelloXsd(ControlliProfiliFascicolo.NOME_SEGNATURA_ARCHIVISTICA);
                fasXmlFascicolo.setDecModelloXsdFascicolo(
                        entityManager.find(DecModelloXsdFascicolo.class, svf.getIdRecXsdProfiloArchivistico()));
                xmlProfilo = this
                        .generaXmlProfilo(versamento.getVersamento().getProfiloArchivistico().getValue().getAny());
                fasXmlFascicolo.setBlXml(xmlProfilo);
                fasXmlFascicolo.setIdStrut(new BigDecimal(svf.getIdStruttura()));
                fasXmlFascicolo.setDtVersFascicolo(convert(sessione.getTmApertura()));
                entityManager.persist(fasXmlFascicolo);
                fascicolo.getFasXmlFascicolos().add(fasXmlFascicolo);
            }
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio profili xml fascicolo: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviUnitaDocFascicolo(RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento,
            SyncFakeSessn sessione, FasFascicolo fascicolo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
        // salvo i riferimenti alle unità documentarie del fascicolo
        tmpRispostaControlli.setrBoolean(false);
        try {
            if (svf.getUnitaDocElencate() != null) {
                for (Long tmpId : svf.getUnitaDocElencate()) {
                    FasUnitaDocFascicolo fasUnitaDocFascicolo = new FasUnitaDocFascicolo();
                    fasUnitaDocFascicolo.setFasFascicolo(fascicolo);
                    fasUnitaDocFascicolo.setAroUnitaDoc(entityManager.find(AroUnitaDoc.class, tmpId));
                    entityManager.persist(fasUnitaDocFascicolo);
                    fascicolo.getFasUnitaDocFascicolos().add(fasUnitaDocFascicolo);
                }
            }
            //
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio unità doc fascicolo: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviLinkFascicolo(RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento,
            SyncFakeSessn sessione, FasFascicolo tmpFasFascicolo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
        // salvo i riferimenti alle unità documentarie del fascicolo
        tmpRispostaControlli.setrBoolean(false);
        try {
            if (svf.getFascicoliLinked() != null) {
                for (FascicoloLink link : svf.getFascicoliLinked()) {
                    FasLinkFascicolo fasLinkFascicolo = new FasLinkFascicolo();
                    fasLinkFascicolo.setCdKeyFascicoloLink(link.getCsChiaveFasc().getNumero());
                    fasLinkFascicolo.setAaFascicoloLink(BigDecimal.valueOf(link.getCsChiaveFasc().getAnno()));
                    fasLinkFascicolo.setDsLink(link.getDescCollegamento());
                    fasLinkFascicolo.setFasFascicolo(tmpFasFascicolo);
                    if (link.getIdLinkFasc() != null) {
                        fasLinkFascicolo.setFasFascicoloLink(
                                entityManager.find(FasFascicolo.class, link.getIdLinkFasc().longValue()));
                    }

                    entityManager.persist(fasLinkFascicolo);
                    tmpFasFascicolo.getFasLinkFascicolos1().add(fasLinkFascicolo);
                }
            }
            //
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio unità doc fascicolo: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }
        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviWarningFascicolo(RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento,
            SyncFakeSessn sessione, FasFascicolo fascicolo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        // salvo i warning della sessione di versamento
        // Nota: al momento della stesura di questo codice il web service
        // non prevede la restituzione di warning, Questo codice è quindi
        // abbastanza inattivo.
        tmpRispostaControlli.setrBoolean(false);
        int progErrore = 1;
        try {
            FasWarnFascicolo fasWarnFascicolo;
            for (VoceDiErrore tmpVoceDiErrore : versamento.getErroriTrovati()) {
                fasWarnFascicolo = new FasWarnFascicolo();
                fasWarnFascicolo.setFasFascicolo(fascicolo);
                fasWarnFascicolo.setPgWarn(new BigDecimal(progErrore));
                fasWarnFascicolo.setDecErrSacer(messaggiWSHelper.caricaDecErrore(tmpVoceDiErrore.getErrorCode()));
                fasWarnFascicolo.setDsWarn(LogSessioneUtils.getDsErrAtMaxLen(tmpVoceDiErrore.getErrorMessage()));
                //
                entityManager.persist(fasWarnFascicolo);
                fascicolo.getFasWarnFascicolos().add(fasWarnFascicolo);
                progErrore++;
            }
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio warning fascicolo: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli salvaWarningAATipoFascicolo(RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento,
            SyncFakeSessn sessione, FasFascicolo tmpFasFascicolo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);
        try {
            long numWarningAggiornati;
            String queryStr = "update DecWarnAaTipoFascicolo al "
                    + "set al.flWarnAaTipoFascicolo = :flWarnAaTipoFascicolo "
                    + "where al.decAaTipoFascicolo.idAaTipoFascicolo = :idAaTipoFascicolo "
                    + "and al.aaTipoFascicolo = :aaTipoFascicolo ";

            // eseguo l'update dell'eventuale record relativo all'anno
            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("flWarnAaTipoFascicolo", "1");
            query.setParameter("idAaTipoFascicolo",
                    versamento.getStrutturaComponenti().getConfigNumFasc().getIdAaNumeroFasc());
            query.setParameter("aaTipoFascicolo",
                    new BigDecimal(versamento.getVersamento().getIntestazione().getChiave().getAnno()));
            numWarningAggiornati = query.executeUpdate();
            // se non ho aggiornato alcun record, vuol dire che lo devo creare...
            if (numWarningAggiornati == 0) {
                DecWarnAaTipoFascicolo tmpDecWarnAaTipoFascicolo = new DecWarnAaTipoFascicolo();
                tmpDecWarnAaTipoFascicolo.setAaTipoFascicolo(
                        new BigDecimal(versamento.getVersamento().getIntestazione().getChiave().getAnno()));
                tmpDecWarnAaTipoFascicolo.setDecAaTipoFascicolo(entityManager.find(DecAaTipoFascicolo.class,
                        versamento.getStrutturaComponenti().getConfigNumFasc().getIdAaNumeroFasc()));
                tmpDecWarnAaTipoFascicolo.setFlWarnAaTipoFascicolo("1");
                entityManager.persist(tmpDecWarnAaTipoFascicolo);
                entityManager.flush();
            }
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio warning aa tipo fascicolo: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli ereditaVersamentiKoFascicolo(RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento,
            SyncFakeSessn sessione, FasFascicolo fascicolo, VrsFascicoloKo fascicoloKo) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        //
        tmpRispostaControlli.setrBoolean(false);
        try {
            // aggiorno tutte le sessione fallite relative al fascicolo KO
            // ancora lockato: pongo a null la FK al fascicolo KO, imposto la
            // FK al fascicolo appena creato e pongo lo stato a RISOLTO
            for (VrsSesFascicoloKo vsfk : fascicoloKo.getVrsSesFascicoloKos()) {
                vsfk.setFasFascicolo(fascicolo);
                vsfk.setVrsFascicoloKo(null);
                vsfk.setTiStatoSes(LogSessioneFascicoliHelper.FAS_RISOLTO);
            }
            entityManager.flush();
            //
            // rimuovo il fascicolo KO
            entityManager.remove(fascicoloKo);
            entityManager.flush();
            tmpRispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio fascicolo: " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }

    private String generaXmlProfilo(Node profilo) throws JAXBException, SAXException, TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(profilo);
        StreamResult result = new StreamResult(new StringWriter());
        transformer.transform(source, result);
        return result.getWriter().toString();
    }

}
