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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.firma.strategy;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.firma.exception.VerificaFirmaConnectionException;
import it.eng.parer.firma.exception.VerificaFirmaWrapperGenericException;
import it.eng.parer.firma.exception.VerificaFirmaWrapperResNotFoundException;
import it.eng.parer.firma.xml.VFAdditionalInfoWrapperType;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;

public interface IVerificaFirmaWrapperResultStrategy<E extends Object> {

    static final Logger LOG = LoggerFactory.getLogger(IVerificaFirmaWrapperResultStrategy.class);

    String getCode();

    // business logic
    default VerificaFirmaWrapper fromVerificaOutOnWrapper(E esito)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        VerificaFirmaWrapper localWrapper = new VerificaFirmaWrapper();
        this.fromVerificaOutOnWrapper(esito, localWrapper);
        return localWrapper;
    }

    void fromVerificaOutOnWrapper(E esito, VerificaFirmaWrapper wrapper)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    default VerificaFirmaWrapper buildVFWrapper(E result, boolean isDetached)
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
            fromVerificaOutOnWrapper(result, wrapper);
            LOG.debug("Termine popolamento esito da [{}]", getCode());
        } catch (Exception ex) {
            throw new VerificaFirmaWrapperGenericException(ex, wrapper);
        }

        return wrapper;
    }

}
