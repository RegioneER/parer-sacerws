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

package it.eng.parer.firma.ejb;

import java.io.File;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

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

import it.eng.parer.crypto.model.verifica.CryptoAroCompDoc;
import it.eng.parer.crypto.model.verifica.input.CryptoDataToValidateBody;
import it.eng.parer.crypto.model.verifica.input.CryptoDataToValidateDataUri;
import it.eng.parer.crypto.model.verifica.input.CryptoDataToValidateMetadata;
import it.eng.parer.crypto.model.verifica.input.CryptoDataToValidateMetadataFile;
import it.eng.parer.crypto.model.verifica.input.CryptoProfiloVerifica;
import it.eng.parer.crypto.model.verifica.input.TipologiaDataRiferimento;
import it.eng.parer.entity.constraint.DecServizioVerificaCompDoc.CdServizioVerificaCompDoc;
import it.eng.parer.firma.exception.VerificaFirmaConnectionException;
import it.eng.parer.firma.exception.VerificaFirmaGenericInvokeException;
import it.eng.parer.firma.exception.VerificaFirmaWrapperGenericException;
import it.eng.parer.firma.exception.VerificaFirmaWrapperResNotFoundException;
import it.eng.parer.firma.helper.CryptoRestConfiguratorHelper;
import it.eng.parer.firma.strategy.CryptoWrapperResultStrategy;
import it.eng.parer.firma.util.CryptoErrorHandler;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.retry.ParerRetryConfiguration;
import it.eng.parer.retry.RestRetryInterceptor;
import it.eng.parer.ws.utils.ParametroApplDB.ParametroApplFl;
import it.eng.parer.ws.versamento.dto.AbsVersamentoExt;
import it.eng.parer.ws.versamento.dto.ComponenteVers;

/**
 *
 * @author Quaranta_M, sinatti_s
 */
@Stateless
@LocalBean
public class CryptoInvoker implements IVerificaFirmaInvoker {

    private static final Logger LOG = LoggerFactory.getLogger(CryptoInvoker.class.getName());

    private static final String FIRMA_API_PATH = "/api/report-verifica";

    @EJB
    protected CryptoRestConfiguratorHelper restInvoker;

    /**
     * Si collega all'endpoint contenente l'health check per il servizio.
     *
     * @param url completo dell'endpoint
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
     * @param contenuto      documento principale
     * @param marcheDetached marche detached (o null)
     * @param firmeDetached  firme detachet (o null)
     * @param metadata       metadati indicanti gli identificativi dei componenti da verificare e la
     *                       configurazione della modalità di verifica
     *
     * @return Report dei componenti verificati
     *
     * @throws VerificaFirmaConnectionException    eccezione in caso di mancata risposta
     * @throws VerificaFirmaGenericInvokeException eccezione in caso di risposta errata
     */
    private CryptoAroCompDoc verificaCrypto(File contenuto, List<File> marcheDetached,
            List<File> firmeDetached, CryptoDataToValidateMetadata metadata)
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
            throw new VerificaFirmaConnectionException(rce, CdServizioVerificaCompDoc.CRYPTO.name(),
                    urlCrypto);
        } catch (Exception ex) {
            throw new VerificaFirmaGenericInvokeException(ex,
                    CdServizioVerificaCompDoc.CRYPTO.name(), urlCrypto);
        }
        return componente;
    }

    private CryptoAroCompDoc verificaCrypto(CryptoDataToValidateDataUri data,
            CryptoDataToValidateMetadata metadata)
            throws VerificaFirmaConnectionException, VerificaFirmaGenericInvokeException {

        RestTemplate restTemplate = buildRestTemplateWithRetry();

        String preferredUrl = restInvoker.preferredEndpoint();
        String urlCrypto = preferredUrl + FIRMA_API_PATH;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        CryptoDataToValidateBody body = new CryptoDataToValidateBody();
        body.setData(data);
        body.setMetadata(metadata);

        HttpEntity<CryptoDataToValidateBody> entity = new HttpEntity<>(body, headers);
        CryptoAroCompDoc componente = null;
        try {
            componente = restTemplate.postForObject(urlCrypto, entity, CryptoAroCompDoc.class);
        } catch (RestClientException rce) {
            throw new VerificaFirmaConnectionException(rce, CdServizioVerificaCompDoc.CRYPTO.name(),
                    urlCrypto);
        } catch (Exception ex) {
            throw new VerificaFirmaGenericInvokeException(ex,
                    CdServizioVerificaCompDoc.CRYPTO.name(), urlCrypto);
        }
        return componente;
    }

    /**
     * Verifica delle firme utilizzando la <em>cryptolibrary</em>.
     *
     * @param componenteVers          file firmato oppure file originale in caso di contenuto
     *                                detached
     * @param sottoComponentiFirma    firme detached
     * @param sottoComponentiMarca    marche detached
     * @param dataDiRiferimento       data a cui effettuare la verifica delle firme
     * @param controlliAbilitati      mappa di alcuni controlli che la verifica può non effettuare
     * @param verificaAllaDataDiFirma true o false
     * @param uuid                    identificativo univoco del processo di verifica
     *
     * @return modello comune a tutte le modalità di verifica delle firme.
     *
     * @throws VerificaFirmaConnectionException     in caso di errori di connessione con l'endpoint
     * @throws VerificaFirmaWrapperGenericException errore generico su wrapper
     * @throws VerificaFirmaGenericInvokeException  eccezione in caso di risposta errata
     */
    @Override
    public VerificaFirmaWrapper verificaAndWrapIt(ComponenteVers componenteVers,
            List<ComponenteVers> sottoComponentiFirma, List<ComponenteVers> sottoComponentiMarca,
            Map<String, Boolean> controlliAbilitati, ZonedDateTime dataDiRiferimento,
            boolean verificaAllaDataDiFirma, String uuid, AbsVersamentoExt versamento)
            throws VerificaFirmaWrapperResNotFoundException, VerificaFirmaConnectionException,
            VerificaFirmaWrapperGenericException, VerificaFirmaGenericInvokeException {

        // Preparo i metadati
        CryptoDataToValidateMetadata metadata = buildMetadata(componenteVers, sottoComponentiFirma,
                sottoComponentiMarca, controlliAbilitati, dataDiRiferimento,
                verificaAllaDataDiFirma, uuid);

        final boolean hasFirmeDetached = sottoComponentiFirma != null
                && !sottoComponentiFirma.isEmpty();
        CryptoAroCompDoc output;
        if (isComponenteSuObjectStorage(componenteVers)
                && !restInvoker.isEnableMultipartRequest().booleanValue()) {
            LOG.debug("Invocazione verifica firma CRYPTO (application/json)");

            CryptoDataToValidateDataUri data = buildDataUri(componenteVers, sottoComponentiFirma,
                    sottoComponentiMarca);
            output = verificaCrypto(data, metadata);

        } else {
            LOG.debug("Invocazione verifica firma CRYPTO (multipart/form-data)");

            // File firme detached (o null)
            List<File> sottoComponentiMarcaFile = compilaFileDetached(sottoComponentiMarca);
            // File marche detached (o null)
            List<File> sottoComponentiFirmaFile = compilaFileDetached(sottoComponentiFirma);

            output = verificaCrypto(componenteVers.getRifFileBinario().getFileSuDisco(),
                    sottoComponentiMarcaFile, sottoComponentiFirmaFile, metadata);

        }

        CryptoWrapperResultStrategy strategy = new CryptoWrapperResultStrategy();
        return strategy.buildVFWrapper(output, hasFirmeDetached);
    }

    /**
     * <strong>ATTENZIONE</strong> a questo livello ho bisogno di capire se il componente di cui
     * devo effettaure la verifica sia su file system oppure sull'object storage. Questo metodo ha
     * proprio lo scopo di individuare questo caso ma, per ora, non ho trovato un modo migliore per
     * capire dove sia l'oggetto. Non è scorretto ma forse non è molto robusto perché valuta
     * solamente il componente principale.
     *
     *
     * @param componenteVers componente versato
     *
     * @return true se il riferimento al file del componente è sull'object storage.
     */
    private boolean isComponenteSuObjectStorage(ComponenteVers componenteVers) {
        return componenteVers.getRifFileBinario().getObjectStorageResource() != null;
    }

    private CryptoDataToValidateMetadata buildMetadata(ComponenteVers componenteVers,
            List<ComponenteVers> sottoComponentiFirma, List<ComponenteVers> sottoComponentiMarca,
            Map<String, Boolean> controlliAbilitati, ZonedDateTime dataDiRiferimento,
            boolean verificaAllaDataDiFirma, String uuid) {
        // Configurazione profilo di verificaCrypto custom per la struttura.
        CryptoProfiloVerifica profiloVerifica = new CryptoProfiloVerifica()
                .setControlloCrittograficoAbilitato(
                        controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_CRITTOG_VERS))
                .setControlloCatenaTrustAbilitato(
                        controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_TRUST_VERS))
                .setControlloCertificatoAbilitato(
                        controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_CERTIF_VERS))
                .setControlloCrlAbilitato(
                        controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS))
                .setIncludeCertificateAndRevocationValues(
                        controlliAbilitati.get(ParametroApplFl.FL_CRYPTO_INCLUDI_FILEBASE64));

        CryptoDataToValidateMetadata metadata = new CryptoDataToValidateMetadata();
        metadata.setUuid(uuid);
        metadata.setProfiloVerifica(profiloVerifica);

        if (verificaAllaDataDiFirma) {
            metadata.setTipologiaDataRiferimento(
                    TipologiaDataRiferimento.verificaAllaDataDiFirma());
        } else {
            boolean isDataDiRiferimentoOnCompVers = !Objects.isNull(
                    componenteVers.withAcdEntity().getTmRifTempVers());/* data di rif. su xml */
            if (isDataDiRiferimentoOnCompVers) {
                metadata.setTipologiaDataRiferimento(TipologiaDataRiferimento
                        .verificaAllaDataSpecifica(dataDiRiferimento.toInstant().toEpochMilli()));
            } else {
                metadata.setTipologiaDataRiferimento(TipologiaDataRiferimento
                        .verificaDataVersamento(dataDiRiferimento.toInstant().toEpochMilli()));
            }
        }
        // Metadati componente principale
        metadata.setComponentePrincipale(
                new CryptoDataToValidateMetadataFile(componenteVers.getId()));
        // Metadati marche detached
        List<CryptoDataToValidateMetadataFile> metadatiMarcheDetached = compilaMetadatiElementiDetached(
                sottoComponentiMarca);
        metadata.setSottoComponentiMarca(metadatiMarcheDetached);
        // Metadati firme detached
        List<CryptoDataToValidateMetadataFile> metadatiFirmeDetached = compilaMetadatiElementiDetached(
                sottoComponentiFirma);
        metadata.setSottoComponentiFirma(metadatiFirmeDetached);
        return metadata;
    }

    private CryptoDataToValidateDataUri buildDataUri(ComponenteVers componenteVers,
            List<ComponenteVers> sottoComponentiFirma, List<ComponenteVers> sottoComponentiMarca) {
        CryptoDataToValidateDataUri data = new CryptoDataToValidateDataUri();

        data.setContenuto(
                componenteVers.getRifFileBinario().getObjectStorageResource().getPresignedURL());
        List<URI> marche = compilaURIDetached(sottoComponentiMarca);
        List<URI> firme = compilaURIDetached(sottoComponentiFirma);
        data.setMarche(marche);
        data.setFirme(firme);

        return data;
    }

    private List<CryptoDataToValidateMetadataFile> compilaMetadatiElementiDetached(
            List<ComponenteVers> componenteDetached) {
        List<CryptoDataToValidateMetadataFile> componenteMetadata = null;
        if (componenteDetached != null) {
            componenteMetadata = componenteDetached.stream()
                    .map(m -> new CryptoDataToValidateMetadataFile(m.getId()))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return componenteMetadata;
    }

    private List<File> compilaFileDetached(List<ComponenteVers> componenteDetached) {
        List<File> elementiDetached = null;
        if (componenteDetached != null) {
            elementiDetached = componenteDetached.stream()
                    .map(m -> m.getRifFileBinario().getFileSuDisco())
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return elementiDetached;
    }

    private List<URI> compilaURIDetached(List<ComponenteVers> componenteDetached) {
        List<URI> elementiDetached = null;
        if (componenteDetached != null) {
            elementiDetached = componenteDetached.stream()
                    .map(m -> m.getRifFileBinario().getObjectStorageResource().getPresignedURL())
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return elementiDetached;
    }

    /**
     * Crea il client per le chiamate rest relativo a questo bean
     *
     * @param timeout timeout in ms
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
        List<URI> endpointsURI = endpoints.stream().map(URI::create).collect(Collectors.toList());

        ParerRetryConfiguration retryClient = restInvoker.retryClient();

        template.getInterceptors().add(new RestRetryInterceptor(endpointsURI, retryClient));

        return template;
    }

}
