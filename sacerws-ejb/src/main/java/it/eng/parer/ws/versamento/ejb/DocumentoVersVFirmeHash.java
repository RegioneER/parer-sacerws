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
package it.eng.parer.ws.versamento.ejb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.DecFormatoFileStandard;
import it.eng.parer.firma.dto.CompDocMock;
import it.eng.parer.firma.dto.StatoComponente;
import it.eng.parer.firma.dto.StatoDocumento;
import it.eng.parer.firma.ejb.FirmeFormatiVers;
import it.eng.parer.firma.exception.VerificaFirmaException;
import it.eng.parer.firma.util.VerificaFirmaEnums.SacerIndication;
import it.eng.parer.firma.util.VerificaFirmaWrapperVersamentoUtil;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.utils.AvanzamentoWs;
import it.eng.parer.ws.utils.BinEncUtility;
import it.eng.parer.ws.utils.Costanti.ModificatoriWS;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.HashUtility;
import it.eng.parer.ws.utils.Hashresult;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.XmlDateUtility;
import it.eng.parer.ws.versamento.dto.AbsVersamentoExt;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.versamento.dto.DocumentoVers;
import it.eng.parer.ws.xml.versResp.ECConfigurazioneType;
import it.eng.parer.ws.xml.versResp.ECDocumentoType;
import it.eng.parer.ws.xml.versResp.ECEsitoIdonFormatoType;
import it.eng.parer.ws.xml.versResp.ECEsitoPosNegWarDisType;
import it.eng.parer.ws.xml.versResp.ECEsitoPosNegWarType;

/**
 *
 * @author Fioravanti_F
 */
@Stateless(mappedName = "DocumentoVersVFirmeHash")
@LocalBean
public class DocumentoVersVFirmeHash {

    private static final Logger log = LoggerFactory.getLogger(DocumentoVersVFirmeHash.class);
    // Stateless ejb per la verifica delle firme
    @EJB
    private FirmeFormatiVers firmeFormatiEjb;
    // stateless ejb per i controlli sul db
    @EJB
    private ControlliSemantici controlliSemantici;
    @EJB
    private ControlliPerFirme controlliPerFirme;
    @EJB
    private ConfigurationHelper configurationHelper;

    public boolean verificaDocumentoGenFirma(DocumentoVers documento, long idTipoUd, AbsVersamentoExt versamento,
            IRispostaWS rispostaWs, ECConfigurazioneType esitoConfigurazione) {

        ECDocumentoType esitoDoc = documento.getRifDocumentoResp();
        esitoDoc.setFirmatoDigitalmente(false);

        StatoDocumento statoDocumento = new StatoDocumento();
        AvanzamentoWs myAvanzamentoWs = rispostaWs.getAvanzamento();
        boolean proseguiElaborazione = true;
        boolean isForzaAccett = esitoConfigurazione.isForzaAccettazione();

        StatoComponente statoCompCorrente = new StatoComponente();

        // verifica firme e formati
        for (ComponenteVers tmpCV : documento.getFileAttesi()) {
            // se sono un componente di tipo file e non ci sono stati errori bloccanti nel
            // componente precedente
            if (tmpCV.getMySottoComponente() == null && tmpCV.getTipoSupporto() == ComponenteVers.TipiSupporto.FILE
                    && proseguiElaborazione) {

                myAvanzamentoWs.setFase("inizio verifica componente firma")
                        .setDocumento(documento.getRifDocumento().getIDDocumento())
                        .setComponente(Long.toString(tmpCV.getMyComponente().getOrdinePresentazione()))
                        .logAvanzamento();

                statoCompCorrente.reset();

                if (proseguiElaborazione) {

                    if (tmpCV.getMyComponente().getRiferimentoTemporale() != null) {
                        tmpCV.withAcdEntity().setTmRifTempVers(XmlDateUtility.xmlGregorianCalendarToZonedDateTime(
                                tmpCV.getMyComponente().getRiferimentoTemporale()));
                    }

                    if (tmpCV.getMyComponente().isUtilizzoDataFirmaPerRifTemp() != null) {
                        if (tmpCV.getMyComponente().isUtilizzoDataFirmaPerRifTemp().booleanValue()) {
                            tmpCV.withAcdEntity().setFlRifTempDataFirmaVers("1");
                        } else {
                            tmpCV.withAcdEntity().setFlRifTempDataFirmaVers("0");
                        }
                    }

                    tmpCV.withAcdEntity()
                            .setIdStrut(new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));

                    List<ComponenteVers> sottoComponentiFirma = new ArrayList<>();
                    List<ComponenteVers> sottoComponentiMarca = new ArrayList<>();
                    List<ComponenteVers> sottoComponentiAltri = new ArrayList<>();

                    for (ComponenteVers tmpCV2 : documento.getFileAttesi()) {
                        // riscorro ogni componente: deve essere un sottocomponente, essere mio figlio e
                        // di tipo FILE
                        if (tmpCV2.getMySottoComponente() != null
                                && tmpCV2.getRifComponenteVersPadre().getMyComponente().getID().equals(tmpCV.getId())
                                && tmpCV2.getTipoSupporto() == ComponenteVers.TipiSupporto.FILE) {
                            // creo l'istanza di AroCompDoc
                            tmpCV2.withAcdEntity()
                                    .setIdStrut(new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
                            // se è una firma
                            switch (tmpCV2.getTipoUso()) {
                            // se è una marca
                            case "FIRMA":
                                sottoComponentiFirma.add(tmpCV2);
                                break;
                            case "MARCA":
                                sottoComponentiMarca.add(tmpCV2);
                                break;
                            default:
                                // se è un sottocomponente generico... viene passato alla libreria per la
                                // verifica del formato
                                sottoComponentiAltri.add(tmpCV2);
                                break;
                            }
                        }
                    }

                    // ho finito di recuperare i sottocomponenti FIRME E MARCHE e altri del
                    // componente in questione:
                    // posso lanciare la verifica delle firme e marche che mi restituirà una entity
                    // AroCompDoc decorata
                    // con la quale popolo il componente versato
                    RispostaControlli rispostaControlli = this.elabCompDocMockWithVerificaFirmaAndFormatoResult(tmpCV,
                            sottoComponentiFirma, sottoComponentiMarca, sottoComponentiAltri, idTipoUd, versamento,
                            esitoConfigurazione);

                    if (rispostaControlli.getrLong() == -1) { // se il valore è -1, allora codErr è un codice di errore
                        proseguiElaborazione = false;
                        // check 666 (errore irreversibile)
                        if (rispostaControlli.getCodErr().equals(MessaggiWSBundle.ERR_666)) {
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                        } else {
                            // il componente è considerato come "non firmato"
                            // aggiunge l'errore che poi potrà essere "forzato" attraverso appositi flag
                            if (isForzaAccett) {
                                versamento.addWarningDoc(documento, tmpCV.getChiaveComp(),
                                        rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                            } else {
                                versamento.addErrorDoc(documento, tmpCV.getChiaveComp(), rispostaControlli.getCodErr(),
                                        rispostaControlli.getDsErr());
                            }
                        }
                    }
                }
                // </editor-fold>

                boolean tmpComponenteErr = false;
                boolean tmpComponenteWarn = false;

                if (proseguiElaborazione) {
                    //
                    VerificaFirmaWrapperVersamentoUtil.buildECFirmatarioMarcaType(versamento, tmpCV, statoCompCorrente);

                    // verifica se il comp è FirmatoDigitalmente
                    if (statoCompCorrente.componenteFirmato) {
                        tmpCV.getRifComponenteResp().setFirmatoDigitalmente(true);
                        esitoDoc.setFirmatoDigitalmente(true);
                        tmpCV.withAcdEntity().setFlCompFirmato("1");
                        documento.setFlFileFirmato("1");

                        // esito verifica firme comp
                        if (statoCompCorrente.compEsitoVerFirmeHasErr) {
                            tmpCV.withAcdEntity().setTiEsitoVerifFirme(SacerIndication.NEGATIVO.name());
                            tmpCV.withAcdEntity().setDsMsgEsitoVerifFirme(SacerIndication.NEGATIVO.message());
                            tmpCV.getRifComponenteResp().getEsitoComponente()
                                    .setVerificaFirmeComponente(ECEsitoPosNegWarType.NEGATIVO);
                            statoDocumento.verFirmeCompCodiceEsitoNeg = true;
                        } else if (statoCompCorrente.compEsitoVerFirmeHasWarn) {
                            tmpCV.withAcdEntity().setTiEsitoVerifFirme(SacerIndication.WARNING.name());
                            tmpCV.withAcdEntity().setDsMsgEsitoVerifFirme(SacerIndication.WARNING.message());
                            tmpCV.getRifComponenteResp().getEsitoComponente()
                                    .setVerificaFirmeComponente(ECEsitoPosNegWarType.WARNING);
                            statoDocumento.verFirmeCompCodiceEsitoWar = true;
                        } else {
                            tmpCV.withAcdEntity().setTiEsitoVerifFirme(SacerIndication.POSITIVO.name());
                            tmpCV.withAcdEntity().setDsMsgEsitoVerifFirme(SacerIndication.POSITIVO.message());
                            tmpCV.getRifComponenteResp().getEsitoComponente()
                                    .setVerificaFirmeComponente(ECEsitoPosNegWarType.POSITIVO);
                        }
                    } else {
                        tmpCV.getRifComponenteResp().setFirmatoDigitalmente(false);
                        tmpCV.withAcdEntity().setFlCompFirmato("0");
                    }

                    // esito componente, tenendo conto di forzature varie
                    // marche:
                    // prima parte: conformità
                    if (statoCompCorrente.ctrlConfMarcheHasFormatoNonConosciuto) {
                        if (versamento.getStrutturaComponenti().isConfigFlagForzaControlloNonConformita()
                                || isForzaAccett && esitoConfigurazione.isAccettaMarcaSconosciuta().booleanValue()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.MARCA_001_001, tmpCV.getChiaveComp());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.MARCA_001_001, tmpCV.getChiaveComp());
                            tmpComponenteErr = true;
                        }
                    }
                    // controlli crittografici, nel caso sia conforme o comunque verificabile
                    if (statoCompCorrente.ctrlMarcheCrittNegativo) {
                        versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                MessaggiWSBundle.MARCA_002_001, tmpCV.getChiaveComp());
                        tmpComponenteWarn = true;
                    }
                    if (statoCompCorrente.ctrlMarcheCatenaNegativo) {
                        versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                MessaggiWSBundle.MARCA_003_001, tmpCV.getChiaveComp());
                        tmpComponenteWarn = true;
                    }
                    if (statoCompCorrente.ctrlMarcheCertificatoNegativo) {
                        versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                MessaggiWSBundle.MARCA_004_001, tmpCV.getChiaveComp());
                        tmpComponenteWarn = true;
                    }
                    if (statoCompCorrente.ctrlMarcheCrlNegativo) {
                        versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                MessaggiWSBundle.MARCA_005_001, tmpCV.getChiaveComp());
                        tmpComponenteWarn = true;
                    }
                    if (statoCompCorrente.ctrlMarcheOcspNegativo) {
                        versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                MessaggiWSBundle.MARCA_006_001, tmpCV.getChiaveComp());
                        tmpComponenteWarn = true;
                    }
                    // firme
                    // prima parte: conformità
                    if (statoCompCorrente.ctrlConfFirmeHasFormatoNonConosciuto) {
                        if (versamento.getStrutturaComponenti().isConfigFlagForzaControlloNonConformita()
                                || isForzaAccett && esitoConfigurazione.isAccettaFirmaSconosciuta().booleanValue()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_001_001, tmpCV.getChiaveComp());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_001_001, tmpCV.getChiaveComp());
                            tmpComponenteErr = true;
                        }
                    }
                    if (statoCompCorrente.ctrlConfFirmeHasFormatoNonConforme) {
                        if (versamento.getStrutturaComponenti().isConfigFlagForzaControlloNonConformita()
                                || isForzaAccett && esitoConfigurazione.isAccettaFirmaNonConforme().booleanValue()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_001_002, tmpCV.getChiaveComp());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_001_002, tmpCV.getChiaveComp());
                            tmpComponenteErr = true;
                        }
                    }
                    if (statoCompCorrente.ctrlConfFirmeHasNonAmmessoDelib45) {
                        if (versamento.getStrutturaComponenti().isConfigFlagForzaControlloNonConformita()
                                || isForzaAccett && esitoConfigurazione.isAccettaFirmaNoDelibera45().booleanValue()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_001_003, tmpCV.getChiaveComp());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_001_003, tmpCV.getChiaveComp());
                            tmpComponenteErr = true;
                        }
                    }
                    // controlli crittografici, nel caso sia conforme o comunque verificabile
                    // controllo Crittografico
                    if (statoCompCorrente.ctrlFirmeCrittWarning) {
                        versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                MessaggiWSBundle.FIRMA_002_001, tmpCV.getChiaveComp());
                        tmpComponenteWarn = true;
                    }
                    if (statoCompCorrente.ctrlFirmeCrittErrore) {
                        versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(), MessaggiWSBundle.FIRMA_002_001,
                                tmpCV.getChiaveComp());
                        tmpComponenteErr = true;
                    }
                    if (statoCompCorrente.ctrlFirmeCrittNegativo) {
                        if (versamento.getStrutturaComponenti().isConfigFlagForzaControlloCrittografico()
                                || isForzaAccett && esitoConfigurazione.isAccettaControlloCrittograficoNegativo()
                                        .booleanValue()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_002_001, tmpCV.getChiaveComp());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_002_001, tmpCV.getChiaveComp());
                            tmpComponenteErr = true;
                        }
                    }
                    // controllo catena trusted
                    if (statoCompCorrente.ctrlFirmeCatenaWarning) {
                        versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                MessaggiWSBundle.FIRMA_003_001, tmpCV.getChiaveComp());
                        tmpComponenteWarn = true;
                    }
                    if (statoCompCorrente.ctrlFirmeCatenaErrore) {
                        versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(), MessaggiWSBundle.FIRMA_003_001,
                                tmpCV.getChiaveComp());
                        tmpComponenteErr = true;
                    }
                    if (statoCompCorrente.ctrlFirmeCatenaNegativo) {
                        if (versamento.getStrutturaComponenti().isConfigFlagForzaControlloTrust() || isForzaAccett
                                && esitoConfigurazione.isAccettaControlloTrustNegativo().booleanValue()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_003_001, tmpCV.getChiaveComp());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_003_001, tmpCV.getChiaveComp());
                            tmpComponenteErr = true;
                        }
                    }
                    // controllo certificato
                    if (statoCompCorrente.ctrlFirmeCertificatoWarning) {
                        versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                MessaggiWSBundle.FIRMA_004_001, tmpCV.getChiaveComp());
                        tmpComponenteWarn = true;
                    }
                    if (statoCompCorrente.ctrlFirmeCertificatoErrore) {
                        versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(), MessaggiWSBundle.FIRMA_004_001,
                                tmpCV.getChiaveComp());
                        tmpComponenteErr = true;
                    }
                    if (statoCompCorrente.ctrlFirmeCertificatoScadRev) {
                        if (versamento.getStrutturaComponenti().isConfigFlagForzaControlloCertificato() || isForzaAccett
                                && esitoConfigurazione.isAccettaControlloCertificatoScaduto().booleanValue()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_004_001, tmpCV.getChiaveComp());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_004_001, tmpCV.getChiaveComp());
                            tmpComponenteErr = true;
                        }
                    }
                    if (statoCompCorrente.ctrlFirmeCertificatoNoValid) {
                        if (versamento.getStrutturaComponenti().isConfigFlagForzaControlloCertificato() || isForzaAccett
                                && esitoConfigurazione.isAccettaControlloCertificatoNoValido().booleanValue()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_004_001, tmpCV.getChiaveComp());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_004_001, tmpCV.getChiaveComp());
                            tmpComponenteErr = true;
                        }
                    }
                    if (statoCompCorrente.ctrlFirmeCertificatoErrato) {
                        if (versamento.getStrutturaComponenti().isConfigFlagForzaControlloCertificato() || isForzaAccett
                                && esitoConfigurazione.isAccettaControlloCertificatoNoFirma().booleanValue()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_004_001, tmpCV.getChiaveComp());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_004_001, tmpCV.getChiaveComp());
                            tmpComponenteErr = true;
                        }
                    }
                    // controllo CRL
                    if (statoCompCorrente.ctrlFirmeCRLWarning) {
                        versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                MessaggiWSBundle.FIRMA_005_001, tmpCV.getChiaveComp());
                        tmpComponenteWarn = true;
                    }
                    if (statoCompCorrente.ctrlFirmeCRLErrore) {
                        versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(), MessaggiWSBundle.FIRMA_005_001,
                                tmpCV.getChiaveComp());
                        tmpComponenteErr = true;
                    }
                    if (statoCompCorrente.ctrlFirmeCRLCertRev) {
                        if (versamento.getStrutturaComponenti().isConfigFlagForzaControlloRevoca() || isForzaAccett
                                && esitoConfigurazione.isAccettaControlloCRLNegativo().booleanValue()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_005_001, tmpCV.getChiaveComp());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_005_001, tmpCV.getChiaveComp());
                            tmpComponenteErr = true;
                        }
                    }
                    if (statoCompCorrente.ctrlFirmeCRLScad) {
                        if (versamento.getStrutturaComponenti().isConfigFlagForzaControlloRevoca()
                                || isForzaAccett && esitoConfigurazione.isAccettaControlloCRLScaduta().booleanValue()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_005_001, tmpCV.getChiaveComp());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_005_001, tmpCV.getChiaveComp());
                            tmpComponenteErr = true;
                        }
                    }
                    if (statoCompCorrente.ctrlFirmeCRLNoValid) {
                        if (versamento.getStrutturaComponenti().isConfigFlagForzaControlloRevoca() || isForzaAccett
                                && esitoConfigurazione.isAccettaControlloCRLNoValida().booleanValue()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_005_001, tmpCV.getChiaveComp());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_005_001, tmpCV.getChiaveComp());
                            tmpComponenteErr = true;
                        }
                    }
                    if (statoCompCorrente.ctrlFirmeCRLNoScaric) {
                        if (versamento.getStrutturaComponenti().isConfigFlagForzaControlloRevoca() || isForzaAccett
                                && esitoConfigurazione.isAccettaControlloCRLNoScaric().booleanValue()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_005_001, tmpCV.getChiaveComp());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_005_001, tmpCV.getChiaveComp());
                            tmpComponenteErr = true;
                        }
                    }

                    // controllo OCSP
                    if (statoCompCorrente.ctrlFirmeOCSPWarning) {
                        versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                MessaggiWSBundle.FIRMA_007_001, tmpCV.getChiaveComp());
                        tmpComponenteWarn = true;
                    }
                    if (statoCompCorrente.ctrlFirmeOCSPNoValid) {
                        if (versamento.getStrutturaComponenti().isConfigFlagForzaControlloRevoca() || isForzaAccett
                                && esitoConfigurazione.isAccettaControlloOCSPNoValido().booleanValue()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_007_001, tmpCV.getChiaveComp());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_007_001, tmpCV.getChiaveComp());
                            tmpComponenteErr = true;
                        }
                    }
                    if (statoCompCorrente.ctrlFirmeOCSPRev) {
                        if (versamento.getStrutturaComponenti().isConfigFlagForzaControlloRevoca() || isForzaAccett
                                && esitoConfigurazione.isAccettaControlloOCSPNegativo().booleanValue()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_007_001, tmpCV.getChiaveComp());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_007_001, tmpCV.getChiaveComp());
                            tmpComponenteErr = true;
                        }
                    }
                    if (statoCompCorrente.ctrlFirmeOCSPNoScaric) {
                        if (versamento.getStrutturaComponenti().isConfigFlagForzaControlloRevoca() || isForzaAccett
                                && esitoConfigurazione.isAccettaControlloOCSPNoScaric().booleanValue()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_007_001, tmpCV.getChiaveComp());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FIRMA_007_001, tmpCV.getChiaveComp());
                            tmpComponenteErr = true;
                        }
                    }

                } // endif proseguiElaborazione - firme e marche

                /*
                 * esito componente, in base a firme, marche, formati
                 */
                if (tmpComponenteErr) {
                    tmpCV.getRifComponenteResp().getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                    statoDocumento.docEsitoCompHasNeg = true;
                } else if (tmpComponenteWarn) {
                    tmpCV.getRifComponenteResp().getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.WARNING);
                    statoDocumento.docEsitoCompHasWar = true;
                } else {
                    tmpCV.getRifComponenteResp().getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                }

            } // se sono un componente e sono di tipo file
        } // fine ---- for

        /*
         * fine ciclo sui componenti
         */
        // nuovo ciclo sui componenti/sottocomponenti di tipo FILE
        // per analisi dell'esito della verifica formati. va rifatto perchè deve
        // valutare sia i componenti che i sottocomponenti
        if (proseguiElaborazione) {
            // MEV#18660
            RispostaControlli rispostaControlli = this.verificaFormatiCompDoc(documento, statoDocumento, versamento,
                    esitoConfigurazione);

            if (rispostaControlli.getrLong() == -1) { // se il valore è -1, allora codErr è un codice di errore 666
                proseguiElaborazione = false;
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
            }
        }
        //
        if (proseguiElaborazione) {
            // VerificaFirmeDocumento
            /*
             * nota, questo esito non tiene conto di forzature varie
             */
            if (esitoDoc.isFirmatoDigitalmente().booleanValue()) {
                if (statoDocumento.verFirmeCompCodiceEsitoNeg) {
                    documento.setTiEsitoVerifFirme(SacerIndication.NEGATIVO.name());
                    documento.setDsMsgEsitoVerifica(SacerIndication.NEGATIVO.message());
                    esitoDoc.getEsitoDocumento().setVerificaFirmeDocumento(ECEsitoPosNegWarType.NEGATIVO);
                } else if (statoDocumento.verFirmeCompCodiceEsitoWar) {
                    documento.setTiEsitoVerifFirme(SacerIndication.WARNING.name());
                    documento.setDsMsgEsitoVerifica(SacerIndication.WARNING.message());
                    esitoDoc.getEsitoDocumento().setVerificaFirmeDocumento(ECEsitoPosNegWarType.WARNING);
                } else {
                    documento.setTiEsitoVerifFirme(SacerIndication.POSITIVO.name());
                    documento.setDsMsgEsitoVerifica(SacerIndication.POSITIVO.message());
                    esitoDoc.getEsitoDocumento().setVerificaFirmeDocumento(ECEsitoPosNegWarType.POSITIVO);
                }
            }

            /*
             * esito documento, in base a firme, marche, hash, formati e forzarure varie
             */
            // se almeno uno dei CodiceEsito Componente ha valore NEGATIVO
            if (statoDocumento.docEsitoCompHasNeg) {
                esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            } else if (statoDocumento.docEsitoCompHasWar) {
                // se almeno un tag CodiceEsito Componente ha valore WARNING e gli altri
                // POSITIVI
                esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.WARNING);
            } else {
                esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
            }
        }

        return proseguiElaborazione;
    }

    ///
    public void verificaDocumentoGenHash(DocumentoVers documento, AbsVersamentoExt versamento, IRispostaWS rispostaWs,
            ECConfigurazioneType esitoConfigurazione) {

        ECDocumentoType esitoDoc = documento.getRifDocumentoResp();
        StatoDocumento statoDocumento = new StatoDocumento();
        AvanzamentoWs myAvanzamentoWs = rispostaWs.getAvanzamento();
        // verifica firme e formati
        for (ComponenteVers tmpCV : documento.getFileAttesi()) {
            // se sono un componente di tipo file e non ci sono stati errori bloccanti nel
            // componente precedente
            if (tmpCV.getMySottoComponente() == null && tmpCV.getTipoSupporto() == ComponenteVers.TipiSupporto.FILE) {

                myAvanzamentoWs.setFase("inizio verifica componente hash")
                        .setDocumento(documento.getRifDocumento().getIDDocumento())
                        .setComponente(Long.toString(tmpCV.getMyComponente().getOrdinePresentazione()))
                        .logAvanzamento();

                boolean tmpComponenteErr = false;
                boolean tmpComponenteWarn = false;

                //
                // verifica hash, riguarda solo i componenti di tipo FILE,
                // privi di hash forzato (versamentoMM), per cui sia attiva la verifica hash
                // e per i quali sia stato possibile determinare encoding ed algoritmo
                // dell'hash versato.
                // Il calcolo dell'hash riguarda anche subcomp e componenti senza verifica,
                // che verranno gestiti come prima nel salvataggio.
                // questo è scomodo perché il calcolo dell'hash avviene in due
                // punti del codice, ma ha il vantaggio di non sprecare tempo
                // nel calcolo di hash "inutili" nel caso si verifichino problemi
                // prima di arrivare al salvataggio
                //
                // <editor-fold defaultstate="collapsed" desc="Controllo hash">
                if (versamento.getStrutturaComponenti().isFlagVerificaHash() && !tmpCV.isHashForzato()
                        && !tmpCV.isHashVersNonDefinito()) {
                    RispostaControlli rispostaControlli = this.elabAroCompDocHash(tmpCV);
                    if (rispostaControlli.getrLong() == -1) { // se il valore è -1, allora codErr è un codice di errore
                        // 666
                        rispostaWs.setSeverity(SeverityEnum.ERROR);
                        rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                    } else if (tmpCV.withAcdEntity().getTiEsitoContrHashVers()
                            .equals(CostantiDB.TipiEsitoVerificaHash.NEGATIVO.name())) {
                        if (versamento.getStrutturaComponenti().isFlagAccettaHashErrato()) {
                            versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.HASH_004_001, tmpCV.getChiaveComp(),
                                    tmpCV.getHashVersatoEncoding(), tmpCV.getHashVersatoAlgoritmo());
                            tmpComponenteWarn = true;
                        } else {
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.HASH_004_001, tmpCV.getChiaveComp(),
                                    tmpCV.getHashVersatoEncoding(), tmpCV.getHashVersatoAlgoritmo());
                            tmpComponenteErr = true;
                        }
                    }
                }

                /*
                 * esito componente, in base a firme, marche, hash e forzarure varie
                 */
                if (tmpComponenteErr) {
                    tmpCV.getRifComponenteResp().getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                    statoDocumento.docEsitoCompHasNeg = true;
                } else if (tmpComponenteWarn) {
                    tmpCV.getRifComponenteResp().getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.WARNING);
                    statoDocumento.docEsitoCompHasWar = true;
                } else {
                    tmpCV.getRifComponenteResp().getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                }

            } // se sono un componente e sono di tipo file
        } // fine ---- for

        /*
         * fine ciclo sui componenti
         */
        //
        /*
         * esito documento, in base a firme, marche, hash, formati e forzarure varie
         */
        // se almeno uno dei CodiceEsito Componente ha valore NEGATIVO
        if (statoDocumento.docEsitoCompHasNeg) {
            esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
        } else if (statoDocumento.docEsitoCompHasWar) {
            // se almeno un tag CodiceEsito Componente ha valore WARNING e gli altri
            // POSITIVI
            esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.WARNING);
        } else {
            esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
        }
    }

    private RispostaControlli verificaFormatiCompDoc(DocumentoVers documento, StatoDocumento statoDoc,
            AbsVersamentoExt versamento, ECConfigurazioneType esitoConfigurazione) {
        //
        RispostaControlli rc = new RispostaControlli();
        rc.setrLong(0); // se in uscita vale -1, allora il chiamante lo interpreta come eccezione

        // nuovo ciclo sui componenti/sottocomponenti di tipo FILE
        // per verificare i formati
        for (ComponenteVers tmpCV : documento.getFileAttesi()) {
            if (tmpCV.getTipoSupporto() == ComponenteVers.TipiSupporto.FILE) {
                CompDocMock mock = tmpCV.withAcdEntity();
                String tmpIdoneitaStdSbustato = null;
                boolean isComponente = (tmpCV.getMySottoComponente() == null);
                boolean isAccettFormNeg = esitoConfigurazione.isAccettaControlloFormatoNegativo();
                boolean isForzaAccettFormato = esitoConfigurazione.isForzaAccettazione()
                        || versamento.getStrutturaComponenti().isConfigFlagForzaFormato();
                // MEV#14881
                boolean isForzaFormato = versamento.getStrutturaComponenti().isConfigFlagForzaFormato();
                boolean isForzaAccettazione = esitoConfigurazione.isForzaAccettazione();

                String tagFormatRapp = null;
                String tagFormatRappEsteso = null;
                ECEsitoIdonFormatoType tagIdoneitaFormato = ECEsitoIdonFormatoType.GESTITO;
                ECEsitoPosNegWarDisType tagEsitoRiconoscimentoFormato = ECEsitoPosNegWarDisType.DISABILITATO;
                String tagMessRiconoscimentoFormato = null;
                ECEsitoPosNegWarType tagEsitoComponente = ECEsitoPosNegWarType.POSITIVO;

                tagFormatRapp = mock.getDsFormatoRapprCalc();
                tagFormatRappEsteso = mock.getDsFormatoRapprEstesoCalc();
                tagMessRiconoscimentoFormato = mock.getDsMsgEsitoContrFormato();

                // MEV#18660
                DecFormatoFileStandard tmpDecFormatoFileStandard = null;
                if (mock.getIdDecFormatoFileStandard() != null) {
                    try {
                        tmpDecFormatoFileStandard = controlliPerFirme
                                .getDecFormatoFileStandardAsEntity(mock.getIdDecFormatoFileStandard().longValue());
                    } catch (VerificaFirmaException e) {
                        rc.setCodErr(MessaggiWSBundle.ERR_666);
                        rc.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                                "ControlliPerFirme.getDecFormatoFileStandard: " + e.getMessage()));
                        return rc;
                    }
                }

                // verifico per errori
                if ((mock.getTiEsitoContrFormatoFile().equals(SacerIndication.POSITIVO.name())
                        || mock.getTiEsitoContrFormatoFile().equals(SacerIndication.WARNING.name()))
                        && tmpDecFormatoFileStandard != null/* MEV#18660 */) {
                    // caso positivo o warning
                    tmpIdoneitaStdSbustato = tmpDecFormatoFileStandard.getTiEsitoContrFormato();

                    tagIdoneitaFormato = ECEsitoIdonFormatoType.valueOf(tmpIdoneitaStdSbustato);
                    tagEsitoRiconoscimentoFormato = ECEsitoPosNegWarDisType.valueOf(mock.getTiEsitoContrFormatoFile());

                    // imposta anche esitoCompHasWar
                    if (mock.getTiEsitoContrFormatoFile().equals(SacerIndication.WARNING.name())) {
                        tagEsitoComponente = ECEsitoPosNegWarType.WARNING;
                        statoDoc.docEsitoCompHasWar = true; // propago la condizione di warning del componente
                        versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                MessaggiWSBundle.FORMATO_001_001, tmpCV.getChiaveComp(), tagMessRiconoscimentoFormato);
                    } else {
                        tagEsitoComponente = ECEsitoPosNegWarType.POSITIVO;
                    }
                } else if (mock.getTiEsitoContrFormatoFile().equals(SacerIndication.NEGATIVO.name())) {
                    // caso negativo
                    if (tmpDecFormatoFileStandard != null/* MEV#18660 */) {
                        tmpIdoneitaStdSbustato = tmpDecFormatoFileStandard.getTiEsitoContrFormato();
                        tagIdoneitaFormato = ECEsitoIdonFormatoType.valueOf(tmpIdoneitaStdSbustato);
                    } else {
                        tagIdoneitaFormato = null;
                    }

                    if (isComponente) { // è un componente
                        if (isForzaFormato || isAccettFormNeg && isForzaAccettazione) { // se ammette errori
                            if (this.isTipoFileAmmesso(mock.getDsFormatoRapprCalc(),
                                    versamento.getStrutturaComponenti().getIdStruttura(),
                                    tmpCV.getIdTipoComponente())) { // se ammette errori e se il formato calcolato è
                                // ammesso
                                tagEsitoRiconoscimentoFormato = ECEsitoPosNegWarDisType.WARNING;
                                tagEsitoComponente = ECEsitoPosNegWarType.WARNING;
                                statoDoc.docEsitoCompHasWar = true; // propago la condizione di warning del componente
                                versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                        MessaggiWSBundle.FORMATO_001_001, tmpCV.getChiaveComp(),
                                        tagMessRiconoscimentoFormato);
                            } else { // se invece il formato non è ammesso
                                tagEsitoRiconoscimentoFormato = ECEsitoPosNegWarDisType.NEGATIVO;
                                tagEsitoComponente = ECEsitoPosNegWarType.NEGATIVO;
                                statoDoc.docEsitoCompHasNeg = true; // propago la condizione di errore del componente
                                versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                        MessaggiWSBundle.FORMATO_001_001, tmpCV.getChiaveComp(),
                                        tagMessRiconoscimentoFormato
                                                + ". Il formato calcolato non è ammesso per il tipo componente.");
                            }
                        } else { // se il componente non ammette errori
                            tagEsitoRiconoscimentoFormato = ECEsitoPosNegWarDisType.NEGATIVO;
                            tagEsitoComponente = ECEsitoPosNegWarType.NEGATIVO;
                            statoDoc.docEsitoCompHasNeg = true; // propago la condizione di errore del componente
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FORMATO_001_001, tmpCV.getChiaveComp(),
                                    tagMessRiconoscimentoFormato);
                        }
                    } else // è un sottocomponente
                    {
                        // MEV#15880 : "rilassato" il vincolo su "forzatura"
                        // esistono casi particolari in cui al sottocomponente viene associato un
                        // formato standard (vedi caso del TSR con formato P7M da estrazione formato)
                        // superfluo verificare ANCHE che non sia associato un formato (già ottenuto
                        // l'errore sul formato vedi 606)
                        // attraverso i flag sarà possibile decidere se l'esito dovrà essere negativo o
                        // warning
                        if (tmpCV.getTipoUso().equals("MARCA")) { // se è una marca
                            if (isForzaAccettFormato /* && tmpTabCD.getDecFormatoFileStandard() == null */
                                    && esitoConfigurazione.isAccettaMarcaSconosciuta().booleanValue()) {
                                tagEsitoRiconoscimentoFormato = ECEsitoPosNegWarDisType.WARNING;
                                tagEsitoComponente = ECEsitoPosNegWarType.WARNING;
                                statoDoc.docEsitoCompHasWar = true; // propago la condizione di warning del componente
                                versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                        MessaggiWSBundle.FORMATO_001_003, tmpCV.getChiaveComp(),
                                        tagMessRiconoscimentoFormato);
                            } else {
                                tagEsitoRiconoscimentoFormato = ECEsitoPosNegWarDisType.NEGATIVO;
                                tagEsitoComponente = ECEsitoPosNegWarType.NEGATIVO;
                                statoDoc.docEsitoCompHasNeg = true; // propago la condizione di errore del componente
                                versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                        MessaggiWSBundle.FORMATO_001_003, tmpCV.getChiaveComp(),
                                        tagMessRiconoscimentoFormato);
                            }
                        } else if (tmpCV.getTipoUso().equals("FIRMA")) { // se è una firma
                            if (isForzaAccettFormato /* && tmpTabCD.getDecFormatoFileStandard() == null */
                                    && esitoConfigurazione.isAccettaFirmaSconosciuta().booleanValue()) {
                                tagEsitoRiconoscimentoFormato = ECEsitoPosNegWarDisType.WARNING;
                                tagEsitoComponente = ECEsitoPosNegWarType.WARNING;
                                statoDoc.docEsitoCompHasWar = true; // propago la condizione di warning del componente
                                versamento.listErrAddWarningDoc(documento, tmpCV.getChiaveComp(),
                                        MessaggiWSBundle.FORMATO_001_002, tmpCV.getChiaveComp(),
                                        tagMessRiconoscimentoFormato);
                            } else {
                                tagEsitoRiconoscimentoFormato = ECEsitoPosNegWarDisType.NEGATIVO;
                                tagEsitoComponente = ECEsitoPosNegWarType.NEGATIVO;
                                statoDoc.docEsitoCompHasNeg = true; // propago la condizione di errore del componente
                                versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                        MessaggiWSBundle.FORMATO_001_002, tmpCV.getChiaveComp(),
                                        tagMessRiconoscimentoFormato);
                            }
                        } else { // se è un sottocomponente generico
                            tagEsitoRiconoscimentoFormato = ECEsitoPosNegWarDisType.NEGATIVO;
                            tagEsitoComponente = ECEsitoPosNegWarType.NEGATIVO;
                            statoDoc.docEsitoCompHasNeg = true; // propago la condizione di errore del componente
                            versamento.listErrAddErrorDoc(documento, tmpCV.getChiaveComp(),
                                    MessaggiWSBundle.FORMATO_001_004, tmpCV.getChiaveComp(),
                                    tagMessRiconoscimentoFormato);
                        }
                    }
                } else if (mock.getTiEsitoContrFormatoFile().equals(SacerIndication.DISABILITATO.name())) {
                    // caso disattivato
                    if (tmpDecFormatoFileStandard != null) {
                        tmpIdoneitaStdSbustato = tmpDecFormatoFileStandard.getTiEsitoContrFormato();
                        tagIdoneitaFormato = ECEsitoIdonFormatoType.valueOf(tmpIdoneitaStdSbustato);
                    } else {
                        tagIdoneitaFormato = null;
                    }

                    tagEsitoRiconoscimentoFormato = ECEsitoPosNegWarDisType.DISABILITATO;
                    tagEsitoComponente = ECEsitoPosNegWarType.POSITIVO;
                    // esitoCompHasWar e esitoCompHasNeg restano inalterati perché questo caso si
                    // considera sempre
                    // come positivo
                }

                /*
                 * imposta l'esito del componente/sottocompenente processato
                 */
                // verifica se è un componente o un sottocomp.
                if (isComponente) { // componente
                    if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_VERIFICA_FORMATI_1_25)) { // versione
                        // WS
                        // successiva
                        // alla
                        // 1.2
                        tmpCV.getRifComponenteResp().setFormatoRappresentazione(tagFormatRapp);
                        tmpCV.getRifComponenteResp().setFormatoRappresentazioneEsteso(tagFormatRappEsteso);
                        tmpCV.getRifComponenteResp().setIdoneitaFormato(tagIdoneitaFormato);
                        tmpCV.getRifComponenteResp().getEsitoComponente()
                                .setVerificaRiconoscimentoFormato(tagEsitoRiconoscimentoFormato);
                        tmpCV.getRifComponenteResp().getEsitoComponente()
                                .setMessaggioRiconoscimentoFormato(tagMessRiconoscimentoFormato);
                    } else if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_VERIFICA_FORMATI_OLD)) { // versione
                        // ws
                        // 1.2
                        // e
                        // precedenti
                        if (tagEsitoRiconoscimentoFormato == ECEsitoPosNegWarDisType.POSITIVO
                                || tagEsitoRiconoscimentoFormato == ECEsitoPosNegWarDisType.NEGATIVO
                                || tagEsitoRiconoscimentoFormato == ECEsitoPosNegWarDisType.WARNING) {
                            tmpCV.getRifComponenteResp().getEsitoComponente()
                                    .setVerificaRiconoscimentoFormato(tagEsitoRiconoscimentoFormato);
                        }
                    }

                    /*
                     * se il precente esito - impostato dalla verifica firma - è positivo, allora l'esito prende il
                     * valore della verifica formato. se il precente esito è warning, l'esito prende il valore della
                     * verifica formato solo se questa non è positiva. altrimenti il valore resta inalterato, cioè
                     * negativo
                     */
                    if (tmpCV.getRifComponenteResp().getEsitoComponente()
                            .getCodiceEsito() == ECEsitoPosNegWarType.POSITIVO
                            || tmpCV.getRifComponenteResp().getEsitoComponente()
                                    .getCodiceEsito() == ECEsitoPosNegWarType.WARNING
                                    && tagEsitoComponente != ECEsitoPosNegWarType.POSITIVO) {
                        tmpCV.getRifComponenteResp().getEsitoComponente().setCodiceEsito(tagEsitoComponente);
                    }
                } else { // sottocomponente
                    if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_VERIFICA_FORMATI_1_25)) { // versione
                        // WS
                        // successiva
                        // alla
                        // 1.2
                        tmpCV.getRifSottoComponenteResp().setFormatoRappresentazione(tagFormatRapp);
                        tmpCV.getRifSottoComponenteResp().setFormatoRappresentazioneEsteso(tagFormatRappEsteso);
                        tmpCV.getRifSottoComponenteResp().setIdoneitaFormato(tagIdoneitaFormato);
                        tmpCV.getRifSottoComponenteResp().getEsitoSottoComponente()
                                .setMessaggioRiconoscimentoFormato(tagMessRiconoscimentoFormato);
                        tmpCV.getRifSottoComponenteResp().getEsitoSottoComponente()
                                .setVerificaRiconoscimentoFormato(tagEsitoRiconoscimentoFormato);
                    } else if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_VERIFICA_FORMATI_OLD)) { // versione
                        // ws
                        // 1.2
                        // e
                        // precedenti
                        if (tagEsitoRiconoscimentoFormato == ECEsitoPosNegWarDisType.POSITIVO
                                || tagEsitoRiconoscimentoFormato == ECEsitoPosNegWarDisType.NEGATIVO) {
                            tmpCV.getRifSottoComponenteResp().getEsitoSottoComponente()
                                    .setVerificaRiconoscimentoFormato(tagEsitoRiconoscimentoFormato);
                        } else if (tagEsitoRiconoscimentoFormato == ECEsitoPosNegWarDisType.WARNING) { // l'esito del
                            // sottocomp non
                            // prevede il
                            // WARNING nel ws
                            // 1.2
                            tmpCV.getRifSottoComponenteResp().getEsitoSottoComponente()
                                    .setVerificaRiconoscimentoFormato(ECEsitoPosNegWarDisType.NEGATIVO);
                        }
                    }

                    /*
                     * se il precedente esito - impostato dalla verifica firma - è positivo, allora l'esito prende il
                     * valore della verifica formato. se il precente esito è warning, l'esito prende il valore della
                     * verifica formato solo se questa non è positiva. altrimenti il valore resta inalterato, cioè
                     * negativo
                     */
                    if (tmpCV.getRifSottoComponenteResp().getEsitoSottoComponente()
                            .getCodiceEsito() == ECEsitoPosNegWarType.POSITIVO
                            || tmpCV.getRifSottoComponenteResp().getEsitoSottoComponente()
                                    .getCodiceEsito() == ECEsitoPosNegWarType.WARNING
                                    && tagEsitoComponente != ECEsitoPosNegWarType.POSITIVO) {
                        tmpCV.getRifSottoComponenteResp().getEsitoSottoComponente().setCodiceEsito(tagEsitoComponente);
                    }
                }
            }
        }

        return rc;
    }

    private boolean isTipoFileAmmesso(String nomeFormatoFile, long idStrut, long idTipoCompDoc) {
        RispostaControlli rc;
        // verifica sul database se il nome del formato calcolato dalla libreria è tra i
        // formati
        // ammessi.
        // Nel caso il formato calcolato fosse SCONOSCIUTO (estensione fittizia '???'),
        // cioè non fosse censito tra i formati standard, questo verrebbe rifiutato

        rc = controlliSemantici.checkFormatoFileVersato(nomeFormatoFile, idStrut, idTipoCompDoc);

        return rc.isrBoolean();
    }

    private RispostaControlli elabAroCompDocHash(ComponenteVers componente) {
        // confronto l'hash versato e quello calcolato
        // nel caso dei sottocomponenti non li devo verificare
        // ma li calcolo al salvataggio, così come gli hash dei componenti per cui non è
        // attivo il controllo
        RispostaControlli rc = new RispostaControlli();
        File tmpFile = componente.getRifFileBinario().getFileSuDisco();
        byte[] tmpHashControllato;
        CompDocMock mock = componente.withAcdEntity();
        try {
            Hashresult hr = HashUtility.calculate(new FileInputStream(tmpFile));
            switch (componente.getHashVersatoAlgoritmo()) {
            case SHA_256:
                tmpHashControllato = hr.getHashSha256();
                break;
            case SHA_1:
                tmpHashControllato = hr.getHashSha1();
                break;
            case MD5:
                tmpHashControllato = hr.getHashMd5();
                break;
            case SHA_224:
                tmpHashControllato = hr.getHashSha224();
                break;
            case SHA_384:
                tmpHashControllato = hr.getHashSha384();
                break;
            case SHA_512:
                tmpHashControllato = hr.getHashSha512();
                break;

            default:
                throw new NoSuchAlgorithmException(
                        "L'argoritmo hash determinato non è supportato da HashUtility.calculate()");
            }
            if (Arrays.equals(componente.getHashVersato(), tmpHashControllato)) {
                mock.setTiEsitoContrHashVers(CostantiDB.TipiEsitoVerificaHash.POSITIVO.name());
            } else {
                mock.setTiEsitoContrHashVers(CostantiDB.TipiEsitoVerificaHash.NEGATIVO.name());
            }

            // memorizzo l'hash da me calcolato su cui ho effettuato il confronto
            // direttamente nella entity, usando l'encoding che ho dedotto dal versamento
            if (componente.getHashVersatoEncoding() == CostantiDB.TipiEncBinari.HEX_BINARY) {
                mock.setDsHashFileContr(BinEncUtility.encodeUTF8HexString(tmpHashControllato));
            } else {
                mock.setDsHashFileContr(BinEncUtility.encodeUTF8Base64String(tmpHashControllato));
            }

            // visto che l'ho calcolato, memorizzo l'hash SHA-256, per non doverlo
            // ricalcolare al salvataggio
            componente.setHashCalcolato(hr.getHashSha256());
            // OK
            rc.setrLong(0);
        } catch (IOException | NoSuchAlgorithmException e) {
            rc.setrLong(-1); // se in uscita vale -1, allora il chiamante lo interpreta come eccezione
            rc.setCodErr(MessaggiWSBundle.ERR_666);
            rc.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "getAroCompDocHash: Errore imprevisto in HashUtility.calculate(): "
                            + ExceptionUtils.getRootCauseMessage(e)));
            log.error("Eccezione nel processo di verifica hash", e);
        }
        return rc;
    }

    private RispostaControlli elabCompDocMockWithVerificaFirmaAndFormatoResult(ComponenteVers componente,
            List<ComponenteVers> sottoComponentiFirma, List<ComponenteVers> sottoComponentiMarca,
            List<ComponenteVers> sottoComponentiAltri, long idTipoUd, AbsVersamentoExt versamento,
            ECConfigurazioneType esitoConfigurazione) {
        RispostaControlli rs = new RispostaControlli();
        rs.setrLong(-1); // se in uscita vale -1, allora il chiamante lo interpreta come eccezione
        List<ComponenteVers> tmpEmptyComponenteVerses = new ArrayList<>();

        // verifica se per il componente da verificare ci sono forzature
        // (questo è possibile solo nel caso di versamento MM oppure la struttura
        // versante abbia attivo il flag di forzatura)
        // verifica se è un versamento multimedia e c'è un formato forzato.
        // da notare che in questo caso viene di fatto ignorato l'eventuale flag di
        // forzatura della struttura
        if (componente.getRifComponenteMM() != null && componente.getRifComponenteMM().isForzaFirmeFormati()) {
            /*
             * caso della forzatura di formato del file in un versamento multimedia Lo stato di verifica firma viene
             * fissato a "non firmato"
             */
            if (sottoComponentiFirma.isEmpty() && sottoComponentiMarca.isEmpty() && sottoComponentiAltri.isEmpty()) {
                it.eng.parer.ws.xml.versReqMultiMedia.ComponenteType tmpComponente = componente.getRifComponenteMM()
                        .getMyComponenteMM();
                CompDocMock mock = componente.withAcdEntity();

                mock.setTiEsitoContrFormatoFile(SacerIndication.DISABILITATO.name());
                mock.setFlNoCalcFmtVerifFirme("1");
                mock.setDsMsgEsitoVerifFirme(null);

                // MEV#18660
                rs.setrLong(0); // l'eventuale valore errato non verrà interpretato come eccezione
                if (tmpComponente.getForzaFormato() != null) {
                    DecFormatoFileStandard tmpFormatoFileStandard;
                    rs = controlliPerFirme.confrontaFormati(componente.getRifComponenteMM().getIdFormatoFileCalc(),
                            componente.getIdFormatoFileVers());
                    if (rs.getrLong() != -1) { // i controlli del formato potrebbero aver reso un'eccezione "666"
                        tmpFormatoFileStandard = (DecFormatoFileStandard) rs.getrObject();
                        mock.setIdDecFormatoFileStandard(
                                new BigDecimal(tmpFormatoFileStandard.getIdFormatoFileStandard()));

                        mock.setDsFormatoRapprCalc(
                                tmpComponente.getForzaFormato().getFormatoRappresentazioneCompatto());
                        mock.setDsFormatoRapprEstesoCalc(
                                tmpComponente.getForzaFormato().getFormatoRappresentazioneEsteso());

                        if (rs.isrBoolean()) {
                            mock.setTiEsitoContrFormatoFile(SacerIndication.POSITIVO.name());
                            mock.setDsMsgEsitoContrFormato("Controllo OK");
                        } else {
                            mock.setTiEsitoContrFormatoFile(SacerIndication.NEGATIVO.name());
                            mock.setDsMsgEsitoContrFormato(rs.getrString());
                        }
                    }
                }
            } else {
                rs.setCodErr(MessaggiWSBundle.ERR_666);
                rs.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "getAroCompDocDecorated: Errore interno: si può forzare il formato solo su componenti privi di sottocomponenti"));
                log.error("Errore interno: si può forzare il formato solo su componenti privi di sottocomponenti");
            }
        } else if (versamento.getStrutturaComponenti().isStrutturaForzaFormato()) {
            // verifica il flag di forzatura del versamento, imposto dal tipo-struttura.
            /*
             * caso della forzatura di formato del file a causa del tipo struttura Lo stato di verifica firma viene
             * fissato a "non firmato"
             */
            if (sottoComponentiFirma.isEmpty() && sottoComponentiMarca.isEmpty() && sottoComponentiAltri.isEmpty()) {
                CompDocMock mock = componente.withAcdEntity();

                mock.setTiEsitoContrFormatoFile(SacerIndication.DISABILITATO.name());
                mock.setFlNoCalcFmtVerifFirme("1");
                mock.setDsMsgEsitoVerifFirme(null);

            } else {
                rs.setCodErr(MessaggiWSBundle.ERR_666);
                rs.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "getAroCompDocDecorated: Errore interno: si può forzare il formato solo su componenti privi di sottocomponenti"));
                log.error("Errore interno: si può forzare il formato solo su componenti privi di sottocomponenti");
            }
        } else {
            /*
             * caso della verifica normale di firme e formati il flag relativo al NON calcolo di firma e formato viene
             * posto a "0"
             */
            ZonedDateTime rifTempVers = componente.withAcdEntity().getTmRifTempVers();
            if (rifTempVers == null) {
                Instant dataVersamento = versamento.getStrutturaComponenti().getDataVersamento().toInstant();

                rifTempVers = ZonedDateTime.ofInstant(dataVersamento, ZoneId.systemDefault());
            }

            // sottopongo a verifca il componente e i suoi sottocomponenti firme/marche
            // detached
            rs = firmeFormatiEjb.verifica(componente, sottoComponentiFirma, sottoComponentiMarca, rifTempVers, idTipoUd,
                    versamento);
            if (rs.getrLong() == -1) {
                return rs; // error
            }
            componente.withAcdEntity().setFlNoCalcFmtVerifFirme("0");

            // sottopongo a verifica tutti i sottocomponenti che non sono marche o firme: mi
            // servirà per l'esito del controllo formati
            for (ComponenteVers tmpComponenteVers : sottoComponentiAltri) {
                rs = firmeFormatiEjb.verifica(tmpComponenteVers, tmpEmptyComponenteVerses, tmpEmptyComponenteVerses,
                        rifTempVers, idTipoUd, versamento);
                if (rs.getrLong() == -1) {
                    return rs; // error
                }
                tmpComponenteVers.withAcdEntity().setFlNoCalcFmtVerifFirme("0");
            }
            rs.setrLong(0); // l'eventuale valore errato non verrà interpretato come eccezione
        }

        return rs;
    }

}
