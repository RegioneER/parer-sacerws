/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.ws.versamento.dto;

import java.io.File;

/**
 *
 * @author Fioravanti_F
 */
public class FileBinario implements java.io.Serializable {

    private static final long serialVersionUID = 3038080614562309178L;
    private String id;
    private File fileSuDisco;
    private long dimensione;
    private String fileName;
    private transient ObjectStorageResource objectStorageResource;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the fileSuDisco
     */
    public File getFileSuDisco() {
        return fileSuDisco;
    }

    /**
     * @param fileSuDisco
     *            the fileSuDisco to set
     */
    public void setFileSuDisco(File fileSuDisco) {
        this.fileSuDisco = fileSuDisco;
    }

    /**
     * @return the dimensione
     */
    public long getDimensione() {
        return dimensione;
    }

    /**
     * @param dimensione
     *            the dimensione to set
     */
    public void setDimensione(long dimensione) {
        this.dimensione = dimensione;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ObjectStorageResource getObjectStorageResource() {
        return objectStorageResource;
    }

    public void setObjectStorageResource(ObjectStorageResource objectStorageResource) {
        this.objectStorageResource = objectStorageResource;
    }

}
