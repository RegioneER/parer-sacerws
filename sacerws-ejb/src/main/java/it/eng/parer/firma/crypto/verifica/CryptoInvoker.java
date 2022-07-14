package it.eng.parer.firma.crypto.verifica;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import it.eng.parer.crypto.model.verifica.CryptoAroCompDoc;
import it.eng.parer.crypto.model.verifica.input.CryptoDataToValidateMetadata;
import it.eng.parer.crypto.model.verifica.input.CryptoDataToValidateMetadataFile;
import it.eng.parer.crypto.model.verifica.input.CryptoProfiloVerifica;
import it.eng.parer.crypto.model.verifica.input.TipologiaDataRiferimento;
import it.eng.parer.entity.constraint.DecServizioVerificaCompDoc.CdServizioVerificaCompDoc;
import it.eng.parer.firma.crypto.helper.CryptoRestConfiguratorHelper;
import it.eng.parer.firma.ejb.IVerificaFirmaInvoker;
import it.eng.parer.firma.exception.VerificaFirmaConnectionException;
import it.eng.parer.firma.exception.VerificaFirmaGenericInvokeException;
import it.eng.parer.firma.exception.VerificaFirmaWrapperGenericException;
import it.eng.parer.firma.exception.VerificaFirmaWrapperResNotFoundException;
import it.eng.parer.firma.strategy.CryptoWrapperResultStrategy;
import it.eng.parer.firma.util.CryptoErrorHandler;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.retry.ParerRetryConfiguration;
import it.eng.parer.retry.RestRetryInterceptor;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.utils.ParametroApplDB.ParametroApplFl;
import it.eng.parer.ws.versamento.dto.AbsVersamentoExt;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

/**
 *
 * @author Quaranta_M, sinatti_s
 */
@Stateless
@LocalBean
public class CryptoInvoker implements IVerificaFirmaInvoker {

    private static final Logger LOG = LoggerFactory.getLogger(CryptoInvoker.class.getName());

    @EJB
    protected ConfigurationHelper configurationHelper;

    private static final String FIRMA_API_PATH = "/v2/report-verifica";

    @EJB
    protected CryptoRestConfiguratorHelper restInvoker;

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
            LOG.warn("Impossibile contattare {}", url, ex);
        }
        return false;
    }

    /**
     * Chiamata al ws REST di verifica disponibile agli endpoint configurati (V2).
     *
     * @param contenuto
     *            documento principale
     * @param marcheDetached
     *            marche detached (o null)
     * @param firmeDetached
     *            firme detachet (o null)
     * @param metadata
     *            metadati indicanti gli identificativi dei componenti da verificare e la configurazione della modalità
     *            di verifica
     *
     * @return Report dei componenti verificati
     *
     * @throws VerificaFirmaConnectionException
     *             eccezione in caso di mancata risposta
     * @throws VerificaFirmaGenericInvokeException
     *             eccezione in caso di risposta errata
     */
    private CryptoAroCompDoc verificaCrypto(File contenuto, List<File> marcheDetached, List<File> firmeDetached,
            CryptoDataToValidateMetadata metadata)
            throws VerificaFirmaConnectionException, VerificaFirmaGenericInvokeException {

        RestTemplate restTemplate = buildRestTemplateWithRetry();

        String preferredUrl = restInvoker.preferredEndpoint();
        String urlCrypto = preferredUrl + FIRMA_API_PATH;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        FileSystemResource fileContenuto = new FileSystemResource(contenuto);
        body.add("contenuto", fileContenuto);

        if (marcheDetached != null) {
            for (File marca : marcheDetached) {
                body.add("marche", new FileSystemResource(marca));
            }
        }
        if (firmeDetached != null) {
            for (File firma : firmeDetached) {
                body.add("firme", new FileSystemResource(firma));
            }
        }
        if (metadata != null) {
            body.add("metadati", metadata);
        }

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        CryptoAroCompDoc componente = null;
        try {
            componente = restTemplate.postForObject(urlCrypto, entity, CryptoAroCompDoc.class);
        } catch (RestClientException rce) {
            throw new VerificaFirmaConnectionException(rce, CdServizioVerificaCompDoc.CRYPTO.name(), urlCrypto);
        } catch (Exception ex) {
            throw new VerificaFirmaGenericInvokeException(ex, CdServizioVerificaCompDoc.CRYPTO.name(), urlCrypto);
        }
        return componente;
    }

    /**
     * Verifica delle firme utilizzando la <em>cryptolibrary</em>.
     *
     * @param componenteVers
     *            file firmato oppure file originale in caso di contenuto detached
     * @param sottoComponentiFirma
     *            firme detached
     * @param sottoComponentiMarca
     *            marche detached
     * @param dataDiRiferimento
     *            data a cui effettuare la verifica delle firme
     * @param controlliAbilitati
     *            mappa di alcuni controlli che la verifica può non effettuare
     * @param verificaAllaDataDiFirma
     *            true o false
     * @param uuid
     *            identificativo univoco del processo di verifica
     *
     * @return modello comune a tutte le modalità di verifica delle firme.
     *
     * @throws VerificaFirmaConnectionException
     *             in caso di errori di connessione con l'endpoint
     * @throws VerificaFirmaWrapperGenericException
     *             errore generico su wrapper
     * @throws VerificaFirmaGenericInvokeException
     *             eccezione in caso di risposta errata
     */
    @Override
    public VerificaFirmaWrapper verificaAndWrapIt(ComponenteVers componenteVers,
            List<ComponenteVers> sottoComponentiFirma, List<ComponenteVers> sottoComponentiMarca,
            Map<String, Boolean> controlliAbilitati, ZonedDateTime dataDiRiferimento, boolean verificaAllaDataDiFirma,
            String uuid, AbsVersamentoExt versamento)
            throws VerificaFirmaWrapperResNotFoundException, VerificaFirmaConnectionException,
            VerificaFirmaWrapperGenericException, VerificaFirmaGenericInvokeException {

        // Verifico il componente
        // Configurazione profilo di verificaCrypto custom per la struttura.
        CryptoProfiloVerifica profiloVerifica = new CryptoProfiloVerifica()
                .setControlloCrittograficoAbilitato(
                        controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_CRITTOG_VERS))
                .setControlloCatenaTrustAbilitato(controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_TRUST_VERS))
                .setControlloCertificatoAbilitato(controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_CERTIF_VERS))
                .setControlloCrlAbilitato(controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS));

        CryptoDataToValidateMetadata metadata = new CryptoDataToValidateMetadata();
        metadata.setUuid(uuid);
        metadata.setProfiloVerifica(profiloVerifica);

        if (verificaAllaDataDiFirma) {
            metadata.setTipologiaDataRiferimento(TipologiaDataRiferimento.verificaAllaDataDiFirma());
        } else {
            if (dataDiRiferimento != null) {
                metadata.setTipologiaDataRiferimento(TipologiaDataRiferimento
                        .verificaAllaDataSpecifica(dataDiRiferimento.toInstant().toEpochMilli()));
            }
        }
        // Metadati componente principale
        metadata.setComponentePrincipale(new CryptoDataToValidateMetadataFile(componenteVers.getId()));
        // Metadati marche detached
        List<CryptoDataToValidateMetadataFile> metadatiMarcheDetached = compilaMetadatiElementiDetached(
                sottoComponentiMarca);
        metadata.setSottoComponentiMarca(metadatiMarcheDetached);
        // Metadati firme detached
        List<CryptoDataToValidateMetadataFile> metadatiFirmeDetached = compilaMetadatiElementiDetached(
                sottoComponentiFirma);
        metadata.setSottoComponentiFirma(metadatiFirmeDetached);

        // File firme detached (o null)
        List<File> sottoComponentiMarcaFile = compilaFileDetached(sottoComponentiMarca);
        // File marche detached (o null)
        List<File> sottoComponentiFirmaFile = compilaFileDetached(sottoComponentiFirma);

        final boolean hasFirmeDetached = sottoComponentiFirma != null && !sottoComponentiFirma.isEmpty();

        CryptoAroCompDoc output = verificaCrypto(componenteVers.getRifFileBinario().getFileSuDisco(),
                sottoComponentiMarcaFile, sottoComponentiFirmaFile, metadata);

        CryptoWrapperResultStrategy strategy = new CryptoWrapperResultStrategy(versamento);
        VerificaFirmaWrapper wrapper = strategy.buildVFWrapper(output, dataDiRiferimento, hasFirmeDetached);

        return wrapper;
    }

    private List<CryptoDataToValidateMetadataFile> compilaMetadatiElementiDetached(
            List<ComponenteVers> componenteDetached) {
        List<CryptoDataToValidateMetadataFile> componenteMetadata = null;
        if (componenteDetached != null) {
            componenteMetadata = componenteDetached.stream().map(m -> new CryptoDataToValidateMetadataFile(m.getId()))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return componenteMetadata;
    }

    private List<File> compilaFileDetached(List<ComponenteVers> componenteDetached) {
        List<File> elementiDetached = null;
        if (componenteDetached != null) {
            elementiDetached = componenteDetached.stream().map(m -> m.getRifFileBinario().getFileSuDisco())
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return elementiDetached;
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
        template.setErrorHandler(new CryptoErrorHandler());

        List<String> endpoints = restInvoker.endPoints();
        List<URI> endpointsURI = endpoints.stream().map(e -> URI.create(e)).collect(Collectors.toList());

        ParerRetryConfiguration retryClient = restInvoker.retryClient();

        template.getInterceptors().add(new RestRetryInterceptor(endpointsURI, retryClient));

        return template;
    }

}
