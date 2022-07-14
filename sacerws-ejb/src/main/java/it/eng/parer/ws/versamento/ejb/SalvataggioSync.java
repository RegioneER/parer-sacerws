package it.eng.parer.ws.versamento.ejb;

import it.eng.parer.entity.AroArchivSec;
import it.eng.parer.entity.AroBustaCrittog;
import it.eng.parer.entity.AroCompDoc;
import it.eng.parer.entity.AroCompUrnCalc;
import it.eng.parer.entity.AroDoc;
import it.eng.parer.entity.AroFirmaComp;
import it.eng.parer.entity.AroLinkUnitaDoc;
import it.eng.parer.entity.AroMarcaComp;
import it.eng.parer.entity.AroStrutDoc;
import it.eng.parer.entity.AroUnitaDoc;
import it.eng.parer.entity.AroUpdUnitaDoc;
import it.eng.parer.entity.AroUsoXsdDatiSpec;
import it.eng.parer.entity.AroValoreAttribDatiSpec;
import it.eng.parer.entity.AroVerIndiceAipUd;
import it.eng.parer.entity.AroWarnUnitaDoc;
import it.eng.parer.entity.AroXmlUpdUnitaDoc;
import it.eng.parer.entity.DecAaRegistroUnitaDoc;
import it.eng.parer.entity.DecAttribDatiSpec;
import it.eng.parer.entity.DecFormatoFileDoc;
import it.eng.parer.entity.DecFormatoFileStandard;
import it.eng.parer.entity.DecRegistroUnitaDoc;
import it.eng.parer.entity.DecTipoCompDoc;
import it.eng.parer.entity.DecTipoDoc;
import it.eng.parer.entity.DecTipoRapprComp;
import it.eng.parer.entity.DecTipoStrutDoc;
import it.eng.parer.entity.DecTipoUnitaDoc;
import it.eng.parer.entity.DecWarnAaRegistroUd;
import it.eng.parer.entity.DecXsdDatiSpec;
import it.eng.parer.entity.ElvDocAggDaElabElenco;
import it.eng.parer.entity.ElvUdVersDaElabElenco;
import it.eng.parer.entity.FasFascicolo;
import it.eng.parer.entity.FirReport;
import it.eng.parer.entity.IamUser;
import it.eng.parer.entity.MonAaUnitaDocRegistro;
import it.eng.parer.entity.OrgStrut;
import it.eng.parer.entity.OrgSubStrut;
import it.eng.parer.entity.SerVerSerie;
import it.eng.parer.entity.VrsUrnXmlSessioneVers;
import it.eng.parer.entity.VrsXmlDatiSessioneVers;
import it.eng.parer.entity.constraint.AroCompUrnCalc.TiUrn;
import it.eng.parer.entity.constraint.VrsUrnXmlSessioneVers.TiUrnXmlSessioneVers;
import it.eng.parer.viewEntity.VrsVLisXmlDocUrnDaCalc;
import it.eng.parer.viewEntity.VrsVLisXmlUdUrnDaCalc;
import it.eng.parer.viewEntity.VrsVLisXmlUpdUrnDaCalc;
import it.eng.parer.firma.dto.CompDocMock;
import it.eng.parer.firma.ejb.SalvataggioFirmaManager;
import it.eng.parer.firma.util.VerificaFirmaEnums;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.BinEncUtility;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.Costanti.CategoriaDocumento;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.CostantiDB.Flag;
import it.eng.parer.ws.utils.CostantiDB.TipiEntitaSacer;
import it.eng.parer.ws.utils.CostantiDB.TipiHash;
import it.eng.parer.ws.utils.CostantiDB.TipiStatoElementoVersato;
import it.eng.parer.ws.utils.CostantiDB.TipiXmlDati;
import it.eng.parer.ws.utils.CostantiDB.TipoCreazioneDoc;
import it.eng.parer.ws.utils.HashCalculator;
import it.eng.parer.ws.utils.JAXBUtils;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.versamento.dto.AbsVersamentoExt;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.versamento.dto.DatoSpecifico;
import it.eng.parer.ws.versamento.dto.DocumentoVers;
import it.eng.parer.ws.versamento.dto.FileBinario;
import it.eng.parer.ws.versamento.dto.IRispostaVersWS;
import it.eng.parer.ws.versamento.dto.RispostaWS;
import it.eng.parer.ws.versamento.dto.RispostaWSAggAll;
import it.eng.parer.ws.versamento.dto.StrutturaVersamento;
import it.eng.parer.ws.versamento.dto.UnitaDocColl;
import it.eng.parer.ws.versamento.dto.VersamentoExt;
import it.eng.parer.ws.versamento.dto.VersamentoExtAggAll;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.versamento.ejb.oracleBlb.WriteCompBlbOracle;
import it.eng.parer.ws.versamentoTpi.ejb.SalvataggioCompFS;
import it.eng.parer.ws.versamentoTpi.ejb.StatoCreaCartelle;
import it.eng.parer.ws.xml.versReq.CamiciaFascicoloType;
import it.eng.parer.ws.xml.versReq.ChiaveType;
import it.eng.parer.ws.xml.versReq.UnitaDocAggAllegati;
import it.eng.parer.ws.xml.versReq.UnitaDocumentaria;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import static it.eng.parer.util.DateUtilsConverter.convert;
import static it.eng.parer.util.DateUtilsConverter.format;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fioravanti_F
 */
@Stateless(mappedName = "SalvataggioSync")
@LocalBean
public class SalvataggioSync {

    private static final Logger log = LoggerFactory.getLogger(SalvataggioSync.class);
    //
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;
    //
    @EJB
    private SalvataggioCompFS salvataggioCompFS;
    //
    @EJB
    private WriteCompBlbOracle writeCompBlbOracle;
    @EJB
    private SalvataggioFirmaManager salvataggioFirmaManager;
    //
    private final static int DS_ERR_MAX_LEN = 1024;
    //
    private final static String LOCKNAME_CREA_DIRECTORY_VERS_TPI = "CREA_DIRECTORY_VERS_TPI";

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public boolean salvaDatiVersamento(VersamentoExt versamento, RispostaWS rispostaWS) {
        // nota l'errore critico di persistenza viene contrassegnato con la lettera P
        // per dare la possibilità all'eventuale chiamante di ripetere il tentativo
        // quando possibile (non è infatti un errore "definitivo" dato dall'input, ma
        // bensì
        // un errore interno provocato da problemi al database)
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        boolean prosegui = true;
        try {
            // prima di tutto verifico la necessità di costruire directory sul file system
            // e nel caso creo i primi 2 livelli
            if (!this.verificaCreaDirectoryFS(tmpRispostaControlli, versamento.getStrutturaComponenti())) {
                rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P, tmpRispostaControlli.getDsErr());
                prosegui = false;
            }

            // SALVO UNITA' DOCUMENTARIA
            if (prosegui) {
                if (!this.salvaUnitaDoc(versamento.getStrutturaComponenti(), versamento.getVersamento(), rispostaWS)) {
                    prosegui = false;
                }
            }

            // SALVO UNITA' DOCUMENTARIA DA INSERIRE NELL'ELENCO DI VERSAMENTO
            if (prosegui) {
                if (!this.salvaUdElencoVers(versamento.getStrutturaComponenti(), versamento.getVersamento())) {
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                            "Eccezione nella persistenza dell'unità documentaria da inserire nell'elenco di versamento");
                    prosegui = false;
                }
            }

            // SALVO IL PROFILO ARCHIVISTICO
            if (prosegui) {
                if (!this.salvaFascicoliSecondari(versamento.getStrutturaComponenti(), versamento.getVersamento())) {
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                            "Eccezione nella persistenza del Profilo Archivistico ");
                    prosegui = false;
                }
            }

            // SALVO I DOCUMENTI COLLEGATI
            if (prosegui) {
                if (!this.salvaDocumentiCollegati(versamento.getStrutturaComponenti(), versamento.getVersamento())) {
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                            "Eccezione nella persistenza dei Documenti Collegati ");
                    prosegui = false;
                }
            }

            // SALVO I Dati specifici UD
            if (prosegui) {
                if (!this.salvaDatiSpecUD(versamento.getStrutturaComponenti())) {
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                            "Eccezione nella persistenza dei Dati Specifici UD ");
                    prosegui = false;
                }
            }

            // SALVO I DOCUMENTI PRINCIPALE / ALLEGATI / ANNESSI / ANNOTAZIONI
            if (prosegui) {
                ConfigPerDoc tmpConfigPerDoc = new ConfigPerDoc();
                tmpConfigPerDoc.setForzaAccettazione(
                        rispostaWS.getIstanzaEsito().getConfigurazione().isForzaAccettazione() ? "1" : "0");
                tmpConfigPerDoc.setForzaConservazione(
                        versamento.getStrutturaComponenti().isConfigFlagForzaConservazione() ? "1" : "0");
                tmpConfigPerDoc
                        .setTipoConservazione(rispostaWS.getIstanzaEsito().getConfigurazione().getTipoConservazione());
                tmpConfigPerDoc.setSistemaDiMigrazione(
                        rispostaWS.getIstanzaEsito().getConfigurazione().getSistemaDiMigrazione());
                tmpConfigPerDoc.setTipoCreazioneDoc(TipoCreazioneDoc.VERSAMENTO_UNITA_DOC);

                if (!this.salvaDocumenti(versamento.getStrutturaComponenti(), tmpConfigPerDoc,
                        versamento.getVersamento().getIntestazione().getChiave(), rispostaWS)) {
                    prosegui = false;
                }
            }

            // SALVO I WARNING
            if (prosegui) {
                if (!this.salvaDatiWarning(versamento)) {
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                            "Eccezione nella persistenza dei warning");
                    prosegui = false;
                }
            }

            // salvo i componenti ed i sottocomponenti
            if (prosegui) {
                if (!this.salvaComponenti(tmpRispostaControlli, versamento.getStrutturaComponenti(),
                        versamento.getVersamento().getIntestazione().getChiave())) {
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                            "Eccezione nella persistenza dei componenti " + tmpRispostaControlli.getDsErr());
                    prosegui = false;
                }
            }

            // SALVO I Dati specifici Comp e Sub-Comp
            if (prosegui) {
                if (!this.salvaDatiSpecCompUD(versamento.getStrutturaComponenti())) {
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                            "Eccezione nella persistenza dei Dati Specifici Componenti ");
                }
            }

        } catch (Exception e) {
            rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase salvataggio metadati versamento: " + e.getMessage());
            log.error("Errore nella fase salvataggio metadati versamento.", e);
        } finally {
            entityManager.clear();
        }

        return prosegui;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public boolean salvaDatiVersamento(VersamentoExtAggAll versamento, RispostaWSAggAll rispostaWS) {
        // nota l'errore critico di persistenza viene contrassegnato con la lettera P
        // per dare la possibilità all'eventuale chiamante di ripetere il tentativo
        // quando possibile (non è infatti un errore "definitivo" dato dall'input, ma
        // bensì
        // un errore interno provocato da problemi al database)
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        boolean prosegui = true;
        try {
            // prima di tutto verifico la necessità di costruire directory sul file system
            // e nel caso creo i primi 2 livelli
            if (!this.verificaCreaDirectoryFS(tmpRispostaControlli, versamento.getStrutturaComponenti())) {
                rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P, tmpRispostaControlli.getDsErr());
                prosegui = false;
            }

            // Aggiorno UNITA' DOCUMENTARIA
            if (prosegui) {
                boolean result = this.aggiornaUnitaDoc(versamento.getStrutturaComponenti(), versamento.getVersamento(),
                        rispostaWS);
                if (!result) {
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                            "Eccezione nella persistenza dell'unità documentaria ");
                    prosegui = false;
                }
            }

            // Aggiorno DOCUMENTI (niOrdDoc e calcolo UrnComponente)
            if (prosegui) {
                boolean result = this.aggiornaDocumenti(versamento.getStrutturaComponenti(), versamento.getVersamento(),
                        rispostaWS);
                if (!result) {
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                            "Eccezione nella persistenza di aggiornamento URN dei documenti ");
                    prosegui = false;
                }
            }

            // Aggiorno PREGRESSO
            if (prosegui) {
                boolean result = this.aggiornaPregressoDocumenti(versamento.getStrutturaComponenti(),
                        versamento.getVersamento(), rispostaWS);
                if (!result) {
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                            "Eccezione nella persistenza sul pregresso dei documenti ");
                    prosegui = false;
                }
            }

            // SALVO IL DOCUMENTO ALLEGATO ANNESSO / ANNOTAZIONE
            if (prosegui) {
                ConfigPerDoc tmpConfigPerDoc = new ConfigPerDoc();
                tmpConfigPerDoc.setForzaAccettazione(
                        rispostaWS.getIstanzaEsito().getConfigurazione().isForzaAccettazione() ? "1" : "0");
                tmpConfigPerDoc.setForzaConservazione(
                        versamento.getStrutturaComponenti().isConfigFlagForzaConservazione() ? "1" : "0");
                tmpConfigPerDoc
                        .setTipoConservazione(rispostaWS.getIstanzaEsito().getConfigurazione().getTipoConservazione());
                tmpConfigPerDoc.setSistemaDiMigrazione(
                        rispostaWS.getIstanzaEsito().getConfigurazione().getSistemaDiMigrazione());
                tmpConfigPerDoc.setTipoCreazioneDoc(TipoCreazioneDoc.AGGIUNTA_DOCUMENTO);

                if (!this.salvaDocumenti(versamento.getStrutturaComponenti(), tmpConfigPerDoc,
                        versamento.getVersamento().getIntestazione().getChiave(), rispostaWS)) {
                    prosegui = false;
                }
            }

            // SALVO DOCUMENTO DA INSERIRE NELL'ELENCO DI VERSAMENTO
            if (prosegui) {
                if (!this.salvaDocElencoVers(versamento.getStrutturaComponenti(), versamento.getVersamento())) {
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                            "Eccezione nella persistenza del documento da inserire nell'elenco di versamento");
                    prosegui = false;
                }
            }

            // SALVO I WARNING
            if (prosegui) {
                if (!this.salvaDatiWarning(versamento)) {
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                            "Eccezione nella persistenza dei warning");
                    prosegui = false;
                }
            }

            // salvo i componenti ed i sottocomponenti
            if (prosegui) {
                if (!this.salvaComponenti(tmpRispostaControlli, versamento.getStrutturaComponenti(),
                        versamento.getVersamento().getIntestazione().getChiave())) {
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                            "Eccezione nella persistenza dei componenti " + tmpRispostaControlli.getDsErr());
                    prosegui = false;
                }
            }

            // SALVO I Dati specifici Comp e Sub-Comp
            if (prosegui) {
                if (!this.salvaDatiSpecCompUD(versamento.getStrutturaComponenti())) {
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                            "Eccezione nella persistenza dei Dati Specifici Componenti ");
                }
            }

            // SALVO I dati di aggregazioni
            if (prosegui) {
                if (!this.salvaDatiAggregazioni(versamento.getStrutturaComponenti())) {
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                            "Eccezione nella persistenza dei Dati di Aggregazioni ");
                }
            }

        } catch (Exception e) {
            rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase salvataggio metadati versamento: " + e.getMessage());
            log.error("Errore nella fase salvataggio metadati aggAll.", e);
        } finally {
            entityManager.clear();
        }
        return prosegui;
    }

    /*
     * ************************************************************************** salvataggio dati e metadati per
     * versamento completo **************************************************************************
     */
    private boolean salvaUnitaDoc(StrutturaVersamento strutV, UnitaDocumentaria ud, RispostaWS rispostaWS) {
        AroUnitaDoc tmpTabUD = new AroUnitaDoc();
        Calendar tmpCal;
        boolean tmpReturn = true;

        tmpTabUD.setOrgStrut(entityManager.find(OrgStrut.class, strutV.getIdStruttura()));
        tmpTabUD.setOrgSubStrut(entityManager.find(OrgSubStrut.class, strutV.getIdSubStruttura()));

        // informazioni di base
        // intestazione
        tmpTabUD.setCdRegistroKeyUnitaDoc(ud.getIntestazione().getChiave().getTipoRegistro());
        tmpTabUD.setAaKeyUnitaDoc(new BigDecimal(ud.getIntestazione().getChiave().getAnno()));
        tmpTabUD.setCdKeyUnitaDoc(ud.getIntestazione().getChiave().getNumero());

        // salvo il registro anche come FK, utile per le ricerche
        tmpTabUD.setDecRegistroUnitaDoc(entityManager.find(DecRegistroUnitaDoc.class, strutV.getIdRegistroUnitaDoc()));

        tmpCal = Calendar.getInstance();
        tmpCal.set(2444, 11, 31, 0, 0, 0);
        tmpTabUD.setDtAnnul(tmpCal.getTime());
        tmpTabUD.setTiAnnul(null);

        tmpTabUD.setDecTipoUnitaDoc(
                entityManager.find(DecTipoUnitaDoc.class, strutV.getIdTipologiaUnitaDocumentaria()));

        tmpTabUD.setDtCreazione(convert(strutV.getDataVersamento()));

        Integer numeroAllegati = ud.getNumeroAllegati() != null ? ud.getNumeroAllegati() : 0;
        Integer numeroAnnessi = ud.getNumeroAnnessi() != null ? ud.getNumeroAnnessi() : 0;
        Integer numeroAnnotazioni = ud.getNumeroAnnotazioni() != null ? ud.getNumeroAnnotazioni() : 0;
        tmpTabUD.setNiAlleg(new BigDecimal(numeroAllegati));
        tmpTabUD.setNiAnnessi(new BigDecimal(numeroAnnessi));
        tmpTabUD.setNiAnnot(new BigDecimal(numeroAnnotazioni));

        tmpTabUD.setIamUser(entityManager.find(IamUser.class, strutV.getIdUser()));

        // aggiunti da Paolo
        tmpTabUD.setFlUnitaDocFirmato(
                rispostaWS.getIstanzaEsito().getUnitaDocumentaria().isFirmatoDigitalmente() != null
                        && rispostaWS.getIstanzaEsito().getUnitaDocumentaria().isFirmatoDigitalmente().booleanValue()
                                ? "1" : "0");
        tmpTabUD.setDsMsgEsitoVerifFirme(strutV.getDsMsgEsitoVerifica());
        tmpTabUD.setTiEsitoVerifFirme(strutV.getTiEsitoVerifFirme());

        // salva la chiave di ordinamento calcolata
        tmpTabUD.setDsKeyOrd(strutV.getKeyOrdCalcolata());
        // salva il progressivo calcolato, estratto dal numero della chiave.. potrebbe
        // non esistere
        if (strutV.getProgressivoCalcolato() != null) {
            tmpTabUD.setPgUnitaDoc(new BigDecimal(strutV.getProgressivoCalcolato()));
        }

        // configurazione
        tmpTabUD.setFlForzaAccettazione(
                rispostaWS.getIstanzaEsito().getConfigurazione().isForzaAccettazione() ? "1" : "0");
        tmpTabUD.setFlForzaConservazione(strutV.isConfigFlagForzaConservazione() ? "1" : "0");
        tmpTabUD.setTiConservazione(rispostaWS.getIstanzaEsito().getConfigurazione().getTipoConservazione());
        tmpTabUD.setNmSistemaMigraz(rispostaWS.getIstanzaEsito().getConfigurazione().getSistemaDiMigrazione() != null
                ? rispostaWS.getIstanzaEsito().getConfigurazione().getSistemaDiMigrazione() : "");

        tmpTabUD.setFlForzaCollegamento(
                rispostaWS.getIstanzaEsito().getConfigurazione().isForzaCollegamento() ? "1" : "0");

        // profilo archivistico - principale (il fascicolo principale esiste sempre se
        // esiste il profilo archivistico)
        if (ud.getProfiloArchivistico() != null) {
            tmpTabUD.setDsClassifPrinc(ud.getProfiloArchivistico().getFascicoloPrincipale().getClassifica());
            if (ud.getProfiloArchivistico().getFascicoloPrincipale().getFascicolo() != null) {
                tmpTabUD.setCdFascicPrinc(
                        ud.getProfiloArchivistico().getFascicoloPrincipale().getFascicolo().getIdentificativo());
                String value = JAXBUtils.getStringValFromJAXBElement(
                        ud.getProfiloArchivistico().getFascicoloPrincipale().getFascicolo().getOggetto());
                tmpTabUD.setDsOggettoFascicPrinc(value);
            }
            if (ud.getProfiloArchivistico().getFascicoloPrincipale().getSottoFascicolo() != null) {
                tmpTabUD.setCdSottofascicPrinc(
                        ud.getProfiloArchivistico().getFascicoloPrincipale().getSottoFascicolo().getIdentificativo());
                String value = JAXBUtils.getStringValFromJAXBElement(
                        ud.getProfiloArchivistico().getFascicoloPrincipale().getSottoFascicolo().getOggetto());
                tmpTabUD.setDsOggettoSottofascicPrinc(value);
            }
        }

        // profilo unità documentaria
        tmpTabUD.setFlCartaceo("0");
        if (ud.getProfiloUnitaDocumentaria() != null) {
            Boolean cartaceo = ud.getProfiloUnitaDocumentaria().isCartaceo();
            tmpTabUD.setFlCartaceo((cartaceo != null && cartaceo) ? "1" : "0");
            tmpTabUD.setDlOggettoUnitaDoc(ud.getProfiloUnitaDocumentaria().getOggetto());
            if (ud.getProfiloUnitaDocumentaria().getData() != null) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date d;
                try {
                    d = format.parse(ud.getProfiloUnitaDocumentaria().getData());
                } catch (ParseException ex) {
                    tmpReturn = false;

                    log.info(
                            "QUESTO E' UN ERRORE PROVOCATO DA UN CLIENT SCRITTO MALE: SQLIntegrityConstraintViolationException ");

                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.UD_002_001, strutV.getUrnPartChiaveUd());
                    rispostaWS.getIstanzaEsito()
                            .setXMLVersamento("La sessione di versamento per l'UD da versare non è presente.");
                    return tmpReturn;
                }
                tmpTabUD.setDtRegUnitaDoc(d);
            }
        }

        // nel caso il salvataggio sia su filesystem/Tivoli,
        // lo stato del documento passerà ad IN_ATTESA_SCHED
        // solo dopo la effettiva memorizzazione in Tivoli
        if (strutV.getTipoSalvataggioFile() == CostantiDB.TipoSalvataggioFile.FILE && strutV.isTpiAbilitato()) {
            tmpTabUD.setTiStatoUdElencoVers(TipiStatoElementoVersato.IN_ATTESA_MEMORIZZAZIONE.name());
        } else {
            tmpTabUD.setTiStatoUdElencoVers(TipiStatoElementoVersato.IN_ATTESA_SCHED.name());
        }
        //
        tmpTabUD.setTiStatoConservazione(CostantiDB.StatoConservazioneUnitaDoc.PRESA_IN_CARICO.name());
        //
        tmpTabUD.setCdKeyUnitaDocNormaliz(strutV.getNumeroUdNormalized());
        try {
            entityManager.persist(tmpTabUD);
            entityManager.flush();
            /*
             * memorizza l'id dell'UD appena creata, mi servirà come FK nei salvataggi delle altre tabelle. Nota che nel
             * caso dell'aggiunta documento, questa informazione è caricata durante i controlli.
             */
            strutV.setIdUnitaDoc(tmpTabUD.getIdUnitaDoc());
        } catch (RuntimeException re) {
            tmpReturn = false;
            if (ExceptionUtils.indexOfThrowable(re, java.sql.SQLIntegrityConstraintViolationException.class) > -1) {
                log.info(
                        "QUESTO E' UN ERRORE PROVOCATO DA UN CLIENT SCRITTO MALE: SQLIntegrityConstraintViolationException ");
                rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.UD_002_001, strutV.getUrnPartChiaveUd());
                rispostaWS.setErroreElementoDoppio(true);
                rispostaWS.setIdElementoDoppio(0); // non serve a nulla,
                // ma visto che in ogni caso non esiste ancora l'ID
                // tanto vale impostare questo dato ad un valore non trovabile
                rispostaWS.getIstanzaEsito()
                        .setXMLVersamento("La sessione di versamento per l'UD da versare non è presente.");
                return tmpReturn;
            } else {
                /// logga l'errore e blocca tutto
                log.error("Eccezione nella persistenza dell'unità documentaria ", re);
            }
        }

        if (tmpReturn && strutV.isWarningFormatoNumero()) {
            try {
                tmpReturn = salvaWarningAARegistroUd(strutV, ud, rispostaWS);
            } catch (RuntimeException re) {
                tmpReturn = false;
                if (ExceptionUtils.indexOfThrowable(re, java.sql.SQLIntegrityConstraintViolationException.class) > -1) {
                    // se sono arrivato qui, vuol dire che un versamento in parallelo ha creato il
                    // record
                    // prima di me, provocando un errore di chiave duplicata.
                    log.info(
                            "QUESTO E' UN ERRORE PROVOCATO DA UN CLIENT SCRITTO MALE: SQLIntegrityConstraintViolationException ");
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                            "Impossibile salvare i dati di warning sul formato del numero, "
                                    + "probabilmente a causa di un versamento errato effettuato in parallelo");
                    return tmpReturn;
                } else {
                    /// logga l'errore e blocca tutto
                    log.error("Eccezione nella persistenza dell'unità documentaria ", re);
                }
            }
        }

        if (tmpReturn) {
            tmpReturn = this.aggiornaRegistriFiscaliUd(strutV, ud);
        }
        if (tmpReturn) {
            tmpReturn = this.riparaCollegamentiUdNonRisolti(strutV, ud);
        }
        if (tmpReturn) {
            tmpReturn = this.rimuoviUdNonVersate(strutV, ud);
        }

        if (!tmpReturn) {
            rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                    "Eccezione nella persistenza dell'unità documentaria ");
            rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
        }

        return tmpReturn;
    }

    // TODO !!!!!!
    private boolean salvaWarningAARegistroUd(StrutturaVersamento strutV, UnitaDocumentaria ud, RispostaWS rispostaWS) {
        // qui, in caso di eccezione, rendo un 666P con un messaggio che indica il
        // problema di
        // aggiornamento parallelo nella tabella.
        long numWarningAggiornati;
        String queryStr = "update DecWarnAaRegistroUd al "
                + "set al.flWarnAaRegistroUnitaDoc = :flWarnAaRegistroUnitaDocIn "
                + "where al.decAaRegistroUnitaDoc.idAaRegistroUnitaDoc = :idAaRegistroUnitaDocIn "
                + "and al.aaRegistroUnitaDoc = :aaRegistroUnitaDocIn ";

        // eseguo l'update dell'eventuale record relativo all'anno
        javax.persistence.Query query = entityManager.createQuery(queryStr);
        query.setParameter("flWarnAaRegistroUnitaDocIn", "1");
        query.setParameter("idAaRegistroUnitaDocIn", strutV.getConfigRegAnno().getIdAaRegistroUnitaDoc());
        query.setParameter("aaRegistroUnitaDocIn", new BigDecimal(ud.getIntestazione().getChiave().getAnno()));
        numWarningAggiornati = query.executeUpdate();
        // se non ho aggiornato alcun record, vuol dire che lo devo creare...
        if (numWarningAggiornati == 0) {
            DecWarnAaRegistroUd tmpDecWarnAaRegistroUd = new DecWarnAaRegistroUd();
            tmpDecWarnAaRegistroUd.setAaRegistroUnitaDoc(new BigDecimal(ud.getIntestazione().getChiave().getAnno()));
            tmpDecWarnAaRegistroUd.setDecAaRegistroUnitaDoc(entityManager.find(DecAaRegistroUnitaDoc.class,
                    strutV.getConfigRegAnno().getIdAaRegistroUnitaDoc()));
            tmpDecWarnAaRegistroUd.setFlWarnAaRegistroUnitaDoc("1");
            entityManager.persist(tmpDecWarnAaRegistroUd);
            entityManager.flush();
        }

        return true;
    }

    private boolean aggiornaRegistriFiscaliUd(StrutturaVersamento strutV, UnitaDocumentaria ud) {
        boolean tmpReturn = true;
        // aggiorno la tabella degli anni usati dai registri
        /*
         * questa tabella viene impiegata per la verifica di contiguità nella conservazione di documenti fiscali. Dal
         * momento che il database è partizionato per anno è necessario effetture le ricerche sulla tabella AroUnitaDoc
         * sapendo già su che anni operare per evitare crolli di performance. --- prima conto se il record nella tabella
         * registri è già presente. In questo modo evito di usare un lock esclusivo se non è necessario
         */
        Long numAaUdReg;
        String queryStr = "select count(ud) " + "from MonAaUnitaDocRegistro ud "
                + "where ud.decRegistroUnitaDoc.idRegistroUnitaDoc = :idRegistroUnitaDocIn "
                + "and ud.aaUnitaDocRegistro = :aaUnitaDocRegistroIn ";
        javax.persistence.Query query = entityManager.createQuery(queryStr);
        query.setParameter("idRegistroUnitaDocIn", strutV.getIdRegistroUnitaDoc());
        query.setParameter("aaUnitaDocRegistroIn", new BigDecimal(ud.getIntestazione().getChiave().getAnno()));
        numAaUdReg = (Long) query.getSingleResult();
        if (numAaUdReg == 0) {
            Map<String, Object> properties = new HashMap();
            properties.put("javax.persistence.lock.timeout", 25);

            /*
             * se il registro è da inserire, lock esclusivo sul registro (che funge da semaforo)
             */
            DecRegistroUnitaDoc tmpRegistroUnitaDoc = null;
            try {
                tmpRegistroUnitaDoc = entityManager.find(DecRegistroUnitaDoc.class, strutV.getIdRegistroUnitaDoc(),
                        LockModeType.PESSIMISTIC_WRITE, properties);
            } catch (Exception re) {
                /// logga l'errore e blocca tutto
                log.error("Eccezione nella persistenza dell'unità documentaria ", re);
                tmpReturn = false;
            }

            if (tmpReturn) {
                /*
                 * rieseguo il conteggio per sicurezza (potrei avere un errore di chiave duplicata in inserimento in
                 * alcune sfigatissime condizioni di concorrenza)
                 */
                queryStr = "select count(ud) " + "from MonAaUnitaDocRegistro ud "
                        + "where ud.decRegistroUnitaDoc.idRegistroUnitaDoc = :idRegistroUnitaDocIn "
                        + "and ud.aaUnitaDocRegistro = :aaUnitaDocRegistroIn ";
                query = entityManager.createQuery(queryStr);
                query.setParameter("idRegistroUnitaDocIn", strutV.getIdRegistroUnitaDoc());
                query.setParameter("aaUnitaDocRegistroIn", new BigDecimal(ud.getIntestazione().getChiave().getAnno()));
                numAaUdReg = (Long) query.getSingleResult();
                if (numAaUdReg == 0) {
                    MonAaUnitaDocRegistro tmpAaUnitaDocRegistro;
                    tmpAaUnitaDocRegistro = new MonAaUnitaDocRegistro();
                    tmpAaUnitaDocRegistro
                            .setAaUnitaDocRegistro(new BigDecimal(ud.getIntestazione().getChiave().getAnno()));
                    tmpAaUnitaDocRegistro.setDecRegistroUnitaDoc(
                            entityManager.find(DecRegistroUnitaDoc.class, strutV.getIdRegistroUnitaDoc()));

                    /*
                     * inserisco il registro. Sono sicuro che non è presente in tabella e che nessuno sta eseguendo la
                     * stessa operazione in contemporanea
                     */
                    try {
                        entityManager.persist(tmpAaUnitaDocRegistro);
                        entityManager.flush();
                    } catch (RuntimeException re) {
                        /// logga l'errore e blocca tutto
                        log.error("Eccezione nella persistenza della tabella MonAaUnitaDocRegistro ", re);
                        tmpReturn = false;
                    }
                }

                /*
                 * rilascio il lock. Da notare che questo in apparenza non funziona ed il lock viene rilasciato solo
                 * dopo il termine della transazione
                 */
                entityManager.lock(tmpRegistroUnitaDoc, LockModeType.NONE);
            }

        }

        return tmpReturn;
    }

    private boolean riparaCollegamentiUdNonRisolti(StrutturaVersamento strutV, UnitaDocumentaria ud) {
        boolean tmpReturn = true;
        /*
         * aggiusto gli eventuali collegamenti di unità documentarie non risolti e forzati durante il versamento. Cerco
         * tutti i collegamenti che puntano all'UD appena inserita (li cerco per la tupla che definisce la chiave) e in
         * ognuno di essi valorizzo il campo aroUnitaDocLink con il valore dell'UD appena creata.
         */
        long numCollAggiustati;
        String queryStr = "update AroLinkUnitaDoc al " + "set al.aroUnitaDocLink = :aroUnitaDocLinkIn "
                + "where al.aroUnitaDocLink is null " + "and al.idStrut = :idStrutIn "
                + "and al.aaKeyUnitaDocLink = :aaKeyUnitaDocLinkIn "
                + "and al.cdKeyUnitaDocLink = :cdKeyUnitaDocLinkIn "
                + "and al.cdRegistroKeyUnitaDocLink = :cdRegistroKeyUnitaDocLinkIn ";
        javax.persistence.Query query = entityManager.createQuery(queryStr);
        query.setParameter("aroUnitaDocLinkIn", entityManager.find(AroUnitaDoc.class, strutV.getIdUnitaDoc()));
        query.setParameter("idStrutIn", new BigDecimal(strutV.getIdStruttura()));
        query.setParameter("cdRegistroKeyUnitaDocLinkIn", ud.getIntestazione().getChiave().getTipoRegistro());
        query.setParameter("aaKeyUnitaDocLinkIn", new BigDecimal(ud.getIntestazione().getChiave().getAnno()));
        query.setParameter("cdKeyUnitaDocLinkIn", ud.getIntestazione().getChiave().getNumero());
        try {
            numCollAggiustati = query.executeUpdate();
            if (numCollAggiustati > 0) {
                log.debug(String.format("Sono stati connessi %s collegamenti forzati da precedenti versamenti",
                        numCollAggiustati));
            }
        } catch (RuntimeException re) {
            /// logga l'errore e blocca tutto
            log.error("Eccezione nell'aggiornamento della tabella AroLinkUnitaDoc ", re);
            tmpReturn = false;
        }

        return tmpReturn;
    }

    private boolean rimuoviUdNonVersate(StrutturaVersamento strutV, UnitaDocumentaria ud) {
        boolean tmpReturn = true;

        /*
         * rimuovo l'eventuale record della tabella delle UD non versate per errore che corrisponde a quella che si sta
         * versando (la cerco per la tupla che definisce la chiave)
         */
        long numUdNonVersRimosse;
        String queryStr = "delete from VrsUnitaDocNonVer al " + "where al.orgStrut.idStrut = :idStrutIn "
                + "and al.aaKeyUnitaDoc = :aaKeyUnitaDocIn " + "and al.cdKeyUnitaDoc = :cdKeyUnitaDocIn "
                + "and al.cdRegistroKeyUnitaDoc = :cdRegistroKeyUnitaDocIn ";
        javax.persistence.Query query = entityManager.createQuery(queryStr);
        query.setParameter("idStrutIn", new BigDecimal(strutV.getIdStruttura()));
        query.setParameter("cdRegistroKeyUnitaDocIn", ud.getIntestazione().getChiave().getTipoRegistro());
        query.setParameter("aaKeyUnitaDocIn", new BigDecimal(ud.getIntestazione().getChiave().getAnno()));
        query.setParameter("cdKeyUnitaDocIn", ud.getIntestazione().getChiave().getNumero());
        try {
            numUdNonVersRimosse = query.executeUpdate();
            if (numUdNonVersRimosse > 0) {
                log.debug(String.format("Sono stati rimossi %s tentativi falliti di versamento UD ",
                        numUdNonVersRimosse));
            }
        } catch (RuntimeException re) {
            /// logga l'errore e blocca tutto
            log.error("Eccezione nell'aggiornamento della tabella VrsUnitaDocNonVer ", re);
            tmpReturn = false;
        }

        return tmpReturn;
    }

    private boolean salvaUdElencoVers(StrutturaVersamento strutV, UnitaDocumentaria ud) {
        boolean tmpReturn = true;
        ElvUdVersDaElabElenco tmpTab = new ElvUdVersDaElabElenco();
        tmpTab.setAroUnitaDoc(entityManager.find(AroUnitaDoc.class, strutV.getIdUnitaDoc()));
        tmpTab.setAaKeyUnitaDoc(new BigDecimal(ud.getIntestazione().getChiave().getAnno()));
        tmpTab.setDtCreazione(convert(strutV.getDataVersamento()));
        tmpTab.setIdStrut(new BigDecimal(strutV.getIdStruttura()));
        // nel caso il salvataggio sia su filesystem/Tivoli,
        // lo stato dell'UD passerà ad IN_ATTESA_SCHED
        // solo dopo la effettiva memorizzazione in Tivoli
        if (strutV.getTipoSalvataggioFile() == CostantiDB.TipoSalvataggioFile.FILE && strutV.isTpiAbilitato()) {
            tmpTab.setTiStatoUdDaElab(TipiStatoElementoVersato.IN_ATTESA_MEMORIZZAZIONE.name());
        } else {
            tmpTab.setTiStatoUdDaElab(TipiStatoElementoVersato.IN_ATTESA_SCHED.name());
        }

        // inserisco su DB
        try {
            entityManager.persist(tmpTab);
            entityManager.flush();
            strutV.setTiStatoUdDaElab(tmpTab.getTiStatoUdDaElab());
        } catch (RuntimeException re) {
            /// logga l'errore e blocca tutto
            log.error("Eccezione nella persistenza del UD da inserire in elenco di versamento ", re);
            tmpReturn = false;
        }
        return tmpReturn;
    }

    private boolean salvaFascicoliSecondari(StrutturaVersamento strutV, UnitaDocumentaria ud) {
        boolean tmpReturn = true;
        AroArchivSec tmpTabAS;
        Map<List<String>, CamiciaFascicoloType> tmpFascicoliUnivoci;
        tmpFascicoliUnivoci = new HashMap<>();
        if (ud.getProfiloArchivistico() != null && ud.getProfiloArchivistico().getFascicoliSecondari() != null) {

            // preparo un array con i dati del fascicolo principale, per confrontarli
            // con quelli degli eventuali secondari ed eliminare i doppioni
            CamiciaFascicoloType tmpFp = ud.getProfiloArchivistico().getFascicoloPrincipale();
            String[] tmpArrP = new String[] { "", "", "", "", "" };
            if (tmpFp.getClassifica() != null) {
                tmpArrP[0] = tmpFp.getClassifica();
            }
            if (tmpFp.getFascicolo() != null && tmpFp.getFascicolo().getIdentificativo() != null) {
                tmpArrP[1] = tmpFp.getFascicolo().getIdentificativo();
            }
            if (tmpFp.getFascicolo() != null && tmpFp.getFascicolo().getOggetto() != null
                    && !tmpFp.getFascicolo().getOggetto().isNil()) {
                tmpArrP[2] = tmpFp.getFascicolo().getOggetto().getValue();
            }
            if (tmpFp.getSottoFascicolo() != null && tmpFp.getSottoFascicolo().getIdentificativo() != null) {
                tmpArrP[3] = tmpFp.getSottoFascicolo().getIdentificativo();
            }
            if (tmpFp.getSottoFascicolo() != null && tmpFp.getSottoFascicolo().getOggetto() != null
                    && !tmpFp.getSottoFascicolo().getOggetto().isNil()) {
                tmpArrP[4] = tmpFp.getSottoFascicolo().getOggetto().getValue();
            }
            List tmpListP = Arrays.asList(tmpArrP);

            // elimina i fascicoli/sottofascicoli inseriti due volte
            // il problema si presenta con alcuni versatori poco precisi.
            // non si vuole rendere un messaggio di errore, perciò i doppioni vengono
            // eliminati senza segnalazioni
            for (CamiciaFascicoloType tmpFs : ud.getProfiloArchivistico().getFascicoliSecondari()
                    .getFascicoloSecondario()) {
                String[] tmpArr = new String[] { "", "", "", "", "" };
                if (tmpFs.getClassifica() != null) {
                    tmpArr[0] = tmpFs.getClassifica();
                }
                if (tmpFs.getFascicolo() != null && tmpFs.getFascicolo().getIdentificativo() != null) {
                    tmpArr[1] = tmpFs.getFascicolo().getIdentificativo();
                }
                if (tmpFs.getFascicolo() != null && tmpFs.getFascicolo().getOggetto() != null
                        && !tmpFs.getFascicolo().getOggetto().isNil()) {
                    tmpArr[2] = tmpFs.getFascicolo().getOggetto().getValue();
                }
                if (tmpFs.getSottoFascicolo() != null && tmpFs.getSottoFascicolo().getIdentificativo() != null) {
                    tmpArr[3] = tmpFs.getSottoFascicolo().getIdentificativo();
                }
                if (tmpFs.getSottoFascicolo() != null && tmpFs.getSottoFascicolo().getOggetto() != null
                        && !tmpFs.getSottoFascicolo().getOggetto().isNil()) {
                    tmpArr[4] = tmpFs.getSottoFascicolo().getOggetto().getValue();
                }
                List tmpList = Arrays.asList(tmpArr);
                if (!tmpListP.equals(tmpList)) {
                    // se il fascicolo è uguale al principale, non lo considero
                    // in ogni caso inserisco il dato in un'hashmap, così i fascicoli duplicati
                    // vengono caricati una volta sola
                    tmpFascicoliUnivoci.put(tmpList, tmpFs);
                }
            }
        }

        // profilo archivistico - secondari
        for (CamiciaFascicoloType tmpFascicolo : tmpFascicoliUnivoci.values()) {
            tmpTabAS = new AroArchivSec();
            tmpTabAS.setAroUnitaDoc(entityManager.find(AroUnitaDoc.class, strutV.getIdUnitaDoc()));
            tmpTabAS.setIdStrut(new BigDecimal(strutV.getIdStruttura()));
            tmpTabAS.setDsClassif(tmpFascicolo.getClassifica());
            if (tmpFascicolo.getFascicolo() != null) {
                tmpTabAS.setCdFascic(tmpFascicolo.getFascicolo().getIdentificativo());
                String value = JAXBUtils.getStringValFromJAXBElement(tmpFascicolo.getFascicolo().getOggetto());
                tmpTabAS.setDsOggettoFascic(value);
            }
            if (tmpFascicolo.getSottoFascicolo() != null) {
                tmpTabAS.setCdSottofascic(tmpFascicolo.getSottoFascicolo().getIdentificativo());
                String value = JAXBUtils.getStringValFromJAXBElement(tmpFascicolo.getSottoFascicolo().getOggetto());
                tmpTabAS.setDsOggettoSottofascic(value);
            }

            // inserisco su DB
            try {
                entityManager.persist(tmpTabAS);
                entityManager.flush();
            } catch (RuntimeException re) {
                /// logga l'errore e blocca tutto
                log.error("Eccezione nella persistenza dei fascicoli secondari ", re);
                tmpReturn = false;
                break;
            }
        }

        return tmpReturn;
    }

    private boolean salvaDocumentiCollegati(StrutturaVersamento strutV, UnitaDocumentaria ud) {
        boolean tmpReturn = true;

        if (ud.getDocumentiCollegati() != null) {
            // Ciclo sul numero di Documenti Collegati
            for (UnitaDocColl tmpUnitaDocColl : strutV.getUnitaDocCollegate()) {
                // Creo l'entità per ogni documento collegato da inserire in ARO_LINK_UNITA_DOC
                AroLinkUnitaDoc tmpTabLUD = new AroLinkUnitaDoc();

                tmpTabLUD.setAroUnitaDoc(entityManager.find(AroUnitaDoc.class, strutV.getIdUnitaDoc()));
                tmpTabLUD.setIdStrut(new BigDecimal(strutV.getIdStruttura()));
                tmpTabLUD.setCdRegistroKeyUnitaDocLink(tmpUnitaDocColl.getChiave().getTipoRegistro());
                tmpTabLUD.setAaKeyUnitaDocLink(new BigDecimal(tmpUnitaDocColl.getChiave().getAnno()));
                tmpTabLUD.setCdKeyUnitaDocLink(tmpUnitaDocColl.getChiave().getNumero());

                // Se il Documento Collegato non è presente, non ha chiave.
                // In quel caso devo "forzare il collegamento" e non inserisco il riferimento
                if (tmpUnitaDocColl.getIdUnitaDocLink() != -1) {
                    tmpTabLUD.setAroUnitaDocLink(
                            entityManager.find(AroUnitaDoc.class, tmpUnitaDocColl.getIdUnitaDocLink()));
                }
                // salva la descrizione del collegamento: è la somma delle descrizioni uniche
                // di tutti i riferimenti alla UD oggetto del collegamento.
                tmpTabLUD.setDsLinkUnitaDoc(tmpUnitaDocColl.generaDescrizione());

                // inserisco su DB
                try {
                    entityManager.persist(tmpTabLUD);
                    entityManager.flush();
                } catch (RuntimeException re) {
                    /// logga l'errore e blocca tutto
                    log.error("Eccezione nella persistenza della struttura del documento ", re);
                    tmpReturn = false;
                    break;
                }
            }
        }

        return tmpReturn;
    }

    private boolean salvaDatiSpecUD(StrutturaVersamento strutturaVersIn) {
        boolean tmpReturn = true;

        tmpReturn = this.salvaDatiSpecGen(CostantiDB.TipiUsoDatiSpec.VERS, TipiEntitaSacer.UNI_DOC,
                strutturaVersIn.getIdUnitaDoc(), strutturaVersIn.getIdStruttura(),
                strutturaVersIn.getIdRecXsdDatiSpec(), strutturaVersIn.getDatiSpecifici(),
                strutturaVersIn.getIdUnitaDoc());

        if (tmpReturn) {
            tmpReturn = this.salvaDatiSpecGen(CostantiDB.TipiUsoDatiSpec.MIGRAZ, TipiEntitaSacer.UNI_DOC,
                    strutturaVersIn.getIdUnitaDoc(), strutturaVersIn.getIdStruttura(),
                    strutturaVersIn.getIdRecXsdDatiSpecMigrazione(), strutturaVersIn.getDatiSpecificiMigrazione(),
                    strutturaVersIn.getIdUnitaDoc());
        }

        return tmpReturn;
    }

    /*
     * ************************************************************************** salvataggio dati e metadati per
     * aggiunta documenti **************************************************************************
     */
    private boolean aggiornaUnitaDoc(StrutturaVersamento strutV, UnitaDocAggAllegati ud, RispostaWSAggAll rispostaWS) {
        AroUnitaDoc tmpTabUD = null;
        boolean tmpReturn = true;
        int progressivoVersione = 0;

        try {
            // se sono già stati creati degli AIP, pongo lo stato conservazione ad AIP IN
            // AGGIORNAMENTO
            progressivoVersione = this.recuperaProgressivoVersione(strutV.getIdUnitaDoc());
            //
            tmpTabUD = entityManager.find(AroUnitaDoc.class, strutV.getIdUnitaDoc(), LockModeType.PESSIMISTIC_WRITE);
        } catch (Exception re) {
            /// logga l'errore e blocca tutto
            log.error("Eccezione nella persistenza dell'unità documentaria ", re);
            tmpReturn = false;
        }
        if (tmpReturn) {
            // prima di tutto aggiungo un elemento
            if (strutV.getDocumentiAttesi().get(0).getCategoriaDoc() == CategoriaDocumento.Allegato) {
                tmpTabUD.setNiAlleg(tmpTabUD.getNiAlleg().add(new BigDecimal(1)));
            }
            if (strutV.getDocumentiAttesi().get(0).getCategoriaDoc() == CategoriaDocumento.Annesso) {
                tmpTabUD.setNiAnnessi(tmpTabUD.getNiAnnessi().add(new BigDecimal(1)));
            }
            if (strutV.getDocumentiAttesi().get(0).getCategoriaDoc() == CategoriaDocumento.Annotazione) {
                tmpTabUD.setNiAnnot(tmpTabUD.getNiAnnot().add(new BigDecimal(1)));
            }

            // se sono già stati creati degli AIP, pongo lo stato conservazione ad AIP IN
            // AGGIORNAMENTO
            if (progressivoVersione > 0
            // MAC#26281
            // /* MAC#22948 */
            // || tmpTabUD.getTiStatoConservazione()
            // .equals(CostantiDB.StatoConservazioneUnitaDoc.IN_VOLUME_DI_CONSERVAZIONE.name())
            // /* end MAC#22948 */
            // end MAC#26281
            ) {
                tmpTabUD.setTiStatoConservazione(CostantiDB.StatoConservazioneUnitaDoc.AIP_IN_AGGIORNAMENTO.name());
            }

            // se il documento appena aggiunto è firmato, aggiorno il flag dell'unitaDoc
            Boolean firmato = rispostaWS.getIstanzaEsito().getUnitaDocumentaria().isFirmatoDigitalmente();
            if (firmato != null && firmato) {
                tmpTabUD.setFlUnitaDocFirmato("1");

                /*
                 * se l'unità doc presente non ha un risultato di verifca firme, aggiorno con quello appena calcolato
                 */
                if (tmpTabUD.getTiEsitoVerifFirme() == null || tmpTabUD.getTiEsitoVerifFirme().equals("")) {
                    tmpTabUD.setTiEsitoVerifFirme(strutV.getTiEsitoVerifFirme());
                    tmpTabUD.setDsMsgEsitoVerifFirme(strutV.getDsMsgEsitoVerifica());
                }

                /*
                 * se il doc appena aggiunto è in errore, aggiorno stato e messaggio nell'unità doc. già presente in
                 * ogni caso con il valore NEGATIVO.
                 *
                 */
                if (strutV.getTiEsitoVerifFirme().equals(VerificaFirmaEnums.EsitoControllo.NEGATIVO.name())) {
                    tmpTabUD.setTiEsitoVerifFirme(strutV.getTiEsitoVerifFirme());
                    tmpTabUD.setDsMsgEsitoVerifFirme(strutV.getDsMsgEsitoVerifica());
                }

                /*
                 * se il doc appena aggiunto è in warning, mentre l'unità doc già presente è POSITIVO, aggiorna l'unità
                 * doc a WARNING
                 */
                if (strutV.getTiEsitoVerifFirme().equals(VerificaFirmaEnums.EsitoControllo.WARNING.name())
                        && tmpTabUD.getTiEsitoVerifFirme().equals(VerificaFirmaEnums.EsitoControllo.POSITIVO.name())) {
                    tmpTabUD.setTiEsitoVerifFirme(strutV.getTiEsitoVerifFirme());
                    tmpTabUD.setDsMsgEsitoVerifFirme(strutV.getDsMsgEsitoVerifica());
                }
                /*
                 * in tutti gli altri casi, non aggiorno l'unità doc, dal momento cha ha già un risultato di verifica
                 * firme uguale o peggiore di quello che si è appena calcolato
                 */
            }
            // set chiave normalizzata
            tmpTabUD.setCdKeyUnitaDocNormaliz(strutV.getNumeroUdNormalized());
        }
        // aggiorno la tabella nel db
        if (tmpReturn) {
            try {
                entityManager.flush();
            } catch (RuntimeException re) {
                /// logga l'errore e blocca tutto
                log.error("Eccezione nella persistenza dell'unità documentaria ", re);
                tmpReturn = false;
            }
        }

        return tmpReturn;
    }

    private boolean aggiornaDocumenti(StrutturaVersamento strutV, UnitaDocAggAllegati versamento,
            RispostaWSAggAll rispostaWS) {
        boolean tmpReturn = true;

        try {
            //
            AroUnitaDoc tmpTabUD = entityManager.find(AroUnitaDoc.class, strutV.getIdUnitaDoc());
            // calcolo (se necessario) niOrdDoc secondo la logica di ordinamento
            this.getAndSetAroDocOrderedByTypeAndDateProg(tmpTabUD);
            // per ogni documento
            for (AroDoc tmpAroDoc : tmpTabUD.getAroDocs()) {
                // per ogni componente
                for (Iterator<AroStrutDoc> it = tmpAroDoc.getAroStrutDocs().iterator(); it.hasNext();) {
                    AroStrutDoc tmpAroStrutDoc = it.next();
                    for (AroCompDoc tmpAroCompDoc : tmpAroStrutDoc.getAroCompDocs()) {
                        this.salvaURNComponente(strutV, tmpAroDoc, tmpAroCompDoc,
                                Arrays.asList(TiUrn.ORIGINALE, TiUrn.NORMALIZZATO, TiUrn.INIZIALE));
                    }
                }
            }
            // aggiorno la tabella nel db

            entityManager.flush();
        } catch (RuntimeException re) {
            /// logga l'errore e blocca tutto
            log.error("Eccezione nella persistenza aggiornamento URN dei documenti ", re);
            tmpReturn = false;
        }

        return tmpReturn;
    }

    /*
     * Data una UD torna la lista dei suoi AroDoc ordinati per: - Tipo documento principale e poi tutti gli altri -
     * Tutti gli altri ordinati per Data Creazione e nella stessa data per tipo documento e nello stesso tipo per
     * progressivo
     */
    private void getAndSetAroDocOrderedByTypeAndDateProg(AroUnitaDoc aroUnitaDoc) {
        BigDecimal prog = BigDecimal.ONE;
        // recupero documenti
        List<AroDoc> listaDoc = aroUnitaDoc.getAroDocs();
        ArrayList<AroDoc> alDef = null;
        if (listaDoc != null) {

            AroDoc aroDocPrinc = null;
            ArrayList<AroDoc> alNew = new ArrayList<>();
            for (AroDoc aroDoc : listaDoc) {
                if (aroDoc.getTiDoc().equals(CategoriaDocumento.Principale.getValoreDb())) {
                    aroDocPrinc = aroDoc; // memorizza per dopo il doc PRINCIPALE
                } else {
                    alNew.add(aroDoc);
                }
            }
            // Ordina gli elementi tranne il PRINCIPALE...
            Collections.sort(alNew, new Comparator<AroDoc>() {
                @Override
                public int compare(AroDoc doc1, AroDoc doc2) {
                    int comparazionePerData = doc1.getDtCreazione().compareTo(doc2.getDtCreazione());
                    if (comparazionePerData == 0) {
                        int comparazionePerTipo = doc1.getTiDoc().compareTo(doc2.getTiDoc());
                        if (comparazionePerTipo == 0) {
                            return doc1.getPgDoc().compareTo(doc2.getPgDoc());
                        } else {
                            return comparazionePerTipo;
                        }
                    } else {
                        return comparazionePerData;
                    }
                }
            });
            // PRINCIPALE FIRST
            alDef = new ArrayList<>();
            if (aroDocPrinc != null) {
                alDef.add(aroDocPrinc);
            }
            for (AroDoc aroDocZ : alNew) {
                alDef.add(aroDocZ);
            }
            // E poi tutti gli altri già ordinati di seguito
            for (AroDoc aroDocx : alDef) {
                // assegno solo se non presente
                if (aroDocx.getNiOrdDoc() == null) {
                    aroDocx.setNiOrdDoc(prog);
                }
                // incremento
                prog = prog.add(BigDecimal.ONE);
            }

        }
    }

    private void salvaURNComponente(StrutturaVersamento strutV, AroDoc tmpAroDoc, AroCompDoc tmpAroCompDoc,
            List<TiUrn> tiUrnToCalculate) {
        // for each tiUrn
        for (TiUrn tmpTiUrn : tiUrnToCalculate) {
            String tmpUrnDoc = null;
            // find with that TiUrn
            long count = new ArrayList<AroCompUrnCalc>(tmpAroCompDoc.getAroAroCompUrnCalcs()).stream()
                    .filter(c -> c.getTiUrn().equals(tmpTiUrn)).count();
            // se non esiste
            if (count == 0) {
                // DOCXXXXXX
                String tmpUrnPartDoc = MessaggiWSFormat.formattaUrnPartDocumento(CategoriaDocumento.Documento,
                        tmpAroDoc.getNiOrdDoc().intValue(), true, Costanti.UrnFormatter.DOC_FMT_STRING_V2,
                        Costanti.UrnFormatter.PAD5DIGITS_FMT);
                if (tmpAroCompDoc.getAroCompDoc() != null) {
                    // E' UN SOTTOCOMPONENTE
                    // DOCXXXXX:NNNNN
                    tmpUrnDoc = MessaggiWSFormat.formattaUrnPartComponente(tmpUrnPartDoc,
                            tmpAroCompDoc.getAroCompDoc().getNiOrdCompDoc().intValue(),
                            Costanti.UrnFormatter.COMP_FMT_STRING_V2, Costanti.UrnFormatter.PAD5DIGITS_FMT);
                    // DOCXXXXX:NNNNN:KK
                    tmpUrnDoc = MessaggiWSFormat.formattaUrnPartComponente(tmpUrnDoc,
                            tmpAroCompDoc.getNiOrdCompDoc().intValue(), Costanti.UrnFormatter.COMP_FMT_STRING_V2,
                            Costanti.UrnFormatter.PAD5DIGITS_FMT);
                } else {
                    // DOCXXXXX:NNNNN
                    tmpUrnDoc = MessaggiWSFormat.formattaUrnPartComponente(tmpUrnPartDoc,
                            tmpAroCompDoc.getNiOrdCompDoc().intValue(), Costanti.UrnFormatter.COMP_FMT_STRING_V2,
                            Costanti.UrnFormatter.PAD5DIGITS_FMT);
                }
                // calculate urn
                String tmpUrn = null;
                switch (tmpTiUrn) {
                case ORIGINALE:
                    tmpUrn = MessaggiWSFormat.formattaBaseUrnDoc(strutV.getUrnPartVersatore(),
                            strutV.getUrnPartChiaveUd(), tmpUrnDoc, Costanti.UrnFormatter.URN_COMP_FMT_STRING);
                    break;
                case NORMALIZZATO:
                    tmpUrn = MessaggiWSFormat.formattaBaseUrnDoc(strutV.getUrnPartVersatoreNormalized(),
                            strutV.getUrnPartChiaveUdNormalized(), tmpUrnDoc,
                            Costanti.UrnFormatter.URN_COMP_FMT_STRING);
                    break;
                default:
                    tmpUrn = tmpAroCompDoc.getDsUrnCompCalc();
                    break;
                }

                if (StringUtils.isNotBlank(tmpUrn)) {
                    this.salvaCompUrnCalc(tmpAroCompDoc, tmpUrn, tmpTiUrn);
                }

            }
        }
    }

    private boolean aggiornaPregressoDocumenti(StrutturaVersamento strutV, UnitaDocAggAllegati versamento,
            RispostaWSAggAll rispostaWS) {
        boolean tmpReturn = true;
        // A. check data massima versamento recuperata in precedenza rispetto parametro
        // su db
        if (strutV.getDtVersMax().before(strutV.getDtInizioCalcoloNewUrn())
                || strutV.getDtVersMax().equals(strutV.getDtInizioCalcoloNewUrn())) {
            try {
                AroUnitaDoc tmpTabUD = entityManager.find(AroUnitaDoc.class, strutV.getIdUnitaDoc());

                // eseguo registra urn sip pregressi ud
                this.scriviUrnSipUdPreg(tmpTabUD, strutV);
                // eseguo registra urn sip pregressi documenti aggiunti
                this.scriviUrnSipDocAggPreg(tmpTabUD, strutV);
                // eseguo registra urn pregressi upd
                this.scriviUrnSipUpdPreg(tmpTabUD, strutV);

                // aggiorno la tabella nel db
                entityManager.flush();
            } catch (RuntimeException re) {
                /// logga l'errore e blocca tutto
                log.error("Eccezione nella persistenza URN pregresso dei documenti ", re);
                tmpReturn = false;
            }

        }

        return tmpReturn;
    }

    private boolean scriviUrnSipUdPreg(AroUnitaDoc tmpTabUD, StrutturaVersamento strutV) {
        boolean tmpReturn = true;

        List<VrsVLisXmlUdUrnDaCalc> vrsVLisXmlUdUrnDaCalcs = this
                .retrieveVrsVLisXmlUdUrnDaCalcByUd(tmpTabUD.getIdUnitaDoc());
        for (VrsVLisXmlUdUrnDaCalc vrs : vrsVLisXmlUdUrnDaCalcs) {

            VrsXmlDatiSessioneVers xmlDatiSessioneVers = entityManager.find(VrsXmlDatiSessioneVers.class,
                    vrs.getIdXmlDatiSessioneVers().longValue());

            // calcolo parte urn ORIGINALE
            String tmpUrn = MessaggiWSFormat.formattaBaseUrnUnitaDoc(strutV.getUrnPartVersatore(),
                    strutV.getUrnPartChiaveUd());
            // calcolo parte urn NORMALIZZATO
            String tmpUrnNorm = MessaggiWSFormat.formattaBaseUrnUnitaDoc(strutV.getUrnPartVersatoreNormalized(),
                    strutV.getUrnPartChiaveUdNormalized());
            switch (vrs.getTiXmlDati()) {
            case TipiXmlDati.RICHIESTA:
                // salvo ORIGINALE
                this.salvaUrnXmlSessioneVers(xmlDatiSessioneVers,
                        MessaggiWSFormat.formattaUrnIndiceSip(tmpUrn, Costanti.UrnFormatter.URN_INDICE_SIP_V2),
                        TiUrnXmlSessioneVers.ORIGINALE);

                // salvo NORMALIZZATO
                this.salvaUrnXmlSessioneVers(xmlDatiSessioneVers,
                        MessaggiWSFormat.formattaUrnIndiceSip(tmpUrnNorm, Costanti.UrnFormatter.URN_INDICE_SIP_V2),
                        TiUrnXmlSessioneVers.NORMALIZZATO);
                break;
            case TipiXmlDati.RISPOSTA:
                // salvo ORIGINALE
                this.salvaUrnXmlSessioneVers(xmlDatiSessioneVers,
                        MessaggiWSFormat.formattaUrnEsitoVers(tmpUrn, Costanti.UrnFormatter.URN_ESITO_VERS_V2),
                        TiUrnXmlSessioneVers.ORIGINALE);

                // salvo NORMALIZZATO
                this.salvaUrnXmlSessioneVers(xmlDatiSessioneVers,
                        MessaggiWSFormat.formattaUrnEsitoVers(tmpUrnNorm, Costanti.UrnFormatter.URN_ESITO_VERS_V2),
                        TiUrnXmlSessioneVers.NORMALIZZATO);
                break;
            case TipiXmlDati.RAPP_VERS:
                // salvo ORIGINALE
                this.salvaUrnXmlSessioneVers(xmlDatiSessioneVers,
                        MessaggiWSFormat.formattaUrnRappVers(tmpUrn, Costanti.UrnFormatter.URN_RAPP_VERS_V2),
                        TiUrnXmlSessioneVers.ORIGINALE);
                // salvo NORMALIZZATO
                this.salvaUrnXmlSessioneVers(xmlDatiSessioneVers,
                        MessaggiWSFormat.formattaUrnRappVers(tmpUrnNorm, Costanti.UrnFormatter.URN_RAPP_VERS_V2),
                        TiUrnXmlSessioneVers.NORMALIZZATO);
                break;
            case TipiXmlDati.INDICE_FILE:
                // salvo ORIGINALE
                this.salvaUrnXmlSessioneVers(xmlDatiSessioneVers,
                        MessaggiWSFormat.formattaUrnPiSip(tmpUrn, Costanti.UrnFormatter.URN_PI_SIP_V2),
                        TiUrnXmlSessioneVers.ORIGINALE);

                // salvo NORMALIZZATO
                this.salvaUrnXmlSessioneVers(xmlDatiSessioneVers,
                        MessaggiWSFormat.formattaUrnPiSip(tmpUrnNorm, Costanti.UrnFormatter.URN_PI_SIP_V2),
                        TiUrnXmlSessioneVers.NORMALIZZATO);
                break;
            default:
                break;
            }

            // salvo INIZIALE
            String tmpUrnIni = StringUtils.isNotBlank(xmlDatiSessioneVers.getDsUrnXmlVers())
                    ? xmlDatiSessioneVers.getDsUrnXmlVers() : "Non disponibile";
            this.salvaUrnXmlSessioneVers(xmlDatiSessioneVers, tmpUrnIni, TiUrnXmlSessioneVers.INIZIALE);

        }

        return tmpReturn;

    }

    private List<VrsVLisXmlUdUrnDaCalc> retrieveVrsVLisXmlUdUrnDaCalcByUd(long idUnitaDoc) {
        Query query = entityManager
                .createQuery("SELECT vrs FROM VrsVLisXmlUdUrnDaCalc vrs WHERE vrs.idUnitaDoc = :idUnitaDoc ");
        query.setParameter("idUnitaDoc", idUnitaDoc);
        List<VrsVLisXmlUdUrnDaCalc> vrs = query.getResultList();
        return vrs;
    }

    private boolean salvaUrnXmlSessioneVers(VrsXmlDatiSessioneVers xmlDatiSessioneVers, String tmpUrn,
            TiUrnXmlSessioneVers tiUrn) {

        VrsUrnXmlSessioneVers tmpVrsUrnXmlSessioneVers = null;

        tmpVrsUrnXmlSessioneVers = new VrsUrnXmlSessioneVers();
        tmpVrsUrnXmlSessioneVers.setDsUrn(tmpUrn);
        tmpVrsUrnXmlSessioneVers.setTiUrn(tiUrn);
        tmpVrsUrnXmlSessioneVers.setVrsXmlDatiSessioneVers(xmlDatiSessioneVers);

        // persist
        entityManager.persist(tmpVrsUrnXmlSessioneVers);

        xmlDatiSessioneVers.getVrsUrnXmlSessioneVers().add(tmpVrsUrnXmlSessioneVers);

        return true;
    }

    private void scriviUrnSipDocAggPreg(AroUnitaDoc tmpAroUnitaDoc, StrutturaVersamento svf) {
        // per ogni documento aggiunto
        for (AroDoc aroDoc : tmpAroUnitaDoc.getAroDocs()) {
            List<VrsVLisXmlDocUrnDaCalc> vrsVLisXmlDocUrnDaCalc = this
                    .retrieveVrsVLisXmlDocUrnDaCalcByDoc(aroDoc.getIdDoc());
            // per ogni vrsVLisXmlDocUrnDaCalc
            for (VrsVLisXmlDocUrnDaCalc vrs : vrsVLisXmlDocUrnDaCalc) {
                //
                VrsXmlDatiSessioneVers xmlDatiSessioneVers = entityManager.find(VrsXmlDatiSessioneVers.class,
                        vrs.getIdXmlDatiSessioneVers().longValue());

                // calcolo parte urn ORIGINALE
                // DOCXXXXXX
                String tmpUrnPartDoc = MessaggiWSFormat.formattaUrnPartDocumento(CategoriaDocumento.Documento,
                        aroDoc.getNiOrdDoc().intValue(), true, Costanti.UrnFormatter.DOC_FMT_STRING_V2,
                        Costanti.UrnFormatter.PAD5DIGITS_FMT);

                String tmpUrn = MessaggiWSFormat.formattaBaseUrnDoc(svf.getUrnPartVersatore(), svf.getUrnPartChiaveUd(),
                        tmpUrnPartDoc, Costanti.UrnFormatter.URN_DOC_FMT_STRING_V2);

                // calcolo urn NORMALIZZATO
                String tmpUrnNorm = MessaggiWSFormat.formattaBaseUrnDoc(svf.getUrnPartVersatoreNormalized(),
                        svf.getUrnPartChiaveUdNormalized(), tmpUrnPartDoc, Costanti.UrnFormatter.URN_DOC_FMT_STRING_V2);
                switch (vrs.getTiXmlDati()) {
                case TipiXmlDati.RICHIESTA:
                    // salvo ORIGINALE
                    this.salvaUrnXmlSessioneVers(xmlDatiSessioneVers,
                            MessaggiWSFormat.formattaUrnIndiceSip(tmpUrn, Costanti.UrnFormatter.URN_INDICE_SIP_V2),
                            TiUrnXmlSessioneVers.ORIGINALE);

                    // salvo NORMALIZZATO
                    this.salvaUrnXmlSessioneVers(xmlDatiSessioneVers,
                            MessaggiWSFormat.formattaUrnIndiceSip(tmpUrnNorm, Costanti.UrnFormatter.URN_INDICE_SIP_V2),
                            TiUrnXmlSessioneVers.NORMALIZZATO);

                    break;
                case TipiXmlDati.RISPOSTA:
                    // salvo ORIGINALE
                    this.salvaUrnXmlSessioneVers(xmlDatiSessioneVers,
                            MessaggiWSFormat.formattaUrnEsitoVers(tmpUrn, Costanti.UrnFormatter.URN_ESITO_VERS_V2),
                            TiUrnXmlSessioneVers.ORIGINALE);

                    // salvo NORMALIZZATO
                    this.salvaUrnXmlSessioneVers(xmlDatiSessioneVers,
                            MessaggiWSFormat.formattaUrnEsitoVers(tmpUrnNorm, Costanti.UrnFormatter.URN_ESITO_VERS_V2),
                            TiUrnXmlSessioneVers.NORMALIZZATO);
                    break;
                case TipiXmlDati.RAPP_VERS:
                    // salvo ORIGINALE
                    this.salvaUrnXmlSessioneVers(xmlDatiSessioneVers,
                            MessaggiWSFormat.formattaUrnRappVers(tmpUrn, Costanti.UrnFormatter.URN_RAPP_VERS_V2),
                            TiUrnXmlSessioneVers.ORIGINALE);

                    // salvo NORMALIZZATO
                    this.salvaUrnXmlSessioneVers(xmlDatiSessioneVers,
                            MessaggiWSFormat.formattaUrnRappVers(tmpUrnNorm, Costanti.UrnFormatter.URN_RAPP_VERS_V2),
                            TiUrnXmlSessioneVers.NORMALIZZATO);
                    break;
                default:
                    break;
                }

                // salvo INIZIALE
                String tmpUrnIni = StringUtils.isNotBlank(xmlDatiSessioneVers.getDsUrnXmlVers())
                        ? xmlDatiSessioneVers.getDsUrnXmlVers() : "Non disponibile";
                this.salvaUrnXmlSessioneVers(xmlDatiSessioneVers, tmpUrnIni, TiUrnXmlSessioneVers.INIZIALE);

            }
        }
    }

    private List<VrsVLisXmlDocUrnDaCalc> retrieveVrsVLisXmlDocUrnDaCalcByDoc(long idDoc) {
        Query query = entityManager.createQuery("SELECT vrs FROM VrsVLisXmlDocUrnDaCalc vrs WHERE vrs.idDoc = :idDoc ");
        query.setParameter("idDoc", idDoc);
        List<VrsVLisXmlDocUrnDaCalc> vrs = query.getResultList();
        return vrs;
    }

    private void scriviUrnSipUpdPreg(AroUnitaDoc tmpAroUnitaDoc, StrutturaVersamento svf) {
        //
        List<AroUpdUnitaDoc> aroUpdUnitaDocs = this.retrieveAroUpdUnitaDocByUd(tmpAroUnitaDoc.getIdUnitaDoc());
        // per ogni aggiornamento metadati
        for (AroUpdUnitaDoc updUnitaDoc : aroUpdUnitaDocs) {
            //
            List<VrsVLisXmlUpdUrnDaCalc> vrsVLisXmlUpdUrnDaCalc = this
                    .retrieveVrsVLisXmlUpdUrnDaCalcByUpd(updUnitaDoc.getIdUpdUnitaDoc());
            // per ogni VrsVLisXmlUpdUrnDaCalc
            for (VrsVLisXmlUpdUrnDaCalc vrs : vrsVLisXmlUpdUrnDaCalc) {
                AroXmlUpdUnitaDoc aroXmlUpdUnitaDoc = entityManager.find(AroXmlUpdUnitaDoc.class,
                        vrs.getIdXmlUpdUnitaDoc().longValue());

                // calcolo parte urn ORIGINALE
                String tmpUrn = MessaggiWSFormat.formattaBaseUrnUpdUnitaDoc(svf.getUrnPartVersatore(),
                        svf.getUrnPartChiaveUd(), updUnitaDoc.getPgUpdUnitaDoc().longValue(), true,
                        Costanti.UrnFormatter.UPD_FMT_STRING_V3, Costanti.UrnFormatter.PAD5DIGITS_FMT);

                // calcolo parte urn NORMALIZZATO
                String tmpUrnNorm = MessaggiWSFormat.formattaBaseUrnUpdUnitaDoc(svf.getUrnPartVersatoreNormalized(),
                        svf.getUrnPartChiaveUdNormalized(), updUnitaDoc.getPgUpdUnitaDoc().longValue(), true,
                        Costanti.UrnFormatter.UPD_FMT_STRING_V3, Costanti.UrnFormatter.PAD5DIGITS_FMT);

                switch (vrs.getTiXmlUpdUnitaDoc()) {
                case TipiXmlDati.RICHIESTA:
                    // salvo ORIGINALE
                    aroXmlUpdUnitaDoc.setDsUrnXml(
                            MessaggiWSFormat.formattaUrnIndiceSipUpd(tmpUrn, Costanti.UrnFormatter.URN_INDICE_SIP_V2));
                    // salvo NORMALIZZATO
                    aroXmlUpdUnitaDoc.setDsUrnNormalizXml(MessaggiWSFormat.formattaUrnIndiceSipUpd(tmpUrnNorm,
                            Costanti.UrnFormatter.URN_INDICE_SIP_V2));
                    break;
                case TipiXmlDati.RISPOSTA:
                    // salvo ORIGINALE
                    aroXmlUpdUnitaDoc.setDsUrnXml(MessaggiWSFormat.formattaUrnPartRappVersUpd(tmpUrn,
                            Costanti.UrnFormatter.URN_RAPP_VERS_V2));
                    // salvo NORMALIZZATO
                    aroXmlUpdUnitaDoc.setDsUrnNormalizXml(MessaggiWSFormat.formattaUrnPartRappVersUpd(tmpUrnNorm,
                            Costanti.UrnFormatter.URN_RAPP_VERS_V2));
                    break;
                default:
                    break;
                }
            }
        }
    }

    private List<AroUpdUnitaDoc> retrieveAroUpdUnitaDocByUd(long idUnitaDoc) {
        Query query = entityManager
                .createQuery("SELECT upd FROM AroUpdUnitaDoc upd WHERE upd.aroUnitaDoc.idUnitaDoc = :idUnitaDoc ");
        query.setParameter("idUnitaDoc", idUnitaDoc);
        List<AroUpdUnitaDoc> upds = query.getResultList();
        return upds;
    }

    private List<VrsVLisXmlUpdUrnDaCalc> retrieveVrsVLisXmlUpdUrnDaCalcByUpd(long idUpdUnitaDoc) {
        Query query = entityManager
                .createQuery("SELECT vrs FROM VrsVLisXmlUpdUrnDaCalc vrs WHERE vrs.idUpdUnitaDoc = :idUpdUnitaDoc ");
        query.setParameter("idUpdUnitaDoc", idUpdUnitaDoc);
        List<VrsVLisXmlUpdUrnDaCalc> vrs = query.getResultList();
        return vrs;
    }

    //
    private boolean salvaDocElencoVers(StrutturaVersamento strutV, UnitaDocAggAllegati ud) {
        boolean tmpReturn = true;
        ElvDocAggDaElabElenco tmpTab = new ElvDocAggDaElabElenco();
        tmpTab.setAroDoc(entityManager.find(AroDoc.class, strutV.getDocumentiAttesi().get(0).getIdRecDocumentoDB()));
        tmpTab.setAaKeyUnitaDoc(new BigDecimal(ud.getIntestazione().getChiave().getAnno()));
        tmpTab.setDtCreazione(convert(strutV.getDataVersamento()));
        tmpTab.setIdStrut(new BigDecimal(strutV.getIdStruttura()));
        // nel caso il salvataggio sia su filesystem/Tivoli,
        // lo stato del documento passerà ad IN_ATTESA_SCHED
        // solo dopo la effettiva memorizzazione in Tivoli
        if (strutV.getTipoSalvataggioFile() == CostantiDB.TipoSalvataggioFile.FILE && strutV.isTpiAbilitato()) {
            tmpTab.setTiStatoDocDaElab(TipiStatoElementoVersato.IN_ATTESA_MEMORIZZAZIONE.name());
        } else {
            tmpTab.setTiStatoDocDaElab(TipiStatoElementoVersato.IN_ATTESA_SCHED.name());
        }

        // inserisco su DB
        try {
            entityManager.persist(tmpTab);
            entityManager.flush();
            strutV.getDocumentiAttesi().get(0).setTiStatoDocDaElab(tmpTab.getTiStatoDocDaElab());
        } catch (RuntimeException re) {
            /// logga l'errore e blocca tutto
            log.error("Eccezione nella persistenza del DOC da inserire in elenco di versamento ", re);
            tmpReturn = false;
        }
        return tmpReturn;
    }

    /*
     * ************************************************************************** parte comune + TIVOLI - verifica
     * percorsi nel caso la persistenza sia su filesystem
     * **************************************************************************
     */
    private boolean verificaCreaDirectoryFS(RispostaControlli risposta, StrutturaVersamento strutV) {
        StatoCreaCartelle statoCreaCartelle = new StatoCreaCartelle();

        risposta.reset();
        if (strutV.getTipoSalvataggioFile() == CostantiDB.TipoSalvataggioFile.FILE && strutV.isTpiAbilitato()) {
            try {
                RispostaControlli tmpControlli;

                // verifica se devo creare la cartella data, via database
                tmpControlli = salvataggioCompFS.verificaDirTPIDataViaDb(strutV, statoCreaCartelle, 1);

                // se non ho trovato la riga nel db corrispondente alla cartella, tento di
                // crearla
                if (tmpControlli.isrBoolean() && statoCreaCartelle.isCreaCartellaData()) {
                    tmpControlli = salvataggioCompFS.generaDirDataTPIBlock(strutV, statoCreaCartelle);
                }

                // se dopo il tentativo di creazione cartella, essa risulta ancora da creare,
                // vuol dire che
                // sono incappato in una violazione di constraint.
                // rifaccio la verifica in modo aggressivo, iterando finché non la trovo
                if (tmpControlli.isrBoolean() && statoCreaCartelle.isCreaCartellaData()) {
                    tmpControlli = salvataggioCompFS.verificaDirTPIDataViaDb(strutV, statoCreaCartelle,
                            SalvataggioCompFS.MAX_NUMERO_TENTATIVI_TEST);
                }

                // se continua a mancare la riga, allora termino in errore
                if (tmpControlli.isrBoolean() && statoCreaCartelle.isCreaCartellaData()) {
                    tmpControlli.setrBoolean(false);
                    tmpControlli
                            .setDsErr("Il record di VrsDtVers relativo alla data " + format(strutV.getDataVersamento())
                                    + "non risulta in database ma non è scrivibile. Impossibile proseguire");
                }

                // verifica se alla fine dei conti esiste la directory relativa alla data
                if (tmpControlli.isrBoolean()) {
                    tmpControlli = salvataggioCompFS.verificaDirSuFs(statoCreaCartelle.getPathCartellaData());
                }

                if (tmpControlli.isrBoolean()) {
                    // verifica se devo creare la cartella data, via database
                    tmpControlli = salvataggioCompFS.verificaDirTPIVersatoreViaDb(strutV, statoCreaCartelle, 1);
                } else {
                    // se manca la directory, allora termino in errore
                    if (tmpControlli.getDsErr() == null || tmpControlli.getDsErr().isEmpty()) {
                        tmpControlli.setDsErr("La directory corrispondentre alla data non è presente sul file system "
                                + "mentre risulta registrata nel database");
                    }
                }

                // se non ho trovato la riga nel db corrispondente alla cartella, tento di
                // crearla
                if (tmpControlli.isrBoolean() && statoCreaCartelle.isCreaCartellaVersatore()) {
                    tmpControlli = salvataggioCompFS.generaDirVersatoreTPIBlock(strutV, statoCreaCartelle);
                }

                // se dopo il tentativo di creazione cartella, essa risulta ancora da creare,
                // vuol dire che
                // sono incappato in una violazione di constraint.
                // rifaccio la verifica in modo aggressivo, iterando finché non la trovo
                if (tmpControlli.isrBoolean() && statoCreaCartelle.isCreaCartellaVersatore()) {
                    tmpControlli = salvataggioCompFS.verificaDirTPIVersatoreViaDb(strutV, statoCreaCartelle,
                            SalvataggioCompFS.MAX_NUMERO_TENTATIVI_TEST);
                }

                // se continua a mancare la riga, allora termino in errore
                if (tmpControlli.isrBoolean() && statoCreaCartelle.isCreaCartellaVersatore()) {
                    tmpControlli.setrBoolean(false);
                    tmpControlli
                            .setDsErr("Il record di VrsPathDtVers relativo alla data " + strutV.getSubPathVersatoreArk()
                                    + "non risulta in database ma non è scrivibile. Impossibile proseguire");
                }

                // verifica se alla fine dei conti esiste la directory relativa alla data
                if (tmpControlli.isrBoolean()) {
                    tmpControlli = salvataggioCompFS.verificaDirSuFs(statoCreaCartelle.getPathCartellaVersatore());
                }

                if (tmpControlli.isrBoolean()) {
                    // se sono ancora in gioco, genero la cartella relativa all'UD. qui sono l'unico
                    // attore e non dovrei avere problemi di concorrenza
                    tmpControlli = salvataggioCompFS.generaDirUDTPI(strutV, statoCreaCartelle);
                } else {
                    // se manca la directory, allora termino in errore
                    if (tmpControlli.getDsErr() == null || tmpControlli.getDsErr().isEmpty()) {
                        tmpControlli
                                .setDsErr("La directory corrispondentre al versatore non è presente sul file system "
                                        + "mentre risulta registrata nel database");
                    }
                }

                if (tmpControlli.isrBoolean()) {
                    risposta.setrObject(statoCreaCartelle);
                    risposta.setrBoolean(true);
                } else {
                    risposta.setDsErr(tmpControlli.getDsErr());
                }

            } catch (Exception re) {
                /// logga l'errore e blocca tutto
                log.error("Eccezione nella verifica/creazione directory per salvataggio su filesystem ", re);
                risposta.setDsErr(re.getMessage());
            }
        } else {
            risposta.setrBoolean(true);
        }
        return risposta.isrBoolean();
    }

    /*
     * ************************************************************************** parte comune
     * **************************************************************************
     */
    private boolean salvaDocumenti(StrutturaVersamento strutV, ConfigPerDoc configPerDoc, ChiaveType chiaveDoc,
            IRispostaVersWS rispostaWS) {
        boolean tmpReturn = true;
        Calendar tmpCal;

        for (DocumentoVers valueDocVers : strutV.getDocumentiAttesi()) {
            // INIZIALIZZO TUTTI i POJO JPA DI INTERESSE
            AroDoc tmpTabD = null;
            // SALVO DOCUMENTO
            tmpTabD = new AroDoc();

            // setto i valori generici
            tmpTabD.setAroUnitaDoc(entityManager.find(AroUnitaDoc.class, strutV.getIdUnitaDoc()));
            tmpTabD.setPgDoc(new BigDecimal(valueDocVers.getProgressivo()));
            tmpTabD.setDecTipoDoc(entityManager.find(DecTipoDoc.class, valueDocVers.getIdTipoDocumentoDB()));
            //
            if (configPerDoc.getTipoCreazioneDoc() == TipoCreazioneDoc.AGGIUNTA_DOCUMENTO) {
                // nel caso il salvataggio sia su filesystem/Tivoli,
                // lo stato del documento passerà ad IN_ATTESA_SCHED
                // solo dopo la effettiva memorizzazione in Tivoli
                if (strutV.getTipoSalvataggioFile() == CostantiDB.TipoSalvataggioFile.FILE && strutV.isTpiAbilitato()) {
                    tmpTabD.setTiStatoDocElencoVers(TipiStatoElementoVersato.IN_ATTESA_MEMORIZZAZIONE.name());
                } else {
                    tmpTabD.setTiStatoDocElencoVers(TipiStatoElementoVersato.IN_ATTESA_SCHED.name());
                }
            }

            tmpTabD.setDtCreazione(convert(strutV.getDataVersamento()));
            tmpCal = Calendar.getInstance();
            tmpCal.set(2444, 11, 31, 0, 0, 0);
            tmpTabD.setDtAnnul(tmpCal.getTime());
            //
            tmpTabD.setCdKeyDocVers(valueDocVers.getRifDocumento().getIDDocumento());
            tmpTabD.setIdStrut(new BigDecimal(strutV.getIdStruttura()));

            // aggiunti da Paolo
            tmpTabD.setFlDocFirmato(valueDocVers.getFlFileFirmato());
            tmpTabD.setDsMsgEsitoVerifFirme(valueDocVers.getDsMsgEsitoVerifica());
            tmpTabD.setTiEsitoVerifFirme(valueDocVers.getTiEsitoVerifFirme());

            // configurazione
            tmpTabD.setFlForzaAccettazione(configPerDoc.getForzaAccettazione());
            tmpTabD.setFlForzaConservazione(configPerDoc.getForzaConservazione());
            tmpTabD.setTiConservazione(configPerDoc.getTipoConservazione());
            tmpTabD.setNmSistemaMigraz(
                    configPerDoc.getSistemaDiMigrazione() != null ? configPerDoc.getSistemaDiMigrazione() : "");

            // salvo se il doc è creato da un versamento o da un'aggiunta documento
            tmpTabD.setTiCreazione(configPerDoc.getTipoCreazioneDoc().name());

            // sezione dati fiscali
            tmpTabD.setFlDocFisc("0");

            // sezione profilo documento
            if (valueDocVers.getRifDocumento().getProfiloDocumento() != null) {
                tmpTabD.setDlDoc(valueDocVers.getRifDocumento().getProfiloDocumento().getDescrizione());
                tmpTabD.setDsAutoreDoc(valueDocVers.getRifDocumento().getProfiloDocumento().getAutore());
            }

            // tipo documento (PRINCIPALE, ALLEGATO, ANNOTAZIONE, ANNESSO)
            tmpTabD.setTiDoc(valueDocVers.getCategoriaDoc().getValoreDb());
            // niOrd
            tmpTabD.setNiOrdDoc(new BigDecimal(valueDocVers.getNiOrdDoc()));

            // inserisco su DB
            try {
                entityManager.persist(tmpTabD);
                entityManager.flush();
                valueDocVers.setIdRecDocumentoDB(tmpTabD.getIdDoc());
            } catch (RuntimeException re) {
                /// logga l'errore e blocca tutto
                tmpReturn = false;
                if (configPerDoc.getTipoCreazioneDoc() == TipoCreazioneDoc.AGGIUNTA_DOCUMENTO && ExceptionUtils
                        .indexOfThrowable(re, java.sql.SQLIntegrityConstraintViolationException.class) > -1) {
                    log.info(
                            "QUESTO E' UN ERRORE PROVOCATO DA UN CLIENT SCRITTO MALE: SQLIntegrityConstraintViolationException ");
                    rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                    rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.DOC_008_001,
                            valueDocVers.getRifDocumento().getIDDocumento(), strutV.getUrnPartChiaveUd());
                    rispostaWS.setErroreElementoDoppio(true);
                    rispostaWS.setIdElementoDoppio(0); // non serve a nulla,
                    // ma visto che in ogni caso non esiste ancora l'ID
                    // tanto vale impostare questo dato ad un valore non trovabile
                    ((RispostaWSAggAll) rispostaWS).getIstanzaEsito()
                            .setXMLVersamento("La sessione di versamento per il DOC da versare non è presente.");
                    break;
                } else {
                    /// logga l'errore e blocca tutto
                    log.error("Eccezione nella persistenza del documento ", re);
                }
            }

            // salvo le tabelle dei dati accessori ai documenti (fiscali, specifici, ecc)
            if (tmpReturn) {
                tmpReturn = this.salvaDatiAccessoriDocumenti(valueDocVers, strutV);
            }

            if (tmpReturn) {
                tmpReturn = this.rimuoviSessVersDocErrate(valueDocVers, strutV, chiaveDoc);
            }

            if (!tmpReturn) {
                rispostaWS.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P, "Eccezione nella persistenza del documento ");
                rispostaWS.setSeverity(IRispostaWS.SeverityEnum.ERROR);
                break;
            }
        }

        return tmpReturn;
    }

    private boolean salvaDatiAccessoriDocumenti(DocumentoVers documentoVersIn, StrutturaVersamento strutV) {
        boolean tmpReturn = true;
        AroStrutDoc tmpTabSD = null;

        // salvo dati specifici, tabelle nuove
        if (tmpReturn) {
            tmpReturn = this.salvaDatiSpecGen(CostantiDB.TipiUsoDatiSpec.VERS, TipiEntitaSacer.DOC,
                    documentoVersIn.getIdRecDocumentoDB(), strutV.getIdStruttura(),
                    documentoVersIn.getIdRecXsdDatiSpec(), documentoVersIn.getDatiSpecifici(), strutV.getIdUnitaDoc());
        }

        if (tmpReturn) {
            tmpReturn = this.salvaDatiSpecGen(CostantiDB.TipiUsoDatiSpec.MIGRAZ, TipiEntitaSacer.DOC,
                    documentoVersIn.getIdRecDocumentoDB(), strutV.getIdStruttura(),
                    documentoVersIn.getIdRecXsdDatiSpecMigrazione(), documentoVersIn.getDatiSpecificiMigrazione(),
                    strutV.getIdUnitaDoc());
        }

        // SALVO STRUTTURA DEL DOCUMENTO
        if (tmpReturn) {
            // Istanzio il POJO AroStrutDoc
            tmpTabSD = new AroStrutDoc();

            // setto i valori generici
            tmpTabSD.setAroDoc(entityManager.find(AroDoc.class, documentoVersIn.getIdRecDocumentoDB()));
            tmpTabSD.setNiOrdStrutDoc(new BigDecimal(1));
            tmpTabSD.setDecTipoStrutDoc(
                    entityManager.find(DecTipoStrutDoc.class, documentoVersIn.getIdTipoStrutturaDB()));
            tmpTabSD.setFlStrutOrig("1");
            tmpTabSD.setIdStrut(new BigDecimal(strutV.getIdStruttura()));

            // inserisco su DB
            try {
                entityManager.persist(tmpTabSD);
                entityManager.flush();
                documentoVersIn.setIdRecStrutturaDB(tmpTabSD.getIdStrutDoc());
            } catch (RuntimeException re) {
                /// logga l'errore e blocca tutto
                log.error("Eccezione nella persistenza della struttura del documento ", re);
                tmpReturn = false;
            }
        }

        return tmpReturn;
    }

    private boolean rimuoviSessVersDocErrate(DocumentoVers documentoVersIn, StrutturaVersamento strutV,
            ChiaveType chiaveUd) {
        boolean tmpReturn = true;
        long numUdNonVersRimosse;
        String queryStr = "delete from VrsDocNonVer al " + "where al.orgStrut.idStrut = :idStrutIn "
                + "and al.aaKeyUnitaDoc = :aaKeyUnitaDocIn " + "and al.cdKeyUnitaDoc = :cdKeyUnitaDocIn "
                + "and al.cdRegistroKeyUnitaDoc = :cdRegistroKeyUnitaDocIn " + "and al.cdKeyDocVers = :cdKeyDocVersIn";
        javax.persistence.Query query = entityManager.createQuery(queryStr);
        query.setParameter("idStrutIn", new BigDecimal(strutV.getIdStruttura()));
        query.setParameter("cdRegistroKeyUnitaDocIn", chiaveUd.getTipoRegistro());
        query.setParameter("aaKeyUnitaDocIn", new BigDecimal(chiaveUd.getAnno()));
        query.setParameter("cdKeyUnitaDocIn", chiaveUd.getNumero());
        query.setParameter("cdKeyDocVersIn", documentoVersIn.getRifDocumento().getIDDocumento());
        try {
            numUdNonVersRimosse = query.executeUpdate();
            if (numUdNonVersRimosse > 0) {
                log.debug(String.format("Sono stati rimossi %s tentativi falliti di versamento UD ",
                        numUdNonVersRimosse));
            }
        } catch (RuntimeException re) {
            /// logga l'errore e blocca tutto
            log.error("Eccezione nell'aggiornamento della tabella VrsUnitaDocNonVer ", re);
            tmpReturn = false;
        }

        return tmpReturn;
    }

    private boolean salvaDatiWarning(AbsVersamentoExt versamento) {
        boolean tmpReturn = true;
        AroWarnUnitaDoc tmpWarnUnitaDoc;
        int progWarn = 1;
        BigDecimal max = null;
        String tmpErrMess;
        try {
            // recupera l'ultimo numero progressivo di warning per l'UD.
            // questo serve nel caso il versamento sia per l'aggiunta di documenti
            // ed il versamento originale abbia presentato dei warning
            String queryStr = "select max(u.pgWarnUnitaDoc) " + "from AroWarnUnitaDoc u "
                    + "where u.aroUnitaDoc.idUnitaDoc = :idUnitaDocIn ";
            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("idUnitaDocIn", versamento.getStrutturaComponenti().getIdUnitaDoc());
            max = (BigDecimal) query.getSingleResult();
            if (max != null) {
                progWarn = max.intValue() + 1;
            }

            for (VoceDiErrore tmpVdE : versamento.getErroriTrovati()) {
                tmpWarnUnitaDoc = new AroWarnUnitaDoc();
                tmpWarnUnitaDoc.setCdWarn(tmpVdE.getErrorCode());

                if (tmpVdE.getErrorMessage().isEmpty()) {
                    tmpErrMess = "(vuoto)";
                } else {
                    tmpErrMess = tmpVdE.getErrorMessage();
                    if (tmpErrMess.length() > DS_ERR_MAX_LEN) {
                        tmpErrMess = tmpErrMess.substring(0, DS_ERR_MAX_LEN);
                    }
                }

                tmpWarnUnitaDoc.setDsWarn(tmpErrMess);
                tmpWarnUnitaDoc.setPgWarnUnitaDoc(new BigDecimal(progWarn));
                tmpWarnUnitaDoc.setTiEntitaSacer(tmpVdE.getElementoResponsabile().name());
                tmpWarnUnitaDoc.setAroUnitaDoc(
                        entityManager.find(AroUnitaDoc.class, versamento.getStrutturaComponenti().getIdUnitaDoc()));
                if (tmpVdE.getRifDocumentoVers() != null) {
                    tmpWarnUnitaDoc.setAroDoc(
                            entityManager.find(AroDoc.class, tmpVdE.getRifDocumentoVers().getIdRecDocumentoDB()));
                }
                progWarn++;
                // inserisco su DB
                entityManager.persist(tmpWarnUnitaDoc);
                entityManager.flush();
            }
        } catch (RuntimeException re) {
            // logga l'errore e blocca tutto
            log.error("Eccezione nella persistenza del warning ", re);
            tmpReturn = false;
        }

        return tmpReturn;
    }

    private boolean salvaComponenti(RispostaControlli risposta, StrutturaVersamento strutV, ChiaveType chiave) {
        boolean tmpReturn = true;
        AroCompDoc tmpTabCDComponente = null;
        AroCompDoc tmpTabCDSottoComp = null;
        AroStrutDoc tmpAroStrutDoc = null;
        AroStrutDoc tmpAroStrutDocMerged = null;
        // String tmpUrnCalc;

        risposta.reset();
        for (DocumentoVers valueDocVers : strutV.getDocumentiAttesi()) {
            // recupero la struttura documento a cui agganciare i componenti del documento
            tmpAroStrutDoc = entityManager.find(AroStrutDoc.class, valueDocVers.getIdRecStrutturaDB());
            for (ComponenteVers tmpCV : valueDocVers.getFileAttesi()) {
                // tmpUrnCalc = this.calcolaUrnComp(tmpCV, strutV.getUrnPartVersatore());
                if (tmpCV.getMySottoComponente() == null) {
                    // sto elaborando un componente
                    // MEV#18660 {
                    // tmpTabCDComponente = tmpCV.getAcdEntity();
                    // if (tmpTabCDComponente == null) { // questo caso si verifica se il tipo
                    // supporto è diverso da
                    // FILE
                    // tmpTabCDComponente = new AroCompDoc();
                    // }
                    tmpTabCDComponente = new AroCompDoc();
                    // } MEV#18660

                    tmpTabCDComponente.setAroStrutDoc(tmpAroStrutDoc);
                    tmpTabCDComponente
                            .setNiOrdCompDoc(new BigDecimal(tmpCV.getMyComponente().getOrdinePresentazione()));
                    tmpTabCDComponente
                            .setTiSupportoComp(tmpCV.getMyComponente().getTipoSupportoComponente().toString());
                    tmpTabCDComponente
                            .setDecTipoCompDoc(entityManager.find(DecTipoCompDoc.class, tmpCV.getIdTipoComponente()));
                    tmpTabCDComponente.setDsNomeCompVers(tmpCV.getMyComponente().getNomeComponente());
                    tmpTabCDComponente.setDlUrnCompVers(tmpCV.getMyComponente().getUrnVersato());
                    tmpTabCDComponente.setDsIdCompVers(tmpCV.getMyComponente().getIDComponenteVersato());
                    tmpTabCDComponente.setDecTipoRapprComp(
                            entityManager.find(DecTipoRapprComp.class, tmpCV.getIdTipoRappresentazioneComponente()));

                    switch (tmpCV.getTipoSupporto()) {
                    case FILE:
                        tmpTabCDComponente.setNiSizeFileCalc(new BigDecimal(tmpCV.getRifFileBinario().getDimensione()));
                        tmpTabCDComponente.setDecFormatoFileDoc(
                                entityManager.find(DecFormatoFileDoc.class, tmpCV.getIdFormatoFileVers()));
                        break;
                    case METADATI:
                        // nulla da fare
                        break;
                    case RIFERIMENTO:
                        tmpTabCDComponente
                                .setAroUnitaDoc(entityManager.find(AroUnitaDoc.class, tmpCV.getIdUnitaDocRif()));
                        break;
                    default:
                    }

                    // aggiungo l'hash versato (solo i componenti)
                    tmpTabCDComponente.setDsHashFileVers(tmpCV.getMyComponente().getHashVersato());

                    // aggiungo la descrizione riferimento temporale (solo per i componenti)
                    tmpTabCDComponente.setDsRifTempVers(tmpCV.getMyComponente().getDescrizioneRiferimentoTemporale());

                    // non viene più persistito
                    // tmpTabCDComponente.setDsUrnCompCalc(tmpUrnCalc);

                    tmpTabCDComponente.setIdStrut(new BigDecimal(strutV.getIdStruttura()));
                    //// MEV#18660 {
                    // TODO: (da firma MEV#18660)
                    CompDocMock mock = tmpCV.withAcdEntity();
                    Date tmRifTempVers = null;
                    if (mock.getTmRifTempVers() != null) {
                        tmRifTempVers = Date.from(mock.getTmRifTempVers().toInstant());
                    }
                    tmpTabCDComponente.setTmRifTempVers(tmRifTempVers);
                    //
                    tmpTabCDComponente.setTiEsitoContrFormatoFile(mock.getTiEsitoContrFormatoFile());
                    tmpTabCDComponente.setTiEsitoVerifFirme(mock.getTiEsitoVerifFirme());
                    tmpTabCDComponente.setTiEsitoVerifFirmeDtVers(mock.getTiEsitoVerifFirmeDtVers());
                    //
                    tmpTabCDComponente.setFlNoCalcFmtVerifFirme(mock.getFlNoCalcFmtVerifFirme());
                    tmpTabCDComponente.setFlRifTempDataFirmaVers(mock.getFlRifTempDataFirmaVers());
                    //
                    tmpTabCDComponente.setDsMsgEsitoVerifFirme(mock.getDsMsgEsitoVerifFirme());
                    tmpTabCDComponente.setDsMsgEsitoContrFormato(mock.getDsMsgEsitoContrFormato());
                    tmpTabCDComponente.setDsEsitoVerifFirmeDtVers(mock.getDsEsitoVerifFirmeDtVers());
                    tmpTabCDComponente.setDsFormatoRapprCalc(mock.getDsFormatoRapprCalc());
                    tmpTabCDComponente.setDsFormatoRapprEstesoCalc(mock.getDsFormatoRapprEstesoCalc());
                    //
                    // TODO : da verificare se le due SET sotto sono necessarie
                    tmpTabCDComponente.setTiEsitoContrHashVers(mock.getTiEsitoContrHashVers());
                    tmpTabCDComponente.setDsHashFileContr(mock.getDsHashFileContr());
                    //
                    tmpTabCDComponente.setFlCompFirmato(mock.getFlCompFirmato());

                    // TODO : da verificare
                    if (mock.getIdDecFormatoFileStandard() != null) {
                        tmpTabCDComponente.setDecFormatoFileStandard(entityManager.find(DecFormatoFileStandard.class,
                                mock.getIdDecFormatoFileStandard().longValue()));
                    }

                    //// } MEV#18660
                    // salvo nell'esito del componente il valore dell'URN calcolato
                    // tmpCV.getRifComponenteResp().setURN(tmpUrnCalc);
                    // aggiungo alla struttura documento il componente appena elaborato
                    tmpAroStrutDoc.getAroCompDocs().add(tmpTabCDComponente);
                    // salvo l'ordine di presentazione nella struttura documento, per recuperarlo in
                    // seguito
                    tmpCV.setOrdinePresentazione(tmpCV.getMyComponente().getOrdinePresentazione());
                    //
                    tmpTabCDComponente.setAroAroCompUrnCalcs(new ArrayList<AroCompUrnCalc>());

                    // gestione nuovo urn ORIGINALE
                    String tmpUrn = MessaggiWSFormat.formattaBaseUrnDoc(strutV.getUrnPartVersatore(),
                            strutV.getUrnPartChiaveUd(), tmpCV.getUrnPartComponenteNiOrdDoc(),
                            Costanti.UrnFormatter.URN_COMP_FMT_STRING);

                    // salvo nell'esito del componente il valore dell'URN calcolato
                    tmpCV.getRifComponenteResp().setURN(tmpUrn);

                    this.salvaCompUrnCalc(tmpTabCDComponente, tmpUrn, TiUrn.ORIGINALE);

                    // gestione nuovo urn NORMALIZZATO
                    tmpUrn = MessaggiWSFormat.formattaBaseUrnDoc(strutV.getUrnPartVersatoreNormalized(),
                            strutV.getUrnPartChiaveUdNormalized(), tmpCV.getUrnPartComponenteNiOrdDoc(),
                            Costanti.UrnFormatter.URN_COMP_FMT_STRING);

                    this.salvaCompUrnCalc(tmpTabCDComponente, tmpUrn, TiUrn.NORMALIZZATO);
                    // init bi-directionals
                    tmpTabCDComponente.setAroMarcaComps(new ArrayList<AroMarcaComp>());
                    tmpTabCDComponente.setAroFirmaComps(new ArrayList<AroFirmaComp>());
                    tmpTabCDComponente.setAroBustaCrittogs(new ArrayList<AroBustaCrittog>());
                    tmpTabCDComponente.setFirReport(new ArrayList<FirReport>());

                    // flag di verifica firma attivati e almeno una busta presente
                    boolean elabResultVerificaFirma = strutV.effettuaVerificaFirma()
                            && tmpCV.hasBusteOnVerificaFirmaWrapper();
                    if (elabResultVerificaFirma) {
                        // per ogni componente salvo i dati relativi alla firma (se esistono)
                        tmpReturn = salvataggioFirmaManager.salvaBustaCrittografica(risposta, tmpCV.getId(),
                                tmpCV.getVfWrapper(), tmpTabCDComponente, tmpTabCDSottoComp);
                        // se ci sono problemi nel salvataggio, blocco tutto
                        if (!tmpReturn) {
                            return tmpReturn;
                        }
                        // per il componente "dichiarato" come firmato salvo i dati relativi al report
                        // della firma
                        if (StringUtils.isNotBlank(tmpTabCDComponente.getFlCompFirmato())
                                && tmpTabCDComponente.getFlCompFirmato().equals(Flag.TRUE)) {
                            tmpReturn = salvataggioFirmaManager.salvaReportVerificaCompDoc(risposta,
                                    tmpCV.getVfWrapper(), strutV, tmpTabCDComponente);
                            // se ci sono problemi nel salvataggio, blocco tutto
                            if (!tmpReturn) {
                                return tmpReturn;
                            }
                        }
                    }
                } else {
                    // MEV#18660 {
                    // sto elaborando un sottocomponente
                    // tmpTabCDSottoComp = tmpCV.getAcdEntity();
                    // if (tmpTabCDSottoComp == null) { // questo caso si verifica se il tipo
                    // supporto è diverso da FILE
                    // tmpTabCDSottoComp = new AroCompDoc();
                    // }
                    tmpTabCDSottoComp = new AroCompDoc();
                    // } MEV#18660

                    // imposto il riferimento al componente padre
                    tmpTabCDSottoComp.setAroCompDoc(tmpTabCDComponente);

                    tmpTabCDSottoComp.setAroStrutDoc(tmpAroStrutDoc);
                    tmpTabCDSottoComp
                            .setNiOrdCompDoc(new BigDecimal(tmpCV.getMySottoComponente().getOrdinePresentazione()));
                    tmpTabCDSottoComp
                            .setTiSupportoComp(tmpCV.getMySottoComponente().getTipoSupportoComponente().toString());
                    tmpTabCDSottoComp
                            .setDecTipoCompDoc(entityManager.find(DecTipoCompDoc.class, tmpCV.getIdTipoComponente()));
                    tmpTabCDSottoComp.setDsNomeCompVers(tmpCV.getMySottoComponente().getNomeComponente());
                    tmpTabCDSottoComp.setDlUrnCompVers(tmpCV.getMySottoComponente().getUrnVersato());
                    tmpTabCDSottoComp.setDsIdCompVers(tmpCV.getMySottoComponente().getIDComponenteVersato());

                    switch (tmpCV.getTipoSupporto()) {
                    case FILE:
                        tmpTabCDSottoComp.setNiSizeFileCalc(new BigDecimal(tmpCV.getRifFileBinario().getDimensione()));
                        tmpTabCDSottoComp.setDecFormatoFileDoc(
                                entityManager.find(DecFormatoFileDoc.class, tmpCV.getIdFormatoFileVers()));
                        break;
                    case METADATI:
                        // nulla da fare
                        break;
                    case RIFERIMENTO:
                        tmpTabCDSottoComp
                                .setAroUnitaDoc(entityManager.find(AroUnitaDoc.class, tmpCV.getIdUnitaDocRif()));
                        break;
                    default:
                    }

                    // non viene più persistito
                    // tmpTabCDSottoComp.setDsUrnCompCalc(tmpUrnCalc);
                    tmpTabCDSottoComp.setIdStrut(new BigDecimal(strutV.getIdStruttura()));

                    // salvo nell'esito del sottocomponente il valore dell'URN calcolato
                    // tmpCV.getRifSottoComponenteResp().setURN(tmpUrnCalc);
                    // aggiungo alla struttura documento il componente appena elaborato
                    tmpAroStrutDoc.getAroCompDocs().add(tmpTabCDSottoComp);
                    // salvo l'ordine di presentazione nella struttura documento, per recuperarlo in
                    // seguito
                    tmpCV.setOrdinePresentazione(tmpCV.getMySottoComponente().getOrdinePresentazione());

                    //
                    tmpTabCDSottoComp.setAroAroCompUrnCalcs(new ArrayList<AroCompUrnCalc>());

                    // gestione nuovo urn ORIGINALE
                    String tmpUrn = MessaggiWSFormat.formattaBaseUrnDoc(strutV.getUrnPartVersatore(),
                            strutV.getUrnPartChiaveUd(), tmpCV.getUrnPartComponenteNiOrdDoc(),
                            Costanti.UrnFormatter.URN_COMP_FMT_STRING);

                    this.salvaCompUrnCalc(tmpTabCDSottoComp, tmpUrn, TiUrn.ORIGINALE);

                    // salvo nell'esito del sottocomponente il valore dell'URN calcolato
                    tmpCV.getRifSottoComponenteResp().setURN(tmpUrn);

                    // gestione nuovo urn NORMALIZZATO
                    tmpUrn = MessaggiWSFormat.formattaBaseUrnDoc(strutV.getUrnPartVersatoreNormalized(),
                            strutV.getUrnPartChiaveUdNormalized(), tmpCV.getUrnPartComponenteNiOrdDoc(),
                            Costanti.UrnFormatter.URN_COMP_FMT_STRING);

                    this.salvaCompUrnCalc(tmpTabCDSottoComp, tmpUrn, TiUrn.NORMALIZZATO);

                    //// MEV#18660 {
                    // TODO: (da firma MEV#18660)
                    CompDocMock mock = tmpCV.withAcdEntity();
                    Date tmRifTempVers = null;
                    if (mock.getTmRifTempVers() != null) {
                        tmRifTempVers = Date.from(mock.getTmRifTempVers().toInstant());
                    }
                    tmpTabCDSottoComp.setTmRifTempVers(tmRifTempVers);
                    //
                    tmpTabCDSottoComp.setTiEsitoContrFormatoFile(mock.getTiEsitoContrFormatoFile());
                    tmpTabCDSottoComp.setTiEsitoVerifFirme(mock.getTiEsitoVerifFirme());
                    tmpTabCDSottoComp.setTiEsitoVerifFirmeDtVers(mock.getTiEsitoVerifFirmeDtVers());

                    //
                    tmpTabCDSottoComp.setFlNoCalcFmtVerifFirme(mock.getFlNoCalcFmtVerifFirme());
                    tmpTabCDSottoComp.setFlRifTempDataFirmaVers(mock.getFlRifTempDataFirmaVers());
                    //
                    tmpTabCDSottoComp.setDsMsgEsitoVerifFirme(mock.getDsMsgEsitoVerifFirme());
                    tmpTabCDSottoComp.setDsMsgEsitoContrFormato(mock.getDsMsgEsitoContrFormato());
                    tmpTabCDSottoComp.setDsEsitoVerifFirmeDtVers(mock.getDsEsitoVerifFirmeDtVers());
                    tmpTabCDSottoComp.setDsFormatoRapprCalc(mock.getDsFormatoRapprCalc());
                    tmpTabCDSottoComp.setDsFormatoRapprEstesoCalc(mock.getDsFormatoRapprEstesoCalc());

                    // TODO : da verificare se le due SET sotto sono necessarie
                    tmpTabCDSottoComp.setTiEsitoContrHashVers(mock.getTiEsitoContrHashVers());
                    tmpTabCDSottoComp.setDsHashFileContr(mock.getDsHashFileContr());
                    //
                    tmpTabCDSottoComp.setFlCompFirmato(mock.getFlCompFirmato());

                    // TODO : da verificare
                    if (mock.getIdDecFormatoFileStandard() != null) {
                        tmpTabCDSottoComp.setDecFormatoFileStandard(entityManager.find(DecFormatoFileStandard.class,
                                mock.getIdDecFormatoFileStandard().longValue()));
                    }
                    // init bi-directionals
                    tmpTabCDSottoComp.setAroMarcaComps(new ArrayList<AroMarcaComp>());
                    tmpTabCDSottoComp.setAroFirmaComps(new ArrayList<AroFirmaComp>());
                    tmpTabCDSottoComp.setAroBustaCrittogs(new ArrayList<AroBustaCrittog>());
                    tmpTabCDSottoComp.setFirReport(new ArrayList<FirReport>());

                    //// } MEV#18660
                    boolean elabResultVerificaFirma = strutV.effettuaVerificaFirma()
                            && tmpCV.hasBusteOnVerificaFirmaWrapper();
                    if (elabResultVerificaFirma) {
                        // per ogni componente salvo i dati relativi alla firma (se esistono)
                        tmpReturn = salvataggioFirmaManager.salvaBustaCrittografica(risposta, tmpCV.getId(),
                                tmpCV.getVfWrapper(), tmpTabCDComponente, tmpTabCDSottoComp);
                        // se ci sono problemi nel salvataggio, blocco tutto
                        if (!tmpReturn) {
                            return tmpReturn;
                        }
                        // per il componente "dichiarato" come firmato salvo i dati relativi al report
                        // della firma
                        if (StringUtils.isNotBlank(tmpTabCDSottoComp.getFlCompFirmato())
                                && tmpTabCDSottoComp.getFlCompFirmato().equals(Flag.TRUE)) {
                            tmpReturn = salvataggioFirmaManager.salvaReportVerificaCompDoc(risposta,
                                    tmpCV.getVfWrapper(), strutV, tmpTabCDSottoComp);
                            // se ci sono problemi nel salvataggio, blocco tutto
                            if (!tmpReturn) {
                                return tmpReturn;
                            }
                        }
                    }
                }
            }

            try {
                // salvo la struttura documento con i componenti aggiunti
                tmpAroStrutDocMerged = entityManager.merge(tmpAroStrutDoc);
                entityManager.flush();
                // ricarico i componenti appena salvati per recuperare gli ID e conservarli in
                // memoria
                // inoltre preparo e salvo i record di contenuto componente relativi ai file
                switch (strutV.getTipoSalvataggioFile()) {
                case BLOB:
                    tmpReturn = this.salvaBlobStrutDoc(tmpAroStrutDocMerged, valueDocVers, risposta, strutV, chiave,
                            WriteCompBlbOracle.TabellaBlob.ARO_CONTENUTO_COMP);
                    break;
                case FILE:
                    if (strutV.isTpiAbilitato()) {
                        tmpReturn = this.salvaFileStrutDoc(tmpAroStrutDocMerged, valueDocVers, risposta, strutV);
                    } else {
                        /*
                         * se il TPI non è stato installato, vuol dire che tutta la gestione asincrona del versamento
                         * basata su TIVOLI è inutilizabile. In questo caso lo storage dei documenti avviene su una
                         * tabella di blob dedicata chiamata ARO_FILE_COMP con struttura identica a ARO_CONTENUTO_COMP
                         */
                        tmpReturn = this.salvaBlobStrutDoc(tmpAroStrutDocMerged, valueDocVers, risposta, strutV, chiave,
                                WriteCompBlbOracle.TabellaBlob.ARO_FILE_COMP);
                    }
                    break;
                }
            } catch (RuntimeException re) {
                /// logga l'errore e blocca tutto
                risposta.setrBoolean(false);
                risposta.setDsErr(re.getMessage());
                log.error("Eccezione nella persistenza del componente ", re);
                tmpReturn = false;
                break;
            }
            // se ci sono problemi nel salvataggio di un documento, blocco tutto
            if (!tmpReturn) {
                break;
            }
        }

        return tmpReturn;
    }

    /**
     * Rispetta la firma degli altri metodi privati, di fatto restituisce sempre true, non esistono infatti casistiche
     * per cui debba fallire
     * 
     * @param aroCompDoc
     * @param tmpUrn
     * @param tiUrn
     */
    private void salvaCompUrnCalc(AroCompDoc aroCompDoc, String tmpUrn, TiUrn tiUrn) {
        AroCompUrnCalc tmpTabCDUrnComponenteCalc = null;

        tmpTabCDUrnComponenteCalc = new AroCompUrnCalc();
        tmpTabCDUrnComponenteCalc.setAroCompDoc(aroCompDoc);
        tmpTabCDUrnComponenteCalc.setDsUrn(tmpUrn);
        tmpTabCDUrnComponenteCalc.setTiUrn(tiUrn);

        aroCompDoc.getAroAroCompUrnCalcs().add(tmpTabCDUrnComponenteCalc);
    }

    private boolean salvaBlobStrutDoc(AroStrutDoc aroStrutDoc, DocumentoVers valueDocVers, RispostaControlli risposta,
            StrutturaVersamento strutV, ChiaveType chiave, WriteCompBlbOracle.TabellaBlob tabellaBlob) {
        // ricarico i componenti appena salvati per recuperare gli ID e conservarli in
        // memoria
        // inoltre preparo e salvo i record di contenuto componente relativi ai file
        for (AroCompDoc tmpCompDoc : aroStrutDoc.getAroCompDocs()) {
            for (ComponenteVers tmpCV : valueDocVers.getFileAttesi()) {
                if (tmpCV.getOrdinePresentazione() == tmpCompDoc.getNiOrdCompDoc().longValue()) {
                    tmpCV.setIdRecDB(tmpCompDoc.getIdCompDoc());
                    if (!this.aggiungiBlobSuBlob(risposta, tmpCV, tmpCompDoc, strutV, chiave, tabellaBlob)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean salvaFileStrutDoc(AroStrutDoc aroStrutDoc, DocumentoVers valueDocVers, RispostaControlli risposta,
            StrutturaVersamento strutV) {
        // ricarico i componenti appena salvati per recuperare gli ID e conservarli in
        // memoria
        // inoltre preparo e salvo i record di contenuto componente relativi ai file
        for (AroCompDoc tmpCompDoc : aroStrutDoc.getAroCompDocs()) {
            for (ComponenteVers tmpCV : valueDocVers.getFileAttesi()) {
                if (tmpCV.getOrdinePresentazione() == tmpCompDoc.getNiOrdCompDoc().longValue()) {
                    tmpCV.setIdRecDB(tmpCompDoc.getIdCompDoc());
                    if (!this.aggiungiBlobSuFile(risposta, tmpCV, tmpCompDoc, strutV)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean aggiungiBlobSuBlob(RispostaControlli risposta, ComponenteVers componente, AroCompDoc compntEntity,
            StrutturaVersamento strutV, ChiaveType chiave, WriteCompBlbOracle.TabellaBlob tabellaBlob) {
        boolean tmpReturn = true;
        FileBinario tmpFb;
        byte[] hash;

        if (componente.getTipoSupporto() != ComponenteVers.TipiSupporto.FILE) {
            return tmpReturn;
        }

        tmpFb = componente.getRifFileBinario();
        if (tmpFb.isInMemoria()) {
            risposta.setrBoolean(false);
            risposta.setDsErr("[BLOB] Errore: non è possibile archiviare un file gestito in memoria.");
            tmpReturn = false;
        }

        if (tmpReturn) {
            try (FileInputStream fis = new FileInputStream(tmpFb.getFileSuDisco())) {
                if (componente.getHashVersatoAlgoritmo() != null) {
                    compntEntity.setDsAlgoHashFileVers(componente.getHashVersatoAlgoritmo().descrivi());
                }
                if (componente.getHashVersatoEncoding() != null) {
                    compntEntity.setCdEncodingHashFileVers(componente.getHashVersatoEncoding().descrivi());
                }

                if (componente.isHashForzato()) {
                    // se l'hash è forzato, via versamento Multimedia
                    hash = componente.getHashCalcolato();
                    compntEntity.setDsHashFileCalc(BinEncUtility.encodeUTF8HexString(hash));
                    compntEntity.setFlNoCalcHashFile("1");
                    if (componente.isHashVersNonDefinito()) {
                        compntEntity.setTiEsitoContrHashVers(CostantiDB.TipiEsitoVerificaHash.NEGATIVO.name());
                    } else {
                        compntEntity.setTiEsitoContrHashVers(CostantiDB.TipiEsitoVerificaHash.NON_EFFETTUATO.name());
                    }
                } else if (componente.getHashCalcolato() != null && componente.getHashCalcolato().length > 0) {
                    // se l'hash è stato calcolato, in seguito alla verifica hash
                    hash = componente.getHashCalcolato();
                    compntEntity.setDsHashFileCalc(BinEncUtility.encodeUTF8HexString(hash));
                    compntEntity.setFlNoCalcHashFile("0");
                } else {
                    // se lo devo calcolare (verifica disattivata, sottocomponenti, componenti con
                    // hash versato non decifrabile)
                    hash = new HashCalculator().calculateSHAX(fis, TipiHash.SHA_256).getHashCalcolato();
                    componente.setHashCalcolato(hash);
                    compntEntity.setDsHashFileCalc(BinEncUtility.encodeUTF8HexString(hash));
                    compntEntity.setFlNoCalcHashFile("0");
                    if (componente.isHashVersNonDefinito()) {
                        compntEntity.setTiEsitoContrHashVers(CostantiDB.TipiEsitoVerificaHash.NEGATIVO.name());
                    } else {
                        compntEntity.setTiEsitoContrHashVers(CostantiDB.TipiEsitoVerificaHash.DISABILITATO.name());
                    }
                }

                // in ogni caso algoritmo ed encoding sono considerati costanti
                // compntEntity.setDsAlgoHashFileCalc(CostantiDB.TipiHash.SHA_1.descrivi());
                compntEntity.setDsAlgoHashFileCalc(CostantiDB.TipiHash.SHA_256.descrivi());
                compntEntity.setCdEncodingHashFileCalc(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi());

                if (componente.getMySottoComponente() == null) {
                    // aggiorno l'esito componente
                    // salvo nell'esito del componente il valore dell'hash calcolato
                    componente.getRifComponenteResp().setHash(componente.getHashCalcolato());
                    componente.getRifComponenteResp().setAlgoritmoHash(compntEntity.getDsAlgoHashFileCalc());
                    componente.getRifComponenteResp().setEncoding(compntEntity.getCdEncodingHashFileCalc());
                } else {
                    // aggiorno l'esito sottocomponente
                    // salvo nell'esito del sottocomponente il valore dell'hash calcolato
                    componente.getRifSottoComponenteResp().setHash(componente.getHashCalcolato());
                    componente.getRifSottoComponenteResp().setAlgoritmoHash(compntEntity.getDsAlgoHashFileCalc());
                    componente.getRifSottoComponenteResp().setEncoding(compntEntity.getCdEncodingHashFileCalc());
                }
            } catch (IOException | NoSuchAlgorithmException ex) {
                // fai qualcosa per segnalare il problema...
                /// logga l'errore e blocca tutto
                risposta.setrBoolean(false);
                risposta.setDsErr("[BLOB] Errore: " + ex.getMessage());
                log.error("Eccezione nel calcolo dell'hash del componente ", ex);
                tmpReturn = false;
            }
        }

        if (tmpReturn) {
            try {
                // procedo alla memorizzazione del file sul blob, via JDBC
                WriteCompBlbOracle.DatiAccessori datiAccessori = new WriteCompBlbOracle().new DatiAccessori();
                datiAccessori.setTabellaBlob(tabellaBlob);
                datiAccessori.setIdPadre(compntEntity.getIdCompDoc());
                datiAccessori.setIdStruttura(compntEntity.getIdStrut().longValue());
                datiAccessori.setAaKeyUnitaDoc(chiave.getAnno());
                datiAccessori.setDtVersamento(strutV.getDataVersamento());

                RispostaControlli tmpControlli = writeCompBlbOracle.salvaStreamSuBlobComp(datiAccessori, tmpFb);
                if (!tmpControlli.isrBoolean()) {
                    risposta.setDsErr(tmpControlli.getDsErr());
                    tmpReturn = false;
                }
            } catch (Exception re) {
                /// logga l'errore e blocca tutto
                log.error("Eccezione nella copia del file per il  salvataggio su blob ", re);
                risposta.setDsErr(re.getMessage());
                tmpReturn = false;
            }
        }

        if (tmpReturn) {
            try {
                entityManager.merge(compntEntity);
                // salvo AroCompDoc aggiornato
                entityManager.flush();
            } catch (Exception ex) {
                risposta.setrBoolean(false);
                risposta.setDsErr(ex.getMessage());
                log.error("Eccezione nella persistenza del contenuto comp ", ex);
                tmpReturn = false;
            }
        }

        return tmpReturn;
    }

    private boolean aggiungiBlobSuFile(RispostaControlli risposta, ComponenteVers componente, AroCompDoc compntEntity,
            StrutturaVersamento strutV) {
        boolean tmpReturn = true;
        FileBinario tmpFb;
        byte[] hash;

        if (componente.getTipoSupporto() != ComponenteVers.TipiSupporto.FILE) {
            return tmpReturn;
        }

        tmpFb = componente.getRifFileBinario();
        if (tmpFb.isInMemoria()) {
            risposta.setrBoolean(false);
            risposta.setDsErr("[TIVOLI] Errore: non è possibile archiviare su nastro un file gestito in memoria.");
            tmpReturn = false;
        }

        if (tmpReturn) {
            try (FileInputStream fis = new FileInputStream(tmpFb.getFileSuDisco())) {
                if (componente.getHashVersatoAlgoritmo() != null) {
                    compntEntity.setDsAlgoHashFileVers(componente.getHashVersatoAlgoritmo().descrivi());
                }
                if (componente.getHashVersatoEncoding() != null) {
                    compntEntity.setCdEncodingHashFileVers(componente.getHashVersatoEncoding().descrivi());
                }

                if (componente.isHashForzato()) {
                    // se l'hash è forzato, via versamento Multimedia
                    hash = componente.getHashCalcolato();
                    compntEntity.setDsHashFileCalc(BinEncUtility.encodeUTF8HexString(hash));
                    compntEntity.setFlNoCalcHashFile("1");
                    if (componente.isHashVersNonDefinito()) {
                        compntEntity.setTiEsitoContrHashVers(CostantiDB.TipiEsitoVerificaHash.NEGATIVO.name());
                    } else {
                        compntEntity.setTiEsitoContrHashVers(CostantiDB.TipiEsitoVerificaHash.NON_EFFETTUATO.name());
                    }
                } else if (componente.getHashCalcolato() != null && componente.getHashCalcolato().length > 0) {
                    // se l'hash è stato calcolato, in seguito alla verifica hash
                    hash = componente.getHashCalcolato();
                    compntEntity.setDsHashFileCalc(BinEncUtility.encodeUTF8HexString(hash));
                    compntEntity.setFlNoCalcHashFile("0");
                } else {
                    // se lo devo calcolare (verifica disattivata, sottocomponenti, componenti con
                    // hash versato non decifrabile)
                    hash = new HashCalculator().calculateSHAX(fis, TipiHash.SHA_256).getHashCalcolato();
                    componente.setHashCalcolato(hash);
                    compntEntity.setDsHashFileCalc(BinEncUtility.encodeUTF8HexString(hash));
                    compntEntity.setFlNoCalcHashFile("0");
                    if (componente.isHashVersNonDefinito()) {
                        compntEntity.setTiEsitoContrHashVers(CostantiDB.TipiEsitoVerificaHash.NEGATIVO.name());
                    } else {
                        compntEntity.setTiEsitoContrHashVers(CostantiDB.TipiEsitoVerificaHash.DISABILITATO.name());
                    }
                }

                // scrivo su compntEntity il path completo del file da salvare su tivoli, con il
                // percorso relativo al filesystem stesso
                String tmpFilePath = MessaggiWSFormat.formattaFilePathArk(strutV.getTpiRootTpi(),
                        strutV.getTpiRootArkVers(), strutV.getSubPathDataVers(), strutV.getSubPathVersatoreArk(),
                        strutV.getSubPathUnitaDocArk(), componente.getNomeFileArk());
                compntEntity.setDsNomeFileArk(tmpFilePath);

                // in ogni caso algoritmo ed encoding sono considerati costanti
                // compntEntity.setDsAlgoHashFileCalc(CostantiDB.TipiHash.SHA_1.descrivi());
                compntEntity.setDsAlgoHashFileCalc(CostantiDB.TipiHash.SHA_256.descrivi());
                compntEntity.setCdEncodingHashFileCalc(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi());

                if (componente.getMySottoComponente() == null) {
                    // aggiorno l'esito componente
                    // salvo nell'esito del componente il valore dell'hash calcolato
                    componente.getRifComponenteResp().setHash(componente.getHashCalcolato());
                    componente.getRifComponenteResp().setAlgoritmoHash(compntEntity.getDsAlgoHashFileCalc());
                    componente.getRifComponenteResp().setEncoding(compntEntity.getCdEncodingHashFileCalc());
                } else {
                    // aggiorno l'esito sottocomponente
                    // salvo nell'esito del sottocomponente il valore dell'hash calcolato
                    componente.getRifSottoComponenteResp().setHash(componente.getHashCalcolato());
                    componente.getRifSottoComponenteResp().setAlgoritmoHash(compntEntity.getDsAlgoHashFileCalc());
                    componente.getRifSottoComponenteResp().setEncoding(compntEntity.getCdEncodingHashFileCalc());
                }
            } catch (IOException | NoSuchAlgorithmException ex) {
                // fai qualcosa per segnalare il problema...
                /// logga l'errore e blocca tutto
                risposta.setrBoolean(false);
                risposta.setDsErr("[TIVOLI] Errore: " + ex.getMessage());
                log.error("Eccezione nel calcolo dell'hash del componente ", ex);
                tmpReturn = false;
            }
        }

        if (tmpReturn) {
            try {
                // procedo alla memorizzazione del file sul file system Tivoli
                RispostaControlli tmpControlli = salvataggioCompFS.copiaCompSuFS(strutV, componente);
                if (!tmpControlli.isrBoolean()) {
                    risposta.setDsErr(tmpControlli.getDsErr());
                    tmpReturn = false;
                }
            } catch (Exception re) {
                /// logga l'errore e blocca tutto
                log.error("Eccezione nella copia del file per il  salvataggio su filesystem ", re);
                risposta.setDsErr(re.getMessage());
                tmpReturn = false;
            }
        }

        if (tmpReturn) {
            try {
                entityManager.merge(compntEntity);
                // salvo AroCompDoc aggiornato
                entityManager.flush();
            } catch (Exception ex) {
                risposta.setrBoolean(false);
                risposta.setDsErr(ex.getMessage());
                log.error("Eccezione nella persistenza del contenuto comp ", ex);
                tmpReturn = false;
            }
        }

        return tmpReturn;
    }

    private boolean salvaDatiSpecCompUD(StrutturaVersamento strutturaVersIn) {
        boolean tmpReturn = true;
        TipiEntitaSacer tmpEntitaSacer;

        for (DocumentoVers valueDocVers : strutturaVersIn.getDocumentiAttesi()) {
            for (ComponenteVers tmpCV : valueDocVers.getFileAttesi()) {
                if (tmpCV.getMySottoComponente() == null) {
                    // sto elaborando un componente
                    tmpEntitaSacer = TipiEntitaSacer.COMP;
                } else {
                    // sto elaboranso un sottocomponente
                    tmpEntitaSacer = TipiEntitaSacer.SUB_COMP;
                }

                tmpReturn = this.salvaDatiSpecGen(CostantiDB.TipiUsoDatiSpec.VERS, tmpEntitaSacer, tmpCV.getIdRecDB(),
                        strutturaVersIn.getIdStruttura(), tmpCV.getIdRecXsdDatiSpec(), tmpCV.getDatiSpecifici(),
                        strutturaVersIn.getIdUnitaDoc());
                if (!tmpReturn) {
                    return false;
                }

                tmpReturn = this.salvaDatiSpecGen(CostantiDB.TipiUsoDatiSpec.MIGRAZ, tmpEntitaSacer, tmpCV.getIdRecDB(),
                        strutturaVersIn.getIdStruttura(), tmpCV.getIdRecXsdDatiSpecMigrazione(),
                        tmpCV.getDatiSpecificiMigrazione(), strutturaVersIn.getIdUnitaDoc());
                if (!tmpReturn) {
                    return false;
                }
            }
        }

        return tmpReturn;
    }

    private boolean salvaDatiSpecGen(CostantiDB.TipiUsoDatiSpec tipoUso, TipiEntitaSacer tipoEntity, long idEntity,
            long idStrut, long idXsd, HashMap<String, DatoSpecifico> datiSpecifici, long idAroUnitaDoc) {
        boolean tmpReturn = true;
        AroUsoXsdDatiSpec tmpAroUsoXsdDatiSpec = null;
        AroValoreAttribDatiSpec tmpAroValoreAttribDatiSpec = null;

        // salvo dati specifici, tabelle nuove
        if (datiSpecifici != null) {
            // salvo il record relativo all'xsd usato per validare i dati specifici
            tmpAroUsoXsdDatiSpec = new AroUsoXsdDatiSpec();
            tmpAroUsoXsdDatiSpec.setTiUsoXsd(tipoUso.name());
            tmpAroUsoXsdDatiSpec.setTiEntitaSacer(tipoEntity.name());
            //
            tmpAroUsoXsdDatiSpec.setAroUnitaDoc(entityManager.find(AroUnitaDoc.class, idAroUnitaDoc));
            //
            switch (tipoEntity) {
            case UNI_DOC:
                tmpAroUsoXsdDatiSpec.setAroUnitaDoc(entityManager.find(AroUnitaDoc.class, idEntity));
                break;
            case DOC:
                tmpAroUsoXsdDatiSpec.setAroDoc(entityManager.find(AroDoc.class, idEntity));
                break;
            case COMP:
                tmpAroUsoXsdDatiSpec.setAroCompDoc(entityManager.find(AroCompDoc.class, idEntity));
                break;
            case SUB_COMP:
                tmpAroUsoXsdDatiSpec.setAroCompDoc(entityManager.find(AroCompDoc.class, idEntity));
                break;
            }

            tmpAroUsoXsdDatiSpec.setIdStrut(new BigDecimal(idStrut));
            tmpAroUsoXsdDatiSpec.setDecXsdDatiSpec(entityManager.find(DecXsdDatiSpec.class, idXsd));

            // inserisco su DB
            try {
                entityManager.persist(tmpAroUsoXsdDatiSpec);
                entityManager.flush();
            } catch (RuntimeException re) {
                /// logga l'errore e blocca tutto
                log.error("Eccezione nella persistenza dei dati specifici ", re);
                tmpReturn = false;
            }

            // salvo i valori dei dati specifici
            if (tmpReturn) {
                for (DatoSpecifico tmpDS : datiSpecifici.values()) {
                    tmpAroValoreAttribDatiSpec = new AroValoreAttribDatiSpec();
                    tmpAroValoreAttribDatiSpec.setAroUsoXsdDatiSpec(tmpAroUsoXsdDatiSpec);
                    tmpAroValoreAttribDatiSpec.setIdStrut(new BigDecimal(idStrut));
                    tmpAroValoreAttribDatiSpec
                            .setDecAttribDatiSpec(entityManager.find(DecAttribDatiSpec.class, tmpDS.getIdDatoSpec()));
                    tmpAroValoreAttribDatiSpec.setDlValore(tmpDS.getValore());
                    // inserisco su DB
                    try {
                        entityManager.persist(tmpAroValoreAttribDatiSpec);
                        entityManager.flush();
                    } catch (RuntimeException re) {
                        /// logga l'errore e blocca tutto
                        log.error("Eccezione nella persistenza dei dati specifici ", re);
                        tmpReturn = false;
                        break;
                    }
                }
            }
        }

        return tmpReturn;
    }

    /**
     * Salva i dati di aggregazione settando gli opportuni indicatori
     *
     *
     * @param strutturaVersIn
     * 
     * @return
     */
    private boolean salvaDatiAggregazioni(StrutturaVersamento strutV) {
        boolean tmpReturn = true;
        // 1. il sistema aggiorna la versione serie settando l’indicatore che segnala
        // che la serie deve essere ricalcolata a causa di aggiornamento
        // (vedi vista SER_V_LIS_VERSER_BY_UPD_UD)
        List<BigDecimal> idVerSeries = this.retrieveSerVLisVerserByUpdUd(strutV.getIdUnitaDoc());
        for (BigDecimal idVerSerie : idVerSeries) {
            SerVerSerie verSerie = entityManager.find(SerVerSerie.class, idVerSerie.longValue());
            verSerie.setFlUpdModifUnitaDoc("1");
        }

        // 2. il sistema aggiorna il fascicolo settando l’indicatore che segnala che la
        // serie deve essere ricalcolata a causa di aggiornamento
        // metadati o aggiunta documento di almeno una unità documentaria
        // (vedi vista FAS_V_LIS_FASC_BY_UPD_UD)
        List<BigDecimal> idFascs = this.retrieveFasVLisFascByUpdUd(strutV.getIdUnitaDoc());
        for (BigDecimal idFasc : idFascs) {
            FasFascicolo fasFascicolo = entityManager.find(FasFascicolo.class, idFasc.longValue());
            fasFascicolo.setFlUpdModifUnitaDoc("1");
        }

        // flush all insert
        try {
            entityManager.flush();
        } catch (RuntimeException re) {
            /// logga l'errore e blocca tutto
            log.error("Eccezione nella persistenza dei dati di aggregazioni ", re);
            tmpReturn = false;
        }

        return tmpReturn;
    }

    /*
     * il sistema determina le versioni serie correnti con stato = CONTROLLATA o VALIDAZIONE_IN_CORSO o VALIDATA o
     * DA_FIRMARE o FIRMATA o IN_CUSTODIA, oppure con stato = DA_CONTROLLARE e contenuto effettivo con stato =
     * CONTROLLO_CONSIST_IN_CORSO, nel cui contenuto di tipo EFFETTIVO sia presente l’unità documentaria in
     * aggiornamento
     * 
     * return list of idVerSerie
     */
    private List<BigDecimal> retrieveSerVLisVerserByUpdUd(long idUnitaDoc) {
        Query query = entityManager
                .createQuery("SELECT ser.idVerSerie FROM SerVLisVerserByUpdUd ser WHERE ser.idUnitaDoc = :idUnitaDoc ");
        query.setParameter("idUnitaDoc", idUnitaDoc);
        List<BigDecimal> serie = query.getResultList();
        return serie;
    }

    /*
     * il sistema determina i fascicoli con stato nell’elenco = IN_ELENCO_IN_CODA_CREAZIONE_AIP o
     * IN_ELENCO_CON_AIP_CREATO o IN_ELENCO_CON_ELENCO_INDICI_AIP_CREATO o IN_ELENCO_COMPLETATO nel cui contenuto e’
     * presente l’unità documentaria in aggiornamento
     * 
     * return list of idFascicolo
     */
    private List<BigDecimal> retrieveFasVLisFascByUpdUd(long idUnitaDoc) {
        Query query = entityManager
                .createQuery("SELECT ser.idFascicolo FROM FasVLisFascByUpdUd ser WHERE ser.idUnitaDoc = :idUnitaDoc ");
        query.setParameter("idUnitaDoc", idUnitaDoc);
        List<BigDecimal> fascicoli = query.getResultList();
        return fascicoli;
    }

    /**
     * Restituisce il valore del progressivo versione indice AIP di tipo UNISINCRO
     *
     * @param idUnitaDoc
     * 
     * @return il progressivo versione oppure 0 se questo ancora non esiste
     */
    private int recuperaProgressivoVersione(Long idUnitaDoc) {
        List<AroVerIndiceAipUd> aroVerIndiceAipList;
        String queryStr = "SELECT u FROM AroVerIndiceAipUd u "
                + "WHERE u.aroIndiceAipUd.aroUnitaDoc.idUnitaDoc = :idUnitaDoc "
                + "AND u.aroIndiceAipUd.tiFormatoIndiceAip = 'UNISYNCRO' " + "ORDER BY u.pgVerIndiceAip DESC ";
        javax.persistence.Query query = entityManager.createQuery(queryStr);
        query.setParameter("idUnitaDoc", idUnitaDoc);
        aroVerIndiceAipList = (List<AroVerIndiceAipUd>) query.getResultList();
        if (aroVerIndiceAipList != null && !aroVerIndiceAipList.isEmpty()) {
            return aroVerIndiceAipList.get(0).getPgVerIndiceAip().intValue();
        } else {
            return 0;
        }
    }

    private class ConfigPerDoc {

        private String forzaAccettazione;
        private String forzaConservazione;
        private String tipoConservazione;
        private String sistemaDiMigrazione;
        private CostantiDB.TipoCreazioneDoc tipoCreazioneDoc;

        public String getForzaAccettazione() {
            return forzaAccettazione;
        }

        public void setForzaAccettazione(String forzaAccettazione) {
            this.forzaAccettazione = forzaAccettazione;
        }

        public String getForzaConservazione() {
            return forzaConservazione;
        }

        public void setForzaConservazione(String forzaConservazione) {
            this.forzaConservazione = forzaConservazione;
        }

        public String getSistemaDiMigrazione() {
            return sistemaDiMigrazione;
        }

        public void setSistemaDiMigrazione(String sistemaDiMigrazione) {
            this.sistemaDiMigrazione = sistemaDiMigrazione;
        }

        public String getTipoConservazione() {
            return tipoConservazione;
        }

        public void setTipoConservazione(String tipoConservazione) {
            this.tipoConservazione = tipoConservazione;
        }

        public TipoCreazioneDoc getTipoCreazioneDoc() {
            return tipoCreazioneDoc;
        }

        public void setTipoCreazioneDoc(TipoCreazioneDoc tipoCreazioneDoc) {
            this.tipoCreazioneDoc = tipoCreazioneDoc;
        }

    }

}
