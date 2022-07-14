package it.eng.parer.firma.crypto.helper;

import it.eng.parer.exception.ParamApplicNotFoundException;
import it.eng.parer.retry.RestConfiguratorHelper;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stateless bean che (auto) configura il comportamento della modalità "Retry" di un metodo. Questo helper include la
 * funzionalità di retry applicato a delle chiamate a endpoint RESTful.
 *
 * @author Snidero_L
 */
@Stateless
@LocalBean
public class EidasRestConfiguratorHelper implements RestConfiguratorHelper {

    private final Logger LOG = LoggerFactory.getLogger(EidasRestConfiguratorHelper.class);

    private static final String EIDAS_CLIENT_TIMEOUT = "VERIFICA_FIRMA_TIMEOUT";

    /* EIDAS */
    private static final String EIDAS_RETRY_TIMEOUT = "EIDAS_VERIFICA_FIRMA_RETRY_TIMEOUT";

    private static final String EIDAS_MAX_TENTATIVI = "EIDAS_VERIFICA_FIRMA_MAX_TENTATIVI";

    private static final String EIDAS_CIRCUIT_BREAKER_OPEN_TIMEOUT = "EIDAS_VERIFICA_FIRMA_CIRCUIT_BREAKER_OPEN_TIMEOUT";

    private static final String EIDAS_CIRCUIT_BREAKER_RESET_TIMEOUT = "EIDAS_VERIFICA_FIRMA_CIRCUIT_BREAKER_RESET_TIMEOUT";

    private static final String EIDAS_PERIODO_BACKOFF = "EIDAS_VERIFICA_FIRMA_PERIODO_BACKOFF";

    private static final String EIDAS_ENDPOINT = "EIDAS_VERIFICA_FIRMA_ENDPOINT";

    private static final String EIDAS_COMPOSITE_POLICY_OPTIMISTIC = "EIDAS_COMPOSITE_POLICY_OPTIMISTIC";

    private static final String PARAMETRO_NON_TROVATO = "Parametro {} non trovato. Utilizzo il valore predefinito.";

    private static final String ENDPOINT_SEPARATOR = "\\|";

    @EJB
    protected ConfigurationHelper configurationHelper;

    private Long getLongParameter(final String name) {
        Long paramValue = null;
        try {
            final String longParameterString = configurationHelper.getParamApplicValue(name);
            paramValue = Long.parseLong(longParameterString);

        } catch (ParamApplicNotFoundException | NumberFormatException ignore) {
            LOG.debug(PARAMETRO_NON_TROVATO, name);

        }
        return paramValue;
    }

    private Integer getIntParameter(final String name) {
        Integer paramValue = null;
        try {
            final String intParameterString = configurationHelper.getParamApplicValue(name);
            paramValue = Integer.parseInt(intParameterString);

        } catch (ParamApplicNotFoundException | NumberFormatException ignore) {
            LOG.debug(PARAMETRO_NON_TROVATO, name);

        }
        return paramValue;
    }

    private Boolean getBooleanParameter(final String name) {
        Boolean paramValue = true;
        try {
            final String boolParameterString = configurationHelper.getParamApplicValue(name);
            paramValue = Boolean.parseBoolean(boolParameterString);

        } catch (ParamApplicNotFoundException ignore) {
            LOG.debug(PARAMETRO_NON_TROVATO, name);

        }
        return paramValue;
    }

    @Override
    public Long getRetryTimeoutParam() {
        return getLongParameter(EIDAS_RETRY_TIMEOUT);
    }

    @Override
    public Integer getMaxRetryParam() {
        return getIntParameter(EIDAS_MAX_TENTATIVI);
    }

    @Override
    public Long getCircuitBreakerOpenTimeoutParam() {
        return getLongParameter(EIDAS_CIRCUIT_BREAKER_OPEN_TIMEOUT);
    }

    @Override
    public Long getCircuitBreakerResetTimeoutParam() {
        return getLongParameter(EIDAS_CIRCUIT_BREAKER_RESET_TIMEOUT);
    }

    @Override
    public Long getPeriodoBackOffParam() {
        return getLongParameter(EIDAS_PERIODO_BACKOFF);
    }

    @Override
    public Long getClientTimeoutInMinutesParam() {
        return getLongParameter(EIDAS_CLIENT_TIMEOUT);

    }

    @Override
    public Boolean isCompositePolicyOptimisticParam() {
        return getBooleanParameter(EIDAS_COMPOSITE_POLICY_OPTIMISTIC);
    }

    /**
     * Lista degli endpoint per i servizi REST. Tendenzialmente questa verrà trattata come una lista circolare.
     *
     * @return lista di endpoint
     */
    @Override
    public List<String> endPoints() {
        final String endPointsString = configurationHelper.getParamApplicValue(EIDAS_ENDPOINT);
        return Pattern.compile(ENDPOINT_SEPARATOR).splitAsStream(endPointsString).map(String::trim)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public String preferredEndpoint() {
        return endPoints().get(0);
    }

}
