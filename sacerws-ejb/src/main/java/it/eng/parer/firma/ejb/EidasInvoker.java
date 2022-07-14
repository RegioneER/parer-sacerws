package it.eng.parer.firma.ejb;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import it.eng.parer.eidas.model.EidasMetadataToValidate;
import it.eng.parer.eidas.model.EidasWSReportsDTOTree;
import it.eng.parer.eidas.model.EsitoValidazioneEidas;
import it.eng.parer.entity.constraint.DecServizioVerificaCompDoc.CdServizioVerificaCompDoc;
import it.eng.parer.firma.crypto.helper.EidasRestConfiguratorHelper;
import it.eng.parer.firma.exception.VerificaFirmaConnectionException;
import it.eng.parer.firma.exception.VerificaFirmaGenericInvokeException;
import it.eng.parer.firma.exception.VerificaFirmaWrapperGenericException;
import it.eng.parer.firma.exception.VerificaFirmaWrapperResNotFoundException;
import it.eng.parer.firma.strategy.EidasWrapperResultStrategy;
import it.eng.parer.firma.util.EidasErrorHandler;
import it.eng.parer.firma.util.EidasUtils;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.retry.ParerRetryConfiguration;
import it.eng.parer.retry.RestRetryInterceptor;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.ejb.XmlVersCache;
import it.eng.parer.ws.versamento.dto.AbsVersamentoExt;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import java.time.ZonedDateTime;

/**
 * Oggetto di business per verificare la firma secondo il Regolamento (UE) N. 910/2014 del Parlamento europero e del
 * Consiglio del 23 luglio 2014 (EIDAS)
 *
 * @author Snidero_L
 */
@Stateless
@LocalBean
public class EidasInvoker implements IVerificaFirmaInvoker {

    private static final Logger LOG = LoggerFactory.getLogger(EidasInvoker.class.getName());

    @EJB
    protected ConfigurationHelper configurationHelper;

    @EJB
    protected XmlVersCache xmlVersCache;

    @EJB
    protected EidasRestConfiguratorHelper restInvoker;

    private static final String FIRMA_API_PATH = "/v2/report-verifica";

    /**
     * Si collega all'endpoint contenente l'health check per il servizio.
     *
     * @param url
     *            completo dell'endpoint
     *
     * @return true se l'applicazion è su
     */
    public boolean isUp(String url) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
            return forEntity.getStatusCode().equals(HttpStatus.OK);
        } catch (RestClientException ex) {
            LOG.warn("Impossibile contattare " + url, ex);
        }
        return false;
    }

    /**
     * Invocazione al WS REST di EIDAS con chiamata Multipart.
     *
     * @param metadata
     *            metadati associati alla verifica
     * @param componenteVers
     *            componente principale del versamento
     * @param sottoComponentiFirma
     *            sottomponente contenente la firma
     *
     * @return modello contenente l'esito della verifica.
     *
     * @throws VerificaFirmaConnectionException
     *             eccezione in caso di mancata risposta
     * @throws VerificaFirmaGenericInvokeException
     *             eccezione in caso di risposta errata
     */
    private EsitoValidazioneEidas verificaEidasMultipart(EidasMetadataToValidate metadata, File signed, File original)
            throws VerificaFirmaConnectionException, VerificaFirmaGenericInvokeException {

        RestTemplate restTemplate = buildRestTemplateWithRetry();

        String preferredUrl = restInvoker.preferredEndpoint();
        String urlEidas = preferredUrl + FIRMA_API_PATH;
        LOG.debug("post per  {}", urlEidas);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        //
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("metadata", metadata);
        body.add("signedFile", new FileSystemResource(signed));
        /*
         * Vedi EidasUtils.buildEidasMetadata: l'odine dell'array di file da passare su originalFiles sarà
         * posizionalmente la medesima passata all'oggetto EidasMetadataToValidate.setOriginalDocumentNames
         * 
         */
        if (original != null) {
            body.add("originalFiles", new FileSystemResource(original));
        }

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        EidasWSReportsDTOTree resp = null;
        try {
            resp = restTemplate.postForObject(urlEidas, entity, EidasWSReportsDTOTree.class);
        } catch (RestClientException rce) {
            throw new VerificaFirmaConnectionException(rce, CdServizioVerificaCompDoc.EIDAS.name(), urlEidas);
        } catch (Exception ex) {
            throw new VerificaFirmaGenericInvokeException(ex, CdServizioVerificaCompDoc.EIDAS.name(), urlEidas);
        }
        return new EsitoValidazioneEidas(resp);

    }

    @Override
    public VerificaFirmaWrapper verificaAndWrapIt(ComponenteVers componenteVers,
            List<ComponenteVers> sottoComponentiFirma, List<ComponenteVers> sottoComponentiMarca,
            Map<String, Boolean> controlliAbilitati, ZonedDateTime dataDiRiferimento, boolean verificaAllaDataDiFirma,
            String uuid, AbsVersamentoExt versamento)
            throws VerificaFirmaWrapperResNotFoundException, VerificaFirmaConnectionException,
            VerificaFirmaWrapperGenericException, VerificaFirmaGenericInvokeException {

        EidasMetadataToValidate metadata = EidasUtils.buildEidasMetadata(componenteVers, sottoComponentiFirma,
                controlliAbilitati, verificaAllaDataDiFirma, dataDiRiferimento, uuid);

        final boolean hasFirmeDetached = sottoComponentiFirma != null && !sottoComponentiFirma.isEmpty();
        File signed = null;
        File original = null;
        //
        if (!hasFirmeDetached) {
            signed = componenteVers.getRifFileBinario().getFileSuDisco();
        } else {
            signed = sottoComponentiFirma.get(0).getRifFileBinario().getFileSuDisco();
            original = componenteVers.getRifFileBinario().getFileSuDisco();
        }

        EsitoValidazioneEidas esito = verificaEidasMultipart(metadata, signed, original);
        EidasWrapperResultStrategy strategy = new EidasWrapperResultStrategy(verificaAllaDataDiFirma,
                controlliAbilitati, versamento);
        VerificaFirmaWrapper wrapper = strategy.buildVFWrapper(esito, dataDiRiferimento,
                CollectionUtils.isNotEmpty(sottoComponentiFirma));
        /*
         * Nota : nella prima versione dei report al fine di non appesanitre l'xml persistito era stata introdotto una
         * gestione "skinny" del report ossia, una copia dell'oggetto "ripulito" da alcune parti. Nella seconda versione
         * (vedi MEV #23229) il concetto di "report" è stato evoluto riportando su DB i singoli report eidas
         * (simple/detailed/diagnostic). La manipolazione dell'oggetto jaxb originale con la modalità skinny porta a
         * problemi in fase di validazione (marshalling) dell'oggetto, per tale motivo si utilizza la riposta originale
         * restituita dal microservizio.
         */
        wrapper.getAdditionalInfo().setReportContent(esito.getEidasWSReportsDTOTree()); // tree object

        return wrapper;
    }

    /**
     * Crea il client per le chiamate rest relativo a questo bean
     *
     * @param timeout
     *            timeout in ms
     *
     * @return restTemplate di spring configurato per "crypto"
     */
    private RestTemplate buildRestTemplateWithRetry() {

        RestTemplate template = new RestTemplate();
        int timeout = restInvoker.clientTimeout();

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setReadTimeout(timeout);
        clientHttpRequestFactory.setConnectTimeout(timeout);
        clientHttpRequestFactory.setConnectionRequestTimeout(timeout);

        template.setRequestFactory(clientHttpRequestFactory);
        template.setErrorHandler(new EidasErrorHandler());

        List<String> endpoints = restInvoker.endPoints();
        List<URI> endpointsURI = endpoints.stream().map(e -> URI.create(e)).collect(Collectors.toList());

        ParerRetryConfiguration retryClient = restInvoker.retryClient();

        template.getInterceptors().add(new RestRetryInterceptor(endpointsURI, retryClient));

        return template;
    }

}
