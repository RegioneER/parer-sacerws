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

/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.firma.ejb;

import it.eng.parer.firma.dto.input.InvokeVerificaInput;
import it.eng.parer.firma.dto.input.InvokeVerificaRule;
import it.eng.parer.firma.exception.VerificaFirmaException;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.ws.versamento.dto.AbsVersamentoExt;

public interface IGenericVerificaFirmaWrapperResult {

    /**
     * Invocazione verifica firma (microservices eidas/crypto)
     *
     * @param rule       regole per invocazione
     * @param in         input
     * @param versamento astrazione con metadati di versamento
     *
     * @return wrapper del risultato ottenuto (secondo modello xsd)
     *
     * @throws VerificaFirmaException eccezione generica
     */
    VerificaFirmaWrapper invokeVerifica(InvokeVerificaRule rule, InvokeVerificaInput in,
	    AbsVersamentoExt versamento) throws VerificaFirmaException;

    void buildAdditionalInfoOnVFWrapper(VerificaFirmaWrapper wrapper) throws VerificaFirmaException;

}
