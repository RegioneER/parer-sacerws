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
import java.math.BigDecimal;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the DEC_CONTROLLO_WS database table.
 */
@Entity
@Table(name = "DEC_CONTROLLO_WS")
@NamedQuery(name = "DecControlloWs.findByCdFamigliaControllo", query = "SELECT d FROM DecControlloWs d WHERE d.cdFamigliaControllo = :cdFamigliaControllo")
@NamedQuery(name = "DecControlloWs.findByCdControlloWs", query = "SELECT d FROM DecControlloWs d WHERE d.cdControlloWs = :cdControlloWs")
public class DecControlloWs implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idControlloWs;

    private String cdCategoriaControllo;

    private String cdControlloWs;

    private String cdFamigliaControllo;

    private String dsControlloWs;

    private BigDecimal niOrdControllo;

    public DecControlloWs() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_CONTROLLO_WS")
    @GenericGenerator(name = "SDEC_CONTROLLO_WS_ID_CONTROLLO_WS_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_CONTROLLO_WS"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_CONTROLLO_WS_ID_CONTROLLO_WS_GENERATOR")
    public Long getIdControlloWs() {
	return this.idControlloWs;
    }

    public void setIdControlloWs(Long idControlloWs) {
	this.idControlloWs = idControlloWs;
    }

    @Column(name = "CD_CATEGORIA_CONTROLLO")
    public String getCdCategoriaControllo() {
	return this.cdCategoriaControllo;
    }

    public void setCdCategoriaControllo(String cdCategoriaControllo) {
	this.cdCategoriaControllo = cdCategoriaControllo;
    }

    @Column(name = "CD_CONTROLLO_WS")
    public String getCdControlloWs() {
	return this.cdControlloWs;
    }

    public void setCdControlloWs(String cdControlloWs) {
	this.cdControlloWs = cdControlloWs;
    }

    @Column(name = "CD_FAMIGLIA_CONTROLLO")
    public String getCdFamigliaControllo() {
	return this.cdFamigliaControllo;
    }

    public void setCdFamigliaControllo(String cdFamigliaControllo) {
	this.cdFamigliaControllo = cdFamigliaControllo;
    }

    @Column(name = "DS_CONTROLLO_WS")
    public String getDsControlloWs() {
	return this.dsControlloWs;
    }

    public void setDsControlloWs(String dsControlloWs) {
	this.dsControlloWs = dsControlloWs;
    }

    @Column(name = "NI_ORD_CONTROLLO")
    public BigDecimal getNiOrdControllo() {
	return this.niOrdControllo;
    }

    public void setNiOrdControllo(BigDecimal niOrdControllo) {
	this.niOrdControllo = niOrdControllo;
    }

}
