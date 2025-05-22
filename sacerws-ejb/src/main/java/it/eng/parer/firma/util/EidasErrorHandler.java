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

package it.eng.parer.firma.util;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.parer.eidas.model.exception.EidasParerException;
import it.eng.parer.eidas.model.exception.ParerError;

/**
 * Gestore centralizzato degli erroi REST. Viene innescato solo quando la chiamata "atterra"
 * sull'endpoint. Viene emessa in ogni caso un'eccezione di tipo {@link EidasParerException}
 */
public class EidasErrorHandler extends DefaultResponseErrorHandler {

    private final Logger log = LoggerFactory.getLogger(EidasErrorHandler.class);

    private final ObjectMapper objectMapper;

    public EidasErrorHandler() {
	objectMapper = new ObjectMapper();
	objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /*
     * HttpCode intercettati su risposta fornita dall'endpoint. Per maggiori dettagli, vedere
     * modello swagger con definizione completa di tutti gli status code gestiti.
     */
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
	return (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR
		|| response.getStatusCode() == HttpStatus.BAD_REQUEST
		|| response.getStatusCode() == HttpStatus.EXPECTATION_FAILED);
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
	EidasParerException exceptionFromJson = null;
	try {
	    exceptionFromJson = objectMapper.readValue(response.getBody(),
		    EidasParerException.class);
	    log.debug("Eccezione registrata {} con codice {}", exceptionFromJson,
		    exceptionFromJson.getCode());
	} catch (IOException e) {
	    exceptionFromJson = new EidasParerException()
		    .withCode(ParerError.ErrorCode.GENERIC_ERROR).withMessage("Errore "
			    + response.getRawStatusCode() + ": " + response.getStatusCode().name());
	    log.debug("Eccezione sull'oggetto di errore costruito", e);
	}

	throw exceptionFromJson;
    }

}
