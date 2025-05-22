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

import java.io.Serializable;

/**
 * Backend su cui viene salvata la risorsa.
 *
 *
 * @author Snidero_L
 */
public interface BackendStorage extends Serializable {

    enum STORAGE_TYPE {
	OS, BLOB, FILE
    }

    STORAGE_TYPE getType();

    String getBackendName();

    default boolean isObjectStorage() {
	return STORAGE_TYPE.OS.equals(getType());
    }

    default boolean isDataBase() {
	return STORAGE_TYPE.BLOB.equals(getType());
    }

    default boolean isFile() {
	return STORAGE_TYPE.FILE.equals(getType());
    }

}
