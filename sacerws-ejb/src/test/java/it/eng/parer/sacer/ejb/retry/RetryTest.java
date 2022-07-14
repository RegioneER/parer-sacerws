package it.eng.parer.sacer.ejb.retry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import it.eng.parer.crypto.model.verifica.CryptoAroCompDoc;
import it.eng.parer.crypto.model.verifica.input.CryptoDataToValidateMetadata;
import it.eng.parer.crypto.model.verifica.input.CryptoDataToValidateMetadataFile;
import it.eng.parer.crypto.model.verifica.input.TipologiaDataRiferimento;
import it.eng.parer.firma.util.CryptoErrorHandler;
import it.eng.parer.firma.util.EidasErrorHandler;
import it.eng.parer.retry.ParerRetryConfiguration;
import it.eng.parer.retry.ParerRetryConfigurationBuilder;
import it.eng.parer.retry.RestRetryInterceptor;

/**
 *
 * @author Snidero_L
 */
public class RetryTest {

    private static final int TIMEOUT = 120000;

    private RestTemplate restTemplateCrypto;

    private RestTemplate restTemplateEidas;

    private String preferredEndpointCrypto;

    private String preferredEndpointEidas;

    private Properties configProps;

    public RetryTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws IOException {
        restTemplateCrypto = new RestTemplate();
        restTemplateEidas = new RestTemplate();

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setReadTimeout(TIMEOUT);
        clientHttpRequestFactory.setConnectTimeout(TIMEOUT);
        clientHttpRequestFactory.setConnectionRequestTimeout(TIMEOUT);

        // get properties
        configProps = new Properties();
        InputStream iStream = new ClassPathResource("retrytest.properties").getInputStream();
        configProps.load(iStream);

        restTemplateCrypto.setRequestFactory(clientHttpRequestFactory);
        restTemplateCrypto.setErrorHandler(new CryptoErrorHandler());

        restTemplateEidas.setRequestFactory(clientHttpRequestFactory);
        restTemplateEidas.setErrorHandler(new EidasErrorHandler());

        preferredEndpointCrypto = configProps.getProperty("crypto.pref.url");
        preferredEndpointEidas = configProps.getProperty("eidas.pref.url");

        List<URI> endpointsCrypto = new ArrayList<>();
        endpointsCrypto.add(URI.create(preferredEndpointCrypto));
        // iterate endpoint
        final AtomicInteger cryptoai = new AtomicInteger(1);
        configProps.keySet().stream().filter(url -> url.toString().startsWith("crypto.url")).forEach(url -> {
            endpointsCrypto
                    .add(URI.create(configProps.getProperty("crypto.url.".concat(String.valueOf(cryptoai.get())))));
            cryptoai.getAndIncrement();
        });

        List<URI> endpointsEidas = new ArrayList<>();
        endpointsEidas.add(URI.create(preferredEndpointEidas));
        // iterate endpoint
        final AtomicInteger eidasai = new AtomicInteger(1);
        configProps.keySet().stream().filter(url -> url.toString().startsWith("eidas.url")).forEach(url -> {
            endpointsEidas.add(URI.create(configProps.getProperty("eidas.url.".concat(String.valueOf(eidasai.get())))));
            eidasai.getAndIncrement();
        });
        configureInterceptor(restTemplateCrypto, endpointsCrypto);
        configureInterceptor(restTemplateEidas, endpointsEidas);

    }

    private void configureInterceptor(RestTemplate restTemplate, List<URI> endPoints) {
        ParerRetryConfigurationBuilder buildConfiguration = ParerRetryConfiguration.builder();
        buildConfiguration.withMaxAttemps(Integer.valueOf((String) configProps.get("retry.maxattempts")));
        ParerRetryConfiguration retryConfiguration = buildConfiguration.build();
        restTemplate.getInterceptors().removeIf(i -> true);
        restTemplate.getInterceptors().add(new RestRetryInterceptor(endPoints, retryConfiguration));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testVerificaCrypto() throws FileNotFoundException {

        File fileFirmato = ResourceUtils.getFile("classpath:firme/xml_sig_controfirma_cert_rev.xml");

        FileSystemResource fileFirmatoRes = new FileSystemResource(fileFirmato);
        CryptoDataToValidateMetadata metadata = new CryptoDataToValidateMetadata();
        metadata.setComponentePrincipale(new CryptoDataToValidateMetadataFile("componente-principale"));

        // input.setContenuto(new CryptoDocumentoVersato("XML_1", Resources.toByteArray(fileFirmato.getURL())));
        Date rifVersato = getDate(8, Calendar.SEPTEMBER, 2013);
        metadata.setTipologiaDataRiferimento(TipologiaDataRiferimento.verificaAllaDataSpecifica(rifVersato.getTime()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("metadati", metadata);
        body.add("contenuto", fileFirmatoRes);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        CryptoAroCompDoc componente = restTemplateCrypto.postForObject("/v2/report-verifica", entity,
                CryptoAroCompDoc.class);

        Assert.assertNotNull(componente);
    }

    /**
     * Chiamata ad una lista di endpoint non validi. Prima dell'ultima patch sul meccanismo di retry si attendeva una
     * <tt>RestClientException</tt>. Con la gestione applicativa del codice 404 qui abbiamo un comportamento
     * "interessante":
     * <ul>
     * <li>la chiamata a <tt>/v0/report-verifica</tt> termina con una RestClientException</li>
     * <li>la chiamata a
     * <tt>https://verificafirma-crypto-parer-svil.parer-apps.ente.regione.emr.it//v0/report-verifica</tt> nonostante
     * l'endpoint NON esista termina con un CryptoAroCompDoc non nullo</li>
     * </ul>
     *
     * Il motivo di questo comportamento sta nel fatto che la seconda POST atterra su una risorsa <em>che risponde</em>
     * un codice 404.
     *
     * Per questa ragione la valutazione finale viene effettuata non sulla nullità dell'oggetto ma sul fatto che
     * l'oggetto sia vuoto.
     *
     * @throws FileNotFoundException
     */
    @Test
    public void testVerificaCryptoEnpointNonValido() throws FileNotFoundException {

        File fileFirmato = ResourceUtils.getFile("classpath:firme/xml_sig_controfirma_cert_rev.xml");

        FileSystemResource fileFirmatoRes = new FileSystemResource(fileFirmato);
        CryptoDataToValidateMetadata metadata = new CryptoDataToValidateMetadata();

        // input.setContenuto(new CryptoDocumentoVersato("XML_1", Resources.toByteArray(fileFirmato.getURL())));
        Date rifVersato = getDate(8, Calendar.SEPTEMBER, 2013);
        metadata.setTipologiaDataRiferimento(TipologiaDataRiferimento.verificaAllaDataSpecifica(rifVersato.getTime()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("metadati", metadata);
        body.add("contenuto", fileFirmatoRes);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        List<URI> badEndPoints = Arrays.asList(URI.create(preferredEndpointCrypto));
        configureInterceptor(restTemplateCrypto, badEndPoints);

        CryptoAroCompDoc componente = restTemplateCrypto.postForObject("/v0/report-verifica", entity,
                CryptoAroCompDoc.class);

        Assert.assertNull(componente.getIdComponente());
    }

    /**
     *
     * @throws FileNotFoundException
     */
    @Test(expected = RestClientException.class)
    public void testVerificaCryptoEnpointInesistente() throws FileNotFoundException {

        File fileFirmato = ResourceUtils.getFile("classpath:firme/xml_sig_controfirma_cert_rev.xml");

        FileSystemResource fileFirmatoRes = new FileSystemResource(fileFirmato);
        CryptoDataToValidateMetadata metadata = new CryptoDataToValidateMetadata();

        // input.setContenuto(new CryptoDocumentoVersato("XML_1", Resources.toByteArray(fileFirmato.getURL())));
        Date rifVersato = getDate(8, Calendar.SEPTEMBER, 2013);
        metadata.setTipologiaDataRiferimento(TipologiaDataRiferimento.verificaAllaDataSpecifica(rifVersato.getTime()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("metadati", metadata);
        body.add("contenuto", fileFirmatoRes);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        List<URI> badEndPoints = Arrays.asList(URI.create("smb://non_esiste/"));

        configureInterceptor(restTemplateCrypto, badEndPoints);

        CryptoAroCompDoc componente = restTemplateCrypto.postForObject("/v2/report-verifica", entity,
                CryptoAroCompDoc.class);

        Assert.assertNull(componente);
    }

    private static Date getDate(int giorno, int mese, int anno) {
        Calendar cal = Calendar.getInstance();
        cal.set(anno, mese, giorno);
        return cal.getTime();

    }

    @Test
    public void testVerificaCryptoConTimestamp() throws FileNotFoundException {

        File fileFirmato = ResourceUtils.getFile("classpath:firme/cades_T_1.pdf.p7m");

        FileSystemResource fileFirmatoRes = new FileSystemResource(fileFirmato);

        File marcaDetached = ResourceUtils.getFile("classpath:firme/cades_T_1.pdf.p7m.tsr");

        FileSystemResource marcaDetachedRes = new FileSystemResource(marcaDetached);

        CryptoDataToValidateMetadata metadata = new CryptoDataToValidateMetadata();
        metadata.setTipologiaDataRiferimento(TipologiaDataRiferimento.verificaAllaDataDiFirma());
        metadata.setComponentePrincipale(new CryptoDataToValidateMetadataFile("cades_t1.p7m"));
        metadata.setSottoComponentiMarca(Arrays.asList(
                new CryptoDataToValidateMetadataFile[] { new CryptoDataToValidateMetadataFile("cades_t1.tsr") }));
        metadata.setUuid("testVerificaFirmaV2TimestampDetached");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("metadati", metadata);
        body.add("contenuto", fileFirmatoRes);
        body.add("marche", marcaDetachedRes);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        String url = preferredEndpointCrypto + "/v2/report-verifica";
        // CryptoAroCompDoc componente = restTemplateMultipartDetached.postForObject("/v2/report-verifica", entity,
        // CryptoAroCompDoc.class);
        CryptoAroCompDoc componente = restTemplateCrypto.postForObject(url, entity, CryptoAroCompDoc.class);

        Assert.assertNotNull(componente);

        /*
         * Se non passo i metadati il valore predefinito è il seguente: - contenuto per il file principale - firma_0 ...
         * firma_n-1 per le firme detached - marca_0 ... marca_n-1 per le marche detached
         */
        Assert.assertEquals("cades_t1.p7m", componente.getAroMarcaComps().get(1).getIdMarca());
        Assert.assertEquals("cades_t1.tsr", componente.getAroMarcaComps().get(0).getIdMarca());

    }

}
