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

import it.eng.parer.entity.*;
import it.eng.parer.entity.constraint.AroCompUrnCalc.TiUrn;
import it.eng.parer.entity.constraint.VrsUrnXmlSessioneVers.TiUrnXmlSessioneVers;
import it.eng.parer.view_entity.VrsVLisXmlDocUrnDaCalc;
import it.eng.parer.view_entity.VrsVLisXmlUdUrnDaCalc;
import it.eng.parer.view_entity.VrsVLisXmlUpdUrnDaCalc;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.Costanti.CategoriaDocumento;
import it.eng.parer.ws.utils.CostantiDB.TipiXmlDati;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;

/**
 *
 * @author sinatti_s
 * 
 *         Gestione salvataggio del pregresso su entity Aro
 * 
 */
@SuppressWarnings("unchecked")
@Stateless(mappedName = "SalvataggioPregVersamentoAroHelper")
@LocalBean
public class SalvataggioPregVersamentoAroHelper extends SalvataggioUpdVersamentoBaseHelper {

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli salvaCdKeyNormUnitaDocumentaria(AroUnitaDoc tmpAroUnitaDoc, StrutturaUpdVers svf) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(true); // default true solo se effettua effettivamente l'update può fallire

        try {
            if (StringUtils.isBlank(tmpAroUnitaDoc.getCdKeyUnitaDocNormaliz())) {
                tmpAroUnitaDoc.setCdKeyUnitaDocNormaliz(svf.getCdKeyNormalized());
                //
                entityManager.flush();
            }
        } catch (Exception e) {
            tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
            tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di aggiornamento dati pregressi " + e.getMessage()));
            tmpRispostaControlli.setrBoolean(false);
            getLogger().error("Errore interno nella fase di aggiornamento dati pregressi chiave normalizzata ud", e);
        }

        return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli aggiornaPregCompDocUrnSip(AroUnitaDoc tmpAroUnitaDoc, StrutturaUpdVers svf) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(true); // default true solo se effettua effettivamente l'update può fallire

        // A. check data massima versamento recuperata in precedenza rispetto parametro
        // su db
        if (svf.getDtVersMax().before(svf.getDtInizioCalcoloNewUrn())
                || svf.getDtVersMax().equals(svf.getDtInizioCalcoloNewUrn())) {
            try {
                // B. eseguo registra urn comp pregressi
                this.scriviUrnCompPreg(tmpAroUnitaDoc, svf);
                // C. eseguo registra urn sip pregressi
                // C.1. eseguo registra urn sip pregressi ud
                this.scriviUrnSipUdPreg(tmpAroUnitaDoc, svf);
                // C.2. eseguo registra urn sip pregressi documenti aggiunti
                this.scriviUrnSipDocAggPreg(tmpAroUnitaDoc, svf);
                // C.3. eseguo registra urn pregressi upd
                this.scriviUrnSipUpdPreg(tmpAroUnitaDoc, svf);
                //
                entityManager.flush();
            } catch (Exception e) {
                tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
                tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
                        "Errore interno nella fase di aggiornamento dati pregressi " + e.getMessage()));
                tmpRispostaControlli.setrBoolean(false);
                getLogger().error("Errore interno nella fase di aggiornamento dati pregressi sip ud/doc/upd", e);
            }
        }

        return tmpRispostaControlli;
    }

    private void scriviUrnCompPreg(AroUnitaDoc tmpAroUnitaDoc, StrutturaUpdVers svf) {
        // calcolo (se necessario) niOrdDoc secondo la logica di ordinamento
        this.getAndSetAroDocOrderedByTypeAndDateProg(tmpAroUnitaDoc);
        // per ogni documento
        for (AroDoc tmpAroDoc : tmpAroUnitaDoc.getAroDocs()) {
            // per ogni componente
            for (Iterator<AroStrutDoc> it = tmpAroDoc.getAroStrutDocs().iterator(); it.hasNext();) {
                AroStrutDoc tmpAroStrutDoc = it.next();
                for (AroCompDoc tmpAroCompDoc : tmpAroStrutDoc.getAroCompDocs()) {
                    //
                    this.salvaURNComponente(svf, tmpAroDoc, tmpAroCompDoc,
                            Arrays.asList(TiUrn.ORIGINALE, TiUrn.NORMALIZZATO, TiUrn.INIZIALE));

                }
            }
        }
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
            Collections.sort(alNew, (doc1, doc2) -> {
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

    private void salvaURNComponente(StrutturaUpdVers svf, AroDoc tmpAroDoc, AroCompDoc tmpAroCompDoc,
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
                    tmpUrn = MessaggiWSFormat.formattaBaseUrnDoc(svf.getUrnPartVersatore(), svf.getUrnPartChiaveUd(),
                            tmpUrnDoc, Costanti.UrnFormatter.URN_COMP_FMT_STRING);
                    break;
                case NORMALIZZATO:
                    String urnPartVersatoreNorm = MessaggiWSFormat.formattaUrnPartVersatore(
                            svf.getVersatoreNonverificato(), true, Costanti.UrnFormatter.VERS_FMT_STRING);
                    String urnPartChiaveUdNorm = MessaggiWSFormat.formattaUrnPartUnitaDoc(svf.getChiaveNonVerificata(),
                            true, Costanti.UrnFormatter.UD_FMT_STRING);
                    tmpUrn = MessaggiWSFormat.formattaBaseUrnDoc(urnPartVersatoreNorm, urnPartChiaveUdNorm, tmpUrnDoc,
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

    private void salvaCompUrnCalc(AroCompDoc aroCompDoc, String tmpUrn, TiUrn tiUrn) {
        AroCompUrnCalc tmpTabCDUrnComponenteCalc = null;

        tmpTabCDUrnComponenteCalc = new AroCompUrnCalc();
        tmpTabCDUrnComponenteCalc.setAroCompDoc(aroCompDoc);
        tmpTabCDUrnComponenteCalc.setDsUrn(tmpUrn);
        tmpTabCDUrnComponenteCalc.setTiUrn(tiUrn);

        // persist
        entityManager.persist(tmpTabCDUrnComponenteCalc);

        aroCompDoc.getAroAroCompUrnCalcs().add(tmpTabCDUrnComponenteCalc);

    }

    private void scriviUrnSipUdPreg(AroUnitaDoc tmpAroUnitaDoc, StrutturaUpdVers svf) {

        List<VrsVLisXmlUdUrnDaCalc> vrsVLisXmlUdUrnDaCalcs = this
                .retrieveVrsVLisXmlUdUrnDaCalcByUd(tmpAroUnitaDoc.getIdUnitaDoc());

        for (VrsVLisXmlUdUrnDaCalc vrs : vrsVLisXmlUdUrnDaCalcs) {

            //
            VrsXmlDatiSessioneVers xmlDatiSessioneVers = entityManager.find(VrsXmlDatiSessioneVers.class,
                    vrs.getIdXmlDatiSessioneVers().longValue());

            // calcolo parte urn ORIGINALE
            String tmpUrn = MessaggiWSFormat.formattaBaseUrnUnitaDoc(svf.getUrnPartVersatore(),
                    svf.getUrnPartChiaveUd());
            // calcolo parte urn NORMALIZZATO
            String tmpUrnNorm = MessaggiWSFormat.formattaBaseUrnUnitaDoc(
                    MessaggiWSFormat.formattaUrnPartVersatore(svf.getVersatoreNonverificato(), true,
                            Costanti.UrnFormatter.VERS_FMT_STRING),
                    MessaggiWSFormat.formattaUrnPartUnitaDoc(svf.getChiaveNonVerificata(), true,
                            Costanti.UrnFormatter.UD_FMT_STRING));
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
    }

    private List<VrsVLisXmlUdUrnDaCalc> retrieveVrsVLisXmlUdUrnDaCalcByUd(long idUnitaDoc) {
        Query query = entityManager
                .createQuery("SELECT vrs FROM VrsVLisXmlUdUrnDaCalc vrs WHERE vrs.idUnitaDoc = :idUnitaDoc ");
        query.setParameter("idUnitaDoc", new BigDecimal(idUnitaDoc));
        return query.getResultList();
    }

    private void scriviUrnSipDocAggPreg(AroUnitaDoc tmpAroUnitaDoc, StrutturaUpdVers svf) {
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
                String tmpUrnNorm = MessaggiWSFormat.formattaBaseUrnDoc(
                        MessaggiWSFormat.formattaUrnPartVersatore(svf.getVersatoreNonverificato(), true,
                                Costanti.UrnFormatter.VERS_FMT_STRING),
                        MessaggiWSFormat.formattaUrnPartUnitaDoc(svf.getChiaveNonVerificata(), true,
                                Costanti.UrnFormatter.UD_FMT_STRING),
                        tmpUrnPartDoc, Costanti.UrnFormatter.URN_DOC_FMT_STRING_V2);
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

    public List<VrsVLisXmlDocUrnDaCalc> retrieveVrsVLisXmlDocUrnDaCalcByDoc(long idDoc) {
        Query query = entityManager.createQuery("SELECT vrs FROM VrsVLisXmlDocUrnDaCalc vrs WHERE vrs.idDoc = :idDoc ");
        query.setParameter("idDoc", new BigDecimal(idDoc));
        return query.getResultList();
    }

    private void scriviUrnSipUpdPreg(AroUnitaDoc tmpAroUnitaDoc, StrutturaUpdVers svf) {
        //
        List<AroUpdUnitaDoc> aroUpdUnitaDocs = retrieveAroUpdUnitaDocByUd(tmpAroUnitaDoc.getIdUnitaDoc());
        // per ogni aggiornamento metadati
        for (AroUpdUnitaDoc updUnitaDoc : aroUpdUnitaDocs) {
            //
            List<VrsVLisXmlUpdUrnDaCalc> vrsVLisXmlUpdUrnDaCalc = retrieveVrsVLisXmlUpdUrnDaCalcByUpd(
                    updUnitaDoc.getIdUpdUnitaDoc());
            // per ogni VrsVLisXmlUpdUrnDaCalc
            for (VrsVLisXmlUpdUrnDaCalc vrs : vrsVLisXmlUpdUrnDaCalc) {
                AroXmlUpdUnitaDoc aroXmlUpdUnitaDoc = entityManager.find(AroXmlUpdUnitaDoc.class,
                        vrs.getIdXmlUpdUnitaDoc().longValue());

                // calcolo parte urn ORIGINALE
                String tmpUrn = MessaggiWSFormat.formattaBaseUrnUpdUnitaDoc(svf.getUrnPartVersatore(),
                        svf.getUrnPartChiaveUd(), updUnitaDoc.getPgUpdUnitaDoc().longValue(), true,
                        Costanti.UrnFormatter.UPD_FMT_STRING_V3, Costanti.UrnFormatter.PAD5DIGITS_FMT);

                // calcolo parte urn NORMALIZZATO
                String tmpUrnNorm = MessaggiWSFormat.formattaBaseUrnUpdUnitaDoc(
                        MessaggiWSFormat.formattaUrnPartVersatore(svf.getVersatoreNonverificato(), true,
                                Costanti.UrnFormatter.VERS_FMT_STRING),
                        MessaggiWSFormat.formattaUrnPartUnitaDoc(svf.getChiaveNonVerificata(), true,
                                Costanti.UrnFormatter.UD_FMT_STRING),
                        updUnitaDoc.getPgUpdUnitaDoc().longValue(), true, Costanti.UrnFormatter.UPD_FMT_STRING_V3,
                        Costanti.UrnFormatter.PAD5DIGITS_FMT);

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
        return query.getResultList();
    }

    private List<VrsVLisXmlUpdUrnDaCalc> retrieveVrsVLisXmlUpdUrnDaCalcByUpd(long idUpdUnitaDoc) {
        Query query = entityManager
                .createQuery("SELECT vrs FROM VrsVLisXmlUpdUrnDaCalc vrs WHERE vrs.idUpdUnitaDoc = :idUpdUnitaDoc ");
        query.setParameter("idUpdUnitaDoc", new BigDecimal(idUpdUnitaDoc));
        return query.getResultList();
    }

    private void salvaUrnXmlSessioneVers(VrsXmlDatiSessioneVers xmlDatiSessioneVers, String tmpUrn,
            TiUrnXmlSessioneVers tiUrn) {

        VrsUrnXmlSessioneVers tmpVrsUrnXmlSessioneVers;

        tmpVrsUrnXmlSessioneVers = new VrsUrnXmlSessioneVers();
        tmpVrsUrnXmlSessioneVers.setDsUrn(tmpUrn);
        tmpVrsUrnXmlSessioneVers.setTiUrn(tiUrn);
        tmpVrsUrnXmlSessioneVers.setVrsXmlDatiSessioneVers(xmlDatiSessioneVers);

        // persist
        entityManager.persist(tmpVrsUrnXmlSessioneVers);

        xmlDatiSessioneVers.getVrsUrnXmlSessioneVers().add(tmpVrsUrnXmlSessioneVers);

    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(SalvataggioPregVersamentoAroHelper.class);
    }

}
