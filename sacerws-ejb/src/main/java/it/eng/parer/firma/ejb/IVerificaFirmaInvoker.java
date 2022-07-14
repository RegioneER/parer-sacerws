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
