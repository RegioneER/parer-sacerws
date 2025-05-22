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

package it.eng.parer.ws.versamento.dto;

import java.net.URI;

import it.eng.parer.ws.utils.ParametroApplDB;

/**
 * Informazioni per la risorsa salvata sull'object storage.
 *
 * @author Snidero_L
 */
public interface ObjectStorageResource {

    /**
     * Questo metodo esiste per ragioni storiche. Tecnicamente non serve a nulla
     *
     * @return il nome del tenant come indicato nel parametro applicativo
     *         {@link ParametroApplDB#TENANT_OBJECT_STORAGE}
     */
    String getTenant();

    String getBucket();

    String getKey();

    String getETag();

    /**
     * Scadenza della risorsa (nel caso ci siano lifecycle policy configurate sul bucket)
     *
     * @return scadenza della risorsa
     */
    String getExpiration();

    /**
     * Ottiene una URI utilizzabile per scaricare l'oggetto <strong>senza</strong> autenticazione
     * alcuna per un lasso di tempo limitato.
     *
     * @return URI da cui effettuare una HTTP GET
     */
    URI getPresignedURL();
}
