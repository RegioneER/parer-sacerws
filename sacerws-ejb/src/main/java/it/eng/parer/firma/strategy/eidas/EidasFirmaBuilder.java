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

package it.eng.parer.firma.strategy.eidas;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.esig.dss.detailedreport.jaxb.XmlBasicBuildingBlocks;
import eu.europa.esig.dss.detailedreport.jaxb.XmlCV;
import eu.europa.esig.dss.detailedreport.jaxb.XmlConstraint;
import eu.europa.esig.dss.detailedreport.jaxb.XmlConstraintsConclusion;
import eu.europa.esig.dss.detailedreport.jaxb.XmlISC;
import eu.europa.esig.dss.detailedreport.jaxb.XmlRAC;
import eu.europa.esig.dss.detailedreport.jaxb.XmlSAV;
import eu.europa.esig.dss.detailedreport.jaxb.XmlStatus;
import eu.europa.esig.dss.detailedreport.jaxb.XmlSubXCV;
import eu.europa.esig.dss.detailedreport.jaxb.XmlXCV;
import eu.europa.esig.dss.diagnostic.CertificateRevocationWrapper;
import eu.europa.esig.dss.diagnostic.CertificateWrapper;
import eu.europa.esig.dss.diagnostic.RevocationWrapper;
import eu.europa.esig.dss.diagnostic.SignatureWrapper;
import eu.europa.esig.dss.diagnostic.TimestampWrapper;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.enumerations.RevocationType;
import eu.europa.esig.dss.enumerations.SubIndication;
import eu.europa.esig.dss.validation.reports.Reports;
import it.eng.parer.eidas.model.EidasWSReportsDTOTree;
import it.eng.parer.firma.util.VerificaFirmaEnums;
import it.eng.parer.firma.util.VerificaFirmaEnums.SacerIndication;
import it.eng.parer.firma.xml.VFAdditionalInfoFirmaCompType;
import it.eng.parer.firma.xml.VFCertifCaType;
import it.eng.parer.firma.xml.VFCertifFirmatarioType;
import it.eng.parer.firma.xml.VFContrFirmaCompType;
import it.eng.parer.firma.xml.VFCrlType;
import it.eng.parer.firma.xml.VFFirmaCompType;
import it.eng.parer.firma.xml.VFOcspType;
import it.eng.parer.firma.xml.VFTipoControlloType;
import it.eng.parer.firma.xml.VFTipoFirmaType;
import it.eng.parer.firma.xml.VFUrlDistribCrlType;
import it.eng.parer.firma.xml.VFUrlDistribOcspType;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.ParametroApplDB.ParametroApplFl;
import it.eng.parer.ws.utils.XmlDateUtility;

public class EidasFirmaBuilder extends EidasBaseWrapperResult implements IEidasBuilderVFObj<VFFirmaCompType> {

    private static final Logger LOG = LoggerFactory.getLogger(EidasFirmaBuilder.class);

    private Map<String, Boolean> controlliAbilitati;
    private boolean isDataDiRiferimentoOnCompVers;
    private Set<Costanti.ModificatoriWS> modificatoriWSCalc;

    public EidasFirmaBuilder(Map<String, Boolean> controlliAbilitati, boolean isDataDiRiferimentoOnCompVers,
            Set<Costanti.ModificatoriWS> modificatoriWSCalc) {
        super();
        this.controlliAbilitati = controlliAbilitati;
        this.isDataDiRiferimentoOnCompVers = isDataDiRiferimentoOnCompVers;
        this.modificatoriWSCalc = modificatoriWSCalc;
    }

    @Override
    public VFFirmaCompType build(EidasWSReportsDTOTree eidasReportsDto, VerificaFirmaWrapper vfWrapper,
            SignatureWrapper signatureW, Optional<TimestampWrapper> timestampW, ZonedDateTime dataDiRiferimento,
            BigDecimal[] pgs)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        // reports by dss
        Reports reports = new Reports(eidasReportsDto.getReport().getDiagnosticData(),
                eidasReportsDto.getReport().getDetailedReport(), eidasReportsDto.getReport().getSimpleReport(),
                null /* validation report non gestito */);

        // FIRMA
        VFFirmaCompType firmaCompType = new VFFirmaCompType();
        // set setPgFirma
        firmaCompType.setPgFirma(pgs[PG_FIRMA]);
        // set id (id componente PARER)
        firmaCompType.setId(eidasReportsDto.getIdComponente());

        // add info (empty)
        VFAdditionalInfoFirmaCompType additionalInfoFirmaCompType = new VFAdditionalInfoFirmaCompType();
        firmaCompType.setAdditionalInfo(additionalInfoFirmaCompType);

        // test
        EidasWrapperResultControl.fieldCannotBeNull(signatureW, "signingCertificate", Optional.empty());
        // SIGNATURE CERTIFICATE
        // certificate
        CertificateWrapper xmlcertificate = reports.getDiagnosticData()
                .getUsedCertificateById(signatureW.getSigningCertificate().getId());

        // FIRMATARIO + BLOB
        VFCertifFirmatarioType certifFirmatarioType = new VFCertifFirmatarioType();
        firmaCompType.setCertifFirmatario(certifFirmatarioType);

        // test
        EidasWrapperResultControl.fieldCannotBeNull(xmlcertificate, "serialNumber",
                Optional.ofNullable(xmlcertificate.getId()));
        certifFirmatarioType.setNiSerialCertifFirmatario(new BigDecimal(xmlcertificate.getSerialNumber()));

        // test
        EidasWrapperResultControl.fieldCannotBeNull(xmlcertificate, "notBefore",
                Optional.ofNullable(xmlcertificate.getId()));
        certifFirmatarioType
                .setDtIniValCertifFirmatario(XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificate.getNotBefore()));

        // test
        EidasWrapperResultControl.fieldCannotBeNull(xmlcertificate, "notAfter",
                Optional.ofNullable(xmlcertificate.getId()));
        certifFirmatarioType
                .setDtFinValCertifFirmatario(XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificate.getNotAfter()));

        // CA
        String certificateCAId = getCertificateCAId(signatureW, xmlcertificate);
        //
        CertificateWrapper xmlcertificateCA = reports.getDiagnosticData().getUsedCertificateById(certificateCAId);

        VFCertifCaType certifCaType = new VFCertifCaType();
        certifFirmatarioType.setCertifCaFirmatario(certifCaType);

        // test
        EidasWrapperResultControl.fieldCannotBeNull(xmlcertificateCA, "serialNumber",
                Optional.ofNullable(xmlcertificateCA.getId()));
        certifCaType.setNiSerialCertifCa(new BigDecimal(xmlcertificateCA.getSerialNumber()));

        // test
        EidasWrapperResultControl.fieldCannotBeNull(xmlcertificateCA, "notBefore",
                Optional.ofNullable(xmlcertificateCA.getId()));
        certifCaType.setDtIniValCertifCa(XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificateCA.getNotBefore()));

        // test
        EidasWrapperResultControl.fieldCannotBeNull(xmlcertificateCA, "notAfter",
                Optional.ofNullable(xmlcertificateCA.getId()));
        certifCaType.setDtFinValCertifCa(XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificateCA.getNotAfter()));
        // Note: report EIDAS doens't cover it ! ( SubjectKeyId non presente su rerport)
        // certifCaType.setDsSubjectKeyId();
        certifCaType.setDlDnIssuerCertifCa(xmlcertificateCA.getCertificateIssuerDN());
        certifCaType.setDlDnSubjectCertifCa(xmlcertificateCA.getCertificateDN());

        int urlIdx = 1;
        // check NON empty URL
        List<String> crlFiltered = xmlcertificateCA.getCRLDistributionPoints().stream().filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        for (String url : crlFiltered) {
            VFUrlDistribCrlType urlDistribCrlType = new VFUrlDistribCrlType();
            urlDistribCrlType.setDlUrlDistribCrl(url);
            urlDistribCrlType.setNiOrdUrlDistribCrl(new BigDecimal(urlIdx));

            certifCaType.getUrlDistribCrls().add(urlDistribCrlType);
            // inc idx
            urlIdx++;
        }
        // ocsp (from CA+certificate)
        Iterable<String> ocspCombinedUrls = CollectionUtils
                .union(xmlcertificateCA.getOCSPAccessUrls(), xmlcertificate.getOCSPAccessUrls()).stream()
                .filter(StringUtils::isNotBlank).collect(Collectors.toList());
        urlIdx = 1;
        for (String url : ocspCombinedUrls) {
            VFUrlDistribOcspType urlDistribOcspType = new VFUrlDistribOcspType();
            urlDistribOcspType.setDlUrlDistribOcsp(url);
            urlDistribOcspType.setNiOrdUrlDistribOcsp(new BigDecimal(urlIdx));

            certifCaType.getUrlDistribOcsps().add(urlDistribOcspType);
            // inc idx
            urlIdx++;
        }

        // FIRMA : CA
        firmaCompType.setCertifCa(certifCaType);
        // nullable element
        String dsAlgoFirma = null;
        if (signatureW.getDigestAlgorithm() != null && signatureW.getEncryptionAlgorithm() != null) {
            dsAlgoFirma = signatureW.getDigestAlgorithm().getName().concat("with")
                    .concat(signatureW.getEncryptionAlgorithm().getName());
        }
        firmaCompType.setDsAlgoFirma(dsAlgoFirma);
        firmaCompType.setDlDnFirmatario(xmlcertificate.getCertificateDN());
        firmaCompType.setDsFirmaBase64(Base64.encodeBase64String(signatureW.getSignatureValue()));

        String cdFirmatario = null;
        if (StringUtils.isNotBlank(xmlcertificate.getSubjectSerialNumber())) {
            cdFirmatario = xmlcertificate.getSubjectSerialNumber();
        } else {
            // test
            EidasWrapperResultControl.fieldCannotBeNull(xmlcertificate, "serialNumber",
                    Optional.ofNullable(xmlcertificate.getId()));
            cdFirmatario = xmlcertificate.getSerialNumber();
        }
        firmaCompType.setCdFirmatario(cdFirmatario);
        firmaCompType.setDtFirma(XmlDateUtility.dateToXMLGregorianCalendar(signatureW.getClaimedSigningTime()));

        //
        if (!signatureW.getTimestampList().isEmpty()) {
            firmaCompType
                    .setTmRifTempUsato(XmlDateUtility.dateToXMLGregorianCalendar(signatureW.getClaimedSigningTime()));
            firmaCompType.setTipoRiferimentoTemporaleUsato(VerificaFirmaEnums.TipoRifTemporale.MT_VERS_NORMA.name());
        } else {
            firmaCompType.setTmRifTempUsato(XmlDateUtility.dateToXMLGregorianCalendar(dataDiRiferimento));
            firmaCompType.setTipoRiferimentoTemporaleUsato(
                    isDataDiRiferimentoOnCompVers ? VerificaFirmaEnums.TipoRifTemporale.RIF_TEMP_VERS.name()
                            : VerificaFirmaEnums.TipoRifTemporale.DATA_VERS.name());
        }

        //
        firmaCompType.setTiFormatoFirma(signatureW.getSignatureFormat().toString());
        //
        firmaCompType.setNmCognomeFirmatario(xmlcertificate.getSurname());
        firmaCompType.setNmFirmatario(xmlcertificate.getGivenName());
        //
        firmaCompType.setTiFirma(VFTipoFirmaType.DIGITALE);

        // basic building block
        XmlBasicBuildingBlocks bbb = reports.getDetailedReport().getBasicBuildingBlockById(signatureW.getId());
        if (bbb == null) {
            // TOFIX: cosa fare in questi casi ?!
            LOG.warn("BasicBuildingBlockById not found SIGNATURE ID = {}", signatureW.getId());
        }
        // basic building block counter signature parent
        XmlBasicBuildingBlocks bbbParent = null;
        if (signatureW.isCounterSignature()) {
            bbbParent = reports.getDetailedReport().getBasicBuildingBlockById(signatureW.getParent().getId());
            if (bbbParent == null) {
                // TOFIX: cosa fare in questi casi ?!
                LOG.warn("Parent BasicBuildingBlockById not found SIGNATURE ID = {}", signatureW.getParent().getId());
            }
        }
        // CONTROLLI FIRMA
        // global result
        boolean isSignaturePassed = reports.getSimpleReport().isValid(signatureW.getId());
        buildContrFirmaComp(firmaCompType, isSignaturePassed, signatureW, xmlcertificate, bbb, bbbParent);

        // OCSP CA
        buildFirmaCompCAwithOCSP(reports, xmlcertificateCA, firmaCompType, dataDiRiferimento);
        // OCSP + CONTR
        buildFirmaCompCERwithOCSP(reports, xmlcertificate, firmaCompType, dataDiRiferimento, bbb);

        // CRL CA
        buildFirmaCompCAwithCRL(xmlcertificateCA, certifCaType, firmaCompType, dataDiRiferimento);
        // CRL + CONTR
        buildFirmaCompCERwithCRL(xmlcertificate, certifCaType, firmaCompType, dataDiRiferimento, bbb);

        return firmaCompType;
    }

    private void buildContrFirmaComp(VFFirmaCompType firmaCompType, boolean isSignaturePassed,
            SignatureWrapper signature, CertificateWrapper xmlcertificate, XmlBasicBuildingBlocks bbb,
            XmlBasicBuildingBlocks bbbParent) {

        // format checking
        boolean isFormatCompliant = false;
        XmlConstraintsConclusion formatCompliantConstraint = null;
        if (bbbParent != null) {
            formatCompliantConstraint = bbbParent.getFC();
            // counter signature : non ha il controllo di formato (lo eredita dal padre)
            isFormatCompliant = formatCompliantConstraint != null
                    && formatCompliantConstraint.getConclusion().getIndication().equals(Indication.PASSED);
        } else {
            formatCompliantConstraint = bbb.getFC();
            isFormatCompliant = bbb != null && formatCompliantConstraint != null
                    && formatCompliantConstraint.getConclusion().getIndication().equals(Indication.PASSED);
        }
        SacerIndication esitoContrConforme = isFormatCompliant ? SacerIndication.POSITIVO
                : SacerIndication.FORMATO_NON_CONFORME;

        firmaCompType.setTiEsitoContrConforme(esitoContrConforme.name());
        //
        firmaCompType.setDsMsgEsitoContrConforme(esitoContrConforme.equals(SacerIndication.POSITIVO)
                ? esitoContrConforme.message() : generateErrorContrMsgEsito(esitoContrConforme.message(),
                        formatCompliantConstraint.getConclusion().getIndication(), Optional.empty()));
        //
        SacerIndication esitoVerifFirma = isSignaturePassed ? SacerIndication.POSITIVO : SacerIndication.NEGATIVO;
        // verifica risultato "globale" di verifica firma
        firmaCompType.setTiEsitoVerifFirma(esitoVerifFirma.name());
        //
        firmaCompType.setDsMsgEsitoVerifFirma(esitoVerifFirma.message());

        // caso "particolare" di building block non trovato (vedi controllo
        // precedente con WARNING)
        // in questi casi si assumono come NON PASSATI i controlli sull'oggetto perché
        // non reperibili
        boolean bbbFounded = bbb != null;

        // CONTROLLI FIRMA - CRITTOGRAFICO signatureValidations
        // CertificateReliability
        VFContrFirmaCompType contrFirmaCompType = new VFContrFirmaCompType();
        firmaCompType.getContrFirmaComps().add(contrFirmaCompType);
        contrFirmaCompType.setTiContr(VFTipoControlloType.CRITTOGRAFICO);

        // valiazione crittografica
        XmlCV cv = bbbFounded ? bbb.getCV() : null;
        if (!controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_CRITTOG_VERS).booleanValue()) {
            SacerIndication esito = SacerIndication.DISABILITATO;
            //
            contrFirmaCompType.setTiEsitoContrFirma(esito.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
        } else {
            if (!bbbFounded) {
                SacerIndication esito = SacerIndication.NEGATIVO;
                //
                contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
            } else {
                if (!cv.getConclusion().getIndication().equals(Indication.PASSED) && !isSignaturePassed) {
                    // init
                    SacerIndication esito = SacerIndication.NEGATIVO; // getSubIndication
                    SubIndication subIndication = cv.getConclusion().getSubIndication();
                    if (subIndication != null) {
                        switch (subIndication) {
                        case FORMAT_FAILURE:
                            esito = SacerIndication.NON_ESEGUITO;
                            break;
                        case NO_POE:
                            esito = SacerIndication.NON_NECESSARIO;
                            break;
                        case SIG_CRYPTO_FAILURE:
                        case HASH_FAILURE:
                            esito = SacerIndication.NEGATIVO;
                            break;
                        default:
                            esito = SacerIndication.NEGATIVO; // default
                            LOG.debug(LOG_FMT_SUBINDICATION, esito, contrFirmaCompType.getTiContr(), subIndication);
                            break;
                        }
                    }
                    // log eidas message
                    logEidasConclusion(xmlcertificate, cv, VFTipoControlloType.CRITTOGRAFICO.name());

                    contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                    contrFirmaCompType.setDsMsgEsitoContrFirma(
                            generateErrorContrMsgEsito(esito.message(), subIndication, Optional.empty()));
                } else {
                    contrFirmaCompType.setTiEsitoContrFirma(SacerIndication.POSITIVO.name());
                    contrFirmaCompType.setDsMsgEsitoContrFirma(SacerIndication.POSITIVO.message());
                }
            }
        }

        // TOFIX: al momento è lo stesso risultato raccolto sopra
        // CONTROLLI FIRMA - CRITTOGRAFICO_ABILITATO signatureValidations
        contrFirmaCompType = new VFContrFirmaCompType();
        firmaCompType.getContrFirmaComps().add(contrFirmaCompType);
        contrFirmaCompType.setTiContr(VFTipoControlloType.CRITTOGRAFICO_ABILITATO);

        if (!bbbFounded) {
            SacerIndication esito = SacerIndication.NEGATIVO;
            //
            contrFirmaCompType.setTiEsitoContrFirma(esito.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
        } else {
            if (!cv.getConclusion().getIndication().equals(Indication.PASSED) && !isSignaturePassed) {
                // init
                SacerIndication esito = SacerIndication.NEGATIVO; // getSubIndication
                SubIndication subIndication = cv.getConclusion().getSubIndication();
                //
                if (subIndication != null) {
                    switch (subIndication) {
                    case FORMAT_FAILURE:
                        esito = SacerIndication.NON_ESEGUITO;
                        break;
                    case NO_POE:
                        esito = SacerIndication.NON_NECESSARIO;
                        break;
                    case SIG_CRYPTO_FAILURE:
                    case HASH_FAILURE:
                        esito = SacerIndication.NEGATIVO;
                        break;
                    default:
                        esito = SacerIndication.NEGATIVO; // default
                        LOG.debug(LOG_FMT_SUBINDICATION, esito, contrFirmaCompType.getTiContr(), subIndication);
                        break;
                    }
                }
                // log eidas message
                logEidasConclusion(xmlcertificate, cv, VFTipoControlloType.CRITTOGRAFICO_ABILITATO.name());

                contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                contrFirmaCompType.setDsMsgEsitoContrFirma(
                        generateErrorContrMsgEsito(esito.message(), subIndication, Optional.empty()));
            } else {
                contrFirmaCompType.setTiEsitoContrFirma(SacerIndication.POSITIVO.name());
                contrFirmaCompType.setDsMsgEsitoContrFirma(SacerIndication.POSITIVO.message());
            }
        }

        // CONTROLLI FIRMA - CATENA_TRUSTED_ABILITATO CertificateAssociation &&
        // CertificateReliability
        contrFirmaCompType = new VFContrFirmaCompType();
        firmaCompType.getContrFirmaComps().add(contrFirmaCompType);
        contrFirmaCompType.setTiContr(VFTipoControlloType.CATENA_TRUSTED_ABILITATO);

        if (!controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_TRUST_VERS).booleanValue()) {
            SacerIndication esito = SacerIndication.DISABILITATO;
            //
            contrFirmaCompType.setTiEsitoContrFirma(esito.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
        } else {
            //
            SacerIndication esito = signature.isTrustedChain() || isSignaturePassed ? SacerIndication.POSITIVO
                    : SacerIndication.NEGATIVO;
            //
            contrFirmaCompType.setTiEsitoContrFirma(esito.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
        }

        // CONTROLLI FIRMA - CERTIFICATO CertificateExpiration
        contrFirmaCompType = new VFContrFirmaCompType();
        firmaCompType.getContrFirmaComps().add(contrFirmaCompType);
        contrFirmaCompType.setTiContr(VFTipoControlloType.CERTIFICATO);

        if (!controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_CERTIF_VERS).booleanValue()) {
            SacerIndication esito = SacerIndication.DISABILITATO;
            //
            contrFirmaCompType.setTiEsitoContrFirma(esito.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
        } else {
            // identificazione certificato
            XmlISC isc = bbbFounded ? bbb.getISC() : null;
            // validità firma
            XmlSAV sav = bbbFounded ? bbb.getSAV() : null;
            // validità del certificato
            XmlXCV xcv = bbbFounded ? bbb.getXCV() : null;
            if (!bbbFounded) {
                SacerIndication esito = SacerIndication.NEGATIVO;

                contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
            } else {
                // isc
                if ((!isc.getConclusion().getIndication().equals(Indication.PASSED)
                        || !sav.getConclusion().getIndication().equals(Indication.PASSED)) && !isSignaturePassed) {
                    // log eidas message
                    XmlConstraintsConclusion constraint = !isc.getConclusion().getIndication().equals(Indication.PASSED)
                            ? isc : sav;
                    logEidasConclusion(xmlcertificate, constraint, VFTipoControlloType.CERTIFICATO.name());

                    SacerIndication esito = SacerIndication.CERTIFICATO_NON_VALIDO;
                    // Nota: non si effettua verifica di sottoindicazione in quanto se questo tipo
                    // di controllo è
                    // errato
                    // si assume che il certificato di firma non è valido
                    contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                    contrFirmaCompType.setDsMsgEsitoContrFirma(generateErrorContrMsgEsito(esito.message(),
                            constraint.getConclusion().getIndication(), Optional.of(
                                    "il certificato non presenta alcuni degli attributi previsti dallo standard ('signing-certificate', 'cert-diget', ecc.)")));
                } else if (!xcv.getConclusion().getIndication().equals(Indication.PASSED) && !isSignaturePassed) {
                    // init
                    SacerIndication esito = SacerIndication.CERTIFICATO_ERRATO; // getSubIndication
                    SubIndication subIndication = xcv.getConclusion().getSubIndication();
                    Optional<String> details = Optional.empty();
                    // get SubXCV for more infos
                    Optional<XmlSubXCV> subXCV = xcv.getSubXCV().stream()
                            .filter(c -> c.getId().equals(xmlcertificate.getId())).findFirst();
                    //
                    if (subIndication != null) {
                        switch (subIndication) {
                        case EXPIRED:
                        case OUT_OF_BOUNDS_NO_POE:
                        case OUT_OF_BOUNDS_NOT_REVOKED:
                            // The current time is not in the validity range of the signers certificate.
                            // Non è chiaro se la data di firma sia precedente o successiva alla validità
                            // del
                            // certificato.
                            // vedi anche https://ec.europa.eu/cefdigital/tracker/browse/DSS-2070
                            esito = SacerIndication.CERTIFICATO_SCADUTO;
                            details = Optional.of((xmlcertificate.getNotAfter() != null
                                    ? " scaduto in data " + dateFormatter.format(xmlcertificate.getNotAfter()) : "")
                                    + (xmlcertificate.getNotBefore() != null ? " valido a partire dalla data "
                                            + dateFormatter.format(xmlcertificate.getNotBefore()) : "")
                                    + " successivo al riferimento temporale utilizzato "
                                    + dateFormatter.format(XmlDateUtility
                                            .xmlGregorianCalendarToDate(firmaCompType.getTmRifTempUsato())));
                            break;
                        case NO_POE:
                        case CRYPTO_CONSTRAINTS_FAILURE_NO_POE:
                        case NOT_YET_VALID:
                            esito = SacerIndication.CERTIFICATO_NON_VALIDO;
                            details = Optional.of("certificato non è ancora valido");
                            break;
                        case TRY_LATER:
                        case REVOCATION_OUT_OF_BOUNDS_NO_POE:
                            // revoche non presenti
                            if (modificatoriWSCalc.contains(Costanti.ModificatoriWS.TAG_FIRMA_1_5)) {
                                esito = SacerIndication.REVOCHE_NON_CONSISTENTI;
                            } else {
                                esito = SacerIndication.CERTIFICATO_ERRATO;
                                // alternative message (che quindi non è il defalut dell'esito)
                                details = Optional.of(
                                        "errore sul controllo delle revoche del certificato, informazioni non scaricabili o non affidabili");
                            }
                            break;
                        case REVOKED_NO_POE:
                        case REVOKED:
                            //
                            esito = SacerIndication.CERTIFICATO_REVOCATO;
                            // message details
                            if (subXCV.isPresent()) {
                                StringBuilder detailedMsg = new StringBuilder();
                                detailedMsg.append((subXCV.get().getRevocationInfo() != null
                                        && subXCV.get().getRevocationInfo().getRevocationDate() != null
                                                ? "revocato in data " + dateFormatter
                                                        .format(subXCV.get().getRevocationInfo().getRevocationDate())
                                                : "data revoca non presente"));
                                //
                                detailedMsg.append(MESSAGE_ERROR_SEPARATOR);
                                //
                                detailedMsg.append(subXCV.get().getConstraint().stream()
                                        .filter(s -> s.getStatus().equals(XmlStatus.NOT_OK)
                                                && s.getAdditionalInfo() != null)
                                        .map(XmlConstraint::getAdditionalInfo)
                                        .collect(Collectors.joining(MESSAGE_ERROR_SEPARATOR)));
                                // details
                                details = Optional.of(detailedMsg.toString());
                            }
                            break;
                        default:
                            esito = SacerIndication.CERTIFICATO_ERRATO; // default
                            details = Optional.of("il certificato non è un certificato di firma");
                            LOG.debug(LOG_FMT_SUBINDICATION, esito, contrFirmaCompType.getTiContr(),
                                    cv.getConclusion().getSubIndication());
                            break;
                        }
                    }

                    // log eidas message
                    logEidasConclusion(xmlcertificate, xcv, VFTipoControlloType.CERTIFICATO.name());

                    contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                    contrFirmaCompType.setDsMsgEsitoContrFirma(
                            generateErrorContrMsgEsito(esito.message(), subIndication, details));
                } else {
                    contrFirmaCompType.setTiEsitoContrFirma(SacerIndication.POSITIVO.name());
                    contrFirmaCompType.setDsMsgEsitoContrFirma(SacerIndication.POSITIVO.message());
                }
            }
        }

        // CONTROLLI FIRMA - CATENA_TRUSTED CertificateAssociation &&
        // CertificateReliability
        // Nota: su DB risultano solo per casi molto vecchi il riferimento di questo
        // controllo con la relativa CRL
        contrFirmaCompType = new VFContrFirmaCompType();
        firmaCompType.getContrFirmaComps().add(contrFirmaCompType);
        contrFirmaCompType.setTiContr(VFTipoControlloType.CATENA_TRUSTED);

        if (!controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_TRUST_VERS).booleanValue()) {
            SacerIndication esito = SacerIndication.DISABILITATO;
            //
            contrFirmaCompType.setTiEsitoContrFirma(esito.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
        } else {
            SacerIndication esito = signature.isTrustedChain() || isSignaturePassed ? SacerIndication.POSITIVO
                    : SacerIndication.NEGATIVO;
            //
            contrFirmaCompType.setTiEsitoContrFirma(esito.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
        }
    }

    private void buildFirmaCompCERwithCRL(CertificateWrapper xmlcertificate, VFCertifCaType certifCaType,
            VFFirmaCompType firmaCompType, ZonedDateTime dataDiRiferimento, XmlBasicBuildingBlocks bbb)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // OCSP prioritario rispetto CRL
        boolean hasOcsp = xmlcertificate.getCertificateRevocationData().stream()
                .anyMatch(r -> r.getRevocationType().equals(RevocationType.OCSP));

        // CRL
        RevocationWrapper crl = findRevocationByType(xmlcertificate, dataDiRiferimento, RevocationType.CRL);
        if (hasOcsp || crl == null) {
            //
            buildContrCRLNotFoundFirmaComp(firmaCompType, xmlcertificate, hasOcsp);
            return;
        }

        // CRL
        VFCrlType crlType = buildCrl(crl);

        // FIRMA : CRL
        firmaCompType.setCrl(crlType);
        // CRL: CA
        crlType.setCertifCa(certifCaType);

        // controlli CRL
        buildContrCRLFirmaComp(firmaCompType, xmlcertificate, crl, bbb, hasOcsp);
        // CRL - END
    }

    private void buildFirmaCompCERwithOCSP(Reports reports, CertificateWrapper xmlcertificate,
            VFFirmaCompType firmaCompType, ZonedDateTime dataDiRiferimento, XmlBasicBuildingBlocks bbb)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // verifica presenza CRL
        boolean hasCrl = xmlcertificate.getCertificateRevocationData().stream()
                .anyMatch(r -> r.getRevocationType().equals(RevocationType.CRL));
        // OCSP
        CertificateRevocationWrapper ocsp = findRevocationByType(xmlcertificate, dataDiRiferimento,
                RevocationType.OCSP);
        if (ocsp == null) {
            //
            buildContrOCSPNotFoundFirmaComp(firmaCompType, xmlcertificate, hasCrl);
            return;
        } // no ocsp

        // CA
        VFCertifCaType certifCaOcspType = buildOcspCertifCA(reports, xmlcertificate, ocsp);

        // OCSP + certif
        VFOcspType ocspType = buildOcspWithCertif(certifCaOcspType, ocsp);
        // FIRMA : OCSP
        firmaCompType.setOcsp(ocspType);

        // controlli OCSP
        buildContrOCSPFirmaComp(firmaCompType, xmlcertificate, ocsp, bbb, hasCrl);
        // OCSP - END

    }

    private void buildFirmaCompCAwithCRL(CertificateWrapper xmlcertificateCA, VFCertifCaType certifCaType,
            VFFirmaCompType firmaCompType, ZonedDateTime dataDiRiferimento)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        // OCSP prioritario rispetto CRL
        boolean hasOcsp = xmlcertificateCA.getCertificateRevocationData().stream()
                .anyMatch(r -> r.getRevocationType().equals(RevocationType.OCSP));

        // CRL
        RevocationWrapper crl = findRevocationByType(xmlcertificateCA, dataDiRiferimento, RevocationType.CRL);
        if (hasOcsp || crl == null) {
            return;
        }

        // CRL
        VFCrlType crlType = buildCrl(crl);

        // CRL : CA
        crlType.setCertifCa(certifCaType);
        // FIRMA : CRL CA
        firmaCompType.setCrlCertifCa(crlType);
        // CRL - END
    }

    private void buildFirmaCompCAwithOCSP(Reports reports, CertificateWrapper xmlcertificateCA,
            VFFirmaCompType firmaCompType, ZonedDateTime dataDiRiferimento)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // OCSP
        RevocationWrapper ocsp = findRevocationByType(xmlcertificateCA, dataDiRiferimento, RevocationType.OCSP);
        if (ocsp == null) {
            return;
        } // no ocsp

        // CA
        VFCertifCaType certifCaOcspType = buildOcspCertifCA(reports, xmlcertificateCA, ocsp);

        // OCSP + certif
        VFOcspType ocspType = buildOcspWithCertif(certifCaOcspType, ocsp);
        // FIRMA : OCSP
        firmaCompType.setOcspCertifCa(ocspType);
        // OCSP - END
    }

    // No CRL
    private void buildContrCRLNotFoundFirmaComp(VFFirmaCompType firmaCompType, CertificateWrapper xmlcertificate,
            boolean hasOcsp) {
        buildContrCRLFirmaComp(firmaCompType, xmlcertificate, null, null, hasOcsp);
    }

    private void buildContrCRLFirmaComp(VFFirmaCompType firmaCompType, CertificateWrapper xmlcertificate,
            RevocationWrapper crl, XmlBasicBuildingBlocks bbb, boolean hasOcsp) {

        /*
         * caso "particolare" di building block non trovato (vedi controllo precedente con WARNING) in questi casi si
         * assumono come NON PASSATI i controlli sull'oggetto perché non reperibili
         */
        boolean bbbFounded = bbb != null;
        // presente la CRL
        boolean hasCrl = crl != null;
        // presente solo la CRL
        boolean onlyCrl = hasCrl && !hasOcsp;
        // presente solo OCSP
        boolean onlyOcsp = !onlyCrl;
        // no revoche
        boolean noRevocations = !hasCrl && !hasOcsp;

        VFContrFirmaCompType contrFirmaCompType = new VFContrFirmaCompType();
        firmaCompType.getContrFirmaComps().add(contrFirmaCompType);
        contrFirmaCompType.setTiContr(VFTipoControlloType.CRL);

        if (!controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS).booleanValue()) {
            SacerIndication esito = SacerIndication.DISABILITATO;
            //
            contrFirmaCompType.setTiEsitoContrFirma(esito.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
        } else {
            // scenario 1 : crl e ocsp non presente
            if (noRevocations) {
                // valutazione CERTIFICATO_SCADUTO_3_12_2009
                // caso 1 : 3/12/2009 <= notAfter && 3/12/2009 >= notBefore
                Date revokedDate = Date
                        .from(LocalDate.of(2009, 12, 3).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                if (xmlcertificate.getNotAfter().before(revokedDate)
                        && xmlcertificate.getNotBefore().after(revokedDate)) {
                    SacerIndication esito = SacerIndication.CERTIFICATO_SCADUTO_3_12_2009;
                    //
                    contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                    contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
                } else /* caso 2 */ {
                    SacerIndication esito = SacerIndication.CRL_NON_SCARICABILE;

                    contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                    contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
                }
            } else if (onlyOcsp) /* scenario 3 : solo ocsp */ {
                SacerIndication esito = SacerIndication.NON_NECESSARIO;
                //
                contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                contrFirmaCompType.setDsMsgEsitoContrFirma(
                        esito.message() + ": controllo non necessario in quanto avviene tramite OCSP");
            } else /* scenario 4 : solo CRL disponibile */ {
                // Nota : non trovato il build block oppure l'indicazione con il risultato di
                // validazione del certifcato
                if (!bbbFounded
                        || bbb.getXCV().getSubXCV().stream().noneMatch(c -> c.getId().equals(xmlcertificate.getId()))) {
                    SacerIndication esito = SacerIndication.NEGATIVO;
                    //
                    contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                    contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
                } else {
                    //
                    XmlSubXCV subXvc = bbb.getXCV().getSubXCV().stream()
                            .filter(c -> c.getId().equals(xmlcertificate.getId())).collect(Collectors.toList()).get(0);
                    //
                    List<XmlRAC> xmlRacs = subXvc.getCRS().getRAC().stream().filter(r -> r.getId().equals(crl.getId()))
                            .collect(Collectors.toList());

                    if (xmlRacs.isEmpty()) {
                        SacerIndication esito = SacerIndication.NEGATIVO;
                        //
                        contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                        contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
                    } else if (!xmlRacs.get(0).getConclusion().getIndication().equals(Indication.PASSED)) {
                        // log eidas message
                        logEidasConclusion(xmlcertificate, xmlRacs.get(0), VFTipoControlloType.CRL.name());

                        // evaluate subindication (Revocation acceptence)
                        SacerIndication esito = evaluateCRLXmlRacSubIndication(contrFirmaCompType.getTiContr().name(),
                                xmlRacs);
                        //
                        contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                        contrFirmaCompType.setDsMsgEsitoContrFirma(generateErrorContrMsgEsito(esito.message(),
                                xmlRacs.get(0).getConclusion().getIndication(), Optional.empty()));
                    } else {
                        SacerIndication esito = SacerIndication.POSITIVO;
                        //
                        contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                        contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
                    }
                }
            }
        }
    }

    private void buildContrOCSPNotFoundFirmaComp(VFFirmaCompType firmaCompType, CertificateWrapper xmlcertificate,
            boolean hasCrl) {
        buildContrOCSPFirmaComp(firmaCompType, xmlcertificate, null, null, hasCrl);
    }

    private void buildContrOCSPFirmaComp(VFFirmaCompType firmaCompType, CertificateWrapper xmlcertificate,
            CertificateRevocationWrapper ocsp, XmlBasicBuildingBlocks bbb, boolean hasCrl) {

        /*
         * caso "particolare" di building block non trovato (vedi controllo precedente con WARNING) in questi casi si
         * assumono come NON PASSATI i controlli sull'oggetto perché non reperibili
         */
        boolean bbbFounded = bbb != null;
        // presente OCSP
        boolean hasOcsp = ocsp != null;
        // no revoche
        boolean noRevocations = !hasCrl && !hasOcsp;

        VFContrFirmaCompType contrFirmaCompType = new VFContrFirmaCompType();
        firmaCompType.getContrFirmaComps().add(contrFirmaCompType);
        contrFirmaCompType.setTiContr(VFTipoControlloType.OCSP);

        if (!controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS).booleanValue()) {
            SacerIndication esito = SacerIndication.DISABILITATO;
            //
            contrFirmaCompType.setTiEsitoContrFirma(esito.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
        } else {
            // scenario 1 : revoche non presenti
            if (noRevocations) {
                SacerIndication esito = SacerIndication.OCSP_NON_SCARICABILE;
                //
                contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
            } else if (hasCrl && !hasOcsp) /* scenario 2 : presente la CRL ma NON OCSP */ {
                SacerIndication esito = SacerIndication.NON_NECESSARIO;
                //
                contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                contrFirmaCompType.setDsMsgEsitoContrFirma(
                        esito.message() + ": controllo non necessario in quanto avviene tramite CRL");
            } else /* scenario 3 : ocsp presente */ {
                // Nota : non trovato il build block oppure l'indicazione con il risultato di
                // validazione del certifcato
                if (!bbbFounded
                        || bbb.getXCV().getSubXCV().stream().noneMatch(c -> c.getId().equals(xmlcertificate.getId()))) {
                    SacerIndication esito = SacerIndication.NEGATIVO;

                    contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                    contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
                } else {
                    //
                    XmlSubXCV subXvc = bbb.getXCV().getSubXCV().stream()
                            .filter(c -> c.getId().equals(xmlcertificate.getId())).collect(Collectors.toList()).get(0);
                    //
                    List<XmlRAC> xmlRacs = subXvc.getCRS().getRAC().stream().filter(r -> r.getId().equals(ocsp.getId()))
                            .collect(Collectors.toList());

                    if (xmlRacs.isEmpty()) {
                        SacerIndication esito = SacerIndication.NEGATIVO;
                        //
                        contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                        contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
                    } else if (!xmlRacs.get(0).getConclusion().getIndication().equals(Indication.PASSED)) {
                        // log eidas message
                        logEidasConclusion(xmlcertificate, xmlRacs.get(0), VFTipoControlloType.OCSP.name());

                        // evalutate subindication
                        SacerIndication esito = evaluateOCSPXmlRacSubIndication(ocsp,
                                contrFirmaCompType.getTiContr().name(), xmlRacs);
                        //
                        contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                        contrFirmaCompType.setDsMsgEsitoContrFirma(generateErrorContrMsgEsito(esito.message(),
                                xmlRacs.get(0).getConclusion().getIndication(), Optional.empty()));
                    } else {
                        SacerIndication esito = SacerIndication.OCSP_VALIDO;
                        //
                        contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                        contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
                    }
                }
            }
        }
    }

}
