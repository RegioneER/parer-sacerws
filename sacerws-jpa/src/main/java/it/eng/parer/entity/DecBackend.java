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

package it.eng.parer.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "DEC_BACKEND")
public class DecBackend implements Serializable {

    private static final long serialVersionUID = 37568565656845L;

    private Long idDecBackend;
    private String nmBackend;
    private String nmTipoBackend;
    private String dlBackendUri;
    private String dsDescrizioneBackend;
    private List<VrsFileSesObjectStorageKo> fileSesObjectStorageKos;
    private List<AroCompObjectStorage> aroCompObjectStorages;
    private List<DecConfigObjectStorage> decConfigObjectStorages;
    private List<FirReport> firReports;

    public DecBackend() {
        super();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DEC_BACKEND")
    public Long getIdDecBackend() {
        return idDecBackend;
    }

    public void setIdDecBackend(Long idDecBackend) {
        this.idDecBackend = idDecBackend;
    }

    @Column(name = "NM_BACKEND")
    public String getNmBackend() {
        return nmBackend;
    }

    public void setNmBackend(String nmBackend) {
        this.nmBackend = nmBackend;
    }

    @Column(name = "NM_TIPO_BACKEND")
    public String getNmTipoBackend() {
        return nmTipoBackend;
    }

    public void setNmTipoBackend(String nmTipoBackend) {
        this.nmTipoBackend = nmTipoBackend;
    }

    @Column(name = "DL_BACKEND_URI")
    public String getDlBackendUri() {
        return dlBackendUri;
    }

    public void setDlBackendUri(String dlBackendUri) {
        this.dlBackendUri = dlBackendUri;
    }

    @Column(name = "DS_DESCRIZIONE_BACKEND")
    public String getDsDescrizioneBackend() {
        return dsDescrizioneBackend;
    }

    public void setDsDescrizioneBackend(String dsDescrizioneBackend) {
        this.dsDescrizioneBackend = dsDescrizioneBackend;
    }

    @OneToMany(mappedBy = "decBackend")
    public List<VrsFileSesObjectStorageKo> getFileSesObjectStorages() {
        return fileSesObjectStorageKos;
    }

    public void setFileSesObjectStorages(List<VrsFileSesObjectStorageKo> fileSesObjectStorageKos) {
        this.fileSesObjectStorageKos = fileSesObjectStorageKos;
    }

    @OneToMany(mappedBy = "decBackend")
    public List<AroCompObjectStorage> getAroCompObjectStorages() {
        return aroCompObjectStorages;
    }

    public void setAroCompObjectStorages(List<AroCompObjectStorage> aroCompObjectStorages) {
        this.aroCompObjectStorages = aroCompObjectStorages;
    }

    @OneToMany(mappedBy = "decBackend")
    public List<DecConfigObjectStorage> getDecConfigObjectStorages() {
        return decConfigObjectStorages;
    }

    public void setDecConfigObjectStorages(List<DecConfigObjectStorage> decConfigObjectStorages) {
        this.decConfigObjectStorages = decConfigObjectStorages;
    }

    @OneToMany(mappedBy = "decBackend")
    public List<FirReport> getFirReports() {
        return firReports;
    }

    public void setFirReports(List<FirReport> firReports) {
        this.firReports = firReports;
    }

}
