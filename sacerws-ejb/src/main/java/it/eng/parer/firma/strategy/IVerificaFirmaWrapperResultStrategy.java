/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.firma.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.firma.exception.VerificaFirmaConnectionException;
import it.eng.parer.firma.exception.VerificaFirmaWrapperGenericException;
import it.eng.parer.firma.exception.VerificaFirmaWrapperResNotFoundException;
import it.eng.parer.firma.xml.VFAdditionalInfoWrapperType;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import java.time.ZonedDateTime;

public interface IVerificaFirmaWrapperResultStrategy<E extends Object> {

    static final Logger LOG = LoggerFactory.getLogger(IVerificaFirmaWrapperResultStrategy.class);

    String getCode();

    // business logic
    default VerificaFirmaWrapper fromVerificaOutOnWrapper(E esito, ZonedDateTime dtRef) throws Exception {
        VerificaFirmaWrapper localWrapper = new VerificaFirmaWrapper();
        this.fromVerificaOutOnWrapper(esito, localWrapper, dtRef);
        return localWrapper;
    }

    void fromVerificaOutOnWrapper(E esito, VerificaFirmaWrapper wrapper, ZonedDateTime dtRef) throws Exception;

    default VerificaFirmaWrapper buildVFWrapper(E result, ZonedDateTime dtRef, boolean isDetached)
            throws VerificaFirmaWrapperResNotFoundException, VerificaFirmaWrapperGenericException,
            VerificaFirmaConnectionException {

        if (result == null) {
            LOG.error("Esito {} verifica firma non presente ! ", getCode());
            throw new VerificaFirmaWrapperResNotFoundException(
                    "Esito (" + getCode() + ") verifica firma non presente ! ", getCode());
        }

        // init wrapper
        VerificaFirmaWrapper wrapper = new VerificaFirmaWrapper();
        // additional info
        VFAdditionalInfoWrapperType additionalInfoWrapperType = new VFAdditionalInfoWrapperType();
        wrapper.setAdditionalInfo(additionalInfoWrapperType);
        //
        additionalInfoWrapperType.setServiceCode(getCode());
        additionalInfoWrapperType.setIsDetached(isDetached);
        try {
            LOG.debug("Inizio popolamento esito da [ {} ]", getCode());
            // populate with output firma
            fromVerificaOutOnWrapper(result, wrapper, dtRef);
            LOG.debug("Termine popolamento esito da [{}]", getCode());
        } catch (Exception ex) {
            throw new VerificaFirmaWrapperGenericException(ex, wrapper);
        }

        return wrapper;
    }

}
