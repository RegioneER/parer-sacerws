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

package it.eng.parer.util.ejb.help;

import it.eng.parer.entity.AplParamApplic;
import it.eng.parer.entity.constraint.AplValoreParamApplic.TiAppart;
import it.eng.parer.exception.ParamApplicNotFoundException;
import it.eng.parer.util.ejb.help.dto.AplVGetValParamDto;
import it.eng.parer.ws.utils.FlagConverter;
import it.eng.parer.ws.utils.ParametroApplDB;
import it.eng.parer.ws.utils.ParametroApplDB.TipoAplVGetValAppart;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Stateless
@LocalBean
public class ConfigurationHelper {

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @EJB
    ConfigurationHelper me;

    /**
     * Default constructor.
     */
    private static final Logger log = LoggerFactory.getLogger(ConfigurationHelper.class.getName());

    public Map<String, String> getConfiguration() {
	String queryStr = "SELECT config FROM AplParamApplic config";
	// CREO LA QUERY ATTRAVERSO L'ENTITY MANAGER
	Query query = entityManager.createQuery(queryStr);
	List<AplParamApplic> configurazioni = query.getResultList();
	Map<String, String> config = new HashMap<>();
	for (AplParamApplic configurazione : configurazioni) {
	    config.put(configurazione.getNmParamApplic(),
		    me.getValoreParamApplicByApplic(configurazione.getNmParamApplic()));
	}
	return config;
    }

    /**
     * Ottieni il valore del parametro indicato dal codice in input. Il valore viene ottenuto
     * filtrando per tipologia <em>APPLIC</em> {@link TipoAplVGetValAppart#APPLIC}
     *
     * @param nmParamApplic codice del parametro
     *
     * @return valore del parametro filtrato per tipologia <em>APPLIC</em> .
     */
    public String getValoreParamApplicByApplic(String nmParamApplic) {
	return getParamApplicValue(nmParamApplic, Integer.MIN_VALUE, Integer.MIN_VALUE,
		Integer.MIN_VALUE, Integer.MIN_VALUE, TipoAplVGetValAppart.APPLIC);
    }

    /**
     * Ottieni il valore del parametro indicato dal codice in input. Il valore viene ottenuto
     * filtrando per tipologia <em>STRUT</em> {@link TipoAplVGetValAppart#STRUT}
     *
     * @param nmParamApplic codice del parametro
     * @param idAmbiente    id ambiente
     * @param idStrut       id struttura
     *
     *
     * @return valore del parametro filtrato per tipologia <em>STRUT</em> .
     */
    public String getValoreParamApplicByStrut(String nmParamApplic, long idStrut, long idAmbiente) {
	return getParamApplicValue(nmParamApplic, idStrut, idAmbiente, Integer.MIN_VALUE,
		Integer.MIN_VALUE, TipoAplVGetValAppart.STRUT);
    }

    /**
     * Ottieni il valore del parametro indicato dal codice in input. Il valore viene ottenuto
     * filtrando per tipologia <em>TIPOUNITADOC</em> {@link TipoAplVGetValAppart#TIPOUNITADOC}
     *
     * @param nmParamApplic  codice del parametro
     * @param idAmbiente     id ambiente
     * @param idStrut        id struttura
     * @param idTipoUnitaDoc id tipologia unità documentaria
     *
     * @return valore del parametro filtrato per tipologia <em>TIPOUNITADOC</em> .
     */
    public String getValoreParamApplicByTipoUd(String nmParamApplic, long idStrut, long idAmbiente,
	    long idTipoUnitaDoc) {
	return getParamApplicValue(nmParamApplic, idStrut, idAmbiente, idTipoUnitaDoc,
		Integer.MIN_VALUE, TipoAplVGetValAppart.TIPOUNITADOC);
    }

    /**
     * Ottieni il valore del parametro indicato dal codice in input. Il valore viene ottenuto
     * filtrando per tipologia <em>AATIPOFASCICOLO</em> {@link TipoAplVGetValAppart#AATIPOFASCICOLO}
     *
     * @param nmParamApplic     codice del parametro
     * @param idAmbiente        id ambiente
     * @param idStrut           id struttura
     * @param idAaTipoFascicolo id tipologia anno fascicolo
     *
     * @return valore del parametro filtrato per tipologia <em>AATIPOFASCICOLO</em> .
     */
    public String getValoreParamApplicByAaTipoFasc(String nmParamApplic, long idStrut,
	    long idAmbiente, long idAaTipoFascicolo) {
	return getParamApplicValue(nmParamApplic, idStrut, idAmbiente, Integer.MIN_VALUE,
		idAaTipoFascicolo, TipoAplVGetValAppart.AATIPOFASCICOLO);
    }

    private static final String APLVGETVALPARAMBYCOL = "AplVGetvalParamByCol";
    private static final String APLVGETVALPARAMBY = "AplVGetvalParamBy";
    private static final String FLAPLPARAMAPPLICAPPART = "flAplParamApplicAppart";
    private static final String IDAPLVGETVALPARAMBY = "idAplVGetvalParamBy";

    /**
     *
     * @param nmParamApplic        nome parametro
     * @param idStrut              id struttura
     * @param idAmbiente           id ambiente
     * @param idTipoUnitaDoc       id tipo unita doc
     * @param idAaTipoFascicolo    id anno tipo fascicolo
     * @param tipoAplVGetValAppart tipo valore {@link TipoAplVGetValAppart}
     *
     * @return Valore del parametro indicato secondo nome
     */
    private String getParamApplicValue(String nmParamApplic, long idStrut, long idAmbiente,
	    long idTipoUnitaDoc, long idAaTipoFascicolo,
	    TipoAplVGetValAppart tipoAplVGetValAppart) {

	long id = Integer.MIN_VALUE;// su questo id non troverò alcun elemento value sicuramente
				    // null
	List<AplVGetValParamDto> result = null;

	// base query (template)
	Map<String, String> queryData = new HashMap<>();
	String queryStr = null;

	// query template -> create DTO
	String queryStrTempl = "SELECT NEW it.eng.parer.util.ejb.help.dto.AplVGetValParamDto (${"
		+ APLVGETVALPARAMBYCOL + "}) " + "FROM AplParamApplic paramApplic, ${"
		+ APLVGETVALPARAMBY + "} getvalParam  "
		+ "WHERE paramApplic.nmParamApplic = :nmParamApplic "
		+ "AND getvalParam.nmParamApplic = paramApplic.nmParamApplic "
		+ "AND paramApplic.${" + FLAPLPARAMAPPLICAPPART + "} = :flAppart ${"
		+ IDAPLVGETVALPARAMBY + "} ";

	// tipo appartenenza
	TiAppart tiAppart = null;

	switch (tipoAplVGetValAppart) {
	case AATIPOFASCICOLO:
	    //
	    id = idAaTipoFascicolo;
	    //
	    tiAppart = TiAppart.PERIODO_TIPO_FASC;
	    //
	    queryData.put(APLVGETVALPARAMBYCOL,
		    "getvalParam.dsValoreParamApplic, getvalParam.tiAppart");
	    queryData.put(APLVGETVALPARAMBY, "AplVGetvalParamByAatifasc");
	    queryData.put(FLAPLPARAMAPPLICAPPART, "flAppartAaTipoFascicolo");
	    queryData.put(IDAPLVGETVALPARAMBY, "AND getvalParam.idAaTipoFascicolo = :id");
	    // replace
	    queryStr = StringSubstitutor.replace(queryStrTempl, queryData);
	    break;
	case TIPOUNITADOC:
	    //
	    id = idTipoUnitaDoc;
	    //
	    tiAppart = TiAppart.TIPO_UNITA_DOC;
	    //
	    queryData.put(APLVGETVALPARAMBYCOL,
		    "getvalParam.dsValoreParamApplic, getvalParam.tiAppart");
	    queryData.put(APLVGETVALPARAMBY, "AplVGetvalParamByTiud");
	    queryData.put(FLAPLPARAMAPPLICAPPART, "flAppartTipoUnitaDoc");
	    queryData.put(IDAPLVGETVALPARAMBY, "AND getvalParam.idTipoUnitaDoc = :id");
	    // replace
	    queryStr = StringSubstitutor.replace(queryStrTempl, queryData);
	    break;
	case STRUT:
	    //
	    id = idStrut;
	    //
	    tiAppart = TiAppart.STRUT;
	    //
	    queryData.put(APLVGETVALPARAMBYCOL,
		    "getvalParam.dsValoreParamApplic, getvalParam.tiAppart");
	    queryData.put(APLVGETVALPARAMBY, "AplVGetvalParamByStrut");
	    queryData.put(FLAPLPARAMAPPLICAPPART, "flAppartStrut");
	    queryData.put(IDAPLVGETVALPARAMBY, "AND getvalParam.idStrut = :id");
	    // replace
	    queryStr = StringSubstitutor.replace(queryStrTempl, queryData);
	    break;
	case AMBIENTE:
	    //
	    id = idAmbiente;
	    //
	    tiAppart = TiAppart.AMBIENTE;
	    //
	    queryData.put(APLVGETVALPARAMBYCOL,
		    "getvalParam.dsValoreParamApplic, getvalParam.tiAppart");
	    queryData.put(APLVGETVALPARAMBY, "AplVGetvalParamByAmb");
	    queryData.put(FLAPLPARAMAPPLICAPPART, "flAppartAmbiente");
	    queryData.put(IDAPLVGETVALPARAMBY, "AND getvalParam.idAmbiente = :id");
	    // replace
	    queryStr = StringSubstitutor.replace(queryStrTempl, queryData);
	    break;
	default:
	    //
	    tiAppart = TiAppart.APPLIC;
	    //
	    queryData.put(APLVGETVALPARAMBYCOL, "getvalParam.dsValoreParamApplic");
	    queryData.put(APLVGETVALPARAMBY, "AplVGetvalParamByApl");
	    queryData.put(FLAPLPARAMAPPLICAPPART, "flAppartApplic");
	    queryData.put(IDAPLVGETVALPARAMBY, "");
	    // replace
	    queryStr = StringSubstitutor.replace(queryStrTempl, queryData);
	    break;
	}

	try {
	    TypedQuery<AplVGetValParamDto> query = entityManager.createQuery(queryStr,
		    AplVGetValParamDto.class);
	    query.setParameter("nmParamApplic", nmParamApplic);
	    query.setParameter("flAppart", "1");// fixed
	    // solo nel caso in cui contenga la condition sull'ID
	    if (StringUtils.isNotBlank(queryData.get(IDAPLVGETVALPARAMBY))) {
		query.setParameter("id", new BigDecimal(id));
	    }
	    // get result
	    result = query.getResultList();
	} catch (Exception e) {
	    // thorws Exception
	    final String msg = "Errore nella lettura del parametro " + nmParamApplic;
	    log.error(msg);
	    throw new ParamApplicNotFoundException(msg, nmParamApplic);
	}

	if (result != null && !result.isEmpty()) {
	    /*
	     * if more than one ....
	     */
	    if (result.size() > 1) {
		/*
		 * Ordine / Priorità TiAppart idAaTipoFascicolo -> idTipoUnitaDoc -> idStrut ->
		 * idAmbiente -> applicazione
		 */
		// filter by getTiAppart
		return getDsValoreParamApplicByTiAppart(nmParamApplic, result, tiAppart);
	    } else {
		return result.get(0).getDsValoreParamApplic(); // one is expected
	    }
	} else if (ParametroApplDB.TipoAplVGetValAppart.next(tipoAplVGetValAppart) != null) {
	    /*
	     * Ordine / Priorità Viste idAaTipoFascicolo -> idTipoUnitaDoc -> idStrut -> idAmbiente
	     * -> applicazione
	     */
	    return getParamApplicValue(nmParamApplic, idStrut, idAmbiente, idTipoUnitaDoc,
		    idAaTipoFascicolo,
		    ParametroApplDB.TipoAplVGetValAppart.next(tipoAplVGetValAppart));
	} else {
	    // thorws Exception
	    final String msg = "Parametro " + nmParamApplic + " non definito o non valorizzato";
	    log.error(msg);
	    throw new ParamApplicNotFoundException(msg, nmParamApplic);
	}
    }

    private String getDsValoreParamApplicByTiAppart(String nmParamApplic,
	    List<AplVGetValParamDto> result, final TiAppart tiAppart) {
	// get entity from list
	List<AplVGetValParamDto> resultFiltered = result.stream()
		.filter(e -> e.getTiAppart() != null && e.getTiAppart().equals(tiAppart.name()))
		.collect(Collectors.toList());

	/* questa condizione non dovrebbe mai verificarsi */
	if (tiAppart.name().equals(TiAppart.APPLIC.name())
		&& (resultFiltered == null || resultFiltered.isEmpty())) {
	    // thorws Exception
	    final String msg = "Parametro " + nmParamApplic + " non definito o non valorizzato";
	    log.error(msg);
	    throw new ParamApplicNotFoundException(msg, nmParamApplic);
	}

	if (resultFiltered == null || resultFiltered.isEmpty()) {
	    TiAppart nextTiAppart = null;
	    switch (tiAppart) {
	    case PERIODO_TIPO_FASC:
		nextTiAppart = TiAppart.STRUT;
		break;
	    case TIPO_UNITA_DOC:
		nextTiAppart = TiAppart.STRUT;
		break;
	    case STRUT:
		nextTiAppart = TiAppart.AMBIENTE;
		break;
	    default:
		nextTiAppart = TiAppart.APPLIC;
		break;
	    }
	    return getDsValoreParamApplicByTiAppart(nmParamApplic, result, nextTiAppart);
	} else {
	    return resultFiltered.get(0).getDsValoreParamApplic();// expected one
	}

    }

    public Map<String, String> getValoreParamApplicByTiParamApplicAsMap(
	    List<String> tiParamApplics) {
	Map<String, String> config = new HashMap<>();

	String queryStr = "select tpa " + "from AplParamApplic tpa "
		+ "where tpa.tiParamApplic IN :tiParamApplic ";

	Query query = entityManager.createQuery(queryStr);
	query.setParameter("tiParamApplic", tiParamApplics);

	List<AplParamApplic> result = query.getResultList();

	for (AplParamApplic cfg : result) {
	    config.put(cfg.getNmParamApplic(),
		    me.getValoreParamApplicByApplic(cfg.getNmParamApplic()));
	}

	return config;
    }

    /**
     *
     * Gestione FLAG true = 1, false = 0
     *
     * @param nmParamApplic nome parametro applicativo
     *
     * @return Valore del flag indicato secondo nome
     */
    public String getValoreParamApplicByApplicAsFl(String nmParamApplic) {
	return FlagConverter.fromBoolToFl(me.getValoreParamApplicByApplic(nmParamApplic));
    }

    /**
     *
     * Gestione FLAG true = 1, false = 0
     *
     * @param nmParamApplic  nome parametro applicativo
     * @param idStrut        id struttura
     * @param idAmbiente     id ambiente
     * @param idTipoUnitaDoc id tipo unita doc
     *
     * @return Valore del flag indicato secondo nome
     */
    public String getValoreParamApplicByTipoUdAsFl(String nmParamApplic, long idStrut,
	    long idAmbiente, long idTipoUnitaDoc) {
	return FlagConverter.fromBoolToFl(me.getValoreParamApplicByTipoUd(nmParamApplic, idStrut,
		idAmbiente, idTipoUnitaDoc));
    }

    /**
     *
     * Gestione FLAG true = 1, false = 0
     *
     * @param nmParamApplic     nome parametro applicativo
     * @param idStrut           id struttura
     * @param idAmbiente        id ambiente
     * @param idAaTipoFascicolo id anno tipo fascicolo
     *
     * @return Valore del flag indicato secondo nome
     */
    public String getValoreParamApplicByAaTipoFascAsFl(String nmParamApplic, long idStrut,
	    long idAmbiente, long idAaTipoFascicolo) {
	return FlagConverter.fromBoolToFl(me.getValoreParamApplicByAaTipoFasc(nmParamApplic,
		idStrut, idAmbiente, idAaTipoFascicolo));
    }
}
