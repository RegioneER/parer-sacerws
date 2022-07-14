/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.firma.dto.input;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.eng.parer.ws.utils.ParametroApplDB.ParametroApplFl;

/**
 * @author sinatti_s
 */
public class InvokeVerificaRule implements Serializable {

    /**
     * SCENARIO DEFAULT
     * 
     * eseguiVerificaFirma = SI eseguiVerificaFirmaOnlyCrypto = NO
     *
     * PRIORITARIO = EIDAS -> SE IGNOTO = CRYPTO
     *
     * SCENARIO SOLO CRYPTO
     *
     * eseguiVerificaFirmaOnlyCrypto = SI eseguiVerificaFirmaOnlyCrypto = SI/NO
     *
     * CASI PARTICOLARI (SOLO CRYPTO)
     *
     * hasMultipleFirmaDetached = true OR hasFirmaAndMarcaDetached = true OR hasMarcaDetached = true
     */
    private static final long serialVersionUID = 8241046973565143256L;
    private boolean eseguiVerificaFirmaOnlyCrypto = false;
    //
    private boolean hasMultipleFirmaDetached = false;
    private boolean hasFirmaAndMarcaDetached = false;
    private boolean hasMarcaDetached = false;
    private boolean verificaAllaDataDiFirma = false;

    // abilitazioni
    private Map<String, Boolean> abilitazioni = defaultAbilitazioni();

    public static InvokeVerificaRule defaultRule() {
        return new InvokeVerificaRule();
    }

    private static Map<String, Boolean> defaultAbilitazioni() {
        return Stream
                .of(new Object[][] { { ParametroApplFl.FL_ABILITA_CONTR_CRITTOG_VERS, true },
                        { ParametroApplFl.FL_ABILITA_CONTR_TRUST_VERS, true },
                        { ParametroApplFl.FL_ABILITA_CONTR_CERTIF_VERS, true },
                        { ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS, true } })
                .collect(Collectors.toMap(data -> (String) data[0], data -> (Boolean) data[1]));
    }

    public boolean isHasMarcaDetached() {
        return hasMarcaDetached;
    }

    public void setHasMarcaDetached(boolean hasMarcaDetached) {
        this.hasMarcaDetached = hasMarcaDetached;
    }

    public boolean isHasMultipleFirmaDetached() {
        return hasMultipleFirmaDetached;
    }

    public void setHasMultipleFirmaDetached(boolean hasMultipleFirmaDetached) {
        this.hasMultipleFirmaDetached = hasMultipleFirmaDetached;
    }

    public boolean isVerificaAllaDataDiFirma() {
        return verificaAllaDataDiFirma;
    }

    public void setVerificaAllaDataDiFirma(boolean verificaAllaDataDiFirma) {
        this.verificaAllaDataDiFirma = verificaAllaDataDiFirma;
    }

    public boolean isHasFirmaAndMarcaDetached() {
        return hasFirmaAndMarcaDetached;
    }

    public void setHasFirmaAndMarcaDetached(boolean hasFirmaAndMarcaDetached) {
        this.hasFirmaAndMarcaDetached = hasFirmaAndMarcaDetached;
    }

    public InvokeVerificaRule withEseguiVerificaFirmaOnlyCrypto(final boolean eseguiVerificaFirmaOnlyCrypto) {
        setEseguiVerificaFirmaOnlyCrypto(eseguiVerificaFirmaOnlyCrypto);
        return this;
    }

    public InvokeVerificaRule withHasMultipleFirmaDetached(final boolean hasMultipleFirmaDetached) {
        setHasMultipleFirmaDetached(hasMultipleFirmaDetached);
        return this;
    }

    public InvokeVerificaRule withHasMarcaDetached(final boolean hasMarcaDetached) {
        setHasMarcaDetached(hasMarcaDetached);
        return this;
    }

    public InvokeVerificaRule withVerificaAllaDataFirma(final boolean verificaAllaDataDiFirma) {
        setVerificaAllaDataDiFirma(verificaAllaDataDiFirma);
        return this;
    }

    public InvokeVerificaRule withFirmaAndMarcaDetached(final boolean hasFirmaAndMarcaDetached) {
        setHasFirmaAndMarcaDetached(hasFirmaAndMarcaDetached);
        return this;
    }

    public Map<String, Boolean> getAbilitazioni() {
        return abilitazioni;
    }

    public void setAbilitazioni(Map<String, Boolean> abilitazioni) {
        this.abilitazioni = abilitazioni;
    }

    public boolean isEseguiVerificaFirmaOnlyCrypto() {
        return eseguiVerificaFirmaOnlyCrypto;
    }

    public void setEseguiVerificaFirmaOnlyCrypto(boolean eseguiVerificaFirmaOnlyCrypto) {
        this.eseguiVerificaFirmaOnlyCrypto = eseguiVerificaFirmaOnlyCrypto;
    }
}
