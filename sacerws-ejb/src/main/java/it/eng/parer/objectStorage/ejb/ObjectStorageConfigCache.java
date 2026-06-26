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

package it.eng.parer.objectStorage.ejb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;

import it.eng.parer.objectStorage.dto.BackendStorage;
import it.eng.parer.objectStorage.dto.ObjectStorageBackend;

/**
 * Cache lock-free per configurazioni di backend e object storage.
 *
 * I dati di configurazione (bucket, credenziali, tipo backend) sono stabili per tutta la vita
 * dell'applicazione: caricarli dal DB una sola volta riduce i round-trip su ogni operazione di
 * versamento.
 *
 * <p>
 * Design: il Singleton non contiene EntityManager. Il caricamento dal DB avviene nel bean chiamante
 * ({@code SalvataggioBackendHelper}, che è {@code @Stateless} e ha un EM thread-safe); il risultato
 * viene depositato qui con {@link ConcurrentHashMap#putIfAbsent}. In caso di race condition alla
 * prima chiamata su una chiave, al massimo due thread caricano lo stesso dato dal DB; tutte le
 * chiamate successive sono completamente lock-free.
 * </p>
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ObjectStorageConfigCache {

    private final Map<String, BackendStorage> backendCache = new ConcurrentHashMap<>();
    private final Map<String, ObjectStorageBackend> osConfigCache = new ConcurrentHashMap<>();
    // cache dell'entity DecBackend: evita query ripetute nei saveObjectStorageLink*
    private final Map<String, Long> backendIdCache = new ConcurrentHashMap<>();

    // --- BackendStorage ---

    public BackendStorage getBackend(String nomeBackend) {
        return backendCache.get(nomeBackend);
    }

    /*
     * Deposita il valore in cache solo se la chiave non è già presente (putIfAbsent). Restituisce
     * il valore effettivamente in cache dopo l'operazione.
     */
    public BackendStorage putBackendIfAbsent(String nomeBackend, BackendStorage value) {
        BackendStorage existing = backendCache.putIfAbsent(nomeBackend, value);
        return existing != null ? existing : value;
    }

    // --- ObjectStorageBackend ---

    public ObjectStorageBackend getOsConfig(String nomeBackend, String tipoUsoOs) {
        return osConfigCache.get(osKey(nomeBackend, tipoUsoOs));
    }

    public ObjectStorageBackend putOsConfigIfAbsent(String nomeBackend, String tipoUsoOs,
            ObjectStorageBackend value) {
        ObjectStorageBackend existing = osConfigCache.putIfAbsent(osKey(nomeBackend, tipoUsoOs),
                value);
        return existing != null ? existing : value;
    }

    // --- DecBackend id (PK) ---

    public Long getBackendId(String nomeBackend) {
        return backendIdCache.get(nomeBackend);
    }

    public void putBackendIdIfAbsent(String nomeBackend, Long id) {
        backendIdCache.putIfAbsent(nomeBackend, id);
    }

    private static String osKey(String nomeBackend, String tipoUsoOs) {
        return nomeBackend + "#" + tipoUsoOs;
    }
}
