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

package it.eng.parer.firma.ejb;

import java.math.BigInteger;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.DecServizioVerificaCompDoc;
import it.eng.parer.entity.FirCertifCa;
import it.eng.parer.entity.FirCertifFirmatario;
import it.eng.parer.entity.FirCertifOcsp;
import it.eng.parer.entity.FirCrl;
import it.eng.parer.entity.FirOcsp;
import it.eng.parer.firma.dto.input.InvokeVerificaInput;
import it.eng.parer.firma.dto.input.InvokeVerificaRule;
import it.eng.parer.firma.exception.VerificaFirmaConnectionException;
import it.eng.parer.firma.exception.VerificaFirmaException;
import it.eng.parer.firma.exception.VerificaFirmaGenericInvokeException;
import it.eng.parer.firma.exception.VerificaFirmaWrapperGenericException;
import it.eng.parer.firma.exception.VerificaFirmaWrapperResNotFoundException;
import it.eng.parer.firma.xml.VFAdditionalInfoBustaType;
import it.eng.parer.firma.xml.VFAdditionalInfoCertifCaType;
import it.eng.parer.firma.xml.VFAdditionalInfoCertifFirmatarioType;
import it.eng.parer.firma.xml.VFAdditionalInfoCertifOcspType;
import it.eng.parer.firma.xml.VFAdditionalInfoCrlType;
import it.eng.parer.firma.xml.VFAdditionalInfoOcspType;
import it.eng.parer.firma.xml.VFCertifCaType;
import it.eng.parer.firma.xml.VFCertifFirmatarioType;
import it.eng.parer.firma.xml.VFCrlType;
import it.eng.parer.firma.xml.VFFirmaCompType;
import it.eng.parer.firma.xml.VFMarcaCompType;
import it.eng.parer.firma.xml.VFOcspType;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.firma.xml.VerificaFirmaWrapper.VFBusta;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.XmlDateUtility;
import it.eng.parer.ws.versamento.dto.AbsVersamentoExt;
import it.eng.parer.ws.versamento.ejb.ControlliPerFirme;

@Stateless(mappedName = "VerificaFirmaWrapperManager")
@LocalBean
public class VerificaFirmaWrapperManager implements IGenericVerificaFirmaWrapperResult {

    private static final Logger LOG = LoggerFactory.getLogger(VerificaFirmaWrapperManager.class);

    @EJB
    protected ControlliPerFirme controlliPerFirme;
    @EJB
    protected EidasInvoker eidasInvoker;
    @EJB
    protected CryptoInvoker cryptoInvoker;

    @Override
    public VerificaFirmaWrapper invokeVerifica(InvokeVerificaRule rule, InvokeVerificaInput in,
            AbsVersamentoExt versamento) throws VerificaFirmaException {

        /**
         * SCENARIO DEFAULT
         *
         * eseguiVerificaFirma = SI
         *
         * PRIORITARIO = EIDAS -> SE IGNOTO = CRYPTO
         *
         * SCENARIO SOLO CRYPTO
         *
         * eseguiVerificaFirmaSoloCrypto = SI eseguiVerificaFirma = NO
         *
         * SCENARI "PARTICOLARI" (= CRYPTO)
         *
         * detached con sottocomponente FIRMA e MARCA o sottocomponente MARCA (marca temporale TSD/TSR), multi
         * sottocomponenti FIRMA (>1)
         *
         */
        // solo crypto
        if (rule.isEseguiVerificaFirmaOnlyCrypto() || rule.isHasMultipleFirmaDetached()
                || rule.isHasFirmaAndMarcaDetached() || rule.isHasMarcaDetached()) {
            //
            return invokeCrypto(in, versamento);
        } else /* tutti gli altri casi */ {
            /*
             * Scenari:
             * 
             * 1) EIDAS riesce ad effettuare la verifica sul file
             * 
             * 2) EIDAS non riesce ad effettaure la verifica sul file nei seguenti casi: a) gli endpoint configurati non
             * rispondono
             * 
             * b) l'endpoint restituisce errore
             * 
             * c) la risposta dell'endpoint non è completa
             * 
             * Nei casi sopra descritti si tenta di effettuare la validazione su crypto se anche questa va male verrà
             * restituito un esito negativo con codice FIRMA_006_001
             */
            VerificaFirmaWrapper wrapper = invokeEidas(in, versamento);
            // firme individuate (altrimenti tenta su crypto)
            if (wrapper != null && BooleanUtils.isTrue(wrapper.isSignsDetected())) {
                return wrapper;
            } else {
                return invokeCrypto(in, versamento);
            }
        }
    }

    private VerificaFirmaWrapper invokeEidas(InvokeVerificaInput in, AbsVersamentoExt versamento) {
        VerificaFirmaWrapper wrapper = null;
        try {
            wrapper = eidasInvoker.verificaAndWrapIt(in.getComponenteVers(), in.getSottoComponentiFirma(),
                    in.getSottoComponentiMarca(), in.getAbilitazioni(), in.getDataDiRiferimento(),
                    Boolean.FALSE/* default */, in.getUuid(), versamento);
        } catch (VerificaFirmaConnectionException ex) {
            /* Errore su invocazione (GRAVE) */
            LOG.error("Errore invocazione servizio verifica firma {}", ex.getCdService(), ex);
            /* Si passa ad invocazione CRYPTO..... */
        } catch (VerificaFirmaGenericInvokeException epex) {
            /* Errore restituito da endpoint (gestito) */
            LOG.debug("Risposta con errore restituito da verifica firma {}", epex.getCdService(), epex);
            /* Si passa ad invocazione CRYPTO..... */
        } catch (VerificaFirmaWrapperResNotFoundException | VerificaFirmaWrapperGenericException wrapex) {
            /* Errore restituito da endpoint o su wrapping della risposta */
            LOG.warn("Errore generico", wrapex);
            /* Si passa ad invocazione CRYPTO..... */
        }
        return wrapper;
    }

    private VerificaFirmaWrapper invokeCrypto(InvokeVerificaInput in, AbsVersamentoExt versamento)
            throws VerificaFirmaException {
        try {
            return cryptoInvoker.verificaAndWrapIt(in.getComponenteVers(), in.getSottoComponentiFirma(),
                    in.getSottoComponentiMarca(), in.getAbilitazioni(), in.getDataDiRiferimento(),
                    in.isVerificaAllaDataDiFirma(), in.getUuid(), versamento);
        } catch (VerificaFirmaGenericInvokeException | VerificaFirmaWrapperResNotFoundException
                | VerificaFirmaWrapperGenericException cpex) {
            /* Errore restituito da endpoint o su wrapping della risposta */
            // tutte le verifiche sono fallite, esco!
            throw new VerificaFirmaException(MessaggiWSBundle.FIRMA_006_002,
                    MessaggiWSBundle.getString(MessaggiWSBundle.FIRMA_006_002, in.getComponenteVers().getChiaveComp()),
                    cpex);
        } catch (VerificaFirmaConnectionException ex) {
            /* GRAVE : Endpoint non raggiunto (chiamata fallita) */
            // tutte le verifiche sono fallite, esco!
            throw new VerificaFirmaException(MessaggiWSBundle.FIRMA_006_001,
                    MessaggiWSBundle.getString(MessaggiWSBundle.FIRMA_006_001, ex.getCdService()), ex);
        }
    }

    @Override
    public void buildAdditionalInfoOnVFWrapper(VerificaFirmaWrapper wrapper) throws VerificaFirmaException {

        // wrapper
        addAdditionalInfoOnWrapper(wrapper);

        // forEach busta
        for (VFBusta busta : wrapper.getVFBusta()) {
            busta.setAdditionalInfo(new VFAdditionalInfoBustaType());
            // FIRMA
            for (VFFirmaCompType firmaCompType : busta.getFirmaComps()) {

                // all additional info needed
                addAdditionalInfoOnVFirma(firmaCompType);

                // contro firma
                for (VFFirmaCompType controFirmaCompType : firmaCompType.getControfirmaFirmaFiglios()) {
                    // all additional info needed
                    addAdditionalInfoOnVFirma(controFirmaCompType);
                }
            }

            // MARCA
            for (VFMarcaCompType marcaCompType : busta.getMarcaComps()) {
                // CA
                VFCertifCaType certifTsa = marcaCompType.getCertifTsa();
                builAdditionalInfoCa(certifTsa);
                // CRL
                VFCrlType crlTsa = marcaCompType.getCrlTsa();
                buildAdditionalInfoCrl(crlTsa);
                // OCSP
                VFOcspType ocspTsa = marcaCompType.getOcspTsa();
                buildAdditionalInfoOcsp(ocspTsa);
            }
        }

    }

    private void addAdditionalInfoOnWrapper(VerificaFirmaWrapper wrapper) throws VerificaFirmaException {
        DecServizioVerificaCompDoc servizioVerificaCompDoc = controlliPerFirme.getDecServizioVerificaCompDoc(
                wrapper.getAdditionalInfo().getServiceCode(), wrapper.getLibraryVersion());
        wrapper.getAdditionalInfo().setIdServizioFirmaCompDoc(servizioVerificaCompDoc.getIdServizioVerificaCompDoc());
    }

    /**
     * @param firmaCompType
     *
     * @throws VerificaFirmaException
     */
    private void addAdditionalInfoOnVFirma(VFFirmaCompType firmaCompType) throws VerificaFirmaException {
        if (firmaCompType.getMarcaComps() != null) {
            for (VFMarcaCompType marcaCompType : firmaCompType.getMarcaComps()) {
                // CA
                VFCertifCaType certifTsa = marcaCompType.getCertifTsa();
                builAdditionalInfoCa(certifTsa);
                // CRL
                VFCrlType crlTsa = marcaCompType.getCrlTsa();
                buildAdditionalInfoCrl(crlTsa);
                // OCSP
                VFOcspType ocspTsa = marcaCompType.getOcspTsa();
                buildAdditionalInfoOcsp(ocspTsa);
            }
        }
        // CA
        VFCertifCaType certifCa = firmaCompType.getCertifCa();
        builAdditionalInfoCa(certifCa);
        // CRL CA
        VFCrlType crlCa = firmaCompType.getCrlCertifCa();
        buildAdditionalInfoCrl(crlCa);
        // CRL
        VFCrlType crl = firmaCompType.getCrl();
        buildAdditionalInfoCrl(crl);
        // OCSP CA
        VFOcspType ocspTsa = firmaCompType.getOcspCertifCa();
        buildAdditionalInfoOcsp(ocspTsa);
        // OCSP
        VFOcspType ocsp = firmaCompType.getOcsp();
        buildAdditionalInfoOcsp(ocsp);

        // FIRMATARIO
        VFCertifFirmatarioType certifFirmatarioType = firmaCompType.getCertifFirmatario();
        buildAdditionalInfoFirmatario(certifFirmatarioType, certifCa);
    }

    private void builAdditionalInfoCa(VFCertifCaType ca) throws VerificaFirmaException {
        if (ca != null) {
            FirCertifCa firCertifCa = getFirCertifCa(ca);

            if (firCertifCa != null) {
                ca.setAdditionalInfo(new VFAdditionalInfoCertifCaType());
                ca.getAdditionalInfo().setIdCertifCa(BigInteger.valueOf(firCertifCa.getIdCertifCa()));
            }
        }
    }

    private FirCertifCa getFirCertifCa(VFCertifCaType ca) throws VerificaFirmaException {
        FirCertifCa firCertifCa = null;
        if (ca != null) {
            firCertifCa = controlliPerFirme.getFirCertifCa(ca.getNiSerialCertifCa(), ca.getDlDnIssuerCertifCa());
        }
        return firCertifCa;
    }

    private void buildAdditionalInfoFirmatario(VFCertifFirmatarioType certifFirmatarioType, VFCertifCaType certifCa)
            throws VerificaFirmaException {
        if (certifFirmatarioType != null) {
            FirCertifCa firCertifCa = getFirCertifCa(certifCa);

            if (firCertifCa != null) {
                FirCertifFirmatario firCertifFirmatario = controlliPerFirme.getFirCertifFirmatario(firCertifCa,
                        certifFirmatarioType.getNiSerialCertifFirmatario());
                if (firCertifFirmatario != null) {
                    certifFirmatarioType.setAdditionalInfo(new VFAdditionalInfoCertifFirmatarioType());

                    certifFirmatarioType.getAdditionalInfo()
                            .setIdCertifFirmatario(BigInteger.valueOf(firCertifFirmatario.getIdCertifFirmatario()));
                }
            }
        }
    }

    private void buildAdditionalInfoCrl(VFCrlType crl) throws VerificaFirmaException {
        if (crl != null) {
            VFCertifCaType ca = crl.getCertifCa();
            FirCertifCa firCertifCa = getFirCertifCa(ca);

            if (firCertifCa != null) {
                ca.setAdditionalInfo(new VFAdditionalInfoCertifCaType());
                ca.getAdditionalInfo().setIdCertifCa(BigInteger.valueOf(firCertifCa.getIdCertifCa()));

                FirCrl firCrl = controlliPerFirme.getFirCrl(firCertifCa, crl.getNiSerialCrl(),
                        XmlDateUtility.xmlGregorianCalendarToDate(crl.getDtIniCrl()),
                        XmlDateUtility.xmlGregorianCalendarToDate(crl.getDtScadCrl()));
                if (firCrl != null) {
                    crl.setAdditionalInfo(new VFAdditionalInfoCrlType());
                    crl.getAdditionalInfo().setIdCrl(BigInteger.valueOf(firCrl.getIdCrl()));
                }

            }
        }
    }

    private void buildAdditionalInfoOcsp(VFOcspType ocsp) throws VerificaFirmaException {
        if (ocsp != null) {
            VFCertifCaType ca = ocsp.getCertifOcsp().getCertifCa();
            FirCertifCa firCertifCa = getFirCertifCa(ca);

            if (firCertifCa != null) {
                ca.setAdditionalInfo(new VFAdditionalInfoCertifCaType());
                ca.getAdditionalInfo().setIdCertifCa(BigInteger.valueOf(firCertifCa.getIdCertifCa()));

                FirCertifOcsp firCertifOcsp = controlliPerFirme.getFirCertifOcsp(firCertifCa,
                        ocsp.getCertifOcsp().getNiSerialCertifOcsp());
                if (firCertifOcsp != null) {
                    ocsp.getCertifOcsp().setAdditionalInfo(new VFAdditionalInfoCertifOcspType());
                    ocsp.getCertifOcsp().getAdditionalInfo()
                            .setIdCertifOcsp(BigInteger.valueOf(firCertifOcsp.getIdCertifOcsp()));
                }

                FirOcsp firOcsp = controlliPerFirme.getFirOcsp(firCertifOcsp, ocsp.getDsCertifIssuername(),
                        ocsp.getDsCertifSerialBase64(), ocsp.getDsCertifSkiBase64());
                if (firOcsp != null) {
                    ocsp.setAdditionalInfo(new VFAdditionalInfoOcspType());
                    ocsp.getAdditionalInfo().setIdOcsp(BigInteger.valueOf(firOcsp.getIdOcsp()));
                }
            }
        }
    }
}
