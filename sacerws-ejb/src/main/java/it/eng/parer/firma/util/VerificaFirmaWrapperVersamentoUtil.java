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
package it.eng.parer.firma.util;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.firma.dto.StatoComponente;
import it.eng.parer.firma.util.VerificaFirmaEnums.SacerIndication;
import it.eng.parer.firma.xml.VFContrFirmaCompType;
import it.eng.parer.firma.xml.VFContrMarcaCompType;
import it.eng.parer.firma.xml.VFFirmaCompType;
import it.eng.parer.firma.xml.VFMarcaCompType;
import it.eng.parer.firma.xml.VFTipoControlloType;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.firma.xml.VerificaFirmaWrapper.VFBusta;
import it.eng.parer.ws.utils.Costanti.ModificatoriWS;
import it.eng.parer.ws.versamento.dto.AbsVersamentoExt;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.xml.versResp.ECComponenteType;
import it.eng.parer.ws.xml.versResp.ECEsitoControlloType;
import it.eng.parer.ws.xml.versResp.ECEsitoPosNegWarType;
import it.eng.parer.ws.xml.versResp.ECFirmatarioType;
import it.eng.parer.ws.xml.versResp.ECMarcaType;

/**
 *
 * @author Sinatti_S
 */
public class VerificaFirmaWrapperVersamentoUtil {

    private static final Logger LOG = LoggerFactory.getLogger(VerificaFirmaWrapperVersamentoUtil.class);
    private static final String LOG_VALNOTMANAGED_MSG = "Valore dell'esito controllo {} non gestito: {}";

    private VerificaFirmaWrapperVersamentoUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void buildECFirmatarioMarcaType(AbsVersamentoExt versamento, ComponenteVers tmpCV,
            StatoComponente statoComponente) {

        // 1. per ogni busta (al momento il num. di busta non è gestito su risposta
        // versamento!)
        VerificaFirmaWrapper wrapper = tmpCV.getVfWrapper();

        for (VFBusta busta : wrapper.getVFBusta()) {
            // 1. Ricavo la lista delle firmaCompType
            for (VFFirmaCompType firmaCompType : busta.getFirmaComps()) {
                // 1.1. Prepara marca e firmatario
                elabMarcaAndFirmatario(versamento, tmpCV, statoComponente, firmaCompType);
                // 2. ControFirmatario
                for (VFFirmaCompType controFirmaCompType : firmaCompType.getControfirmaFirmaFiglios()) {
                    // 2.1. Prepara marca e firmatario
                    elabMarcaAndFirmatario(versamento, tmpCV, statoComponente, controFirmaCompType);
                }

            } // firma

            // 3. Solo marche
            for (VFMarcaCompType marcaCompType : busta.getMarcaComps()) {
                buildECMarcaType(versamento, tmpCV, statoComponente, marcaCompType);
            }
        } // busta
    }

    private static void elabMarcaAndFirmatario(AbsVersamentoExt versamento, ComponenteVers tmpCV,
            StatoComponente statoComponente, VFFirmaCompType firmaCompType) {
        // 2. Ricavo la lista delle marcaCompType
        for (VFMarcaCompType marcaCompType : firmaCompType.getMarcaComps()) {
            buildECMarcaType(versamento, tmpCV, statoComponente, marcaCompType);
        }
        // 2-A. Firmatario
        buildECFirmatarioType(versamento, tmpCV, statoComponente, firmaCompType);
    }

    /*
     * (non-Javadoc)
     *
     *
     * Nota: ex DocumentoVersVFirme.loopMarcheComp
     *
     * TODO: da integrare su WRAPPER (gestione del report/risultato al netto della verifica della firma)
     *
     * marcaCompType.getTiEsitoVerifMarca() / marcaCompType.setTiEsitoVerifMarca()
     * firmaCompType.getDsMsgEsitoContrConforme() / firmaCompType.setDsMsgEsitoContrConforme()
     *
     *
     * @see it.eng.parer.firma.helper.IBaseWrapperParser#buildECMarcaType(it.eng.parer. firma.xml.VerificaFirmaWrapper)
     */
    private static void buildECMarcaType(AbsVersamentoExt versamento, ComponenteVers tmpCV,
            StatoComponente statoComponente, VFMarcaCompType marcaCompType) {

        statoComponente.componenteMarcato = true;

        if (tmpCV.getRifComponenteResp().getMarche() == null) {
            tmpCV.getRifComponenteResp().setMarche(new ECComponenteType.Marche());
        }
        ECMarcaType tmpMarcaResp = new ECMarcaType();
        tmpCV.getRifComponenteResp().getMarche().getMarca().add(tmpMarcaResp);
        // imposto il tag di esito marca
        tmpMarcaResp.setEsitoMarca(new ECMarcaType.EsitoMarca());

        // compilo i dati di base della marca
        tmpMarcaResp.setOrdineMarca(
                BigInteger.valueOf(marcaCompType.getPgMarca() != null ? marcaCompType.getPgMarca().longValue() : 0));
        tmpMarcaResp.setFormatoMarca(marcaCompType.getTiFormatoMarca());
        tmpMarcaResp.setTimestamp(marcaCompType.getTmMarcaTemp());

        // ControlloConformità
        String esitoContrConf = marcaCompType.getTiEsitoContrConforme();
        // verifica se la versione del versamento è almeno la 1.4.
        // in questo caso nell'esito deve mettere i tag estesi degli esiti di firma e
        // marca
        boolean scriviDettagliContrMarca = false;
        if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_INFO_FIRME_EXT_OUT)) {
            tmpMarcaResp.getEsitoMarca().setDettaglioControlloConformita(marcaCompType.getDsMsgEsitoContrConforme());
            scriviDettagliContrMarca = true;
        }
        if (esitoContrConf.equals(SacerIndication.POSITIVO.name())) {
            tmpMarcaResp.getEsitoMarca().setControlloConformita(ECEsitoControlloType.POSITIVO);

            // Se l'esito è positivo imposto il tag di VerificaMarca
            tmpMarcaResp.getEsitoMarca().setVerificaMarca(new ECMarcaType.EsitoMarca.VerificaMarca());

            // Prima imposto gli esiti di ogni controllo
            String tmpTiEsitoControlloMarca;
            String messaggioEsitoCMarca;
            for (VFContrMarcaCompType contrMarcaCompType : marcaCompType.getContrMarcaComps()) {
                tmpTiEsitoControlloMarca = contrMarcaCompType.getTiEsitoContrMarca();
                messaggioEsitoCMarca = contrMarcaCompType.getDsMsgEsitoContrMarca();
                //
                if (contrMarcaCompType.getTiContr().name().equals(VFTipoControlloType.CRITTOGRAFICO.name())) {
                    tmpMarcaResp.getEsitoMarca().getVerificaMarca()
                            .setControlloCrittografico(ECEsitoControlloType.valueOf(tmpTiEsitoControlloMarca));
                    if (scriviDettagliContrMarca) {
                        tmpMarcaResp.getEsitoMarca().getVerificaMarca()
                                .setDettaglioControlloCrittografico(messaggioEsitoCMarca);
                    }
                    if (!tmpTiEsitoControlloMarca.equals(SacerIndication.POSITIVO.name())) {
                        statoComponente.ctrlMarcheCrittNegativo = true;
                        // memorizzo l'anomalia relativa alla marca del componente. alla fine del ciclo
                        // firme-marche aggiungerò un warning
                    }
                } else if (contrMarcaCompType.getTiContr().name().equals(VFTipoControlloType.CATENA_TRUSTED.name())) {
                    tmpMarcaResp.getEsitoMarca().getVerificaMarca()
                            .setControlloCatenaTrusted(ECEsitoControlloType.valueOf(tmpTiEsitoControlloMarca));
                    if (scriviDettagliContrMarca) {
                        tmpMarcaResp.getEsitoMarca().getVerificaMarca()
                                .setDettaglioControlloCatenaTrusted(messaggioEsitoCMarca);
                    }
                    if (!tmpTiEsitoControlloMarca.equals(SacerIndication.POSITIVO.name())) {
                        statoComponente.ctrlMarcheCatenaNegativo = true;
                        // memorizzo l'anomalia relativa alla marca del componente. alla fine del ciclo
                        // firme-marche aggiungerò un warning
                    }
                } else if (contrMarcaCompType.getTiContr().name().equals(VFTipoControlloType.CERTIFICATO.name())) {
                    tmpMarcaResp.getEsitoMarca().getVerificaMarca()
                            .setControlloCertificato(ECEsitoControlloType.valueOf(tmpTiEsitoControlloMarca));
                    if (scriviDettagliContrMarca) {
                        tmpMarcaResp.getEsitoMarca().getVerificaMarca()
                                .setDettaglioControlloCertificato(messaggioEsitoCMarca);
                    }
                    if (!tmpTiEsitoControlloMarca.equals(SacerIndication.POSITIVO.name())) {
                        statoComponente.ctrlMarcheCertificatoNegativo = true;
                        // memorizzo l'anomalia relativa alla marca del componente. alla fine del ciclo
                        // firme-marche aggiungerò un warning
                    }
                } else if (contrMarcaCompType.getTiContr().name().equals(VFTipoControlloType.CRL.name())) {
                    tmpMarcaResp.getEsitoMarca().getVerificaMarca()
                            .setControlloCRL(ECEsitoControlloType.valueOf(tmpTiEsitoControlloMarca));
                    if (scriviDettagliContrMarca) {
                        tmpMarcaResp.getEsitoMarca().getVerificaMarca().setDettaglioControlloCRL(messaggioEsitoCMarca);
                    }
                    if (!tmpTiEsitoControlloMarca.equals(SacerIndication.POSITIVO.name())
                            && !tmpTiEsitoControlloMarca.equals(SacerIndication.NON_NECESSARIO.name())) {
                        statoComponente.ctrlMarcheCrlNegativo = true;
                        // memorizzo l'anomalia relativa alla marca del componente. alla fine del ciclo
                        // firme-marche aggiungerò un warning
                    }
                } else if (contrMarcaCompType.getTiContr().name().equals(VFTipoControlloType.OCSP.name())
                        && versamento.getModificatoriWSCalc()
                                .contains(ModificatoriWS.TAG_FIRMA_1_5)/* tag visibile a partire da v1.5 */) {
                    tmpMarcaResp.getEsitoMarca().getVerificaMarca()
                            .setControlloOCSP(ECEsitoControlloType.valueOf(tmpTiEsitoControlloMarca));
                    if (scriviDettagliContrMarca) {
                        tmpMarcaResp.getEsitoMarca().getVerificaMarca().setDettaglioControlloOCSP(messaggioEsitoCMarca);
                    }
                    if (!tmpTiEsitoControlloMarca.equals(SacerIndication.OCSP_VALIDO.name())
                            && !tmpTiEsitoControlloMarca.equals(SacerIndication.NON_NECESSARIO.name())) {
                        statoComponente.ctrlMarcheOcspNegativo = true;
                        // memorizzo l'anomalia relativa alla marca del componente. alla fine del ciclo
                        // firme-marche aggiungerò un warning
                    }
                }
            }

            // Quindi valorizzo VerificaMarca.CodiceEsito
            if (marcaCompType.getTiEsitoVerifMarca().equals(SacerIndication.POSITIVO.name())) {
                tmpMarcaResp.getEsitoMarca().getVerificaMarca().setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
            } else {
                tmpMarcaResp.getEsitoMarca().getVerificaMarca().setCodiceEsito(ECEsitoPosNegWarType.WARNING);
            }
        } else { // se l'esito non è positivo
            tmpMarcaResp.getEsitoMarca().setControlloConformita(ECEsitoControlloType.FORMATO_NON_CONOSCIUTO);
            statoComponente.ctrlConfMarcheHasFormatoNonConosciuto = true;
            // memorizzo l'anomalia relativa alla marca del componente.
            // alla fine del ciclo firme-marche aggiungerò un warning o un errore in
            // funzione di forzature varie
        }

    }

    /*
     *
     * (non-Javadoc)
     *
     * Nota: ex DocumentoVersVFirme.loopFirmeComp
     *
     * @see it.eng.parer.firma.helper.IBaseWrapperParser#buildECFirmatarioType(it.eng.
     * parer.firma.xml.VerificaFirmaWrapper)
     */
    private static void buildECFirmatarioType(AbsVersamentoExt versamento, ComponenteVers tmpCV,
            StatoComponente statoComponente, VFFirmaCompType firmaCompType) {

        boolean firmaVerificabile = true;
        if (tmpCV.getRifComponenteResp().getFirmatari() == null) {
            tmpCV.getRifComponenteResp().setFirmatari(new ECComponenteType.Firmatari());
        }

        // creo il ramo dell'XML di esito relativo alla firma/firmatario
        ECFirmatarioType tmpFirmatarioResp = new ECFirmatarioType();
        tmpCV.getRifComponenteResp().getFirmatari().getFirmatario().add(tmpFirmatarioResp);
        tmpFirmatarioResp.setEsitoFirma(new ECFirmatarioType.EsitoFirma());
        // compilo i dati di base del firmatario
        tmpFirmatarioResp.setOrdineFirma(
                BigInteger.valueOf(firmaCompType.getPgFirma() != null ? firmaCompType.getPgFirma().longValue() : 0));
        // compilo il cognome e nome, se presente, oppure riporto l'intero campo DN
        if (firmaCompType.getNmCognomeFirmatario() != null && firmaCompType.getNmFirmatario() != null) {
            tmpFirmatarioResp
                    .setCognomeNome(firmaCompType.getNmCognomeFirmatario() + " " + firmaCompType.getNmFirmatario());
        } else {
            tmpFirmatarioResp.setCognomeNome(firmaCompType.getDlDnFirmatario());
        }
        tmpFirmatarioResp.setFormatoFirma(firmaCompType.getTiFormatoFirma());
        tmpFirmatarioResp.setRiferimentoTemporaleUsato(firmaCompType.getTmRifTempUsato());
        //
        tmpFirmatarioResp.setTipoRiferimentoTemporaleUsato(firmaCompType.getTipoRiferimentoTemporaleUsato());

        // ControlloConformità
        String esitoContrConf = firmaCompType.getTiEsitoContrConforme();

        if (esitoContrConf.equals(SacerIndication.FORMATO_NON_CONOSCIUTO.name())) {
            tmpFirmatarioResp.getEsitoFirma().setControlloConformita(ECEsitoControlloType.FORMATO_NON_CONOSCIUTO);
            firmaVerificabile = false; // nota bene: SOLO in questo caso, viene considerato come NON FIRMATO
            statoComponente.ctrlConfFirmeHasFormatoNonConosciuto = true;
            // memorizzo l'anomalia relativa alla firma del componente.
            // alla fine del ciclo firme-marche aggiungerò un warning o un errore in
            // funzione di forzature varie
        } else if (esitoContrConf.equals(SacerIndication.FORMATO_NON_CONFORME.name())) {
            tmpFirmatarioResp.getEsitoFirma().setControlloConformita(ECEsitoControlloType.FORMATO_NON_CONFORME);
            statoComponente.ctrlConfFirmeHasFormatoNonConforme = true;
            // memorizzo l'anomalia relativa alla firma del componente.
            // alla fine del ciclo firme-marche aggiungerò un warning o un errore in
            // funzione di forzature varie
        } else if (esitoContrConf.equals(SacerIndication.NON_AMMESSO_DELIB_45_CNIPA.name())) {
            tmpFirmatarioResp.getEsitoFirma().setControlloConformita(ECEsitoControlloType.NON_AMMESSO_DELIB_45_CNIPA);
            statoComponente.ctrlConfFirmeHasNonAmmessoDelib45 = true;
            // memorizzo l'anomalia relativa alla firma del componente.
            // alla fine del ciclo firme-marche aggiungerò un warning o un errore in
            // funzione di forzature varie
        } else {
            tmpFirmatarioResp.getEsitoFirma().setControlloConformita(ECEsitoControlloType.POSITIVO);
        }

        // verifica se la versione del versamento è almeno la 1.4.
        // in questo caso nell'esito deve mettere i tag estesi degli esiti di firma e
        // marca
        boolean scriviDettagliContrFirma = false;
        if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_INFO_FIRME_EXT_OUT)) {
            tmpFirmatarioResp.getEsitoFirma()
                    .setDettaglioControlloConformita(firmaCompType.getDsMsgEsitoContrConforme());
            scriviDettagliContrFirma = true;
        }

        // se ControlloConformità è Positivo o Non Ammesso Delibera 45 o Non Conforme
        // allora posso popolare il codice esito di Verifica Firma
        if (firmaVerificabile) {
            statoComponente.componenteFirmato = true; // c'è almeno una firma utilizzabile nel componente
            tmpFirmatarioResp.getEsitoFirma().setVerificaFirma(new ECFirmatarioType.EsitoFirma.VerificaFirma());
            // Imposto gli esiti di ogni controllo

            boolean firmaErr = false;
            boolean firmaWarn = false;
            String esitoControlloFirma;
            String messaggioEsitoCFirma;

            for (VFContrFirmaCompType contrFirmaCompType : firmaCompType.getContrFirmaComps()) {
                esitoControlloFirma = contrFirmaCompType.getTiEsitoContrFirma();
                messaggioEsitoCFirma = contrFirmaCompType.getDsMsgEsitoContrFirma();

                final boolean esitoControlloPositivo = esitoControlloFirma.equals(SacerIndication.POSITIVO.name());

                if (contrFirmaCompType.getTiContr().name().equals(VFTipoControlloType.CRITTOGRAFICO.name())) {
                    // imposto il valore e l'eventuale messaggio di dettaglio nell'esito
                    tmpFirmatarioResp.getEsitoFirma().getVerificaFirma()
                            .setControlloCrittografico(ECEsitoControlloType.valueOf(esitoControlloFirma));
                    if (scriviDettagliContrFirma) {
                        tmpFirmatarioResp.getEsitoFirma().getVerificaFirma()
                                .setDettaglioControlloCrittografico(messaggioEsitoCFirma);
                    }
                    // valuto gli esiti negativi o warning
                    if (!esitoControlloPositivo) {
                        // verifico lo stato:
                        if (esitoControlloFirma.equals(SacerIndication.NON_ESEGUITO.name())) {
                            // se non eseguito la firma è in warn e aggiungo il warn alla lista
                            statoComponente.ctrlFirmeCrittWarning = true;
                            firmaWarn = true;
                        } else if (esitoControlloFirma.equals(SacerIndication.ERRORE.name())) {
                            // se è andato in errore, la firma è in err, il componente è in err e aggiungo
                            // l'errore alla lista
                            statoComponente.ctrlFirmeCrittErrore = true;
                            firmaErr = true;
                        } else if (esitoControlloFirma.equals(SacerIndication.NEGATIVO.name())) {
                            statoComponente.ctrlFirmeCrittNegativo = true;
                            firmaErr = true;
                        } else {
                            LOG.debug(LOG_VALNOTMANAGED_MSG, contrFirmaCompType.getTiContr(), esitoControlloFirma);
                        }
                    }
                } else if (contrFirmaCompType.getTiContr().name().equals(VFTipoControlloType.CATENA_TRUSTED.name())) {
                    // imposto il valore e l'eventuale messaggio di dettaglio nell'esito
                    tmpFirmatarioResp.getEsitoFirma().getVerificaFirma()
                            .setControlloCatenaTrusted(ECEsitoControlloType.valueOf(esitoControlloFirma));
                    if (scriviDettagliContrFirma) {
                        tmpFirmatarioResp.getEsitoFirma().getVerificaFirma()
                                .setDettaglioControlloCatenaTrusted(messaggioEsitoCFirma);
                    }
                    // valuto gli esiti negativi o warning
                    if (!esitoControlloPositivo) {
                        // verifico lo stato:
                        if (esitoControlloFirma.equals(SacerIndication.NON_ESEGUITO.name())) {
                            // se non eseguito la firma è in warn e aggiungo il warn alla lista
                            statoComponente.ctrlFirmeCatenaWarning = true;
                            firmaWarn = true;
                        } else if (esitoControlloFirma.equals(SacerIndication.ERRORE.name())) {
                            // se è andato in errore, la firma è in err, il componente è in err e aggiungo
                            // l'errore alla lista
                            statoComponente.ctrlFirmeCatenaErrore = true;
                            firmaErr = true;
                        } else if (esitoControlloFirma.equals(SacerIndication.NEGATIVO.name())) {
                            statoComponente.ctrlFirmeCatenaNegativo = true;
                            firmaErr = true;
                        } else {
                            LOG.debug(LOG_VALNOTMANAGED_MSG, contrFirmaCompType.getTiContr(), esitoControlloFirma);
                        }
                    }
                } else if (contrFirmaCompType.getTiContr().name().equals(VFTipoControlloType.CERTIFICATO.name())) {
                    // imposto il valore e l'eventuale messaggio di dettaglio nell'esito
                    tmpFirmatarioResp.getEsitoFirma().getVerificaFirma()
                            .setControlloCertificato(ECEsitoControlloType.valueOf(esitoControlloFirma));
                    if (scriviDettagliContrFirma) {
                        tmpFirmatarioResp.getEsitoFirma().getVerificaFirma()
                                .setDettaglioControlloCertificato(messaggioEsitoCFirma);
                    }
                    // valuto gli esiti negativi o warning
                    if (!esitoControlloPositivo) {
                        // verifico lo stato:
                        if (esitoControlloFirma.equals(SacerIndication.NON_ESEGUITO.name())) {
                            // se non eseguito la firma è in warn e aggiungo il warn alla lista
                            statoComponente.ctrlFirmeCertificatoWarning = true;
                            firmaWarn = true;
                        } else if (esitoControlloFirma.equals(SacerIndication.ERRORE.name())) {
                            // se è andato in errore, la firma è in err, il componente è in err e aggiungo
                            // l'errore alla lista
                            statoComponente.ctrlFirmeCertificatoErrore = true;
                            firmaErr = true;
                        } else if (esitoControlloFirma.equals(SacerIndication.CERTIFICATO_SCADUTO.name())
                                || esitoControlloFirma.equals(SacerIndication.REVOCHE_NON_CONSISTENTI.name())
                                || esitoControlloFirma.equals(SacerIndication.CERTIFICATO_REVOCATO.name())) {
                            statoComponente.ctrlFirmeCertificatoScadRev = true;
                            firmaErr = true;
                        } else if (esitoControlloFirma.equals(SacerIndication.CERTIFICATO_NON_VALIDO.name())) {
                            statoComponente.ctrlFirmeCertificatoNoValid = true;
                            firmaErr = true;
                        } else if (esitoControlloFirma.equals(SacerIndication.CERTIFICATO_ERRATO.name())) {
                            statoComponente.ctrlFirmeCertificatoErrato = true;
                            firmaErr = true;
                        } else {
                            LOG.debug(LOG_VALNOTMANAGED_MSG, contrFirmaCompType.getTiContr(), esitoControlloFirma);
                        }
                    }
                } else if (contrFirmaCompType.getTiContr().name().equals(VFTipoControlloType.CRL.name())) {
                    // imposto il valore e l'eventuale messaggio di dettaglio nell'esito
                    tmpFirmatarioResp.getEsitoFirma().getVerificaFirma()
                            .setControlloCRL(ECEsitoControlloType.valueOf(esitoControlloFirma));
                    if (scriviDettagliContrFirma) {
                        tmpFirmatarioResp.getEsitoFirma().getVerificaFirma()
                                .setDettaglioControlloCRL(messaggioEsitoCFirma);
                    }
                    // valuto gli esiti negativi o warning
                    if (!esitoControlloPositivo) {
                        // verifico lo stato:
                        if (esitoControlloFirma.equals(SacerIndication.NON_ESEGUITO.name())
                                || esitoControlloFirma.equals(SacerIndication.CERTIFICATO_SCADUTO_3_12_2009.name())) {
                            statoComponente.ctrlFirmeCRLWarning = true;
                            firmaWarn = true;
                        } else if (esitoControlloFirma.equals(SacerIndication.ERRORE.name())) {
                            statoComponente.ctrlFirmeCRLErrore = true;
                            firmaErr = true;
                        } else if (esitoControlloFirma.equals(SacerIndication.CERTIFICATO_REVOCATO.name())) {
                            statoComponente.ctrlFirmeCRLCertRev = true;
                            firmaErr = true;
                        } else if (esitoControlloFirma.equals(SacerIndication.CRL_SCADUTA.name())) {
                            statoComponente.ctrlFirmeCRLScad = true;
                            firmaErr = true;
                        } else if (esitoControlloFirma.equals(SacerIndication.CRL_NON_VALIDA.name())) {
                            statoComponente.ctrlFirmeCRLNoValid = true;
                            firmaErr = true;
                        } else if (esitoControlloFirma.equals(SacerIndication.CRL_NON_SCARICABILE.name())) {
                            statoComponente.ctrlFirmeCRLNoScaric = true;
                            firmaErr = true;
                        } else {
                            LOG.debug(LOG_VALNOTMANAGED_MSG, contrFirmaCompType.getTiContr(), esitoControlloFirma);
                        }
                    }
                } else if (contrFirmaCompType.getTiContr().name().equals(VFTipoControlloType.OCSP.name())
                        && versamento.getModificatoriWSCalc()
                                .contains(ModificatoriWS.TAG_FIRMA_1_5)/* tag visibile a partire da v1.5 */) {
                    // imposto il valore e l'eventuale messaggio di dettaglio nell'esito
                    tmpFirmatarioResp.getEsitoFirma().getVerificaFirma()
                            .setControlloOCSP(ECEsitoControlloType.valueOf(esitoControlloFirma));
                    if (scriviDettagliContrFirma) {
                        tmpFirmatarioResp.getEsitoFirma().getVerificaFirma()
                                .setDettaglioControlloOCSP(messaggioEsitoCFirma);
                    }
                    // valuto gli esiti negativi o warning
                    if (!esitoControlloPositivo) {
                        // verifico lo stato:
                        if (esitoControlloFirma.equals(SacerIndication.NON_ESEGUITO.name())
                                || esitoControlloFirma.equals(SacerIndication.OCSP_NON_AGGIORNATO.name())) {
                            statoComponente.ctrlFirmeOCSPWarning = true;
                            firmaWarn = true;
                        } else if (esitoControlloFirma.equals(SacerIndication.OCSP_SCONOSCIUTO.name())) {
                            statoComponente.ctrlFirmeOCSPNoValid = true;
                            firmaErr = true;
                        } else if (esitoControlloFirma.equals(SacerIndication.OCSP_REVOCATO.name())) {
                            statoComponente.ctrlFirmeOCSPRev = true;
                            firmaErr = true;
                        } else if (esitoControlloFirma.equals(SacerIndication.OCSP_NON_SCARICABILE.name())) {
                            statoComponente.ctrlFirmeOCSPNoScaric = true;
                            firmaErr = true;
                        } else {
                            LOG.debug(LOG_VALNOTMANAGED_MSG, contrFirmaCompType.getTiContr(), esitoControlloFirma);
                        }
                    }
                }
            }

            // Quindi valorizzo VerificaFirma.CodiceEsito.
            // nel caso di errore o warning, mi ricordo che una firma è fallita, per poterlo
            // riportare nell'esito firme comp.
            if (firmaErr) {
                tmpFirmatarioResp.getEsitoFirma().getVerificaFirma().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                statoComponente.compEsitoVerFirmeHasErr = true;
            } else if (firmaWarn) {
                tmpFirmatarioResp.getEsitoFirma().getVerificaFirma().setCodiceEsito(ECEsitoPosNegWarType.WARNING);
                statoComponente.compEsitoVerFirmeHasWarn = true;
            } else {
                tmpFirmatarioResp.getEsitoFirma().getVerificaFirma().setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
            }
        }

    }

}
