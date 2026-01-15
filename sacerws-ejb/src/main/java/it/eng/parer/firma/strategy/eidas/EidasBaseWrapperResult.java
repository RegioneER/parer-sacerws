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

package it.eng.parer.firma.strategy.eidas;

import static it.eng.parer.util.DateUtilsConverter.convert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.esig.dss.detailedreport.jaxb.XmlConclusion;
import eu.europa.esig.dss.detailedreport.jaxb.XmlConstraintsConclusion;
import eu.europa.esig.dss.detailedreport.jaxb.XmlMessage;
import eu.europa.esig.dss.detailedreport.jaxb.XmlRAC;
import eu.europa.esig.dss.diagnostic.AbstractTokenProxy;
import eu.europa.esig.dss.diagnostic.CertificateRevocationWrapper;
import eu.europa.esig.dss.diagnostic.CertificateWrapper;
import eu.europa.esig.dss.diagnostic.RevocationWrapper;
import eu.europa.esig.dss.enumerations.CertificateSourceType;
import eu.europa.esig.dss.enumerations.RevocationType;
import eu.europa.esig.dss.enumerations.SubIndication;
import eu.europa.esig.dss.enumerations.UriBasedEnum;
import eu.europa.esig.dss.validation.reports.Reports;
import it.eng.parer.eidas.model.EidasWSReportsDTOTree;
import it.eng.parer.firma.util.VerificaFirmaEnums.SacerIndication;
import it.eng.parer.firma.xml.VFCertifCaType;
import it.eng.parer.firma.xml.VFCertifOcspType;
import it.eng.parer.firma.xml.VFCrlType;
import it.eng.parer.firma.xml.VFOcspType;
import it.eng.parer.firma.xml.VFUrlDistribCrlType;
import it.eng.parer.firma.xml.VFUrlDistribOcspType;
import it.eng.parer.ws.utils.XmlDateUtility;

public abstract class EidasBaseWrapperResult {

    private static final Logger LOG = LoggerFactory.getLogger(EidasBaseWrapperResult.class);
    protected static final String LOG_FMT_SUBINDICATION = "Eidas esito = {} controllo tipo = {} subindication = {}";
    private static final String MESSAGE_ESITO_CONTR_WITH_ERRORS = "{0} [{1}]";
    private static final String MESSAGE_ESITO_CONTR_WITH_DETAILS = MESSAGE_ESITO_CONTR_WITH_ERRORS
            + " : {2}";
    protected static final String MESSAGE_NO_INDICATION = "no indication uri";
    protected static final String MESSAGE_ERROR_SEPARATOR = ";";

    protected DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.LONG,
            DateFormat.LONG, Locale.ITALY);

    protected static final int PG_BUSTA = 0;
    protected static final int PG_FIRMA = 1;
    protected static final int PG_MARCA = 2;

    protected String getCertificateCAId(AbstractTokenProxy sigts,
            CertificateWrapper xmlcertificate) {
        // se trusted o selfsigned è la CA
        if (xmlcertificate.isTrusted() || xmlcertificate.isSelfSigned()) {
            return xmlcertificate.getId();
        }
        // se non è la CA la individua dalla chain
        // nota : non esiste più di una CA nella catena
        if (sigts.isTrustedChain()) {
            return sigts.getCertificateChain().stream()
                    .filter(c -> c.getSources().contains(CertificateSourceType.TRUSTED_STORE)
                            || c.getSources().contains(CertificateSourceType.TRUSTED_LIST))
                    .collect(Collectors.toList()).get(0).getId();
        }
        // verifica se esite in chain un certificato selfsigned
        if (sigts.getCertificateChain().stream().anyMatch(CertificateWrapper::isSelfSigned)) {
            return sigts.getCertificateChain().stream().filter(CertificateWrapper::isSelfSigned)
                    .collect(Collectors.toList()).get(0).getId();
        }

        // ultimo scenario : restituisce l'id dell'ultimo elemento della catena
        return sigts.getCertificateChain().get(sigts.getCertificateChain().size() - 1).getId();
    }

    protected CertificateRevocationWrapper findRevocationByType(CertificateWrapper certificate,
            ZonedDateTime dtRef, RevocationType type) {
        //
        CertificateRevocationWrapper revocation = null;
        // no revocation
        if (certificate.getCertificateRevocationData().stream()
                .noneMatch(r -> r.getRevocationType().equals(type))) {
            // null
            return revocation;
        }

        // extract revocations by source type
        List<CertificateRevocationWrapper> revocationFilteredBySource = certificate
                .getCertificateRevocationData().stream()
                .filter(r -> r.getRevocationType().equals(type)).collect(Collectors.toList());

        // get only one
        if (revocationFilteredBySource.size() == 1) {
            revocation = revocationFilteredBySource.get(0);
        } else {
            //
            List<CertificateRevocationWrapper> revocationFilteredByOriginDate = null;

            // filter by RevocationOrigin (EXTERNAL / CACHED)
            // verify
            // https://github.com/esig/dss/blob/5.8/dss-enumerations/src/main/java/eu/europa/esig/dss/enumerations/RevocationOrigin.java
            if (revocationFilteredBySource.stream().filter(c -> !c.getOrigin().isInternalOrigin())
                    .count() != 0) {
                revocationFilteredByOriginDate = revocationFilteredBySource.stream()
                        .filter(c -> !c.getOrigin().isInternalOrigin())
                        .collect(Collectors.toList());
            } else if (revocationFilteredBySource.stream()
                    .filter(c -> c.getOrigin().isInternalOrigin()).count() != 0) {
                // filter by RevocationOrigin (embedded)
                revocationFilteredByOriginDate = revocationFilteredBySource.stream()
                        .filter(c -> c.getOrigin().isInternalOrigin()).collect(Collectors.toList());
            } else {
                // filter by date
                revocationFilteredByOriginDate = revocationFilteredBySource.stream()
                        .filter(c -> c.getThisUpdate().after(convert(dtRef))
                                && c.getNextUpdate().before(convert(dtRef)))
                        .collect(Collectors.toList());
            }

            // error
            if (revocationFilteredByOriginDate.isEmpty() && LOG.isErrorEnabled()) {
                LOG.error("{} certificate id {} not found", type.name(), certificate.getId());
                // null
                return revocation;
            }

            // warn
            if (revocationFilteredByOriginDate.size() > 1 && LOG.isWarnEnabled()) {
                LOG.warn("{} certificate id {} more than one (get first of list)", type.name(),
                        certificate.getId());
            }
            // get first
            revocation = revocationFilteredByOriginDate.get(0);
        }
        //
        return revocation;
    }

    /*
     * OCSP REVOCATION + CERTIFICATE
     */

    /*
     * Accedendo fino all'ultimo elemento a partire da sinistra percorrendo i successivi livelli
     *
     */
    protected String getMimeTypeUnsigned(EidasWSReportsDTOTree parent,
            List<EidasWSReportsDTOTree> child) {
        // condizioni di uscita
        // ultimo elemento unsigned non trovato
        if (child == null || child.isEmpty()) {
            return parent.getMimeType();
        }
        // left child
        EidasWSReportsDTOTree leftChild = child.get(0);
        // mime found
        if (leftChild.isUnsigned()) {
            return leftChild.getMimeType();
        } else {
            return getMimeTypeUnsigned(leftChild, leftChild.getChildren());
        }

    }

    protected void logEidasConclusion(CertificateWrapper certificate, XmlConclusion conclusion) {
        logEidasConclusion(certificate, conclusion, null);
    }

    protected void logEidasConclusion(CertificateWrapper certificate,
            XmlConstraintsConclusion contraints, String tipo) {
        logEidasConclusion(certificate, contraints.getConclusion(), tipo);
    }

    protected void logEidasConclusion(CertificateWrapper certificate, XmlConclusion conclusion,
            String tipo) {
        StringBuilder msg = new StringBuilder();
        if (conclusion.getWarnings().stream().count() != 0) {
            msg.append(conclusion.getWarnings().stream().map(XmlMessage::getValue)
                    .collect(Collectors.joining(";")));
        }
        if (conclusion.getErrors().stream().count() != 0) {
            msg.append(conclusion.getErrors().stream().map(XmlMessage::getValue)
                    .collect(Collectors.joining(";")));
        }
        LOG.debug("EIDAS FIRMA CONCLUSION ID = {} {}, msg {}",
                certificate != null ? certificate.getId() : "ID_FIRMA_NOT_FOUND",
                tipo == null ? "" : " tipo controllo " + tipo, msg);
    }

    protected static class EidasWrapperResultControl {

        private EidasWrapperResultControl() {
            throw new IllegalStateException("Utility class");
        }

        public static void fieldCannotBeNull(Object obj, String fieldName, Optional<String> param)
                throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException,
                InvocationTargetException {
            Method getField = findMethod(obj, fieldName);
            if (getField.invoke(obj) == null) {
                final String msg = "[" + obj.getClass().getName() + "] il campo " + fieldName
                        + " richiesto alla compilazione è NULL."
                        + (param.isPresent() ? " EIDAS signature id = " + param.get() : "");
                throw new NullPointerException(msg);
            }
        }

        private static Method findMethod(Object obj, String fieldName)
                throws NoSuchMethodException {
            Method getField;
            try {
                getField = ClassUtils.getPublicMethod(obj.getClass(),
                        "get" + StringUtils.capitalize(fieldName));
            } catch (NoSuchMethodException e) {
                getField = obj.getClass().getSuperclass()
                        .getDeclaredMethod("get" + StringUtils.capitalize(fieldName));
            }
            return getField;
        }
    }

    protected SacerIndication evaluateOCSPXmlRacSubIndication(CertificateRevocationWrapper ocsp,
            String tiContr, List<XmlRAC> xmlRacs) {
        SacerIndication esito = SacerIndication.OCSP_SCONOSCIUTO;
        // ocsp response known by server
        if (ocsp.isKnown()) {
            // subindication
            SubIndication subIndication = xmlRacs.get(0).getConclusion().getSubIndication();
            if (subIndication != null) {
                switch (subIndication) {
                case TRY_LATER:
                    esito = SacerIndication.OCSP_NON_AGGIORNATO;
                    break;
                default:
                    esito = SacerIndication.OCSP_SCONOSCIUTO; // default
                    LOG.debug(LOG_FMT_SUBINDICATION, esito, tiContr, subIndication);
                    break;
                }
            }
        }
        // revoked response by server
        if (ocsp.isRevoked()) {
            esito = SacerIndication.OCSP_REVOCATO;
        }
        return esito;
    }

    protected SacerIndication evaluateCRLXmlRacSubIndication(String tiContr, List<XmlRAC> xmlRacs) {
        SacerIndication esito = SacerIndication.CRL_NON_VALIDA;
        SubIndication subIndication = xmlRacs.get(0).getConclusion().getSubIndication();
        if (subIndication != null) {
            switch (subIndication) {
            case OUT_OF_BOUNDS_NO_POE:
                esito = SacerIndication.CERTIFICATO_SCADUTO;
                break;
            case REVOKED:
                esito = SacerIndication.CERTIFICATO_REVOCATO;
                break;
            case TRY_LATER:
            case NOT_YET_VALID:
                esito = SacerIndication.CRL_NON_VALIDA;
                break;
            case EXPIRED:
                esito = SacerIndication.CRL_SCADUTA;
                break;
            default:
                esito = SacerIndication.CRL_NON_VALIDA; // default
                LOG.debug(LOG_FMT_SUBINDICATION, esito, tiContr, subIndication);
                break;
            }
        }
        return esito;
    }

    protected VFCertifCaType buildOcspCertifCA(Reports reports, CertificateWrapper xmlcertificate,
            RevocationWrapper ocsp) throws NoSuchMethodException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        // CA
        String certificateCAId = getCertificateCAId(ocsp, xmlcertificate);
        //
        CertificateWrapper xmlcertificateCAOCSP = reports.getDiagnosticData()
                .getUsedCertificateById(certificateCAId);

        VFCertifCaType certifCaOcspType = new VFCertifCaType();
        // test
        EidasWrapperResultControl.fieldCannotBeNull(xmlcertificateCAOCSP, "serialNumber",
                Optional.ofNullable(xmlcertificateCAOCSP.getId()));
        certifCaOcspType.setDsSerialCertifCa(xmlcertificate.getSerialNumber());

        // test
        EidasWrapperResultControl.fieldCannotBeNull(xmlcertificateCAOCSP, "notBefore",
                Optional.ofNullable(xmlcertificate.getId()));
        certifCaOcspType.setDtIniValCertifCa(
                XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificate.getNotBefore()));

        // test
        EidasWrapperResultControl.fieldCannotBeNull(xmlcertificateCAOCSP, "notAfter",
                Optional.ofNullable(xmlcertificate.getId()));
        certifCaOcspType.setDtFinValCertifCa(
                XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificateCAOCSP.getNotAfter()));
        // Note: report EIDAS doens't cover it ! ( SubjectKeyId non presente su rerport)
        // certifCaOcspType.setDsSubjectKeyId();
        certifCaOcspType.setDlDnIssuerCertifCa(xmlcertificateCAOCSP.getCertificateIssuerDN());
        certifCaOcspType.setDlDnSubjectCertifCa(xmlcertificateCAOCSP.getCertificateDN());

        int urlIdx = 1;
        // check NON empty URL
        List<String> crlFiltered = xmlcertificateCAOCSP.getCRLDistributionPoints().stream()
                .filter(StringUtils::isNotBlank).collect(Collectors.toList());
        for (String url : crlFiltered) {
            VFUrlDistribCrlType urlDistribCrlType = new VFUrlDistribCrlType();
            urlDistribCrlType.setDlUrlDistribCrl(url);
            urlDistribCrlType.setNiOrdUrlDistribCrl(new BigDecimal(urlIdx));

            certifCaOcspType.getUrlDistribCrls().add(urlDistribCrlType);
            // inc idx
            urlIdx++;
        }
        // ocsp (from CA+certificate)
        // check NON empty URL
        Iterable<String> ocspCombinedUrls = CollectionUtils
                .union(xmlcertificateCAOCSP.getOCSPAccessUrls(), xmlcertificate.getOCSPAccessUrls())
                .stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        urlIdx = 1;
        for (String url : ocspCombinedUrls) {
            VFUrlDistribOcspType urlDistribOcspType = new VFUrlDistribOcspType();
            urlDistribOcspType.setDlUrlDistribOcsp(url);
            urlDistribOcspType.setNiOrdUrlDistribOcsp(new BigDecimal(urlIdx));

            certifCaOcspType.getUrlDistribOcsps().add(urlDistribOcspType);
            // inc idx
            urlIdx++;
        }

        return certifCaOcspType;
    }

    /*
     * OCSP REVOCATION + CERTIFICATE
     */
    protected VFOcspType buildOcspWithCertif(VFCertifCaType certifCaType, RevocationWrapper ocsp)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        // certificate
        CertificateWrapper xmlcertificateOcspResp = ocsp.getSigningCertificate();

        // certif
        VFCertifOcspType certifOcspType = new VFCertifOcspType();
        certifOcspType.setCertifCa(certifCaType);
        // Nota: certificato ocsp responder presenta un serialnumber e non un
        // subjectserial number
        // test
        EidasWrapperResultControl.fieldCannotBeNull(xmlcertificateOcspResp, "serialNumber",
                Optional.ofNullable(xmlcertificateOcspResp.getId()));
        certifOcspType.setDsSerialCertifOcsp(xmlcertificateOcspResp.getSerialNumber());
        // test
        EidasWrapperResultControl.fieldCannotBeNull(xmlcertificateOcspResp, "notBefore",
                Optional.ofNullable(xmlcertificateOcspResp.getId()));
        certifOcspType.setDtIniValCertifOcsp(
                XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificateOcspResp.getNotBefore()));
        // test
        EidasWrapperResultControl.fieldCannotBeNull(xmlcertificateOcspResp, "notAfter",
                Optional.ofNullable(xmlcertificateOcspResp.getId()));
        certifOcspType.setDtFinValCertifOcsp(
                XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificateOcspResp.getNotAfter()));
        // test
        EidasWrapperResultControl.fieldCannotBeNull(xmlcertificateOcspResp, "certificateDN",
                Optional.ofNullable(xmlcertificateOcspResp.getId()));
        certifOcspType.setDlDnSubject(xmlcertificateOcspResp.getCertificateDN());

        // OCSP
        VFOcspType ocspType = new VFOcspType();
        // TOFIX: da verificare (ResponderID)
        // https://tools.ietf.org/html/rfc6960#section-4.2.2.3
        if (StringUtils.isNotBlank(ocsp.getSigningCertificateReference().getIssuerName())) {
            ocspType.setDsCertifIssuername(ocsp.getSigningCertificateReference().getIssuerName());
        }
        if (ocsp.getSigningCertificateReference().isIssuerSerialPresent()) {
            ocspType.setDsCertifSerialBase64(Base64
                    .encodeBase64String(ocsp.getSigningCertificateReference().getIssuerSerial()));
        }
        if (ocsp.getSigningCertificateReference().getSki() != null) {
            ocspType.setDsCertifSkiBase64(
                    Base64.encodeBase64String(ocsp.getSigningCertificateReference().getSki()));
        }

        ocspType.setCertifOcsp(certifOcspType);

        return ocspType;
    }

    /*
     * CRL + FILE
     */
    protected VFCrlType buildCrl(RevocationWrapper crl) throws NoSuchMethodException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        VFCrlType crlType = new VFCrlType();
        // test
        EidasWrapperResultControl.fieldCannotBeNull(crl, "thisUpdate",
                Optional.ofNullable(crl.getId()));
        crlType.setDtIniCrl(XmlDateUtility.dateToXMLGregorianCalendar(crl.getThisUpdate()));

        // test
        EidasWrapperResultControl.fieldCannotBeNull(crl, "nextUpdate",
                Optional.ofNullable(crl.getId()));
        crlType.setDtScadCrl(XmlDateUtility.dateToXMLGregorianCalendar(crl.getNextUpdate()));
        // Note: report EIDAS doens't cover it ! ( SubjectKeyId non presente su rerport)
        // crlType.setNiSerialCrl();

        return crlType;
    }

    /*
     * formatter message esito
     */
    protected String generateErrorContrMsgEsito(String localmsg, UriBasedEnum indication,
            Optional<String> details) {
        if (details.isPresent()) {
            return MessageFormat.format(MESSAGE_ESITO_CONTR_WITH_DETAILS, localmsg,
                    indication != null ? indication.getUri() : MESSAGE_NO_INDICATION,
                    details.get());
        } else {
            return MessageFormat.format(MESSAGE_ESITO_CONTR_WITH_ERRORS, localmsg,
                    indication != null ? indication.getUri() : MESSAGE_NO_INDICATION);

        }
    }

}
