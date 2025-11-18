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

package it.eng.parer.ws.versamento.ejb;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.OrgStrut;
import it.eng.parer.firma.util.VerificaFirmaEnums.SacerIndication;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.AvanzamentoWs;
import it.eng.parer.ws.utils.Costanti.ModificatoriWS;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.ParametroApplDB.ParametroApplFl;
import it.eng.parer.ws.versamento.dto.DocumentoVers;
import it.eng.parer.ws.versamento.dto.RispostaWSAggAll;
import it.eng.parer.ws.versamento.dto.StrutturaVersamento;
import it.eng.parer.ws.versamento.dto.VersamentoExtAggAll;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.xml.versReq.ConfigAggAllType;
import it.eng.parer.ws.xml.versResp.ECEsitoPosNegWarDisType;
import it.eng.parer.ws.xml.versResp.ECEsitoPosNegWarType;
import it.eng.parer.ws.xml.versResp.EsitoVersAggAllegati;

@Stateless(mappedName = "VerificaFirmeHashAggAll")
@LocalBean
public class VerificaFirmeHashAggAll {

    private static final Logger log = LoggerFactory.getLogger(VerificaFirmeHashAggAll.class);
    // EJB
    @EJB
    private ControlliPerFirme controlliPerFirme;
    @EJB
    private ConfigurationHelper configurationHelper;
    @EJB
    private DocumentoVersVFirmeHash myDocumentoVersVFirme;

    public void controlloFirmeMarcheHash(VersamentoExtAggAll versamento,
            RispostaWSAggAll rispostaWs) {
        // Richiamo l'istanza dell'esito generale
        EsitoVersAggAllegati myEsito = rispostaWs.getIstanzaEsito();
        StrutturaVersamento strutV = versamento.getStrutturaComponenti();
        AvanzamentoWs myAvanzamentoWs = rispostaWs.getAvanzamento();

        boolean verFirmeDocCodiceEsitoNeg = false;
        boolean verFirmeDocCodiceEsitoWar = false;
        boolean esitoDocHasWar = false;
        boolean esitoDocHasNeg = false;

        OrgStrut os = null;

        myAvanzamentoWs.setFase("Inizializzazione...").logAvanzamento();

        // §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
        // inizializzazione verifica firme del documento
        // NB: può fallire, quindi passo RispostaWS che può tornare in stato di errore
        // §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            RispostaControlli rispostaControlli = controlliPerFirme
                    .getOrgStrutt(strutV.getIdStruttura());
            if (rispostaControlli.getrLong() != -1) {
                os = (OrgStrut) rispostaControlli.getrObject();
            } else {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                        "Errore nel recupero della tabella di decodifica OrgStrut");
                log.error("Errore nel recupero della tabella di decodifica OrgStrut");
            }
        }

        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            //
            elabFlagSipVDB(os, versamento);
            //
            buildFlEsito(myEsito, os.getIdStrut(), os.getOrgEnte().getOrgAmbiente().getIdAmbiente(),
                    versamento.getStrutturaComponenti().getIdTipologiaUnitaDocumentaria(),
                    versamento);

            // Scorro i documenti
            /*
             * Nota bene: sono perfettamente a conoscenza del fatto che questa lista è composta da
             * un solo elemento e che sarebbe bastato impostare valueDocVers al primo elemento della
             * collection DocumentiAttesi. Ho deciso di scrivere così il codice per mantenere la
             * stessa struttura della verifica firme del versamento, così da rendere più evidenti e
             * facili le inevitabili operazioni di manutenzione e correzione. ff
             */
            //
            myAvanzamentoWs.setFase("Verifica documento  - inizio").logAvanzamento();
            for (DocumentoVers valueDocVers : strutV.getDocumentiAttesi()) {
                // init non firmato
                valueDocVers.setFlFileFirmato(CostantiDB.Flag.FALSE);
                // verifica firma
                if (versamento.getStrutturaComponenti().effettuaVerificaFirma()
                        && myDocumentoVersVFirme.verificaDocumentoGenFirma(valueDocVers,
                                versamento.getStrutturaComponenti()
                                        .getIdTipologiaUnitaDocumentaria(),
                                versamento, rispostaWs, myEsito.getConfigurazione())) {
                    // se non si sono verificati errori catastrofici, aggiorno alcune variabili
                    // interne
                    if (valueDocVers.getFlFileFirmato().equals(CostantiDB.Flag.TRUE)) {
                        myEsito.getUnitaDocumentaria().setFirmatoDigitalmente(true);
                        if (valueDocVers.getRifDocumentoResp().getEsitoDocumento()
                                .getVerificaFirmeDocumento() == ECEsitoPosNegWarType.NEGATIVO) {
                            verFirmeDocCodiceEsitoNeg = true;
                        } else if (valueDocVers.getRifDocumentoResp().getEsitoDocumento()
                                .getVerificaFirmeDocumento() == ECEsitoPosNegWarType.WARNING) {
                            verFirmeDocCodiceEsitoWar = true;
                        }
                    }
                    // codice esito
                    if (valueDocVers.getRifDocumentoResp().getEsitoDocumento()
                            .getCodiceEsito() == ECEsitoPosNegWarType.NEGATIVO) {
                        esitoDocHasNeg = true;
                    } else if (valueDocVers.getRifDocumentoResp().getEsitoDocumento()
                            .getCodiceEsito() == ECEsitoPosNegWarType.WARNING) {
                        esitoDocHasWar = true;
                    }
                }

                // verifica hash
                myDocumentoVersVFirme.verificaDocumentoGenHash(valueDocVers, versamento, rispostaWs,
                        myEsito.getConfigurazione());

                // codice esito (verifica se negativo in precedenza)
                if (!esitoDocHasNeg) {
                    if (valueDocVers.getRifDocumentoResp().getEsitoDocumento()
                            .getCodiceEsito() == ECEsitoPosNegWarType.NEGATIVO) {
                        esitoDocHasNeg = true;
                    } else if (valueDocVers.getRifDocumentoResp().getEsitoDocumento()
                            .getCodiceEsito() == ECEsitoPosNegWarType.WARNING) {
                        esitoDocHasWar = true;
                    }
                }
            }
            myAvanzamentoWs.resetFase().setFase("Verifica documento  - fine").logAvanzamento();
            if (!versamento.getStrutturaComponenti().effettuaVerificaFirma()) {
                // versione 1.5
                if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_FIRMA_1_5)) {
                    versamento.getStrutturaComponenti()
                            .setTiEsitoVerifFirme(SacerIndication.NON_ESEGUITO.name());
                    versamento.getStrutturaComponenti()
                            .setDsMsgEsitoVerifica(SacerIndication.NON_ESEGUITO.message());
                    //
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setVerificaFirmeUnitaDocumentaria(
                                    ECEsitoPosNegWarDisType.DISABILITATO);
                }
            } else {
                // VerificaFirmeUnitaDocumentaria
                Boolean bool = myEsito.getUnitaDocumentaria().isFirmatoDigitalmente();
                if (bool != null && bool) {
                    if (verFirmeDocCodiceEsitoNeg) {
                        versamento.getStrutturaComponenti()
                                .setTiEsitoVerifFirme(SacerIndication.NEGATIVO.name());
                        versamento.getStrutturaComponenti()
                                .setDsMsgEsitoVerifica(SacerIndication.NEGATIVO.message());
                        myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                                .setVerificaFirmeUnitaDocumentaria(
                                        ECEsitoPosNegWarDisType.NEGATIVO);
                    } else if (verFirmeDocCodiceEsitoWar) {
                        versamento.getStrutturaComponenti()
                                .setTiEsitoVerifFirme(SacerIndication.WARNING.name());
                        versamento.getStrutturaComponenti()
                                .setDsMsgEsitoVerifica(SacerIndication.WARNING.message());
                        myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                                .setVerificaFirmeUnitaDocumentaria(ECEsitoPosNegWarDisType.WARNING);
                    } else {
                        versamento.getStrutturaComponenti()
                                .setTiEsitoVerifFirme(SacerIndication.POSITIVO.name());
                        versamento.getStrutturaComponenti()
                                .setDsMsgEsitoVerifica(SacerIndication.POSITIVO.message());
                        myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                                .setVerificaFirmeUnitaDocumentaria(
                                        ECEsitoPosNegWarDisType.POSITIVO);
                    }
                }
            }

            /*
             * esito dell'unità documentaria in base alle verifiche di firme e formati.
             */
            if (versamento.getStrutturaComponenti().effettuaVerificaFirma()) {
                Boolean firmato = myEsito.getUnitaDocumentaria().isFirmatoDigitalmente();
                if (firmato != null && firmato) {
                    if (esitoDocHasNeg) {
                        myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                                .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                    } else if (esitoDocHasWar) {
                        myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                                .setCodiceEsito(ECEsitoPosNegWarType.WARNING);
                    }
                } else if (versamento.getStrutturaComponenti()
                        .isFlagAbilitaConservazioneNonFirmati()) {
                    boolean forzaWarning = versamento.getStrutturaComponenti()
                            .isConfigFlagForzaConservazione()
                            || versamento.getStrutturaComponenti()
                                    .isFlagAccettaConservazioneNonFirmatiNeg()
                                    && myEsito.getConfigurazione().isForzaAccettazione();
                    if (forzaWarning) { // Forza Conservazione
                        // "Non sono stati trovati componenti firmati digitalmente"
                        versamento.listErrAddWarningUd(
                                versamento.getStrutturaComponenti().getUrnPartChiaveUd(),
                                MessaggiWSBundle.UD_008_001,
                                versamento.getStrutturaComponenti().getUrnPartChiaveUd());
                    } else {
                        // "Non sono stati trovati componenti firmati digitalmente"
                        versamento.listErrAddErrorUd(
                                versamento.getStrutturaComponenti().getUrnPartChiaveUd(),
                                MessaggiWSBundle.UD_008_001,
                                versamento.getStrutturaComponenti().getUrnPartChiaveUd());
                    }
                }
            }
        }

        /*
         * esito del versamento, in funzione degi eventuali errori e warning trovati
         */
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            VoceDiErrore tmpVdE = versamento.calcolaErrorePrincipale();
            if (tmpVdE != null) {
                rispostaWs.setSeverity(tmpVdE.getSeverity());
                if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.NEGATIVO) {
                    rispostaWs.setEsitoWsError(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage());
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                } else if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.WARNING) {
                    rispostaWs.setEsitoWsWarning(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage());
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setCodiceEsito(ECEsitoPosNegWarType.WARNING);
                }
            }
        }
    }

    private void elabFlagSipVDB(OrgStrut os, VersamentoExtAggAll versamento) {
        long idStrut = os.getIdStrut();
        long idAmbiente = os.getOrgEnte().getOrgAmbiente().getIdAmbiente();
        long idTipoUd = versamento.getStrutturaComponenti().getIdTipologiaUnitaDocumentaria();
        //
        versamento.getStrutturaComponenti()
                .setFlagAbilitaVerificaFirma(configurationHelper
                        .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ABILITA_VERIFICA_FIRMA,
                                idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));
        //
        versamento.getStrutturaComponenti()
                .setFlagAbilitaVerificaFirmaSoloCrypto(
                        configurationHelper
                                .getValoreParamApplicByTipoUdAsFl(
                                        ParametroApplFl.FL_ABILITA_VERIFICA_FIRMA_SOLO_CRYPTO,
                                        idStrut, idAmbiente, idTipoUd)
                                .equals(CostantiDB.Flag.TRUE));
        //
        versamento.getStrutturaComponenti().setFlagAbilitaConservazioneNonFirmati(
                configurationHelper.getValoreParamApplicByTipoUdAsFl(
                        ParametroApplFl.FL_ABILITA_CONTR_NON_FIRMATI, idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));
        //
        versamento.getStrutturaComponenti().setFlagAccettaConservazioneNonFirmatiNeg(
                configurationHelper.getValoreParamApplicByTipoUdAsFl(
                        ParametroApplFl.FL_ACCETTA_CONTR_NON_FIRMATI_NEG, idStrut, idAmbiente,
                        idTipoUd).equals(CostantiDB.Flag.TRUE));
        /*
         * MAC #23544: La gestione dei parametri FORZA_COLLEGAMENTO (solo su versamento sincrono) e
         * FORZA_CONSERVAZIONE sono opzionali sul SIP. Nel caso non siano configurati sul SIP viene
         * utilizzato il valore impostato sul DB. A causa dell'errata impostazione del default su
         * VersamentoExtPrsr e VersamentoExtAggAllPrsr qui non posso utilizzare la configurazione
         * esposta da myesito.getConfigurazione() ma utilizzare il file xml originale, ovvero
         * versamento.getVersamento().getConfigurazione() I* Cerca lo stesso commento in
         * VersamentoExtPrsr, VersamentoExtAggAllPrsr e VerificaFirmeHash (tramite la stringa MAC
         * #23544)
         */
        ConfigAggAllType configurazioneXML = versamento.getVersamento().getConfigurazione();

        // SIP vs DB
        boolean forzaCons = false;
        if (configurazioneXML != null && configurazioneXML.isForzaConservazione() != null) {
            forzaCons = configurazioneXML.isForzaConservazione();
        } else {
            forzaCons = configurationHelper.getValoreParamApplicByTipoUdAsFl(
                    ParametroApplFl.FL_FORZA_CONTR_NON_FIRMATI_NEG, idStrut, idAmbiente, idTipoUd)
                    .equals(CostantiDB.Flag.TRUE);
        }
        versamento.getStrutturaComponenti().setConfigFlagForzaConservazione(forzaCons);

        // forzature controlli
        versamento.getStrutturaComponenti().setConfigFlagForzaControlloRevoca(configurationHelper
                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_FORZA_CONTR_REVOCA_VERS,
                        idStrut, idAmbiente, idTipoUd)
                .equals(CostantiDB.Flag.TRUE));

        versamento.getStrutturaComponenti()
                .setConfigFlagForzaControlloTrust(configurationHelper
                        .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_FORZA_CONTR_TRUST_VERS,
                                idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));

        versamento.getStrutturaComponenti().setConfigFlagForzaControlloCrittografico(
                configurationHelper.getValoreParamApplicByTipoUdAsFl(
                        ParametroApplFl.FL_FORZA_CONTR_CRITTOG_VERS, idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));

        versamento.getStrutturaComponenti().setConfigFlagForzaControlloCertificato(
                configurationHelper.getValoreParamApplicByTipoUdAsFl(
                        ParametroApplFl.FL_FORZA_CONTR_CERTIF_VERS, idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));

        versamento.getStrutturaComponenti()
                .setConfigFlagForzaControlloNonConformita(configurationHelper
                        .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_FORZA_CONTR_NOCONF,
                                idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));

        // revoca
        versamento.getStrutturaComponenti()
                .setFlagAbilitaControlloRevoca(configurationHelper.getValoreParamApplicByTipoUdAsFl(
                        ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS, idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));

        // ocsp
        versamento.getStrutturaComponenti()
                .setFlagAccettaControlloOCSPNegativo(configurationHelper
                        .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_CONTR_OCSP_NEG,
                                idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));

        versamento.getStrutturaComponenti().setFlagAccettaControlloOCSPNoScaric(configurationHelper
                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_CONTR_OCSP_NOSCAR,
                        idStrut, idAmbiente, idTipoUd)
                .equals(CostantiDB.Flag.TRUE));

        versamento.getStrutturaComponenti().setFlagAccettaControlloOCSPNoValido(configurationHelper
                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_CONTR_OCSP_NOVAL,
                        idStrut, idAmbiente, idTipoUd)
                .equals(CostantiDB.Flag.TRUE));

        versamento.getStrutturaComponenti()
                .setGenerazioneRerortVerificaFirma(configurationHelper.getValoreParamApplicByTipoUd(
                        ParametroApplFl.GENERAZIONE_REPORT_VERIFICA_FIRMA, idStrut, idAmbiente,
                        idTipoUd));
    }

    private void buildFlEsito(EsitoVersAggAllegati myEsito, long idStrut, long idAmbiente,
            long idTipoUd, VersamentoExtAggAll versamento) {
        // Imposto da DB i valori di configurazione prettamente legati alla verifica
        // firme/marche
        // pre versione 1.5
        if (!versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_FIRMA_1_5)) {
            // Nota: viene utilizzato il flag FL_ABILITA_CONTR_REVOCA_VERS sia per
            // setAbilitaControlloCRL che per
            // setAbilitaControlloRevoca
            // questo in quanto tale flag include sia le CRL che OCSP, cambia esclusivamente il tag
            // dell'esito
            myEsito.getConfigurazione()
                    .setAbilitaControlloCRL(configurationHelper.getValoreParamApplicByTipoUdAsFl(
                            ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS, idStrut, idAmbiente,
                            idTipoUd).equals(CostantiDB.Flag.TRUE));
        }
        myEsito.getConfigurazione().setAbilitaControlloCertificato(configurationHelper
                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ABILITA_CONTR_CERTIF_VERS,
                        idStrut, idAmbiente, idTipoUd)
                .equals(CostantiDB.Flag.TRUE));
        myEsito.getConfigurazione().setAbilitaControlloCrittografico(configurationHelper
                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ABILITA_CONTR_CRITTOG_VERS,
                        idStrut, idAmbiente, idTipoUd)
                .equals(CostantiDB.Flag.TRUE));
        myEsito.getConfigurazione()
                .setAbilitaControlloTrust(configurationHelper.getValoreParamApplicByTipoUdAsFl(
                        ParametroApplFl.FL_ABILITA_CONTR_TRUST_VERS, idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));
        myEsito.getConfigurazione()
                .setAccettaControlloCRLNegativo(configurationHelper
                        .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_CONTR_CRL_NEG,
                                idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));
        myEsito.getConfigurazione().setAccettaControlloCRLNoScaric(configurationHelper
                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_CONTR_CRL_NOSCAR,
                        idStrut, idAmbiente, idTipoUd)
                .equals(CostantiDB.Flag.TRUE));
        myEsito.getConfigurazione().setAccettaControlloCRLNoValida(configurationHelper
                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_CONTR_CRL_NOVAL,
                        idStrut, idAmbiente, idTipoUd)
                .equals(CostantiDB.Flag.TRUE));
        myEsito.getConfigurazione()
                .setAccettaControlloCRLScaduta(configurationHelper
                        .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_CONTR_CRL_SCAD,
                                idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));
        myEsito.getConfigurazione().setAccettaControlloCertificatoNoFirma(configurationHelper
                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_CONTR_CERTIF_NOCERT,
                        idStrut, idAmbiente, idTipoUd)
                .equals(CostantiDB.Flag.TRUE));
        myEsito.getConfigurazione().setAccettaControlloCertificatoNoValido(configurationHelper
                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_CONTR_CERTIF_NOVAL,
                        idStrut, idAmbiente, idTipoUd)
                .equals(CostantiDB.Flag.TRUE));
        myEsito.getConfigurazione().setAccettaControlloCertificatoScaduto(configurationHelper
                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_CONTR_CERTIF_SCAD,
                        idStrut, idAmbiente, idTipoUd)
                .equals(CostantiDB.Flag.TRUE));
        myEsito.getConfigurazione().setAccettaControlloCrittograficoNegativo(configurationHelper
                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_CONTR_CRITTOG_NEG,
                        idStrut, idAmbiente, idTipoUd)
                .equals(CostantiDB.Flag.TRUE));
        myEsito.getConfigurazione().setAccettaControlloTrustNegativo(configurationHelper
                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_CONTR_TRUST_NEG,
                        idStrut, idAmbiente, idTipoUd)
                .equals(CostantiDB.Flag.TRUE));
        myEsito.getConfigurazione()
                .setAccettaFirmaNoDelibera45(configurationHelper.getValoreParamApplicByTipoUdAsFl(
                        ParametroApplFl.FL_ACCETTA_FIRMA_GIUGNO_2011, idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));
        myEsito.getConfigurazione()
                .setAccettaFirmaNonConforme(configurationHelper
                        .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_FIRMA_NOCONF,
                                idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));
        myEsito.getConfigurazione()
                .setAccettaFirmaSconosciuta(configurationHelper
                        .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_FIRMA_NOCONOS,
                                idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));

        // MAC #23544
        // aggiorna il tag [Configurazione] dell'esito (vedi elabFlagSipVDB)
        myEsito.getConfigurazione().setForzaConservazione(
                versamento.getStrutturaComponenti().isConfigFlagForzaConservazione());

        //
        if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_VERIFICA_FORMATI_1_25)) {
            // Imposto da DB i valori di configurazione prettamente legati alla verifica
            // formati
            myEsito.getConfigurazione()
                    .setAbilitaControlloFormato(configurationHelper
                            .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ABILITA_CONTR_FMT,
                                    idStrut, idAmbiente, idTipoUd)
                            .equals(CostantiDB.Flag.TRUE));
            myEsito.getConfigurazione().setAccettaControlloFormatoNegativo(configurationHelper
                    .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_CONTR_FMT_NEG,
                            idStrut, idAmbiente, idTipoUd)
                    .equals(CostantiDB.Flag.TRUE));
            myEsito.getConfigurazione().setAccettaMarcaSconosciuta(configurationHelper
                    .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_MARCA_NOCONOS,
                            idStrut, idAmbiente, idTipoUd)
                    .equals(CostantiDB.Flag.TRUE));
        }
        //
        if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_FIRMA_1_5)) {
            // Imposto da DB i valori di configurazione prettamente legati alla verifica
            // formati

            // parametri di servizio
            myEsito.getConfigurazione().setAbilitaVerificaFirma(
                    versamento.getStrutturaComponenti().isFlagAbilitaVerificaFirma());

            myEsito.getConfigurazione().setAbilitaVerificaFirmaSoloCrypto(
                    versamento.getStrutturaComponenti().isFlagAbilitaVerificaFirmaSoloCrypto());

            // abilitazioni/accettazioni/forzature
            myEsito.getConfigurazione().setAbilitaConservazioneNonFirmati(
                    versamento.getStrutturaComponenti().isFlagAbilitaConservazioneNonFirmati());

            myEsito.getConfigurazione().setAccettaConservazioneNonFirmatiNegativa(
                    versamento.getStrutturaComponenti().isFlagAccettaConservazioneNonFirmatiNeg());

            myEsito.getConfigurazione().setAbilitaControlloRevoca(
                    versamento.getStrutturaComponenti().isFlagAbilitaControlloRevoca());

            myEsito.getConfigurazione().setForzaControlloRevoca(
                    versamento.getStrutturaComponenti().isConfigFlagForzaControlloRevoca());

            myEsito.getConfigurazione().setForzaControlloTrust(
                    versamento.getStrutturaComponenti().isConfigFlagForzaControlloTrust());

            myEsito.getConfigurazione().setForzaControlloCertificato(
                    versamento.getStrutturaComponenti().isConfigFlagForzaControlloCertificato());

            myEsito.getConfigurazione().setForzaControlloCrittografico(
                    versamento.getStrutturaComponenti().isConfigFlagForzaControlloCrittografico());

            myEsito.getConfigurazione().setForzaControlloNonConformita(
                    versamento.getStrutturaComponenti().isConfigFlagForzaControlloNonConformita());

            myEsito.getConfigurazione().setAccettaControlloOCSPNegativo(
                    versamento.getStrutturaComponenti().isFlagAccettaControlloOCSPNegativo());

            myEsito.getConfigurazione().setAccettaControlloOCSPNoScaric(
                    versamento.getStrutturaComponenti().isFlagAccettaControlloOCSPNoScaric());

            myEsito.getConfigurazione().setAccettaControlloOCSPNoValido(
                    versamento.getStrutturaComponenti().isFlagAccettaControlloOCSPNoValido());
        }
    }
}
