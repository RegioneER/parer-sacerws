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

package it.eng.parer.firma.strategy;

import static it.eng.parer.ws.utils.XmlDateUtility.dateToXMLGregorianCalendar;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.crypto.model.verifica.CryptoAroCompDoc;
import it.eng.parer.crypto.model.verifica.CryptoAroContrFirmaComp;
import it.eng.parer.crypto.model.verifica.CryptoAroContrMarcaComp;
import it.eng.parer.crypto.model.verifica.CryptoAroControfirmaFirma;
import it.eng.parer.crypto.model.verifica.CryptoAroFirmaComp;
import it.eng.parer.crypto.model.verifica.CryptoAroMarcaComp;
import it.eng.parer.crypto.model.verifica.CryptoAroUsoCertifCaContrComp;
import it.eng.parer.crypto.model.verifica.CryptoAroUsoCertifCaContrMarca;
import it.eng.parer.crypto.model.verifica.CryptoFirCertifCa;
import it.eng.parer.crypto.model.verifica.CryptoFirCertifFirmatario;
import it.eng.parer.crypto.model.verifica.CryptoFirCrl;
import it.eng.parer.crypto.model.verifica.CryptoFirUrlDistribCrl;
import it.eng.parer.entity.constraint.DecServizioVerificaCompDoc.CdServizioVerificaCompDoc;
import it.eng.parer.firma.util.VerificaFirmaEnums.SacerIndication;
import it.eng.parer.firma.xml.VFAdditionalInfoFirmaCompType;
import it.eng.parer.firma.xml.VFAdditionalInfoMarcaCompType;
import it.eng.parer.firma.xml.VFCertifCaType;
import it.eng.parer.firma.xml.VFCertifFirmatarioType;
import it.eng.parer.firma.xml.VFContrFirmaCompType;
import it.eng.parer.firma.xml.VFContrMarcaCompType;
import it.eng.parer.firma.xml.VFCrlType;
import it.eng.parer.firma.xml.VFFirmaCompType;
import it.eng.parer.firma.xml.VFMarcaCompType;
import it.eng.parer.firma.xml.VFTipoControlloType;
import it.eng.parer.firma.xml.VFTipoFirmaType;
import it.eng.parer.firma.xml.VFUrlDistribCrlType;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.firma.xml.VerificaFirmaWrapper.VFBusta;

public class CryptoWrapperResultStrategy implements IVerificaFirmaWrapperResultStrategy<CryptoAroCompDoc> {

    private final Logger LOG = LoggerFactory.getLogger(CryptoWrapperResultStrategy.class);

    public CryptoWrapperResultStrategy() {
        super();
    }

    @Override
    public String getCode() {
        return CdServizioVerificaCompDoc.CRYPTO.name();
    }

    @Override
    public void fromVerificaOutOnWrapper(CryptoAroCompDoc result, VerificaFirmaWrapper wrapper) {
        // Inizio e fine validazione

        wrapper.setStartDate(dateToXMLGregorianCalendar(result.getInizioValidazione()));
        wrapper.setEndDate(dateToXMLGregorianCalendar(result.getFineValidazione()));
        // set service version
        wrapper.setServiceVersion(result.getValidatorVersion());
        // // set library version
        wrapper.setLibraryVersion(result.getLibraryVersion());
        // SelfLink del servizio di validazione chiamato
        wrapper.setSelfLink(result.getLink());
        wrapper.getAdditionalInfo().setTikaMime(result.getTikaMimeComponentePrincipale());

        Map<BigDecimal, VFBusta> buste = new HashMap<>();

        riorganizzaGerarchiaFirme(result);

        // Firme
        for (CryptoAroFirmaComp aroFirmaComp : result.getAroFirmaComps()) {
            BigDecimal pgBusta = aroFirmaComp.getPgBusta();
            VFBusta busta = buste.get(pgBusta);
            if (busta == null) {
                busta = new VFBusta();
                buste.put(pgBusta, busta);
                busta.setPgBusta(pgBusta);
            }

            VFFirmaCompType firma = buildFirma(aroFirmaComp);
            busta.getFirmaComps().add(firma);
        }

        // Marche
        for (CryptoAroMarcaComp aroMarcaComp : result.getAroMarcaComps()) {
            BigDecimal pgBusta = aroMarcaComp.getPgBusta();
            VFBusta busta = buste.get(pgBusta);
            if (busta == null) {
                busta = new VFBusta();
                buste.put(pgBusta, busta);
                busta.setPgBusta(pgBusta);
            }

            VFMarcaCompType marca = buildMarca(aroMarcaComp);
            busta.getMarcaComps().add(marca);
        }
        buste.values().forEach(b -> wrapper.getVFBusta().add(b));

        // Report
        wrapper.getAdditionalInfo().setReportContent(result);
        if (LOG.isDebugEnabled()) {
            LOG.debug("I componenti versati sono [{}]", String.join(",", componentiVersati(result)));
        }
    }

    /**
     * Modifica la lista delle firme per allinearle, relativamente a controfirme alla struttura utilizzata dal wrapper
     *
     * @param resultCrypto
     *            risultato della verifica del ws crypto
     */
    private void riorganizzaGerarchiaFirme(CryptoAroCompDoc resultCrypto) {
        // Filtro le firme dagli eventuali doppioni in caso di controfirme
        resultCrypto.setAroFirmaComps(normalizzaFirmeConControfirme(resultCrypto));
    }

    /**
     * Nel modello crypto se una firma appare tra le controfirme compare anche tra le firme "normali" creando, quindi,
     * una duplicazione da eliminare
     *
     * @param resultCrypto
     *            output del ws crypto
     * 
     * @return lista delle firme modificata
     */
    private List<CryptoAroFirmaComp> normalizzaFirmeConControfirme(CryptoAroCompDoc resultCrypto) {
        List<CryptoAroFirmaComp> firmeEsitoCrypto = resultCrypto.getAroFirmaComps();

        // Cortocircuito 1, se non ho firme non avanzo con la logica
        if (firmeEsitoCrypto == null || firmeEsitoCrypto.isEmpty()) {
            return firmeEsitoCrypto;
        }

        List<ChiaveFirma> controFirme = new ArrayList<>();

        // firme verificate con la controfirma
        List<CryptoAroFirmaComp> firmeConControfirma = firmeEsitoCrypto.stream()
                .filter(f -> f.getAroControfirmaFirmaFiglios() != null && !f.getAroControfirmaFirmaFiglios().isEmpty())
                .collect(Collectors.toList());

        // Cortocircuito 2, se non ho controfirme non avanzo con la logica
        if (firmeConControfirma.isEmpty()) {
            return firmeEsitoCrypto;
        }

        // Identifico le controfirme presenti
        for (CryptoAroFirmaComp firma : firmeConControfirma) {
            for (CryptoAroControfirmaFirma controFirma : firma.getAroControfirmaFirmaFiglios()) {
                CryptoAroFirmaComp aroFirmaFiglio = controFirma.getAroFirmaFiglio();
                if (aroFirmaFiglio != null) {
                    controFirme.add(new ChiaveFirma(aroFirmaFiglio));
                }
            }

        }

        // Elimino, dalle firme quelle già censite tra le controfirme
        List<CryptoAroFirmaComp> result = new ArrayList<>();
        for (CryptoAroFirmaComp firma : firmeEsitoCrypto) {
            if (!controFirme.contains(new ChiaveFirma(firma))) {
                result.add(firma);
            }
        }

        return result;

    }

    /**
     * Classe statica utilizzata per poter implementare {@link #equals(java.lang.Object)} e {@link #hashCode()} sulle
     * firme per poter chiamare il metodo {@link List#contains(java.lang.Object) } avendo la certezza che identifichi
     * correttamente il record.
     *
     */
    private static final class ChiaveFirma {

        private final String base64;
        private final int pgBusta;
        private final int pgFirma;

        ChiaveFirma(CryptoAroFirmaComp firma) {
            this.base64 = firma.getDsFirmaBase64();
            this.pgBusta = firma.getPgBusta().intValue();
            this.pgFirma = firma.getPgFirma().intValue();
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 17 * hash + Objects.hashCode(this.base64);
            hash = 17 * hash + this.pgBusta;
            hash = 17 * hash + this.pgFirma;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ChiaveFirma other = (ChiaveFirma) obj;
            if (this.pgBusta != other.pgBusta) {
                return false;
            }
            if (this.pgFirma != other.pgFirma) {
                return false;
            }
            if (!Objects.equals(this.base64, other.base64)) {
                return false;
            }
            return true;
        }
    }

    /**
     * Ottengo gli id dei componenti univoci versati.
     *
     * @param resultVerificaCrypto
     *            wrapper
     * 
     * @return insieme non nullo.
     */
    private Set<String> componentiVersati(CryptoAroCompDoc resultVerificaCrypto) {
        Set<String> componentiVersati = new HashSet<>();
        if (resultVerificaCrypto.getAroFirmaComps() != null) {
            resultVerificaCrypto.getAroFirmaComps().stream().forEach(f -> {
                if (f.getIdFirma() != null) {
                    componentiVersati.add(f.getIdFirma());
                }
                if (f.getAroMarcaComp() != null && f.getAroMarcaComp().getIdMarca() != null) {
                    componentiVersati.add(f.getAroMarcaComp().getIdMarca());
                }
            });

        }
        if (resultVerificaCrypto.getAroMarcaComps() != null) {
            resultVerificaCrypto.getAroMarcaComps().stream().forEach(m -> {
                if (m.getIdMarca() != null) {
                    componentiVersati.add(m.getIdMarca());
                }
            });
        }
        return componentiVersati;
    }

    /**
     * Costruiisci l'oggetto "firma"
     *
     * @param cryptoFirma
     * 
     * @return firmacomptype
     */
    private VFFirmaCompType buildFirma(CryptoAroFirmaComp cryptoFirma) {

        VFFirmaCompType firma = new VFFirmaCompType();
        firma.setId(cryptoFirma.getIdFirma());
        firma.setCdFirmatario(cryptoFirma.getCdFirmatario());
        // FIXME: questo non può essere nullo!!!
        if (cryptoFirma.getFirCertifFirmatario() != null) {
            VFCertifFirmatarioType certifFirmatario = buildFirmatario(cryptoFirma.getFirCertifFirmatario());
            firma.setCertifFirmatario(certifFirmatario);
        }

        if (cryptoFirma.getAroControfirmaFirmaFiglios() != null) {
            for (CryptoAroControfirmaFirma cryptoControFirma : cryptoFirma.getAroControfirmaFirmaFiglios()) {
                CryptoAroFirmaComp aroFirmaFiglio = cryptoControFirma.getAroFirmaFiglio();
                if (aroFirmaFiglio != null) {
                    VFFirmaCompType firmaFiglio = buildFirma(aroFirmaFiglio);
                    firma.getControfirmaFirmaFiglios().add(firmaFiglio);
                }
            }
        }

        // Controlli ed uso
        for (CryptoAroContrFirmaComp aroContrFirmaComp : cryptoFirma.getAroContrFirmaComps()) {

            VFContrFirmaCompType contrFirmaComp = new VFContrFirmaCompType();
            contrFirmaComp.setDsMsgEsitoContrFirma(aroContrFirmaComp.getDsMsgEsitoContrFirma());
            contrFirmaComp.setTiEsitoContrFirma(aroContrFirmaComp.getTiEsitoContrFirma());
            contrFirmaComp.setTiContr(VFTipoControlloType.fromValue(aroContrFirmaComp.getTiContr()));

            firma.getContrFirmaComps().add(contrFirmaComp);

            CryptoFirCrl firCrlControllo = aroContrFirmaComp.getFirCrl();

            // SOLO PER IL CONTROLLO CRL
            if (firCrlControllo != null) {
                VFCrlType crlControllo = buildCrl(firCrlControllo);
                firma.setCrl(crlControllo);
            }

            CryptoAroUsoCertifCaContrComp aroUsoCertifCaContrComp = null;
            // se esite è uno solo, quello di tipo CATENA_TRUSTED (specifictà di Crypto)
            if (aroContrFirmaComp.getAroUsoCertifCaContrComps() != null
                    && !aroContrFirmaComp.getAroUsoCertifCaContrComps().isEmpty()) {
                aroUsoCertifCaContrComp = aroContrFirmaComp.getAroUsoCertifCaContrComps().get(0);

                CryptoFirCertifCa firCertifCa = aroUsoCertifCaContrComp.getFirCertifCa();
                if (firCertifCa != null) {
                    VFCertifCaType certifCA = buildCertifCA(firCertifCa);
                    firma.setCertifCa(certifCA);
                }
                // Non dovrebbe essere più popolato.
                CryptoFirCrl usoCertificatoCrl = aroUsoCertifCaContrComp.getFirCrl();
                if (usoCertificatoCrl != null) {
                    VFCrlType certifCrl = buildCrl(usoCertificatoCrl);
                    firma.setCrlCertifCa(certifCrl);
                }
            }
        }
        // Aggiungo il controllo OCSP (con esito NON_NECESSARIO)
        VFContrFirmaCompType contrFirmaCompOCSP = new VFContrFirmaCompType();
        contrFirmaCompOCSP.setTiContr(VFTipoControlloType.OCSP);
        contrFirmaCompOCSP.setTiEsitoContrFirma(SacerIndication.NON_NECESSARIO.name());
        contrFirmaCompOCSP.setDsMsgEsitoContrFirma(SacerIndication.NON_NECESSARIO.message());

        firma.getContrFirmaComps().add(contrFirmaCompOCSP);
        firma.setDlDnFirmatario(cryptoFirma.getDlDnFirmatario());
        firma.setDsAlgoFirma(cryptoFirma.getDsAlgoFirma());
        firma.setDsFirmaBase64(cryptoFirma.getDsFirmaBase64());
        firma.setDsMsgEsitoContrConforme(cryptoFirma.getDsMsgEsitoContrConforme());
        /*
         * Attenzione! la data firma può essere nulla. In quel caso *dovrebbe* essere valorizzato il rif. temporale
         * versato
         */
        if (cryptoFirma.getDtFirma() != null) {
            firma.setDtFirma(dateToXMLGregorianCalendar(cryptoFirma.getDtFirma()));
        }
        if (cryptoFirma.getTmRifTempUsato() != null) {
            firma.setTmRifTempUsato(dateToXMLGregorianCalendar(cryptoFirma.getTmRifTempUsato()));
        }

        firma.setDsMsgEsitoVerifFirma(cryptoFirma.getDsMsgEsitoVerifFirma());

        firma.setNmCognomeFirmatario(cryptoFirma.getNmCognomeFirmatario());
        firma.setNmFirmatario(cryptoFirma.getNmFirmatario());
        firma.setPgFirma(cryptoFirma.getPgFirma());
        firma.setTiEsitoContrConforme(cryptoFirma.getTiEsitoContrConforme());
        firma.setTiEsitoVerifFirma(cryptoFirma.getTiEsitoVerifFirma());
        firma.setTiFirma(VFTipoFirmaType.fromValue(cryptoFirma.getTiFirma()));
        firma.setTiFormatoFirma(cryptoFirma.getTiFormatoFirma());
        firma.setTipoRiferimentoTemporaleUsato(cryptoFirma.getTiRifTempUsato());

        // Marca della firma
        CryptoAroMarcaComp aroMarcaComp = cryptoFirma.getAroMarcaComp();
        if (aroMarcaComp != null) {
            VFMarcaCompType marca = buildMarca(aroMarcaComp);
            firma.getMarcaComps().add(marca);
        }

        VFAdditionalInfoFirmaCompType infoFirma = new VFAdditionalInfoFirmaCompType();

        infoFirma.setTiRifTempUsato(cryptoFirma.getTiRifTempUsato());
        firma.setAdditionalInfo(infoFirma);
        return firma;
    }

    /**
     * Popola le informazioni relative alla marca temporale.
     *
     * @param cryptoMarca
     * 
     * @return marcacomptype
     */
    private VFMarcaCompType buildMarca(CryptoAroMarcaComp cryptoMarca) {
        VFMarcaCompType marca = new VFMarcaCompType();
        marca.setId(cryptoMarca.getIdMarca());

        marca.setDsAlgoMarca(cryptoMarca.getDsAlgoMarca());
        marca.setDsMarcaBase64(cryptoMarca.getDsMarcaBase64());
        marca.setDsMsgEsitoContrConforme(cryptoMarca.getDsMsgEsitoContrConforme());
        marca.setDsMsgEsitoVerifMarca(cryptoMarca.getDsMsgEsitoVerifMarca());
        marca.setDtScadMarca(dateToXMLGregorianCalendar(cryptoMarca.getDtScadMarca()));
        marca.setPgMarca(cryptoMarca.getPgMarca());
        marca.setTiEsitoContrConforme(cryptoMarca.getTiEsitoContrConforme());
        marca.setTiEsitoVerifMarca(cryptoMarca.getTiEsitoVerifMarca());
        marca.setTiFormatoMarca(cryptoMarca.getTiFormatoMarca());
        marca.setTmMarcaTemp(dateToXMLGregorianCalendar(cryptoMarca.getTmMarcaTemp()));

        CryptoFirCertifCa firCertifCaMarca = cryptoMarca.getFirCertifCa();
        if (firCertifCaMarca != null) {
            VFCertifCaType certifTSA = buildCertifCA(firCertifCaMarca);
            marca.setCertifTsa(certifTSA);
        }
        VFAdditionalInfoMarcaCompType infoMarca = new VFAdditionalInfoMarcaCompType();
        infoMarca.setTiMarcaTemp(cryptoMarca.getTiMarcaTemp());
        marca.setAdditionalInfo(infoMarca);

        // Controlli e uso
        for (CryptoAroContrMarcaComp aroContrMarcaComp : cryptoMarca.getAroContrMarcaComps()) {
            VFContrMarcaCompType contrMarcaComp = new VFContrMarcaCompType();
            // FIXME controllare che i valori siano all'interno di quelli possibili
            contrMarcaComp.setTiContr(VFTipoControlloType.valueOf(aroContrMarcaComp.getTiContr()));
            contrMarcaComp.setTiEsitoContrMarca(aroContrMarcaComp.getTiEsitoContrMarca());
            contrMarcaComp.setDsMsgEsitoContrMarca(aroContrMarcaComp.getDsMsgEsitoContrMarca());

            CryptoFirCrl firCrlControllo = aroContrMarcaComp.getFirCrl();

            // SOLO PER IL CONTROLLO CRL
            if (firCrlControllo != null) {
                VFCrlType crlControllo = buildCrl(firCrlControllo);
                marca.setCrlTsa(crlControllo);
            }
            CryptoAroUsoCertifCaContrMarca aroUsoCertifCaContrMarca = null;

            // se esite è uno solo, quello di tipo CATENA_TRUSTED (specifictà di Crypto)
            if (aroContrMarcaComp.getAroUsoCertifCaContrMarcas() != null
                    && !aroContrMarcaComp.getAroUsoCertifCaContrMarcas().isEmpty()) {
                aroUsoCertifCaContrMarca = aroContrMarcaComp.getAroUsoCertifCaContrMarcas().get(0);

                CryptoFirCertifCa firCertifCa = aroUsoCertifCaContrMarca.getFirCertifCa();
                if (firCertifCa != null) {
                    VFCertifCaType certifCa = buildCertifCA(firCertifCa);
                    marca.setCertifTsa(certifCa);
                }

                CryptoFirCrl firCrl = aroUsoCertifCaContrMarca.getFirCrl();
                if (firCrl != null) {
                    VFCrlType crlCa = buildCrl(firCrl);
                    marca.setCrlTsa(crlCa);
                }
            }
            marca.getContrMarcaComps().add(contrMarcaComp);
        }

        // Aggiungo il controllo OCSP (con esito NON_NECESSARIO)
        VFContrMarcaCompType contrMarcaCompOCSP = new VFContrMarcaCompType();
        contrMarcaCompOCSP.setTiContr(VFTipoControlloType.OCSP);
        contrMarcaCompOCSP.setTiEsitoContrMarca(SacerIndication.NON_NECESSARIO.name());
        contrMarcaCompOCSP.setDsMsgEsitoContrMarca(SacerIndication.NON_NECESSARIO.message());
        marca.getContrMarcaComps().add(contrMarcaCompOCSP);

        return marca;
    }

    /**
     * Popola il certificato del firmatario corredato dalla sua CA.
     *
     * @param cryptoFirmatario
     *            modello del firmatario per la crypto
     * 
     * @return modello del firmatario per il wrapper.
     */
    private VFCertifFirmatarioType buildFirmatario(CryptoFirCertifFirmatario cryptoFirmatario) {
        VFCertifFirmatarioType certifFirmatario = new VFCertifFirmatarioType();
        certifFirmatario.setDtIniValCertifFirmatario(
                dateToXMLGregorianCalendar(cryptoFirmatario.getDtIniValCertifFirmatario()));
        certifFirmatario.setDtFinValCertifFirmatario(
                dateToXMLGregorianCalendar(cryptoFirmatario.getDtFinValCertifFirmatario()));
        certifFirmatario.setNiSerialCertifFirmatario(cryptoFirmatario.getNiSerialCertifFirmatario());

        if (cryptoFirmatario.getFirCertifCa() != null) {
            VFCertifCaType certifCA = buildCertifCA(cryptoFirmatario.getFirCertifCa());
            certifFirmatario.setCertifCaFirmatario(certifCA);
        }

        return certifFirmatario;
    }

    /**
     * Compila la busta relativa alla CA (e tutte le sottobuste)
     *
     * @param cryptoCa
     *            Oggetto proveniente dalla Vecchia cryptolibrary.
     * 
     * @return Oggetto del wrapper contentente
     */
    private VFCertifCaType buildCertifCA(CryptoFirCertifCa cryptoCa) {

        VFCertifCaType certifCA = new VFCertifCaType();
        certifCA.setDsSubjectKeyId(cryptoCa.getDsSubjectKeyId());
        certifCA.setDtIniValCertifCa(dateToXMLGregorianCalendar(cryptoCa.getDtIniValCertifCa()));
        certifCA.setDtFinValCertifCa(dateToXMLGregorianCalendar(cryptoCa.getDtFinValCertifCa()));
        certifCA.setNiSerialCertifCa(cryptoCa.getNiSerialCertifCa());
        certifCA.setDlDnIssuerCertifCa(cryptoCa.getFirIssuer().getDlDnIssuerCertifCa());
        certifCA.setDlDnSubjectCertifCa(cryptoCa.getFirIssuer().getDlDnSubjectCertifCa());

        // Issuer
        for (CryptoFirUrlDistribCrl urlDistribCrl : cryptoCa.getFirUrlDistribCrls()) {
            VFUrlDistribCrlType urlDistribCrlType = new VFUrlDistribCrlType();
            urlDistribCrlType.setNiOrdUrlDistribCrl(urlDistribCrl.getNiOrdUrlDistribCrl());
            urlDistribCrlType.setDlUrlDistribCrl(urlDistribCrl.getDlUrlDistribCrl());
            certifCA.getUrlDistribCrls().add(urlDistribCrlType);
        }

        return certifCA;
    }

    /**
     * Compila la busta relativa alle CRL. <em>ATTENZIONE:</em> il blob delle crl ({@code parerCrl.getEncoded()}) può
     * arrivare a superare i 40MB.
     *
     * @param cryptoCrl
     *            Modello di crl restituito da crypto
     * 
     * @return modello di crl del Wrapper.
     */
    private VFCrlType buildCrl(CryptoFirCrl cryptoCrl) {
        VFCrlType crl = new VFCrlType();
        crl.setDtIniCrl(dateToXMLGregorianCalendar(cryptoCrl.getDtIniCrl()));
        crl.setDtScadCrl(dateToXMLGregorianCalendar(cryptoCrl.getDtScadCrl()));
        crl.setNiSerialCrl(cryptoCrl.getNiSerialCrl());

        CryptoFirCertifCa firCertifCa = cryptoCrl.getFirCertifCa();
        VFCertifCaType buildCertifCA = buildCertifCA(firCertifCa);
        crl.setCertifCa(buildCertifCA);

        return crl;
    }
}
