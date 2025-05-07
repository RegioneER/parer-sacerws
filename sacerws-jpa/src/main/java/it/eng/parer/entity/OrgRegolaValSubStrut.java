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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlID;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the ORG_REGOLA_VAL_SUB_STRUT database table.
 */
@Entity
@Table(name = "ORG_REGOLA_VAL_SUB_STRUT")
public class OrgRegolaValSubStrut implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idRegolaValSubStrut;

    private Date dtIstituz;

    private Date dtSoppres;

    private List<OrgCampoValSubStrut> orgCampoValSubStruts = new ArrayList<>();

    private DecTipoDoc decTipoDoc;

    private DecTipoUnitaDoc decTipoUnitaDoc;

    public OrgRegolaValSubStrut() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_REGOLA_VAL_SUB_STRUT")
    @XmlID
    @GenericGenerator(name = "SORG_REGOLA_VAL_SUB_STRUT_ID_REGOLA_VAL_SUB_STRUT_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SORG_REGOLA_VAL_SUB_STRUT"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORG_REGOLA_VAL_SUB_STRUT_ID_REGOLA_VAL_SUB_STRUT_GENERATOR")
    public Long getIdRegolaValSubStrut() {
        return this.idRegolaValSubStrut;
    }

    public void setIdRegolaValSubStrut(Long idRegolaValSubStrut) {
        this.idRegolaValSubStrut = idRegolaValSubStrut;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_ISTITUZ")
    public Date getDtIstituz() {
        return this.dtIstituz;
    }

    public void setDtIstituz(Date dtIstituz) {
        this.dtIstituz = dtIstituz;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_SOPPRES")
    public Date getDtSoppres() {
        return this.dtSoppres;
    }

    public void setDtSoppres(Date dtSoppres) {
        this.dtSoppres = dtSoppres;
    }

    // bi-directional many-to-one association to OrgCampoValSubStrut
    @OneToMany(mappedBy = "orgRegolaValSubStrut", cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    public List<OrgCampoValSubStrut> getOrgCampoValSubStruts() {
        return this.orgCampoValSubStruts;
    }

    public void setOrgCampoValSubStruts(List<OrgCampoValSubStrut> orgCampoValSubStruts) {
        this.orgCampoValSubStruts = orgCampoValSubStruts;
    }

    // bi-directional many-to-one association to DecTipoDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_DOC")
    public DecTipoDoc getDecTipoDoc() {
        return this.decTipoDoc;
    }

    public void setDecTipoDoc(DecTipoDoc decTipoDoc) {
        this.decTipoDoc = decTipoDoc;
    }

    // bi-directional many-to-one association to DecTipoUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_UNITA_DOC")
    public DecTipoUnitaDoc getDecTipoUnitaDoc() {
        return this.decTipoUnitaDoc;
    }

    public void setDecTipoUnitaDoc(DecTipoUnitaDoc decTipoUnitaDoc) {
        this.decTipoUnitaDoc = decTipoUnitaDoc;
    }
}
