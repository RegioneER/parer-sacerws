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

package it.eng.parer.view_entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The persistent class for the ARO_V_DT_VERS_MAX_BY_UNITA_DOC database table.
 *
 */
@Entity
@Table(name = "ARO_V_DT_VERS_MAX_BY_UNITA_DOC")
public class AroVDtVersMaxByUnitaDoc implements Serializable {

    private static final long serialVersionUID = 1L;
    private BigDecimal idUnitaDoc;
    private Date dtVersMax;

    public AroVDtVersMaxByUnitaDoc(BigDecimal idUnitaDoc, Date dtVersMax) {
	this.idUnitaDoc = idUnitaDoc;
	this.dtVersMax = dtVersMax;
    }

    public AroVDtVersMaxByUnitaDoc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_UNITA_DOC")
    public BigDecimal getIdUnitaDoc() {
	return this.idUnitaDoc;
    }

    public void setIdUnitaDoc(BigDecimal idUnitaDoc) {
	this.idUnitaDoc = idUnitaDoc;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_VERS_MAX")
    public Date getDtVersMax() {
	return this.dtVersMax;
    }

    public void setDtVersMax(Date dtVersMax) {
	this.dtVersMax = dtVersMax;
    }

}
