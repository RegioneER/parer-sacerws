/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
     * @param rule
     *            regole per invocazione
     * @param in
     *            input
     * @param versamento
     *            astrazione con metadati di versamento
     * 
     * @return wrapper del risultato ottenuto (secondo modello xsd)
     * 
     * @throws VerificaFirmaException
     *             eccezione generica
     */
    VerificaFirmaWrapper invokeVerifica(InvokeVerificaRule rule, InvokeVerificaInput in, AbsVersamentoExt versamento)
            throws VerificaFirmaException;

    void buildAdditionalInfoOnVFWrapper(VerificaFirmaWrapper wrapper) throws VerificaFirmaException;

}
