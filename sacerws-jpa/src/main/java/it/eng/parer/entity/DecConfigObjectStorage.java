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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "DEC_CONFIG_OBJECT_STORAGE")
public class DecConfigObjectStorage implements Serializable {

    private static final long serialVersionUID = 33456789043235L;

    private Long idDecConfigObjectStorage;
    private DecBackend decBackend;
    private String nmConfigObjectStorage;
    private String dsValoreConfigObjectStorage;
    private String tiUsoConfigObjectStorage;
    private String dsDescrizioneConfigObjectStorage;

    public DecConfigObjectStorage() {
        super();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DEC_CONFIG_OBJECT_STORAGE")
    public Long getIdDecConfigObjectStorage() {
        return idDecConfigObjectStorage;
    }

    public void setIdDecConfigObjectStorage(Long idDecConfigObjectStorage) {
        this.idDecConfigObjectStorage = idDecConfigObjectStorage;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DEC_BACKEND")
    public DecBackend getDecBackend() {
        return decBackend;
    }

    public void setDecBackend(DecBackend decBackend) {
        this.decBackend = decBackend;
    }

    /**
     * Attualmente questa colonna accetta solamente questi valori:
     * <ul>
     * <li>BUCKET</li>
     * <li>ACCESS_KEY_ID_SYS_PROP</li>
     * <li>SECRET_KEY_SYS_PROP</li>
     * </ul>
     *
     * @return a cosa si riferiesce questa configurazione dell'OS
     */
    @Column(name = "NM_CONFIG_OBJECT_STORAGE")
    public String getNmConfigObjectStorage() {
        return nmConfigObjectStorage;
    }

    public void setNmConfigObjectStorage(String nmConfigObjectStorage) {
        this.nmConfigObjectStorage = nmConfigObjectStorage;
    }

    @Column(name = "DS_VALORE_CONFIG_OBJECT_STORAGE")
    public String getDsValoreConfigObjectStorage() {
        return dsValoreConfigObjectStorage;
    }

    public void setDsValoreConfigObjectStorage(String dsValoreConfigObjectStorage) {
        this.dsValoreConfigObjectStorage = dsValoreConfigObjectStorage;
    }

    /**
     * Attualmente questa colonna accetta solamente questi valori:
     * <ul>
     * <li>STAGING</li>
     * <li>COMPONENTI</li>
     * <li>SIP</li>
     * <li>AIP</li>
     * </ul>
     *
     * @return di fatto il nome del bucket
     */
    @Column(name = "TI_USO_CONFIG_OBJECT_STORAGE")
    public String getTiUsoConfigObjectStorage() {
        return tiUsoConfigObjectStorage;
    }

    public void setTiUsoConfigObjectStorage(String tiUsoConfigObjectStorage) {
        this.tiUsoConfigObjectStorage = tiUsoConfigObjectStorage;
    }

    @Column(name = "DS_DESCRIZIONE_CONFIG_OBJECT_STORAGE")
    public String getDsDescrizioneConfigObjectStorage() {
        return dsDescrizioneConfigObjectStorage;
    }

    public void setDsDescrizioneConfigObjectStorage(String dsDescrizioneConfigObjectStorage) {
        this.dsDescrizioneConfigObjectStorage = dsDescrizioneConfigObjectStorage;
    }

}
