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

import java.util.List;
import java.util.Map;

import it.eng.parer.firma.exception.VerificaFirmaConnectionException;
import it.eng.parer.firma.exception.VerificaFirmaGenericInvokeException;
import it.eng.parer.firma.exception.VerificaFirmaWrapperGenericException;
import it.eng.parer.firma.exception.VerificaFirmaWrapperResNotFoundException;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.ws.versamento.dto.AbsVersamentoExt;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import java.time.ZonedDateTime;

public interface IVerificaFirmaInvoker {

    /**
     * Effettua verifica e wrappping della risposta
     *
     * @param componenteVers
     *            componente versato
     * @param sottoComponentiFirma
     *            sottocomponente versato di tipo "FIRMA"
     * @param sottoComponentiMarca
     *            sottocomponente versato di tipo "MARCA"
     * @param controlliAbilitati
     *            controlli abilitati (e.g. abilita controllo CRL, etc.)
     * @param dataVersamento
     *            data versamento
     * @param verificaAllaDataDiFirma
     *            flag 1/0 (true/false) per abilitare o meno la verifica alla data di firma
     * @param uuid
     *            UUID
     * @param versamento
     *            astrazione con metadati di versamento
     *
     * @return wrapper compilato con risposta da microservizio
     *
     * @throws VerificaFirmaWrapperResNotFoundException
     *             eccezione generica
     * @throws VerificaFirmaConnectionException
     *             eccezione dovuta a mancanza di ristosta dell'endpoint
     * @throws VerificaFirmaWrapperGenericException
     *             eccezione generica durante wrapping riposta da microservizio
     * @throws VerificaFirmaGenericInvokeException
     *             eccezione generica durante chiamata endpoint
     */
    VerificaFirmaWrapper verificaAndWrapIt(ComponenteVers componenteVers, List<ComponenteVers> sottoComponentiFirma,
            List<ComponenteVers> sottoComponentiMarca, Map<String, Boolean> controlliAbilitati,
            ZonedDateTime dataVersamento, boolean verificaAllaDataDiFirma, String uuid, AbsVersamentoExt versamento)
            throws VerificaFirmaWrapperResNotFoundException, VerificaFirmaConnectionException,
            VerificaFirmaWrapperGenericException, VerificaFirmaGenericInvokeException;

}
