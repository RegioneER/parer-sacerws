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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import it.eng.parer.entity.inheritance.oop.AroXmlObjectStorage;

@Entity
@Table(name = "ARO_XML_DOC_OBJECT_STORAGE")
public class AroXmlDocObjectStorage extends AroXmlObjectStorage {

    private static final long serialVersionUID = 1L;

    public AroXmlDocObjectStorage() {
        super();
    }

    private Long idDoc;
    private AroDoc aroDoc;

    @Id
    @Column(name = "ID_DOC")
    public Long getIdDoc() {
        return idDoc;
    }

    public void setIdDoc(Long idDoc) {
        this.idDoc = idDoc;
    }

    @MapsId
    @OneToOne(mappedBy = "aroXmlDocObjectStorage")
    @JoinColumn(name = "ID_DOC")
    public AroDoc getAroDoc() {
        return aroDoc;
    }

    public void setAroDoc(AroDoc aroDoc) {
        this.aroDoc = aroDoc;
    }
}
