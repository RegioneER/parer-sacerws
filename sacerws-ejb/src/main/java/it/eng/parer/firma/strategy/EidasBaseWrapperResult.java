package it.eng.parer.firma.strategy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.esig.dss.detailedreport.jaxb.XmlConclusion;
import eu.europa.esig.dss.detailedreport.jaxb.XmlMessage;
import eu.europa.esig.dss.diagnostic.AbstractTokenProxy;
import eu.europa.esig.dss.diagnostic.CertificateRevocationWrapper;
import eu.europa.esig.dss.diagnostic.CertificateWrapper;
import eu.europa.esig.dss.enumerations.CertificateSourceType;
import eu.europa.esig.dss.enumerations.RevocationType;
import it.eng.parer.eidas.model.EidasWSReportsDTOTree;
import it.eng.parer.firma.exception.VerificaFirmaWrapperGenericException;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import java.time.ZonedDateTime;
import static it.eng.parer.util.DateUtilsConverter.convert;

public abstract class EidasBaseWrapperResult {

    private static final Logger LOG = LoggerFactory.getLogger(EidasBaseWrapperResult.class);

    protected String getCertificateCAId(AbstractTokenProxy sigts, CertificateWrapper xmlcertificate) {
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
        if (sigts.getCertificateChain().stream().anyMatch(c -> c.isSelfSigned())) {
            return sigts.getCertificateChain().stream().filter(c -> c.isSelfSigned()).collect(Collectors.toList())
                    .get(0).getId();
        }

        // ultimo scenario : restituisce l'id dell'ultimo elemento della catena
        return sigts.getCertificateChain().get(sigts.getCertificateChain().size() - 1).getId();
    }

    protected CertificateRevocationWrapper findRevocationByType(CertificateWrapper certificate, ZonedDateTime dtRef,
            RevocationType type) {
        //
        CertificateRevocationWrapper revocation = null;
        // no revocation
        if (certificate.getCertificateRevocationData().stream().noneMatch(r -> r.getRevocationType().equals(type))) {
            // null
            return revocation;
        }

        // extract revocations by source type
        List<CertificateRevocationWrapper> revocationFilteredBySource = certificate.getCertificateRevocationData()
                .stream().filter(r -> r.getRevocationType().equals(type)).collect(Collectors.toList());

        // get only one
        if (revocationFilteredBySource.size() == 1) {
            revocation = revocationFilteredBySource.get(0);
        } else {
            //
            List<CertificateRevocationWrapper> revocationFilteredByOriginDate = null;

            // filter by RevocationOrigin (EXTERNAL / CACHED)
            // verify
            // https://github.com/esig/dss/blob/5.8/dss-enumerations/src/main/java/eu/europa/esig/dss/enumerations/RevocationOrigin.java
            if (revocationFilteredBySource.stream().filter(c -> !c.getOrigin().isInternalOrigin()).count() != 0) {
                revocationFilteredByOriginDate = revocationFilteredBySource.stream()
                        .filter(c -> !c.getOrigin().isInternalOrigin()).collect(Collectors.toList());
            } else if (revocationFilteredBySource.stream().filter(c -> c.getOrigin().isInternalOrigin()).count() != 0) {
                // filter by RevocationOrigin (embedded)
                revocationFilteredByOriginDate = revocationFilteredBySource.stream()
                        .filter(c -> c.getOrigin().isInternalOrigin()).collect(Collectors.toList());
            } else {
                // filter by date
                revocationFilteredByOriginDate = revocationFilteredBySource.stream().filter(
                        c -> c.getThisUpdate().after(convert(dtRef)) && c.getNextUpdate().before(convert(dtRef)))
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
                LOG.warn("{} certificate id {} more than one (get first of list)", type.name(), certificate.getId());
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
    protected String getMimeTypeUnsigned(EidasWSReportsDTOTree parent, List<EidasWSReportsDTOTree> child) {
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

    protected void logEidasConclusion(CertificateWrapper certificate, XmlConclusion conclusion, String tipo) {
        StringBuilder msg = new StringBuilder();
        if (conclusion.getWarnings().stream().count() != 0) {
            msg.append(conclusion.getWarnings().stream().map(XmlMessage::getValue).collect(Collectors.joining(";")));
        }
        if (conclusion.getErrors().stream().count() != 0) {
            msg.append(conclusion.getErrors().stream().map(XmlMessage::getValue).collect(Collectors.joining(";")));
        }
        LOG.debug("EIDAS FIRMA CONCLUSION ID = {} {}, msg {}", certificate.getId(),
                tipo == null ? "" : " tipo controllo " + tipo, msg);
    }

    protected static class EidasWrapperResultControl {

        private EidasWrapperResultControl() {
            throw new IllegalStateException("Utility class");
        }

        public static void fieldCannotBeNull(VerificaFirmaWrapper wrapper, Object obj, String fieldName,
                Optional<String> param) throws VerificaFirmaWrapperGenericException {
            try {
                Method getField = findMethod(obj, fieldName);
                if (getField.invoke(obj) == null) {
                    final String msg = "[" + obj.getClass().getName() + " nome campo " + fieldName + " risulta null, "
                            + (param.isPresent() ? " EIDAS signature id = " + param.get() : "") + "]";
                    LOG.error(msg);
                    //
                    throw new VerificaFirmaWrapperGenericException(msg, wrapper);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e) {
                throw new VerificaFirmaWrapperGenericException(e, wrapper);
            }
        }

        private static Method findMethod(Object obj, String fieldName) throws NoSuchMethodException {
            Method getField;
            try {
                getField = obj.getClass().getDeclaredMethod("get" + StringUtils.capitalize(fieldName));
            } catch (NoSuchMethodException e) {
                getField = obj.getClass().getSuperclass().getDeclaredMethod("get" + StringUtils.capitalize(fieldName));
            }
            return getField;
        }
    }

}
