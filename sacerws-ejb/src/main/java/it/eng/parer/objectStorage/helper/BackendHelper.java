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

package it.eng.parer.objectStorage.helper;

import static it.eng.parer.ws.utils.Costanti.WS_AGGIORNAMENTO_VERS_NOME;
import static it.eng.parer.ws.utils.Costanti.WS_AGGIUNTA_DOC_NOME;
import static it.eng.parer.ws.utils.Costanti.WS_VERSAMENTO_MM_NOME;
import static it.eng.parer.ws.utils.Costanti.WS_VERSAMENTO_NOME;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import it.eng.parer.entity.DecBackend;
import it.eng.parer.entity.DecTipoUnitaDoc;
import it.eng.parer.exception.ParamApplicNotFoundException;
import it.eng.parer.objectStorage.dto.BackendStorage;
import it.eng.parer.objectStorage.ejb.ObjectStorageConfigCache;
import it.eng.parer.objectStorage.exceptions.BackendException;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.utils.ParametroApplDB;

@Stateless(mappedName = "BackendHelper")
@LocalBean
public class BackendHelper {

    private static final String NO_PARAMETER = "Impossibile ottenere il parametro {0}";

    @EJB
    protected ConfigurationHelper configurationHelper;

    @EJB
    protected ObjectStorageConfigCache configCache;

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    public enum BACKEND_VERSAMENTO {
        DATABASE, OBJECT_STORAGE
    }

    /**
     * Ottieni la configurazione applicativa relativa alla tipologia di Backend per il salvataggio
     * in "staging"
     *
     * @return configurazione del backend. Può essere, per esempio OBJECT_STORAGE_STAGING oppure
     *         DATABASE_PRIMARIO
     *
     * @throws BackendException in caso di errore di recupero del parametro
     */
    public String getBackendStaging() throws BackendException {
        try {
            return configurationHelper
                    .getValoreParamApplicByApplic(ParametroApplDB.BACKEND_VRS_SES_UD_STAGING);

        } catch (ParamApplicNotFoundException | IllegalArgumentException e) {
            throw BackendException.builder()
                    .message(NO_PARAMETER, ParametroApplDB.BACKEND_VRS_SES_UD_STAGING).cause(e)
                    .build();
        }
    }

    // MEV#29276
    /**
     * Ottieni la configurazione applicativa relativa alla tipologia di Backend per il salvataggio
     * delle sessioni errate/fallite dell'aggiornamento metadati
     *
     * @return configurazione del backend. Può essere, per esempio OBJECT_STORAGE_STAGING oppure
     *         DATABASE_PRIMARIO
     *
     * @throws BackendException in caso di errore di recupero del parametro
     */
    public String getBackendSessioniErrKoAggMd() throws BackendException {
        try {
            return configurationHelper
                    .getValoreParamApplicByApplic(ParametroApplDB.BACKEND_XML_SES_AGG_MD_ERR_KO);

        } catch (ParamApplicNotFoundException | IllegalArgumentException e) {
            throw BackendException.builder()
                    .message(NO_PARAMETER, ParametroApplDB.BACKEND_XML_SES_AGG_MD_ERR_KO).cause(e)
                    .build();
        }
    }
    // end MEV#29276

    /**
     * Ottieni la tipologia di backend per servizio
     *
     * @param idTipoUnitaDoc id della tipologia dell'UD
     * @param nomeWs         nome del servizio
     *
     * @return configurazione del backend. Può essere, per esempio OBJECT_STORAGE_STAGING oppure
     *         DATABASE_PRIMARIO
     *
     * @throws BackendException in caso di errore
     */
    public String getBackendByServiceName(long idTipoUnitaDoc, String nomeWs)
            throws BackendException {
        try {

            final String backendDatiVersamento;
            switch (nomeWs) {
            case WS_VERSAMENTO_NOME:
                backendDatiVersamento = ParametroApplDB.BACKEND_VERSAMENTO_SYNC;
                break;
            case WS_AGGIUNTA_DOC_NOME:
                backendDatiVersamento = ParametroApplDB.BACKEND_AGGIUNTALLEGATI_SYNC;
                break;
            case WS_VERSAMENTO_MM_NOME:
                backendDatiVersamento = ParametroApplDB.BACKEND_VERSAMENTO_MULTIMEDIA;
                break;
            // MEV#29276
            case WS_AGGIORNAMENTO_VERS_NOME:
                backendDatiVersamento = ParametroApplDB.BACKEND_VERSAMENTO_AGG_MD;
                break;
            // end MEV#29276
            default:
                throw new IllegalArgumentException("Tipo creazione documento non supportato");
            }

            return getParameter(backendDatiVersamento, idTipoUnitaDoc);

        } catch (ParamApplicNotFoundException | IllegalArgumentException e) {
            throw BackendException.builder().message(
                    "Impossibile ottenere il parametro per id tipo unita doc {0} e nome servizio {1}",
                    idTipoUnitaDoc, nomeWs).cause(e).build();
        }
    }

    private String getParameter(String parameterName, long idTipoUnitaDoc) {
        DecTipoUnitaDoc tipoUd = entityManager.find(DecTipoUnitaDoc.class, idTipoUnitaDoc);
        long idStrut = tipoUd.getOrgStrut().getIdStrut();

        long idAmbiente = tipoUd.getOrgStrut().getOrgEnte().getOrgAmbiente().getIdAmbiente();

        return configurationHelper.getValoreParamApplicByTipoUd(parameterName, idStrut, idAmbiente,
                idTipoUnitaDoc);
    }

    public DecBackend getBackendEntity(String nomeBackend) {
        Long cachedId = configCache.getBackendId(nomeBackend);
        if (cachedId != null) {
            return entityManager.getReference(DecBackend.class, cachedId);
        }
        return loadBackendEntity(nomeBackend);
    }

    private DecBackend loadBackendEntity(String nomeBackend) {
        TypedQuery<DecBackend> query = entityManager.createQuery(
                "Select d from DecBackend d where d.nmBackend = :nomeBackend", DecBackend.class);
        query.setParameter("nomeBackend", nomeBackend);
        DecBackend backend = query.getSingleResult();
        configCache.putBackendIdIfAbsent(nomeBackend, backend.getIdDecBackend());
        return backend;
    }

    /**
     * Ottieni la configurazione del backend a partire dal nome del backend
     *
     * @param nomeBackend per esempio "OBJECT_STORAGE_PRIMARIO"
     *
     * @return Informazioni sul Backend identificato
     *
     * @throws BackendException in caso di errore
     */
    public BackendStorage getBackend(String nomeBackend) throws BackendException {
        BackendStorage cached = configCache.getBackend(nomeBackend);
        if (cached != null) {
            return cached;
        }
        try {
            DecBackend backend = loadBackendEntity(nomeBackend);
            final BackendStorage.STORAGE_TYPE type = BackendStorage.STORAGE_TYPE
                    .valueOf(backend.getNmTipoBackend());
            final String backendName = backend.getNmBackend();

            BackendStorage loaded = new BackendStorage() {
                private static final long serialVersionUID = 1L;

                @Override
                public BackendStorage.STORAGE_TYPE getType() {
                    return type;
                }

                @Override
                public String getBackendName() {
                    return backendName;
                }
            };
            return configCache.putBackendIfAbsent(nomeBackend, loaded);
        } catch (IllegalArgumentException | NonUniqueResultException e) {
            throw BackendException.builder()
                    .message("Impossibile ottenere le informazioni di backend").cause(e).build();
        }
    }

}
