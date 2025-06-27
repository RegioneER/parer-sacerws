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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the ARO_VERIF_FIRMA_DT_VERS database table.
 */
@Entity
@Table(name = "ARO_VERIF_FIRMA_DT_VERS")
public class AroVerifFirmaDtVer implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idVerifFirmaDtVers;

    private String dsMsgEsitoVerifFirma;

    private String tiEsitoVerifFirma;

    private AroFirmaComp aroFirmaComp;

    public AroVerifFirmaDtVer() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_VERIF_FIRMA_DT_VERS")
    @GenericGenerator(name = "SARO_VERIF_FIRMA_DT_VERS_ID_VERIF_FIRMA_DT_VERS_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_VERIF_FIRMA_DT_VERS"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_VERIF_FIRMA_DT_VERS_ID_VERIF_FIRMA_DT_VERS_GENERATOR")
    public Long getIdVerifFirmaDtVers() {
	return this.idVerifFirmaDtVers;
    }

    public void setIdVerifFirmaDtVers(Long idVerifFirmaDtVers) {
	this.idVerifFirmaDtVers = idVerifFirmaDtVers;
    }

    @Column(name = "DS_MSG_ESITO_VERIF_FIRMA")
    public String getDsMsgEsitoVerifFirma() {
	return this.dsMsgEsitoVerifFirma;
    }

    public void setDsMsgEsitoVerifFirma(String dsMsgEsitoVerifFirma) {
	this.dsMsgEsitoVerifFirma = dsMsgEsitoVerifFirma;
    }

    @Column(name = "TI_ESITO_VERIF_FIRMA")
    public String getTiEsitoVerifFirma() {
	return this.tiEsitoVerifFirma;
    }

    public void setTiEsitoVerifFirma(String tiEsitoVerifFirma) {
	this.tiEsitoVerifFirma = tiEsitoVerifFirma;
    }

    // bi-directional many-to-one association to AroFirmaComp
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FIRMA_COMP")
    public AroFirmaComp getAroFirmaComp() {
	return this.aroFirmaComp;
    }

    public void setAroFirmaComp(AroFirmaComp aroFirmaComp) {
	this.aroFirmaComp = aroFirmaComp;
    }
}
